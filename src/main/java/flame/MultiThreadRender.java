package flame;

import flame.imageTransformators.ColoredTransformation;
import flame.objects.*;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class MultiThreadRender implements Render {
    final static int MIN_X = -1;
    final static int MAX_X = 1;
    final static int MIN_Y = -1;
    final static int MAX_Y = 1;
    private final static int SKIP = 20;
    private final static int DEFAULT_NTHREADS = 6;
    private final int nthreads;

    public MultiThreadRender() {
        this(DEFAULT_NTHREADS);
    }

    public MultiThreadRender(int nthreads) {
        this.nthreads = nthreads;
    }

    private static Point scale(Point point, FractalImage image) {
        return new Point(
            ((point.x()) * image.width() / (MAX_X - MIN_X)),
            ((point.y()) * image.height() / (MAX_Y - MIN_Y))
        );
    }

    private static Point shift(Point point, FractalImage image) {
        return new Point(
            point.x() + (double) image.width() / 2,
            point.y() + (double) image.height() / 2
        );
    }

    private static Point rotate(Point point, double angle) {
        return new Point(
            point.x() * Math.cos(angle) - point.y() * Math.sin(angle),
            point.x() * Math.sin(angle) + point.y() * Math.cos(angle)
        );
    }

    private static void countMaxHint(FractalImage image, Rect rectToCount, AtomicInteger maxHit) {
        for (int x = rectToCount.x(); x < rectToCount.x() + rectToCount.width(); ++x) {
            for (int y = rectToCount.y(); y < rectToCount.y() + rectToCount.height(); ++y) {
                int oldValue = maxHit.get();
                while (image.pixel(x, y).getHitCount() > oldValue) {
                    maxHit.compareAndExchange(oldValue, image.pixel(x, y).getHitCount());
                    oldValue = maxHit.get();
                }
            }
        }
    }

    private static void gammaLogCore(FractalImage image, Rect rectToProcess, int maxHit, double gamma) {
        double maxHitLog = Math.log10(maxHit);
        for (int x = rectToProcess.x(); x < rectToProcess.x() + rectToProcess.width(); ++x) {
            for (int y = rectToProcess.y(); y < rectToProcess.y() + rectToProcess.height(); ++y) {
                Pixel pixel = image.pixel(x, y);
                if (pixel.getHitCount() != 0) {
                    double alpha = Math.min(1, Math.log10(pixel.getHitCount()) / maxHitLog);
                    alpha = Math.pow(alpha, 1 / gamma);
                    pixel.getColor().multiply(alpha);
                } else {
                    pixel.setColor(new Color(0, 0, 0));
                }
            }
        }
    }

    public static void reduceCore(FractalImage original, FractalImage reduced, Rect coveredRect) {
        int scaleX = original.width() / reduced.width();
        int scaleY = original.height() / reduced.height();
        Rect.RectIterator iter = coveredRect.iterator();
        while (iter.hasNext()) {
            Point point = iter.next();
            int x = (int) point.x();
            int y = (int) point.y();
            Pixel reducedPixel = reduced.pixel(x, y);
            int cnt = 0;
            for (int neighbourX = scaleX * x - scaleX / 2; neighbourX <= scaleX * x + scaleX / 2; ++neighbourX) {
                for (int neighbourY = scaleY * y - scaleY / 2; neighbourY <= scaleY * y + scaleY / 2;
                     ++neighbourY) {
                    if (original.contains(neighbourX, neighbourY)) {
                        Pixel neighbour = original.pixel(neighbourX, neighbourY);
                        if (neighbour.getHitCount() != 0) {
                            reducedPixel.getColor().add(neighbour.getColor());
                        }
                        ++cnt;
                        reducedPixel.addCount(neighbour.getHitCount());
                    }
                }
            }
            reducedPixel.getColor().divide(cnt);
        }
    }

    @Override
    public void generate(
        WorldInfo worldInfo, ProcessInfo processInfo
    ) {
        for (int worldIndex = 0; worldIndex < processInfo.samples(); ++worldIndex) {
            Point currentPoint = worldInfo.generator().generate();
            for (long step = -SKIP; step < processInfo.iterPerSample(); ++step) {
                ColoredTransformation transformation = worldInfo.variations().get();
                currentPoint = transformation.apply(currentPoint);
                if (step > 0) {
                    if (step == 1) {
                        currentPoint = new Point(0, 0);
                    }
                    for (double angle = 0; angle < 2 * Math.PI; angle += 2 * Math.PI / processInfo.symmetry()) {
                        Point scaledPoint =
                            shift(rotate(scale(currentPoint, worldInfo.canvas()), angle), worldInfo.canvas());
                        if (worldInfo.world().contains(scaledPoint)) {

                            Pixel pixel = worldInfo.canvas().pixel((int) scaledPoint.x(), (int) scaledPoint.y());
                            Color color = transformation.getColor();
                            synchronized (pixel) {
                                pixel.getColor().add(color);
                                pixel.getColor().divide(2);
                                pixel.addCount();
                            }
                        }
                    }

                }
            }
        }
    }

    @Override
    public void gammaLog(FractalImage image, double gamma) {
        AtomicInteger maxHit = new AtomicInteger(0);
        ExecutorService pool = Executors.newFixedThreadPool(nthreads);
        int delta = (image.width() + nthreads - 1) / nthreads;
        for (int startX = 0; startX < image.width(); startX += delta) {
            if (startX + delta > image.width()) {
                delta = image.width() - startX;
            }
            Rect coveredRect = new Rect(startX, 0, delta, image.height());
            pool.execute(() -> countMaxHint(image, coveredRect, maxHit));
        }
        pool.close();
        pool = Executors.newFixedThreadPool(nthreads);
        delta = (image.width() + nthreads - 1) / nthreads;
        for (int startX = 0; startX < image.width(); startX += delta) {
            if (startX + delta > image.width()) {
                delta = image.width() - startX;
            }
            Rect coveredRect = new Rect(startX, 0, delta, image.height());
            pool.execute(() -> gammaLogCore(image, coveredRect, maxHit.get(), gamma));
        }
        pool.close();
    }

    public void reduce(FractalImage original, FractalImage reduced) {
        ExecutorService pool = Executors.newFixedThreadPool(nthreads);
        int delta = (reduced.width() + nthreads - 1) / nthreads;
        for (int startX = 0; startX < reduced.width(); startX += delta) {
            if (startX + delta > reduced.width()) {
                delta = reduced.width() - startX;
            }
            Rect coveredRect = new Rect(startX, 0, delta, reduced.height());
            pool.execute(() -> reduceCore(original, reduced, coveredRect));
        }
        pool.close();

    }

    @Override
    public void render(
        WorldInfo worldInfo, ProcessInfo processInfo
    ) {
        FractalImage dirtyImage = FractalImage.create(
            worldInfo.canvas().width() * processInfo.filterScale(),
            worldInfo.canvas().height() * processInfo.filterScale()
        );
        Rect dirtyImageWorld = new Rect(
            worldInfo.world().x() * processInfo.filterScale(),
            worldInfo.world().y() * processInfo.filterScale(),
            worldInfo.world().width() * processInfo.filterScale(),
            worldInfo.world().height() * processInfo.filterScale()
        );
        ExecutorService executor = Executors.newFixedThreadPool(nthreads);
        ProcessInfo oneThreadProcessInfo = new ProcessInfo(1, processInfo.iterPerSample(), processInfo.symmetry());
        WorldInfo oneThreadWorldInfo = new WorldInfo(dirtyImage, dirtyImageWorld, worldInfo.generator(),
            worldInfo.variations()
        );
        for (int i = 0; i < processInfo.samples(); ++i) {
            executor.execute(() -> generate(
                oneThreadWorldInfo, oneThreadProcessInfo
            ));
        }
        executor.close();
        gammaLog(dirtyImage, processInfo.gamma());
        reduce(dirtyImage, worldInfo.canvas());
    }
}

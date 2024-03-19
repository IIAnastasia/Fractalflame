package flame;

import flame.imageTransformators.ColoredTransformation;
import flame.objects.*;

// Во многом идет дублирование кода MultiThreadRender,
// но не выношу общие части в отдельные функции,
// так как по логике OneThreadRender не существует
// Сейчас не реализую однопоточную версию через MultiThreadRender,
// для более честного сравнения времени работы
public class OneThreadRender implements Render {
    final static int MIN_X = -1;
    final static int MAX_X = 1;
    final static int MIN_Y = -1;
    final static int MAX_Y = 1;
    private final static int SKIP = 20;

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

    @Override
    public void generate(
        WorldInfo worldInfo,
        ProcessInfo processInfo
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
                            pixel.getColor().add(color);
                            pixel.getColor().divide(2);
                            pixel.addCount();

                        }
                    }

                }
            }
        }
    }

    @Override
    public void gammaLog(FractalImage image, double gamma) {
        int maxHit = 0;
        for (int x = 0; x < image.width(); ++x) {
            for (int y = 0; y < image.height(); ++y) {
                if (image.pixel(x, y).getHitCount() > maxHit) {
                    maxHit = image.pixel(x, y).getHitCount();
                }
            }
        }
        double maxHitLog = Math.log10(maxHit);
        for (int x = 0; x < image.width(); ++x) {
            for (int y = 0; y < image.height(); ++y) {
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

    @Override
    public void reduce(FractalImage original, FractalImage reduced) {
        int scaleX = original.width() / reduced.width();
        int scaleY = original.height() / reduced.height();
        int x = 0;
        int y = 0;
        while (y < reduced.height() && x < reduced.width()) {
            Pixel pixel = reduced.pixel(x, y);
            int neighbourCnt = 0;
            for (int neighbourX = x * scaleX - scaleX / 2; neighbourX <= x * scaleX + scaleX / 2; ++neighbourX) {
                for (int neighbourY = y * scaleY - scaleY / 2; neighbourY <= y * scaleY + scaleX / 2;
                     ++neighbourY) {
                    if (original.contains(neighbourX, neighbourY)) {
                        Pixel neighbour = original.pixel(neighbourX, neighbourY);
                        if (neighbour.getHitCount() != 0) {
                            pixel.getColor().add(neighbour.getColor());
                        }
                        ++neighbourCnt;
                        pixel.addCount(neighbour.getHitCount());
                    }
                }
            }
            pixel.getColor().divide(neighbourCnt);
            ++x;
            if (x == reduced.width()) {
                x = 0;
                ++y;
            }
        }
    }

    @Override
    public void render(
        WorldInfo worldInfo,
        ProcessInfo processInfo
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
        WorldInfo dirtyWorldInfo = new WorldInfo(dirtyImage, dirtyImageWorld, worldInfo.generator(),
            worldInfo.variations()
        );
        generate(dirtyWorldInfo, processInfo);
        gammaLog(dirtyImage, processInfo.gamma());
        reduce(dirtyImage, worldInfo.canvas());
    }
}

package flame;

import flame.generators.ListVariationStorage;
import flame.generators.ThreadLocalGenerator;
import flame.imageTransformators.ColoredTransformation;
import flame.imageTransformators.TransformFactory;
import flame.imageTransformators.Transformation;
import flame.objects.*;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Function;

public class GenerateBeautiful {
    private static final int WIDTH = 1920;
    private static final int HEIGHT = 1080;
    private static final int LINEAR = 0;
    private static final int SINUSOIDAL = 1;
    private static final int SPHERICAL = 2;
    private static final int HANDKERCHIEF = 3;
    private static final int SWIRL = 4;
    private static final int DIAMOND = 5;
    private static final Function<Point, Double> rSquareFunc = point -> point.x() * point.x() + point.y() * point.y();
    private static final List<Transformation> basicVars = List.of(
        point -> point,
        point -> new Point(Math.sin(point.x()), Math.sin(point.y())),
        point -> {
            double rSquare = rSquareFunc.apply(point);
            if (Math.abs(rSquare) < 0.001) {
                rSquare = 0.001;
            }
            return new Point(point.x() / rSquare, point.y() / rSquare);
        },
        point -> {
            double r = Math.sqrt(rSquareFunc.apply(point));
            double theta;
            if (point.y() == 0) {
                theta = Math.signum(point.x()) * Math.PI / 2;
            } else {
                theta = Math.atan(point.x() / point.y());
            }
            return new Point(r * Math.sin(theta + r), r * Math.cos(theta - r));
        },

        point -> {
            double rSquare = rSquareFunc.apply(point);
            return new Point(
                point.x() * Math.sin(rSquare) - point.y() * Math.cos(rSquare),
                point.x() * Math.cos(rSquare) + point.y() * Math.sin(rSquare)
            );

        },
        point -> {
            double r = Math.sqrt(rSquareFunc.apply(point));
            double theta;
            if (point.y() == 0) {
                theta = Math.signum(point.x()) * Math.PI / 2;
            } else {
                theta = Math.atan(point.x() / point.y());
            }
            return new Point(
                Math.sin(theta) * Math.cos(r),
                Math.cos(theta) * Math.sin(r)
            );
        }
    );

    private static final List<Transformation> transformations = List.of(
        TransformFactory.linear(0.427, 0.632, -0.195, 0.165, -1.190, 0.677),
        TransformFactory.linear(-0.271, 1.379, 0.419, 1.131, 0.044, -0.393),
        TransformFactory.linear(
            0.3455459239497958,
            -0.5202187531021916,
            -1.6671475914799525,
            -0.17256699902596573,
            -0.009051319883357056,
            -1.0150699266200758
        ),
        TransformFactory.linear(

            -0.6247144534481976,
            -0.7341720818130808,
            -1.8050259310869978,
            -0.2664091962017765,
            0.48626970190179897,
            -1.6014113750290138
        ),
        TransformFactory.linear(-1.231, 0.845, 0.299, 1.236, -0.887, 0.443),
        TransformFactory.linear(1.259, 0.598, 0.390, -0.258, -0.035, -1.493)
    );

    private final static List<Color> colors = List.of(
        new Color(0.29, 0.41, 0.89),
        new Color(0.4, 0.94, 0.86),
        new Color(0.83, 0.09, 0.75),
        new Color(0.93, 0.26, 0.42),
        new Color(1, 0.81, 0),
        new Color(1, 0.62, 0)
    );
    private final static ListVariationStorage variations1 = new ListVariationStorage(
        List.of(
            new ColoredTransformation(basicVars.get(1).compose(transformations.get(1)), colors.get(0)),
            new ColoredTransformation(basicVars.get(2).compose(transformations.get(1)), colors.get(1))
        )
    );
    private final static ListVariationStorage variations2 = new ListVariationStorage(
        List.of(
            new ColoredTransformation(basicVars.get(3).compose(transformations.get(0)), colors.get(4)),
            new ColoredTransformation(basicVars.get(3).compose(transformations.get(1)), colors.get(5))
        )
    );
    private final static ListVariationStorage variations3 = new ListVariationStorage(
        List.of(
            new ColoredTransformation(basicVars.get(SINUSOIDAL).compose(
                transformations.get(0)), colors.get(0)),
            new ColoredTransformation(
                basicVars.get(SPHERICAL).compose(
                    transformations.get(1)),
                colors.get(1)
            ),
            new ColoredTransformation(
                basicVars.get(LINEAR).compose(
                    transformations.get(2)),
                colors.get(2)
            ),
            new ColoredTransformation(
                basicVars.get(SWIRL).compose(
                    transformations.get(3)),
                colors.get(3)
            )
        ));

    public static void main(String[] args) throws IOException {
        generate();
    }
    static void generate() throws IOException {
        FractalImage image = FractalImage.create(WIDTH, HEIGHT);
        Render multiThread = new MultiThreadRender();
        multiThread.render(new WorldInfo(image,
            new Rect(0, 0, image.width(), image.height()),
            new ThreadLocalGenerator(),
            variations1),
            new ProcessInfo(
                100000,
            1000,
            6,
                5,
                2.2)
        );

        ImageUtils.save(image, Path.of("src/main/resources/pic1.jpg"), ImageFormat.JPEG);
        image = FractalImage.create(WIDTH, HEIGHT);
        multiThread.render(new WorldInfo(image,
            new Rect(0, 0, image.width(), image.height()),
            new ThreadLocalGenerator(),
            variations2),
            new ProcessInfo(
                100000,
                1000,
            11)
        );
        ImageUtils.save(image, Path.of("src/main/resources/pic2.jpg"), ImageFormat.JPEG);

        image = FractalImage.create(WIDTH, HEIGHT);
        multiThread.render(new WorldInfo(image,
            new Rect(0, 0, image.width(), image.height()),
            new ThreadLocalGenerator(),
            variations3),
            new ProcessInfo(
            100000,
            1000,
            5, 5, 3)
        );
        ImageUtils.save(image, Path.of("src/main/resources/pic3.jpg"), ImageFormat.JPEG);
    }
}

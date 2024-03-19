package flame;

import flame.generators.ListVariationStorage;
import flame.generators.ThreadLocalGenerator;
import flame.imageTransformators.ColoredTransformation;
import flame.imageTransformators.TransformFactory;
import flame.imageTransformators.Transformation;
import flame.objects.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

public class TestProject4 {
    static List<Transformation> transformations = List.of(
        TransformFactory.linear(0.427,0.632,-0.195,0.165,-1.190,0.677),
        TransformFactory.linear(-0.271,1.379,0.419,1.131,0.044,-0.393)
    );
    static List<Transformation> functions = new ArrayList<>(List.of(
        point -> new Point(Math.sin(point.x()), Math.sin(point.y()))
        ,
        point -> {
            double rSquare = point.x()*point.x() + point.y() * point.y();
            if (Math.abs(rSquare) < 0.001) {
                rSquare = 0.001;
            }
            return new Point(point.x() / rSquare, point.y() / rSquare);
        }
    )
    );

    static List<List<Color>> colors = List.of(
        List.of(new Color(0.99, 0, 0), new Color(0, 0.8, 0)),
        List.of(new Color(1,1,0), new Color(0.44,0.04,0.67)),
        List.of(new Color(0.82, 0.0, 0.43), new Color(1, 0.57, 0.41))
    );

    static List<ColoredTransformation> transformationList = new ArrayList<>();
    static {
        for (int i = 0; i < transformations.size(); ++i) {
            for (int j = 0; j < functions.size(); ++j) {
                Transformation transformation = transformations.get(i);
                Transformation function = functions.get(j);
                Color color = colors.get(j).get(i);
                transformationList.add(new ColoredTransformation(
                    point -> function.apply(transformation.apply(point)),
                    color
                ));
            }
        }
    }

    // На моем устройстве 2 минуты 29 секунд
    @Test
    void oneThread() {
        FractalImage image = FractalImage.create(1920, 1080);
        Render oneThread = new OneThreadRender();
        oneThread.render(new WorldInfo(image,
            new Rect(0, 0, image.width(), image.height()),
            new ThreadLocalGenerator(),
            new ListVariationStorage(transformationList)),
            new ProcessInfo(
            1000000,
            100,
            5)
        );
    }

    // на моем устройстве 28 секунд. Ускорение в 5 раз
    @Test
    void multiThreads() {
        FractalImage image = FractalImage.create(1920, 1080);
        Render multiThread = new MultiThreadRender();
        multiThread.render(new WorldInfo(image,
            new Rect(0, 0, image.width(), image.height()),
            new ThreadLocalGenerator(),
            new ListVariationStorage(transformationList)),
            new ProcessInfo(
            1000000,
            100,
            5)
        );
    }


    @Test
    void adequacyTest() {
        for (ColoredTransformation transformation : transformationList) {
            transformation.getColor().multiply(0);
            transformation.getColor().add(new Color(1, 0, 0));
        }
        FractalImage image = FractalImage.create(1920, 1080);
        Render multiThread = new MultiThreadRender();
        multiThread.render(new WorldInfo(image,
            new Rect(0, 0, image.width(), image.height()),
            new ThreadLocalGenerator(),
            new ListVariationStorage(transformationList)),
            new ProcessInfo(
            1000000,
            100,
            5)
        );
        int brightCount = 0;
        for (int x = 0; x < image.width(); ++x) {
            for (int y = 0; y < image.height(); ++y) {
                Color color = image.pixel(x, y).getColor();
                Assertions.assertTrue(color.getG() < 0.01);
                Assertions.assertTrue(color.getB() < 0.01);

                if (image.pixel(x, y).getColor().getR() > 0.4) {
                    ++brightCount;
                }
            }
        }
       Assertions.assertTrue((double)brightCount / (image.height() * image.width()) > 0.05);
    }


}

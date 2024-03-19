package flame;

import flame.objects.FractalImage;
import flame.objects.ImageFormat;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public final class ImageUtils {
    private ImageUtils() {
    }

    static void save(FractalImage image, Path filename, ImageFormat format) throws IOException {
        BufferedImage bufferedImage = new BufferedImage(
            image.width(),
            image.height(),
            BufferedImage.TYPE_INT_RGB
        );
        for (int i = 0; i < image.width(); ++i) {
            for (int j = 0; j < image.height(); ++j) {
                bufferedImage.setRGB(i, j, image.pixel(i, j).getColor().asInt());
            }
        }
        try (var outputStream = Files.newOutputStream(filename, StandardOpenOption.CREATE,
            StandardOpenOption.TRUNCATE_EXISTING
        )) {
            ImageIO.write(bufferedImage, format.name(), outputStream);
        }
    }

}

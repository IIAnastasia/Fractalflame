package flame.objects;

import flame.generators.PointGenerator;
import flame.generators.VariationStorage;

public record WorldInfo(FractalImage canvas, Rect world, PointGenerator generator,
                        VariationStorage variations) {
}

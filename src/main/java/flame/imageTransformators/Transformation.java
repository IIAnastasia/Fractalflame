package flame.imageTransformators;

import flame.objects.Point;

import java.util.function.Function;

public interface Transformation extends Function<Point, Point> {
    default Transformation compose(Transformation before) {
        return point -> this.apply(before.apply(point));
    }
}

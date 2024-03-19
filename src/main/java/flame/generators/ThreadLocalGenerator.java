package flame.generators;

import flame.objects.Point;

import java.util.concurrent.ThreadLocalRandom;

public class ThreadLocalGenerator implements PointGenerator {
    @Override
    public Point generate() {
        return new Point(
            ThreadLocalRandom.current().nextDouble(-1, 1),
            ThreadLocalRandom.current().nextDouble(-1, 1)
        );
    }
}

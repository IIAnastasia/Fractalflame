package flame.generators;

import flame.imageTransformators.ColoredTransformation;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class ListVariationStorage implements VariationStorage {
    final List<ColoredTransformation> transformations;

    public ListVariationStorage(List<ColoredTransformation> transformations) {
        this.transformations = new ArrayList<>(transformations);
    }

    @Override
    public ColoredTransformation get() {
        int index = ThreadLocalRandom.current().nextInt(0, transformations.size());
        return transformations.get(index);
    }

}

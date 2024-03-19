package flame.objects;

public record FractalImage(Pixel[][] data, int width, int height) {

    public static FractalImage create(int width, int height) {
        Pixel[][] data = new Pixel[width][height];
        for (int i = 0; i < width; ++i) {
            for (int j = 0; j < height; ++j) {
                data[i][j] = new Pixel();
            }
        }
        return new FractalImage(data, width, height);
    }

    public boolean contains(int x, int y) {
        return x >= 0 && y >= 0 && x < width && y < height;
    }

    public Pixel pixel(int x, int y) {
        return data[x][y];
    }
}

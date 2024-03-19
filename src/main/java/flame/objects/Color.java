package flame.objects;

public class Color {
    private final static int MAX_RGB_VALUE = 255;
    double r;
    double g;
    double b;

    public Color(double r, double g, double b) {
        this.r = r;
        this.g = g;
        this.b = b;
    }

    public void add(Color another) {
        r += another.r;
        g += another.g;
        b += another.b;
    }

    public void multiply(double alpha) {
        r *= alpha;
        g *= alpha;
        b *= alpha;
    }

    public void divide(double alpha) {
        multiply(1 / alpha);
    }

    public int asInt() {
        return new java.awt.Color(
            (int) (r * MAX_RGB_VALUE),
            (int) (g * MAX_RGB_VALUE),
            (int) (b * MAX_RGB_VALUE)
        ).getRGB();
    }

    public double getR() {
        return r;
    }

    public double getG() {
        return g;
    }

    public double getB() {
        return b;
    }
}

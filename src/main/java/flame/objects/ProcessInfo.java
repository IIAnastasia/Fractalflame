package flame.objects;

public record ProcessInfo(int samples,
                          int iterPerSample,
                          int symmetry,
                          int filterScale, double gamma) {
    private final static int DEFAULT_FILTER_SCALE = 5;
    private final static double DEFAULT_GAMMA = 2.2;

    public ProcessInfo(int samples, int iterPerSample, int symmetry) {
        this(samples, iterPerSample, symmetry, DEFAULT_FILTER_SCALE, DEFAULT_GAMMA);
    }
}

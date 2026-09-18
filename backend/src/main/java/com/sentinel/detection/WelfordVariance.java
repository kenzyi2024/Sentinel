package com.sentinel.detection;

/**
 * Welford's online algorithm for computing a running mean and variance in a single pass, without
 * storing the samples. It is numerically stable (unlike the naive "sum of squares minus square of
 * sum" formula) and updates in O(1) per sample, which is exactly what a streaming anomaly baseline
 * needs.
 *
 * <p>The behavioral anomaly detector feeds it the read-count of each sliding-window position to
 * build a baseline, then asks for the {@link #zScore(double)} of the current window — how many
 * standard deviations the current activity sits above normal.
 */
public final class WelfordVariance {

    private long count;
    private double mean;
    private double m2;

    /** Incorporates one observation into the running statistics. */
    public void add(double value) {
        count++;
        double delta = value - mean;
        mean += delta / count;
        double delta2 = value - mean;
        m2 += delta * delta2;
    }

    public long count() {
        return count;
    }

    public double mean() {
        return mean;
    }

    /** Sample variance (Bessel-corrected); 0 until at least two samples are seen. */
    public double variance() {
        return count < 2 ? 0.0 : m2 / (count - 1);
    }

    public double standardDeviation() {
        return Math.sqrt(variance());
    }

    /**
     * The z-score of {@code value} against the accumulated distribution. Returns 0 when there is
     * no established spread (fewer than two samples, or all samples identical) so a cold start
     * never manufactures an anomaly.
     */
    public double zScore(double value) {
        double sd = standardDeviation();
        return sd == 0.0 ? 0.0 : (value - mean) / sd;
    }
}

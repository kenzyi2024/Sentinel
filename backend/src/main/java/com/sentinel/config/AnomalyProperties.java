package com.sentinel.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Externalized configuration for the behavioral anomaly detector (bound from
 * {@code sentinel.anomaly.*}).
 */
@ConfigurationProperties("sentinel.anomaly")
public class AnomalyProperties {

    /** Number of recent actions kept in the sliding window. */
    private int windowSize = 25;
    /** Read-like actions within the window that count as bulk enumeration. */
    private int enumerationThreshold = 12;
    /** Standard deviations above the moving baseline that flags a frequency spike. */
    private double zscoreAlert = 2.5;

    public int getWindowSize() {
        return windowSize;
    }

    public void setWindowSize(int windowSize) {
        this.windowSize = windowSize;
    }

    public int getEnumerationThreshold() {
        return enumerationThreshold;
    }

    public void setEnumerationThreshold(int enumerationThreshold) {
        this.enumerationThreshold = enumerationThreshold;
    }

    public double getZscoreAlert() {
        return zscoreAlert;
    }

    public void setZscoreAlert(double zscoreAlert) {
        this.zscoreAlert = zscoreAlert;
    }
}

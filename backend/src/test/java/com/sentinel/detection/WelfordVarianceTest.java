package com.sentinel.detection;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class WelfordVarianceTest {

    @Test
    void computesRunningMeanAndSampleVariance() {
        WelfordVariance stats = new WelfordVariance();
        for (double value : new double[]{2, 4, 4, 4, 5, 5, 7, 9}) {
            stats.add(value);
        }
        assertThat(stats.count()).isEqualTo(8);
        assertThat(stats.mean()).isEqualTo(5.0);
        // sum of squared deviations = 32; sample variance = 32 / (8 - 1)
        assertThat(stats.variance()).isCloseTo(32.0 / 7.0, org.assertj.core.data.Offset.offset(1e-9));
        assertThat(stats.standardDeviation()).isCloseTo(Math.sqrt(32.0 / 7.0), org.assertj.core.data.Offset.offset(1e-9));
    }

    @Test
    void zScoreExpressesDeviationInStandardDeviations() {
        WelfordVariance stats = new WelfordVariance();
        for (double value : new double[]{2, 4, 4, 4, 5, 5, 7, 9}) {
            stats.add(value);
        }
        double expected = (9 - 5) / Math.sqrt(32.0 / 7.0);
        assertThat(stats.zScore(9)).isCloseTo(expected, org.assertj.core.data.Offset.offset(1e-9));
    }

    @Test
    void coldStartAndConstantSeriesNeverManufactureAnomaly() {
        WelfordVariance empty = new WelfordVariance();
        assertThat(empty.variance()).isZero();
        assertThat(empty.zScore(100)).isZero();

        WelfordVariance constant = new WelfordVariance();
        constant.add(5);
        constant.add(5);
        constant.add(5);
        assertThat(constant.standardDeviation()).isZero();
        assertThat(constant.zScore(5)).isZero();
    }
}

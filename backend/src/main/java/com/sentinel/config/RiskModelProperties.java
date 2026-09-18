package com.sentinel.config;

import com.sentinel.domain.enums.RiskBand;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Externalized configuration for the risk model (bound from {@code sentinel.risk.*}). Keeping the
 * weights and band cut-points here — rather than as constants in the scorers — is what lets the
 * model be re-tuned without recompiling: change {@code application.yml}, restart, done.
 */
@ConfigurationProperties("sentinel.risk")
public class RiskModelProperties {

    private Weights weights = new Weights();
    private Bands bands = new Bands();
    private int criticalBlockTerminateThreshold = 3;

    public Weights getWeights() {
        return weights;
    }

    public void setWeights(Weights weights) {
        this.weights = weights;
    }

    public Bands getBands() {
        return bands;
    }

    public void setBands(Bands bands) {
        this.bands = bands;
    }

    public int getCriticalBlockTerminateThreshold() {
        return criticalBlockTerminateThreshold;
    }

    public void setCriticalBlockTerminateThreshold(int criticalBlockTerminateThreshold) {
        this.criticalBlockTerminateThreshold = criticalBlockTerminateThreshold;
    }

    /** Resolves a numeric score into a band using the configured cut-points. */
    public RiskBand band(int score) {
        return RiskBand.fromScore(score, bands.getMedium(), bands.getHigh(), bands.getCritical());
    }

    /** Per-scorer weights: each scorer's normalized [0,1] signal is multiplied by its weight. */
    public static class Weights {
        private double permission = 40;
        private double resourceSensitivity = 25;
        private double commandDanger = 50;
        private double promptInjection = 30;
        private double behavioralAnomaly = 30;
        private double policyViolation = 35;
        private double privilegeEscalation = 45;

        public double getPermission() { return permission; }
        public void setPermission(double v) { this.permission = v; }
        public double getResourceSensitivity() { return resourceSensitivity; }
        public void setResourceSensitivity(double v) { this.resourceSensitivity = v; }
        public double getCommandDanger() { return commandDanger; }
        public void setCommandDanger(double v) { this.commandDanger = v; }
        public double getPromptInjection() { return promptInjection; }
        public void setPromptInjection(double v) { this.promptInjection = v; }
        public double getBehavioralAnomaly() { return behavioralAnomaly; }
        public void setBehavioralAnomaly(double v) { this.behavioralAnomaly = v; }
        public double getPolicyViolation() { return policyViolation; }
        public void setPolicyViolation(double v) { this.policyViolation = v; }
        public double getPrivilegeEscalation() { return privilegeEscalation; }
        public void setPrivilegeEscalation(double v) { this.privilegeEscalation = v; }
    }

    /** Inclusive lower bounds for each risk band. */
    public static class Bands {
        private int medium = 30;
        private int high = 60;
        private int critical = 85;

        public int getMedium() { return medium; }
        public void setMedium(int medium) { this.medium = medium; }
        public int getHigh() { return high; }
        public void setHigh(int high) { this.high = high; }
        public int getCritical() { return critical; }
        public void setCritical(int critical) { this.critical = critical; }
    }
}

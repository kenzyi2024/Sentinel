package com.sentinel.persistence;

import com.sentinel.domain.enums.AgentArchetype;
import com.sentinel.domain.enums.RiskBand;
import com.sentinel.domain.enums.RunStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

/**
 * JPA persistence view of a simulation run. Aggregate counters are stored as columns for cheap list
 * views; the full {@link com.sentinel.domain.SecurityReport} is kept as JSON in {@code reportJson}.
 */
@Entity
@Table(name = "runs")
public class RunEntity {

    @Id
    private String id;

    private String scenarioId;
    private String scenarioName;
    private String agentId;
    private String agentName;

    @Enumerated(EnumType.STRING)
    private AgentArchetype archetype;

    @Enumerated(EnumType.STRING)
    private RunStatus status;

    private Instant startedAt;
    private Instant finishedAt;

    private int totalActions;
    private int allowed;
    private int blocked;
    private int requiresApproval;
    private int maxRiskScore;

    @Enumerated(EnumType.STRING)
    private RiskBand overallRiskBand;

    @Column(length = 1_000_000)
    private String reportJson;

    protected RunEntity() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getScenarioId() {
        return scenarioId;
    }

    public void setScenarioId(String scenarioId) {
        this.scenarioId = scenarioId;
    }

    public String getScenarioName() {
        return scenarioName;
    }

    public void setScenarioName(String scenarioName) {
        this.scenarioName = scenarioName;
    }

    public String getAgentId() {
        return agentId;
    }

    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    public String getAgentName() {
        return agentName;
    }

    public void setAgentName(String agentName) {
        this.agentName = agentName;
    }

    public AgentArchetype getArchetype() {
        return archetype;
    }

    public void setArchetype(AgentArchetype archetype) {
        this.archetype = archetype;
    }

    public RunStatus getStatus() {
        return status;
    }

    public void setStatus(RunStatus status) {
        this.status = status;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(Instant startedAt) {
        this.startedAt = startedAt;
    }

    public Instant getFinishedAt() {
        return finishedAt;
    }

    public void setFinishedAt(Instant finishedAt) {
        this.finishedAt = finishedAt;
    }

    public int getTotalActions() {
        return totalActions;
    }

    public void setTotalActions(int totalActions) {
        this.totalActions = totalActions;
    }

    public int getAllowed() {
        return allowed;
    }

    public void setAllowed(int allowed) {
        this.allowed = allowed;
    }

    public int getBlocked() {
        return blocked;
    }

    public void setBlocked(int blocked) {
        this.blocked = blocked;
    }

    public int getRequiresApproval() {
        return requiresApproval;
    }

    public void setRequiresApproval(int requiresApproval) {
        this.requiresApproval = requiresApproval;
    }

    public int getMaxRiskScore() {
        return maxRiskScore;
    }

    public void setMaxRiskScore(int maxRiskScore) {
        this.maxRiskScore = maxRiskScore;
    }

    public RiskBand getOverallRiskBand() {
        return overallRiskBand;
    }

    public void setOverallRiskBand(RiskBand overallRiskBand) {
        this.overallRiskBand = overallRiskBand;
    }

    public String getReportJson() {
        return reportJson;
    }

    public void setReportJson(String reportJson) {
        this.reportJson = reportJson;
    }
}

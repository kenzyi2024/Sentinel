package com.sentinel.persistence;

import com.sentinel.domain.enums.ActionType;
import com.sentinel.domain.enums.Decision;
import com.sentinel.domain.enums.RiskBand;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import java.time.Instant;

/**
 * JPA persistence view of an {@link com.sentinel.domain.Event}.
 *
 * <p>This follows a small event-store pattern: the columns that are queried, sorted, or aggregated
 * (run id, sequence, decision, risk score, action type, timestamp) are first-class columns, while
 * the full structured event — risk factors, injection findings, anomaly signals, reasons — is kept
 * verbatim as JSON in {@code detailsJson}. That keeps the schema small and stable even as the rich
 * domain model evolves, and avoids a sprawl of child tables for data that is always read as a whole.
 */
@Entity
@Table(name = "events", indexes = {
        @Index(name = "idx_events_run_seq", columnList = "runId, sequence")
})
public class EventEntity {

    @Id
    private String id;

    @Column(nullable = false)
    private String runId;

    private int sequence;

    private Instant timestamp;

    @Enumerated(EnumType.STRING)
    private ActionType actionType;

    @Column(length = 1024)
    private String resource;

    @Enumerated(EnumType.STRING)
    private Decision decision;

    private int riskScore;

    @Enumerated(EnumType.STRING)
    private RiskBand riskBand;

    private String previousEventId;

    @Column(length = 1_000_000)
    private String detailsJson;

    protected EventEntity() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRunId() {
        return runId;
    }

    public void setRunId(String runId) {
        this.runId = runId;
    }

    public int getSequence() {
        return sequence;
    }

    public void setSequence(int sequence) {
        this.sequence = sequence;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public ActionType getActionType() {
        return actionType;
    }

    public void setActionType(ActionType actionType) {
        this.actionType = actionType;
    }

    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    public Decision getDecision() {
        return decision;
    }

    public void setDecision(Decision decision) {
        this.decision = decision;
    }

    public int getRiskScore() {
        return riskScore;
    }

    public void setRiskScore(int riskScore) {
        this.riskScore = riskScore;
    }

    public RiskBand getRiskBand() {
        return riskBand;
    }

    public void setRiskBand(RiskBand riskBand) {
        this.riskBand = riskBand;
    }

    public String getPreviousEventId() {
        return previousEventId;
    }

    public void setPreviousEventId(String previousEventId) {
        this.previousEventId = previousEventId;
    }

    public String getDetailsJson() {
        return detailsJson;
    }

    public void setDetailsJson(String detailsJson) {
        this.detailsJson = detailsJson;
    }
}

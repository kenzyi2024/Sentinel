// TypeScript mirror of the backend JSON contracts (see com.sentinel.domain / api.dto).

export type Decision = 'ALLOWED' | 'REQUIRES_APPROVAL' | 'BLOCKED';
export type RiskBand = 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';
export type AgentArchetype = 'DEVELOPER' | 'RESEARCH' | 'MALICIOUS' | 'COMPROMISED';
export type AgentState = 'INITIALIZING' | 'ACTIVE' | 'COMPROMISED' | 'QUARANTINED' | 'TERMINATED';
export type RunStatus = 'PENDING' | 'RUNNING' | 'COMPLETED' | 'TERMINATED' | 'FAILED';
export type PolicyEffect = 'LOG_ONLY' | 'WARN' | 'ALLOW' | 'REQUIRE_APPROVAL' | 'DENY';
export type ResourceSensitivity = 'PUBLIC' | 'INTERNAL' | 'SENSITIVE' | 'SECRET';

export interface RiskFactor {
  category: string;
  name: string;
  signal: number;
  weight: number;
  contribution: number;
  explanation: string;
}

export interface RiskAssessment {
  score: number;
  band: RiskBand;
  factors: RiskFactor[];
}

export interface InjectionFinding {
  ruleId: string;
  category: string;
  severity: string;
  matchedText: string;
  position: number;
  explanation: string;
}

export interface AnomalyResult {
  score: number;
  zScore: number;
  windowActionCount: number;
  enumerationDetected: boolean;
  sensitiveAfterEnumeration: boolean;
  reasons: string[];
}

export interface PolicyDecision {
  matched: boolean;
  policyId: string | null;
  policyName: string | null;
  effect: PolicyEffect;
  reason: string;
}

export interface SafeAlternative {
  action: string;
  rationale: string;
}

export interface SentinelEvent {
  id: string;
  runId: string;
  sequence: number;
  timestamp: string;
  agentId: string;
  agentName: string;
  actionType: string;
  resource: string;
  command: string | null;
  intent: string;
  parameters: Record<string, string>;
  requiredCapabilities: string[];
  missingCapabilities: string[];
  resourceSensitivity: ResourceSensitivity;
  riskAssessment: RiskAssessment;
  injectionFindings: InjectionFinding[];
  anomalyResult: AnomalyResult;
  policyDecision: PolicyDecision;
  decision: Decision;
  executed: boolean;
  reasons: string[];
  warnings: string[];
  recommendations: string[];
  previousEventId: string | null;
  agentStateAfter: AgentState;
  inducedByInjection: boolean;
  safeAlternative: SafeAlternative | null;
}

export interface RunSummary {
  id: string;
  scenarioId: string;
  scenarioName: string;
  agentName: string;
  archetype: AgentArchetype;
  status: RunStatus;
  startedAt: string;
  finishedAt: string;
  totalActions: number;
  allowed: number;
  blocked: number;
  requiresApproval: number;
  maxRiskScore: number;
  overallRiskBand: RiskBand;
}

export interface EventSummary {
  id: string;
  sequence: number;
  actionType: string;
  resource: string;
  decision: Decision;
  riskScore: number;
  riskBand: RiskBand;
}

export interface SecurityReport {
  runId: string;
  scenarioId: string;
  scenarioName: string;
  agentName: string;
  archetype: AgentArchetype;
  status: RunStatus;
  finalAgentState: AgentState;
  totalActions: number;
  allowed: number;
  blocked: number;
  requiresApproval: number;
  warningsRaised: number;
  policyViolations: number;
  injectionFindings: number;
  anomaliesDetected: number;
  maxRiskScore: number;
  averageRiskScore: number;
  overallRiskBand: RiskBand;
  highestRiskEvents: EventSummary[];
  detectedAttackPatterns: string[];
  behavioralAnomalies: string[];
  recommendedPolicyChanges: string[];
  generatedAt: string;
}

export interface RunDetail {
  summary: RunSummary;
  events: SentinelEvent[];
  report: SecurityReport;
}

export interface ScenarioInfo {
  id: string;
  name: string;
  description: string;
  objective: string;
  threatModel: string;
  archetype: AgentArchetype;
  expectedBehavior: string;
  grantedCapabilities: string[];
  actionCount: number;
}

export interface Agent {
  id: string;
  name: string;
  archetype: AgentArchetype;
  declaredGoal: string;
  capabilities: string[];
}

export interface Policy {
  id: string;
  name: string;
  priority: number;
  archetypes: string[];
  actionTypes: string[];
  resourcePattern: string | null;
  minSensitivity: ResourceSensitivity | null;
  requiredCapability: string | null;
  effect: PolicyEffect;
  reason: string;
  enabled: boolean;
}

export interface EngineMeta {
  name: string;
  version: string;
  deterministic: boolean;
  usesExternalModel: boolean;
  riskScorers: number;
  scenarios: number;
  totalRuns: number;
  totalEvents: number;
  capabilities: string[];
  actionTypes: string[];
}

export interface CustomActionInput {
  type: string;
  resource: string;
  command?: string;
  content?: string;
  intent?: string;
  inducedByInjection?: boolean;
}

export interface CustomScenarioInput {
  name: string;
  archetype: AgentArchetype;
  capabilities: string[];
  actions: CustomActionInput[];
}

export type Mode = 'live' | 'demo';

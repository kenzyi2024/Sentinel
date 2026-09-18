import type { Decision, PolicyEffect, RiskBand } from '../types';

/** CSS class suffix for a risk band (matches the .badge.* / semantic color classes). */
export function bandClass(band: RiskBand): string {
  return band.toLowerCase();
}

/** Map a final decision onto the shared risk color scale. */
export function decisionClass(decision: Decision): string {
  switch (decision) {
    case 'ALLOWED':
      return 'low';
    case 'REQUIRES_APPROVAL':
      return 'medium';
    case 'BLOCKED':
      return 'critical';
  }
}

/** Compact label used in the dense event stream. */
export function decisionLabel(decision: Decision): string {
  switch (decision) {
    case 'ALLOWED':
      return 'ALLOWED';
    case 'REQUIRES_APPROVAL':
      return 'REVIEW';
    case 'BLOCKED':
      return 'BLOCKED';
  }
}

const CSS_COLOR: Record<string, string> = {
  low: 'var(--low)',
  medium: 'var(--medium)',
  high: 'var(--high)',
  critical: 'var(--critical)',
};

export function colorFor(classSuffix: string): string {
  return CSS_COLOR[classSuffix] ?? 'var(--fg-muted)';
}

export function bandColor(band: RiskBand): string {
  return colorFor(bandClass(band));
}

export function decisionColor(decision: Decision): string {
  return colorFor(decisionClass(decision));
}

/** HH:MM:SS from an ISO timestamp. */
export function shortTime(iso: string): string {
  try {
    return new Date(iso).toLocaleTimeString('en-GB', { hour12: false });
  } catch {
    return iso;
  }
}

/** ENUM_NAME -> "Enum name". */
export function humanize(value: string): string {
  const lower = value.replace(/_/g, ' ').toLowerCase();
  return lower.charAt(0).toUpperCase() + lower.slice(1);
}

export function effectLabel(effect: PolicyEffect): string {
  return humanize(effect);
}

import { describe, expect, it } from 'vitest';
import { bandClass, decisionClass, decisionLabel, humanize } from './format';

describe('format helpers', () => {
  it('maps risk bands to css class suffixes', () => {
    expect(bandClass('CRITICAL')).toBe('critical');
    expect(bandClass('LOW')).toBe('low');
  });

  it('maps decisions onto the shared risk color scale', () => {
    expect(decisionClass('ALLOWED')).toBe('low');
    expect(decisionClass('REQUIRES_APPROVAL')).toBe('medium');
    expect(decisionClass('BLOCKED')).toBe('critical');
  });

  it('uses a compact REVIEW label for approvals', () => {
    expect(decisionLabel('REQUIRES_APPROVAL')).toBe('REVIEW');
    expect(decisionLabel('BLOCKED')).toBe('BLOCKED');
  });

  it('humanizes enum names', () => {
    expect(humanize('PROMPT_INJECTION')).toBe('Prompt injection');
  });
});

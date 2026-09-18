import { render, screen } from '@testing-library/react';
import { describe, expect, it } from 'vitest';
import { DecisionBadge, RiskBadge } from './ui';

describe('badges', () => {
  it('renders the REVIEW label for an approval decision', () => {
    render(<DecisionBadge decision="REQUIRES_APPROVAL" />);
    expect(screen.getByText('REVIEW')).toBeInTheDocument();
  });

  it('renders a risk badge with score and band', () => {
    render(<RiskBadge band="CRITICAL" score={100} />);
    expect(screen.getByText(/CRITICAL/)).toBeInTheDocument();
    expect(screen.getByText(/100/)).toBeInTheDocument();
  });
});

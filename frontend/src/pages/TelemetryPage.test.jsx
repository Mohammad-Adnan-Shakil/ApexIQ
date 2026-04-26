import { describe, it, expect, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import TelemetryPage from './TelemetryPage';

// Mock the API
vi.mock('../services/api', () => ({
  default: {
    get: vi.fn(),
  },
}));

describe('TelemetryPage', () => {
  it('should render telemetry comparison page title', () => {
    render(
      <BrowserRouter>
        <TelemetryPage />
      </BrowserRouter>
    );

    expect(screen.getByText(/Telemetry Comparison/i)).toBeInTheDocument();
  });

  it('should render session parameters form', () => {
    render(
      <BrowserRouter>
        <TelemetryPage />
      </BrowserRouter>
    );

    expect(screen.getByPlaceholderText(/2024/i)).toBeInTheDocument();
    expect(screen.getByPlaceholderText(/Monaco/i)).toBeInTheDocument();
  });

  it('should have Analyze button', () => {
    render(
      <BrowserRouter>
        <TelemetryPage />
      </BrowserRouter>
    );

    expect(screen.getByText(/Analyze/i)).toBeInTheDocument();
  });

  it('should have driver input fields', () => {
    render(
      <BrowserRouter>
        <TelemetryPage />
      </BrowserRouter>
    );

    expect(screen.getByPlaceholderText(/VER/i)).toBeInTheDocument();
    expect(screen.getByPlaceholderText(/LEC/i)).toBeInTheDocument();
  });
});

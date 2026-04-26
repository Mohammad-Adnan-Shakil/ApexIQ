import { describe, it, expect, vi } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import AIPage from './AIPage';

// Mock the API
vi.mock('../services/api', () => ({
  default: {
    get: vi.fn(),
  },
}));

describe('AIPage', () => {
  it('should render AI page title', () => {
    render(
      <BrowserRouter>
        <AIPage />
      </BrowserRouter>
    );

    expect(screen.getByText(/AI Driver Intelligence/i)).toBeInTheDocument();
  });

  it('should render prediction form', () => {
    render(
      <BrowserRouter>
        <AIPage />
      </BrowserRouter>
    );

    expect(screen.getByPlaceholderText(/Driver Code/i)).toBeInTheDocument();
    expect(screen.getByPlaceholderText(/Grid Position/i)).toBeInTheDocument();
  });

  it('should have Analyze button', () => {
    render(
      <BrowserRouter>
        <AIPage />
      </BrowserRouter>
    );

    expect(screen.getByText(/Analyze/i)).toBeInTheDocument();
  });
});

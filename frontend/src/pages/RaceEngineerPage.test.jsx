import { describe, it, expect, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import RaceEngineerPage from './RaceEngineerPage';

// Mock the API
vi.mock('../services/api', () => ({
  default: {
    post: vi.fn(),
  },
}));

describe('RaceEngineerPage', () => {
  it('should render race engineer page title', () => {
    render(
      <BrowserRouter>
        <RaceEngineerPage />
      </BrowserRouter>
    );

    expect(screen.getByText(/Race Engineer/i)).toBeInTheDocument();
  });

  it('should render race context form', () => {
    render(
      <BrowserRouter>
        <RaceEngineerPage />
      </BrowserRouter>
    );

    expect(screen.getByPlaceholderText(/Lap/i)).toBeInTheDocument();
    expect(screen.getByPlaceholderText(/Position/i)).toBeInTheDocument();
  });

  it('should have Send button', () => {
    render(
      <BrowserRouter>
        <RaceEngineerPage />
      </BrowserRouter>
    );

    expect(screen.getByText(/Send/i)).toBeInTheDocument();
  });
});

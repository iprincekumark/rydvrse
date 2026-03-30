/**
 * Error Boundary Component
 * Catches rendering errors and displays a fallback UI
 * Essential for React 19 which silently swallows unhandled render errors
 */

import { Component } from 'react';
import type { ErrorInfo, ReactNode } from 'react';

interface ErrorBoundaryProps {
  children: ReactNode;
  fallback?: ReactNode;
}

interface ErrorBoundaryState {
  hasError: boolean;
  error: Error | null;
  errorInfo: ErrorInfo | null;
}

export class ErrorBoundary extends Component<ErrorBoundaryProps, ErrorBoundaryState> {
  constructor(props: ErrorBoundaryProps) {
    super(props);
    this.state = {
      hasError: false,
      error: null,
      errorInfo: null,
    };
  }

  static getDerivedStateFromError(error: Error): Partial<ErrorBoundaryState> {
    return { hasError: true, error };
  }

  componentDidCatch(error: Error, errorInfo: ErrorInfo): void {
    console.error('[ErrorBoundary] Caught error:', error);
    console.error('[ErrorBoundary] Component stack:', errorInfo.componentStack);
    this.setState({ errorInfo });
  }

  handleReset = (): void => {
    this.setState({ hasError: false, error: null, errorInfo: null });
    // Clear any stale persisted state
    try {
      localStorage.removeItem('rydvrse-auth-storage');
      localStorage.removeItem('rydvrse-trip-storage');
      localStorage.removeItem('rydvrse-driver-storage');
      localStorage.removeItem('rydvrse-customer-storage');
    } catch {
      // Ignore localStorage errors
    }
    window.location.href = '/login';
  };

  render(): ReactNode {
    if (this.state.hasError) {
      if (this.props.fallback) {
        return this.props.fallback;
      }

      return (
        <div style={{
          minHeight: '100vh',
          display: 'flex',
          flexDirection: 'column',
          alignItems: 'center',
          justifyContent: 'center',
          padding: '2rem',
          fontFamily: 'system-ui, -apple-system, sans-serif',
          backgroundColor: '#fafafa',
        }}>
          <div style={{
            maxWidth: '500px',
            textAlign: 'center',
            padding: '2rem',
            borderRadius: '1rem',
            backgroundColor: '#fff',
            boxShadow: '0 4px 24px rgba(0, 0, 0, 0.08)',
            border: '1px solid #eee',
          }}>
            <div style={{
              width: '64px',
              height: '64px',
              borderRadius: '50%',
              backgroundColor: '#fee2e2',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              margin: '0 auto 1.5rem',
              fontSize: '28px',
            }}>
              ⚠️
            </div>
            <h2 style={{
              fontSize: '1.5rem',
              fontWeight: 700,
              color: '#1a1a1a',
              marginBottom: '0.75rem',
            }}>
              Something went wrong
            </h2>
            <p style={{
              color: '#666',
              marginBottom: '1.5rem',
              lineHeight: 1.6,
            }}>
              The application encountered an unexpected error. Please try again.
            </p>
            {import.meta.env.DEV && this.state.error && (
              <pre style={{
                textAlign: 'left',
                padding: '1rem',
                borderRadius: '0.5rem',
                backgroundColor: '#f5f5f5',
                fontSize: '0.75rem',
                overflow: 'auto',
                maxHeight: '200px',
                marginBottom: '1.5rem',
                color: '#dc2626',
                border: '1px solid #e5e5e5',
              }}>
                {this.state.error.message}
                {this.state.errorInfo?.componentStack && (
                  <>
                    {'\n\nComponent Stack:'}
                    {this.state.errorInfo.componentStack}
                  </>
                )}
              </pre>
            )}
            <button
              onClick={this.handleReset}
              style={{
                padding: '0.75rem 2rem',
                borderRadius: '0.5rem',
                backgroundColor: '#1a1a1a',
                color: '#fff',
                border: 'none',
                cursor: 'pointer',
                fontSize: '0.875rem',
                fontWeight: 600,
                transition: 'background-color 0.2s',
              }}
              onMouseOver={(e) => { (e.target as HTMLButtonElement).style.backgroundColor = '#333'; }}
              onMouseOut={(e) => { (e.target as HTMLButtonElement).style.backgroundColor = '#1a1a1a'; }}
            >
              Reset & Try Again
            </button>
          </div>
        </div>
      );
    }

    return this.props.children;
  }
}

export default ErrorBoundary;

import React from "react";
import { AlertTriangle } from "lucide-react";
import { Card } from "./common/Card";

class ErrorBoundary extends React.Component {
  constructor(props) {
    super(props);
    this.state = { hasError: false, error: null };
  }

  static getDerivedStateFromError(error) {
    return { hasError: true, error };
  }

  componentDidCatch(error, errorInfo) {
    console.error("❌ [ErrorBoundary] Caught error:", error, errorInfo);
  }

  render() {
    if (this.state.hasError) {
      return (
        <div className="py-10">
          <Card className="border-accentRed/40 bg-accentRed/10">
            <div className="flex gap-3 items-start">
              <AlertTriangle className="h-5 w-5 text-accentRed mt-0.5 flex-shrink-0" />
              <div>
                <p className="font-semibold text-whitePrimary">Something went wrong</p>
                <p className="text-sm text-whiteMuted mt-1">
                  {this.state.error?.message || "Unable to load this page. Please try again."}
                </p>
                <button
                  onClick={() => window.location.reload()}
                  className="mt-3 px-4 py-2 bg-accentRed/20 text-accentRed rounded hover:bg-accentRed/30 text-sm font-medium"
                >
                  Reload Page
                </button>
              </div>
            </div>
          </Card>
        </div>
      );
    }

    return this.props.children;
  }
}

export default ErrorBoundary;

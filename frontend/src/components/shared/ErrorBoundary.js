import React from 'react';
import { toast } from 'react-toastify';

class ErrorBoundary extends React.Component {
  constructor(props) {
    super(props);
    this.state = { 
      hasError: false, 
      error: null,
      errorInfo: null
    };
  }

  static getDerivedStateFromError(error) {
    return { hasError: true, error };
  }

  componentDidCatch(error, errorInfo) {
    console.error('🔴 ErrorBoundary caught an error:', {
      error,
      errorInfo,
      componentStack: errorInfo.componentStack
    });

    this.setState({ errorInfo });

    // Show toast notification
    toast.error('Đã xảy ra lỗi trong ứng dụng. Vui lòng tải lại trang.', {
      autoClose: 5000
    });
  }

  handleReset = () => {
    this.setState({ hasError: false, error: null, errorInfo: null });
    window.location.href = '/';
  };

  render() {
    if (this.state.hasError) {
      return (
        <div style={{
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          minHeight: '100vh',
          backgroundColor: '#f8f9fa',
          padding: '2rem'
        }}>
          <div style={{
            backgroundColor: 'white',
            borderRadius: '12px',
            padding: '3rem',
            maxWidth: '600px',
            width: '100%',
            boxShadow: '0 4px 12px rgba(0,0,0,0.1)',
            textAlign: 'center'
          }}>
            {/* Icon */}
            <div style={{
              fontSize: '5rem',
              marginBottom: '1.5rem'
            }}>
              ⚠️
            </div>

            {/* Title */}
            <h2 style={{
              color: '#e74c3c',
              marginBottom: '1rem',
              fontSize: '1.75rem',
              fontWeight: '600'
            }}>
              Đã xảy ra lỗi
            </h2>

            {/* Description */}
            <p style={{
              color: '#7f8c8d',
              marginBottom: '1.5rem',
              fontSize: '1rem',
              lineHeight: '1.6'
            }}>
              Ứng dụng đã gặp sự cố không mong muốn. 
              Chúng tôi đã ghi nhận lỗi và sẽ khắc phục sớm nhất.
            </p>

            {/* Error Message (for developers) */}
            {process.env.NODE_ENV === 'development' && this.state.error && (
              <div style={{
                backgroundColor: '#fff5f5',
                border: '1px solid #feb2b2',
                borderRadius: '6px',
                padding: '1rem',
                marginBottom: '1.5rem',
                textAlign: 'left',
                maxHeight: '200px',
                overflow: 'auto'
              }}>
                <div style={{
                  fontWeight: '600',
                  color: '#e74c3c',
                  marginBottom: '0.5rem',
                  fontSize: '0.875rem'
                }}>
                  Error Details (Dev Only):
                </div>
                <pre style={{
                  fontSize: '0.75rem',
                  color: '#c62828',
                  margin: 0,
                  whiteSpace: 'pre-wrap',
                  wordBreak: 'break-word'
                }}>
                  {this.state.error.toString()}
                </pre>
              </div>
            )}

            {/* Action Buttons */}
            <div style={{
              display: 'flex',
              gap: '1rem',
              justifyContent: 'center',
              flexWrap: 'wrap'
            }}>
              <button
                onClick={() => window.location.reload()}
                style={{
                  padding: '0.875rem 2rem',
                  backgroundColor: '#3498db',
                  color: 'white',
                  border: 'none',
                  borderRadius: '8px',
                  cursor: 'pointer',
                  fontWeight: '500',
                  fontSize: '1rem',
                  transition: 'all 0.3s ease'
                }}
                onMouseOver={(e) => e.target.style.backgroundColor = '#2980b9'}
                onMouseOut={(e) => e.target.style.backgroundColor = '#3498db'}
              >
                🔄 Tải lại trang
              </button>

              <button
                onClick={this.handleReset}
                style={{
                  padding: '0.875rem 2rem',
                  backgroundColor: '#27ae60',
                  color: 'white',
                  border: 'none',
                  borderRadius: '8px',
                  cursor: 'pointer',
                  fontWeight: '500',
                  fontSize: '1rem',
                  transition: 'all 0.3s ease'
                }}
                onMouseOver={(e) => e.target.style.backgroundColor = '#229954'}
                onMouseOut={(e) => e.target.style.backgroundColor = '#27ae60'}
              >
                🏠 Về trang chủ
              </button>
            </div>

            {/* Support Info */}
            <div style={{
              marginTop: '2rem',
              padding: '1rem',
              backgroundColor: '#f8f9fa',
              borderRadius: '6px',
              fontSize: '0.875rem',
              color: '#7f8c8d'
            }}>
              <strong>Cần hỗ trợ?</strong>
              <div style={{ marginTop: '0.5rem' }}>
                Vui lòng liên hệ bộ phận kỹ thuật hoặc quản trị viên hệ thống
              </div>
            </div>
          </div>
        </div>
      );
    }

    return this.props.children;
  }
}

export default ErrorBoundary;
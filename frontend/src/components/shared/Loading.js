import React from 'react';

const Loading = ({ 
  message = 'Đang tải dữ liệu...', 
  size = 'medium',
  fullScreen = false 
}) => {
  const sizes = {
    small: { spinner: '24px', text: '0.875rem', padding: '1rem' },
    medium: { spinner: '40px', text: '1rem', padding: '2rem' },
    large: { spinner: '60px', text: '1.125rem', padding: '3rem' }
  };

  const currentSize = sizes[size] || sizes.medium;

  const containerStyle = fullScreen ? {
    position: 'fixed',
    top: 0,
    left: 0,
    right: 0,
    bottom: 0,
    backgroundColor: 'rgba(255, 255, 255, 0.9)',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    zIndex: 9999
  } : {
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    padding: currentSize.padding,
    minHeight: '200px'
  };

  return (
    <>
      <style>
        {`
          @keyframes spin {
            0% { transform: rotate(0deg); }
            100% { transform: rotate(360deg); }
          }
          
          @keyframes pulse {
            0%, 100% { opacity: 1; }
            50% { opacity: 0.5; }
          }
        `}
      </style>
      
      <div style={containerStyle}>
        <div style={{
          display: 'flex',
          flexDirection: 'column',
          alignItems: 'center',
          gap: '1rem'
        }}>
          {/* Spinner */}
          <div style={{
            width: currentSize.spinner,
            height: currentSize.spinner,
            border: `4px solid #e8e8e8`,
            borderTop: `4px solid #3498db`,
            borderRadius: '50%',
            animation: 'spin 1s linear infinite'
          }} />
          
          {/* Message */}
          <div style={{
            color: '#7f8c8d',
            fontSize: currentSize.text,
            textAlign: 'center',
            animation: 'pulse 1.5s ease-in-out infinite',
            fontWeight: '500'
          }}>
            {message}
          </div>
        </div>
      </div>
    </>
  );
};

export default Loading;
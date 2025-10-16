import React from 'react';

const Notification = ({ type = 'info', message, onClose, autoClose = 5000 }) => {
  React.useEffect(() => {
    if (autoClose) {
      const timer = setTimeout(() => {
        onClose && onClose();
      }, autoClose);
      return () => clearTimeout(timer);
    }
  }, [autoClose, onClose]);

  const colors = {
    success: { bg: '#d5f4e6', border: '#27ae60', icon: '✓' },
    error: { bg: '#fadbd8', border: '#e74c3c', icon: '✕' },
    warning: { bg: '#fef5e7', border: '#f39c12', icon: '⚠' },
    info: { bg: '#e3f2fd', border: '#3498db', icon: 'ℹ' }
  };

  const style = colors[type] || colors.info;

  return (
    <div style={{
      position: 'fixed',
      top: '20px',
      right: '20px',
      backgroundColor: style.bg,
      border: `2px solid ${style.border}`,
      borderRadius: '8px',
      padding: '1rem 1.5rem',
      boxShadow: '0 4px 12px rgba(0,0,0,0.15)',
      zIndex: 9999,
      maxWidth: '400px',
      animation: 'slideIn 0.3s ease-out'
    }}>
      <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
        <span style={{ fontSize: '1.5rem' }}>{style.icon}</span>
        <span style={{ flex: 1, color: '#2c3e50', fontWeight: '500' }}>{message}</span>
        {onClose && (
          <button
            onClick={onClose}
            style={{
              background: 'none',
              border: 'none',
              fontSize: '1.25rem',
              cursor: 'pointer',
              color: '#7f8c8d',
              padding: '0 0.25rem'
            }}
          >
            ×
          </button>
        )}
      </div>
      <style>{`
        @keyframes slideIn {
          from {
            transform: translateX(100%);
            opacity: 0;
          }
          to {
            transform: translateX(0);
            opacity: 1;
          }
        }
      `}</style>
    </div>
  );
};

export default Notification;
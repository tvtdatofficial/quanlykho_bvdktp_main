import React from 'react';

const ConfirmDialog = ({ 
  isOpen, 
  onClose, 
  onConfirm, 
  title = 'Xác nhận',
  message,
  confirmText = 'Xác nhận',
  cancelText = 'Hủy',
  type = 'warning' // warning, danger, info
}) => {
  if (!isOpen) return null;

  const typeColors = {
    warning: { bg: '#fff3e0', border: '#f39c12', icon: '⚠️' },
    danger: { bg: '#ffebee', border: '#e74c3c', icon: '🚨' },
    info: { bg: '#e3f2fd', border: '#3498db', icon: 'ℹ️' }
  };

  const colors = typeColors[type] || typeColors.warning;

  return (
    <div style={{
      position: 'fixed',
      top: 0,
      left: 0,
      right: 0,
      bottom: 0,
      backgroundColor: 'rgba(0, 0, 0, 0.5)',
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center',
      zIndex: 10000,
      padding: '1rem'
    }}>
      <div style={{
        backgroundColor: 'white',
        borderRadius: '12px',
        maxWidth: '450px',
        width: '100%',
        boxShadow: '0 20px 60px rgba(0, 0, 0, 0.3)'
      }}>
        {/* Header */}
        <div style={{
          padding: '1.5rem',
          borderBottom: '1px solid #e8e8e8'
        }}>
          <h3 style={{ 
            margin: 0, 
            color: '#2c3e50',
            fontSize: '1.25rem',
            fontWeight: '600',
            display: 'flex',
            alignItems: 'center',
            gap: '0.5rem'
          }}>
            <span style={{ fontSize: '1.5rem' }}>{colors.icon}</span>
            {title}
          </h3>
        </div>

        {/* Content */}
        <div style={{
          padding: '1.5rem',
          backgroundColor: colors.bg,
          border: `1px solid ${colors.border}`,
          margin: '1rem 1.5rem',
          borderRadius: '8px'
        }}>
          <p style={{ 
            margin: 0, 
            color: '#2c3e50',
            lineHeight: '1.6',
            whiteSpace: 'pre-line'
          }}>
            {message}
          </p>
        </div>

        {/* Footer */}
        <div style={{
          padding: '1rem 1.5rem',
          display: 'flex',
          gap: '1rem',
          justifyContent: 'flex-end'
        }}>
          <button
            onClick={onClose}
            style={{
              padding: '0.75rem 1.5rem',
              backgroundColor: '#95a5a6',
              color: 'white',
              border: 'none',
              borderRadius: '8px',
              cursor: 'pointer',
              fontWeight: '500',
              fontSize: '1rem'
            }}
          >
            {cancelText}
          </button>
          <button
            onClick={() => {
              onConfirm();
              onClose();
            }}
            style={{
              padding: '0.75rem 1.5rem',
              backgroundColor: type === 'danger' ? '#e74c3c' : '#27ae60',
              color: 'white',
              border: 'none',
              borderRadius: '8px',
              cursor: 'pointer',
              fontWeight: '500',
              fontSize: '1rem'
            }}
          >
            {confirmText}
          </button>
        </div>
      </div>
    </div>
  );
};

export default ConfirmDialog;
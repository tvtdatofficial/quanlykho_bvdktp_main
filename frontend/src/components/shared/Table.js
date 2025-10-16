import React from 'react';
import Loading from './Loading';

const Table = ({ 
  columns, 
  data, 
  loading = false, 
  onRowClick,
  emptyMessage = 'Không có dữ liệu',
  striped = false,
  hoverable = true
}) => {
  if (loading) {
    return <Loading message="Đang tải dữ liệu..." />;
  }

  if (!data || data.length === 0) {
    return (
      <div style={{
        padding: '3rem 2rem',
        textAlign: 'center',
        color: '#95a5a6',
        backgroundColor: '#f8f9fa',
        borderRadius: '8px',
        margin: '1rem 0'
      }}>
        <div style={{ fontSize: '3rem', marginBottom: '1rem', opacity: 0.3 }}>
          📋
        </div>
        <p style={{ margin: 0, fontSize: '1rem' }}>
          {emptyMessage}
        </p>
      </div>
    );
  }

  return (
    <div style={{ overflowX: 'auto' }}>
      <table style={{ 
        width: '100%', 
        borderCollapse: 'collapse',
        fontSize: '0.875rem'
      }}>
        <thead>
          <tr style={{ 
            backgroundColor: '#f8f9fa',
            borderBottom: '2px solid #dee2e6'
          }}>
            {columns.map((column, index) => (
              <th
                key={index}
                style={{
                  padding: '1rem',
                  textAlign: column.align || 'left',
                  fontWeight: '600',
                  color: '#2c3e50',
                  fontSize: '0.875rem',
                  textTransform: 'uppercase',
                  letterSpacing: '0.5px',
                  whiteSpace: 'nowrap'
                }}
              >
                {column.title}
              </th>
            ))}
          </tr>
        </thead>
        <tbody>
          {data.map((row, rowIndex) => (
            <tr
              key={rowIndex}
              onClick={() => onRowClick && onRowClick(row)}
              style={{
                cursor: onRowClick ? 'pointer' : 'default',
                backgroundColor: striped && rowIndex % 2 === 0 ? '#f8f9fa' : 'white',
                borderBottom: '1px solid #dee2e6',
                transition: 'background-color 0.2s ease'
              }}
              onMouseOver={(e) => {
                if (hoverable) {
                  e.currentTarget.style.backgroundColor = '#e3f2fd';
                }
              }}
              onMouseOut={(e) => {
                if (hoverable) {
                  e.currentTarget.style.backgroundColor = 
                    striped && rowIndex % 2 === 0 ? '#f8f9fa' : 'white';
                }
              }}
            >
              {columns.map((column, colIndex) => (
                <td
                  key={colIndex}
                  style={{
                    padding: '1rem',
                    textAlign: column.align || 'left',
                    color: '#2c3e50'
                  }}
                >
                  {column.render 
                    ? column.render(row[column.dataIndex], row, rowIndex) 
                    : row[column.dataIndex] || '-'}
                </td>
              ))}
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
};

export default Table;
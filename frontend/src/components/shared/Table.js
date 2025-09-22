
import React from 'react';

const Table = ({ columns, data, loading = false, onRowClick }) => {
  if (loading) {
    return (
      <div style={{ padding: '2rem', textAlign: 'center' }}>
        Đang tải...
      </div>
    );
  }

  if (!data || data.length === 0) {
    return (
      <div style={{ padding: '2rem', textAlign: 'center', color: '#7f8c8d' }}>
        Không có dữ liệu
      </div>
    );
  }

  return (
    <table style={{ width: '100%', borderCollapse: 'collapse' }}>
      <thead>
        <tr style={{ backgroundColor: '#f8f9fa' }}>
          {columns.map((column, index) => (
            <th
              key={index}
              style={{
                padding: '1rem',
                textAlign: column.align || 'left',
                borderBottom: '1px solid #dee2e6',
                fontWeight: '500',
                color: '#2c3e50'
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
              backgroundColor: 'white'
            }}
            onMouseOver={(e) => onRowClick && (e.target.closest('tr').style.backgroundColor = '#f8f9fa')}
            onMouseOut={(e) => onRowClick && (e.target.closest('tr').style.backgroundColor = 'white')}
          >
            {columns.map((column, colIndex) => (
              <td
                key={colIndex}
                style={{
                  padding: '1rem',
                  borderBottom: '1px solid #dee2e6',
                  textAlign: column.align || 'left'
                }}
              >
                {column.render ? column.render(row[column.dataIndex], row) : row[column.dataIndex]}
              </td>
            ))}
          </tr>
        ))}
      </tbody>
    </table>
  );
};

export default Table;
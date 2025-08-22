import React, { useState } from 'react';

const Layout = ({ children }) => {
  const [sidebarOpen, setSidebarOpen] = useState(true);
  const userData = JSON.parse(localStorage.getItem('userData') || '{}');

  const dangXuat = () => {
    if (window.confirm('Bạn có chắc muốn đăng xuất?')) {
      localStorage.clear();
      window.location.href = '/dang-nhap';
    }
  };

  const toggleSidebar = () => setSidebarOpen(!sidebarOpen);

  const menuItems = [
    { name: 'Trang chủ', path: '/', icon: '🏠' },
    { name: 'Quản lý kho', path: '/quan-ly-kho', icon: '🏭' },
    { name: 'Hàng hóa', path: '/hang-hoa', icon: '📦' },
    { name: 'Nhập kho', path: '/nhap-kho', icon: '📥' },
    { name: 'Xuất kho', path: '/xuat-kho', icon: '📤' },
    { name: 'Báo cáo', path: '/bao-cao', icon: '📊' },
  ];

  return (
    <div style={{ display: 'flex', minHeight: '100vh', backgroundColor: '#f5f5f5' }}>
      {/* Sidebar */}
      <div style={{
        width: sidebarOpen ? '250px' : '60px',
        backgroundColor: '#2c3e50',
        color: 'white',
        transition: 'width 0.3s ease',
        position: 'fixed',
        height: '100vh',
        overflowY: 'auto',
        zIndex: 1000
      }}>
        {/* Header */}
        <div style={{
          padding: '1rem',
          borderBottom: '1px solid #34495e',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'space-between'
        }}>
          {sidebarOpen && (
            <span style={{ fontSize: '1.1rem', fontWeight: 'bold' }}>
              Kho Bệnh viện
            </span>
          )}
          <button
            onClick={toggleSidebar}
            style={{
              background: 'none',
              border: 'none',
              color: 'white',
              fontSize: '1.2rem',
              cursor: 'pointer',
              padding: '0.25rem'
            }}
          >
            {sidebarOpen ? '◀' : '▶'}
          </button>
        </div>

        {/* Menu */}
        <nav style={{ padding: '1rem 0' }}>
          {menuItems.map((item, index) => (
            <a
              key={index}
              href={item.path}
              style={{
                display: 'flex',
                alignItems: 'center',
                padding: '0.75rem 1rem',
                color: 'white',
                textDecoration: 'none',
                transition: 'background-color 0.2s',
                borderLeft: window.location.pathname === item.path ? '3px solid #3498db' : 'none'
              }}
              onMouseOver={(e) => e.target.style.backgroundColor = '#34495e'}
              onMouseOut={(e) => e.target.style.backgroundColor = 'transparent'}
            >
              <span style={{ marginRight: '0.75rem' }}>{item.icon}</span>
              {sidebarOpen && <span>{item.name}</span>}
            </a>
          ))}
        </nav>
      </div>

      {/* Main Content */}
      <div style={{
        marginLeft: sidebarOpen ? '250px' : '60px',
        flex: 1,
        transition: 'margin-left 0.3s ease'
      }}>
        {/* Top Bar */}
        <header style={{
          backgroundColor: 'white',
          padding: '1rem 2rem',
          boxShadow: '0 2px 4px rgba(0,0,0,0.1)',
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center'
        }}>
          <h1 style={{ margin: 0, fontSize: '1.5rem', color: '#2c3e50' }}>
            Hệ thống quản lý kho bệnh viện
          </h1>
          
          <div style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
            <div style={{ textAlign: 'right' }}>
              <div style={{ fontWeight: 'bold', color: '#2c3e50' }}>
                {userData.hoTen || 'Người dùng'}
              </div>
              <div style={{ fontSize: '0.8rem', color: '#7f8c8d' }}>
                {userData.tenKhoaPhong} • {userData.role}
              </div>
            </div>
            <button
              onClick={dangXuat}
              style={{
                backgroundColor: '#e74c3c',
                color: 'white',
                border: 'none',
                padding: '0.5rem 1rem',
                borderRadius: '4px',
                cursor: 'pointer',
                fontSize: '0.9rem'
              }}
            >
              Đăng xuất
            </button>
          </div>
        </header>

        {/* Page Content */}
        <main style={{ padding: '2rem' }}>
          {children}
        </main>
      </div>
    </div>
  );
};

export default Layout;
import React from 'react';
import Layout from '../components/layout/Layout';

const TrangChu = () => {
  const userData = JSON.parse(localStorage.getItem('userData') || '{}');

  const thongKe = [
    { tieuDe: 'Tổng kho', giaTri: '5', mauSac: '#3498db', icon: '🏭' },
    { tieuDe: 'Hàng hóa', giaTri: '247', mauSac: '#2ecc71', icon: '📦' },
    { tieuDe: 'Nhập hôm nay', giaTri: '12', mauSac: '#f39c12', icon: '📥' },
    { tieuDe: 'Xuất hôm nay', giaTri: '8', mauSac: '#e74c3c', icon: '📤' },
  ];

  const hoatDongGanDay = [
    { hanhDong: 'Nhập kho', vatPham: 'Găng tay y tế', thoiGian: '10 phút trước', loai: 'nhap' },
    { hanhDong: 'Xuất kho', vatPham: 'Khẩu trang N95', thoiGian: '25 phút trước', loai: 'xuat' },
    { hanhDong: 'Nhập kho', vatPham: 'Cồn sát khuẩn', thoiGian: '1 giờ trước', loai: 'nhap' },
    { hanhDong: 'Cập nhật', vatPham: 'Thông tin thuốc', thoiGian: '2 giờ trước', loai: 'capnhat' },
  ];

  return (
    <Layout>
      <div>
        {/* Chào mừng */}
        <div style={{
          backgroundColor: 'white',
          padding: '1.5rem',
          borderRadius: '8px',
          boxShadow: '0 2px 4px rgba(0,0,0,0.1)',
          marginBottom: '2rem'
        }}>
          <h2 style={{ margin: '0 0 0.5rem 0', color: '#2c3e50' }}>
            Chào mừng, {userData.hoTen}!
          </h2>
          <p style={{ margin: 0, color: '#7f8c8d' }}>
            {userData.tenKhoaPhong} • {userData.role}
          </p>
        </div>

        {/* Thống kê */}
        <div style={{
          display: 'grid',
          gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))',
          gap: '1.5rem',
          marginBottom: '2rem'
        }}>
          {thongKe.map((item, index) => (
            <div
              key={index}
              style={{
                backgroundColor: 'white',
                padding: '1.5rem',
                borderRadius: '8px',
                boxShadow: '0 2px 4px rgba(0,0,0,0.1)',
                border: `3px solid ${item.mauSac}`,
                textAlign: 'center'
              }}
            >
              <div style={{ fontSize: '2rem', marginBottom: '0.5rem' }}>
                {item.icon}
              </div>
              <h3 style={{
                margin: '0 0 0.5rem 0',
                color: item.mauSac,
                fontSize: '0.9rem',
                textTransform: 'uppercase',
                letterSpacing: '0.5px'
              }}>
                {item.tieuDe}
              </h3>
              <p style={{
                margin: 0,
                fontSize: '2rem',
                fontWeight: 'bold',
                color: '#2c3e50'
              }}>
                {item.giaTri}
              </p>
            </div>
          ))}
        </div>

        {/* Hoạt động gần đây */}
        <div style={{
          backgroundColor: 'white',
          borderRadius: '8px',
          boxShadow: '0 2px 4px rgba(0,0,0,0.1)',
          overflow: 'hidden'
        }}>
          <div style={{
            padding: '1.5rem',
            borderBottom: '1px solid #ecf0f1',
            backgroundColor: '#f8f9fa'
          }}>
            <h3 style={{ margin: 0, color: '#2c3e50' }}>
              Hoạt động gần đây
            </h3>
          </div>
          
          <div style={{ padding: '1rem' }}>
            {hoatDongGanDay.map((hoatDong, index) => (
              <div
                key={index}
                style={{
                  display: 'flex',
                  alignItems: 'center',
                  padding: '0.75rem',
                  borderRadius: '6px',
                  marginBottom: index < hoatDongGanDay.length - 1 ? '0.5rem' : 0,
                  backgroundColor: '#f8f9fa'
                }}
              >
                <div style={{
                  width: '40px',
                  height: '40px',
                  borderRadius: '50%',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  marginRight: '1rem',
                  backgroundColor: 
                    hoatDong.loai === 'nhap' ? '#d5f4e6' :
                    hoatDong.loai === 'xuat' ? '#ffeaa7' : '#ddd'
                }}>
                  {hoatDong.loai === 'nhap' ? '📥' : 
                   hoatDong.loai === 'xuat' ? '📤' : '✏️'}
                </div>
                <div style={{ flex: 1 }}>
                  <div style={{ fontWeight: '500', color: '#2c3e50' }}>
                    {hoatDong.hanhDong}: {hoatDong.vatPham}
                  </div>
                  <div style={{ fontSize: '0.8rem', color: '#7f8c8d' }}>
                    {hoatDong.thoiGian}
                  </div>
                </div>
              </div>
            ))}
          </div>
        </div>
      </div>
    </Layout>
  );
};

export default TrangChu;
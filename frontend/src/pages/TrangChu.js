import React, { useState, useEffect } from 'react';
import Layout from '../components/layout/Layout';
import api from '../services/api';

const TrangChu = () => {
  const userData = JSON.parse(localStorage.getItem('userData') || '{}');
  const [statistics, setStatistics] = useState({
    tongKho: 0,
    hangHoa: 0,
    nhapHomNay: 0,
    xuatHomNay: 0
  });
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchStatistics();
  }, []);

  const fetchStatistics = async () => {
    setLoading(true);
    try {
      // Fetch real statistics from APIs
      const [khoResponse, hangHoaResponse] = await Promise.all([
        api.get('/api/kho/statistics/count').catch(() => ({ data: { data: 0 } })),
        api.get('/api/hang-hoa?size=1').catch(() => ({ data: { data: { totalElements: 0 } } }))
      ]);

      setStatistics({
        tongKho: khoResponse.data.data || 0,
        hangHoa: hangHoaResponse.data.data?.totalElements || 0,
        nhapHomNay: 12, // Will be replaced with real data when APIs are available
        xuatHomNay: 8   // Will be replaced with real data when APIs are available
      });
    } catch (error) {
      console.error('Error fetching statistics:', error);
      // Keep default values if API calls fail
    } finally {
      setLoading(false);
    }
  };

  const thongKe = [
    { tieuDe: 'Tổng kho', giaTri: statistics.tongKho.toString(), mauSac: '#3498db', icon: '🏭' },
    { tieuDe: 'Hàng hóa', giaTri: statistics.hangHoa.toString(), mauSac: '#2ecc71', icon: '📦' },
    { tieuDe: 'Nhập hôm nay', giaTri: statistics.nhapHomNay.toString(), mauSac: '#f39c12', icon: '📥' },
    { tieuDe: 'Xuất hôm nay', giaTri: statistics.xuatHomNay.toString(), mauSac: '#e74c3c', icon: '📤' },
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
                {loading ? '...' : item.giaTri}
              </p>
            </div>
          ))}
        </div>

        {/* Quick Actions */}
        <div style={{
          display: 'grid',
          gridTemplateColumns: 'repeat(auto-fit, minmax(250px, 1fr))',
          gap: '1rem',
          marginBottom: '2rem'
        }}>
          <button
            onClick={() => window.location.href = '/quan-ly-kho'}
            style={{
              backgroundColor: '#3498db',
              color: 'white',
              border: 'none',
              padding: '1rem',
              borderRadius: '8px',
              cursor: 'pointer',
              fontSize: '1rem',
              fontWeight: '500',
              display: 'flex',
              alignItems: 'center',
              gap: '0.5rem'
            }}
          >
            🏭 Quản lý Kho
          </button>
          <button
            onClick={() => window.location.href = '/hang-hoa'}
            style={{
              backgroundColor: '#2ecc71',
              color: 'white',
              border: 'none',
              padding: '1rem',
              borderRadius: '8px',
              cursor: 'pointer',
              fontSize: '1rem',
              fontWeight: '500',
              display: 'flex',
              alignItems: 'center',
              gap: '0.5rem'
            }}
          >
            📦 Quản lý Hàng hóa
          </button>
          <button
            onClick={() => window.location.href = '/nhap-kho'}
            style={{
              backgroundColor: '#f39c12',
              color: 'white',
              border: 'none',
              padding: '1rem',
              borderRadius: '8px',
              cursor: 'pointer',
              fontSize: '1rem',
              fontWeight: '500',
              display: 'flex',
              alignItems: 'center',
              gap: '0.5rem'
            }}
          >
            📥 Nhập Kho
          </button>
          <button
            onClick={() => window.location.href = '/xuat-kho'}
            style={{
              backgroundColor: '#e74c3c',
              color: 'white',
              border: 'none',
              padding: '1rem',
              borderRadius: '8px',
              cursor: 'pointer',
              fontSize: '1rem',
              fontWeight: '500',
              display: 'flex',
              alignItems: 'center',
              gap: '0.5rem'
            }}
          >
            📤 Xuất Kho
          </button>
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
import React, { useState, useEffect } from 'react';
import Layout from '../components/layout/Layout';
import api from '../services/api';
import {
  BarChart, Bar, LineChart, Line, PieChart, Pie, Cell,
  XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer
} from 'recharts';
import {
  Package, TrendingUp, TrendingDown, AlertTriangle,
  Clock, ShoppingCart, Database, Activity
} from 'lucide-react';

const TrangChu = () => {
  const userData = JSON.parse(localStorage.getItem('userData') || '{}');

  const [statistics, setStatistics] = useState({
    tongKho: 0,
    tongHangHoa: 0,
    tongTonKho: 0,
    hangSapHetHan: 0,
    hangCanNhap: 0,
    phieuNhapHomNay: 0,
    phieuXuatHomNay: 0,
    giaTriTonKho: 0
  });

  const [recentActivities, setRecentActivities] = useState([]);
  const [lowStockItems, setLowStockItems] = useState([]);
  const [expiringItems, setExpiringItems] = useState([]);
  const [chartData, setChartData] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchDashboardData();
  }, []);

  const fetchDashboardData = async () => {
    setLoading(true);
    try {
      // 1. Fetch statistics - Wrap riêng từng API để handle lỗi
      const khoRes = await api.get('/api/kho/statistics/count').catch(() => ({ data: { data: 0 } }));
      const hangHoaRes = await api.get('/api/hang-hoa?size=1').catch(() => ({ data: { data: { totalElements: 0 } } }));

      // ✅ SỬA: THÊM sortBy và sortDir để lấy phiếu MỚI NHẤT
      const phieuNhapRes = await api.get('/api/phieu-nhap', {
        params: {
          page: 0,
          size: 50,  // ✅ Tăng lên để đủ dữ liệu
          sortBy: 'createdAt',  // ✅ Sắp xếp theo thời gian tạo
          sortDir: 'desc'        // ✅ Mới nhất trước
        }
      }).catch(() => ({ data: { data: { content: [] } } }));

      const phieuXuatRes = await api.get('/api/phieu-xuat', {
        params: {
          page: 0,
          size: 50,
          sortBy: 'createdAt',
          sortDir: 'desc'
        }
      }).catch(() => ({ data: { data: { content: [] } } }));

      // ⚠️ API cảnh báo có thể bị lỗi - skip nếu fail
      let canhBaoData = [];
      try {
        const canhBaoRes = await api.get('/api/canh-bao?page=0&size=100');
        canhBaoData = canhBaoRes.data.data?.content || [];
      } catch (error) {
        console.warn('⚠️ API cảnh báo không khả dụng, sử dụng dữ liệu mặc định');
        canhBaoData = [];
      }

      // Calculate statistics
      const today = new Date().toISOString().split('T')[0];

      const phieuNhapContent = phieuNhapRes.data.data?.content || [];
      const phieuXuatContent = phieuXuatRes.data.data?.content || [];

      // ✅ SỬA: Tính phiếu hôm nay dựa trên createdAt (chính xác hơn)
      const phieuNhapHomNay = phieuNhapContent.filter(p => {
        if (!p.createdAt) return false;
        const createdDate = p.createdAt.split('T')[0] || p.createdAt.split(' ')[0];
        return createdDate === today;
      }).length;

      const phieuXuatHomNay = phieuXuatContent.filter(p => {
        if (!p.createdAt) return false;
        const createdDate = p.createdAt.split('T')[0] || p.createdAt.split(' ')[0];
        return createdDate === today;
      }).length;

      const hangSapHetHan = canhBaoData.filter(cb =>
        cb.loaiCanhBao === 'GAN_HET_HAN' && !cb.daXuLy
      ).length;

      const hangCanNhap = canhBaoData.filter(cb =>
        cb.loaiCanhBao === 'TON_KHO_THAP' && !cb.daXuLy
      ).length;

      // Calculate total inventory value
      let totalValue = 0;
      let totalStock = 0;

      try {
        const hangHoaListRes = await api.get('/api/hang-hoa?size=1000');
        const hangHoaList = hangHoaListRes.data.data?.content || [];

        totalValue = hangHoaList.reduce((sum, item) =>
          sum + ((item.soLuongCoTheXuat || 0) * (item.giaNhapTrungBinh || 0)), 0
        );

        totalStock = hangHoaList.reduce((sum, item) =>
          sum + (item.soLuongCoTheXuat || 0), 0
        );
      } catch (error) {
        console.warn('⚠️ Không thể tính tổng tồn kho');
      }

      setStatistics({
        tongKho: khoRes.data.data || 0,
        tongHangHoa: hangHoaRes.data.data?.totalElements || 0,
        tongTonKho: totalStock,
        hangSapHetHan: hangSapHetHan,
        hangCanNhap: hangCanNhap,
        phieuNhapHomNay: phieuNhapHomNay,
        phieuXuatHomNay: phieuXuatHomNay,
        giaTriTonKho: totalValue
      });

      // 2. Fetch low stock items
      try {
        const lowStockRes = await api.get('/api/hang-hoa?page=0&size=100&sort=soLuongCoTheXuat,asc');
        const allItems = lowStockRes.data.data?.content || [];
        setLowStockItems(allItems.filter(item =>
          (item.soLuongCoTheXuat || 0) < (item.soLuongToiThieu || 0) && item.soLuongToiThieu > 0
        ).slice(0, 5));
      } catch (error) {
        console.warn('⚠️ Không thể lấy danh sách hàng tồn kho thấp');
        setLowStockItems([]);
      }

      // 3. Fetch expiring items
      try {
        const loHangRes = await api.get('/api/lo-hang?page=0&size=100&sort=hanSuDung,asc');
        const loHangList = loHangRes.data.data?.content || [];
        const now = new Date();
        const threeMonthsLater = new Date(now.getTime() + 90 * 24 * 60 * 60 * 1000);

        setExpiringItems(loHangList.filter(item => {
          if (!item.hanSuDung) return false;
          const expiryDate = new Date(item.hanSuDung);
          return expiryDate <= threeMonthsLater && expiryDate > now && item.soLuongHienTai > 0;
        }).slice(0, 5));
      } catch (error) {
        console.warn('⚠️ Không thể lấy danh sách lô hàng sắp hết hạn');
        setExpiringItems([]);
      }

      // ✅ 4. SỬA: Fetch recent activities - ĐÚNG DỮ LIỆU MỚI NHẤT
      const activities = [];

      try {
        // ✅ Recent imports - Backend đã sort DESC, lấy 5 đầu = 5 mới nhất
        const recentImports = phieuNhapContent
          .filter(p => p.trangThai === 'DA_DUYET')
          .slice(0, 5)
          .map(p => ({
            type: 'nhap',
            title: `Nhập kho ${p.maPhieuNhap}`,
            description: `Kho: ${p.tenKho || 'N/A'}`,
            time: formatTimeAgo(p.createdAt),  // ✅ Dùng createdAt (thời gian thực)
            timestamp: new Date(p.createdAt || Date.now())
          }));

        // ✅ Recent exports - Backend đã sort DESC, lấy 5 đầu = 5 mới nhất
        const recentExports = phieuXuatContent
          .filter(p => p.trangThai === 'DA_DUYET')
          .slice(0, 5)
          .map(p => ({
            type: 'xuat',
            title: `Xuất kho ${p.maPhieuXuat}`,
            description: `Kho: ${p.tenKho || 'N/A'}`,
            time: formatTimeAgo(p.createdAt),  // ✅ Dùng createdAt
            timestamp: new Date(p.createdAt || Date.now())
          }));

        // ✅ Gộp và sắp xếp lại theo timestamp
        activities.push(...recentImports, ...recentExports);
        activities.sort((a, b) => b.timestamp - a.timestamp);

        // ✅ Lấy 10 hoạt động mới nhất
        setRecentActivities(activities.slice(0, 10));

        console.log('✅ Recent activities loaded:', activities.slice(0, 10));
      } catch (error) {
        console.warn('⚠️ Không thể lấy danh sách hoạt động gần đây');
        setRecentActivities([]);
      }

      // 5. Chart data - Last 7 days imports/exports (GIỮ NGUYÊN dùng ngayNhap/ngayXuat)
      try {
        const last7Days = [];
        for (let i = 6; i >= 0; i--) {
          const date = new Date();
          date.setDate(date.getDate() - i);
          const dateStr = date.toISOString().split('T')[0];

          const nhapCount = phieuNhapContent.filter(p =>
            p.ngayNhap?.startsWith(dateStr) && p.trangThai === 'DA_DUYET'
          ).length;

          const xuatCount = phieuXuatContent.filter(p =>
            p.ngayXuat?.startsWith(dateStr) && p.trangThai === 'DA_DUYET'
          ).length;

          last7Days.push({
            date: date.toLocaleDateString('vi-VN', { day: '2-digit', month: '2-digit' }),
            nhap: nhapCount,
            xuat: xuatCount
          });
        }
        setChartData(last7Days);
      } catch (error) {
        console.warn('⚠️ Không thể tạo dữ liệu biểu đồ');
        setChartData([]);
      }

    } catch (error) {
      console.error('❌ Error fetching dashboard data:', error);
      setStatistics({
        tongKho: 0,
        tongHangHoa: 0,
        tongTonKho: 0,
        hangSapHetHan: 0,
        hangCanNhap: 0,
        phieuNhapHomNay: 0,
        phieuXuatHomNay: 0,
        giaTriTonKho: 0
      });
    } finally {
      setLoading(false);
    }
  };

  const formatTimeAgo = (dateString) => {
    if (!dateString) return 'N/A';
    const date = new Date(dateString);
    const now = new Date();
    const diffMs = now - date;
    const diffMins = Math.floor(diffMs / 60000);
    const diffHours = Math.floor(diffMs / 3600000);
    const diffDays = Math.floor(diffMs / 86400000);

    if (diffMins < 60) return `${diffMins} phút trước`;
    if (diffHours < 24) return `${diffHours} giờ trước`;
    return `${diffDays} ngày trước`;
  };

  const formatCurrency = (value) => {
    return new Intl.NumberFormat('vi-VN', {
      style: 'currency',
      currency: 'VND'
    }).format(value);
  };

  const StatCard = ({ icon: Icon, title, value, subtitle, color, trend }) => (
    <div style={{
      backgroundColor: 'white',
      borderRadius: '12px',
      padding: '1.5rem',
      boxShadow: '0 2px 8px rgba(0,0,0,0.08)',
      border: `2px solid ${color}15`,
      transition: 'all 0.3s ease',
      cursor: 'pointer'
    }}
      onMouseEnter={(e) => {
        e.currentTarget.style.transform = 'translateY(-4px)';
        e.currentTarget.style.boxShadow = '0 4px 16px rgba(0,0,0,0.12)';
      }}
      onMouseLeave={(e) => {
        e.currentTarget.style.transform = 'translateY(0)';
        e.currentTarget.style.boxShadow = '0 2px 8px rgba(0,0,0,0.08)';
      }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
        <div>
          <p style={{
            margin: '0 0 0.5rem 0',
            fontSize: '0.875rem',
            color: '#64748b',
            fontWeight: '500'
          }}>
            {title}
          </p>
          <h2 style={{
            margin: '0 0 0.5rem 0',
            fontSize: '2rem',
            fontWeight: '700',
            color: '#1e293b'
          }}>
            {loading ? '...' : value}
          </h2>
          {subtitle && (
            <p style={{
              margin: 0,
              fontSize: '0.75rem',
              color: '#94a3b8'
            }}>
              {subtitle}
            </p>
          )}
        </div>
        <div style={{
          width: '56px',
          height: '56px',
          borderRadius: '12px',
          backgroundColor: `${color}15`,
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center'
        }}>
          <Icon size={28} color={color} strokeWidth={2} />
        </div>
      </div>
      {trend && (
        <div style={{
          marginTop: '1rem',
          display: 'flex',
          alignItems: 'center',
          gap: '0.5rem'
        }}>
          {trend > 0 ? (
            <TrendingUp size={16} color="#10b981" />
          ) : (
            <TrendingDown size={16} color="#ef4444" />
          )}
          <span style={{
            fontSize: '0.75rem',
            color: trend > 0 ? '#10b981' : '#ef4444',
            fontWeight: '500'
          }}>
            {Math.abs(trend)}% so với tuần trước
          </span>
        </div>
      )}
    </div>
  );

  return (
    <Layout>
      <div style={{ padding: '0.5rem' }}>
        {/* Welcome Section */}
        <div style={{
          background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
          borderRadius: '16px',
          padding: '2rem',
          marginBottom: '2rem',
          color: 'white',
          boxShadow: '0 4px 20px rgba(102, 126, 234, 0.4)'
        }}>
          <h1 style={{
            margin: '0 0 0.5rem 0',
            fontSize: '2rem',
            fontWeight: '700'
          }}>
            Xin chào, {userData.hoTen}! 👋
          </h1>
          <p style={{
            margin: 0,
            fontSize: '1rem',
            opacity: 0.9
          }}>
            {userData.tenKhoaPhong} • {userData.role}
          </p>
          <p style={{
            margin: '1rem 0 0 0',
            fontSize: '0.875rem',
            opacity: 0.8
          }}>
            📅 {new Date().toLocaleDateString('vi-VN', {
              weekday: 'long',
              year: 'numeric',
              month: 'long',
              day: 'numeric'
            })}
          </p>
        </div>

        {/* Statistics Grid */}
        <div style={{
          display: 'grid',
          gridTemplateColumns: 'repeat(auto-fit, minmax(240px, 1fr))',
          gap: '1.5rem',
          marginBottom: '2rem'
        }}>
          <StatCard
            icon={Database}
            title="Tổng Kho"
            value={statistics.tongKho}
            subtitle="Kho hoạt động"
            color="#3b82f6"
          />
          <StatCard
            icon={Package}
            title="Hàng Hóa"
            value={statistics.tongHangHoa}
            subtitle="Mặt hàng đang quản lý"
            color="#10b981"
          />
          <StatCard
            icon={ShoppingCart}
            title="Tồn Kho"
            value={statistics.tongTonKho.toLocaleString()}
            subtitle={formatCurrency(statistics.giaTriTonKho)}
            color="#8b5cf6"
          />
          <StatCard
            icon={AlertTriangle}
            title="Cần Nhập"
            value={statistics.hangCanNhap}
            subtitle="Hàng dưới mức tối thiểu"
            color="#f59e0b"
          />
          <StatCard
            icon={Clock}
            title="Sắp Hết Hạn"
            value={statistics.hangSapHetHan}
            subtitle="Trong 90 ngày tới"
            color="#ef4444"
          />
          <StatCard
            icon={TrendingUp}
            title="Nhập Hôm Nay"
            value={statistics.phieuNhapHomNay}
            subtitle="Phiếu nhập đã duyệt"
            color="#06b6d4"
          />
          <StatCard
            icon={TrendingDown}
            title="Xuất Hôm Nay"
            value={statistics.phieuXuatHomNay}
            subtitle="Phiếu xuất đã duyệt"
            color="#ec4899"
          />
          <StatCard
            icon={Activity}
            title="Hoạt Động"
            value={recentActivities.length}
            subtitle="Giao dịch gần đây"
            color="#14b8a6"
          />
        </div>

        {/* Charts Row */}
        <div style={{
          display: 'grid',
          gridTemplateColumns: 'repeat(auto-fit, minmax(400px, 1fr))',
          gap: '1.5rem',
          marginBottom: '2rem'
        }}>
          {/* Import/Export Chart */}
          <div style={{
            backgroundColor: 'white',
            borderRadius: '12px',
            padding: '1.5rem',
            boxShadow: '0 2px 8px rgba(0,0,0,0.08)'
          }}>
            <h3 style={{
              margin: '0 0 1.5rem 0',
              fontSize: '1.125rem',
              fontWeight: '600',
              color: '#1e293b'
            }}>
              📊 Nhập/Xuất 7 Ngày Qua
            </h3>
            <ResponsiveContainer width="100%" height={250}>
              <LineChart data={chartData}>
                <CartesianGrid strokeDasharray="3 3" stroke="#e2e8f0" />
                <XAxis
                  dataKey="date"
                  tick={{ fontSize: 12 }}
                  stroke="#94a3b8"
                />
                <YAxis
                  tick={{ fontSize: 12 }}
                  stroke="#94a3b8"
                />
                <Tooltip
                  contentStyle={{
                    backgroundColor: 'white',
                    border: '1px solid #e2e8f0',
                    borderRadius: '8px',
                    boxShadow: '0 4px 12px rgba(0,0,0,0.1)'
                  }}
                />
                <Legend />
                <Line
                  type="monotone"
                  dataKey="nhap"
                  stroke="#10b981"
                  strokeWidth={2}
                  name="Nhập kho"
                  dot={{ fill: '#10b981', r: 4 }}
                />
                <Line
                  type="monotone"
                  dataKey="xuat"
                  stroke="#ef4444"
                  strokeWidth={2}
                  name="Xuất kho"
                  dot={{ fill: '#ef4444', r: 4 }}
                />
              </LineChart>
            </ResponsiveContainer>
          </div>

          {/* Quick Actions */}
          <div style={{
            backgroundColor: 'white',
            borderRadius: '12px',
            padding: '1.5rem',
            boxShadow: '0 2px 8px rgba(0,0,0,0.08)'
          }}>
            <h3 style={{
              margin: '0 0 1.5rem 0',
              fontSize: '1.125rem',
              fontWeight: '600',
              color: '#1e293b'
            }}>
              ⚡ Thao Tác Nhanh
            </h3>
            <div style={{
              display: 'grid',
              gridTemplateColumns: 'repeat(2, 1fr)',
              gap: '1rem'
            }}>
              {[
                { label: '📥 Nhập Kho', path: '/nhap-kho', color: '#10b981' },
                { label: '📤 Xuất Kho', path: '/xuat-kho', color: '#ef4444' },
                { label: '📦 Hàng Hóa', path: '/hang-hoa', color: '#3b82f6' },
                { label: '🏭 Quản Lý Kho', path: '/quan-ly-kho', color: '#8b5cf6' },
                { label: '⚠️ Cảnh Báo', path: '/canh-bao', color: '#f59e0b' },
                { label: '📊 Báo Cáo', path: '/bao-cao', color: '#06b6d4' }
              ].map((action, idx) => (
                <button
                  key={idx}
                  onClick={() => window.location.href = action.path}
                  style={{
                    backgroundColor: `${action.color}10`,
                    border: `2px solid ${action.color}`,
                    color: action.color,
                    padding: '1rem',
                    borderRadius: '8px',
                    fontSize: '0.9rem',
                    fontWeight: '600',
                    cursor: 'pointer',
                    transition: 'all 0.2s',
                    textAlign: 'center'
                  }}
                  onMouseEnter={(e) => {
                    e.currentTarget.style.backgroundColor = action.color;
                    e.currentTarget.style.color = 'white';
                    e.currentTarget.style.transform = 'scale(1.05)';
                  }}
                  onMouseLeave={(e) => {
                    e.currentTarget.style.backgroundColor = `${action.color}10`;
                    e.currentTarget.style.color = action.color;
                    e.currentTarget.style.transform = 'scale(1)';
                  }}
                >
                  {action.label}
                </button>
              ))}
            </div>
          </div>
        </div>

        {/* Bottom Section */}
        <div style={{
          display: 'grid',
          gridTemplateColumns: 'repeat(auto-fit, minmax(300px, 1fr))',
          gap: '1.5rem'
        }}>
          {/* Recent Activities */}
          <div style={{
            backgroundColor: 'white',
            borderRadius: '12px',
            padding: '1.5rem',
            boxShadow: '0 2px 8px rgba(0,0,0,0.08)'
          }}>
            <h3 style={{
              margin: '0 0 1rem 0',
              fontSize: '1.125rem',
              fontWeight: '600',
              color: '#1e293b'
            }}>
              🕐 Hoạt Động Gần Đây
            </h3>
            <div style={{ maxHeight: '300px', overflowY: 'auto' }}>
              {recentActivities.length === 0 ? (
                <p style={{ color: '#94a3b8', textAlign: 'center', padding: '2rem 0' }}>
                  Chưa có hoạt động nào
                </p>
              ) : (
                recentActivities.map((activity, idx) => (
                  <div
                    key={idx}
                    style={{
                      display: 'flex',
                      alignItems: 'center',
                      gap: '1rem',
                      padding: '0.75rem',
                      borderRadius: '8px',
                      marginBottom: '0.5rem',
                      backgroundColor: '#f8fafc',
                      border: '1px solid #e2e8f0'
                    }}
                  >
                    <div style={{
                      width: '40px',
                      height: '40px',
                      borderRadius: '50%',
                      backgroundColor: activity.type === 'nhap' ? '#d1fae5' : '#fee2e2',
                      display: 'flex',
                      alignItems: 'center',
                      justifyContent: 'center',
                      fontSize: '1.25rem'
                    }}>
                      {activity.type === 'nhap' ? '📥' : '📤'}
                    </div>
                    <div style={{ flex: 1 }}>
                      <div style={{
                        fontWeight: '500',
                        color: '#1e293b',
                        fontSize: '0.875rem'
                      }}>
                        {activity.title}
                      </div>
                      <div style={{
                        fontSize: '0.75rem',
                        color: '#64748b',
                        marginTop: '0.25rem'
                      }}>
                        {activity.description} • {activity.time}
                      </div>
                    </div>
                  </div>
                ))
              )}
            </div>
          </div>

          {/* Low Stock Items */}
          <div style={{
            backgroundColor: 'white',
            borderRadius: '12px',
            padding: '1.5rem',
            boxShadow: '0 2px 8px rgba(0,0,0,0.08)'
          }}>
            <h3 style={{
              margin: '0 0 1rem 0',
              fontSize: '1.125rem',
              fontWeight: '600',
              color: '#1e293b'
            }}>
              ⚠️ Hàng Cần Nhập
            </h3>
            <div style={{ maxHeight: '300px', overflowY: 'auto' }}>
              {lowStockItems.length === 0 ? (
                <p style={{ color: '#94a3b8', textAlign: 'center', padding: '2rem 0' }}>
                  Tất cả hàng hóa đều đủ tồn kho ✅
                </p>
              ) : (
                lowStockItems.map((item, idx) => (
                  <div
                    key={idx}
                    style={{
                      padding: '0.75rem',
                      borderRadius: '8px',
                      marginBottom: '0.5rem',
                      backgroundColor: '#fef3c7',
                      border: '1px solid #fbbf24'
                    }}
                  >
                    <div style={{
                      fontWeight: '500',
                      color: '#92400e',
                      fontSize: '0.875rem',
                      marginBottom: '0.25rem'
                    }}>
                      {item.tenHangHoa}
                    </div>
                    <div style={{
                      fontSize: '0.75rem',
                      color: '#78350f'
                    }}>
                      Tồn: {item.soLuongCoTheXuat} / Tối thiểu: {item.soLuongToiThieu}
                    </div>
                  </div>
                ))
              )}
            </div>
          </div>

          {/* Expiring Items */}
          <div style={{
            backgroundColor: 'white',
            borderRadius: '12px',
            padding: '1.5rem',
            boxShadow: '0 2px 8px rgba(0,0,0,0.08)'
          }}>
            <h3 style={{
              margin: '0 0 1rem 0',
              fontSize: '1.125rem',
              fontWeight: '600',
              color: '#1e293b'
            }}>
              ⏰ Sắp Hết Hạn
            </h3>
            <div style={{ maxHeight: '300px', overflowY: 'auto' }}>
              {expiringItems.length === 0 ? (
                <p style={{ color: '#94a3b8', textAlign: 'center', padding: '2rem 0' }}>
                  Không có lô hàng sắp hết hạn ✅
                </p>
              ) : (
                expiringItems.map((item, idx) => (
                  <div
                    key={idx}
                    style={{
                      padding: '0.75rem',
                      borderRadius: '8px',
                      marginBottom: '0.5rem',
                      backgroundColor: '#fecaca',
                      border: '1px solid #ef4444'
                    }}
                  >
                    <div style={{
                      fontWeight: '500',
                      color: '#7f1d1d',
                      fontSize: '0.875rem',
                      marginBottom: '0.25rem'
                    }}>
                      Lô: {item.soLo}
                    </div>
                    <div style={{
                      fontSize: '0.75rem',
                      color: '#991b1b'
                    }}>
                      HSD: {new Date(item.hanSuDung).toLocaleDateString('vi-VN')} •
                      SL: {item.soLuongHienTai}
                    </div>
                  </div>
                ))
              )}
            </div>
          </div>
        </div>
      </div>
    </Layout>
  );
};

export default TrangChu;
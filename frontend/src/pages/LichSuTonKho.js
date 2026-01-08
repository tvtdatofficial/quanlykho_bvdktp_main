import React, { useState, useEffect } from 'react';
import Layout from '../components/layout/Layout';
import api from '../services/api';
import { 
  LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer 
} from 'recharts';
import { 
  History, Filter, Search, Calendar, TrendingUp, TrendingDown,
  Package, MapPin, FileText, User, Clock, ArrowUpCircle, ArrowDownCircle,
  RefreshCw, Download, Eye
} from 'lucide-react';

const LichSuTonKho = () => {
  // States
  const [lichSuList, setLichSuList] = useState([]);
  const [loading, setLoading] = useState(false);
  const [pagination, setPagination] = useState({
    page: 0,
    size: 20,
    totalPages: 0,
    totalElements: 0
  });

  // Filter states
  const [filters, setFilters] = useState({
    hangHoaId: '',
    viTriKhoId: '',
    loaiBienDong: '',
    tuNgay: '',
    denNgay: '',
    keyword: ''
  });

  // Dropdown data
  const [hangHoaList, setHangHoaList] = useState([]);
  const [viTriKhoList, setViTriKhoList] = useState([]);

  // Chart data
  const [selectedHangHoaForChart, setSelectedHangHoaForChart] = useState(null);
  const [chartData, setChartData] = useState([]);
  const [showChartModal, setShowChartModal] = useState(false);

  // Loại biến động options
  const loaiBienDongOptions = [
    { value: '', label: 'Tất cả' },
    { value: 'NHAP_KHO', label: 'Nhập kho' },
    { value: 'XUAT_KHO', label: 'Xuất kho' },
    { value: 'DIEU_CHINH', label: 'Điều chỉnh' },
    { value: 'KIEM_KE', label: 'Kiểm kê' },
    { value: 'HUY_HANG', label: 'Hủy hàng' },
    { value: 'CHUYEN_KHO', label: 'Chuyển kho' },
    { value: 'HUY_DUYET_NHAP', label: 'Hủy duyệt nhập' },
    { value: 'HUY_DUYET_XUAT', label: 'Hủy duyệt xuất' }
  ];

  useEffect(() => {
    fetchLichSuTonKho();
    fetchHangHoaList();
    fetchViTriKhoList();
  }, [pagination.page, filters]);

  // Fetch lịch sử
  const fetchLichSuTonKho = async () => {
    setLoading(true);
    try {
      const params = {
        page: pagination.page,
        size: pagination.size,
        ...filters
      };

      // Remove empty filters
      Object.keys(params).forEach(key => {
        if (params[key] === '' || params[key] === null) {
          delete params[key];
        }
      });

      const response = await api.get('/api/lich-su-ton-kho', { params });
      
      if (response.data.success) {
        setLichSuList(response.data.data.content || []);
        setPagination(prev => ({
          ...prev,
          totalPages: response.data.data.totalPages || 0,
          totalElements: response.data.data.totalElements || 0
        }));
      }
    } catch (error) {
      console.error('Error fetching lich su ton kho:', error);
      alert('Lỗi khi tải lịch sử tồn kho!');
    } finally {
      setLoading(false);
    }
  };

  // Fetch danh sách hàng hóa
  const fetchHangHoaList = async () => {
    try {
      const response = await api.get('/api/hang-hoa', {
        params: { page: 0, size: 1000 }
      });
      if (response.data.success) {
        setHangHoaList(response.data.data.content || []);
      }
    } catch (error) {
      console.error('Error fetching hang hoa:', error);
    }
  };

  // Fetch danh sách vị trí kho
  const fetchViTriKhoList = async () => {
    try {
      const response = await api.get('/api/vi-tri-kho', {
        params: { page: 0, size: 1000 }
      });
      if (response.data.success) {
        setViTriKhoList(response.data.data.content || []);
      }
    } catch (error) {
      console.error('Error fetching vi tri kho:', error);
    }
  };

  // Fetch chart data
  const fetchChartData = async (hangHoaId, days = 30) => {
    try {
      const response = await api.get(`/api/lich-su-ton-kho/chart/${hangHoaId}`, {
        params: { days }
      });
      
      if (response.data.success) {
        const data = response.data.data || [];
        // Transform data for chart
        const chartData = data.map(item => ({
          date: new Date(item.createdAt).toLocaleDateString('vi-VN'),
          tonKho: item.soLuongSau,
          bienDong: item.soLuongBienDong
        }));
        setChartData(chartData);
      }
    } catch (error) {
      console.error('Error fetching chart data:', error);
    }
  };

  // Handle filter change
  const handleFilterChange = (name, value) => {
    setFilters(prev => ({ ...prev, [name]: value }));
    setPagination(prev => ({ ...prev, page: 0 })); // Reset to page 0
  };

  // Handle reset filters
  const handleResetFilters = () => {
    setFilters({
      hangHoaId: '',
      viTriKhoId: '',
      loaiBienDong: '',
      tuNgay: '',
      denNgay: '',
      keyword: ''
    });
    setPagination(prev => ({ ...prev, page: 0 }));
  };

  // Handle view chart
  const handleViewChart = (hangHoa) => {
    setSelectedHangHoaForChart(hangHoa);
    fetchChartData(hangHoa.hangHoaId);
    setShowChartModal(true);
  };

  // Get icon by loai bien dong
  const getBienDongIcon = (loaiBienDong) => {
    switch (loaiBienDong) {
      case 'NHAP_KHO':
        return <ArrowDownCircle size={20} color="#10b981" />;
      case 'XUAT_KHO':
        return <ArrowUpCircle size={20} color="#ef4444" />;
      case 'DIEU_CHINH':
        return <RefreshCw size={20} color="#f59e0b" />;
      case 'KIEM_KE':
        return <FileText size={20} color="#3b82f6" />;
      case 'HUY_HANG':
        return <TrendingDown size={20} color="#ef4444" />;
      case 'CHUYEN_KHO':
        return <RefreshCw size={20} color="#8b5cf6" />;
      default:
        return <History size={20} color="#6b7280" />;
    }
  };

  // Get color by loai bien dong
  const getBienDongColor = (loaiBienDong) => {
    switch (loaiBienDong) {
      case 'NHAP_KHO':
        return '#10b981';
      case 'XUAT_KHO':
        return '#ef4444';
      case 'DIEU_CHINH':
        return '#f59e0b';
      case 'KIEM_KE':
        return '#3b82f6';
      case 'HUY_HANG':
        return '#ef4444';
      case 'CHUYEN_KHO':
        return '#8b5cf6';
      default:
        return '#6b7280';
    }
  };

  // Format currency
  const formatCurrency = (value) => {
    if (!value) return '0 ₫';
    return new Intl.NumberFormat('vi-VN', {
      style: 'currency',
      currency: 'VND'
    }).format(value);
  };

  return (
    <Layout>
      <div style={{ padding: '1.5rem' }}>
        {/* Header */}
        <div style={{
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center',
          marginBottom: '2rem'
        }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
            <div style={{
              width: '48px',
              height: '48px',
              borderRadius: '12px',
              background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center'
            }}>
              <History size={24} color="white" />
            </div>
            <div>
              <h1 style={{ margin: 0, fontSize: '1.75rem', fontWeight: '700', color: '#1e293b' }}>
                Lịch Sử Tồn Kho
              </h1>
              <p style={{ margin: '0.25rem 0 0 0', color: '#64748b', fontSize: '0.875rem' }}>
                Theo dõi biến động tồn kho theo thời gian
              </p>
            </div>
          </div>

          <button
            onClick={fetchLichSuTonKho}
            disabled={loading}
            style={{
              display: 'flex',
              alignItems: 'center',
              gap: '0.5rem',
              padding: '0.75rem 1.5rem',
              backgroundColor: '#3b82f6',
              color: 'white',
              border: 'none',
              borderRadius: '8px',
              fontSize: '0.875rem',
              fontWeight: '600',
              cursor: loading ? 'not-allowed' : 'pointer',
              transition: 'all 0.2s',
              opacity: loading ? 0.6 : 1
            }}
            onMouseEnter={(e) => !loading && (e.currentTarget.style.backgroundColor = '#2563eb')}
            onMouseLeave={(e) => !loading && (e.currentTarget.style.backgroundColor = '#3b82f6')}
          >
            <RefreshCw size={16} />
            Làm mới
          </button>
        </div>

        {/* Filters */}
        <div style={{
          backgroundColor: 'white',
          borderRadius: '12px',
          padding: '1.5rem',
          marginBottom: '1.5rem',
          boxShadow: '0 2px 8px rgba(0,0,0,0.08)'
        }}>
          <div style={{
            display: 'flex',
            alignItems: 'center',
            gap: '0.5rem',
            marginBottom: '1rem'
          }}>
            <Filter size={20} color="#3b82f6" />
            <h3 style={{ margin: 0, fontSize: '1rem', fontWeight: '600', color: '#1e293b' }}>
              Bộ lọc
            </h3>
          </div>

          <div style={{
            display: 'grid',
            gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))',
            gap: '1rem'
          }}>
            {/* Keyword Search */}
            <div>
              <label style={{ display: 'block', marginBottom: '0.5rem', fontSize: '0.875rem', fontWeight: '500', color: '#374151' }}>
                Tìm kiếm
              </label>
              <div style={{ position: 'relative' }}>
                <Search size={18} style={{ position: 'absolute', left: '12px', top: '50%', transform: 'translateY(-50%)', color: '#9ca3af' }} />
                <input
                  type="text"
                  value={filters.keyword}
                  onChange={(e) => handleFilterChange('keyword', e.target.value)}
                  placeholder="Tên hàng, mã hàng, mã chứng từ..."
                  style={{
                    width: '100%',
                    padding: '0.625rem 0.75rem 0.625rem 2.5rem',
                    border: '1px solid #e5e7eb',
                    borderRadius: '8px',
                    fontSize: '0.875rem'
                  }}
                />
              </div>
            </div>

            {/* Hàng hóa */}
            <div>
              <label style={{ display: 'block', marginBottom: '0.5rem', fontSize: '0.875rem', fontWeight: '500', color: '#374151' }}>
                Hàng hóa
              </label>
              <select
                value={filters.hangHoaId}
                onChange={(e) => handleFilterChange('hangHoaId', e.target.value)}
                style={{
                  width: '100%',
                  padding: '0.625rem 0.75rem',
                  border: '1px solid #e5e7eb',
                  borderRadius: '8px',
                  fontSize: '0.875rem'
                }}
              >
                <option value="">Tất cả hàng hóa</option>
                {hangHoaList.map(hh => (
                  <option key={hh.id} value={hh.id}>
                    {hh.maHangHoa} - {hh.tenHangHoa}
                  </option>
                ))}
              </select>
            </div>

            {/* Vị trí kho */}
            <div>
              <label style={{ display: 'block', marginBottom: '0.5rem', fontSize: '0.875rem', fontWeight: '500', color: '#374151' }}>
                Vị trí kho
              </label>
              <select
                value={filters.viTriKhoId}
                onChange={(e) => handleFilterChange('viTriKhoId', e.target.value)}
                style={{
                  width: '100%',
                  padding: '0.625rem 0.75rem',
                  border: '1px solid #e5e7eb',
                  borderRadius: '8px',
                  fontSize: '0.875rem'
                }}
              >
                <option value="">Tất cả vị trí</option>
                {viTriKhoList.map(vt => (
                  <option key={vt.id} value={vt.id}>
                    {vt.tenViTri} - {vt.tenKho}
                  </option>
                ))}
              </select>
            </div>

            {/* Loại biến động */}
            <div>
              <label style={{ display: 'block', marginBottom: '0.5rem', fontSize: '0.875rem', fontWeight: '500', color: '#374151' }}>
                Loại biến động
              </label>
              <select
                value={filters.loaiBienDong}
                onChange={(e) => handleFilterChange('loaiBienDong', e.target.value)}
                style={{
                  width: '100%',
                  padding: '0.625rem 0.75rem',
                  border: '1px solid #e5e7eb',
                  borderRadius: '8px',
                  fontSize: '0.875rem'
                }}
              >
                {loaiBienDongOptions.map(opt => (
                  <option key={opt.value} value={opt.value}>
                    {opt.label}
                  </option>
                ))}
              </select>
            </div>

            {/* Từ ngày */}
            <div>
              <label style={{ display: 'block', marginBottom: '0.5rem', fontSize: '0.875rem', fontWeight: '500', color: '#374151' }}>
                Từ ngày
              </label>
              <input
                type="datetime-local"
                value={filters.tuNgay}
                onChange={(e) => handleFilterChange('tuNgay', e.target.value)}
                style={{
                  width: '100%',
                  padding: '0.625rem 0.75rem',
                  border: '1px solid #e5e7eb',
                  borderRadius: '8px',
                  fontSize: '0.875rem'
                }}
              />
            </div>

            {/* Đến ngày */}
            <div>
              <label style={{ display: 'block', marginBottom: '0.5rem', fontSize: '0.875rem', fontWeight: '500', color: '#374151' }}>
                Đến ngày
              </label>
              <input
                type="datetime-local"
                value={filters.denNgay}
                onChange={(e) => handleFilterChange('denNgay', e.target.value)}
                style={{
                  width: '100%',
                  padding: '0.625rem 0.75rem',
                  border: '1px solid #e5e7eb',
                  borderRadius: '8px',
                  fontSize: '0.875rem'
                }}
              />
            </div>
          </div>

          {/* Reset Button */}
          <div style={{ marginTop: '1rem', textAlign: 'right' }}>
            <button
              onClick={handleResetFilters}
              style={{
                padding: '0.5rem 1rem',
                backgroundColor: '#f3f4f6',
                color: '#374151',
                border: 'none',
                borderRadius: '6px',
                fontSize: '0.875rem',
                fontWeight: '500',
                cursor: 'pointer'
              }}
            >
              Xóa bộ lọc
            </button>
          </div>
        </div>

        {/* Table */}
        <div style={{
          backgroundColor: 'white',
          borderRadius: '12px',
          boxShadow: '0 2px 8px rgba(0,0,0,0.08)',
          overflow: 'hidden'
        }}>
          {/* Table Header Info */}
          <div style={{
            padding: '1rem 1.5rem',
            borderBottom: '1px solid #e5e7eb',
            display: 'flex',
            justifyContent: 'space-between',
            alignItems: 'center'
          }}>
            <span style={{ fontSize: '0.875rem', color: '#64748b' }}>
              Tổng: <strong>{pagination.totalElements}</strong> bản ghi
            </span>
          </div>

          {/* Loading State */}
          {loading ? (
            <div style={{
              padding: '3rem',
              textAlign: 'center',
              color: '#9ca3af'
            }}>
              <RefreshCw size={32} style={{ animation: 'spin 1s linear infinite' }} />
              <p style={{ marginTop: '1rem' }}>Đang tải dữ liệu...</p>
            </div>
          ) : lichSuList.length === 0 ? (
            <div style={{
              padding: '3rem',
              textAlign: 'center',
              color: '#9ca3af'
            }}>
              <History size={48} />
              <p style={{ marginTop: '1rem', fontSize: '1rem' }}>
                Không có dữ liệu lịch sử
              </p>
            </div>
          ) : (
            <>
              {/* Table Content */}
              <div style={{ overflowX: 'auto' }}>
                <table style={{ width: '100%', borderCollapse: 'collapse' }}>
                  <thead style={{ backgroundColor: '#f8fafc' }}>
                    <tr>
                      <th style={tableHeaderStyle}>#</th>
                      <th style={tableHeaderStyle}>Thời gian</th>
                      <th style={tableHeaderStyle}>Hàng hóa</th>
                      <th style={tableHeaderStyle}>Loại biến động</th>
                      <th style={tableHeaderStyle}>Số lượng</th>
                      <th style={tableHeaderStyle}>Giá trị</th>
                      <th style={tableHeaderStyle}>Vị trí kho</th>
                      <th style={tableHeaderStyle}>Chứng từ</th>
                      <th style={tableHeaderStyle}>Người thực hiện</th>
                      <th style={tableHeaderStyle}>Thao tác</th>
                    </tr>
                  </thead>
                  <tbody>
                    {lichSuList.map((item, index) => (
                      <tr
                        key={item.id}
                        style={{
                          borderBottom: '1px solid #f1f5f9',
                          transition: 'background-color 0.2s'
                        }}
                        onMouseEnter={(e) => e.currentTarget.style.backgroundColor = '#f8fafc'}
                        onMouseLeave={(e) => e.currentTarget.style.backgroundColor = 'white'}
                      >
                        <td style={tableCellStyle}>
                          {pagination.page * pagination.size + index + 1}
                        </td>
                        <td style={tableCellStyle}>
                          <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                            <Clock size={16} color="#64748b" />
                            <div>
                              <div style={{ fontWeight: '500', fontSize: '0.875rem' }}>
                                {item.createdAtFormatted}
                              </div>
                            </div>
                          </div>
                        </td>
                        <td style={tableCellStyle}>
                          <div>
                            <div style={{ fontWeight: '600', color: '#1e293b', fontSize: '0.875rem' }}>
                              {item.tenHangHoa}
                            </div>
                            <div style={{ fontSize: '0.75rem', color: '#64748b', marginTop: '0.25rem' }}>
                              {item.maHangHoa} • {item.donViTinh}
                            </div>
                            {item.soLo && (
                              <div style={{ fontSize: '0.75rem', color: '#64748b', marginTop: '0.25rem' }}>
                                Lô: {item.soLo}
                              </div>
                            )}
                          </div>
                        </td>
                        <td style={tableCellStyle}>
                          <div style={{
                            display: 'inline-flex',
                            alignItems: 'center',
                            gap: '0.5rem',
                            padding: '0.375rem 0.75rem',
                            borderRadius: '6px',
                            backgroundColor: `${getBienDongColor(item.loaiBienDong)}15`,
                            border: `1px solid ${getBienDongColor(item.loaiBienDong)}40`
                          }}>
                            {getBienDongIcon(item.loaiBienDong)}
                            <span style={{
                              fontSize: '0.75rem',
                              fontWeight: '600',
                              color: getBienDongColor(item.loaiBienDong)
                            }}>
                              {item.loaiBienDongText}
                            </span>
                          </div>
                        </td>
                        <td style={tableCellStyle}>
                          <div style={{ fontSize: '0.875rem' }}>
                            <div style={{ color: '#64748b' }}>
                              Trước: <strong>{item.soLuongTruoc}</strong>
                            </div>
                            <div style={{
                              color: item.soLuongBienDong > 0 ? '#10b981' : '#ef4444',
                              fontWeight: '600',
                              marginTop: '0.25rem'
                            }}>
                              {item.soLuongBienDong > 0 ? '+' : ''}{item.soLuongBienDong}
                            </div>
                            <div style={{ color: '#1e293b', fontWeight: '600', marginTop: '0.25rem' }}>
                              Sau: <strong>{item.soLuongSau}</strong>
                            </div>
                          </div>
                        </td>
                        <td style={tableCellStyle}>
                          <div style={{ fontSize: '0.875rem' }}>
                            {item.donGia && (
                              <div style={{ color: '#64748b' }}>
                                Đơn giá: {formatCurrency(item.donGia)}
                              </div>
                            )}
                            {item.giaTriBienDong && (
                              <div style={{ color: '#1e293b', fontWeight: '600', marginTop: '0.25rem' }}>
                                Giá trị: {formatCurrency(item.giaTriBienDong)}
                              </div>
                            )}
                          </div>
                        </td>
                        <td style={tableCellStyle}>
                          {item.tenViTriKho ? (
                            <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                              <MapPin size={16} color="#64748b" />
                              <div>
                                <div style={{ fontSize: '0.875rem', fontWeight: '500' }}>
                                  {item.tenViTriKho}
                                </div>
                                <div style={{ fontSize: '0.75rem', color: '#64748b' }}>
                                  {item.tenKho}
                                </div>
                              </div>
                            </div>
                          ) : (
                            <span style={{ color: '#9ca3af', fontSize: '0.875rem' }}>-</span>
                          )}
                        </td>
                        <td style={tableCellStyle}>
                          {item.maChungTu ? (
                            <div>
                              <div style={{
                                display: 'inline-flex',
                                alignItems: 'center',
                                gap: '0.5rem',
                                padding: '0.25rem 0.5rem',
                                backgroundColor: '#f3f4f6',
                                borderRadius: '4px',
                                fontSize: '0.75rem',
                                fontWeight: '600',
                                color: '#374151'
                              }}>
                                <FileText size={14} />
                                {item.maChungTu}
                              </div>
                              {item.loaiChungTuText && (
                                <div style={{ fontSize: '0.75rem', color: '#64748b', marginTop: '0.25rem' }}>
                                  {item.loaiChungTuText}
                                </div>
                              )}
                            </div>
                          ) : (
                            <span style={{ color: '#9ca3af', fontSize: '0.875rem' }}>-</span>
                          )}
                        </td>
                        <td style={tableCellStyle}>
                          {item.tenNguoiThucHien ? (
                            <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                              <User size={16} color="#64748b" />
                              <span style={{ fontSize: '0.875rem' }}>
                                {item.tenNguoiThucHien}
                              </span>
                            </div>
                          ) : (
                            <span style={{ color: '#9ca3af', fontSize: '0.875rem' }}>Hệ thống</span>
                          )}
                        </td>
                        <td style={tableCellStyle}>
                          <button
                            onClick={() => handleViewChart(item)}
                            style={{
                              padding: '0.5rem',
                              backgroundColor: '#eff6ff',
                              color: '#3b82f6',
                              border: 'none',
                              borderRadius: '6px',
                              cursor: 'pointer',
                              display: 'flex',
                              alignItems: 'center',
                              gap: '0.5rem',
                              fontSize: '0.75rem',
                              fontWeight: '600'
                            }}
                            title="Xem biểu đồ"
                          >
                            <Eye size={14} />
                            Biểu đồ
                          </button>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>

              {/* Pagination */}
              {pagination.totalPages > 1 && (
                <div style={{
                  padding: '1rem 1.5rem',
                  borderTop: '1px solid #e5e7eb',
                  display: 'flex',
                  justifyContent: 'space-between',
                  alignItems: 'center'
                }}>
                  <span style={{ fontSize: '0.875rem', color: '#64748b' }}>
                    Trang {pagination.page + 1} / {pagination.totalPages}
                  </span>
                  <div style={{ display: 'flex', gap: '0.5rem' }}>
                    <button
                      onClick={() => setPagination(prev => ({ ...prev, page: Math.max(0, prev.page - 1) }))}
                      disabled={pagination.page === 0}
                      style={{
                        padding: '0.5rem 1rem',
                        backgroundColor: pagination.page === 0 ? '#f3f4f6' : '#3b82f6',
                        color: pagination.page === 0 ? '#9ca3af' : 'white',
                        border: 'none',
                        borderRadius: '6px',
                        fontSize: '0.875rem',
                        fontWeight: '600',
                        cursor: pagination.page === 0 ? 'not-allowed' : 'pointer'
                      }}
                    >
                      Trước
                    </button>
                    <button
                      onClick={() => setPagination(prev => ({ ...prev, page: Math.min(prev.totalPages - 1, prev.page + 1) }))}
                      disabled={pagination.page >= pagination.totalPages - 1}
                      style={{
                        padding: '0.5rem 1rem',
                        backgroundColor: pagination.page >= pagination.totalPages - 1 ? '#f3ff6' : '#3b82f6',
                        color: pagination.page >= pagination.totalPages - 1 ? '#9ca3af' : 'white',
                        border: 'none',
                        borderRadius: '6px',
                        fontSize: '0.875rem',
                        fontWeight: '600',
                        cursor: pagination.page >= pagination.totalPages - 1 ? 'not-allowed' : 'pointer'
                      }}
                    >
                      Sau
                    </button>
                  </div>
                </div>
              )}
            </>
          )}
        </div>

        {/* Chart Modal */}
        {showChartModal && selectedHangHoaForChart && (
          <div
            style={{
              position: 'fixed',
              top: 0,
              left: 0,
              right: 0,
              bottom: 0,
              backgroundColor: 'rgba(0, 0, 0, 0.5)',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              zIndex: 1000,
              padding: '1rem'
            }}
            onClick={() => setShowChartModal(false)}
          >
            <div
              style={{
                backgroundColor: 'white',
                borderRadius: '16px',
                padding: '2rem',
                maxWidth: '900px',
                width: '100%',
                maxHeight: '90vh',
                overflow: 'auto',
                boxShadow: '0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 10px 10px -5px rgba(0, 0, 0, 0.04)'
              }}
              onClick={(e) => e.stopPropagation()}
            >
              {/* Modal Header */}
              <div style={{
                display: 'flex',
                justifyContent: 'space-between',
                alignItems: 'flex-start',
                marginBottom: '1.5rem'
              }}>
                <div>
                  <h2 style={{
                    margin: 0,
                    fontSize: '1.5rem',
                    fontWeight: '700',
                    color: '#1e293b'
                  }}>
                    📊 Biểu Đồ Tồn Kho
                  </h2>
                  <p style={{
                    margin: '0.5rem 0 0 0',
                    fontSize: '0.875rem',
                    color: '#64748b'
                  }}>
                    {selectedHangHoaForChart.tenHangHoa} ({selectedHangHoaForChart.maHangHoa})
                  </p>
                </div>
                <button
                  onClick={() => setShowChartModal(false)}
                  style={{
                    padding: '0.5rem',
                    backgroundColor: '#f3f4f6',
                    border: 'none',
                    borderRadius: '8px',
                    cursor: 'pointer',
                    fontSize: '1.25rem',
                    color: '#6b7280',
                    lineHeight: 1
                  }}
                >
                  ✕
                </button>
              </div>

              {/* Chart Options */}
              <div style={{
                display: 'flex',
                gap: '0.5rem',
                marginBottom: '1.5rem'
              }}>
                {[7, 15, 30, 60, 90].map(days => (
                  <button
                    key={days}
                    onClick={() => fetchChartData(selectedHangHoaForChart.hangHoaId, days)}
                    style={{
                      padding: '0.5rem 1rem',
                      backgroundColor: '#f3f4f6',
                      border: 'none',
                      borderRadius: '6px',
                      fontSize: '0.875rem',
                      fontWeight: '600',
                      color: '#374151',
                      cursor: 'pointer',
                      transition: 'all 0.2s'
                    }}
                    onMouseEnter={(e) => {
                      e.currentTarget.style.backgroundColor = '#3b82f6';
                      e.currentTarget.style.color = 'white';
                    }}
                    onMouseLeave={(e) => {
                      e.currentTarget.style.backgroundColor = '#f3f4f6';
                      e.currentTarget.style.color = '#374151';
                    }}
                  >
                    {days} ngày
                  </button>
                ))}
              </div>

              {/* Chart */}
              {chartData.length === 0 ? (
                <div style={{
                  padding: '3rem',
                  textAlign: 'center',
                  color: '#9ca3af'
                }}>
                  <History size={48} />
                  <p style={{ marginTop: '1rem' }}>
                    Không có dữ liệu biểu đồ
                  </p>
                </div>
              ) : (
                <ResponsiveContainer width="100%" height={400}>
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
                      label={{ value: 'Số lượng', angle: -90, position: 'insideLeft' }}
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
                      dataKey="tonKho"
                      stroke="#3b82f6"
                      strokeWidth={3}
                      name="Tồn kho"
                      dot={{ fill: '#3b82f6', r: 5 }}
                      activeDot={{ r: 8 }}
                    />
                  </LineChart>
                </ResponsiveContainer>
              )}

              {/* Chart Stats */}
              {chartData.length > 0 && (
                <div style={{
                  marginTop: '1.5rem',
                  display: 'grid',
                  gridTemplateColumns: 'repeat(auto-fit, minmax(150px, 1fr))',
                  gap: '1rem'
                }}>
                  <div style={{
                    padding: '1rem',
                    backgroundColor: '#f0f9ff',
                    borderRadius: '8px',
                    border: '1px solid #bfdbfe'
                  }}>
                    <div style={{ fontSize: '0.75rem', color: '#3b82f6', fontWeight: '600' }}>
                      Tồn kho hiện tại
                    </div>
                    <div style={{ fontSize: '1.5rem', fontWeight: '700', color: '#1e40af', marginTop: '0.5rem' }}>
                      {chartData[chartData.length - 1]?.tonKho || 0}
                    </div>
                  </div>
                  <div style={{
                    padding: '1rem',
                    backgroundColor: '#f0fdf4',
                    borderRadius: '8px',
                    border: '1px solid #bbf7d0'
                  }}>
                    <div style={{ fontSize: '0.75rem', color: '#10b981', fontWeight: '600' }}>
                      Cao nhất
                    </div>
                    <div style={{ fontSize: '1.5rem', fontWeight: '700', color: '#047857', marginTop: '0.5rem' }}>
                      {Math.max(...chartData.map(d => d.tonKho))}
                    </div>
                  </div>
                  <div style={{
                    padding: '1rem',
                    backgroundColor: '#fef2f2',
                    borderRadius: '8px',
                    border: '1px solid #fecaca'
                  }}>
                    <div style={{ fontSize: '0.75rem', color: '#ef4444', fontWeight: '600' }}>
                      Thấp nhất
                    </div>
                    <div style={{ fontSize: '1.5rem', fontWeight: '700', color: '#b91c1c', marginTop: '0.5rem' }}>
                      {Math.min(...chartData.map(d => d.tonKho))}
                    </div>
                  </div>
                  <div style={{
                    padding: '1rem',
                    backgroundColor: '#fefce8',
                    borderRadius: '8px',
                    border: '1px solid #fef08a'
                  }}>
                    <div style={{ fontSize: '0.75rem', color: '#f59e0b', fontWeight: '600' }}>
                      Trung bình
                    </div>
                    <div style={{ fontSize: '1.5rem', fontWeight: '700', color: '#d97706', marginTop: '0.5rem' }}>
                      {Math.round(chartData.reduce((sum, d) => sum + d.tonKho, 0) / chartData.length)}
                    </div>
                  </div>
                </div>
              )}
            </div>
          </div>
        )}
      </div>

      {/* CSS Animation for Spin */}
      <style>{`
        @keyframes spin {
          from { transform: rotate(0deg); }
          to { transform: rotate(360deg); }
        }
      `}</style>
    </Layout>
  );
};

// Table Styles
const tableHeaderStyle = {
  padding: '1rem',
  textAlign: 'left',
  fontSize: '0.75rem',
  fontWeight: '700',
  color: '#64748b',
  textTransform: 'uppercase',
  letterSpacing: '0.05em',
  borderBottom: '2px solid #e2e8f0'
};

const tableCellStyle = {
  padding: '1rem',
  fontSize: '0.875rem',
  color: '#1e293b',
  verticalAlign: 'top'
};

export default LichSuTonKho;
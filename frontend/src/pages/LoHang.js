import React, { useState, useEffect } from 'react';
import Layout from '../components/layout/Layout';
import { Modal, Table, Loading } from '../components/shared';
import LoHangForm from '../components/forms/LoHangForm';
import { toast } from 'react-toastify';
import api, {getImageUrl} from '../services/api';

const LoHang = () => {
  const [loHangList, setLoHangList] = useState([]);
  const [loading, setLoading] = useState(false);
  const [showModal, setShowModal] = useState(false);
  const [editingLoHang, setEditingLoHang] = useState(null);
  const [viewingLoHang, setViewingLoHang] = useState(null);
  const [showDetailModal, setShowDetailModal] = useState(false);
  const [searchTerm, setSearchTerm] = useState('');
  const [selectedTrangThai, setSelectedTrangThai] = useState('');
  const [sapHetHan, setSapHetHan] = useState(false);
  const [currentPage, setCurrentPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);

  useEffect(() => {
    fetchLoHangList();
  }, [currentPage, searchTerm, selectedTrangThai, sapHetHan]);

  const fetchLoHangList = async () => {
    setLoading(true);
    try {
      const response = await api.get('/api/lo-hang', {
        params: {
          search: searchTerm,
          trangThai: selectedTrangThai || undefined,
          sapHetHan: sapHetHan || undefined,
          page: currentPage,
          size: 20,
          sortBy: 'hanSuDung',
          sortDir: 'ASC'
        }
      });
      
      const data = response.data.data || response.data;
      setLoHangList(data.content || []);
      setTotalPages(data.totalPages || 0);
      
    } catch (error) {
      console.error('Error fetching lo hang:', error);
      toast.error('Lỗi khi tải danh sách lô hàng');
    } finally {
      setLoading(false);
    }
  };

  const handleCreateLoHang = async (formData) => {
    try {
      const response = await api.post('/api/lo-hang', formData);
      toast.success(`Tạo lô hàng thành công: ${response.data.soLo || ''}`);
      setShowModal(false);
      fetchLoHangList();
    } catch (error) {
      const message = error.response?.data?.message || 'Lỗi khi tạo lô hàng';
      toast.error(message);
      throw error;
    }
  };

  const handleUpdateLoHang = async (formData) => {
    try {
      await api.put(`/api/lo-hang/${editingLoHang.id}`, formData);
      toast.success('Cập nhật lô hàng thành công');
      setShowModal(false);
      setEditingLoHang(null);
      fetchLoHangList();
    } catch (error) {
      const message = error.response?.data?.message || 'Lỗi khi cập nhật lô hàng';
      toast.error(message);
      throw error;
    }
  };

  const handleDeleteLoHang = async (id, loHang) => {
    if (loHang.soLuongHienTai < loHang.soLuongNhap) {
      toast.error('Không thể xóa lô hàng đã xuất');
      return;
    }

    if (window.confirm(`Bạn có chắc muốn xóa lô hàng "${loHang.soLo}"?\n\nHành động này KHÔNG thể hoàn tác!`)) {
      try {
        await api.delete(`/api/lo-hang/${id}`);
        toast.success('Xóa lô hàng thành công');
        fetchLoHangList();
      } catch (error) {
        const message = error.response?.data?.message || 'Lỗi khi xóa lô hàng';
        toast.error(message);
      }
    }
  };

  const handleViewDetail = async (id) => {
    try {
      const response = await api.get(`/api/lo-hang/${id}`);
      const data = response.data.data || response.data;
      setViewingLoHang(data);
      setShowDetailModal(true);
    } catch (error) {
      console.error('Error fetching lo hang detail:', error);
      toast.error('Lỗi khi tải thông tin lô hàng');
    }
  };

  const formatCurrency = (value) => {
    return new Intl.NumberFormat('vi-VN', {
      style: 'currency',
      currency: 'VND'
    }).format(value || 0);
  };

  const formatDate = (dateStr) => {
    if (!dateStr) return '-';
    return new Date(dateStr).toLocaleDateString('vi-VN', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric'
    });
  };

  const columns = [
  // ✅ THÊM CỘT HÌNH ẢNH MỚI
  {
    title: 'Hình ảnh',
    dataIndex: 'hinhAnhUrl',
    width: '80px',
    render: (url) => (
      url ? (
        <img
          src={getImageUrl(url)}
          alt="Product"
          style={{
            width: '60px',
            height: '60px',
            objectFit: 'cover',
            borderRadius: '8px',
            border: '2px solid #ddd'
          }}
          onError={(e) => {
            e.target.onerror = null;
            e.target.src = 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iNjAiIGhlaWdodD0iNjAiIHhtbG5zPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwL3N2ZyI+CiAgPHJlY3Qgd2lkdGg9IjYwIiBoZWlnaHQ9IjYwIiBmaWxsPSIjZjBmMGYwIi8+CiAgPHRleHQgeD0iNTAlIiB5PSI1MCUiIGZvbnQtZmFtaWx5PSJBcmlhbCIgZm9udC1zaXplPSIxMCIgZmlsbD0iIzk5OSIgdGV4dC1hbmNob3I9Im1pZGRsZSIgZHk9Ii4zZW0iPk4vQTwvdGV4dD4KPC9zdmc+';
          }}
        />
      ) : (
        <div style={{
          width: '60px',
          height: '60px',
          backgroundColor: '#f0f0f0',
          borderRadius: '8px',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          color: '#999',
          fontSize: '1.5rem'
        }}>
          📦
        </div>
      )
    )
  },
  { 
    title: 'Số Lô', 
    dataIndex: 'soLo',
    render: (value, record) => (
      <div>
        <div style={{ fontWeight: '600', color: '#2c3e50' }}>{value}</div>
        <div style={{ fontSize: '0.75rem', color: '#7f8c8d' }}>{record.maHangHoa}</div>
      </div>
    )
  },
  { 
    title: 'Hàng Hóa', 
    dataIndex: 'tenHangHoa',
    render: (value, record) => (
      <div>
        <div style={{ fontWeight: '500' }}>{value}</div>
        <div style={{ fontSize: '0.75rem', color: '#7f8c8d' }}>
          ĐVT: {record.tenDonViTinh}
        </div>
      </div>
    )
  },
  {
    title: 'Số Lượng',
    dataIndex: 'soLuongHienTai',
    align: 'center',
    render: (value, record) => (
      <div>
        <div>
          <span style={{
            fontWeight: 'bold',
            fontSize: '1.1rem',
            color: value === 0 ? '#e74c3c' : value < record.soLuongNhap * 0.2 ? '#f39c12' : '#27ae60'
          }}>
            {value}
          </span>
          <span style={{ color: '#7f8c8d', fontSize: '0.85rem' }}>
            /{record.soLuongNhap}
          </span>
        </div>
        <div style={{ fontSize: '0.75rem', color: '#7f8c8d', marginTop: '0.25rem' }}>
          {((value / record.soLuongNhap) * 100).toFixed(0)}% còn lại
        </div>
      </div>
    )
  },
  {
    title: 'Giá Nhập',
    dataIndex: 'giaNhap',
    align: 'right',
    render: (value) => (
      <div style={{ fontWeight: '500', color: '#2c3e50' }}>
        {formatCurrency(value)}
      </div>
    )
  },
  {
    title: 'Hạn Sử Dụng',
    dataIndex: 'hanSuDung',
    render: (value, record) => (
      <div>
        <div style={{ fontWeight: '500' }}>{formatDate(value)}</div>
        {record.soNgayConLai !== null && (
          <div style={{
            fontSize: '0.75rem',
            fontWeight: '600',
            marginTop: '0.25rem',
            padding: '0.25rem 0.5rem',
            borderRadius: '4px',
            display: 'inline-block',
            backgroundColor: record.soNgayConLai < 0 ? '#ffebee' :
                             record.soNgayConLai < 30 ? '#fff3e0' :
                             record.soNgayConLai < 90 ? '#fff9e6' : '#e8f5e9',
            color: record.soNgayConLai < 0 ? '#c62828' :
                   record.soNgayConLai < 30 ? '#e65100' :
                   record.soNgayConLai < 90 ? '#f57c00' : '#2e7d32'
          }}>
            {record.soNgayConLai < 0 ? `Hết hạn ${Math.abs(record.soNgayConLai)} ngày` :
             record.soNgayConLai === 0 ? 'Hết hạn hôm nay' :
             `Còn ${record.soNgayConLai} ngày`}
          </div>
        )}
      </div>
    )
  },
  {
    title: 'Trạng Thái',
    dataIndex: 'trangThai',
    align: 'center',
    render: (value) => {
      const statusConfig = {
        MOI: { label: 'Mới', bg: '#e3f2fd', color: '#1976d2' },
        DANG_SU_DUNG: { label: 'Đang SD', bg: '#e8f5e9', color: '#2e7d32' },
        GAN_HET_HAN: { label: 'Sắp hết hạn', bg: '#fff3e0', color: '#e65100' },
        HET_HAN: { label: 'Hết hạn', bg: '#ffebee', color: '#c62828' },
        HET_HANG: { label: 'Hết hàng', bg: '#f5f5f5', color: '#616161' }
      };
      const config = statusConfig[value] || statusConfig.MOI;
      return (
        <span style={{
          padding: '0.375rem 0.75rem',
          borderRadius: '9999px',
          fontSize: '0.75rem',
          fontWeight: '600',
          backgroundColor: config.bg,
          color: config.color,
          whiteSpace: 'nowrap'
        }}>
          {config.label}
        </span>
      );
    }
  },
  {
    title: 'Thao Tác',
    dataIndex: 'actions',
    align: 'center',
    render: (_, record) => (
      <div style={{ display: 'flex', gap: '0.5rem', justifyContent: 'center', flexWrap: 'wrap' }}>
        <button
          onClick={() => handleViewDetail(record.id)}
          style={{
            padding: '0.5rem 0.75rem',
            backgroundColor: '#3498db',
            color: 'white',
            border: 'none',
            borderRadius: '6px',
            cursor: 'pointer',
            fontSize: '0.875rem',
            fontWeight: '500'
          }}
          title="Xem chi tiết"
        >
          Xem
        </button>
        <button
          onClick={() => {
            setEditingLoHang(record);
            setShowModal(true);
          }}
          style={{
            padding: '0.5rem 0.75rem',
            backgroundColor: '#f39c12',
            color: 'white',
            border: 'none',
            borderRadius: '6px',
            cursor: 'pointer',
            fontSize: '0.875rem',
            fontWeight: '500'
          }}
          title="Chỉnh sửa"
        >
          Sửa
        </button>
        <button
          onClick={() => handleDeleteLoHang(record.id, record)}
          style={{
            padding: '0.5rem 0.75rem',
            backgroundColor: record.soLuongHienTai < record.soLuongNhap ? '#95a5a6' : '#e74c3c',
            color: 'white',
            border: 'none',
            borderRadius: '6px',
            cursor: record.soLuongHienTai < record.soLuongNhap ? 'not-allowed' : 'pointer',
            fontSize: '0.875rem',
            fontWeight: '500',
            opacity: record.soLuongHienTai < record.soLuongNhap ? 0.6 : 1
          }}
          title={record.soLuongHienTai < record.soLuongNhap ? 'Không thể xóa lô đã xuất' : 'Xóa lô hàng'}
          disabled={record.soLuongHienTai < record.soLuongNhap}
        >
          Xóa
        </button>
      </div>
    )
  }
];

  const stats = {
    total: loHangList.length,
    sapHetHan: loHangList.filter(l => l.sapHetHan && l.soLuongHienTai > 0).length,
    hetHan: loHangList.filter(l => l.trangThai === 'HET_HAN').length,
    hetHang: loHangList.filter(l => l.trangThai === 'HET_HANG').length,
    tongGiaTri: loHangList.reduce((sum, l) => sum + (l.soLuongHienTai * l.giaNhap), 0)
  };

  return (
    <Layout>
      <div style={{ padding: '1.5rem' }}>
        <div style={{ marginBottom: '2rem' }}>
          <h1 style={{ fontSize: '1.875rem', fontWeight: 'bold', color: '#2c3e50', marginBottom: '0.5rem' }}>
            Quản lý Lô Hàng
          </h1>
          <p style={{ color: '#7f8c8d' }}>Theo dõi và quản lý lô hàng trong kho</p>
        </div>

        {/* Statistics Cards */}
        <div style={{
          display: 'grid',
          gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))',
          gap: '1.5rem',
          marginBottom: '1.5rem'
        }}>
          <div style={{
            backgroundColor: 'white',
            padding: '1.5rem',
            borderRadius: '0.75rem',
            boxShadow: '0 2px 8px rgba(0,0,0,0.1)',
            borderLeft: '4px solid #3498db'
          }}>
            <p style={{ color: '#7f8c8d', fontSize: '0.875rem', marginBottom: '0.5rem' }}>Tổng lô hàng</p>
            <p style={{ fontSize: '2rem', fontWeight: 'bold', color: '#2c3e50', margin: 0 }}>
              {stats.total}
            </p>
          </div>

          <div style={{
            backgroundColor: 'white',
            padding: '1.5rem',
            borderRadius: '0.75rem',
            boxShadow: '0 2px 8px rgba(0,0,0,0.1)',
            borderLeft: '4px solid #f39c12'
          }}>
            <p style={{ color: '#7f8c8d', fontSize: '0.875rem', marginBottom: '0.5rem' }}>Sắp hết hạn</p>
            <p style={{ fontSize: '2rem', fontWeight: 'bold', color: '#2c3e50', margin: 0 }}>
              {stats.sapHetHan}
            </p>
          </div>

          <div style={{
            backgroundColor: 'white',
            padding: '1.5rem',
            borderRadius: '0.75rem',
            boxShadow: '0 2px 8px rgba(0,0,0,0.1)',
            borderLeft: '4px solid #e74c3c'
          }}>
            <p style={{ color: '#7f8c8d', fontSize: '0.875rem', marginBottom: '0.5rem' }}>Đã hết hạn</p>
            <p style={{ fontSize: '2rem', fontWeight: 'bold', color: '#2c3e50', margin: 0 }}>
              {stats.hetHan}
            </p>
          </div>

          <div style={{
            backgroundColor: 'white',
            padding: '1.5rem',
            borderRadius: '0.75rem',
            boxShadow: '0 2px 8px rgba(0,0,0,0.1)',
            borderLeft: '4px solid #9b59b6'
          }}>
            <p style={{ color: '#7f8c8d', fontSize: '0.875rem', marginBottom: '0.5rem' }}>Tổng giá trị</p>
            <p style={{ fontSize: '1.25rem', fontWeight: 'bold', color: '#2c3e50', margin: 0 }}>
              {formatCurrency(stats.tongGiaTri)}
            </p>
          </div>
        </div>

        {/* Filter & Search Bar */}
        <div style={{
          backgroundColor: 'white',
          padding: '1rem',
          borderRadius: '0.75rem',
          marginBottom: '1.5rem',
          boxShadow: '0 2px 8px rgba(0,0,0,0.1)',
          display: 'flex',
          gap: '1rem',
          alignItems: 'center',
          flexWrap: 'wrap'
        }}>
          <input
            type="text"
            placeholder="Tìm kiếm theo số lô, tên hàng hóa..."
            value={searchTerm}
            onChange={(e) => {
              setSearchTerm(e.target.value);
              setCurrentPage(0);
            }}
            style={{
              flex: 1,
              minWidth: '200px',
              padding: '0.75rem 1rem',
              border: '1px solid #ddd',
              borderRadius: '0.5rem',
              fontSize: '1rem'
            }}
          />
          
          <select
            value={selectedTrangThai}
            onChange={(e) => {
              setSelectedTrangThai(e.target.value);
              setCurrentPage(0);
            }}
            style={{
              padding: '0.75rem 1rem',
              border: '1px solid #ddd',
              borderRadius: '0.5rem',
              fontSize: '1rem',
              minWidth: '150px'
            }}
          >
            <option value="">Tất cả trạng thái</option>
            <option value="MOI">Mới</option>
            <option value="DANG_SU_DUNG">Đang sử dụng</option>
            <option value="GAN_HET_HAN">Gần hết hạn</option>
            <option value="HET_HAN">Hết hạn</option>
            <option value="HET_HANG">Hết hàng</option>
          </select>
          
          <label style={{
            display: 'flex',
            alignItems: 'center',
            gap: '0.5rem',
            padding: '0.75rem 1rem',
            backgroundColor: sapHetHan ? '#fff3e0' : '#f8f9fa',
            borderRadius: '0.5rem',
            cursor: 'pointer',
            border: sapHetHan ? '2px solid #f39c12' : '2px solid transparent',
            fontWeight: sapHetHan ? '600' : 'normal'
          }}>
            <input
              type="checkbox"
              checked={sapHetHan}
              onChange={(e) => {
                setSapHetHan(e.target.checked);
                setCurrentPage(0);
              }}
              style={{ cursor: 'pointer' }}
            />
            <span>Chỉ lô sắp hết hạn</span>
          </label>
          
          <button
            onClick={() => {
              setEditingLoHang(null);
              setShowModal(true);
            }}
            style={{
              backgroundColor: '#27ae60',
              color: 'white',
              border: 'none',
              padding: '0.75rem 1.5rem',
              borderRadius: '0.5rem',
              cursor: 'pointer',
              fontWeight: '500',
              fontSize: '1rem'
            }}
          >
            + Thêm Lô Hàng
          </button>
        </div>

        {/* Table */}
        <div style={{
          backgroundColor: 'white',
          borderRadius: '0.75rem',
          boxShadow: '0 2px 8px rgba(0,0,0,0.1)',
          overflow: 'hidden'
        }}>
          {loading ? (
            <Loading message="Đang tải danh sách lô hàng..." />
          ) : (
            <>
              <Table
                columns={columns}
                data={loHangList}
                loading={loading}
                emptyMessage="Không tìm thấy lô hàng nào"
              />

              {totalPages > 1 && (
                <div style={{
                  padding: '1rem',
                  textAlign: 'center',
                  borderTop: '1px solid #dee2e6',
                  display: 'flex',
                  justifyContent: 'center',
                  gap: '0.5rem',
                  flexWrap: 'wrap'
                }}>
                  {Array.from({ length: totalPages }, (_, i) => (
                    <button
                      key={i}
                      onClick={() => setCurrentPage(i)}
                      style={{
                        padding: '0.5rem 1rem',
                        border: '1px solid #ddd',
                        backgroundColor: i === currentPage ? '#3498db' : 'white',
                        color: i === currentPage ? 'white' : '#333',
                        borderRadius: '0.375rem',
                        cursor: 'pointer',
                        fontWeight: i === currentPage ? '500' : 'normal'
                      }}
                    >
                      {i + 1}
                    </button>
                  ))}
                </div>
              )}
            </>
          )}
        </div>

        {/* Create/Edit Modal */}
        <Modal
          isOpen={showModal}
          onClose={() => {
            setShowModal(false);
            setEditingLoHang(null);
          }}
          title={editingLoHang ? 'Cập nhật Lô Hàng' : 'Thêm Lô Hàng Mới'}
          size="large"
        >
          <LoHangForm
            initialData={editingLoHang}
            onSubmit={editingLoHang ? handleUpdateLoHang : handleCreateLoHang}
            onCancel={() => {
              setShowModal(false);
              setEditingLoHang(null);
            }}
          />
        </Modal>

        {/* Detail Modal */}
        <Modal
          isOpen={showDetailModal}
          onClose={() => {
            setShowDetailModal(false);
            setViewingLoHang(null);
          }}
          title="Chi tiết Lô Hàng"
          size="large"
        >
          {viewingLoHang && (
  <div>
    {/* ✅ HEADER: Ảnh hàng hóa */}
    {viewingLoHang.hinhAnhUrl && (
      <div style={{
        marginBottom: '1.5rem',
        textAlign: 'center',
        padding: '1.5rem',
        background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
        borderRadius: '12px',
        boxShadow: '0 4px 15px rgba(102, 126, 234, 0.3)'
      }}>
        <img
          src={getImageUrl(viewingLoHang.hinhAnhUrl)}
          alt={viewingLoHang.tenHangHoa}
          style={{
            maxWidth: '250px',
            maxHeight: '250px',
            objectFit: 'cover',
            borderRadius: '12px',
            border: '4px solid rgba(255,255,255,0.9)',
            boxShadow: '0 8px 24px rgba(0,0,0,0.2)',
            backgroundColor: 'white'
          }}
          onError={(e) => {
            e.target.onerror = null;
            e.target.src = 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMjUwIiBoZWlnaHQ9IjI1MCIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj4KICA8cmVjdCB3aWR0aD0iMjUwIiBoZWlnaHQ9IjI1MCIgZmlsbD0iI2YwZjBmMCIvPgogIDx0ZXh0IHg9IjUwJSIgeT0iNTAlIiBmb250LWZhbWlseT0iQXJpYWwiIGZvbnQtc2l6ZT0iMjAiIGZpbGw9IiM5OTkiIHRleHQtYW5jaG9yPSJtaWRkbGUiIGR5PSIuM2VtIj7huqJuaCBs4buXaTwvdGV4dD4KPC9zdmc+';
          }}
        />
        <div style={{
          marginTop: '1rem',
          color: 'white',
          fontSize: '1.1rem',
          fontWeight: '600',
          textShadow: '0 2px 4px rgba(0,0,0,0.2)'
        }}>
          {viewingLoHang.tenHangHoa}
        </div>
        <div style={{
          marginTop: '0.25rem',
          color: 'rgba(255,255,255,0.9)',
          fontSize: '0.9rem'
        }}>
          {viewingLoHang.maHangHoa}
        </div>
      </div>
    )}

    {/* 2 CỘT THÔNG TIN */}
    <div style={{ 
      display: 'grid', 
      gridTemplateColumns: '1fr 1fr', 
      gap: '1.5rem', 
      marginBottom: '1.5rem' 
    }}>
      {/* CỘT 1: Thông tin lô hàng */}
      <div style={{ 
        backgroundColor: '#f8f9fa', 
        padding: '1.25rem', 
        borderRadius: '0.5rem',
        border: '1px solid #e8e8e8'
      }}>
        <h4 style={{ 
          color: '#2c3e50', 
          marginBottom: '1rem', 
          fontSize: '1rem', 
          fontWeight: '600',
          paddingBottom: '0.5rem',
          borderBottom: '2px solid #3498db'
        }}>
          📋 Thông tin lô hàng
        </h4>
        <div style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.875rem' }}>
            <span style={{ color: '#7f8c8d' }}>Số lô:</span>
            <span style={{ fontWeight: '600', color: '#2c3e50' }}>{viewingLoHang.soLo}</span>
          </div>
          <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.875rem' }}>
            <span style={{ color: '#7f8c8d' }}>Hàng hóa:</span>
            <span style={{ fontWeight: '600', color: '#2c3e50' }}>{viewingLoHang.tenHangHoa}</span>
          </div>
          <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.875rem' }}>
            <span style={{ color: '#7f8c8d' }}>Mã hàng:</span>
            <span style={{ fontWeight: '600', color: '#2c3e50' }}>{viewingLoHang.maHangHoa}</span>
          </div>
          <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.875rem' }}>
            <span style={{ color: '#7f8c8d' }}>Ngày sản xuất:</span>
            <span style={{ fontWeight: '600', color: '#2c3e50' }}>
              {viewingLoHang.ngaySanXuat ? formatDate(viewingLoHang.ngaySanXuat) : 'Không có'}
            </span>
          </div>
          <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.875rem' }}>
            <span style={{ color: '#7f8c8d' }}>Hạn sử dụng:</span>
            <span style={{ fontWeight: '600', color: '#2c3e50' }}>
              {formatDate(viewingLoHang.hanSuDung)}
            </span>
          </div>
          
          {/* Cảnh báo hết hạn */}
          {viewingLoHang.soNgayConLai !== null && (
            <div style={{
              marginTop: '0.5rem',
              padding: '0.75rem',
              borderRadius: '0.5rem',
              backgroundColor: viewingLoHang.soNgayConLai < 0 ? '#ffebee' :
                               viewingLoHang.soNgayConLai < 30 ? '#fff3e0' : '#e8f5e9',
              color: viewingLoHang.soNgayConLai < 0 ? '#c62828' :
                     viewingLoHang.soNgayConLai < 30 ? '#e65100' : '#2e7d32',
              fontWeight: '600',
              textAlign: 'center',
              fontSize: '0.875rem',
              border: `2px solid ${
                viewingLoHang.soNgayConLai < 0 ? '#ef5350' :
                viewingLoHang.soNgayConLai < 30 ? '#ff9800' : '#66bb6a'
              }`
            }}>
              {viewingLoHang.soNgayConLai < 0 ? (
                <span>⚠️ Đã hết hạn {Math.abs(viewingLoHang.soNgayConLai)} ngày</span>
              ) : viewingLoHang.soNgayConLai === 0 ? (
                <span>⚠️ Hết hạn hôm nay!</span>
              ) : viewingLoHang.soNgayConLai < 30 ? (
                <span>⏰ Còn {viewingLoHang.soNgayConLai} ngày đến hạn</span>
              ) : (
                <span>✓ Còn {viewingLoHang.soNgayConLai} ngày đến hạn</span>
              )}
            </div>
          )}
        </div>
      </div>

      {/* CỘT 2: Thông tin số lượng & giá */}
      <div style={{ 
        backgroundColor: '#f8f9fa', 
        padding: '1.25rem', 
        borderRadius: '0.5rem',
        border: '1px solid #e8e8e8'
      }}>
        <h4 style={{ 
          color: '#2c3e50', 
          marginBottom: '1rem', 
          fontSize: '1rem', 
          fontWeight: '600',
          paddingBottom: '0.5rem',
          borderBottom: '2px solid #27ae60'
        }}>
          💰 Số lượng & Giá trị
        </h4>
        <div style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.875rem' }}>
            <span style={{ color: '#7f8c8d' }}>Số lượng nhập:</span>
            <span style={{ fontWeight: '600', color: '#2c3e50' }}>{viewingLoHang.soLuongNhap}</span>
          </div>
          
          {/* Số lượng hiện tại - Nổi bật */}
          <div style={{ 
            display: 'flex', 
            justifyContent: 'space-between', 
            alignItems: 'center',
            padding: '0.75rem',
            backgroundColor: viewingLoHang.soLuongHienTai === 0 ? '#ffebee' : '#e8f5e9',
            borderRadius: '6px',
            border: `2px solid ${viewingLoHang.soLuongHienTai === 0 ? '#ef5350' : '#66bb6a'}`
          }}>
            <span style={{ fontSize: '0.875rem', color: '#7f8c8d', fontWeight: '600' }}>
              Số lượng hiện tại:
            </span>
            <span style={{
              fontWeight: '700',
              fontSize: '1.5rem',
              color: viewingLoHang.soLuongHienTai === 0 ? '#e74c3c' : '#27ae60'
            }}>
              {viewingLoHang.soLuongHienTai}
            </span>
          </div>

          <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.875rem' }}>
            <span style={{ color: '#7f8c8d' }}>Đã xuất:</span>
            <span style={{ fontWeight: '600', color: '#e67e22' }}>
              {viewingLoHang.soLuongNhap - viewingLoHang.soLuongHienTai}
            </span>
          </div>

          {/* Phần trăm còn lại */}
          <div style={{ 
            marginTop: '0.25rem',
            marginBottom: '0.5rem'
          }}>
            <div style={{ 
              fontSize: '0.75rem', 
              color: '#7f8c8d', 
              marginBottom: '0.25rem',
              display: 'flex',
              justifyContent: 'space-between'
            }}>
              <span>Tỷ lệ còn lại</span>
              <span style={{ fontWeight: '600' }}>
                {((viewingLoHang.soLuongHienTai / viewingLoHang.soLuongNhap) * 100).toFixed(1)}%
              </span>
            </div>
            <div style={{
              height: '8px',
              backgroundColor: '#e0e0e0',
              borderRadius: '4px',
              overflow: 'hidden'
            }}>
              <div style={{
                height: '100%',
                width: `${(viewingLoHang.soLuongHienTai / viewingLoHang.soLuongNhap) * 100}%`,
                backgroundColor: viewingLoHang.soLuongHienTai === 0 
                  ? '#e74c3c' 
                  : viewingLoHang.soLuongHienTai < viewingLoHang.soLuongNhap * 0.2 
                  ? '#f39c12' 
                  : '#27ae60',
                transition: 'width 0.3s ease'
              }} />
            </div>
          </div>

          <div style={{ 
            height: '1px', 
            backgroundColor: '#dee2e6', 
            margin: '0.5rem 0' 
          }} />

          <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.875rem' }}>
            <span style={{ color: '#7f8c8d' }}>Giá nhập:</span>
            <span style={{ fontWeight: '600', color: '#2c3e50' }}>
              {formatCurrency(viewingLoHang.giaNhap)}
            </span>
          </div>
          
          {/* Giá trị còn lại - Nổi bật */}
          <div style={{ 
            display: 'flex', 
            justifyContent: 'space-between', 
            alignItems: 'center',
            padding: '0.75rem',
            backgroundColor: '#e3f2fd',
            borderRadius: '6px',
            border: '2px solid #42a5f5'
          }}>
            <span style={{ fontSize: '0.875rem', color: '#1976d2', fontWeight: '600' }}>
              Giá trị còn lại:
            </span>
            <span style={{ fontWeight: '700', fontSize: '1.1rem', color: '#1565c0' }}>
              {formatCurrency(viewingLoHang.soLuongHienTai * viewingLoHang.giaNhap)}
            </span>
          </div>

          <div style={{ 
            height: '1px', 
            backgroundColor: '#dee2e6', 
            margin: '0.5rem 0' 
          }} />

          <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.875rem' }}>
            <span style={{ color: '#7f8c8d' }}>Nhà cung cấp:</span>
            <span style={{ fontWeight: '600', color: '#2c3e50' }}>
              {viewingLoHang.tenNhaCungCap || 'Không có'}
            </span>
          </div>
          
          {viewingLoHang.soChungTuNhap && (
            <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.875rem' }}>
              <span style={{ color: '#7f8c8d' }}>Số chứng từ:</span>
              <span style={{ fontWeight: '600', color: '#2c3e50' }}>
                {viewingLoHang.soChungTuNhap}
              </span>
            </div>
          )}
        </div>
      </div>
    </div>

    {/* GHI CHÚ */}
    {viewingLoHang.ghiChu && (
      <div style={{
        backgroundColor: '#fff9e6',
        border: '2px solid #ffe082',
        borderRadius: '8px',
        padding: '1.25rem',
        borderLeft: '4px solid #ffc107'
      }}>
        <h5 style={{ 
          fontWeight: '600', 
          color: '#f57c00', 
          marginBottom: '0.75rem', 
          fontSize: '0.95rem',
          display: 'flex',
          alignItems: 'center',
          gap: '0.5rem'
        }}>
          <span>📝</span>
          <span>Ghi chú</span>
        </h5>
        <p style={{ 
          color: '#5d4037', 
          margin: 0, 
          fontSize: '0.9rem', 
          lineHeight: '1.6',
          fontStyle: 'italic'
        }}>
          {viewingLoHang.ghiChu}
        </p>
      </div>
    )}
  </div>
)}
        </Modal>
      </div>
    </Layout>
  );
};

export default LoHang;
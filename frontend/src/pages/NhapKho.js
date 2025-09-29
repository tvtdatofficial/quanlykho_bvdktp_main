import React, { useState, useEffect } from 'react';
import Layout from '../components/layout/Layout';
import { Modal, Table, Loading } from '../components/shared';
import NhapKhoForm from '../components/forms/NhapKhoForm';
import { toast } from 'react-toastify';
import api from '../services/api';

const NhapKho = () => {
  const [phieuNhapList, setPhieuNhapList] = useState([]);
  const [loading, setLoading] = useState(false);
  const [showCreateForm, setShowCreateForm] = useState(false);
  const [showDetailModal, setShowDetailModal] = useState(false);
  const [viewingPhieu, setViewingPhieu] = useState(null);
  const [currentPage, setCurrentPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [searchTerm, setSearchTerm] = useState('');
  const [selectedStatus, setSelectedStatus] = useState('');

  useEffect(() => {
    fetchPhieuNhapList();
  }, [currentPage, searchTerm, selectedStatus]);

  const fetchPhieuNhapList = async () => {
    setLoading(true);
    try {
      const response = await api.get('/api/phieu-nhap', {
        params: {
          search: searchTerm,
          trangThai: selectedStatus || undefined,
          page: currentPage,
          size: 10
        }
      });
      setPhieuNhapList(response.data.data.content || []);
      setTotalPages(response.data.data.totalPages || 0);
    } catch (error) {
      console.error('Error fetching phieu nhap:', error);
      toast.error('Lỗi khi tải danh sách phiếu nhập');
    } finally {
      setLoading(false);
    }
  };

  const handleCreatePhieuNhap = async (formData) => {
    try {
      await api.post('/api/phieu-nhap', formData);
      toast.success('Tạo phiếu nhập thành công');
      setShowCreateForm(false);
      fetchPhieuNhapList();
    } catch (error) {
      const message = error.response?.data?.message || error.response?.data?.error || 'Lỗi khi tạo phiếu nhập';
      toast.error(message);
      throw error;
    }
  };

  const handleDuyetPhieu = async (id) => {
    if (window.confirm('Bạn có chắc muốn duyệt phiếu nhập này?')) {
      try {
        await api.patch(`/api/phieu-nhap/${id}/duyet`);
        toast.success('Duyệt phiếu nhập thành công');
        fetchPhieuNhapList();
      } catch (error) {
        const message = error.response?.data?.message || 'Lỗi khi duyệt phiếu nhập';
        toast.error(message);
      }
    }
  };

  const handleViewDetail = async (id) => {
    try {
      const response = await api.get(`/api/phieu-nhap/${id}`);
      setViewingPhieu(response.data.data);
      setShowDetailModal(true);
    } catch (error) {
      toast.error('Lỗi khi tải thông tin phiếu nhập');
    }
  };

  const columns = [
    { title: 'Mã Phiếu', dataIndex: 'maPhieuNhap' },
    { title: 'Kho', dataIndex: 'tenKho' },
    { title: 'Nhà Cung Cấp', dataIndex: 'tenNhaCungCap' },
    { title: 'Ngày Nhập', dataIndex: 'ngayNhap' },
    { 
      title: 'Tổng Giá Trị', 
      dataIndex: 'tongGiaTri',
      align: 'right',
      render: (value) => new Intl.NumberFormat('vi-VN', { 
        style: 'currency', 
        currency: 'VND' 
      }).format(value || 0)
    },
    {
      title: 'Trạng Thái',
      dataIndex: 'trangThai',
      render: (value) => {
        const statusConfig = {
          CHO_DUYET: { label: 'Chờ duyệt', bg: '#fef5e7', color: '#f39c12' },
          DA_DUYET: { label: 'Đã duyệt', bg: '#d5f4e6', color: '#27ae60' },
          HUY: { label: 'Đã hủy', bg: '#fadbd8', color: '#e74c3c' }
        };
        const config = statusConfig[value] || statusConfig.CHO_DUYET;
        return (
          <span style={{
            padding: '0.375rem 0.75rem',
            borderRadius: '9999px',
            fontSize: '0.875rem',
            fontWeight: '500',
            backgroundColor: config.bg,
            color: config.color,
            display: 'inline-block'
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
        <div style={{ display: 'flex', gap: '0.5rem', justifyContent: 'center', alignItems: 'center' }}>
          <button
            onClick={() => handleViewDetail(record.id)}
            style={{
              padding: '0.5rem',
              backgroundColor: '#3498db',
              color: 'white',
              border: 'none',
              borderRadius: '0.5rem',
              cursor: 'pointer',
              display: 'inline-flex',
              alignItems: 'center',
              justifyContent: 'center'
            }}
            title="Xem chi tiết"
          >
            👁️
          </button>
          {record.trangThai === 'CHO_DUYET' && (
            <button
              onClick={() => handleDuyetPhieu(record.id)}
              style={{
                padding: '0.5rem',
                backgroundColor: '#27ae60',
                color: 'white',
                border: 'none',
                borderRadius: '0.5rem',
                cursor: 'pointer',
                display: 'inline-flex',
                alignItems: 'center',
                justifyContent: 'center'
              }}
              title="Duyệt phiếu"
            >
              ✓
            </button>
          )}
        </div>
      )
    }
  ];

  const totalValue = phieuNhapList.reduce((sum, p) => sum + (p.tongGiaTri || 0), 0);
  const pendingCount = phieuNhapList.filter(p => p.trangThai === 'CHO_DUYET').length;

  return (
    <Layout>
      <div style={{ padding: '1.5rem' }}>
        <div style={{ marginBottom: '2rem' }}>
          <h1 style={{ fontSize: '1.875rem', fontWeight: 'bold', color: '#2c3e50', marginBottom: '0.5rem' }}>
            Quản lý Nhập Kho
          </h1>
          <p style={{ color: '#7f8c8d' }}>Theo dõi và quản lý các phiếu nhập kho</p>
        </div>

        {/* Statistics */}
        <div style={{
          display: 'grid',
          gridTemplateColumns: 'repeat(auto-fit, minmax(250px, 1fr))',
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
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <div>
                <p style={{ color: '#7f8c8d', fontSize: '0.875rem', marginBottom: '0.5rem' }}>Tổng phiếu nhập</p>
                <p style={{ fontSize: '2rem', fontWeight: 'bold', color: '#2c3e50', margin: 0 }}>
                  {phieuNhapList.length}
                </p>
              </div>
              <div style={{ fontSize: '3rem', opacity: '0.2' }}>📦</div>
            </div>
          </div>

          <div style={{
            backgroundColor: 'white',
            padding: '1.5rem',
            borderRadius: '0.75rem',
            boxShadow: '0 2px 8px rgba(0,0,0,0.1)',
            borderLeft: '4px solid #f39c12'
          }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <div>
                <p style={{ color: '#7f8c8d', fontSize: '0.875rem', marginBottom: '0.5rem' }}>Chờ duyệt</p>
                <p style={{ fontSize: '2rem', fontWeight: 'bold', color: '#2c3e50', margin: 0 }}>
                  {pendingCount}
                </p>
              </div>
              <div style={{ fontSize: '3rem', opacity: '0.2' }}>⏰</div>
            </div>
          </div>

          <div style={{
            backgroundColor: 'white',
            padding: '1.5rem',
            borderRadius: '0.75rem',
            boxShadow: '0 2px 8px rgba(0,0,0,0.1)',
            borderLeft: '4px solid #27ae60'
          }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <div>
                <p style={{ color: '#7f8c8d', fontSize: '0.875rem', marginBottom: '0.5rem' }}>Tổng giá trị</p>
                <p style={{ fontSize: '1.5rem', fontWeight: 'bold', color: '#2c3e50', margin: 0 }}>
                  {new Intl.NumberFormat('vi-VN', { 
                    style: 'currency', 
                    currency: 'VND',
                    maximumFractionDigits: 0
                  }).format(totalValue)}
                </p>
              </div>
              <div style={{ fontSize: '3rem', opacity: '0.2' }}>💰</div>
            </div>
          </div>
        </div>

        {/* Action Bar */}
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
            placeholder="🔍 Tìm kiếm theo mã phiếu..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
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
            value={selectedStatus}
            onChange={(e) => setSelectedStatus(e.target.value)}
            style={{
              padding: '0.75rem 1rem',
              border: '1px solid #ddd',
              borderRadius: '0.5rem',
              fontSize: '1rem',
              minWidth: '180px'
            }}
          >
            <option value="">Tất cả trạng thái</option>
            <option value="CHO_DUYET">Chờ duyệt</option>
            <option value="DA_DUYET">Đã duyệt</option>
            <option value="HUY">Đã hủy</option>
          </select>
          
          <button
            onClick={() => setShowCreateForm(true)}
            style={{
              backgroundColor: '#27ae60',
              color: 'white',
              border: 'none',
              padding: '0.75rem 1.5rem',
              borderRadius: '0.5rem',
              cursor: 'pointer',
              fontWeight: '500',
              fontSize: '1rem',
              display: 'flex',
              alignItems: 'center',
              gap: '0.5rem'
            }}
          >
            ➕ Tạo Phiếu Nhập
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
            <Loading message="Đang tải danh sách phiếu nhập..." />
          ) : (
            <>
              <Table
                columns={columns}
                data={phieuNhapList}
                loading={loading}
              />

              {/* Pagination */}
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

        {/* Create Form Modal */}
        <Modal
          isOpen={showCreateForm}
          onClose={() => setShowCreateForm(false)}
          title="Tạo Phiếu Nhập Mới"
          size="xlarge"
        >
          <NhapKhoForm
            onSubmit={handleCreatePhieuNhap}
            onCancel={() => setShowCreateForm(false)}
          />
        </Modal>

        {/* Detail Modal */}
        <Modal
          isOpen={showDetailModal}
          onClose={() => {
            setShowDetailModal(false);
            setViewingPhieu(null);
          }}
          title="Chi tiết Phiếu Nhập"
          size="xlarge"
        >
          {viewingPhieu && (
            <div>
              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1.5rem', marginBottom: '1.5rem' }}>
                <div style={{ backgroundColor: '#f8f9fa', padding: '1.25rem', borderRadius: '0.5rem' }}>
                  <h4 style={{ color: '#2c3e50', marginBottom: '1rem', fontSize: '1rem', fontWeight: '600', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                    📦 Thông tin phiếu nhập
                  </h4>
                  <div style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
                    <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.875rem' }}>
                      <span style={{ color: '#7f8c8d' }}>Mã phiếu:</span>
                      <span style={{ fontWeight: '600', color: '#2c3e50' }}>{viewingPhieu.maPhieuNhap}</span>
                    </div>
                    <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.875rem' }}>
                      <span style={{ color: '#7f8c8d' }}>Kho:</span>
                      <span style={{ fontWeight: '600', color: '#2c3e50' }}>{viewingPhieu.tenKho}</span>
                    </div>
                    <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.875rem' }}>
                      <span style={{ color: '#7f8c8d' }}>Nhà cung cấp:</span>
                      <span style={{ fontWeight: '600', color: '#2c3e50' }}>{viewingPhieu.tenNhaCungCap || 'Không có'}</span>
                    </div>
                    <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.875rem' }}>
                      <span style={{ color: '#7f8c8d' }}>Loại nhập:</span>
                      <span style={{ fontWeight: '600', color: '#2c3e50' }}>{viewingPhieu.loaiNhap}</span>
                    </div>
                    <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.875rem' }}>
                      <span style={{ color: '#7f8c8d' }}>Ngày nhập:</span>
                      <span style={{ fontWeight: '600', color: '#2c3e50' }}>{viewingPhieu.ngayNhap}</span>
                    </div>
                    <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.875rem' }}>
                      <span style={{ color: '#7f8c8d' }}>Người tạo:</span>
                      <span style={{ fontWeight: '600', color: '#2c3e50' }}>{viewingPhieu.nguoiTao}</span>
                    </div>
                  </div>
                </div>

                <div style={{ backgroundColor: '#f8f9fa', padding: '1.25rem', borderRadius: '0.5rem' }}>
                  <h4 style={{ color: '#2c3e50', marginBottom: '1rem', fontSize: '1rem', fontWeight: '600', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                    🚚 Thông tin giao hàng
                  </h4>
                  <div style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
                    <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.875rem' }}>
                      <span style={{ color: '#7f8c8d' }}>Người giao:</span>
                      <span style={{ fontWeight: '600', color: '#2c3e50' }}>{viewingPhieu.nguoiGiao || 'Không có'}</span>
                    </div>
                    <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.875rem' }}>
                      <span style={{ color: '#7f8c8d' }}>SĐT:</span>
                      <span style={{ fontWeight: '600', color: '#2c3e50' }}>{viewingPhieu.sdtNguoiGiao || 'Không có'}</span>
                    </div>
                    <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.875rem' }}>
                      <span style={{ color: '#7f8c8d' }}>Số hóa đơn:</span>
                      <span style={{ fontWeight: '600', color: '#2c3e50' }}>{viewingPhieu.soHoaDon || 'Không có'}</span>
                    </div>
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', fontSize: '0.875rem' }}>
                      <span style={{ color: '#7f8c8d' }}>Trạng thái:</span>
                      {(() => {
                        const statusConfig = {
                          CHO_DUYET: { label: 'Chờ duyệt', bg: '#fef5e7', color: '#f39c12' },
                          DA_DUYET: { label: 'Đã duyệt', bg: '#d5f4e6', color: '#27ae60' },
                          HUY: { label: 'Đã hủy', bg: '#fadbd8', color: '#e74c3c' }
                        };
                        const config = statusConfig[viewingPhieu.trangThai] || statusConfig.CHO_DUYET;
                        return (
                          <span style={{
                            padding: '0.25rem 0.75rem',
                            borderRadius: '9999px',
                            fontSize: '0.75rem',
                            fontWeight: '600',
                            backgroundColor: config.bg,
                            color: config.color
                          }}>
                            {config.label}
                          </span>
                        );
                      })()}
                    </div>
                  </div>
                </div>
              </div>

              {/* Chi tiết hàng hóa */}
              <div style={{ marginBottom: '1.5rem' }}>
                <h4 style={{ color: '#2c3e50', marginBottom: '1rem', fontSize: '1rem', fontWeight: '600' }}>
                  Chi tiết hàng hóa
                </h4>
                <div style={{ overflowX: 'auto', border: '1px solid #dee2e6', borderRadius: '0.5rem' }}>
                  <table style={{ width: '100%', borderCollapse: 'collapse' }}>
                    <thead>
                      <tr style={{ backgroundColor: '#f8f9fa' }}>
                        <th style={{ padding: '0.75rem', border: '1px solid #dee2e6', textAlign: 'left', fontSize: '0.875rem', fontWeight: '600' }}>STT</th>
                        <th style={{ padding: '0.75rem', border: '1px solid #dee2e6', textAlign: 'left', fontSize: '0.875rem', fontWeight: '600' }}>Hàng hóa</th>
                        <th style={{ padding: '0.75rem', border: '1px solid #dee2e6', textAlign: 'center', fontSize: '0.875rem', fontWeight: '600' }}>Số lượng</th>
                        <th style={{ padding: '0.75rem', border: '1px solid #dee2e6', textAlign: 'right', fontSize: '0.875rem', fontWeight: '600' }}>Đơn giá</th>
                        <th style={{ padding: '0.75rem', border: '1px solid #dee2e6', textAlign: 'right', fontSize: '0.875rem', fontWeight: '600' }}>Thành tiền</th>
                        <th style={{ padding: '0.75rem', border: '1px solid #dee2e6', textAlign: 'center', fontSize: '0.875rem', fontWeight: '600' }}>Số lô</th>
                      </tr>
                    </thead>
                    <tbody>
                      {viewingPhieu.chiTiet && viewingPhieu.chiTiet.map((item, index) => (
                        <tr key={index} style={{ backgroundColor: 'white' }}>
                          <td style={{ padding: '0.75rem', border: '1px solid #dee2e6', fontSize: '0.875rem' }}>{index + 1}</td>
                          <td style={{ padding: '0.75rem', border: '1px solid #dee2e6', fontWeight: '500', fontSize: '0.875rem' }}>{item.tenHangHoa}</td>
                          <td style={{ padding: '0.75rem', border: '1px solid #dee2e6', textAlign: 'center', fontSize: '0.875rem' }}>{item.soLuong}</td>
                          <td style={{ padding: '0.75rem', border: '1px solid #dee2e6', textAlign: 'right', fontSize: '0.875rem' }}>
                            {new Intl.NumberFormat('vi-VN').format(item.donGia)}₫
                          </td>
                          <td style={{ padding: '0.75rem', border: '1px solid #dee2e6', textAlign: 'right', fontWeight: '600', fontSize: '0.875rem' }}>
                            {new Intl.NumberFormat('vi-VN').format(item.thanhTien)}₫
                          </td>
                          <td style={{ padding: '0.75rem', border: '1px solid #dee2e6', textAlign: 'center', color: '#7f8c8d', fontSize: '0.875rem' }}>
                            {item.soLo || '-'}
                          </td>
                        </tr>
                      ))}
                    </tbody>
                    <tfoot>
                      <tr style={{ backgroundColor: '#d5f4e6' }}>
                        <td colSpan="4" style={{ padding: '0.75rem', border: '1px solid #dee2e6', textAlign: 'right', fontWeight: '600', fontSize: '0.875rem' }}>
                          Tổng cộng:
                        </td>
                        <td style={{ padding: '0.75rem', border: '1px solid #dee2e6', textAlign: 'right', fontWeight: 'bold', color: '#27ae60', fontSize: '1rem' }}>
                          {new Intl.NumberFormat('vi-VN', {
                            style: 'currency',
                            currency: 'VND'
                          }).format(viewingPhieu.tongGiaTri)}
                        </td>
                        <td style={{ padding: '0.75rem', border: '1px solid #dee2e6' }}></td>
                      </tr>
                    </tfoot>
                  </table>
                </div>
              </div>

              {viewingPhieu.ghiChu && (
                <div style={{ 
                  backgroundColor: '#fff9e6', 
                  border: '1px solid #ffe082', 
                  borderRadius: '0.5rem', 
                  padding: '1rem' 
                }}>
                  <h5 style={{ fontWeight: '600', color: '#2c3e50', marginBottom: '0.5rem', fontSize: '0.875rem' }}>
                    Ghi chú
                  </h5>
                  <p style={{ color: '#5d4037', margin: 0, fontSize: '0.875rem' }}>
                    {viewingPhieu.ghiChu}
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

export default NhapKho;
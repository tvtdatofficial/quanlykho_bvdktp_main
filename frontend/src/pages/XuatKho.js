import React, { useState, useEffect } from 'react';
import Layout from '../components/layout/Layout';
import { Modal, Table, Loading } from '../components/shared';
import XuatKhoForm from '../components/forms/XuatKhoForm';
import { toast } from 'react-toastify';
import api, { getImageUrl } from '../services/api';


const XuatKho = () => {
  const [phieuXuatList, setPhieuXuatList] = useState([]);
  const [loading, setLoading] = useState(false);
  const [showCreateForm, setShowCreateForm] = useState(false);
  const [showEditForm, setShowEditForm] = useState(false);
  const [editingPhieu, setEditingPhieu] = useState(null);
  const [showDetailModal, setShowDetailModal] = useState(false);
  const [viewingPhieu, setViewingPhieu] = useState(null);
  const [currentPage, setCurrentPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [searchTerm, setSearchTerm] = useState('');
  const [selectedStatus, setSelectedStatus] = useState('');

  useEffect(() => {
    fetchPhieuXuatList();
  }, [currentPage, searchTerm, selectedStatus]);

  const fetchPhieuXuatList = async () => {
    setLoading(true);
    try {
      const response = await api.get('/api/phieu-xuat', {
        params: {
          search: searchTerm,
          trangThai: selectedStatus || undefined,
          page: currentPage,
          size: 10,
          sortBy: 'createdAt',        // ✅ ĐỔI: Sắp xếp theo thời gian tạo
          sortDir: 'desc'              // ✅ GIỮ NGUYÊN: desc = mới nhất trước
        }
      });
      setPhieuXuatList(response.data.data.content || []);
      setTotalPages(response.data.data.totalPages || 0);
    } catch (error) {
      console.error('Error fetching phieu xuat:', error);
      toast.error('Lỗi khi tải danh sách phiếu xuất');
    } finally {
      setLoading(false);
    }
  };

  const handleCreatePhieuXuat = async (formData) => {
    try {
      const response = await api.post('/api/phieu-xuat', formData);
      toast.success(`Tạo phiếu xuất thành công: ${response.data.data.maPhieuXuat}`);
      setShowCreateForm(false);
      fetchPhieuXuatList();
    } catch (error) {
      const message = error.response?.data?.message || 'Lỗi khi tạo phiếu xuất';
      toast.error(message);
      throw error;
    }
  };

  const handleEditPhieu = async (id) => {
    try {
      const response = await api.get(`/api/phieu-xuat/${id}`);
      const phieu = response.data.data;

      if (phieu.trangThai !== 'XUAT' && phieu.trangThai !== 'CHO_DUYET') {
        toast.error('Chỉ có thể sửa phiếu đang xuất hoặc chờ duyệt');
        return;
      }

      setEditingPhieu(phieu);
      setShowEditForm(true);
    } catch (error) {
      toast.error('Lỗi khi tải thông tin phiếu xuất');
    }
  };

  const handleUpdatePhieuXuat = async (formData) => {
    try {
      await api.put(`/api/phieu-xuat/${editingPhieu.id}`, formData);
      toast.success('Cập nhật phiếu xuất thành công');
      setShowEditForm(false);
      setEditingPhieu(null);
      fetchPhieuXuatList();
    } catch (error) {
      const message = error.response?.data?.message || 'Lỗi khi cập nhật phiếu xuất';
      toast.error(message);
      throw error;
    }
  };

  const handleDuyetPhieu = async (id) => {
    if (window.confirm('Xác nhận duyệt phiếu xuất?\n\nSau khi duyệt:\n✓ Tồn kho sẽ được trừ\n✓ Lô hàng sẽ được cập nhật\n✗ Không thể sửa hoặc xóa')) {
      try {
        await api.patch(`/api/phieu-xuat/${id}/duyet`);
        toast.success('✅ Duyệt thành công!');
        fetchPhieuXuatList();
      } catch (error) {
        toast.error(error.response?.data?.message || 'Lỗi khi duyệt phiếu xuất');
      }
    }
  };

  const handleHuyPhieu = async (id) => {
    const lyDoHuy = window.prompt('Nhập lý do hủy phiếu:');

    if (lyDoHuy === null) return;

    if (!lyDoHuy.trim()) {
      toast.error('Lý do hủy không được để trống');
      return;
    }

    try {
      await api.patch(`/api/phieu-xuat/${id}/huy?lyDoHuy=${encodeURIComponent(lyDoHuy)}`);
      toast.success('Hủy phiếu xuất thành công');
      fetchPhieuXuatList();
    } catch (error) {
      const message = error.response?.data?.message || 'Lỗi khi hủy phiếu xuất';
      toast.error(message);
    }
  };


  // ✅ THÊM: Handler hủy duyệt
  const handleHuyDuyetPhieu = async (id) => {
    const lyDoHuyDuyet = window.prompt(
      '⚠️ HỦY DUYỆT PHIẾU XUẤT\n\n' +
      'Hành động này sẽ:\n' +
      '• Hoàn nguyên tồn kho (cộng lại)\n' +
      '• Cộng lại số lượng vào lô hàng\n' +
      '• Chuyển phiếu về trạng thái "Chờ duyệt"\n\n' +
      'Nhập lý do hủy duyệt:'
    );

    if (lyDoHuyDuyet === null) return;

    if (!lyDoHuyDuyet.trim()) {
      toast.error('Lý do hủy duyệt không được để trống');
      return;
    }

    try {
      await api.patch(`/api/phieu-xuat/${id}/huy-duyet?lyDoHuyDuyet=${encodeURIComponent(lyDoHuyDuyet)}`);
      toast.success('✅ Hủy duyệt thành công! Tồn kho đã được hoàn nguyên.');
      fetchPhieuXuatList();
    } catch (error) {
      const message = error.response?.data?.message || 'Lỗi khi hủy duyệt phiếu xuất';
      toast.error(message);
    }
  };

  const handleDeletePhieu = async (id, trangThai) => {
    if (trangThai === 'DA_DUYET' || trangThai === 'DA_GIAO') {
      toast.error('Không thể xóa phiếu đã duyệt hoặc đã giao');
      return;
    }

    if (window.confirm('Bạn có chắc muốn XÓA phiếu xuất này?\n\nHành động này KHÔNG thể hoàn tác!')) {
      try {
        await api.delete(`/api/phieu-xuat/${id}`);
        toast.success('Xóa phiếu xuất thành công');
        fetchPhieuXuatList();
      } catch (error) {
        const message = error.response?.data?.message || 'Lỗi khi xóa phiếu xuất';
        toast.error(message);
      }
    }
  };

  const handleViewDetail = async (id) => {
    try {
      const response = await api.get(`/api/phieu-xuat/${id}`);
      setViewingPhieu(response.data.data);
      setShowDetailModal(true);
    } catch (error) {
      toast.error('Lỗi khi tải thông tin phiếu xuất');
    }
  };

  const formatDate = (dateStr) => {
    if (!dateStr) return '-';
    const date = new Date(dateStr.replace(' ', 'T'));
    return date.toLocaleDateString('vi-VN', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric'
    });
  };

  const formatDateTime = (dateStr) => {
    if (!dateStr) return 'Không có';
    const date = new Date(dateStr.replace(' ', 'T'));
    return date.toLocaleString('vi-VN', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  };

  const formatCurrency = (value) => {
    return new Intl.NumberFormat('vi-VN', {
      style: 'currency',
      currency: 'VND'
    }).format(value || 0);
  };

  const columns = [
    { title: 'Mã Phiếu', dataIndex: 'maPhieuXuat' },
    { title: 'Kho', dataIndex: 'tenKho' },
    {
      title: 'Loại Xuất',
      dataIndex: 'loaiXuat',
      render: (value) => {
        const loaiXuatMap = {
          'XUAT_SU_DUNG': 'Xuất sử dụng',
          'XUAT_BAN': 'Xuất bán',
          'XUAT_TRA': 'Xuất trả',
          'XUAT_HUY': 'Xuất hủy',
          'XUAT_CHUYEN_KHO': 'Chuyển kho',
          'XUAT_SUA_CHUA': 'Sửa chữa'
        };
        return loaiXuatMap[value] || value;
      }
    },
    { title: 'Ngày Xuất', dataIndex: 'createdAt', render: formatDateTime },
    {
      title: 'Tổng Giá Trị',
      dataIndex: 'tongGiaTri',
      align: 'right',
      render: formatCurrency
    },
    {
      title: 'Trạng Thái',
      dataIndex: 'trangThai',
      render: (value) => {
        const statusConfig = {
          XUAT: { label: 'Đang xuất', bg: '#e3f2fd', color: '#1976d2' },
          CHO_DUYET: { label: 'Chờ duyệt', bg: '#fef5e7', color: '#f39c12' },
          DA_DUYET: { label: 'Đã duyệt', bg: '#d5f4e6', color: '#27ae60' },
          DA_GIAO: { label: 'Đã giao', bg: '#e8f5e9', color: '#2e7d32' },
          HUY: { label: 'Đã hủy', bg: '#fadbd8', color: '#e74c3c' }
        };
        const config = statusConfig[value] || statusConfig.XUAT;
        return (
          <span style={{
            padding: '0.375rem 0.75rem',
            borderRadius: '9999px',
            fontSize: '0.875rem',
            fontWeight: '500',
            backgroundColor: config.bg,
            color: config.color
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
              borderRadius: '0.5rem',
              cursor: 'pointer',
              fontSize: '0.875rem'
            }}
            title="Xem chi tiết"
          >
            Xem
          </button>

          {(record.trangThai === 'XUAT' || record.trangThai === 'CHO_DUYET') && (
            <>
              <button
                onClick={() => handleEditPhieu(record.id)}
                style={{
                  padding: '0.5rem 0.75rem',
                  backgroundColor: '#f39c12',
                  color: 'white',
                  border: 'none',
                  borderRadius: '0.5rem',
                  cursor: 'pointer',
                  fontSize: '0.875rem'
                }}
                title="Chỉnh sửa"
              >
                Sửa
              </button>

              <button
                onClick={() => handleDuyetPhieu(record.id)}
                style={{
                  padding: '0.5rem 0.75rem',
                  backgroundColor: '#27ae60',
                  color: 'white',
                  border: 'none',
                  borderRadius: '0.5rem',
                  cursor: 'pointer',
                  fontWeight: '600',
                  fontSize: '0.875rem'
                }}
                title="Duyệt phiếu"
              >
                Duyệt
              </button>

              <button
                onClick={() => handleHuyPhieu(record.id)}
                style={{
                  padding: '0.5rem 0.75rem',
                  backgroundColor: '#e67e22',
                  color: 'white',
                  border: 'none',
                  borderRadius: '0.5rem',
                  cursor: 'pointer',
                  fontSize: '0.875rem'
                }}
                title="Hủy phiếu"
              >
                Hủy
              </button>

              <button
                onClick={() => handleDeletePhieu(record.id, record.trangThai)}
                style={{
                  padding: '0.5rem 0.75rem',
                  backgroundColor: '#e74c3c',
                  color: 'white',
                  border: 'none',
                  borderRadius: '0.5rem',
                  cursor: 'pointer',
                  fontSize: '0.875rem'
                }}
                title="Xóa phiếu"
              >
                Xóa
              </button>
            </>
          )}

          {record.trangThai === 'HUY' && (
            <button
              onClick={() => handleDeletePhieu(record.id, record.trangThai)}
              style={{
                padding: '0.5rem 0.75rem',
                backgroundColor: '#e74c3c',
                color: 'white',
                border: 'none',
                borderRadius: '0.5rem',
                cursor: 'pointer',
                fontSize: '0.875rem'
              }}
              title="Xóa phiếu"
            >
              Xóa
            </button>
          )}

          {/* ✅ THÊM: Nút HỦY DUYỆT cho ADMIN */}
          {(record.trangThai === 'DA_DUYET' || record.trangThai === 'DA_GIAO') && (
            <>
              <span style={{ color: '#7f8c8d', fontStyle: 'italic', fontSize: '0.875rem' }}>
                {record.trangThai === 'DA_DUYET' ? 'Đã duyệt' : 'Đã giao'}
              </span>

              {/* Chỉ hiển thị nếu user là ADMIN VÀ chưa giao */}
              {localStorage.getItem('userRole') === 'ADMIN' && record.trangThai === 'DA_DUYET' && (
                <button
                  onClick={() => handleHuyDuyetPhieu(record.id)}
                  style={{
                    padding: '0.5rem 0.75rem',
                    backgroundColor: '#e67e22',
                    color: 'white',
                    border: 'none',
                    borderRadius: '0.5rem',
                    cursor: 'pointer',
                    fontSize: '0.875rem',
                    fontWeight: '600'
                  }}
                  title="Hủy duyệt (chỉ ADMIN)"
                >
                  ⚠️ Hủy duyệt
                </button>
              )}
            </>
          )}
        </div>
      )
    }
  ];

  const totalValue = phieuXuatList.reduce((sum, p) => sum + (p.tongGiaTri || 0), 0);
  const pendingCount = phieuXuatList.filter(p => p.trangThai === 'CHO_DUYET' || p.trangThai === 'XUAT').length;
  const approvedCount = phieuXuatList.filter(p => p.trangThai === 'DA_DUYET').length;

  return (
    <Layout>
      <div style={{ padding: '1.5rem' }}>
        <div style={{ marginBottom: '2rem' }}>
          <h1 style={{ fontSize: '1.875rem', fontWeight: 'bold', color: '#2c3e50', marginBottom: '0.5rem' }}>
            Quản lý Xuất Kho
          </h1>
          <p style={{ color: '#7f8c8d' }}>Tạo, duyệt và quản lý phiếu xuất kho</p>
        </div>

        {/* Statistics */}
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
            <p style={{ color: '#7f8c8d', fontSize: '0.875rem', marginBottom: '0.5rem' }}>Tổng phiếu</p>
            <p style={{ fontSize: '2rem', fontWeight: 'bold', color: '#2c3e50', margin: 0 }}>
              {phieuXuatList.length}
            </p>
          </div>

          <div style={{
            backgroundColor: 'white',
            padding: '1.5rem',
            borderRadius: '0.75rem',
            boxShadow: '0 2px 8px rgba(0,0,0,0.1)',
            borderLeft: '4px solid #f39c12'
          }}>
            <p style={{ color: '#7f8c8d', fontSize: '0.875rem', marginBottom: '0.5rem' }}>Chờ xử lý</p>
            <p style={{ fontSize: '2rem', fontWeight: 'bold', color: '#2c3e50', margin: 0 }}>
              {pendingCount}
            </p>
          </div>

          <div style={{
            backgroundColor: 'white',
            padding: '1.5rem',
            borderRadius: '0.75rem',
            boxShadow: '0 2px 8px rgba(0,0,0,0.1)',
            borderLeft: '4px solid #27ae60'
          }}>
            <p style={{ color: '#7f8c8d', fontSize: '0.875rem', marginBottom: '0.5rem' }}>Đã duyệt</p>
            <p style={{ fontSize: '2rem', fontWeight: 'bold', color: '#2c3e50', margin: 0 }}>
              {approvedCount}
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
              {formatCurrency(totalValue)}
            </p>
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
            placeholder="Tìm kiếm theo mã phiếu..."
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
            value={selectedStatus}
            onChange={(e) => {
              setSelectedStatus(e.target.value);
              setCurrentPage(0);
            }}
            style={{
              padding: '0.75rem 1rem',
              border: '1px solid #ddd',
              borderRadius: '0.5rem',
              fontSize: '1rem',
              minWidth: '180px'
            }}
          >
            <option value="">Tất cả trạng thái</option>
            <option value="XUAT">Đang xuất</option>
            <option value="CHO_DUYET">Chờ duyệt</option>
            <option value="DA_DUYET">Đã duyệt</option>
            <option value="DA_GIAO">Đã giao</option>
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
              fontSize: '1rem'
            }}
          >
            + Tạo Phiếu Xuất
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
            <Loading message="Đang tải danh sách phiếu xuất..." />
          ) : (
            <>
              <Table
                columns={columns}
                data={phieuXuatList}
                loading={loading}
                emptyMessage="Không tìm thấy phiếu xuất nào"
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

        {/* Modals */}
        <Modal
          isOpen={showCreateForm}
          onClose={() => setShowCreateForm(false)}
          title="Tạo Phiếu Xuất Mới"
          size="xlarge"
        >
          <XuatKhoForm
            onSubmit={handleCreatePhieuXuat}
            onCancel={() => setShowCreateForm(false)}
          />
        </Modal>

        <Modal
          isOpen={showEditForm}
          onClose={() => {
            setShowEditForm(false);
            setEditingPhieu(null);
          }}
          title="Chỉnh Sửa Phiếu Xuất"
          size="xlarge"
        >
          <XuatKhoForm
            initialData={editingPhieu}
            onSubmit={handleUpdatePhieuXuat}
            onCancel={() => {
              setShowEditForm(false);
              setEditingPhieu(null);
            }}
          />
        </Modal>

        {/* Detail Modal */}
        <Modal
          isOpen={showDetailModal}
          onClose={() => {
            setShowDetailModal(false);
            setViewingPhieu(null);
          }}
          title="Chi tiết Phiếu Xuất"
          size="xlarge"
        >
          {viewingPhieu && (
            <div>
              {/* Header thông tin cơ bản */}
              <div style={{
                display: 'grid',
                gridTemplateColumns: '1fr 1fr',
                gap: '1.5rem',
                marginBottom: '1.5rem'
              }}>
                {/* Thông tin phiếu */}
                <div style={{
                  backgroundColor: '#f8f9fa',
                  padding: '1.25rem',
                  borderRadius: '0.5rem'
                }}>
                  <h4 style={{
                    color: '#2c3e50',
                    marginBottom: '1rem',
                    fontSize: '1rem',
                    fontWeight: '600'
                  }}>
                    📋 Thông tin phiếu xuất
                  </h4>
                  <div style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
                    <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.875rem' }}>
                      <span style={{ color: '#7f8c8d' }}>Mã phiếu:</span>
                      <span style={{ fontWeight: '600', color: '#2c3e50' }}>{viewingPhieu.maPhieuXuat}</span>
                    </div>
                    <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.875rem' }}>
                      <span style={{ color: '#7f8c8d' }}>Kho:</span>
                      <span style={{ fontWeight: '600', color: '#2c3e50' }}>{viewingPhieu.tenKho}</span>
                    </div>
                    <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.875rem' }}>
                      <span style={{ color: '#7f8c8d' }}>Loại xuất:</span>
                      <span style={{ fontWeight: '600', color: '#2c3e50' }}>
                        {viewingPhieu.loaiXuat === 'XUAT_SU_DUNG' ? 'Xuất sử dụng' :
                          viewingPhieu.loaiXuat === 'XUAT_BAN' ? 'Xuất bán' :
                            viewingPhieu.loaiXuat === 'XUAT_TRA' ? 'Xuất trả' :
                              viewingPhieu.loaiXuat === 'XUAT_HUY' ? 'Xuất hủy' :
                                viewingPhieu.loaiXuat === 'XUAT_CHUYEN_KHO' ? 'Chuyển kho' : viewingPhieu.loaiXuat}
                      </span>
                    </div>
                    <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.875rem' }}>
                      <span style={{ color: '#7f8c8d' }}>Thời gian xuất:</span>
                      <span style={{ fontWeight: '600', color: '#2c3e50' }}>
                        {formatDateTime(viewingPhieu.createdAt)}
                      </span>
                    </div>
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', fontSize: '0.875rem' }}>
                      <span style={{ color: '#7f8c8d' }}>Trạng thái:</span>
                      {(() => {
                        const statusConfig = {
                          XUAT: { label: 'Đang xuất', bg: '#e3f2fd', color: '#1976d2' },
                          CHO_DUYET: { label: 'Chờ duyệt', bg: '#fef5e7', color: '#f39c12' },
                          DA_DUYET: { label: 'Đã duyệt', bg: '#d5f4e6', color: '#27ae60' },
                          DA_GIAO: { label: 'Đã giao', bg: '#e8f5e9', color: '#2e7d32' },
                          HUY: { label: 'Đã hủy', bg: '#fadbd8', color: '#e74c3c' }
                        };
                        const config = statusConfig[viewingPhieu.trangThai] || statusConfig.XUAT;
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

                {/* Thông tin người liên quan */}
                <div style={{
                  backgroundColor: '#f8f9fa',
                  padding: '1.25rem',
                  borderRadius: '0.5rem'
                }}>
                  <h4 style={{
                    color: '#2c3e50',
                    marginBottom: '1rem',
                    fontSize: '1rem',
                    fontWeight: '600'
                  }}>
                    👥 Thông tin liên quan
                  </h4>
                  <div style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
                    {viewingPhieu.nguoiYeuCau && (
                      <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.875rem' }}>
                        <span style={{ color: '#7f8c8d' }}>Người yêu cầu:</span>
                        <span style={{ fontWeight: '600', color: '#2c3e50' }}>{viewingPhieu.nguoiYeuCau}</span>
                      </div>
                    )}
                    {viewingPhieu.nguoiNhan && (
                      <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.875rem' }}>
                        <span style={{ color: '#7f8c8d' }}>Người nhận:</span>
                        <span style={{ fontWeight: '600', color: '#2c3e50' }}>{viewingPhieu.nguoiNhan}</span>
                      </div>
                    )}
                    {viewingPhieu.tenNguoiXuat && (
                      <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.875rem' }}>
                        <span style={{ color: '#7f8c8d' }}>Người xuất:</span>
                        <span style={{ fontWeight: '600', color: '#2c3e50' }}>{viewingPhieu.tenNguoiXuat}</span>
                      </div>
                    )}
                    {viewingPhieu.tenNguoiDuyet && (
                      <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.875rem' }}>
                        <span style={{ color: '#7f8c8d' }}>Người duyệt:</span>
                        <span style={{ fontWeight: '600', color: '#2c3e50' }}>{viewingPhieu.tenNguoiDuyet}</span>
                      </div>
                    )}
                    {viewingPhieu.diaChiGiao && (
                      <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.875rem' }}>
                        <span style={{ color: '#7f8c8d' }}>Địa chỉ giao:</span>
                        <span style={{ fontWeight: '600', color: '#2c3e50' }}>{viewingPhieu.diaChiGiao}</span>
                      </div>
                    )}
                  </div>
                </div>
              </div>
              {/* Chi tiết hàng hóa */}
              <div style={{ marginBottom: '1.5rem' }}>
                <h4 style={{
                  color: '#2c3e50',
                  marginBottom: '1rem',
                  fontSize: '1rem',
                  fontWeight: '600'
                }}>
                  📦 Chi tiết hàng hóa
                </h4>
                <div style={{ overflowX: 'auto', border: '1px solid #dee2e6', borderRadius: '0.5rem' }}>
                  <table style={{ width: '100%', borderCollapse: 'collapse' }}>
                    <thead>
                      <tr style={{ backgroundColor: '#f8f9fa' }}>
                        <th style={{ padding: '0.75rem', border: '1px solid #dee2e6', textAlign: 'left', fontSize: '0.875rem', fontWeight: '600' }}>STT</th>
                        <th style={{ padding: '0.75rem', border: '1px solid #dee2e6', textAlign: 'left', fontSize: '0.875rem', fontWeight: '600' }}>Hàng hóa</th>
                        <th style={{ padding: '0.75rem', border: '1px solid #dee2e6', textAlign: 'center', fontSize: '0.875rem', fontWeight: '600' }}>Số lô</th>
                        <th style={{ padding: '0.75rem', border: '1px solid #dee2e6', textAlign: 'center', fontSize: '0.875rem', fontWeight: '600' }}>Vị trí</th>
                        <th style={{ padding: '0.75rem', border: '1px solid #dee2e6', textAlign: 'center', fontSize: '0.875rem', fontWeight: '600' }}>SL yêu cầu</th>
                        <th style={{ padding: '0.75rem', border: '1px solid #dee2e6', textAlign: 'center', fontSize: '0.875rem', fontWeight: '600' }}>SL xuất</th>
                        <th style={{ padding: '0.75rem', border: '1px solid #dee2e6', textAlign: 'right', fontSize: '0.875rem', fontWeight: '600' }}>Đơn giá</th>
                        <th style={{ padding: '0.75rem', border: '1px solid #dee2e6', textAlign: 'right', fontSize: '0.875rem', fontWeight: '600' }}>Thành tiền</th>
                      </tr>
                    </thead>
                    <tbody>
                      {viewingPhieu.chiTiet && viewingPhieu.chiTiet.map((item, index) => (
                        <tr key={index} style={{ backgroundColor: 'white' }}>
                          <td style={{ padding: '0.75rem', border: '1px solid #dee2e6', fontSize: '0.875rem' }}>{index + 1}</td>
                          <td style={{ padding: '0.75rem', border: '1px solid #dee2e6', fontWeight: '500', fontSize: '0.875rem' }}>
                            {/* ✅ THÊM: Flex container cho ảnh + tên */}
                            <div style={{ display: 'flex', gap: '0.75rem', alignItems: 'center' }}>
                              {/* Hình ảnh hàng hóa */}
                              {item.hinhAnhUrl ? (
                                <img
                                  src={getImageUrl(item.hinhAnhUrl)}
                                  alt={item.tenHangHoa}
                                  style={{
                                    width: '50px',
                                    height: '50px',
                                    objectFit: 'cover',
                                    borderRadius: '6px',
                                    border: '2px solid #e0e0e0',
                                    flexShrink: 0
                                  }}
                                  onError={(e) => {
                                    e.target.onerror = null;
                                    e.target.style.display = 'none';
                                  }}
                                />
                              ) : (
                                <div style={{
                                  width: '50px',
                                  height: '50px',
                                  backgroundColor: '#f5f5f5',
                                  borderRadius: '6px',
                                  display: 'flex',
                                  alignItems: 'center',
                                  justifyContent: 'center',
                                  fontSize: '1.2rem',
                                  flexShrink: 0
                                }}>
                                  📦
                                </div>
                              )}

                              {/* Tên + Mã hàng hóa */}
                              <div>
                                <div style={{ fontWeight: '600' }}>{item.tenHangHoa}</div>
                                <div style={{ fontSize: '0.75rem', color: '#7f8c8d', marginTop: '0.25rem' }}>
                                  {item.maHangHoa}
                                </div>
                              </div>
                            </div>
                          </td>
                          <td style={{ padding: '0.75rem', border: '1px solid #dee2e6', textAlign: 'center', fontSize: '0.875rem', color: '#7f8c8d' }}>
                            {item.soLo || '-'}
                          </td>
                          <td style={{ padding: '0.75rem', border: '1px solid #dee2e6', textAlign: 'center', fontSize: '0.875rem', color: '#7f8c8d' }}>
                            {item.tenViTriKho || '-'}
                          </td>
                          <td style={{ padding: '0.75rem', border: '1px solid #dee2e6', textAlign: 'center', fontSize: '0.875rem' }}>
                            {item.soLuongYeuCau}
                          </td>
                          <td style={{ padding: '0.75rem', border: '1px solid #dee2e6', textAlign: 'center', fontSize: '0.875rem', fontWeight: '600', color: '#27ae60' }}>
                            {item.soLuongXuat}
                          </td>
                          <td style={{ padding: '0.75rem', border: '1px solid #dee2e6', textAlign: 'right', fontSize: '0.875rem' }}>
                            {formatCurrency(item.donGia)}
                          </td>
                          <td style={{ padding: '0.75rem', border: '1px solid #dee2e6', textAlign: 'right', fontWeight: '600', fontSize: '0.875rem' }}>
                            {formatCurrency(item.thanhTien)}
                          </td>
                        </tr>
                      ))}
                    </tbody>
                    <tfoot>
                      <tr style={{ backgroundColor: '#d5f4e6' }}>
                        <td colSpan="7" style={{ padding: '0.75rem', border: '1px solid #dee2e6', textAlign: 'right', fontWeight: '600', fontSize: '0.875rem' }}>
                          Tổng cộng:
                        </td>
                        <td style={{ padding: '0.75rem', border: '1px solid #dee2e6', textAlign: 'right', fontWeight: 'bold', color: '#27ae60', fontSize: '1rem' }}>
                          {formatCurrency(viewingPhieu.tongGiaTri || 0)}
                        </td>
                      </tr>
                    </tfoot>
                  </table>
                </div>
              </div>

              {/* Lý do xuất và ghi chú */}
              {(viewingPhieu.lyDoXuat || viewingPhieu.ghiChu || viewingPhieu.lyDoHuy) && (
                <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
                  {viewingPhieu.lyDoXuat && (
                    <div style={{
                      backgroundColor: '#e3f2fd',
                      border: '1px solid #90caf9',
                      borderRadius: '0.5rem',
                      padding: '1rem',
                      borderLeft: '4px solid #2196f3'
                    }}>
                      <h5 style={{ fontWeight: '600', color: '#1976d2', marginBottom: '0.5rem', fontSize: '0.875rem' }}>
                        📝 Lý do xuất
                      </h5>
                      <p style={{ color: '#424242', margin: 0, fontSize: '0.875rem' }}>
                        {viewingPhieu.lyDoXuat}
                      </p>
                    </div>
                  )}

                  {viewingPhieu.ghiChu && (
                    <div style={{
                      backgroundColor: '#fff9e6',
                      border: '1px solid #ffe082',
                      borderRadius: '0.5rem',
                      padding: '1rem',
                      borderLeft: '4px solid #ffc107'
                    }}>
                      <h5 style={{ fontWeight: '600', color: '#f57c00', marginBottom: '0.5rem', fontSize: '0.875rem' }}>
                        💬 Ghi chú
                      </h5>
                      <p style={{ color: '#5d4037', margin: 0, fontSize: '0.875rem' }}>
                        {viewingPhieu.ghiChu}
                      </p>
                    </div>
                  )}

                  {viewingPhieu.lyDoHuy && (
                    <div style={{
                      backgroundColor: '#ffebee',
                      border: '1px solid #ef5350',
                      borderRadius: '0.5rem',
                      padding: '1rem',
                      borderLeft: '4px solid #e74c3c'
                    }}>
                      <h5 style={{ fontWeight: '600', color: '#c62828', marginBottom: '0.5rem', fontSize: '0.875rem' }}>
                        ❌ Lý do hủy
                      </h5>
                      <p style={{ color: '#d32f2f', margin: 0, fontSize: '0.875rem' }}>
                        {viewingPhieu.lyDoHuy}
                      </p>
                    </div>
                  )}
                </div>
              )}
            </div>
          )}
        </Modal>
      </div>
    </Layout>
  );
};
export default XuatKho;
import React, { useState, useEffect } from 'react';
import Layout from '../components/layout/Layout';
import { Modal, Table, Loading, ConfirmDialog } from '../components/shared';
import NhapKhoForm from '../components/forms/NhapKhoForm';
import { toast } from 'react-toastify';
import api, { getImageUrl } from '../services/api';

const NhapKho = () => {
  const [phieuNhapList, setPhieuNhapList] = useState([]);
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

  // Thêm state
  const [confirmDialog, setConfirmDialog] = useState({
    isOpen: false,
    type: 'warning',
    title: '',
    message: '',
    onConfirm: null
  });

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
          size: 10,
          sortBy: 'createdAt',        // ✅ ĐÃ ĐÚNG
          sortDir: 'desc'              // ✅ ĐÃ ĐÚNG (desc = mới nhất trước)
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
      const response = await api.post('/api/phieu-nhap', formData);
      toast.success(`Tạo phiếu nhập thành công: ${response.data.data.maPhieuNhap}`);
      setShowCreateForm(false);
      fetchPhieuNhapList();
    } catch (error) {
      const message = error.response?.data?.message || 'Lỗi khi tạo phiếu nhập';
      toast.error(message);
      throw error;
    }
  };

  const handleEditPhieu = async (id) => {
    try {
      const response = await api.get(`/api/phieu-nhap/${id}`);
      const phieu = response.data.data;

      if (phieu.trangThai !== 'NHAP' && phieu.trangThai !== 'CHO_DUYET') {
        toast.error('Chỉ có thể sửa phiếu đang nhập hoặc chờ duyệt');
        return;
      }

      setEditingPhieu(phieu);
      setShowEditForm(true);
    } catch (error) {
      toast.error('Lỗi khi tải thông tin phiếu nhập');
    }
  };

  const handleUpdatePhieuNhap = async (formData) => {
    try {
      await api.put(`/api/phieu-nhap/${editingPhieu.id}`, formData);
      toast.success('Cập nhật phiếu nhập thành công');
      setShowEditForm(false);
      setEditingPhieu(null);
      fetchPhieuNhapList();
    } catch (error) {
      const message = error.response?.data?.message || 'Lỗi khi cập nhật phiếu nhập';
      toast.error(message);
      throw error;
    }
  };

  const handleDuyetPhieu = async (id) => {
    setConfirmDialog({
      isOpen: true,
      type: 'warning',
      title: 'Xác nhận duyệt phiếu nhập',
      message: `Sau khi duyệt:
✓ Tồn kho sẽ được cập nhật
✓ Lô hàng sẽ được tạo tự động
✗ Không thể sửa hoặc xóa`,
      onConfirm: async () => {
        try {
          await api.patch(`/api/phieu-nhap/${id}/duyet`);
          toast.success('✅ Duyệt thành công!');
          fetchPhieuNhapList();
        } catch (error) {
          toast.error(error.response?.data?.message);
        }
      }
    });
  };

  const handleHuyPhieu = async (id) => {
    const lyDoHuy = window.prompt('Nhập lý do hủy phiếu:');

    if (lyDoHuy === null) return;

    if (!lyDoHuy.trim()) {
      toast.error('Lý do hủy không được để trống');
      return;
    }

    try {
      await api.patch(`/api/phieu-nhap/${id}/huy?lyDoHuy=${encodeURIComponent(lyDoHuy)}`);
      toast.success('Hủy phiếu nhập thành công');
      fetchPhieuNhapList();
    } catch (error) {
      const message = error.response?.data?.message || 'Lỗi khi hủy phiếu nhập';
      toast.error(message);
    }
  };

  // ✅ THÊM: Handler hủy duyệt
  const handleHuyDuyetPhieu = async (id) => {
    const lyDoHuyDuyet = window.prompt(
      '⚠️ HỦY DUYỆT PHIẾU NHẬP\n\n' +
      'Hành động này sẽ:\n' +
      '• Hoàn nguyên tồn kho\n' +
      '• Xóa/giảm lô hàng đã tạo\n' +
      '• Chuyển phiếu về trạng thái "Chờ duyệt"\n\n' +
      'Nhập lý do hủy duyệt:'
    );

    if (lyDoHuyDuyet === null) return;

    if (!lyDoHuyDuyet.trim()) {
      toast.error('Lý do hủy duyệt không được để trống');
      return;
    }

    try {
      await api.patch(`/api/phieu-nhap/${id}/huy-duyet?lyDoHuyDuyet=${encodeURIComponent(lyDoHuyDuyet)}`);
      toast.success('✅ Hủy duyệt thành công! Tồn kho đã được hoàn nguyên.');
      fetchPhieuNhapList();
    } catch (error) {
      const message = error.response?.data?.message || 'Lỗi khi hủy duyệt phiếu nhập';
      toast.error(message);
    }
  };

  const handleDeletePhieu = async (id, trangThai) => {
    if (trangThai === 'DA_DUYET') {
      toast.error('Không thể xóa phiếu đã duyệt');
      return;
    }

    if (window.confirm('Bạn có chắc muốn XÓA phiếu nhập này?\n\nHành động này KHÔNG thể hoàn tác!')) {
      try {
        await api.delete(`/api/phieu-nhap/${id}`);
        toast.success('Xóa phiếu nhập thành công');
        fetchPhieuNhapList();
      } catch (error) {
        const message = error.response?.data?.message || 'Lỗi khi xóa phiếu nhập';
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

  const columns = [
    { title: 'Mã Phiếu', dataIndex: 'maPhieuNhap' },
    { title: 'Kho', dataIndex: 'tenKho' },
    { title: 'NCC', dataIndex: 'tenNhaCungCap', render: (value) => value || '-' },

    // ✅ ĐÃ SỬA
    {
      title: 'Thời gian tạo',
      dataIndex: 'createdAt',
      render: formatDateTime
    },

    {
      title: 'Tổng Giá Trị',
      dataIndex: 'tongThanhToan',
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
          NHAP: { label: 'Đang nhập', bg: '#e3f2fd', color: '#1976d2' },
          CHO_DUYET: { label: 'Chờ duyệt', bg: '#fef5e7', color: '#f39c12' },
          DA_DUYET: { label: 'Đã duyệt', bg: '#d5f4e6', color: '#27ae60' },
          HUY: { label: 'Đã hủy', bg: '#fadbd8', color: '#e74c3c' }
        };
        const config = statusConfig[value] || statusConfig.NHAP;
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

          {(record.trangThai === 'NHAP' || record.trangThai === 'CHO_DUYET') && (
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

          {/* ✅ Phần Đã Duyệt - Có nút Hủy duyệt + Excel */}
          {record.trangThai === 'DA_DUYET' && (
            <>
              {/* Nút Hủy Duyệt */}
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
                ↩️ Hủy duyệt
              </button>
            </>
          )}

          {/* ✅ BỔ SUNG: Nút Export Excel - Luôn hiển thị cho tất cả phiếu */}
          <button
            onClick={async () => {
              try {
                const response = await api.get(`/api/phieu-nhap/${record.id}/export-excel`, {
                  responseType: 'blob'
                });
                const url = window.URL.createObjectURL(new Blob([response.data]));
                const link = document.createElement('a');
                link.href = url;
                link.setAttribute('download', `Phieu_Nhap_${record.maPhieuNhap}.xlsx`);
                document.body.appendChild(link);
                link.click();
                link.remove();
                window.URL.revokeObjectURL(url);
                toast.success('Xuất Excel thành công!');
              } catch (error) {
                toast.error('Lỗi khi xuất Excel');
              }
            }}
            style={{
              padding: '0.5rem 0.75rem',
              backgroundColor: '#16a085',
              color: 'white',
              border: 'none',
              borderRadius: '0.5rem',
              cursor: 'pointer',
              fontSize: '0.875rem'
            }}
            title="Xuất Excel"
          >
            📊 Excel
          </button>
        </div>
      )
    }
  ];

  const totalValue = phieuNhapList.reduce((sum, p) => sum + (p.tongThanhToan || 0), 0);
  const pendingCount = phieuNhapList.filter(p => p.trangThai === 'CHO_DUYET' || p.trangThai === 'NHAP').length;
  const approvedCount = phieuNhapList.filter(p => p.trangThai === 'DA_DUYET').length;

  return (
    <Layout>
      <div style={{ padding: '1.5rem' }}>
        <div style={{ marginBottom: '2rem' }}>
          <h1 style={{ fontSize: '1.875rem', fontWeight: 'bold', color: '#2c3e50', marginBottom: '0.5rem' }}>
            Quản lý Nhập Kho
          </h1>
          <p style={{ color: '#7f8c8d' }}>Tạo, duyệt và quản lý phiếu nhập kho</p>
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
              {phieuNhapList.length}
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
              {new Intl.NumberFormat('vi-VN', {
                style: 'currency',
                currency: 'VND',
                maximumFractionDigits: 0
              }).format(totalValue)}
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
            <option value="NHAP">Đang nhập</option>
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
              fontSize: '1rem'
            }}
          >
            + Tạo Phiếu Nhập
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
                emptyMessage="Không tìm thấy phiếu nhập nào"
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
          title="Tạo Phiếu Nhập Mới"
          size="xlarge"
        >
          <NhapKhoForm
            onSubmit={handleCreatePhieuNhap}
            onCancel={() => setShowCreateForm(false)}
          />
        </Modal>

        <Modal
          isOpen={showEditForm}
          onClose={() => {
            setShowEditForm(false);
            setEditingPhieu(null);
          }}
          title="Chỉnh Sửa Phiếu Nhập"
          size="xlarge"
        >
          <NhapKhoForm
            initialData={editingPhieu}
            onSubmit={handleUpdatePhieuNhap}
            onCancel={() => {
              setShowEditForm(false);
              setEditingPhieu(null);
            }}
          />
        </Modal>

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
                  <h4 style={{ color: '#2c3e50', marginBottom: '1rem', fontSize: '1rem', fontWeight: '600' }}>
                    Thông tin phiếu nhập
                  </h4>
                  <div style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
                    <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.875rem' }}>
                      <span style={{ color: '#7f8c8d' }}>Mã phiếu:</span>
                      <span style={{ fontWeight: '600', color: '#2c3e50' }}>{viewingPhieu.maPhieuNhap}</span>
                    </div>

                    {/* ✅ THỜI GIAN TẠO (CÓ GIỜ) */}
                    <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.875rem' }}>
                      <span style={{ color: '#7f8c8d' }}>Thời gian tạo:</span>
                      <span style={{ fontWeight: '600', color: '#2c3e50' }}>
                        {formatDateTime(viewingPhieu.createdAt)}
                      </span>
                    </div>

                    {/* ✅ NGÀY NHẬP DỰ KIẾN (CHỈ NGÀY) */}
                    {/* <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.875rem' }}>
      <span style={{ color: '#7f8c8d' }}>Ngày nhập dự kiến:</span>
      <span style={{ fontWeight: '600', color: '#2c3e50' }}>
        {formatDate(viewingPhieu.ngayNhap)}
      </span>
    </div> */}

                    <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.875rem' }}>
                      <span style={{ color: '#7f8c8d' }}>Kho:</span>
                      <span style={{ fontWeight: '600', color: '#2c3e50' }}>{viewingPhieu.tenKho}</span>
                    </div>
                    <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.875rem' }}>
                      <span style={{ color: '#7f8c8d' }}>NCC:</span>
                      <span style={{ fontWeight: '600', color: '#2c3e50' }}>{viewingPhieu.tenNhaCungCap || 'Không có'}</span>
                    </div>
                    <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.875rem' }}>
                      <span style={{ color: '#7f8c8d' }}>Loại nhập:</span>
                      <span style={{ fontWeight: '600', color: '#2c3e50' }}>{viewingPhieu.loaiNhap}</span>
                    </div>
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', fontSize: '0.875rem' }}>
                      <span style={{ color: '#7f8c8d' }}>Trạng thái:</span>
                      {(() => {
                        const statusConfig = {
                          NHAP: { label: 'Đang nhập', bg: '#e3f2fd', color: '#1976d2' },
                          CHO_DUYET: { label: 'Chờ duyệt', bg: '#fef5e7', color: '#f39c12' },
                          DA_DUYET: { label: 'Đã duyệt', bg: '#d5f4e6', color: '#27ae60' },
                          HUY: { label: 'Đã hủy', bg: '#fadbd8', color: '#e74c3c' }
                        };
                        const config = statusConfig[viewingPhieu.trangThai] || statusConfig.NHAP;
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

                <div style={{ backgroundColor: '#f8f9fa', padding: '1.25rem', borderRadius: '0.5rem' }}>
                  <h4 style={{ color: '#2c3e50', marginBottom: '1rem', fontSize: '1rem', fontWeight: '600' }}>
                    Thông tin giao hàng
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
                    <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.875rem' }}>
                      <span style={{ color: '#7f8c8d' }}>Ngày hóa đơn:</span>
                      <span style={{ fontWeight: '600', color: '#2c3e50' }}>
                        {viewingPhieu.ngayHoaDon ? formatDate(viewingPhieu.ngayHoaDon) : 'Không có'}
                      </span>
                    </div>
                    <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.875rem' }}>
                      <span style={{ color: '#7f8c8d' }}>Người nhận:</span>
                      <span style={{ fontWeight: '600', color: '#2c3e50' }}>{viewingPhieu.tenNguoiNhan || 'Không có'}</span>
                    </div>
                    {viewingPhieu.trangThai === 'DA_DUYET' && (
                      <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.875rem' }}>
                        <span style={{ color: '#7f8c8d' }}>Người duyệt:</span>
                        <span style={{ fontWeight: '600', color: '#2c3e50' }}>{viewingPhieu.tenNguoiDuyet || 'Không có'}</span>
                      </div>
                    )}
                  </div>
                </div>
              </div>

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
                        <th style={{ padding: '0.75rem', border: '1px solid #dee2e6', textAlign: 'left', fontSize: '0.875rem', fontWeight: '600' }}>Vị trí kho</th>
                        <th style={{ padding: '0.75rem', border: '1px solid #dee2e6', textAlign: 'center', fontSize: '0.875rem', fontWeight: '600' }}>SL</th>
                        <th style={{ padding: '0.75rem', border: '1px solid #dee2e6', textAlign: 'right', fontSize: '0.875rem', fontWeight: '600' }}>Đơn giá</th>
                        <th style={{ padding: '0.75rem', border: '1px solid #dee2e6', textAlign: 'right', fontSize: '0.875rem', fontWeight: '600' }}>Thành tiền</th>
                        <th style={{ padding: '0.75rem', border: '1px solid #dee2e6', textAlign: 'center', fontSize: '0.875rem', fontWeight: '600' }}>Số lô</th>
                        <th style={{ padding: '0.75rem', border: '1px solid #dee2e6', textAlign: 'center', fontSize: '0.875rem', fontWeight: '600' }}>HSD</th>
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
                          <td style={{ padding: '0.75rem', border: '1px solid #dee2e6', fontSize: '0.875rem', color: '#7f8c8d' }}>
                            {item.tenViTriKho || 'Chưa xác định'}
                          </td>
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
                          <td style={{ padding: '0.75rem', border: '1px solid #dee2e6', textAlign: 'center', fontSize: '0.875rem' }}>
                            {item.hanSuDung ? formatDate(item.hanSuDung) : '-'}
                          </td>
                        </tr>
                      ))}
                    </tbody>
                    <tfoot>
                      <tr style={{ backgroundColor: '#d5f4e6' }}>
                        <td colSpan="5" style={{ padding: '0.75rem', border: '1px solid #dee2e6', textAlign: 'right', fontWeight: '600', fontSize: '0.875rem' }}>
                          Tổng cộng:
                        </td>
                        <td style={{ padding: '0.75rem', border: '1px solid #dee2e6', textAlign: 'right', fontWeight: 'bold', color: '#27ae60', fontSize: '1rem' }}>
                          {new Intl.NumberFormat('vi-VN', {
                            style: 'currency',
                            currency: 'VND'
                          }).format(viewingPhieu.tongThanhToan || 0)}
                        </td>
                        <td colSpan="2" style={{ padding: '0.75rem', border: '1px solid #dee2e6' }}></td>
                      </tr>
                    </tfoot>
                  </table>
                </div>
              </div>

              {(viewingPhieu.ghiChu || viewingPhieu.lyDoHuy) && (
                <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
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

                  {viewingPhieu.lyDoHuy && (
                    <div style={{
                      backgroundColor: '#ffebee',
                      border: '1px solid #ef5350',
                      borderRadius: '0.5rem',
                      padding: '1rem'
                    }}>
                      <h5 style={{ fontWeight: '600', color: '#c62828', marginBottom: '0.5rem', fontSize: '0.875rem' }}>
                        Lý do hủy
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


        {/* ✅ THÊM Ở ĐÂY - Sau tất cả Modal, trước thẻ đóng </div> */}
        <ConfirmDialog
          {...confirmDialog}
          onClose={() => setConfirmDialog({ ...confirmDialog, isOpen: false })}
        />
      </div>
    </Layout>
  );
};

export default NhapKho;
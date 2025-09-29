import React, { useState, useEffect } from 'react';
import Layout from '../components/layout/Layout';
import { Modal, Table, Loading } from '../components/shared';
import LoHangForm from '../components/forms/LoHangForm';
import { toast } from 'react-toastify';
import api from '../services/api';

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
          size: 20
        }
      });
      // SỬA: Truy cập trực tiếp response.data (không có .data.data)
      setLoHangList(response.data.content || []);
      setTotalPages(response.data.totalPages || 0);
    } catch (error) {
      console.error('Error fetching lo hang:', error);
      toast.error('Lỗi khi tải danh sách lô hàng');
    } finally {
      setLoading(false);
    }
  };

  const handleCreateLoHang = async (formData) => {
    try {
      await api.post('/api/lo-hang', formData);
      toast.success('Tạo lô hàng thành công');
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

  const handleDeleteLoHang = async (id) => {
    if (window.confirm('Bạn có chắc muốn xóa lô hàng này?')) {
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
      // SỬA: Truy cập trực tiếp response.data
      setViewingLoHang(response.data);
      setShowDetailModal(true);
    } catch (error) {
      console.error('Error fetching lo hang detail:', error);
      toast.error('Lỗi khi tải thông tin lô hàng');
    }
  };

  const columns = [
    { title: 'Số Lô', dataIndex: 'soLo' },
    { title: 'Hàng Hóa', dataIndex: 'tenHangHoa' },
    { title: 'ĐVT', dataIndex: 'tenDonViTinh', align: 'center' },
    {
      title: 'Số Lượng',
      dataIndex: 'soLuongHienTai',
      align: 'center',
      render: (value, record) => (
        <div>
          <span style={{
            fontWeight: 'bold',
            color: value === 0 ? '#e74c3c' : '#27ae60'
          }}>
            {value}
          </span>
          <span style={{ color: '#7f8c8d', fontSize: '0.85rem' }}>
            /{record.soLuongNhap}
          </span>
        </div>
      )
    },
    {
      title: 'Hạn Sử Dụng',
      dataIndex: 'hanSuDung',
      render: (value, record) => (
        <div>
          <div>{value}</div>
          {record.soNgayConLai !== null && (
            <div style={{
              fontSize: '0.75rem',
              color: record.soNgayConLai < 0 ? '#e74c3c' :
                     record.soNgayConLai < 30 ? '#e67e22' :
                     record.soNgayConLai < 90 ? '#f39c12' : '#27ae60'
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
      render: (value) => {
        const colors = {
          MOI: '#3498db',
          DANG_SU_DUNG: '#27ae60',
          GAN_HET_HAN: '#f39c12',
          HET_HAN: '#e74c3c',
          HET_HANG: '#95a5a6'
        };
        const labels = {
          MOI: 'Mới',
          DANG_SU_DUNG: 'Đang sử dụng',
          GAN_HET_HAN: 'Gần hết hạn',
          HET_HAN: 'Hết hạn',
          HET_HANG: 'Hết hàng'
        };
        return (
          <span style={{
            padding: '0.25rem 0.5rem',
            borderRadius: '4px',
            fontSize: '0.8rem',
            backgroundColor: colors[value] + '20',
            color: colors[value]
          }}>
            {labels[value]}
          </span>
        );
      }
    },
    {
      title: 'Thao Tác',
      dataIndex: 'actions',
      align: 'center',
      render: (_, record) => (
        <div style={{ display: 'flex', gap: '0.5rem', justifyContent: 'center' }}>
          <button
            onClick={() => handleViewDetail(record.id)}
            style={{
              padding: '0.25rem 0.5rem',
              backgroundColor: '#3498db',
              color: 'white',
              border: 'none',
              borderRadius: '4px',
              cursor: 'pointer'
            }}
            title="Xem chi tiết"
          >
            👁️
          </button>
          <button
            onClick={() => {
              setEditingLoHang(record);
              setShowModal(true);
            }}
            style={{
              padding: '0.25rem 0.5rem',
              backgroundColor: '#f39c12',
              color: 'white',
              border: 'none',
              borderRadius: '4px',
              cursor: 'pointer'
            }}
            title="Chỉnh sửa"
          >
            ✏️
          </button>
          <button
            onClick={() => handleDeleteLoHang(record.id)}
            style={{
              padding: '0.25rem 0.5rem',
              backgroundColor: record.soLuongHienTai < record.soLuongNhap ? '#95a5a6' : '#e74c3c',
              color: 'white',
              border: 'none',
              borderRadius: '4px',
              cursor: record.soLuongHienTai < record.soLuongNhap ? 'not-allowed' : 'pointer',
              opacity: record.soLuongHienTai < record.soLuongNhap ? 0.6 : 1
            }}
            title={record.soLuongHienTai < record.soLuongNhap ? 'Không thể xóa lô đã xuất' : 'Xóa'}
            disabled={record.soLuongHienTai < record.soLuongNhap}
          >
            🗑️
          </button>
        </div>
      )
    }
  ];

  return (
    <Layout>
      <div>
        <h2 style={{ marginBottom: '1.5rem', color: '#2c3e50' }}>Quản lý Lô Hàng</h2>

        {/* Filter & Search Bar */}
        <div style={{
          backgroundColor: 'white',
          padding: '1rem',
          borderRadius: '8px',
          marginBottom: '1.5rem',
          boxShadow: '0 2px 4px rgba(0,0,0,0.1)',
          display: 'flex',
          gap: '1rem',
          alignItems: 'center',
          flexWrap: 'wrap'
        }}>
          <input
            type="text"
            placeholder="Tìm kiếm theo số lô, tên hàng hóa..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            style={{
              flex: 1,
              minWidth: '200px',
              padding: '0.75rem',
              border: '1px solid #ddd',
              borderRadius: '6px',
              fontSize: '1rem'
            }}
          />
          <select
            value={selectedTrangThai}
            onChange={(e) => setSelectedTrangThai(e.target.value)}
            style={{
              padding: '0.75rem',
              border: '1px solid #ddd',
              borderRadius: '6px',
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
            padding: '0.75rem',
            backgroundColor: sapHetHan ? '#fff3e0' : '#f8f9fa',
            borderRadius: '6px',
            cursor: 'pointer'
          }}>
            <input
              type="checkbox"
              checked={sapHetHan}
              onChange={(e) => setSapHetHan(e.target.checked)}
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
              borderRadius: '6px',
              cursor: 'pointer',
              fontWeight: '500'
            }}
          >
            ➕ Thêm Lô Hàng
          </button>
        </div>

        {/* Quick Stats */}
        <div style={{
          display: 'grid',
          gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))',
          gap: '1rem',
          marginBottom: '1.5rem'
        }}>
          <div style={{
            backgroundColor: 'white',
            padding: '1rem',
            borderRadius: '8px',
            boxShadow: '0 2px 4px rgba(0,0,0,0.1)',
            textAlign: 'center',
            border: '3px solid #3498db'
          }}>
            <div style={{ fontSize: '1.5rem', fontWeight: 'bold', color: '#3498db' }}>
              {loHangList.length}
            </div>
            <div style={{ color: '#7f8c8d' }}>Tổng lô hàng</div>
          </div>
          <div style={{
            backgroundColor: 'white',
            padding: '1rem',
            borderRadius: '8px',
            boxShadow: '0 2px 4px rgba(0,0,0,0.1)',
            textAlign: 'center',
            border: '3px solid #f39c12'
          }}>
            <div style={{ fontSize: '1.5rem', fontWeight: 'bold', color: '#f39c12' }}>
              {loHangList.filter(l => l.sapHetHan).length}
            </div>
            <div style={{ color: '#7f8c8d' }}>Sắp hết hạn</div>
          </div>
          <div style={{
            backgroundColor: 'white',
            padding: '1rem',
            borderRadius: '8px',
            boxShadow: '0 2px 4px rgba(0,0,0,0.1)',
            textAlign: 'center',
            border: '3px solid #e74c3c'
          }}>
            <div style={{ fontSize: '1.5rem', fontWeight: 'bold', color: '#e74c3c' }}>
              {loHangList.filter(l => l.trangThai === 'HET_HAN').length}
            </div>
            <div style={{ color: '#7f8c8d' }}>Đã hết hạn</div>
          </div>
        </div>

        {/* Table */}
        <div style={{
          backgroundColor: 'white',
          borderRadius: '8px',
          boxShadow: '0 2px 4px rgba(0,0,0,0.1)',
          overflow: 'hidden'
        }}>
          {loading ? (
            <Loading />
          ) : (
            <>
              <Table
                columns={columns}
                data={loHangList}
                loading={loading}
                emptyMessage="Không tìm thấy lô hàng nào"
              />

              {/* Pagination */}
              {totalPages > 1 && (
                <div style={{ padding: '1rem', textAlign: 'center', borderTop: '1px solid #dee2e6' }}>
                  {Array.from({ length: totalPages }, (_, i) => (
                    <button
                      key={i}
                      onClick={() => setCurrentPage(i)}
                      style={{
                        margin: '0 0.25rem',
                        padding: '0.5rem 0.75rem',
                        border: '1px solid #ddd',
                        backgroundColor: i === currentPage ? '#3498db' : 'white',
                        color: i === currentPage ? 'white' : '#333',
                        borderRadius: '4px',
                        cursor: 'pointer'
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
              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1.5rem' }}>
                <div>
                  <h4 style={{ color: '#2c3e50', marginBottom: '1rem' }}>Thông tin lô hàng</h4>
                  <div style={{ marginBottom: '0.5rem' }}>
                    <strong>Số lô:</strong> {viewingLoHang.soLo}
                  </div>
                  <div style={{ marginBottom: '0.5rem' }}>
                    <strong>Hàng hóa:</strong> {viewingLoHang.tenHangHoa}
                  </div>
                  <div style={{ marginBottom: '0.5rem' }}>
                    <strong>Ngày sản xuất:</strong> {viewingLoHang.ngaySanXuat || 'Không có'}
                  </div>
                  <div style={{ marginBottom: '0.5rem' }}>
                    <strong>Hạn sử dụng:</strong> {viewingLoHang.hanSuDung}
                  </div>
                  {viewingLoHang.soNgayConLai !== null && (
                    <div style={{
                      marginTop: '0.5rem',
                      padding: '0.5rem',
                      borderRadius: '4px',
                      backgroundColor: viewingLoHang.soNgayConLai < 30 ? '#ffebee' : '#e3f2fd',
                      color: viewingLoHang.soNgayConLai < 30 ? '#c62828' : '#1565c0'
                    }}>
                      {viewingLoHang.soNgayConLai < 0 ?
                        `Đã hết hạn ${Math.abs(viewingLoHang.soNgayConLai)} ngày` :
                        `Còn ${viewingLoHang.soNgayConLai} ngày đến hạn`}
                    </div>
                  )}
                </div>

                <div>
                  <h4 style={{ color: '#2c3e50', marginBottom: '1rem' }}>Thông tin số lượng</h4>
                  <div style={{ marginBottom: '0.5rem' }}>
                    <strong>Số lượng nhập:</strong> {viewingLoHang.soLuongNhap}
                  </div>
                  <div style={{ marginBottom: '0.5rem' }}>
                    <strong>Số lượng hiện tại:</strong>
                    <span style={{
                      marginLeft: '0.5rem',
                      color: viewingLoHang.soLuongHienTai === 0 ? '#e74c3c' : '#27ae60',
                      fontWeight: 'bold'
                    }}>
                      {viewingLoHang.soLuongHienTai}
                    </span>
                  </div>
                  <div style={{ marginBottom: '0.5rem' }}>
                    <strong>Giá nhập:</strong> {new Intl.NumberFormat('vi-VN', {
                      style: 'currency',
                      currency: 'VND'
                    }).format(viewingLoHang.giaNhap)}
                  </div>
                  <div style={{ marginBottom: '0.5rem' }}>
                    <strong>Nhà cung cấp:</strong> {viewingLoHang.tenNhaCungCap || 'Không có'}
                  </div>
                </div>
              </div>

              {viewingLoHang.ghiChu && (
                <div style={{ marginTop: '1.5rem' }}>
                  <h4 style={{ color: '#2c3e50', marginBottom: '0.5rem' }}>Ghi chú</h4>
                  <p style={{
                    backgroundColor: '#f8f9fa',
                    padding: '1rem',
                    borderRadius: '4px',
                    margin: 0
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
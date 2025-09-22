import React, { useState, useEffect } from 'react';
import Layout from '../components/layout/Layout';
import { Modal, Table, Loading } from '../components/shared';
import HangHoaForm from '../components/forms/HangHoaForm';
import { toast } from 'react-toastify';
import api from '../services/api';

const HangHoa = () => {
  const [hangHoaList, setHangHoaList] = useState([]);
  const [loading, setLoading] = useState(false);
  const [showModal, setShowModal] = useState(false);
  const [editingHangHoa, setEditingHangHoa] = useState(null);
  const [viewingHangHoa, setViewingHangHoa] = useState(null);
  const [showDetailModal, setShowDetailModal] = useState(false);
  const [searchTerm, setSearchTerm] = useState('');
  const [selectedDanhMuc, setSelectedDanhMuc] = useState('');
  const [selectedTrangThai, setSelectedTrangThai] = useState('HOAT_DONG');
  const [currentPage, setCurrentPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [danhMucList, setDanhMucList] = useState([]);

  useEffect(() => {
    fetchHangHoaList();
  }, [currentPage, searchTerm, selectedDanhMuc, selectedTrangThai]);

  useEffect(() => {
    fetchDanhMucList();
  }, []);

  const fetchHangHoaList = async () => {
    setLoading(true);
    try {
      const response = await api.get('/api/hang-hoa', {
        params: {
          search: searchTerm,
          danhMucId: selectedDanhMuc || undefined,
          trangThai: selectedTrangThai,
          page: currentPage,
          size: 10
        }
      });
      setHangHoaList(response.data.data.content || []);
      setTotalPages(response.data.data.totalPages || 0);
    } catch (error) {
      toast.error('Lỗi khi tải danh sách hàng hóa');
    } finally {
      setLoading(false);
    }
  };

  const fetchDanhMucList = async () => {
    try {
      const response = await api.get('/api/danh-muc/root');
      setDanhMucList(response.data.data || []);
    } catch (error) {
      console.error('Error fetching categories:', error);
    }
  };

  const handleCreateHangHoa = async (formData) => {
    try {
      await api.post('/api/hang-hoa', formData);
      toast.success('Tạo hàng hóa thành công');
      setShowModal(false);
      fetchHangHoaList();
    } catch (error) {
      const message = error.response?.data?.message || 'Lỗi khi tạo hàng hóa';
      toast.error(message);
      throw error;
    }
  };

  const handleUpdateHangHoa = async (formData) => {
    try {
      await api.put(`/api/hang-hoa/${editingHangHoa.id}`, formData);
      toast.success('Cập nhật hàng hóa thành công');
      setShowModal(false);
      setEditingHangHoa(null);
      fetchHangHoaList();
    } catch (error) {
      const message = error.response?.data?.message || 'Lỗi khi cập nhật hàng hóa';
      toast.error(message);
      throw error;
    }
  };

  const handleDeleteHangHoa = async (id) => {
    if (window.confirm('Bạn có chắc muốn xóa hàng hóa này?')) {
      try {
        await api.delete(`/api/hang-hoa/${id}`);
        toast.success('Xóa hàng hóa thành công');
        fetchHangHoaList();
      } catch (error) {
        const message = error.response?.data?.message || 'Lỗi khi xóa hàng hóa';
        toast.error(message);
      }
    }
  };

  const handleViewDetail = async (id) => {
    try {
      const response = await api.get(`/api/hang-hoa/${id}`);
      setViewingHangHoa(response.data.data);
      setShowDetailModal(true);
    } catch (error) {
      toast.error('Lỗi khi tải thông tin hàng hóa');
    }
  };

  const columns = [
    { title: 'Mã Hàng Hóa', dataIndex: 'maHangHoa' },
    { title: 'Tên Hàng Hóa', dataIndex: 'tenHangHoa' },
    { title: 'Danh Mục', dataIndex: 'tenDanhMuc' },
    { title: 'Đơn Vị Tính', dataIndex: 'tenDonViTinh' },
    {
      title: 'Tồn Kho',
      dataIndex: 'soLuongCoTheXuat',
      align: 'center',
      render: (value, record) => (
        <span style={{
          color: value < record.soLuongToiThieu ? '#e74c3c' : '#27ae60',
          fontWeight: 'bold'
        }}>
          {value || 0}
        </span>
      )
    },
    {
      title: 'Trạng Thái',
      dataIndex: 'trangThai',
      render: (value) => (
        <span style={{
          padding: '0.25rem 0.5rem',
          borderRadius: '4px',
          fontSize: '0.8rem',
          backgroundColor: value === 'HOAT_DONG' ? '#d5f4e6' : value === 'TAM_DUNG' ? '#ffeaa7' : '#fab1a0',
          color: value === 'HOAT_DONG' ? '#00b894' : value === 'TAM_DUNG' ? '#fdcb6e' : '#e17055'
        }}>
          {value === 'HOAT_DONG' ? 'Hoạt động' :
            value === 'TAM_DUNG' ? 'Tạm dừng' : 'Ngừng kinh doanh'}
        </span>
      )
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
              setEditingHangHoa(record);
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
            onClick={() => handleDeleteHangHoa(record.id)}
            style={{
              padding: '0.25rem 0.5rem',
              backgroundColor: '#e74c3c',
              color: 'white',
              border: 'none',
              borderRadius: '4px',
              cursor: 'pointer'
            }}
            title="Xóa"
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
        <h2 style={{ marginBottom: '1.5rem', color: '#2c3e50' }}>Quản lý Hàng Hóa</h2>

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
            placeholder="Tìm kiếm hàng hóa..."
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
            value={selectedDanhMuc}
            onChange={(e) => setSelectedDanhMuc(e.target.value)}
            style={{
              padding: '0.75rem',
              border: '1px solid #ddd',
              borderRadius: '6px',
              fontSize: '1rem',
              minWidth: '150px'
            }}
          >
            <option value="">Tất cả danh mục</option>
            {danhMucList.map(dm => (
              <option key={dm.id} value={dm.id}>{dm.tenDanhMuc}</option>
            ))}
          </select>
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
            <option value="HOAT_DONG">Hoạt động</option>
            <option value="TAM_DUNG">Tạm dừng</option>
            <option value="NGUNG_KINH_DOANH">Ngừng kinh doanh</option>
          </select>
          <button
            onClick={() => {
              setEditingHangHoa(null);
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
            ➕ Thêm Hàng Hóa
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
              {hangHoaList.length}
            </div>
            <div style={{ color: '#7f8c8d' }}>Tổng hàng hóa</div>
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
              {hangHoaList.filter(h => h.soLuongCoTheXuat < h.soLuongToiThieu).length}
            </div>
            <div style={{ color: '#7f8c8d' }}>Tồn kho thấp</div>
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
                data={hangHoaList}
                loading={loading}
                emptyMessage="Không tìm thấy hàng hóa nào"
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
            setEditingHangHoa(null);
          }}
          title={editingHangHoa ? 'Cập nhật Hàng Hóa' : 'Thêm Hàng Hóa Mới'}
          size="xlarge"
        >
          <HangHoaForm
            initialData={editingHangHoa}
            onSubmit={editingHangHoa ? handleUpdateHangHoa : handleCreateHangHoa}
            onCancel={() => {
              setShowModal(false);
              setEditingHangHoa(null);
            }}
          />
        </Modal>

        {/* Detail Modal */}
        <Modal
          isOpen={showDetailModal}
          onClose={() => {
            setShowDetailModal(false);
            setViewingHangHoa(null);
          }}
          title="Chi tiết Hàng Hóa"
          size="large"
        >
          {viewingHangHoa && (
            <div style={{ maxHeight: '70vh', overflowY: 'auto' }}>
              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
                <div>
                  <h4 style={{ color: '#2c3e50', marginBottom: '1rem' }}>Thông tin cơ bản</h4>
                  <div style={{ marginBottom: '0.5rem' }}>
                    <strong>Mã hàng hóa:</strong> {viewingHangHoa.maHangHoa}
                  </div>
                  <div style={{ marginBottom: '0.5rem' }}>
                    <strong>Tên hàng hóa:</strong> {viewingHangHoa.tenHangHoa}
                  </div>
                  <div style={{ marginBottom: '0.5rem' }}>
                    <strong>Tên khoa học:</strong> {viewingHangHoa.tenKhoaHoc || 'Không có'}
                  </div>
                  <div style={{ marginBottom: '0.5rem' }}>
                    <strong>Danh mục:</strong> {viewingHangHoa.tenDanhMuc}
                  </div>
                  <div style={{ marginBottom: '0.5rem' }}>
                    <strong>Đơn vị tính:</strong> {viewingHangHoa.tenDonViTinh}
                  </div>
                  <div style={{ marginBottom: '0.5rem' }}>
                    <strong>Nhà cung cấp:</strong> {viewingHangHoa.tenNhaCungCap || 'Không có'}
                  </div>
                </div>

                <div>
                  <h4 style={{ color: '#2c3e50', marginBottom: '1rem' }}>Thông tin kho</h4>
                  <div style={{ marginBottom: '0.5rem' }}>
                    <strong>Tồn kho hiện tại:</strong>
                    <span style={{
                      color: viewingHangHoa.soLuongCoTheXuat < viewingHangHoa.soLuongToiThieu ? '#e74c3c' : '#27ae60',
                      fontWeight: 'bold',
                      marginLeft: '0.5rem'
                    }}>
                      {viewingHangHoa.soLuongCoTheXuat || 0}
                    </span>
                  </div>
                  <div style={{ marginBottom: '0.5rem' }}>
                    <strong>Số lượng tối thiểu:</strong> {viewingHangHoa.soLuongToiThieu || 0}
                  </div>
                  <div style={{ marginBottom: '0.5rem' }}>
                    <strong>Số lượng tối đa:</strong> {viewingHangHoa.soLuongToiDa || 0}
                  </div>
                  <div style={{ marginBottom: '0.5rem' }}>
                    <strong>Trạng thái:</strong>
                    <span style={{
                      marginLeft: '0.5rem',
                      padding: '0.25rem 0.5rem',
                      borderRadius: '4px',
                      fontSize: '0.8rem',
                      backgroundColor: viewingHangHoa.trangThai === 'HOAT_DONG' ? '#d5f4e6' : '#ffeaa7',
                      color: viewingHangHoa.trangThai === 'HOAT_DONG' ? '#00b894' : '#fdcb6e'
                    }}>
                      {viewingHangHoa.trangThai === 'HOAT_DONG' ? 'Hoạt động' : 'Tạm dừng'}
                    </span>
                  </div>
                </div>
              </div>

              {viewingHangHoa.moTa && (
                <div style={{ marginTop: '1rem' }}>
                  <h4 style={{ color: '#2c3e50', marginBottom: '0.5rem' }}>Mô tả</h4>
                  <p style={{ backgroundColor: '#f8f9fa', padding: '1rem', borderRadius: '4px', margin: 0 }}>
                    {viewingHangHoa.moTa}
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

export default HangHoa;
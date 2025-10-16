import React, { useState, useEffect } from 'react';
import Layout from '../components/layout/Layout';
import Modal from '../components/shared/Modal';
import KhoForm from '../components/forms/KhoForm';
import Table from '../components/shared/Table';
import Loading from '../components/shared/Loading';
import { toast } from 'react-toastify';
import api from '../services/api';

const QuanLyKho = () => {
  const [khoList, setKhoList] = useState([]);
  const [loading, setLoading] = useState(false);
  const [showModal, setShowModal] = useState(false);
  const [editingKho, setEditingKho] = useState(null);
  const [searchTerm, setSearchTerm] = useState('');
  const [selectedLoaiKho, setSelectedLoaiKho] = useState('');
  const [currentPage, setCurrentPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);

  useEffect(() => {
    fetchKhoList();
  }, [currentPage, searchTerm, selectedLoaiKho]);

  const fetchKhoList = async () => {
    setLoading(true);
    try {
      const response = await api.get('/api/kho', {
        params: {
          search: searchTerm,
          loaiKho: selectedLoaiKho || undefined,
          page: currentPage,
          size: 10
        }
      });
      setKhoList(response.data.data.content || []);
      setTotalPages(response.data.data.totalPages || 0);
    } catch (error) {
      toast.error('Lỗi khi tải danh sách kho');
    } finally {
      setLoading(false);
    }
  };

  const handleCreateKho = async (formData) => {
    try {
      await api.post('/api/kho', formData);
      toast.success('Tạo kho thành công');
      setShowModal(false);
      fetchKhoList();
    } catch (error) {
      const message = error.response?.data?.message || 'Lỗi khi tạo kho';
      toast.error(message);
      throw error;
    }
  };

  const handleUpdateKho = async (formData) => {
    try {
      await api.put(`/api/kho/${editingKho.id}`, formData);
      toast.success('Cập nhật kho thành công');
      setShowModal(false);
      setEditingKho(null);
      fetchKhoList();
    } catch (error) {
      const message = error.response?.data?.message || 'Lỗi khi cập nhật kho';
      toast.error(message);
      throw error;
    }
  };

  const handleDeleteKho = async (id) => {
    if (window.confirm('Bạn có chắc muốn xóa kho này?')) {
      try {
        await api.delete(`/api/kho/${id}`);
        toast.success('Xóa kho thành công');
        fetchKhoList();
      } catch (error) {
        const message = error.response?.data?.message || 'Lỗi khi xóa kho';
        toast.error(message);
      }
    }
  };

  const columns = [
    { title: 'Mã Kho', dataIndex: 'maKho' },
    { title: 'Tên Kho', dataIndex: 'tenKho' },
    {
      title: 'Loại Kho',
      dataIndex: 'loaiKho',
      render: (value) => {
        const loaiKhoMap = {
          'KHO_CHINH': 'Kho Chính',
          'KHO_DUOC': 'Kho Dược',
          'KHO_VAT_TU': 'Kho Vật Tư',
          'KHO_THIET_BI': 'Kho Thiết Bị',
          'KHO_TINH_THAT': 'Kho Trang Thiết Bị',
          'KHO_HOA_CHAT': 'Kho Hóa Chất'
        };
        return loaiKhoMap[value] || value;
      }
    },
    { title: 'Khoa Phòng', dataIndex: 'tenKhoaPhong' },
    {
      title: 'Trạng Thái',
      dataIndex: 'trangThai',
      render: (value) => (
        <span style={{
          padding: '0.25rem 0.5rem',
          borderRadius: '4px',
          fontSize: '0.8rem',
          backgroundColor: value === 'HOAT_DONG' ? '#d5f4e6' : value === 'BAO_TRI' ? '#ffeaa7' : '#fab1a0',
          color: value === 'HOAT_DONG' ? '#00b894' : value === 'BAO_TRI' ? '#fdcb6e' : '#e17055'
        }}>
          {value === 'HOAT_DONG' ? 'Hoạt động' : value === 'BAO_TRI' ? 'Bảo trì' : 'Đóng cửa'}
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
            onClick={() => {
              setEditingKho(record);
              setShowModal(true);
            }}
            style={{
              padding: '0.25rem 0.5rem',
              backgroundColor: '#3498db',
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
            onClick={() => handleDeleteKho(record.id)}
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
        <h2 style={{ marginBottom: '1.5rem', color: '#2c3e50' }}>Quản lý Kho</h2>

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
            placeholder="Tìm kiếm kho..."
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
            value={selectedLoaiKho}
            onChange={(e) => setSelectedLoaiKho(e.target.value)}
            style={{
              padding: '0.75rem',
              border: '1px solid #ddd',
              borderRadius: '6px',
              fontSize: '1rem',
              minWidth: '150px'
            }}
          >
            <option value="">Tất cả loại kho</option>
            <option value="KHO_CHINH">Kho Chính</option>
            <option value="KHO_DUOC">Kho Dược</option>
            <option value="KHO_VAT_TU">Kho Vật Tư</option>
            <option value="KHO_THIET_BI">Kho Thiết Bị</option>
            <option value="KHO_TINH_THAT">Kho Trang Thiết Bị</option>
            <option value="KHO_HOA_CHAT">Kho Hóa Chất</option>
          </select>
          <button
            onClick={() => {
              setEditingKho(null);
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
            ➕ Thêm Kho Mới
          </button>
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
                data={khoList}
                loading={loading}
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

        {/* Modal */}
        <Modal
          isOpen={showModal}
          onClose={() => {
            setShowModal(false);
            setEditingKho(null);
          }}
          title={editingKho ? 'Cập nhật Kho' : 'Thêm Kho Mới'}
          size="large"
        >
          <KhoForm
            initialData={editingKho}
            onSubmit={editingKho ? handleUpdateKho : handleCreateKho}
            onCancel={() => {
              setShowModal(false);
              setEditingKho(null);
            }}
          />
        </Modal>
      </div>
    </Layout>
  );
};

export default QuanLyKho;
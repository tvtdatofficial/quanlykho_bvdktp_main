import React, { useState, useEffect } from 'react';
import Layout from '../components/layout/Layout';
import { Modal, Table, Loading } from '../components/shared';
import ViTriKhoForm from '../components/forms/ViTriKhoForm';
import { toast } from 'react-toastify';
import api from '../services/api';

const ViTriKho = () => {
  const [viTriList, setViTriList] = useState([]);
  const [loading, setLoading] = useState(false);
  const [showModal, setShowModal] = useState(false);
  const [editingViTri, setEditingViTri] = useState(null);
  const [currentPage, setCurrentPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [selectedKho, setSelectedKho] = useState('');
  const [khoList, setKhoList] = useState([]);

  useEffect(() => {
    fetchKhoList();
  }, []);

  useEffect(() => {
    if (selectedKho) {
      fetchViTriList();
    }
  }, [currentPage, selectedKho]);

  const fetchKhoList = async () => {
    try {
      const response = await api.get('/api/kho/active');
      setKhoList(response.data.data || response.data || []);
    } catch (error) {
      console.error('Error fetching kho:', error);
    }
  };

  const fetchViTriList = async () => {
    if (!selectedKho) {
      console.log('⚠️ No kho selected');
      return;
    }

    console.log('🔍 Fetching vi tri for kho:', selectedKho);

    setLoading(true);
    try {
      const response = await api.get('/api/vi-tri-kho', {
        params: {
          khoId: selectedKho,
          page: currentPage,
          size: 20
        }
      });

      console.log('📦 Raw response:', response);
      console.log('📦 Response data:', response.data);

      // XỬ LÝ CẢ 2 TRƯỜNG HỢP
      const pageData = response.data.success
        ? response.data.data
        : response.data;

      console.log('📦 Page data:', pageData);
      console.log('📦 Content:', pageData.content);

      setViTriList(pageData.content || []);
      setTotalPages(pageData.totalPages || 0);

      console.log(`✅ Set ${(pageData.content || []).length} items to state`);
    } catch (error) {
      console.error('❌ Error:', error);
      console.error('❌ Error response:', error.response);
      toast.error(error.response?.data?.message || 'Lỗi khi tải danh sách vị trí kho');
    } finally {
      setLoading(false);
    }
  };

  const handleCreate = async (formData) => {
    try {
      await api.post('/api/vi-tri-kho', formData);
      toast.success('Tạo vị trí kho thành công');
      setShowModal(false);
      fetchViTriList();
    } catch (error) {
      const message = error.response?.data?.message || 'Lỗi khi tạo vị trí kho';
      toast.error(message);
      throw error;
    }
  };

  const handleUpdate = async (formData) => {
    try {
      await api.put(`/api/vi-tri-kho/${editingViTri.id}`, formData);
      toast.success('Cập nhật vị trí kho thành công');
      setShowModal(false);
      setEditingViTri(null);
      fetchViTriList();
    } catch (error) {
      const message = error.response?.data?.message || 'Lỗi khi cập nhật vị trí kho';
      toast.error(message);
      throw error;
    }
  };

  const handleDelete = async (id) => {
    if (window.confirm('Bạn có chắc muốn xóa vị trí này?')) {
      try {
        await api.delete(`/api/vi-tri-kho/${id}`);
        toast.success('Xóa vị trí kho thành công');
        fetchViTriList();
      } catch (error) {
        const message = error.response?.data?.message || 'Lỗi khi xóa vị trí kho';
        toast.error(message);
      }
    }
  };

  const columns = [
    { title: 'Mã Vị Trí', dataIndex: 'maViTri' },
    { title: 'Tên Vị Trí', dataIndex: 'tenViTri' },
    {
      title: 'Loại',
      dataIndex: 'loaiViTri',
      render: (value) => {
        const labels = {
          KE: 'Kệ',
          NGAN: 'Ngăn',
          O: 'Ô',
          TU_LANH: 'Tủ lạnh',
          TU_DONG: 'Tủ đông',
          KHU_VUC: 'Khu vực'
        };
        return labels[value] || value;
      }
    },
    {
      title: 'Sức Chứa',
      dataIndex: 'sucChuaToiDa',
      align: 'center',
      render: (value) => value || '-'
    },
    {
      title: 'Trạng Thái',
      dataIndex: 'trangThai',
      render: (value) => {
        const colors = {
          TRONG: '#3498db',
          CO_HANG: '#f39c12',
          DAY: '#e74c3c',
          BAO_TRI: '#95a5a6'
        };
        const labels = {
          TRONG: 'Trống',
          CO_HANG: 'Có hàng',
          DAY: 'Đầy',
          BAO_TRI: 'Bảo trì'
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
            onClick={() => {
              setEditingViTri(record);
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
          >
            ✏️
          </button>
          <button
            onClick={() => handleDelete(record.id)}
            style={{
              padding: '0.25rem 0.5rem',
              backgroundColor: '#e74c3c',
              color: 'white',
              border: 'none',
              borderRadius: '4px',
              cursor: 'pointer'
            }}
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
        <h2 style={{ marginBottom: '1.5rem', color: '#2c3e50' }}>Quản lý Vị Trí Kho</h2>

        <div style={{
          backgroundColor: 'white',
          padding: '1rem',
          borderRadius: '8px',
          marginBottom: '1.5rem',
          boxShadow: '0 2px 4px rgba(0,0,0,0.1)',
          display: 'flex',
          gap: '1rem',
          alignItems: 'center'
        }}>
          <select
            value={selectedKho}
            onChange={(e) => {
              setSelectedKho(e.target.value);
              setCurrentPage(0);
            }}
            style={{
              flex: 1,
              padding: '0.75rem',
              border: '1px solid #ddd',
              borderRadius: '6px',
              fontSize: '1rem'
            }}
          >
            <option value="">-- Chọn kho --</option>
            {khoList.map(kho => (
              <option key={kho.id} value={kho.id}>{kho.tenKho}</option>
            ))}
          </select>

          <button
            onClick={() => {
              setEditingViTri(null);
              setShowModal(true);
            }}
            disabled={!selectedKho}
            style={{
              backgroundColor: selectedKho ? '#27ae60' : '#95a5a6',
              color: 'white',
              border: 'none',
              padding: '0.75rem 1.5rem',
              borderRadius: '6px',
              cursor: selectedKho ? 'pointer' : 'not-allowed',
              fontWeight: '500'
            }}
          >
            ➕ Thêm Vị Trí
          </button>
        </div>

        {!selectedKho ? (
          <div style={{
            backgroundColor: 'white',
            padding: '3rem',
            borderRadius: '8px',
            textAlign: 'center',
            boxShadow: '0 2px 4px rgba(0,0,0,0.1)'
          }}>
            <div style={{ fontSize: '4rem', marginBottom: '1rem', opacity: '0.3' }}>📦</div>
            <p style={{ color: '#7f8c8d', fontSize: '1.1rem' }}>
              Vui lòng chọn kho để xem danh sách vị trí
            </p>
          </div>
        ) : (
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
                  data={viTriList}
                  loading={loading}
                  emptyMessage="Chưa có vị trí kho nào"
                />

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
        )}

        <Modal
          isOpen={showModal}
          onClose={() => {
            setShowModal(false);
            setEditingViTri(null);
          }}
          title={editingViTri ? 'Cập nhật Vị Trí Kho' : 'Thêm Vị Trí Kho Mới'}
          size="large"
        >
          <ViTriKhoForm
            initialData={editingViTri}
            onSubmit={editingViTri ? handleUpdate : handleCreate}
            onCancel={() => {
              setShowModal(false);
              setEditingViTri(null);
            }}
          />
        </Modal>
      </div>
    </Layout>
  );
};

export default ViTriKho;
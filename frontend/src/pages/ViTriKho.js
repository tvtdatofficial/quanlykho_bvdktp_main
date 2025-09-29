import React, { useState, useEffect } from 'react';
import Layout from '../components/layout/Layout';
import { Modal, Table, Loading } from '../components/shared';
import ViTriKhoForm from '../components/forms/ViTriKhoForm';
import { toast } from 'react-toastify';
import api from '../services/api';

const ViTriKho = () => {
  const [viTriKhoList, setViTriKhoList] = useState([]);
  const [loading, setLoading] = useState(false);
  const [showModal, setShowModal] = useState(false);
  const [editingViTri, setEditingViTri] = useState(null);
  const [selectedKho, setSelectedKho] = useState('');
  const [khoList, setKhoList] = useState([]);
  const [viewMode, setViewMode] = useState('table'); // 'table' or 'tree'
  const [treeData, setTreeData] = useState([]);

  useEffect(() => {
    fetchKhoList();
  }, []);

  useEffect(() => {
    if (selectedKho) {
      if (viewMode === 'tree') {
        fetchTreeData();
      } else {
        fetchViTriKhoList();
      }
    }
  }, [selectedKho, viewMode]);

  const fetchKhoList = async () => {
    try {
      const response = await api.get('/api/kho/active');
      // SỬA: Kiểm tra cấu trúc response
      const khos = response.data.data || response.data || [];
      setKhoList(khos);
      if (khos.length > 0) {
        setSelectedKho(khos[0].id);
      }
    } catch (error) {
      console.error('Error fetching kho list:', error);
      toast.error('Lỗi khi tải danh sách kho');
    }
  };

  const fetchViTriKhoList = async () => {
    setLoading(true);
    try {
      const response = await api.get('/api/vi-tri-kho', {
        params: {
          khoId: selectedKho,
          page: 0,
          size: 100
        }
      });
      // SỬA: Truy cập trực tiếp response.data.content
      setViTriKhoList(response.data.content || []);
    } catch (error) {
      console.error('Error fetching vi tri kho:', error);
      toast.error('Lỗi khi tải danh sách vị trí kho');
    } finally {
      setLoading(false);
    }
  };

  const fetchTreeData = async () => {
    setLoading(true);
    try {
      const response = await api.get(`/api/vi-tri-kho/tree/${selectedKho}`);
      // SỬA: API trả về List trực tiếp
      setTreeData(response.data || []);
    } catch (error) {
      console.error('Error fetching tree data:', error);
      toast.error('Lỗi khi tải cây vị trí kho');
    } finally {
      setLoading(false);
    }
  };

  const handleCreateViTri = async (formData) => {
    try {
      await api.post('/api/vi-tri-kho', formData);
      toast.success('Tạo vị trí kho thành công');
      setShowModal(false);
      viewMode === 'tree' ? fetchTreeData() : fetchViTriKhoList();
    } catch (error) {
      const message = error.response?.data?.message || 'Lỗi khi tạo vị trí kho';
      toast.error(message);
      throw error;
    }
  };

  const handleUpdateViTri = async (formData) => {
    try {
      await api.put(`/api/vi-tri-kho/${editingViTri.id}`, formData);
      toast.success('Cập nhật vị trí kho thành công');
      setShowModal(false);
      setEditingViTri(null);
      viewMode === 'tree' ? fetchTreeData() : fetchViTriKhoList();
    } catch (error) {
      const message = error.response?.data?.message || 'Lỗi khi cập nhật vị trí kho';
      toast.error(message);
      throw error;
    }
  };

  const handleDeleteViTri = async (id) => {
    if (window.confirm('Bạn có chắc muốn xóa vị trí kho này?')) {
      try {
        await api.delete(`/api/vi-tri-kho/${id}`);
        toast.success('Xóa vị trí kho thành công');
        viewMode === 'tree' ? fetchTreeData() : fetchViTriKhoList();
      } catch (error) {
        const message = error.response?.data?.message || 'Lỗi khi xóa vị trí kho';
        toast.error(message);
      }
    }
  };

  const renderTreeNode = (node, level = 0) => {
    const statusColor = {
      TRONG: '#27ae60',
      CO_HANG: '#f39c12',
      DAY: '#e74c3c',
      BAO_TRI: '#95a5a6'
    };

    const statusLabel = {
      TRONG: 'Trống',
      CO_HANG: 'Có hàng',
      DAY: 'Đầy',
      BAO_TRI: 'Bảo trì'
    };

    return (
      <div key={node.id}>
        <div style={{
          marginLeft: `${level * 2}rem`,
          padding: '0.75rem',
          marginBottom: '0.5rem',
          backgroundColor: 'white',
          border: '1px solid #dee2e6',
          borderRadius: '6px',
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center'
        }}>
          <div style={{ flex: 1 }}>
            <span style={{ fontWeight: 'bold', color: '#2c3e50' }}>
              {node.maViTri}
            </span>
            {node.tenViTri && (
              <span style={{ marginLeft: '0.5rem', color: '#7f8c8d' }}>
                - {node.tenViTri}
              </span>
            )}
            <div style={{ fontSize: '0.85rem', color: '#7f8c8d', marginTop: '0.25rem' }}>
              <span style={{
                display: 'inline-block',
                padding: '0.125rem 0.5rem',
                borderRadius: '4px',
                backgroundColor: statusColor[node.trangThai] + '20',
                color: statusColor[node.trangThai],
                marginRight: '0.5rem'
              }}>
                {statusLabel[node.trangThai] || node.trangThai}
              </span>
              {(node.soLuongHienTai > 0 || node.sucChuaToiDa > 0) && (
                <span>
                  Đang chứa: {node.soLuongHienTai || 0}
                  {node.sucChuaToiDa && ` / ${node.sucChuaToiDa}`}
                </span>
              )}
            </div>
          </div>
          <div style={{ display: 'flex', gap: '0.5rem' }}>
            <button
              onClick={() => {
                setEditingViTri(node);
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
              onClick={() => handleDeleteViTri(node.id)}
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
        </div>
        {node.viTriCon && node.viTriCon.length > 0 && (
          node.viTriCon.map(child => renderTreeNode(child, level + 1))
        )}
      </div>
    );
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
      render: (value, record) => (
        <div>
          <span>
            {record.soLuongHienTai || 0} / {value || '∞'}
          </span>
          {record.phanTramSuDung > 0 && (
            <div style={{
              fontSize: '0.75rem',
              color: record.dangDay ? '#e74c3c' : '#7f8c8d'
            }}>
              ({record.phanTramSuDung}%)
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
          TRONG: '#27ae60',
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
            {labels[value] || value}
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
            title="Chỉnh sửa"
          >
            ✏️
          </button>
          <button
            onClick={() => handleDeleteViTri(record.id)}
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
        <h2 style={{ marginBottom: '1.5rem', color: '#2c3e50' }}>Quản lý Vị Trí Kho</h2>

        {/* Action Bar */}
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
          <select
            value={selectedKho}
            onChange={(e) => setSelectedKho(e.target.value)}
            style={{
              padding: '0.75rem',
              border: '1px solid #ddd',
              borderRadius: '6px',
              fontSize: '1rem',
              minWidth: '200px'
            }}
          >
            {khoList.length === 0 ? (
              <option value="">Đang tải...</option>
            ) : (
              khoList.map(kho => (
                <option key={kho.id} value={kho.id}>{kho.tenKho}</option>
              ))
            )}
          </select>

          <div style={{
            display: 'flex',
            gap: '0.5rem',
            backgroundColor: '#f8f9fa',
            padding: '0.25rem',
            borderRadius: '6px'
          }}>
            <button
              onClick={() => setViewMode('table')}
              style={{
                padding: '0.5rem 1rem',
                backgroundColor: viewMode === 'table' ? '#3498db' : 'transparent',
                color: viewMode === 'table' ? 'white' : '#2c3e50',
                border: 'none',
                borderRadius: '4px',
                cursor: 'pointer'
              }}
            >
              📋 Bảng
            </button>
            <button
              onClick={() => setViewMode('tree')}
              style={{
                padding: '0.5rem 1rem',
                backgroundColor: viewMode === 'tree' ? '#3498db' : 'transparent',
                color: viewMode === 'tree' ? 'white' : '#2c3e50',
                border: 'none',
                borderRadius: '4px',
                cursor: 'pointer'
              }}
            >
              🌳 Cây
            </button>
          </div>

          <button
            onClick={() => {
              setEditingViTri(null);
              setShowModal(true);
            }}
            style={{
              backgroundColor: '#27ae60',
              color: 'white',
              border: 'none',
              padding: '0.75rem 1.5rem',
              borderRadius: '6px',
              cursor: 'pointer',
              fontWeight: '500',
              marginLeft: 'auto'
            }}
            disabled={!selectedKho}
          >
            ➕ Thêm Vị Trí
          </button>
        </div>

        {/* Content */}
        <div style={{
          backgroundColor: 'white',
          borderRadius: '8px',
          boxShadow: '0 2px 4px rgba(0,0,0,0.1)',
          overflow: 'hidden'
        }}>
          {loading ? (
            <Loading />
          ) : !selectedKho ? (
            <div style={{ textAlign: 'center', padding: '2rem', color: '#7f8c8d' }}>
              Vui lòng chọn kho
            </div>
          ) : viewMode === 'tree' ? (
            <div style={{ padding: '1rem' }}>
              {treeData.length === 0 ? (
                <div style={{ textAlign: 'center', padding: '2rem', color: '#7f8c8d' }}>
                  Chưa có vị trí kho nào
                </div>
              ) : (
                treeData.map(node => renderTreeNode(node))
              )}
            </div>
          ) : (
            <Table
              columns={columns}
              data={viTriKhoList}
              loading={loading}
              emptyMessage="Không tìm thấy vị trí kho nào"
            />
          )}
        </div>

        {/* Modal */}
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
            initialData={editingViTri ? { ...editingViTri, khoId: selectedKho } : { khoId: selectedKho }}
            onSubmit={editingViTri ? handleUpdateViTri : handleCreateViTri}
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
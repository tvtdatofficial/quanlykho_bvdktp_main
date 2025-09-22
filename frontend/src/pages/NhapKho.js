import React, { useState, useEffect } from 'react';
import Layout from '../components/layout/Layout';
import { Modal, Table, Loading } from '../components/shared';
import { toast } from 'react-toastify';
import api from '../services/api';

const NhapKho = () => {
  const [phieuNhapList, setPhieuNhapList] = useState([]);
  const [loading, setLoading] = useState(false);
  const [showModal, setShowModal] = useState(false);
  const [showCreateForm, setShowCreateForm] = useState(false);
  const [currentPage, setCurrentPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [searchTerm, setSearchTerm] = useState('');
  const [selectedStatus, setSelectedStatus] = useState('');
  
  // Form data for creating new import
  const [formData, setFormData] = useState({
    khoId: '',
    nhaCungCapId: '',
    loaiNhap: 'NHAP_MOI',
    soHoaDon: '',
    ngayHoaDon: '',
    ngayNhap: new Date().toISOString().split('T')[0],
    nguoiGiao: '',
    sdtNguoiGiao: '',
    ghiChu: '',
    chiTiet: []
  });

  const [khoList, setKhoList] = useState([]);
  const [nhaCungCapList, setNhaCungCapList] = useState([]);
  const [hangHoaList, setHangHoaList] = useState([]);

  useEffect(() => {
    fetchPhieuNhapList();
    fetchSelectData();
  }, [currentPage, searchTerm, selectedStatus]);

  const fetchPhieuNhapList = async () => {
    setLoading(true);
    try {
      // Mock data since API might not be ready
      const mockData = [
        {
          id: 1,
          maPhieuNhap: 'PN001',
          tenKho: 'Kho Dược Chính',
          tenNhaCungCap: 'Công ty ABC',
          ngayNhap: '2024-01-15',
          tongGiaTri: 5000000,
          trangThai: 'CHO_DUYET',
          nguoiNhan: 'Nguyễn Văn A'
        },
        {
          id: 2,
          maPhieuNhap: 'PN002',
          tenKho: 'Kho Vật Tư',
          tenNhaCungCap: 'Công ty XYZ',
          ngayNhap: '2024-01-14',
          tongGiaTri: 3000000,
          trangThai: 'DA_DUYET',
          nguoiNhan: 'Trần Thị B'
        }
      ];
      
      setPhieuNhapList(mockData);
      setTotalPages(1);
    } catch (error) {
      toast.error('Lỗi khi tải danh sách phiếu nhập');
    } finally {
      setLoading(false);
    }
  };

  const fetchSelectData = async () => {
    try {
      const [khoRes, nhaCungCapRes, hangHoaRes] = await Promise.all([
        api.get('/api/kho/active').catch(() => ({ data: { data: [] } })),
        api.get('/api/nha-cung-cap/active').catch(() => ({ data: { data: [] } })),
        api.get('/api/hang-hoa?size=100').catch(() => ({ data: { data: { content: [] } } }))
      ]);

      setKhoList(khoRes.data.data || []);
      setNhaCungCapList(nhaCungCapRes.data.data || []);
      setHangHoaList(hangHoaRes.data.data?.content || []);
    } catch (error) {
      console.error('Error fetching select data:', error);
    }
  };

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
  };

  const addChiTietRow = () => {
    setFormData(prev => ({
      ...prev,
      chiTiet: [...prev.chiTiet, {
        hangHoaId: '',
        soLuong: '',
        donGia: '',
        thanhTien: 0,
        hanSuDung: '',
        soLo: '',
        ghiChu: ''
      }]
    }));
  };

  const removeChiTietRow = (index) => {
    setFormData(prev => ({
      ...prev,
      chiTiet: prev.chiTiet.filter((_, i) => i !== index)
    }));
  };

  const updateChiTiet = (index, field, value) => {
    setFormData(prev => {
      const newChiTiet = [...prev.chiTiet];
      newChiTiet[index][field] = value;
      
      // Auto calculate thanh tien
      if (field === 'soLuong' || field === 'donGia') {
        const soLuong = parseFloat(newChiTiet[index].soLuong || 0);
        const donGia = parseFloat(newChiTiet[index].donGia || 0);
        newChiTiet[index].thanhTien = soLuong * donGia;
      }
      
      return {
        ...prev,
        chiTiet: newChiTiet
      };
    });
  };

  const handleSubmitPhieuNhap = async (e) => {
    e.preventDefault();
    
    if (formData.chiTiet.length === 0) {
      toast.error('Vui lòng thêm ít nhất một hàng hóa');
      return;
    }

    try {
      // Mock API call
      console.log('Creating phieu nhap:', formData);
      toast.success('Tạo phiếu nhập thành công');
      setShowCreateForm(false);
      resetForm();
      fetchPhieuNhapList();
    } catch (error) {
      toast.error('Lỗi khi tạo phiếu nhập');
    }
  };

  const resetForm = () => {
    setFormData({
      khoId: '',
      nhaCungCapId: '',
      loaiNhap: 'NHAP_MOI',
      soHoaDon: '',
      ngayHoaDon: '',
      ngayNhap: new Date().toISOString().split('T')[0],
      nguoiGiao: '',
      sdtNguoiGiao: '',
      ghiChu: '',
      chiTiet: []
    });
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
      }).format(value)
    },
    {
      title: 'Trạng Thái',
      dataIndex: 'trangThai',
      render: (value) => (
        <span style={{
          padding: '0.25rem 0.5rem',
          borderRadius: '4px',
          fontSize: '0.8rem',
          backgroundColor: value === 'DA_DUYET' ? '#d5f4e6' : '#ffeaa7',
          color: value === 'DA_DUYET' ? '#00b894' : '#fdcb6e'
        }}>
          {value === 'DA_DUYET' ? 'Đã duyệt' : 'Chờ duyệt'}
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
          {record.trangThai === 'CHO_DUYET' && (
            <button
              style={{
                padding: '0.25rem 0.5rem',
                backgroundColor: '#27ae60',
                color: 'white',
                border: 'none',
                borderRadius: '4px',
                cursor: 'pointer'
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

  const inputStyle = {
    width: '100%',
    padding: '0.75rem',
    border: '1px solid #ddd',
    borderRadius: '6px',
    fontSize: '1rem'
  };

  const labelStyle = {
    display: 'block',
    marginBottom: '0.5rem',
    fontWeight: '500',
    color: '#2c3e50'
  };

  return (
    <Layout>
      <div>
        <h2 style={{ marginBottom: '1.5rem', color: '#2c3e50' }}>Quản lý Nhập Kho</h2>
        
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
          <input
            type="text"
            placeholder="Tìm kiếm phiếu nhập..."
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
            value={selectedStatus}
            onChange={(e) => setSelectedStatus(e.target.value)}
            style={{
              padding: '0.75rem',
              border: '1px solid #ddd',
              borderRadius: '6px',
              fontSize: '1rem',
              minWidth: '150px'
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
              borderRadius: '6px',
              cursor: 'pointer',
              fontWeight: '500'
            }}
          >
            ➕ Tạo Phiếu Nhập
          </button>
        </div>

        {/* Statistics */}
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
              {phieuNhapList.length}
            </div>
            <div style={{ color: '#7f8c8d' }}>Tổng phiếu nhập</div>
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
              {phieuNhapList.filter(p => p.trangThai === 'CHO_DUYET').length}
            </div>
            <div style={{ color: '#7f8c8d' }}>Chờ duyệt</div>
          </div>
          
          <div style={{
            backgroundColor: 'white',
            padding: '1rem',
            borderRadius: '8px',
            boxShadow: '0 2px 4px rgba(0,0,0,0.1)',
            textAlign: 'center',
            border: '3px solid #27ae60'
          }}>
            <div style={{ fontSize: '1.5rem', fontWeight: 'bold', color: '#27ae60' }}>
              {new Intl.NumberFormat('vi-VN', { 
                style: 'currency', 
                currency: 'VND',
                maximumFractionDigits: 0
              }).format(phieuNhapList.reduce((sum, p) => sum + p.tongGiaTri, 0))}
            </div>
            <div style={{ color: '#7f8c8d' }}>Tổng giá trị</div>
          </div>
        </div>

        {/* Table */}
        <div style={{
          backgroundColor: 'white',
          borderRadius: '8px',
          boxShadow: '0 2px 4px rgba(0,0,0,0.1)',
          overflow: 'hidden'
        }}>
          <Table
            columns={columns}
            data={phieuNhapList}
            loading={loading}
            emptyMessage="Không tìm thấy phiếu nhập nào"
          />
        </div>

        {/* Create Form Modal */}
        <Modal
          isOpen={showCreateForm}
          onClose={() => {
            setShowCreateForm(false);
            resetForm();
          }}
          title="Tạo Phiếu Nhập Mới"
          size="xlarge"
        >
          <form onSubmit={handleSubmitPhieuNhap}>
            {/* Basic Information */}
            <div style={{ marginBottom: '2rem' }}>
              <h4 style={{ marginBottom: '1rem', color: '#2c3e50', borderBottom: '2px solid #3498db', paddingBottom: '0.5rem' }}>
                Thông tin cơ bản
              </h4>
              
              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem', marginBottom: '1rem' }}>
                <div>
                  <label style={labelStyle}>Kho nhập *</label>
                  <select
                    name="khoId"
                    value={formData.khoId}
                    onChange={handleInputChange}
                    style={inputStyle}
                    required
                  >
                    <option value="">-- Chọn kho --</option>
                    {khoList.map(kho => (
                      <option key={kho.id} value={kho.id}>{kho.tenKho}</option>
                    ))}
                  </select>
                </div>
                
                <div>
                  <label style={labelStyle}>Nhà cung cấp</label>
                  <select
                    name="nhaCungCapId"
                    value={formData.nhaCungCapId}
                    onChange={handleInputChange}
                    style={inputStyle}
                  >
                    <option value="">-- Chọn nhà cung cấp --</option>
                    {nhaCungCapList.map(ncc => (
                      <option key={ncc.id} value={ncc.id}>{ncc.tenNcc}</option>
                    ))}
                  </select>
                </div>
              </div>
              
              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr 1fr', gap: '1rem', marginBottom: '1rem' }}>
                <div>
                  <label style={labelStyle}>Loại nhập</label>
                  <select
                    name="loaiNhap"
                    value={formData.loaiNhap}
                    onChange={handleInputChange}
                    style={inputStyle}
                  >
                    <option value="NHAP_MOI">Nhập mới</option>
                    <option value="NHAP_TRA">Nhập trả</option>
                    <option value="NHAP_CHUYEN_KHO">Nhập chuyển kho</option>
                    <option value="NHAP_DIEU_CHINH">Nhập điều chỉnh</option>
                  </select>
                </div>
                
                <div>
                  <label style={labelStyle}>Số hóa đơn</label>
                  <input
                    type="text"
                    name="soHoaDon"
                    value={formData.soHoaDon}
                    onChange={handleInputChange}
                    style={inputStyle}
                  />
                </div>
                
                <div>
                  <label style={labelStyle}>Ngày nhập *</label>
                  <input
                    type="date"
                    name="ngayNhap"
                    value={formData.ngayNhap}
                    onChange={handleInputChange}
                    style={inputStyle}
                    required
                  />
                </div>
              </div>
              
              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem', marginBottom: '1rem' }}>
                <div>
                  <label style={labelStyle}>Người giao</label>
                  <input
                    type="text"
                    name="nguoiGiao"
                    value={formData.nguoiGiao}
                    onChange={handleInputChange}
                    style={inputStyle}
                  />
                </div>
                
                <div>
                  <label style={labelStyle}>SĐT người giao</label>
                  <input
                    type="tel"
                    name="sdtNguoiGiao"
                    value={formData.sdtNguoiGiao}
                    onChange={handleInputChange}
                    style={inputStyle}
                  />
                </div>
              </div>
              
              <div>
                <label style={labelStyle}>Ghi chú</label>
                <textarea
                  name="ghiChu"
                  value={formData.ghiChu}
                  onChange={handleInputChange}
                  style={{ ...inputStyle, minHeight: '80px' }}
                  rows={3}
                />
              </div>
            </div>

            {/* Chi tiết hàng hóa */}
            <div style={{ marginBottom: '2rem' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1rem' }}>
                <h4 style={{ margin: 0, color: '#2c3e50', borderBottom: '2px solid #27ae60', paddingBottom: '0.5rem' }}>
                  Chi tiết hàng hóa
                </h4>
                <button
                  type="button"
                  onClick={addChiTietRow}
                  style={{
                    backgroundColor: '#27ae60',
                    color: 'white',
                    border: 'none',
                    padding: '0.5rem 1rem',
                    borderRadius: '6px',
                    cursor: 'pointer'
                  }}
                >
                  + Thêm hàng hóa
                </button>
              </div>

              {formData.chiTiet.length === 0 ? (
                <div style={{ 
                  textAlign: 'center', 
                  padding: '2rem', 
                  backgroundColor: '#f8f9fa', 
                  borderRadius: '6px',
                  color: '#7f8c8d'
                }}>
                  Chưa có hàng hóa nào. Nhấn "Thêm hàng hóa" để bắt đầu.
                </div>
              ) : (
                <div style={{ overflowX: 'auto' }}>
                  <table style={{ width: '100%', borderCollapse: 'collapse', minWidth: '800px' }}>
                    <thead>
                      <tr style={{ backgroundColor: '#f8f9fa' }}>
                        <th style={{ padding: '0.75rem', border: '1px solid #dee2e6', textAlign: 'left' }}>Hàng hóa</th>
                        <th style={{ padding: '0.75rem', border: '1px solid #dee2e6', textAlign: 'center' }}>Số lượng</th>
                        <th style={{ padding: '0.75rem', border: '1px solid #dee2e6', textAlign: 'right' }}>Đơn giá</th>
                        <th style={{ padding: '0.75rem', border: '1px solid #dee2e6', textAlign: 'right' }}>Thành tiền</th>
                        <th style={{ padding: '0.75rem', border: '1px solid #dee2e6', textAlign: 'center' }}>Hạn SD</th>
                        <th style={{ padding: '0.75rem', border: '1px solid #dee2e6', textAlign: 'center' }}>Số lô</th>
                        <th style={{ padding: '0.75rem', border: '1px solid #dee2e6', textAlign: 'center' }}>Thao tác</th>
                      </tr>
                    </thead>
                    <tbody>
                      {formData.chiTiet.map((item, index) => (
                        <tr key={index}>
                          <td style={{ padding: '0.5rem', border: '1px solid #dee2e6' }}>
                            <select
                              value={item.hangHoaId}
                              onChange={(e) => updateChiTiet(index, 'hangHoaId', e.target.value)}
                              style={{ width: '100%', padding: '0.5rem', border: '1px solid #ddd', borderRadius: '4px' }}
                            >
                              <option value="">-- Chọn hàng hóa --</option>
                              {hangHoaList.map(hh => (
                                <option key={hh.id} value={hh.id}>{hh.tenHangHoa}</option>
                              ))}
                            </select>
                          </td>
                          <td style={{ padding: '0.5rem', border: '1px solid #dee2e6' }}>
                            <input
                              type="number"
                              value={item.soLuong}
                              onChange={(e) => updateChiTiet(index, 'soLuong', e.target.value)}
                              style={{ width: '80px', padding: '0.5rem', border: '1px solid #ddd', borderRadius: '4px', textAlign: 'center' }}
                              min="1"
                            />
                          </td>
                          <td style={{ padding: '0.5rem', border: '1px solid #dee2e6' }}>
                            <input
                              type="number"
                              value={item.donGia}
                              onChange={(e) => updateChiTiet(index, 'donGia', e.target.value)}
                              style={{ width: '100px', padding: '0.5rem', border: '1px solid #ddd', borderRadius: '4px', textAlign: 'right' }}
                              min="0"
                              step="1000"
                            />
                          </td>
                          <td style={{ padding: '0.5rem', border: '1px solid #dee2e6', textAlign: 'right', fontWeight: 'bold' }}>
                            {new Intl.NumberFormat('vi-VN').format(item.thanhTien || 0)}
                          </td>
                          <td style={{ padding: '0.5rem', border: '1px solid #dee2e6' }}>
                            <input
                              type="date"
                              value={item.hanSuDung}
                              onChange={(e) => updateChiTiet(index, 'hanSuDung', e.target.value)}
                              style={{ width: '120px', padding: '0.5rem', border: '1px solid #ddd', borderRadius: '4px' }}
                            />
                          </td>
                          <td style={{ padding: '0.5rem', border: '1px solid #dee2e6' }}>
                            <input
                              type="text"
                              value={item.soLo}
                              onChange={(e) => updateChiTiet(index, 'soLo', e.target.value)}
                              style={{ width: '80px', padding: '0.5rem', border: '1px solid #ddd', borderRadius: '4px' }}
                              placeholder="Số lô"
                            />
                          </td>
                          <td style={{ padding: '0.5rem', border: '1px solid #dee2e6', textAlign: 'center' }}>
                            <button
                              type="button"
                              onClick={() => removeChiTietRow(index)}
                              style={{
                                backgroundColor: '#e74c3c',
                                color: 'white',
                                border: 'none',
                                padding: '0.25rem 0.5rem',
                                borderRadius: '4px',
                                cursor: 'pointer'
                              }}
                            >
                              Xóa
                            </button>
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              )}

              {/* Tổng kết */}
              {formData.chiTiet.length > 0 && (
                <div style={{ 
                  marginTop: '1rem', 
                  padding: '1rem', 
                  backgroundColor: '#f8f9fa', 
                  borderRadius: '6px',
                  textAlign: 'right'
                }}>
                  <div style={{ fontSize: '1.1rem', fontWeight: 'bold', color: '#2c3e50' }}>
                    Tổng giá trị: {new Intl.NumberFormat('vi-VN', { 
                      style: 'currency', 
                      currency: 'VND' 
                    }).format(formData.chiTiet.reduce((sum, item) => sum + (item.thanhTien || 0), 0))}
                  </div>
                  <div style={{ color: '#7f8c8d', fontSize: '0.9rem', marginTop: '0.25rem' }}>
                    Tổng số mặt hàng: {formData.chiTiet.length}
                  </div>
                </div>
              )}
            </div>

            {/* Action buttons */}
            <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '1rem', borderTop: '1px solid #dee2e6', paddingTop: '1rem' }}>
              <button
                type="button"
                onClick={() => {
                  setShowCreateForm(false);
                  resetForm();
                }}
                style={{
                  padding: '0.75rem 1.5rem',
                  backgroundColor: '#95a5a6',
                  color: 'white',
                  border: 'none',
                  borderRadius: '6px',
                  cursor: 'pointer'
                }}
              >
                Hủy
              </button>
              <button
                type="submit"
                style={{
                  padding: '0.75rem 1.5rem',
                  backgroundColor: '#27ae60',
                  color: 'white',
                  border: 'none',
                  borderRadius: '6px',
                  cursor: 'pointer',
                  fontWeight: '500'
                }}
              >
                Tạo Phiếu Nhập
              </button>
            </div>
          </form>
        </Modal>
      </div>
    </Layout>
  );
};

export default NhapKho;
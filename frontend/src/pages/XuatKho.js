import React, { useState, useEffect } from 'react';
import Layout from '../components/layout/Layout';
import { Modal, Table, Loading } from '../components/shared';
import { toast } from 'react-toastify';
import api from '../services/api';

const XuatKho = () => {
  const [phieuXuatList, setPhieuXuatList] = useState([]);
  const [loading, setLoading] = useState(false);
  const [showCreateForm, setShowCreateForm] = useState(false);
  const [currentPage, setCurrentPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [searchTerm, setSearchTerm] = useState('');
  const [selectedStatus, setSelectedStatus] = useState('');
  
  const [formData, setFormData] = useState({
    khoId: '',
    khoaPhongYeuCauId: '',
    loaiXuat: 'XUAT_SU_DUNG',
    soPhieuYeuCau: '',
    ngayXuat: new Date().toISOString().split('T')[0],
    nguoiYeuCau: '',
    sdtNguoiYeuCau: '',
    nguoiNhan: '',
    sdtNguoiNhan: '',
    diaChiGiao: '',
    lyDoXuat: '',
    ghiChu: '',
    chiTiet: []
  });

  const [khoList, setKhoList] = useState([]);
  const [khoaPhongList, setKhoaPhongList] = useState([]);
  const [hangHoaList, setHangHoaList] = useState([]);

  useEffect(() => {
    fetchPhieuXuatList();
    fetchSelectData();
  }, [currentPage, searchTerm, selectedStatus]);

  const fetchPhieuXuatList = async () => {
    setLoading(true);
    try {
      // Mock data
      const mockData = [
        {
          id: 1,
          maPhieuXuat: 'PX001',
          tenKho: 'Kho Dược Chính',
          tenKhoaPhong: 'Khoa Nội',
          ngayXuat: '2024-01-15',
          tongGiaTri: 2000000,
          trangThai: 'CHO_DUYET',
          nguoiYeuCau: 'BS. Nguyễn Văn A'
        },
        {
          id: 2,
          maPhieuXuat: 'PX002',
          tenKho: 'Kho Vật Tư',
          tenKhoaPhong: 'Khoa Ngoại',
          ngayXuat: '2024-01-14',
          tongGiaTri: 1500000,
          trangThai: 'DA_DUYET',
          nguoiYeuCau: 'BS. Trần Thị B'
        }
      ];
      
      setPhieuXuatList(mockData);
      setTotalPages(1);
    } catch (error) {
      toast.error('Lỗi khi tải danh sách phiếu xuất');
    } finally {
      setLoading(false);
    }
  };

  const fetchSelectData = async () => {
    try {
      const [khoRes, khoaPhongRes, hangHoaRes] = await Promise.all([
        api.get('/api/kho/active').catch(() => ({ data: { data: [] } })),
        api.get('/api/khoa-phong/active').catch(() => ({ data: { data: [] } })),
        api.get('/api/hang-hoa?size=100').catch(() => ({ data: { data: { content: [] } } }))
      ]);

      setKhoList(khoRes.data.data || []);
      setKhoaPhongList(khoaPhongRes.data.data || []);
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
        soLuongYeuCau: '',
        soLuongXuat: '',
        tonKhoHienTai: 0,
        donGia: '',
        thanhTien: 0,
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

  const updateChiTiet = async (index, field, value) => {
    setFormData(prev => {
      const newChiTiet = [...prev.chiTiet];
      newChiTiet[index][field] = value;
      
      // Check inventory when selecting product
      if (field === 'hangHoaId' && value) {
        const selectedProduct = hangHoaList.find(p => p.id === parseInt(value));
        if (selectedProduct) {
          newChiTiet[index].tonKhoHienTai = selectedProduct.soLuongCoTheXuat || 0;
          newChiTiet[index].donGia = selectedProduct.giaXuatTrungBinh || 0;
        }
      }
      
      // Auto calculate thanh tien
      if (field === 'soLuongXuat' || field === 'donGia') {
        const soLuong = parseFloat(newChiTiet[index].soLuongXuat || 0);
        const donGia = parseFloat(newChiTiet[index].donGia || 0);
        newChiTiet[index].thanhTien = soLuong * donGia;
      }
      
      // Set soLuongXuat = soLuongYeuCau if not specified
      if (field === 'soLuongYeuCau' && !newChiTiet[index].soLuongXuat) {
        newChiTiet[index].soLuongXuat = value;
        const donGia = parseFloat(newChiTiet[index].donGia || 0);
        newChiTiet[index].thanhTien = parseFloat(value || 0) * donGia;
      }
      
      return {
        ...prev,
        chiTiet: newChiTiet
      };
    });
  };

  const handleSubmitPhieuXuat = async (e) => {
    e.preventDefault();
    
    if (formData.chiTiet.length === 0) {
      toast.error('Vui lòng thêm ít nhất một hàng hóa');
      return;
    }

    // Check inventory
    const invalidItems = formData.chiTiet.filter(item => 
      parseFloat(item.soLuongXuat) > item.tonKhoHienTai
    );
    
    if (invalidItems.length > 0) {
      toast.error('Có hàng hóa xuất vượt quá tồn kho');
      return;
    }

    try {
      // Mock API call
      console.log('Creating phieu xuat:', formData);
      toast.success('Tạo phiếu xuất thành công');
      setShowCreateForm(false);
      resetForm();
      fetchPhieuXuatList();
    } catch (error) {
      toast.error('Lỗi khi tạo phiếu xuất');
    }
  };

  const resetForm = () => {
    setFormData({
      khoId: '',
      khoaPhongYeuCauId: '',
      loaiXuat: 'XUAT_SU_DUNG',
      soPhieuYeuCau: '',
      ngayXuat: new Date().toISOString().split('T')[0],
      nguoiYeuCau: '',
      sdtNguoiYeuCau: '',
      nguoiNhan: '',
      sdtNguoiNhan: '',
      diaChiGiao: '',
      lyDoXuat: '',
      ghiChu: '',
      chiTiet: []
    });
  };

  const columns = [
    { title: 'Mã Phiếu', dataIndex: 'maPhieuXuat' },
    { title: 'Kho', dataIndex: 'tenKho' },
    { title: 'Khoa/Phòng', dataIndex: 'tenKhoaPhong' },
    { title: 'Ngày Xuất', dataIndex: 'ngayXuat' },
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
        <h2 style={{ marginBottom: '1.5rem', color: '#2c3e50' }}>Quản lý Xuất Kho</h2>
        
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
            placeholder="Tìm kiếm phiếu xuất..."
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
            <option value="DA_GIAO">Đã giao</option>
            <option value="HUY">Đã hủy</option>
          </select>
          
          <button
            onClick={() => setShowCreateForm(true)}
            style={{
              backgroundColor: '#e74c3c',
              color: 'white',
              border: 'none',
              padding: '0.75rem 1.5rem',
              borderRadius: '6px',
              cursor: 'pointer',
              fontWeight: '500'
            }}
          >
            📤 Tạo Phiếu Xuất
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
            border: '3px solid #e74c3c'
          }}>
            <div style={{ fontSize: '1.5rem', fontWeight: 'bold', color: '#e74c3c' }}>
              {phieuXuatList.length}
            </div>
            <div style={{ color: '#7f8c8d' }}>Tổng phiếu xuất</div>
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
              {phieuXuatList.filter(p => p.trangThai === 'CHO_DUYET').length}
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
              }).format(phieuXuatList.reduce((sum, p) => sum + p.tongGiaTri, 0))}
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
            data={phieuXuatList}
            loading={loading}
            emptyMessage="Không tìm thấy phiếu xuất nào"
          />
        </div>

        {/* Create Form Modal */}
        <Modal
          isOpen={showCreateForm}
          onClose={() => {
            setShowCreateForm(false);
            resetForm();
          }}
          title="Tạo Phiếu Xuất Mới"
          size="xlarge"
        >
          <form onSubmit={handleSubmitPhieuXuat}>
            {/* Basic Information */}
            <div style={{ marginBottom: '2rem' }}>
              <h4 style={{ marginBottom: '1rem', color: '#2c3e50', borderBottom: '2px solid #e74c3c', paddingBottom: '0.5rem' }}>
                Thông tin cơ bản
              </h4>
              
              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem', marginBottom: '1rem' }}>
                <div>
                  <label style={labelStyle}>Kho xuất *</label>
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
                  <label style={labelStyle}>Khoa/Phòng yêu cầu *</label>
                  <select
                    name="khoaPhongYeuCauId"
                    value={formData.khoaPhongYeuCauId}
                    onChange={handleInputChange}
                    style={inputStyle}
                    required
                  >
                    <option value="">-- Chọn khoa phòng --</option>
                    {khoaPhongList.map(kp => (
                      <option key={kp.id} value={kp.id}>{kp.tenKhoaPhong}</option>
                    ))}
                  </select>
                </div>
              </div>
              
              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr 1fr', gap: '1rem', marginBottom: '1rem' }}>
                <div>
                  <label style={labelStyle}>Loại xuất</label>
                  <select
                    name="loaiXuat"
                    value={formData.loaiXuat}
                    onChange={handleInputChange}
                    style={inputStyle}
                  >
                    <option value="XUAT_SU_DUNG">Xuất sử dụng</option>
                    <option value="XUAT_TRA">Xuất trả</option>
                    <option value="XUAT_CHUYEN_KHO">Xuất chuyển kho</option>
                    <option value="XUAT_HUY">Xuất hủy</option>
                    <option value="XUAT_SUA_CHUA">Xuất sửa chữa</option>
                  </select>
                </div>
                
                <div>
                  <label style={labelStyle}>Số phiếu yêu cầu</label>
                  <input
                    type="text"
                    name="soPhieuYeuCau"
                    value={formData.soPhieuYeuCau}
                    onChange={handleInputChange}
                    style={inputStyle}
                  />
                </div>
                
                <div>
                  <label style={labelStyle}>Ngày xuất *</label>
                  <input
                    type="date"
                    name="ngayXuat"
                    value={formData.ngayXuat}
                    onChange={handleInputChange}
                    style={inputStyle}
                    required
                  />
                </div>
              </div>
              
              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem', marginBottom: '1rem' }}>
                <div>
                  <label style={labelStyle}>Người yêu cầu</label>
                  <input
                    type="text"
                    name="nguoiYeuCau"
                    value={formData.nguoiYeuCau}
                    onChange={handleInputChange}
                    style={inputStyle}
                  />
                </div>
                
                <div>
                  <label style={labelStyle}>SĐT người yêu cầu</label>
                  <input
                    type="tel"
                    name="sdtNguoiYeuCau"
                    value={formData.sdtNguoiYeuCau}
                    onChange={handleInputChange}
                    style={inputStyle}
                  />
                </div>
              </div>
              
              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem', marginBottom: '1rem' }}>
                <div>
                  <label style={labelStyle}>Người nhận</label>
                  <input
                    type="text"
                    name="nguoiNhan"
                    value={formData.nguoiNhan}
                    onChange={handleInputChange}
                    style={inputStyle}
                  />
                </div>
                
                <div>
                  <label style={labelStyle}>SĐT người nhận</label>
                  <input
                    type="tel"
                    name="sdtNguoiNhan"
                    value={formData.sdtNguoiNhan}
                    onChange={handleInputChange}
                    style={inputStyle}
                  />
                </div>
              </div>
              
              <div style={{ marginBottom: '1rem' }}>
                <label style={labelStyle}>Địa chỉ giao</label>
                <input
                  type="text"
                  name="diaChiGiao"
                  value={formData.diaChiGiao}
                  onChange={handleInputChange}
                  style={inputStyle}
                />
              </div>
              
              <div style={{ marginBottom: '1rem' }}>
                <label style={labelStyle}>Lý do xuất *</label>
                <textarea
                  name="lyDoXuat"
                  value={formData.lyDoXuat}
                  onChange={handleInputChange}
                  style={{ ...inputStyle, minHeight: '60px' }}
                  rows={2}
                  required
                />
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
                  Chi tiết hàng hóa xuất
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
                  <table style={{ width: '100%', borderCollapse: 'collapse', minWidth: '900px' }}>
                    <thead>
                      <tr style={{ backgroundColor: '#f8f9fa' }}>
                        <th style={{ padding: '0.75rem', border: '1px solid #dee2e6', textAlign: 'left' }}>Hàng hóa</th>
                        <th style={{ padding: '0.75rem', border: '1px solid #dee2e6', textAlign: 'center' }}>Tồn kho</th>
                        <th style={{ padding: '0.75rem', border: '1px solid #dee2e6', textAlign: 'center' }}>SL yêu cầu</th>
                        <th style={{ padding: '0.75rem', border: '1px solid #dee2e6', textAlign: 'center' }}>SL xuất</th>
                        <th style={{ padding: '0.75rem', border: '1px solid #dee2e6', textAlign: 'right' }}>Đơn giá</th>
                        <th style={{ padding: '0.75rem', border: '1px solid #dee2e6', textAlign: 'right' }}>Thành tiền</th>
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
                          <td style={{ 
                            padding: '0.5rem', 
                            border: '1px solid #dee2e6', 
                            textAlign: 'center',
                            fontWeight: 'bold',
                            color: item.tonKhoHienTai < 10 ? '#e74c3c' : '#27ae60'
                          }}>
                            {item.tonKhoHienTai || 0}
                          </td>
                          <td style={{ padding: '0.5rem', border: '1px solid #dee2e6' }}>
                            <input
                              type="number"
                              value={item.soLuongYeuCau}
                              onChange={(e) => updateChiTiet(index, 'soLuongYeuCau', e.target.value)}
                              style={{ width: '80px', padding: '0.5rem', border: '1px solid #ddd', borderRadius: '4px', textAlign: 'center' }}
                              min="1"
                            />
                          </td>
                          <td style={{ padding: '0.5rem', border: '1px solid #dee2e6' }}>
                            <input
                              type="number"
                              value={item.soLuongXuat}
                              onChange={(e) => updateChiTiet(index, 'soLuongXuat', e.target.value)}
                              style={{ 
                                width: '80px', 
                                padding: '0.5rem', 
                                border: `1px solid ${parseFloat(item.soLuongXuat || 0) > item.tonKhoHienTai ? '#e74c3c' : '#ddd'}`,
                                borderRadius: '4px', 
                                textAlign: 'center' 
                              }}
                              min="1"
                              max={item.tonKhoHienTai}
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
                    Tổng số mặt hàng: {formData.chiTiet.length} | 
                    Tổng số lượng: {formData.chiTiet.reduce((sum, item) => sum + (parseFloat(item.soLuongXuat) || 0), 0)}
                  </div>
                  
                  {/* Warning for over-stock items */}
                  {formData.chiTiet.some(item => parseFloat(item.soLuongXuat) > item.tonKhoHienTai) && (
                    <div style={{ 
                      color: '#e74c3c', 
                      fontSize: '0.9rem', 
                      marginTop: '0.5rem',
                      padding: '0.5rem',
                      backgroundColor: '#ffebee',
                      borderRadius: '4px',
                      textAlign: 'left'
                    }}>
                      ⚠️ Có hàng hóa xuất vượt quá tồn kho. Vui lòng kiểm tra lại!
                    </div>
                  )}
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
                  backgroundColor: '#e74c3c',
                  color: 'white',
                  border: 'none',
                  borderRadius: '6px',
                  cursor: 'pointer',
                  fontWeight: '500'
                }}
              >
                Tạo Phiếu Xuất
              </button>
            </div>
          </form>
        </Modal>
      </div>
    </Layout>
  );
};

export default XuatKho;
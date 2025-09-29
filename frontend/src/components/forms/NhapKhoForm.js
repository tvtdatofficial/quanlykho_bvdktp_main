import React, { useState, useEffect } from 'react';
import { toast } from 'react-toastify';
import api from '../../services/api';

const NhapKhoForm = ({ onSubmit, onCancel }) => {
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
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    fetchSelectData();
  }, []);

  const fetchSelectData = async () => {
    try {
      const [khoRes, nhaCungCapRes, hangHoaRes] = await Promise.all([
        api.get('/api/kho/active').catch(() => ({ data: { data: [] } })),
        api.get('/api/nha-cung-cap/active').catch(() => ({ data: { data: [] } })),
        api.get('/api/hang-hoa?size=100').catch(() => ({ data: { data: { content: [] } } }))
      ]);

      setKhoList(khoRes.data.data || khoRes.data || []);
      setNhaCungCapList(nhaCungCapRes.data.data || nhaCungCapRes.data || []);
      setHangHoaList(hangHoaRes.data.data?.content || hangHoaRes.data?.content || []);
    } catch (error) {
      console.error('Error fetching select data:', error);
    }
  };

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({ ...prev, [name]: value }));
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
      
      if (field === 'soLuong' || field === 'donGia') {
        const soLuong = parseFloat(newChiTiet[index].soLuong || 0);
        const donGia = parseFloat(newChiTiet[index].donGia || 0);
        newChiTiet[index].thanhTien = soLuong * donGia;
      }
      
      return { ...prev, chiTiet: newChiTiet };
    });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    if (formData.chiTiet.length === 0) {
      toast.error('Vui lòng thêm ít nhất một hàng hóa');
      return;
    }

    setLoading(true);
    try {
      await onSubmit(formData);
    } catch (error) {
      console.error('Error in form submission:', error);
    } finally {
      setLoading(false);
    }
  };

  const inputStyle = {
    width: '100%',
    padding: '0.75rem',
    border: '1px solid #ddd',
    borderRadius: '0.5rem',
    fontSize: '1rem'
  };

  const labelStyle = {
    display: 'block',
    marginBottom: '0.5rem',
    fontWeight: '500',
    color: '#2c3e50',
    fontSize: '0.875rem'
  };

  return (
    <form onSubmit={handleSubmit}>
      {/* Basic Information */}
      <div style={{ marginBottom: '2rem' }}>
        <h3 style={{ 
          fontSize: '1.125rem', 
          fontWeight: '600', 
          color: '#2c3e50', 
          marginBottom: '1rem',
          paddingBottom: '0.5rem',
          borderBottom: '2px solid #3498db'
        }}>
          Thông tin cơ bản
        </h3>
        
        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem', marginBottom: '1rem' }}>
          <div>
            <label style={labelStyle}>
              Kho nhập <span style={{ color: '#e74c3c' }}>*</span>
            </label>
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
              placeholder="Nhập số hóa đơn"
            />
          </div>
          
          <div>
            <label style={labelStyle}>
              Ngày nhập <span style={{ color: '#e74c3c' }}>*</span>
            </label>
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
              placeholder="Tên người giao hàng"
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
              placeholder="Số điện thoại"
            />
          </div>
        </div>
        
        <div>
          <label style={labelStyle}>Ghi chú</label>
          <textarea
            name="ghiChu"
            value={formData.ghiChu}
            onChange={handleInputChange}
            style={{ ...inputStyle, minHeight: '80px', resize: 'vertical' }}
            rows={3}
            placeholder="Nhập ghi chú nếu có..."
          />
        </div>
      </div>

      {/* Chi tiết hàng hóa */}
      <div style={{ marginBottom: '2rem' }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1rem' }}>
          <h3 style={{ 
            fontSize: '1.125rem', 
            fontWeight: '600', 
            color: '#2c3e50',
            margin: 0,
            paddingBottom: '0.5rem',
            borderBottom: '2px solid #27ae60'
          }}>
            Chi tiết hàng hóa
          </h3>
          <button
            type="button"
            onClick={addChiTietRow}
            style={{
              backgroundColor: '#27ae60',
              color: 'white',
              border: 'none',
              padding: '0.5rem 1rem',
              borderRadius: '0.5rem',
              cursor: 'pointer',
              fontWeight: '500',
              display: 'flex',
              alignItems: 'center',
              gap: '0.5rem'
            }}
          >
            ➕ Thêm hàng hóa
          </button>
        </div>

        {formData.chiTiet.length === 0 ? (
          <div style={{ 
            textAlign: 'center', 
            padding: '3rem 2rem', 
            backgroundColor: '#f8f9fa', 
            borderRadius: '0.5rem',
            border: '2px dashed #dee2e6'
          }}>
            <div style={{ fontSize: '4rem', marginBottom: '1rem', opacity: '0.3' }}>📦</div>
            <p style={{ color: '#7f8c8d', margin: 0 }}>
              Chưa có hàng hóa nào. Nhấn "Thêm hàng hóa" để bắt đầu.
            </p>
          </div>
        ) : (
          <div style={{ overflowX: 'auto', border: '1px solid #dee2e6', borderRadius: '0.5rem' }}>
            <table style={{ width: '100%', borderCollapse: 'collapse', minWidth: '900px' }}>
              <thead>
                <tr style={{ backgroundColor: '#f8f9fa' }}>
                  <th style={{ padding: '0.75rem', border: '1px solid #dee2e6', textAlign: 'left', fontSize: '0.875rem', fontWeight: '600' }}>Hàng hóa</th>
                  <th style={{ padding: '0.75rem', border: '1px solid #dee2e6', textAlign: 'center', fontSize: '0.875rem', fontWeight: '600' }}>Số lượng</th>
                  <th style={{ padding: '0.75rem', border: '1px solid #dee2e6', textAlign: 'right', fontSize: '0.875rem', fontWeight: '600' }}>Đơn giá</th>
                  <th style={{ padding: '0.75rem', border: '1px solid #dee2e6', textAlign: 'right', fontSize: '0.875rem', fontWeight: '600' }}>Thành tiền</th>
                  <th style={{ padding: '0.75rem', border: '1px solid #dee2e6', textAlign: 'center', fontSize: '0.875rem', fontWeight: '600' }}>Hạn SD</th>
                  <th style={{ padding: '0.75rem', border: '1px solid #dee2e6', textAlign: 'center', fontSize: '0.875rem', fontWeight: '600' }}>Số lô</th>
                  <th style={{ padding: '0.75rem', border: '1px solid #dee2e6', textAlign: 'center', fontSize: '0.875rem', fontWeight: '600' }}>Thao tác</th>
                </tr>
              </thead>
              <tbody>
                {formData.chiTiet.map((item, index) => (
                  <tr key={index} style={{ backgroundColor: 'white' }}>
                    <td style={{ padding: '0.5rem', border: '1px solid #dee2e6' }}>
                      <select
                        value={item.hangHoaId}
                        onChange={(e) => updateChiTiet(index, 'hangHoaId', e.target.value)}
                        style={{ width: '100%', padding: '0.5rem', border: '1px solid #ddd', borderRadius: '0.375rem', fontSize: '0.875rem' }}
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
                        style={{ width: '80px', padding: '0.5rem', border: '1px solid #ddd', borderRadius: '0.375rem', textAlign: 'center', fontSize: '0.875rem' }}
                        min="1"
                      />
                    </td>
                    <td style={{ padding: '0.5rem', border: '1px solid #dee2e6' }}>
                      <input
                        type="number"
                        value={item.donGia}
                        onChange={(e) => updateChiTiet(index, 'donGia', e.target.value)}
                        style={{ width: '120px', padding: '0.5rem', border: '1px solid #ddd', borderRadius: '0.375rem', textAlign: 'right', fontSize: '0.875rem' }}
                        min="0"
                        step="1000"
                      />
                    </td>
                    <td style={{ padding: '0.5rem', border: '1px solid #dee2e6', textAlign: 'right', fontWeight: '600', color: '#2c3e50', fontSize: '0.875rem' }}>
                      {new Intl.NumberFormat('vi-VN').format(item.thanhTien || 0)}₫
                    </td>
                    <td style={{ padding: '0.5rem', border: '1px solid #dee2e6' }}>
                      <input
                        type="date"
                        value={item.hanSuDung}
                        onChange={(e) => updateChiTiet(index, 'hanSuDung', e.target.value)}
                        style={{ width: '130px', padding: '0.5rem', border: '1px solid #ddd', borderRadius: '0.375rem', fontSize: '0.875rem' }}
                      />
                    </td>
                    <td style={{ padding: '0.5rem', border: '1px solid #dee2e6' }}>
                      <input
                        type="text"
                        value={item.soLo}
                        onChange={(e) => updateChiTiet(index, 'soLo', e.target.value)}
                        style={{ width: '80px', padding: '0.5rem', border: '1px solid #ddd', borderRadius: '0.375rem', fontSize: '0.875rem' }}
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
                          padding: '0.375rem 0.75rem',
                          borderRadius: '0.375rem',
                          cursor: 'pointer',
                          fontSize: '0.875rem'
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
            padding: '1rem 1.5rem', 
            background: 'linear-gradient(135deg, #d5f4e6 0%, #e3f2fd 100%)', 
            borderRadius: '0.5rem',
            border: '1px solid #b2dfdb'
          }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '1rem' }}>
              <div>
                <p style={{ color: '#546e7a', fontSize: '0.875rem', margin: 0, marginBottom: '0.25rem' }}>
                  Tổng số mặt hàng: <strong style={{ color: '#2c3e50' }}>{formData.chiTiet.length}</strong>
                </p>
              </div>
              <div style={{ textAlign: 'right' }}>
                <p style={{ color: '#546e7a', fontSize: '0.875rem', margin: 0, marginBottom: '0.25rem' }}>
                  Tổng giá trị:
                </p>
                <p style={{ fontSize: '1.5rem', fontWeight: 'bold', color: '#27ae60', margin: 0 }}>
                  {new Intl.NumberFormat('vi-VN', { 
                    style: 'currency', 
                    currency: 'VND' 
                  }).format(formData.chiTiet.reduce((sum, item) => sum + (item.thanhTien || 0), 0))}
                </p>
              </div>
            </div>
          </div>
        )}
      </div>

      {/* Action buttons */}
      <div style={{ 
        display: 'flex', 
        justifyContent: 'flex-end', 
        gap: '1rem', 
        borderTop: '1px solid #dee2e6', 
        paddingTop: '1.5rem',
        marginTop: '1rem'
      }}>
        <button
          type="button"
          onClick={onCancel}
          style={{
            padding: '0.75rem 1.5rem',
            backgroundColor: '#95a5a6',
            color: 'white',
            border: 'none',
            borderRadius: '0.5rem',
            cursor: 'pointer',
            fontWeight: '500',
            fontSize: '1rem'
          }}
        >
          Hủy
        </button>
        <button
          type="submit"
          disabled={loading}
          style={{
            padding: '0.75rem 1.5rem',
            backgroundColor: loading ? '#95a5a6' : '#27ae60',
            color: 'white',
            border: 'none',
            borderRadius: '0.5rem',
            cursor: loading ? 'not-allowed' : 'pointer',
            fontWeight: '500',
            fontSize: '1rem',
            display: 'flex',
            alignItems: 'center',
            gap: '0.5rem'
          }}
        >
          {loading ? 'Đang xử lý...' : '✓ Tạo Phiếu Nhập'}
        </button>
      </div>
    </form>
  );
};

export default NhapKhoForm;
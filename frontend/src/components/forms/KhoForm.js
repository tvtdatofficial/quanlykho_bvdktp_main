import React, { useState, useEffect } from 'react';
import { toast } from 'react-toastify';
import api from '../../services/api';

const KhoForm = ({ initialData, onSubmit, onCancel }) => {
  const [formData, setFormData] = useState({
    maKho: '',
    tenKho: '',
    loaiKho: 'KHO_CHINH',
    moTa: '',
    diaChi: '',
    dienTich: '',
    nhietDoMin: '',
    nhietDoMax: '',
    doAmMin: '',
    doAmMax: '',
    khoaPhongId: '',
    quanLyKhoId: '',
    trangThai: 'HOAT_DONG'
  });
  
  const [khoaPhongList, setKhoaPhongList] = useState([]);
  const [userList, setUserList] = useState([]);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (initialData) {
      setFormData(initialData);
    }
    fetchKhoaPhongList();
    fetchUserList();
  }, [initialData]);

  const fetchKhoaPhongList = async () => {
    try {
      const response = await api.get('/api/khoa-phong/active');
      setKhoaPhongList(response.data.data || []);
    } catch (error) {
      console.log('Không thể tải danh sách khoa phòng:', error);
      // Fallback to empty array if API fails
      setKhoaPhongList([]);
    }
  };

  const fetchUserList = async () => {
    try {
      const response = await api.get('/api/user/quan-ly-kho');
      setUserList(response.data.data || []);
    } catch (error) {
      console.log('Không thể tải danh sách quản lý kho:', error);
      // Fallback to empty array if API fails
      setUserList([]);
    }
  };

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
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
    borderRadius: '6px',
    fontSize: '1rem',
    outline: 'none'
  };

  const labelStyle = {
    display: 'block',
    marginBottom: '0.5rem',
    color: '#2c3e50',
    fontWeight: '500'
  };

  return (
    <form onSubmit={handleSubmit}>
      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem', marginBottom: '1rem' }}>
        <div>
          <label style={labelStyle}>Mã Kho *</label>
          <input
            type="text"
            name="maKho"
            value={formData.maKho}
            onChange={handleChange}
            style={inputStyle}
            required
            maxLength={20}
          />
        </div>
        <div>
          <label style={labelStyle}>Tên Kho *</label>
          <input
            type="text"
            name="tenKho"
            value={formData.tenKho}
            onChange={handleChange}
            style={inputStyle}
            required
            maxLength={100}
          />
        </div>
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem', marginBottom: '1rem' }}>
        <div>
          <label style={labelStyle}>Loại Kho *</label>
          <select
            name="loaiKho"
            value={formData.loaiKho}
            onChange={handleChange}
            style={inputStyle}
            required
          >
            <option value="KHO_CHINH">Kho Chính</option>
            <option value="KHO_DUOC">Kho Dược</option>
            <option value="KHO_VAT_TU">Kho Vật Tư</option>
            <option value="KHO_THIET_BI">Kho Thiết Bị</option>
            <option value="KHO_TINH_THAT">Kho Trang Thiết Bị</option>
            <option value="KHO_HOA_CHAT">Kho Hóa Chất</option>
          </select>
        </div>
        <div>
          <label style={labelStyle}>Khoa Phòng *</label>
          <select
            name="khoaPhongId"
            value={formData.khoaPhongId}
            onChange={handleChange}
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

      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem', marginBottom: '1rem' }}>
        <div>
          <label style={labelStyle}>Quản Lý Kho</label>
          <select
            name="quanLyKhoId"
            value={formData.quanLyKhoId}
            onChange={handleChange}
            style={inputStyle}
          >
            <option value="">-- Chọn quản lý kho --</option>
            {userList.map(user => (
              <option key={user.id} value={user.id}>{user.hoTen}</option>
            ))}
          </select>
        </div>
        <div>
          <label style={labelStyle}>Trạng Thái</label>
          <select
            name="trangThai"
            value={formData.trangThai}
            onChange={handleChange}
            style={inputStyle}
          >
            <option value="HOAT_DONG">Hoạt động</option>
            <option value="BAO_TRI">Bảo trì</option>
            <option value="DONG_CUA">Đóng cửa</option>
          </select>
        </div>
      </div>

      <div style={{ marginBottom: '1rem' }}>
        <label style={labelStyle}>Mô Tả</label>
        <textarea
          name="moTa"
          value={formData.moTa}
          onChange={handleChange}
          style={{ ...inputStyle, minHeight: '80px' }}
          rows={3}
        />
      </div>

      <div style={{ marginBottom: '1rem' }}>
        <label style={labelStyle}>Địa Chỉ</label>
        <input
          type="text"
          name="diaChi"
          value={formData.diaChi}
          onChange={handleChange}
          style={inputStyle}
        />
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr 1fr', gap: '1rem', marginBottom: '1rem' }}>
        <div>
          <label style={labelStyle}>Diện Tích (m²)</label>
          <input
            type="number"
            name="dienTich"
            value={formData.dienTich}
            onChange={handleChange}
            style={inputStyle}
            step="0.01"
          />
        </div>
        <div>
          <label style={labelStyle}>Nhiệt Độ Min (°C)</label>
          <input
            type="number"
            name="nhietDoMin"
            value={formData.nhietDoMin}
            onChange={handleChange}
            style={inputStyle}
            step="0.1"
          />
        </div>
        <div>
          <label style={labelStyle}>Nhiệt Độ Max (°C)</label>
          <input
            type="number"
            name="nhietDoMax"
            value={formData.nhietDoMax}
            onChange={handleChange}
            style={inputStyle}
            step="0.1"
          />
        </div>
      </div>

      <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '1rem', marginTop: '2rem' }}>
        <button
          type="button"
          onClick={onCancel}
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
          disabled={loading}
          style={{
            padding: '0.75rem 1.5rem',
            backgroundColor: loading ? '#95a5a6' : '#27ae60',
            color: 'white',
            border: 'none',
            borderRadius: '6px',
            cursor: loading ? 'not-allowed' : 'pointer'
          }}
        >
          {loading ? 'Đang xử lý...' : (initialData ? 'Cập nhật' : 'Tạo mới')}
        </button>
      </div>
    </form>
  );
};

export default KhoForm;
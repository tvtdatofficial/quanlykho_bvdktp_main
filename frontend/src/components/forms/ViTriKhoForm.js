import React, { useState, useEffect } from 'react';
import api from '../../services/api';

const ViTriKhoForm = ({ initialData, onSubmit, onCancel }) => {
  const [formData, setFormData] = useState({
    khoId: '',
    maViTri: '',
    tenViTri: '',
    loaiViTri: 'KE',
    viTriChaId: '',
    moTa: '',
    sucChuaToiDa: '',
    trongLuongToiDa: '',
    nhietDoYeuCau: '',
    trangThai: 'TRONG'
  });

  const [khoList, setKhoList] = useState([]);
  const [viTriChaList, setViTriChaList] = useState([]);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (initialData) {
      setFormData(initialData);
    }
    fetchKhoList();
  }, [initialData]);

  useEffect(() => {
    if (formData.khoId) {
      fetchViTriChaList(formData.khoId);
    }
  }, [formData.khoId]);

  const fetchKhoList = async () => {
    try {
      const response = await api.get('/api/kho/active');
      setKhoList(response.data.data || []);
    } catch (error) {
      console.error('Error fetching kho:', error);
      setKhoList([]);
    }
  };

  const fetchViTriChaList = async (khoId) => {
    try {
      const response = await api.get(`/api/vi-tri-kho/tree/${khoId}`);
      setViTriChaList(response.data.data || []);
    } catch (error) {
      console.error('Error fetching vi tri cha:', error);
      setViTriChaList([]);
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
          <label style={labelStyle}>Kho *</label>
          <select
            name="khoId"
            value={formData.khoId}
            onChange={handleChange}
            style={inputStyle}
            required
            disabled={!!initialData}
          >
            <option value="">-- Chọn kho --</option>
            {khoList.map(kho => (
              <option key={kho.id} value={kho.id}>{kho.tenKho}</option>
            ))}
          </select>
        </div>
        <div>
          <label style={labelStyle}>Mã Vị Trí *</label>
          <input
            type="text"
            name="maViTri"
            value={formData.maViTri}
            onChange={handleChange}
            style={inputStyle}
            required
            maxLength={30}
            placeholder="VD: KE-A-01"
          />
        </div>
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem', marginBottom: '1rem' }}>
        <div>
          <label style={labelStyle}>Tên Vị Trí</label>
          <input
            type="text"
            name="tenViTri"
            value={formData.tenViTri}
            onChange={handleChange}
            style={inputStyle}
            maxLength={100}
            placeholder="VD: Kệ A - Ngăn 1"
          />
        </div>
        <div>
          <label style={labelStyle}>Loại Vị Trí</label>
          <select
            name="loaiViTri"
            value={formData.loaiViTri}
            onChange={handleChange}
            style={inputStyle}
          >
            <option value="KE">Kệ</option>
            <option value="NGAN">Ngăn</option>
            <option value="O">Ô</option>
            <option value="TU_LANH">Tủ lạnh</option>
            <option value="TU_DONG">Tủ đông</option>
            <option value="KHU_VUC">Khu vực</option>
          </select>
        </div>
      </div>

      <div style={{ marginBottom: '1rem' }}>
        <label style={labelStyle}>Vị Trí Cha</label>
        <select
          name="viTriChaId"
          value={formData.viTriChaId}
          onChange={handleChange}
          style={inputStyle}
          disabled={!formData.khoId}
        >
          <option value="">-- Không có (Vị trí gốc) --</option>
          {viTriChaList.map(vt => (
            <option key={vt.id} value={vt.id} disabled={vt.id === initialData?.id}>
              {vt.maViTri} - {vt.tenViTri}
            </option>
          ))}
        </select>
        <small style={{ color: '#7f8c8d', fontSize: '0.85rem' }}>
          Chọn vị trí cha để tạo cấu trúc phân cấp (VD: Kệ → Ngăn → Ô)
        </small>
      </div>

      <div style={{ marginBottom: '1rem' }}>
        <label style={labelStyle}>Mô Tả</label>
        <textarea
          name="moTa"
          value={formData.moTa}
          onChange={handleChange}
          style={{ ...inputStyle, minHeight: '80px' }}
          rows={3}
          placeholder="Mô tả chi tiết về vị trí này..."
        />
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr 1fr', gap: '1rem', marginBottom: '1rem' }}>
        <div>
          <label style={labelStyle}>Sức Chứa Tối Đa</label>
          <input
            type="number"
            name="sucChuaToiDa"
            value={formData.sucChuaToiDa}
            onChange={handleChange}
            style={inputStyle}
            min="0"
            placeholder="Số lượng"
          />
        </div>
        <div>
          <label style={labelStyle}>Trọng Lượng Tối Đa (kg)</label>
          <input
            type="number"
            name="trongLuongToiDa"
            value={formData.trongLuongToiDa}
            onChange={handleChange}
            style={inputStyle}
            step="0.01"
            min="0"
          />
        </div>
        <div>
          <label style={labelStyle}>Nhiệt Độ Yêu Cầu (°C)</label>
          <input
            type="number"
            name="nhietDoYeuCau"
            value={formData.nhietDoYeuCau}
            onChange={handleChange}
            style={inputStyle}
            step="0.1"
            placeholder="VD: 2-8"
          />
        </div>
      </div>

      <div style={{ marginBottom: '1rem' }}>
        <label style={labelStyle}>Trạng Thái</label>
        <select
          name="trangThai"
          value={formData.trangThai}
          onChange={handleChange}
          style={inputStyle}
        >
          <option value="TRONG">Trống</option>
          <option value="CO_HANG">Có hàng</option>
          <option value="DAY">Đầy</option>
          <option value="BAO_TRI">Bảo trì</option>
        </select>
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

export default ViTriKhoForm;
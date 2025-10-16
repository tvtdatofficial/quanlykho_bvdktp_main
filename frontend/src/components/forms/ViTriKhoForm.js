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
  const [errors, setErrors] = useState({});

  useEffect(() => {
    fetchSelectData();
    if (initialData) {
      setFormData(initialData);
    }
  }, [initialData]);

  useEffect(() => {
    if (formData.khoId) {
      fetchViTriCha(formData.khoId);
    }
  }, [formData.khoId]);

  const fetchSelectData = async () => {
    try {
      const response = await api.get('/api/kho/active');
      setKhoList(response.data.data || response.data || []);
    } catch (error) {
      console.error('Error fetching kho:', error);
    }
  };

  const fetchViTriCha = async (khoId) => {
    try {
      const response = await api.get(`/api/vi-tri-kho?khoId=${khoId}&size=100`);
      setViTriChaList(response.data.content || []);
    } catch (error) {
      console.error('Error fetching vi tri cha:', error);
    }
  };

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({ ...prev, [name]: value }));
    
    // Clear error khi user nhập
    if (errors[name]) {
      setErrors(prev => ({ ...prev, [name]: '' }));
    }
  };

  const validateForm = () => {
    const newErrors = {};
    
    if (!formData.khoId) newErrors.khoId = 'Vui lòng chọn kho';
    if (!formData.maViTri.trim()) newErrors.maViTri = 'Vui lòng nhập mã vị trí';
    if (!formData.tenViTri.trim()) newErrors.tenViTri = 'Vui lòng nhập tên vị trí';
    if (formData.sucChuaToiDa && formData.sucChuaToiDa < 0) {
      newErrors.sucChuaToiDa = 'Sức chứa phải >= 0';
    }
    
    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    if (!validateForm()) return;
    
    setLoading(true);
    try {
      const submitData = {
        ...formData,
        khoId: parseInt(formData.khoId),
        viTriChaId: formData.viTriChaId ? parseInt(formData.viTriChaId) : null,
        sucChuaToiDa: formData.sucChuaToiDa ? parseInt(formData.sucChuaToiDa) : null,
        trongLuongToiDa: formData.trongLuongToiDa ? parseFloat(formData.trongLuongToiDa) : null,
        nhietDoYeuCau: formData.nhietDoYeuCau ? parseFloat(formData.nhietDoYeuCau) : null
      };
      
      await onSubmit(submitData);
    } catch (error) {
      console.error('Error submitting:', error);
    } finally {
      setLoading(false);
    }
  };

  const inputStyle = {
    width: '100%',
    padding: '0.75rem',
    border: '1px solid #ddd',
    borderRadius: '6px',
    fontSize: '1rem'
  };

  const errorInputStyle = {
    ...inputStyle,
    borderColor: '#e74c3c'
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
          <label style={labelStyle}>
            Kho <span style={{ color: '#e74c3c' }}>*</span>
          </label>
          <select
            name="khoId"
            value={formData.khoId}
            onChange={handleChange}
            style={errors.khoId ? errorInputStyle : inputStyle}
            required
            disabled={!!initialData}
          >
            <option value="">-- Chọn kho --</option>
            {khoList.map(kho => (
              <option key={kho.id} value={kho.id}>{kho.tenKho}</option>
            ))}
          </select>
          {errors.khoId && <div style={{ color: '#e74c3c', fontSize: '0.875rem', marginTop: '0.25rem' }}>{errors.khoId}</div>}
        </div>

        <div>
          <label style={labelStyle}>Vị trí cha</label>
          <select
            name="viTriChaId"
            value={formData.viTriChaId}
            onChange={handleChange}
            style={inputStyle}
          >
            <option value="">-- Không có (vị trí gốc) --</option>
            {viTriChaList.map(vt => (
              
              <option key={vt.id} value={vt.id}>{vt.tenViTri}</option>
            ))}
          </select>
        </div>
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem', marginBottom: '1rem' }}>
        <div>
          <label style={labelStyle}>
            Mã vị trí <span style={{ color: '#e74c3c' }}>*</span>
          </label>
          <input
            type="text"
            name="maViTri"
            value={formData.maViTri}
            onChange={handleChange}
            style={errors.maViTri ? errorInputStyle : inputStyle}
            required
            maxLength={30}
            placeholder="VD: KE-A1"
          />
          {errors.maViTri && <div style={{ color: '#e74c3c', fontSize: '0.875rem', marginTop: '0.25rem' }}>{errors.maViTri}</div>}
        </div>

        <div>
          <label style={labelStyle}>
            Tên vị trí <span style={{ color: '#e74c3c' }}>*</span>
          </label>
          <input
            type="text"
            name="tenViTri"
            value={formData.tenViTri}
            onChange={handleChange}
            style={errors.tenViTri ? errorInputStyle : inputStyle}
            required
            maxLength={100}
            placeholder="VD: Kệ A Ngăn 1"
          />
          {errors.tenViTri && <div style={{ color: '#e74c3c', fontSize: '0.875rem', marginTop: '0.25rem' }}>{errors.tenViTri}</div>}
        </div>
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem', marginBottom: '1rem' }}>
        <div>
          <label style={labelStyle}>Loại vị trí</label>
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

        <div>
          <label style={labelStyle}>Trạng thái</label>
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
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr 1fr', gap: '1rem', marginBottom: '1rem' }}>
        <div>
          <label style={labelStyle}>Sức chứa tối đa</label>
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
          <label style={labelStyle}>Trọng lượng tối đa (kg)</label>
          <input
            type="number"
            name="trongLuongToiDa"
            value={formData.trongLuongToiDa}
            onChange={handleChange}
            style={inputStyle}
            min="0"
            step="0.1"
          />
        </div>

        <div>
          <label style={labelStyle}>Nhiệt độ yêu cầu (°C)</label>
          <input
            type="number"
            name="nhietDoYeuCau"
            value={formData.nhietDoYeuCau}
            onChange={handleChange}
            style={inputStyle}
            step="0.1"
          />
        </div>
      </div>

      <div style={{ marginBottom: '1.5rem' }}>
        <label style={labelStyle}>Mô tả</label>
        <textarea
          name="moTa"
          value={formData.moTa}
          onChange={handleChange}
          style={{ ...inputStyle, minHeight: '80px', resize: 'vertical' }}
          rows={3}
          placeholder="Mô tả về vị trí kho..."
        />
      </div>

      <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '1rem', paddingTop: '1rem', borderTop: '1px solid #ddd' }}>
        <button
          type="button"
          onClick={onCancel}
          style={{
            padding: '0.75rem 1.5rem',
            backgroundColor: '#95a5a6',
            color: 'white',
            border: 'none',
            borderRadius: '6px',
            cursor: 'pointer',
            fontWeight: '500'
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
            cursor: loading ? 'not-allowed' : 'pointer',
            fontWeight: '500'
          }}
        >
          {loading ? 'Đang xử lý...' : (initialData ? 'Cập nhật' : 'Tạo mới')}
        </button>
      </div>
    </form>
  );
};

export default ViTriKhoForm;
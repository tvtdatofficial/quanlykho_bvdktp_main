import React, { useState, useEffect, useCallback } from 'react';
import api from '../../services/api';

const LoHangForm = ({ initialData, onSubmit, onCancel }) => {
  const [formData, setFormData] = useState({
    hangHoaId: '',
    soLo: '',
    ngaySanXuat: '',
    hanSuDung: '',
    soLuongNhap: '',
    giaNhap: '',
    nhaCungCapId: '',
    soChungTuNhap: '',
    ghiChu: ''
  });

  const [hangHoaList, setHangHoaList] = useState([]);
  const [nhaCungCapList, setNhaCungCapList] = useState([]);
  const [loading, setLoading] = useState(false);
  const [loadingData, setLoadingData] = useState(true);
  const [selectedHangHoa, setSelectedHangHoa] = useState(null);
  const [errors, setErrors] = useState({});
  const [submitError, setSubmitError] = useState('');

  useEffect(() => {
    fetchSelectData();
  }, []);

  useEffect(() => {
    if (initialData) {
      setFormData({
        hangHoaId: initialData.hangHoaId || '',
        soLo: initialData.soLo || '',
        ngaySanXuat: initialData.ngaySanXuat || '',
        hanSuDung: initialData.hanSuDung || '',
        soLuongNhap: initialData.soLuongNhap || '',
        giaNhap: initialData.giaNhap || '',
        nhaCungCapId: initialData.nhaCungCapId || '',
        soChungTuNhap: initialData.soChungTuNhap || '',
        ghiChu: initialData.ghiChu || ''
      });

      if (initialData.hangHoaId && hangHoaList.length > 0) {
        const hangHoa = hangHoaList.find(h => h.id === initialData.hangHoaId);
        setSelectedHangHoa(hangHoa || null);
      }
    }
  }, [initialData, hangHoaList]);

  const fetchSelectData = async () => {
    setLoadingData(true);
    try {
      const [hangHoaRes, nhaCungCapRes] = await Promise.all([
        api.get('/api/hang-hoa?size=100'),
        api.get('/api/nha-cung-cap/active')
      ]);

      setHangHoaList(hangHoaRes.data.data?.content || []);
      setNhaCungCapList(nhaCungCapRes.data.data || []);
    } catch (error) {
      console.error('Error fetching select data:', error);
      setSubmitError('Không thể tải dữ liệu. Vui lòng thử lại sau.');
    } finally {
      setLoadingData(false);
    }
  };

  const validateField = (name, value) => {
    const newErrors = { ...errors };

    switch (name) {
      case 'hangHoaId':
        if (!value) {
          newErrors.hangHoaId = 'Vui lòng chọn hàng hóa';
        } else {
          delete newErrors.hangHoaId;
        }
        break;

      case 'soLo':
        if (!value.trim()) {
          newErrors.soLo = 'Vui lòng nhập số lô';
        } else if (value.length > 50) {
          newErrors.soLo = 'Số lô không được quá 50 ký tự';
        } else {
          delete newErrors.soLo;
        }
        break;

      case 'hanSuDung':
        if (!value) {
          newErrors.hanSuDung = 'Vui lòng chọn hạn sử dụng';
        } else if (formData.ngaySanXuat && new Date(value) < new Date(formData.ngaySanXuat)) {
          newErrors.hanSuDung = 'Hạn sử dụng phải sau ngày sản xuất';
        } else {
          delete newErrors.hanSuDung;
        }
        break;

      case 'ngaySanXuat':
        if (value && new Date(value) > new Date()) {
          newErrors.ngaySanXuat = 'Ngày sản xuất không được là tương lai';
        } else if (value && formData.hanSuDung && new Date(value) > new Date(formData.hanSuDung)) {
          newErrors.ngaySanXuat = 'Ngày sản xuất phải trước hạn sử dụng';
        } else {
          delete newErrors.ngaySanXuat;
        }
        break;

      case 'soLuongNhap':
        if (!value || value <= 0) {
          newErrors.soLuongNhap = 'Số lượng phải lớn hơn 0';
        } else {
          delete newErrors.soLuongNhap;
        }
        break;

      case 'giaNhap':
        if (!value || value < 0) {
          newErrors.giaNhap = 'Giá nhập không hợp lệ';
        } else {
          delete newErrors.giaNhap;
        }
        break;

      default:
        break;
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleChange = (e) => {
    const { name, value } = e.target;

    setFormData(prev => ({
      ...prev,
      [name]: value
    }));

    validateField(name, value);

    if (name === 'hangHoaId' && value) {
      const hangHoa = hangHoaList.find(h => h.id === parseInt(value));
      setSelectedHangHoa(hangHoa || null);

      if (hangHoa && !formData.giaNhap) {
        setFormData(prev => ({
          ...prev,
          giaNhap: hangHoa.giaNhapTrungBinh || ''
        }));
      }
    }

    if (name === 'ngaySanXuat' && formData.hanSuDung) {
      validateField('hanSuDung', formData.hanSuDung);
    }

    if (name === 'hanSuDung' && formData.ngaySanXuat) {
      validateField('ngaySanXuat', formData.ngaySanXuat);
    }
  };

  const validateForm = () => {
    const requiredFields = {
      hangHoaId: 'Vui lòng chọn hàng hóa',
      soLo: 'Vui lòng nhập số lô',
      hanSuDung: 'Vui lòng chọn hạn sử dụng',
      soLuongNhap: 'Vui lòng nhập số lượng',
      giaNhap: 'Vui lòng nhập giá'
    };

    const newErrors = {};

    Object.keys(requiredFields).forEach(field => {
      if (!formData[field]) {
        newErrors[field] = requiredFields[field];
      }
    });

    if (formData.ngaySanXuat && formData.hanSuDung) {
      if (new Date(formData.hanSuDung) < new Date(formData.ngaySanXuat)) {
        newErrors.hanSuDung = 'Hạn sử dụng phải sau ngày sản xuất';
      }
    }

    if (formData.soLuongNhap && formData.soLuongNhap <= 0) {
      newErrors.soLuongNhap = 'Số lượng phải lớn hơn 0';
    }

    if (formData.giaNhap && formData.giaNhap < 0) {
      newErrors.giaNhap = 'Giá nhập không hợp lệ';
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    if (!validateForm()) {
      setSubmitError('Vui lòng kiểm tra lại thông tin');
      return;
    }

    setLoading(true);
    try {
      const submitData = {
        ...formData,
        hangHoaId: parseInt(formData.hangHoaId),
        soLuongNhap: parseInt(formData.soLuongNhap),
        giaNhap: parseFloat(formData.giaNhap),
        nhaCungCapId: formData.nhaCungCapId ?
          parseInt(formData.nhaCungCapId) : null,

        // ✅ Khi edit, chỉ cho phép sửa ngày nếu đang tạo mới
        ngaySanXuat: initialData ?
          initialData.ngaySanXuat : // Giữ nguyên khi edit
          (formData.ngaySanXuat || null),

        hanSuDung: initialData ?
          initialData.hanSuDung : // Giữ nguyên khi edit
          (formData.hanSuDung || null),

        // Chỉ cho phép sửa ghi chú
        ghiChu: formData.ghiChu
      };

      await onSubmit(submitData);
    } catch (error) {
      console.error('Error:', error);
      setSubmitError(
        error.response?.data?.message ||
        'Có lỗi xảy ra. Vui lòng thử lại.'
      );
    } finally {
      setLoading(false);
    }
  };

  const calculateDaysLeft = useCallback(() => {
    if (!formData.hanSuDung) return null;
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    const expiryDate = new Date(formData.hanSuDung);
    expiryDate.setHours(0, 0, 0, 0);
    const diffTime = expiryDate - today;
    const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
    return diffDays;
  }, [formData.hanSuDung]);

  const daysLeft = calculateDaysLeft();

  const formatCurrency = (value) => {
    if (!value) return '';
    return new Intl.NumberFormat('vi-VN', {
      style: 'currency',
      currency: 'VND'
    }).format(value);
  };

  const inputStyle = {
    width: '100%',
    padding: '0.75rem',
    border: '1px solid #ddd',
    borderRadius: '6px',
    fontSize: '1rem',
    outline: 'none',
    transition: 'border-color 0.3s'
  };

  const inputErrorStyle = {
    ...inputStyle,
    borderColor: '#e74c3c'
  };

  const labelStyle = {
    display: 'block',
    marginBottom: '0.5rem',
    color: '#2c3e50',
    fontWeight: '500',
    fontSize: '0.95rem'
  };

  const errorStyle = {
    color: '#e74c3c',
    fontSize: '0.85rem',
    marginTop: '0.25rem'
  };

  if (loadingData) {
    return (
      <div style={{ textAlign: 'center', padding: '3rem' }}>
        <div style={{ fontSize: '1.2rem', color: '#7f8c8d' }}>
          Đang tải dữ liệu...
        </div>
      </div>
    );
  }

  return (
    <form onSubmit={handleSubmit}>
      {submitError && (
        <div style={{
          padding: '1rem',
          backgroundColor: '#ffebee',
          color: '#c62828',
          borderRadius: '6px',
          marginBottom: '1rem',
          border: '1px solid #ef5350'
        }}>
          ⚠️ {submitError}
        </div>
      )}

      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem', marginBottom: '1rem' }}>
        <div>
          <label style={labelStyle}>
            Hàng Hóa <span style={{ color: '#e74c3c' }}>*</span>
          </label>
          <select
            name="hangHoaId"
            value={formData.hangHoaId}
            onChange={handleChange}
            style={errors.hangHoaId ? inputErrorStyle : inputStyle}
            required
            disabled={!!initialData}
          >
            <option value="">-- Chọn hàng hóa --</option>
            {hangHoaList.map(hh => (
              <option key={hh.id} value={hh.id}>
                {hh.tenHangHoa} ({hh.maHangHoa})
              </option>
            ))}
          </select>
          {errors.hangHoaId && <div style={errorStyle}>{errors.hangHoaId}</div>}
        </div>

        <div>
          <label style={labelStyle}>
            Số Lô <span style={{ color: '#e74c3c' }}>*</span>
          </label>
          <input
            type="text"
            name="soLo"
            value={formData.soLo}
            onChange={handleChange}
            style={errors.soLo ? inputErrorStyle : inputStyle}
            required
            maxLength={50}
            placeholder="VD: LOT-2024-001"
          />
          {errors.soLo && <div style={errorStyle}>{errors.soLo}</div>}
        </div>
      </div>

      {selectedHangHoa && (
        <div style={{
          padding: '1rem',
          backgroundColor: '#e8f5e9',
          borderRadius: '6px',
          marginBottom: '1rem',
          fontSize: '0.9rem',
          border: '1px solid #a5d6a7'
        }}>
          <strong style={{ color: '#2e7d32' }}>📦 Thông tin hàng hóa:</strong>
          <div style={{ marginTop: '0.5rem', lineHeight: '1.6' }}>
            <div>Danh mục: <strong>{selectedHangHoa.tenDanhMuc}</strong></div>
            <div>Đơn vị: <strong>{selectedHangHoa.tenDonViTinh}</strong></div>
            <div>Tồn kho hiện tại: <strong style={{ color: '#1976d2' }}>{selectedHangHoa.soLuongCoTheXuat || 0}</strong></div>
            {selectedHangHoa.giaNhapTrungBinh && (
              <div>Giá nhập TB: <strong>{formatCurrency(selectedHangHoa.giaNhapTrungBinh)}</strong></div>
            )}
          </div>
        </div>
      )}

      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem', marginBottom: '1rem' }}>
        <div>
          <label style={labelStyle}>Ngày Sản Xuất</label>
          <input
            type="date"
            name="ngaySanXuat"
            value={formData.ngaySanXuat}
            onChange={handleChange}
            style={errors.ngaySanXuat ? inputErrorStyle : inputStyle}
            disabled={!!initialData}  // ✅ THÊM DÒNG NÀY
            max={new Date().toISOString().split('T')[0]}
          />
          {errors.ngaySanXuat && <div style={errorStyle}>{errors.ngaySanXuat}</div>}
        </div>

        <div>
          <label style={labelStyle}>
            Hạn Sử Dụng <span style={{ color: '#e74c3c' }}>*</span>
          </label>
          <input
            type="date"
            name="hanSuDung"
            value={formData.hanSuDung}
            onChange={handleChange}
            style={errors.hanSuDung ? inputErrorStyle : inputStyle}
            disabled={!!initialData}
            required
          />
          {errors.hanSuDung && <div style={errorStyle}>{errors.hanSuDung}</div>}
        </div>
      </div>

      {daysLeft !== null && (
        <div style={{
          padding: '1rem',
          backgroundColor: daysLeft < 30 ? '#ffebee' : daysLeft < 90 ? '#fff3e0' : '#e3f2fd',
          borderRadius: '6px',
          marginBottom: '1rem',
          fontSize: '0.95rem',
          fontWeight: '500',
          color: daysLeft < 30 ? '#c62828' : daysLeft < 90 ? '#e65100' : '#1565c0',
          border: `1px solid ${daysLeft < 30 ? '#ef5350' : daysLeft < 90 ? '#ff9800' : '#42a5f5'}`
        }}>
          {daysLeft < 0 ? (
            <span>⚠️ Đã hết hạn {Math.abs(daysLeft)} ngày</span>
          ) : daysLeft === 0 ? (
            <span>⚠️ Hết hạn hôm nay</span>
          ) : daysLeft < 30 ? (
            <span>⚠️ Còn {daysLeft} ngày đến hạn sử dụng</span>
          ) : daysLeft < 90 ? (
            <span>📅 Còn {daysLeft} ngày đến hạn sử dụng</span>
          ) : (
            <span>✓ Còn {daysLeft} ngày đến hạn sử dụng</span>
          )}
        </div>
      )}

      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem', marginBottom: '1rem' }}>
        <div>
          <label style={labelStyle}>
            Số Lượng Nhập <span style={{ color: '#e74c3c' }}>*</span>
          </label>
          <input
            type="number"
            name="soLuongNhap"
            value={formData.soLuongNhap}
            onChange={handleChange}
            style={errors.soLuongNhap ? inputErrorStyle : inputStyle}
            required
            min="1"
            disabled={!!initialData}
          />
          {errors.soLuongNhap && <div style={errorStyle}>{errors.soLuongNhap}</div>}
        </div>

        <div>
          <label style={labelStyle}>
            Giá Nhập <span style={{ color: '#e74c3c' }}>*</span>
          </label>
          <input
            type="number"
            name="giaNhap"
            value={formData.giaNhap}
            onChange={handleChange}
            style={errors.giaNhap ? inputErrorStyle : inputStyle}
            required
            min="0"
            step="1000"
          />
          {errors.giaNhap && <div style={errorStyle}>{errors.giaNhap}</div>}
          {formData.giaNhap && (
            <div style={{ marginTop: '0.25rem', fontSize: '0.85rem', color: '#7f8c8d' }}>
              {formatCurrency(formData.giaNhap)}
            </div>
          )}
        </div>
      </div>

      {formData.soLuongNhap && formData.giaNhap && (
        <div style={{
          padding: '0.75rem',
          backgroundColor: '#f5f5f5',
          borderRadius: '6px',
          marginBottom: '1rem',
          fontSize: '0.95rem',
          color: '#424242'
        }}>
          <strong>Tổng giá trị nhập:</strong> {formatCurrency(formData.soLuongNhap * formData.giaNhap)}
        </div>
      )}

      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem', marginBottom: '1rem' }}>
        <div>
          <label style={labelStyle}>Nhà Cung Cấp</label>
          <select
            name="nhaCungCapId"
            value={formData.nhaCungCapId}
            onChange={handleChange}
            style={inputStyle}
          >
            <option value="">-- Chọn nhà cung cấp --</option>
            {nhaCungCapList.map(ncc => (
              <option key={ncc.id} value={ncc.id}>
                {ncc.tenNcc}
              </option>
            ))}
          </select>
        </div>

        <div>
          <label style={labelStyle}>Số Chứng Từ Nhập</label>
          <input
            type="text"
            name="soChungTuNhap"
            value={formData.soChungTuNhap}
            onChange={handleChange}
            style={inputStyle}
            maxLength={50}
            placeholder="Mã phiếu nhập"
          />
        </div>
      </div>

      <div style={{ marginBottom: '1.5rem' }}>
        <label style={labelStyle}>Ghi Chú</label>
        <textarea
          name="ghiChu"
          value={formData.ghiChu}
          onChange={handleChange}
          style={{ ...inputStyle, minHeight: '80px', resize: 'vertical' }}
          rows={3}
          placeholder="Thông tin bổ sung về lô hàng..."
          maxLength={500}
        />
        <div style={{ marginTop: '0.25rem', fontSize: '0.85rem', color: '#7f8c8d', textAlign: 'right' }}>
          {formData.ghiChu.length}/500 ký tự
        </div>
      </div>

      <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '1rem', marginTop: '2rem', paddingTop: '1rem', borderTop: '1px solid #ddd' }}>
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
            fontSize: '1rem',
            fontWeight: '500',
            transition: 'background-color 0.3s'
          }}
          onMouseOver={(e) => e.target.style.backgroundColor = '#7f8c8d'}
          onMouseOut={(e) => e.target.style.backgroundColor = '#95a5a6'}
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
            fontSize: '1rem',
            fontWeight: '500',
            transition: 'background-color 0.3s'
          }}
          onMouseOver={(e) => !loading && (e.target.style.backgroundColor = '#229954')}
          onMouseOut={(e) => !loading && (e.target.style.backgroundColor = '#27ae60')}
        >
          {loading ? 'Đang xử lý...' : (initialData ? 'Cập nhật' : 'Tạo mới')}
        </button>
      </div>
    </form>
  );
};

export default LoHangForm;
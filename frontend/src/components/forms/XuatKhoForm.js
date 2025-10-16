import React, { useState, useEffect } from 'react';
import { toast } from 'react-toastify';
import api from '../../services/api';

const XuatKhoForm = ({ initialData, onSubmit, onCancel }) => {
  const [formData, setFormData] = useState({
    khoId: '',
    khoaPhongYeuCauId: '',
    loaiXuat: 'XUAT_SU_DUNG',
    soPhieuYeuCau: '',
    ngayYeuCau: '',
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
  const [loading, setLoading] = useState(false);
  const [loadingData, setLoadingData] = useState(true);
  const [errors, setErrors] = useState({});

  useEffect(() => {
    fetchSelectData();
  }, []);

  useEffect(() => {
    if (initialData) {
      setFormData({
        ...initialData,
        ngayXuat: initialData.ngayXuat ? initialData.ngayXuat.split(' ')[0] : new Date().toISOString().split('T')[0],
        ngayYeuCau: initialData.ngayYeuCau ? initialData.ngayYeuCau.split(' ')[0] : '',
        chiTiet: initialData.chiTiet || []
      });
    }
  }, [initialData]);

  const fetchSelectData = async () => {
    setLoadingData(true);
    try {
      const [khoRes, khoaPhongRes, hangHoaRes] = await Promise.all([
        api.get('/api/kho/active').catch(() => ({ data: { data: [] } })),
        api.get('/api/khoa-phong/active').catch(() => ({ data: { data: [] } })),
        api.get('/api/hang-hoa?size=100').catch(() => ({ data: { data: { content: [] } } }))
      ]);

      setKhoList(khoRes.data.data || khoRes.data || []);
      setKhoaPhongList(khoaPhongRes.data.data || khoaPhongRes.data || []);
      setHangHoaList(hangHoaRes.data.data?.content || hangHoaRes.data?.content || []);
    } catch (error) {
      console.error('Error fetching select data:', error);
      toast.error('Lỗi khi tải dữ liệu');
    } finally {
      setLoadingData(false);
    }
  };

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({ ...prev, [name]: value }));
    if (errors[name]) {
      setErrors(prev => ({ ...prev, [name]: '' }));
    }
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

  const updateChiTiet = (index, field, value) => {
    setFormData(prev => {
      const newChiTiet = [...prev.chiTiet];
      newChiTiet[index][field] = value;

      // Tự động cập nhật thông tin khi chọn hàng hóa
      if (field === 'hangHoaId' && value) {
        const hangHoa = hangHoaList.find(h => h.id === parseInt(value));
        if (hangHoa) {
          newChiTiet[index].tonKhoHienTai = hangHoa.soLuongCoTheXuat || 0;
          newChiTiet[index].donGia = hangHoa.giaNhapTrungBinh || 0;
          
          // Tự động set số lượng xuất = số lượng yêu cầu nếu chưa có
          if (!newChiTiet[index].soLuongXuat && newChiTiet[index].soLuongYeuCau) {
            newChiTiet[index].soLuongXuat = newChiTiet[index].soLuongYeuCau;
          }
        }
      }

      // Tự động set số lượng xuất khi nhập số lượng yêu cầu
      if (field === 'soLuongYeuCau') {
        if (!newChiTiet[index].soLuongXuat) {
          newChiTiet[index].soLuongXuat = value;
        }
      }

      // Tính thành tiền
      if (field === 'soLuongXuat' || field === 'donGia') {
        const soLuong = parseFloat(newChiTiet[index].soLuongXuat || 0);
        const donGia = parseFloat(newChiTiet[index].donGia || 0);
        newChiTiet[index].thanhTien = soLuong * donGia;
      }

      return { ...prev, chiTiet: newChiTiet };
    });
  };

  const validateForm = () => {
    const newErrors = {};

    if (!formData.khoId) {
      newErrors.khoId = 'Vui lòng chọn kho';
      toast.error('⚠️ Vui lòng chọn kho');
      return false;
    }

    if (!formData.lyDoXuat || !formData.lyDoXuat.trim()) {
      newErrors.lyDoXuat = 'Vui lòng nhập lý do xuất';
      toast.error('⚠️ Vui lòng nhập lý do xuất');
      return false;
    }

    if (!formData.ngayXuat) {
      newErrors.ngayXuat = 'Vui lòng chọn ngày xuất';
      toast.error('⚠️ Vui lòng chọn ngày xuất');
      return false;
    }

    if (formData.chiTiet.length === 0) {
      newErrors.chiTiet = 'Vui lòng thêm ít nhất một hàng hóa';
      toast.error('⚠️ Vui lòng thêm ít nhất một hàng hóa');
      return false;
    }

    // Validate từng dòng chi tiết
    for (let i = 0; i < formData.chiTiet.length; i++) {
      const item = formData.chiTiet[i];
      const lineNumber = i + 1;

      if (!item.hangHoaId) {
        toast.error(`❌ Dòng ${lineNumber}: Chưa chọn hàng hóa`);
        return false;
      }

      if (!item.soLuongYeuCau || item.soLuongYeuCau <= 0) {
        toast.error(`❌ Dòng ${lineNumber}: Số lượng yêu cầu phải lớn hơn 0`);
        return false;
      }

      if (!item.soLuongXuat || item.soLuongXuat <= 0) {
        toast.error(`❌ Dòng ${lineNumber}: Số lượng xuất phải lớn hơn 0`);
        return false;
      }

      if (item.soLuongXuat > item.tonKhoHienTai) {
        const hangHoa = hangHoaList.find(h => h.id === parseInt(item.hangHoaId));
        toast.error(
          `❌ Dòng ${lineNumber} - ${hangHoa?.tenHangHoa}: ` +
          `Không đủ hàng! Yêu cầu ${item.soLuongXuat}, Tồn kho ${item.tonKhoHienTai}`,
          { autoClose: 5000 }
        );
        return false;
      }

      if (!item.donGia || item.donGia < 0) {
        toast.error(`❌ Dòng ${lineNumber}: Đơn giá không hợp lệ`);
        return false;
      }
    }

    setErrors(newErrors);
    return true;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    if (!validateForm()) return;

    setLoading(true);
    try {
      const submitData = {
        khoId: parseInt(formData.khoId),
        khoaPhongYeuCauId: formData.khoaPhongYeuCauId ? 
          parseInt(formData.khoaPhongYeuCauId) : null,
        loaiXuat: formData.loaiXuat,
        soPhieuYeuCau: formData.soPhieuYeuCau || null,
        ngayYeuCau: formData.ngayYeuCau ? `${formData.ngayYeuCau} 00:00:00` : null,
        ngayXuat: formData.ngayXuat ? `${formData.ngayXuat} 00:00:00` : null,
        nguoiYeuCau: formData.nguoiYeuCau || null,
        sdtNguoiYeuCau: formData.sdtNguoiYeuCau || null,
        nguoiNhan: formData.nguoiNhan || null,
        sdtNguoiNhan: formData.sdtNguoiNhan || null,
        diaChiGiao: formData.diaChiGiao || null,
        lyDoXuat: formData.lyDoXuat,
        ghiChu: formData.ghiChu || null,
        chiTiet: formData.chiTiet.map(item => ({
          hangHoaId: parseInt(item.hangHoaId),
          soLuongYeuCau: parseInt(item.soLuongYeuCau),
          soLuongXuat: parseInt(item.soLuongXuat),
          donGia: parseFloat(item.donGia),
          thanhTien: parseFloat(item.soLuongXuat) * parseFloat(item.donGia),
          ghiChu: item.ghiChu || null
        }))
      };

      await onSubmit(submitData);
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

  const formatCurrency = (value) => {
    if (!value) return '';
    return new Intl.NumberFormat('vi-VN', {
      style: 'currency',
      currency: 'VND'
    }).format(value);
  };

  if (loadingData) {
    return (
      <div style={{ textAlign: 'center', padding: '3rem' }}>
        <div style={{ fontSize: '1.2rem', color: '#7f8c8d' }}>Đang tải dữ liệu...</div>
      </div>
    );
  }

  return (
    <form onSubmit={handleSubmit}>
      {/* Thông tin cơ bản */}
      <div style={{ marginBottom: '2rem' }}>
        <h3 style={{
          fontSize: '1.125rem',
          fontWeight: '600',
          color: '#2c3e50',
          marginBottom: '1rem',
          paddingBottom: '0.5rem',
          borderBottom: '2px solid #e74c3c'
        }}>
          Thông tin cơ bản
        </h3>

        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem', marginBottom: '1rem' }}>
          <div>
            <label style={labelStyle}>
              Kho xuất <span style={{ color: '#e74c3c' }}>*</span>
            </label>
            <select
              name="khoId"
              value={formData.khoId}
              onChange={handleInputChange}
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
            <label style={labelStyle}>Khoa phòng yêu cầu</label>
            <select
              name="khoaPhongYeuCauId"
              value={formData.khoaPhongYeuCauId}
              onChange={handleInputChange}
              style={inputStyle}
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
              <option value="XUAT_BAN">Xuất bán</option>
              <option value="XUAT_TRA">Xuất trả</option>
              <option value="XUAT_HUY">Xuất hủy</option>
              <option value="XUAT_CHUYEN_KHO">Chuyển kho</option>
              <option value="XUAT_SUA_CHUA">Sửa chữa</option>
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
              placeholder="Mã phiếu yêu cầu (nếu có)"
              maxLength={50}
            />
          </div>

          <div>
            <label style={labelStyle}>
              Ngày xuất <span style={{ color: '#e74c3c' }}>*</span>
            </label>
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

        <div style={{ marginBottom: '1rem' }}>
          <label style={labelStyle}>
            Lý do xuất <span style={{ color: '#e74c3c' }}>*</span>
          </label>
          <textarea
            name="lyDoXuat"
            value={formData.lyDoXuat}
            onChange={handleInputChange}
            style={{ ...inputStyle, minHeight: '80px', resize: 'vertical' }}
            rows={3}
            placeholder="Nhập lý do xuất kho..."
            maxLength={500}
            required
          />
          <div style={{ marginTop: '0.25rem', fontSize: '0.85rem', color: '#7f8c8d', textAlign: 'right' }}>
            {formData.lyDoXuat.length}/500 ký tự
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
              placeholder="Tên người yêu cầu"
              maxLength={100}
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
              placeholder="Số điện thoại"
              maxLength={15}
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
              placeholder="Tên người nhận hàng"
              maxLength={100}
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
              placeholder="Số điện thoại"
              maxLength={15}
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
            placeholder="Địa chỉ giao hàng"
            maxLength={255}
          />
        </div>

        <div>
          <label style={labelStyle}>Ghi chú</label>
          <textarea
            name="ghiChu"
            value={formData.ghiChu}
            onChange={handleInputChange}
            style={{ ...inputStyle, minHeight: '80px', resize: 'vertical' }}
            rows={3}
            placeholder="Thông tin bổ sung..."
            maxLength={1000}
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
            disabled={!formData.khoId}
            style={{
              backgroundColor: !formData.khoId ? '#95a5a6' : '#27ae60',
              color: 'white',
              border: 'none',
              padding: '0.5rem 1rem',
              borderRadius: '0.5rem',
              cursor: !formData.khoId ? 'not-allowed' : 'pointer',
              fontWeight: '500'
            }}
            title={!formData.khoId ? 'Vui lòng chọn kho trước' : ''}
          >
            + Thêm hàng hóa
          </button>
        </div>

        {!formData.khoId && (
          <div style={{
            padding: '1rem',
            backgroundColor: '#fff3e0',
            borderRadius: '0.5rem',
            marginBottom: '1rem',
            color: '#e65100',
            textAlign: 'center'
          }}>
            Vui lòng chọn kho trước khi thêm hàng hóa
          </div>
        )}

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
            <table style={{ width: '100%', borderCollapse: 'collapse', minWidth: '1000px' }}>
              <thead>
                <tr style={{ backgroundColor: '#f8f9fa' }}>
                  <th style={{ padding: '0.75rem', border: '1px solid #dee2e6', textAlign: 'left', fontSize: '0.875rem', fontWeight: '600' }}>Hàng hóa</th>
                  <th style={{ padding: '0.75rem', border: '1px solid #dee2e6', textAlign: 'center', fontSize: '0.875rem', fontWeight: '600' }}>Tồn kho</th>
                  <th style={{ padding: '0.75rem', border: '1px solid #dee2e6', textAlign: 'center', fontSize: '0.875rem', fontWeight: '600' }}>SL yêu cầu</th>
                  <th style={{ padding: '0.75rem', border: '1px solid #dee2e6', textAlign: 'center', fontSize: '0.875rem', fontWeight: '600' }}>SL xuất</th>
                  <th style={{ padding: '0.75rem', border: '1px solid #dee2e6', textAlign: 'right', fontSize: '0.875rem', fontWeight: '600' }}>Đơn giá</th>
                  <th style={{ padding: '0.75rem', border: '1px solid #dee2e6', textAlign: 'right', fontSize: '0.875rem', fontWeight: '600' }}>Thành tiền</th>
                  <th style={{ padding: '0.75rem', border: '1px solid #dee2e6', textAlign: 'center', fontSize: '0.875rem', fontWeight: '600' }}>Xóa</th>
                </tr>
              </thead>
              <tbody>
                {formData.chiTiet.map((item, index) => (
                  <tr key={index} style={{ backgroundColor: 'white' }}>
                    <td style={{ padding: '0.5rem', border: '1px solid #dee2e6', minWidth: '250px' }}>
  <div style={{ display: 'flex', gap: '0.75rem', alignItems: 'center' }}>
    {/* ✅ THÊM: Hình ảnh hàng hóa */}
    {item.hangHoaId && (() => {
      const selectedHH = hangHoaList.find(h => h.id === parseInt(item.hangHoaId));
      return selectedHH?.hinhAnhUrl ? (
        <img
          src={getImageUrl(selectedHH.hinhAnhUrl)}
          alt={selectedHH.tenHangHoa}
          style={{
            width: '50px',
            height: '50px',
            objectFit: 'cover',
            borderRadius: '6px',
            border: '2px solid #e0e0e0',
            flexShrink: 0
          }}
          onError={(e) => {
            e.target.onerror = null;
            e.target.src = 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iNTAiIGhlaWdodD0iNTAiIHhtbG5zPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwL3N2ZyI+PHJlY3Qgd2lkdGg9IjUwIiBoZWlnaHQ9IjUwIiBmaWxsPSIjZjBmMGYwIi8+PHRleHQgeD0iNTAlIiB5PSI1MCUiIGZvbnQtZmFtaWx5PSJBcmlhbCIgZm9udC1zaXplPSI4IiBmaWxsPSIjOTk5IiB0ZXh0LWFuY2hvcj0ibWlkZGxlIiBkeT0iLjNlbSI+Ti9BPC90ZXh0Pjwvc3ZnPg==';
          }}
        />
      ) : (
        <div style={{
          width: '50px',
          height: '50px',
          backgroundColor: '#f5f5f5',
          borderRadius: '6px',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          fontSize: '1.2rem',
          flexShrink: 0
        }}>
          📦
        </div>
      );
    })()}

    {/* Select hàng hóa */}
    <div style={{ flex: 1 }}>
      <select
        value={item.hangHoaId}
        onChange={(e) => updateChiTiet(index, 'hangHoaId', e.target.value)}
        style={{
          width: '100%',
          padding: '0.5rem',
          border: !item.hangHoaId ? '2px solid #e74c3c' : '1px solid #ddd',
          borderRadius: '0.375rem',
          fontSize: '0.875rem',
          backgroundColor: !item.hangHoaId ? '#fff5f5' : 'white'
        }}
      >
        <option value="">-- Chọn hàng hóa --</option>
        {hangHoaList.map(hh => (
          <option key={hh.id} value={hh.id}>
            {hh.tenHangHoa} (Tồn: {hh.soLuongCoTheXuat || 0})
          </option>
        ))}
      </select>
    </div>
  </div>
</td>

                    <td style={{ padding: '0.5rem', border: '1px solid #dee2e6', textAlign: 'center' }}>
                      <span style={{
                        fontWeight: 'bold',
                        fontSize: '1.1rem',
                        color: item.tonKhoHienTai === 0 ? '#e74c3c' : 
                               item.tonKhoHienTai < 10 ? '#f39c12' : '#27ae60'
                      }}>
                        {item.tonKhoHienTai || 0}
                      </span>
                    </td>

                    <td style={{ padding: '0.5rem', border: '1px solid #dee2e6' }}>
                      <input
                        type="number"
                        value={item.soLuongYeuCau}
                        onChange={(e) => updateChiTiet(index, 'soLuongYeuCau', e.target.value)}
                        style={{
                          width: '80px',
                          padding: '0.5rem',
                          border: !item.soLuongYeuCau || item.soLuongYeuCau <= 0 ? '2px solid #e74c3c' : '1px solid #ddd',
                          borderRadius: '0.375rem',
                          textAlign: 'center',
                          fontSize: '0.875rem',
                          backgroundColor: !item.soLuongYeuCau || item.soLuongYeuCau <= 0 ? '#fff5f5' : 'white'
                        }}
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
                          border: !item.soLuongXuat || item.soLuongXuat <= 0 || item.soLuongXuat > item.tonKhoHienTai ? 
                                 '2px solid #e74c3c' : '1px solid #ddd',
                          borderRadius: '0.375rem',
                          textAlign: 'center',
                          fontSize: '0.875rem',
                          fontWeight: '600',
                          backgroundColor: !item.soLuongXuat || item.soLuongXuat <= 0 || item.soLuongXuat > item.tonKhoHienTai ?
                                          '#fff5f5' : 'white'
                        }}
                        min="1"
                        max={item.tonKhoHienTai}
                      />
                      {item.soLuongXuat > item.tonKhoHienTai && (
                        <div style={{
                          fontSize: '0.7rem',
                          color: '#e74c3c',
                          marginTop: '0.25rem',
                          fontWeight: '700'
                        }}>
                          ⚠️ Vượt tồn kho!
                        </div>
                      )}
                    </td>

                    <td style={{ padding: '0.5rem', border: '1px solid #dee2e6' }}>
                      <input
                        type="number"
                        value={item.donGia}
                        onChange={(e) => updateChiTiet(index, 'donGia', e.target.value)}
                        style={{
                          width: '120px',
                          padding: '0.5rem',
                          border: '1px solid #ddd',
                          borderRadius: '0.375rem',
                          textAlign: 'right',
                          fontSize: '0.875rem'
                        }}
                        min="0"
                        step="1000"
                      />
                    </td>

                    <td style={{
                      padding: '0.5rem',
                      border: '1px solid #dee2e6',
                      textAlign: 'right',
                      fontWeight: '600',
                      color: '#2c3e50',
                      fontSize: '0.875rem'
                    }}>
                      {formatCurrency(item.thanhTien || 0)}
                    </td>

                    <td style={{ padding: '0.5rem', border: '1px solid #dee2e6', textAlign: 'center' }}>
                      <button
                        type="button"
                        onClick={() => removeChiTietRow(index)}
                        style={{
                          backgroundColor: '#e74c3c',
                          color: 'white',
                          border: 'none',
                          padding: '0.5rem 0.75rem',
                          borderRadius: '0.375rem',
                          cursor: 'pointer',
                          fontSize: '0.875rem',
                          fontWeight: '600'
                        }}
                        title="Xóa dòng này"
                      >
                        🗑️ Xóa
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
            </div>
        )}

        {formData.chiTiet.length > 0 && (
          <div style={{
            marginTop: '1rem',
            padding: '1rem 1.5rem',
            background: 'linear-gradient(135deg, #ffeaa7 0%, #fab1a0 100%)',
            borderRadius: '0.5rem',
            border: '1px solid #fdcb6e'
          }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '1rem' }}>
              <div>
                <p style={{ color: '#2d3436', fontSize: '0.875rem', margin: 0 }}>
                  Tổng số mặt hàng: <strong style={{ color: '#2c3e50' }}>{formData.chiTiet.length}</strong>
                </p>
                <p style={{ color: '#2d3436', fontSize: '0.875rem', margin: '0.25rem 0 0 0' }}>
                  Tổng số lượng xuất: <strong style={{ color: '#2c3e50' }}>
                    {formData.chiTiet.reduce((sum, item) => sum + (parseInt(item.soLuongXuat) || 0), 0)}
                  </strong>
                </p>
              </div>
              <div style={{ textAlign: 'right' }}>
                <p style={{ color: '#2d3436', fontSize: '0.875rem', margin: 0 }}>
                  Tổng giá trị:
                </p>
                <p style={{ fontSize: '1.5rem', fontWeight: 'bold', color: '#d63031', margin: 0 }}>
                  {formatCurrency(formData.chiTiet.reduce((sum, item) => sum + (item.thanhTien || 0), 0))}
                </p>
              </div>
            </div>
          </div>
        )}
      </div>

      {/* Nút hành động */}
      <div style={{
        display: 'flex',
        justifyContent: 'flex-end',
        gap: '1rem',
        borderTop: '1px solid #dee2e6',
        paddingTop: '1.5rem'
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
            backgroundColor: loading ? '#95a5a6' : '#e74c3c',
            color: 'white',
            border: 'none',
            borderRadius: '0.5rem',
            cursor: loading ? 'not-allowed' : 'pointer',
            fontWeight: '500',
            fontSize: '1rem'
          }}
        >
          {loading ? 'Đang xử lý...' : (initialData ? 'Cập nhật' : 'Tạo Phiếu Xuất')}
        </button>
      </div>
    </form>
  );
};

export default XuatKhoForm;
import React, { useState, useEffect } from 'react';
import { toast } from 'react-toastify';
import api, { getImageUrl } from '../../services/api';


const NhapKhoForm = ({ initialData, onSubmit, onCancel }) => {
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
  const [viTriKhoList, setViTriKhoList] = useState([]);
  const [loading, setLoading] = useState(false);
  const [loadingData, setLoadingData] = useState(true);
  const [errors, setErrors] = useState({});

  const [hangHoaDetails, setHangHoaDetails] = useState({});


  useEffect(() => {
    fetchSelectData();
  }, []);

  useEffect(() => {
    if (initialData) {
      setFormData({
        ...initialData,
        ngayNhap: initialData.ngayNhap ? initialData.ngayNhap.split(' ')[0] : new Date().toISOString().split('T')[0],
        ngayHoaDon: initialData.ngayHoaDon ? initialData.ngayHoaDon.split(' ')[0] : '',
        chiTiet: initialData.chiTiet || []
      });
    }
  }, [initialData]);

  useEffect(() => {
    if (formData.khoId) {
      fetchViTriKho(formData.khoId);
    } else {
      setViTriKhoList([]);
    }
  }, [formData.khoId]);


  useEffect(() => {
    if (hangHoaList.length > 0) {
      const details = {};
      hangHoaList.forEach(hh => {
        details[hh.id] = {
          coQuanLyLo: hh.coQuanLyLo,
          coHanSuDung: hh.coHanSuDung,
          tenHangHoa: hh.tenHangHoa,
          maHangHoa: hh.maHangHoa
        };
      });
      setHangHoaDetails(details);
    }
  }, [hangHoaList]);

  const fetchSelectData = async () => {
    setLoadingData(true);
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
      toast.error('Lỗi khi tải dữ liệu');
    } finally {
      setLoadingData(false);
    }
  };

  const fetchViTriKho = async (khoId) => {
    try {
      const response = await api.get(`/api/vi-tri-kho/trong?khoId=${khoId}`);
      const data = response.data.data || response.data;
      setViTriKhoList(Array.isArray(data) ? data : []);
    } catch (error) {
      console.error('Error fetching vi tri kho:', error);
      toast.error('Lỗi khi tải danh sách vị trí kho');
      setViTriKhoList([]);
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
        viTriKhoId: '',
        soLuong: '',
        donGia: '',
        thanhTien: 0,
        ngaySanXuat: '',
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

  // SỬA HÀM validateForm
  const validateForm = () => {
    const newErrors = {};

    if (!formData.khoId) {
      newErrors.khoId = 'Vui lòng chọn kho';
      toast.error('⚠️ Vui lòng chọn kho');
      return false;
    }

    if (!formData.ngayNhap) {
      newErrors.ngayNhap = 'Vui lòng chọn ngày nhập';
      toast.error('⚠️ Vui lòng chọn ngày nhập');
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

      // Kiểm tra trường bắt buộc
      if (!item.hangHoaId) {
        toast.error(`❌ Dòng ${lineNumber}: Chưa chọn hàng hóa`);
        return false;
      }

      if (!item.viTriKhoId) {
        toast.error(`❌ Dòng ${lineNumber}: Chưa chọn vị trí kho`);
        return false;
      }

      if (!item.soLuong || item.soLuong <= 0) {
        toast.error(`❌ Dòng ${lineNumber}: Số lượng phải lớn hơn 0`);
        return false;
      }

      if (!item.donGia || item.donGia < 0) {
        toast.error(`❌ Dòng ${lineNumber}: Đơn giá không hợp lệ`);
        return false;
      }

      // Kiểm tra yêu cầu lô hàng
      const hangHoaInfo = hangHoaDetails[item.hangHoaId];

      if (hangHoaInfo && hangHoaInfo.coQuanLyLo) {
        if (!item.soLo || !item.soLo.trim()) {
          toast.error(
            `❌ Dòng ${lineNumber} - ${hangHoaInfo.tenHangHoa}: YÊU CẦU nhập số lô`,
            { autoClose: 5000 }
          );
          return false;
        }

        if (hangHoaInfo.coHanSuDung && !item.hanSuDung) {
          toast.error(
            `❌ Dòng ${lineNumber} - ${hangHoaInfo.tenHangHoa}: YÊU CẦU nhập hạn sử dụng`,
            { autoClose: 5000 }
          );
          return false;
        }
      }

      // Validate ngày tháng
      if (item.ngaySanXuat && item.hanSuDung) {
        if (new Date(item.hanSuDung) < new Date(item.ngaySanXuat)) {
          toast.error(`❌ Dòng ${lineNumber}: Hạn sử dụng phải sau ngày sản xuất`);
          return false;
        }
      }

      if (item.hanSuDung && new Date(item.hanSuDung) < new Date()) {
        toast.warning(
          `⚠️ Dòng ${lineNumber}: Hàng hóa đã HẾT HẠN. Bạn có chắc muốn nhập?`,
          { autoClose: 8000 }
        );
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
        nhaCungCapId: formData.nhaCungCapId ? parseInt(formData.nhaCungCapId) : null,
        loaiNhap: formData.loaiNhap,
        soHoaDon: formData.soHoaDon || null,
        ngayHoaDon: formData.ngayHoaDon ? `${formData.ngayHoaDon} 00:00:00` : null,
        ngayNhap: formData.ngayNhap ? `${formData.ngayNhap} 00:00:00` : null,
        nguoiGiao: formData.nguoiGiao || null,
        sdtNguoiGiao: formData.sdtNguoiGiao || null,
        ghiChu: formData.ghiChu || null,
        chiTiet: formData.chiTiet.map(item => ({
          hangHoaId: parseInt(item.hangHoaId),
          viTriKhoId: parseInt(item.viTriKhoId),
          soLuong: parseInt(item.soLuong),
          donGia: parseFloat(item.donGia),
          thanhTien: parseFloat(item.soLuong) * parseFloat(item.donGia),
          ngaySanXuat: item.ngaySanXuat || null,
          hanSuDung: item.hanSuDung || null,
          soLo: item.soLo || null,
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

  if (loadingData) {
    return (
      <div style={{ textAlign: 'center', padding: '3rem' }}>
        <div style={{ fontSize: '1.2rem', color: '#7f8c8d' }}>Đang tải dữ liệu...</div>
      </div>
    );
  }

  return (
    <form onSubmit={handleSubmit}>
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
              disabled={!!initialData}
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
              maxLength={50}
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
              maxLength={100}
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
              maxLength={15}
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
            maxLength={500}
          />
        </div>
      </div>

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
            <table style={{ width: '100%', borderCollapse: 'collapse', minWidth: '1100px' }}>
              <thead>
                <tr style={{ backgroundColor: '#f8f9fa' }}>
                  <th style={{ padding: '0.75rem', border: '1px solid #dee2e6', textAlign: 'left', fontSize: '0.875rem', fontWeight: '600' }}>Hàng hóa</th>
                  <th style={{ padding: '0.75rem', border: '1px solid #dee2e6', textAlign: 'left', fontSize: '0.875rem', fontWeight: '600' }}>Vị trí kho</th>
                  <th style={{ padding: '0.75rem', border: '1px solid #dee2e6', textAlign: 'center', fontSize: '0.875rem', fontWeight: '600' }}>SL</th>
                  <th style={{ padding: '0.75rem', border: '1px solid #dee2e6', textAlign: 'right', fontSize: '0.875rem', fontWeight: '600' }}>Đơn giá</th>
                  <th style={{ padding: '0.75rem', border: '1px solid #dee2e6', textAlign: 'right', fontSize: '0.875rem', fontWeight: '600' }}>Thành tiền</th>
                  <th style={{ padding: '0.75rem', border: '1px solid #dee2e6', textAlign: 'center', fontSize: '0.875rem', fontWeight: '600' }}>HSD</th>
                  <th style={{ padding: '0.75rem', border: '1px solid #dee2e6', textAlign: 'center', fontSize: '0.875rem', fontWeight: '600' }}>Số lô</th>
                  <th style={{ padding: '0.75rem', border: '1px solid #dee2e6', textAlign: 'center', fontSize: '0.875rem', fontWeight: '600' }}>Xóa</th>
                </tr>
              </thead>
              <tbody>
                {formData.chiTiet.map((item, index) => {
                  const hangHoaInfo = hangHoaDetails[item.hangHoaId];
                  const requiresLot = hangHoaInfo?.coQuanLyLo;
                  const requiresExpiry = hangHoaInfo?.coHanSuDung;

                  return (
                    <tr
                      key={index}
                      style={{
                        backgroundColor: 'white',
                        borderLeft: requiresLot ? '4px solid #f39c12' : 'none'
                      }}
                    >
                      {/* Cell Hàng hóa - CÓ ẢNH */}
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
                                  {hh.tenHangHoa}
                                  {hh.coQuanLyLo && ' 📦'}
                                  {hh.coHanSuDung && ' 📅'}
                                </option>
                              ))}
                            </select>
                            {requiresLot && (
                              <div style={{
                                fontSize: '0.7rem',
                                color: '#f39c12',
                                marginTop: '0.25rem',
                                fontWeight: '600',
                                display: 'flex',
                                alignItems: 'center',
                                gap: '0.25rem'
                              }}>
                                <span>⚠️</span>
                                <span>YÊU CẦU quản lý lô</span>
                              </div>
                            )}
                          </div>
                        </div>
                      </td>

                      {/* Cell Vị trí kho */}
                      <td style={{ padding: '0.5rem', border: '1px solid #dee2e6', minWidth: '180px' }}>
                        <select
                          value={item.viTriKhoId}
                          onChange={(e) => updateChiTiet(index, 'viTriKhoId', e.target.value)}
                          style={{
                            width: '100%',
                            padding: '0.5rem',
                            border: !item.viTriKhoId ? '2px solid #e74c3c' : '1px solid #ddd',
                            borderRadius: '0.375rem',
                            fontSize: '0.875rem',
                            backgroundColor: !item.viTriKhoId ? '#fff5f5' : 'white'
                          }}
                          disabled={viTriKhoList.length === 0}
                        >
                          <option value="">-- Chọn vị trí --</option>
                          {viTriKhoList.map(vt => (
                            <option key={vt.id} value={vt.id}>
                              {vt.tenViTri}
                              {vt.sucChuaToiDa && ` (${vt.soLuongHienTai || 0}/${vt.sucChuaToiDa})`}
                            </option>
                          ))}
                        </select>
                      </td>

                      {/* Cell Số lượng */}
                      <td style={{ padding: '0.5rem', border: '1px solid #dee2e6' }}>
                        <input
                          type="number"
                          value={item.soLuong}
                          onChange={(e) => updateChiTiet(index, 'soLuong', e.target.value)}
                          style={{
                            width: '80px',
                            padding: '0.5rem',
                            border: !item.soLuong || item.soLuong <= 0 ? '2px solid #e74c3c' : '1px solid #ddd',
                            borderRadius: '0.375rem',
                            textAlign: 'center',
                            fontSize: '0.875rem',
                            backgroundColor: !item.soLuong || item.soLuong <= 0 ? '#fff5f5' : 'white'
                          }}
                          min="1"
                        />
                      </td>

                      {/* Cell Đơn giá */}
                      <td style={{ padding: '0.5rem', border: '1px solid #dee2e6' }}>
                        <input
                          type="number"
                          value={item.donGia}
                          onChange={(e) => updateChiTiet(index, 'donGia', e.target.value)}
                          style={{
                            width: '120px',
                            padding: '0.5rem',
                            border: !item.donGia || item.donGia < 0 ? '2px solid #e74c3c' : '1px solid #ddd',
                            borderRadius: '0.375rem',
                            textAlign: 'right',
                            fontSize: '0.875rem',
                            backgroundColor: !item.donGia || item.donGia < 0 ? '#fff5f5' : 'white'
                          }}
                          min="0"
                          step="any"
                        />
                      </td>

                      {/* Cell Thành tiền */}
                      <td style={{
                        padding: '0.5rem',
                        border: '1px solid #dee2e6',
                        textAlign: 'right',
                        fontWeight: '600',
                        color: '#2c3e50',
                        fontSize: '0.875rem'
                      }}>
                        {new Intl.NumberFormat('vi-VN').format(item.thanhTien || 0)}₫
                      </td>

                      {/* Cell Hạn sử dụng */}
                      <td style={{ padding: '0.5rem', border: '1px solid #dee2e6' }}>
                        <input
                          type="date"
                          value={item.hanSuDung}
                          onChange={(e) => updateChiTiet(index, 'hanSuDung', e.target.value)}
                          style={{
                            width: '140px',
                            padding: '0.5rem',
                            border: requiresExpiry && !item.hanSuDung ? '2px solid #e74c3c' : '1px solid #ddd',
                            borderRadius: '0.375rem',
                            fontSize: '0.875rem',
                            backgroundColor: requiresExpiry && !item.hanSuDung ? '#fff5f5' : 'white'
                          }}
                          min={item.ngaySanXuat || new Date().toISOString().split('T')[0]}
                        />
                        {requiresExpiry && !item.hanSuDung && (
                          <div style={{
                            fontSize: '0.7rem',
                            color: '#e74c3c',
                            marginTop: '0.25rem',
                            fontWeight: '700'
                          }}>
                            ⚠️ BẮT BUỘC
                          </div>
                        )}
                      </td>

                      {/* Cell Số lô */}
                      <td style={{ padding: '0.5rem', border: '1px solid #dee2e6' }}>
                        <input
                          type="text"
                          value={item.soLo}
                          onChange={(e) => updateChiTiet(index, 'soLo', e.target.value)}
                          style={{
                            width: '100px',
                            padding: '0.5rem',
                            border: requiresLot && !item.soLo ? '2px solid #e74c3c' : '1px solid #ddd',
                            borderRadius: '0.375rem',
                            fontSize: '0.875rem',
                            backgroundColor: requiresLot && !item.soLo ? '#fff5f5' : 'white'
                          }}
                          placeholder={requiresLot ? 'BẮT BUỘC *' : 'Số lô'}
                          maxLength={50}
                        />
                        {requiresLot && !item.soLo && (
                          <div style={{
                            fontSize: '0.7rem',
                            color: '#e74c3c',
                            marginTop: '0.25rem',
                            fontWeight: '700'
                          }}>
                            ⚠️ BẮT BUỘC
                          </div>
                        )}
                      </td>

                      {/* Cell Xóa */}
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
                  );
                })}
              </tbody>
            </table>
          </div>
        )}

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
                <p style={{ color: '#546e7a', fontSize: '0.875rem', margin: 0 }}>
                  Tổng số mặt hàng: <strong style={{ color: '#2c3e50' }}>{formData.chiTiet.length}</strong>
                </p>
              </div>
              <div style={{ textAlign: 'right' }}>
                <p style={{ color: '#546e7a', fontSize: '0.875rem', margin: 0 }}>
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
            backgroundColor: loading ? '#95a5a6' : '#27ae60',
            color: 'white',
            border: 'none',
            borderRadius: '0.5rem',
            cursor: loading ? 'not-allowed' : 'pointer',
            fontWeight: '500',
            fontSize: '1rem'
          }}
        >
          {loading ? 'Đang xử lý...' : (initialData ? 'Cập nhật' : 'Tạo Phiếu Nhập')}
        </button>
      </div>
    </form>
  );
};

export default NhapKhoForm;
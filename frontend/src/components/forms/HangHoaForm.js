import React, { useState, useEffect } from 'react';
import api from '../../services/api';

const HangHoaForm = ({ initialData, onSubmit, onCancel }) => {
  const [formData, setFormData] = useState({
    maHangHoa: '',
    tenHangHoa: '',
    tenKhoaHoc: '',
    danhMucId: '',
    donViTinhId: '',
    nhaCungCapId: '',
    moTa: '',
    thanhPhan: '',
    congDung: '',
    cachSuDung: '',
    lieuLuong: '',
    dongGoi: '',
    xuatXu: '',
    hangSanXuat: '',
    soDangKy: '',
    soLuongToiThieu: '',
    soLuongToiDa: '',
    trongLuong: '',
    kichThuoc: '',
    mauSac: '',
    yeuCauBaoQuan: '',
    nhietDoBaoQuanMin: '',
    nhietDoBaoQuanMax: '',
    doAmBaoQuan: '',
    hanSuDungMacDinh: '',
    canhBaoHetHan: 30,
    coQuanLyLo: false,
    coHanSuDung: true,
    coKiemSoatChatLuong: false,
    laThuocDoc: false,
    laThuocHuongThan: false,
    trangThai: 'HOAT_DONG'
  });

  const [danhMucList, setDanhMucList] = useState([]);
  const [donViTinhList, setDonViTinhList] = useState([]);
  const [nhaCungCapList, setNhaCungCapList] = useState([]);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (initialData) {
      setFormData(initialData);
    }
    fetchSelectData();
  }, [initialData]);

  const fetchSelectData = async () => {
    try {
      const [danhMucRes, donViTinhRes, nhaCungCapRes] = await Promise.all([
        api.get('/api/danh-muc/root'),
        api.get('/api/don-vi-tinh/all'),
        api.get('/api/nha-cung-cap/active')
      ]);

      setDanhMucList(danhMucRes.data.data || []);
      setDonViTinhList(donViTinhRes.data.data || []);
      setNhaCungCapList(nhaCungCapRes.data.data || []);
    } catch (error) {
      console.error('Error fetching select data:', error);
    }
  };

  const handleChange = (e) => {
    const { name, value, type, checked } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: type === 'checkbox' ? checked : value
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
      {/* Thông tin cơ bản */}
      <div style={{ marginBottom: '2rem' }}>
        <h4 style={{ marginBottom: '1rem', color: '#2c3e50', borderBottom: '2px solid #3498db', paddingBottom: '0.5rem' }}>
          Thông tin cơ bản
        </h4>
        
        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem', marginBottom: '1rem' }}>
          <div>
            <label style={labelStyle}>Mã Hàng Hóa *</label>
            <input
              type="text"
              name="maHangHoa"
              value={formData.maHangHoa}
              onChange={handleChange}
              style={inputStyle}
              required
              maxLength={30}
            />
          </div>
          <div>
            <label style={labelStyle}>Tên Hàng Hóa *</label>
            <input
              type="text"
              name="tenHangHoa"
              value={formData.tenHangHoa}
              onChange={handleChange}
              style={inputStyle}
              required
              maxLength={200}
            />
          </div>
        </div>

        <div style={{ marginBottom: '1rem' }}>
          <label style={labelStyle}>Tên Khoa Học</label>
          <input
            type="text"
            name="tenKhoaHoc"
            value={formData.tenKhoaHoc}
            onChange={handleChange}
            style={inputStyle}
            maxLength={200}
          />
        </div>

        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr 1fr', gap: '1rem', marginBottom: '1rem' }}>
          <div>
            <label style={labelStyle}>Danh Mục *</label>
            <select
              name="danhMucId"
              value={formData.danhMucId}
              onChange={handleChange}
              style={inputStyle}
              required
            >
              <option value="">-- Chọn danh mục --</option>
              {danhMucList.map(dm => (
                <option key={dm.id} value={dm.id}>{dm.tenDanhMuc}</option>
              ))}
            </select>
          </div>
          <div>
            <label style={labelStyle}>Đơn Vị Tính *</label>
            <select
              name="donViTinhId"
              value={formData.donViTinhId}
              onChange={handleChange}
              style={inputStyle}
              required
            >
              <option value="">-- Chọn đơn vị tính --</option>
              {donViTinhList.map(dvt => (
                <option key={dvt.id} value={dvt.id}>{dvt.tenDvt}</option>
              ))}
            </select>
          </div>
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
                <option key={ncc.id} value={ncc.id}>{ncc.tenNcc}</option>
              ))}
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
      </div>

      {/* Thông tin chi tiết */}
      <div style={{ marginBottom: '2rem' }}>
        <h4 style={{ marginBottom: '1rem', color: '#2c3e50', borderBottom: '2px solid #e74c3c', paddingBottom: '0.5rem' }}>
          Thông tin chi tiết
        </h4>
        
        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem', marginBottom: '1rem' }}>
          <div>
            <label style={labelStyle}>Thành Phần</label>
            <textarea
              name="thanhPhan"
              value={formData.thanhPhan}
              onChange={handleChange}
              style={{ ...inputStyle, minHeight: '60px' }}
              rows={2}
            />
          </div>
          <div>
            <label style={labelStyle}>Công Dụng</label>
            <textarea
              name="congDung"
              value={formData.congDung}
              onChange={handleChange}
              style={{ ...inputStyle, minHeight: '60px' }}
              rows={2}
            />
          </div>
        </div>

        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem', marginBottom: '1rem' }}>
          <div>
            <label style={labelStyle}>Cách Sử Dụng</label>
            <input
              type="text"
              name="cachSuDung"
              value={formData.cachSuDung}
              onChange={handleChange}
              style={inputStyle}
            />
          </div>
          <div>
            <label style={labelStyle}>Liều Lượng</label>
            <input
              type="text"
              name="lieuLuong"
              value={formData.lieuLuong}
              onChange={handleChange}
              style={inputStyle}
            />
          </div>
        </div>

        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr 1fr', gap: '1rem', marginBottom: '1rem' }}>
          <div>
            <label style={labelStyle}>Đóng Gói</label>
            <input
              type="text"
              name="dongGoi"
              value={formData.dongGoi}
              onChange={handleChange}
              style={inputStyle}
            />
          </div>
          <div>
            <label style={labelStyle}>Xuất Xứ</label>
            <input
              type="text"
              name="xuatXu"
              value={formData.xuatXu}
              onChange={handleChange}
              style={inputStyle}
            />
          </div>
          <div>
            <label style={labelStyle}>Hãng Sản Xuất</label>
            <input
              type="text"
              name="hangSanXuat"
              value={formData.hangSanXuat}
              onChange={handleChange}
              style={inputStyle}
            />
          </div>
        </div>
      </div>

      {/* Thông tin kho bãi */}
      <div style={{ marginBottom: '2rem' }}>
        <h4 style={{ marginBottom: '1rem', color: '#2c3e50', borderBottom: '2px solid #f39c12', paddingBottom: '0.5rem' }}>
          Thông tin kho bãi
        </h4>
        
        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem', marginBottom: '1rem' }}>
          <div>
            <label style={labelStyle}>Số Lượng Tối Thiểu</label>
            <input
              type="number"
              name="soLuongToiThieu"
              value={formData.soLuongToiThieu}
              onChange={handleChange}
              style={inputStyle}
              min="0"
            />
          </div>
          <div>
            <label style={labelStyle}>Số Lượng Tối Đa</label>
            <input
              type="number"
              name="soLuongToiDa"
              value={formData.soLuongToiDa}
              onChange={handleChange}
              style={inputStyle}
              min="0"
            />
          </div>
        </div>

        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr 1fr', gap: '1rem', marginBottom: '1rem' }}>
          <div>
            <label style={labelStyle}>Trọng Lượng (kg)</label>
            <input
              type="number"
              name="trongLuong"
              value={formData.trongLuong}
              onChange={handleChange}
              style={inputStyle}
              step="0.001"
            />
          </div>
          <div>
            <label style={labelStyle}>Kích Thước</label>
            <input
              type="text"
              name="kichThuoc"
              value={formData.kichThuoc}
              onChange={handleChange}
              style={inputStyle}
            />
          </div>
          <div>
            <label style={labelStyle}>Màu Sắc</label>
            <input
              type="text"
              name="mauSac"
              value={formData.mauSac}
              onChange={handleChange}
              style={inputStyle}
            />
          </div>
        </div>

        <div style={{ marginBottom: '1rem' }}>
          <label style={labelStyle}>Yêu Cầu Bảo Quản</label>
          <textarea
            name="yeuCauBaoQuan"
            value={formData.yeuCauBaoQuan}
            onChange={handleChange}
            style={{ ...inputStyle, minHeight: '60px' }}
            rows={2}
          />
        </div>

        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr 1fr', gap: '1rem', marginBottom: '1rem' }}>
          <div>
            <label style={labelStyle}>Nhiệt Độ Min (°C)</label>
            <input
              type="number"
              name="nhietDoBaoQuanMin"
              value={formData.nhietDoBaoQuanMin}
              onChange={handleChange}
              style={inputStyle}
              step="0.1"
            />
          </div>
          <div>
            <label style={labelStyle}>Nhiệt Độ Max (°C)</label>
            <input
              type="number"
              name="nhietDoBaoQuanMax"
              value={formData.nhietDoBaoQuanMax}
              onChange={handleChange}
              style={inputStyle}
              step="0.1"
            />
          </div>
          <div>
            <label style={labelStyle}>Độ Ẩm Bảo Quản (%)</label>
            <input
              type="text"
              name="doAmBaoQuan"
              value={formData.doAmBaoQuan}
              onChange={handleChange}
              style={inputStyle}
            />
          </div>
        </div>

        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem', marginBottom: '1rem' }}>
          <div>
            <label style={labelStyle}>Hạn Sử Dụng Mặc Định (ngày)</label>
            <input
              type="number"
              name="hanSuDungMacDinh"
              value={formData.hanSuDungMacDinh}
              onChange={handleChange}
              style={inputStyle}
              min="1"
            />
          </div>
          <div>
            <label style={labelStyle}>Cảnh Báo Hết Hạn (ngày)</label>
            <input
              type="number"
              name="canhBaoHetHan"
              value={formData.canhBaoHetHan}
              onChange={handleChange}
              style={inputStyle}
              min="1"
            />
          </div>
        </div>
      </div>

      {/* Cấu hình */}
      <div style={{ marginBottom: '2rem' }}>
        <h4 style={{ marginBottom: '1rem', color: '#2c3e50', borderBottom: '2px solid #9b59b6', paddingBottom: '0.5rem' }}>
          Cấu hình
        </h4>
        
        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '2rem' }}>
          <div>
            <label style={{ ...labelStyle, display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
              <input
                type="checkbox"
                name="coQuanLyLo"
                checked={formData.coQuanLyLo}
                onChange={handleChange}
              />
              Có quản lý lô
            </label>
            
            <label style={{ ...labelStyle, display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
              <input
                type="checkbox"
                name="coHanSuDung"
                checked={formData.coHanSuDung}
                onChange={handleChange}
              />
              Có hạn sử dụng
            </label>
            
            <label style={{ ...labelStyle, display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
              <input
                type="checkbox"
                name="coKiemSoatChatLuong"
                checked={formData.coKiemSoatChatLuong}
                onChange={handleChange}
              />
              Có kiểm soát chất lượng
            </label>
          </div>
          
          <div>
            <label style={{ ...labelStyle, display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
              <input
                type="checkbox"
                name="laThuocDoc"
                checked={formData.laThuocDoc}
                onChange={handleChange}
              />
              Là thuốc độc
            </label>
            
            <label style={{ ...labelStyle, display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
              <input
                type="checkbox"
                name="laThuocHuongThan"
                checked={formData.laThuocHuongThan}
                onChange={handleChange}
              />
              Là thuốc hướng thần
            </label>
            
            <div>
              <label style={labelStyle}>Trạng Thái</label>
              <select
                name="trangThai"
                value={formData.trangThai}
                onChange={handleChange}
                style={inputStyle}
              >
                <option value="HOAT_DONG">Hoạt động</option>
                <option value="TAM_DUNG">Tạm dừng</option>
                <option value="NGUNG_KINH_DOANH">Ngừng kinh doanh</option>
              </select>
            </div>
          </div>
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

export default HangHoaForm;
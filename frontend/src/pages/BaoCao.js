import React, { useState, useEffect } from 'react';
import Layout from '../components/layout/Layout';
import { Loading } from '../components/shared';
import { toast } from 'react-toastify';
import api from '../services/api';

const BaoCao = () => {
  const [loading, setLoading] = useState(false);
  const [selectedReport, setSelectedReport] = useState('ton-kho');
  const [dateRange, setDateRange] = useState({
    tuNgay: new Date().toISOString().split('T')[0],
    denNgay: new Date().toISOString().split('T')[0]
  });
  const [reportData, setReportData] = useState(null);
  const [khoList, setKhoList] = useState([]);
  const [selectedKho, setSelectedKho] = useState('');

  useEffect(() => {
    fetchKhoList();
  }, []);

  const fetchKhoList = async () => {
    try {
      const response = await api.get('/api/kho/active');
      setKhoList(response.data.data || []);
    } catch (error) {
      console.error('Error fetching kho list:', error);
    }
  };

  const generateReport = async () => {
    setLoading(true);
    try {
      let response;
      switch (selectedReport) {
        case 'ton-kho':
          response = await api.get('/api/hang-hoa', {
            params: { size: 100 }
          });
          setReportData({
            type: 'ton-kho',
            data: response.data.data?.content || []
          });
          break;
        case 'hang-sap-het-han':
          response = await api.get('/api/hang-hoa/het-hang');
          setReportData({
            type: 'hang-sap-het-han',
            data: response.data.data || []
          });
          break;
        case 'ton-kho-thap':
          response = await api.get('/api/hang-hoa/ton-kho-thap');
          setReportData({
            type: 'ton-kho-thap',
            data: response.data.data || []
          });
          break;
        default:
          toast.info('Báo cáo này đang được phát triển');
          setReportData(null);
      }
    } catch (error) {
      toast.error('Lỗi khi tạo báo cáo');
      setReportData(null);
    } finally {
      setLoading(false);
    }
  };

  const exportToExcel = () => {
    toast.info('Tính năng xuất Excel đang được phát triển');
  };

  const printReport = () => {
    window.print();
  };

  const reportTypes = [
    { value: 'ton-kho', label: 'Báo cáo tồn kho' },
    { value: 'xuat-nhap', label: 'Báo cáo xuất nhập kho' },
    { value: 'hang-sap-het-han', label: 'Hàng sắp hết hạn' },
    { value: 'ton-kho-thap', label: 'Tồn kho thấp' },
    { value: 'gia-tri-kho', label: 'Báo cáo giá trị kho' },
    { value: 'thiet-bi', label: 'Báo cáo thiết bị' }
  ];

  const renderReportContent = () => {
    if (!reportData) return null;

    switch (reportData.type) {
      case 'ton-kho':
        return (
          <div>
            <h4 style={{ marginBottom: '1rem', color: '#2c3e50' }}>Báo cáo Tồn Kho</h4>
            <table style={{ width: '100%', borderCollapse: 'collapse' }}>
              <thead>
                <tr style={{ backgroundColor: '#f8f9fa' }}>
                  <th style={{ padding: '0.75rem', border: '1px solid #dee2e6', textAlign: 'left' }}>Mã HH</th>
                  <th style={{ padding: '0.75rem', border: '1px solid #dee2e6', textAlign: 'left' }}>Tên Hàng Hóa</th>
                  <th style={{ padding: '0.75rem', border: '1px solid #dee2e6', textAlign: 'center' }}>Tồn Kho</th>
                  <th style={{ padding: '0.75rem', border: '1px solid #dee2e6', textAlign: 'center' }}>Tối Thiểu</th>
                  <th style={{ padding: '0.75rem', border: '1px solid #dee2e6', textAlign: 'center' }}>Trạng Thái</th>
                </tr>
              </thead>
              <tbody>
                {reportData.data.map((item, index) => (
                  <tr key={index}>
                    <td style={{ padding: '0.75rem', border: '1px solid #dee2e6' }}>{item.maHangHoa}</td>
                    <td style={{ padding: '0.75rem', border: '1px solid #dee2e6' }}>{item.tenHangHoa}</td>
                    <td style={{ 
                      padding: '0.75rem', 
                      border: '1px solid #dee2e6', 
                      textAlign: 'center',
                      color: item.soLuongCoTheXuat < item.soLuongToiThieu ? '#e74c3c' : '#27ae60',
                      fontWeight: 'bold'
                    }}>
                      {item.soLuongCoTheXuat || 0}
                    </td>
                    <td style={{ padding: '0.75rem', border: '1px solid #dee2e6', textAlign: 'center' }}>
                      {item.soLuongToiThieu || 0}
                    </td>
                    <td style={{ padding: '0.75rem', border: '1px solid #dee2e6', textAlign: 'center' }}>
                      {item.soLuongCoTheXuat < item.soLuongToiThieu ? 
                        <span style={{ color: '#e74c3c', fontWeight: 'bold' }}>Thiếu hàng</span> :
                        <span style={{ color: '#27ae60' }}>Đủ hàng</span>
                      }
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        );

      case 'ton-kho-thap':
        return (
          <div>
            <h4 style={{ marginBottom: '1rem', color: '#e74c3c' }}>Báo cáo Tồn Kho Thấp</h4>
            {reportData.data.length === 0 ? (
              <div style={{ textAlign: 'center', padding: '2rem', color: '#27ae60' }}>
                <h5>Tuyệt vời! Không có hàng hóa nào bị thiếu</h5>
                <p>Tất cả hàng hóa đều có tồn kho đủ theo yêu cầu.</p>
              </div>
            ) : (
              <table style={{ width: '100%', borderCollapse: 'collapse' }}>
                <thead>
                  <tr style={{ backgroundColor: '#f8f9fa' }}>
                    <th style={{ padding: '0.75rem', border: '1px solid #dee2e6', textAlign: 'left' }}>Mã HH</th>
                    <th style={{ padding: '0.75rem', border: '1px solid #dee2e6', textAlign: 'left' }}>Tên Hàng Hóa</th>
                    <th style={{ padding: '0.75rem', border: '1px solid #dee2e6', textAlign: 'center' }}>Tồn Hiện Tại</th>
                    <th style={{ padding: '0.75rem', border: '1px solid #dee2e6', textAlign: 'center' }}>Tối Thiểu</th>
                    <th style={{ padding: '0.75rem', border: '1px solid #dee2e6', textAlign: 'center' }}>Thiếu</th>
                  </tr>
                </thead>
                <tbody>
                  {reportData.data.map((item, index) => (
                    <tr key={index}>
                      <td style={{ padding: '0.75rem', border: '1px solid #dee2e6' }}>{item.maHangHoa}</td>
                      <td style={{ padding: '0.75rem', border: '1px solid #dee2e6' }}>{item.tenHangHoa}</td>
                      <td style={{ padding: '0.75rem', border: '1px solid #dee2e6', textAlign: 'center', color: '#e74c3c', fontWeight: 'bold' }}>
                        {item.soLuongCoTheXuat || 0}
                      </td>
                      <td style={{ padding: '0.75rem', border: '1px solid #dee2e6', textAlign: 'center' }}>
                        {item.soLuongToiThieu || 0}
                      </td>
                      <td style={{ padding: '0.75rem', border: '1px solid #dee2e6', textAlign: 'center', color: '#e74c3c', fontWeight: 'bold' }}>
                        {(item.soLuongToiThieu || 0) - (item.soLuongCoTheXuat || 0)}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
          </div>
        );

      default:
        return (
          <div style={{ textAlign: 'center', padding: '2rem', color: '#7f8c8d' }}>
            <h4>Báo cáo đang được phát triển</h4>
            <p>Loại báo cáo này sẽ có trong phiên bản tiếp theo.</p>
          </div>
        );
    }
  };

  return (
    <Layout>
      <div>
        <h2 style={{ marginBottom: '1.5rem', color: '#2c3e50' }}>Báo Cáo & Thống Kê</h2>
        
        {/* Report Configuration */}
        <div style={{
          backgroundColor: 'white',
          padding: '1.5rem',
          borderRadius: '8px',
          marginBottom: '1.5rem',
          boxShadow: '0 2px 4px rgba(0,0,0,0.1)'
        }}>
          <h4 style={{ marginBottom: '1rem', color: '#2c3e50' }}>Cấu hình báo cáo</h4>
          
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '1rem', marginBottom: '1rem' }}>
            <div>
              <label style={{ display: 'block', marginBottom: '0.5rem', fontWeight: '500' }}>
                Loại báo cáo
              </label>
              <select
                value={selectedReport}
                onChange={(e) => setSelectedReport(e.target.value)}
                style={{
                  width: '100%',
                  padding: '0.75rem',
                  border: '1px solid #ddd',
                  borderRadius: '6px',
                  fontSize: '1rem'
                }}
              />
            </div>
          </div>
          
          <div style={{ display: 'flex', gap: '1rem', marginTop: '1.5rem' }}>
            <button
              onClick={generateReport}
              disabled={loading}
              style={{
                backgroundColor: loading ? '#95a5a6' : '#3498db',
                color: 'white',
                border: 'none',
                padding: '0.75rem 1.5rem',
                borderRadius: '6px',
                cursor: loading ? 'not-allowed' : 'pointer',
                fontWeight: '500'
              }}
            >
              {loading ? 'Đang tạo báo cáo...' : 'Tạo báo cáo'}
            </button>
            
            {reportData && (
              <>
                <button
                  onClick={exportToExcel}
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
                  Xuất Excel
                </button>
                
                <button
                  onClick={printReport}
                  style={{
                    backgroundColor: '#f39c12',
                    color: 'white',
                    border: 'none',
                    padding: '0.75rem 1.5rem',
                    borderRadius: '6px',
                    cursor: 'pointer',
                    fontWeight: '500'
                  }}
                >
                  In báo cáo
                </button>
              </>
            )}
          </div>
        </div>

        {/* Report Content */}
        {loading ? (
          <div style={{
            backgroundColor: 'white',
            borderRadius: '8px',
            boxShadow: '0 2px 4px rgba(0,0,0,0.1)',
            overflow: 'hidden'
          }}>
            <Loading message="Đang tạo báo cáo..." />
          </div>
        ) : reportData ? (
          <div style={{
            backgroundColor: 'white',
            padding: '1.5rem',
            borderRadius: '8px',
            boxShadow: '0 2px 4px rgba(0,0,0,0.1)',
            overflow: 'hidden'
          }}>
            <div style={{ marginBottom: '1rem', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <div>
                <h3 style={{ margin: 0, color: '#2c3e50' }}>
                  {reportTypes.find(t => t.value === selectedReport)?.label}
                </h3>
                <p style={{ margin: '0.25rem 0 0 0', color: '#7f8c8d', fontSize: '0.9rem' }}>
                  Ngày tạo: {new Date().toLocaleDateString('vi-VN')} | 
                  Thời gian: {new Date().toLocaleTimeString('vi-VN')}
                </p>
              </div>
            </div>
            
            <div style={{ borderTop: '2px solid #3498db', paddingTop: '1rem' }}>
              {renderReportContent()}
            </div>
            
            {/* Summary */}
            {reportData.data && reportData.data.length > 0 && (
              <div style={{
                marginTop: '2rem',
                padding: '1rem',
                backgroundColor: '#f8f9fa',
                borderRadius: '6px',
                borderLeft: '4px solid #3498db'
              }}>
                <h5 style={{ margin: '0 0 0.5rem 0', color: '#2c3e50' }}>Tóm tắt báo cáo</h5>
                <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '1rem' }}>
                  <div>
                    <div style={{ fontSize: '1.5rem', fontWeight: 'bold', color: '#3498db' }}>
                      {reportData.data.length}
                    </div>
                    <div style={{ color: '#7f8c8d', fontSize: '0.9rem' }}>Tổng số mục</div>
                  </div>
                  
                  {reportData.type === 'ton-kho' && (
                    <>
                      <div>
                        <div style={{ fontSize: '1.5rem', fontWeight: 'bold', color: '#e74c3c' }}>
                          {reportData.data.filter(item => item.soLuongCoTheXuat < item.soLuongToiThieu).length}
                        </div>
                        <div style={{ color: '#7f8c8d', fontSize: '0.9rem' }}>Hàng thiếu</div>
                      </div>
                      <div>
                        <div style={{ fontSize: '1.5rem', fontWeight: 'bold', color: '#27ae60' }}>
                          {reportData.data.filter(item => item.soLuongCoTheXuat >= item.soLuongToiThieu).length}
                        </div>
                        <div style={{ color: '#7f8c8d', fontSize: '0.9rem' }}>Hàng đủ</div>
                      </div>
                    </>
                  )}
                  
                  {reportData.type === 'ton-kho-thap' && reportData.data.length > 0 && (
                    <div>
                      <div style={{ fontSize: '1.5rem', fontWeight: 'bold', color: '#e74c3c' }}>
                        {reportData.data.reduce((sum, item) => sum + ((item.soLuongToiThieu || 0) - (item.soLuongCoTheXuat || 0)), 0)}
                      </div>
                      <div style={{ color: '#7f8c8d', fontSize: '0.9rem' }}>Tổng số lượng thiếu</div>
                    </div>
                  )}
                </div>
              </div>
            )}
          </div>
        ) : (
          <div style={{
            backgroundColor: 'white',
            padding: '3rem',
            borderRadius: '8px',
            boxShadow: '0 2px 4px rgba(0,0,0,0.1)',
            textAlign: 'center'
          }}>
            <div style={{ fontSize: '3rem', marginBottom: '1rem' }}>📊</div>
            <h3 style={{ color: '#2c3e50', marginBottom: '0.5rem' }}>Chọn loại báo cáo và nhấn "Tạo báo cáo"</h3>
            <p style={{ color: '#7f8c8d', margin: 0 }}>
              Hệ thống sẽ tạo báo cáo chi tiết theo yêu cầu của bạn
            </p>
          </div>
        )}

        {/* Quick Reports */}
        <div style={{ marginTop: '2rem' }}>
          <h3 style={{ marginBottom: '1rem', color: '#2c3e50' }}>Báo cáo nhanh</h3>
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(250px, 1fr))', gap: '1rem' }}>
            <div
              onClick={() => {
                setSelectedReport('ton-kho-thap');
                generateReport();
              }}
              style={{
                backgroundColor: 'white',
                padding: '1.5rem',
                borderRadius: '8px',
                boxShadow: '0 2px 4px rgba(0,0,0,0.1)',
                cursor: 'pointer',
                border: '2px solid transparent',
                transition: 'all 0.2s'
              }}
              onMouseOver={(e) => e.target.style.borderColor = '#e74c3c'}
              onMouseOut={(e) => e.target.style.borderColor = 'transparent'}
            >
              <div style={{ fontSize: '2rem', marginBottom: '0.5rem' }}>⚠️</div>
              <h4 style={{ margin: '0 0 0.5rem 0', color: '#e74c3c' }}>Hàng tồn kho thấp</h4>
              <p style={{ margin: 0, color: '#7f8c8d', fontSize: '0.9rem' }}>
                Xem danh sách hàng hóa có tồn kho dưới mức tối thiểu
              </p>
            </div>
            
            <div
              onClick={() => {
                setSelectedReport('ton-kho');
                generateReport();
              }}
              style={{
                backgroundColor: 'white',
                padding: '1.5rem',
                borderRadius: '8px',
                boxShadow: '0 2px 4px rgba(0,0,0,0.1)',
                cursor: 'pointer',
                border: '2px solid transparent',
                transition: 'all 0.2s'
              }}
              onMouseOver={(e) => e.target.style.borderColor = '#3498db'}
              onMouseOut={(e) => e.target.style.borderColor = 'transparent'}
            >
              <div style={{ fontSize: '2rem', marginBottom: '0.5rem' }}>📦</div>
              <h4 style={{ margin: '0 0 0.5rem 0', color: '#3498db' }}>Báo cáo tồn kho</h4>
              <p style={{ margin: 0, color: '#7f8c8d', fontSize: '0.9rem' }}>
                Xem tình trạng tồn kho tất cả hàng hóa
              </p>
            </div>
            
            <div
              style={{
                backgroundColor: 'white',
                padding: '1.5rem',
                borderRadius: '8px',
                boxShadow: '0 2px 4px rgba(0,0,0,0.1)',
                cursor: 'not-allowed',
                opacity: 0.6
              }}
            >
              <div style={{ fontSize: '2rem', marginBottom: '0.5rem' }}>⏰</div>
              <h4 style={{ margin: '0 0 0.5rem 0', color: '#f39c12' }}>Hàng sắp hết hạn</h4>
              <p style={{ margin: 0, color: '#7f8c8d', fontSize: '0.9rem' }}>
                Đang phát triển...
              </p>
            </div>
            
            <div
              style={{
                backgroundColor: 'white',
                padding: '1.5rem',
                borderRadius: '8px',
                boxShadow: '0 2px 4px rgba(0,0,0,0.1)',
                cursor: 'not-allowed',
                opacity: 0.6
              }}
            >
              <div style={{ fontSize: '2rem', marginBottom: '0.5rem' }}>📈</div>
              <h4 style={{ margin: '0 0 0.5rem 0', color: '#9b59b6' }}>Thống kê xuất nhập</h4>
              <p style={{ margin: 0, color: '#7f8c8d', fontSize: '0.9rem' }}>
                Đang phát triển...
              </p>
            </div>
          </div>
        </div>
      </div>
    </Layout>
  );
};

export default BaoCao;
                  
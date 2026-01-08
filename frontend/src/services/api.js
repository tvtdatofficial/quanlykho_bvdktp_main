import axios from 'axios';
import { toast } from 'react-toastify';

// ✅ Lấy API URL từ environment variable hoặc dùng localhost
const API_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080';

const api = axios.create({
  baseURL: API_URL,
  headers: {
    'Content-Type': 'application/json',
  },
  timeout: 30000, // 30 seconds
});

// Request interceptor
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('accessToken');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }

    // ✅ QUAN TRỌNG: Giữ nguyên Content-Type cho multipart/form-data
    // Khi upload file, axios tự động set Content-Type, không override
    if (config.data instanceof FormData) {
      delete config.headers['Content-Type'];
    }

    // Log request for debugging
    console.log('🚀 API Request:', {
      method: config.method?.toUpperCase(),
      url: config.url,
      // Không log data nếu là FormData (file có thể rất lớn)
      data: config.data instanceof FormData ? '[FormData]' : config.data
    });

    return config;
  },
  (error) => {
    console.error('❌ Request Error:', error);
    return Promise.reject(error);
  }
);

// Response interceptor
api.interceptors.response.use(
  (response) => {
    // Log successful response
    console.log('✅ API Response:', {
      url: response.config.url,
      status: response.status,
      // Không log toàn bộ data nếu response lớn
      dataSize: JSON.stringify(response.data).length
    });

    return response;
  },
  (error) => {
    console.error('❌ API Error:', {
      url: error.config?.url,
      status: error.response?.status,
      message: error.response?.data?.message || error.message
    });

    // Handle specific error cases
    if (error.response) {
      const { status, data } = error.response;

      switch (status) {
        case 401:
          toast.error('Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.');
          localStorage.clear();
          setTimeout(() => {
            window.location.href = '/dang-nhap';
          }, 1500);
          break;

        case 403:
          toast.error('Bạn không có quyền thực hiện thao tác này.');
          break;

        case 404:
          toast.error('Không tìm thấy dữ liệu yêu cầu.');
          break;

        case 409:
          toast.error(data.message || 'Dữ liệu bị trùng lặp.');
          break;

        case 500:
          toast.error('Lỗi hệ thống. Vui lòng liên hệ quản trị viên.');
          break;

        default:
          if (data && data.message) {
            toast.error(data.message);
          }
      }
    } else if (error.request) {
      toast.error('Không thể kết nối đến máy chủ. Vui lòng kiểm tra kết nối mạng.');
    } else {
      toast.error('Đã xảy ra lỗi: ' + error.message);
    }

    return Promise.reject(error);
  }
);

// ✅ THÊM: Helper function để build URL ảnh đầy đủ
export const getImageUrl = (imagePath) => {
  if (!imagePath) return null;

  // Nếu đã là URL đầy đủ, return luôn
  if (imagePath.startsWith('http://') || imagePath.startsWith('https://')) {
    return imagePath;
  }

  // Nếu bắt đầu bằng /api/, ghép với baseURL
  if (imagePath.startsWith('/api/')) {
    return `${API_URL}${imagePath}`;
  }

  // Nếu là đường dẫn tương đối (hang-hoa/abc.jpg), ghép với /api/files/view/
  return `${API_URL}/api/files/view/${imagePath}`;
};

export default api;
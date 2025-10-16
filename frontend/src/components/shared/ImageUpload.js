import React, { useState, useEffect } from 'react';
import api, { getImageUrl } from '../../services/api';
import { toast } from 'react-toastify';

const ImageUpload = ({ currentImage, onImageChange }) => {
  const [uploading, setUploading] = useState(false);
  const [previewUrl, setPreviewUrl] = useState(null);

  // ✅ Base64 placeholder image
  const PLACEHOLDER_IMAGE = 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMzAwIiBoZWlnaHQ9IjIwMCIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj4KICA8cmVjdCB3aWR0aD0iMzAwIiBoZWlnaHQ9IjIwMCIgZmlsbD0iI2YwZjBmMCIvPgogIDx0ZXh0IHg9IjUwJSIgeT0iNTAlIiBmb250LWZhbWlseT0iQXJpYWwiIGZvbnQtc2l6ZT0iMTgiIGZpbGw9IiM5OTkiIHRleHQtYW5jaG9yPSJtaWRkbGUiIGR5PSIuM2VtIj7huqJuaCBs4buXaTwvdGV4dD4KPC9zdmc+';

  // ✅ QUAN TRỌNG: Cập nhật preview khi currentImage thay đổi
  useEffect(() => {
    if (currentImage) {
      // Nếu là URL từ server (bắt đầu bằng /api/)
      if (currentImage.startsWith('/api/')) {
        setPreviewUrl(getImageUrl(currentImage));
      }
      // Nếu là base64 hoặc blob URL
      else if (currentImage.startsWith('data:') || currentImage.startsWith('blob:')) {
        setPreviewUrl(currentImage);
      }
      // Nếu là URL đầy đủ
      else if (currentImage.startsWith('http')) {
        setPreviewUrl(currentImage);
      }
      // Các trường hợp khác
      else {
        setPreviewUrl(getImageUrl(currentImage));
      }
    } else {
      setPreviewUrl(null);
    }
  }, [currentImage]);

  const handleFileSelect = async (e) => {
    const file = e.target.files[0];
    if (!file) return;

    // Validate file type
    const validTypes = ['image/jpeg', 'image/jpg', 'image/png', 'image/gif', 'image/webp'];
    if (!validTypes.includes(file.type)) {
      toast.error('Chỉ chấp nhận file ảnh (JPG, PNG, GIF, WEBP)');
      e.target.value = ''; // Reset input
      return;
    }

    // Validate file size (max 5MB)
    if (file.size > 5 * 1024 * 1024) {
      toast.error('Kích thước file không được vượt quá 5MB');
      e.target.value = ''; // Reset input
      return;
    }

    // ✅ Show preview IMMEDIATELY using FileReader
    const reader = new FileReader();
    reader.onloadend = () => {
      setPreviewUrl(reader.result); // Base64 string
    };
    reader.readAsDataURL(file);

    // Upload to server
    setUploading(true);
    const formData = new FormData();
    formData.append('file', file);

    try {
      const response = await api.post('/api/files/upload/hang-hoa', formData, {
        headers: {
          'Content-Type': 'multipart/form-data'
        }
      });

      const fileUrl = response.data.data?.fileUrl || response.data.data;
      
      // ✅ Cập nhật preview với URL từ server
      setPreviewUrl(getImageUrl(fileUrl));
      
      // ✅ Gọi callback để cập nhật form
      onImageChange(fileUrl);
      
      toast.success('✅ Upload ảnh thành công');
    } catch (error) {
      console.error('Upload error:', error);
      toast.error(error.response?.data?.message || 'Lỗi khi upload ảnh');
      
      // ✅ Revert về ảnh cũ nếu upload thất bại
      if (currentImage) {
        setPreviewUrl(getImageUrl(currentImage));
      } else {
        setPreviewUrl(null);
      }
    } finally {
      setUploading(false);
      e.target.value = ''; // Reset input để có thể chọn lại file
    }
  };

  const handleRemoveImage = async () => {
    if (!currentImage) return;

    if (!window.confirm('Bạn có chắc muốn xóa ảnh này?')) return;

    try {
      // Extract file path từ URL
      const filePath = currentImage.replace('/api/files/view/', '');
      
      await api.delete('/api/files/delete', {
        params: { filePath }
      });

      setPreviewUrl(null);
      onImageChange(''); // Gửi empty string thay vì null
      toast.success('🗑️ Đã xóa ảnh');
    } catch (error) {
      console.error('Delete error:', error);
      // Không hiện lỗi nếu file không tồn tại
      if (error.response?.status === 404) {
        setPreviewUrl(null);
        onImageChange('');
        toast.info('Đã xóa ảnh');
      } else {
        toast.error('Lỗi khi xóa ảnh');
      }
    }
  };

  return (
    <div style={{ marginBottom: '1.5rem' }}>
      <label style={{
        display: 'block',
        marginBottom: '0.5rem',
        color: '#2c3e50',
        fontWeight: '500',
        fontSize: '0.95rem'
      }}>
        Hình Ảnh
      </label>

      <div style={{
        border: '2px dashed #ddd',
        borderRadius: '8px',
        padding: '1.5rem',
        textAlign: 'center',
        backgroundColor: '#fafafa',
        transition: 'border-color 0.3s',
        position: 'relative'
      }}>
        {previewUrl ? (
          // ========== CÓ ẢNH - Hiển thị preview ==========
          <div style={{ position: 'relative' }}>
            <img
              src={previewUrl}
              alt="Preview"
              style={{
                maxWidth: '100%',
                maxHeight: '300px',
                borderRadius: '8px',
                objectFit: 'contain',
                border: '2px solid #e0e0e0',
                backgroundColor: 'white'
              }}
              onError={(e) => {
                console.error('Image load error');
                e.target.onerror = null; // Ngăn vòng lặp vô hạn
                e.target.src = PLACEHOLDER_IMAGE;
              }}
            />

            {/* Loading overlay khi đang upload */}
            {uploading && (
              <div style={{
                position: 'absolute',
                top: 0,
                left: 0,
                right: 0,
                bottom: 0,
                background: 'rgba(0,0,0,0.6)',
                borderRadius: '8px',
                display: 'flex',
                flexDirection: 'column',
                alignItems: 'center',
                justifyContent: 'center',
                color: 'white',
                fontSize: '0.95rem',
                fontWeight: '600'
              }}>
                <div style={{ 
                  fontSize: '2.5rem', 
                  marginBottom: '0.75rem',
                  animation: 'spin 1.5s linear infinite'
                }}>
                  ⏳
                </div>
                <div>Đang tải ảnh lên server...</div>
              </div>
            )}

            {/* Buttons: Thay đổi & Xóa */}
            <div style={{
              marginTop: '1rem',
              display: 'flex',
              gap: '0.75rem',
              justifyContent: 'center',
              flexWrap: 'wrap'
            }}>
              <label
                style={{
                  padding: '0.65rem 1.25rem',
                  backgroundColor: uploading ? '#95a5a6' : '#3498db',
                  color: 'white',
                  borderRadius: '6px',
                  cursor: uploading ? 'not-allowed' : 'pointer',
                  display: 'inline-block',
                  fontWeight: '500',
                  fontSize: '0.9rem',
                  transition: 'background-color 0.3s',
                  opacity: uploading ? 0.7 : 1
                }}
                onMouseEnter={(e) => !uploading && (e.target.style.backgroundColor = '#2980b9')}
                onMouseLeave={(e) => !uploading && (e.target.style.backgroundColor = '#3498db')}
              >
                🔄 Thay đổi ảnh
                <input
                  type="file"
                  accept="image/jpeg,image/jpg,image/png,image/gif,image/webp"
                  onChange={handleFileSelect}
                  style={{ display: 'none' }}
                  disabled={uploading}
                />
              </label>

              <button
                type="button"
                onClick={handleRemoveImage}
                disabled={uploading}
                style={{
                  padding: '0.65rem 1.25rem',
                  backgroundColor: uploading ? '#95a5a6' : '#e74c3c',
                  color: 'white',
                  border: 'none',
                  borderRadius: '6px',
                  cursor: uploading ? 'not-allowed' : 'pointer',
                  fontWeight: '500',
                  fontSize: '0.9rem',
                  transition: 'background-color 0.3s',
                  opacity: uploading ? 0.7 : 1
                }}
                onMouseEnter={(e) => !uploading && (e.target.style.backgroundColor = '#c0392b')}
                onMouseLeave={(e) => !uploading && (e.target.style.backgroundColor = '#e74c3c')}
              >
                🗑️ Xóa ảnh
              </button>
            </div>
          </div>
        ) : (
          // ========== CHƯA CÓ ẢNH - Upload zone ==========
          <label
            style={{
              display: 'block',
              padding: '2rem 1rem',
              cursor: uploading ? 'not-allowed' : 'pointer',
              transition: 'background-color 0.3s'
            }}
            onMouseEnter={(e) => !uploading && (e.currentTarget.style.backgroundColor = '#f0f0f0')}
            onMouseLeave={(e) => (e.currentTarget.style.backgroundColor = 'transparent')}
          >
            <div style={{ 
              fontSize: '4rem', 
              marginBottom: '1rem',
              opacity: uploading ? 0.5 : 0.4
            }}>
              📷
            </div>
            <div style={{ 
              color: '#7f8c8d', 
              marginBottom: '0.75rem',
              fontSize: '1rem',
              fontWeight: '500'
            }}>
              {uploading ? '⏳ Đang upload ảnh...' : 'Click để chọn ảnh'}
            </div>
            <div style={{ 
              fontSize: '0.85rem', 
              color: '#95a5a6',
              lineHeight: '1.5'
            }}>
              Hỗ trợ: JPG, PNG, GIF, WEBP
              <br />
              Kích thước tối đa: 5MB
            </div>
            <input
              type="file"
              accept="image/jpeg,image/jpg,image/png,image/gif,image/webp"
              onChange={handleFileSelect}
              style={{ display: 'none' }}
              disabled={uploading}
            />
          </label>
        )}
      </div>

      {/* Loading indicator bên ngoài */}
      {uploading && !previewUrl && (
        <div style={{
          marginTop: '0.75rem',
          padding: '0.75rem',
          backgroundColor: '#e3f2fd',
          borderRadius: '6px',
          color: '#1976d2',
          textAlign: 'center',
          fontWeight: '500',
          fontSize: '0.9rem',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          gap: '0.5rem'
        }}>
          <span style={{ animation: 'spin 1s linear infinite' }}>⏳</span>
          Đang tải ảnh lên server...
        </div>
      )}

      {/* CSS Animation */}
      <style>
        {`
          @keyframes spin {
            from { transform: rotate(0deg); }
            to { transform: rotate(360deg); }
          }
        `}
      </style>
    </div>
  );
};

export default ImageUpload;
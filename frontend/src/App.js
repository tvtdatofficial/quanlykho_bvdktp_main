import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { ToastContainer } from 'react-toastify';
import 'react-toastify/dist/ReactToastify.css';

// Components
import BaoVeRoute from './components/BaoVeRoute';
import ErrorBoundary from './components/shared/ErrorBoundary';

// Pages
import DangNhap from './pages/DangNhap';
import TrangChu from './pages/TrangChu';
import QuanLyKho from './pages/QuanLyKho';
import HangHoa from './pages/HangHoa';
import NhapKho from './pages/NhapKho';
import XuatKho from './pages/XuatKho';
import BaoCao from './pages/BaoCao';
import ViTriKho from './pages/ViTriKho';
import LoHang from './pages/LoHang';

function App() {
  return (
    <ErrorBoundary>
      <Router>
        <div className="App">
          <Routes>
            {/* Public Route */}
            <Route path="/dang-nhap" element={<DangNhap />} />
            
            {/* Protected Routes */}
            <Route
              path="/"
              element={
                <BaoVeRoute>
                  <TrangChu />
                </BaoVeRoute>
              }
            />
            
            <Route
              path="/quan-ly-kho"
              element={
                <BaoVeRoute>
                  <QuanLyKho />
                </BaoVeRoute>
              }
            />
            
            <Route
              path="/hang-hoa"
              element={
                <BaoVeRoute>
                  <HangHoa />
                </BaoVeRoute>
              }
            />
            
            <Route
              path="/nhap-kho"
              element={
                <BaoVeRoute>
                  <NhapKho />
                </BaoVeRoute>
              }
            />
            
            <Route
              path="/xuat-kho"
              element={
                <BaoVeRoute>
                  <XuatKho />
                </BaoVeRoute>
              }
            />
            
            <Route
              path="/bao-cao"
              element={
                <BaoVeRoute>
                  <BaoCao />
                </BaoVeRoute>
              }
            />
            
            <Route
              path="/vi-tri-kho"
              element={
                <BaoVeRoute>
                  <ViTriKho />
                </BaoVeRoute>
              }
            />
            
            <Route
              path="/lo-hang"
              element={
                <BaoVeRoute>
                  <LoHang />
                </BaoVeRoute>
              }
            />
            
            {/* Catch all - redirect to home */}
            <Route path="*" element={<Navigate to="/" replace />} />
          </Routes>

          {/* Toast Container với config tối ưu */}
          <ToastContainer
            position="top-right"
            autoClose={3000}
            hideProgressBar={false}
            newestOnTop
            closeOnClick
            rtl={false}
            pauseOnFocusLoss
            draggable
            pauseOnHover
            theme="light"
            style={{ zIndex: 99999 }}
            toastClassName="custom-toast"
          />
        </div>
      </Router>
    </ErrorBoundary>
  );
}

export default App;
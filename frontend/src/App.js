// App.js
import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { ToastContainer } from 'react-toastify';
import DangNhap from './pages/DangNhap';
import TrangChu from './pages/TrangChu';
import QuanLyKho from './pages/QuanLyKho';
import HangHoa from './pages/HangHoa';
import NhapKho from './pages/NhapKho';
import XuatKho from './pages/XuatKho';
import BaoCao from './pages/BaoCao';


import ViTriKho from './pages/ViTriKho';
import LoHang from './pages/LoHang';

import BaoVeRoute from './components/BaoVeRoute';



import 'react-toastify/dist/ReactToastify.css';

function App() {
  return (
    <Router>
      <div className="App">
        <Routes>
          <Route path="/dang-nhap" element={<DangNhap />} />
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


          <Route path="/vi-tri-kho" element={<ViTriKho />} />
          <Route path="/lo-hang" element={<LoHang />} />



          <Route path="*" element={<Navigate to="/" />} />
        </Routes>

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
        />
      </div>
    </Router>
  );
}

export default App;
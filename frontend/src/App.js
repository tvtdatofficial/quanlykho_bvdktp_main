import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { ToastContainer } from 'react-toastify';
import DangNhap from './pages/DangNhap';
import TrangChu from './pages/TrangChu';
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
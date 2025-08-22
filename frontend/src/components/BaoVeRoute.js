import React from 'react';
import { Navigate } from 'react-router-dom';

const BaoVeRoute = ({ children }) => {
  const token = localStorage.getItem('accessToken');
  
  if (!token) {
    return <Navigate to="/dang-nhap" replace />;
  }
  
  return children;
};

export default BaoVeRoute;
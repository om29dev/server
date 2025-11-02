// src/App.jsx

import React from 'react';
import { Routes, Route } from 'react-router-dom';
import Header from './components/Header';
import ProtectedRoute from './components/ProtectedRoute';
import Login from './pages/Login';
import Register from './pages/Register';
import Dashboard from './pages/Dashboard';
import JoinClassroom from './pages/JoinClassroom';
import { useAuth } from './context/AuthContext';

function App() {
  const { loading } = useAuth();
  
  if (loading) {
    return <div className="loading">Loading authentication state...</div>;
  }

  return (
    <>
      <Header />
      <main className="container">
        <Routes>
          <Route path="/login" element={<Login />} />
          <Route path="/register" element={<Register />} />
          
          {/* Protected Routes: Require a logged-in user */}
          <Route element={<ProtectedRoute />}>
            <Route path="/" element={<Dashboard />} />
            <Route path="/join" element={<JoinClassroom />} />
          </Route>

          <Route path="*" element={<h1>404 Not Found</h1>} />
        </Routes>
      </main>
    </>
  );
}

export default App;
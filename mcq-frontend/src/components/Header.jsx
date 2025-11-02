// src/components/Header.jsx

import React from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

const Header = () => {
  const { isAuthenticated, user, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = async () => {
    try {
      await logout();
      navigate('/login');
    } catch (error) {
      console.error('Logout failed:', error);
      alert('Logout failed. See console for details.');
    }
  };

  return (
    <header className="header">
      <nav>
        <Link to="/" className="logo">MCQ Server</Link>
        <div className="nav-links">
          {isAuthenticated ? (
            <>
              <span>Logged in as: <strong>{user.username} ({user.role.replace('ROLE_', '')})</strong></span>
              <button onClick={handleLogout} className="btn-logout">Logout</button>
            </>
          ) : (
            <>
              <Link to="/login">Login</Link>
              <Link to="/register">Register</Link>
            </>
          )}
        </div>
      </nav>
    </header>
  );
};

export default Header;
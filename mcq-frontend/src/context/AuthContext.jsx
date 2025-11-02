// src/context/AuthContext.jsx

import React, { createContext, useContext, useState, useEffect } from 'react';
import { loginUser, logoutUser, registerUser } from '../services/apiService';

const AuthContext = createContext();

export const useAuth = () => useContext(AuthContext);

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  // Check for stored user data on initial load
  useEffect(() => {
    const storedUser = localStorage.getItem('user');
    if (storedUser) {
      setUser(JSON.parse(storedUser));
    }
    setLoading(false);
  }, []);

  const login = async (username, password) => {
    // 1. Authenticate against the backend (sets the JSESSIONID cookie)
    await loginUser(username, password);
    
    // 2. Since the backend doesn't return user details on login, we mock minimal user data.
    const tempUser = { 
        username: username, 
        // WARNING: This role is mocked. In a production app, you MUST fetch the actual role.
        role: 'ROLE_STUDENT' 
    }; 
    
    setUser(tempUser);
    localStorage.setItem('user', JSON.stringify(tempUser));
  };

  const register = async (user, role) => {
    // Passes the full user object including the role to the backend
    const newUser = { ...user, role: role };
    await registerUser(newUser);
  };

  const logout = async () => {
    await logoutUser();
    setUser(null);
    localStorage.removeItem('user');
  };

  const isAuthenticated = !!user;
  const isAdmin = user?.role === 'ROLE_ADMIN';
  const isTeacher = user?.role === 'ROLE_TEACHER';
  const isStudent = user?.role === 'ROLE_STUDENT';

  return (
    <AuthContext.Provider value={{ user, isAuthenticated, isAdmin, isTeacher, isStudent, login, register, logout, loading }}>
      {!loading && children}
    </AuthContext.Provider>
  );
};
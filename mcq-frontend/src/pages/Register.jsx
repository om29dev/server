// src/pages/Register.jsx

import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

const Register = () => {
  const [form, setForm] = useState({
    firstname: '',
    lastname: '',
    email: '',
    username: '',
    password: '',
    role: 'ROLE_STUDENT', // Default role selection
  });
  const [error, setError] = useState('');
  const { register } = useAuth();
  const navigate = useNavigate();

  const handleChange = (e) => {
    setForm({ ...form, [e.target.name]: e.target.value });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    try {
      await register(form, form.role);
      alert('Registration successful! Please log in.');
      navigate('/login');
    } catch (err) {
      console.error(err);
      setError(err.message || 'Registration failed. Check if username/email is taken.');
    }
  };

  return (
    <div className="auth-form-container">
      <h2>Register New User</h2>
      <form onSubmit={handleSubmit} className="auth-form">
        <input name="firstname" type="text" placeholder="First Name" value={form.firstname} onChange={handleChange} required />
        <input name="lastname" type="text" placeholder="Last Name" value={form.lastname} onChange={handleChange} required />
        <input name="email" type="email" placeholder="Email" value={form.email} onChange={handleChange} required />
        <input name="username" type="text" placeholder="Username" value={form.username} onChange={handleChange} required />
        <input name="password" type="password" placeholder="Password" value={form.password} onChange={handleChange} required />
        
        <label>
          Role:
          <select name="role" value={form.role} onChange={handleChange}>
            <option value="ROLE_STUDENT">Student</option>
            <option value="ROLE_TEACHER">Teacher</option>
            <option value="ROLE_ADMIN">Admin</option>
          </select>
        </label>
        
        <button type="submit" className="btn-primary">Register</button>
        {error && <p className="error-message">{error}</p>}
      </form>
    </div>
  );
};

export default Register;
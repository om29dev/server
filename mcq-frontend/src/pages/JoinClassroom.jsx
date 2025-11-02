// src/pages/JoinClassroom.jsx

import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { joinClassroom } from '../services/apiService';

const JoinClassroom = () => {
  const [code, setCode] = useState('');
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setMessage('');
    setError('');
    
    // Unique codes are 4 characters long
    if (code.length !== 4) { 
        setError("Classroom code must be 4 characters long.");
        return;
    }

    try {
      const successMessage = await joinClassroom(code.toUpperCase()); 
      setMessage(successMessage);
      alert('Successfully joined the classroom!');
      navigate('/');
    } catch (err) {
      console.error(err);
      setError(err.message || 'Failed to join classroom. Check the code.');
    }
  };

  return (
    <div className="auth-form-container">
      <h2>Join a Classroom</h2>
      <p>Enter the 4-digit code provided by your teacher.</p>
      <form onSubmit={handleSubmit} className="auth-form">
        <input 
          type="text" 
          placeholder="4-digit Code (e.g., A1B2)" 
          value={code} 
          onChange={(e) => setCode(e.target.value)} 
          maxLength="4"
          style={{ textTransform: 'uppercase' }}
          required 
        />
        <button type="submit" className="btn-primary">Join</button>
        {message && <p className="success-message">{message}</p>}
        {error && <p className="error-message">{error}</p>}
      </form>
    </div>
  );
};

export default JoinClassroom;
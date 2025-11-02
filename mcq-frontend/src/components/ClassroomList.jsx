// src/components/ClassroomList.jsx

import React, { useState, useEffect } from 'react';
import { getMyClassrooms, leaveClassroom } from '../services/apiService';
import { useAuth } from '../context/AuthContext';

const ClassroomList = () => {
  const [classrooms, setClassrooms] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const { isStudent } = useAuth();

  const fetchClassrooms = async () => {
    try {
      setLoading(true);
      const data = await getMyClassrooms(); 
      setClassrooms(data);
    } catch (err) {
      console.error('Error fetching classrooms:', err);
      // Basic error handling for session expiry
      if (err.message.includes('401') || err.message.includes('403')) {
          setError('Session expired. Please log in again.');
      } else {
          setError('Failed to load classrooms.');
      }
      setClassrooms([]);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchClassrooms();
  }, []);

  const handleLeaveClassroom = async (code) => {
    if (!window.confirm('Are you sure you want to leave this classroom?')) {
        return;
    }
    try {
        await leaveClassroom(code); 
        alert('Successfully left the classroom.');
        fetchClassrooms(); // Refresh the list
    } catch (err) {
        console.error('Error leaving classroom:', err);
        alert(err.message || 'Failed to leave classroom.');
    }
  }

  if (loading) return <p>Loading classrooms...</p>;
  if (error) return <p className="error-message">{error}</p>;
  if (classrooms.length === 0) return <p>You are not currently enrolled in any classrooms or the teacher of any classrooms.</p>;

  return (
    <div className="classroom-list">
      {classrooms.map((classroom) => (
        <div key={classroom.code} className="classroom-card">
          <h3>{classroom.classroomname}</h3>
          <p>Code: <strong>{classroom.code}</strong></p>
          <p>Teacher: {classroom.classroomteacher.username}</p>
          {isStudent && (
              <button 
                className="btn-danger" 
                onClick={() => handleLeaveClassroom(classroom.code)}
              >
                  Leave
              </button>
          )}
          <button className="btn-link">View Tests (Not Implemented)</button>
        </div>
      ))}
    </div>
  );
};

export default ClassroomList;
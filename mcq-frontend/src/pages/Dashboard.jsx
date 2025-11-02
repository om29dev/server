// src/pages/Dashboard.jsx

import React from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import ClassroomList from '../components/ClassroomList';

const Dashboard = () => {
  const { isTeacher, isAdmin } = useAuth();

  return (
    <div className="dashboard">
      <h2>Your Classrooms</h2>
      
      <div className="actions">
        {/* Only Teachers or Admins can create a classroom */}
        {(isTeacher || isAdmin) && (
          <button className="btn-secondary" onClick={() => alert('Feature not implemented: Create Classroom')}>
            + Create New Classroom
          </button>
        )}
        <Link to="/join" className="btn-primary">
          Join Classroom
        </Link>
      </div>

      <ClassroomList />

    </div>
  );
};

export default Dashboard;
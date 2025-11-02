// src/services/apiService.js

const API_BASE_URL = 'http://localhost:8080/api';

/**
 * Common function for all API calls.
 * Sets 'credentials: include' for session cookie support.
 */
const fetchApi = async (url, options = {}) => {
  const defaultOptions = {
    method: 'GET',
    headers: {
      'Content-Type': 'application/json',
    },
    // IMPORTANT: Include credentials to send/receive JSESSIONID cookie
    credentials: 'include',
    ...options,
  };

  const response = await fetch(url, defaultOptions);

  if (!response.ok) {
    let error;
    try {
      error = await response.json();
    } catch (e) {
      error = await response.text();
    }
    // Extract error message if it's a string, or fall back to HTTP status
    throw new Error(error.message || error || `HTTP error! Status: ${response.status}`);
  }

  // Handle successful logout or delete requests with no content (204)
  if (response.status === 204 || response.headers.get('content-length') === '0') {
    return null;
  }

  try {
    return await response.json();
  } catch (e) {
    // Return text for success responses that send a message string (like login/logout)
    return await response.text();
  }
};

// --- AUTH API ---

export const registerUser = (user) => {
  return fetchApi(`${API_BASE_URL}/auth/register`, {
    method: 'POST',
    body: JSON.stringify(user),
  });
};

export const loginUser = async (username, password) => {
  const response = await fetchApi(`${API_BASE_URL}/auth/login`, {
    method: 'POST',
    // Key case must match LoginRequest DTO: { "Username": "...", "Password": "..." }
    body: JSON.stringify({ Username: username, Password: password }), 
  });
  return response;
};

export const logoutUser = () => {
  return fetchApi(`${API_BASE_URL}/auth/logout`, {
    method: 'POST',
    headers: {},
  });
};

// --- CLASSROOM API ---

export const getMyClassrooms = () => {
  // GET /api/classrooms?filter=mine
  return fetchApi(`${API_BASE_URL}/classrooms?filter=mine`);
};

export const joinClassroom = (code) => {
  // POST /api/classrooms/{code}/join
  return fetchApi(`${API_BASE_URL}/classrooms/${code}/join`, {
    method: 'POST',
    body: JSON.stringify({}), 
  });
};

export const leaveClassroom = (code) => {
  // DELETE /api/classrooms/{code}/leave
  return fetchApi(`${API_BASE_URL}/classrooms/${code}/leave`, {
    method: 'DELETE',
  });
};
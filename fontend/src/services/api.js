import axios from 'axios';

const BASE_URL = 'http://localhost:8080/api/pomodoro';

export const createSession = (sessionId, timerMode = 'CONTINUOUS') =>
  axios.post(`${BASE_URL}/create`, { sessionId, timerMode });

export const startTimer = (sessionId) =>
  axios.post(`${BASE_URL}/${sessionId}/start`);

export const pauseTimer = (sessionId) =>
  axios.post(`${BASE_URL}/${sessionId}/pause`);

export const resetTimer = (sessionId) =>
  axios.post(`${BASE_URL}/${sessionId}/reset`);

export const deleteSession = (sessionId) =>
  axios.delete(`${BASE_URL}/${sessionId}`);

export const getTimerInfo = (sessionId) =>
  axios.get(`${BASE_URL}/${sessionId}/info`); 
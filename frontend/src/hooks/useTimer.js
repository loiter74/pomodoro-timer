import { useState, useEffect, useRef } from 'react';
import { createSession, startTimer, pauseTimer, resetTimer, getTimerInfo } from '../services/api';
import { FaAppleAlt } from 'react-icons/fa';

const SESSION_ID = localStorage.getItem('pomodoro_session_id') || (() => {
  const id = 'user_' + Math.random().toString(36).slice(2, 10);
  localStorage.setItem('pomodoro_session_id', id);
  return id;
})();

export default function useTimer() {
  const [info, setInfo] = useState(null);
  const [loading, setLoading] = useState(false);
  const timerRef = useRef(null);

  // 创建会话
  useEffect(() => {
    // 只在首次进入时创建会话
    createSession(SESSION_ID, 'CONTINUOUS')
      .catch(() => {}) // 如果已存在则忽略错误
      .finally(fetchInfo);
    // 清理
    return () => {
      if (timerRef.current) clearInterval(timerRef.current);
    };
  }, []);

  // 轮询获取状态
  const fetchInfo = () => {
    getTimerInfo(SESSION_ID).then(res => setInfo(res.data));
  };

  const start = async () => {
    setLoading(true);
    await startTimer(SESSION_ID);
    setLoading(false);
    fetchInfo();
    if (!timerRef.current) {
      timerRef.current = setInterval(fetchInfo, 1000);
    }
  };

  const pause = async () => {
    setLoading(true);
    await pauseTimer(SESSION_ID);
    setLoading(false);
    fetchInfo();
    if (timerRef.current) {
      clearInterval(timerRef.current);
      timerRef.current = null;
    }
  };

  const reset = async () => {
    setLoading(true);
    await resetTimer(SESSION_ID);
    setLoading(false);
    fetchInfo();
  };

  return { info, loading, start, pause, reset };
} 
import React from 'react';
import styled from 'styled-components';

const BtnGroup = styled.div`
  display: flex;
  justify-content: center;
  gap: 1.5rem;
  margin-top: 2rem;
`;

const Btn = styled.button`
  background: #fff;
  color: #e74c3c;
  border: none;
  border-radius: 2rem;
  padding: 0.7rem 2rem;
  font-size: 1.1rem;
  font-weight: bold;
  cursor: pointer;
  transition: background 0.2s;
  &:hover {
    background: #ffe5e0;
  }
`;

export default function TimerControls({ running, onStart, onPause, onReset, loading }) {
  return (
    <BtnGroup>
      {running ? (
        <Btn onClick={onPause} disabled={loading}>暂停</Btn>
      ) : (
        <Btn onClick={onStart} disabled={loading}>开始</Btn>
      )}
      <Btn onClick={onReset} disabled={loading}>重置</Btn>
    </BtnGroup>
  );
} 
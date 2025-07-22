import React from 'react';
import styled, { createGlobalStyle } from 'styled-components';
import { Card, Button, Typography } from 'antd';
import 'antd/dist/reset.css';
import TimerDisplay from './TimerDisplay';
import useTimer from '../hooks/useTimer';

const { Title } = Typography;

const GlobalStyle = createGlobalStyle`
  body {
    background: linear-gradient(135deg, #f8fafc 0%, #e0e7ef 100%);
    min-height: 100vh;
    margin: 0;
    font-family: 'PingFang SC', 'Microsoft YaHei', 'Segoe UI', 'Roboto', sans-serif;
  }
`;

const Center = styled.div`
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
`;

const Tomato = styled.div`
  width: 220px;
  height: 220px;
  background: radial-gradient(circle at 60% 40%, #ffb199 80%, #ff6347 100%);
  border-radius: 50%;
  box-shadow: 0 8px 32px rgba(231,76,60,0.15);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  position: relative;
  margin-bottom: 2rem;
  transition: box-shadow 0.2s;
  &:hover {
    box-shadow: 0 12px 40px rgba(231,76,60,0.25);
  }
  &::before {
    content: '';
    position: absolute;
    top: 10px;
    left: 150px;
    width: 40px;
    height: 40px;
    background: #27ae60;
    border-radius: 0 50% 50% 0;
    transform: rotate(-50deg);
  }
`;


export default function App() {
  const { info, loading, start, pause, reset } = useTimer();

  return (
    <>
      <GlobalStyle />
      <Center>
        <Card
          style={{
            width: 350,
            borderRadius: 16,
            boxShadow: '0 8px 32px rgba(76, 110, 245, 0.10)',
            background: 'rgba(255,255,255,0.95)',
            marginBottom: 32,
          }}
          styles={{ body: { display: 'flex', flexDirection: 'column', alignItems: 'center', background: 'transparent' } }}
        >
          <Title level={2} style={{ color: '#ff6347', marginBottom: 16 }}>番茄钟</Title>
          <Tomato
            onClick={() => {
              if (!info?.isRunning) {
                start();
              } else {
                pause();
              }
            }}
            title="点击番茄可开始/暂停"
          >
            <TimerDisplay info={info} />
          </Tomato>
          <div style={{ display: 'flex', gap: 16, marginTop: 24 }}>
            {info?.isRunning ? (
              <Button type="primary" danger onClick={pause} loading={loading}>暂停</Button>
            ) : (
              <Button type="primary" onClick={start} loading={loading}>开始</Button>
            )}
            <Button onClick={reset} loading={loading}>重置</Button>
          </div>
        </Card>
        <Typography.Text type="secondary" style={{ marginTop: 16 }}>
          专注每一刻，成就更好的自己
        </Typography.Text>
      </Center>
    </>
  );
} 
import React, { useEffect, useRef } from 'react';
import {playBeep, SOUND_PRESETS} from '../utils/audioUtils';
import styled from 'styled-components';

const Time = styled.div`
  font-size: 3rem;
  font-weight: bold;
  color: #fff;
  margin-top: 1rem;
`;

const State = styled.div`
  font-size: 1.2rem;
  color: #fff;
  margin-bottom: 0.5rem;
`;

export default function TimerDisplay({ info }) {

    const lastStateRef = useRef(null);

    useEffect(() => {
        if (!info) return;

        // 检测是否切换到休息状态
        if (lastStateRef.current &&
            lastStateRef.current !== info.currentState &&
            (info.currentState === 'SHORT_BREAK' || info.currentState === 'LONG_BREAK')) {
            playBeep(SOUND_PRESETS.gentle);
        }

        lastStateRef.current = info.currentState;
    }, [info]);

    if (!info) return null;

  console.log(info);
  return (
    <div>
      <State>
        {info.currentState === 'WORK' || info.currentState === 'WORKING'
          ? '专注中'
          : info.currentState === 'SHORT_BREAK'
          ? '短休息'
          : info.currentState === 'LONG_BREAK'
          ? '长休息'
          : '已暂停'}
      </State>
      <Time>{info.remainingTimeFormatted}</Time>
    </div>
  );
} 
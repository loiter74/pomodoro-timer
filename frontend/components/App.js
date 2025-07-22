import pomodoroImg from '../pomodoroImg/pomodoro.png';

const Tomato = styled.div`
  width: 220px;
  height: 220px;
  margin-bottom: 2rem;
  cursor: pointer;
  transition: box-shadow 0.2s;
  box-shadow: 0 8px 32px rgba(231,76,60,0.15);
  background-image: url(${pomodoroImg});
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
  &:hover {
    box-shadow: 0 12px 40px rgba(231,76,60,0.25);
  }
`; 
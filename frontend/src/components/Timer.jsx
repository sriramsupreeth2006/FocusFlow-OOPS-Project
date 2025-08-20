import { useState, useEffect } from "react";
import "./Timer.css";

export default function Timer({ defaultMinutes = 25 }) {
  const [seconds, setSeconds] = useState(defaultMinutes * 60);
  const [running, setRunning] = useState(false);
  const [customMinutes, setCustomMinutes] = useState(defaultMinutes);

  useEffect(() => {
    let timer;
    if (running && seconds > 0) {
      timer = setInterval(() => setSeconds((s) => s - 1), 1000);
    }
    return () => clearInterval(timer);
  }, [running, seconds]);

  const formatTime = (s) => {
    const m = Math.floor(s / 60);
    const sec = s % 60;
    return `${m.toString().padStart(2, "0")}:${sec.toString().padStart(2, "0")}`;
  };

  const handleSetTimer = (mins) => {
    setRunning(false);
    setCustomMinutes(mins);
    setSeconds(mins * 60);
  };

  return (
    <div className="timer-container">
      <h2 className="timer-title">Focus Timer</h2>

      {/* Timer Box */}
      <div className="timer-box">{formatTime(seconds)}</div>

      {/* Preset Buttons */}
      <div className="preset-buttons">
        <button onClick={() => handleSetTimer(25)}>25 min</button>
        <button onClick={() => handleSetTimer(50)}>50 min</button>
      </div>

      {/* Custom Input */}
      <div className="custom-timer">
        <input
          type="number"
          min="1"
          value={customMinutes}
          onChange={(e) => setCustomMinutes(Number(e.target.value))}
        />
        <button onClick={() => handleSetTimer(customMinutes)}>Set</button>
      </div>

      {/* Controls */}
      <div className="timer-controls">
        <button onClick={() => setRunning(!running)}>
          {running ? "Pause" : "Start"}
        </button>
        <button onClick={() => handleSetTimer(defaultMinutes)}>Reset</button>
      </div>
    </div>
  );
}

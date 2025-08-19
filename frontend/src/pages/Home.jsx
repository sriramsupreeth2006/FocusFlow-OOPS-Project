import React, { useState, useEffect } from "react";
import Tasks from "./Tasks"; 
import "./Home.css";

export default function Home() {
  const [time, setTime] = useState(25 * 60); 
  const [isRunning, setIsRunning] = useState(false);
  const [customMinutes, setCustomMinutes] = useState(25);

  useEffect(() => {
    let timer;
    if (isRunning && time > 0) {
      timer = setInterval(() => setTime((t) => t - 1), 1000);
    }
    return () => clearInterval(timer);
  }, [isRunning, time]);

  const formatTime = (secs) => {
    const minutes = Math.floor(secs / 60);
    const seconds = secs % 60;
    return `${minutes.toString().padStart(2, "0")}:${seconds
      .toString()
      .padStart(2, "0")}`;
  };

  const handleSetTimer = (mins) => {
    setIsRunning(false);
    setCustomMinutes(mins);
    setTime(mins * 60);
  };

  return (
    <div className="home-container">
      <div className="home-box">
        <h2>Focus Timer</h2>
        <div className="timer">{formatTime(time)}</div>


        <div className="preset-buttons">
          <button onClick={() => handleSetTimer(25)}>25 min</button>
          <button onClick={() => handleSetTimer(50)}>50 min</button>
        </div>

        <div className="custom-timer">
          <input
            type="number"
            min="1"
            placeholder="Minutes"
            value={customMinutes}
            onChange={(e) => setCustomMinutes(Number(e.target.value))}
          />
          <button onClick={() => handleSetTimer(customMinutes)}>
            Set Timer
          </button>
        </div>

        <div className="timer-controls">
          <button onClick={() => setIsRunning(!isRunning)}>
            {isRunning ? "Pause" : "Start"}
          </button>
          <button onClick={() => handleSetTimer(customMinutes)}>Reset</button>
        </div>

        <p className="motivation">Stay focused, you got this! </p>
      </div>

      <div className="home-box">
        <h2>Tasks</h2>
        <Tasks />
      </div>
    </div>
  );
}

import { useState, useEffect } from "react";

export default function Timer() {
  const [seconds, setSeconds] = useState(25 * 60);
  const [running, setRunning] = useState(false);

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
    return `${m}:${sec < 10 ? "0" : ""}${sec}`;
  };

  return (
    <div className="p-4 bg-white shadow rounded-lg text-center">
      <h2 className="text-lg font-bold mb-2">Focus Timer</h2>
      <div className="text-3xl font-mono">{formatTime(seconds)}</div>
      <div className="mt-3 space-x-2">
        <button onClick={() => setRunning(!running)} className="bg-green-600 text-white px-4 py-2 rounded">
          {running ? "Pause" : "Start"}
        </button>
        <button onClick={() => { setRunning(false); setSeconds(25 * 60); }} className="bg-red-600 text-white px-4 py-2 rounded">
          Reset
        </button>
      </div>
    </div>
  );
}

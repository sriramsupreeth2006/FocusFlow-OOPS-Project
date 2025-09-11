import React from "react";
import Tasks from "./Tasks";
import Timer from "../components/Timer";
import "./Home.css"



export default function Home() {
  return (
    <div className="home">
      {/* Timer Section */}
      <div className="card timer-card">
        <Timer defaultMinutes={25} />
        <p className="motivation">Stay focused, you got this!</p>
      </div>

      {/* Tasks Section */}
      <div className="card tasks-card">
        <h2 className="title">Tasks</h2>
        <Tasks />
      </div>
    </div>
  );
}

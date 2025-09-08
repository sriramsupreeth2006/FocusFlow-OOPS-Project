import React from "react";
import { NavLink } from "react-router-dom";
import "./Navbar.css";

export default function Navbar() {
  return (
    <aside className="sidebar">
      <h2 className="logo">Focus Flow</h2>
      <ul className="menu">
        <li><NavLink end to="/" className="link"> Home</NavLink></li>
        <li><NavLink to="/tasks" className="link"> Tasks</NavLink></li>
        <li><NavLink to="/stats" className="link"> Stats</NavLink></li>
        <li><NavLink to="/goals" className="link"> Goals</NavLink></li>
        <li><NavLink to="/blocker" className="link"> Blocker</NavLink></li>
        <li><NavLink to="/profile" className="link"> Profile</NavLink></li>
      </ul>
    </aside>
  );
}

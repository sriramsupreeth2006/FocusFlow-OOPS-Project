import { useEffect, useState } from "react";
import { api } from "./api"; // FIX: Corrected import path

export default function AdminDashboard() {
  const [rules, setRules] = useState([]);
  const [pattern, setPattern] = useState("");

  useEffect(() => {
    loadRules();
  }, []);

  async function loadRules() {
    setRules(await api("/api/rules"));
  }

  async function addRule() {
    await api("/api/rules", {
      method: "POST",
      body: JSON.stringify({ type: "BLACKLIST", targetType: "WEBSITE", pattern }),
    });
    setPattern("");
    loadRules();
  }

  async function pushRules() {
    await api("/api/rules/push", { method: "POST" });
    alert("Rules pushed to all clients");
  }

  return (
    <div className="p-4">
      <h1 className="text-xl font-bold">Admin Dashboard</h1>
      <div className="mt-4">
        <input
          className="border p-2 mr-2"
          value={pattern}
          onChange={(e) => setPattern(e.target.value)}
          placeholder="e.g., instagram.com"
        />
        <button onClick={addRule} className="bg-blue-500 text-white px-4 py-2 rounded">
          Add Rule
        </button>
        <button onClick={pushRules} className="ml-2 bg-green-500 text-white px-4 py-2 rounded">
          Push Rules
        </button>
      </div>
      <ul className="mt-4">
        {rules.map((r) => (
          <li key={r.id} className="border p-2 my-1 rounded">
            {r.type} {r.targetType} → {r.pattern}
          </li>
        ))}
      </ul>
    </div>
  );
}

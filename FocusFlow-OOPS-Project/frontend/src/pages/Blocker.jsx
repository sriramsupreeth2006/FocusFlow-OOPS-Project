import { useEffect, useState } from "react";
import { api } from "../api";

export default function Blocker() {
  const [rules, setRules] = useState([]);
  const [pattern, setPattern] = useState("");
  const [type, setType] = useState("BLACKLIST");
  const [busy, setBusy] = useState(false);

  useEffect(() => {
    load();
  }, []);

  async function load() {
    const all = await api("/api/rules");
    setRules(all.filter((r) => r.targetType === "APP"));
  }

  async function addRule() {
    if (!pattern.trim()) return;
    setBusy(true);
    try {
      await api("/api/rules", {
        method: "POST",
        body: JSON.stringify({ type, targetType: "APP", pattern: pattern.trim(), enabled: true }),
      });
      setPattern("");
      await load();
    } finally { setBusy(false); }
  }

  async function delRule(id) {
    setBusy(true);
    try {
      await api(`/api/rules/${id}`, { method: "DELETE" });
      await load();
    } finally { setBusy(false); }
  }

  async function pushAll() {
    setBusy(true);
    try {
      await api("/api/rules/push", { method: "POST" });
      alert("Pushed rules to all connected agents");
    } finally { setBusy(false); }
  }

  return (
    <div className="p-4">
      <h1 className="text-xl font-bold">App Blocker</h1>
      <p className="text-sm text-gray-600">Add an app/process name to block, e.g., chrome.exe, notepad.exe</p>

      <div className="mt-4 flex gap-2 items-center">
        <select value={type} onChange={(e) => setType(e.target.value)} className="border p-2">
          <option value="BLACKLIST">BLACKLIST</option>
          <option value="WHITELIST">WHITELIST</option>
        </select>
        <input
          className="border p-2 flex-1"
          value={pattern}
          onChange={(e) => setPattern(e.target.value)}
          placeholder="e.g., chrome.exe"
        />
        <button disabled={busy} onClick={addRule} className="bg-blue-600 text-white px-4 py-2 rounded">
          Add
        </button>
        <button disabled={busy} onClick={pushAll} className="bg-green-600 text-white px-4 py-2 rounded">
          Push to Agents
        </button>
      </div>

      <ul className="mt-4">
        {rules.length === 0 && <li className="text-gray-500">No app rules yet</li>}
        {rules.map((r) => (
          <li key={r.id} className="border p-2 my-1 rounded flex items-center justify-between">
            <span>
              <b>{r.type}</b> APP → {r.pattern}
            </span>
            <button disabled={busy} onClick={() => delRule(r.id)} className="bg-red-600 text-white px-3 py-1 rounded">
              Delete
            </button>
          </li>
        ))}
      </ul>
    </div>
  );
}

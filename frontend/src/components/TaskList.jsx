import { useState } from "react";

export default function TaskList() {
  const [tasks, setTasks] = useState([]);
  const [input, setInput] = useState("");

  const addTask = () => {
    if (!input.trim()) return;
    setTasks([...tasks, { text: input, done: false }]);
    setInput("");
  };

  const toggleTask = (index) => {
    const newTasks = [...tasks];
    newTasks[index].done = !newTasks[index].done;
    setTasks(newTasks);
  };

  return (
    <div className="p-4 bg-white shadow rounded-lg">
      <h2 className="text-lg font-bold mb-2">Tasks</h2>
      <div className="flex gap-2">
        <input
          value={input}
          onChange={(e) => setInput(e.target.value)}
          className="border p-2 flex-1 rounded"
          placeholder="Add a new task..."
        />
        <button onClick={addTask} className="bg-blue-600 text-white px-4 py-2 rounded">
          Add
        </button>
      </div>
      <ul className="mt-3">
        {tasks.map((task, i) => (
          <li
            key={i}
            onClick={() => toggleTask(i)}
            className={`cursor-pointer p-2 rounded ${task.done ? "line-through text-gray-400" : ""}`}
          >
            {task.text}
          </li>
        ))}
      </ul>
    </div>
  );
}

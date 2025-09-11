// Base URL of your backend (configurable via Vite env)
export const API = import.meta.env.VITE_API_URL || "http://localhost:8081";

// Admin token expected by backend (x-admin-token)
export const ADMIN_TOKEN = import.meta.env.VITE_ADMIN_TOKEN || "FOCUSFLOW_ADMIN_TOKEN";

// Common API wrapper
export async function api(path, opts = {}) {
  const res = await fetch(`${API}${path}`, {
    ...opts,
    headers: {
      "Content-Type": "application/json",
      "x-admin-token": ADMIN_TOKEN,
      ...(opts.headers || {}),
    },
  });

  const text = await res.text();                 // read as text first
  if (!res.ok) {
    throw new Error(text || res.statusText);
  }
  return text ? JSON.parse(text) : null;         // safely handle 204/empty
}

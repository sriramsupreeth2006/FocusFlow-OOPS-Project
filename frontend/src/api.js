// Base URL of your backend
export const API = "http://localhost:8080";

// Replace this with the actual admin token your backend expects
// (example: "supersecret123" or JWT depending on backend setup)
export const ADMIN_TOKEN = "supersecret123";  

// Common API wrapper
export async function api(path, opts = {}) {
  const res = await fetch(`${API}${path}`, {
    ...opts,
    headers: {
      "Content-Type": "application/json",
      "x-admin-token": ADMIN_TOKEN,  // custom admin header
      ...(opts.headers || {}),
    },
  });

  if (!res.ok) {
    const errorMessage = await res.text();
    throw new Error(errorMessage);
  }

  return res.json();
}

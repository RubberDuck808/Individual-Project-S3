// Error
async function readError(res) {
  const contentType = res.headers.get("content-type") || "";
  try {
    if (contentType.includes("application/json")) {
      const data = await res.json();
      // Common patterns: {error:"..."}, {message:"..."} etc.
      return data.error || data.message || JSON.stringify(data);
    }
    return await res.text();
  } catch {
    return "Request failed";
  }
}

// Register
export async function register(username, email, password, name) {
  const res = await fetch(`${import.meta.env.VITE_API_URL}/api/users/register`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ username, email, password, name }),
  });

  if (!res.ok) {
    throw new Error((await readError(res)) || "Registration failed");
  }

  return res.json();
}


// Login
export async function login(email, password) {
  const res = await fetch(`${import.meta.env.VITE_API_URL}/api/users/login`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ email, password }),
  });

  if (!res.ok) {
    throw new Error((await readError(res)) || "Login failed");
  }

  const data = await res.json();

  // Store authentication data
  localStorage.setItem("token", data.token);
  localStorage.setItem("user", JSON.stringify(data.user));

  return data;
}

// Authentication fetch helper
export async function authFetch(path, options = {}) {
  const token = localStorage.getItem("token");
  if (!token) throw new Error("Not authenticated");

  const res = await fetch(`${import.meta.env.VITE_API_URL}${path}`, {
    ...options,
    headers: {
      ...(options.headers || {}),
      "Content-Type": "application/json",
      Authorization: `Bearer ${token}`,
    },
  });

  if (!res.ok) {
    throw new Error((await readError(res)) || "Request failed");
  }

  if (res.status === 204) return null;

  const contentType = res.headers.get("content-type") || "";
  if (contentType.includes("application/json")) return res.json();
  return res.text();
}

// Get current user
export async function fetchCurrentUser() {
  const user = await authFetch("/api/users/me");

  // Keep localStorage in sync (nice to have)
  localStorage.setItem("user", JSON.stringify(user));
  return user;
}

// Logged user helper
export function logout() {
  localStorage.removeItem("token");
  localStorage.removeItem("user");
}

export function getStoredUser() {
  const raw = localStorage.getItem("user");
  if (!raw) return null;
  try {
    return JSON.parse(raw);
  } catch {
    return null;
  }
}

export function isLoggedIn() {
  return !!localStorage.getItem("token");
}

export function getAuthHeader() {
  const token = localStorage.getItem("token");
  if (!token) throw new Error("Not authenticated");
  return { Authorization: `Bearer ${token}` };
}

export function getStoredUserId() {
  const u = getStoredUser();
  return u?.id ?? u?.userId ?? null;
}

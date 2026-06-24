async function readError(res) {
  const contentType = res.headers.get("content-type") || "";
  try {
    if (contentType.includes("application/json")) {
      const data = await res.json();
      return data.error || data.message || JSON.stringify(data);
    }
    return await res.text();
  } catch {
    return "Request failed";
  }
}

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

  sessionStorage.setItem("token", data.token);
  sessionStorage.setItem("user", JSON.stringify(data.user));

  return data;
}

export async function authFetch(path, options = {}) {
  const token = sessionStorage.getItem("token");
  if (!token) throw new Error("Not authenticated");

  const defaultHeaders = {
    "Content-Type": "application/json",
    Authorization: `Bearer ${token}`,
  };

  const res = await fetch(`${import.meta.env.VITE_API_URL}${path}`, {
    ...options,
    headers: {
      ...defaultHeaders,
      ...options.headers,
    },
  });

  if (res.status === 401) {
    // Token expired or invalid — clear session and redirect to login
    sessionStorage.removeItem("token");
    sessionStorage.removeItem("user");
    window.location.href = "/login";
    throw new Error("Session expired. Please log in again.");
  }

  if (!res.ok) {
    throw new Error((await readError(res)) || "Request failed");
  }

  if (res.status === 204) return null;

  const contentType = res.headers.get("content-type") || "";
  if (contentType.includes("application/json")) return res.json();
  return res.text();
}

export async function fetchCurrentUser() {
  const user = await authFetch("/api/users/me");
  sessionStorage.setItem("user", JSON.stringify(user));
  return user;
}

export function logout() {
  sessionStorage.removeItem("token");
  sessionStorage.removeItem("user");
}

export function getStoredUser() {
  const raw = sessionStorage.getItem("user");
  if (!raw) return null;
  try {
    return JSON.parse(raw);
  } catch {
    return null;
  }
}

export function isLoggedIn() {
  return !!sessionStorage.getItem("token");
}

export function getAuthHeader() {
  const token = sessionStorage.getItem("token");
  if (!token) throw new Error("Not authenticated");
  return { Authorization: `Bearer ${token}` };
}

export function getStoredUserId() {
  const u = getStoredUser();
  return u?.id ?? u?.userId ?? null;
}

export async function login(email, password) {
  const res = await fetch(
    `${import.meta.env.VITE_API_URL}/api/users/login`,
    {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ email, password }),
    }
  );

  if (!res.ok) {
    throw new Error(await res.text() || "Login failed");
  }

  const data = await res.json();

  // Store authentication data
  localStorage.setItem("token", data.token);
  localStorage.setItem("user", JSON.stringify(data.user));

  return data;
}

export async function authFetch(url, options = {}) {
  const token = localStorage.getItem("token");

  if (!token) {
    throw new Error("Not authenticated");
  }

  const res = await fetch(`${import.meta.env.VITE_API_URL}${url}`, {
    ...options,
    headers: {
      "Content-Type": "application/json",
      ...(options.headers || {}),
      Authorization: `Bearer ${token}`,
    }
  });

  if (!res.ok) {
    throw new Error(await res.text() || "Request failed");
  }

  return res.json();
}


export function logout() {
  localStorage.removeItem("token");
  localStorage.removeItem("user");
}

export function getCurrentUser() {
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

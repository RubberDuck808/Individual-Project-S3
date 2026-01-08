import { authFetch } from "./auth";

export async function fetchAvatars() {
  return authFetch("/api/avatars");
}

export async function changeMyAvatar(avatarName) {
  return authFetch("/api/users/me/avatar", {
    method: "PUT",
    body: JSON.stringify({ avatarName }),
  });
}

// Fetch image
export async function fetchAvatarBlobByPath(imagePath) {
  const token = localStorage.getItem("token");
  if (!token) throw new Error("Not authenticated");

  const res = await fetch(
    `${import.meta.env.VITE_API_URL}/api/storage/object?path=${encodeURIComponent(imagePath)}`,
    {
      method: "GET",
      headers: { Authorization: `Bearer ${token}` },
    }
  );

  if (!res.ok) {
    const msg = await res.text().catch(() => "Failed to load avatar image");
    throw new Error(msg);
  }

  return res.blob();
}

import { authFetch } from "./auth";

/**
 * Send friend request by username
 * POST /api/friendships/request
 * body: { username }
 */
export function sendFriendRequest(username) {
  return authFetch("/api/friendships/request", {
    method: "POST",
    body: JSON.stringify({ username }),
  });
}

/**
 * Accept incoming request from username
 * POST /api/friendships/accept/{username}
 */
export function acceptFriendRequest(username) {
  return authFetch(`/api/friendships/accept/${encodeURIComponent(username)}`, {
    method: "POST",
  });
}

/**
 * Decline incoming request from username
 * DELETE /api/friendships/decline/{username}
 */
export function declineFriendRequest(username) {
  return authFetch(`/api/friendships/decline/${encodeURIComponent(username)}`, {
    method: "DELETE",
  });
}

/**
 * Cancel outgoing request to username
 * DELETE /api/friendships/cancel/{username}
 */
export function cancelFriendRequest(username) {
  return authFetch(`/api/friendships/cancel/${encodeURIComponent(username)}`, {
    method: "DELETE",
  });
}

/**
 * Unfriend username
 * DELETE /api/friendships/unfriend/{username}
 */
export function unfriend(username) {
  return authFetch(`/api/friendships/unfriend/${encodeURIComponent(username)}`, {
    method: "DELETE",
  });
}

/**
 * Lists
 */
export function getIncomingRequests() {
  return authFetch("/api/friendships/requests/incoming");
}

export function getOutgoingRequests() {
  return authFetch("/api/friendships/requests/outgoing");
}

export function getFriends() {
  return authFetch("/api/friendships");
}

export function getFriendsOfUser(username) {
  return authFetch(`/api/friendships/user/${encodeURIComponent(username)}`);
}

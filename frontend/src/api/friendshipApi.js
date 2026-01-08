import { authFetch } from "./auth";

// Send request
export function sendFriendRequest(username) {
  return authFetch("/api/friendships/request", {
    method: "POST",
    body: JSON.stringify({ username }),
  });
}

// Accept request
export function acceptFriendRequest(username) {
  return authFetch(`/api/friendships/accept/${encodeURIComponent(username)}`, {
    method: "POST",
  });
}

// Decline request
export function declineFriendRequest(username) {
  return authFetch(`/api/friendships/decline/${encodeURIComponent(username)}`, {
    method: "DELETE",
  });
}

// Cancel request
export function cancelFriendRequest(username) {
  return authFetch(`/api/friendships/cancel/${encodeURIComponent(username)}`, {
    method: "DELETE",
  });
}

// Unfriend
export function unfriend(username) {
  return authFetch(`/api/friendships/unfriend/${encodeURIComponent(username)}`, {
    method: "DELETE",
  });
}

// Lists
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

import React, { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import {
  sendFriendRequest,
  acceptFriendRequest,
  declineFriendRequest,
  cancelFriendRequest,
  unfriend,
  getIncomingRequests,
  getOutgoingRequests,
  getFriends,
  getFriendsOfUser,
} from "../../api/friendships";

function otherUser(f, usernameForPerspective) {
  return f.requesterUsername === usernameForPerspective
    ? f.addresseeUsername
    : f.requesterUsername;
}

export default function FriendsTab({ user, profileUser, isMe }) {
  const navigate = useNavigate();

  const myUsername = useMemo(() => user?.username, [user]);
  const profileUsername = useMemo(() => profileUser?.username, [profileUser]);

  const [incoming, setIncoming] = useState([]);
  const [outgoing, setOutgoing] = useState([]);
  const [friends, setFriends] = useState([]);

  const [username, setUsername] = useState("");
  const [msg, setMsg] = useState("");
  const [err, setErr] = useState("");
  const [loading, setLoading] = useState(true);

  async function reload() {
    setErr("");
    setLoading(true);

    try {
      if (isMe) {
        const [inc, out, fr] = await Promise.all([
          getIncomingRequests(),
          getOutgoingRequests(),
          getFriends(),
        ]);
        setIncoming(inc);
        setOutgoing(out);
        setFriends(fr);
      } else {
        const fr = await getFriendsOfUser(profileUsername);
        setIncoming([]);
        setOutgoing([]);
        setFriends(fr);
      }
    } catch (e) {
      setErr(e?.message || "Something went wrong");
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    reload();
  }, [isMe, profileUsername]);

  async function onSend(e) {
    e.preventDefault();
    setErr("");
    setMsg("");

    const u = username.trim();
    if (!u) {
      setErr("Enter a username");
      return;
    }

    try {
      await sendFriendRequest(u);
      setMsg(`Request sent to ${u}`);
      setUsername("");
      await reload();
    } catch (e) {
      setErr(e?.message || "Could not send request");
    }
  }

  async function onAccept(fromUsername) {
    await acceptFriendRequest(fromUsername);
    reload();
  }

  async function onDecline(fromUsername) {
    await declineFriendRequest(fromUsername);
    reload();
  }

  async function onCancel(toUsername) {
    await cancelFriendRequest(toUsername);
    reload();
  }

  async function onUnfriend(u) {
    await unfriend(u);
    reload();
  }

  function goToProfile(username) {
    navigate(`/profile/${username}`);
  }

  if (!profileUsername) return <div className="text-red-500">Missing username.</div>;

  return (
    <div className="mt-6 space-y-6">
      {isMe && (
        <div className="bg-gray-50 dark:bg-gray-800/40 border rounded-2xl p-4">
          <form onSubmit={onSend} className="flex flex-col sm:flex-row gap-3">
            <input
              className="flex-1 px-4 py-3 rounded-xl border"
              placeholder="Add friend by username"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
            />
            <button className="px-5 py-3 rounded-xl bg-blue-600 text-white font-bold">
              Send request
            </button>
          </form>
          {err && <p className="mt-2 text-red-500">{err}</p>}
          {msg && <p className="mt-2 text-green-500">{msg}</p>}
        </div>
      )}

      {loading ? (
        <p className="text-gray-500">Loading friends…</p>
      ) : !isMe ? (
        <div className="bg-white dark:bg-gray-900 rounded-2xl border p-4">
          <h3 className="font-extrabold mb-3">
            Friends of @{profileUsername}
          </h3>

          {friends.length === 0 ? (
            <p className="text-sm opacity-60">No friends to show.</p>
          ) : (
            <ul className="space-y-2">
              {friends.map((f) => {
                const other = otherUser(f, profileUsername);
                return (
                  <li
                    key={`${f.requesterUsername}-${f.addresseeUsername}`}
                    onClick={() => goToProfile(other)}
                    className="cursor-pointer p-3 rounded-xl bg-gray-50 dark:bg-gray-800 hover:bg-gray-100 dark:hover:bg-gray-700 font-semibold"
                  >
                    @{other}
                  </li>
                );
              })}
            </ul>
          )}
        </div>
      ) : (
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-4">
          {/* Incoming */}
          <div className="bg-white dark:bg-gray-900 rounded-2xl border p-4">
            <h3 className="font-extrabold mb-3">Incoming</h3>
            {incoming.map((f) => (
              <div key={f.requesterUsername} className="flex justify-between p-3 bg-gray-50 dark:bg-gray-800 rounded-xl">
                <span
                  onClick={() => goToProfile(f.requesterUsername)}
                  className="cursor-pointer font-semibold hover:underline"
                >
                  @{f.requesterUsername}
                </span>
                <div className="flex gap-2">
                  <button onClick={() => onAccept(f.requesterUsername)}>Accept</button>
                  <button onClick={() => onDecline(f.requesterUsername)}>Decline</button>
                </div>
              </div>
            ))}
          </div>

          {/* Outgoing */}
          <div className="bg-white dark:bg-gray-900 rounded-2xl border p-4">
            <h3 className="font-extrabold mb-3">Outgoing</h3>
            {outgoing.map((f) => (
              <div key={f.addresseeUsername} className="flex justify-between p-3 bg-gray-50 dark:bg-gray-800 rounded-xl">
                <span
                  onClick={() => goToProfile(f.addresseeUsername)}
                  className="cursor-pointer font-semibold hover:underline"
                >
                  @{f.addresseeUsername}
                </span>
                <button onClick={() => onCancel(f.addresseeUsername)}>Cancel</button>
              </div>
            ))}
          </div>

          {/* Friends */}
          <div className="bg-white dark:bg-gray-900 rounded-2xl border p-4">
            <h3 className="font-extrabold mb-3">Friends</h3>
            {friends.map((f) => {
              const other = otherUser(f, myUsername);
              return (
                <div
                  key={`${f.requesterUsername}-${f.addresseeUsername}`}
                  className="flex justify-between p-3 bg-gray-50 dark:bg-gray-800 rounded-xl"
                >
                  <span
                    onClick={() => goToProfile(other)}
                    className="cursor-pointer font-semibold hover:underline"
                  >
                    @{other}
                  </span>
                  <button onClick={() => onUnfriend(other)}>Unfriend</button>
                </div>
              );
            })}
          </div>
        </div>
      )}
    </div>
  );
}

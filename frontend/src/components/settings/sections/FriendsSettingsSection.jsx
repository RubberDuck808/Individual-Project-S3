import React, { useEffect, useState, useMemo } from "react";
import PropTypes from "prop-types";
import {
  sendFriendRequest,
  acceptFriendRequest,
  declineFriendRequest,
  cancelFriendRequest,
  getIncomingRequests,
  getOutgoingRequests,
  getFriends,
} from "../../../api/friendshipApi";
import { useNavigate } from "react-router-dom";
import UserAvatar from "../../profile/avatars/UserAvatar";
import { useMultipleUsers } from "../../../hooks/useUserData";

const cx = (...classes) => classes.filter(Boolean).join(" ");

const FriendButton = ({ children, onClick, variant = "default" }) => {
  const variants = {
    default: "bg-white hover:bg-slate-50",
    danger: "bg-[#FF4545] text-white",
    success: "bg-[#00D1FF] text-black",
  };
  
  return (
    <button
      onClick={onClick}
      className={cx(
        "px-4 py-1.5 rounded-xl border-[3px] border-black text-[10px] font-black uppercase tracking-widest transition-all shadow-[2px_2px_0px_0px_rgba(0,0,0,1)] hover:translate-y-0.5 hover:shadow-none",
        variants[variant]
      )}
    >
      {children}
    </button>
  );
};

export default function FriendsSettingsSection({ me }) {
  const navigate = useNavigate();
  const [incoming, setIncoming] = useState([]);
  const [outgoing, setOutgoing] = useState([]);
  const [friends, setFriends] = useState([]);
  const [username, setUsername] = useState("");
  const [msg, setMsg] = useState("");
  const [err, setErr] = useState("");
  const [loading, setLoading] = useState(true);
  
  // Extract all usernames for batch fetching
  const allUsernames = useMemo(() => {
    const usernames = new Set();
    for (const f of incoming) {
      usernames.add(f.requesterUsername);
    }
    for (const f of outgoing) {
      usernames.add(f.addresseeUsername);
    }
    for (const f of friends) {
      const other = f.requesterUsername === me?.username ? f.addresseeUsername : f.requesterUsername;
      usernames.add(other);
    }
    return Array.from(usernames);
  }, [incoming, outgoing, friends, me?.username]);
  
  // Fetch user data for all users
  const { usersData } = useMultipleUsers(allUsernames);

  async function reload() {
    setErr("");
    setMsg("");
    setLoading(true);
    try {
      const [inc, out, fr] = await Promise.all([
        getIncomingRequests(),
        getOutgoingRequests(),
        getFriends(),
      ]);
      setIncoming(inc);
      setOutgoing(out);
      setFriends(fr);
    } catch (e) {
      const errorMessage = e?.message || "Something went wrong";
      console.error("Failed to reload friends data:", e);
      setErr(errorMessage);
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    reload();
  }, []);

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
      const errorMessage = e?.message || "Could not send request";
      console.error("Failed to send friend request:", e);
      setErr(errorMessage);
    }
  }

  const goToProfile = (un) => {
    if (un?.trim()) {
      navigate(`/profile/${un.trim()}`);
    }
  };

  return (
    <div className="w-full space-y-8">
      {/* ADD FRIEND SECTION */}
      <div className="bg-slate-50 border-[3px] border-black rounded-[2rem] p-6">
        <h3 className="text-xs font-[1000] uppercase tracking-[0.2em] mb-4 text-slate-400">
          Expand Network
        </h3>
        <form onSubmit={onSend} className="flex flex-col sm:flex-row gap-4">
          <input
            className="flex-1 px-5 py-4 rounded-2xl border-[3px] border-black font-bold focus:outline-none bg-white transition-colors"
            placeholder="Enter username handle..."
            value={username}
            onChange={(e) => setUsername(e.target.value)}
          />
          <button
            type="submit"
            className="px-8 py-4 rounded-2xl bg-[#FF6AC1] text-white font-[1000] uppercase tracking-widest border-[3px] border-black shadow-[4px_4px_0px_0px_rgba(0,0,0,1)] hover:translate-y-0.5 hover:shadow-none transition-all"
          >
            Send Request
          </button>
        </form>
        {err && (
          <p className="mt-3 text-[#FF4545] font-black text-xs uppercase italic">
            {err}
          </p>
        )}
        {msg && (
          <p className="mt-3 text-[#00D1FF] font-black text-xs uppercase italic">
            {msg}
          </p>
        )}
      </div>

      {loading ? (
        <div className="font-black animate-pulse uppercase text-slate-400">
          Accessing Database...
        </div>
      ) : (
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
          {/* INCOMING REQUESTS */}
          <div className="bg-slate-50 border-[3px] border-black rounded-[2.5rem] p-6">
            <h3 className="font-[1000] uppercase italic tracking-tighter text-xl mb-4">
              Incoming
            </h3>
            <div className="space-y-3">
              {incoming.length === 0 && (
                <p className="text-xs font-bold text-slate-300">Empty</p>
              )}
              {incoming.map((f) => {
                const userData = usersData.get(f.requesterUsername);
                return (
                  <div
                    key={f.requesterUsername}
                    className="flex flex-col gap-3 p-4 bg-white border-[3px] border-black rounded-2xl"
                  >
                    <button
                      type="button"
                      onClick={() => goToProfile(f.requesterUsername)}
                      className="flex items-center gap-3 cursor-pointer hover:text-[#FF6AC1] transition-colors text-left bg-transparent border-none p-0"
                    >
                      <UserAvatar user={userData} size={40} className="flex-shrink-0" />
                      <span className="font-black">@{f.requesterUsername}</span>
                    </button>
                    <div className="flex gap-2">
                      <FriendButton
                        variant="success"
                        onClick={() =>
                          acceptFriendRequest(f.requesterUsername).then(reload)
                        }
                      >
                        Accept
                      </FriendButton>
                      <FriendButton
                        onClick={() =>
                          declineFriendRequest(f.requesterUsername).then(reload)
                        }
                      >
                        Ignore
                      </FriendButton>
                    </div>
                  </div>
                );
              })}
            </div>
          </div>

          {/* OUTGOING REQUESTS */}
          <div className="bg-slate-50 border-[3px] border-black rounded-[2.5rem] p-6">
            <h3 className="font-[1000] uppercase italic tracking-tighter text-xl mb-4">
              Pending
            </h3>
            <div className="space-y-3">
              {outgoing.length === 0 && (
                <p className="text-xs font-bold text-slate-300">Empty</p>
              )}
              {outgoing.map((f) => {
                const userData = usersData.get(f.addresseeUsername);
                return (
                  <div
                    key={f.addresseeUsername}
                    className="flex justify-between items-center p-4 bg-white border-[3px] border-black rounded-2xl gap-3"
                  >
                    <button
                      type="button"
                      onClick={() => goToProfile(f.addresseeUsername)}
                      className="flex items-center gap-3 cursor-pointer hover:text-[#FF6AC1] transition-colors flex-1 min-w-0 text-left bg-transparent border-none p-0"
                    >
                      <UserAvatar user={userData} size={40} className="flex-shrink-0" />
                      <span className="font-black truncate">@{f.addresseeUsername}</span>
                    </button>
                    <FriendButton
                      variant="danger"
                      onClick={() =>
                        cancelFriendRequest(f.addresseeUsername).then(reload)
                      }
                    >
                      Cancel
                    </FriendButton>
                  </div>
                );
              })}
            </div>
          </div>

          {/* FRIENDS LIST */}
          <div className="bg-slate-50 border-[3px] border-black rounded-[2.5rem] p-6">
            <h3 className="font-[1000] uppercase italic tracking-tighter text-xl mb-4">
              Friends
            </h3>
            <div className="space-y-3">
              {friends.length === 0 && (
                <p className="text-xs font-bold text-slate-300">
                  No friends yet
                </p>
              )}
              {friends.map((f) => {
                const other =
                  f.requesterUsername === me?.username
                    ? f.addresseeUsername
                    : f.requesterUsername;
                const userData = usersData.get(other);
                return (
                  <div
                    key={other}
                    className="flex justify-between items-center p-4 bg-white border-[3px] border-black rounded-2xl gap-3"
                  >
                    <button
                      type="button"
                      onClick={() => goToProfile(other)}
                      className="flex items-center gap-3 cursor-pointer hover:text-[#FF6AC1] transition-colors flex-1 min-w-0 text-left bg-transparent border-none p-0"
                    >
                      <UserAvatar user={userData} size={40} className="flex-shrink-0" />
                      <span className="font-black truncate">@{other}</span>
                    </button>
                  </div>
                );
              })}
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

FriendsSettingsSection.propTypes = {
  me: PropTypes.shape({
    username: PropTypes.string,
  }),
};

FriendButton.propTypes = {
  children: PropTypes.node.isRequired,
  onClick: PropTypes.func.isRequired,
  variant: PropTypes.oneOf(["default", "danger", "success"]),
};

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
} from "../../../api/friendshipApi";

// Utility for cleaner class merging
const cx = (...classes) => classes.filter(Boolean).join(" ");

function otherUser(f, usernameForPerspective) {
  return f.requesterUsername === usernameForPerspective
    ? f.addresseeUsername
    : f.requesterUsername;
}

// Reusable Button Component for consistency
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

  useEffect(() => { reload(); }, [isMe, profileUsername]);

  async function onSend(e) {
    e.preventDefault();
    setErr(""); setMsg("");
    const u = username.trim();
    if (!u) { setErr("Enter a username"); return; }
    try {
      await sendFriendRequest(u);
      setMsg(`Request sent to ${u}`);
      setUsername("");
      await reload();
    } catch (e) { setErr(e?.message || "Could not send request"); }
  }

  const goToProfile = (un) => navigate(`/profile/${un}`);

  if (!profileUsername) return <div className="text-[#FF4545] font-black">SYSTEM ERROR: Missing username.</div>;

  return (
    <div className="mt-8 space-y-8">
      {/* ADD FRIEND SECTION */}
      {isMe && (
        <div className="bg-white border-[3px] border-black rounded-[2rem] p-6 shadow-[8px_8px_0px_0px_rgba(0,0,0,1)]">
          <h3 className="text-xs font-[1000] uppercase tracking-[0.2em] mb-4 text-slate-400">Expand Network</h3>
          <form onSubmit={onSend} className="flex flex-col sm:flex-row gap-4">
            <input
              className="flex-1 px-5 py-4 rounded-2xl border-[3px] border-black font-bold focus:outline-none bg-slate-50 focus:bg-white transition-colors"
              placeholder="Enter username handle..."
              value={username}
              onChange={(e) => setUsername(e.target.value)}
            />
            <button className="px-8 py-4 rounded-2xl bg-[#FF6AC1] text-white font-[1000] uppercase tracking-widest border-[3px] border-black shadow-[4px_4px_0px_0px_rgba(0,0,0,1)] hover:translate-y-0.5 hover:shadow-none transition-all">
              Send Request
            </button>
          </form>
          {err && <p className="mt-3 text-[#FF4545] font-black text-xs uppercase italic">{err}</p>}
          {msg && <p className="mt-3 text-[#00D1FF] font-black text-xs uppercase italic">{msg}</p>}
        </div>
      )}

      {loading ? (
        <div className="font-black animate-pulse uppercase text-slate-400">Accessing Database...</div>
      ) : (
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
          
          {/* INCOMING REQUESTS */}
          {isMe && (
            <div className="bg-white border-[3px] border-black rounded-[2.5rem] p-6 shadow-[8px_8px_0px_0px_rgba(0,0,0,1)]">
              <h3 className="font-[1000] uppercase italic tracking-tighter text-xl mb-4">Incoming</h3>
              <div className="space-y-3">
                {incoming.length === 0 && <p className="text-xs font-bold text-slate-300">Empty</p>}
                {incoming.map((f) => (
                  <div key={f.requesterUsername} className="flex flex-col gap-3 p-4 bg-slate-50 border-[3px] border-black rounded-2xl">
                    <span onClick={() => goToProfile(f.requesterUsername)} className="cursor-pointer font-black hover:text-[#FF6AC1] transition-colors">
                      @{f.requesterUsername}
                    </span>
                    <div className="flex gap-2">
                      <FriendButton variant="success" onClick={() => acceptFriendRequest(f.requesterUsername).then(reload)}>Accept</FriendButton>
                      <FriendButton onClick={() => declineFriendRequest(f.requesterUsername).then(reload)}>Ignore</FriendButton>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          )}

          {/* OUTGOING REQUESTS */}
          {isMe && (
            <div className="bg-white border-[3px] border-black rounded-[2.5rem] p-6 shadow-[8px_8px_0px_0px_rgba(0,0,0,1)]">
              <h3 className="font-[1000] uppercase italic tracking-tighter text-xl mb-4">Pending</h3>
              <div className="space-y-3">
                {outgoing.length === 0 && <p className="text-xs font-bold text-slate-300">Empty</p>}
                {outgoing.map((f) => (
                  <div key={f.addresseeUsername} className="flex justify-between items-center p-4 bg-slate-50 border-[3px] border-black rounded-2xl">
                    <span onClick={() => goToProfile(f.addresseeUsername)} className="cursor-pointer font-black hover:text-[#FF6AC1] transition-colors">
                      @{f.addresseeUsername}
                    </span>
                    <FriendButton variant="danger" onClick={() => cancelFriendRequest(f.addresseeUsername).then(reload)}>Cancel</FriendButton>
                  </div>
                ))}
              </div>
            </div>
          )}

          {/* FRIENDS LIST */}
          <div className={cx(
            "bg-white border-[3px] border-black rounded-[2.5rem] p-6 shadow-[8px_8px_0px_0px_rgba(0,0,0,1)]",
            !isMe && "lg:col-span-3"
          )}>
            <h3 className="font-[1000] uppercase italic tracking-tighter text-xl mb-4">
              {isMe ? "Friends" : `Connections of @${profileUsername}`}
            </h3>
            <div className={cx("grid gap-3", !isMe && "sm:grid-cols-2 lg:grid-cols-3")}>
              {friends.length === 0 && <p className="text-xs font-bold text-slate-300">No connections found.</p>}
              {friends.map((f) => {
                const other = otherUser(f, isMe ? myUsername : profileUsername);
                return (
                  <div key={other} className="flex justify-between items-center p-4 bg-slate-50 border-[3px] border-black rounded-2xl">
                    <span onClick={() => goToProfile(other)} className="cursor-pointer font-black hover:text-[#FF6AC1] transition-colors">
                      @{other}
                    </span>
                    {isMe && (
                      <FriendButton variant="danger" onClick={() => unfriend(other).then(reload)}>Remove</FriendButton>
                    )}
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
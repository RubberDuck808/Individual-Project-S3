import React, { useEffect, useMemo, useState } from "react";
import PropTypes from "prop-types";
import { Link } from "react-router-dom";
import {
  unfriend,
  getFriends,
  getFriendsOfUser,
} from "../../../api/friendshipApi";
import UserAvatar from "../avatars/UserAvatar";
import { useMultipleUsers } from "../../../hooks/useUserData";

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

FriendButton.propTypes = {
  children: PropTypes.node.isRequired,
  onClick: PropTypes.func.isRequired,
  variant: PropTypes.oneOf(["default", "danger", "success"]),
};

// Friend Item Component with Avatar
function FriendItem({ username, isMe, onRemove, userData }) {
  const friendUser = userData?.get(username);

  return (
    <div className="flex justify-between items-center p-4 bg-slate-50 border-[3px] border-black rounded-2xl gap-3">
      <Link
        to={`/profile/${username}`}
        className="flex items-center gap-3 cursor-pointer hover:text-[#FF6AC1] transition-colors focus:outline-none focus:ring-2 focus:ring-[#FF6AC1] rounded flex-1 min-w-0"
      >
        <UserAvatar user={friendUser} size={40} className="flex-shrink-0" />
        <span className="font-black truncate">@{username}</span>
      </Link>
      {isMe && (
        <FriendButton 
          variant="danger" 
          onClick={(e) => { 
            e.preventDefault();
            e.stopPropagation(); 
            onRemove(); 
          }}
        >
          Remove
        </FriendButton>
      )}
    </div>
  );
}

FriendItem.propTypes = {
  username: PropTypes.string.isRequired,
  isMe: PropTypes.bool.isRequired,
  onRemove: PropTypes.func.isRequired,
  userData: PropTypes.instanceOf(Map),
};

export default function FriendsTab({ user, profileUser, isMe }) {
  const myUsername = useMemo(() => user?.username, [user]);
  const profileUsername = useMemo(() => profileUser?.username, [profileUser]);

  const [friends, setFriends] = useState([]);
  const [loading, setLoading] = useState(true);
  
  // Extract all friend usernames for batch fetching
  const friendUsernames = useMemo(() => {
    const perspectiveUsername = isMe ? myUsername : profileUsername;
    return friends
      .map((f) => otherUser(f, perspectiveUsername))
      .filter(Boolean);
  }, [friends, isMe, myUsername, profileUsername]);
  
  // Fetch user data for all friends
  const { usersData } = useMultipleUsers(friendUsernames);

  async function reload() {
    setLoading(true);
    try {
      if (isMe) {
        const fr = await getFriends();
        setFriends(fr);
      } else {
        const fr = await getFriendsOfUser(profileUsername);
        setFriends(fr);
      }
    } catch (e) {
      console.error("Failed to load friends:", e);
      setFriends([]);
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => { reload(); }, [isMe, profileUsername]);

  if (!profileUsername) return <div className="text-[#FF4545] font-black">SYSTEM ERROR: Missing username.</div>;

  return (
    <div className="mt-8 space-y-8">
      {loading ? (
        <div className="font-black animate-pulse uppercase text-slate-400">Accessing Database...</div>
      ) : (
        <div className="bg-white border-[3px] border-black rounded-[2.5rem] p-6 shadow-[8px_8px_0px_0px_rgba(0,0,0,1)]">
          <h3 className="font-[1000] uppercase italic tracking-tighter text-xl mb-4">
            {isMe ? "Friends" : `Connections of @${profileUsername}`}
          </h3>
          <div className={cx("grid gap-3", !isMe && "sm:grid-cols-2 lg:grid-cols-3")}>
            {friends.length === 0 && (
              <p className="text-xs font-bold text-slate-300 col-span-full">
                {isMe ? "No friends yet. Add friends in Settings!" : "No connections found."}
              </p>
            )}
            {friends.map((f) => {
              const perspectiveUsername = isMe ? myUsername : profileUsername;
              const other = otherUser(f, perspectiveUsername);
              
              if (!other) {
                console.warn('FriendsTab: Could not determine other user from friendship', f);
                return null;
              }
              
              return (
                <FriendItem
                  key={other}
                  username={other}
                  isMe={isMe}
                  onRemove={() => unfriend(other).then(reload)}
                  userData={usersData}
                />
              );
            })}
          </div>
        </div>
      )}
    </div>
  );
}

FriendsTab.propTypes = {
  user: PropTypes.shape({
    username: PropTypes.string,
  }),
  profileUser: PropTypes.shape({
    username: PropTypes.string,
  }),
  isMe: PropTypes.bool.isRequired,
};
import PropTypes from "prop-types";
import { useEffect, useState } from "react";
import { submitVote, getVoteCounts } from "../api/voteApi";

export default function VotePanel({ hazardId, onClose }) {
  const [votes, setVotes] = useState({ upvotes: 0, downvotes: 0 });
  const [loading, setLoading] = useState(false);
  const user = JSON.parse(localStorage.getItem("user"));

  useEffect(() => {
    if (hazardId) loadVotes();
  }, [hazardId]);

  const loadVotes = async () => {
    if (!hazardId) return;
    const data = await getVoteCounts(hazardId);
    setVotes(data);
  };

  const handleVote = async (type) => {
    if (!user) return alert("You must be logged in to vote");
    setLoading(true);

    try {
      await submitVote(user.id, hazardId, type);
      await loadVotes();
    } catch (err) {
      alert(err.message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="fixed bottom-40 right-7 bg-white dark:bg-gray-800 p-4 rounded-xl shadow-xl w-64 z-50">
      <div className="flex justify-between items-center mb-2">
        <h3 className="font-bold text-lg">Vote</h3>
        <button onClick={onClose} className="text-xl">✕</button>
      </div>

      <div className="flex items-center justify-between">
        <button
          disabled={loading || !hazardId}
          onClick={() => handleVote("UPVOTE")}
          className="px-4 py-2 bg-green-600 text-white rounded-lg"
        >
          Up {votes.upvotes}
        </button>

        <button
          disabled={loading || !hazardId}
          onClick={() => handleVote("DOWNVOTE")}
          className="px-4 py-2 bg-red-600 text-white rounded-lg"
        >
          Down {votes.downvotes}
        </button>
      </div>
    </div>
  );
}

// Prop validation
VotePanel.propTypes = {
  hazardId: PropTypes.oneOfType([PropTypes.string, PropTypes.number]).isRequired,
  onClose: PropTypes.func.isRequired
};

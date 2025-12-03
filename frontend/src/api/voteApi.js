export async function submitVote(userId, hazardId, voteType) {
  const res = await fetch(`${import.meta.env.VITE_API_URL}/api/votes`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({
      userId,
      hazardId,
      voteType, // "UPVOTE" or "DOWNVOTE"
    }),
  });

  if (!res.ok) {
    throw new Error(await res.text());
  }

  return res.json(); // VoteDTO
}

export async function getVoteCounts(hazardId) {
  const res = await fetch(`${import.meta.env.VITE_API_URL}/api/votes/${hazardId}/count`);

  if (!res.ok) {
    throw new Error("Failed to fetch vote counts");
  }

  const text = await res.text(); // "Upvotes: X, Downvotes: Y"
  const [up, down] = text.match(/\d+/g).map(Number);

  return { upvotes: up, downvotes: down };
}

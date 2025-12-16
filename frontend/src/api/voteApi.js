import { authFetch } from "./auth";

export async function submitVote(userId, hazardId, voteType) {
  return authFetch(`/api/votes`, {
    method: "POST",
    body: JSON.stringify({
      userId,
      hazardId,
      voteType
    }),
  });
}

export async function getVoteCounts(hazardId) {
  return authFetch(`/api/votes/${hazardId}/count`);
}

import { authFetch } from "./auth";

export async function submitVote(hazardId, voteType) {
  return authFetch(`/api/votes`, {
    method: "POST",
    body: JSON.stringify({ hazardId, voteType }),
  });
}

export async function getVoteCounts(hazardId) {
  return authFetch(`/api/votes/${hazardId}/count`);
}

export async function getTotalVotesCastForUser(username) {
  return authFetch(`/api/votes/user/${encodeURIComponent(username)}/cast`);
}


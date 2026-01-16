import { useState, useEffect } from "react";
import { fetchUserByUsername } from "../api/userApi";

export function useUserData(username) {
  const [userData, setUserData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    if (!username) {
      setUserData(null);
      setLoading(false);
      return;
    }

    let cancelled = false;
    setLoading(true);
    setError(null);

    fetchUserByUsername(username)
      .then((user) => {
        if (!cancelled) {
          setUserData(user);
          setError(null);
        }
      })
      .catch((e) => {
        if (!cancelled) {
          setError(e?.message || "Failed to load user");
          setUserData(null);
        }
      })
      .finally(() => {
        if (!cancelled) {
          setLoading(false);
        }
      });

    return () => {
      cancelled = true;
    };
  }, [username]);

  return { userData, loading, error };
}

export function useMultipleUsers(usernames) {
  const [usersData, setUsersData] = useState(new Map());
  const [loading, setLoading] = useState(true);
  const [errors, setErrors] = useState(new Map());

  useEffect(() => {
    if (!usernames || usernames.length === 0) {
      setUsersData(new Map());
      setLoading(false);
      return;
    }

    let cancelled = false;
    setLoading(true);

    Promise.all(
      usernames.map((username) =>
        fetchUserByUsername(username)
          .then((user) => ({ username, user, error: null }))
          .catch((error) => ({
            username,
            user: null,
            error: error?.message || "Failed to load user",
          }))
      )
    )
      .then((results) => {
        if (cancelled) return;

        const usersMap = new Map();
        const errorsMap = new Map();

        for (const { username, user, error } of results) {
          if (user) {
            usersMap.set(username, user);
          }
          if (error) {
            errorsMap.set(username, error);
          }
        }

        setUsersData(usersMap);
        setErrors(errorsMap);
      })
      .finally(() => {
        if (!cancelled) {
          setLoading(false);
        }
      });

    return () => {
      cancelled = true;
    };
  }, [usernames.join(",")]);

  return { usersData, loading, errors };
}

import React, { createContext, useContext, useState, useEffect, useMemo, useCallback } from "react";
import PropTypes from "prop-types";
import { fetchAvatars } from "../api/avatarApi";
import { fetchBackgrounds } from "../api/backgroundApi";

const AssetsCacheContext = createContext(null);

export function AssetsCacheProvider({ children }) {
  const [avatars, setAvatars] = useState(null);
  const [backgrounds, setBackgrounds] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    let cancelled = false;

    async function loadAssets() {
      try {
        const [avatarsData, backgroundsData] = await Promise.all([
          fetchAvatars(),
          fetchBackgrounds(),
        ]);

        if (!cancelled) {
          setAvatars(avatarsData);
          setBackgrounds(backgroundsData);
          setError(null);
        }
      } catch (e) {
        if (!cancelled) {
          setError(e?.message || "Failed to load assets");
        }
      } finally {
        if (!cancelled) {
          setLoading(false);
        }
      }
    }

    // Only load if not already cached
    if (!avatars && !backgrounds) {
      loadAssets();
    } else {
      setLoading(false);
    }

    return () => {
      cancelled = true;
    };
  }, []);

  const refreshAssets = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const [avatarsData, backgroundsData] = await Promise.all([
        fetchAvatars(),
        fetchBackgrounds(),
      ]);
      setAvatars(avatarsData);
      setBackgrounds(backgroundsData);
    } catch (e) {
      setError(e?.message || "Failed to refresh assets");
    } finally {
      setLoading(false);
    }
  }, []);

  const contextValue = useMemo(
    () => ({
      avatars,
      backgrounds,
      loading,
      error,
      refreshAssets,
    }),
    [avatars, backgrounds, loading, error, refreshAssets]
  );

  return (
    <AssetsCacheContext.Provider value={contextValue}>
      {children}
    </AssetsCacheContext.Provider>
  );
}

AssetsCacheProvider.propTypes = {
  children: PropTypes.node.isRequired,
};

export function useAssetsCache() {
  const context = useContext(AssetsCacheContext);
  if (!context) {
    throw new Error("useAssetsCache must be used within AssetsCacheProvider");
  }
  return context;
}

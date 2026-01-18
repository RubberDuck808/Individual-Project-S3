import { useEffect } from "react";
import SockJS from "sockjs-client";
import { Client } from "@stomp/stompjs";
import { getAuthHeader } from "../../../api/auth";

export function useHazardsWebSocket({ enabled, onEvent }) {
  useEffect(() => {
    if (!enabled) return;

    const base = import.meta.env.VITE_API_URL;
    if (!base) {
      console.error("VITE_API_URL is not set. WebSocket connection cannot be established.");
      return;
    }

    let authHeader;
    try {
      authHeader = getAuthHeader();
    } catch {
      console.warn("Failed to get auth header for WebSocket");
      return;
    }

    const client = new Client({
      webSocketFactory: () =>
        new SockJS(`${base}/ws`, null, { withCredentials: false }),

      connectHeaders: authHeader,

      reconnectDelay: 3000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,

      onConnect: () => {
        console.log("Hazards WebSocket connected");
        client.subscribe("/topic/hazards", (msg) => {
          try {
            onEvent(JSON.parse(msg.body));
          } catch (e) {
            console.error("Bad WS payload", e);
          }
        });
      },

      onDisconnect: () => {
        console.log("Hazards WebSocket disconnected");
      },

      onStompError: (frame) => {
        console.error("WebSocket STOMP error:", frame);
      },

      onWebSocketError: (event) => {
        console.error("WebSocket error:", event);
      },
    });

    client.activate();
    
    return () => {
      client.deactivate();
    };
  }, [enabled, onEvent]);
}

import { useEffect } from "react";
import SockJS from "sockjs-client";
import { Client } from "@stomp/stompjs";
import { getAuthHeader } from "../../api/auth";

export function useTelemetryWebSocket({ deviceId, enabled, onEvent }) {
  useEffect(() => {
    if (!enabled || !deviceId) return;

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
        console.log("Telemetry WebSocket connected");
        const topic = `/topic/telemetry/${deviceId}`;
        client.subscribe(topic, (msg) => {
          try {
            const event = JSON.parse(msg.body);
            if (event.type === "UPDATE" && event.carHealth) {
              onEvent(event.carHealth);
            }
          } catch (e) {
            console.error("Bad WebSocket payload", e);
          }
        });
        client.subscribe("/topic/telemetry", (msg) => {
          try {
            const event = JSON.parse(msg.body);
            if (event.type === "UPDATE" && event.deviceId === deviceId && event.carHealth) {
              onEvent(event.carHealth);
            }
          } catch (e) {
            console.error("Bad WebSocket payload", e);
          }
        });
      },

      onDisconnect: () => {
        console.log("Telemetry WebSocket disconnected");
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
  }, [deviceId, enabled, onEvent]);
}

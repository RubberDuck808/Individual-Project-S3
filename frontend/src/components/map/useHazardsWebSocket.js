import { useEffect } from "react";
import SockJS from "sockjs-client";
import { Client } from "@stomp/stompjs";

export function useHazardsWebSocket({ enabled, onEvent }) {
  useEffect(() => {
    if (!enabled) return;

    const base = import.meta.env.VITE_API_URL; // e.g. http://localhost:8080
    const client = new Client({
      webSocketFactory: () => new SockJS(`${base}/ws`),
      reconnectDelay: 3000,
      onConnect: () => {
        client.subscribe("/topic/hazards", (msg) => {
          try {
            onEvent(JSON.parse(msg.body));
          } catch (e) {
            console.error("Bad WS payload", e);
          }
        });
      },
    });

    client.activate();
    return () => client.deactivate();
  }, [enabled, onEvent]);
}

import { useEffect, useRef } from "react";

export function useWebSocketNewLibraryNotification(
    channel: string,
    onMessage: (msg: string) => void
) {
    const wsRef = useRef<WebSocket | null>(null);
    const keepAliveRef = useRef<NodeJS.Timeout | null>(null);

    useEffect(() => {
        const token = localStorage.getItem("access_token");
        if (!token) return;

        const apiBaseUrl = import.meta.env.VITE_API_BASE_URL?.replace(/^https?:\/\//, "") || "localhost";
        const wsUrl = `wss://${apiBaseUrl}/ws?token=${encodeURIComponent(token)}&channel=${encodeURIComponent(channel)}`;

        console.log("Connecting to:", wsUrl);

        const ws = new WebSocket(wsUrl);
        wsRef.current = ws;

        ws.onopen = () => {
            console.log("WebSocket opened:", wsUrl);

            // Keep-alive ping for the established WS connection
            keepAliveRef.current = setInterval(() => {
                if (ws.readyState === WebSocket.OPEN) {
                    ws.send("ping");
                }
            }, 30000);
        };

        ws.onmessage = (event) => {
            onMessage(event.data);
        };

        ws.onerror = (err) => {
            console.error("WebSocket error:", err);
        };

        ws.onclose = (event) => {
            console.warn("WebSocket closed", event);
            if (keepAliveRef.current) {
                clearInterval(keepAliveRef.current);
            }
        };

        return () => {
            console.log("Cleaning up WebSocket connection");
            if (keepAliveRef.current) {
                clearInterval(keepAliveRef.current);
            }
            ws.close();
            wsRef.current = null;
        };
    }, [channel]);
}

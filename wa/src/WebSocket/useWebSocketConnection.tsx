import { useEffect, useRef } from "react";

const useWebSocketConnection = (
    channel: string,
    onMessage: (msg: string) => void
) => {
    const wsRef = useRef<WebSocket | null>(null);

    useEffect(() => {
        const jwtToken = localStorage.getItem('access_token');
        if (!jwtToken || wsRef.current) return; // 💡 Avoid reconnect if already connected

        const apiBaseUrl = import.meta.env.VITE_API_BASE_URL?.replace(/^https?:\/\//, '') || "";
        const wsUrl = `wss://${apiBaseUrl}/ws?token=${encodeURIComponent(jwtToken)}&channel=${encodeURIComponent(channel)}`;
        const ws = new WebSocket(wsUrl);

        console.log(`✅ WebSocket connecting to: ${wsUrl}`);

        ws.onopen = () => {
            console.log(`✅ WebSocket connected to: ${wsUrl}`);
        };

        ws.onmessage = (e) => {
            onMessage(e.data);
        };

        ws.onerror = (err) => {
            console.error("WebSocket error:", err);
        };

        ws.onclose = () => {
            console.log("WebSocket closed");
            wsRef.current = null;
        };

        wsRef.current = ws;

        return () => {
            if (wsRef.current) {
                wsRef.current.close();
                wsRef.current = null;
            }
        };
    }, [channel, onMessage]);
};

export default useWebSocketConnection;

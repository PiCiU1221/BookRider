import { useEffect, useRef } from "react";

interface UseWebSocketOptions {
    channel: string;
    onMessage: (msg: string) => void;
}

const useWebSocketConnection = ({ channel, onMessage }: UseWebSocketOptions) => {
    const wsRef = useRef<WebSocket | null>(null);

    useEffect(() => {
        const token = localStorage.getItem('access_token'); // ✅ For web

        if (!token) {
            console.warn("No JWT token found in localStorage.");
            return;
        }

        const baseUrl = import.meta.env.VITE_API_BASE_URL || "";
        const domain = baseUrl.replace(/^https?:\/\//, ""); // Strip protocol
        const wsUrl = `wss://${domain}/ws?token=${encodeURIComponent(token)}&channel=${encodeURIComponent(channel)}`;

        const ws = new WebSocket(wsUrl);
        wsRef.current = ws;

        ws.onopen = () => console.log("WebSocket connected:", wsUrl);
        ws.onmessage = (event) => {
            console.log("WebSocket message:", event.data);
            onMessage(event.data);
        };
        ws.onerror = (error) => console.error("WebSocket error:", error);
        ws.onclose = () => console.log("WebSocket closed");

        return () => {
            if (wsRef.current) {
                wsRef.current.close();
                wsRef.current = null;
            }
        };
    }, [channel, onMessage]);
};

export default useWebSocketConnection;

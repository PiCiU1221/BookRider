import { useEffect, useRef, useState } from "react";
import AsyncStorage from "@react-native-async-storage/async-storage";
import CONFIG from "@/config";

const useWebSocketConnection = (
    channel: string,
    onMessage: (msg: string) => void
) => {
    const wsRef = useRef<WebSocket | null>(null);
    const [jwtToken, setJwtToken] = useState<string | null>(null);

    useEffect(() => {
        const fetchToken = async () => {
            const token = await AsyncStorage.getItem("jwtToken");
            setJwtToken(token);
        };

        fetchToken();
    }, []);

    useEffect(() => {
        if (!jwtToken) return;

        if (wsRef.current) {
            wsRef.current.close();
        }

        const apiBaseUrl = CONFIG.API_BASE_URL ? CONFIG.API_BASE_URL.replace(/^https?:\/\//, '') : '';
        const wsUrl = `ws://${apiBaseUrl}/ws?token=${encodeURIComponent(jwtToken)}&channel=${encodeURIComponent(channel)}`;
        const ws = new WebSocket(wsUrl);

        ws.onmessage = (e) => {
            onMessage(e.data);
        };

        wsRef.current = ws;

        return () => {
            if (wsRef.current) {
                wsRef.current.close();
            }
        };
    }, [jwtToken, channel]);
};

export default useWebSocketConnection;

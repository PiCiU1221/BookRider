import {ActivityIndicator, Text, View, Image} from "react-native";
import {router} from "expo-router";
import React, {useEffect} from "react";
import AsyncStorage from "@react-native-async-storage/async-storage";
import {StatusBar} from "expo-status-bar";
import CONFIG from "@/config";

export default function Index() {
    useEffect(() => {
        const initializeApp = async () => {
            try {
                const token = await AsyncStorage.getItem("jwtToken");
                const isVerified = await AsyncStorage.getItem("isVerified");

                if (isVerified === "true") {
                    router.replace("/dashboard");
                    return;
                }

                if (!token) {
                    router.replace("/login");
                    return;
                }

                if (!checkTokenExpiration(token)) {
                    router.replace("/login");
                    return;
                }

                const response = await fetch(`${CONFIG.API_BASE_URL}/api/users/is-verified`, {
                    method: "GET",
                    headers: {
                        "Authorization": `Bearer ${token}`,
                    },
                });

                if (!response.ok) {
                    router.replace("/driver_application");
                    return;
                }

                const { isVerified: newVerifiedStatus } = await response.json();

                await AsyncStorage.setItem("isVerified", JSON.stringify(newVerifiedStatus));

                if (newVerifiedStatus) {
                    router.replace("/dashboard");
                } else {
                    router.replace("/driver_application");
                }
            } catch (error) {
                router.replace("/login");
            }
        };

        initializeApp();
    }, []);

    const checkTokenExpiration = (token: string): boolean => {
        try {
            const payload = JSON.parse(atob(token.split(".")[1]));
            const expiration = new Date(payload.exp * 1000);
            return expiration > new Date();
        } catch (error) {
            console.error("Invalid token format:", error);
            return false;
        }
    };

    return (
        <View className="flex-1 justify-center items-center bg-theme_background">
            <StatusBar style={"light"} />

            <View className="flex-1 justify-start items-center mt-24">
                <Image
                    source={require("@/assets/images/book-rider-driver-logo.png")}
                    className="w-72 h-72"
                    resizeMode="contain"
                />
            </View>

            <View className="flex items-center mb-64">
                <ActivityIndicator size={100} color="#f7ca65" />
                <Text className="mt-8 text-white text-xl">Initializing...</Text>
            </View>
        </View>
    );
}

import {ActivityIndicator, Text, View, Image} from "react-native";
import {router} from "expo-router";
import React, {useEffect} from "react";
import AsyncStorage from "@react-native-async-storage/async-storage";
import {StatusBar} from "expo-status-bar";

export default function Index() {
    useEffect(() => {
        const initializeApp = async () => {
            try {
                const token = await AsyncStorage.getItem("jwtToken");

                if (token) {
                    router.replace("/book_search");
                    return;
                } else {
                    router.replace("/login");
                    return;
                }
            } catch (error) {
                router.replace("/login");
            }
        };

        initializeApp();
    }, []);

    return (
        <View className="flex-1 justify-center items-center bg-theme_background">
            <StatusBar style={"light"} />

            <View className="flex-1 justify-start items-center mt-24">
                <Image
                    source={require("@/assets/images/book-rider-high-resolution-logo.png")}
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

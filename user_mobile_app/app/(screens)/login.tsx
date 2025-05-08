import {Text, View, TextInput, TouchableOpacity, Image, ScrollView, KeyboardAvoidingView, Platform} from "react-native";
import {StatusBar} from "expo-status-bar";
import React, { useState } from "react";
import {Link, router} from "expo-router";
import Animated, {FadeInDown} from "react-native-reanimated";
import CustomModal from "@/app/components/custom_modal";
import AsyncStorage from "@react-native-async-storage/async-storage";
import CONFIG from "@/config";

interface ApiResponse {
    status: string;
    data?: any;
    message?: string;
}

export default function Login() {
    const [email, setEmail] = useState<string>("");
    const [password, setPassword] = useState<string>("");
    const [apiResponse, setApiResponse] = useState<ApiResponse | null>(null);
    const [loading, setLoading] = useState<boolean>(false);
    const [modalVisible, setModalVisible] = useState<boolean>(false);

    const handleLogin = async () => {
        setLoading(true);
        setModalVisible(true);

        const requestBody = {
            identifier: email,
            password: password,
        };

        try {
            const response = await fetch(`${CONFIG.API_BASE_URL}/api/auth/login/user`, {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                },
                body: JSON.stringify(requestBody),
            });

            if (response.ok) {
                const tokenWithBearer = response.headers.get("Authorization");

                if (tokenWithBearer) {
                    const token = tokenWithBearer.split(" ")[1];

                    await AsyncStorage.setItem("jwtToken", token);

                    router.replace("/book_search");
                } else {
                    setApiResponse({
                        status: "error",
                        message: "Token not found in response",
                    });
                }
            } else {
                const responseData = await response.json();

                setApiResponse({
                    status: "error",
                    message: responseData.message || "Something went wrong",
                });
            }
        } catch (error) {
            setApiResponse({
                status: "error",
                message: error instanceof Error ? error.message : "Unknown error",
            });
        } finally {
            setLoading(false);
            if (apiResponse?.status === "success") {
                setModalVisible(false);
            }
        }
    };

    return (
        <KeyboardAvoidingView
            behavior={Platform.OS === "ios" ? "padding" : "height"}
            className="flex-1 bg-theme_background"
        >
            <StatusBar style="light" />
            <ScrollView
                contentContainerStyle={{ flexGrow: 1 }}
                keyboardShouldPersistTaps="handled"
                className="w-full"
            >
                <View className="flex-1 justify-start items-center mt-24">
                    <Image
                        source={require("@/assets/images/book-rider-high-resolution-logo.png")}
                        className="w-72 h-72"
                        resizeMode="contain"
                    />
                </View>

                <View className="flex-1 justify-center mb-10 mx-8">
                    <Text className="text-theme_accent text-3xl font-bold tracking-wide text-left ml-2 mb-8">
                        Logowanie
                    </Text>

                    <Animated.View
                        entering={FadeInDown.duration(1000).springify()}
                        className="bg-black/10 px-2 rounded-xl w-full mb-6 justify-center h-14"
                    >
                        <TextInput
                            className="text-base text-white"
                            placeholder="Email"
                            placeholderTextColor="#C8C8C8"
                            value={email}
                            onChangeText={setEmail}
                            autoCapitalize="none"
                        />
                    </Animated.View>

                    <Animated.View
                        entering={FadeInDown.delay(200).duration(1000).springify()}
                        className="bg-black/10 p-2 rounded-xl w-full mb-11 h-14 justify-center"
                    >
                        <TextInput
                            className="text-base text-white"
                            placeholder="Hasło"
                            placeholderTextColor="#C8C8C8"
                            secureTextEntry={true}
                            value={password}
                            onChangeText={setPassword}
                            autoCapitalize="none"
                        />
                    </Animated.View>

                    <Animated.View
                        entering={FadeInDown.delay(400).duration(1000).springify()}
                        className="w-full"
                    >
                        <TouchableOpacity
                            className="w-full bg-theme_accent rounded-xl mb-20 h-14 flex justify-center items-center"
                            onPress={handleLogin}
                            disabled={loading}
                        >
                            <Text className="text-2xl font-bold text-white text-center">Zaloguj się</Text>
                        </TouchableOpacity>
                    </Animated.View>

                    <Animated.View
                        entering={FadeInDown.delay(600).duration(1000).springify()}
                        className="flex-row justify-center"
                    >
                        <Text className="text-white text-base">Nie masz konta? </Text>
                        <Link replace href="/register">
                            <Text className="text-theme_accent text-base">Zarejestruj się</Text>
                        </Link>
                    </Animated.View>

                    <CustomModal
                        isVisible={modalVisible}
                        title={apiResponse?.status === "success" ? "Pomyślnie zalogowano!" : "Błąd"
                        }
                        message={apiResponse?.message}
                        onClose={() => setModalVisible(false)}
                        loading={loading}
                    />
                </View>
            </ScrollView>
        </KeyboardAvoidingView>
    );
}

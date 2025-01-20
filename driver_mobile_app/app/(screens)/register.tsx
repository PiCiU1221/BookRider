import {Text, View, TextInput, TouchableOpacity, Image, Alert, Platform, ScrollView, KeyboardAvoidingView} from "react-native";
import {StatusBar} from "expo-status-bar";
import React, { useState } from "react";
import Animated, {FadeInDown} from "react-native-reanimated";
import {Link, router} from "expo-router";
import CustomModal from "@/app/components/custom_modal";
import CONFIG from "@/config";

interface ApiResponse {
    status: string;
    data?: any;
    message?: string;
}

export default function Register() {
    const [email, setEmail] = useState<string>("");
    const [firstName, setFirstName] = useState<string>("");
    const [lastName, setLastName] = useState<string>("");
    const [password, setPassword] = useState<string>("");
    const [apiResponse, setApiResponse] = useState<ApiResponse | null>(null);
    const [loading, setLoading] = useState<boolean>(false);
    const [modalVisible, setModalVisible] = useState<boolean>(false);

    const handleRegister = async () => {
        setLoading(true);
        setModalVisible(true);

        const requestBody = {
            email: email,
            firstName: firstName,
            lastName: lastName,
            password: password,
        };

        try {
            const response = await fetch(`${CONFIG.API_BASE_URL}/api/auth/register/driver`, {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                },
                body: JSON.stringify(requestBody),
            });

            const responseData = await response.json();

            if (response.ok) {
                setApiResponse({
                    status: "success",
                    data: responseData,
                });
            } else {
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
                        source={require("@/assets/images/book-rider-driver-logo.png")}
                        className="w-72 h-72"
                        resizeMode="contain"
                    />
                </View>

                <View className="flex-1 justify-center mb-10 mx-8">
                    <Text className="text-theme_accent text-3xl font-bold tracking-wide text-left ml-2 mb-8">
                        Register
                    </Text>

                    <Animated.View entering={FadeInDown.duration(1000).springify()}
                                   className="bg-black/10 px-2 rounded-xl w-full mb-6 h-14 justify-center"
                    >
                        <TextInput
                            className="text-base text-white"
                            placeholder="Email"
                            placeholderTextColor={"#C8C8C8"}
                            value={email}
                            onChangeText={setEmail}
                        />
                    </Animated.View>

                    <Animated.View entering={FadeInDown.delay(200).duration(1000).springify()}
                                   className="flex-row mb-6 justify-center"
                    >
                        <View className="bg-black/10 px-2 rounded-xl flex-1 mr-2 h-14 justify-center">
                            <TextInput
                                className="text-base text-white"
                                placeholder="First Name"
                                placeholderTextColor={"#C8C8C8"}
                                value={firstName}
                                onChangeText={setFirstName}
                            />
                        </View>
                        <View className="bg-black/10 px-2 rounded-xl flex-1 ml-2 h-14 justify-center">
                            <TextInput
                                className="text-base text-white"
                                placeholder="Last Name"
                                placeholderTextColor={"#C8C8C8"}
                                value={lastName}
                                onChangeText={setLastName}
                            />
                        </View>
                    </Animated.View>

                    <Animated.View entering={FadeInDown.delay(400).duration(1000).springify()}
                                   className="bg-black/10 px-2 rounded-xl w-full mb-11 h-14 justify-center"
                    >
                        <TextInput
                            className="text-base text-white"
                            placeholder="Password"
                            placeholderTextColor={"#C8C8C8"}
                            secureTextEntry={true}
                            value={password}
                            onChangeText={setPassword}
                        />
                    </Animated.View>

                    <Animated.View entering={FadeInDown.delay(600).duration(1000).springify()} className="w-full">
                        <TouchableOpacity
                            className="w-full bg-theme_accent rounded-xl mb-20 h-14 flex justify-center items-center"
                            onPress={handleRegister}
                            disabled={loading}
                        >
                            <Text className="text-2xl font-bold text-white text-center">Register</Text>
                        </TouchableOpacity>
                    </Animated.View>

                    <Animated.View entering={FadeInDown.delay(800).duration(1000).springify()} className="flex-row justify-center">
                        <Text className="text-white text-base">Already have an account? </Text>
                        <Link replace href="/login">
                            <Text className="text-theme_accent text-base">Log in</Text>
                        </Link>
                    </Animated.View>

                    <CustomModal
                        isVisible={modalVisible}
                        title={apiResponse?.status === "success" ? "Registration Successful!" : "Error"}
                        message={apiResponse?.message}
                        onClose={() => router.replace("/login")}
                        loading={loading}
                    />
                </View>
            </ScrollView>
        </KeyboardAvoidingView>
    );
}
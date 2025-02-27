import React, { useEffect, useState } from "react";
import {View, Text, TouchableOpacity, Alert, ScrollView} from "react-native";
import AsyncStorage from "@react-native-async-storage/async-storage";
import { router } from "expo-router";
import { StatusBar } from "expo-status-bar";
import CONFIG from "@/config";
import CustomModal from "@/app/components/custom_modal";
import {Feather} from "@expo/vector-icons";

interface UserProfile {
    username: string | null;
    email: string;
    firstName: string;
    lastName: string;
    balance: number;
    createdAt: string;
}

export default function AccountPage() {
    const [profile, setProfile] = useState<UserProfile | null>(null);
    const [loading, setLoading] = useState(true);

    const fetchProfile = async () => {
        try {
            const token = await AsyncStorage.getItem("jwtToken");
            const response = await fetch(`${CONFIG.API_BASE_URL}/api/users/profile`, {
                method: "GET",
                headers: {
                    Authorization: `Bearer ${token}`,
                    "Content-Type": "application/json",
                },
            });

            if (response.ok) {
                const data: UserProfile = await response.json();
                setProfile(data);
            } else {
                Alert.alert("Error", "Failed to fetch profile data");
            }
        } catch (error) {
            Alert.alert("Error", "There was an error fetching profile data");
        } finally {
            setLoading(false);
        }
    };

    const handleLogout = async () => {
        try {
            await AsyncStorage.clear();
            router.replace("/login");
        } catch (error) {
            Alert.alert("Error", "There was an error logging out");
        }
    };

    useEffect(() => {
        fetchProfile();
    }, []);

    return (
        <View className="flex-1 bg-theme_background p-4">
            <StatusBar style="light" />
            <Text className="text-2xl font-bold text-white mt-10 mb-4">Account Information</Text>

            <CustomModal isVisible={loading} title="Loading..." loading={true} onClose={() => {}} />
            {!loading && (
                <ScrollView contentContainerStyle={{ paddingBottom: 24 }}>
                    {profile ? (
                        <View className="bg-black/10 p-6 rounded-lg border border-gray-300">
                            <View className="flex-row items-center mb-4">
                                <Feather name="mail" size={20} color="#f7ca65" />
                                <Text className="text-white text-lg ml-2">
                                    <Text className="font-bold">Email:</Text> {profile.email}
                                </Text>
                            </View>
                            <View className="flex-row items-center mb-4">
                                <Feather name="user" size={20} color="#f7ca65" />
                                <Text className="text-white text-lg ml-2">
                                    <Text className="font-bold">First Name:</Text> {profile.firstName}
                                </Text>
                            </View>
                            <View className="flex-row items-center mb-4">
                                <Feather name="user-check" size={20} color="#f7ca65" />
                                <Text className="text-white text-lg ml-2">
                                    <Text className="font-bold">Last Name:</Text> {profile.lastName}
                                </Text>
                            </View>
                            <View className="flex-row items-center mb-4">
                                <Feather name="dollar-sign" size={20} color="#f7ca65" />
                                <Text className="text-white text-lg ml-2">
                                    <Text className="font-bold">Balance:</Text> {profile.balance.toFixed(2)} zł
                                </Text>
                            </View>
                            <View className="flex-row items-center">
                                <Feather name="calendar" size={20} color="#f7ca65" />
                                <Text className="text-white text-lg ml-2">
                                    <Text className="font-bold">Joined At:</Text> {new Date(profile.createdAt).toLocaleDateString()}
                                </Text>
                            </View>
                        </View>
                    ) : (
                        <Text className="text-white text-lg text-center">
                            Unable to load profile information.
                        </Text>
                    )}
                </ScrollView>
            )}

            <TouchableOpacity
                onPress={handleLogout}
                className="mt-auto bg-red-500 p-4 rounded-2xl shadow-md"
                style={{ elevation: 5 }}
            >
                <Text className="text-white text-center font-bold text-lg">Logout</Text>
            </TouchableOpacity>
        </View>
    );
}

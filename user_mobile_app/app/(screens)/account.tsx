import React, { useEffect, useState } from "react";
import { View, Text, TouchableOpacity, Alert, ScrollView } from "react-native";
import AsyncStorage from "@react-native-async-storage/async-storage";
import { router } from "expo-router";
import { StatusBar } from "expo-status-bar";
import CONFIG from "@/config";
import CustomModal from "@/app/components/custom_modal";
import { Feather } from "@expo/vector-icons";
// @ts-ignore
import { Barcode } from 'expo-barcode-generator';

interface UserProfile {
    username: string | null;
    email: string;
    firstName: string;
    lastName: string;
    balance: number;
    createdAt: string;
}

interface LibraryCard {
    userId: string;
    cardId: string;
    firstName: string;
    lastName: string;
    expirationDate: string;
}

export default function AccountPage() {
    const [profile, setProfile] = useState<UserProfile | null>(null);
const [loading, setLoading] = useState(true);
    const [userId, setUserId] = useState<string | null>(null);
    const [modalVisible, setModalVisible] = useState<boolean>(false);
    const [libraryCards, setLibraryCards] = useState<LibraryCard[]>([]);

    const fetchUserId = async () => {
        let userId = await AsyncStorage.getItem("userId");
        if (!userId) {
            try {
                const token = await AsyncStorage.getItem("jwtToken");
                const response = await fetch(`${CONFIG.API_BASE_URL}/api/users/id`, {
                    method: "GET",
                    headers: {
                        Authorization: `Bearer ${token}`,
                        "Content-Type": "application/json",
                    },
                });

                if (response.ok) {
                    const data = await response.json();
                    userId = data.userId;
                    if (userId) {
                        await AsyncStorage.setItem("userId", userId);
                    } else {
                        Alert.alert("Error", "Failed to retrieve user ID.");
                        return null;
                    }
                } else {
                    const errorData = await response.json();
                    Alert.alert("Error", errorData.message || "Failed to fetch user ID.");
                    return null;
                }
            } catch (error) {
                Alert.alert("Error", "There was an error fetching user ID");
                return null;
            }
        }
        return userId;
    };

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
                await fetchLibraryCards();
            } else {
                Alert.alert("Error", "Failed to fetch profile data");
            }
        } catch (error) {
            Alert.alert("Error", "There was an error fetching profile data");
        } finally {
            setLoading(false);
        }
    };

    const fetchLibraryCards = async () => {
        const userId = await fetchUserId();
        if (!userId) return;

        try {
            const token = await AsyncStorage.getItem("jwtToken");
            const response = await fetch(`${CONFIG.API_BASE_URL}/api/library-cards/${userId}?page=0&size=10`, {
                method: "GET",
                headers: {
                    Authorization: `Bearer ${token}`,
                    "Content-Type": "application/json",
                },
            });

            if (response.ok) {
                const data: LibraryCard[] = await response.json();
                setLibraryCards(data);
            } else {
                Alert.alert("Error", "Failed to fetch library cards");
            }
        } catch (error) {
            Alert.alert("Error", "There was an error fetching library cards");
        }
    };

    const depositAmount = async () => {
        try {
            const token = await AsyncStorage.getItem("jwtToken");
            if (!token) {
                Alert.alert("Error", "User not authenticated");
                return;
            }

            const response = await fetch(`${CONFIG.API_BASE_URL}/api/transactions/deposit?amount=10`, {
                method: "POST",
                headers: {
                    Authorization: `Bearer ${token}`,
                    "Content-Type": "application/json",
                },
            });

            if (response.ok) {
                fetchProfile();
            } else {
                Alert.alert("Error", "Failed to deposit amount");
            }
        } catch (error) {
            Alert.alert("Error", "There was an error processing the deposit");
        }
    };

    const handleShowID = async () => {
        const userId = await fetchUserId();
        if (!userId) return;

        setUserId(userId);
        setModalVisible(true);
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
            <View className="flex-row items-center justify-center mt-10 mb-4">
                <Feather name="user" size={24} color="#f7ca65" className="mr-2" />
                <Text className="text-2xl font-bold text-white">Konto</Text>
            </View>

            <CustomModal
                isVisible={modalVisible || loading}
                title={loading ? "Loading..." : "ID konta"}
                onClose={() => setModalVisible(false)}
                loading={loading}
            >
                {!loading && userId && (
                    <View className="justify-center mb-6 w-full items-center">
                        <Barcode
                            value={userId}
                            options={{ format: 'CODE128', lineColor: '#000000', background: '#ffffff'}}
                        />
                    </View>
                )}
            </CustomModal>

            {!loading && (
                <ScrollView contentContainerStyle={{ paddingBottom: 24 }}>
                    {profile ? (
                        <View>
                            <View className="bg-black/10 p-6 rounded-lg border border-gray-300">
                                <View className="flex-row items-center mb-4">
                                    <Feather name="mail" size={20} color="#f7ca65" />
                                    <Text className="text-white text-lg ml-2">
                                        <Text className="font-bold">Email:</Text> {profile.email}
                                    </Text>
                                </View>
                                <View className="flex-row items-center mb-4">
                                    <Feather name="dollar-sign" size={20} color="#f7ca65" />
                                    <Text className="text-white text-lg ml-2">
                                        <Text className="font-bold">Stan konta:</Text> {profile.balance.toFixed(2)} zł
                                    </Text>
                                    <TouchableOpacity
                                        className="bg-green-500 p-0.5 rounded-full ml-4 flex justify-center items-center"
                                        onPress={() => depositAmount()}
                                    >
                                        <Feather name="plus" size={26} color="white" />
                                    </TouchableOpacity>
                                </View>
                                <View className="flex-row items-center mb-4">
                                    <Feather name="calendar" size={20} color="#f7ca65" />
                                    <Text className="text-white text-lg ml-2">
                                        <Text className="font-bold">Data dołączenia:</Text> {new Date(profile.createdAt).toLocaleDateString('pl-PL')}
                                    </Text>
                                </View>
                            </View>

                            <View className="mt-4 p-6 bg-black/10 rounded-lg border border-gray-300">
                                <Text className="text-xl font-bold text-white mb-3">Karty biblioteczne</Text>

                                {libraryCards.length > 0 ? (
                                    libraryCards.map((card) => (
                                        <View key={card.cardId} className="mb-4 p-4 bg-gray-700 rounded-lg">
                                            <View className="flex-row items-center mb-4">
                                                <Feather name="credit-card" size={20} color="#f7ca65" />
                                                <Text className="text-white text-lg ml-2">
                                                    <Text className="font-bold">Imię i nazwisko:</Text> {card.firstName} {card.lastName}
                                                </Text>
                                            </View>
                                            <View className="flex-row items-center mb-4">
                                                <Feather name="credit-card" size={20} color="#f7ca65" />
                                                <Text className="text-white text-lg ml-2">
                                                    <Text className="font-bold">ID karty:</Text> {card.cardId}
                                                </Text>
                                            </View>
                                            <View className="flex-row items-center">
                                                <Feather name="calendar" size={20} color="#f7ca65" />
                                                <Text className="text-white text-lg ml-2">
                                                    <Text className="font-bold">Data ważności:</Text> {new Date(card.expirationDate).toLocaleDateString('pl-PL')}
                                                </Text>
                                            </View>
                                        </View>
                                    ))
                                ) : (
                                    <Text className="text-gray-300">Brak podpiętej karty bibliotecznej.</Text>
                                )}

                                <TouchableOpacity
                                    onPress={handleShowID}
                                    className="bg-yellow-500 px-6 py-3 rounded-lg mt-4"
                                >
                                    <Text className="text-white text-center font-semibold text-lg">Pokaż ID konta</Text>
                                </TouchableOpacity>
                            </View>
                        </View>
                    ) : (
                        <Text className="text-white text-lg text-center">
                            Nie można załadować informacji o profilu.
                        </Text>
                    )}
                </ScrollView>
            )}

            <TouchableOpacity
                onPress={handleLogout}
                className="mt-auto bg-red-500 p-4 rounded-2xl shadow-md"
                style={{ elevation: 5 }}
            >
                <Text className="text-white text-center font-bold text-lg">Wyloguj się</Text>
            </TouchableOpacity>
        </View>
    );
}

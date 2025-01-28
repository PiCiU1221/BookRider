import React, {useEffect} from "react";
import {View, Text, TouchableOpacity, Alert, BackHandler} from "react-native";
import AsyncStorage from "@react-native-async-storage/async-storage";
import { useNavigation } from "@react-navigation/native";
import {router} from "expo-router";
import {StatusBar} from "expo-status-bar";

export default function AccountPage() {
    const navigation = useNavigation();

    const handleLogout = async () => {
        try {
            await AsyncStorage.clear();

            router.replace("/login");
        } catch (error) {
            Alert.alert("Error", "There was an error logging out");
        }
    };

    return (
        <View className="flex-1 justify-center items-center bg-theme_background">
            <StatusBar style="light" />
            <Text className="text-white text-lg">Account Page</Text>
            <TouchableOpacity
                onPress={handleLogout}
                className="mt-4 bg-red-500 p-4 rounded-full"
            >
                <Text className="text-white">Logout</Text>
            </TouchableOpacity>
        </View>
    );
}

import React, { useEffect, useState } from "react";
import {Text, View, TouchableOpacity, Alert, ActivityIndicator, Linking, Image} from "react-native";
import MapView, { Polyline } from "react-native-maps";
import Icon from "react-native-vector-icons/Ionicons";
import * as Location from "expo-location";
import { StatusBar } from "expo-status-bar";
import {router, useLocalSearchParams} from "expo-router";
import CustomModal from "@/app/components/custom_modal";
import {Feather} from "@expo/vector-icons";

interface Coordinate {
    latitude: number;
    longitude: number;
}

export default function Dashboard() {
    const { navigationData } = useLocalSearchParams();
    const [location, setLocation] = useState<{ latitude: number; longitude: number } | null>(null);
    const [directions, setDirections] = useState<Coordinate[]>([]);
    const [totalDistance, setTotalDistance] = useState<number | null>(null);
    const [totalDuration, setTotalDuration] = useState<number | null>(null);
    const [loading, setLoading] = useState<boolean>(true);
    const [modalVisible, setModalVisible] = useState(false);

    const handleClearDirections = () => {
        setDirections([]);
        setModalVisible(false);
    };

    useEffect(() => {
        const getLocation = async () => {
            const { status } = await Location.requestForegroundPermissionsAsync();
            if (status === "granted") {
                const lastKnownPosition = await Location.getLastKnownPositionAsync();

                if (lastKnownPosition) {
                    setLocation({
                        latitude: lastKnownPosition.coords.latitude,
                        longitude: lastKnownPosition.coords.longitude,
                    });
                    setLoading(false);
                } else {
                    const { coords } = await Location.getCurrentPositionAsync({});
                    setLocation({
                        latitude: coords.latitude,
                        longitude: coords.longitude,
                    });
                    setLoading(false);
                }
            } else {
                Alert.alert("Permission Denied", "We need your permission to access your location");
                setLoading(false);
            }
        };

        getLocation();
    }, []);

    useEffect(() => {
        if (navigationData) {
            const parsedData = JSON.parse(navigationData as string);

            const steps = parsedData.steps;
            const allCoordinates: Coordinate[] = [];
            steps.forEach((step: any) => {
                const waypoints: Coordinate[] = step.wayPoints.map((point: any) => ({
                    latitude: point.latitude,
                    longitude: point.longitude,
                }));
                allCoordinates.push(...waypoints);
            });

            setTotalDistance(parsedData.totalDistance);
            setTotalDuration(parsedData.totalDuration);

            setDirections(allCoordinates);
        }
    }, [navigationData]);

    const openGoogleMapsApp = (destinationLatitude: number, destinationLongitude: number) => {
        const url = `geo:${destinationLatitude},${destinationLongitude}?q=${destinationLatitude},${destinationLongitude}`;
        Linking.openURL(url).catch(() => {
            Alert.alert("Error", "Failed to open Google Maps app. Make sure the app is installed.");
        });
    };

    return (
        <View className="flex-1 bg-gray-800">
            <StatusBar style="light" />
            {loading ? (
                <View className="flex-1 justify-center items-center bg-gray-800">
                    <ActivityIndicator size="large" color="#fff" />
                    <Text className="text-white mt-4">"Wczytywanie twojej lokalizacji...</Text>
                </View>
            ) : (
                <>
                    <MapView
                        className="flex-1"
                        initialRegion={{
                            latitude: location?.latitude || 37.78825,
                            longitude: location?.longitude || -122.4324,
                            latitudeDelta: 0.0922,
                            longitudeDelta: 0.0421,
                        }}
                        showsUserLocation={true}
                        followsUserLocation={true}
                        style={{ flex: 1 }}
                    >
                        {directions.length > 0 && (
                            <Polyline coordinates={directions} strokeColor="#FF5733" strokeWidth={3} />
                        )}
                    </MapView>

                    {directions.length > 0 && (
                        <TouchableOpacity
                            className="absolute top-16 left-6 p-4 bg-theme_background rounded-full shadow-md z-50"
                            onPress={() => {
                                if (directions.length > 0) {
                                    const destination = directions[directions.length - 1];
                                    openGoogleMapsApp(destination.latitude, destination.longitude);
                                } else {
                                    Alert.alert("Error", "Route or location is not available.");
                                }
                            }}
                        >
                            <Image
                                source={require("@/assets/images/google-maps-icon.png")}
                                style={{ width: 35, height: 35 }}
                            />
                        </TouchableOpacity>
                    )}

                    {directions.length > 0 && (
                        <>
                            <View className="absolute top-16 right-3 p-4 bg-theme_background rounded-lg shadow-md z-50">
                                <Text className="text-white text-sm">
                                    <Text className="font-bold">Odległość: </Text>
                                    {totalDistance?.toFixed(2)} km
                                </Text>
                                <Text className="text-white text-sm">
                                    <Text className="font-bold">Czas podróży: </Text>
                                    {totalDuration ? `${(totalDuration / 60).toFixed(0)} min` : "N/A"}
                                </Text>
                            </View>

                            <View className="absolute top-36 right-3">
                                <TouchableOpacity
                                    className="p-2 bg-red-600 rounded-lg shadow-md flex-row items-center"
                                    onPress={() => setModalVisible(true)}
                                >
                                    <Feather name="x-circle" size={24} color="white" />
                                </TouchableOpacity>
                            </View>
                        </>
                    )}

                    <View className="absolute bottom-0 left-0 w-full bg-theme_background flex-row">
                        <TouchableOpacity
                            className="w-1/3 p-3 items-center justify-center"
                            onPress={() => {
                                router.push("/order_history");
                            }}
                        >
                            <Icon name="document-text" size={30} color="white" />
                            <Text className="text-white text-sm text-center">Zamówienia</Text>
                        </TouchableOpacity>

                        <TouchableOpacity
                            className="w-1/3 p-3 items-center justify-center bg-theme_accent"
                            onPress={() => {
                                router.push("/deliveries");
                            }}
                        >
                            <Icon name="car" size={30} color="white" />
                            <Text className="text-white text-sm">Dostawy</Text>
                        </TouchableOpacity>

                        <TouchableOpacity
                            className="w-1/3 p-3 rounded-full items-center justify-center"
                            onPress={() => {
                                router.push("/account");
                            }}
                        >
                            <Icon name="person" size={30} color="white" />
                            <Text className="text-white text-sm">Konto</Text>
                        </TouchableOpacity>
                    </View>

                    <CustomModal
                        isVisible={modalVisible}
                        title="Potwierdź usunięcie nawigacji"
                        onClose={() => setModalVisible(false)}
                        loading={loading}
                    >
                        <Text className="text-lg text-center mb-4 text-white">
                            Czy na pewno chcesz usunąć nawigację?
                        </Text>
                        <TouchableOpacity
                            onPress={handleClearDirections}
                            className="bg-red-600 rounded-xl w-full py-3"
                        >
                            <Text className="text-xl text-white text-center font-semibold">
                                Potwierdź
                            </Text>
                        </TouchableOpacity>
                    </CustomModal>
                </>
            )}
        </View>
    );
}

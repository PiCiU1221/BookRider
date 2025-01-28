import React, { useEffect, useState } from "react";
import { FlatList, Text, TouchableOpacity, View, TextInput, Image, Alert } from "react-native";
import { StatusBar } from "expo-status-bar";
import AsyncStorage from "@react-native-async-storage/async-storage";
import CustomModal from "@/app/components/custom_modal";
import CONFIG from "@/config";
// @ts-ignore
import { Barcode } from 'expo-barcode-generator';
import {router} from "expo-router";
import * as Location from "expo-location";
import * as ImagePicker from "expo-image-picker";
import {Feather} from "@expo/vector-icons";

interface OrderDetailsDTO {
    orderId: number;
    userId: string;
    libraryName: string;
    deliveryAddress: string;
    status: string;
    amount: string;
    paymentStatus: string;
    noteToDriver: string;
    orderItems: Array<{ book: BookResponseDto; quantity: number }>;
}

interface BookResponseDto {
    id: number;
    title: string;
    categoryName: string;
    authorNames: string[];
    releaseYear: number;
    publisherName: string;
    isbn: string;
    languageName: string;
    image: string;
}

interface DeliveryNavigationRequestDTO {
    transportProfile: string;
    latitude: number;
    longitude: number;
}

export default function Deliveries() {
    const [inRealizationOrders, setInRealizationOrders] = useState<OrderDetailsDTO[]>([]);
    const [pendingOrders, setPendingOrders] = useState<OrderDetailsDTO[]>([]);
    const [loading, setLoading] = useState<boolean>(false);
    const [modalVisible, setModalVisible] = useState<boolean>(false);
    const [selectedOrder, setSelectedOrder] = useState<OrderDetailsDTO | null>(null);
    const [userId, setUserId] = useState<string | null>(null);
    const [maxDistance, setMaxDistance] = useState<string>("");
    const [deliverMode, setDeliverMode] = useState(false);
    const [deliveryImageUri, setDeliveryImageUri] = useState<string>("");
    const [deliveryImageBase64, setDeliveryImageBase64] = useState<string>("");
    const [deliveryCompleted, setDeliveryCompleted] = useState(false);
    const [customMessage, setCustomMessage] = useState(null);
    const [hasSearched, setHasSearched] = useState(false);

    const fetchInRealizationOrders = async (): Promise<void> => {
        setLoading(true);
        setModalVisible(true);

        try {
            const token = await AsyncStorage.getItem("jwtToken");
            const response = await fetch(`${CONFIG.API_BASE_URL}/api/orders/driver/in-realization?page=0&size=10`, {
                method: "GET",
                headers: {
                    Authorization: `Bearer ${token}`,
                    "Content-Type": "application/json",
                },
            });

            if (response.ok) {
                const data = await response.json();
                setInRealizationOrders(data.content);
            } else {
                const errorData = await response.json();
                Alert.alert("Error", errorData.message || "Failed to fetch in-realization orders.");
            }
        } catch (error: any) {
            Alert.alert("Error", error.message || "Unknown error occurred.");
        } finally {
            setLoading(false);
            setModalVisible(false);
        }
    };

    useEffect(() => {
        fetchInRealizationOrders();
    }, []);

    const handleOrderPress = (order: OrderDetailsDTO): void => {
        setSelectedOrder(order);
        setModalVisible(true);
    };

    const handleShowID = async () => {
        try {
            let userId = await AsyncStorage.getItem("userId");

            if (!userId) {
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
                        return;
                    }
                } else {
                    const errorData = await response.json();
                    Alert.alert("Error", errorData.message || "Failed to fetch user ID.");
                    return;
                }
            }

            setUserId(userId);
        } catch (error: any) {
            Alert.alert("Error", error.message || "Unknown error occurred.");
        }
    };

    const getUserLocation = async () => {
        try {
            const { status } = await Location.requestForegroundPermissionsAsync();
            if (status !== 'granted') {
                Alert.alert("Permission Denied", "Please allow location access to continue.");
                return null;
            }

            const lastKnownPosition = await Location.getLastKnownPositionAsync();
            if (!lastKnownPosition) {
                Alert.alert("Location Error", "Unable to retrieve last known location.");
                return null;
            }

            return lastKnownPosition.coords;
        } catch (error: any) {
            Alert.alert("Error", error.message || "Failed to retrieve location.");
            return null;
        }
    };

    const handleNavigation = async () => {
        setLoading(true);

        if (!selectedOrder) return;

        const userLocation = await getUserLocation();
        if (!userLocation) {
            setLoading(false);
            return;
        }

        const { latitude, longitude } = userLocation;

        try {
            const token = await AsyncStorage.getItem("jwtToken");
            const navigationRequest: DeliveryNavigationRequestDTO = {
                transportProfile: "CAR",
                latitude,
                longitude,
            };

            const endpoint =
                selectedOrder.status === 'DRIVER_PICKED'
                    ? `/api/orders/${selectedOrder.orderId}/pickup-navigation`
                    : `/api/orders/${selectedOrder.orderId}/delivery-navigation`;

            const response = await fetch(
                `${CONFIG.API_BASE_URL}${endpoint}`,
                {
                    method: "POST",
                    headers: {
                        Authorization: `Bearer ${token}`,
                        "Content-Type": "application/json",
                    },
                    body: JSON.stringify(navigationRequest),
                }
            );

            if (response.ok) {
                const responseData = await response.json();

                router.dismissTo({
                    pathname: '/dashboard',
                    params: { navigationData: JSON.stringify(responseData) },
                });
            } else {
                const errorData = await response.json();
                Alert.alert("Error", errorData.message || "Failed to fetch navigation data.");
            }
        } catch (error: any) {
            Alert.alert("Error", error.message || "Unknown error occurred.");
        } finally {
            setLoading(false);
        }
    };

    const fetchPendingOrders = async (): Promise<void> => {
        setLoading(true);
        setModalVisible(true);
        setHasSearched(true);

        try {
            const userLocation = await getUserLocation();
            if (!userLocation) {
                setLoading(false);
                return;
            }

            const locationString = `${userLocation.latitude},${userLocation.longitude}`;

            const token = await AsyncStorage.getItem("jwtToken");
            const response = await fetch(
                `${CONFIG.API_BASE_URL}/api/orders/driver/pending?page=0&size=10&locationString=${encodeURIComponent(locationString)}&maxDistanceInMeters=${maxDistance}`,
                {
                method: "GET",
                headers: {
                    Authorization: `Bearer ${token}`,
                    "Content-Type": "application/json",
                },
            });

            if (response.ok) {
                const data = await response.json();
                setPendingOrders(data.content);
            } else {
                const errorData = await response.json();
                Alert.alert("Error", errorData.message || "Failed to fetch in-realization orders.");
            }
        } catch (error: any) {
            Alert.alert("Error", error.message || "Unknown error occurred.");
        } finally {
            setLoading(false);
            setModalVisible(false);
        }
    };

    useEffect(() => {
        fetchInRealizationOrders();
    }, []);

    const assignDriverToOrder = async (orderId: number): Promise<void> => {
        setLoading(true);
        try {
            const token = await AsyncStorage.getItem("jwtToken");
            const response = await fetch(
                `${CONFIG.API_BASE_URL}/api/orders/${orderId}/assign`,
                {
                    method: "PUT",
                    headers: {
                        "Content-Type": "application/json",
                        Authorization: `Bearer ${token}`,
                    },
                }
            );

            if (response.ok) {
                await fetchPendingOrders();
                await fetchInRealizationOrders();
            } else {
                const errorData = await response.json();
                Alert.alert("Error", errorData.message || "Failed to assign driver to order.");
            }
        } catch (error: any) {
            Alert.alert("Error", error.message || "Unknown error occurred.");
        } finally {
            setLoading(false);
        }
    };

    const takeDeliveryPicture = async () => {
        let result = await ImagePicker.launchCameraAsync({
            allowsEditing: false,
            quality: 1,
            base64: true,
        });

        if (!result.canceled && result.assets?.[0]?.uri) {
            const base64Image = result.assets[0].base64!;
            setDeliveryImageBase64(base64Image);
            setDeliveryImageUri(result.assets[0].uri);
        }
    };

    const removeDeliveryImage = () => {
        setDeliveryImageUri("");
    };

    const confirmDelivery = async () => {
        setLoading(true);

        const location = await getUserLocation();
        if (!location) {
            Alert.alert("Error", "Unable to retrieve location.");
            return;
        }

        const requestBody = {
            location: {
                //latitude: location.latitude,
                //longitude: location.longitude,
                latitude: 53.424517,
                longitude: 14.553033,
            },
            photoBase64: deliveryImageBase64,
        };

        try {
            const token = await AsyncStorage.getItem("jwtToken");

            const response = await fetch(`${CONFIG.API_BASE_URL}/api/orders/${selectedOrder?.orderId}/deliver`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    "Authorization": `Bearer ${token}`,
                },
                body: JSON.stringify(requestBody),
            });

            if (response.ok) {
                await fetchInRealizationOrders();
                setDeliveryCompleted(true);
            } else {
                const error = await response.json();
                setCustomMessage(error.message || "Failed to confirm delivery.");
            }
        } catch (error) {
            Alert.alert("Error", "An error occurred while confirming the delivery.");
        }

        setLoading(false);
    };

    return (
        <View className="flex-1 p-4 bg-theme_background">
            <StatusBar style="light" />
            <Text className="text-2xl font-bold text-white mt-10 mb-4">Deliveries</Text>

            <Text className="text-xl font-bold text-white mb-2">In Realization</Text>
            <View style={{ minHeight: 80 }}>
                <FlatList
                    data={inRealizationOrders}
                    keyExtractor={(item) => item.orderId.toString()}
                    renderItem={({ item }) => (
                        <TouchableOpacity onPress={() => handleOrderPress(item)}>
                            <View className="bg-black/10 p-4 mb-4 rounded-lg border border-gray-300 flex-row justify-between items-center">
                                <View className="flex-1">
                                    <Text className="text-lg font-semibold text-white">Order ID: {item.orderId}</Text>
                                    <Text className="text-white">Delivery Address: {item.deliveryAddress}</Text>
                                    <Text className="text-white">Library: {item.libraryName}</Text>
                                </View>
                                <Text className="text-3xl font-bold text-green-500">{item.amount} zł</Text>
                            </View>
                        </TouchableOpacity>
                    )}
                    ListEmptyComponent={
                        !loading ? <Text className="text-white">No orders in realization.</Text> : null
                    }
                />
            </View>

            <Text className="text-xl font-bold text-white mb-2">Pending Orders</Text>
            <View className="mb-4">
                <Text className="text-white text-base mb-2">
                    Enter the maximum search distance (in meters):
                </Text>
                <View className="flex-row items-center">
                    <TextInput
                        className="p-2 bg-white rounded text-black flex-1"
                        placeholder="Enter max search distance in meters"
                        value={maxDistance}
                        onChangeText={(text) => setMaxDistance(text)}
                        keyboardType="numeric"
                    />
                    <TouchableOpacity
                        onPress={fetchPendingOrders}
                        className="bg-blue-500 px-3 py-2 rounded ml-2 flex-row items-center justify-center"
                    >
                        <Feather name="search" size={18} color="white" />
                        <Text className="text-white font-semibold text-base ml-2">
                            Search
                        </Text>
                    </TouchableOpacity>
                </View>
            </View>

            <FlatList
                data={pendingOrders}
                keyExtractor={(item) => item.orderId.toString()}
                renderItem={({ item }) => (
                    <TouchableOpacity onPress={() => handleOrderPress(item)}>
                        <View className="bg-black/10 p-4 mb-4 rounded-lg border border-gray-300 flex-row justify-between items-center">
                            <View className="flex-1">
                                <Text className="text-lg font-semibold text-white">Order ID: {item.orderId}</Text>
                                <Text className="text-white">Delivery Address: {item.deliveryAddress}</Text>
                                <Text className="text-white">Library: {item.libraryName}</Text>
                            </View>
                            <Text className="text-3xl font-bold text-green-500">{item.amount} zł</Text>
                        </View>
                    </TouchableOpacity>
                )}
                ListEmptyComponent={
                    !loading && hasSearched ? (
                        <Text className="text-white">
                            No pending orders in the selected radius.
                        </Text>
                    ) : null
                }
            />

            <CustomModal
                isVisible={modalVisible}
                title={
                    loading ? "Loading..."
                        : deliverMode ? "Complete Delivery"
                            : customMessage ? "Error"
                                : selectedOrder ? "Order Details"
                                    : "User ID"
                }
                onClose={() => {
                    if (userId) {
                        setUserId(null);
                    } else if (customMessage) {
                        setCustomMessage(null);
                    } else if (deliveryCompleted) {
                        setDeliverMode(false);
                        setModalVisible(false);
                        setSelectedOrder(null);
                        setUserId(null);
                        setDeliveryCompleted(false);
                    } else if (deliverMode) {
                        setDeliverMode(false);
                    } else {
                        setModalVisible(false);
                        setSelectedOrder(null);
                        setUserId(null);
                    }
                }}
                loading={loading}
            >
                {!loading && selectedOrder && !userId && !deliverMode && (
                    <View className="space-y-4">
                        <View className="flex-row items-center">
                            <Feather name="hash" size={20} color="#f7ca65" />
                            <Text className="text-white text-2xl font-semibold ml-2">Order ID: {selectedOrder.orderId}</Text>
                        </View>
                        <View className="flex-row items-center">
                            <Feather name="dollar-sign" size={20} color="#f7ca65" />
                            <Text className="text-white text-lg ml-2">Amount: {selectedOrder.amount} zł</Text>
                        </View>
                        <View className="flex-row items-center mt-4">
                            <Feather name="info" size={20} color="#f7ca65" />
                            <Text className="text-white text-lg ml-2">Status: {selectedOrder.status}</Text>
                        </View>
                        <View className="flex-row items-center">
                            <Feather name="credit-card" size={20} color="#f7ca65" />
                            <Text className="text-white text-lg ml-2">Payment Status: {selectedOrder.paymentStatus}</Text>
                        </View>
                        <View className="flex-row items-center mt-4">
                            <Feather name="message-square" size={20} color="#f7ca65" />
                            <Text className="text-white text-lg ml-2">Note to Driver: {selectedOrder.noteToDriver || "None"}</Text>
                        </View>
                        <View className="flex-row items-center mt-4">
                            <Feather name="book" size={20} color="#f7ca65" />
                            <Text className="text-white text-lg ml-2">Library Name: {selectedOrder.libraryName}</Text>
                        </View>
                        <View className="flex-row items-center">
                            <Feather name="map-pin" size={20} color="#f7ca65" />
                            <Text className="text-white text-lg ml-2">Delivery Address: {selectedOrder.deliveryAddress}</Text>
                        </View>

                        <Text className="text-white text-xl font-semibold mt-4 mb-2">Order Items:</Text>
                        <View className="bg-black/10 p-4 rounded-lg space-y-2 mb-2 border border-gray-300">
                            <View className="flex-row justify-between border-gray-300 pb-2">
                                <Text className="text-white font-semibold">Book Title</Text>
                                <Text className="text-white font-semibold">Authors</Text>
                                <Text className="text-white font-semibold">Quantity</Text>
                            </View>

                            {selectedOrder.orderItems.map((item, index) => (
                                <View key={index} className="flex-row justify-between py-2 border-t border-gray-300">
                                    <Text className="text-white">{item.book.title}</Text>
                                    <Text className="text-white">{item.book.authorNames.join(", ")}</Text>
                                    <Text className="text-white">{item.quantity}</Text>
                                </View>
                            ))}
                        </View>

                        {selectedOrder?.status === "ACCEPTED" ? (
                            <TouchableOpacity
                                onPress={() => assignDriverToOrder(selectedOrder.orderId)}
                                className="bg-green-500 px-6 py-3 rounded-lg flex-1 mt-2 mb-4"
                            >
                                <Text className="text-white text-center font-semibold text-lg">ASSIGN</Text>
                            </TouchableOpacity>
                        ) : (
                            <View className="flex-row justify-between mt-2 mb-4">
                                <TouchableOpacity
                                    onPress={handleNavigation}
                                    className="bg-blue-500 px-6 py-1 rounded-lg flex-1 mr-2"
                                >
                                    <Text className="text-white text-center font-semibold text-sm">
                                        {selectedOrder?.status === 'DRIVER_PICKED' ? 'Navigate to Pickup' : 'Navigate to Delivery Address'}
                                    </Text>
                                </TouchableOpacity>

                                {selectedOrder?.status === 'DRIVER_PICKED' ? (
                                    <TouchableOpacity
                                        onPress={() => handleShowID()}
                                        className="bg-yellow-500 px-6 py-3 rounded-lg flex-1 ml-2"
                                    >
                                        <Text className="text-white text-center font-semibold text-lg">Show ID</Text>
                                    </TouchableOpacity>
                                ) : (
                                    <TouchableOpacity
                                        onPress={() => setDeliverMode(true)}
                                        className="bg-green-500 px-6 py-3 rounded-lg flex-1 ml-2"
                                    >
                                        <Text className="text-white text-center font-semibold text-lg">Deliver</Text>
                                    </TouchableOpacity>
                                )}
                            </View>
                        )}
                    </View>
                )}

                {!loading && userId && (
                    <View className="justify-center mb-6">
                        <Barcode
                            value={userId}
                            options={{ format: 'CODE128', lineColor: '#ffffff' }}
                        />
                    </View>
                )}

                {!loading && deliverMode && !deliveryCompleted && !customMessage && (
                    <View className="space-y-4">
                        <Text className="text-white mb-2">Order ID: {selectedOrder?.orderId}</Text>
                        <Text className="text-white">Delivery Address: {selectedOrder?.deliveryAddress}</Text>

                        {deliveryImageUri ? (
                            <View className="mt-4 flex-row items-center">
                                <Image
                                    source={{ uri: deliveryImageUri }}
                                    style={{ width: 100, height: 100, borderRadius: 8 }}
                                />
                                <TouchableOpacity
                                    className="ml-2 bg-red-700 p-2 rounded-lg"
                                    onPress={removeDeliveryImage}
                                >
                                    <Text className="text-white">Remove Picture</Text>
                                </TouchableOpacity>
                            </View>
                        ) : (
                            <TouchableOpacity
                                className="bg-theme_accent p-3 rounded-lg items-center mt-4"
                                onPress={takeDeliveryPicture}
                            >
                                <Text className="text-white text-lg">Take a Picture</Text>
                            </TouchableOpacity>
                        )}

                        <TouchableOpacity
                            className={`${
                                deliveryImageUri ? 'bg-green-500' : 'bg-gray-500'
                            } px-6 py-3 rounded-lg flex-1 mt-4 mb-4`}
                            disabled={!deliveryImageUri}
                            onPress={confirmDelivery}
                        >
                            <Text className="text-white text-center font-semibold text-lg">Confirm Delivery</Text>
                        </TouchableOpacity>
                    </View>
                )}

                {!loading && deliveryCompleted && (
                    <View className="flex-1 justify-center items-center mb-4">
                        <Text className="text-white text-lg text-center font-semibold">Delivery Completed!</Text>
                    </View>
                )}

                {!loading && customMessage && (
                    <View className="flex-1 justify-center items-center mb-4">
                        <Text className="text-white text-lg text-center font-semibold">{customMessage}</Text>
                    </View>
                )}
            </CustomModal>
        </View>
    );
}

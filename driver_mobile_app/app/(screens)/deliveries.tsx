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
    pickupAddress: string;
    destinationAddress: string;
    isReturn: boolean;
    status: string;
    amount: string;
    paymentStatus: string;
    noteToDriver: string;
    createdAt: Date;
    acceptedAt: Date;
    driverAssignedAt: Date;
    pickedUpAt: Date;
    deliveredAt: Date;
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

            if (!deliveryCompleted) {
                setModalVisible(false);
            }
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
                (selectedOrder?.status === 'DRIVER_ACCEPTED' && !selectedOrder?.isReturn) ||
                (selectedOrder?.status === 'DRIVER_ACCEPTED' && selectedOrder?.isReturn)
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
            quality: 0.5,
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
                latitude: location.latitude,
                longitude: location.longitude,
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
                setDeliveryCompleted(true);
                await fetchInRealizationOrders();
            } else {
                const error = await response.json();
                setCustomMessage(error.message || "Failed to confirm delivery.");
            }
        } catch (error) {
            Alert.alert("Error", "An error occurred while confirming the delivery.");
        }

        setLoading(false);
        setModalVisible(true);
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
                                    <Text className="text-white">Pickup: {item.pickupAddress}</Text>
                                    <Text className="text-white">Destination: {item.destinationAddress}</Text>
                                    <Text className="text-white">Library: {item.libraryName}</Text>
                                    <Text className="text-white">
                                        Driver Assigned At: {new Date(item.driverAssignedAt).toLocaleString()}
                                    </Text>
                                    {item.isReturn && (
                                        <Text className="text-red-400 font-bold">Return Order</Text>
                                    )}
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
                                <Text className="text-white">Pickup: {item.pickupAddress}</Text>
                                <Text className="text-white">Destination: {item.destinationAddress}</Text>
                                <Text className="text-white">Library: {item.libraryName}</Text>

                                {item.isReturn ? (
                                    <Text className="text-white">
                                        Created At: {new Date(item.createdAt).toLocaleString()}
                                    </Text>
                                ) : (
                                    <Text className="text-white">
                                        Accepted At: {new Date(item.acceptedAt).toLocaleString()}
                                    </Text>
                                )}

                                {item.isReturn && (
                                    <Text className="text-red-400 font-bold">Return Order</Text>
                                )}
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
                            <Text className="text-white text-lg ml-2">Pickup Address: {selectedOrder.pickupAddress}</Text>
                        </View>
                        <View className="flex-row items-center">
                            <Feather name="map-pin" size={20} color="#f7ca65" />
                            <Text className="text-white text-lg ml-2">Destination Address: {selectedOrder.destinationAddress}</Text>
                        </View>
                        {selectedOrder.isReturn && (
                            <View className="flex-row items-center mt-2">
                                <Feather name="refresh-cw" size={20} color="#ff5555" />
                                <Text className="text-red-400 text-lg font-bold ml-2">Return Order</Text>
                            </View>
                        )}
                        {selectedOrder?.driverAssignedAt && (
                            <View className="flex-row items-center">
                                <Feather name="user-check" size={20} color="#f7ca65" />
                                <Text className="text-white text-lg ml-2 mt-2">
                                    Driver Assigned At: {new Date(selectedOrder.driverAssignedAt).toLocaleString()}
                                </Text>
                            </View>
                        )}

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

                        {(selectedOrder?.status === "ACCEPTED" && !selectedOrder?.isReturn) ||
                        (selectedOrder?.status === "PENDING" && selectedOrder?.isReturn) ? (
                            <TouchableOpacity
                                onPress={() => assignDriverToOrder(selectedOrder.orderId)}
                                className="bg-green-500 px-6 py-3 rounded-xl flex-1 mt-2"
                            >
                                <Text className="text-white text-center font-semibold text-lg">ASSIGN</Text>
                            </TouchableOpacity>
                        ) : (selectedOrder?.status === "IN_TRANSIT" || selectedOrder?.status === "DRIVER_ACCEPTED") ? (
                            <View className="flex-row justify-between mt-2">
                                <TouchableOpacity
                                    onPress={handleNavigation}
                                    className="bg-blue-500 px-6 py-1 rounded-lg flex-1 mr-2 justify-center"
                                >
                                    <Text className="text-white text-center font-semibold text-sm">
                                        {(selectedOrder?.status === 'DRIVER_ACCEPTED' && !selectedOrder?.isReturn) ||
                                        (selectedOrder?.status === 'DRIVER_ACCEPTED' && selectedOrder?.isReturn)
                                            ? 'Navigate to Pickup'
                                            : 'Navigate to Delivery Address'}
                                    </Text>
                                </TouchableOpacity>

                                {((selectedOrder?.status === 'DRIVER_ACCEPTED' && !selectedOrder?.isReturn) ||
                                    (selectedOrder?.status === 'DRIVER_ACCEPTED' && selectedOrder?.isReturn)) ? (
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
                        ) : (
                            <TouchableOpacity
                                onPress={() => handleShowID()}
                                className="bg-yellow-500 px-6 py-3 rounded-lg mt-2"
                            >
                                <Text className="text-white text-center font-semibold text-lg">Show ID</Text>
                            </TouchableOpacity>
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
                        <View className="flex-row items-center mb-2">
                            <Feather name="hash" size={20} color="#f7ca65" />
                            <Text className="text-white text-2xl font-semibold ml-2">
                                Order ID: {selectedOrder?.orderId}
                            </Text>
                        </View>

                        <View className="flex-row items-center">
                            <Feather name="map-pin" size={20} color="#f7ca65" />
                            <Text className="text-white text-lg ml-2">
                                Destination: {selectedOrder?.destinationAddress}
                            </Text>
                        </View>

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
                                className="bg-theme_accent p-3 rounded-lg flex flex-row items-center justify-center mt-4"
                                onPress={takeDeliveryPicture}
                            >
                                <Feather name="camera" size={20} color="white" className="mr-2" />
                                <Text className="text-white">Take a Picture</Text>
                            </TouchableOpacity>
                        )}

                        <TouchableOpacity
                            className={`${
                                deliveryImageUri ? 'bg-green-500' : 'bg-gray-500'
                            } px-6 py-3 rounded-lg flex-1 mt-4`}
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

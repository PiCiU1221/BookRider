import React, { useEffect, useState } from "react";
import { FlatList, Text, TouchableOpacity, View, Image } from "react-native";
import { StatusBar } from "expo-status-bar";
import AsyncStorage from "@react-native-async-storage/async-storage";
import CustomModal from "@/app/components/custom_modal";
import CONFIG from "@/config";
import {Feather} from "@expo/vector-icons";

interface OrderDriverDetailsDTO {
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
    deliveryPhotoUrl: string;
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

export default function OrderHistory() {
    const [orders, setOrders] = useState<OrderDriverDetailsDTO[]>([]);
    const [loading, setLoading] = useState<boolean>(false);
    const [modalVisible, setModalVisible] = useState<boolean>(false);
    const [selectedOrder, setSelectedOrder] = useState<OrderDriverDetailsDTO | null>(null);
    const [apiResponse, setApiResponse] = useState<any>(null);
    const [showImage, setShowImage] = useState(false);

    const fetchOrders = async (): Promise<void> => {
        setLoading(true);
        setModalVisible(true);

        try {
            const token = await AsyncStorage.getItem("jwtToken");
            const response = await fetch(`${CONFIG.API_BASE_URL}/api/orders/driver/completed?page=0&size=10`, {
                method: "GET",
                headers: {
                    Authorization: `Bearer ${token}`,
                    "Content-Type": "application/json",
                },
            });

            if (response.ok) {
                const data = await response.json();
                setOrders(data.content);
                setApiResponse({ status: "success" });
            } else {
                const errorData = await response.json();
                setApiResponse({
                    status: "error",
                    message: errorData.message || "Failed to fetch orders",
                });
            }
        } catch (error: any) {
            setApiResponse({
                status: "error",
                message: error.message || "Unknown error",
            });
        } finally {
            setLoading(false);
            setModalVisible(false);
        }
    };

    useEffect(() => {
        fetchOrders();
    }, []);

    const handleOrderPress = (order: OrderDriverDetailsDTO): void => {
        setSelectedOrder(order);
        setModalVisible(true);
    };

    return (
        <View className="flex-1 p-4 bg-theme_background">
            <StatusBar style="light" />
            <Text className="text-2xl font-bold text-white mt-10 mb-4">Order History</Text>

            <FlatList
                data={orders}
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
                                    Delivered At: {new Date(item.deliveredAt).toLocaleString()}
                                </Text>
                            </View>
                            <Text className="text-3xl font-bold text-green-500">{item.amount} zł</Text>
                        </View>
                    </TouchableOpacity>
                )}
                ListEmptyComponent={
                    <View className="items-center justify-center">
                        <Text className="text-white">You currently have no order history. Please assign new orders in the Deliveries tab to get started.</Text>
                    </View>
                }
            />

            <CustomModal
                isVisible={modalVisible}
                title={showImage ? "Delivery Image" : selectedOrder ? "Order Details" : "Loading..."}
                onClose={() => {
                    if (showImage) {
                        setShowImage(false);
                    } else {
                        setModalVisible(false);
                        setSelectedOrder(null);
                    }
                }}
                loading={loading}
            >
                {selectedOrder && !showImage ? (
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

                        {selectedOrder.isReturn ? (
                            <>
                                <View className="flex-row items-center mt-2">
                                    <Feather name="refresh-cw" size={20} color="#ff5555" />
                                    <Text className="text-red-400 text-lg font-bold ml-2">Return Order</Text>
                                </View>

                                <View className="flex-row items-center mt-2">
                                    <Feather name="calendar" size={20} color="#f7ca65" />
                                    <Text className="text-white text-lg ml-2">
                                        Created At: {new Date(selectedOrder.createdAt).toLocaleString()}
                                    </Text>
                                </View>
                                <View className="flex-row items-center">
                                    <Feather name="user-check" size={20} color="#f7ca65" />
                                    <Text className="text-white text-lg ml-2">
                                        Driver Assigned At: {new Date(selectedOrder.driverAssignedAt).toLocaleString()}
                                    </Text>
                                </View>
                                <View className="flex-row items-center">
                                    <Feather name="package" size={20} color="#f7ca65" />
                                    <Text className="text-white text-lg ml-2">
                                        Picked Up At: {new Date(selectedOrder.pickedUpAt).toLocaleString()}
                                    </Text>
                                </View>
                                <View className="flex-row items-center">
                                    <Feather name="check-circle" size={20} color="#f7ca65" />
                                    <Text className="text-white text-lg ml-2">
                                        Delivered At: {new Date(selectedOrder.deliveredAt).toLocaleString()}
                                    </Text>
                                </View>
                                <View className="flex-row items-center">
                                    <Feather name="clock" size={20} color="#f7ca65" />
                                    <Text className="text-white text-lg ml-2">
                                        Accepted At: {new Date(selectedOrder.acceptedAt).toLocaleString()}
                                    </Text>
                                </View>
                            </>
                        ) : (
                            <>
                                <View className="flex-row items-center mt-4">
                                    <Feather name="calendar" size={20} color="#f7ca65" />
                                    <Text className="text-white text-lg ml-2">
                                        Created At: {new Date(selectedOrder.createdAt).toLocaleString()}
                                    </Text>
                                </View>
                                <View className="flex-row items-center">
                                    <Feather name="clock" size={20} color="#f7ca65" />
                                    <Text className="text-white text-lg ml-2">
                                        Accepted At: {new Date(selectedOrder.acceptedAt).toLocaleString()}
                                    </Text>
                                </View>
                                <View className="flex-row items-center">
                                    <Feather name="user-check" size={20} color="#f7ca65" />
                                    <Text className="text-white text-lg ml-2">
                                        Driver Assigned At: {new Date(selectedOrder.driverAssignedAt).toLocaleString()}
                                    </Text>
                                </View>
                                <View className="flex-row items-center">
                                    <Feather name="package" size={20} color="#f7ca65" />
                                    <Text className="text-white text-lg ml-2">
                                        Picked Up At: {new Date(selectedOrder.pickedUpAt).toLocaleString()}
                                    </Text>
                                </View>
                                <View className="flex-row items-center">
                                    <Feather name="check-circle" size={20} color="#f7ca65" />
                                    <Text className="text-white text-lg ml-2">
                                        Delivered At: {new Date(selectedOrder.deliveredAt).toLocaleString()}
                                    </Text>
                                </View>
                            </>
                        )}

                        <Text className="text-white text-xl font-semibold mt-4 mb-2">Order Items:</Text>
                        <View className="bg-black/10 p-4 rounded-lg space-y-2 border border-gray-300">
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

                        <TouchableOpacity
                            onPress={() => setShowImage(true)}
                            className="bg-blue-600 rounded-lg p-3 flex-row items-center justify-center mt-4"
                        >
                            <Feather name="image" size={20} color="white" />
                            <Text className="text-white text-lg font-semibold ml-2">Show Delivery Image</Text>
                        </TouchableOpacity>
                    </View>
                ) : (
                    <View>
                        <Image
                            source={{ uri: selectedOrder?.deliveryPhotoUrl }}
                            className="w-full h-96 rounded-lg"
                            resizeMode="cover"
                        />
                    </View>
                )}
            </CustomModal>

            <CustomModal
                isVisible={apiResponse?.status === "error"}
                title="Error"
                message={apiResponse?.message}
                onClose={() => setApiResponse(null)}
                loading={loading}
            />
        </View>
    );
}

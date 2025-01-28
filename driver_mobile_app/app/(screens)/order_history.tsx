import React, { useEffect, useState } from "react";
import { FlatList, Text, TouchableOpacity, View, ActivityIndicator, Alert } from "react-native";
import { StatusBar } from "expo-status-bar";
import AsyncStorage from "@react-native-async-storage/async-storage";
import CustomModal from "@/app/components/custom_modal";
import CONFIG from "@/config";

interface OrderItem {
    orderId: number;
    deliveryAddress: string;
    libraryName: string;
    amount: string;
}

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

export default function OrderHistory() {
    const [orders, setOrders] = useState<OrderDetailsDTO[]>([]);
    const [loading, setLoading] = useState<boolean>(false);
    const [modalVisible, setModalVisible] = useState<boolean>(false);
    const [selectedOrder, setSelectedOrder] = useState<OrderDetailsDTO | null>(null);
    const [apiResponse, setApiResponse] = useState<any>(null);

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

    const handleOrderPress = (order: OrderDetailsDTO): void => {
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
                                <Text className="text-white">Delivery Address: {item.deliveryAddress}</Text>
                                <Text className="text-white">Library: {item.libraryName}</Text>
                            </View>
                            <Text className="text-3xl font-bold text-green-500">{item.amount} zł</Text>
                        </View>
                    </TouchableOpacity>
                )}
            />

            <CustomModal
                isVisible={modalVisible}
                title={selectedOrder ? "Order Details" : "Loading..."}
                onClose={() => {
                    setModalVisible(false);
                    setSelectedOrder(null);
                }}
                loading={loading}
            >
                {selectedOrder && (
                    <View className="space-y-4">
                        <Text className="text-white text-2xl font-semibold mb-2">Order ID: {selectedOrder.orderId}</Text>
                        <Text className="text-white">Amount: {selectedOrder.amount} zł</Text>
                        <Text className="text-white mt-4">Status: {selectedOrder.status}</Text>
                        <Text className="text-white">Payment Status: {selectedOrder.paymentStatus}</Text>

                        <Text className="text-white">
                            Note to Driver: {selectedOrder.noteToDriver || "None"}
                        </Text>

                        <Text className="text-white mt-4">Library Name: {selectedOrder.libraryName}</Text>
                        <Text className="text-white">Delivery Address: {selectedOrder.deliveryAddress}</Text>

                        <Text className="text-white text-xl font-semibold mt-4 mb-2">Order Items:</Text>

                        <View className="bg-black/10 p-4 rounded-lg space-y-2 mb-2">
                            <View className="flex-row justify-between border-b pb-2">
                                <Text className="text-white font-semibold">Book Title</Text>
                                <Text className="text-white font-semibold">Authors</Text>
                                <Text className="text-white font-semibold">Quantity</Text>
                            </View>

                            {selectedOrder.orderItems.map((item, index) => (
                                <View key={index} className="flex-row justify-between py-2 border-b">
                                    <Text className="text-white">{item.book.title}</Text>
                                    <Text className="text-white">{item.book.authorNames.join(", ")}</Text>
                                    <Text className="text-white">{item.quantity}</Text>
                                </View>
                            ))}
                        </View>
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

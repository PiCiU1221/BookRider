import React, { useEffect, useState } from "react";
import { FlatList, Text, TouchableOpacity, View } from "react-native";
import { StatusBar } from "expo-status-bar";
import AsyncStorage from "@react-native-async-storage/async-storage";
import CustomModal from "@/app/components/custom_modal";
import CONFIG from "@/config";
import { Feather } from "@expo/vector-icons";

import orderStatusLabels from "@/app/constants/orderStatusLabels";
import paymentStatusLabels from "@/app/constants/paymentStatusLabels";

interface UserOrderResponseDTO {
    userPayment: number;
    orderResponseDTO: OrderDetailsDTO;
}

interface OrderDetailsDTO {
    orderId: number;
    userId: string;
    libraryName: string;
    pickupAddress: string;
    destinationAddress: string;
    isReturn: boolean;
    status: string;
    amount: number;
    paymentStatus: string;
    noteToDriver: string;
    deliveryPhotoUrl: string;
    createdAt: Date;
    acceptedAt: Date;
    driverAssignedAt: Date;
    pickedUpAt: Date;
    deliveredAt: Date;
    orderItems: Array<{ book: BookResponseDTO; quantity: number }>;
}

interface BookResponseDTO {
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
    const [orders, setOrders] = useState<UserOrderResponseDTO[]>([]);
    const [loading, setLoading] = useState<boolean>(false);
    const [modalVisible, setModalVisible] = useState<boolean>(false);
    const [selectedOrder, setSelectedOrder] = useState<UserOrderResponseDTO | null>(null);
    const [apiResponse, setApiResponse] = useState<any>(null);
    const [selectedOrderType, setSelectedOrderType] = useState<OrderType>("inRealization");
    const [currentPage, setCurrentPage] = useState(0);
    const [totalPages, setTotalPages] = useState(1);

    const fetchOrders = async (orderType: string, page: number) => {
        setLoading(true);
        setModalVisible(true);

        try {
            const token = await AsyncStorage.getItem("jwtToken");

            let url = '';
            if (orderType === 'delivered') {
                url = `${CONFIG.API_BASE_URL}/api/orders/user/completed?page=${page}&size=10`;
            } else if (orderType === 'pending') {
                url = `${CONFIG.API_BASE_URL}/api/orders/user/pending?page=${page}&size=10`;
            } else if (orderType === 'inRealization') {
                url = `${CONFIG.API_BASE_URL}/api/orders/user/in-realization?page=${page}&size=10`;
            }

            const response = await fetch(url, {
                method: "GET",
                headers: {
                    Authorization: `Bearer ${token}`,
                    "Content-Type": "application/json",
                },
            });

            const data = await response.json();

            setOrders(data.content);
            setCurrentPage(data.currentPage);
            setTotalPages(data.totalPages);
            setApiResponse({ status: "success" });
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
        fetchOrders(selectedOrderType, currentPage);
    }, [selectedOrderType, currentPage]);

    const handleOrderPress = (order: UserOrderResponseDTO): void => {
        setSelectedOrder(order);
        setModalVisible(true);
    };

    const handleOrderTypeChange = (newOrderType: string) => {
        setCurrentPage(0);
        setSelectedOrderType(newOrderType as OrderType);
    };

    type OrderType = "inRealization" | "pending" | "delivered";

    const orderTranslations: Record<OrderType, string> = {
        inRealization: "W realizacji",
        pending: "Oczekujące",
        delivered: "Dostarczone",
    };

    const getOrderTitle = () => {
        return orderTranslations[selectedOrderType];
    };

    return (
        <View className="flex-1 p-4 bg-theme_background">
            <StatusBar style="light" />
            <View className="flex-row items-center justify-center mt-10 mb-4">
                <Feather name="archive" size={24} color="#f7ca65" className="mr-2" />
                <Text className="text-2xl font-bold text-white">Historia zamówień</Text>
            </View>

            <View className="flex-row justify-between mb-6 space-x-3">
                {["inRealization", "pending", "delivered"].map((orderType, index) => (
                    <TouchableOpacity
                        key={orderType}
                        onPress={() => handleOrderTypeChange(orderType)}
                        className={`flex-1 py-2 px-4 text-center border border-transparent ${
                            selectedOrderType === orderType
                                ? "bg-theme_accent text-white"
                                : "bg-black/20 text-gray-400"
                        } ${
                            index === 0
                                ? "rounded-l-lg"
                                : index === 2
                                    ? "rounded-r-lg"
                                    : ""
                        }`}
                    >
                        <Text
                            className={`font-semibold text-base text-center ${
                                selectedOrderType === orderType ? "text-white" : "text-gray-300"
                            }`}
                        >
                            {orderTranslations[orderType as OrderType]}
                        </Text>
                    </TouchableOpacity>
                ))}
            </View>

            <Text className="text-xl font-bold text-white mb-4">{getOrderTitle()}</Text>

            <FlatList
                data={orders}
                keyExtractor={(item) => item.orderResponseDTO.orderId.toString()}
                contentContainerStyle={{ paddingBottom: 10 }}
                renderItem={({ item }) => (
                    <TouchableOpacity onPress={() => handleOrderPress(item)}>
                        <View className="bg-black/10 p-4 mb-4 rounded-lg border border-gray-300 flex-row justify-between items-center">
                            <View className="flex-1">
                                <Text className="text-lg font-semibold text-white">ID zamówienia: {item.orderResponseDTO.orderId}</Text>
                                <Text className="text-white">Odbiór: {item.orderResponseDTO.pickupAddress}</Text>
                                <Text className="text-white">Dostawa: {item.orderResponseDTO.destinationAddress}</Text>
                                <Text className="text-white">Biblioteka: {item.orderResponseDTO.libraryName}</Text>
                                <Text className="text-white">
                                    {item.orderResponseDTO.deliveredAt ?
                                        `Data dostarczenia: ${new Date(item.orderResponseDTO.deliveredAt).toLocaleString('pl-PL')}` :
                                        `Data utworzenia: ${new Date(item.orderResponseDTO.createdAt).toLocaleString('pl-PL')}`}
                                </Text>
                            </View>
                            <Text className="text-3xl font-bold text-green-500">{item.userPayment.toFixed(2)} zł</Text>
                        </View>
                    </TouchableOpacity>
                )}
                ListEmptyComponent={
                    <View className="items-center justify-center mb-12 mt-12">
                        <Text className="text-white text-lg">Brak dokonanych zamówień</Text>
                    </View>
                }
                ListFooterComponent={
                    <View className="flex-row items-center px-6 relative mt-2">
                        <TouchableOpacity
                            className="w-32 px-4 py-2 bg-theme_accent rounded-lg disabled:opacity-50 absolute left-0"
                            onPress={() => fetchOrders(selectedOrderType, currentPage - 1)}
                            disabled={currentPage === 0}
                        >
                            <Text className="text-white text-lg font-semibold text-center">Poprzednia</Text>
                        </TouchableOpacity>

                        <View className="flex-1 items-center">
                            <Text className="text-white text-lg font-bold">
                                Strona {currentPage + 1} z {totalPages}
                            </Text>
                        </View>

                        <TouchableOpacity
                            className="w-32 px-4 py-2 bg-theme_accent rounded-lg disabled:opacity-50 absolute right-0"
                            onPress={() => fetchOrders(selectedOrderType, currentPage + 1)}
                            disabled={currentPage >= totalPages - 1}
                        >
                            <Text className="text-white text-lg font-semibold text-center">Następna</Text>
                        </TouchableOpacity>
                    </View>
                }
            />

            <CustomModal
                isVisible={modalVisible}
                title={selectedOrder ? "Szczegóły zamówienia" : "Ładowanie..."}
                onClose={() => {
                    setModalVisible(false);
                    setSelectedOrder(null);
                }}
                loading={loading}
            >
                {selectedOrder && (
                    <View className="space-y-4">
                        <View className="flex-row items-center">
                            <Feather name="hash" size={20} color="#f7ca65" />
                            <Text className="text-white text-2xl font-semibold ml-2">ID zamówienia: {selectedOrder.orderResponseDTO.orderId}</Text>
                        </View>
                        <View className="flex-row items-center">
                            <Feather name="dollar-sign" size={20} color="#f7ca65" />
                            <Text className="text-white text-lg ml-2">Kwota: {selectedOrder.userPayment.toFixed(2)} zł</Text>
                        </View>
                        <View className="flex-row items-center mt-4">
                            <Feather name="info" size={20} color="#f7ca65" />
                            <Text className="text-white text-lg ml-2">Status: {orderStatusLabels[selectedOrder.orderResponseDTO.status as keyof typeof orderStatusLabels]}</Text>
                        </View>
                        <View className="flex-row items-center">
                            <Feather name="credit-card" size={20} color="#f7ca65" />
                            <Text className="text-white text-lg ml-2">Status płatności: {paymentStatusLabels[selectedOrder.orderResponseDTO.paymentStatus as keyof typeof paymentStatusLabels]}</Text>
                        </View>
                        <View className="flex-row items-center mt-4">
                            <Feather name="message-square" size={20} color="#f7ca65" />
                            <Text className="text-white text-lg ml-2">Wiadomość dla kierowcy: {selectedOrder.orderResponseDTO.noteToDriver || "None"}</Text>
                        </View>
                        <View className="flex-row items-center mt-4">
                            <Feather name="book" size={20} color="#f7ca65" />
                            <Text className="text-white text-lg ml-2">Biblioteka: {selectedOrder.orderResponseDTO.libraryName}</Text>
                        </View>
                        <View className="flex-row items-center">
                            <Feather name="map-pin" size={20} color="#f7ca65" />
                            <Text className="text-white text-lg ml-2">Odbiór: {selectedOrder.orderResponseDTO.pickupAddress}</Text>
                        </View>
                        <View className="flex-row items-center">
                            <Feather name="map-pin" size={20} color="#f7ca65" />
                            <Text className="text-white text-lg ml-2">Dostawa: {selectedOrder.orderResponseDTO.destinationAddress}</Text>
                        </View>

                        {selectedOrder.orderResponseDTO.isReturn ? (
                            <>
                                <View className="flex-row items-center mt-2">
                                    <Feather name="refresh-cw" size={20} color="#ff5555" />
                                    <Text className="text-red-400 text-lg font-bold ml-2">Zamówienie zwrotne</Text>
                                </View>

                                <View className="flex-row items-center mt-2">
                                    <Feather name="calendar" size={20} color="#f7ca65" />
                                    <Text className="text-white text-lg ml-2">
                                        Utworzono: {new Date(selectedOrder.orderResponseDTO.createdAt).toLocaleString('pl-PL')}
                                    </Text>
                                </View>
                                {selectedOrder.orderResponseDTO.driverAssignedAt && (
                                    <View className="flex-row items-center">
                                        <Feather name="user-check" size={20} color="#f7ca65" />
                                        <Text className="text-white text-lg ml-2">
                                            Przypisano kierowce: {new Date(selectedOrder.orderResponseDTO.driverAssignedAt).toLocaleString('pl-PL')}
                                        </Text>
                                    </View>
                                )}
                                {selectedOrder.orderResponseDTO.pickedUpAt && (
                                    <View className="flex-row items-center">
                                        <Feather name="package" size={20} color="#f7ca65" />
                                        <Text className="text-white text-lg ml-2">
                                            Odebrano: {new Date(selectedOrder.orderResponseDTO.pickedUpAt).toLocaleString('pl-PL')}
                                        </Text>
                                    </View>
                                )}
                                {selectedOrder.orderResponseDTO.deliveredAt && (
                                    <View className="flex-row items-center">
                                        <Feather name="check-circle" size={20} color="#f7ca65" />
                                        <Text className="text-white text-lg ml-2">
                                            Dostarczono: {new Date(selectedOrder.orderResponseDTO.deliveredAt).toLocaleString('pl-PL')}
                                        </Text>
                                    </View>
                                )}
                                {selectedOrder.orderResponseDTO.acceptedAt && (
                                    <View className="flex-row items-center">
                                        <Feather name="clock" size={20} color="#f7ca65" />
                                        <Text className="text-white text-lg ml-2">
                                            Przyjęto: {new Date(selectedOrder.orderResponseDTO.acceptedAt).toLocaleString('pl-PL')}
                                        </Text>
                                    </View>
                                )}
                            </>
                        ) : (
                            <>
                                <View className="flex-row items-center mt-4">
                                    <Feather name="calendar" size={20} color="#f7ca65" />
                                    <Text className="text-white text-lg ml-2">
                                        Utworzono: {new Date(selectedOrder.orderResponseDTO.createdAt).toLocaleString('pl-PL')}
                                    </Text>
                                </View>
                                {selectedOrder.orderResponseDTO.acceptedAt && (
                                    <View className="flex-row items-center">
                                        <Feather name="clock" size={20} color="#f7ca65" />
                                        <Text className="text-white text-lg ml-2">
                                            Przyjęto: {new Date(selectedOrder.orderResponseDTO.acceptedAt).toLocaleString('pl-PL')}
                                        </Text>
                                    </View>
                                )}
                                {selectedOrder.orderResponseDTO.driverAssignedAt && (
                                    <View className="flex-row items-center">
                                        <Feather name="user-check" size={20} color="#f7ca65" />
                                        <Text className="text-white text-lg ml-2">
                                            Kierowca przypisany: {new Date(selectedOrder.orderResponseDTO.driverAssignedAt).toLocaleString('pl-PL')}
                                        </Text>
                                    </View>
                                )}
                                {selectedOrder.orderResponseDTO.pickedUpAt && (
                                    <View className="flex-row items-center">
                                        <Feather name="package" size={20} color="#f7ca65" />
                                        <Text className="text-white text-lg ml-2">
                                            Odebrano: {new Date(selectedOrder.orderResponseDTO.pickedUpAt).toLocaleString('pl-PL')}
                                        </Text>
                                    </View>
                                )}
                                {selectedOrder.orderResponseDTO.deliveredAt && (
                                    <View className="flex-row items-center">
                                        <Feather name="check-circle" size={20} color="#f7ca65" />
                                        <Text className="text-white text-lg ml-2">
                                            Dostarczono: {new Date(selectedOrder.orderResponseDTO.deliveredAt).toLocaleString('pl-PL')}
                                        </Text>
                                    </View>
                                )}
                            </>
                        )}

                        <View className="flex-row items-center mt-4 mb-2">
                            <Feather name="book" size={20} color="#f7ca65" />
                            <Text className="text-white text-xl font-semibold ml-2">Zamówione książki</Text>
                        </View>
                        <View className="bg-black/10 p-4 rounded-lg space-y-2 border border-gray-300">
                            <View className="flex-row justify-between border-gray-300 pb-2">
                                <Text className="text-white font-semibold">Tytuł</Text>
                                <Text className="text-white font-semibold">Autorzy</Text>
                                <Text className="text-white font-semibold">Ilość</Text>
                            </View>

                            {selectedOrder.orderResponseDTO.orderItems.map((item, index) => (
                                <View key={index} className="flex-row justify-between py-2 border-t border-gray-300">
                                    <Text className="text-white" style={{ maxWidth: 120, flexWrap: 'wrap' }}>
                                        {item.book.title}
                                    </Text>
                                    <Text className="text-white" style={{ maxWidth: 120, flexWrap: 'wrap' }}>
                                        {item.book.authorNames.join(", ")}
                                    </Text>
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

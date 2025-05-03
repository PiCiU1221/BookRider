import AsyncStorage from "@react-native-async-storage/async-storage";
import CONFIG from "@/config";
import React, {useEffect, useState} from "react";
import {Alert, FlatList, Text, TouchableOpacity, View} from "react-native";
import {StatusBar} from "expo-status-bar";
import CustomModal from "@/app/components/custom_modal";
import {Feather} from "@expo/vector-icons";
import {CameraView, useCameraPermissions} from 'expo-camera';
import { StyleSheet } from 'react-native';

import rentalReturnStatusLabels from "../constants/rentalReturnStatusLabels";
import orderStatusLabels from "../constants/orderStatusLabels";

interface RentalReturnDTO {
    id: number;
    libraryName: string;
    orderId: number | null;
    orderStatus: string;
    status: string;
    returnedAt: Date | null;
    createdAt: Date;
    rentalReturnItems: RentalReturnItemDTO[];
}

interface RentalReturnItemDTO {
    id: number;
    rentalId: number;
    book: BookDTO;
    returnedQuantity: number;
}

interface BookDTO {
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

export default function RentalReturns() {
    const [rentalReturns, setRentalReturns] = useState<RentalReturnDTO[]>([]);
    const [loading, setLoading] = useState<boolean>(false);
    const [modalVisible, setModalVisible] = useState<boolean>(false);
    const [selectedRentalReturn, setSelectedRentalReturn] = useState<RentalReturnDTO | null>(null);

    const [apiResponse, setApiResponse] = useState<any>(null);

    const [currentPage, setCurrentPage] = useState(0);
    const [totalPages, setTotalPages] = useState(1);

    const [permission, requestPermission] = useCameraPermissions();
    const [showCamera, setShowCamera] = useState(false);
    const [scanned, setScanned] = useState(false);

    const fetchRentalReturns = async (page: number) => {
        setLoading(true);
        setModalVisible(true);

        try {
            const token = await AsyncStorage.getItem("jwtToken");

            let url = `${CONFIG.API_BASE_URL}/api/rental-returns?page=${page}&size=10`;

            const response = await fetch(url, {
                method: "GET",
                headers: {
                    Authorization: `Bearer ${token}`,
                    "Content-Type": "application/json",
                },
            });

            const data = await response.json();

            setRentalReturns(data.content);
            setCurrentPage(data.currentPage);
            setTotalPages(data.totalPages);
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
        fetchRentalReturns(currentPage);
    }, [currentPage]);

    const handleRentalReturnPress = (rentalReturn: RentalReturnDTO): void => {
        setSelectedRentalReturn(rentalReturn);
        setModalVisible(true);
    };

    const scanDriversID = async () => {
        if (!permission?.granted) {
            const { granted } = await requestPermission();
            if (!granted) {
                Alert.alert("Camera Permission", "Camera access is required to scan barcodes.");
                return;
            }
        }

        setModalVisible(false);
        setShowCamera(true);
        setScanned(false);
    };

    const handoverRentalReturn = async (driverId: string) => {
        setLoading(true);
        setModalVisible(true);

        try {
            const token = await AsyncStorage.getItem("jwtToken");

            let url = `${CONFIG.API_BASE_URL}/api/rental-returns/${selectedRentalReturn?.id}/handover?driverId=${driverId}`;

            const response = await fetch(url, {
                method: "PUT",
                headers: {
                    Authorization: `Bearer ${token}`,
                    "Content-Type": "application/json",
                },
            });

            if (!response.ok) {
                throw new Error();
            }

            await fetchRentalReturns(currentPage);
            setApiResponse({
                status: "Sukces",
                message: "Pomyślnie zatwierdzono odbiór"
            });
        } catch (error: any) {
            setApiResponse({
                status: "error",
                message: error.message || "Unknown error",
            });
        } finally {
            setModalVisible(true);
        }
    }

    return (
        <View className="flex-1 p-4 bg-theme_background">
            <StatusBar style="light" />

            {showCamera && (
                <View className="absolute top-0 bottom-0 left-0 right-0 z-50">
                    <CameraView
                        style={{ flex: 1 }}
                        facing="back"
                        barcodeScannerSettings={{ barcodeTypes: ["code128"] }}
                        onBarcodeScanned={({ data }) => {
                            if (!scanned) {
                                setScanned(true);
                                setShowCamera(false);
                                handoverRentalReturn(data);
                            }
                        }}
                    >
                        <TouchableOpacity
                            style={styles.cancelButton}
                            onPress={() => {
                                setShowCamera(false);
                                setModalVisible(true);
                            }}
                        >
                            <Text style={styles.cancelText}>Cancel</Text>
                        </TouchableOpacity>

                        <View style={styles.scanArea} />
                    </CameraView>
                </View>
            )}

            <View className="flex-row items-center justify-center mt-10 mb-4">
                <Feather name="feather" size={24} color="#f7ca65" className="mr-2" />
                <Text className="text-2xl font-bold text-white">Zwroty wypożyczeń</Text>
            </View>

            <FlatList
                data={rentalReturns}
                keyExtractor={(item) => item.id.toString()}
                contentContainerStyle={{ paddingBottom: 10 }}
                renderItem={({ item }) => (
                    <TouchableOpacity onPress={() => handleRentalReturnPress(item)}>
                        <View className="bg-black/10 p-4 mb-4 rounded-lg border border-gray-300 flex-row justify-between items-center">
                            <View className="flex-1">
                                <Text className="text-lg font-semibold text-white">ID zwrotu: {item.id}</Text>
                                <Text className="text-white">Biblioteka: {item.libraryName}</Text>

                                {item.orderId && (
                                    <Text className="text-white">ID zamówienia zwrotnego: {item.orderId}</Text>
                                )}

                                <Text className="text-white">Status: {rentalReturnStatusLabels[item.status as keyof typeof rentalReturnStatusLabels]}</Text>

                                {item.returnedAt && (
                                    <Text className="text-white">
                                        Zwrócono: {new Date(item.returnedAt).toLocaleString('pl-PL')}
                                    </Text>
                                )}

                                <Text className="text-white">
                                    Utworzono: {new Date(item.createdAt).toLocaleString('pl-PL')}
                                </Text>
                            </View>
                        </View>
                    </TouchableOpacity>
                )}
                ListEmptyComponent={
                    <View className="items-center justify-center mb-12 mt-12">
                        <Text className="text-white text-lg">Brak utworzonych zwrotów zamówień.</Text>
                    </View>
                }
                ListFooterComponent={
                    <View className="flex-row items-center px-6 relative mt-2">
                        <TouchableOpacity
                            className="w-32 px-4 py-2 bg-theme_accent rounded-lg disabled:opacity-50 absolute left-0"
                            onPress={() => fetchRentalReturns(currentPage - 1)}
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
                            onPress={() => fetchRentalReturns(currentPage + 1)}
                            disabled={currentPage >= totalPages - 1}
                        >
                            <Text className="text-white text-lg font-semibold text-center">Następna</Text>
                        </TouchableOpacity>
                    </View>
                }
            />

            <CustomModal
                isVisible={modalVisible}
                title={selectedRentalReturn ? "Szczegóły zwrotu" : "Loading..."}
                onClose={() => {
                    setModalVisible(false);
                    setSelectedRentalReturn(null);
                }}
                loading={loading}
            >
                {selectedRentalReturn && (
                    <View className="space-y-4">
                        <View className="flex-row items-center">
                            <Feather name="package" size={20} color="#f7ca65" />
                            <Text className="text-white text-2xl font-semibold ml-2">ID zwrotu: {selectedRentalReturn.id}</Text>
                        </View>

                        <View className="flex-row items-center">
                            <Feather name="book-open" size={20} color="#f7ca65" />
                            <Text className="text-white text-lg ml-2">Biblioteka: {selectedRentalReturn.libraryName}</Text>
                        </View>

                        {selectedRentalReturn.orderId && (
                            <View className="flex-row items-center">
                                <Feather name="hash" size={20} color="#f7ca65" />
                                <Text className="text-white text-lg ml-2">ID zamówienia zwrotnego: {selectedRentalReturn.orderId}</Text>
                            </View>
                        )}

                        <View className="flex-row items-center mt-4">
                            <Feather name="info" size={20} color="#f7ca65" />
                            <Text className="text-white text-lg ml-2">Status: {rentalReturnStatusLabels[selectedRentalReturn.status as keyof typeof rentalReturnStatusLabels]}</Text>
                        </View>

                        {selectedRentalReturn.orderStatus && (
                            <View className="flex-row items-center">
                                <Feather name="info" size={20} color="#f7ca65" />
                                <Text className="text-white text-lg ml-2">Status zamówienia: {orderStatusLabels[selectedRentalReturn.orderStatus as keyof typeof orderStatusLabels]}</Text>
                            </View>
                        )}

                        {selectedRentalReturn.returnedAt && (
                            <View className="flex-row items-center">
                                <Feather name="calendar" size={20} color="#f7ca65" />
                                <Text className="text-white text-lg ml-2">
                                    Zwrócono: {new Date(selectedRentalReturn.returnedAt).toLocaleString('pl-PL')}
                                </Text>
                            </View>
                        )}

                        <View className="flex-row items-center">
                            <Feather name="calendar" size={20} color="#f7ca65" />
                            <Text className="text-white text-lg ml-2">
                                Utworzono: {new Date(selectedRentalReturn.createdAt).toLocaleString('pl-PL')}
                            </Text>
                        </View>

                        <View className="flex-row items-center mt-4 mb-2">
                            <Feather name="book" size={20} color="#f7ca65" />
                            <Text className="text-white text-xl font-semibold ml-2">Książki do zwrotu</Text>
                        </View>
                        <View className="bg-black/10 p-4 rounded-lg space-y-2 border border-gray-300">
                            <View className="flex-row justify-between border-gray-300 pb-2">
                                <Text className="text-white font-semibold">Tytuł</Text>
                                <Text className="text-white font-semibold">Autorzy</Text>
                                <Text className="text-white font-semibold">Ilość</Text>
                            </View>

                            {selectedRentalReturn.rentalReturnItems.map((item, index) => (
                                <View key={index} className="flex-row justify-between py-2 border-t border-gray-300">
                                    <Text className="text-white" style={{ maxWidth: 120, flexWrap: 'wrap' }}>
                                        {item.book.title}
                                    </Text>
                                    <Text className="text-white" style={{ maxWidth: 120, flexWrap: 'wrap' }}>
                                        {item.book.authorNames.join(", ")}
                                    </Text>
                                    <Text className="text-white">{item.returnedQuantity}</Text>
                                </View>
                            ))}
                        </View>

                        {selectedRentalReturn.orderStatus == "DRIVER_ACCEPTED" && (
                            <TouchableOpacity
                                onPress={scanDriversID}
                                className="bg-blue-500 px-6 py-3 rounded-lg mt-4 flex-row items-center justify-center space-x-2"
                            >
                                <Feather name="camera" size={24} color="white" className="mr-2" />
                                <Text className="text-white text-lg font-semibold">Zeskanuj ID kierowcy</Text>
                            </TouchableOpacity>
                        )}
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

const styles = StyleSheet.create({
    cancelButton: {
        position: 'absolute',
        top: 50,
        left: 20,
        backgroundColor: 'rgba(0,0,0,0.6)',
        padding: 10,
        borderRadius: 8,
        zIndex: 2,
    },
    cancelText: {
        color: 'white',
        fontSize: 16,
    },
    scanArea: {
        position: 'absolute',
        top: '40%',
        left: '10%',
        width: '80%',
        height: 150,
        borderColor: '#00FF00',
        borderWidth: 2,
        borderRadius: 10,
        backgroundColor: 'rgba(0, 0, 0, 0.2)',
    },
});
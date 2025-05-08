import React, { useEffect, useState } from "react";
import {FlatList, Image, Text, TouchableOpacity, View, Animated, TextInput} from "react-native";
import { StatusBar } from "expo-status-bar";
import AsyncStorage from "@react-native-async-storage/async-storage";
import CustomModal from "@/app/components/custom_modal";
import CONFIG from "@/config";
import { Feather } from "@expo/vector-icons";

interface Book {
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

interface Rental {
    rentalId: number;
    book: Book;
    libraryName: string;
    libraryAddress: string;
    orderId: number;
    quantity: number;
    rentedAt: string;
    returnDeadline: string;
    status: string;
}

interface RentalReturnCost {
    totalPrice: number;
    deliveryCost: number;
    totalLateFees: number;
    lateFees: LateFee[];
}

interface LateFee {
    rental: Rental;
    lateFee: number;
}

export default function Rentals() {
    const [rentals, setRentals] = useState<Rental[]>([]);
    const [loading, setLoading] = useState<boolean>(false);
    const [modalVisible, setModalVisible] = useState<boolean>(false);
    const [selectedRental, setSelectedRental] = useState<Rental | null>(null);
    const [apiResponse, setApiResponse] = useState<any>(null);
    const [markedRentals, setMarkedRentals] = useState<Set<number>>(new Set());
    const [bottomBoxHeight] = useState(new Animated.Value(0));
    const [rentalCheckViewVisible, setRentalCheckViewVisible] = useState<boolean>(false);
    const [quantities, setQuantities] = useState<{ [rentalId: number]: number }>({});
    const [rentalReturnCost, setRentalReturnCost] = useState<RentalReturnCost | null>(null);
    const [rentalReturnPriceVisible, setRentalReturnPriceVisible] = useState<boolean>(false);

    const [currentPage, setCurrentPage] = useState(0);
    const [totalPages, setTotalPages] = useState(1);

    const fetchRentals = async (page: number): Promise<void> => {
        setLoading(true);
        setModalVisible(true);

        try {
            const token = await AsyncStorage.getItem("jwtToken");
            const response = await fetch(`${CONFIG.API_BASE_URL}/api/rentals?page=${page}&size=10`, {
                method: "GET",
                headers: {
                    Authorization: `Bearer ${token}`,
                    "Content-Type": "application/json",
                },
            });

            if (response.ok) {
                const data = await response.json();
                setRentals(data.content.map((rental: any) => ({
                    rentalId: rental.rentalId,
                    book: { ...rental.book },
                    libraryName: rental.libraryName,
                    libraryAddress: rental.libraryAddress,
                    orderId: rental.orderId,
                    quantity: rental.quantity,
                    rentedAt: rental.rentedAt,
                    returnDeadline: rental.returnDeadline,
                    status: rental.status,
                })));

                setCurrentPage(data.currentPage);
                setTotalPages(data.totalPages);
            } else {
                const errorData = await response.json();
                setApiResponse({
                    status: "Error",
                    message: errorData.message || "Failed to fetch rentals",
                });
            }
        } catch (error: any) {
            setApiResponse({
                status: "Error",
                message: error.message || "Unknown error",
            });
        } finally {
            setLoading(false);
            setModalVisible(false);
        }
    };

    useEffect(() => {
        fetchRentals(currentPage);
    }, []);

    const handleRentalPress = (rental: Rental): void => {
        setSelectedRental(rental);
        setModalVisible(true);
    };

    useEffect(() => {
        if (markedRentals.size > 0) {
            Animated.spring(bottomBoxHeight, {
                toValue: 120,
                useNativeDriver: false,
            }).start();
        } else {
            Animated.spring(bottomBoxHeight, {
                toValue: 0,
                useNativeDriver: false,
            }).start();
        }
    }, [markedRentals]);

    const toggleMarkRental = (rentalId: number) => {
        setMarkedRentals((prevMarkedRentals) => {
            const newMarkedRentals = new Set(prevMarkedRentals);
            if (newMarkedRentals.has(rentalId)) {
                newMarkedRentals.delete(rentalId);
            } else {
                newMarkedRentals.add(rentalId);
            }
            return newMarkedRentals;
        });
    };

    const handleQuantityChange = (rentalId: number, delta: number) => {
        setQuantities((prevQuantities) => {
            const rental = rentals.find(r => r.rentalId === rentalId);
            if (!rental) return prevQuantities;

            const currentQuantity = prevQuantities[rentalId] || rental.quantity; // Default to rental.quantity
            const newQuantity = currentQuantity + delta;

            if (newQuantity >= 1 && newQuantity <= rental.quantity) {
                return { ...prevQuantities, [rentalId]: newQuantity };
            }

            return prevQuantities;
        });
    };

    const fetchRentalReturn = async (inPersonReturn: boolean, isPriceCalculation: boolean): Promise<void> => {
        setLoading(true);

        try {
            const token = await AsyncStorage.getItem("jwtToken");
            const baseEndpoint = inPersonReturn ? "/api/rental-returns/in-person" : "/api/rental-returns";
            const endpoint = isPriceCalculation ? `${baseEndpoint}/calculate-price` : baseEndpoint;

            const rentalReturnRequests = Array.from(markedRentals).map((rentalId) => ({
                rentalId,
                quantityToReturn: quantities[rentalId] || 1,
            }));

            const requestBody: any = { rentalReturnRequests };

            if (!inPersonReturn) {
                requestBody.createAddressDTO = { street, city, postalCode };
            }

            const response = await fetch(`${CONFIG.API_BASE_URL}${endpoint}`, {
                method: "POST",
                headers: {
                    Authorization: `Bearer ${token}`,
                    "Content-Type": "application/json",
                },
                body: JSON.stringify(requestBody),
            });

            if (response.ok) {
                const data: RentalReturnCost = await response.json();
                if (isPriceCalculation) {
                    setRentalReturnCost(data);
                    setRentalReturnPriceVisible(true);
                } else {
                    setRentalReturnPriceVisible(false);
                    setApiResponse({ status: "Sukces", message: "Zwrot przetworzony pomyślnie" });
                    setSelectedRental(null);
                }
            } else {
                const errorData = await response.json();
                setApiResponse({ status: "Error", message: errorData.message || "Failed to process rental return" });
            }
        } catch (error: any) {
            setApiResponse({ status: "Error", message: error.message || "Unknown error" });
        } finally {
            setLoading(false);
        }
    };

    const [addressInputMode, setAddressInputMode] = useState<boolean>(false);

    const [street, setStreet] = useState("");
    const [city, setCity] = useState("");
    const [postalCode, setPostalCode] = useState("");

    const handleUpdateAddress = () => {
        if (!street || !city || !postalCode) {
            setApiResponse({
                status: "Error",
                message: "Please fill in all fields",
            });
            return;
        }
        setAddressInputMode(false);
        fetchRentalReturn(false, true);
    };

    return (
        <View className="flex-1 p-4 bg-theme_background">
            <StatusBar style="light" />
            <View className="flex-row items-center justify-center mt-10 mb-4">
                <Feather name="book-open" size={24} color="#f7ca65" className="mr-2" />
                <Text className="text-2xl font-bold text-white">Obecne wypożyczenia</Text>
            </View>

            <Animated.View style={{ marginBottom: bottomBoxHeight, paddingBottom: 60 }}>
                <FlatList
                    data={rentals}
                    keyExtractor={(item) => item.rentalId.toString()}
                    contentContainerStyle={{ paddingBottom: 20 }}
                    renderItem={({ item }) => (
                        <TouchableOpacity onPress={() => handleRentalPress(item)}>
                            <View className="bg-black/10 p-4 mb-4 rounded-lg border border-gray-300 flex-row justify-between items-center">
                                <TouchableOpacity onPress={() => toggleMarkRental(item.rentalId)}>
                                    <Feather
                                        name={markedRentals.has(item.rentalId) ? "check-square" : "square"}
                                        size={24}
                                        color="#f7ca65"
                                    />
                                </TouchableOpacity>
                                <View className="flex-1 ml-4">
                                    <Text className="text-lg font-semibold text-white">{item.book.title}</Text>
                                    <Text className="text-white text-base font-medium">{item.book.authorNames.join(', ')}</Text>
                                    <Text className="text-white mt-2">Biblioteka: {item.libraryName}</Text>
                                    <Text className="text-white">Termin zwrotu: {new Date(item.returnDeadline).toLocaleDateString('pl-PL')}</Text>
                                </View>
                                <Text className="text-2xl font-bold text-yellow-400">x{item.quantity}</Text>
                            </View>
                        </TouchableOpacity>
                    )}
                    ListEmptyComponent={
                        <View className="items-center justify-center mb-12 mt-10">
                            <Text className="text-white text-lg">Nie masz jeszcze wypożyczeń. Zamów książkę i po dostawie pojawi się tutaj.</Text>
                        </View>
                    }
                    ListFooterComponent={
                        <View className="flex-row items-center px-6 relative mt-2">
                            <TouchableOpacity
                                className="w-32 px-4 py-2 bg-theme_accent rounded-lg disabled:opacity-50 absolute left-0"
                                onPress={() => fetchRentals(currentPage - 1)}
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
                                onPress={() => fetchRentals(currentPage + 1)}
                                disabled={currentPage >= totalPages - 1}
                            >
                                <Text className="text-white text-lg font-semibold text-center">Następna</Text>
                            </TouchableOpacity>
                        </View>
                    }
                />
            </Animated.View>

            <CustomModal
                isVisible={modalVisible}
                title={selectedRental ? "Szczegóły wypożyczenia" : "Ładowanie..."}
                onClose={() => {
                    setModalVisible(false);
                    setSelectedRental(null);
                }}
                loading={loading}
            >
                {selectedRental && (
                    <View className="space-y-4">
                        <View className="items-center">
                            <Image
                                source={{ uri: selectedRental.book.image }}
                                style={{ width: "100%", height: 200, borderRadius: 8 }}
                                resizeMode="contain"
                            />
                        </View>

                        <View className="flex-row items-center">
                            <Feather name="book" size={20} color="#f7ca65" />
                            <Text className="text-white text-lg font-semibold ml-2">Tytuł: {selectedRental.book.title}</Text>
                        </View>

                        <View className="flex-row items-center">
                            <Feather name="user" size={20} color="#f7ca65" />
                            <Text className="text-white text-lg ml-2">Autorzy: {selectedRental.book.authorNames.join(", ")}</Text>
                        </View>

                        <View className="flex-row items-center">
                            <Feather name="layers" size={20} color="#f7ca65" />
                            <Text className="text-white text-lg ml-2">Kategoria: {selectedRental.book.categoryName}</Text>
                        </View>

                        <View className="flex-row items-center">
                            <Feather name="calendar" size={20} color="#f7ca65" />
                            <Text className="text-white text-lg ml-2">Rok wydania: {selectedRental.book.releaseYear}</Text>
                        </View>

                        <View className="flex-row items-center">
                            <Feather name="book-open" size={20} color="#f7ca65" />
                            <Text className="text-white text-lg ml-2">Wydawnictwo: {selectedRental.book.publisherName}</Text>
                        </View>

                        <View className="flex-row items-center mb-4">
                            <Feather name="globe" size={20} color="#f7ca65" />
                            <Text className="text-white text-lg ml-2">Język: {selectedRental.book.languageName}</Text>
                        </View>

                        <View className="flex-row items-center">
                            <Feather name="map-pin" size={20} color="#f7ca65" />
                            <Text className="text-white text-lg ml-2">Biblioteka: {selectedRental.libraryName}</Text>
                        </View>

                        <View className="flex-row items-center mb-4">
                            <Feather name="map-pin" size={20} color="#f7ca65" />
                            <Text className="text-white text-lg ml-2">Adres biblioteki: {selectedRental.libraryAddress}</Text>
                        </View>

                        <View className="flex-row items-center">
                            <Feather name="calendar" size={20} color="#f7ca65" />
                            <Text className="text-white text-lg ml-2">
                                Wypożyczono: {new Date(selectedRental.rentedAt).toLocaleString('pl-PL')}
                            </Text>
                        </View>

                        <View className="flex-row items-center">
                            <Feather name="clock" size={20} color="#ff5555" />
                            <Text className="text-red-400 text-lg font-semibold ml-2">
                                Termin zwrotu: {new Date(selectedRental.returnDeadline).toLocaleString('pl-PL')}
                            </Text>
                        </View>
                    </View>
                )}
            </CustomModal>

            <CustomModal
                isVisible={rentalCheckViewVisible}
                title="Podgląd zwrotu"
                loading={loading}
                onClose={() => setRentalCheckViewVisible(false)}
            >
                <View>
                    <View className="flex-1 bg-theme_background">
                        {Object.entries(
                            Array.from(markedRentals).reduce<{ [key: string]: Rental[] }>((acc, rentalId) => {
                                const rental = rentals.find((r) => r.rentalId === rentalId);
                                if (rental) {
                                    const { libraryName } = rental;
                                    if (!acc[libraryName]) {
                                        acc[libraryName] = [];
                                    }
                                    acc[libraryName].push(rental);
                                }
                                return acc;
                            }, {})
                        ).map(([libraryName, libraryGroup]) => (
                            <View key={libraryName} className="p-4 rounded-lg border border-white">
                                <View className="flex-row justify-between items-center mb-4">
                                    <Text className="text-white text-lg font-semibold max-w-[70%] text-ellipsis overflow-hidden">
                                        {libraryName}
                                    </Text>
                                </View>

                                <View>
                                    {libraryGroup.map((rental: Rental) => {
                                        const currentQuantity = quantities[rental.rentalId] || rental.quantity;
                                        return (
                                            <View key={rental.rentalId} className="flex-row items-center border-t border-white">
                                                <View className="flex-1 p-2 self-stretch">
                                                    <Text className="text-white">{rental.book.title}</Text>
                                                </View>

                                                <View className="flex-1 p-2 border-l border-white self-stretch">
                                                    <Text className="text-white">{rental.book.authorNames.join(", ")}</Text>
                                                </View>

                                                <View className="w-20 p-2 border-l border-white flex items-center justify-center self-stretch">
                                                    <Text className="text-white text-center mb-1 text-lg">x{currentQuantity}</Text>
                                                    <View className="flex-row justify-evenly items-center w-full">
                                                        <TouchableOpacity
                                                            onPress={() => handleQuantityChange(rental.rentalId, -1)}
                                                            disabled={currentQuantity <= 1}
                                                            className={`p-1 rounded-full border-2 border-white ${currentQuantity <= 1 ? 'bg-transparent border-gray-500' : 'bg-transparent border-white'}`}
                                                        >
                                                            <Feather name="minus" size={18} color={currentQuantity <= 1 ? 'gray' : 'white'} />
                                                        </TouchableOpacity>

                                                        <TouchableOpacity
                                                            onPress={() => handleQuantityChange(rental.rentalId, 1)}
                                                            disabled={currentQuantity >= rental.quantity}
                                                            className={`p-1 rounded-full border-2 border-white ${currentQuantity >= rental.quantity ? 'bg-transparent border-gray-500' : 'bg-transparent border-white'}`}
                                                        >
                                                            <Feather name="plus" size={18} color={currentQuantity >= rental.quantity ? 'gray' : 'white'} />
                                                        </TouchableOpacity>
                                                    </View>
                                                </View>
                                            </View>
                                        );
                                    })}
                                </View>
                            </View>
                        ))}
                    </View>

                    <Text className="text-gray-300 text-lg mt-3 text-center">
                        Wybierz opcje zwrotu:
                    </Text>

                    <View className="flex flex-row w-full space-x-4 mt-2">
                        <TouchableOpacity
                            onPress={() => {
                                fetchRentalReturn(true, true);
                                setRentalCheckViewVisible(false);
                            }}
                            className="bg-green-500 py-4 mr-1 rounded-lg flex-1 justify-center items-center"
                        >
                            <View className="flex flex-row items-center">
                                <Feather
                                    name="user"
                                    size={24}
                                    color="white"
                                    style={{ marginRight: 8 }}
                                />
                                <Text className="text-white text-lg font-semibold">Osobisty</Text>
                            </View>
                        </TouchableOpacity>

                        <TouchableOpacity
                            onPress={() => {
                                setAddressInputMode(true);
                                setRentalCheckViewVisible(false);
                            }}
                            className="bg-orange-500 py-4 ml-2 rounded-lg flex-1 justify-center items-center"
                        >
                            <View className="flex flex-row items-center">
                                <Feather
                                    name="truck"
                                    size={24}
                                    color="white"
                                    style={{ marginRight: 8 }}
                                />
                                <Text className="text-white text-lg font-semibold">Z odbiorem</Text>
                            </View>
                        </TouchableOpacity>
                    </View>
                </View>
            </CustomModal>

            <CustomModal
                isVisible={rentalReturnPriceVisible}
                title="Cena zwrotu"
                loading={loading}
                onClose={() => {
                    setRentalReturnPriceVisible(false);
                    setRentalCheckViewVisible(true);
                }}
                hideOkButton={true}
            >
                <View className="flex-1 bg-theme_background">

                    {rentalReturnCost?.deliveryCost! > 0 && (
                        <View className="flex-row items-center mb-4">
                            <Text className="text-white text-lg">{street + ", " + city}</Text>

                            <TouchableOpacity
                                onPress={() => {
                                    setAddressInputMode(true);
                                    setRentalReturnPriceVisible(false);
                                }}
                                className="ml-4 p-2 bg-theme_accent rounded-full"
                            >
                                <Feather name="edit-2" size={18} color="white" />
                            </TouchableOpacity>
                        </View>
                    )}

                    {Object.entries(
                        rentalReturnCost?.lateFees?.reduce<{ [key: string]: Rental[] }>((acc, lateFee) => {
                            const rental = lateFee.rental;
                            const { libraryName } = rental;

                            if (!acc[libraryName]) {
                                acc[libraryName] = [];
                            }
                            acc[libraryName].push(rental);
                            return acc;
                        }, {}) || {}
                    ).map(([libraryName, libraryGroup]) => (
                        <View key={libraryName} className="p-4 rounded-lg border border-white mb-4">
                            <View className="flex-row justify-between items-center mb-4">
                                <Text className="text-white text-lg font-semibold max-w-[70%] text-ellipsis overflow-hidden">
                                    {libraryName}
                                </Text>
                            </View>

                            <View className="border-t border-white">
                                {libraryGroup.map((rental, index) => {
                                    const currentQuantity = `x${quantities[rental.rentalId] || rental.quantity}`;
                                    const lateFeeEntry = rentalReturnCost?.lateFees.find(
                                        (fee) => fee.rental.rentalId === rental.rentalId
                                    );
                                    return (
                                        <View key={rental.rentalId} className="flex-row border-t border-white">
                                            <View className="flex-1 border-r border-white p-2">
                                                <Text className="text-white">{rental.book.title}</Text>
                                            </View>

                                            <View className="flex-1 border-r border-white p-2">
                                                <Text className="text-white">{rental.book.authorNames.join(", ")}</Text>
                                            </View>

                                            <View className="w-12 border-r border-white p-2 items-center justify-center">
                                                <Text className="text-white">{currentQuantity}</Text>
                                            </View>

                                            <View className="w-16 p-2 items-center justify-center">
                                                <Text className="text-red-500">
                                                    {lateFeeEntry ? `${lateFeeEntry.lateFee.toFixed(2)} PLN` : "-"}
                                                </Text>
                                            </View>
                                        </View>
                                    );
                                })}
                            </View>
                        </View>
                    ))}

                    {rentalReturnCost && (
                        <View className="p-4 rounded-lg border border-white mb-4">
                            <View className="flex-row">
                                <View className="flex-1 p-1">
                                    <Text className="text-white font-semibold">Koszt dostawy:</Text>
                                </View>
                                <View className="w-24 p-1 items-center justify-center">
                                    <Text className="text-white">{rentalReturnCost.deliveryCost.toFixed(2)} PLN</Text>
                                </View>
                            </View>

                            <View className="flex-row">
                                <View className="flex-1 p-1">
                                    <Text className="text-white font-semibold">Opłaty za opóźnienia:</Text>
                                </View>
                                <View className="w-24 p-1 items-center justify-center">
                                    <Text className="text-red-500">{rentalReturnCost.totalLateFees.toFixed(2)} PLN</Text>
                                </View>
                            </View>

                            <View className="flex-row">
                                <View className="flex-1 p-1">
                                    <Text className="text-white font-semibold">Cena całkowita:</Text>
                                </View>
                                <View className="w-24 p-1 items-center justify-center">
                                    <Text className="text-white">{rentalReturnCost.totalPrice.toFixed(2)} PLN</Text>
                                </View>
                            </View>
                        </View>
                    )}

                    <View className="flex flex-row w-full space-x-4 mt-4">
                        <TouchableOpacity
                            onPress={() => {
                                setRentalReturnPriceVisible(false);
                            }}
                            className="bg-red-500 py-4 mr-1 rounded-lg flex-1 justify-center items-center"
                        >
                            <View className="flex flex-row items-center">
                                <Feather
                                    name="x-circle"
                                    size={24}
                                    color="white"
                                    style={{ marginRight: 8 }}
                                />
                                <Text className="text-white text-lg font-semibold">Anuluj</Text>
                            </View>
                        </TouchableOpacity>

                        <TouchableOpacity
                            onPress={async () => {
                                const deliveryCost = rentalReturnCost!.deliveryCost;
                                if (deliveryCost > 0) {
                                    await fetchRentalReturn(false, false);
                                } else {
                                    await fetchRentalReturn(true, false);
                                }
                            }}
                            className="bg-green-500 py-4 rounded-lg flex-1 justify-center items-center"
                        >
                            <View className="flex flex-row items-center">
                                <Feather
                                    name="check-circle"
                                    size={24}
                                    color="white"
                                    style={{ marginRight: 8 }}
                                />
                                <Text className="text-white text-lg font-semibold">Potwierdź</Text>
                            </View>
                        </TouchableOpacity>
                    </View>
                </View>
            </CustomModal>

            <CustomModal
                isVisible={addressInputMode}
                title={"Wprowadź adres odbioru"}
                onClose={() => {
                    setAddressInputMode(false);
                }}
            >
                <View>
                    <TextInput
                        value={street}
                        onChangeText={setStreet}
                        placeholder="Ulica"
                        placeholderTextColor="#C8C8C8"
                        className="bg-black/10 text-lg text-white border border-gray-300 rounded-lg p-2 mb-3"
                    />

                    <TextInput
                        value={city}
                        onChangeText={setCity}
                        placeholder="Miasto"
                        placeholderTextColor="#C8C8C8"
                        className="bg-black/10 text-lg text-white border border-gray-300 rounded-lg p-2 mb-3"
                    />

                    <TextInput
                        value={postalCode}
                        onChangeText={setPostalCode}
                        placeholder="Kod pocztowy"
                        placeholderTextColor="#C8C8C8"
                        className="bg-black/10 text-lg text-white border border-gray-300 rounded-lg p-2 mb-2"
                    />

                    <TouchableOpacity
                        onPress={handleUpdateAddress}
                        className="bg-green-500 px-6 py-3 rounded-xl flex-1 mt-2"
                    >
                        <Text className="text-white text-center font-semibold text-xl">
                            Potwierdź adres
                        </Text>
                    </TouchableOpacity>
                </View>
            </CustomModal>

            <Animated.View
                style={{
                    position: "absolute",
                    bottom: 0,
                    left: 0,
                    right: 0,
                    height: bottomBoxHeight,
                    justifyContent: "center",
                    alignItems: "center",
                    padding: 0,
                    zIndex: 999,
                }}
                className="bg-gray-700"
            >
                {markedRentals.size > 0 && (
                    <View className="items-center px-4">
                        <Text className="text-white text-xl font-semibold mb-5">
                            Zaznaczone wypożyczenia: {markedRentals.size}
                        </Text>

                        <View className="flex flex-row w-full space-x-4">
                            <TouchableOpacity
                                onPress={() => {
                                    setRentalCheckViewVisible(true);
                                }}
                                className="bg-green-500 px-8 py-4 rounded-lg w-full flex justify-center items-center"
                            >
                                <View className="flex flex-row items-center">
                                    <Feather
                                        name="rotate-ccw"
                                        size={24}
                                        color="white"
                                        style={{ marginRight: 8 }}
                                    />
                                    <Text className="text-white text-xl font-semibold">Dokonaj zwrotu</Text>
                                </View>
                            </TouchableOpacity>
                        </View>
                    </View>
                )}
            </Animated.View>

            <CustomModal
                isVisible={!!apiResponse}
                title={apiResponse?.status}
                message={apiResponse?.message}
                onClose={() => setApiResponse(null)}
                loading={loading}
            />
        </View>
    );
}

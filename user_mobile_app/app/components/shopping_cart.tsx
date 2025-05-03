import {View, Text, TouchableOpacity, Alert, ScrollView, TextInput} from "react-native";
import { Feather, Ionicons } from "@expo/vector-icons";
import React, { useEffect, useState, forwardRef, useImperativeHandle } from "react";
import AsyncStorage from "@react-native-async-storage/async-storage";
import CONFIG from "@/config";
import CustomModal from "@/app/components/custom_modal";

type Book = {
    subItemId: number;
    id: string;
    title: string;
    categoryName: string;
    authorNames: string[];
    releaseYear: number;
    publisherName: string;
    isbn: string;
    languageName: string;
    image: string;
    quantity: number;
};

type CartItem = {
    libraryName: string;
    totalItemDeliveryCost: number;
    books: Book[];
};

type ShoppingCart = {
    totalCost: number;
    address: string;
    items: CartItem[];
};

interface ApiResponse {
    status: string;
    data?: any;
    message?: string;
}

const fetchShoppingCart = async (setShoppingCart: (cart: ShoppingCart) => void, setLoading: (loading: boolean) => void) => {
    setLoading(true);
    try {
        const token = await AsyncStorage.getItem("jwtToken");
        const response = await fetch(`${CONFIG.API_BASE_URL}/api/shopping-cart`, {
            method: "GET",
            headers: { Authorization: `Bearer ${token}`, "Content-Type": "application/json" },
        });

        if (response.ok) {
            const data = await response.json();
            setShoppingCart({
                totalCost: data.totalCartDeliveryCost,
                address: data.deliveryAddress,
                items: data.items.map((item: any) => ({
                    libraryName: item.libraryName,
                    totalItemDeliveryCost: item.totalItemDeliveryCost,
                    books: item.books.map((book: any) => ({
                        subItemId: book.subItemId,
                        id: book.book.id,
                        title: book.book.title,
                        categoryName: book.book.categoryName,
                        authorNames: book.book.authorNames ?? [],
                        releaseYear: book.book.releaseYear,
                        publisherName: book.book.publisherName,
                        isbn: book.book.isbn,
                        languageName: book.book.languageName,
                        image: book.book.image,
                        quantity: book.quantity,
                    })),
                })),
            });
        } else {
            Alert.alert("Error", "Failed to fetch shopping cart");
        }
    } catch (error) {
        Alert.alert("Error", "There was an error fetching the shopping cart");
    } finally {
        setLoading(false);
    }
};

const ShoppingCartComponent = forwardRef(
    ({ setShoppingCartVisible, setLoading }: { setShoppingCartVisible: (visible: boolean) => void; setLoading: (loading: boolean) => void; }, ref) => {
        const [shoppingCart, setShoppingCart] = useState<ShoppingCart>({totalCost: 0, address: "", items: []});
        const [confirmDeleteIndex, setConfirmDeleteIndex] = useState<{ itemIndex: number, bookIndex: number } | null>(null);
        const [addressInputMode, setAddressInputMode] = useState<boolean>(false);

        const [street, setStreet] = useState("");
        const [city, setCity] = useState("");
        const [postalCode, setPostalCode] = useState("");

        useImperativeHandle(ref, () => ({
            refreshCart: () => fetchShoppingCart(setShoppingCart, setLoading),
        }));

        useEffect(() => {
            fetchShoppingCart(setShoppingCart, setLoading);
        }, []);

        const removeItemFromCart = async (subItemId: number) => {
            setLoading(true);

            try {
                const token = await AsyncStorage.getItem("jwtToken");
                const response = await fetch(`${CONFIG.API_BASE_URL}/api/shopping-cart/delete-sub-item/${subItemId}`, {
                    method: "DELETE",
                    headers: {
                        Authorization: `Bearer ${token}`,
                        "Content-Type": "application/json",
                    },
                });

                if (response.ok) {
                    fetchShoppingCart(setShoppingCart, setLoading);
                    setConfirmDeleteIndex(null);
                } else {
                    const data = await response.json();
                    Alert.alert("Error", data?.message || "Failed to remove item from shopping cart");
                }
            } catch (error) {
                Alert.alert("Error", "There was an error while removing item from shopping cart");
            } finally {
                setLoading(false);
            }
        };

        const handleTrashCanClick = (itemIndex: number, bookIndex: number, subItemId: number) => {
            if (confirmDeleteIndex?.itemIndex === itemIndex && confirmDeleteIndex?.bookIndex === bookIndex) {
                removeItemFromCart(subItemId);
            } else {
                setConfirmDeleteIndex({ itemIndex, bookIndex });
            }
        };

        const checkout = async () => {
            setLoading(true);

            try {
                const token = await AsyncStorage.getItem("jwtToken");
                const response = await fetch(`${CONFIG.API_BASE_URL}/api/checkout`, {
                    method: "POST",
                    headers: {
                        Authorization: `Bearer ${token}`,
                        "Content-Type": "application/json",
                    },
                });

                if (response.ok) {
                    setApiResponse({
                        status: "Success",
                        message: "Your order(s) were successfully created.",
                    });
                } else {
                    const data = await response.json();
                    setApiResponse({
                        status: "Error",
                        message: data?.message || "Failed to remove item from shopping cart",
                    });
                }
            } catch (error) {
                setApiResponse({
                    status: "Error",
                    message: "There was an error while removing item from shopping cart",
                });
            } finally {
                setLoading(false);
                setModalVisible(true);
            }
        };

        const updateDeliveryAddress = async (street: string, city: string, postalCode: string) => {
            setLoading(true);

            try {
                const token = await AsyncStorage.getItem("jwtToken");
                const response = await fetch(`${CONFIG.API_BASE_URL}/api/shopping-cart/address`, {
                    method: "POST",
                    headers: {
                        Authorization: `Bearer ${token}`,
                        "Content-Type": "application/json",
                    },
                    body: JSON.stringify({
                        street,
                        city,
                        postalCode,
                    }),
                });

                if (response.ok) {
                    setApiResponse({
                        status: "Success",
                        message: "Successfully updated delivery address",
                    });
                    setAddressInputMode(false);
                    setStreet("");
                    setCity("");
                    setPostalCode("");
                    fetchShoppingCart(setShoppingCart, setLoading);
                } else {
                    const errorData = await response.json();
                    Alert.alert("Error", errorData.message || "Failed to update delivery address");
                }
            } catch (error) {
                Alert.alert("Error", "There was an issue updating the delivery address");
            } finally {
                setLoading(false);
                setModalVisible(true);
            }
        };

        const handleUpdateAddress = () => {
            if (!street || !city || !postalCode) {
                setApiResponse({
                    status: "Error",
                    message: "Please fill in all fields",
                });
                return;
            }
            updateDeliveryAddress(street, city, postalCode);
        };

        const [apiResponse, setApiResponse] = useState<ApiResponse | null>(null);
        const [modalVisible, setModalVisible] = useState<boolean>(false);
        const [checkoutConfirmVisible, setCheckoutConfirmVisible] = useState<boolean>(false);

        return (
            <View className="absolute inset-0 bg-theme_background p-6 z-50 pt-9">
                <CustomModal
                    isVisible={modalVisible}
                    title={apiResponse?.status === "Success" ? "Success!" : "Error"
                    }
                    message={apiResponse?.message}
                    onClose={() => {
                        setModalVisible(false);
                        if (apiResponse?.status === "Success") {
                            fetchShoppingCart(setShoppingCart, setLoading);
                        }
                    }}
                />

                <CustomModal
                    isVisible={checkoutConfirmVisible}
                    title="Potwierdzenie zamówienia"
                    onClose={() => {
                        setCheckoutConfirmVisible(false);
                    }}
                    hideOkButton={true}
                >
                    <View>
                        <Text className="text-gray-300 text-lg text-center">
                            Czy na pewno chcesz złożyć zamówienie?
                        </Text>

                        <View className="flex flex-row w-full space-x-4 mt-6">
                            <TouchableOpacity
                                onPress={() => {
                                    setCheckoutConfirmVisible(false);
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
                                onPress={checkout}
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
                    title={"Zaktualizuj adres dostawy"}
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
                            keyboardType="numeric"
                            placeholderTextColor="#C8C8C8"
                            className="bg-black/10 text-lg text-white border border-gray-300 rounded-lg p-2 mb-2"
                        />

                        <TouchableOpacity
                            onPress={handleUpdateAddress}
                            className="bg-green-500 px-6 py-3 rounded-xl flex-1 mt-2"
                        >
                            <Text className="text-white text-center font-semibold text-xl">
                                Zaktualizuj adres
                            </Text>
                        </TouchableOpacity>
                    </View>
                </CustomModal>

                <View className="flex-row items-center justify-between mb-3">
                    <View className="flex-row items-center">
                        <Feather name="shopping-cart" size={24} color="#f7ca65" className="mr-2 mt-1" />
                        <Text className="text-white text-2xl font-bold">Koszyk</Text>
                    </View>
                    <TouchableOpacity onPress={() => setShoppingCartVisible(false)} className="p-2 bg-theme_accent rounded-full">
                        <Ionicons name="close" size={28} color="white" />
                    </TouchableOpacity>
                </View>

                <View className="flex-row items-center mb-4">
                    <Text className="text-white text-lg">{shoppingCart.address || "Brak ustawionego adresu"}</Text>

                    <TouchableOpacity
                        onPress={() => {setAddressInputMode(true)}}
                        className="ml-4 p-2 bg-theme_accent rounded-full"
                    >
                        <Feather name="edit-2" size={18} color="white" />
                    </TouchableOpacity>
                </View>

                <ScrollView className="mt-2">
                    {shoppingCart.items.length > 0 ? (
                        shoppingCart.items.map((item, itemIndex) => (
                            <View key={itemIndex} className="mb-2 p-4 rounded-lg border border-white bg-theme_card">
                                <View className="flex-row justify-between items-center mb-4">
                                    <Text className="text-white text-lg font-semibold max-w-[70%] text-ellipsis overflow-hidden">
                                        {item.libraryName}
                                    </Text>
                                    <Text className="text-white text-lg font-medium">
                                        {item.totalItemDeliveryCost.toFixed(2)} zł
                                    </Text>
                                </View>

                                <View className="mb-4">
                                    {item.books.map((book, bookIndex) => (
                                        <View key={bookIndex} className="flex-row items-center border-t border-white">
                                            <View className="flex-1 p-2 self-stretch">
                                                <Text className="text-white">{book.title}</Text>
                                            </View>

                                            <View className="flex-1 p-2 border-l border-white self-stretch">
                                                <Text className="text-white">{book.authorNames.join(", ")}</Text>
                                            </View>

                                            <View className="w-14 p-2 border-l border-white flex items-center justify-center self-stretch">
                                                <Text className="text-white text-center">x{book.quantity}</Text>
                                            </View>

                                            <View className="w-14 p-2 border-l border-white flex items-center justify-center self-stretch">
                                                <TouchableOpacity
                                                    onPress={() =>
                                                        handleTrashCanClick(itemIndex, bookIndex, book.subItemId)
                                                    }
                                                >
                                                    <Feather
                                                        name="trash-2"
                                                        size={24}
                                                        color={
                                                            confirmDeleteIndex?.itemIndex === itemIndex && confirmDeleteIndex?.bookIndex === bookIndex
                                                                ? "red"
                                                                : "#f7ca65"
                                                        }
                                                    />
                                                </TouchableOpacity>
                                            </View>
                                        </View>
                                    ))}
                                </View>
                            </View>
                        ))
                    ) : (
                        <Text className="text-white text-xl">Twój koszyk jest pusty.</Text>
                    )}

                    {shoppingCart.items.length > 0 && (
                        <View className="mt-4 bg-theme_card p-4 rounded-lg border border-white">
                            <View className="flex-row justify-between items-center">
                                <Text className="text-white text-lg">Całkowity koszt:</Text>
                                <Text className="text-white text-xl font-bold">{shoppingCart.totalCost.toFixed(2)} zł</Text>
                            </View>
                        </View>
                    )}
                </ScrollView>

                {shoppingCart.items.length > 0 && (
                    <View className="w-full p-2 bg-theme_accent rounded-lg mt-6">
                        <TouchableOpacity
                            onPress={() => setCheckoutConfirmVisible(true)}
                            className="flex-row items-center justify-center p-3 bg-theme_primary rounded-lg"
                        >
                            <Feather name="shopping-bag" size={20} color="white" className="mr-2" />
                            <Text className="text-white text-2xl font-bold">Finalizacja zamówienia</Text>
                        </TouchableOpacity>
                    </View>
                )}
            </View>
        );
    }
);

export default ShoppingCartComponent;

import {FlatList, Text, TouchableOpacity, View, Image, Dimensions, Alert, Button, TextInput} from "react-native";
import {StatusBar} from "expo-status-bar";
import React, {useEffect, useRef, useState} from "react";
import {router} from "expo-router";
import Icon from "react-native-vector-icons/Ionicons";
import AsyncStorage from "@react-native-async-storage/async-storage";
import CONFIG from "@/config";
import CustomModal from "@/app/components/custom_modal";
import {Feather, Ionicons} from "@expo/vector-icons";
import Animated, {useSharedValue, useAnimatedStyle, withTiming, SharedValue} from "react-native-reanimated";
import { Picker } from '@react-native-picker/picker';
import ShoppingCartComponent from "@/app/components/shopping_cart";

type Book = {
    id: number;
    title: string;
    categoryName: string;
    authorNames: string[];
    releaseYear: number;
    publisherName: string;
    isbn: string;
    languageName: string;
    image: string;
};

interface Option {
    quoteOptionId: number;
    libraryName: string;
    distanceKm: number;
    totalDeliveryCost: number;
}

interface ApiQuoteResponse {
    validUntil: string;
    book: Book;
    quantity: number;
    options: Option[];
}

const screenWidth = Dimensions.get('window').width;
const bookWidth = (screenWidth / 2) - 24;

interface BookCardProps {
    book: Book;
    setSelectedBook: (book: Book) => void;
}

const BookCard = ({ book, setSelectedBook }: BookCardProps) => {
    return (
        <TouchableOpacity
            className="bg-black/10 p-6 rounded-lg border border-gray-300 mb-4"
            style={{ width: bookWidth }}
            onPress={() => setSelectedBook(book)}>
            <Image
                source={{ uri: book.image }}
                style={{ width: "100%", height: 200, borderRadius: 8 }}
                resizeMode="contain"
            />
            <Text
                className="text-lg font-bold text-white mt-3"
                style={{ lineHeight: 18 }}
            >
                {book.title}
            </Text>
            <Text className="text-base text-gray-300">{book.authorNames.join(", ")}</Text>
            <Text className="text-base text-gray-400">{book.categoryName} | {book.releaseYear}</Text>
        </TouchableOpacity>
    );
};

export default function BookSearch() {
    const [loading, setLoading] = useState<boolean>(true);
    const [books, setBooks] = useState<Book[]>([]);
    const [modalVisible, setModalVisible] = useState<boolean>(false);
    const [currentPage, setCurrentPage] = useState(0);
    const [totalPages, setTotalPages] = useState(1);
    const [filterVisible, setFilterVisible] = useState(false);
    const [shoppingCartVisible, setShoppingCartVisible] = useState(false);

    const [categories, setCategories] = useState<string[]>([]);
    const [selectedCategory, setSelectedCategory] = useState<string | null>(null);

    const [languages, setLanguages] = useState<string[]>([]);
    const [selectedLanguage, setSelectedLanguage] = useState<string | null>(null);

    const [selectedLibrary, setSelectedLibrary] = useState<string | null>(null);
    const [libraries, setLibraries] = useState<string[]>([]);
    const [libraryName, setLibraryName] = useState("");

    const [bookTitle, setBookTitle] = useState('');
    const [bookTitles, setBookTitles] = useState<string[]>([]);
    const [selectedTitle, setSelectedTitle] = useState<string | null>(null);

    const [selectedPublisher, setSelectedPublisher] = useState<string | null>(null);
    const [publishers, setPublishers] = useState<string[]>([]);
    const [publisher, setPublisher] = useState("");

    const [selectedBook, setSelectedBook] = useState<Book | null>(null);
    const [bookQuotes, setBookQuotes] = useState<Option[]>([]);
    const [quantity, setQuantity] = useState(1);

    const [customMessage, setCustomMessage] = useState<{ title: string, message: string } | null>(null);

    const shoppingCartRef = useRef<{ refreshCart: () => void } | null>(null);

    useEffect(() => {
        if (selectedBook !== null) {
            setModalVisible(true);
        } else {
            setModalVisible(false);
        }
    }, [selectedBook]);

    const fetchBooks = async (page: number) => {
        setLoading(true);

        try {
            const token = await AsyncStorage.getItem("jwtToken");

            let url = `${CONFIG.API_BASE_URL}/api/books/search?page=${page}&size=10`;

            if (selectedCategory) {
                url += `&category=${encodeURIComponent(selectedCategory)}`;
            }

            if (selectedLanguage) {
                url += `&language=${encodeURIComponent(selectedLanguage)}`;
            }

            if (selectedLibrary) {
                url += `&library=${encodeURIComponent(selectedLibrary)}`;
            }

            if (selectedTitle) {
                url += `&title=${encodeURIComponent(selectedTitle)}`;
            }

            const response = await fetch(url, {
                method: "GET",
                headers: {
                    Authorization: `Bearer ${token}`,
                    "Content-Type": "application/json",
                },
            });

            if (response.ok) {
                const data = await response.json();
                setBooks(data.content);
                setCurrentPage(data.currentPage);
                setTotalPages(data.totalPages);
            } else {
                Alert.alert("Error", "Failed to fetch books");
            }
        } catch (error) {
            Alert.alert("Error", "There was an error fetching books");
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchCategories();
        fetchLanguages();
        fetchBooks(0);
    }, []);

    const fetchCategories = async () => {
        try {
            const token = await AsyncStorage.getItem("jwtToken");
            const response = await fetch(`${CONFIG.API_BASE_URL}/api/categories`, {
                method: "GET",
                headers: {
                    Authorization: `Bearer ${token}`,
                    "Content-Type": "application/json",
                },
            });
            if (response.ok) {
                const data = await response.json();
                setCategories(data.map((item: { name: string }) => item.name));
            } else {
                Alert.alert('Error', 'Failed to fetch categories');
            }
        } catch (error) {
            Alert.alert('Error', 'There was an error fetching categories');
        }
    };

    const fetchLanguages = async () => {
        try {
            const token = await AsyncStorage.getItem("jwtToken");
            const response = await fetch(`${CONFIG.API_BASE_URL}/api/languages`, {
                method: "GET",
                headers: {
                    Authorization: `Bearer ${token}`,
                    "Content-Type": "application/json",
                },
            });
            if (response.ok) {
                const data = await response.json();
                setLanguages(data.map((item: { name: string }) => item.name));
            } else {
                Alert.alert('Error', 'Failed to fetch languages');
            }
        } catch (error) {
            Alert.alert('Error', 'There was an error fetching languages');
        }
    };

    const translateY = useSharedValue(100);
    const opacity = useSharedValue(0);
    const listTranslateY = useSharedValue(30);
    const listOpacity = useSharedValue(0);

    const animateValue = (value: SharedValue<number>, toValue: number) => {
        value.value = withTiming(toValue, { duration: 300 });
    };

    useEffect(() => {
        animateValue(translateY, filterVisible ? 0 : 100);
        animateValue(opacity, filterVisible ? 1 : 0);
    }, [filterVisible]);

    useEffect(() => {
        if (!loading) {
            animateValue(listTranslateY, 0);
            animateValue(listOpacity, 1);
        }
    }, [loading]);

    const animatedFilterStyle = useAnimatedStyle(() => ({
        transform: [{ translateY: translateY.value }],
        opacity: opacity.value,
    }));

    const animatedListStyle = useAnimatedStyle(() => ({
        transform: [{ translateY: listTranslateY.value }],
        opacity: listOpacity.value,
    }));

    const fetchLibraries = async (name: string) => {
        if (name.length < 3) return;

        try {
            const token = await AsyncStorage.getItem("jwtToken");
            const response = await fetch(`${CONFIG.API_BASE_URL}/api/libraries/search?name=${name}`, {
                method: "GET",
                headers: {
                    Authorization: `Bearer ${token}`,
                    "Content-Type": "application/json",
                },
            });
            if (response.ok) {
                const data = await response.json();
                setLibraries(data.map((item: { name: string }) => item.name));
            } else {
                Alert.alert("Error", "Failed to fetch libraries");
            }
        } catch (error) {
            Alert.alert("Error", "There was an error fetching libraries");
        }
    };

    let debounceTimer: NodeJS.Timeout;

    const handleLibraryNameChange = (text: string) => {
        setLibraryName(text);

        clearTimeout(debounceTimer);

        debounceTimer = setTimeout(() => {
            fetchLibraries(text);
        }, 300);
    };

    const fetchBookTitles = async (title: string) => {
        if (title.length < 3) return;

        try {
            const token = await AsyncStorage.getItem("jwtToken");
            const response = await fetch(`${CONFIG.API_BASE_URL}/api/books/search-book-titles?title=${title}`, {
                method: "GET",
                headers: {
                    Authorization: `Bearer ${token}`,
                    "Content-Type": "application/json",
                },
            });

            if (response.ok) {
                const data = await response.json();
                setBookTitles(data.map((item: { name: string }) => item.name));
            } else {
                Alert.alert("Error", "Failed to fetch book titles");
            }
        } catch (error) {
            Alert.alert("Error", "There was an error fetching book titles");
        }
    };

    let bookTitleDebounceTimer: NodeJS.Timeout;

    const handleBookTitleChange = (text: string) => {
        setBookTitle(text);

        clearTimeout(bookTitleDebounceTimer);

        bookTitleDebounceTimer = setTimeout(() => {
            fetchBookTitles(text);
        }, 300);
    };

    const fetchPublishers = async (name: string) => {
        if (name.length < 3) return;

        try {
            const token = await AsyncStorage.getItem("jwtToken");
            const response = await fetch(`${CONFIG.API_BASE_URL}/api/publishers/search?name=${name}`, {
                method: "GET",
                headers: {
                    Authorization: `Bearer ${token}`,
                    "Content-Type": "application/json",
                },
            });
            if (response.ok) {
                const data = await response.json();
                setPublishers(data.map((item: { name: string }) => item.name));
            } else {
                Alert.alert("Error", "Failed to fetch publishers");
            }
        } catch (error) {
            Alert.alert("Error", "There was an error fetching publishers");
        }
    };

    let publisherDebounceTimer: NodeJS.Timeout;

    const handlePublisherChange = (text: string) => {
        setPublisher(text);

        clearTimeout(publisherDebounceTimer);

        publisherDebounceTimer = setTimeout(() => {
            fetchPublishers(text);
        }, 300);
    };

    const fetchQuotes = async (quantity: number) => {
        setLoading(true);

        try {
            const token = await AsyncStorage.getItem("jwtToken");
            const response = await fetch(`${CONFIG.API_BASE_URL}/api/quotes?bookId=${selectedBook?.id}&quantity=${quantity}`, {
                method: "POST",
                headers: {
                    Authorization: `Bearer ${token}`,
                    "Content-Type": "application/json",
                },
            });

            if (response.ok) {
                const data: ApiQuoteResponse = await response.json();

                const options = data.options.map((item) => ({
                    quoteOptionId: item.quoteOptionId,
                    libraryName: item.libraryName,
                    distanceKm: item.distanceKm,
                    totalDeliveryCost: item.totalDeliveryCost,
                }));

                setBookQuotes(options);
            } else {
                const data = await response.json();
                let errorMessage = data?.message || "Failed to fetch book quotes";

                if (data?.message === "No delivery address set for the user") {
                    setCustomMessage({
                        title: "Error",
                        message: "You need to set your delivery address first before you can fetch libraries. Please tap on the shopping cart icon and add one.",
                    });
                } else {
                    Alert.alert("Error", errorMessage);

                }
            }
        } catch (error) {
            Alert.alert("Error", "There was an error fetching book quotes");
        } finally {
            setLoading(false);
        }
    };

    const increaseQuantity = () => {
        setQuantity(prevQuantity => {
            const newQuantity = prevQuantity + 1;
            fetchQuotes(newQuantity);
            return newQuantity;
        });
    };

    const decreaseQuantity = () => {
        if (quantity > 1) {
            setQuantity(prevQuantity => {
                const newQuantity = prevQuantity - 1;
                fetchQuotes(newQuantity);
                return newQuantity;
            });
        }
    };

    const addItemToCart = async (quoteOptionId: number) => {
        setLoading(true);

        try {
            const token = await AsyncStorage.getItem("jwtToken");
            const response = await fetch(`${CONFIG.API_BASE_URL}/api/shopping-cart/add-quote-option/${quoteOptionId}`, {
                method: "POST",
                headers: {
                    Authorization: `Bearer ${token}`,
                    "Content-Type": "application/json",
                },
            });

            if (response.ok) {
                setCustomMessage({
                    title: "Sukces",
                    message: "Pomyślnie dodano książke do koszyka",
                });
            } else {
                const data = await response.json();
                Alert.alert("Error", data?.message || "Failed to add item to shopping cart");
            }
        } catch (error) {
            Alert.alert("Error", "There was an error while adding item to shopping cart");
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        if (shoppingCartVisible) {
            shoppingCartRef.current?.refreshCart();
        }
    }, [shoppingCartVisible]);

    return (
        <View className="flex-1 bg-theme_background pt-8">
            <StatusBar style="light" />

            <CustomModal
                isVisible={modalVisible || loading || customMessage !== null}
                title={customMessage ? customMessage.title : selectedBook ? selectedBook.title : "Loading..."}
                onClose={() => {
                    setModalVisible(false);
                    if (customMessage !== null) {
                        setCustomMessage(null);
                    } else if (bookQuotes.length > 0) {
                        setBookQuotes([]);
                        setQuantity(1);
                    } else if (selectedBook !== null) {
                        setSelectedBook(null);
                    }
                }}
                loading={loading}
                message={customMessage?.message ?? undefined}
            >
                {selectedBook && bookQuotes.length === 0 && !customMessage && (
                    <View>
                        <Image
                            source={{ uri: selectedBook.image }}
                            style={{ width: "100%", height: 300}}
                            resizeMode="contain"
                        />

                        <View className="flex-row items-center mb-1 mt-3">
                            <Feather name="user" size={20} color="#f7ca65" />
                            <Text className="text-white text-lg ml-2">
                                <Text className="font-bold">Autor{selectedBook.authorNames.length > 1 ? "zy" : ""}: </Text>
                                {selectedBook.authorNames.join(", ")}
                            </Text>
                        </View>

                        <View className="flex-row items-center mb-1">
                            <Feather name="book" size={20} color="#f7ca65" />
                            <Text className="text-white text-lg ml-2">
                                <Text className="font-bold">Kategoria:</Text> {selectedBook.categoryName}
                            </Text>
                        </View>

                        <View className="flex-row items-center mb-1">
                            <Feather name="calendar" size={20} color="#f7ca65" />
                            <Text className="text-white text-lg ml-2">
                                <Text className="font-bold">Rok wydania:</Text> {selectedBook.releaseYear}
                            </Text>
                        </View>

                        <View className="flex-row items-center mb-1">
                            <Feather name="package" size={20} color="#f7ca65" />
                            <Text className="text-white text-lg ml-2">
                                <Text className="font-bold">Wydawca:</Text> {selectedBook.publisherName}
                            </Text>
                        </View>

                        <View className="flex-row items-center mb-1">
                            <Feather name="hash" size={20} color="#f7ca65" />
                            <Text className="text-white text-lg ml-2">
                                <Text className="font-bold">ISBN:</Text> {selectedBook.isbn}
                            </Text>
                        </View>

                        <View className="flex-row items-center">
                            <Feather name="globe" size={20} color="#f7ca65" />
                            <Text className="text-white text-lg ml-2">
                                <Text className="font-bold">Język:</Text> {selectedBook.languageName}
                            </Text>
                        </View>

                        <TouchableOpacity
                            onPress={() => fetchQuotes(1)}
                            className="bg-green-500 px-6 py-3 rounded-xl flex-1 mt-3"
                        >
                            <Text className="text-white text-center font-semibold text-lg">
                                Znajdź książki do wypożyczenia
                            </Text>
                        </TouchableOpacity>
                    </View>
                )}

                {bookQuotes.length > 0 && !customMessage && (
                    <Animated.View style={[animatedListStyle]} className="flex-1">
                        <View className="flex-row items-center justify-center mb-4">
                            <TouchableOpacity
                                className={`border w-12 h-12 rounded-full flex justify-center items-center ${
                                    quantity === 1 ? "border-gray-400" : "border-white"
                                }`}
                                onPress={decreaseQuantity}
                                disabled={quantity === 1}
                            >
                                <Text
                                    className={`text-white text-3xl font-bold ${
                                        quantity === 1 ? "text-gray-400" : ""
                                    }`}
                                >
                                    -
                                </Text>
                            </TouchableOpacity>

                            <Text className="text-white text-lg mx-4">
                                <Text className="font-bold">Ilość:</Text> {quantity}
                            </Text>

                            <TouchableOpacity
                                className="border border-white w-12 h-12 rounded-full flex justify-center items-center"
                                onPress={increaseQuantity}
                            >
                                <Text className="text-white text-3xl">+</Text>
                            </TouchableOpacity>
                        </View>

                        <FlatList
                            data={bookQuotes}
                            scrollEnabled={false}
                            keyExtractor={(item) => item.quoteOptionId.toString()}
                            renderItem={({ item, index }) => (
                                <View
                                    style={{
                                        marginBottom: index === bookQuotes.length - 1 ? 1 : 10
                                    }}
                                >
                                    <TouchableOpacity
                                        onPress={() => {
                                            addItemToCart(item.quoteOptionId);
                                            setQuantity(1);
                                            setBookQuotes([]);
                                            setSelectedBook(null);
                                        }}
                                    >
                                        <View className="bg-black/10 p-3 rounded-lg border border-gray-300 flex-row justify-between items-center">
                                            <View className="flex-1">
                                                <View className="flex-row items-center mb-1">
                                                    <Feather name="home" size={20} color="#f7ca65" />
                                                    <Text className="text-lg ml-2 text-white">{item.libraryName}</Text>
                                                </View>

                                                <View className="flex-row items-center mb-1">
                                                    <Feather name="map-pin" size={20} color="#f7ca65" />
                                                    <Text className="text-lg ml-2 text-white">
                                                        <Text className="font-bold">Dystans:</Text> {item.distanceKm} km
                                                    </Text>
                                                </View>
                                            </View>
                                            <Text className="text-3xl font-bold text-green-500">
                                                {item.totalDeliveryCost.toFixed(2)} zł
                                            </Text>
                                        </View>
                                    </TouchableOpacity>
                                </View>
                            )}
                        />
                    </Animated.View>
                )}
            </CustomModal>

            {!filterVisible && !shoppingCartVisible && (
                <View className="flex-row items-center justify-between bg-theme_background relative">
                    <TouchableOpacity
                        onPress={() => setFilterVisible(true)}
                        className="absolute left-4 bg-theme_accent rounded-full flex items-center justify-center shadow-2xl z-50"
                        style={{ top: 0, width: 56, height: 56 }}
                    >
                        <Ionicons name="search" size={36} color="white" />
                    </TouchableOpacity>

                    <TouchableOpacity
                        onPress={() => setShoppingCartVisible(true)}
                        className="absolute right-6 bg-theme_accent rounded-full flex items-center justify-center shadow-2xl z-50"
                        style={{ top: 0, width: 56, height: 56, right: 15}}
                    >
                        <Ionicons name="cart" size={36} color="white" />
                    </TouchableOpacity>
                </View>
            )}

            {filterVisible && (
                <Animated.View style={[animatedFilterStyle]} className="absolute inset-0 bg-theme_background p-6 z-40 pt-9">
                    <View className="flex-row items-center justify-between mb-6">
                        <View className="flex-row items-center">
                            <Feather name="filter" size={24} color="#f7ca65" className="mr-2 mt-1" />
                            <Text className="text-white text-2xl font-bold">Filtrowanie książek</Text>
                        </View>

                        <TouchableOpacity
                            onPress={() => setFilterVisible(false)}
                            className="p-2 bg-theme_accent rounded-full"
                        >
                            <Ionicons name="close" size={28} color="white" />
                        </TouchableOpacity>
                    </View>

                    <View className="space-y-4">
                        <View className="flex-row items-center mb-1">
                            <Feather name="book" size={20} color="#f7ca65" className="mr-1" />
                            <Text className="text-white text-xl mb-1">Tytuł książki</Text>
                        </View>
                        <TextInput
                            value={bookTitle}
                            onChangeText={handleBookTitleChange}
                            placeholder="Wprowadź tytuł książki"
                            placeholderTextColor="#C8C8C8"
                            className="bg-black/10 text-lg text-white border border-gray-300 rounded-lg p-2 mb-2"
                        />

                        <View className="border border-gray-300 rounded-lg mb-4 h-12 flex justify-center">
                            <Picker
                                selectedValue={selectedTitle}
                                onValueChange={(itemValue) => setSelectedTitle(itemValue)}
                                style={{ color: 'white' }}
                                className="bg-black/10 text-sm text-white border border-gray-300"
                                dropdownIconColor="#C8C8C8"
                                enabled={bookTitles.length > 0}
                            >
                                {bookTitles.length === 0 ? (
                                    <Picker.Item label="Nie znaleziono tytułów" value="" enabled={false} />
                                ) : (
                                    [<Picker.Item key="empty" label="Brak" value="" />,
                                        ...bookTitles.map((title, index) => (
                                            <Picker.Item key={index} label={title} value={title} />
                                        ))]
                                )}
                            </Picker>
                        </View>

                        <View className="flex-row items-center mb-1">
                            <Feather name="tag" size={20} color="#f7ca65" className="mr-1" />
                            <Text className="text-white text-xl mb-1">Kategoria</Text>
                        </View>
                        <View className="border border-gray-300 rounded-lg mb-4 h-12 flex justify-center">
                            <Picker
                                selectedValue={selectedCategory}
                                onValueChange={(itemValue) => setSelectedCategory(itemValue)}
                                style={{ color: 'white' }}
                                className="bg-black/10 text-sm text-white border border-gray-300"
                                dropdownIconColor="#C8C8C8"
                            >
                                <Picker.Item label="Brak" value={null} />

                                {categories.map((category, index) => (
                                    <Picker.Item key={index} label={category} value={category} />
                                ))}
                            </Picker>
                        </View>

                        <View className="flex-row items-center mb-1">
                            <Feather name="user" size={20} color="#f7ca65" className="mr-1" />
                            <Text className="text-white text-xl mb-1">Wydawca</Text>
                        </View>
                        <TextInput
                            value={publisher}
                            onChangeText={handlePublisherChange}
                            placeholder="Wprowadź wydawcę"
                            placeholderTextColor="#C8C8C8"
                            className="bg-black/10 text-lg text-white border border-gray-300 rounded-lg p-2 mb-2"
                        />

                        <View className="border border-gray-300 rounded-lg mb-4 h-12 flex justify-center">
                            <Picker
                                selectedValue={selectedPublisher}
                                onValueChange={(itemValue) => setSelectedPublisher(itemValue)}
                                style={{ color: 'white' }}
                                className="bg-black/10 text-sm text-white border border-gray-300"
                                dropdownIconColor="#C8C8C8"
                                enabled={publishers.length > 0}
                            >
                                {publishers.length === 0 ? (
                                    <Picker.Item label="Nie znaleziono wydawców" value="" enabled={false} />
                                ) : (
                                    [<Picker.Item key="empty" label="Brak" value="" />,
                                        ...publishers.map((publisher, index) => (
                                            <Picker.Item key={index} label={publisher} value={publisher} />
                                        ))]
                                )}
                            </Picker>
                        </View>

                        <View className="flex-row items-center mb-1">
                            <Feather name="home" size={20} color="#f7ca65" className="mr-1" />
                            <Text className="text-white text-xl mb-1">Biblioteka</Text>
                        </View>
                        <TextInput
                            value={libraryName}
                            onChangeText={handleLibraryNameChange}
                            placeholder="Wprowadź nazwę biblioteki"
                            placeholderTextColor="#C8C8C8"
                            className="bg-black/10 text-lg text-white border border-gray-300 rounded-lg p-2 mb-2"
                        />

                        <View className="border border-gray-300 rounded-lg mb-4 h-12 flex justify-center">
                            <Picker
                                selectedValue={selectedLibrary}
                                onValueChange={(itemValue) => setSelectedLibrary(itemValue)}
                                style={{ color: 'white' }}
                                className="bg-black/10 text-sm text-white border border-gray-300"
                                dropdownIconColor="#C8C8C8"
                                enabled={libraries.length > 0}
                            >
                                {libraries.length === 0 ? (
                                    <Picker.Item label="Nie znaleziono bibliotek" value="" enabled={false} />
                                ) : (
                                    [<Picker.Item key="empty" label="Brak" value="" />,
                                        ...libraries.map((library, index) => (
                                            <Picker.Item key={index} label={library} value={library} />
                                        ))]
                                )}
                            </Picker>
                        </View>

                        <View className="flex-row items-center mb-1">
                            <Feather name="globe" size={20} color="#f7ca65" className="mr-1" />
                            <Text className="text-white text-xl mb-1">Język</Text>
                        </View>
                        <View className="border border-gray-300 rounded-lg mb-2 h-12 flex justify-center">
                            <Picker
                                selectedValue={selectedLanguage}
                                onValueChange={(itemValue) => setSelectedLanguage(itemValue)}
                                style={{ color: 'white' }}
                                className="bg-black/10 text-sm text-white border border-gray-300"
                                dropdownIconColor="#C8C8C8"
                            >
                                <Picker.Item label="Brak" value={null} />

                                {languages.map((language, index) => (
                                    <Picker.Item key={index} label={language} value={language} />
                                ))}
                            </Picker>
                        </View>
                    </View>

                    <TouchableOpacity
                        className="mt-2 p-4 bg-theme_accent rounded-lg flex"
                        onPress={() => {
                            setFilterVisible(false)
                            fetchBooks(0);
                        }}
                        style={{
                            flexDirection: 'row',
                            alignItems: 'center',
                            justifyContent: 'center'
                        }}
                    >
                        <Ionicons name="search" size={20} color="white" style={{ marginRight: 8 }} />
                        <Text className="text-white text-lg font-semibold">Zatwierdź filtry</Text>
                    </TouchableOpacity>

                    <TouchableOpacity
                        className="mt-4 p-4 bg-gray-500 rounded-lg flex"
                        onPress={() => {
                            setSelectedCategory(null);
                            setSelectedLanguage(null);
                            setSelectedLibrary(null);
                            setLibraries([]);
                            setLibraryName("");
                            setSelectedTitle(null);
                            setBookTitle("");
                            setBookTitles([]);
                            setSelectedPublisher(null);
                            setPublisher("");
                            setPublishers([]);
                        }}
                        style={{
                            flexDirection: 'row',
                            alignItems: 'center',
                            justifyContent: 'center'
                        }}
                    >
                        <Ionicons name="refresh" size={20} color="white" style={{ marginRight: 8 }} />
                        <Text className="text-white text-lg font-semibold">Zresetuj filtry</Text>
                    </TouchableOpacity>
                </Animated.View>
            )}

            {shoppingCartVisible && (
                <ShoppingCartComponent
                    ref={shoppingCartRef}
                    setShoppingCartVisible={setShoppingCartVisible}
                    setLoading={setLoading}
                />
            )}

            {!filterVisible && !shoppingCartVisible && !loading && (
                <Animated.View className="flex-1">
                    {books.length === 0 ? (
                        <View className="flex-1 justify-center items-center">
                            <Text className="text-white text-lg font-semibold">
                                No books were found with the specified filters.
                            </Text>
                        </View>
                    ) : (
                        <FlatList
                            data={books}
                            keyExtractor={(item) => item.id.toString()}
                            renderItem={({ item }) =>
                                <BookCard book={item}
                                          setSelectedBook={setSelectedBook}/>}
                            numColumns={2}
                            contentContainerStyle={{ paddingHorizontal: 16, paddingBottom: 80 }}
                            columnWrapperStyle={{ justifyContent: "space-between" }}
                            ListFooterComponent={
                                <View className="flex-row items-center px-6 relative mt-2">
                                    <TouchableOpacity
                                        className="w-32 px-4 py-2 bg-theme_accent rounded-lg disabled:opacity-50 absolute left-0"
                                        onPress={() => fetchBooks(currentPage - 1)}
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
                                        onPress={() => fetchBooks(currentPage + 1)}
                                        disabled={currentPage >= totalPages - 1}
                                    >
                                        <Text className="text-white text-lg font-semibold text-center">Następna</Text>
                                    </TouchableOpacity>
                                </View>
                            }
                        />
                    )}
                </Animated.View>
            )}

            <View className="absolute bottom-0 left-0 w-full h-16 bg-theme_background flex-row">
                <TouchableOpacity
                    className="w-1/5 p-3 items-center justify-center"
                    onPress={() => {
                        router.push("/rental_returns");
                    }}
                >
                    <Icon name="swap-horizontal" size={30} color="white" />
                </TouchableOpacity>

                <TouchableOpacity
                    className="w-1/5 p-3 items-center justify-center"
                    onPress={() => {
                        router.push("/rentals");
                    }}
                >
                    <Icon name="pricetag" size={30} color="white" />
                </TouchableOpacity>

                <TouchableOpacity
                    className="w-1/5 p-3 items-center justify-center bg-theme_accent"
                    onPress={() => {
                        router.push("/book_search");
                    }}
                >
                    <Icon name="book" size={30} color="white" />
                </TouchableOpacity>

                <TouchableOpacity
                    className="w-1/5 p-3 items-center justify-center"
                    onPress={() => {
                        router.push("/order_history");
                    }}
                >
                    <Icon name="document-text" size={30} color="white" />
                </TouchableOpacity>

                <TouchableOpacity
                    className="w-1/5 p-3 rounded-full items-center justify-center"
                    onPress={() => {
                        router.push("/account");
                    }}
                >
                    <Icon name="person" size={30} color="white" />
                </TouchableOpacity>
            </View>
        </View>
    );
}

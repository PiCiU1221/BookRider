import React, { useEffect, useState } from 'react';
import { TextInput, Text, ScrollView, View, TouchableOpacity, BackHandler, Image } from 'react-native';
import {AntDesign, Feather} from '@expo/vector-icons';
import { StatusBar } from 'expo-status-bar';
import { router } from "expo-router";
import * as ImagePicker from 'expo-image-picker';
import CustomModal from '@/app/components/custom_modal';
import AsyncStorage from "@react-native-async-storage/async-storage";
import CONFIG from "@/config";
import { Picker } from "@react-native-picker/picker";

enum DocumentType {
    ID = "ID",
    DRIVER_LICENSE = "DRIVER_LICENSE",
}

const documentTypes: Record<string, string> = {
    ID: "Dowód osobisty",
    DRIVER_LICENSE: "Prawo jazdy",
};

interface Document {
    documentType: string;
    expirationDate: string;
    base64Image: string;
    imageUri: string | null;
    imageSaved: boolean;
}

interface ApiResponse {
    status: string;
    message?: string;
}

export default function CreateDriverApplication() {
    const [documents, setDocuments] = useState<Document[]>([
        { documentType: 'ID', expirationDate: '', base64Image: '', imageUri: null, imageSaved: false },
    ]);
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [apiResponse, setApiResponse] = useState<ApiResponse | null>(null);
    const [loading, setLoading] = useState<boolean>(false);
    const [modalVisible, setModalVisible] = useState<boolean>(false);

    const handleAddDocument = () => {
        setDocuments([
            ...documents,
            { documentType: 'ID', expirationDate: '', base64Image: '', imageUri: null, imageSaved: false },
        ]);
    };

    const handleChange = (index: number, field: keyof Document, value: string) => {
        const updatedDocuments = [...documents];
        updatedDocuments[index] = {
            ...updatedDocuments[index],
            [field]: value,
        };
        setDocuments(updatedDocuments);
    };

    const handleSubmit = async () => {
        setIsSubmitting(true);
        setLoading(true);
        setModalVisible(true);

        const requestBody = documents.map((document) => ({
            base64Image: document.base64Image,
            documentType: document.documentType,
            expirationDate: document.expirationDate,
        }));

        const invalidDocuments = documents
            .map((doc, index) => {
                const missingFields: string[] = [];

                if (!doc.base64Image) missingFields.push("zdjęcie");
                if (!doc.documentType) missingFields.push("typ dokumentu");
                if (!doc.expirationDate) {
                    missingFields.push("data ważności");
                } else {
                    const today = new Date();
                    const expiration = new Date(doc.expirationDate);

                    today.setHours(0, 0, 0, 0);
                    expiration.setHours(0, 0, 0, 0);

                    if (expiration <= today) {
                        missingFields.push("data ważności (musi być przyszła)");
                    }
                }

                return missingFields.length > 0
                    ? `Dokument ${index + 1} nie zawiera: ${missingFields.join(", ")}.`
                    : null;
            })
            .filter((msg) => msg !== null);

        if (invalidDocuments.length > 0) {
            setApiResponse({
                status: "error",
                message: invalidDocuments.join("\n"),
            });
            setLoading(false);
            setIsSubmitting(false);
            return;
        }

        try {
            const token = await AsyncStorage.getItem("jwtToken");

            const response = await fetch(`${CONFIG.API_BASE_URL}/api/driver-applications`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${token}`,
                },
                body: JSON.stringify(requestBody),
            });

            const responseData = await response.json();

            if (response.ok) {
                setApiResponse({
                    status: 'success',
                    message: 'Twój wniosek został pomyślnie złożony!',
                });
                setDocuments([
                    { documentType: 'ID', expirationDate: '', base64Image: '', imageUri: null, imageSaved: false },
                ]);
            } else {
                setApiResponse({
                    status: 'error',
                    message: responseData.message || 'Something went wrong',
                });
            }
        } catch (error) {
            setApiResponse({
                status: 'error',
                message: error instanceof Error ? error.message : 'Unknown error',
            });
        } finally {
            setLoading(false);
            setIsSubmitting(false);
        }
    };

    const handleRemoveDocument = (index: number) => {
        if (documents.length > 1) {
            const updatedDocuments = documents.filter((_, i) => i !== index);
            setDocuments(updatedDocuments);
        }
    };

    const pickImage = async (index: number) => {
        let result = await ImagePicker.launchCameraAsync({
            allowsEditing: true,
            quality: 0.5,
            base64: true,
        });

        if (!result.canceled && result.assets?.[0]?.uri) {
            const base64Image = result.assets[0].base64!;

            const updatedDocuments = [...documents];
            updatedDocuments[index].base64Image = base64Image;
            updatedDocuments[index].imageUri = result.assets[0].uri;
            updatedDocuments[index].imageSaved = true;
            setDocuments(updatedDocuments);
        }
    };

    const removeImage = (index: number) => {
        const updatedDocuments = [...documents];
        updatedDocuments[index].base64Image = '';
        updatedDocuments[index].imageUri = null;
        updatedDocuments[index].imageSaved = false;
        setDocuments(updatedDocuments);
    };

    useEffect(() => {
        const backAction = () => {
            router.replace('/driver_application');
            return true;
        };

        BackHandler.addEventListener('hardwareBackPress', backAction);

        return () => {
            BackHandler.removeEventListener('hardwareBackPress', backAction);
        };
    }, []);

    return (
        <ScrollView className="flex-1 p-4 bg-theme_background">
            <StatusBar style="light" />

            <Text className="text-2xl font-bold text-white mt-10 mb-4">
                Stwórz wniosek
            </Text>
            <Text className="text-white text-sm mb-6">
                Proszę przesłać wymagane dokumenty (np. dowód osobisty, prawo jazdy), aby wypełnić wniosek.
            </Text>

            {documents.map((document, index) => (
                <View className="bg-black/10 p-4 mb-4 rounded-xl border border-gray-300" key={index}>
                    {documents.length > 1 && (
                        <TouchableOpacity
                            className="absolute top-0 right-1 p-2 rounded-full"
                            onPress={() => handleRemoveDocument(index)}
                        >
                            <AntDesign name="close" size={25} color="white" />
                        </TouchableOpacity>
                    )}

                    <Text className="text-lg font-semibold text-white mb-2">
                        Dokument {index + 1}
                    </Text>

                    <Text
                        className="text-base font-semibold mb-1"
                        style={{ color: '#C8C8C8'}}
                    >
                        Typ dokumentu:
                    </Text>
                    <View className="border border-gray-300 rounded-lg mb-2 h-12 flex justify-center">
                        <Picker
                            selectedValue={document.documentType}
                            onValueChange={(itemValue: string) => handleChange(index, "documentType", itemValue as DocumentType)}
                            className="bg-black/5 p-2 text-sm text-white border border-gray-300"
                            dropdownIconColor="#C8C8C8"
                        >
                            {Object.values(DocumentType).map((key) => (
                                <Picker.Item
                                    key={key}
                                    label={documentTypes[key]}
                                    value={key}
                                    color="#C8C8C8"
                                />
                            ))}
                        </Picker>
                    </View>

                    <Text
                        className="text-base font-semibold mb-1"
                        style={{ color: '#C8C8C8'}}
                    >
                        Termin ważności:
                    </Text>
                    <TextInput
                        className="bg-black/5 p-3 rounded-lg mb-2 text-base text-white border border-gray-300"
                        placeholder="Termin ważności (RRRR-MM-DD)"
                        placeholderTextColor="#C8C8C8"
                        value={document.expirationDate}
                        onChangeText={(text) => {
                            const updatedDocuments = [...documents];
                            updatedDocuments[index].expirationDate = text;
                            setDocuments(updatedDocuments);
                        }}
                    />

                    {document.imageUri && document.imageSaved && (
                        <View className="mt-2 flex-row items-center">
                            <Image
                                source={{ uri: document.imageUri }}
                                style={{ width: 100, height: 100, borderRadius: 8 }}
                            />
                            <TouchableOpacity
                                className="ml-2 bg-red-700 p-2 rounded-lg"
                                onPress={() => removeImage(index)}
                            >
                                <Text className="text-white">Usuń zdjęcie</Text>
                            </TouchableOpacity>
                        </View>
                    )}

                    {!document.imageUri && (
                        <TouchableOpacity
                            className="bg-theme_accent p-3 rounded-lg flex flex-row items-center justify-center"
                            onPress={() => pickImage(index)}
                        >
                            <Feather name="camera" size={20} color="white" className="mr-2" />
                            <Text className="text-white">Zrób zdjęcie</Text>
                        </TouchableOpacity>
                    )}
                </View>
            ))}

            <TouchableOpacity
                className="bg-theme_accent p-4 rounded-xl shadow-lg mb-4 items-center h-14"
                onPress={handleAddDocument}
            >
                <AntDesign name="plus" size={24} color="white" />
            </TouchableOpacity>

            <TouchableOpacity
                className="w-full bg-green-500 rounded-xl mb-8 h-14 flex justify-center items-center"
                onPress={handleSubmit}
                disabled={isSubmitting}
            >
                <Text className="text-xl font-bold text-white text-center">Złóż wniosek</Text>
            </TouchableOpacity>

            <CustomModal
                isVisible={modalVisible}
                title={apiResponse?.status === 'success' ? 'Złożenie wniosku zakończone sukcesem!' : 'Błąd'}
                message={apiResponse?.message}
                onClose={() => {
                    if (apiResponse?.status === 'success') {
                        router.replace('/driver_application');
                    } else {
                        setModalVisible(false);
                    }
                }}
                loading={loading}
            />
        </ScrollView>
    );
}

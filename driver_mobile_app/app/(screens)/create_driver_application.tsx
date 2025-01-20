import React, { useEffect, useState } from 'react';
import { TextInput, Text, ScrollView, View, TouchableOpacity, BackHandler, Image } from 'react-native';
import { AntDesign } from '@expo/vector-icons';
import { StatusBar } from 'expo-status-bar';
import { router } from "expo-router";
import * as ImagePicker from 'expo-image-picker';
import CustomModal from '@/app/components/custom_modal';
import AsyncStorage from "@react-native-async-storage/async-storage";

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
        { documentType: '', expirationDate: '', base64Image: '', imageUri: null, imageSaved: false },
    ]);
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [apiResponse, setApiResponse] = useState<ApiResponse | null>(null);
    const [loading, setLoading] = useState<boolean>(false);
    const [modalVisible, setModalVisible] = useState<boolean>(false);

    const handleAddDocument = () => {
        setDocuments([
            ...documents,
            { documentType: '', expirationDate: '', base64Image: '', imageUri: null, imageSaved: false },
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

        try {
            const token = await AsyncStorage.getItem("jwtToken");

            const response = await fetch('http://192.168.0.108:8080/api/driver-applications', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    "Authorization": `Bearer ${token}`,
                },
                body: JSON.stringify(requestBody),
            });

            const responseData = await response.json();

            if (response.ok) {
                setApiResponse({
                    status: 'success',
                    message: 'Your application has been submitted successfully!',
                });
                setDocuments([
                    { documentType: '', expirationDate: '', base64Image: '', imageUri: null, imageSaved: false },
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
            quality: 1,
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
                Create Driver Application
            </Text>
            <Text className="text-white text-sm mb-6">
                Please upload your required documents (e.g., ID, Driving License) to complete your application.
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
                        Document {index + 1}
                    </Text>

                    <TextInput
                        className="bg-black/5 p-3 rounded-lg mb-2 text-base text-white border border-gray-300"
                        placeholder="Document Type (e.g., ID)"
                        placeholderTextColor="#C8C8C8"
                        value={document.documentType}
                        onChangeText={(text) => handleChange(index, 'documentType', text)}
                    />

                    <TextInput
                        className="bg-black/5 p-3 rounded-lg mb-2 text-base text-white border border-gray-300"
                        placeholder="Expiration Date (YYYY-MM-DD)"
                        placeholderTextColor="#C8C8C8"
                        value={document.expirationDate}
                        onChangeText={(text) => handleChange(index, 'expirationDate', text)}
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
                                <Text className="text-white">Remove picture</Text>
                            </TouchableOpacity>
                        </View>
                    )}

                    {!document.imageUri && (
                        <TouchableOpacity
                            className="bg-theme_accent p-3 rounded-lg items-center"
                            onPress={() => pickImage(index)}
                        >
                            <Text className="text-white">Take a Picture</Text>
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
                className="w-full bg-theme_accent rounded-xl mb-8 h-14 flex justify-center items-center"
                onPress={handleSubmit}
                disabled={isSubmitting}
            >
                <Text className="text-xl font-bold text-white text-center">Submit application</Text>
            </TouchableOpacity>

            <CustomModal
                isVisible={modalVisible}
                title={apiResponse?.status === 'success' ? 'Submission Successful!' : 'Error'}
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

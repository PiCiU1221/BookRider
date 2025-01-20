import React, { useEffect, useState } from "react";
import { FlatList, Text, TouchableOpacity, View, Alert, Image } from "react-native";
import { AntDesign } from "@expo/vector-icons";
import { StatusBar } from "expo-status-bar";
import { router } from "expo-router";
import AsyncStorage from "@react-native-async-storage/async-storage";
import CustomModal from "@/app/components/custom_modal";
import CONFIG from "@/config";

interface DriverApplication {
    id: string;
    status: string;
    submittedAt: string;
    driverEmail: string;
}

interface DriverApplicationDetailsDTO {
    id: number;
    reviewerID: string;
    status: string;
    submittedAt: string;
    reviewedAt: string | null;
    rejectionReason: string | null;
    driverDocuments: {
        documentType: string;
        documentPhotoUrl: string;
        expiryDate: string;
    }[];
}

export default function DriverApplication() {
    const [applications, setApplications] = useState<DriverApplication[]>([]);
    const [loading, setLoading] = useState<boolean>(false);
    const [modalVisible, setModalVisible] = useState<boolean>(false);
    const [apiResponse, setApiResponse] = useState<{ status: string; message?: string } | null>(null);
    const [selectedApplication, setSelectedApplication] = useState<DriverApplicationDetailsDTO | null>(null);
    const [detailsModalVisible, setDetailsModalVisible] = useState<boolean>(false);

    const fetchApplications = async (): Promise<void> => {
        setLoading(true);
        setModalVisible(true);

        try {
            const token = await AsyncStorage.getItem("jwtToken");

            const response = await fetch(`${CONFIG.API_BASE_URL}/api/driver-applications/me?page=0&size=10`, {
                method: "GET",
                headers: {
                    Authorization: `Bearer ${token}`,
                    "Content-Type": "application/json",
                },
            });

            const data: DriverApplication[] = await response.json();

            const formattedData = data.map((item) => ({
                id: item.id.toString(),
                status: item.status,
                submittedAt: item.submittedAt,
                driverEmail: item.driverEmail,
            }));

            setApplications(formattedData);
            setApiResponse({ status: "success" });
        } catch (error: any) {
            setApiResponse({ status: "error", message: error.message || "Unknown error" });
        } finally {
            setLoading(false);
            setModalVisible(false);
        }
    };

    const fetchApplicationDetails = async (id: string): Promise<void> => {
        setLoading(true);
        setDetailsModalVisible(true);

        try {
            const token = await AsyncStorage.getItem("jwtToken");

            const response = await fetch(`${CONFIG.API_BASE_URL}/api/driver-applications/${id}`, {
                method: "GET",
                headers: {
                    Authorization: `Bearer ${token}`,
                    "Content-Type": "application/json",
                },
            });

            const details: DriverApplicationDetailsDTO = await response.json();
            setSelectedApplication(details);
        } catch (error: any) {
            Alert.alert("Error", error.message || "Unknown error");
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchApplications();
    }, []);

    const handleCreateApplicationPress = (): void => {
        router.replace("/create_driver_application");
    };

    return (
        <View className="flex-1 p-4 bg-theme_background">
            <StatusBar style="light" />
            <Text className="text-2xl font-bold text-white mt-10 mb-4">Driver Applications</Text>

            {(applications.length === 0 ? (
                <View className="flex-1 justify-center items-center">
                    <Text className="text-white text-center text-lg mb-4">
                        You have no applications. Create one to get started!
                    </Text>
                </View>
            ) : (
                <FlatList
                    data={applications}
                    keyExtractor={(item) => item.id}
                    renderItem={({ item }) => (
                        <TouchableOpacity onPress={() => fetchApplicationDetails(item.id)}>
                            <View className="bg-black/10 p-4 mb-4 rounded-lg border border-gray-300">
                                <Text className="text-lg font-semibold text-white">Status: {item.status}</Text>
                                <Text className="text-white">
                                    Submitted At: {new Date(item.submittedAt).toLocaleString()}
                                </Text>
                            </View>
                        </TouchableOpacity>
                    )}
                />
            ))}

            <View className="absolute bottom-6 right-6">
                <TouchableOpacity
                    onPress={handleCreateApplicationPress}
                    className="bg-theme_accent p-4 rounded-full shadow-lg"
                    style={{ elevation: 5 }}
                >
                    <AntDesign name="plus" size={32} color="white" />
                </TouchableOpacity>
            </View>

            <CustomModal
                isVisible={modalVisible}
                title={apiResponse?.status === "success" ? "Fetch Successful!" : "Error"}
                message={apiResponse?.message}
                onClose={() => setModalVisible(false)}
                loading={loading}
            />

            <CustomModal
                isVisible={detailsModalVisible}
                title={selectedApplication ? "Application Details" : "Loading..."}
                onClose={() => {
                    setDetailsModalVisible(false);
                    setSelectedApplication(null);
                }}
                loading={!selectedApplication}
            >
                {selectedApplication && (
                    <View className="space-y-4">
                        <Text className="text-white text-2xl font-semibold">ID: <Text className="font-normal">{selectedApplication.id}</Text></Text>
                        <Text className="text-white text-lg">Reviewer ID: <Text className="font-normal">{selectedApplication.reviewerID || "N/A"}</Text></Text>
                        <Text className="text-white text-lg">Status: <Text className="font-normal">{selectedApplication.status}</Text></Text>
                        <Text className="text-white text-lg">
                            Submitted At: <Text className="font-normal">{new Date(selectedApplication.submittedAt).toLocaleString()}</Text>
                        </Text>
                        <Text className="text-white text-lg">
                            Reviewed At: <Text className="font-normal">{selectedApplication.reviewedAt ? new Date(selectedApplication.reviewedAt).toLocaleString() : "N/A"}</Text>
                        </Text>
                        <Text className="text-white text-lg">
                            Rejection Reason: <Text className="font-normal">{selectedApplication.rejectionReason || "N/A"}</Text>
                        </Text>

                        <View className="mt-6">
                            <Text className="text-white text-xl font-semibold mb-2">Driver Documents:</Text>
                            {selectedApplication.driverDocuments.map((doc, index) => (
                                <View key={index} className="bg-black/10 p-4 rounded-lg mb-4">
                                    <Text className="text-white text-lg font-medium">Type: <Text className="font-normal">{doc.documentType}</Text></Text>
                                    <Text className="text-white text-lg font-medium">Expiry Date: <Text className="font-normal">{new Date(doc.expiryDate).toLocaleDateString()}</Text></Text>
                                    <View className="mt-4">
                                        <Image
                                            source={{ uri: doc.documentPhotoUrl }}
                                            style={{ width: 200, height: 200, borderRadius: 8 }}
                                        />
                                    </View>
                                </View>
                            ))}
                        </View>
                    </View>

                )}
            </CustomModal>
        </View>
    );
}
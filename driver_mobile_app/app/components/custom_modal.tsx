import React from "react";
import {Text, View, TouchableOpacity, ActivityIndicator, ScrollView} from "react-native";
import Modal from "react-native-modal";

interface CustomModalProps {
    isVisible: boolean;
    title: string;
    message?: string;
    onClose: () => void;
    loading?: boolean;
    children?: React.ReactNode;
}

const CustomModal: React.FC<CustomModalProps> = ({
                                                     isVisible,
                                                     title,
                                                     message,
                                                     onClose,
                                                     loading = false,
                                                     children,
                                                 }) => {
    const showContent = isVisible && !loading;

    return (
        <Modal
            isVisible={isVisible}
            onBackdropPress={onClose}
            onBackButtonPress={onClose}
            backdropColor="rgba(0, 0, 0, 0.5)"
            backdropOpacity={0.7}
            animationIn="slideInUp"
            animationOut="slideOutDown"
        >
            <View
                className="bg-theme_background p-6 rounded-lg"
                style={{ maxHeight: 700 }}
            >
                {loading ? (
                    <ActivityIndicator className="p-6" size={80} color="#f7ca65" />
                ) : showContent && title ? (
                    <>
                        <Text className="text-xl font-bold text-center mb-4 text-white">
                            {title}
                        </Text>
                        {message && (
                            <Text className="text-lg text-center mb-6 text-white">{message}</Text>
                        )}
                        <ScrollView contentContainerStyle={{ flexGrow: 1 }}>
                            {children && <View>{children}</View>}
                        </ScrollView>
                        <TouchableOpacity
                            onPress={onClose}
                            className="bg-theme_accent rounded-xl w-full py-3 mt-4"
                        >
                            <Text className="text-xl text-white text-center font-semibold">OK</Text>
                        </TouchableOpacity>
                    </>
                ) : null}
            </View>
        </Modal>
    );
};

export default CustomModal;

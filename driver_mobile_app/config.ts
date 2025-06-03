import Constants from 'expo-constants';

const CONFIG = {
    API_BASE_URL: Constants.expoConfig?.extra?.EXPO_PUBLIC_API_BASE_URL ?? '',
};

export default CONFIG;

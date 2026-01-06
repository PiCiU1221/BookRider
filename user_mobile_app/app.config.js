import 'dotenv/config';

export default ({ config }) => ({
  ...config,
  name: "BookRider Driver",
  slug: "bookrider_driver",
  version: "1.0.0",
  orientation: "portrait",
  icon: "./assets/images/icon.png",
  scheme: "bookrider",
  userInterfaceStyle: "automatic",
  newArchEnabled: true,
  ios: {
    supportsTablet: true
  },
  android: {
    adaptiveIcon: {
      foregroundImage: "./assets/images/splash-icon.png",
      backgroundColor: "#ffffff"
    },
    package: "com.piciu1221.bookrider_driver"
  },
  web: {
    bundler: "metro",
    output: "static",
    favicon: "./assets/images/icon.png"
  },
  plugins: [
    "expo-router",
    [
      "expo-splash-screen",
      {
        image: "./assets/images/splash-icon.png",
        imageWidth: 200,
        resizeMode: "contain",
        backgroundColor: "#ffffff"
      }
    ],
    [
      "expo-build-properties",
      {
        android: {
          kotlinVersion: "1.9.25"
        }
      }
    ]
  ],
  experiments: {
    typedRoutes: true
  },
  extra: {
    router: {
      origin: false
    },
    eas: {
      projectId: "f59e5713-6e44-4fd8-896f-97f9442bdb3b",
    },
    EXPO_PUBLIC_API_BASE_URL: process.env.EXPO_PUBLIC_API_BASE_URL,
  },
});

import {SplashScreen, Stack} from "expo-router";
import * as SystemUI from "expo-system-ui";

import "../global.css"
import React, {useEffect} from "react";

export default function RootLayout() {
    useEffect(() => {
        SystemUI.setBackgroundColorAsync("#3b576c");

        SplashScreen.hide();
    }, []);

    return (
        <Stack
            screenOptions={{
                headerShown: false,
            }}
        />
    );
}
import { StyleSheet,Text, View } from "react-native";
import {Link} from "expo-router";
import {SafeAreaView, SafeAreaProvider} from 'react-native-safe-area-context';

export default function Index() {
    return (
        <SafeAreaProvider>
            <SafeAreaView style={{flex: 1}}>
                <View>
                    <Text>Here will be books one day</Text>
                </View>
            </SafeAreaView>
        </SafeAreaProvider>
    );
}
const styles ={
    container: {
        flex:1,
        backgroundColor: "#fff",
        alignItems: "center",
        justifyContent: "center",

    }
}
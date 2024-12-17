import {Button, StyleSheet, Text, TextInput, View} from "react-native";
import {Link} from 'expo-router';
import {SafeAreaProvider, SafeAreaView} from "react-native-safe-area-context";

export default function register() {
    return (
        <SafeAreaProvider>
            <SafeAreaView style={{
                height: 650,
                flexDirection: 'row',
                alignItems: "center",
                flex: 1,
                justifyContent:"center"
            }}>
                <View
                    style={{
                        justifyContent: "center",
                        alignItems: "center",
                        borderWidth: 1,
                        borderColor: "black",
                        width: "80%",
                        height: "80%"
                    }}
                >
                    <Text>Register now!</Text>
                    <TextInput style={styles.input} placeholder="email"></TextInput>
                    <TextInput style={styles.input} placeholder="first name"></TextInput>
                    <TextInput style={styles.input} placeholder="last name"></TextInput>
                    <TextInput style={styles.input} placeholder="username"></TextInput>
                    <TextInput style={styles.input} placeholder="passwors"></TextInput>
                    <TextInput style={styles.input} placeholder="confirm password"></TextInput>
                    <Button title="Register"  color="#009933"/>
                </View>
            </SafeAreaView>
        </SafeAreaProvider>
    );
}
const styles = StyleSheet.create({
    input: {
        height: 40,
        margin: 12,
        borderWidth: 1,
        padding: 10,
        backgroundColor: '#ffffff',
        width: 195 ,

    },
});
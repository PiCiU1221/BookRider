import { StyleSheet,Text, View, TextInput} from "react-native";
import {Link} from 'expo-router';
import {SafeAreaView, SafeAreaProvider} from 'react-native-safe-area-context';

export default function log_in() {
    return (
        <View
            style={{
                flex: 1,
                justifyContent: "center",
                alignItems: "center",
            }}
        >
            <Text>login?</Text>
            <Text> Username/e-mail</Text>
            <TextInput
            style={styles.input}

            >

            </TextInput>
            <Link href="/register" style={{color:'blue'}}>Resgister now!</Link>
        </View>
    );
}
const styles = StyleSheet.create({
    input: {
        height: 40,
        margin: 12,
        borderWidth: 1,
        padding: 10,
    },
});
import {StyleSheet, Text, View, TextInput, Button, Pressable} from "react-native";
import {Link} from 'expo-router';
import {SafeAreaView, SafeAreaProvider} from 'react-native-safe-area-context';

export default function log_in() {
    return (
        <SafeAreaProvider>
            <SafeAreaView
                style={{
                    height: 650,
                    flexDirection: 'row',
                    alignItems: "center",
                    flex: 1,
                    justifyContent:"center"
                }}>
                <View
                style={{
                    //flex: 1,
                    justifyContent: "center",
                    alignItems: "center",
                    borderWidth: 1,
                    borderColor: "black",
                    width: "80%",

                }}
                >
                    <Text>login?</Text>
                    {/*<Text> Username/e-mail</Text>*/}
                    <TextInput
                    style={styles.input}
                    placeholder="Username/e-mail"
                    >

                    </TextInput>
                    <TextInput
                        style={styles.input}
                        placeholder="Password"
                    >

                    </TextInput>
                    <Button title="Log in!" color="#009933" />
                        <Link href="/book_search" style={{color:'blue'}}>Temporary link to book search page </Link>
                    <Link href="/register" style={{color:'blue'}}>Register now!</Link>
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

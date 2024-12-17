import {StyleSheet, Text, View, TextInput, Button, Pressable} from "react-native";
import {Link} from 'expo-router';
import {SafeAreaView, SafeAreaProvider} from 'react-native-safe-area-context';
import {useState} from "react";

const validateInput = (email:string,userName:string) => {
    //TO DO:
    //validate (or send it to back end to validate) Inputs!!!
    console.warn("works");
}

export default function log_in() {
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
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
                    <TextInput
                    style={styles.input}
                    placeholder="Username/e-mail"
                    value={email}
                    onChangeText={email => setEmail(email)}
                    ></TextInput>
                    <TextInput
                        style={styles.input}
                        placeholder="Password"
                        value={password}
                        onChangeText={password => setPassword(email)}
                    ></TextInput>
                    <Button title="Log in!" color="#009933" onPress={()=>validateInput(email,password)} />
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

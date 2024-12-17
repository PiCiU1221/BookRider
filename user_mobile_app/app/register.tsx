import {Button, StyleSheet, Text, TextInput, View} from "react-native";
import {Link} from 'expo-router';
import {SafeAreaProvider, SafeAreaView} from "react-native-safe-area-context";
import {useState} from "react";

const validateInput=(email:string,userName:string,password:string,confirmPassword:string,firstName:string,lastName:string)=> {
    //TO DO:
    //validate (or send it to back end to validate) Inputs!!!
    console.warn("works");
}

export default function register() {
    const [email, setEmail] = useState('');
    const [userName, setUserName] = useState('');
    const [password, setPassword] = useState('');
    const [confirmPassword, setConfirmPassword] = useState('');
    const [firstName, setFirstName] = useState('');
    const [lastName, setLastName] = useState('');
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
                    <TextInput style={styles.input}
                               placeholder="email"
                               value={email}
                               onChangeText={(val)=>{setEmail(val)}}
                    ></TextInput>
                    <TextInput style={styles.input}
                               placeholder="first name"
                               value={firstName}
                               onChangeText={(val)=>{setFirstName(val)}}
                    ></TextInput>
                    <TextInput style={styles.input}
                               placeholder="last name"
                               value={lastName}
                               onChangeText={(val)=>{setLastName(val)}}
                    ></TextInput>
                    <TextInput style={styles.input}
                               placeholder="username"
                               value={userName}
                               onChangeText={(val)=>{setUserName(val)}}
                    ></TextInput>
                    <TextInput style={styles.input}
                               placeholder="passwors"
                               value={password}
                               onChangeText={(val)=>{setPassword(val)}}
                    ></TextInput>
                    <TextInput style={styles.input}
                               placeholder="confirm password"
                               value={confirmPassword}
                               onChangeText={(val)=>{setConfirmPassword(val)}}
                    ></TextInput>
                    <Button title="Register"  color="#009933" onPress={()=>validateInput(email,userName,password,confirmPassword,firstName,lastName)}/>
                    <Link href="/log_in" style={{color: "blue"}}> Temporary log in link</Link>
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
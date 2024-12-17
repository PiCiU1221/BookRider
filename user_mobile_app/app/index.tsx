import { StyleSheet,Text, View } from "react-native";
import {Link} from "expo-router";

export default function Index() {
  return (
    <View
      style={{
        flex: 1,
        justifyContent: "center",
        alignItems: "center",
      }}
    >
      <Text>Book Rider</Text>
        <Link href="/log_in">Log in!</Link>
    </View>
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

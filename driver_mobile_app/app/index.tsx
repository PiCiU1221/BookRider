import { Text, View } from "react-native";
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
      <Text>Driver app</Text>
        <Link href="/log_in" style={{color:'blue'}}> Log in here</Link>
    </View>
  );
}

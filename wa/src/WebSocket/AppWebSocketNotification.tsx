// // App.tsx (wrap this in all pages if needed)
// import useWebSocketConnection from "./useWebSocketConnection";
// import { useToast } from "@/components/ui/use-toast";
//
// const AppWebSocketNotification: React.FC = () => {
//     const { toast } = useToast();
//
//     useWebSocketConnection({
//         channel: "librarian/orders/pending",
//         onMessage: (message) => {
//             // Show bottom-right notification
//             toast({
//                 title: "Nowe zamówienie",
//                 description: message,
//                 duration: 5000,
//                 className: "bottom-4 right-4 fixed", // depends on toast lib
//             });
//         },
//     });
//
//     return null; // Just a hook, no visible content
// };
//
// export default AppWebSocketNotification;

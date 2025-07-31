import React, {useState, useEffect } from 'react';
import {Link, useNavigate} from 'react-router-dom';
import {useWebSocketNewOrderNotification} from './useWebSocketNewOrderNotification.tsx';
import {toast} from 'react-toastify';
import 'react-toastify/dist/ReactToastify.css';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL;

const LibrarianOrders: React.FC = () => {
    interface Book {
        id: number;
        title: string;
        categoryName: string;
        authorNames: string[];
        releaseYear: number;
        publisherName: string;
        isbn: string;
        languageName: string;
        image: string;
    }

    // Error messages
    // const [message, setMessage] = useState<string | null>(null);
    // const [messageType, setMessageType] = useState<'success' | 'error' | null>(null);

    const [pendingMessage, setPendingMessage] = React.useState('');
    const [pendingMessageType, setPendingMessageType] = React.useState<'success' | 'error' | ''>('');

    const [realizationMessage, setRealizationMessage] = React.useState('');
    const [realizationMessageType, setRealizationMessageType] = React.useState<'success' | 'error' | ''>('');

    // Orders
    const [orderDetails, setOrderDetails] = useState<OrderDetails[]>([]);
    const [showRejectionInput, setShowRejectionInput] = useState(false);
    const [rejectionReason, setRejectionReason] = useState('');
    const [selectedOrderId, setSelectedOrderId] = useState<number | null>(null);
    const [customReason, setCustomReason] = useState('');
    const [handoverVisible, setHandoverVisible] = useState<{ [orderId: number]: boolean }>({});

    // Active section
    // const [currentStatus, setCurrentStatus] = useState<'PENDING' | 'IN_REALIZATION' | 'COMPLETED'>('PENDING');

    interface OrderItem {
        book: Book;
        quantity: number;
    }

    interface OrderDetails {
        orderId: number;
        userId: string;
        libraryName: string;
        pickupAddress: string;
        destinationAddress: string;
        isReturn: boolean;
        status: string;
        amount: number;
        paymentStatus: string;
        noteToDriver: string;
        createdAt: string;
        acceptedAt: string;
        driverAssignedAt: string;
        pickedUpAt: string;
        deliveredAt: string;
        orderItems: OrderItem[];
        driverId: string;

        displayStatus?: 'PENDING' | 'IN_REALIZATION' | 'COMPLETED';
    }

    const navigate = useNavigate();

    useWebSocketNewOrderNotification('librarian/orders/pending', () => {
        toast.info("Otrzymano nowe zamówienie!", {
            position: "bottom-right",
        });
        console.log("New order received!");
    });

    // Orders ----------------------------------------------------------------------------------------------------------
    useEffect(() => {
        fetchOrderDetails();
    }, []);

    useEffect(() => {
        if (pendingMessageType || realizationMessageType) {
            const handleClick = () => {
                setPendingMessage('');
                setPendingMessageType('');
                setRealizationMessage('');
                setRealizationMessageType('');
            };

            document.addEventListener("click", handleClick);

            return () => {
                document.removeEventListener("click", handleClick);
            };
        }
        return undefined;
    }, [pendingMessageType, realizationMessageType]);

    const fetchOrderDetails = async () => {
        const token = localStorage.getItem('access_token');

        try {
            const [pendingRes, realizationRes, completedRes] = await Promise.all([
                fetch(`${API_BASE_URL}/api/orders/librarian/pending?page=0&size=10`, {
                    headers: { 'Authorization': `Bearer ${token}` },
                }),
                fetch(`${API_BASE_URL}/api/orders/librarian/in-realization?page=0&size=10`, {
                    headers: { 'Authorization': `Bearer ${token}` },
                }),
                fetch(`${API_BASE_URL}/api/orders/librarian/completed?page=0&size=10`, {
                    headers: { 'Authorization': `Bearer ${token}` },
                }),
            ]);

            const pendingData = pendingRes.ok ? await pendingRes.json() : { content: [] };
            const realizationData = realizationRes.ok ? await realizationRes.json() : { content: [] };
            const completedData = completedRes.ok ? await completedRes.json() : { content: [] };

            const pendingOrders: OrderDetails[] = pendingData.content.map((order: OrderDetails) => ({
                ...order,
                displayStatus: 'PENDING',
            }));

            const realizationOrders: OrderDetails[] = realizationData.content.map((order: OrderDetails) => ({
                ...order,
                displayStatus: 'IN_REALIZATION',
            }));

            const completedOrders: OrderDetails[] = completedData.content.map((order: OrderDetails) => ({
                ...order,
                displayStatus: 'COMPLETED',
            }));

            const combined = [...pendingOrders, ...realizationOrders, ...completedOrders];
            setOrderDetails(combined);
        } catch (error) {
            console.error('Error fetching orders:', error);
        }
    };

    const handleAccept = async (orderId: number) => {
        const token = localStorage.getItem('access_token');

        try {
            const response = await fetch(`${API_BASE_URL}/api/orders/${orderId}/accept`, {
                method: 'PATCH',
                headers: {
                    'Authorization': `Bearer ${token}`,
                },
            });

            if (response.ok) {
                setPendingMessage('Zamówienie zostało zatwierdzone.');
                setPendingMessageType('success');
                await fetchOrderDetails();
            } else {
                setPendingMessage('Nie udało się zatwierdzić zamówienia.');
                setPendingMessageType('error');
            }
        } catch (error) {
            console.error('Błąd przy zatwierdzaniu zamówienia:', error);
        }
    };

    const handleConfirmRejection = async (orderId: number) => {
        const token = localStorage.getItem('access_token');
        const reasonToSend = rejectionReason === 'Inne' ? customReason : rejectionReason;

        if (!reasonToSend || reasonToSend.trim() === '') {
            setPendingMessage('Proszę podać powód odrzucenia.');
            setPendingMessageType('error');
            return;
        }

        try {
            const response = await fetch(`${API_BASE_URL}/api/orders/${orderId}/decline`, {
                method: 'PATCH',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${token}`,
                },
                body: JSON.stringify({ reason: reasonToSend }),
            });

            if (response.ok) {
                setPendingMessage('Zamówienie zostało odrzucone.');
                setPendingMessageType('success');
                setShowRejectionInput(false);
                setSelectedOrderId(null);
                setRejectionReason('');
                setCustomReason('');
                await fetchOrderDetails();
            } else {
                setPendingMessage('Błąd przy odrzucaniu zamówienia.');
                setPendingMessageType('error');
            }
        } catch (err) {
            console.error('Error declining order:', err);
        }
    };

    const handleHandover = async (orderId: number, driverId: string) => {
        const token = localStorage.getItem('access_token');

        if (!driverId || driverId.trim() === "") {
            setRealizationMessage("Proszę wprowadzić ID kierowcy.");
            setRealizationMessageType("error");
            return;
        }

        try {
            const response = await fetch(`${API_BASE_URL}/api/orders/${orderId}/handover?driverId=${encodeURIComponent(driverId)}`, {
                method: "PUT",
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${token}`,
                },
            });

            if (response.ok) {
                setRealizationMessage(`Zamówienie ${orderId} zostało przekazane pomyślnie!`);
                setRealizationMessageType("success");
                await fetchOrderDetails();
            } else {
                setRealizationMessage('Błąd przy przekazywaniu zamówienia.');
                setRealizationMessageType('error');
            }
        } catch (err) {
            console.error('Error handing over order:', err);
        }
    };

    const handleLogout = () => {
        localStorage.removeItem('username');
        localStorage.removeItem('email');
        localStorage.removeItem('access_token');
        localStorage.removeItem('role');

        navigate('/');
    };

    return (
        <div className="bg-[#314757] min-h-screen h-screen overflow-hidden">
            <header
                className="flex justify-around p-1 pr-4 space-x-2.5 bg-[#3B576C] text-white sticky top-0 z-[1000] shadow-md">
                <div>
                    <img
                        className="relative w-[80vw] h-auto object-cover left-[1%]"
                        alt="Book Rider Logo"
                        src="/book-rider-high-resolution-logo.png"
                    />
                </div>
                {[
                    {id: 'addBook', label: 'Książki', path: '/librarian-dashboard'},
                    {id: 'orders', label: 'Wypożyczenia', path: '/orders'},
                    {id: 'returns', label: 'Zwroty', path: '/returns'},
                    {id: 'readers', label: 'Czytelnicy', path: '/readers'},
                    {id: 'settings', label: 'Ustawienia', path: '/librarian-settings'},
                ].map(({id, label, path}) => (
                    <Link
                        key={id}
                        to={path}
                        className="w-full px-12 py-2 h-[3vw] self-center rounded border-none cursor-pointer text-[2.5vh] transition-colors bg-[#314757] hover:bg-[#4B6477] duration-200 ease-out flex justify-center items-center"
                    >
                        {label}
                    </Link>
                ))}
                <button
                    onClick={handleLogout}
                    className="w-full px-12 py-2 h-[3vw] self-center rounded border-none cursor-pointer text-[2.5vh] transition-colors bg-[#314757] hover:bg-[#4B6477] duration-200 ease-out"
                >
                    Wyloguj się
                </button>
            </header>

            <div className="flex justify-center w-full p-10 gap-10">
                <div className="w-1/3">
                    <h2 className="text-2xl text-white font-bold text-center pb-2 mb-2">Oczekujące</h2>

                    {/* PENDING */}
                    <section className="w-full overflow-y-auto max-h-[80vh]">
                        {orderDetails
                            .filter((order) => order.displayStatus === 'PENDING')
                            .map((order) => (
                                <div key={order.orderId} className="">
                                    <div className="order-details-container mb-10 bg-white p-10 rounded-2xl shadow-lg">
                                        <div className="flex justify-between items-center mb-7">
                                            <h1 className="text-3xl font-bold text-gray-700">Zamówienie
                                                nr: {order.orderId}</h1>
                                        </div>

                                        <p className="border-t text-lg border-gray-400 text-gray-700 pt-5">
                                            <strong>ID użytkownika:</strong> {order.userId}
                                        </p>
                                        <p className="text-gray-700 text-lg">
                                            <strong>Data
                                                utworzenia:</strong> {new Date(order.createdAt).toLocaleString()}
                                        </p>

                                        {order.orderItems.map((item, index) => (
                                            <div key={index} className="mt-4 pt-4">
                                                <div className="flex justify-center mb-4">
                                                    <img
                                                        src={item.book.image}
                                                        alt={item.book.title}
                                                        className="w-45 h-65 border-gray-400 border rounded-xl object-cover"
                                                    />
                                                </div>

                                                <div className="space-y-1.5 ml-1">
                                                    <h2 className="text-3xl mb-5 mt-12 font-semibold text-gray-800">{item.book.title}</h2>
                                                    <p className="text-gray-600 text-lg">
                                                        <strong>Autor:</strong> {item.book.authorNames.join(', ')}</p>
                                                    <p className="text-gray-600 text-lg">
                                                        <strong>ISBN:</strong> {item.book.isbn}</p>
                                                    <p className="text-gray-600 text-lg">
                                                        <strong>Kategoria:</strong> {item.book.categoryName}</p>
                                                    <p className="text-gray-600 text-lg"><strong>Rok
                                                        wydania:</strong> {item.book.releaseYear}</p>
                                                    <p className="text-gray-600 text-lg">
                                                        <strong>Wydawnictwo:</strong> {item.book.publisherName}</p>
                                                    <p className="text-gray-600 text-lg">
                                                        <strong>Język:</strong> {item.book.languageName}</p>
                                                    <p className="text-gray-600 text-lg"><strong>ID
                                                        książki:</strong> {item.book.id}</p>
                                                    <p className="text-gray-600 text-lg"><strong>Zamówiona
                                                        ilość:</strong> {item.quantity}</p>
                                                </div>
                                            </div>
                                        ))}

                                        <div className="flex justify-between mt-6">
                                            <button
                                                onClick={() => handleAccept(order.orderId)}
                                                className="bg-[#3E4851] text-white px-7 py-2 rounded-lg border-2 border-[#23292F]"
                                            >
                                                Zatwierdź
                                            </button>

                                            <button
                                                onClick={() => {
                                                    setSelectedOrderId(order.orderId);
                                                    setShowRejectionInput(true);
                                                    setRejectionReason('');
                                                    setCustomReason('');
                                                }}
                                                className="bg-gray-200 text-[#2D343A] px-5 py-2 rounded-lg border-2 border-[#314757]"
                                            >
                                                Odrzuć
                                            </button>
                                        </div>

                                        {showRejectionInput && selectedOrderId === order.orderId && (
                                            <div className="mt-12">
                                                <h3 className="text-gray-600 text-lg font-semibold">Wybierz przyczynę
                                                    odmowy:</h3>
                                                <form className="flex flex-col items-start gap-2 mt-2">

                                                    {['Brak w zbiorach biblioteki', 'Wszystkie egzemplarze zostały wypożyczone', 'Inne'].map(reason => (
                                                        <label key={reason}
                                                               className="flex text-lg items-center text-gray-600">
                                                            <input
                                                                type="radio"
                                                                value={reason}
                                                                checked={rejectionReason === reason}
                                                                onChange={(e) => {
                                                                    setRejectionReason(e.target.value);
                                                                    if (e.target.value !== 'Inne') setCustomReason('');
                                                                }}
                                                                className="peer hidden"
                                                            />
                                                            <span
                                                                className="w-5 h-5 border-2 rounded-full mr-2 inline-block bg-white peer-checked:bg-[#3B576C] peer-checked:ring-1 peer-checked:ring-[#3B576C]"/>
                                                            {reason.toLowerCase()}
                                                        </label>
                                                    ))}

                                                    {rejectionReason === 'Inne' && (
                                                        <input
                                                            type="text"
                                                            className="border text-gray-600 border-gray-400 p-2 rounded-lg mt-2 w-full mb-2 focus:outline-none focus:ring-2 focus:ring-[#3B576C]"
                                                            placeholder="Podaj powód odrzucenia"
                                                            value={customReason}
                                                            onChange={(e) => setCustomReason(e.target.value)}
                                                        />
                                                    )}
                                                </form>

                                                {pendingMessage && (
                                                    <div
                                                        className={`mb-4 mt-5 p-1 text-lg ${
                                                            pendingMessageType === 'success' ? 'text-[#314757]' : 'text-red-600'
                                                        }`}
                                                    >
                                                        {pendingMessage}
                                                    </div>
                                                )}

                                                <button
                                                    className="bg-gray-200 text-[#2D343A] px-5 py-2 rounded-lg border-2 border-[#314757] mt-4"
                                                    onClick={() => handleConfirmRejection(order.orderId)}
                                                >
                                                    Potwierdź odrzucenie
                                                </button>
                                            </div>
                                        )}
                                    </div>
                                </div>
                            ))}
                    </section>
                </div>

                {/* IN_REALIZATION */}
                <div className="w-1/3">
                    <h2 className="text-2xl text-white font-bold text-center pb-2 mb-2">W realizacji</h2>

                    <section className="w-full overflow-y-auto max-h-[80vh]">
                        {orderDetails
                            .filter((order) => order.displayStatus === 'IN_REALIZATION')
                            .map((order) => (
                                <div key={order.orderId} className="">
                                    <div
                                        className="order-details-container mb-10 bg-[#b6c6d1] p-10 rounded-2xl shadow-lg">
                                        <div className="flex justify-between items-center mb-7">
                                            <h1 className="text-3xl font-bold text-gray-700">
                                                Zamówienie nr: {order.orderId}
                                            </h1>
                                        </div>

                                        <p className="border-t text-lg border-gray-500 text-gray-700 pt-5">
                                            <strong>ID użytkownika:</strong> {order.userId}
                                        </p>
                                        <p className="text-gray-700 text-lg">
                                            <strong>Data utworzenia:</strong>{' '}
                                            {new Date(order.createdAt).toLocaleString()}
                                        </p>

                                        {order.orderItems.map((item, index) => (
                                            <div key={index} className="mt-4 pt-4">
                                                <div className="flex justify-center mb-4">
                                                    <img
                                                        src={item.book.image}
                                                        alt={item.book.title}
                                                        className="w-45 h-65 border-gray-400 border rounded-xl object-cover"
                                                    />
                                                </div>

                                                <div className="space-y-1.5 ml-1">
                                                    <h2 className="text-3xl mb-5 mt-12 font-semibold text-gray-800">
                                                        {item.book.title}
                                                    </h2>
                                                    <p className="text-gray-600 text-lg">
                                                        <strong>Autor:</strong> {item.book.authorNames.join(', ')}
                                                    </p>
                                                    <p className="text-gray-600 text-lg">
                                                        <strong>ISBN:</strong> {item.book.isbn}
                                                    </p>
                                                    <p className="text-gray-600 text-lg">
                                                        <strong>Kategoria:</strong> {item.book.categoryName}
                                                    </p>
                                                    <p className="text-gray-600 text-lg">
                                                        <strong>Rok wydania:</strong> {item.book.releaseYear}
                                                    </p>
                                                    <p className="text-gray-600 text-lg">
                                                        <strong>Wydawnictwo:</strong> {item.book.publisherName}
                                                    </p>
                                                    <p className="text-gray-600 text-lg">
                                                        <strong>Język:</strong> {item.book.languageName}
                                                    </p>
                                                    <p className="text-gray-600 text-lg">
                                                        <strong>ID książki:</strong> {item.book.id}
                                                    </p>
                                                    <p className="text-gray-600 text-lg">
                                                        <strong>Zamówiona ilość:</strong> {item.quantity}
                                                    </p>
                                                </div>
                                            </div>
                                        ))}

                                        <button
                                            onClick={() =>
                                                setHandoverVisible((prev) => ({
                                                    ...prev,
                                                    [order.orderId]: !prev[order.orderId],
                                                }))
                                            }
                                            className="mt-6 mb-2 w-full py-2 px-4 bg-[#3B576C] text-white rounded-md cursor-pointer hover:bg-[#314757] duration-200 ease-out"
                                        >
                                            Zakończ zamówienie
                                        </button>

                                        {handoverVisible[order.orderId] && (
                                            <>
                                                <input
                                                    type="text"
                                                    className="w-full px-4 py-2 mb-3 mt-12 border rounded-lg focus:outline-none focus:ring-2 focus:ring-[#314757]"
                                                    placeholder="Wprowadź ID kierowcy"
                                                    value={order.driverId || ''}
                                                    onChange={(e) => {
                                                        const newDriverId = e.target.value;
                                                        setOrderDetails((prevOrders) =>
                                                            prevOrders.map((o) =>
                                                                o.orderId === order.orderId
                                                                    ? { ...o, driverId: newDriverId }
                                                                    : o
                                                            )
                                                        );
                                                    }}

                                                />

                                                {realizationMessage && (
                                                    <div
                                                        className={`mb-4 mt-1 p-1 text-lg ${
                                                            realizationMessageType === 'success' ? 'text-[#314757]' : 'text-red-600'
                                                        }`}
                                                    >
                                                        {realizationMessage}
                                                    </div>
                                                )}

                                                <button
                                                    onClick={() => handleHandover(order.orderId, String(order.driverId))}
                                                    className="mt-2 mb-2 w-full py-2 px-4 bg-[#5B7F9A] text-white rounded-md cursor-pointer hover:bg-[#4B6477] duration-200 ease-out"
                                                >
                                                    Przekaż książkę
                                                </button>
                                            </>
                                        )}
                                    </div>
                                </div>
                            ))}
                    </section>
                </div>

                <div className="w-1/3">
                    <h2 className="text-2xl text-white font-bold text-center pb-2 mb-2">Zrealizowane</h2>
                    {/* COMPLETED */}
                    <section className="w-full overflow-y-auto max-h-[80vh]">
                        {orderDetails
                            .filter((order) => order.displayStatus === 'COMPLETED')
                            .map((order) => (
                                <div key={order.orderId} className="">
                                    <div
                                        className="order-details-container mb-10 bg-[#9ca6ad] p-10 rounded-2xl shadow-lg">
                                        <div className="flex justify-between items-center mb-7">
                                            <h1 className="text-3xl font-bold text-gray-700">Zamówienie
                                                nr: {order.orderId}</h1>
                                        </div>

                                        <p className="border-t text-lg border-gray-500 text-gray-700 pt-5">
                                            <strong>ID użytkownika:</strong> {order.userId}
                                        </p>
                                        <p className="text-gray-700 text-lg">
                                            <strong>Data
                                                utworzenia:</strong> {new Date(order.createdAt).toLocaleString()}
                                        </p>

                                        {order.orderItems.map((item, index) => (
                                            <div key={index} className="mt-4 pt-4">
                                                <div className="flex justify-center mb-4">
                                                    <img
                                                        src={item.book.image}
                                                        alt={item.book.title}
                                                        className="w-45 h-65 border-gray-400 border rounded-xl object-cover"
                                                    />
                                                </div>

                                                <div className="space-y-1.5 ml-1">
                                                    <h2 className="text-3xl mb-5 mt-12 font-semibold text-gray-800">{item.book.title}</h2>
                                                    <p className="text-gray-600 text-lg">
                                                        <strong>Autor:</strong> {item.book.authorNames.join(', ')}</p>
                                                    <p className="text-gray-600 text-lg">
                                                        <strong>ISBN:</strong> {item.book.isbn}</p>
                                                    <p className="text-gray-600 text-lg">
                                                        <strong>Kategoria:</strong> {item.book.categoryName}</p>
                                                    <p className="text-gray-600 text-lg"><strong>Rok
                                                        wydania:</strong> {item.book.releaseYear}</p>
                                                    <p className="text-gray-600 text-lg">
                                                        <strong>Wydawnictwo:</strong> {item.book.publisherName}</p>
                                                    <p className="text-gray-600 text-lg">
                                                        <strong>Język:</strong> {item.book.languageName}</p>
                                                    <p className="text-gray-600 text-lg"><strong>ID
                                                        książki:</strong> {item.book.id}</p>
                                                    <p className="text-gray-600 text-lg"><strong>Zamówiona
                                                        ilość:</strong> {item.quantity}</p>
                                                </div>
                                            </div>
                                        ))}
                                    </div>
                                </div>
                            ))}
                    </section>
                </div>
            </div>
        </div>
    );
};

export default LibrarianOrders;
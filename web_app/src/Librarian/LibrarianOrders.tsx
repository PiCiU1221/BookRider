import React, {useState, useEffect } from 'react';
import {Link, useNavigate} from 'react-router-dom';

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
    const [message, setMessage] = useState<string | null>(null);
    const [messageType, setMessageType] = useState<'success' | 'error' | null>(null);

    // Orders
    const [orderDetails, setOrderDetails] = useState<OrderDetails[]>([]);
    const [showRejectionInput, setShowRejectionInput] = useState(false);
    const [rejectionReason, setRejectionReason] = useState('');
    const [selectedOrderId, setSelectedOrderId] = useState<number | null>(null);
    const [customReason, setCustomReason] = useState('');

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

        displayStatus?: 'PENDING' | 'IN_REALIZATION';
    }

    const navigate = useNavigate();

    // Orders ----------------------------------------------------------------------------------------------------------
    useEffect(() => {
        fetchOrderDetails();
    }, []);

    const fetchOrderDetails = async () => {
        const token = localStorage.getItem('access_token');

        try {
            const [pendingRes, realizationRes] = await Promise.all([
                fetch(`${API_BASE_URL}/api/orders/librarian/pending?page=0&size=10`, {
                    headers: { 'Authorization': `Bearer ${token}` },
                }),
                fetch(`${API_BASE_URL}/api/orders/librarian/in-realization?page=0&size=10`, {
                    headers: { 'Authorization': `Bearer ${token}` },
                }),
            ]);

            const pendingData = pendingRes.ok ? await pendingRes.json() : { content: [] };
            const realizationData = realizationRes.ok ? await realizationRes.json() : { content: [] };

            const pendingOrders: OrderDetails[] = pendingData.content.map((order: OrderDetails) => ({
                ...order,
                displayStatus: 'PENDING',
            }));

            const realizationOrders: OrderDetails[] = realizationData.content.map((order: OrderDetails) => ({
                ...order,
                displayStatus: 'IN_REALIZATION',
            }));

            const combined = [...pendingOrders, ...realizationOrders];
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
                setOrderDetails(prev =>
                    prev.map(order =>
                        order.orderId === orderId
                            ? { ...order, status: 'IN_REALIZATION' }
                            : order
                    )
                );
                setMessage('Zamówienie zostało zatwierdzone.');
                setMessageType('success');
            } else {
                setMessage('Nie udało się zatwierdzić zamówienia.');
                setMessageType('error');
            }
        } catch (error) {
            console.error('Błąd przy zatwierdzaniu zamówienia:', error);
        }
    };

    const handleConfirmRejection = async (orderId: number) => {
        const token = localStorage.getItem('access_token');
        const reasonToSend = rejectionReason === 'Inne' ? customReason : rejectionReason;

        if (!reasonToSend || reasonToSend.trim() === '') {
            setMessage('Proszę podać powód odrzucenia.');
            setMessageType('error');
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
                setMessage('Zamówienie zostało odrzucone.');
                setMessageType('success');
                setShowRejectionInput(false);
                setSelectedOrderId(null);
                setRejectionReason('');
                setCustomReason('');
                fetchOrderDetails();
            } else {
                setMessage('Błąd przy odrzucaniu zamówienia.');
                setMessageType('error');
            }
        } catch (err) {
            console.error('Error declining order:', err);
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
        <div className="bg-[#314757] min-h-screen">
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

            {/*{activeSection === 'settings' && (*/}
            {/*    <section className="p-5 rounded-md mb-[400px] h-[80%] max-h-[90%] w-[65%]">*/}
            {/*        <h2 className="text-center text-white">Ustawienia</h2>*/}
            {/*    </section>*/}
            {/*)}*/}
            <div className="flex justify-center items-center w-full h-full">
                <section className="p-5 rounded-md mb-12 h-[80%] max-h-[90%] w-[70%] overflow-y-auto">
                    {orderDetails.length > 0 ? (
                        orderDetails
                            .filter((order) => order.status !== 'COMPLETED')
                            .map((order) => (
                                <div key={order.orderId} className="order-details-container mb-10">
                                    <div
                                        className={`p-10 rounded-2xl shadow-lg ${
                                            order.displayStatus === 'IN_REALIZATION' ? 'bg-gray-300' : 'bg-white'
                                        }`}
                                    >
                                        <div className="flex justify-between items-center mb-7">
                                            <h1 className="text-3xl font-bold text-gray-700">
                                                Zamówienie nr: {order.orderId}
                                            </h1>
                                            <p className="text-xl font-thin text-gray-600">
                                                <strong>Status:</strong>{' '}
                                                {order.displayStatus === 'PENDING'
                                                    ? 'Oczekujące'
                                                    : order.displayStatus === 'IN_REALIZATION'
                                                        ? 'W realizacji'
                                                        : 'Nieznany'}
                                            </p>
                                        </div>

                                        <p className="border-t text-lg border-gray-400 text-gray-700 pt-5 "><strong>ID
                                            użytkownika:</strong> {order.userId}</p>
                                        <p className="text-gray-700 text-lg"><strong>Data
                                            utworzenia:</strong> {new Date(order.createdAt).toLocaleString()}</p>

                                        {order.orderItems.map((item, index) => (
                                            <div key={index} className=" mt-4 pt-4">
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
                                                        <strong>Autor:</strong> {item.book.authorNames.join(', ')}
                                                    </p>
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

                                        {order.displayStatus !== 'IN_REALIZATION' && (
                                            <div className="flex justify-between mt-6">
                                                <button
                                                    onClick={() => handleAccept(order.orderId)}
                                                    className="bg-[#3E4851] text-white px-7 py-2 rounded-lg border-2 border-[#23292F]"
                                                >
                                                    Zatwierdź
                                                </button>

                                                {selectedOrderId !== order.orderId && (
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
                                                )}
                                            </div>
                                        )}

                                        {showRejectionInput && selectedOrderId === order.orderId && (
                                            <div className="mt-12">
                                                <h3 className="text-gray-600 text-lg font-semibold">Wybierz
                                                    przyczynę odmowy:</h3>
                                                <form className="flex flex-col items-start gap-2 mt-2">
                                                    <label className="flex text-lg items-center text-gray-600">
                                                        <input
                                                            type="radio"
                                                            value="Brak w zbiorach biblioteki"
                                                            checked={rejectionReason === 'Brak w zbiorach biblioteki'}
                                                            onChange={(e) => setRejectionReason(e.target.value)}
                                                            className="peer hidden"
                                                        />
                                                        <span
                                                            className="w-5 h-5 border-2 rounded-full mr-2 inline-block bg-white
                                                                 peer-checked:bg-[#3B576C] peer-checked:ring-1 peer-checked:ring-[#3B576C]"
                                                        ></span>
                                                        brak w zbiorach biblioteki
                                                    </label>

                                                    <label className="flex text-lg items-center text-gray-600">
                                                        <input
                                                            type="radio"
                                                            value="Wszystkie egzemplarze zostały wypożyczone"
                                                            checked={rejectionReason === 'Wszystkie egzemplarze zostały wypożyczone'}
                                                            onChange={(e) => setRejectionReason(e.target.value)}
                                                            className="peer hidden"
                                                        />
                                                        <span
                                                            className="w-5 h-5 border-2 rounded-full mr-2 inline-block bg-white
                                                                 peer-checked:bg-[#3B576C] peer-checked:ring-1 peer-checked:ring-[#3B576C]"
                                                        ></span>
                                                        wszystkie egzemplarze zostały wypożyczone
                                                    </label>

                                                    <label className="flex text-lg items-center text-gray-600">
                                                        <input
                                                            type="radio"
                                                            value="Inne"
                                                            checked={rejectionReason === 'Inne'}
                                                            onChange={(e) => {
                                                                setRejectionReason(e.target.value);
                                                                setCustomReason('');
                                                            }}
                                                            className="peer hidden"
                                                        />
                                                        <span
                                                            className="w-5 h-5 border-2 rounded-full mr-2 inline-block bg-white
                                                                 peer-checked:bg-[#3B576C] peer-checked:ring-1 peer-checked:ring-[#3B576C]"
                                                        ></span>
                                                        inne
                                                    </label>

                                                    {rejectionReason === 'Inne' && (
                                                        <input
                                                            type="text"
                                                            className="border text-gray-600 border-gray-400 p-2 rounded-lg mt-2
                                                                 w-full mb-2 focus:outline-none focus:ring-2 focus:ring-[#3B576C]"
                                                            placeholder="Podaj powód odrzucenia"
                                                            value={customReason}
                                                            onChange={(e) => setCustomReason(e.target.value)}
                                                        />
                                                    )}
                                                </form>

                                                {message && (
                                                    <div
                                                        className={`mb-4 mt-5 p-1 text-lg ${
                                                            messageType === 'success' ? 'text-[#314757]' : 'text-red-600'
                                                        }`}
                                                    >
                                                        {message}
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
                            ))
                    ) : (
                        <div className="flex items-center">
                            <p className="text-white">Brak zamówień do wyświetlenia.</p>
                        </div>
                    )}
                </section>
            </div>
            </div>
);
};

export default LibrarianOrders;
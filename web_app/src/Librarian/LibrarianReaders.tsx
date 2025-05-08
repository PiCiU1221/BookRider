import React, {useState} from 'react';
import {Link, useNavigate} from 'react-router-dom';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL;

const LibrarianReaders: React.FC = () => {
    // Readers
    const [userId, setUserId] = useState('');
    const [cardId, setCardId] = useState('');
    const [firstName, setFirstName] = useState('');
    const [lastName, setLastName] = useState('');
    const [expirationDate, setExpirationDate] = useState('2025-01-01');
    const [libraryCardSearchId, setLibraryCardSearchId] = useState('');

    // User
    interface LibraryCardDetails {
        userId: string;
        cardId: string;
        firstName: string;
        lastName: string;
        expirationDate: string;
    }

    const [libraryCardDetails, setLibraryCardDetails] = useState<LibraryCardDetails | null>(null);

    const navigate = useNavigate();

    // Readers ---------------------------------------------------------------------------------------------------------
    const handleCreateLibraryCard = async () => {
        const token = localStorage.getItem('access_token');
        if (!token) {
            console.error('No token found');
            return;
        }

        try {
            const response = await fetch(`${API_BASE_URL}/api/library-cards`, {
                method: 'POST',
                headers: {
                    'Authorization': `Bearer ${token}`,
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    userId,
                    cardId,
                    firstName,
                    lastName,
                    expirationDate,
                }),
            });

            if (response.ok) {
                alert('Library card created successfully');
                setUserId('');
                setCardId('');
                setFirstName('');
                setLastName('');
                setExpirationDate('2025-01-01');
            } else {
                throw new Error('Error creating library card');
            }
        } catch (error) {
            console.error('Error creating library card: ', error);
        }
    };

    const handleSearchLibraryCard = async () => {
        const token = localStorage.getItem('access_token');
        if (!token) {
            console.error("No token found");
            return;
        }

        try {
            const response = await fetch(`${API_BASE_URL}/api/library-cards/${libraryCardSearchId}`, {
                method: 'GET',
                headers: {
                    'Authorization': `Bearer ${token}`,
                    'Content-Type': 'application/json',
                },
            });

            if (response.ok) {
                const data = await response.json();
                setLibraryCardDetails(data[0]);
            } else {
                throw new Error('Error searching for library card');
            }
        } catch (error) {
            console.error('Error searching for library card: ', error);
            setLibraryCardDetails(null);
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

            <main className="flex justify-center items-center p-9 w-full max-w-[800vw]">
                    <div className="bg-white p-9 rounded-2xl shadow-md h-[80%] max-h-[90%] w-[65%]">
                        <h3 className="text-3xl p-4 font-semibold mb-11 text-gray-600 text-center">Czytelnicy</h3>

                        <div className="mb-6">
                            <h4 className="text-2xl font-thin text-gray-700 mb-8">Dodaj nowego użytkownika
                                BookRider:</h4>
                            <div className="space-y-5 text-lg">
                                <div>
                                    <label htmlFor="userId" className="block text-gray-600 font-medium mb-2">Identyfikator
                                        użytkownika:</label>
                                    <input
                                        id="userId"
                                        type="text"
                                        placeholder="Wprowadź ID użytkownika"
                                        value={userId}
                                        onChange={(e) => setUserId(e.target.value)}
                                        className="w-full p-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-[#3B576C]"
                                    />
                                </div>
                                <div>
                                    <label htmlFor="cardId" className="block text-gray-600 font-medium mb-2">Identyfikator
                                        karty:</label>
                                    <input
                                        id="cardId"
                                        type="text"
                                        placeholder="Wprowadź ID karty"
                                        value={cardId}
                                        onChange={(e) => setCardId(e.target.value)}
                                        className="w-full p-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-[#3B576C]"
                                    />
                                </div>
                                <div>
                                    <label htmlFor="firstName"
                                           className="block text-gray-600 font-medium mb-2">Imię:</label>
                                    <input
                                        id="firstName"
                                        type="text"
                                        placeholder="Wprowadź imię użytkownika"
                                        value={firstName}
                                        onChange={(e) => setFirstName(e.target.value)}
                                        className="w-full p-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-[#3B576C]"
                                    />
                                </div>
                                <div>
                                    <label htmlFor="lastName"
                                           className="block text-gray-600 font-medium mb-2">Nazwisko:</label>
                                    <input
                                        id="lastName"
                                        type="text"
                                        placeholder="Wprowadź nazwisko użytkownika"
                                        value={lastName}
                                        onChange={(e) => setLastName(e.target.value)}
                                        className="w-full p-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-[#3B576C]"
                                    />
                                </div>
                                <div>
                                    <label htmlFor="expirationDate" className="block text-gray-600 font-medium mb-2">Data
                                        ważności karty bibliotecznej:</label>
                                    <input
                                        id="expirationDate"
                                        type="date"
                                        value={expirationDate}
                                        onChange={(e) => setExpirationDate(e.target.value)}
                                        className="mb-5 w-full p-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-[#3B576C]"
                                    />
                                </div>
                                <div className="flex justify-center mt-4">
                                    <button
                                        onClick={handleCreateLibraryCard}
                                        className="mb-12 mt-5 w-[15vw] py-2 px-4 bg-[#3B576C] text-white rounded-md hover:bg-[#314757] ease-out duration-300"
                                    >
                                        Dodaj
                                    </button>
                                </div>
                            </div>
                        </div>


                        <div className="border-t border-gray-300 my-8 mb-[6%]"></div>

                        <div className="mb-6">
                            <h4 className="text-2xl font-thin text-gray-700 mb-8">Wyszukaj użytkownika:</h4>
                            <div className="space-y-4 text-lg">
                                <div>
                                    <label htmlFor="libraryCardSearchId"
                                           className="block text-gray-600 font-medium mb-2">
                                        Wprowadź identyfikator użytkownika
                                    </label>
                                    <input
                                        id="libraryCardSearchId"
                                        type="text"
                                        placeholder="ID użytkownika"
                                        value={libraryCardSearchId}
                                        onChange={(e) => setLibraryCardSearchId(e.target.value)}
                                        className="mb-5 w-full p-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-[#3B576C]"
                                    />
                                </div>
                                <div className="flex justify-center mt-4">
                                    <button
                                        onClick={handleSearchLibraryCard}
                                        className="w-[15vw] py-2 px-4 mt-5 bg-[#3B576C] text-white rounded-md hover:bg-[#314757] ease-out duration-300"
                                    >
                                        Wyszukaj
                                    </button>
                                </div>
                            </div>
                        </div>

                        {libraryCardDetails && (
                            <div className="mb-6 border border-gray-300 p-4 rounded-2xl">
                                <h4 className="text-xl font-medium text-gray-700 mb-4">
                                    Szczegóły wyszukiwanego konta:
                                </h4>
                                <p><strong>ID użytkownika:</strong> {libraryCardDetails.userId}</p>
                                <p><strong>ID karty:</strong> {libraryCardDetails.cardId}</p>
                                <p><strong>Imię:</strong> {libraryCardDetails.firstName}</p>
                                <p><strong>Nazwisko:</strong> {libraryCardDetails.lastName}</p>
                                <p><strong>Data ważności karty
                                    bibliotecznej:</strong> {libraryCardDetails.expirationDate}</p>
                            </div>
                        )}
                    </div>

                {/*{activeSection === 'settings' && (*/}
                {/*    <section className="p-5 rounded-md mb-[400px] h-[80%] max-h-[90%] w-[65%]">*/}
                {/*        <h2 className="text-center text-white">Ustawienia</h2>*/}
                {/*    </section>*/}
                {/*)}*/}

            </main>
        </div>
    );
};

export default LibrarianReaders;
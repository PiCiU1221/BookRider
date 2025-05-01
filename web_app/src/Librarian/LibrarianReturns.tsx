import React, {useState} from 'react';
import {Link, useNavigate} from 'react-router-dom';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL;

const LibrarianReturns: React.FC = () => {
    // Error utils
    const [message, setMessage] = useState<string | null>(null);
    const [messageType, setMessageType] = useState<'success' | 'error' | null>(null);

    // Returns
    interface RentalReturnItem {
        id: number;
        rentalId: number;
        book: {
            id: number;
            title: string;
            categoryName: string;
            authorNames: string[];
            releaseYear: number;
            publisherName: string;
            isbn: string;
            languageName: string;
            image: string;
        };
        returnedQuantity: number;
    }

    interface RentalReturnDetails {
        id: number;
        orderId: number;
        returnedAt: string;
        status: 'IN_PROGRESS' | 'COMPLETED';
        rentalReturnItems: RentalReturnItem[];
    }

    const [returnType, setReturnType] = useState<'inPerson' | 'driver'>('inPerson');
    const [rentalReturnId, setRentalReturnId] = useState<number | null>(null);
    const [rentalReturnDetails, setRentalReturnDetails] = useState<RentalReturnDetails | null>(null);
    const [driverId, setDriverId] = useState<string | null>(null);
    // const [returnStatus, setReturnStatus] = useState<'IN_PROGRESS' | 'COMPLETED'>('IN_PROGRESS');

    const navigate = useNavigate();

    // Returns ---------------------------------------------------------------------------------------------------------
    const fetchRentalReturnDetailsInPerson = async (rentalReturnId: number) => {
        const token = localStorage.getItem('access_token');
        if (!token) return;

        try {
            const response = await fetch(`${API_BASE_URL}/api/rental-returns/${rentalReturnId}`, {
                headers: {
                    'Authorization': `Bearer ${token}`,
                },
            });
            const data = await response.json();
            setRentalReturnDetails(data);
        } catch (error) {
            console.error('Error fetching rental return details:', error);
        }
    };

    const completeInPersonReturn = async (rentalReturnId: number) => {
        const token = localStorage.getItem('access_token');
        if (!token) return;

        try {
            const response = await fetch(`${API_BASE_URL}/api/rental-returns/${rentalReturnId}/complete-in-person`, {
                method: 'PATCH',
                headers: {
                    'Authorization': `Bearer ${token}`,
                },
            });

            if (response.ok) {
                //setReturnStatus('COMPLETED');
                setMessage('Return completed successfully.');
                setMessageType('success');
            } else {
                setMessage('Failed to complete the return.');
                setMessageType('error');
            }
        } catch (error) {
            console.error('Error:', error);
            setMessage('Error completing the return.');
            setMessageType('error');
        }
    };

    const fetchRentalReturnDetailsByDriver = async (driverId: string) => {
        const token = localStorage.getItem('access_token');
        if (!token) return;

        try {
            const response = await fetch(`${API_BASE_URL}/api/rental-returns/latest-by-driver/${driverId}`, {
                headers: {
                    'Authorization': `Bearer ${token}`,
                },
            });
            const data = await response.json();
            setRentalReturnDetails(data);
        } catch (error) {
            console.error('Error:', error);
        }
    };

    const completeDriverReturn = async (rentalReturnId: number) => {
        const token = localStorage.getItem('access_token');
        if (!token) return;

        try {
            const response = await fetch(`${API_BASE_URL}/api/rental-returns/${rentalReturnId}/complete-delivery`, {
                method: 'PATCH',
                headers: {
                    'Authorization': `Bearer ${token}`,
                },
            });

            if (response.ok) {
                //setReturnStatus('COMPLETED');
                setMessage('Return completed successfully.');
                setMessageType('success');
            } else {
                setMessage('Failed to complete the return.');
                setMessageType('error');
            }
        } catch (error) {
            console.error('Error completing return:', error);
            setMessage('Error completing the return.');
            setMessageType('error');
        }
    };

    const handleReturnOptionChange = (option: 'inPerson' | 'driver') => {
        setReturnType(option);
        setRentalReturnId(null);
        setRentalReturnDetails(null);
    };

    const handleFetchReturnDetails = async () => {
        if (!rentalReturnId) return;

        if (returnType === 'inPerson') {
            await fetchRentalReturnDetailsInPerson(rentalReturnId);
        } else if (returnType === 'driver' && driverId) {
            await fetchRentalReturnDetailsByDriver(driverId);
        }
    };

    const handleCompleteReturn = async () => {
        if (!rentalReturnId) return;

        try {
            if (returnType === 'inPerson') {
                await completeInPersonReturn(rentalReturnId);
                setMessage('Zwrot zakończony pomyślnie.');
                setMessageType('success');
            } else if (returnType === 'driver') {
                await completeDriverReturn(rentalReturnId);
                setMessage('Zwrot przez kierowcę zakończony pomyślnie.');
                setMessageType('success');
            }
        } catch (error) {
            console.error('Błąd podczas zatwierdzania zwrotu:', error);
            setMessage('Nie udało się zakończyć zwrotu.');
            setMessageType('error');
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

                {/*{activeSection === 'settings' && (*/}
                {/*    <section className="p-5 rounded-md mb-[400px] h-[80%] max-h-[90%] w-[65%]">*/}
                {/*        <h2 className="text-center text-white">Ustawienia</h2>*/}
                {/*    </section>*/}
                {/*)}*/}
                <div className="bg-white p-9 rounded-2xl shadow-md h-[80%] max-h-[90%] w-[65%]">
                    <h3 className="text-3xl p-4 font-semibold mb-20 text-gray-600 text-center">Zwroty</h3>

                    <div className="mb-20">
                        <h4 className="text-2xl font-thin text-gray-700 mb-8">Wybierz rodzaj zwrotu:</h4>
                        <div className="flex justify-center space-x-8 w-full">
                            <button
                                onClick={() => handleReturnOptionChange('inPerson')}
                                className={`w-full py-3 px-6 rounded-lg ${returnType === 'inPerson' ? 'bg-[#4B6477] text-white' : 'bg-gray-200 text-gray-800'} sm:w-[75%] md:w-[70%] lg:w-[60%]`}
                            >
                                Zwrot osobiście
                            </button>

                            <button
                                onClick={() => handleReturnOptionChange('driver')}
                                className={`w-full py-3 px-6 rounded-lg ${returnType === 'driver' ? 'bg-[#4B6477] text-white' : 'bg-gray-200 text-gray-800'} sm:w-[75%] md:w-[70%] lg:w-[60%]`}
                            >
                                Zwrot przez kierowcę
                            </button>
                        </div>
                    </div>

                    <div className="border-t border-gray-300 my-8 mb-[8%]"></div>

                    <div className="mb-6">
                        <h4 className="text-2xl font-thin text-gray-700 mb-8">Wprowadź wymagane dane:</h4>
                        <div className="space-y-4">
                            <div>
                                <label htmlFor="rentalReturnId" className="block text-gray-600 font-medium mb-2">ID
                                    zwrotu</label>
                                <input
                                    id="rentalReturnId"
                                    type="text"
                                    placeholder="Wprowadź ID zwrotu"
                                    value={rentalReturnId || ''}
                                    onChange={(e) => setRentalReturnId(Number(e.target.value))}
                                    className="w-full p-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-[#3B576C]"
                                />
                            </div>

                            {returnType === 'driver' && (
                                <div>
                                    <label htmlFor="driverId" className="block text-gray-600 font-medium mb-2">ID
                                        kierowcy</label>
                                    <input
                                        id="driverId"
                                        type="text"
                                        placeholder="Wprowadź ID kierowcy"
                                        value={driverId || ''}
                                        onChange={(e) => setDriverId(e.target.value)}
                                        className="w-full p-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-[#3B576C]"
                                    />
                                </div>
                            )}

                            <div className="flex justify-center mt-4">
                                <button
                                    onClick={handleFetchReturnDetails}
                                    className="mb-6 mt-5 w-[15vw] py-2 px-4 bg-[#3B576C] text-white rounded-md cursor-pointer hover:bg-[#314757] duration-200 ease-out"
                                >
                                    Zwróć
                                </button>
                            </div>
                        </div>
                    </div>

                    {rentalReturnDetails && (
                        <div className="mt-6">
                            <h4 className="text-xl font-semibold text-gray-700 mb-4">Szczegóły zwrotu:</h4>

                            <div className="space-y-4">
                                {rentalReturnDetails.rentalReturnItems.map((item) => (
                                    <div key={item.id} className="border p-4 rounded shadow-sm bg-gray-100">
                                        <p>{item.book.authorNames.join(', ')}</p>
                                        <p><strong>{item.book.title}</strong></p>
                                        <p>Ilość: {item.returnedQuantity}</p>
                                        <p>ID książki: {item.book.id}</p>
                                        <p>ISBN: {item.book.isbn}</p>
                                    </div>
                                ))}
                            </div>

                            <div className="flex justify-center mt-6">
                                <button
                                    onClick={handleCompleteReturn}
                                    className="w-[15vw] py-2 px-4 bg-green-600 text-white rounded-md hover:bg-green-700"
                                >
                                    Zatwierdź zwrot
                                </button>
                            </div>
                        </div>
                    )}

                    {message && (
                        <div
                            className={`mt-4 p-4 rounded-md ${messageType === 'success' ? 'bg-green-100 text-[#3B576C]' : 'bg-red-100 text-red-800'}`}>
                            {message}
                        </div>
                    )}
                </div>

            </main>
        </div>
    );
};

export default LibrarianReturns;
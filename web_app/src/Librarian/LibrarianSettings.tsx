import React, { useState } from 'react';
import {Link, useNavigate} from 'react-router-dom';
import {useWebSocketNewOrderNotification} from './useWebSocketNewOrderNotification.tsx';
import {toast} from 'react-toastify';
import 'react-toastify/dist/ReactToastify.css';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL;

const LibrarianSettings: React.FC = () => {
    const [oldPassword, setOldPassword] = useState('');
    const [newPassword, setNewPassword] = useState('');
    const [repeatPassword, setRepeatPassword] = useState('');
    const [message, setMessage] = useState('');
    const [error, setError] = useState('');

    useWebSocketNewOrderNotification('librarian/orders/pending', () => {
        toast.info("Otrzymano nowe zamówienie!", {
            position: "bottom-right",
        });
        console.log("New order received!");
    });

    const handleChangePassword = async (e: React.FormEvent) => {
        e.preventDefault();
        setMessage('');
        setError('');

        if (newPassword !== repeatPassword) {
            setError('Hasła nie pasują do siebie.');
            return;
        }

        try {
            const token = localStorage.getItem('access_token');
            if (!token) throw new Error('Brak tokenu autoryzacyjnego.');

            const response = await fetch(`${API_BASE_URL}/api/users/change-password`, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${token}`,
                },
                body: JSON.stringify({
                    oldPassword,
                    newPassword,
                }),
            });

            const text = await response.text();

            if (!response.ok) {
                const errorData = text ? JSON.parse(text) : { message: 'Błąd zmiany hasła' };
                throw new Error(errorData.message);
            }

            setMessage('Hasło zostało zmienione.');
            setOldPassword('');
            setNewPassword('');
            setRepeatPassword('');
        } catch (err: unknown) {
            if (err instanceof Error) {
                setError(err.message);
            } else {
                setError('Wystąpił nieznany błąd.');
            }
        }
    };

    const navigate = useNavigate();

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
                    <form
                        onSubmit={handleChangePassword}
                        className="w-full"
                    >
                        <h2 className="text-3xl p-4 font-semibold mb-10 text-gray-600 text-center">Zmień hasło</h2>

                        <div className="mb-4">
                            <label className="block mb-1 text-gray-600 text-xl">Stare hasło:</label>
                            <input
                                type="password"
                                value={oldPassword}
                                onChange={(e) => setOldPassword(e.target.value)}
                                className="text-lg w-full p-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-[#3B576C]"
                                required
                            />
                        </div>

                        <div className="block mb-5 text-gray-600 text-lg">
                            <label className="block mb-1">Nowe hasło:</label>
                            <input
                                type="password"
                                value={newPassword}
                                onChange={(e) => setNewPassword(e.target.value)}
                                className="text-lg text-black w-full p-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-[#3B576C]"
                                required
                            />
                        </div>

                        <div className="block mb-5 text-gray-600 text-lg"> {/* ✅ NEW FIELD */}
                            <label className="block mb-1">Powtórz hasło:</label>
                            <input
                                type="password"
                                value={repeatPassword}
                                onChange={(e) => setRepeatPassword(e.target.value)}
                                className="text-lg text-black w-full p-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-[#3B576C]"
                                required
                            />
                        </div>

                        <div className="flex justify-center mt-4">
                            <button
                                type="submit"
                                className="mb-5 mt-5 w-[15vw] py-2 px-4 bg-[#3B576C] text-white rounded-md hover:bg-[#314757] ease-out duration-300"
                            >
                                Zmień hasło
                            </button>
                        </div>

                        {message && <p className="text-green-500 mt-4">{message}</p>}
                        {error && <p className="text-red-500 mt-4">{error}</p>}
                    </form>
                </div>
            </main>
        </div>
    );
};

export default LibrarianSettings;
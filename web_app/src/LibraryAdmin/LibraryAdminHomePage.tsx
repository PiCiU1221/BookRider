import React, { useEffect, useState } from 'react';
import {Link, useNavigate} from "react-router-dom";

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL;

interface Librarian {
    id: string;
    username: string;
    firstName: string;
    lastName: string;
}

const LibraryAdminHomePage: React.FC = () => {
    const [usernameSearch, setUsernameSearch] = useState('');
    const [librarians, setLibrarians] = useState<Librarian[]>([]);
    const [message, setMessage] = useState('');

    const navigate = useNavigate();

    useEffect(() => {
        const token = localStorage.getItem('access_token');
        const fetchAll = async () => {
            try {
                const response = await fetch(`${API_BASE_URL}/api/library-admins/librarians`, {
                    method: 'GET',
                    headers: {
                        Authorization: `Bearer ${token}`,
                    },
                });
                if (response.ok) {
                    const data = await response.json();
                    setLibrarians(data);
                    setMessage('');
                } else {
                    setMessage('Nie udało się pobrać listy bibliotekarzy.');
                }
            } catch (err) {
                console.error(err);
                setMessage('Error fetching librarian list');
            }
        };

        if (usernameSearch.trim() === '') {
            fetchAll();
        }
    }, [usernameSearch]);

    const fetchLibrarian = async () => {
        const token = localStorage.getItem('access_token');
        if (!usernameSearch.trim()) return; // prevent triggering on empty string

        try {
            const response = await fetch(`${API_BASE_URL}/api/library-admins/librarians?username=${usernameSearch}`, {
                method: 'GET',
                headers: {
                    Authorization: `Bearer ${token}`,
                },
            });

            if (response.ok) {
                const data = await response.json();
                setLibrarians(Array.isArray(data) ? data : [data]);
                setMessage('');
            } else {
                setLibrarians([]);
                setMessage('Nie znaleziono bibliotekarza.');
            }
        } catch (err) {
            console.error(err);
            setMessage('Error fetching librarian');
        }
    };

    const resetPassword = async (username: string) => {
        const token = localStorage.getItem('access_token');
        try {
            const res = await fetch(`${API_BASE_URL}/api/library-admins/librarians/reset-password/${username}`, {
                method: 'PATCH',
                headers: {
                    Authorization: `Bearer ${token}`,
                },
            });
            setMessage(res.ok ? 'Hasło bibliotekarza zresetowano pomyślnie.' : 'Nie udało się zresetować hasła bibliotekarza.');
        } catch (err) {
            console.error(err);
            setMessage('Error resetting password');
        }
    };

    const deleteLibrarian = async (username: string) => {
        const token = localStorage.getItem('access_token');
        try {
            const res = await fetch(`${API_BASE_URL}/api/library-admins/librarians/${username}`, {
                method: 'DELETE',
                headers: {
                    Authorization: `Bearer ${token}`,
                },
            });
            if (res.ok) {
                setLibrarians(librarians.filter(lib => lib.username !== username));
                setMessage('Usunięto bibliotekarza.');
            } else {
                setMessage('Nie udało się usunąć bibliotekarza.');
            }
        } catch (err) {
            console.error(err);
            setMessage('Error deleting librarian');
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
                        className="relative w-[45vw] h-auto object-cover left-[1%]"
                        alt="Book Rider Logo"
                        src="/book-rider-high-resolution-logo.png"
                    />
                </div>
                {[
                    {id: 'librarian_search', label: 'Bibliotekarze', path: '/library-admin-dashboard'},
                    {id: 'add_librarians', label: 'Dodaj', path: '/library-admin-add-librarian'},
                    {id: 'settings', label: 'Ustawienia', path: '/library-admin-settings'},
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
            <main className="flex justify-center items-center p-9">
                <section className="h-[80%] max-h-[90%] p-9 rounded-2xl mb-[400px] w-[65%] bg-white text-gray-600">
                    <h2 className="text-center p-4 mb-4 text-3xl font-semibold">Wyszukaj bibliotekarza</h2>
                    <div className="grid gap-4">
                        <div className="relative">
                            <div className="mb-8">
                                <div className="flex gap-2 w-[58vw] relative">
                                    <input
                                        type="text"
                                        placeholder="Nazwa użytkownika"
                                        value={usernameSearch}
                                        onChange={(e) => setUsernameSearch(e.target.value)}
                                        className="border border-gray-300 rounded p-2 w-full pr-10"
                                    />
                                    {usernameSearch && (
                                        <button
                                            onClick={() => setUsernameSearch('')}
                                            className="absolute right-28 top-1/2 transform -translate-y-1/2 text-gray-500 hover:text-gray-700 px-2"
                                        >
                                            ✕
                                        </button>
                                    )}
                                    <button
                                        onClick={fetchLibrarian}
                                        className="bg-[#3B576C] text-white px-4 py-2 rounded-md whitespace-nowrap"
                                    >
                                        Szukaj
                                    </button>
                                </div>
                            </div>

                            {librarians.length > 0 && (
                                <div className="mb-6">
                                    <h3 className="text-xl font-semibold mb-2">Bibliotekarze w Twojej bibliotece:</h3>
                                    <ul className="space-y-4">
                                        {librarians.map(lib => (
                                            <li key={lib.id}
                                                className="border border-gray-300 rounded p-4 flex justify-between items-center">
                                                <div>
                                                    <p className="font-semibold text-lg mb-3">{lib.firstName} {lib.lastName}</p>
                                                    <p className="text-gray-600">Nazwa użytkownika: <br /> {lib.username}</p>
                                                </div>
                                                <div className="flex gap-2">
                                                    <button
                                                        onClick={() => resetPassword(lib.username)}
                                                        className="bg-[#5B7F9A] text-white px-9 py-2 rounded-md"
                                                    >
                                                        Zresetuj hasło
                                                    </button>
                                                    <button
                                                        onClick={() => deleteLibrarian(lib.username)}
                                                        className="bg-red-600 text-white px-8 py-2 rounded-md"
                                                    >
                                                        Usuń
                                                    </button>
                                                </div>
                                            </li>
                                        ))}
                                    </ul>
                                </div>
                            )}

                            {message && <p className="text-sm text-gray-700 mt-4">{message}</p>}
                        </div>
                    </div>
                </section>
            </main>
        </div>
    );
};

export default LibraryAdminHomePage;

import React from 'react';
import {Link, useNavigate} from 'react-router-dom';

// const API_BASE_URL = import.meta.env.VITE_API_BASE_URL;

const LibraryAdminSettings: React.FC = () => {
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
            <main className="flex justify-center items-center p-9 w-full max-w-[800vw]">
                <section className="h-[80%] max-h-[90%] p-9 rounded-2xl mb-[400px] w-[65%] bg-white text-gray-600">
                    <h2 className="text-center p-4 mb-4 text-3xl font-semibold">Ustawienia</h2>
                </section>
            </main>
        </div>
    );
};
export default LibraryAdminSettings;
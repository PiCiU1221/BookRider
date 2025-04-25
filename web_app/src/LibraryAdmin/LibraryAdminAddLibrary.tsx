import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL;

const LibraryAdminAddLibrary: React.FC = () => {
    const navigate = useNavigate();

    interface FormData {
        libraryName: string;
        addressLine: string;
        city: string;
        postalCode: string;
        phoneNumber: string;
        emailAddress: string;
    }

    const [formData, setFormData] = useState<FormData>({
        libraryName: '',
        addressLine: '',
        city: '',
        postalCode: '',
        phoneNumber: '',
        emailAddress: '',
    });

    const handleLogout = () => {
        localStorage.removeItem('access_token');
        localStorage.removeItem('role');

        navigate('/');
    };

    const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        const { name, value } = e.target;
        setFormData({ ...formData, [name]: value });
    };

    const handleSubmit = async (e: React.FormEvent<HTMLFormElement>) => {
        e.preventDefault();

        const requestBody = {
            street: formData.addressLine,
            city: formData.city,
            postalCode: formData.postalCode.toString(),
            libraryName: formData.libraryName,
            phoneNumber: formData.phoneNumber,
            libraryEmail: formData.emailAddress,
        };

        const token = localStorage.getItem('access_token');

        try {
            const response = await fetch(`${API_BASE_URL}/api/library-requests`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${token}`,
                },
                body: JSON.stringify(requestBody),
            });

            if (response.ok) {
                navigate('/processing-info');
            } else {
                console.error('Error submitting request', response.statusText);
            }
        } catch (error) {
            console.error('Error:', error);
        }
    };

    const handleSettings = () => {
        alert('Ustawienia');
    };

    return (
        <div className="bg-[#3B576C] min-h-screen">
            <header
                className="flex justify-between items-center w-screen bg-[#3B576C] text-white sticky top-0 z-50 shadow-md">
                <div>
                    <img
                        className="relative w-[7%] h-auto object-cover left-[1%]"
                        alt="Book Rider Logo"
                        src="/book-rider-high-resolution-logo.png"
                    />
                </div>
            <button
                onClick={handleSettings}
                className="relative mr-3 px-6 py-3 right-[1%] w-[8%] bg-[#314757] rounded-md text-sm transition-all duration-300 hover:bg-[#4b6477] flex items-center justify-center"
            >
                Ustawienia
            </button>
            <button
                onClick={handleLogout}
                className="relative py-3 right-[1%] w-[13%] bg-[#314757] rounded-md text-sm transition-all duration-300 hover:bg-[#4b6477]"
            >
                Wyloguj się
            </button>
        </header>

    <main className="p-10 max-w-3xl mx-auto">
        <section className="bg-white p-8 rounded-md shadow-lg">
            <h2 className="text-xl font-semibold text-[#314757] text-center mb-6">
                Złóż podanie o dodanie Twojej biblioteki do systemu BookRider
            </h2>
            <form onSubmit={handleSubmit}>
                {[
                    {label: 'Nazwa biblioteki:', name: 'libraryName', maxLength: 70, required: true },
                            { label: 'Ulica i nr budynku:', name: 'addressLine', maxLength: 70, required: true },
                            { label: 'Miasto:', name: 'city', maxLength: 50, required: true },
                            { label: 'Kod pocztowy:', name: 'postalCode', maxLength: 6, required: true, pattern: '\\d{2}-\\d{3}', title: 'XX-XXX' },
                            { label: 'Numer telefonu:', name: 'phoneNumber', maxLength: 9, required: true, pattern: '\\d{9}', title: 'Podaj prawidłowy numer telefonu bez numeru kierunkowego' },
                            { label: 'Adres e-mail:', name: 'emailAddress', maxLength: 50, required: true, type: 'email' },
                        ].map((field, index) => (
                            <div key={index} className="mb-4">
                                <label htmlFor={field.name} className="block text-sm font-medium text-gray-700">
                                    {field.label}
                                </label>
                                <input
                                    type={field.type || 'text'}
                                    id={field.name}
                                    name={field.name}
                                    value={formData[field.name as keyof FormData]}
                                    onChange={handleChange}
                                    className="mt-2 block w-full px-4 py-2 border border-gray-300 rounded-md text-gray-700 bg-gray-50 focus:outline-none focus:ring-2 focus:ring-[#3B576C]"
                                    maxLength={field.maxLength}
                                    required={field.required}
                                    pattern={field.pattern}
                                    title={field.title}
                                />
                            </div>
                        ))}
                        <div className="flex justify-center mt-8">
                            <button
                                type="submit"
                                className="py-3 px-0 border-none w-[22%] rounded-lg bg-[#3B576C] text-white text-lg cursor-pointer transition-all duration-300 hover:bg-[#314757]"
                            >
                                Złóż podanie
                            </button>
                        </div>
                    </form>
                </section>
            </main>
        </div>
    );
};

export default LibraryAdminAddLibrary;

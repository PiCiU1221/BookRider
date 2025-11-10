import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL;

interface FormData {
    firstName: string;
    lastName: string;
    email: string;
    password: string;
    confirm_password: string;
}

const RegistrationForm: React.FC = () => {
    const [formData, setFormData] = useState<FormData>({
        firstName: '',
        lastName: '',
        email: '',
        password: '',
        confirm_password: '',
    });

    const [error, setError] = useState<string | null>(null);
    const navigate = useNavigate();

    const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>): void => {
        const { name, value } = e.target;
        setFormData((prev) => ({ ...prev, [name]: value }));
    };

    const isPasswordSafe = (password: string): boolean => {
        const passwordRegex = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&#])[A-Za-z\d@$!%*?&#]{12,}$/;
        return passwordRegex.test(password);
    };

    const handleSubmit = async (e: React.FormEvent<HTMLFormElement>): Promise<void> => {
        e.preventDefault();

        if (formData.password !== formData.confirm_password) {
            setError('Hasła nie pasują do siebie.');
            return;
        }

        if (!isPasswordSafe(formData.password)) {
            setError(
                'Hasło powinno mieć co najmniej 12 znaków, w tym wielkie i małe litery, cyfry i znaki specjalne.'
            );
            return;
        }

        setError(null);

        const requestBody = {
            email: formData.email,
            firstName: formData.firstName,
            lastName: formData.lastName,
            password: formData.password,
        };

        try {
            const response = await fetch(`${API_BASE_URL}/api/auth/register/library_administrator`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(requestBody),
            });

            console.log(response.status);

            if (!response.ok) {
                throw new Error('Registration failed');
            }

            navigate('/library-admin-login');
        } catch (error) {
            setError('Podczas rejestracji nastąpił błąd.');
            console.error('Error:', error);
        }
    };


    return (
        <div className="bg-[#3b576c] flex flex-row justify-center w-full h-[57vw]">

            {/* Top Bar */}
                    <button>
                        <Link to="/">
                            <img
                                className="absolute w-[10%] h-[13%] top-[0%] left-[8%] object-cover"
                                alt="Book Rider Logo"
                                src="/book-rider-high-resolution-logo.png"
                            />
                        </Link>
                    </button>

                    <div className="absolute w-[17%] top-[5%] left-[18%]">
                        <Link to="/system-admin-login">
                            <button
                                className="w-full hover:text-[#2D343A] transition-all duration-[0.3s] font-normal text-white text-[1.2vw] text-center tracking-[0] leading-[normal]">
                                Administrator systemów
                            </button>
                        </Link>
                    </div>

                    <div className="absolute w-[15%] top-[5%] left-[35%]">
                        <Link to="/library-admin-login">
                            <button
                                className="w-full hover:text-[#2D343A] transition-all duration-[0.3s] ease-[ease] font-normal text-white text-[1.2vw] text-center tracking-[0] leading-[normal]">
                                Administrator biblioteki
                            </button>
                        </Link>
                    </div>

                    <div className="absolute w-[15%] top-[5%] left-[48%]">
                        <Link to="/librarian-login">
                            <button
                                className="w-full hover:text-[#2D343A] transition-all duration-[0.3s] ease-[ease] font-normal text-white text-[1.2vw] text-center tracking-[0] leading-[normal]">
                                Bibliotekarz
                            </button>
                        </Link>
                    </div>

                    <div className="absolute w-[20%] top-[5%] left-[57%]">
                        <Link to="/legal-info">
                            <button
                                className="w-full hover:text-[#2D343A] transition-all duration-[0.3s] font-normal text-white text-[1.2vw] text-center tracking-[0] leading-[normal]">
                                Informacje prawne
                            </button>
                        </Link>
                    </div>

                    <div className="absolute w-[12%] top-[5%] left-[71%]">
                        <Link to="/contact">
                            <button
                                className="w-full hover:text-[#2D343A] transition-all duration-[0.3s] font-normal text-white text-[1.2vw] text-center tracking-[0] leading-[normal]">
                                Kontakt
                            </button>
                        </Link>
                    </div>

            <div className="absolute max-w-lg p-10 top-[8vw] bg-white rounded-lg shadow-lg w-full h-fit">
                <h2 className="text-center text-2xl font-bold text-gray-800 mb-5">Rejestracja <br /> administratora biblioteki</h2>
                <form onSubmit={handleSubmit} className="flex flex-col gap-4">
                    <div>
                        <label htmlFor="firstName" className="block text-gray-600 mb-2">Imię:</label>
                        <input
                            type="text"
                            id="firstName"
                            name="firstName"
                            value={formData.firstName}
                            onChange={handleInputChange}
                            maxLength={25}
                            required
                            className="bg-gray-100 w-full p-3 rounded-md text-gray-800 border border-gray-300 focus:outline-none focus:ring-2 focus:ring-[#3B576C]"
                        />
                    </div>
                    <div>
                        <label htmlFor="lastName" className="block text-gray-600 mb-2">Nazwisko:</label>
                        <input
                            type="text"
                            id="lastName"
                            name="lastName"
                            value={formData.lastName}
                            onChange={handleInputChange}
                            maxLength={25}
                            required
                            className="bg-gray-100 w-full p-3 rounded-md text-gray-800 border border-gray-300 focus:outline-none focus:ring-2 focus:ring-[#3B576C]"
                        />
                    </div>
                    <div>
                        <label htmlFor="email" className="block text-gray-600 mb-2">Adres email:</label>
                        <input
                            type="email"
                            id="email"
                            name="email"
                            value={formData.email}
                            onChange={handleInputChange}
                            maxLength={25}
                            required
                            className="bg-gray-100 w-full p-3 rounded-md text-gray-800 border border-gray-300 focus:outline-none focus:ring-2 focus:ring-[#3B576C]"
                        />
                    </div>
                    <div>
                        <label htmlFor="password" className="block text-gray-600 mb-2">Hasło:</label>
                        <input
                            type="password"
                            id="password"
                            name="password"
                            value={formData.password}
                            onChange={handleInputChange}
                            maxLength={25}
                            required
                            className="bg-gray-100 w-full p-3 rounded-md text-gray-800 border border-gray-300 focus:outline-none focus:ring-2 focus:ring-[#3B576C]"
                        />
                    </div>
                    <div>
                        <label htmlFor="confirm_password" className="block text-gray-600 mb-2">Powtórz hasło:</label>
                        <input
                            type="password"
                            id="confirm_password"
                            name="confirm_password"
                            value={formData.confirm_password}
                            onChange={handleInputChange}
                            maxLength={25}
                            required
                            className="bg-gray-100 w-full p-3 rounded-md text-gray-800 border border-gray-300 focus:outline-none focus:ring-2 focus:ring-[#3B576C]"
                        />
                    </div>

                    {error && <p className="text-red-500 text-sm">{error}</p>}

                    <button
                        type="submit"
                        className="mt-6 py-3 px-0 border-none rounded-lg bg-[#3B576C] text-white text-lg cursor-pointer transition-all duration-300 hover:bg-[#314757]"
                    >
                        Rejestracja
                    </button>
                </form>

                <div className="text-center mt-5">
                    <Link to="/library-admin-login">
                        <button
                            className="absolute text-base mb-8 mt-[-2%] left-[1%] py-3 px-8 w-full hover:text-[#7c92a3] transition-all duration-[0.3s] font-normal text-[#3B576C] text-[0.8vw] text-center tracking-[0] leading-[normal]">
                            Jesteś już zarejestrowany?
                        </button>
                    </Link>
                </div>
            </div>
        </div>
    );
};

export default RegistrationForm;

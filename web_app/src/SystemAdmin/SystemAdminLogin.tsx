import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL;

const SysAdminLogin: React.FC = () => {
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [error, setError] = useState<string | null>(null);
    const [role, setRole] = useState<string>('system_administrator');
    const [emailValid, setEmailValid] = useState(true);
    const navigate = useNavigate();

    const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        const { name, value } = e.target;

        if (name === 'email') {
            setEmail(value);

            const isValid = /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value);
            setEmailValid(isValid);

            if (!isValid) {
                setError('Wprowadź poprawny adres email');
            } else {
                setError(null);
            }
        } else if (name === 'password') {
            setPassword(value);
        } else if (name === 'role') {
            setRole(value);
        }
    };

    const handleSubmit = async (e: React.FormEvent<HTMLFormElement>) => {
        e.preventDefault();
        setError(null);

        if (!emailValid) {
            setError('Wprowadź poprawny adres email');
            return;
        }

        try {
            const response = await fetch(`${API_BASE_URL}/api/auth/login/${role}`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    identifier: email,
                    password,
                }),
            });

            if (response.ok) {
                const authHeader = response.headers.get('Authorization');
                if (authHeader) {
                    const token = authHeader.split(' ')[1];

                    localStorage.setItem('access_token', token);
                    localStorage.setItem('role', role);
                    localStorage.setItem('email', email);

                    navigate('/system-admin-dashboard');

                } else {
                    setError('Authorization header missing');
                }
            } else {
                const errorData = await response.json();
                switch (errorData.code) {
                    case 401:
                        setError('Nieprawidłowy email lub hasło.');
                        break;
                    case 500:
                        setError('Wewnętrzny błąd serwera. Spróbuj ponownie później.');
                        break;
                    case 400:
                        setError('Należy podać adres email oraz hasło.');
                        break;
                    default:
                        setError(errorData.message || 'Błąd logowania. Spróbuj ponownie.');
                        break;
                }
            }
        } catch (error) {
            console.error('Login error: ', error);
            setError('Podczas logowania wystąpił błąd');
        }
    };

    return (
        <div className="bg-[#3b576c] flex flex-row justify-center w-full h-screen overflow-hidden">

            {/* Top Bar */}
            <div className="bg-[#3b576c] w-screen h-screen">
                <div className="relative w-screen h-screen">
                    <div className="absolute w-screen h-screen top-0 left-0 bg-[#3b576c]"/>

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
                    <div style={formContainerStyle}>
                        <h2 style={headerStyle}>Logowanie <br /> administratora systemów</h2>
                        <form onSubmit={handleSubmit} style={formStyle}>
                            <div style={formGroupStyle}>
                                <label htmlFor="email" style={labelStyle}>Adres email:</label>
                                <input
                                    type="text"
                                    id="email"
                                    name="email"
                                    value={email}
                                    onChange={handleInputChange}
                                    required
                                    maxLength={25}
                                    className={`peer p-2 border rounded-md w-full ${!emailValid ? 'border-red-500' : 'border-gray-300'}`}
                                />
                            </div>

                            <div style={formGroupStyle}>
                                <label htmlFor="password" style={labelStyle}>Hasło:</label>
                                <input
                                    type="password"
                                    id="password"
                                    name="password"
                                    value={password}
                                    onChange={handleInputChange}
                                    required
                                    maxLength={25}
                                    className="peer p-2 border rounded-md w-full"
                                />
                            </div>

                            <div style={errorStyle} className="flex items-center gap-2">
                                {error && (
                                    <>
                                        <svg
                                            className="flex-shrink-0 w-4 h-4"
                                            aria-hidden="true"
                                            xmlns="http://www.w3.org/2000/svg"
                                            fill="currentColor"
                                            viewBox="0 0 20 20"
                                        >
                                            <path
                                                d="M10 .5a9.5 9.5 0 1 0 9.5 9.5A9.51 9.51 0 0 0 10 .5ZM9.5 4a1.5 1.5 0 1 1 0 3 1.5 1.5 0 0 1 0-3ZM12 15H8a1 1 0 0 1 0-2h1v-3H8a1 1 0 0 1 0-2h2a1 1 0 1 1 1 1v4h1a1 1 0 0 1 0 2Z"/>
                                        </svg>
                                        <p className="m-0">{error}</p>
                                    </>
                                )}
                            </div>

                            <button
                                type="submit"
                                className="py-3 px-0 border-none rounded-lg bg-[#3B576C] text-white text-lg cursor-pointer transition-all duration-300 hover:bg-[#314757]"
                            >
                                Logowanie
                            </button>
                        </form>
                    </div>
                </div>
            </div>
        </div>
    );
};

const formContainerStyle: React.CSSProperties = {
    backgroundColor: '#fff',
    padding: '40px',
    borderRadius: '8px',
    boxShadow: '0 4px 15px rgba(0, 0, 0, 0.3)',
    width: 'clamp(280px, 30vw, 400px)',
    height: 'clamp(430px, 48vh, 90vh)',
    position: 'absolute',
    top: '25%',
    left: '50%',
    transform: 'translateX(-50%)',
};

const headerStyle: React.CSSProperties = {
    textAlign: 'center',
    marginBottom: '25px',
    fontSize: '28px',
    fontWeight: '600',
    color: '#2c3e50',
    width: '120%',
    marginLeft: '-9%',
};

const formStyle = {
    display: 'flex',
    flexDirection: 'column' as const,
};

const formGroupStyle = {
    marginBottom: '20px',
};

const labelStyle: React.CSSProperties = {
    fontSize: '16px',
    color: '#34495e',
    marginBottom: '8px',
};

const errorStyle: React.CSSProperties = {
    color: 'red',
    marginBottom: '2%',
    fontSize: '14px',
    marginTop: '-3%',
};

export default SysAdminLogin;

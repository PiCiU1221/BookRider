import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';

const LoginPage: React.FC = () => {
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const [error, setError] = useState<string | null>(null);
    const navigate = useNavigate();

    const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        const { name, value } = e.target;
        if (name === 'username') {
            setUsername(value);
        } else if (name === 'password') {
            setPassword(value);
        }
    };

    const setCookie = (name: string, value: string, days: number = 7): void => {
        const expires = new Date();
        expires.setTime(expires.getTime() + days * 24 * 60 * 60 * 1000); // Set expiration date
        document.cookie = `${name}=${value};expires=${expires.toUTCString()};path=/;secure;HttpOnly;SameSite=Strict`;
    };

    const handleSubmit = async (e: React.FormEvent<HTMLFormElement>) => {
        e.preventDefault();
        setError(null);

        try {
            const role = "library_administrator";

            const response = await fetch(`https://bookrider.onrender.com/api/auth/login/${role}`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    identifier: username,
                    password,
                }),
            });

            let headersString = '';
            response.headers.forEach((value, key) => {
                headersString += `${key}: ${value}\n`;
            });

            console.log('Response Headers:\n', headersString);

            if (response.ok) {
                const authHeader = response.headers.get('Authorization');
                if (authHeader) {
                    const token = authHeader.split(' ')[1];

                    setCookie('access_token', `${token}`);

                    const userRole = role;
                    localStorage.setItem('role', userRole);

                    if (userRole === 'library_administrator') {
                        navigate('/librarian-dashboard');
                    } else {
                        navigate('/dashboard');
                    }
                } else {
                    setError('Authorization header missing');
                }
            } else {
                const errorData = await response.text();
                setError(errorData || 'Login failed');
            }
        } catch (error) {
            console.error('Login error: ', error);
            setError('An error occurred while logging in');
        }
    };

    return (
        <div>
            <div style={formContainerStyle}>
                <h2 style={headerStyle}>Logowanie</h2>
                <form onSubmit={handleSubmit} style={formStyle}>
                    <div style={formGroupStyle}>
                        <label htmlFor="username" style={labelStyle}>Nazwa użytkownika:</label>
                        <input
                            type="email"
                            id="username"
                            name="username"
                            value={username}
                            onChange={handleInputChange}
                            required
                            style={inputStyle}
                            maxLength={25}
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
                            style={inputStyle}
                            maxLength={25}
                        />
                    </div>

                    {error && <p style={errorStyle}>{error}</p>}

                    <button
                        type="submit"
                        style={submitButtonStyle}
                    >
                        Logowanie
                    </button>
                </form>

                <div style={registerLinkStyle}>
                    <Link to="/register">
                        <button style={registerButtonStyle}>
                            Nie jesteś zarejestrowany?
                        </button>
                    </Link>
                </div>
            </div>
        </div>
    );
};

const formContainerStyle = {
    backgroundColor: '#fff',
    padding: '40px',
    borderRadius: '8px',
    boxShadow: '0 4px 15px rgba(0, 0, 0, 0.3)',
    width: '100%',
    maxWidth: '400px',
};

const headerStyle: React.CSSProperties = {
    textAlign: 'center',
    marginBottom: '25px',
    fontSize: '28px',
    fontWeight: '600',
    color: '#2c3e50',
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

const inputStyle: React.CSSProperties = {
    padding: '12px',
    borderRadius: '6px',
    border: '1px solid #ccc',
    fontSize: '16px',
    backgroundColor: '#ecf0f1',
    color: '#2c3e50',
    width: '100%',
    boxSizing: 'border-box' as const,
};

const errorStyle = {
    color: 'red',
    marginBottom: '20px',
};

const submitButtonStyle: React.CSSProperties = {
    padding: '12px 0',
    border: 'none',
    borderRadius: '6px',
    backgroundColor: '#3B576C',
    color: '#fff',
    fontSize: '16px',
    cursor: 'pointer',
    transition: 'background-color 0.3s',
};

const registerLinkStyle: React.CSSProperties = {
    textAlign: 'center',
    marginTop: '20px',
};

const registerButtonStyle: React.CSSProperties = {
    padding: '12px 30px',
    backgroundColor: '#fff',
    color: '#3B576C',
    border: 'none',
    borderRadius: '6px',
    cursor: 'pointer',
    fontSize: '16px',
    transition: 'background-color 0.3s',
};

export default LoginPage;

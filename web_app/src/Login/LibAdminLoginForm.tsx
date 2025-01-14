import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';

const LoginPage: React.FC = () => {
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [error, setError] = useState<string | null>(null);
    const [role, setRole] = useState<string>('library_administrator'); // Role to be set correctly using radio button input
    const navigate = useNavigate();

    const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        const { name, value } = e.target;
        if (name === 'email') {
            setEmail(value);
        } else if (name === 'password') {
            setPassword(value);
        } else if (name === 'role') {
            setRole(value);
        }
    };

    const handleSubmit = async (e: React.FormEvent<HTMLFormElement>) => {
        e.preventDefault();
        setError(null);

        try {
            const response = await fetch(`https://bookrider.onrender.com/api/auth/login/${role}`, {
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

                    // Navigation on the basis of the user's role
                    if (role === 'library_administrator') {
                        navigate('/add-library');
                    } else if (role === 'librarian') {
                        navigate('/librarian-dashboard');
                    } else if (role === 'system_administrator') {
                        navigate('/sys-admin-dashboard');
                    } else {
                        navigate('/login');
                    }
                } else {
                    setError('Authorization header missing');
                }
            } else {
                const errorData = await response.json();
                switch (errorData.code) {
                    case 401:
                        setError('Nieprawidłowy email, hasło lub rola.');
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
        <div>
            <div style={formContainerStyle}>
                <h2 style={headerStyle}>Logowanie</h2>
                <form onSubmit={handleSubmit} style={formStyle}>
                    <div style={formGroupStyle}>
                        <label htmlFor="email" style={labelStyle}>Adres email:</label>
                        <input
                            type="email"
                            id="email"
                            name="email"
                            value={email}
                            onChange={handleInputChange}
                            required
                            style={inputStyle}
                            maxLength={25}
                        />
                    </div>

                    <div style={formGroupStyle}>
                        <label htmlFor="password" style={radioLabel}>Hasło:</label>
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
                    <div style={radioGroupStyle}>
                        <label style={labelStyle}>Jestem:</label>
                        <div>
                            <label style={radioStyle}>
                                <input
                                    type="radio"
                                    name="role"
                                    value="library_administrator"
                                    checked={role === 'library_administrator'}
                                    onChange={handleInputChange}
                                />
                                administratorem biblioteki
                            </label>
                            <label style={radioStyle}>
                                <input
                                    type="radio"
                                    name="role"
                                    value="librarian"
                                    checked={role === 'librarian'}
                                    onChange={handleInputChange}
                                />
                                bibliotekarzem
                            </label>
                            <label style={radioStyle}>
                                <input
                                    type="radio"
                                    name="role"
                                    value="system_administrator"
                                    checked={role === 'system_administrator'}
                                    onChange={handleInputChange}
                                />
                                administratorem systemów
                            </label>
                        </div>
                    </div>
                    <div style={errorStyle}>
                    {error && <p>{error}</p>}
                    </div>
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
    alignItems: 'center',
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

const radioLabel: React.CSSProperties = {
    color: '#3B576C',
};

const radioStyle: React.CSSProperties = {
    color: '#3B576C',
    display: 'flex',
    gap: '10px',
    marginBottom: '7px',
};

const radioGroupStyle: React.CSSProperties = {
    marginBottom: '5%',
    marginTop: '5%',
};

export default LoginPage;

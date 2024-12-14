import React, { useState } from 'react';
import { Link } from 'react-router-dom';

const LoginPage: React.FC = () => {
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const [error, setError] = useState<string | null>(null);

    // Handle input field changes for username and password
    const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        const { name, value } = e.target;
        if (name === 'username') {
            setUsername(value);
        } else if (name === 'password') {
            setPassword(value);
        }
    };

    // Handle form submission
    const handleSubmit = async (e: React.FormEvent<HTMLFormElement>) => {
        e.preventDefault();

        // Reset previous error messages
        setError(null);

    };

    return (
        <div style={{maxWidth: '400px', margin: 'auto', padding: '20px'}}>
            <h2 style={{display: 'flex', justifyContent: 'center', alignItems: 'center', marginBottom: '70px' }}>Logowanie</h2>
            <form onSubmit={handleSubmit}>
                <div style={{marginBottom: '10px'}}>
                    <label htmlFor="username" style={{display: 'block', marginBottom: '5px'}}>
                        Nazwa użytkownika:
                    </label>
                    <input
                        type="email"
                        id="username"
                        name="username"
                        value={username}
                        onChange={handleInputChange}
                        maxLength={25}
                        required
                        style={{backgroundColor: '#314757', width: '100%', padding: '8px', boxSizing: 'border-box'}}
                    />
                </div>
                <div style={{marginBottom: '10px'}}>
                    <label htmlFor="password" style={{display: 'block', marginBottom: '5px'}}>
                        Hasło:
                    </label>
                    <input
                        type="password"
                        id="password"
                        name="password"
                        value={password}
                        onChange={handleInputChange}
                        maxLength={25}
                        required
                        style={{backgroundColor: '#314757', width: '100%', padding: '8px', boxSizing: 'border-box'}}
                    />
                </div>
                {error && <p style={{color: 'red', marginBottom: '10px'}}>{error}</p>}
                <button
                    type="submit"
                    style={{
                        width: '100%',
                        marginTop: '60px',
                        padding: '10px',
                        backgroundColor: '#2d343a',
                        color: '#fff',
                        border: 'none',
                        borderRadius: '4px',
                        cursor: 'pointer',
                    }}
                >
                    Logowanie
                </button>
            </form>

            {/* Navigation */}
            <div style={{marginTop: '20px', textAlign: 'center'}}>
                <Link to="/register">
                    <button
                        style={{backgroundColor: '#3B576C', color: '#f7ca65', margin: '10px', padding: '10px 20px'}}>Nie
                        jesteś zarejestrowany?
                    </button>
                </Link>
            </div>
        </div>
    );
};

export default LoginPage;

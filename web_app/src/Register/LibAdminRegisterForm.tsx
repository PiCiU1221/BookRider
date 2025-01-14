import React, { useState } from 'react';
import { Link } from 'react-router-dom';

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

        const finalFormData = {
            ...formData,
            role: 'library_administrator'
        };

        console.log('Form Data:', finalFormData);

        try {
            const response = await fetch(`https://bookrider.onrender.com/api/auth/register/library_administrator`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(finalFormData),
            });

            console.log(response.status);

            if (!response.ok) {
                throw new Error('Registration failed');
            }

            console.log('Registration successful');
        } catch (error) {
            setError('An error occurred while registering the account.');
            console.error('Registration Error:', error);
        }
    };

    return (
        <div style={containerStyle}>
            <h2 style={headingStyle}>Rejestracja</h2>
            <form onSubmit={handleSubmit} style={formStyle}>
                <div style={inputContainerStyle}>
                    <label htmlFor="firstName" style={labelStyle}>Imię:</label>
                    <input
                        type="text"
                        id="firstName"
                        name="firstName"
                        value={formData.firstName}
                        onChange={handleInputChange}
                        maxLength={25}
                        required
                        style={inputStyle}
                    />
                </div>
                <div style={inputContainerStyle}>
                    <label htmlFor="lastName" style={labelStyle}>Nazwisko:</label>
                    <input
                        type="text"
                        id="lastName"
                        name="lastName"
                        value={formData.lastName}
                        onChange={handleInputChange}
                        maxLength={25}
                        required
                        style={inputStyle}
                    />
                </div>
                <div style={inputContainerStyle}>
                    <label htmlFor="email" style={labelStyle}>Adres email:</label>
                    <input
                        type="email"
                        id="email"
                        name="email"
                        value={formData.email}
                        onChange={handleInputChange}
                        maxLength={25}
                        required
                        style={inputStyle}
                    />
                </div>
                <div style={inputContainerStyle}>
                    <label htmlFor="password" style={labelStyle}>Hasło:</label>
                    <input
                        type="password"
                        id="password"
                        name="password"
                        value={formData.password}
                        onChange={handleInputChange}
                        maxLength={25}
                        required
                        style={inputStyle}
                    />
                </div>
                <div style={inputContainerStyle}>
                    <label htmlFor="confirm_password" style={labelStyle}>Powtórz hasło:</label>
                    <input
                        type="password"
                        id="confirm_password"
                        name="confirm_password"
                        value={formData.confirm_password}
                        onChange={handleInputChange}
                        maxLength={25}
                        required
                        style={inputStyle}
                    />
                </div>

                {error && <p style={errorStyle}>{error}</p>}

                <button
                    type="submit"
                    style={submitButtonStyle}
                >
                    Rejestracja
                </button>
            </form>

            <div style={navigationStyle}>
                <Link to="/login">
                    <button style={navButtonStyle}>Jesteś już zarejestrowany?</button>
                </Link>
            </div>
        </div>
    );
};

const containerStyle: React.CSSProperties = {
    maxWidth: '550px',
    padding: '40px',
    backgroundColor: 'white',
    borderRadius: '8px',
    boxShadow: '0 4px 8px rgba(0, 0, 0, 0.3)',
    width: "150%",
    justifyContent: 'space-between',
};

const headingStyle: React.CSSProperties = {
    textAlign: 'center',
    color: '#2c3e50',
    marginBottom: '20px',
    fontSize: '28px',
    fontWeight: 'bold',
};

const formStyle: React.CSSProperties = {
    display: 'flex',
    flexDirection: 'column',
};

const inputContainerStyle: React.CSSProperties = {
    marginBottom: '15px',
};

const labelStyle: React.CSSProperties = {
    display: 'block',
    color: '#555',
    marginBottom: '5px',
    fontSize: '16px',
};

const inputStyle: React.CSSProperties = {
    backgroundColor: '#f9f9f9',
    width: '95%',
    padding: '12px',
    borderRadius: '4px',
    color: '#333',
    border: '1px solid #ccc',
    fontSize: '14px',
    transition: 'border 0.3s',
};

const errorStyle: React.CSSProperties = {
    color: 'red',
    marginBottom: '15px',
    fontSize: '14px',
};

const submitButtonStyle: React.CSSProperties = {
    width: '40%',
    padding: '12px',
    backgroundColor: '#3B576C',
    color: 'white',
    border: 'none',
    borderRadius: '4px',
    cursor: 'pointer',
    marginTop: '15px',
    fontSize: '16px',
    alignSelf: 'center',
};

const navigationStyle: React.CSSProperties = {
    marginTop: '20px',
    textAlign: 'center',
};

const navButtonStyle: React.CSSProperties = {
    color: '#3B576C',
    margin: '3px',
    borderRadius: '4px',
    fontSize: '16px',
    cursor: 'pointer',
    backgroundColor: '#fff',
};

export default RegistrationForm;

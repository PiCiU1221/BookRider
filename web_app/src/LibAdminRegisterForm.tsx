import React, { useState } from 'react';
import { Link } from 'react-router-dom';

interface FormData {
    firstName: string;
    lastName: string;
    username: string;
    email: string;
    password: string;
    confirm_password: string;
    library_address: string;
}

const RegistrationForm: React.FC = () => {
    const [formData, setFormData] = useState<FormData>({
        firstName: '',
        lastName: '',
        username: '',
        email: '',
        password: '',
        confirm_password: '',
        library_address: '',
    });

    const [addressLines, setAddressLines] = useState({
        line1: '',
        line2: '',
    });

    const [error, setError] = useState<string | null>(null);

    const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>): void => {
        const { name, value } = e.target;

        if (name === 'line1' || name === 'line2') {
            // Update address lines
            setAddressLines((prev) => ({ ...prev, [name]: value }));
        } else {
            // Update other form fields
            setFormData((prev) => ({ ...prev, [name]: value }));
        }
    };

    const isPasswordSafe = (password: string): boolean => {
        const passwordRegex = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&#])[A-Za-z\d@$!%*?&#]{12,}$/;
        return passwordRegex.test(password);
    };

    const handleSubmit = (e: React.FormEvent<HTMLFormElement>): void => {
        e.preventDefault();

        // Check if username is at least 10 characters long
        if (formData.username.length < 10) {
            setError('Nazwa użytkownika musi mieć co najmniej 10 znaków.');
            return;
        }

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

        const combinedAddress = `${addressLines.line1}, ${addressLines.line2}`.trim();

        const finalFormData = { ...formData, library_address: combinedAddress };
        console.log('Form Data:', finalFormData);
        // Add form submission logic (e.g., API call) here
    };

    return (
        <div style={{ maxWidth: '400px', margin: 'auto', padding: '20px' }}>
            <h2>Rejestracja</h2>
            <form onSubmit={handleSubmit}>
                <div style={{marginBottom: '10px'}}>
                    <label htmlFor="firstName" style={{display: 'block', marginBottom: '5px'}}>
                        Imię:
                    </label>
                    <input
                        type="text"
                        id="firstName"
                        name="firstName"
                        value={formData.firstName}
                        onChange={handleInputChange}
                        required
                        style={{width: '100%', padding: '8px', boxSizing: 'border-box'}}
                    />
                </div>
                <div style={{marginBottom: '10px'}}>
                    <label htmlFor="lastName" style={{display: 'block', marginBottom: '5px'}}>
                        Nazwisko:
                    </label>
                    <input
                        type="text"
                        id="lastName"
                        name="lastName"
                        value={formData.lastName}
                        onChange={handleInputChange}
                        required
                        style={{width: '100%', padding: '8px', boxSizing: 'border-box'}}
                    />
                </div>
                <div style={{marginBottom: '10px'}}>
                    <label htmlFor="username" style={{display: 'block', marginBottom: '5px'}}>
                        Nazwa użytkownika:
                    </label>
                    <input
                        type="text"
                        id="username"
                        name="username"
                        value={formData.username}
                        onChange={handleInputChange}
                        required
                        style={{width: '100%', padding: '8px', boxSizing: 'border-box'}}
                    />
                </div>
                <div style={{marginBottom: '10px'}}>
                    <label htmlFor="email" style={{display: 'block', marginBottom: '5px'}}>
                        Adres email:
                    </label>
                    <input
                        type="email"
                        id="email"
                        name="email"
                        value={formData.email}
                        onChange={handleInputChange}
                        required
                        style={{width: '100%', padding: '8px', boxSizing: 'border-box'}}
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
                        value={formData.password}
                        onChange={handleInputChange}
                        required
                        style={{width: '100%', padding: '8px', boxSizing: 'border-box'}}
                    />
                </div>
                <div style={{marginBottom: '10px'}}>
                    <label htmlFor="confirmPassword" style={{display: 'block', marginBottom: '5px'}}>
                        Powtórz hasło:
                    </label>
                    <input
                        type="password"
                        id="confirmPassword"
                        name="confirmPassword"
                        value={formData.confirm_password}
                        onChange={handleInputChange}
                        required
                        style={{width: '100%', padding: '8px', boxSizing: 'border-box'}}
                    />
                </div>
                <div style={{marginBottom: '10px'}}>
                    <label htmlFor="line1" style={{display: 'block', marginBottom: '5px'}}>
                        Adres biblioteki linia 1:
                    </label>
                    <input
                        type="text"
                        id="line1"
                        name="line1"
                        value={addressLines.line1}
                        onChange={handleInputChange}
                        required
                        style={{width: '100%', padding: '8px', boxSizing: 'border-box'}}
                    />
                </div>
                <div style={{marginBottom: '10px'}}>
                    <label htmlFor="line2" style={{display: 'block', marginBottom: '5px'}}>
                        Adres biblioteki linia 2:
                    </label>
                    <input
                        type="text"
                        id="line2"
                        name="line2"
                        value={addressLines.line2}
                        onChange={handleInputChange}
                        style={{width: '100%', padding: '8px', boxSizing: 'border-box'}}
                    />
                </div>
                {error && (
                    <p style={{color: 'red', marginBottom: '10px'}}>{error}</p>
                )}
                <button
                    type="submit"
                    style={{
                        width: '100%',
                        padding: '10px',
                        backgroundColor: '#007BFF',
                        color: '#fff',
                        border: 'none',
                        borderRadius: '4px',
                        cursor: 'pointer',
                    }}
                >
                    Rejestracja
                </button>
            </form>

            {/* Navigation */}
            <div style={{marginTop: '20px', textAlign: 'center'}}>
                <Link to="/login">
                    <button style={{margin: '10px', padding: '10px 20px'}}>Logowanie</button>
                </Link>
            </div>
        </div>
    );
};

export default RegistrationForm;

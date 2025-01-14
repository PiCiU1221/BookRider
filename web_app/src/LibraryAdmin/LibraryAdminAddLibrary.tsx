import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';

const LibraryAdminAddLibrary: React.FC = () => {
    const navigate = useNavigate();

    const [formData, setFormData] = useState({
        libraryName: '',
        addressLine1: '',
        addressLine2: '',
        city: '',
        postalCode: '',
        phoneNumber: '',
        emailAddress: '',
    });

    const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        const { name, value } = e.target;
        setFormData({ ...formData, [name]: value });
    };

    const handleSubmit = async (e: React.FormEvent<HTMLFormElement>) => {
        e.preventDefault();

        const fullAddress = `${formData.addressLine1} ${formData.addressLine2}`;

        const requestBody = {
            street: fullAddress,
            city: formData.city,
            postalCode: formData.postalCode,
            libraryName: formData.libraryName,
            phoneNumber: formData.phoneNumber,
            libraryEmail: formData.emailAddress,
        };

        const token = localStorage.getItem('token');

        try {
            const response = await fetch('https://bookrider.onrender.com/api/library-requests', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${token}`,
                },
                body: JSON.stringify(requestBody),
            });

            if (response.ok) {
                console.log('Library addition request submitted successfully');
                navigate('/processing-info');
            } else {
                console.error('Error submitting request', response.statusText);
            }
        } catch (error) {
            console.error('Error during API call:', error);
        }
    };

    const handleSettings = () => {
        alert('Settings clicked');
    };

    const handleLogout = () => {
        alert('Logging out');
    };

    return (
        <div style={{ backgroundColor: "#f4f6f9", minHeight: '100vh', fontFamily: 'Arial, sans-serif' }}>
            <div
                style={{
                    display: 'flex',
                    justifyContent: 'space-between',
                    alignItems: 'center',
                    padding: '20px 20px',
                    backgroundColor: '#34495e',
                    color: '#fff',
                    position: 'sticky',
                    top: 0,
                    zIndex: 1100,
                    width: '97%',
                    marginLeft: '-6px',
                }}
            >
                <div style={{ fontWeight: 'bold', fontSize: '18px' }}>
                    Widok administratora biblioteki
                </div>
                <div>
                    <button
                        onClick={handleSettings}
                        style={{
                            marginRight: '15px',
                            padding: '12px 25px',
                            border: 'none',
                            borderRadius: '6px',
                            backgroundColor: '#2d343a',
                            color: '#fff',
                            cursor: 'pointer',
                            fontSize: '14px',
                            transition: 'background-color 0.3s',
                        }}
                    >
                        Ustawienia
                    </button>
                    <button
                        onClick={handleLogout}
                        style={{
                            padding: '12px 25px',
                            border: 'none',
                            borderRadius: '6px',
                            backgroundColor: '#2d343a',
                            color: '#fff',
                            cursor: 'pointer',
                            fontSize: '14px',
                            transition: 'background-color 0.3s',
                        }}
                    >
                        Wyloguj się
                    </button>
                </div>
            </div>

            <main style={{ padding: '40px', maxWidth: '900px', margin: 'auto' }}>
                <section style={{
                    padding: '30px',
                    backgroundColor: '#fff',
                    borderRadius: '8px',
                    boxShadow: '0 4px 15px rgba(0, 0, 0, 0.1)',
                }}>
                    <h2 style={{
                        color: '#2c3e50',
                        textAlign: 'center',
                        marginBottom: '20px',
                        fontSize: '28px',
                        fontWeight: '600'
                    }}>
                        Złóż podanie o dodanie Twojej biblioteki do systemu BookRider
                    </h2>
                    <form onSubmit={handleSubmit}>
                        <div style={formGroupStyle}>
                            <label htmlFor="libraryName" style={labelStyle}>Nazwa biblioteki:</label>
                            <input
                                type="text"
                                id="libraryName"
                                name="libraryName"
                                value={formData.libraryName}
                                onChange={handleChange}
                                style={inputStyle}
                                maxLength={30}
                                required
                            />
                        </div>
                        <div style={formGroupStyle}>
                            <label htmlFor="addressLine1" style={labelStyle}>Adres biblioteki linia 1:</label>
                            <input
                                type="text"
                                id="addressLine1"
                                name="addressLine1"
                                value={formData.addressLine1}
                                onChange={handleChange}
                                style={inputStyle}
                                maxLength={25}
                                required
                            />
                        </div>
                        <div style={formGroupStyle}>
                            <label htmlFor="addressLine2" style={labelStyle}>Adres biblioteki linia 2:</label>
                            <input
                                type="text"
                                id="addressLine2"
                                name="addressLine2"
                                value={formData.addressLine2}
                                onChange={handleChange}
                                style={inputStyle}
                                maxLength={25}
                            />
                        </div>
                        <div style={formGroupStyle}>
                            <label htmlFor="city" style={labelStyle}>Miasto:</label>
                            <input
                                type="text"
                                id="city"
                                name="city"
                                value={formData.city}
                                onChange={handleChange}
                                style={inputStyle}
                                maxLength={25}
                                required
                            />
                        </div>
                        <div style={formGroupStyle}>
                            <label htmlFor="postalCode" style={labelStyle}>Kod pocztowy:</label>
                            <input
                                type="text"
                                id="postalCode"
                                name="postalCode"
                                value={formData.postalCode}
                                onChange={handleChange}
                                style={inputStyle}
                                maxLength={6}
                                pattern="\d{2}-\d{3}"
                                required
                                title="XX-XXX"
                            />
                        </div>
                        <div style={formGroupStyle}>
                            <label htmlFor="phoneNumber" style={labelStyle}>Numer telefonu:</label>
                            <input
                                type="tel"
                                id="phoneNumber"
                                name="phoneNumber"
                                value={formData.phoneNumber}
                                onChange={handleChange}
                                style={inputStyle}
                                maxLength={9}
                                pattern="\d{9}"
                                required
                                title="Podaj prawidłowy numer telefonu bez numeru kierunkowego"
                            />
                        </div>
                        <div style={formGroupStyle}>
                            <label htmlFor="emailAddress" style={labelStyle}>Adres e-mail:</label>
                            <input
                                type="email"
                                id="emailAddress"
                                name="emailAddress"
                                value={formData.emailAddress}
                                onChange={handleChange}
                                style={inputStyle}
                                maxLength={25}
                                required
                            />
                        </div>
                        <div style={{ display: 'flex', justifyContent: 'center', marginTop: '30px' }}>
                            <button
                                type="submit"
                                style={{
                                    padding: '12px 30px',
                                    border: 'none',
                                    borderRadius: '6px',
                                    backgroundColor: '#3B576C',
                                    color: '#fff',
                                    cursor: 'pointer',
                                    fontSize: '16px',
                                    transition: 'background-color 0.3s',
                                }}
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

const formGroupStyle = {
    marginBottom: '20px',
    display: 'flex',
    flexDirection: 'column' as const,
};

const inputStyle = {
    marginTop: '8px',
    padding: '12px',
    borderRadius: '6px',
    border: '1px solid #ccc',
    fontSize: '16px',
    backgroundColor: '#ecf0f1',
    color: '#2c3e50',
};

const labelStyle = {
    fontSize: '16px',
    color: '#34495e',
};

export default LibraryAdminAddLibrary;

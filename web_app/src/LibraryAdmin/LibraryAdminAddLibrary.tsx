import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';

const LibraryAdminAddLibrary: React.FC = () => {
    const navigate = useNavigate();
    const sysAdminUsername = 'admin123';

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

    const handleSubmit = (e: React.FormEvent<HTMLFormElement>) => {
        e.preventDefault();

        const fullAddress = `${formData.addressLine1} ${formData.addressLine2}`;

        console.log('Form submitted:', { ...formData, address: fullAddress });

        navigate('/processing-info');
    };

    const handleSettings = () => {
        alert('Settings clicked');
    };

    const handleLogout = () => {
        alert('Logging out');
    };

    return (
        <div style={{ backgroundColor: "#3B576C" }}>
            {/* Top Panel */}
            <div
                style={{
                    display: 'flex',
                    justifyContent: 'space-between',
                    alignItems: 'center',
                    padding: '10px 20px',
                    backgroundColor: '#314757',
                    color: '#fff',
                    position: 'sticky',
                    top: 0,
                    zIndex: 1100,
                    width: '90%',
                    marginLeft: '20px',
                }}
            >
                <div>
                    <strong>Witaj, {sysAdminUsername}</strong>
                </div>
                <div>
                    <button
                        onClick={handleSettings}
                        style={{
                            marginRight: '10px',
                            padding: '8px 16px',
                            border: 'none',
                            borderRadius: '4px',
                            backgroundColor: '#2d343a',
                            color: '#fff',
                            cursor: 'pointer',
                        }}
                    >
                        Ustawienia
                    </button>
                    <button
                        onClick={handleLogout}
                        style={{
                            padding: '8px 16px',
                            border: 'none',
                            borderRadius: '4px',
                            backgroundColor: '#2d343a',
                            color: '#fff',
                            cursor: 'pointer',
                        }}
                    >
                        Wyloguj się
                    </button>
                </div>
            </div>

            {/* Main Body */}
            <main style={{ padding: '20px', maxWidth: '800px', margin: 'auto' }}>
                <section style={{ padding: '20px', borderRadius: '4px', backgroundColor: '#38424a' }}>
                    <h2 style={{ color: '#fff', textAlign: "center" }}>Złóż podanie o dodanie Twojej biblioteki do systemu BookRider</h2>
                    <form onSubmit={handleSubmit} style={{marginTop: '20px', color: '#fff'}}>
                        <div style={formGroupStyle}>
                            <label htmlFor="libraryName">Nazwa biblioteki:</label>
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
                            <label htmlFor="addressLine1">Adres biblioteki linia 1:</label>
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
                            <label htmlFor="addressLine2">Adres biblioteki linia 2:</label>
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
                            <label htmlFor="city">Miasto:</label>
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
                            <label htmlFor="postalCode">Kod pocztowy:</label>
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
                            <label htmlFor="phoneNumber">Numer telefonu:</label>
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
                                title="Podaj prawidłowy number telefonu bez numeru kierunkowego"
                            />
                        </div>
                        <div style={formGroupStyle}>
                            <label htmlFor="emailAddress">Adres e-mail:</label>
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
                        <div style={{display: 'flex', justifyContent: 'center', marginTop: '20px'}}>
                            <button
                                type="submit"
                                style={{
                                    padding: '10px 20px',
                                    border: 'none',
                                    borderRadius: '4px',
                                    backgroundColor: '#2d343a',
                                    color: '#fff',
                                    cursor: 'pointer',
                                    marginTop: '10px',
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
    marginBottom: '15px',
    display: 'flex',
    flexDirection: 'column' as const,
};

const inputStyle = {
    marginTop: '5px',
    padding: '10px',
    borderRadius: '4px',
    border: '1px solid #ccc',
    fontSize: '14px',
    backgroundColor: '#4f5d69',
};

export default LibraryAdminAddLibrary;

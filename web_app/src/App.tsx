import React from 'react';
import { BrowserRouter as Router, Route, Routes, Link } from 'react-router-dom';
import RegistrationForm from './LibAdminRegisterForm.tsx';
import LoginPage from './LibAdminLoginForm.tsx';

const App: React.FC = () => {
    return (
        <Router>
            <div>
                <img
                    src="/book-rider-high-resolution-logo.png"
                    alt="Logo"
                    style={{ width: '300px', display: 'block', margin: '0 auto' }}
                />

                {/* Default Page */}
                <Routes>
                    <Route
                        path="/"
                        element={
                            <div style={{ textAlign: 'center', margin: '20px 0' }}>
                                <Link to="/login">
                                    <button style={{ margin: '10px', padding: '10px 20px'}}>
                                        Logowanie
                                    </button>
                                </Link>
                                <Link to="/register">
                                    <button style={{ margin: '10px', padding: '10px 20px'}}>
                                        Rejestracja
                                    </button>
                                </Link>
                            </div>
                        }
                    />

                    {/* Routes for Login and Register pages */}
                    <Route path="/login" element={<LoginPage />} />
                    <Route path="/register" element={<RegistrationForm />} />
                </Routes>
            </div>
        </Router>
    );
};

export default App;

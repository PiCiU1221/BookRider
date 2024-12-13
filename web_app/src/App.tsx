import React from 'react';
import { BrowserRouter as Router, Route, Routes, Link } from 'react-router-dom';
import './App.css'; // Import the CSS file
import RegistrationForm from './Register/LibAdminRegisterForm.tsx';
import LoginPage from './Login/LibAdminLoginForm.tsx';
import ProcessingPage from './ProcessingInfo.tsx';
import SystemAdminDashboard from './SystemAdmin/SystemAdminHomePage.tsx';
import SubmissionDetails from './SystemAdmin/SubmissionDetails.tsx';

const App: React.FC = () => {
    return (
        <Router>
            <div className="app-container">
                {/* Logo Container */}
                <div className="logo-container">
                    <img
                        src="/book-rider-high-resolution-logo.png"
                        alt="Logo"
                        className="logo"
                    />
                </div>

                {/* Default Page */}
                <Routes>
                    <Route
                        path="/"
                        element={
                            <div className="button-container">
                                <Link to="/login">
                                    <button className="action-button">Logowanie</button>
                                </Link>
                                <Link to="/register">
                                    <button className="action-button">Rejestracja</button>
                                </Link>
                            </div>
                        }
                    />
                    <Route path="/login" element={<LoginPage />} />
                    <Route path="/register" element={<RegistrationForm />} />
                    <Route path="/processing" element={<ProcessingPage />} />
                    <Route path="/sys-admin-dashboard" element={<SystemAdminDashboard />} />
                    <Route path="/submission/:submissionType/:submissionId" element={<SubmissionDetails />} />
                </Routes>
            </div>
        </Router>
    );
};

export default App;

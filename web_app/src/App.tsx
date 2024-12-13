import React from 'react';
import { BrowserRouter as Router, Route, Routes, Link } from 'react-router-dom';
import RegistrationForm from './Register/LibAdminRegisterForm.tsx';
import LoginPage from './Login/LibAdminLoginForm.tsx';
import ProcessingPage from './ProcessingInfo.tsx';
import SystemAdminDashboard from './SystemAdmin/SystemAdminHomePage.tsx';
import SubmissionDetails from './SystemAdmin/SubmissionDetails.tsx';

const App: React.FC = () => {
    return (
        <Router>
            <div>
                {/* Logo Container with background color */}
                <div
                    style={{
                        backgroundColor: '#3B576C',  // Set the background color to #3B576C
                        display: 'flex',
                        justifyContent: 'center',    // Centers the logo horizontally
                        alignItems: 'center',        // Centers the logo vertically
                    }}
                >
                    <img
                        src="/book-rider-high-resolution-logo.png"
                        alt="Logo"
                        style={{width: '300px'}}  // Keeps the logo centered with fixed width
                    />
                </div>

                {/* Default Page */}
                <Routes>
                    <Route
                        path="/"
                        element={
                            <div style={{textAlign: 'center', margin: '20px 0'}}>
                                <Link to="/login">
                                    <button style={{margin: '10px', padding: '10px 20px'}}>
                                        Logowanie
                                    </button>
                                </Link>
                                <Link to="/register">
                                    <button style={{margin: '10px', padding: '10px 20px'}}>
                                        Rejestracja
                                    </button>
                                </Link>
                            </div>
                        }
                    />

                    <Route path="/login" element={<LoginPage/>}/>
                    <Route path="/register" element={<RegistrationForm/>}/>

                    <Route path="/" element={<RegistrationForm/>}/>
                    <Route path="/processing" element={<ProcessingPage/>}/>

                    <Route path="/" element={<RegistrationForm/>}/>
                    <Route path="/processing" element={<ProcessingPage/>}/>

                    // PREVIEW ADMIN DASHBOARD
                    // use the path to view the sys admin dashboard in preview version
                    <Route path="/sys-admin-dashboard" element={<SystemAdminDashboard/>}/>
                    <Route path="/" element={<SystemAdminDashboard />} />
                    <Route path="/submission/:submissionType/:submissionId" element={<SubmissionDetails />} />
                </Routes>
            </div>
        </Router>
);
};

export default App;

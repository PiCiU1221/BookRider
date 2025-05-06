import React from 'react';
import './index.css';
import { BrowserRouter as Router, Route, Routes } from 'react-router-dom';

import LandingPage from './LandingPage';

// System Administrator
import SysAdminLogin from "./SystemAdmin/SystemAdminLogin.tsx";
import SystemAdminDashboard from './SystemAdmin/SystemAdminHomePage';
import SubmissionDetailsLibrary from './SystemAdmin/SubmissionDetailsLibrary.tsx';
import SubmissionDetailsDriver from './SystemAdmin/SubmissionDetailsDriver.tsx';

// Library Administrator
import RegistrationForm from './LibraryAdmin/LibraryAdminRegisterForm.tsx';
import LibraryAdminLogin from './LibraryAdmin/LibraryAdminLogin.tsx';
import LibraryAdminHomePage from './LibraryAdmin/LibraryAdminHomePage.tsx';
import LibraryAdminAddLibrary from './LibraryAdmin/LibraryAdminAddLibrary.tsx';
import LibraryAdminAddLibrarian from './LibraryAdmin/LibraryAdminAddLibrarian.tsx';
import LibraryAdminSettings from './LibraryAdmin/LibraryAdminSettings.tsx';

// Librarian
import LibrarianLogin from './Librarian/LibrarianLogin.tsx';
import LibrarianHomePage from './Librarian/LibrarianHomePage.tsx';
import LibrarianAddBook from './Librarian/LibrarianAddBook.tsx';
import LibrarianOrders from './Librarian/LibrarianOrders.tsx';
import LibrarianReturns from './Librarian/LibrarianReturns.tsx';
import LibrarianReaders from './Librarian/LibrarianReaders.tsx';
import LibrarianSettings from './Librarian/LibrarianSettings.tsx';

// Utils
import ProcessingPage from './Utils/ProcessingInfoPage.tsx';
import UserManualPage from './Utils/UserManualPage.tsx';
import ContactInfoPage from './Utils/ContactInfoPage.tsx';
import LegalInfoPage from './Utils/LegalInfoPage.tsx';

import SwaggerUI from 'swagger-ui-react';
import 'swagger-ui-react/swagger-ui.css';

const App: React.FC = () => {
    return (
        <Router>
            <Routes>
                <Route path="/" element={<LandingPage />} />
                <Route path="/librarian-login" element={<LibrarianLogin />} />
                <Route path="/library-admin-login" element={<LibraryAdminLogin />} />
                <Route path="/system-admin-login" element={<SysAdminLogin />} />
                <Route path="/register" element={<RegistrationForm />} />
                <Route path="/processing-info" element={<ProcessingPage />} />
                <Route path="/system-admin-dashboard" element={<SystemAdminDashboard />} />
                <Route path="/submissionDetailsLibrary/:submissionId" element={<SubmissionDetailsLibrary />} />
                <Route path="/submissionDetailsDriver/:submissionId" element={<SubmissionDetailsDriver />} />
                <Route path="/add-library" element={<LibraryAdminAddLibrary />} />
                <Route path="/librarian-dashboard" element={<LibrarianHomePage />} />
                <Route path="/info-page" element={<UserManualPage />} />
                <Route path="/contact" element={<ContactInfoPage />} />
                <Route path="/legal-info" element={<LegalInfoPage />} />
                <Route path="/library-admin-dashboard" element={<LibraryAdminHomePage />} />
                <Route path="/add-book" element={<LibrarianAddBook />} />
                <Route path="/orders" element={<LibrarianOrders />} />
                <Route path="/returns" element={<LibrarianReturns />} />
                <Route path="/readers" element={<LibrarianReaders />} />
                <Route path="/librarian-settings" element={<LibrarianSettings />} />
                <Route path="/library-admin-add-librarian" element={<LibraryAdminAddLibrarian />} />
                <Route path="/library-admin-settings" element={<LibraryAdminSettings />} />
                <Route
                    path="/api-docs"
                    element={
                        <div style={{ margin: '20px' }}>
                            <SwaggerUI
                                url="https://bookrider.pl/v3/api-docs"
                                docExpansion="none"
                                deepLinking={true}
                                layout="BaseLayout"
                                requestInterceptor={(request) => {
                                    const token = localStorage.getItem('access_token');
                                    if (token) {
                                        request.headers['Authorization'] = `Bearer ${token}`;
                                    }
                                    return request;
                                }}
                            />
                        </div>
                    }
                />
            </Routes>
        </Router>
    );
};

export default App;

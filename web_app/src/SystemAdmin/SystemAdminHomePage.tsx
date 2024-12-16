import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';

const SystemAdminDashboard: React.FC = () => {
    const [activeSection, setActiveSection] = useState<string>('librarySubmissions');
    const navigate = useNavigate();
    const sysAdminUsername = 'admin123';

    const handleSectionChange = (section: string) => {
        setActiveSection(section);
    };

    const handleSettings = () => {
        alert('Settings clicked');
    };

    const handleLogout = () => {
        alert('Logging out');
    };

    const handleExpand = (submissionType: string, submissionId: number) => {
        navigate(`/submission/${submissionType}/${submissionId}`);
    };

    return (
        <div style={{ backgroundColor: "#3B576C", marginBottom: "-250px" }}>
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
                    marginTop: "-40px"
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

            {/* Section Buttons */}
            <header
                style={{
                    display: 'flex',
                    justifyContent: 'space-around',
                    padding: '10px',
                    backgroundColor: '#314757',
                    color: '#fff',
                    position: 'sticky',
                    top: 50,
                    zIndex: 1000,
                }}
            >
                <button
                    onClick={() => handleSectionChange('librarySubmissions')}
                    style={{
                        padding: '10px 20px',
                        border: 'none',
                        borderRadius: '4px',
                        backgroundColor: activeSection === 'librarySubmissions' ? '#3B576C' : '#2d343a',
                        color: '#fff',
                        cursor: 'pointer',
                    }}
                >
                    Podania o zatwierdzenie bibliotek
                </button>
                <button
                    onClick={() => handleSectionChange('driverSubmissions')}
                    style={{
                        padding: '10px 20px',
                        border: 'none',
                        borderRadius: '4px',
                        backgroundColor: activeSection === 'driverSubmissions' ? '#3B576C' : '#2d343a',
                        color: '#fff',
                        cursor: 'pointer',
                    }}
                >
                    Podania o zatwierdzenie kierowców
                </button>
            </header>

            {/* Main Body */}
            <main style={{ padding: '20px', maxWidth: '800px', marginLeft: '-40px', width: '107%'}}>
                {activeSection === 'librarySubmissions' && (
                    <section style={{ padding: '20px', borderRadius: '4px' }}>
                        <h2 style={{ color: '#fff' }}>Podania o zatwierdzenie bibliotek</h2>
                        <div style={{ marginBottom: '20px' }}>
                            {[1, 2, 3].map((id) => (
                                <div key={id} style={submissionStyle}>
                                    <h3 style={{ color: '#fff' }}>Library Submission {id}</h3>
                                    <p style={{ color: '#fff' }}>Details of library submission {id} go here.</p>
                                    <div>
                                        <button
                                            onClick={() => handleExpand('Library', id)}
                                            style={{
                                                ...actionButtonStyle,
                                                backgroundColor: '#3B576C',
                                            }}
                                        >
                                            Otwórz
                                        </button>
                                    </div>
                                </div>
                            ))}
                        </div>
                    </section>
                )}
                {activeSection === 'driverSubmissions' && (
                    <section style={{ padding: '20px', borderRadius: '4px' }}>
                        <h2 style={{ color: '#fff' }}>Podania o zatwierdzenie kierowców</h2>
                        <div style={{ marginBottom: '20px' }}>
                            {[1, 2, 3].map((id) => (
                                <div key={id} style={submissionStyle}>
                                    <h3 style={{ color: '#fff' }}>Driver Submission {id}</h3>
                                    <p style={{ color: '#fff' }}>Details of driver submission {id} go here.</p>
                                    <div>
                                        <button
                                            onClick={() => handleExpand('Driver', id)}
                                            style={{
                                                ...actionButtonStyle,
                                                backgroundColor: '#3B576C',
                                            }}
                                        >
                                            Otwórz
                                        </button>
                                    </div>
                                </div>
                            ))}
                        </div>
                    </section>
                )}
            </main>
        </div>
    );
};

const submissionStyle = {
    backgroundColor: '#38424a',
    padding: '15px',
    marginBottom: '10px',
    borderRadius: '4px',
};

const actionButtonStyle = {
    padding: '8px 16px',
    margin: '0 10px',
    backgroundColor: '#3B576C',
    color: '#fff',
    border: 'none',
    borderRadius: '4px',
    cursor: 'pointer',
};

export default SystemAdminDashboard;

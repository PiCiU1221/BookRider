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
        alert('Ustawienia');
    };

    const handleLogout = () => {
        alert('Wylogowywanie');
    };

    const handleExpand = (submissionType: string, submissionId: number) => {
        navigate(`/submission/${submissionType}/${submissionId}`);
    };

    return (
        <div style={pageStyle}>
            <div style={headerStyle}>
                <div style={greetingStyle}>
                    <strong>Witaj, {sysAdminUsername}</strong>
                </div>
                <div>
                    <button onClick={handleSettings} style={buttonStyle}>Ustawienia</button>
                    <button onClick={handleLogout} style={buttonStyle}>Wyloguj się</button>
                </div>
            </div>

            <header style={navHeaderStyle}>
                <button
                    onClick={() => handleSectionChange('librarySubmissions')}
                    style={getNavButtonStyle1('librarySubmissions', activeSection)}
                >
                    Podania o zatwierdzenie bibliotek
                </button>
                <button
                    onClick={() => handleSectionChange('driverSubmissions')}
                    style={getNavButtonStyle2('driverSubmissions', activeSection)}
                >
                    Podania o zatwierdzenie kierowców
                </button>
            </header>

            <main style={mainBodyStyle}>
                {activeSection === 'librarySubmissions' && (
                    <section style={sectionStyle}>
                        <h2 style={sectionTitleStyle}>Podania o zatwierdzenie bibliotek</h2>
                        <div style={submissionListStyle}>
                            {[1, 2, 3].map((id) => (
                                <div key={id} style={submissionStyle}>
                                    <h3 style={submissionTitleStyle}>Library Submission {id}</h3>
                                    <p style={submissionDetailStyle}>Details of library submission {id} go here.</p>
                                    <button
                                        onClick={() => handleExpand('Library', id)}
                                        style={actionButtonStyle}
                                    >
                                        Otwórz
                                    </button>
                                </div>
                            ))}
                        </div>
                    </section>
                )}
                {activeSection === 'driverSubmissions' && (
                    <section style={sectionStyle}>
                        <h2 style={sectionTitleStyle}>Podania o zatwierdzenie kierowców</h2>
                        <div style={submissionListStyle}>
                            {[1, 2, 3].map((id) => (
                                <div key={id} style={submissionStyle}>
                                    <h3 style={submissionTitleStyle}>Driver Submission {id}</h3>
                                    <p style={submissionDetailStyle}>Details of driver submission {id} go here.</p>
                                    <button
                                        onClick={() => handleExpand('Driver', id)}
                                        style={actionButtonStyle}
                                    >
                                        Otwórz
                                    </button>
                                </div>
                            ))}
                        </div>
                    </section>
                )}
            </main>
        </div>
    );
};

// Styles
const pageStyle = {
    backgroundColor: '#f4f4f4',
    margin: '0',
    padding: '0',
};

const headerStyle: React.CSSProperties = {
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'center',
    padding: '15px 30px',
    backgroundColor: '#314757',
    color: '#fff',
    position: 'sticky',
    top: 0,
    zIndex: 1100,
    marginTop: "-30px",
    marginBottom: "-10px",
};

const greetingStyle = {
    fontSize: '20px',
};

const buttonStyle = {
    marginLeft: '18px',
    marginRight: '-5px',
    padding: '10px 20px',
    backgroundColor: '#2d343a',
    color: '#fff',
    border: 'none',
    borderRadius: '5px',
    cursor: 'pointer',
};

const navHeaderStyle: React.CSSProperties = {
    display: 'flex',
    justifyContent: 'center',
    padding: '10px 50px',
    backgroundColor: '#314757',
    color: '#fff',
    position: 'sticky',
    zIndex: 1000,
};


const getNavButtonStyle1 = (section: string, activeSection: string) => ({
    padding: '12px 65px',
    backgroundColor: section === activeSection ? '#3B576C' : '#2d343a',
    color: '#fff',
    border: 'none',
    borderRadius: '6px',
    cursor: 'pointer',
    marginLeft: "-30px",
});

const getNavButtonStyle2 = (section: string, activeSection: string) => ({
    padding: '12px 65px',
    backgroundColor: section === activeSection ? '#3B576C' : '#2d343a',
    color: '#fff',
    border: 'none',
    borderRadius: '6px',
    cursor: 'pointer',
    marginRight: "-30px",
});

const mainBodyStyle: React.CSSProperties = {
    padding: '20px',
    display: 'flex',
    justifyContent: 'center',
    flexDirection: 'column',
    gap: '15px',
    maxWidth: '1100px',
    margin: '0 auto',
};

const sectionStyle = {
    padding: '20px',
};

const sectionTitleStyle = {
    color: '#314757',
    fontSize: '22px',
    marginBottom: '15px',
    display: 'flex',
    justifyContent: 'center',
    marginTop: '10px',
};

const submissionListStyle: React.CSSProperties = {
    display: 'flex',
    flexDirection: 'column',
    gap: '15px',
    textAlign: 'center',
};

const submissionStyle = {
    backgroundColor: '#ffff',
    padding: '15px',
    borderRadius: '6px',
    boxShadow: '0px 2px 4px rgba(0, 0, 0, 0.3)',
};

const submissionTitleStyle = {
    fontSize: '18px',
    color: '#2d343a',
};

const submissionDetailStyle = {
    color: '#2d343a',
};

const actionButtonStyle = {
    padding: '8px 20px',
    backgroundColor: '#3B576C',
    color: '#fff',
    border: 'none',
    borderRadius: '5px',
    cursor: 'pointer',
};

export default SystemAdminDashboard;

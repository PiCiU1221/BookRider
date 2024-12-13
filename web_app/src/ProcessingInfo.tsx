import React from 'react';
import { Link } from 'react-router-dom';

const ProcessingPage: React.FC = () => {
    return (
        <div style={{ textAlign: 'center', marginTop: '50px' }}>
            <h2>Twoje zgłoszenie jest przetwarzane</h2>
            <p>Może to zająć od 1 do 7 dni roboczych.</p>
            <p>Prosimy o cierpliwość.</p>
            <Link to="/">
                <button
                    style={{
                        marginTop: '20px',
                        padding: '10px 20px',
                        color: '#fff',
                        border: 'none',
                        borderRadius: '4px',
                        cursor: 'pointer',
                    }}
                >
                    Wróć do strony głównej
                </button>
            </Link>
        </div>
    );
};

export default ProcessingPage;

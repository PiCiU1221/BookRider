import React from 'react';
import { Link } from 'react-router-dom';

const ProcessingPage: React.FC = () => {
    return (
        <div style={{ textAlign: 'center', marginTop: '50px' }}>
            <h1 style = {headerStyle} >Twoje zgłoszenie jest przetwarzane</h1>
            <p style = {infoStyle}>Może to zająć od 1 do 7 dni roboczych. <br/> Prosimy o cierpliwość.</p>
            <Link to="/">
                <button style={homeButtonStyle}>
                    Wróć do strony głównej
                </button>
            </Link>
        </div>
    );
};

export default ProcessingPage;

const headerStyle = {
    marginTop: '-50px',
}

const infoStyle = {
    marginTop: '60px',
    marginBottom: '60px',
}

const homeButtonStyle = {
    marginTop: '20px',
    padding: '10px 20px',
    color: '#fff',
    border: 'none',
    borderRadius: '4px',
    cursor: 'pointer',
    backgroundColor: '#38424a',
}
import React from 'react';
import { Link } from 'react-router-dom';

const LoginPage: React.FC = () => {
    return (
        <div>
            <h2>Logowanie</h2>
            <form>
                <div>
                    <label>Nazwa użytkownika:</label>
                    <input type="email" required />
                </div>
                <div>
                    <label>Password:</label>
                    <input type="password" required />
                </div>
                <button type="submit">Log In</button>
            </form>

            {/* Navigation */}
            <div style={{ marginTop: '20px', textAlign: 'center' }}>
                <Link to="/register">
                    <button style={{ margin: '10px', padding: '10px 20px' }}>Rejestracja</button>
                </Link>
            </div>
        </div>
    );
};

export default LoginPage;

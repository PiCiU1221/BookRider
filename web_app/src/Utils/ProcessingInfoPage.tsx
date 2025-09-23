import React from 'react';
import { Link } from 'react-router-dom';

const ProcessingPage: React.FC = () => {
    return (
        <div className="min-h-screen bg-[#3b576c] flex items-center justify-center text-white">
            <div className="text-center space-y-10 px-4">
                {/* Big Book Logo Image */}
                <img
                    className="w-[8vw] mx-auto object-cover mb-12"
                    alt="Book Rider Logo"
                    src="/book-high-res.png"
                />
                <h1 className="text-5xl font-semibold">Twoje zgłoszenie jest przetwarzane</h1>
                <p className="text-xl">
                    Może to zająć od 1 do 7 dni roboczych. <br /> Prosimy o cierpliwość.
                </p>
                <Link to="/">
                    <button className="mt-10 bg-gray-200 text-[#314757] px-6 py-2 rounded-lg text-lg hover:scale-105 transition-all duration-[0.3s]">
                        Powrót do strony głównej
                    </button>
                </Link>
            </div>
        </div>
    );
};

export default ProcessingPage;

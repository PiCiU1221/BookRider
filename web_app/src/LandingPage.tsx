import React from 'react';
import { Link } from 'react-router-dom';

export const LandingPage: React.FC = () => {
    return (
        <div className="bg-[#3b576c] flex flex-row justify-center w-full h-screen overflow-hidden">

            {/* Top Bar */}
            <div className="bg-[#3b576c] w-screen h-screen">
                <div className="relative w-screen h-screen">
                    <div className="absolute w-screen h-screen top-0 left-0 bg-[#3b576c]"/>

                    <button>
                    <Link to="/">
                        <img
                            className="absolute w-[10%] h-[13%] top-[0%] left-[8%] object-cover"
                            alt="Book Rider Logo"
                            src="/book-rider-high-resolution-logo.png"
                        />
                    </Link>
                    </button>

                    <div className="absolute w-[17%] top-[5%] left-[18%]">
                        <Link to="/system-admin-login">
                            <button
                                className="w-full hover:text-[#2D343A] transition-all duration-[0.3s] font-normal text-white text-[1.2vw] text-center tracking-[0] leading-[normal]">
                                Administrator systemów
                            </button>
                        </Link>
                    </div>

                    <div className="absolute w-[15%] top-[5%] left-[35%]">
                        <Link to="/library-admin-login">
                            <button
                                className="w-full hover:text-[#2D343A] transition-all duration-[0.3s] ease-[ease] font-normal text-white text-[1.2vw] text-center tracking-[0] leading-[normal]">
                                Administrator biblioteki
                            </button>
                        </Link>
                    </div>

                    <div className="absolute w-[15%] top-[5%] left-[48%]">
                        <Link to="/librarian-login">
                            <button
                                className="w-full hover:text-[#2D343A] transition-all duration-[0.3s] ease-[ease] font-normal text-white text-[1.2vw] text-center tracking-[0] leading-[normal]">
                                Bibliotekarz
                            </button>
                        </Link>
                    </div>

                    <div className="absolute w-[20%] top-[5%] left-[57%]">
                        <Link to="/legal-info">
                            <button
                                className="w-full hover:text-[#2D343A] transition-all duration-[0.3s] font-normal text-white text-[1.2vw] text-center tracking-[0] leading-[normal]">
                                Informacje prawne
                            </button>
                        </Link>
                    </div>

                    <div className="absolute w-[12%] top-[5%] left-[71%]">
                        <Link to="/contact">
                            <button
                                className="w-full hover:text-[#2D343A] transition-all duration-[0.3s] font-normal text-white text-[1.2vw] text-center tracking-[0] leading-[normal]">
                                Kontakt
                            </button>
                        </Link>
                    </div>

                    {/* Big Slogan Text */}
                    <p className="absolute w-[40%] top-[30%] left-[8%] font-bold text-transparent text-[5vw] tracking-[0] leading-[normal]">
                        <span className="text-[#f7ca65] drop-shadow-md">Biblioteka</span>

                        <span className="text-white drop-shadow-md"> prosto do Twoich </span>

                        <span className="text-[#f7ca65] drop-shadow-md">drzwi</span>
                    </p>

                    {/* Frame */}
                    <div className="absolute w-[50%] h-[50%] top-[18%] left-[50%]">
                        <div className="relative w-[50%] h-[50%] -top-[1%] -left-[6%]">
                            <div
                                className="absolute w-screen h-screen top-[3%] left-[42%] border-4 border-solid border-white"/>

                            <div
                                className="absolute w-screen h-screen top-[17%] left-[34%] border-4 border-solid border-white"/>

                            <div
                                className="absolute w-screen h-screen top-[32%] left-[26%] border-4 border-solid border-white"/>
                        </div>
                    </div>

                    {/* Main Buttons */}
                    <div className="absolute w-[17%] h-[40%] top-[77%] left-[8%]">
                        <Link to="/librarian-login">
                            <button
                                className="relative w-[66%] h-[20%] bg-[#f7ca65] rounded-lg hover:scale-105 transition-all duration-[0.3s] font-normal text-[#3b576c] text-[1.2vw] text-center tracking-[0] leading-[normal]">
                                Logowanie
                                <br/>
                                bibliotekarza
                            </button>
                        </Link>
                    </div>

                    <div
                        className="absolute w-[10%] h-[8.5%] top-[77%] left-[21%] transition-all duration-[0.2s] ease-[ease]">
                        <Link to="/info-page">
                            <button
                                className="relative w-[105%] h-[95%] bg-[#4b6477] rounded-lg hover:scale-105 transition-all duration-[0.3s] font-normal text-white text-[1.2vw] text-center tracking-[0] leading-[normal]">
                                Dowiedz się <br/> więcej
                            </button>
                        </Link>
                    </div>

                    {/* Big Book Logo Image */}
                    <img
                        className="absolute w-[image] h-[50%] top-[40%] left-[70%] object-cover"
                        alt="Book Rider Logo"
                        src="/book-high-res.png"
                    />
                </div>
            </div>
        </div>
    );
};

export default LandingPage;

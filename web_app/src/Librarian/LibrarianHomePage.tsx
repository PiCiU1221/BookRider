import React, { useState } from 'react';
import {useNavigate, useParams} from 'react-router-dom';

const LibrarianHomePage: React.FC = () => {
    const [activeSection, setActiveSection] = useState<string>('addBook');
    const navigate = useNavigate();

    const [searchResults, setBookSearchResults] = useState<string[]>([
        'Book 1',
        'Book 2',
        'Book 3',
    ]);

    const [bookSearchInput, setSearchInput] = useState<string>('');
    const [showBookDropdown, setShowBookDropdown] = useState<boolean>(false);

    const handleBookSearchChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        const query = e.target.value;
        setSearchInput(query);

        const filteredResults = ['Book 1', 'Book 2', 'Book 3'].filter((book) =>
            book.toLowerCase().includes(query.toLowerCase())
        );
        setBookSearchResults(filteredResults);
    };

    const [publisherResults, setPublisherResults] = useState<string[]>([
        'Publisher 1',
        'Publisher 2',
        'Publisher 3',
    ]);

    const [publisherInput, setPublisherInput] = useState<string>('');
    const [showPublisherDropdown, setShowPublisherDropdown] = useState<boolean>(false);

    const handlePublisherSearchChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        const query = e.target.value;
        setPublisherInput(query);

        const filteredResults = ['Publisher 1', 'Publisher 2', 'Publisher 3'].filter((publisher) =>
            publisher.toLowerCase().includes(query.toLowerCase())
        );
        setPublisherResults(filteredResults);
    };

    const handleSectionChange = (section: string) => {
        setActiveSection(section);
    };

    // const handleLogout = () => {
    //     alert('Logging out');
    // };

    const {orderType, orderId} = useParams();

    const [showRejectionInput, setShowRejectionInput] = useState(false);
    const [rejectionReason, setRejectionReason] = useState('');

    const handleAccept = () => {
        alert(`${orderType} order ${orderId} accepted!`);
    };

    const handleDecline = () => {
        setShowRejectionInput(true);
    };

    const handleRejectionReasonChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        setRejectionReason(e.target.value);
    };

    const handleConfirmRejection = () => {
        if (rejectionReason === '') {
            alert('Proszę wybrać przyczynę odmowy realizacji zamówienia.');
        } else {
            alert(`Zamówienie ${orderId} odrzucone z powodu: ${rejectionReason}`);
            setShowRejectionInput(false);
            setRejectionReason('');
        }
    };

    const placeholderDetails = {
        title: 'Carrie',
        publisher: 'Pruszyński i S-ka',
        author: 'Stephen King',
        year: '2024',
        image: '/placeholder_book.png',
    };

    return (
        <div>
            <header className="flex justify-around p-2 bg-[#3B576C] text-white sticky top-0 z-[1000]">
                <div>
                    <img
                        className="relative w-[15%] h-auto object-cover left-[1%]"
                        alt="Book Rider Logo"
                        src="/book-rider-high-resolution-logo.png"
                    />
                </div>
                {[
                    {id: 'addBook', label: 'Dodaj książkę'},
                    {id: 'addPublisher', label: 'Dodaj wydawnictwo'},
                    {id: 'orders', label: 'Zamówienia'},
                    {id: 'addReader', label: 'Dodaj czytelnika'},
                    {id: 'settings', label: 'Ustawienia'},
                ].map(({id, label}) => (
                    <button
                        key={id}
                        onClick={() => handleSectionChange(id)}
                        className={`px-5 py-2 rounded border-none cursor-pointer text-[15px] transition-colors ${
                            activeSection === id ? 'bg-[#314757]' : 'bg-[#4B6477]'
                        }`}
                    >
                        {label}
                    </button>
                ))}
            </header>

            <main className="p-5 max-w-[800px] -ml-10 w-[107%]">
                {activeSection === "addBook" && (
                    <section className="p-5 rounded mb-[400px]">
                        <h2 className="text-center text-white">Dodaj książkę</h2>
                        <div className="relative mb-5">
                            <input
                                type="text"
                                placeholder="Wyszukaj książkę..."
                                value={bookSearchInput}
                                onChange={handleBookSearchChange}
                                onFocus={() => setShowBookDropdown(true)}
                                onBlur={() => setTimeout(() => setShowBookDropdown(false), 200)}
                                className="w-[97%] p-2 rounded border-none outline-none text-lg bg-[#2d343a] text-white"
                            />
                            {showBookDropdown && (
                                <div className="absolute top-full left-0 w-full bg-[#2d343a] rounded shadow-lg z-50 max-h-[200px] overflow-y-auto">
                                    {searchResults.map((result, index) => (
                                        <div key={index} className="p-2 text-white cursor-pointer hover:bg-[#2d343a]">
                                            {result}
                                        </div>
                                    ))}
                                    <div className="p-2 text-center border-t border-[#3B576C]">
                                        <p className="text-white mb-2">Nie ma tego, czego szukasz?</p>
                                        <button
                                            onClick={() => navigate("/add-book")}
                                            className="px-4 py-2 bg-[#3B576C] text-white rounded cursor-pointer hover:bg-[#3B576C]"
                                        >
                                            Dodaj książkę
                                        </button>
                                    </div>
                                </div>
                            )}
                        </div>
                    </section>
                )}

                {activeSection === 'addPublisher' && (
                    <section className="p-5 rounded-md mb-[400px]">
                        <h2 className="text-center text-white">Dodaj wydawnictwo</h2>
                        <div className="relative mb-5">
                            <input
                                type="text"
                                placeholder="Wyszukaj wydawnictwo..."
                                value={publisherInput}
                                onChange={handlePublisherSearchChange}
                                onFocus={() => setShowPublisherDropdown(true)}
                                onBlur={() => setTimeout(() => setShowPublisherDropdown(false), 200)}
                                className="w-[97%] p-2.5 rounded-md border-none outline-none text-lg bg-[#2d343a] text-white"
                            />
                            {showPublisherDropdown && (
                                <div className="absolute top-full left-0 w-full bg-[#2d343a] rounded-md shadow-lg z-50 max-h-[200px] overflow-y-auto">
                                    {publisherResults.map((result, index) => (
                                        <div
                                            key={index}
                                            className="p-2.5 text-white cursor-pointer hover:bg-[#3B576C]"
                                        >
                                            {result}
                                        </div>
                                    ))}
                                    <div className="p-2.5 text-center border-t border-[#3B576C]">
                                        <p className="text-white mb-2.5">Nie ma tego, czego szukasz?</p>
                                        <button
                                            onClick={() => navigate('/add-publisher')}
                                            className="px-4 py-2 bg-[#3B576C] text-white rounded-md cursor-pointer hover:bg-[#314757]"
                                        >
                                            Dodaj wydawnictwo
                                        </button>
                                    </div>
                                </div>
                            )}
                        </div>
                    </section>
                )}

                {activeSection === 'addReader' && (
                    <section className="p-5 rounded-md mb-[400px]">
                        <h2 className="text-center text-white">Dodaj czytelnika</h2>
                        {/* Dodaj elementy formularza do dodawania czytelnika */}
                    </section>
                )}

                {activeSection === 'settings' && (
                    <section className="p-5 rounded-md mb-[400px]">
                        <h2 className="text-center text-white">Ustawienia</h2>
                        {/* Dodaj formularz ustawień lub konfiguracji */}
                    </section>
                )}

                {activeSection === 'orders' && (
                    <section className="p-5 rounded-md mb-12">
                        <div className="order-details-container">
                            <h1 className="text-xl font-bold mb-4">Szczegóły zamówienia nr: {orderId}</h1>

                            <div className="bg-gray-800 p-6 rounded-lg shadow-lg">
                                <div className="flex justify-center mb-4">
                                    <img
                                        src={placeholderDetails.image}
                                        alt="Placeholder Book"
                                        className="w-40 h-60 object-cover rounded-md"
                                    />
                                </div>

                                <h2 className="text-lg font-semibold text-white">{placeholderDetails.title}</h2>
                                <p className="text-gray-300"><strong>Autor:</strong> {placeholderDetails.author}</p>
                                <p className="text-gray-300"><strong>Wydawnictwo:</strong> {placeholderDetails.publisher}</p>
                                <p className="text-gray-300"><strong>Rok wydania:</strong> {placeholderDetails.year}</p>

                                <div className="flex justify-between mt-4">
                                    <button
                                        onClick={handleAccept}
                                        className="bg-green-600 text-white py-2 px-4 rounded-md hover:bg-green-700 transition"
                                    >
                                        Zatwierdź
                                    </button>
                                    <button
                                        onClick={handleDecline}
                                        className="bg-red-600 text-white py-2 px-4 rounded-md hover:bg-red-700 transition"
                                    >
                                        Odrzuć
                                    </button>
                                </div>

                                {showRejectionInput && (
                                    <div className="mt-6">
                                        <h3 className="text-gray-600 font-semibold">Wybierz przyczynę odmowy:</h3>
                                        <form className="flex flex-col items-start gap-2 mt-2">
                                            <label className="flex items-center text-gray-600">
                                                <input
                                                    type="radio"
                                                    value="Brak w zbiorach biblioteki"
                                                    checked={rejectionReason === 'Brak w zbiorach biblioteki'}
                                                    onChange={handleRejectionReasonChange}
                                                    className="mr-2"
                                                />
                                                brak w zbiorach biblioteki
                                            </label>
                                            <label className="flex items-center text-gray-600">
                                                <input
                                                    type="radio"
                                                    value="Wszystkie egzemplarze zostały wypożyczone"
                                                    checked={rejectionReason === 'Wszystkie egzemplarze zostały wypożyczone'}
                                                    onChange={handleRejectionReasonChange}
                                                    className="mr-2"
                                                />
                                                wszystkie egzemplarze zostały wypożyczone
                                            </label>
                                            <label className="flex items-center text-gray-600">
                                                <input
                                                    type="radio"
                                                    value="Inne"
                                                    checked={rejectionReason === 'Inne'}
                                                    onChange={handleRejectionReasonChange}
                                                    className="mr-2"
                                                />
                                                inne
                                            </label>
                                        </form>
                                        <button
                                            className="bg-blue-600 text-white py-2 px-4 rounded-md mt-4 hover:bg-blue-700 transition"
                                            onClick={handleConfirmRejection}
                                        >
                                            Potwierdź
                                        </button>
                                    </div>
                                )}
                            </div>
                        </div>
                    </section>
                )}
            </main>
        </div>
    );
};

export default LibrarianHomePage;

import React, { useState } from 'react';
import {Link, useNavigate, useParams} from 'react-router-dom';

const LibrarianHomePage: React.FC = () => {
    const [activeSection, setActiveSection] = useState<string>('addBook');
    const navigate = useNavigate();
    const librarianUsername = 'librarian123';

    const [searchResults, setBookSearchResults] = useState<string[]>([
        'Book 1',
        'Book 2',
        'Book 3',
    ]);

    const [bookSearchInput, setSearchInput] = useState<string>('');
    const [showDropdown, setShowBookDropdown] = useState<boolean>(false);

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

    const handleSettings = () => {
        alert('Settings');
    };

    const handleLogout = () => {
        alert('Logging out');
    };

    const { orderType, orderId } = useParams();

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
                    <strong>Witaj, {librarianUsername}</strong>
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
                            marginRight: '-10px',
                        }}
                    >
                        Wyloguj się
                    </button>
                </div>
            </div>

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
                    onClick={() => handleSectionChange('addBook')}
                    style={{
                        padding: '10px 20px',
                        border: 'none',
                        borderRadius: '4px',
                        backgroundColor: activeSection === 'addBook' ? '#3B576C' : '#2d343a',
                        color: '#fff',
                        cursor: 'pointer',
                    }}
                >
                    Dodaj książkę
                </button>
                <button
                    onClick={() => handleSectionChange('addPublisher')}
                    style={{
                        padding: '10px 20px',
                        border: 'none',
                        borderRadius: '4px',
                        backgroundColor: activeSection === 'addPublisher' ? '#3B576C' : '#2d343a',
                        color: '#fff',
                        cursor: 'pointer',
                    }}
                >
                    Dodaj wydawnictwo
                </button>
                <button
                    onClick={() => handleSectionChange('orders')}
                    style={{
                        padding: '10px 20px',
                        border: 'none',
                        borderRadius: '4px',
                        backgroundColor: activeSection === 'orders' ? '#3B576C' : '#2d343a',
                        color: '#fff',
                        cursor: 'pointer',
                    }}
                >
                    Zamówienia
                </button>
            </header>

            <main style={{ padding: '20px', maxWidth: '800px', marginLeft: '-40px', width: '107%' }}>
                {activeSection === 'addBook' && (
                    <section style={{ padding: '20px', borderRadius: '4px', marginBottom: '400px' }}>
                        <h2 style={{ textAlign: "center", color: '#fff' }}>Dodaj książkę</h2>

                        <div style={{position: 'relative', marginBottom: '20px'}}>
                            <input
                                type="text"
                                placeholder="Wyszukaj książkę..."
                                value={bookSearchInput}
                                onChange={handleBookSearchChange}
                                onFocus={() => setShowBookDropdown(true)}
                                onBlur={() => setTimeout(() => setShowBookDropdown(false), 200)}
                                style={{
                                    width: '97%',
                                    padding: '10px',
                                    borderRadius: '4px',
                                    border: 'none',
                                    outline: 'none',
                                    fontSize: '16px',
                                    backgroundColor: '#2d343a',
                                }}
                            />

                            {showDropdown && (
                                <div
                                    style={{
                                        position: 'absolute',
                                        top: '100%',
                                        left: 0,
                                        width: '100%',
                                        backgroundColor: '#2d343a',
                                        borderRadius: '4px',
                                        boxShadow: '0 4px 6px rgba(0, 0, 0, 0.2)',
                                        zIndex: 1000,
                                        maxHeight: '200px',
                                        overflowY: 'auto',
                                    }}
                                >
                                    {searchResults.map((result, index) => (
                                        <div
                                            key={index}
                                            style={{
                                                padding: '10px',
                                                color: '#fff',
                                                cursor: 'pointer',
                                            }}
                                        >
                                            {result}
                                        </div>
                                    ))}

                                    <div
                                        style={{
                                            padding: '10px',
                                            textAlign: 'center',
                                            borderTop: '1px solid #3B576C',
                                        }}
                                    >
                                        <p style={{color: '#fff', marginBottom: '10px'}}>
                                            Nie ma tego, czego szukasz?
                                        </p>
                                        <button
                                            onClick={() => navigate('/add-book')}
                                            style={{
                                                padding: '8px 16px',
                                                backgroundColor: '#3B576C',
                                                color: '#fff',
                                                border: 'none',
                                                borderRadius: '4px',
                                                cursor: 'pointer',
                                            }}
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
                    <section style={{ padding: '20px', borderRadius: '4px', marginBottom: '400px' }}>
                        <h2 style={{ textAlign: "center", color: '#fff' }}>Dodaj wydawnictwo</h2>

                        <div style={{ position: 'relative', marginBottom: '20px' }}>
                            <input
                                type="text"
                                placeholder="Wyszukaj wydawnictwo..."
                                value={publisherInput}
                                onChange={handlePublisherSearchChange}
                                onFocus={() => setShowPublisherDropdown(true)}
                                onBlur={() => setTimeout(() => setShowPublisherDropdown(false), 200)}
                                style={{
                                    width: '97%',
                                    padding: '10px',
                                    borderRadius: '4px',
                                    border: 'none',
                                    outline: 'none',
                                    fontSize: '16px',
                                    backgroundColor: '#2d343a',
                                }}
                            />

                            {showPublisherDropdown && (
                                <div
                                    style={{
                                        position: 'absolute',
                                        top: '100%',
                                        left: 0,
                                        width: '100%',
                                        backgroundColor: '#2d343a',
                                        borderRadius: '4px',
                                        boxShadow: '0 4px 6px rgba(0, 0, 0, 0.2)',
                                        zIndex: 1000,
                                        maxHeight: '200px',
                                        overflowY: 'auto',
                                    }}
                                >
                                    {publisherResults.map((result, index) => (
                                        <div
                                            key={index}
                                            style={{
                                                padding: '10px',
                                                color: '#fff',
                                                cursor: 'pointer',
                                            }}
                                        >
                                            {result}
                                        </div>
                                    ))}

                                    <div
                                        style={{
                                            padding: '10px',
                                            textAlign: 'center',
                                            borderTop: '1px solid #3B576C',
                                        }}
                                    >
                                        <p style={{ color: '#fff', marginBottom: '10px' }}>
                                            Nie ma tego, czego szukasz?
                                        </p>
                                        <button
                                            onClick={() => navigate('/add-publisher')}
                                            style={{
                                                padding: '8px 16px',
                                                backgroundColor: '#3B576C',
                                                color: '#fff',
                                                border: 'none',
                                                borderRadius: '4px',
                                                cursor: 'pointer',
                                            }}
                                        >
                                            Dodaj wydawnictwo
                                        </button>
                                    </div>
                                </div>
                            )}
                        </div>
                    </section>
                )}

                {activeSection === 'orders' && (
                    <section style={{padding: '20px', borderRadius: '4px', marginBottom: '50px'}}>
                            <div className="order-details-container">
                                <h1 className="order-title">Szczegóły zamówienia nr: {orderId}</h1>

                                <div className="order-card">
                                    <div className="book-image">
                                        <img
                                            src={placeholderDetails.image}
                                            alt="Placeholder Book"
                                            className="book-image"
                                        />
                                    </div>

                                    <h2 className="book-title">{placeholderDetails.title}</h2>
                                    <p className="book-info"><strong>Autor:</strong> {placeholderDetails.author}</p>
                                    <p className="book-info"><strong>Wydawnictwo:</strong> {placeholderDetails.publisher}</p>
                                    <p className="book-info"><strong>Rok wydania:</strong> {placeholderDetails.year}</p>

                                    <div className="button-container">
                                        <button onClick={handleAccept} className="accept-button">Zatwierdź</button>
                                        <button onClick={handleDecline} className="decline-button">Odrzuć</button>
                                    </div>

                                    {showRejectionInput && (
                                        <div>
                                            <h3 style={{color: '#4b4b4b'}} >Wybierz przyczynę odmowy:</h3>
                                            <form style={{ display: 'flex', flexDirection: 'column', alignItems: 'flex-start', gap: '10px' }}>
                                                <label style={{ display: 'flex', alignItems: 'center', color: '#4b4b4b' }}>
                                                    <input
                                                        type="radio"
                                                        value="Brak w zbiorach biblioteki"
                                                        checked={rejectionReason === 'Brak w zbiorach biblioteki'}
                                                        onChange={handleRejectionReasonChange}
                                                    />
                                                    <span style={{ marginLeft: '10px' }}>brak w zbiorach biblioteki</span>
                                                </label>
                                                <label style={{ display: 'flex', alignItems: 'center', color: '#4b4b4b' }}>
                                                    <input
                                                        type="radio"
                                                        value="Wszystkie egzemplarze zostały wypożyczone"
                                                        checked={rejectionReason === 'Wszystkie egzemplarze zostały wypożyczone'}
                                                        onChange={handleRejectionReasonChange}
                                                    />
                                                    <span style={{ marginLeft: '10px' }}>wszystkie egzemplarze zostały wypożyczone</span>
                                                </label>
                                                <label style={{ display: 'flex', alignItems: 'center', color: '#4b4b4b' }}>
                                                    <input
                                                        type="radio"
                                                        value="Inne"
                                                        checked={rejectionReason === 'Inne'}
                                                        onChange={handleRejectionReasonChange}
                                                    />
                                                    <span style={{ marginLeft: '10px' }}>inne</span>
                                                </label>
                                            </form>
                                            <button className="confirm-rejection-button" onClick={handleConfirmRejection}>Potwierdź</button>
                                        </div>
                                    )}
                                </div>

                                <div className="back-button-container">
                                    <Link to="/librarian-dashboard">
                                        <button className="back-button">Powrót do strony głównej</button>
                                    </Link>
                                </div>
                            </div>
                    </section>
                )}
            </main>
        </div>
    );
};

export default LibrarianHomePage;

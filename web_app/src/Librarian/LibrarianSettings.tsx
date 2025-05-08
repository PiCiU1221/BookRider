import React, {useEffect, useState} from 'react';
import {useNavigate} from 'react-router-dom';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL;

const LibrarianSettings: React.FC = () => {
    interface Book {
        id: number;
        title: string;
        categoryName: string;
        authorNames: string[];
        releaseYear: number;
        publisherName: string;
        isbn: string;
        languageName: string;
        image: string;
    }

    interface Category {
        name: string;
    }

    interface Language {
        name: string;
    }

    interface Title {
        name: string;
    }

    interface Author {
        name: string;
    }

    interface Publisher {
        name: string;
    }

    const [activeSection, setActiveSection] = useState<string>('addBook');

    // Error messages
    const [message, setMessage] = useState<string | null>(null);
    const [messageType, setMessageType] = useState<'success' | 'error' | null>(null);

    // Books
    const [bookSearchInput, setBookSearchInput] = useState('');
    const [categoryInput, setCategoryInput] = useState('');
    const [languageInput, setLanguageInput] = useState('');
    const [publisherInput, setPublisherInput] = useState('');
    const [authorInput, setAuthorInput] = useState('');
    const [isbnInput, setIsbnInput] = useState('');
    const [releaseYearFrom, setReleaseYearFrom] = useState<number | string>('');
    const [releaseYearTo, setReleaseYearTo] = useState<number | string>('');
    const [searchResults, setBookSearchResults] = useState<Book[]>([]);
    const [isUserLibraryChecked, setIsUserLibraryChecked] = useState(false);
    // const [page, setPage] = useState(0);
    // const [hasMore, setHasMore] = useState(true);
    // const [loading, setLoading] = useState(false);

    const [categoryOptions, setCategoryOptions] = useState<string[]>([]);
    const [languageOptions, setLanguageOptions] = useState<string[]>([]);
    const [bookTitleOptions, setBookTitleOptions] = useState<string[]>([]);
    const [authorOptions, setAuthorOptions] = useState<string[]>([]);
    const [publisherOptions, setPublisherOptions] = useState<string[]>([]);

    // Readers
    const [userId, setUserId] = useState('');
    const [cardId, setCardId] = useState('');
    const [firstName, setFirstName] = useState('');
    const [lastName, setLastName] = useState('');
    const [expirationDate, setExpirationDate] = useState('2025-01-01');
    const [libraryCardSearchId, setLibraryCardSearchId] = useState('');

    // Orders
    const [orderDetails, setOrderDetails] = useState<OrderDetails[]>([]);
    const [showRejectionInput, setShowRejectionInput] = useState(false);
    const [rejectionReason, setRejectionReason] = useState('');
    const [selectedOrderId, setSelectedOrderId] = useState<number | null>(null);
    const [customReason, setCustomReason] = useState('');

    interface OrderItem {
        book: Book;
        quantity: number;
    }

    interface OrderDetails {
        orderId: number;
        userId: string;
        libraryName: string;
        pickupAddress: string;
        destinationAddress: string;
        isReturn: boolean;
        status: string;
        amount: number;
        paymentStatus: string;
        noteToDriver: string;
        createdAt: string;
        acceptedAt: string;
        driverAssignedAt: string;
        pickedUpAt: string;
        deliveredAt: string;
        orderItems: OrderItem[];

        displayStatus?: 'PENDING' | 'IN_REALIZATION';
    }

    // User
    interface LibraryCardDetails {
        userId: string;
        cardId: string;
        firstName: string;
        lastName: string;
        expirationDate: string;
    }

    const [libraryCardDetails, setLibraryCardDetails] = useState<LibraryCardDetails | null>(null);

    // Returns
    interface RentalReturnItem {
        id: number;
        rentalId: number;
        book: {
            id: number;
            title: string;
            categoryName: string;
            authorNames: string[];
            releaseYear: number;
            publisherName: string;
            isbn: string;
            languageName: string;
            image: string;
        };
        returnedQuantity: number;
    }

    interface RentalReturnDetails {
        id: number;
        orderId: number;
        returnedAt: string;
        status: 'IN_PROGRESS' | 'COMPLETED';
        rentalReturnItems: RentalReturnItem[];
    }

    const [returnType, setReturnType] = useState<'inPerson' | 'driver'>('inPerson');
    const [rentalReturnId, setRentalReturnId] = useState<number | null>(null);
    const [rentalReturnDetails, setRentalReturnDetails] = useState<RentalReturnDetails | null>(null);
    const [driverId, setDriverId] = useState<string | null>(null);
    // const [returnStatus, setReturnStatus] = useState<'IN_PROGRESS' | 'COMPLETED'>('IN_PROGRESS');

    const navigate = useNavigate();

    // Add book --------------------------------------------------------------------------------------------------------
    useEffect(() => {
        if (activeSection === "addBook") {
            // setPage(0);
            setBookSearchResults([]);
            //setHasMore(true);
            fetchBooks();
        } else if (activeSection === "bookSearch") {
            setBookSearchResults([]);
            // setPage(0);
            // setHasMore(true);

            fetchBooks();
        } else {
            setBookSearchResults([]);
            // setPage(0);
            // setHasMore(true);
        }

        const fetchDropdownData = async () => {
            const token = localStorage.getItem('access_token');

            try {
                const [
                    categoriesRes,
                    languagesRes
                ] = await Promise.all([
                    fetch(`${API_BASE_URL}/api/categories`, { headers: { 'Authorization': `Bearer ${token}` } }),
                    fetch(`${API_BASE_URL}/api/languages`, { headers: { 'Authorization': `Bearer ${token}` } }),
                ]);

                if (categoriesRes.ok) {
                    const categories: Category[] = await categoriesRes.json();
                    setCategoryOptions(categories.map((c) => c.name));
                }

                if (languagesRes.ok) {
                    const languages: Language[] = await languagesRes.json();
                    setLanguageOptions(languages.map((l) => l.name));
                }

                await fetchAuthors('');
                await fetchBookTitles('');
                await fetchPublishers('');

            } catch (error) {
                console.error("Error fetching dropdown data: ", error);
            }
        };

        fetchDropdownData();
        fetchOrderDetails();

        setShowRejectionInput(false);
        setRejectionReason('');

        // on-scroll load
        // window.addEventListener('scroll', handleScroll);
        // return () => window.removeEventListener('scroll', handleScroll);
    }, [activeSection]);

    const fetchBookTitles = async (query: string) => {
        if (!query) return;
        const token = localStorage.getItem('access_token');
        const response = await fetch(`${API_BASE_URL}/api/books/search-book-titles?title=${query}`, {
            headers: {
                'Authorization': `Bearer ${token}`,
            },
        });
        const data: Title[] = await response.json();
        setBookTitleOptions(data.map((b) => b.name));
    };

    const fetchAuthors = async (query: string) => {
        if (!query) return;
        const token = localStorage.getItem('access_token');
        const response = await fetch(`${API_BASE_URL}/api/authors/search?name=${query}`, {
            headers: {
                'Authorization': `Bearer ${token}`,
            },
        });
        const data: Author[] = await response.json();
        setAuthorOptions(data.map((a) => a.name));
    };

    const fetchPublishers = async (query: string) => {
        if (!query) return;
        const token = localStorage.getItem('access_token');

        try {
            const response = await fetch(`${API_BASE_URL}/api/publishers/search?name=${query}`, {
                headers: { 'Authorization': `Bearer ${token}` },
            });

            if (response.ok) {
                const data: Publisher[] = await response.json();
                setPublisherOptions(data.map((p) => p.name));
            }
        } catch (error) {
            console.error("Error fetching publishers:", error);
        }
    };

    const fetchBooks = async () => {
        // setLoading(true);

        const token = localStorage.getItem('access_token');
        if (!token) return;

        const queryParams = new URLSearchParams();
        if (bookSearchInput) queryParams.append("title", bookSearchInput);
        if (categoryInput) queryParams.append("category", categoryInput);
        if (languageInput) queryParams.append("language", languageInput);
        if (publisherInput) queryParams.append("publisher", publisherInput);
        if (isbnInput) queryParams.append("isbn", isbnInput);
        if (authorInput) queryParams.append("authorNames", authorInput);
        if (releaseYearFrom) queryParams.append("releaseYearFrom", releaseYearFrom.toString());
        if (releaseYearTo) queryParams.append("releaseYearTo", releaseYearTo.toString());

        queryParams.append("page", "0");
        queryParams.append("size", "20");

        try {
            const response = await fetch(`${API_BASE_URL}/api/books/search?${queryParams.toString()}`, {
                method: 'GET',
                headers: {
                    'Authorization': `Bearer ${token}`,
                    'Content-Type': 'application/json',
                },
            });

            if (!response.ok) {
                throw new Error(`Error fetching books: ${response.statusText}`);
            }

            const data = await response.json();
            setBookSearchResults(data.content); // Always replace results
        } catch (error) {
            console.error("Error fetching books: ", error);
        }
    };

    // const handleScroll = () => {
    //     if (
    //         window.innerHeight + document.documentElement.scrollTop >= document.documentElement.offsetHeight - 200 &&
    //         hasMore &&
    //         !loading &&
    //         (!bookSearchInput && !categoryInput && !languageInput && !publisherInput && !isbnInput && !authorInput)
    //     ) {
    //         fetchBooks(page + 1);
    //     }
    // };

    const handleSearch = (reset: boolean = false) => {
        if (reset) {
            setBookSearchResults([]);
            // setPage(0);
            // setHasMore(true);
        }
        fetchBooks();
    };

    const handleRedirectToAddBook = () => {
        navigate('/add-book');
    };

    // Readers ---------------------------------------------------------------------------------------------------------
    const handleCreateLibraryCard = async () => {
        const token = localStorage.getItem('access_token');
        if (!token) {
            console.error('No token found');
            return;
        }

        try {
            const response = await fetch(`${API_BASE_URL}/api/library-cards`, {
                method: 'POST',
                headers: {
                    'Authorization': `Bearer ${token}`,
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    userId,
                    cardId,
                    firstName,
                    lastName,
                    expirationDate,
                }),
            });

            if (response.ok) {
                alert('Library card created successfully');
                setUserId('');
                setCardId('');
                setFirstName('');
                setLastName('');
                setExpirationDate('2025-01-01');
            } else {
                throw new Error('Error creating library card');
            }
        } catch (error) {
            console.error('Error creating library card: ', error);
        }
    };

    const handleSearchLibraryCard = async () => {
        const token = localStorage.getItem('access_token');
        if (!token) {
            console.error("No token found");
            return;
        }

        try {
            const response = await fetch(`${API_BASE_URL}/api/library-cards/${libraryCardSearchId}`, {
                method: 'GET',
                headers: {
                    'Authorization': `Bearer ${token}`,
                    'Content-Type': 'application/json',
                },
            });

            if (response.ok) {
                const data = await response.json();
                setLibraryCardDetails(data[0]);
            } else {
                throw new Error('Error searching for library card');
            }
        } catch (error) {
            console.error('Error searching for library card: ', error);
            setLibraryCardDetails(null);
        }
    };

    const handleLogout = () => {
        localStorage.removeItem('username');
        localStorage.removeItem('email');
        localStorage.removeItem('access_token');
        localStorage.removeItem('role');

        navigate('/');
    };

    const handleSectionChange = (section: string) => {
        setActiveSection(section);
    };

    // Orders ----------------------------------------------------------------------------------------------------------
    const fetchOrderDetails = async () => {
        const token = localStorage.getItem('access_token');

        try {
            const [pendingRes, realizationRes] = await Promise.all([
                fetch(`${API_BASE_URL}/api/orders/librarian/pending?page=0&size=10`, {
                    headers: { 'Authorization': `Bearer ${token}` },
                }),
                fetch(`${API_BASE_URL}/api/orders/librarian/in-realization?page=0&size=10`, {
                    headers: { 'Authorization': `Bearer ${token}` },
                }),
            ]);

            const pendingData = pendingRes.ok ? await pendingRes.json() : { content: [] };
            const realizationData = realizationRes.ok ? await realizationRes.json() : { content: [] };

            const pendingOrders: OrderDetails[] = pendingData.content.map((order: OrderDetails) => ({
                ...order,
                displayStatus: 'PENDING',
            }));

            const realizationOrders: OrderDetails[] = realizationData.content.map((order: OrderDetails) => ({
                ...order,
                displayStatus: 'IN_REALIZATION',
            }));

            const combined = [...pendingOrders, ...realizationOrders];
            setOrderDetails(combined);
        } catch (error) {
            console.error('Error fetching orders:', error);
        }
    };

    const handleAccept = async (orderId: number) => {
        const token = localStorage.getItem('access_token');

        try {
            const response = await fetch(`${API_BASE_URL}/api/orders/${orderId}/accept`, {
                method: 'PATCH',
                headers: {
                    'Authorization': `Bearer ${token}`,
                },
            });

            if (response.ok) {
                setOrderDetails(prev =>
                    prev.map(order =>
                        order.orderId === orderId
                            ? { ...order, status: 'IN_REALIZATION' }
                            : order
                    )
                );
                setMessage('Zamówienie zostało zatwierdzone.');
                setMessageType('success');
            } else {
                setMessage('Nie udało się zatwierdzić zamówienia.');
                setMessageType('error');
            }
        } catch (error) {
            console.error('Błąd przy zatwierdzaniu zamówienia:', error);
        }
    };

    const handleConfirmRejection = async (orderId: number) => {
        const token = localStorage.getItem('access_token');
        const reasonToSend = rejectionReason === 'Inne' ? customReason : rejectionReason;

        if (!reasonToSend || reasonToSend.trim() === '') {
            setMessage('Proszę podać powód odrzucenia.');
            setMessageType('error');
            return;
        }

        try {
            const response = await fetch(`${API_BASE_URL}/api/orders/${orderId}/decline`, {
                method: 'PATCH',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${token}`,
                },
                body: JSON.stringify({ reason: reasonToSend }),
            });

            if (response.ok) {
                setMessage('Zamówienie zostało odrzucone.');
                setMessageType('success');
                setShowRejectionInput(false);
                setSelectedOrderId(null);
                setRejectionReason('');
                setCustomReason('');
                fetchOrderDetails();
            } else {
                setMessage('Błąd przy odrzucaniu zamówienia.');
                setMessageType('error');
            }
        } catch (err) {
            console.error('Error declining order:', err);
        }
    };

    // Returns ---------------------------------------------------------------------------------------------------------
    const fetchRentalReturnDetailsInPerson = async (rentalReturnId: number) => {
        const token = localStorage.getItem('access_token');
        if (!token) return;

        try {
            const response = await fetch(`${API_BASE_URL}/api/rental-returns/${rentalReturnId}`, {
                headers: {
                    'Authorization': `Bearer ${token}`,
                },
            });
            const data = await response.json();
            setRentalReturnDetails(data);
        } catch (error) {
            console.error('Error fetching rental return details:', error);
        }
    };

    const completeInPersonReturn = async (rentalReturnId: number) => {
        const token = localStorage.getItem('access_token');
        if (!token) return;

        try {
            const response = await fetch(`${API_BASE_URL}/api/rental-returns/${rentalReturnId}/complete-in-person`, {
                method: 'PATCH',
                headers: {
                    'Authorization': `Bearer ${token}`,
                },
            });

            if (response.ok) {
                //setReturnStatus('COMPLETED');
                setMessage('Return completed successfully.');
                setMessageType('success');
            } else {
                setMessage('Failed to complete the return.');
                setMessageType('error');
            }
        } catch (error) {
            console.error('Error completing return:', error);
            setMessage('Error completing the return.');
            setMessageType('error');
        }
    };

    const fetchRentalReturnDetailsByDriver = async (driverId: string) => {
        const token = localStorage.getItem('access_token');
        if (!token) return;

        try {
            const response = await fetch(`${API_BASE_URL}/api/rental-returns/latest-by-driver/${driverId}`, {
                headers: {
                    'Authorization': `Bearer ${token}`,
                },
            });
            const data = await response.json();
            setRentalReturnDetails(data);
        } catch (error) {
            console.error('Error fetching rental return details by driver:', error);
        }
    };

    const completeDriverReturn = async (rentalReturnId: number) => {
        const token = localStorage.getItem('access_token');
        if (!token) return;

        try {
            const response = await fetch(`${API_BASE_URL}/api/rental-returns/${rentalReturnId}/complete-delivery`, {
                method: 'PATCH',
                headers: {
                    'Authorization': `Bearer ${token}`,
                },
            });

            if (response.ok) {
                //setReturnStatus('COMPLETED');
                setMessage('Return completed successfully.');
                setMessageType('success');
            } else {
                setMessage('Failed to complete the return.');
                setMessageType('error');
            }
        } catch (error) {
            console.error('Error completing return:', error);
            setMessage('Error completing the return.');
            setMessageType('error');
        }
    };

    const handleReturnOptionChange = (option: 'inPerson' | 'driver') => {
        setReturnType(option);
        setRentalReturnId(null);
        setRentalReturnDetails(null);
    };

    const handleFetchReturnDetails = async () => {
        if (!rentalReturnId) return;

        if (returnType === 'inPerson') {
            await fetchRentalReturnDetailsInPerson(rentalReturnId);
        } else if (returnType === 'driver' && driverId) {
            await fetchRentalReturnDetailsByDriver(driverId);
        }
    };

    const handleCompleteReturn = async () => {
        if (!rentalReturnId) return;

        if (returnType === 'inPerson') {
            await completeInPersonReturn(rentalReturnId);
        } else if (returnType === 'driver') {
            await completeDriverReturn(rentalReturnId);
        }
    };

    return (
        <div className="bg-[#314757] min-h-screen">
            <header
                className="flex justify-around p-1 pr-4 space-x-2.5 bg-[#3B576C] text-white sticky top-0 z-[1000] shadow-md">
                <div>
                    <img
                        className="relative w-[80vw] h-auto object-cover left-[1%]"
                        alt="Book Rider Logo"
                        src="/book-rider-high-resolution-logo.png"
                    />
                </div>
                {[
                    {id: 'addBook', label: 'Książki'},
                    {id: 'orders', label: 'Wypożyczenia'},
                    {id: 'returns', label: 'Zwroty'},
                    {id: 'readers', label: 'Czytelnicy'},
                    {id: 'settings', label: 'Ustawienia'},
                    {id: 'logout', label: 'Wyloguj się'},
                ].map(({id, label}) => (
                    <button
                        key={id}
                        onClick={() => id === 'logout' ? handleLogout() : handleSectionChange(id)}
                        className={`w-full px-12 py-2 h-[3vw] self-center rounded border-none cursor-pointer text-[2.5vh] transition-colors ${
                            activeSection === id ? 'bg-[#4B6477]' : 'bg-[#314757]'
                        } hover:bg-[#4B6477] duration-200 ease-out`}
                    >
                        {label}
                    </button>
                ))}
            </header>

            <main className="flex justify-center items-center p-9 w-full max-w-[800vw]">
                {activeSection === "addBook" && (
                    <section className="h-[80%] max-h-[90%] p-9 rounded-2xl mb-[400px] w-[65%] bg-white text-gray-600">
                        <h2 className="text-center p-4 mb-4 text-3xl font-semibold">Wyszukaj książkę</h2>
                        <div className="grid gap-4">
                            <input
                                type="text"
                                list="bookTitles"
                                placeholder="Tytuł"
                                value={bookSearchInput}
                                onChange={(e) => {
                                    setBookSearchInput(e.target.value);
                                    fetchBookTitles(e.target.value);
                                }}
                                className="w-full p-2 rounded-lg border-2 outline-none bg-white text-[#3b4248] focus:outline-none focus:ring-2 focus:ring-[#3B576C]"
                            />
                            <datalist id="bookTitles">
                                {bookTitleOptions.map((option) => (
                                    <option key={option} value={option}/>
                                ))}
                            </datalist>

                            <input
                                type="text"
                                list="publishers"
                                placeholder="Wydawca"
                                value={publisherInput}
                                onChange={(e) => {
                                    setPublisherInput(e.target.value);
                                    fetchPublishers(e.target.value);
                                }}
                                className="w-full p-2 rounded-lg border-2 outline-none bg-white text-[#3b4248] focus:outline-none focus:ring-2 focus:ring-[#3B576C]"
                            />
                            <datalist id="publishers">
                                {publisherOptions.map((option) => (
                                    <option key={option} value={option}/>
                                ))}
                            </datalist>

                            <input
                                type="text"
                                list="authors"
                                placeholder="Autor"
                                value={authorInput}
                                onChange={(e) => {
                                    setAuthorInput(e.target.value);
                                    fetchAuthors(e.target.value);
                                }}
                                className="w-full p-2 rounded-lg border-2 outline-none bg-white text-[#3b4248] focus:outline-none focus:ring-2 focus:ring-[#3B576C]"
                            />
                            <datalist id="authors">
                                {authorOptions.map((option) => (
                                    <option key={option} value={option}/>
                                ))}
                            </datalist>

                            <input
                                type="text"
                                list="categories"
                                placeholder="Kategoria"
                                value={categoryInput}
                                onChange={(e) => setCategoryInput(e.target.value)}
                                className="w-full p-2 rounded-lg border-2 outline-none bg-white text-[#3b4248] focus:outline-none focus:ring-2 focus:ring-[#3B576C]"
                            />
                            <datalist id="categories">
                                {categoryOptions.map((option) => (
                                    <option key={option} value={option}/>
                                ))}
                            </datalist>

                            <input
                                type="text"
                                list="languages"
                                placeholder="Język"
                                value={languageInput}
                                onChange={(e) => setLanguageInput(e.target.value)}
                                className="w-full p-2 rounded-lg border-2 outline-none bg-white text-[#3b4248] focus:outline-none focus:ring-2 focus:ring-[#3B576C]"
                            />
                            <datalist id="languages">
                                {languageOptions.map((option) => (
                                    <option key={option} value={option}/>
                                ))}
                            </datalist>

                            <input
                                type="text"
                                placeholder="ISBN"
                                value={isbnInput}
                                onChange={(e) => setIsbnInput(e.target.value)}
                                className="w-full p-2 rounded-lg border-2 outline-none bg-white text-[#3b4248] focus:outline-none focus:ring-2 focus:ring-[#3B576C]"
                            />
                            <input
                                type="number"
                                placeholder="Rok wydania (od)"
                                value={releaseYearFrom}
                                onChange={(e) => setReleaseYearFrom(e.target.value)}
                                className="w-full p-2 rounded-lg border-2 outline-none bg-white text-[#3b4248] focus:outline-none focus:ring-2 focus:ring-[#3B576C]"
                            />
                            <input
                                type="number"
                                placeholder="Rok wydania (do)"
                                value={releaseYearTo}
                                onChange={(e) => setReleaseYearTo(e.target.value)}
                                className="w-full p-2 rounded-lg border-2 outline-none bg-white text-[#3b4248] focus:outline-none focus:ring-2 focus:ring-[#3B576C]"
                            />

                            <div className="flex items-center gap-2">
                                <input
                                    type="checkbox"
                                    checked={isUserLibraryChecked}
                                    onChange={() => setIsUserLibraryChecked(!isUserLibraryChecked)}
                                    className="cursor-pointer accent-[#3B576C]"
                                />
                                <label>Wyszukaj w mojej bibliotece</label>
                            </div>

                            <div className="flex justify-center">
                                <button
                                    onClick={() => handleSearch(true)}
                                    className="w-[11vw] p-2 bg-[#3B576C] text-white rounded-md cursor-pointer hover:bg-[#314757] duration-200 ease-out">
                                    Szukaj
                                </button>
                            </div>
                        </div>

                        {searchResults.length > 0 ? (
                            <ul className="mt-12 bg-white p-3 rounded max-h-[40vw] overflow-y-auto">
                                {searchResults.map((book, index) => (
                                    <li key={index} className="p-5 border-b border-gray-600 flex items-center gap-3">
                                        <img src={book.image} alt={book.title} className="w-[7vw] h-[10vw]"/>
                                        <div className="space-y-1 ">
                                            <h3 className="text-xl font-bold mb-3">{book.title} ({book.releaseYear})</h3>
                                            <p className="text-thin">Autor: {book.authorNames.join(", ")}</p>
                                            <p className="text-thin">Kategoria: {book.categoryName}</p>
                                            <p className="text-thin">Język: {book.languageName}</p>
                                            <p className="text-thin">Wydawnictwo: {book.publisherName}</p>
                                            <p className="text-thin">ISBN: {book.isbn}</p>
                                            <p className="text-thin">ID książki: {book.id}</p>
                                        </div>
                                    </li>
                                ))}
                            </ul>
                        ) : (
                            <div className="mt-5 p-3 text-lg border-2 rounded-lg text-center">
                                <p>Nie znaleziono książki.</p>
                            </div>
                        )}

                        <div className="mt-5 p-3 text-lg border-2 rounded-xl text-center">
                            <p>Nie możesz znaleźć, czego szukasz?</p>
                            <button
                                onClick={handleRedirectToAddBook}
                                className="w-full mt-3 p-2 bg-[#3B576C] text-white rounded-md cursor-pointer hover:bg-[#314757] duration-200 ease-out">
                                Dodaj nową książkę
                            </button>
                        </div>

                    </section>
                )}

                {activeSection === 'readers' && (
                    <div className="bg-white p-9 rounded-2xl shadow-md h-[80%] max-h-[90%] w-[65%]">
                        <h3 className="text-3xl p-4 font-semibold mb-11 text-gray-600 text-center">Czytelnicy</h3>

                        <div className="mb-6">
                            <h4 className="text-2xl font-thin text-gray-700 mb-8">Dodaj nowego użytkownika
                                BookRider:</h4>
                            <div className="space-y-5 text-lg">
                                <div>
                                    <label htmlFor="userId" className="block text-gray-600 font-medium mb-2">Identyfikator
                                        użytkownika:</label>
                                    <input
                                        id="userId"
                                        type="text"
                                        placeholder="Wprowadź ID użytkownika"
                                        value={userId}
                                        onChange={(e) => setUserId(e.target.value)}
                                        className="w-full p-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-[#3B576C]"
                                    />
                                </div>
                                <div>
                                    <label htmlFor="cardId" className="block text-gray-600 font-medium mb-2">Identyfikator
                                        karty:</label>
                                    <input
                                        id="cardId"
                                        type="text"
                                        placeholder="Wprowadź ID karty"
                                        value={cardId}
                                        onChange={(e) => setCardId(e.target.value)}
                                        className="w-full p-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-[#3B576C]"
                                    />
                                </div>
                                <div>
                                    <label htmlFor="firstName"
                                           className="block text-gray-600 font-medium mb-2">Imię:</label>
                                    <input
                                        id="firstName"
                                        type="text"
                                        placeholder="Wprowadź imię użytkownika"
                                        value={firstName}
                                        onChange={(e) => setFirstName(e.target.value)}
                                        className="w-full p-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-[#3B576C]"
                                    />
                                </div>
                                <div>
                                    <label htmlFor="lastName"
                                           className="block text-gray-600 font-medium mb-2">Nazwisko:</label>
                                    <input
                                        id="lastName"
                                        type="text"
                                        placeholder="Wprowadź nazwisko użytkownika"
                                        value={lastName}
                                        onChange={(e) => setLastName(e.target.value)}
                                        className="w-full p-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-[#3B576C]"
                                    />
                                </div>
                                <div>
                                    <label htmlFor="expirationDate" className="block text-gray-600 font-medium mb-2">Data
                                        ważności karty bibliotecznej:</label>
                                    <input
                                        id="expirationDate"
                                        type="date"
                                        value={expirationDate}
                                        onChange={(e) => setExpirationDate(e.target.value)}
                                        className="mb-5 w-full p-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-[#3B576C]"
                                    />
                                </div>
                                <div className="flex justify-center mt-4">
                                    <button
                                        onClick={handleCreateLibraryCard}
                                        className="mb-12 mt-5 w-[15vw] py-2 px-4 bg-[#3B576C] text-white rounded-md hover:bg-[#314757] ease-out duration-300"
                                    >
                                        Dodaj
                                    </button>
                                </div>
                            </div>
                        </div>


                        <div className="border-t border-gray-300 my-8 mb-[6%]"></div>

                        <div className="mb-6">
                            <h4 className="text-2xl font-thin text-gray-700 mb-8">Wyszukaj użytkownika:</h4>
                            <div className="space-y-4 text-lg">
                                <div>
                                    <label htmlFor="libraryCardSearchId"
                                           className="block text-gray-600 font-medium mb-2">
                                        Wprowadź identyfikator użytkownika
                                    </label>
                                    <input
                                        id="libraryCardSearchId"
                                        type="text"
                                        placeholder="ID użytkownika"
                                        value={libraryCardSearchId}
                                        onChange={(e) => setLibraryCardSearchId(e.target.value)}
                                        className="mb-5 w-full p-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-[#3B576C]"
                                    />
                                </div>
                                <div className="flex justify-center mt-4">
                                    <button
                                        onClick={handleSearchLibraryCard}
                                        className="w-[15vw] py-2 px-4 mt-5 bg-[#3B576C] text-white rounded-md hover:bg-[#314757] ease-out duration-300"
                                    >
                                        Wyszukaj
                                    </button>
                                </div>
                            </div>
                        </div>

                        {libraryCardDetails && (
                            <div className="mb-6 border border-gray-300 p-4 rounded-2xl">
                                <h4 className="text-xl font-medium text-gray-700 mb-4">
                                    Szczegóły wyszukiwanego konta:
                                </h4>
                                <p><strong>ID użytkownika:</strong> {libraryCardDetails.userId}</p>
                                <p><strong>ID karty:</strong> {libraryCardDetails.cardId}</p>
                                <p><strong>Imię:</strong> {libraryCardDetails.firstName}</p>
                                <p><strong>Nazwisko:</strong> {libraryCardDetails.lastName}</p>
                                <p><strong>Data ważności karty
                                    bibliotecznej:</strong> {libraryCardDetails.expirationDate}</p>
                            </div>
                        )}
                    </div>
                )}

                {activeSection === 'settings' && (
                    <section className="p-5 rounded-md mb-[400px] h-[80%] max-h-[90%] w-[65%]">
                        <h2 className="text-center text-white">Ustawienia</h2>
                    </section>
                )}

                {activeSection === 'orders' && orderDetails && (
                    <section className="p-5 rounded-md mb-12 h-[80%] max-h-[90%] w-[70%] overflow-y-auto">
                        {orderDetails.length > 0 ? (
                            orderDetails
                                .filter((order) => order.status !== 'COMPLETED')
                                .map((order) => (
                                    <div key={order.orderId} className="order-details-container mb-10">
                                        <div
                                            className={`p-10 rounded-2xl shadow-lg ${
                                                order.displayStatus === 'IN_REALIZATION' ? 'bg-gray-300' : 'bg-white'
                                            }`}
                                        >
                                            <div className="flex justify-between items-center mb-7">
                                                <h1 className="text-3xl font-bold text-gray-700">
                                                    Zamówienie nr: {order.orderId}
                                                </h1>
                                                <p className="text-xl font-thin text-gray-600">
                                                    <strong>Status:</strong>{' '}
                                                    {order.displayStatus === 'PENDING'
                                                        ? 'Oczekujące'
                                                        : order.displayStatus === 'IN_REALIZATION'
                                                            ? 'W realizacji'
                                                            : 'Nieznany'}
                                                </p>
                                            </div>

                                            <p className="border-t text-lg border-gray-400 text-gray-700 pt-5 "><strong>ID
                                                użytkownika:</strong> {order.userId}</p>
                                            <p className="text-gray-700 text-lg"><strong>Data
                                                utworzenia:</strong> {new Date(order.createdAt).toLocaleString()}</p>

                                            {order.orderItems.map((item, index) => (
                                                <div key={index} className=" mt-4 pt-4">
                                                    <div className="flex justify-center mb-4">
                                                        <img
                                                            src={item.book.image}
                                                            alt={item.book.title}
                                                            className="w-45 h-65 border-gray-400 border rounded-xl object-cover"
                                                        />
                                                    </div>

                                                    <div className="space-y-1.5 ml-1">
                                                        <h2 className="text-3xl mb-5 mt-12 font-semibold text-gray-800">{item.book.title}</h2>
                                                        <p className="text-gray-600 text-lg">
                                                            <strong>Autor:</strong> {item.book.authorNames.join(', ')}
                                                        </p>
                                                        <p className="text-gray-600 text-lg">
                                                            <strong>ISBN:</strong> {item.book.isbn}</p>
                                                        <p className="text-gray-600 text-lg">
                                                            <strong>Kategoria:</strong> {item.book.categoryName}</p>
                                                        <p className="text-gray-600 text-lg"><strong>Rok
                                                            wydania:</strong> {item.book.releaseYear}</p>
                                                        <p className="text-gray-600 text-lg">
                                                            <strong>Wydawnictwo:</strong> {item.book.publisherName}</p>
                                                        <p className="text-gray-600 text-lg">
                                                            <strong>Język:</strong> {item.book.languageName}</p>
                                                        <p className="text-gray-600 text-lg"><strong>ID
                                                            książki:</strong> {item.book.id}</p>
                                                        <p className="text-gray-600 text-lg"><strong>Zamówiona
                                                            ilość:</strong> {item.quantity}</p>
                                                    </div>
                                                </div>
                                            ))}

                                            {order.displayStatus !== 'IN_REALIZATION' && (
                                                <div className="flex justify-between mt-6">
                                                    <button
                                                        onClick={() => handleAccept(order.orderId)}
                                                        className="bg-[#3E4851] text-white px-7 py-2 rounded-lg border-2 border-[#23292F]"
                                                    >
                                                        Zatwierdź
                                                    </button>

                                                    {selectedOrderId !== order.orderId && (
                                                        <button
                                                            onClick={() => {
                                                                setSelectedOrderId(order.orderId);
                                                                setShowRejectionInput(true);
                                                                setRejectionReason('');
                                                                setCustomReason('');
                                                            }}
                                                            className="bg-gray-200 text-[#2D343A] px-5 py-2 rounded-lg border-2 border-[#314757]"
                                                        >
                                                            Odrzuć
                                                        </button>
                                                    )}
                                                </div>
                                            )}

                                            {showRejectionInput && selectedOrderId === order.orderId && (
                                                <div className="mt-12">
                                                    <h3 className="text-gray-600 text-lg font-semibold">Wybierz
                                                        przyczynę odmowy:</h3>
                                                    <form className="flex flex-col items-start gap-2 mt-2">
                                                        <label className="flex text-lg items-center text-gray-600">
                                                            <input
                                                                type="radio"
                                                                value="Brak w zbiorach biblioteki"
                                                                checked={rejectionReason === 'Brak w zbiorach biblioteki'}
                                                                onChange={(e) => setRejectionReason(e.target.value)}
                                                                className="peer hidden"
                                                            />
                                                            <span
                                                                className="w-5 h-5 border-2 rounded-full mr-2 inline-block bg-white
                                                                 peer-checked:bg-[#3B576C] peer-checked:ring-1 peer-checked:ring-[#3B576C]"
                                                            ></span>
                                                            brak w zbiorach biblioteki
                                                        </label>

                                                        <label className="flex text-lg items-center text-gray-600">
                                                            <input
                                                                type="radio"
                                                                value="Wszystkie egzemplarze zostały wypożyczone"
                                                                checked={rejectionReason === 'Wszystkie egzemplarze zostały wypożyczone'}
                                                                onChange={(e) => setRejectionReason(e.target.value)}
                                                                className="peer hidden"
                                                            />
                                                            <span
                                                                className="w-5 h-5 border-2 rounded-full mr-2 inline-block bg-white
                                                                 peer-checked:bg-[#3B576C] peer-checked:ring-1 peer-checked:ring-[#3B576C]"
                                                            ></span>
                                                            wszystkie egzemplarze zostały wypożyczone
                                                        </label>

                                                        <label className="flex text-lg items-center text-gray-600">
                                                            <input
                                                                type="radio"
                                                                value="Inne"
                                                                checked={rejectionReason === 'Inne'}
                                                                onChange={(e) => {
                                                                    setRejectionReason(e.target.value);
                                                                    setCustomReason('');
                                                                }}
                                                                className="peer hidden"
                                                            />
                                                            <span
                                                                className="w-5 h-5 border-2 rounded-full mr-2 inline-block bg-white
                                                                 peer-checked:bg-[#3B576C] peer-checked:ring-1 peer-checked:ring-[#3B576C]"
                                                            ></span>
                                                            inne
                                                        </label>

                                                        {rejectionReason === 'Inne' && (
                                                            <input
                                                                type="text"
                                                                className="border text-gray-600 border-gray-400 p-2 rounded-lg mt-2
                                                                 w-full mb-2 focus:outline-none focus:ring-2 focus:ring-[#3B576C]"
                                                                placeholder="Podaj powód odrzucenia"
                                                                value={customReason}
                                                                onChange={(e) => setCustomReason(e.target.value)}
                                                            />
                                                        )}
                                                    </form>

                                                    {message && (
                                                        <div
                                                            className={`mb-4 mt-5 p-1 text-lg ${
                                                                messageType === 'success' ? 'text-[#314757]' : 'text-red-600'
                                                            }`}
                                                        >
                                                            {message}
                                                        </div>
                                                    )}

                                                    <button
                                                        className="bg-gray-200 text-[#2D343A] px-5 py-2 rounded-lg border-2 border-[#314757] mt-4"
                                                        onClick={() => handleConfirmRejection(order.orderId)}
                                                    >
                                                        Potwierdź odrzucenie
                                                    </button>
                                                </div>
                                            )}
                                        </div>
                                    </div>
                                ))
                        ) : (
                            <p className="text-white">Brak zamówień do wyświetlenia.</p>
                        )}
                    </section>
                )}

                {activeSection === 'returns' && (
                    <div className="bg-white p-9 rounded-2xl shadow-md h-[80%] max-h-[90%] w-[65%]">
                        <h3 className="text-3xl p-4 font-semibold mb-20 text-gray-600 text-center">Zwroty</h3>

                        <div className="mb-20">
                            <h4 className="text-2xl font-thin text-gray-700 mb-8">Wybierz rodzaj zwrotu:</h4>
                            <div className="flex justify-center space-x-8 w-full">
                                <button
                                    onClick={() => handleReturnOptionChange('inPerson')}
                                    className={`w-full py-3 px-6 rounded-lg ${returnType === 'inPerson' ? 'bg-[#4B6477] text-white' : 'bg-gray-200 text-gray-800'} sm:w-[75%] md:w-[70%] lg:w-[60%]`}
                                >
                                    Zwrot osobiście
                                </button>

                                <button
                                    onClick={() => handleReturnOptionChange('driver')}
                                    className={`w-full py-3 px-6 rounded-lg ${returnType === 'driver' ? 'bg-[#4B6477] text-white' : 'bg-gray-200 text-gray-800'} sm:w-[75%] md:w-[70%] lg:w-[60%]`}
                                >
                                    Zwrot przez kierowcę
                                </button>
                            </div>
                        </div>

                        <div className="border-t border-gray-300 my-8 mb-[8%]"></div>

                        <div className="mb-6">
                            <h4 className="text-2xl font-thin text-gray-700 mb-8">Wprowadź wymagane dane:</h4>
                            <div className="space-y-4">
                                <div>
                                    <label htmlFor="rentalReturnId" className="block text-gray-600 font-medium mb-2">ID
                                        zwrotu</label>
                                    <input
                                        id="rentalReturnId"
                                        type="text"
                                        placeholder="Wprowadź ID zwrotu"
                                        value={rentalReturnId || ''}
                                        onChange={(e) => setRentalReturnId(Number(e.target.value))}
                                        className="w-full p-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-[#3B576C]"
                                    />
                                </div>

                                {returnType === 'driver' && (
                                    <div>
                                        <label htmlFor="driverId" className="block text-gray-600 font-medium mb-2">ID
                                            kierowcy</label>
                                        <input
                                            id="driverId"
                                            type="text"
                                            placeholder="Wprowadź ID kierowcy"
                                            value={driverId || ''}
                                            onChange={(e) => setDriverId(e.target.value)}
                                            className="w-full p-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-[#3B576C]"
                                        />
                                    </div>
                                )}

                                <div className="flex justify-center mt-4">
                                    <button
                                        onClick={handleFetchReturnDetails}
                                        className="mb-6 mt-5 w-[15vw] py-2 px-4 bg-[#3B576C] text-white rounded-md cursor-pointer hover:bg-[#314757] duration-200 ease-out"
                                    >
                                        Zwróć
                                    </button>
                                </div>
                            </div>
                        </div>

                        {rentalReturnDetails && (
                            <div className="mb-6 border border-gray-300 p-4 rounded-2xl">
                                <h4 className="text-xl font-medium text-gray-700 mb-4">Szczegóły zwrotu</h4>
                                <p><strong>ID zwrotu:</strong> {rentalReturnDetails.id}</p>
                                <p><strong>Data zwrotu:</strong> {rentalReturnDetails.returnedAt}</p>
                                <p><strong>Status:</strong> {rentalReturnDetails.status}</p>

                                <div className="mt-4">
                                    <h5 className="text-lg font-medium text-gray-700">Zwrócone książki:</h5>
                                    <ul>
                                        {rentalReturnDetails.rentalReturnItems.map((item) => (
                                            <li key={item.id}>
                                                <strong>{item.book.title}</strong> ({item.returnedQuantity} szt.)
                                            </li>
                                        ))}
                                    </ul>
                                </div>

                                <div className="flex justify-center mt-4">
                                    <button
                                        onClick={handleCompleteReturn}
                                        className="w-[15vw] py-2 px-4 bg-[#3B576C] text-white rounded-md hover:bg-[#314757] ease-out duration-300"
                                    >
                                        Zatwierdź
                                    </button>
                                </div>
                            </div>
                        )}

                        {message && (
                            <div
                                className={`mt-4 p-4 rounded-md ${messageType === 'success' ? 'bg-green-100 text-[#3B576C]' : 'bg-red-100 text-red-800'}`}>
                                {message}
                            </div>
                        )}
                    </div>
                )}

            </main>
        </div>
    );
};

export default LibrarianSettings;
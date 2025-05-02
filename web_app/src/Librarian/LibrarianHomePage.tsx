import React, {useEffect, useState} from 'react';
import {Link, useNavigate} from 'react-router-dom';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL;

const LibrarianHomePage: React.FC = () => {

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

    interface Library {
        id: number;
        name: string;
    }

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
    const [selectedBooks, setSelectedBooks] = useState<number[]>([]);

    const [assignedLibrary, setAssignedLibrary] = useState<Library | null>(null);
    const [isUserLibraryChecked, setIsUserLibraryChecked] = useState(false);

    const [categoryOptions, setCategoryOptions] = useState<string[]>([]);
    const [languageOptions, setLanguageOptions] = useState<string[]>([]);
    const [bookTitleOptions, setBookTitleOptions] = useState<string[]>([]);
    const [authorOptions, setAuthorOptions] = useState<string[]>([]);
    const [publisherOptions, setPublisherOptions] = useState<string[]>([]);

    const [addBooksMessage, setAddBooksMessage] = useState<{ text: string; type: "success" | "error" | null }>({ text: "", type: null });

    const navigate = useNavigate();

    useEffect(() => {
        fetchAssignedLibrary();
        fetchDropdownData();
    }, []);

    useEffect(() => {
        if (assignedLibrary !== null) {
            fetchBooks(isUserLibraryChecked);
        }
    }, [isUserLibraryChecked, assignedLibrary]);

    useEffect(() => {
        if (isUserLibraryChecked) {
            setSelectedBooks([]);
        }
    }, [isUserLibraryChecked]);

    useEffect(() => {
        if (addBooksMessage.type) {
            const handleClick = () => {
                setAddBooksMessage({ text: "", type: null });
            };

            document.addEventListener("click", handleClick);

            return () => {
                document.removeEventListener("click", handleClick);
            };
        }
    }, [addBooksMessage]);

    const fetchDropdownData = async () => {
        const token = localStorage.getItem('access_token');

        try {
            const [
                categoriesRes,
                languagesRes
            ] = await Promise.all([
                fetch(`${API_BASE_URL}/api/categories`, {headers: {'Authorization': `Bearer ${token}`}}),
                fetch(`${API_BASE_URL}/api/languages`, {headers: {'Authorization': `Bearer ${token}`}}),
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
            console.error("Error: ", error);
        }
    };

    const fetchAssignedLibrary = async () => {
        const token = localStorage.getItem('access_token');
        try {
            const response = await fetch(`${API_BASE_URL}/api/libraries/assigned`, {
                headers: {
                    'Authorization': `Bearer ${token}`,
                },
            });
            const data: Library = await response.json();
            setAssignedLibrary(data);
        } catch (error) {
            console.error("Error:", error);
        }
    };

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
                headers: {'Authorization': `Bearer ${token}`},
            });

            if (response.ok) {
                const data: Publisher[] = await response.json();
                setPublisherOptions(data.map((p) => p.name));
            }
        } catch (error) {
            console.error("Error:", error);
        }
    };

    const fetchBooks = async (filterByLibrary: boolean) => {
        const token = localStorage.getItem('access_token');
        if (!token) return;

        const queryParams = new URLSearchParams();

        if (filterByLibrary && assignedLibrary?.name) {
            queryParams.append("library", assignedLibrary.name);
        }

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
                throw new Error(`Error: ${response.statusText}`);
            }

            const data = await response.json();
            setBookSearchResults(data.content);
        } catch (error) {
            console.error("Error: ", error);
        }
    };

    const handleSearch = (reset: boolean = false) => {
        if (reset) {
            setBookSearchResults([]);
        }
        fetchBooks(isUserLibraryChecked);
    };

    const handleRedirectToAddBook = () => {
        navigate('/add-book');
    };

    const toggleBookSelection = (bookId: number) => {
        setSelectedBooks((prevSelected) =>
            prevSelected.includes(bookId)
                ? prevSelected.filter((id) => id !== bookId)
                : [...prevSelected, bookId]
        );
    };

    const handleAddSelectedBooks = async () => {
        const token = localStorage.getItem('access_token');
        if (!token) return;

        if (!assignedLibrary || !assignedLibrary.id) {
            console.error("No library ID available.");
            return;
        }

        try {
            const responses = await Promise.all(
                selectedBooks.map(async (bookId) => {
                    const response = await fetch(`${API_BASE_URL}/api/books/add-existing/${bookId}?libraryId=${assignedLibrary.id}`, {
                        method: "POST",
                        headers: {
                            Authorization: `Bearer ${token}`,
                        },
                    });

                    return response;
                })
            );

            const allSuccessful = responses.every(response => response.ok);

            if (allSuccessful) {
                setAddBooksMessage({ text: "Pomyślnie dodano książki do biblioteki.", type: "success" });
            } else {
                setAddBooksMessage({ text: "Niektóre z książek już wcześniej zostały przypisane do Twojej biblioteki.", type: "error" });
            }

            setSelectedBooks([]);
        } catch {
            setAddBooksMessage({ text: "Wystąpił błąd podczas dodawania książek.", type: "error" });
        }
    };

    const handleLogout = () => {
        localStorage.removeItem('username');
        localStorage.removeItem('email');
        localStorage.removeItem('access_token');
        localStorage.removeItem('role');

        navigate('/');
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
                    {id: 'addBook', label: 'Książki', path: '/librarian-dashboard'},
                    {id: 'orders', label: 'Wypożyczenia', path: '/orders'},
                    {id: 'returns', label: 'Zwroty', path: '/returns'},
                    {id: 'readers', label: 'Czytelnicy', path: '/readers'},
                    {id: 'settings', label: 'Ustawienia', path: '/librarian-settings'},
                ].map(({id, label, path}) => (
                    <Link
                        key={id}
                        to={path}
                        className="w-full px-12 py-2 h-[3vw] self-center rounded border-none cursor-pointer text-[2.5vh] transition-colors bg-[#314757] hover:bg-[#4B6477] duration-200 ease-out flex justify-center items-center"
                    >
                        {label}
                    </Link>
                ))}
                <button
                    onClick={handleLogout}
                    className="w-full px-12 py-2 h-[3vw] self-center rounded border-none cursor-pointer text-[2.5vh] transition-colors bg-[#314757] hover:bg-[#4B6477] duration-200 ease-out"
                >
                    Wyloguj się
                </button>
            </header>

            <main className="flex justify-center items-center p-9 w-full max-w-[800vw]">
                <section className="h-[80%] max-h-[90%] p-9 rounded-2xl mb-[400px] w-[65%] bg-white text-gray-600">
                    <h2 className="text-center p-4 mb-4 text-3xl font-semibold">Wyszukaj książkę</h2>
                    <div className="grid gap-4">
                        <div className="relative">
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
                            {bookSearchInput && (
                                <button
                                    onClick={() => setBookSearchInput('')}
                                    className="absolute right-3 top-1/2 transform -translate-y-1/2 text-gray-400 text-4xl"
                                >
                                    ×
                                </button>
                            )}
                        </div>
                        <datalist id="bookTitles">
                            {bookTitleOptions.map((option) => (
                                <option key={option} value={option}/>
                            ))}
                        </datalist>

                        <div className="relative">
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
                            {publisherInput && (
                                <button
                                    onClick={() => setPublisherInput('')}
                                    className="absolute right-3 top-1/2 transform -translate-y-1/2 text-gray-400 text-4xl"
                                >
                                    ×
                                </button>
                            )}
                        </div>
                        <datalist id="publishers">
                            {publisherOptions.map((option) => (
                                <option key={option} value={option}/>
                            ))}
                        </datalist>

                        <div className="relative">
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
                            {authorInput && (
                                <button
                                    onClick={() => setAuthorInput('')}
                                    className="absolute right-3 top-1/2 transform -translate-y-1/2 text-gray-400 text-4xl"
                                >
                                    ×
                                </button>
                            )}
                        </div>
                        <datalist id="authors">
                            {authorOptions.map((option) => (
                                <option key={option} value={option}/>
                            ))}
                        </datalist>

                        <div className="relative">
                            <input
                                type="text"
                                list="categories"
                                placeholder="Kategoria"
                                value={categoryInput}
                                onChange={(e) => setCategoryInput(e.target.value)}
                                className="w-full p-2 rounded-lg border-2 outline-none bg-white text-[#3b4248] focus:outline-none focus:ring-2 focus:ring-[#3B576C]"
                            />
                            {categoryInput && (
                                <button
                                    onClick={() => setCategoryInput('')}
                                    className="absolute right-3 top-1/2 transform -translate-y-1/2 text-gray-400 text-4xl"
                                >
                                    ×
                                </button>
                            )}
                        </div>
                        <datalist id="categories">
                            {categoryOptions.map((option) => (
                                <option key={option} value={option}/>
                            ))}
                        </datalist>

                        <div className="relative">
                            <input
                                type="text"
                                list="languages"
                                placeholder="Język"
                                value={languageInput}
                                onChange={(e) => setLanguageInput(e.target.value)}
                                className="w-full p-2 rounded-lg border-2 outline-none bg-white text-[#3b4248] focus:outline-none focus:ring-2 focus:ring-[#3B576C]"
                            />
                            {languageInput && (
                                <button
                                    onClick={() => setLanguageInput('')}
                                    className="absolute right-3 top-1/2 transform -translate-y-1/2 text-gray-400 text-4xl"
                                >
                                    ×
                                </button>
                            )}
                        </div>
                        <datalist id="languages">
                            {languageOptions.map((option) => (
                                <option key={option} value={option}/>
                            ))}
                        </datalist>

                        <div className="relative">
                            <input
                                type="text"
                                placeholder="ISBN"
                                value={isbnInput}
                                onChange={(e) => setIsbnInput(e.target.value)}
                                className="w-full p-2 rounded-lg border-2 outline-none bg-white text-[#3b4248] focus:outline-none focus:ring-2 focus:ring-[#3B576C]"
                            />
                            {isbnInput && (
                                <button
                                    onClick={() => setIsbnInput('')}
                                    className="absolute right-3 top-1/2 transform -translate-y-1/2 text-gray-400 text-4xl"
                                >
                                    ×
                                </button>
                            )}
                        </div>

                        <div className="relative">
                            <input
                                type="text"
                                placeholder="Rok wydania (od)"
                                value={releaseYearFrom}
                                onChange={(e) => setReleaseYearFrom(e.target.value)}
                                className="w-full p-2 rounded-lg border-2 outline-none bg-white text-[#3b4248] focus:outline-none focus:ring-2 focus:ring-[#3B576C]"
                            />
                            {releaseYearFrom && (
                                <button
                                    onClick={() => setReleaseYearFrom('')}
                                    className="absolute right-3 top-1/2 transform -translate-y-1/2 text-gray-400 text-4xl"
                                >
                                    ×
                                </button>
                            )}
                        </div>

                        <div className="relative">
                            <input
                                type="text"
                                placeholder="Rok wydania (do)"
                                value={releaseYearTo}
                                onChange={(e) => setReleaseYearTo(e.target.value)}
                                className="w-full p-2 rounded-lg border-2 outline-none bg-white text-[#3b4248] focus:outline-none focus:ring-2 focus:ring-[#3B576C]"
                            />
                            {releaseYearTo && (
                                <button
                                    onClick={() => setReleaseYearTo('')}
                                    className="absolute right-3 top-1/2 transform -translate-y-1/2 text-gray-400 text-4xl"
                                >
                                    ×
                                </button>
                            )}
                        </div>

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
                                <li
                                    key={index}
                                    onClick={() => !isUserLibraryChecked && toggleBookSelection(book.id)}
                                    className={`relative p-5 flex items-center gap-3 cursor-pointer rounded-lg ${
                                        selectedBooks.includes(book.id) ? "bg-gray-200" : "hover:bg-gray-100"
                                    } ${isUserLibraryChecked ? 'cursor-default' : ''}`}
                                >
                                    <img src={book.image} alt={book.title} className="w-[7vw] h-[10vw]"/>
                                    <div className="space-y-1 flex-1">
                                        <h3 className="text-xl font-bold mb-3 flex items-center justify-between">
                                            {book.title} ({book.releaseYear})
                                            {selectedBooks.includes(book.id) && (
                                                <span className="text-gray-500 text-xl">✔</span>
                                            )}
                                        </h3>
                                        <p className="text-thin">Autor: {book.authorNames.join(", ")}</p>
                                        <p className="text-thin">Kategoria: {book.categoryName}</p>
                                        <p className="text-thin">Język: {book.languageName}</p>
                                        <p className="text-thin">Wydawnictwo: {book.publisherName}</p>
                                        <p className="text-thin">ISBN: {book.isbn}</p>
                                        <p className="text-thin">ID książki: {book.id}</p>
                                    </div>

                                    <div className="absolute bottom-0 left-4 right-4 h-px bg-gray-300"/>
                                </li>
                            ))}
                        </ul>
                    ) : (
                        <div className="mt-5 p-3 text-lg border-2 rounded-lg text-center">
                            <p>Nie znaleziono książki.</p>
                        </div>
                    )}

                    {selectedBooks.length > 0 && !isUserLibraryChecked && (
                        <div className="mt-5 p-3 text-lg border-2 rounded-xl text-center space-y-3">
                            <p className="font-medium">
                                Zaznaczone książki: {selectedBooks.length}
                            </p>

                            <button
                                onClick={() => setSelectedBooks([])}
                                className="text-lg text-gray-400 hover:underline"
                            >
                                Odznacz wszystkie
                            </button>

                            <button
                                onClick={handleAddSelectedBooks}
                                className="w-full mt-3 p-2 bg-gray-500 text-white rounded-md cursor-pointer hover:bg-gray-600 duration-200 ease-out"
                            >
                                Dodaj wybrane książki do biblioteki
                            </button>
                        </div>
                    )}

                    {addBooksMessage.type && (
                        <div
                            className={`mt-5 p-5 text-lg rounded-lg text-center border-2 ${
                                addBooksMessage.type === "success" ? "bg-green-100 text-green-700 border border-green-300" : "bg-red-100 text-red-500 border border-red-300"
                            }`}
                        >
                            {addBooksMessage.text}
                        </div>
                    )}

                    <div className="mt-5 p-3 text-lg border-2 rounded-xl text-center">
                        <p>Nie możesz znaleźć tego, czego szukasz?</p>
                        <button
                            onClick={handleRedirectToAddBook}
                            className="w-full mt-3 p-2 bg-[#3B576C] text-white rounded-md cursor-pointer hover:bg-[#314757] duration-200 ease-out">
                            Dodaj nową książkę
                        </button>
                    </div>

                </section>
            </main>
        </div>
    );
};

export default LibrarianHomePage;
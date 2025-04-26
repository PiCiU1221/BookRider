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

    const [assignedLibrary, setAssignedLibrary] = useState<Library | null>(null);
    const [isUserLibraryChecked, setIsUserLibraryChecked] = useState(false);

    const [categoryOptions, setCategoryOptions] = useState<string[]>([]);
    const [languageOptions, setLanguageOptions] = useState<string[]>([]);
    const [bookTitleOptions, setBookTitleOptions] = useState<string[]>([]);
    const [authorOptions, setAuthorOptions] = useState<string[]>([]);
    const [publisherOptions, setPublisherOptions] = useState<string[]>([]);

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
            console.error("Error fetching dropdown data: ", error);
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
            console.error("Error fetching assigned library:", error);
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
            console.error("Error fetching publishers:", error);
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
                throw new Error(`Error fetching books: ${response.statusText}`);
            }

            const data = await response.json();
            setBookSearchResults(data.content); // Always replace results
        } catch (error) {
            console.error("Error fetching books: ", error);
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
            </main>
        </div>
    );
};

export default LibrarianHomePage;
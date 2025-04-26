import React, { useState, useEffect  } from 'react';
import { useNavigate } from 'react-router-dom';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL;

interface BookData {
    title: string;
    categoryName: string;
    authors: string[];
    releaseYear: number;
    publisher: string;
    isbn: string;
    language: string;
    image?: string;
}

interface Category {
    name: string;
}

interface Language {
    name: string;
}

interface Author {
    name: string;
}

interface Publisher {
    name: string;
}

const LibrarianAddBook: React.FC = () => {
    const [title, setTitle] = useState('');
    const [categoryName, setCategoryName] = useState('');
    const [releaseYear, setReleaseYear] = useState('');
    const [publisher, setPublisher] = useState('');
    const [isbn, setIsbn] = useState('');
    const [language, setLanguage] = useState('');
    const [image, setImage] = useState('');
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState('');
    const [authors, setAuthors] = useState<string[]>([]);
    const navigate = useNavigate();

    // Dropdown variables
    const [categoryOptions, setCategoryOptions] = useState<string[]>([]);
    const [languageOptions, setLanguageOptions] = useState<string[]>([]);
    const [authorOptions, setAuthorOptions] = useState<string[]>([]);
    const [publisherOptions, setPublisherOptions] = useState<string[]>([]);
    const [authorInput, setAuthorInput] = useState("");
    const [showDropdown, setShowDropdown] = useState(false);
    //const [publisherInput, setPublisherInput] = useState('');
    const [showPublisherDropdown, setShowPublisherDropdown] = useState(false);

    useEffect(() => {
        const fetchDropdownData = async () => {
            const token = localStorage.getItem('access_token');
            if (!token) return;

            try {
                const [categoriesRes, languagesRes] = await Promise.all([
                    fetch(`${API_BASE_URL}/api/categories`, { headers: { 'Authorization': `Bearer ${token}` } }),
                    fetch(`${API_BASE_URL}/api/languages`, { headers: { 'Authorization': `Bearer ${token}` } }),
                ]);

                if (categoriesRes.ok) {
                    const categories: Category[] = await categoriesRes.json();
                    setCategoryOptions(categories.map(c => c.name));
                }
                if (languagesRes.ok) {
                    const languages: Language[] = await languagesRes.json();
                    setLanguageOptions(languages.map(l => l.name));
                }

                await fetchAuthors('');
                await fetchPublishers('');
            } catch (error) {
                console.error("Error fetching dropdown data: ", error);
            }
        };

        fetchDropdownData();
    }, []);

    const handleAuthorSelect = async (author: string) => {
        if (!authors.includes(author)) {
            setAuthors([...authors, author]);
            if (!authorOptions.includes(author)) {
                try {
                    const token = localStorage.getItem('access_token');
                    if (!token) {
                        setError('Brak tokena autoryzacyjnego.');
                        return;
                    }

                    const response = await fetch(`${API_BASE_URL}/api/authors`, {
                        method: 'POST',
                        headers: {
                            'Authorization': `Bearer ${token}`,
                            'Content-Type': 'application/json',
                        },
                        body: JSON.stringify({ name: author }),
                    });

                    if (!response.ok) {
                        const errorText = await response.text();
                        throw new Error(`Błąd ${response.status}: ${errorText}`);
                    }

                    await fetchAuthors('');
                    await fetchPublishers('');

                } catch (err) {
                    console.error("Error adding author:", err);
                    setError('Wystąpił błąd podczas dodawania autora.');
                }
            }
        }
        setAuthorInput("");
        setShowDropdown(false);
    };

    const handleAuthorInput = (e: React.ChangeEvent<HTMLInputElement>) => {
        setAuthorInput(e.target.value);
        setShowDropdown(true);
    };

    const handleRemoveAuthor = (authorToRemove: string) => {
        setAuthors(authors.filter((author) => author !== authorToRemove));
    };

    const fetchAuthors = async (query: string) => {
        const token = localStorage.getItem('access_token');
        const response = await fetch(`${API_BASE_URL}/api/authors/search?name=${query}`, {
            headers: { 'Authorization': `Bearer ${token}` },
        });
        const data: Author[] = await response.json();
        setAuthorOptions(data.map(a => a.name));
    };

    const handlePublisherSelect = async (publisherName: string) => {
        setPublisher(publisherName);
        if (!publisherOptions.includes(publisherName)) {
            try {
                const token = localStorage.getItem('access_token');
                if (!token) return;

                const response = await fetch(`${API_BASE_URL}/api/publishers`, {
                    method: 'POST',
                    headers: {
                        'Authorization': `Bearer ${token}`,
                        'Content-Type': 'application/json',
                    },
                    body: JSON.stringify({ name: publisherName }),
                });

                if (!response.ok) throw new Error(await response.text());

                await fetchPublishers('');
            } catch (err) {
                console.error("Error adding publisher:", err);
            }
        }
        //setPublisherInput('');
        setShowPublisherDropdown(false);
    };

    const fetchPublishers = async (query: string) => {
        const token = localStorage.getItem('access_token');
        const response = await fetch(`${API_BASE_URL}/api/publishers/search?name=${query}`, {
            headers: { 'Authorization': `Bearer ${token}` },
        });
        const data: Publisher[] = await response.json();
        setPublisherOptions(data.map(p => p.name));
    };

    const handleAddBook = async () => {
        if (!title.trim() || !categoryName.trim() || authors.length === 0 || !publisher.trim() || !isbn.trim() || !language.trim() || releaseYear === null) {
            setError('Wszystkie pola są wymagane.');
            return;
        }

        setLoading(true);
        setError('');

        const token = localStorage.getItem('access_token');
        if (!token) {
            setError('Brak tokena autoryzacyjnego.');
            setLoading(false);
            return;
        }

        const bookData: BookData = {
            title,
            categoryName,
            authors: authors.map(a => a.trim()),
            releaseYear: Number(releaseYear),
            publisher,
            isbn,
            language,
        };

        if (image.trim()) {
            bookData.image = image;
        }

        try {
            const response = await fetch(`${API_BASE_URL}/api/books`, {
                method: 'POST',
                headers: {
                    'Authorization': `Bearer ${token}`,
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(bookData),
            });

            if (!response.ok) {
                const errorText = await response.text();
                throw new Error(`Błąd ${response.status}: ${errorText}`);
            }

            // alert('Książka dodana');
            navigate('/librarian-dashboard');
        } catch (error) {
            setError((error as Error).message || 'Wystąpił błąd podczas dodawania książki.');
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="bg-[#314757] min-h-screen flex flex-col items-center justify-center text-white">
            <header className="fixed top-0 left-0 w-full flex h-[6vw] justify-between p-4 bg-[#3B576C] shadow-md z-50">
                <img
                    className="relative w-[7vw] h-auto object-cover left-[-0.5%]"
                    alt="Book Rider Logo"
                    src="/book-rider-high-resolution-logo.png"
                />
            </header>

            <div className="mt-20 bg-white p-6 rounded shadow-md w-96 text-gray-800">
                <h2 className="text-center text-lg mb-7">Dodaj książkę</h2>

                <input
                    type="text"
                    placeholder="Tytuł"
                    value={title}
                    onChange={(e) => setTitle(e.target.value)}
                    className="input-field mb-2 border-2 rounded w-full p-1.5"
                />

                <select
                    value={categoryName}
                    onChange={(e) => setCategoryName(e.target.value)}
                    className="input-field mb-2 border-2 rounded w-full p-1.5"
                >
                    <option value="">Wybierz kategorię</option>
                    {categoryOptions.map((category) => (
                        <option key={category} value={category}>{category}</option>
                    ))}
                </select>

                <div className="relative mb-2">
                    <input
                        type="text"
                        placeholder="Dodaj autora"
                        value={authorInput}
                        onChange={handleAuthorInput}
                        onFocus={() => setShowDropdown(true)}
                        className="input-field border-2 rounded w-full p-1.5"
                    />
                    {showDropdown && (
                        <div
                            className="absolute left-0 w-full bg-white border border-gray-300 rounded mt-1 shadow-md z-10">
                            {authorOptions
                                .filter((author) =>
                                    author.toLowerCase().includes(authorInput.toLowerCase())
                                )
                                .map((author) => (
                                    <div
                                        key={author}
                                        onClick={() => handleAuthorSelect(author)}
                                        className="p-2 hover:bg-gray-200 cursor-pointer"
                                    >
                                        {author}
                                    </div>
                                ))}
                            {authorInput && !authorOptions.includes(authorInput) && (
                                <div
                                    onClick={() => handleAuthorSelect(authorInput)}
                                    className="p-2 hover:bg-gray-200 cursor-pointer"
                                >
                                    Dodaj "{authorInput}"
                                </div>
                            )}
                        </div>
                    )}
                </div>

                <div className="flex flex-wrap gap-2 mb-2">
                    {authors.map((author) => (
                        <div key={author} className="flex items-center bg-gray-300 px-2 py-1 rounded">
                            {author}
                            <button
                                onClick={() => handleRemoveAuthor(author)}
                                className="ml-2 text-red-500"
                            >
                                ×
                            </button>
                        </div>
                    ))}
                </div>

                <input
                    type="number"
                    placeholder="Rok wydania"
                    value={releaseYear}
                    onChange={(e) => setReleaseYear(e.target.value)}
                    className="input-field mb-2 border-2 rounded w-full p-1.5"
                />

                <div className="relative mb-2">
                    <input
                        type="text"
                        placeholder="Dodaj wydawnictwo"
                        value={publisher}
                        onChange={(e) => {
                            setPublisher(e.target.value);
                            setShowPublisherDropdown(true);
                        }}
                        onFocus={() => setShowPublisherDropdown(true)}
                        className="input-field border-2 rounded w-full p-1.5"
                    />
                    {showPublisherDropdown && (
                        <div className="absolute left-0 w-full bg-white border border-gray-300 rounded mt-1 shadow-md z-10">
                            {publisherOptions
                                .filter((pub) =>
                                    pub.toLowerCase().includes(publisher.toLowerCase())
                                )
                                .map((pub) => (
                                    <div
                                        key={pub}
                                        onClick={() => handlePublisherSelect(pub)}
                                        className="p-2 hover:bg-gray-200 cursor-pointer"
                                    >
                                        {pub}
                                    </div>
                                ))}
                            {publisher && !publisherOptions.includes(publisher) && (
                                <div
                                    onClick={() => handlePublisherSelect(publisher)}
                                    className="p-2 hover:bg-gray-200 cursor-pointer"
                                >
                                    Dodaj "{publisher}"
                                </div>
                            )}
                        </div>
                    )}
                </div>

                    <input
                        type="text"
                        placeholder="ISBN"
                        value={isbn}
                        onChange={(e) => setIsbn(e.target.value)}
                        className="input-field mb-2 border-2 rounded w-full p-1.5"
                    />

                    <select
                        value={language}
                        onChange={(e) => setLanguage(e.target.value)}
                        className="input-field mb-2 border-2 rounded w-full p-1.5"
                    >
                        <option value="">Wybierz język</option>
                        {languageOptions.map((lang) => (
                            <option key={lang} value={lang}>{lang}</option>
                        ))}
                    </select>

                    <input
                        type="text"
                        placeholder="Obrazek (Base64)"
                        value={image}
                        onChange={(e) => setImage(e.target.value)}
                        className="input-field mb-2 border-2 rounded w-full p-1.5"
                    />

                    {error && <p className="text-red-400 text-sm mb-2">{error}</p>}

                    <button
                        onClick={handleAddBook}
                        disabled={loading}
                        className="w-full px-4 py-2 bg-[#4B6477] text-white rounded cursor-pointer hover:bg-[#2d343a] transition-colors duration-200"
                    >
                        {loading ? 'Dodawanie...' : 'Dodaj'}
                    </button>
                    <button
                        onClick={() => navigate('/librarian-dashboard')}
                        className="w-full mt-3 px-4 py-2 bg-gray-600 text-white rounded cursor-pointer hover:bg-gray-500 transition-colors duration-200"
                    >
                        Powrót
                    </button>
                </div>
            </div>
            );
        };

export default LibrarianAddBook;

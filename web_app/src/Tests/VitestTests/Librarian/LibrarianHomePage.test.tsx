import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import LibrarianHomePage from '../../../Librarian/LibrarianHomePage';
import { BrowserRouter } from 'react-router-dom';

vi.mock('../Utils/useWebSocketNotification.tsx', () => ({
    useWebSocketNotification: vi.fn(),
}));

vi.mock('react-toastify', () => ({
    toast: { info: vi.fn(), success: vi.fn(), error: vi.fn() },
}));

const mockedNavigate = vi.fn();
vi.mock('react-router-dom', async () => {
    const actual = await vi.importActual('react-router-dom');
    return {
        ...actual,
        useNavigate: () => mockedNavigate,
    };
});

vi.stubGlobal('import.meta', { env: { VITE_API_BASE_URL: 'http://localhost:8080' } });

const renderComponent = () => {
    return render(
        <BrowserRouter>
            <LibrarianHomePage />
        </BrowserRouter>
    );
};

const setupFetchMock = () => {
    global.fetch = vi.fn().mockImplementation((url: string, options: any) => {
        if (url.includes('/libraries/assigned')) {
            return Promise.resolve({ ok: true, json: () => Promise.resolve({ id: 99, name: 'Main Library' }) });
        }
        if (url.includes('/api/categories')) {
            return Promise.resolve({ ok: true, json: () => Promise.resolve([{ name: 'Fantasy' }]) });
        }
        if (url.includes('/api/languages')) {
            return Promise.resolve({ ok: true, json: () => Promise.resolve([{ name: 'Polish' }]) });
        }

        if (url.includes('/books/search?')) {
            return Promise.resolve({
                ok: true,
                json: () => Promise.resolve({
                    content: [
                        {
                            id: 101,
                            title: 'The Witcher',
                            authorNames: ['Sapkowski'],
                            releaseYear: 1993,
                            categoryName: 'Fantasy',
                            publisherName: 'SuperNowa',
                            isbn: '12345',
                            languageName: 'Polish',
                            image: 'img.jpg'
                        }
                    ],
                    last: true
                })
            });
        }

        if (url.includes('/books/my-library') && options?.method === 'DELETE') {
            return Promise.resolve({
                ok: true,
                status: 200,
                json: () => Promise.resolve({})
            });
        }

        if (url.includes('/add-existing') && options?.method === 'POST') {
            return Promise.resolve({ ok: true, json: () => Promise.resolve({}) });
        }

        return Promise.resolve({ ok: true, json: () => Promise.resolve([]) });
    }) as any;
};

describe('LibrarianHomePage Integration Tests', () => {

    beforeEach(() => {
        vi.clearAllMocks();
        setupFetchMock();
        Storage.prototype.getItem = vi.fn(() => 'fake_token');
        Storage.prototype.removeItem = vi.fn();
    });

    describe('Initialization & Rendering', () => {
        it('renders the dashboard and fetches initial library data', async () => {
            renderComponent();

            expect(screen.getByText('Wyszukaj książkę')).toBeInTheDocument();
            expect(screen.getByPlaceholderText('Tytuł')).toBeInTheDocument();

            await waitFor(() => {
                expect(global.fetch).toHaveBeenCalledWith(
                    expect.stringContaining('/libraries/assigned'),
                    expect.any(Object)
                );
                expect(global.fetch).toHaveBeenCalledWith(
                    expect.stringContaining('/api/categories'),
                    expect.any(Object)
                );
            });
        });
    });

    describe('Search Functionality', () => {
        it('performs a book search and renders results', async () => {
            renderComponent();

            const titleInput = screen.getByPlaceholderText('Tytuł');
            fireEvent.change(titleInput, { target: { value: 'Witcher' } });

            const searchBtn = screen.getByRole('button', { name: /Szukaj/i });
            fireEvent.click(searchBtn);

            await waitFor(() => {
                expect(screen.getByText('The Witcher (1993)')).toBeInTheDocument();
                expect(screen.getByText('Autor: Sapkowski')).toBeInTheDocument();
            });

            expect(global.fetch).toHaveBeenCalledWith(
                expect.stringContaining('title=Witcher'),
                expect.objectContaining({ method: 'GET' })
            );
        });

        it('handles empty search results gracefully', async () => {
            (global.fetch as any).mockImplementationOnce((url: string) => {
                if (url.includes('/search')) {
                    return Promise.resolve({ ok: true, json: () => Promise.resolve({ content: [], last: true }) });
                }
                return Promise.resolve({ ok: true, json: () => Promise.resolve([]) });
            });

            renderComponent();

            const searchBtn = screen.getByRole('button', { name: /Szukaj/i });
            fireEvent.click(searchBtn);

            await waitFor(() => {
                expect(screen.getByText('Nie znaleziono książki.')).toBeInTheDocument();
            });
        });
    });

    describe('Library Management (Add/Delete)', () => {
        it('allows adding a selected book to the library', async () => {
            renderComponent();

            fireEvent.click(screen.getByRole('button', { name: /Szukaj/i }));
            const bookItem = await screen.findByText('The Witcher (1993)');
            fireEvent.click(bookItem);

            expect(screen.getByText(/Zaznaczone książki: 1/i)).toBeInTheDocument();
            const addBtn = screen.getByRole('button', { name: /Dodaj wybrane książki do biblioteki/i });
            fireEvent.click(addBtn);

            await waitFor(() => {
                expect(global.fetch).toHaveBeenCalledWith(
                    expect.stringContaining('/api/books/add-existing/101?libraryId=99'),
                    expect.objectContaining({ method: 'POST' })
                );
            });

            expect(await screen.findByText('Pomyślnie dodano książki do biblioteki.')).toBeInTheDocument();
        });

        it('allows deleting a book from the library when "search in my library" is checked', async () => {
            renderComponent();

            const myLibCheckbox = screen.getByRole('checkbox');
            fireEvent.click(myLibCheckbox);

            fireEvent.click(screen.getByRole('button', { name: /Szukaj/i }));

            const bookItem = await screen.findByText('The Witcher (1993)');
            fireEvent.click(bookItem);

            const deleteBtn = screen.getByRole('button', { name: /Usuń wybrane książki/i });
            expect(deleteBtn).toBeInTheDocument();
            expect(screen.queryByText(/Dodaj wybrane/i)).not.toBeInTheDocument();

            fireEvent.click(deleteBtn);

            await waitFor(() => {
                expect(global.fetch).toHaveBeenCalledWith(
                    expect.stringContaining('/api/books/my-library/101'),
                    expect.objectContaining({ method: 'DELETE' })
                );
            });

            expect(await screen.findByText('Wybrane książki zostały usunięte z biblioteki.')).toBeInTheDocument();
        });
    });

    describe('Navigation & Auth', () => {
        it('redirects to Add Book page', () => {
            renderComponent();
            const link = screen.getByText('Dodaj nową książkę');
            fireEvent.click(link);
            expect(mockedNavigate).toHaveBeenCalledWith('/add-book');
        });

        it('handles logout correctly', () => {
            renderComponent();

            const logoutBtn = screen.getByRole('button', { name: /Wyloguj się/i });
            fireEvent.click(logoutBtn);

            expect(localStorage.removeItem).toHaveBeenCalledWith('access_token');
            expect(localStorage.removeItem).toHaveBeenCalledWith('role');
            expect(mockedNavigate).toHaveBeenCalledWith('/');
        });
    });
});

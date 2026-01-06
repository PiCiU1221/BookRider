import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { describe, it, expect, vi, beforeEach, type Mock } from 'vitest';
import { BrowserRouter } from 'react-router-dom';
import LibrarianAddBook from '../../../Librarian/LibrarianAddBook';
import { toast } from 'react-toastify';
import { useWebSocketNotification } from '../../../Utils/useWebSocketNotification';

const mocks = vi.hoisted(() => {
    return {
        navigate: vi.fn(),
        wsCallback: vi.fn(),
    };
});

vi.mock('react-router-dom', async () => {
    const actual = await vi.importActual('react-router-dom');
    return {
        ...actual,
        useNavigate: () => mocks.navigate,
    };
});

vi.mock('react-toastify', () => ({
    toast: {
        info: vi.fn(),
        error: vi.fn(),
        success: vi.fn(),
        warning: vi.fn(),
    },
}));

vi.mock('../../../Utils/useWebSocketNotification', () => ({
    useWebSocketNotification: vi.fn(),
}));

const fillFormAndSubmit = async () => {
    fireEvent.change(screen.getByPlaceholderText('Tytuł'), { target: { value: 'Test Book' } });
    fireEvent.change(screen.getByPlaceholderText('ISBN'), { target: { value: '123-456' } });
    fireEvent.change(screen.getByPlaceholderText('Rok wydania'), { target: { value: '2023' } });

    const pubInput = screen.getByPlaceholderText('Dodaj wydawnictwo');
    fireEvent.change(pubInput, { target: { value: 'Publisher 1' } });
    await waitFor(() => fireEvent.click(screen.getByText('Publisher 1')));

    const authorInput = screen.getByPlaceholderText('Dodaj autora');
    fireEvent.change(authorInput, { target: { value: 'Author 1' } });
    await waitFor(() => fireEvent.click(screen.getByText('Author 1')));

    await waitFor(() => {
        fireEvent.change(screen.getByDisplayValue('Wybierz kategorię'), { target: { value: 'Fiction' } });
    });
    await waitFor(() => {
        fireEvent.change(screen.getByDisplayValue('Wybierz język'), { target: { value: 'English' } });
    });

    fireEvent.click(screen.getByText('Dodaj'));
};

const defaultFetch = (url: string | Request, options?: RequestInit) => {
    const urlString = url.toString();

    if (urlString.includes('/categories')) return Promise.resolve({ ok: true, json: async () => [{ id: 1, name: 'Fiction' }] });
    if (urlString.includes('/languages')) return Promise.resolve({ ok: true, json: async () => [{ id: 1, name: 'English' }] });
    if (urlString.includes('/authors')) return Promise.resolve({ ok: true, json: async () => [{ name: 'Author 1' }] });
    if (urlString.includes('/publishers')) return Promise.resolve({ ok: true, json: async () => [{ id: 1, name: 'Publisher 1' }] });

    if (options?.method === 'POST' && urlString.includes('/books')) {
        return Promise.resolve({
            ok: true,
            json: async () => ({ success: true }),
        });
    }
    if (options?.method === 'POST') {
        return Promise.resolve({ ok: true, json: async () => ({}) });
    }

    return Promise.resolve({ ok: true, json: async () => [] });
};

beforeEach(() => {
    vi.clearAllMocks();
    localStorage.setItem('access_token', 'mock-token');
    global.fetch = vi.fn(defaultFetch) as any;
});

describe('LibrarianAddBook', () => {

    it('renders all form fields', () => {
        render(<BrowserRouter><LibrarianAddBook /></BrowserRouter>);
        expect(screen.getByPlaceholderText('Tytuł')).toBeInTheDocument();
    });

    it('shows validation error if required fields are empty', async () => {
        render(<BrowserRouter><LibrarianAddBook /></BrowserRouter>);
        fireEvent.click(screen.getByText('Dodaj'));
        expect(await screen.findByText('Wszystkie pola są wymagane.')).toBeInTheDocument();
    });

    it('submits the form successfully', async () => {
        render(<BrowserRouter><LibrarianAddBook /></BrowserRouter>);
        await fillFormAndSubmit();
        await waitFor(() => {
            expect(mocks.navigate).toHaveBeenCalledWith('/librarian-dashboard');
            expect(toast.success).toHaveBeenCalledWith("Książka została dodana!");
        });
    });

    it('triggers WebSocket toast notification', () => {
        let triggerNotification: ((data: any) => void) | undefined;
        (useWebSocketNotification as Mock).mockImplementation((_path, cb) => {
            triggerNotification = cb;
        });

        render(<BrowserRouter><LibrarianAddBook /></BrowserRouter>);

        if (triggerNotification) triggerNotification({ });
        else throw new Error('Hook not called');

        expect(toast.info).toHaveBeenCalledWith(
            'Otrzymano nowe zamówienie!',
            expect.objectContaining({ position: 'bottom-right' })
        );
    });

    it('displays error for Duplicate ISBN (409 Conflict)', async () => {
        (global.fetch as Mock).mockImplementation(async (url, options) => {
            if (options?.method === 'POST' && url.toString().includes('/books')) {
                return {
                    ok: false,
                    status: 409,
                    statusText: 'Conflict',
                    text: async () => 'Ignored Text',
                };
            }
            return defaultFetch(url, options);
        });

        render(<BrowserRouter><LibrarianAddBook /></BrowserRouter>);
        await fillFormAndSubmit();

        expect(await screen.findByText("Książka o podanym numerze ISBN już istnieje.")).toBeInTheDocument();
        expect(toast.error).toHaveBeenCalledWith("Książka o podanym numerze ISBN już istnieje.");
    });

    it('displays error for Payload Too Large (413)', async () => {
        (global.fetch as Mock).mockImplementation(async (url, options) => {
            if (options?.method === 'POST' && url.toString().includes('/books')) {
                return { ok: false, status: 413, statusText: 'Payload Too Large', text: async () => '' };
            }
            return defaultFetch(url, options);
        });

        render(<BrowserRouter><LibrarianAddBook /></BrowserRouter>);
        await fillFormAndSubmit();

        expect(await screen.findByText("Wybrane zdjęcie jest zbyt duże.")).toBeInTheDocument();
    });

    it('displays error for Unauthorized / Session Expired (401)', async () => {
        (global.fetch as Mock).mockImplementation(async (url, options) => {
            if (options?.method === 'POST' && url.toString().includes('/books')) {
                return { ok: false, status: 401, statusText: 'Unauthorized', text: async () => '' };
            }
            return defaultFetch(url, options);
        });

        render(<BrowserRouter><LibrarianAddBook /></BrowserRouter>);
        await fillFormAndSubmit();

        expect(await screen.findByText("Sesja wygasła. Proszę zalogować się ponownie.")).toBeInTheDocument();
    });

    it('displays error for Bad Request (400)', async () => {
        (global.fetch as Mock).mockImplementation(async (url, options) => {
            if (options?.method === 'POST' && url.toString().includes('/books')) {
                return { ok: false, status: 400, statusText: 'Bad Request', text: async () => 'Nieprawidłowy format ISBN' };
            }
            return defaultFetch(url, options);
        });

        render(<BrowserRouter><LibrarianAddBook /></BrowserRouter>);
        await fillFormAndSubmit();

        expect(await screen.findByText("Nieprawidłowy format ISBN")).toBeInTheDocument();
    });

    it('displays generic error for Server Crash (500)', async () => {
        (global.fetch as Mock).mockImplementation(async (url, options) => {
            if (options?.method === 'POST' && url.toString().includes('/books')) {
                return { ok: false, status: 500, statusText: 'Internal Server Error', text: async () => '' };
            }
            return defaultFetch(url, options);
        });

        render(<BrowserRouter><LibrarianAddBook /></BrowserRouter>);
        await fillFormAndSubmit();

        expect(await screen.findByText(/Błąd 500: Internal Server Error/i)).toBeInTheDocument();
    });

    it('displays error when adding a duplicate Author (409)', async () => {
        (global.fetch as Mock).mockImplementation(async (url, options) => {
            const urlStr = url.toString();
            if (options?.method === 'POST' && urlStr.includes('/authors')) {
                return {
                    ok: false,
                    status: 409,
                    statusText: 'Conflict',
                    text: async () => 'Conflict',
                };
            }
            return defaultFetch(url, options);
        });

        render(<BrowserRouter><LibrarianAddBook /></BrowserRouter>);

        const authorInput = screen.getByPlaceholderText('Dodaj autora');
        fireEvent.change(authorInput, { target: { value: 'New Author' } });

        await waitFor(() => {
            const addOption = screen.getByText('Dodaj "New Author"');
            fireEvent.click(addOption);
        });

        expect(toast.error).toHaveBeenCalledWith("Ten autor już istnieje.");
        expect(await screen.findByText("Ten autor już istnieje.")).toBeInTheDocument();
    });

    it('displays error when adding a duplicate Publisher (409)', async () => {
        (global.fetch as Mock).mockImplementation(async (url, options) => {
            const urlStr = url.toString();
            if (options?.method === 'POST' && urlStr.includes('/publishers')) {
                return {
                    ok: false,
                    status: 409,
                    statusText: 'Conflict',
                    text: async () => 'Conflict',
                };
            }
            return defaultFetch(url, options);
        });

        render(<BrowserRouter><LibrarianAddBook /></BrowserRouter>);

        const pubInput = screen.getByPlaceholderText('Dodaj wydawnictwo');
        fireEvent.change(pubInput, { target: { value: 'New Publisher' } });

        await waitFor(() => {
            const addOption = screen.getByText('Dodaj "New Publisher"');
            fireEvent.click(addOption);
        });

        expect(toast.error).toHaveBeenCalledWith("To wydawnictwo już istnieje.");
    });

});

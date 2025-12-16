import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import LibrarianReaders from '../../../Librarian/LibrarianReaders';
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

const mockReaderDetails = {
    userId: '1001',
    cardId: 'CARD-555',
    firstName: 'John',
    lastName: 'Doe',
    expirationDate: '2030-12-31'
};

const setupFetchMock = () => {
    global.fetch = vi.fn().mockImplementation((url: string, options: any) => {
        const method = options?.method || 'GET';

        if (url.includes('/api/library-cards') && method === 'POST') {
            return Promise.resolve({
                ok: true,
                status: 201,
                json: () => Promise.resolve({})
            });
        }

        if (url.includes('/api/library-cards/') && method === 'GET') {
            return Promise.resolve({
                ok: true,
                status: 200,
                json: () => Promise.resolve([mockReaderDetails])
            });
        }

        return Promise.resolve({ ok: false, status: 404 });
    }) as any;
};

describe('LibrarianReaders Integration Tests', () => {

    const alertMock = vi.spyOn(window, 'alert').mockImplementation(() => {});

    beforeEach(() => {
        vi.clearAllMocks();
        setupFetchMock();
        Storage.prototype.getItem = vi.fn((key) => {
            if (key === 'access_token') return 'fake_token';
            return null;
        });
        Storage.prototype.removeItem = vi.fn();
    });

    const renderComponent = () => {
        render(
            <BrowserRouter>
                <LibrarianReaders />
            </BrowserRouter>
        );
    };

    it('renders the creation form and search form correctly', () => {
        renderComponent();

        expect(screen.getByText('Dodaj nowego użytkownika BookRider:')).toBeInTheDocument();
        expect(screen.getByLabelText(/^Identyfikator użytkownika:/i)).toBeInTheDocument();
        expect(screen.getByLabelText(/Identyfikator karty/i)).toBeInTheDocument();
        expect(screen.getByText('Wyszukaj użytkownika:')).toBeInTheDocument();
    });

    it('successfully creates a library card and resets the form', async () => {
        renderComponent();

        fireEvent.change(screen.getByLabelText(/^Identyfikator użytkownika:/i), { target: { value: 'user-123' } });
        fireEvent.change(screen.getByLabelText(/Identyfikator karty/i), { target: { value: 'card-999' } });
        fireEvent.change(screen.getByLabelText(/Imię/i), { target: { value: 'Alice' } });
        fireEvent.change(screen.getByLabelText(/Nazwisko/i), { target: { value: 'Smith' } });
        fireEvent.change(screen.getByLabelText(/Data ważności/i), { target: { value: '2030-01-01' } });

        const addBtn = screen.getByRole('button', { name: /^Dodaj$/i });
        fireEvent.click(addBtn);

        await waitFor(() => {
            expect(global.fetch).toHaveBeenCalledWith(
                expect.stringContaining('/api/library-cards'),
                expect.objectContaining({
                    method: 'POST',
                    body: JSON.stringify({
                        userId: 'user-123',
                        cardId: 'card-999',
                        firstName: 'Alice',
                        lastName: 'Smith',
                        expirationDate: '2030-01-01'
                    })
                })
            );
        });

        expect(alertMock).toHaveBeenCalledWith('Library card created successfully');

        expect(screen.getByLabelText(/^Identyfikator użytkownika:/i)).toHaveValue('');
        expect(screen.getByLabelText(/Imię/i)).toHaveValue('');
    });

    it('searches for a library card and displays the results', async () => {
        renderComponent();

        const searchInput = screen.getByLabelText(/Wprowadź identyfikator użytkownika/i);
        fireEvent.change(searchInput, { target: { value: '1001' } });

        const searchBtn = screen.getByRole('button', { name: /Wyszukaj/i });
        fireEvent.click(searchBtn);

        await waitFor(() => {
            expect(global.fetch).toHaveBeenCalledWith(
                expect.stringContaining('/api/library-cards/1001'),
                expect.objectContaining({ method: 'GET' })
            );
        });

        expect(await screen.findByText('Szczegóły wyszukiwanego konta:')).toBeInTheDocument();

        expect(screen.getByText('John')).toBeInTheDocument();
        expect(screen.getByText('Doe')).toBeInTheDocument();
        expect(screen.getByText('CARD-555')).toBeInTheDocument();
    });

    it('handles missing token scenario gracefully', async () => {
        vi.spyOn(Storage.prototype, 'getItem').mockReturnValue(null);
        const consoleSpy = vi.spyOn(console, 'error').mockImplementation(() => {});

        renderComponent();

        fireEvent.click(screen.getByRole('button', { name: /^Dodaj$/i }));

        expect(global.fetch).not.toHaveBeenCalled();
        expect(consoleSpy).toHaveBeenCalledWith('No token found');
    });

    it('handles logout correctly', () => {
        renderComponent();

        const logoutBtn = screen.getByRole('button', { name: /Wyloguj się/i });
        fireEvent.click(logoutBtn);

        expect(localStorage.removeItem).toHaveBeenCalledWith('access_token');
        expect(mockedNavigate).toHaveBeenCalledWith('/');
    });

    it('handles failure when linking card to user (e.g. User ID not found or Card conflict)', async () => {
        const consoleSpy = vi.spyOn(console, 'error').mockImplementation(() => {});
        const alertMock = vi.spyOn(window, 'alert').mockImplementation(() => {});

        vi.spyOn(global, 'fetch').mockResolvedValueOnce({
            ok: false,
            status: 400,
            json: async () => ({ message: "User not found or Card ID exists" })
        } as Response);

        renderComponent();

        fireEvent.change(screen.getByLabelText(/^Identyfikator użytkownika:/i), { target: { value: 'invalid-user-id' } });
        fireEvent.change(screen.getByLabelText(/Identyfikator karty/i), { target: { value: 'duplicate-card' } });
        fireEvent.change(screen.getByLabelText(/Imię/i), { target: { value: 'John' } });

        fireEvent.click(screen.getByRole('button', { name: /^Dodaj$/i }));

        await waitFor(() => {
            expect(global.fetch).toHaveBeenCalled();
        });

        expect(alertMock).not.toHaveBeenCalled();

        expect(consoleSpy).toHaveBeenCalledWith(
            'Error creating library card: ',
            expect.any(Error)
        );

        expect(screen.getByLabelText(/^Identyfikator użytkownika:/i)).toHaveValue('invalid-user-id');
    });

    it('handles search failure (User/Card not found)', async () => {
        const consoleSpy = vi.spyOn(console, 'error').mockImplementation(() => {});

        vi.spyOn(global, 'fetch').mockResolvedValueOnce({
            ok: false,
            status: 404,
            json: async () => ({})
        } as Response);

        renderComponent();

        const searchInput = screen.getByLabelText(/Wprowadź identyfikator użytkownika/i);
        fireEvent.change(searchInput, { target: { value: 'non-existent-id' } });

        const searchBtn = screen.getByRole('button', { name: /Wyszukaj/i });
        fireEvent.click(searchBtn);

        await waitFor(() => {
            expect(global.fetch).toHaveBeenCalled();
        });

        expect(consoleSpy).toHaveBeenCalledWith(
            'Error searching for library card: ',
            expect.any(Error)
        );

        expect(screen.queryByText('Szczegóły wyszukiwanego konta:')).not.toBeInTheDocument();
        expect(screen.queryByText('ID karty:')).not.toBeInTheDocument();
    });
});

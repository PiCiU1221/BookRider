import { render, screen, fireEvent, waitFor, cleanup } from '@testing-library/react';
import { vi, describe, it, expect, beforeEach, afterEach } from 'vitest';
import { BrowserRouter } from 'react-router-dom';
import LibraryAdminHomePage from '../../../LibraryAdmin/LibraryAdminHomePage';

const mockedNavigate = vi.fn();

vi.mock('react-router-dom', async () => {
    const actual = await vi.importActual('react-router-dom');
    return {
        ...actual,
        useNavigate: () => mockedNavigate,
    };
});

vi.stubGlobal('import', { meta: { env: { VITE_API_BASE_URL: 'http://localhost:8080' } } });

describe('LibraryAdminHomePage', () => {
    const renderComponent = () => {
        return render(
            <BrowserRouter>
                <LibraryAdminHomePage />
            </BrowserRouter>
        );
    };

    const mockLibrarians = [
        { id: '1', username: 'john_doe', firstName: 'John', lastName: 'Doe' },
        { id: '2', username: 'jane_smith', firstName: 'Jane', lastName: 'Smith' },
    ];

    beforeEach(() => {
        vi.clearAllMocks();
        global.fetch = vi.fn();
        localStorage.setItem('access_token', 'fake-token');
    });

    afterEach(() => {
        cleanup();
        localStorage.clear();
    });

    it('renders the header and fetches/displays all librarians on mount', async () => {
        (global.fetch as any).mockResolvedValue({
            ok: true,
            json: async () => mockLibrarians,
        });

        renderComponent();

        expect(screen.getByText('Wyszukaj bibliotekarza')).toBeInTheDocument();

        await waitFor(() => {
            expect(screen.getByText('John Doe')).toBeInTheDocument();
            expect(screen.getByText('Jane Smith')).toBeInTheDocument();
        });
    });

    it('displays error message if fetching librarians fails', async () => {
        (global.fetch as any).mockResolvedValue({ ok: false });

        renderComponent();

        await waitFor(() => {
            expect(screen.getByText('Nie udało się pobrać listy bibliotekarzy.')).toBeInTheDocument();
        });
    });

    it('searches for a specific librarian when "Szukaj" is clicked', async () => {
        (global.fetch as any).mockImplementation((url: string) => {
            if (url.includes('username=')) {
                return Promise.resolve({
                    ok: true,
                    json: async () => [mockLibrarians[0]],
                });
            }
            return Promise.resolve({
                ok: true,
                json: async () => [],
            });
        });

        renderComponent();

        const input = screen.getByPlaceholderText('Nazwa użytkownika');
        const searchBtn = screen.getByText('Szukaj');

        fireEvent.change(input, { target: { value: 'john_doe' } });
        fireEvent.click(searchBtn);

        await waitFor(() => {
            expect(screen.getByText('John Doe')).toBeInTheDocument();
            expect(screen.queryByText('Jane Smith')).not.toBeInTheDocument();
        });
    });

    it('displays "not found" message if search yields no results', async () => {
        (global.fetch as any).mockImplementation((url: string) => {
            if (url.includes('username=')) {
                return Promise.resolve({
                    ok: false,
                    status: 404,
                    json: async () => ({}),
                });
            }
            return Promise.resolve({
                ok: true,
                json: async () => [{ id: '99', username: 'init_user', firstName: 'Init', lastName: 'User' }],
            });
        });

        renderComponent();

        await waitFor(() => {
            expect(screen.getByText('Init User')).toBeInTheDocument();
        });

        const input = screen.getByPlaceholderText('Nazwa użytkownika');
        const searchBtn = screen.getByText('Szukaj');

        fireEvent.change(input, { target: { value: 'unknown_user' } });
        fireEvent.click(searchBtn);

        await waitFor(() => {
            expect(screen.getByText('Nie znaleziono bibliotekarza.')).toBeInTheDocument();
        });
    });

    it('resets librarian password successfully', async () => {
        (global.fetch as any).mockResolvedValueOnce({ ok: true, json: async () => mockLibrarians });
        renderComponent();

        await waitFor(() => screen.getByText('John Doe'));

        const resetBtns = screen.getAllByText('Zresetuj hasło');

        (global.fetch as any).mockResolvedValueOnce({ ok: true });

        fireEvent.click(resetBtns[0]);

        await waitFor(() => {
            expect(screen.getByText('Hasło bibliotekarza zresetowano pomyślnie.')).toBeInTheDocument();
        });
    });

    it('deletes a librarian and removes them from the UI', async () => {
        (global.fetch as any).mockResolvedValueOnce({ ok: true, json: async () => mockLibrarians });
        renderComponent();

        await waitFor(() => screen.getByText('John Doe'));

        const deleteBtns = screen.getAllByText('Usuń');

        (global.fetch as any).mockResolvedValueOnce({ ok: true });

        fireEvent.click(deleteBtns[0]);

        await waitFor(() => {
            expect(screen.getByText('Usunięto bibliotekarza.')).toBeInTheDocument();
            expect(screen.queryByText('John Doe')).not.toBeInTheDocument();
        });
    });

    it('handles logout by clearing storage and navigating', () => {
        (global.fetch as any).mockResolvedValue({ ok: true, json: async () => [] });

        localStorage.setItem('username', 'admin');
        localStorage.setItem('role', 'admin');

        renderComponent();

        const logoutBtn = screen.getByText('Wyloguj się');
        fireEvent.click(logoutBtn);

        expect(localStorage.getItem('access_token')).toBeNull();
        expect(mockedNavigate).toHaveBeenCalledWith('/');
    });
});

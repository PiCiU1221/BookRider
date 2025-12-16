import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import LibraryAdminHomePage from '../../../LibraryAdmin/LibraryAdminAddLibrarian';
import { BrowserRouter } from 'react-router-dom';

const mockedNavigate = vi.fn();
vi.mock('react-router-dom', async () => {
    const actual = await vi.importActual('react-router-dom');
    return {
        ...actual,
        useNavigate: () => mockedNavigate,
    };
});

vi.stubGlobal('import.meta', { env: { VITE_API_BASE_URL: 'http://localhost:8080' } });

describe('LibraryAdminHomePage Integration Tests', () => {

    beforeEach(() => {
        vi.clearAllMocks();

        Storage.prototype.getItem = vi.fn((key) => {
            if (key === 'access_token') return 'fake_admin_token';
            return null;
        });
        Storage.prototype.removeItem = vi.fn();

        global.fetch = vi.fn();
    });

    const renderComponent = () => {
        render(
            <BrowserRouter>
                <LibraryAdminHomePage />
            </BrowserRouter>
        );
    };

    it('renders the form inputs and allows user typing', () => {
        renderComponent();

        expect(screen.getByPlaceholderText('Nazwa użytkownika')).toBeInTheDocument();
        expect(screen.getByPlaceholderText('Imię')).toBeInTheDocument();
        expect(screen.getByPlaceholderText('Nazwisko')).toBeInTheDocument();
        expect(screen.getByRole('button', { name: /Dodaj bibliotekarza/i })).toBeInTheDocument();

        const usernameInput = screen.getByPlaceholderText('Nazwa użytkownika');
        fireEvent.change(usernameInput, { target: { value: 'new_librarian' } });
        expect(usernameInput).toHaveValue('new_librarian');
    });

    it('successfully adds a librarian and resets the form', async () => {
        vi.spyOn(global, 'fetch').mockResolvedValueOnce({
            ok: true,
            status: 201,
            json: async () => ({})
        } as Response);

        renderComponent();

        fireEvent.change(screen.getByPlaceholderText('Nazwa użytkownika'), { target: { value: 'lib_john' } });
        fireEvent.change(screen.getByPlaceholderText('Imię'), { target: { value: 'John' } });
        fireEvent.change(screen.getByPlaceholderText('Nazwisko'), { target: { value: 'Doe' } });

        const addBtn = screen.getByRole('button', { name: /Dodaj bibliotekarza/i });
        fireEvent.click(addBtn);

        await waitFor(() => {
            expect(global.fetch).toHaveBeenCalledWith(
                expect.stringContaining('/api/library-admins/librarians'),
                expect.objectContaining({
                    method: 'POST',
                    headers: expect.objectContaining({
                        'Authorization': 'Bearer fake_admin_token',
                        'Content-Type': 'application/json'
                    }),
                    body: JSON.stringify({
                        username: 'lib_john',
                        firstName: 'John',
                        lastName: 'Doe'
                    })
                })
            );
        });

        expect(await screen.findByText('Dodano bibliotekarza.')).toBeInTheDocument();

        expect(screen.getByPlaceholderText('Nazwa użytkownika')).toHaveValue('');
        expect(screen.getByPlaceholderText('Imię')).toHaveValue('');
        expect(screen.getByPlaceholderText('Nazwisko')).toHaveValue('');
    });

    it('handles server errors gracefully (e.g., username taken)', async () => {
        vi.spyOn(global, 'fetch').mockResolvedValueOnce({
            ok: false,
            status: 400,
            json: async () => ({ message: "Username exists" })
        } as Response);

        renderComponent();

        fireEvent.change(screen.getByPlaceholderText('Nazwa użytkownika'), { target: { value: 'duplicate_user' } });

        fireEvent.click(screen.getByRole('button', { name: /Dodaj bibliotekarza/i }));

        expect(await screen.findByText('Nie udało się dodać bibliotekarza.')).toBeInTheDocument();

        expect(screen.getByPlaceholderText('Nazwa użytkownika')).toHaveValue('duplicate_user');
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

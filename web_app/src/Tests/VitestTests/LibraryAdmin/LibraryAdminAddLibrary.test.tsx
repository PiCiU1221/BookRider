import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import LibraryAdminAddLibrary from '../../../LibraryAdmin/LibraryAdminAddLibrary';
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

const fillForm = (overrides = {}) => {
    const defaults = {
        libraryName: 'Test Library',
        addressLine: 'Test Street 1',
        city: 'Test City',
        postalCode: '00-000',
        phoneNumber: '123456789',
        emailAddress: 'test@example.com'
    };
    const data = { ...defaults, ...overrides };

    fireEvent.change(screen.getByLabelText(/Nazwa biblioteki:/i), { target: { value: data.libraryName } });
    fireEvent.change(screen.getByLabelText(/Ulica i nr budynku:/i), { target: { value: data.addressLine } });
    fireEvent.change(screen.getByLabelText(/Miasto:/i), { target: { value: data.city } });
    fireEvent.change(screen.getByLabelText(/Kod pocztowy:/i), { target: { value: data.postalCode } });
    fireEvent.change(screen.getByLabelText(/Numer telefonu:/i), { target: { value: data.phoneNumber } });
    fireEvent.change(screen.getByLabelText(/Adres e-mail:/i), { target: { value: data.emailAddress } });
};

describe('LibraryAdminAddLibrary Integration Tests', () => {

    beforeEach(() => {
        vi.clearAllMocks();

        Storage.prototype.getItem = vi.fn((key) => {
            if (key === 'access_token') return 'fake_admin_token';
            return null;
        });
        Storage.prototype.removeItem = vi.fn();

        global.fetch = vi.fn();

        vi.spyOn(console, 'error').mockImplementation(() => {});
    });

    const renderComponent = () => {
        render(
            <BrowserRouter>
                <LibraryAdminAddLibrary />
            </BrowserRouter>
        );
    };

    it('renders all form fields and accepts user input', () => {
        renderComponent();

        expect(screen.getByLabelText(/Nazwa biblioteki:/i)).toBeInTheDocument();
        expect(screen.getByLabelText(/Ulica i nr budynku:/i)).toBeInTheDocument();

        expect(screen.getByRole('button', { name: /Złóż podanie/i })).toBeInTheDocument();

        const nameInput = screen.getByLabelText(/Nazwa biblioteki:/i);
        fireEvent.change(nameInput, { target: { value: 'Central Library' } });
        expect(nameInput).toHaveValue('Central Library');
    });

    it('submits the form with correctly mapped payload and navigates on success', async () => {
        vi.spyOn(global, 'fetch').mockResolvedValueOnce({
            ok: true,
            status: 201,
            json: async () => ({})
        } as Response);

        renderComponent();

        fillForm({
            libraryName: 'Grand Library',
            addressLine: '123 Main St',
            city: 'Warsaw',
            postalCode: '00-001',
            phoneNumber: '123456789',
            emailAddress: 'contact@library.com'
        });

        fireEvent.click(screen.getByRole('button', { name: /Złóż podanie/i }));

        await waitFor(() => {
            expect(global.fetch).toHaveBeenCalledWith(
                expect.stringContaining('/api/library-requests'),
                expect.objectContaining({
                    method: 'POST',
                    body: JSON.stringify({
                        street: '123 Main St',
                        city: 'Warsaw',
                        postalCode: '00-001',
                        libraryName: 'Grand Library',
                        phoneNumber: '123456789',
                        libraryEmail: 'contact@library.com',
                    }),
                })
            );
        });

        expect(mockedNavigate).toHaveBeenCalledWith('/processing-info');
    });

    it('displays error message when server rejects the request', async () => {
        vi.spyOn(global, 'fetch').mockResolvedValueOnce({
            ok: false,
            status: 500,
            statusText: 'Internal Server Error'
        } as Response);

        renderComponent();
        fillForm();

        fireEvent.click(screen.getByRole('button', { name: /Złóż podanie/i }));

        expect(await screen.findByText('Wystąpił błąd podczas wysyłania formularza.')).toBeInTheDocument();

        expect(mockedNavigate).not.toHaveBeenCalled();
    });
});

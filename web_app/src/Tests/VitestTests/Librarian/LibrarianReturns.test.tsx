import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import LibrarianReturns from '../../../Librarian/LibrarianReturns';
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

const mockReturnDetails = {
    id: 101,
    orderId: 500,
    status: 'IN_PROGRESS',
    rentalReturnItems: [
        {
            id: 1,
            rentalId: 99,
            returnedQuantity: 1,
            book: {
                id: 10,
                title: 'Clean Code',
                authorNames: ['Robert Martin'],
                isbn: '978-0132350884',
                categoryName: 'Tech',
                image: 'img.jpg'
            }
        }
    ]
};

const setupFetchMock = () => {
    global.fetch = vi.fn().mockImplementation((url: string, options: any) => {
        const method = options?.method || 'GET';

        if (method === 'GET') {
            if (url.includes('/api/rental-returns/101')) {
                return Promise.resolve({
                    ok: true,
                    json: () => Promise.resolve(mockReturnDetails)
                });
            }
            if (url.includes('/api/rental-returns/latest-by-driver/DRIVER-1')) {
                return Promise.resolve({
                    ok: true,
                    json: () => Promise.resolve(mockReturnDetails)
                });
            }
            return Promise.resolve({ ok: false, status: 404 });
        }

        if (method === 'PATCH') {
            if (url.includes('/complete-in-person')) {
                return Promise.resolve({ ok: true, json: () => Promise.resolve({}) });
            }
            if (url.includes('/complete-delivery')) {
                return Promise.resolve({ ok: true, json: () => Promise.resolve({}) });
            }
        }

        return Promise.resolve({ ok: false, status: 500 });
    }) as any;
};

describe('LibrarianReturns Integration Tests', () => {
    beforeEach(() => {
        vi.clearAllMocks();
        setupFetchMock();
        Storage.prototype.getItem = vi.fn(() => 'fake_token');
        Storage.prototype.removeItem = vi.fn();
    });

    const renderComponent = () => {
        render(
            <BrowserRouter>
                <LibrarianReturns />
            </BrowserRouter>
        );
    };

    it('renders return type toggles and switches inputs correctly', () => {
        renderComponent();

        expect(screen.getByText('Zwrot osobiście')).toBeInTheDocument();
        expect(screen.getByLabelText(/ID zwrotu/i)).toBeInTheDocument();
        expect(screen.queryByLabelText(/ID kierowcy/i)).not.toBeInTheDocument();

        fireEvent.click(screen.getByText('Zwrot przez kierowcę'));

        expect(screen.getByLabelText(/ID kierowcy/i)).toBeInTheDocument();
        expect(screen.queryByLabelText(/ID zwrotu/i)).not.toBeInTheDocument();
    });

    it('successfully processes an "In-Person" return flow', async () => {
        renderComponent();

        fireEvent.change(screen.getByLabelText(/ID zwrotu/i), { target: { value: '101' } });

        const fetchBtn = screen.getByRole('button', { name: /^Zwróć$/i });
        fireEvent.click(fetchBtn);

        await waitFor(() => {
            expect(global.fetch).toHaveBeenCalledWith(
                expect.stringContaining('/api/rental-returns/101'),
                expect.objectContaining({ method: 'GET' })
            );
            expect(screen.getByText('Clean Code')).toBeInTheDocument();
            expect(screen.getByText(/Robert Martin/)).toBeInTheDocument();
        });

        const confirmBtn = screen.getByText('Zatwierdź zwrot');
        fireEvent.click(confirmBtn);

        await waitFor(() => {
            expect(global.fetch).toHaveBeenCalledWith(
                expect.stringContaining('/api/rental-returns/101/complete-in-person'),
                expect.objectContaining({ method: 'PATCH' })
            );
            expect(screen.getByText('Zwrot zakończony pomyślnie.')).toBeInTheDocument();
        });
    });

    it('successfully processes a "Driver" return flow', async () => {
        renderComponent();

        fireEvent.click(screen.getByText('Zwrot przez kierowcę'));

        fireEvent.change(screen.getByLabelText(/ID kierowcy/i), { target: { value: 'DRIVER-1' } });

        const fetchBtn = screen.getByRole('button', { name: /^Zwróć$/i });
        fireEvent.click(fetchBtn);

        await waitFor(() => {
            expect(global.fetch).toHaveBeenCalledWith(
                expect.stringContaining('/api/rental-returns/latest-by-driver/DRIVER-1'),
                expect.objectContaining({ method: 'GET' })
            );
            expect(screen.getByText('Clean Code')).toBeInTheDocument();
        });

        const confirmBtn = screen.getByText('Zatwierdź zwrot');
        fireEvent.click(confirmBtn);

        await waitFor(() => {
            expect(global.fetch).toHaveBeenCalledWith(
                expect.stringContaining('/api/rental-returns/101/complete-delivery'),
                expect.objectContaining({ method: 'PATCH' })
            );
            expect(screen.getByText('Zwrot przez kierowcę zakończony pomyślnie.')).toBeInTheDocument();
        });
    });

    it('validates empty inputs before fetching', async () => {
        renderComponent();

        const fetchBtn = screen.getByRole('button', { name: /^Zwróć$/i });
        fireEvent.click(fetchBtn);

        expect(screen.getByText('Nieprawidłowe ID zwrotu')).toBeInTheDocument();
        expect(global.fetch).not.toHaveBeenCalled();

        fireEvent.click(screen.getByText('Zwrot przez kierowcę'));
        fireEvent.click(screen.getByRole('button', { name: /^Zwróć$/i }));

        expect(screen.getByText('Nieprawidłowe ID kierowcy')).toBeInTheDocument();
        expect(global.fetch).not.toHaveBeenCalled();
    });

    it('handles API errors gracefully (e.g. Return Not Found)', async () => {
        renderComponent();

        fireEvent.change(screen.getByLabelText(/ID zwrotu/i), { target: { value: '999' } });
        fireEvent.click(screen.getByRole('button', { name: /^Zwróć$/i }));

        await waitFor(() => {
            expect(screen.getByText('Wystąpił błąd podczas pobierania danych.')).toBeInTheDocument();
        });

        expect(screen.queryByText('Szczegóły zwrotu:')).not.toBeInTheDocument();
    });

    it('handles logout correctly', () => {
        renderComponent();

        fireEvent.click(screen.getByRole('button', { name: /Wyloguj się/i }));

        expect(localStorage.removeItem).toHaveBeenCalledWith('access_token');
        expect(mockedNavigate).toHaveBeenCalledWith('/');
    });
});

import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import LibrarianOrders from '../../../Librarian/LibrarianOrders';
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

const createMockOrder = (id: number, status: string) => ({
    orderId: id,
    userId: 'user-123',
    status: status,
    createdAt: new Date().toISOString(),
    orderItems: [
        {
            quantity: 1,
            book: {
                id: 101,
                title: `Book Title ${id}`,
                authorNames: ['Author A'],
                image: 'img.jpg',
                isbn: '123',
                categoryName: 'Fiction',
                releaseYear: 2020,
                publisherName: 'Pub',
                languageName: 'PL'
            }
        }
    ],
    driverId: ''
});

const setupFetchMock = () => {
    global.fetch = vi.fn().mockImplementation((url: string, options: any) => {
        const method = options?.method || 'GET';

        if (method === 'GET') {
            if (url.includes('/pending')) return Promise.resolve({ ok: true, json: () => Promise.resolve({ content: [createMockOrder(1, 'PENDING')] }) });
            if (url.includes('/in-realization')) return Promise.resolve({ ok: true, json: () => Promise.resolve({ content: [createMockOrder(2, 'IN_REALIZATION')] }) });
            if (url.includes('/completed')) return Promise.resolve({ ok: true, json: () => Promise.resolve({ content: [createMockOrder(3, 'COMPLETED')] }) });
        }

        if ((url.includes('/accept') || url.includes('/decline')) && method === 'PATCH') {
            return Promise.resolve({ ok: true, status: 200, json: () => Promise.resolve({}) });
        }
        if (url.includes('/handover') && method === 'PUT') {
            return Promise.resolve({ ok: true, status: 200, json: () => Promise.resolve({}) });
        }

        return Promise.resolve({ ok: false, status: 404 });
    }) as any;
};

describe('LibrarianOrders Integration Tests', () => {

    beforeEach(() => {
        vi.clearAllMocks();
        setupFetchMock();
        Storage.prototype.getItem = vi.fn(() => 'fake_token');
    });

    const renderComponent = () => {
        render(
            <BrowserRouter>
                <LibrarianOrders />
            </BrowserRouter>
        );
    };

    it('renders and segregates orders into correct columns based on status', async () => {
        renderComponent();
        await waitFor(() => {
            expect(screen.getByText('Book Title 1')).toBeInTheDocument();
            expect(screen.getByText('Book Title 2')).toBeInTheDocument();
            expect(screen.getByText('Book Title 3')).toBeInTheDocument();
        });
    });

    it('successfully accepts a pending order', async () => {
        renderComponent();
        await waitFor(() => expect(screen.getByText('Book Title 1')).toBeInTheDocument());

        const acceptBtns = screen.getAllByRole('button', { name: /Zatwierdź/i });
        fireEvent.click(acceptBtns[0]);

        await waitFor(() => {
            expect(global.fetch).toHaveBeenCalledWith(
                expect.stringContaining('/api/orders/1/accept'),
                expect.objectContaining({ method: 'PATCH' })
            );
        });
    });

    it('handles the rejection flow with a selected reason', async () => {
        renderComponent();
        await waitFor(() => expect(screen.getByText('Book Title 1')).toBeInTheDocument());

        const rejectBtns = screen.getAllByRole('button', { name: /Odrzuć/i });
        fireEvent.click(rejectBtns[0]);

        const reasonRadio = await screen.findByLabelText(/brak w zbiorach biblioteki/i);
        fireEvent.click(reasonRadio);

        const confirmBtn = screen.getByRole('button', { name: /Potwierdź odrzucenie/i });
        fireEvent.click(confirmBtn);

        await waitFor(() => {
            expect(global.fetch).toHaveBeenCalledWith(
                expect.stringContaining('/api/orders/1/decline'),
                expect.objectContaining({
                    method: 'PATCH',
                    body: JSON.stringify({ reason: 'Brak w zbiorach biblioteki' })
                })
            );
        });

        await waitFor(() => {
            expect(screen.queryByText('Wybierz przyczynę odmowy:')).not.toBeInTheDocument();
        });
    });

    it('handles the handover flow by assigning a driver', async () => {
        renderComponent();
        await waitFor(() => expect(screen.getByText('Book Title 2')).toBeInTheDocument());

        const finishOrderBtn = screen.getByRole('button', { name: /Zakończ zamówienie/i });
        fireEvent.click(finishOrderBtn);

        const driverInput = await screen.findByPlaceholderText(/Wprowadź ID kierowcy/i);
        fireEvent.change(driverInput, { target: { value: 'Driver-X' } });

        const handoverBtn = screen.getByRole('button', { name: /Przekaż książkę/i });
        fireEvent.click(handoverBtn);

        await waitFor(() => {
            expect(global.fetch).toHaveBeenCalledWith(
                expect.stringContaining('/api/orders/2/handover?driverId=Driver-X'),
                expect.objectContaining({ method: 'PUT' })
            );
        });

        expect(await screen.findByText(/zostało przekazane pomyślnie/i)).toBeInTheDocument();
    });

    it('validates input before allowing handover', async () => {
        renderComponent();
        await waitFor(() => expect(screen.getByText('Book Title 2')).toBeInTheDocument());

        fireEvent.click(screen.getByRole('button', { name: /Zakończ zamówienie/i }));

        const handoverBtn = screen.getByRole('button', { name: /Przekaż książkę/i });
        fireEvent.click(handoverBtn);

        expect(await screen.findByText('Proszę wprowadzić ID kierowcy.')).toBeInTheDocument();

        expect(global.fetch).not.toHaveBeenCalledWith(
            expect.stringContaining('/handover'),
            expect.anything()
        );
    });
});

import { render, screen, fireEvent, waitFor, cleanup } from '@testing-library/react';
import { vi, describe, it, expect, beforeEach, afterEach } from 'vitest';
import { BrowserRouter } from 'react-router-dom';
import SubmissionDetailsDriver from '../../../SystemAdmin/SubmissionDetailsDriver';

const mockedNavigate = vi.fn();
vi.mock('react-router-dom', async () => {
    const actual = await vi.importActual('react-router-dom');
    return {
        ...actual,
        useNavigate: () => mockedNavigate,
        useParams: () => ({ submissionId: '123' }),
    };
});

vi.stubGlobal('import', { meta: { env: { VITE_API_BASE_URL: 'http://localhost:8080' } } });

describe('SubmissionDetailsDriver', () => {
    const mockDriverApp = {
        id: 123,
        userEmail: 'driver@test.com',
        reviewerID: null,
        status: 'PENDING',
        submittedAt: '2023-11-01T10:00:00Z',
        reviewedAt: null,
        rejectionReason: null,
        driverDocuments: [
            {
                documentType: 'License',
                documentPhotoUrl: 'http://img.url/license.jpg',
                expiryDate: '2025-01-01T00:00:00Z',
            },
        ],
    };

    const renderComponent = () => {
        return render(
            <BrowserRouter>
                <SubmissionDetailsDriver />
            </BrowserRouter>
        );
    };

    beforeEach(() => {
        vi.clearAllMocks();
        global.fetch = vi.fn();
        localStorage.setItem('access_token', 'fake-token');
        localStorage.setItem('email', 'admin@test.com');
    });

    afterEach(() => {
        cleanup();
        localStorage.clear();
    });

    it('renders loading state then displays application details', async () => {
        (global.fetch as any).mockResolvedValueOnce({
            ok: true,
            json: async () => mockDriverApp,
        });

        renderComponent();

        expect(screen.getByAltText('Book Rider Logo')).toBeInTheDocument();
        expect(screen.getByText('admin@test.com')).toBeInTheDocument();

        await waitFor(() => {
            expect(screen.getByText('Szczegóły podania nr: 123')).toBeInTheDocument();
            expect(screen.getByText('Email użytkownika:')).toBeInTheDocument();
            expect(screen.getByText('driver@test.com')).toBeInTheDocument();
            expect(screen.getByText('License')).toBeInTheDocument();
        });

        expect(global.fetch).toHaveBeenCalledWith(
            expect.stringContaining('/api/driver-applications/123'),
            expect.objectContaining({ method: 'GET' })
        );
    });

    it('handles API errors gracefully', async () => {
        (global.fetch as any).mockResolvedValueOnce({
            ok: false,
            statusText: 'Not Found',
        });

        renderComponent();

        await waitFor(() => {
            expect(screen.getByText('Błąd podczas pobierania danych.')).toBeInTheDocument();
        });
    });

    it('approves the application when "Zatwierdź" is clicked', async () => {
        (global.fetch as any).mockResolvedValueOnce({
            ok: true,
            json: async () => mockDriverApp,
        });

        renderComponent();

        const approveBtn = await screen.findByText('Zatwierdź');

        (global.fetch as any).mockResolvedValueOnce({
            ok: true,
            json: async () => ({}),
        });

        fireEvent.click(approveBtn);

        await waitFor(() => {
            expect(global.fetch).toHaveBeenCalledWith(
                expect.stringContaining('/api/driver-applications/123/status?status=APPROVED'),
                expect.objectContaining({ method: 'PUT' })
            );
            expect(mockedNavigate).toHaveBeenCalledWith('/system-admin-dashboard');
        });
    });

    it('rejects the application with a standard reason', async () => {
        (global.fetch as any).mockResolvedValueOnce({
            ok: true,
            json: async () => mockDriverApp,
        });

        renderComponent();

        const declineBtn = await screen.findByText('Odrzuć');
        fireEvent.click(declineBtn);

        const reasonRadio = screen.getByLabelText('Nieprawidłowy dokument');
        fireEvent.click(reasonRadio);

        (global.fetch as any).mockResolvedValueOnce({
            ok: true,
            json: async () => ({}),
        });

        const submitBtn = screen.getByText('Wyślij');
        fireEvent.click(submitBtn);

        await waitFor(() => {
            const expectedUrl = '/api/driver-applications/123/status?status=REJECTED&rejectionReason=' + encodeURIComponent('Nieprawidłowy dokument');
            expect(global.fetch).toHaveBeenCalledWith(
                expect.stringContaining(expectedUrl),
                expect.objectContaining({ method: 'PUT' })
            );
            expect(mockedNavigate).toHaveBeenCalledWith('/system-admin-dashboard');
        });
    });

    it('rejects the application with a CUSTOM reason', async () => {
        (global.fetch as any).mockResolvedValueOnce({
            ok: true,
            json: async () => mockDriverApp,
        });

        renderComponent();

        fireEvent.click(await screen.findByText('Odrzuć'));

        fireEvent.click(screen.getByLabelText('Inne'));

        const input = screen.getByPlaceholderText('Podaj przyczynę odmowy akceptacji aplikacji');
        fireEvent.change(input, { target: { value: 'Custom Reason Test' } });

        (global.fetch as any).mockResolvedValueOnce({
            ok: true,
            json: async () => ({}),
        });

        fireEvent.click(screen.getByText('Wyślij'));

        await waitFor(() => {
            const expectedUrl = encodeURIComponent('Custom Reason Test');
            expect(global.fetch).toHaveBeenCalledWith(
                expect.stringContaining(expectedUrl),
                expect.anything()
            );
        });
    });

    it('displays validation error if no reason is selected', async () => {
        (global.fetch as any).mockResolvedValueOnce({
            ok: true,
            json: async () => mockDriverApp,
        });

        renderComponent();

        fireEvent.click(await screen.findByText('Odrzuć'));

        fireEvent.click(screen.getByText('Wyślij'));

        await waitFor(() => {
            expect(screen.getByText('Proszę podać powód odrzucenia.')).toBeInTheDocument();
        });

        expect(global.fetch).toHaveBeenCalledTimes(1);
    });
});

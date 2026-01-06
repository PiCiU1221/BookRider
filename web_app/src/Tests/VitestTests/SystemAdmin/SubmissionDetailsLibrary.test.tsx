import { render, screen, fireEvent, waitFor, cleanup } from '@testing-library/react';
import { vi, describe, it, expect, beforeEach, afterEach } from 'vitest';
import { BrowserRouter } from 'react-router-dom';
import SubmissionDetailsLibrary from '../../../SystemAdmin/SubmissionDetailsLibrary';

const mockedNavigate = vi.fn();
vi.mock('react-router-dom', async () => {
    const actual = await vi.importActual('react-router-dom');
    return {
        ...actual,
        useNavigate: () => mockedNavigate,
        useParams: () => ({ submissionId: '555' }),
    };
});

vi.stubGlobal('import', { meta: { env: { VITE_API_BASE_URL: 'http://localhost:8080' } } });

describe('SubmissionDetailsLibrary', () => {
    const mockLibraryRequest = {
        id: 555,
        creatorEmail: 'librarian@test.com',
        reviewerId: null,
        address: {
            id: 1,
            street: 'Main St 123',
            city: 'Warsaw',
            postalCode: '00-001',
            latitude: 52.2297,
            longitude: 21.0122,
        },
        libraryName: 'Central Library',
        phoneNumber: '123-456-789',
        libraryEmail: 'contact@library.com',
        status: 'PENDING',
        submittedAt: '2023-11-15T10:00:00Z',
        reviewedAt: null,
        rejectionReason: null,
    };

    const renderComponent = () => {
        return render(
            <BrowserRouter>
                <SubmissionDetailsLibrary />
            </BrowserRouter>
        );
    };

    beforeEach(() => {
        vi.clearAllMocks();
        global.fetch = vi.fn();
        localStorage.setItem('access_token', 'fake-admin-token');
        localStorage.setItem('email', 'admin@test.com');
    });

    afterEach(() => {
        cleanup();
        localStorage.clear();
    });

    it('renders loading state then displays library details', async () => {
        (global.fetch as any).mockResolvedValueOnce({
            ok: true,
            json: async () => mockLibraryRequest,
        });

        renderComponent();

        expect(screen.getByAltText('Book Rider Logo')).toBeInTheDocument();
        expect(screen.getByText('admin@test.com')).toBeInTheDocument();

        await waitFor(() => {
            expect(screen.getByText('Szczegóły podania nr: 555')).toBeInTheDocument();
            expect(screen.getByText('Central Library')).toBeInTheDocument();
            expect(screen.getByText('Main St 123, Warsaw, 00-001')).toBeInTheDocument();
            expect(screen.getByText('librarian@test.com')).toBeInTheDocument();
            expect(screen.getByText('123-456-789')).toBeInTheDocument();
        });

        expect(global.fetch).toHaveBeenCalledWith(
            expect.stringContaining('/api/library-requests/555'),
            expect.objectContaining({ method: 'GET' })
        );
    });

    it('handles API errors gracefully', async () => {
        (global.fetch as any).mockResolvedValueOnce({
            ok: false,
            statusText: 'Internal Server Error',
        });

        renderComponent();

        await waitFor(() => {
            expect(screen.getByText('Error: Failed to fetch library request')).toBeInTheDocument();
        });
    });

    it('approves the library request when "Zatwierdź" is clicked', async () => {
        (global.fetch as any).mockResolvedValueOnce({
            ok: true,
            json: async () => mockLibraryRequest,
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
                expect.stringContaining('/api/library-requests/555/status?status=APPROVED'),
                expect.objectContaining({ method: 'PUT' })
            );
            expect(mockedNavigate).toHaveBeenCalledWith('/system-admin-dashboard');
        });
    });

    it('rejects the request with a standard reason', async () => {
        (global.fetch as any).mockResolvedValueOnce({
            ok: true,
            json: async () => mockLibraryRequest,
        });

        renderComponent();

        const declineBtn = await screen.findByText('Odrzuć');
        fireEvent.click(declineBtn);

        const reasonRadio = screen.getByLabelText(/Biblioteka została już dodana/i);
        fireEvent.click(reasonRadio);

        (global.fetch as any).mockResolvedValueOnce({
            ok: true,
            json: async () => ({}),
        });

        const submitBtn = screen.getByText('Wyślij');
        fireEvent.click(submitBtn);

        await waitFor(() => {
            const reason = 'Biblioteka została już dodana do systemu';
            const expectedUrl = `/api/library-requests/555/status?status=REJECTED&rejectionReason=${encodeURIComponent(reason)}`;

            expect(global.fetch).toHaveBeenCalledWith(
                expect.stringContaining(expectedUrl),
                expect.objectContaining({ method: 'PUT' })
            );
            expect(mockedNavigate).toHaveBeenCalledWith('/system-admin-dashboard');
        });
    });

    it('rejects the request with a CUSTOM reason', async () => {
        (global.fetch as any).mockResolvedValueOnce({
            ok: true,
            json: async () => mockLibraryRequest,
        });

        renderComponent();

        fireEvent.click(await screen.findByText('Odrzuć'));

        fireEvent.click(screen.getByLabelText('Inne'));

        const input = screen.getByPlaceholderText('Podaj przyczynę odmowy akceptacji podania');
        fireEvent.change(input, { target: { value: 'Fake Library Address' } });

        (global.fetch as any).mockResolvedValueOnce({
            ok: true,
            json: async () => ({}),
        });

        fireEvent.click(screen.getByText('Wyślij'));

        await waitFor(() => {
            const expectedUrl = encodeURIComponent('Fake Library Address');
            expect(global.fetch).toHaveBeenCalledWith(
                expect.stringContaining(expectedUrl),
                expect.anything()
            );
        });
    });

    it('displays validation error if "Inne" is selected but input is empty', async () => {
        (global.fetch as any).mockResolvedValueOnce({
            ok: true,
            json: async () => mockLibraryRequest,
        });

        renderComponent();

        fireEvent.click(await screen.findByText('Odrzuć'));

        fireEvent.click(screen.getByLabelText('Inne'));

        fireEvent.click(screen.getByText('Wyślij'));

        await waitFor(() => {
            expect(screen.getByText('Podaj przyczynę odmowy.')).toBeInTheDocument();
        });

        expect(global.fetch).toHaveBeenCalledTimes(1);
    });

    it('displays validation error if NO reason is selected', async () => {
        (global.fetch as any).mockResolvedValueOnce({
            ok: true,
            json: async () => mockLibraryRequest,
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

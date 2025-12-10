import { render, screen, fireEvent, waitFor, cleanup } from '@testing-library/react';
import { vi, describe, it, expect, beforeEach, afterEach } from 'vitest';
import { BrowserRouter } from 'react-router-dom';
import SystemAdminHomePage from '../../../SystemAdmin/SystemAdminHomePage';
import { useWebSocketNotification } from '../../../Utils/useWebSocketNotification';

const mockedNavigate = vi.fn();
vi.mock('react-router-dom', async () => {
    const actual = await vi.importActual('react-router-dom');
    return {
        ...actual,
        useNavigate: () => mockedNavigate,
    };
});

vi.mock('../../../Utils/useWebSocketNotification', () => ({
    useWebSocketNotification: vi.fn(),
}));

vi.mock('react-toastify', () => ({
    toast: {
        info: vi.fn(),
        error: vi.fn(),
    },
}));

vi.stubGlobal('import', { meta: { env: { VITE_API_BASE_URL: 'http://localhost:8080' } } });

describe('SystemAdminHomePage', () => {
    const renderComponent = () => {
        return render(
            <BrowserRouter>
                <SystemAdminHomePage />
            </BrowserRouter>
        );
    };

    const mockDriverData = [
        { id: 101, driverEmail: 'driver@test.com', status: 'PENDING', submittedAt: '2023-10-01T10:00:00Z' }
    ];

    const mockLibraryData = [
        { id: 202, creatorEmail: 'lib@test.com', libraryName: 'Library 1', status: 'PENDING', submittedAt: '2023-10-02T12:00:00Z' }
    ];

    beforeEach(() => {
        vi.clearAllMocks();
        global.fetch = vi.fn();
        localStorage.setItem('access_token', 'fake-admin-token');
    });

    afterEach(() => {
        cleanup();
        localStorage.clear();
    });

    it('UNIT: Renders the dashboard static elements and initializes WebSockets', () => {
        renderComponent();

        expect(screen.getByAltText('Book Rider Logo')).toBeInTheDocument();
        expect(screen.getByText('Podania o zatwierdzenie bibliotek')).toBeInTheDocument();
        expect(screen.getByText('Podania o zatwierdzenie kierowców')).toBeInTheDocument();

        expect(useWebSocketNotification).toHaveBeenCalledWith('administrator/library-requests', expect.any(Function));
        expect(useWebSocketNotification).toHaveBeenCalledWith('administrator/driver-applications', expect.any(Function));
    });

    it('UNIT: Handles Logout', () => {
        renderComponent();
        localStorage.setItem('role', 'admin');

        const logoutBtn = screen.getByText('Wyloguj się');
        fireEvent.click(logoutBtn);

        expect(localStorage.getItem('access_token')).toBeNull();
        expect(mockedNavigate).toHaveBeenCalledWith('/');
    });

    it('INTEGRATION (Driver): Fetches data, displays list, and processes "Open" action', async () => {
        (global.fetch as any).mockResolvedValueOnce({
            ok: true,
            json: async () => mockDriverData,
        });

        renderComponent();

        fireEvent.click(screen.getByText('Podania o zatwierdzenie kierowców'));

        await waitFor(() => {
            expect(global.fetch).toHaveBeenCalledWith(
                expect.stringContaining('/api/driver-applications'),
                expect.objectContaining({ method: 'GET' })
            );
            expect(screen.getByText('ID podania: 101')).toBeInTheDocument();
            expect(screen.getByText('Utworzone przez: driver@test.com')).toBeInTheDocument();
        });

        const openBtn = screen.getByText('Otwórz');
        (global.fetch as any).mockResolvedValueOnce({
            ok: true,
            json: async () => ({}),
        });

        fireEvent.click(openBtn);

        await waitFor(() => {
            expect(global.fetch).toHaveBeenCalledWith(
                expect.stringContaining('/api/driver-applications/101/status?status=UNDER_REVIEW'),
                expect.objectContaining({ method: 'PUT' })
            );
            expect(mockedNavigate).toHaveBeenCalledWith('/submissionDetailsDriver/101');
        });
    });

    it('INTEGRATION (Library): Fetches data, displays list, and processes "Open" action', async () => {
        (global.fetch as any).mockImplementation((url: string, options: any) => {
            const method = options?.method || 'GET';

            if (url.includes('driver-applications') && method === 'GET') {
                return Promise.resolve({ ok: true, json: async () => [] });
            }

            if (url.includes('library-requests') && method === 'GET') {
                return Promise.resolve({ ok: true, json: async () => mockLibraryData });
            }

            if (url.includes('status') && method === 'PUT') {
                return Promise.resolve({ ok: true, json: async () => ({}) });
            }

            return Promise.resolve({ ok: false });
        });

        renderComponent();

        fireEvent.click(screen.getByText('Podania o zatwierdzenie kierowców'));

        await waitFor(() => {
            expect(global.fetch).toHaveBeenCalledWith(
                expect.stringContaining('/api/driver-applications'),
                expect.objectContaining({ method: 'GET' })
            );
        });

        fireEvent.click(screen.getByText('Podania o zatwierdzenie bibliotek'));

        await waitFor(() => {
            expect(screen.getByText('Nazwa biblioteki: City Library')).toBeInTheDocument();
        });

        const openBtn = screen.getByText('Otwórz');
        fireEvent.click(openBtn);

        await waitFor(() => {
            expect(global.fetch).toHaveBeenCalledWith(
                expect.stringContaining('/api/library-requests/202/status?status=UNDER_REVIEW'),
                expect.objectContaining({ method: 'PUT' })
            );
            expect(mockedNavigate).toHaveBeenCalledWith('/submissionDetailsLibrary/202');
        });
    });

    it('INTEGRATION: Appends new data when "Load More" is clicked', async () => {
        (global.fetch as any).mockResolvedValueOnce({
            ok: true,
            json: async () => mockDriverData,
        });

        renderComponent();
        fireEvent.click(screen.getByText('Podania o zatwierdzenie kierowców'));

        await screen.findByText('ID podania: 101');

        (global.fetch as any).mockResolvedValueOnce({
            ok: true,
            json: async () => [{ id: 999, driverEmail: 'page2@test.com', status: 'PENDING', submittedAt: '2023-10-03T10:00:00Z' }],
        });

        const loadMoreBtn = screen.getByText('Załaduj więcej');
        fireEvent.click(loadMoreBtn);

        await waitFor(() => {
            expect(screen.getByText('ID podania: 101')).toBeInTheDocument();
            expect(screen.getByText('ID podania: 999')).toBeInTheDocument();
        });

        expect(global.fetch).toHaveBeenCalledWith(
            expect.stringContaining('page=1'),
            expect.anything()
        );
    });
});

import React, {useEffect, useRef, useState} from 'react';
import {useNavigate} from 'react-router-dom';
import {useWebSocketNotification} from '../Utils/useWebSocketNotification.tsx';
import {toast} from 'react-toastify';
import 'react-toastify/dist/ReactToastify.css';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL;

interface DriverApplication {
    id: number;
    driverEmail: string;
    status: string;
    submittedAt: string;
}

interface LibraryRequest {
    id: number;
    creatorEmail: string;
    libraryName: string;
    status: string;
    submittedAt: string;
}

const formatDate = (isoString: string) => {
    return new Date(isoString).toLocaleString('pl-PL', {
        year: 'numeric',
        month: 'long',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit'
    });
};

const SystemAdminDashboard: React.FC = () => {
    const [activeSection, setActiveSection] = useState<string>('librarySubmissions');
    const [driverApplications, setDriverApplications] = useState<DriverApplication[]>([]);
    const [libraryRequests, setLibraryRequests] = useState<LibraryRequest[]>([]);
    const [driverPage, setDriverPage] = useState(0);
    const [driverHasMore, setDriverHasMore] = useState(true);
    const [libraryPage, setLibraryPage] = useState(0);
    const [libraryHasMore, setLibraryHasMore] = useState(true);
    const navigate = useNavigate();

    const firstLoad = useRef(true);

    useWebSocketNotification('administrator/library-requests', () => {
        toast.info("Otrzymano nowe zgłoszenie biblioteki!", {
            position: "bottom-right",
        });
        console.log("New library request received!");
    });

    useWebSocketNotification('administrator/driver-applications', () => {
        toast.info("Otrzymano nowe zgłoszenie kierowcy!", {
            position: "bottom-right",
        });
        console.log("New driver request received!");
    });

    const handleChangePassword = ()=> {
        navigate('/system-admin-settings');
    }

    const handleLogout = () => {
        localStorage.removeItem('email');
        localStorage.removeItem('access_token');
        localStorage.removeItem('role');

        navigate('/');
    };

    useEffect(() => {
        if (firstLoad.current) {
            firstLoad.current = false;
            return;
        }

        if (activeSection === 'driverSubmissions') {
            setDriverApplications([]);
            setDriverPage(0);
            setDriverHasMore(true);
            fetchDriverApplications(0);
        } else if (activeSection === 'librarySubmissions') {
            setLibraryRequests([]);
            setLibraryPage(0);
            setLibraryHasMore(true);
            fetchLibraryRequests(0);
        }
    }, [activeSection]);

    const fetchDriverApplications = async (page: number = 0) => {
        const token = localStorage.getItem('access_token');
        if (!token) {
            console.error("No token found.");
            return;
        }

        try {
            const response = await fetch(`${API_BASE_URL}/api/driver-applications?statuses=PENDING,UNDER_REVIEW&page=${page}&size=1`, {
                method: 'GET',
                headers: {
                    'Authorization': `Bearer ${token}`,
                    'Content-Type': 'application/json',
                },
            });

            if (!response.ok) {
                throw new Error(`Error: ${response.statusText}`);
            }

            const data: DriverApplication[] = await response.json();

            if (data.length > 0) {
                setDriverApplications((prev) => [...prev, ...data]);
                setDriverPage(page + 1);
            } else {
                setDriverHasMore(false);
            }
        } catch (error) {
            console.error('Error:', error);
            setDriverHasMore(false);
        }
    };

    const fetchLibraryRequests = async (page: number) => {
        const token = localStorage.getItem('access_token');
        if (!token) return;

        try {
            const response = await fetch(`${API_BASE_URL}/api/library-requests?statuses=PENDING,UNDER_REVIEW&page=${page}&size=1`, {
                method: 'GET',
                headers: {
                    'Authorization': `Bearer ${token}`,
                    'Content-Type': 'application/json',
                },
            });

            if (!response.ok) throw new Error(`Error:: ${response.statusText}`);

            const data: LibraryRequest[] = await response.json();

            if (data.length > 0) {
                setLibraryRequests((prev) => [...prev, ...data]);
                setLibraryPage(page + 1);
            } else {
                setLibraryHasMore(false);
            }
        } catch (error) {
            console.error('Error:', error);
            setLibraryHasMore(false);
        }
    };

    const updateStatusAndNavigate = async (id: number, type: 'driver' | 'library') => {
        const token = localStorage.getItem('access_token');
        if (!token) return;

        const endpoint = type === 'driver' ? `/api/driver-applications/${id}/status?status=UNDER_REVIEW` : `/api/library-requests/${id}/status?status=UNDER_REVIEW`;

        try {
            const response = await fetch(`${API_BASE_URL}${endpoint}`, {
                method: 'PUT',
                headers: {
                    'Authorization': `Bearer ${token}`,
                    'Content-Type': 'application/json',
                },
            });

            if (!response.ok) throw new Error(`Error: ${response.statusText}`);

            navigate(type === 'driver' ? `/submissionDetailsDriver/${id}` : `/submissionDetailsLibrary/${id}`);
        } catch (error) {
            console.error('Error:', error);
        }
    };

    return (
        <div className="bg-[#314757] min-h-screen">
            <header
                className="flex justify-between items-center w-screen bg-[#3B576C] text-white sticky top-0 z-50 shadow-md">
                <div>
                    <img
                        className="relative w-[7%] h-auto object-cover left-[1%]"
                        alt="Book Rider Logo"
                        src="/book-rider-high-resolution-logo.png"
                    />
                </div>
                <div className="text-white p-4 flex justify-end">

                    <div className="flex items-center">
                        <button onClick={handleChangePassword} className="bg-gray-700 text-white px-6 py-2 rounded-md ml-4 whitespace-nowrap">
                            Ustawienia
                        </button>
                        <button onClick={handleLogout} className="bg-gray-700 text-white px-6 py-2 rounded-md ml-4 whitespace-nowrap">
                            Wyloguj się
                        </button>
                    </div>
                </div>
            </header>

            <header className="bg-[#314757] text-white flex justify-center p-3 text-xl">
                <button
                    onClick={() => setActiveSection('librarySubmissions')}
                    className={`px-12 py-3 rounded-md ${activeSection === 'librarySubmissions' ? 'bg-[#3B576C]' : 'bg-[#314757]'}`}
                >
                    Podania o zatwierdzenie bibliotek
                </button>
                <button
                    onClick={() => setActiveSection('driverSubmissions')}
                    className={`px-12 py-3 rounded-md ml-4 ${activeSection === 'driverSubmissions' ? 'bg-[#3B576C]' : 'bg-[#314757]'}`}
                >
                    Podania o zatwierdzenie kierowców
                </button>
            </header>


            <main className="p-0 max-w-4xl mx-auto">
                {activeSection === 'driverSubmissions' && (
                    <section>
                        <div className="space-y-5">
                            {driverApplications.length > 0 ? (
                                driverApplications.map((application) => (
                                    <div key={application.id} className="bg-white p-4 rounded-xl shadow-md">
                                        <h3 className="text-xl font-semibold mb-5 mt-1 text-gray-800">ID podania: {application.id}</h3>
                                        <p className="text-lg text-gray-600">Data wysłania: {formatDate(application.submittedAt)}</p>
                                        <p className="text-lg text-gray-600">Utworzone przez: {application.driverEmail}</p>
                                        <button
                                            onClick={() => updateStatusAndNavigate(application.id, 'driver')}
                                            className="mt-5 mb-1 text-lg font-semibold bg-[#F5BE3D] text-gray-700 px-4 py-2 rounded-md border-2 border-[#E7B33A]"
                                        >
                                            Otwórz
                                        </button>
                                    </div>
                                ))
                            ) : (
                                <p className="text-gray-400 text-center text-2xl">Brak podań.</p>
                            )}
                        </div>
                        {driverHasMore ? (
                            <button
                                onClick={() => fetchDriverApplications(driverPage)}
                                className="mt-4 bg-[#314757] text-gray-400 px-4 py-2 rounded w-full"
                            >
                                Załaduj więcej
                            </button>
                        ) : (
                            <p className="mt-4 text-gray-400 text-center">Brak więcej podań do wyświetlenia.</p>
                        )}
                    </section>
                )}

                {activeSection === 'librarySubmissions' && (
                    <section>
                        <div className="space-y-5">
                            {libraryRequests.length > 0 ? (
                                libraryRequests.map((request) => (
                                    <div key={request.id} className="bg-white p-4 rounded-xl shadow-md">
                                        <h3 className="text-xl font-semibold mb-5 mt-1 text-gray-800">ID podania: {request.id}</h3>
                                        <p className="text-lg text-gray-600">Data wysłania: {formatDate(request.submittedAt)}</p>
                                        <p className="text-lg text-gray-600">Utworzone przez: {request.creatorEmail}</p>
                                        <p className="text-lg text-gray-600">Nazwa biblioteki: {request.libraryName}</p>
                                        <button
                                            onClick={() => updateStatusAndNavigate(request.id, 'library')}
                                            className="mt-5 mb-1 text-lg font-semibold bg-[#F5BE3D] text-gray-700 px-4 py-2 rounded-md border-2 border-[#E7B33A]"
                                        >
                                            Otwórz
                                        </button>
                                    </div>
                                ))
                            ) : (
                                <p className="text-gray-400 text-center text-2xl">Brak podań.</p>
                            )}
                        </div>
                        {libraryHasMore ? (
                            <button
                                onClick={() => fetchLibraryRequests(libraryPage)}
                                className="mt-4 bg-[#314757] text-gray-400 px-4 py-2 rounded w-full"
                            >
                                Załaduj więcej
                            </button>
                        ) : (
                            <p className="mt-4 text-gray-400 text-center">Brak więcej podań do wyświetlenia.</p>
                        )}
                    </section>
                )}
            </main>
        </div>
    );
};

export default SystemAdminDashboard;

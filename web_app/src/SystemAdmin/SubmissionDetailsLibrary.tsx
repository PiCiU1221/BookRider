import React, { useState, useEffect } from 'react';
import { useNavigate, Link, useParams } from 'react-router-dom';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL;

interface Address {
    id: number;
    street: string;
    city: string;
    postalCode: string;
    latitude: number;
    longitude: number;
}

interface LibraryRequest {
    id: number;
    creatorEmail: string;
    reviewerId: string;
    address: Address;
    libraryName: string;
    phoneNumber: string;
    libraryEmail: string;
    status: string;
    submittedAt: string;
    reviewedAt: string;
    rejectionReason: string;
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

const SubmissionDetailsLibrary: React.FC = () => {
    const [email, setEmail] = useState<string | null>(null);
    const { submissionId } = useParams();
    const [libraryRequest, setLibraryRequest] = useState<LibraryRequest | null>(null);
    const [error, setError] = useState<string>('');
    const [showRejectionInput, setShowRejectionInput] = useState(false);
    const [rejectionReason, setRejectionReason] = useState('');
    const [selectedReason, setSelectedReason] = useState<string>('');
    const [validationError, setValidationError] = useState<string>('');
    const navigate = useNavigate();

    const getEmail = () => {
        return localStorage.getItem('email');
    }

    const handleLogout = () => {
        localStorage.removeItem('access_token');
        localStorage.removeItem('role');

        navigate('/');
    };

    useEffect(() => {
        const fetchLibraryRequest = async () => {
            const token = localStorage.getItem('access_token');
            if (!token) return;

            const userEmail = getEmail();
            if (userEmail) {
                setEmail(userEmail);
            }

            try {
                const response = await fetch(`${API_BASE_URL}/api/library-requests/${submissionId}`, {
                    method: 'GET',
                    headers: {
                        'Authorization': `Bearer ${token}`,
                        'Content-Type': 'application/json',
                    },
                });

                if (!response.ok) {
                    throw new Error('Failed to fetch library request');
                }
                const data: LibraryRequest = await response.json();
                setLibraryRequest(data);
            } catch (error) {
                setError(error instanceof Error ? error.message : 'Unknown error');
            }
        };

        fetchLibraryRequest();
    }, [submissionId]);

    const handleAccept = async () => {
        const token = localStorage.getItem('access_token');
        if (!token) return;

        try {
            const response = await fetch(`${API_BASE_URL}/api/library-requests/${submissionId}/status?status=APPROVED`, {
                method: 'PUT',
                headers: {
                    'Authorization': `Bearer ${token}`,
                    'Content-Type': 'application/json',
                },
            });

            if (!response.ok) {
                throw new Error('Failed to accept the library request');
            }

            setLibraryRequest((prev) => prev ? { ...prev, status: 'APPROVED' } : prev);

            navigate('/system-admin-dashboard');
        } catch (error) {
            setError(error instanceof Error ? error.message : 'Unknown error');
        }
    };

    const handleDecline = () => {
        setShowRejectionInput(true);
    };

    const handleReasonChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        setSelectedReason(e.target.value);
        if (e.target.value !== 'Inne') {
            setRejectionReason(e.target.value);
        } else {
            setRejectionReason('');
        }
    };

    const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        setRejectionReason(e.target.value);
    };

    const handleConfirmRejection = async () => {
        if (!selectedReason) {
            setValidationError('Proszę podać powód odrzucenia.');
            return;
        }

        if (selectedReason === 'Inne' && !rejectionReason.trim()) {
            setValidationError('Podaj przyczynę odmowy.');
            return;
        }

        const token = localStorage.getItem('access_token');
        if (!token) return;

        try {
            const response = await fetch(`${API_BASE_URL}/api/library-requests/${submissionId}/status?status=REJECTED&rejectionReason=${encodeURIComponent(rejectionReason)}`, {
                method: 'PUT',
                headers: {
                    'Authorization': `Bearer ${token}`,
                    'Content-Type': 'application/json',
                },
            });

            if (!response.ok) {
                throw new Error('Failed to reject the library request');
            }

            setLibraryRequest((prev) => prev ? { ...prev, status: 'REJECTED', rejectionReason } : prev);
            setShowRejectionInput(false);
            setSelectedReason('');
            setRejectionReason('');
            setValidationError('');

            navigate('/system-admin-dashboard');
        } catch (error) {
            setError(error instanceof Error ? error.message : 'Unknown error');
        }
    };

    if (error) {
        return <div>Error: {error}</div>;
    }

    return (
        <div className="bg-[#314757] min-h-screen flex flex-col items-center">
            <header
                className="flex justify-between items-center w-screen bg-[#3B576C] text-white sticky top-0 z-50 shadow-md px-2">
                <div>
                    <img
                        className="relative w-[7%] h-auto object-cover left-[1%]"
                        alt="Book Rider Logo"
                        src="/book-rider-high-resolution-logo.png"
                    />
                </div>
                <div className="text-white p-4 flex justify-end">
                    <div className="flex items-center">
                        {email && <span className="mr-4">{email}</span>}
                        <button onClick={handleLogout}
                                className="bg-gray-700 text-white px-6 py-2 rounded ml-4 whitespace-nowrap">
                            Wyloguj się
                        </button>
                    </div>
                </div>
            </header>

            <div className="flex justify-center items-center w-full mt-10">
                <div className="bg-white shadow-lg p-8 rounded-lg w-[90%] max-w-3xl">
                    <h1 className="text-2xl font-bold text-center mb-12">
                        Szczegóły podania nr: {submissionId}
                    </h1>

                    <div className="submission-info space-y-2 text-lg">
                        <p><strong>Nazwa biblioteki:</strong> {libraryRequest?.libraryName || 'Brak nazwy'}</p>
                        <p><strong>Email twórcy:</strong> {libraryRequest?.creatorEmail}</p>
                        <p>
                            <strong>Adres:</strong> {libraryRequest?.address ? `${libraryRequest.address.street}, ${libraryRequest.address.city}, ${libraryRequest.address.postalCode}` : "Brak adresu"}
                        </p>
                        <p><strong>Szerokość geograficzna:</strong> {libraryRequest?.address?.latitude || "Brak danych"}
                        </p>
                        <p><strong>Długość geograficzna:</strong> {libraryRequest?.address?.longitude || "Brak danych"}
                        </p>
                        <p><strong>Numer telefonu:</strong> {libraryRequest?.phoneNumber || "Brak numeru"}</p>
                        <p><strong>Email biblioteki:</strong> {libraryRequest?.libraryEmail || "Brak adresu e-mail"}</p>
                        <p><strong>Status:</strong> {libraryRequest?.status}</p>
                        <p><strong>Data
                            złożenia:</strong> {libraryRequest?.submittedAt ? formatDate(libraryRequest.submittedAt) : "Brak danych"}
                        </p>
                        {libraryRequest?.status === "Odrzucone" && (
                            <p><strong>Powód odrzucenia:</strong> {libraryRequest?.rejectionReason || "Brak powodu"}</p>
                        )}
                    </div>

                    {!showRejectionInput && (
                        <div className="button-container flex justify-center gap-6 mt-6">
                            <button onClick={handleAccept}
                                    className="mt-2 bg-[#3B576C] text-white px-3 py-2 rounded-md border-2 border-[#314757]">
                                Zatwierdź
                            </button>
                            <button onClick={handleDecline}
                                    className="mt-2 bg-white text-[#2D343A] px-5 py-2 rounded-md border-2 border-[#314757]">
                                Odrzuć
                            </button>
                        </div>
                    )}

                    {showRejectionInput && (
                        <div className="rejection-input-container mt-12">

                            <div className="border-t border-gray-300 mt-12 mb-6"></div>

                            <p className="font-semibold py-5 text-xl">Wybierz powód odrzucenia:</p>
                            <div className="space-y-3">
                                {["Wprowadzono niepoprawne dane (nie możemy potwierdzić istnienia biblioteki)",
                                    "Biblioteka została już dodana do systemu",
                                    "Inne"].map((reason) => (
                                    <label key={reason} className="block">
                                        <input
                                            type="radio"
                                            name="rejectionReason"
                                            value={reason}
                                            onChange={handleReasonChange}
                                            className="mr-2"
                                        />
                                        {reason}
                                    </label>
                                ))}

                                {selectedReason === 'Inne' && (
                                    <input
                                        type="text"
                                        placeholder="Podaj przyczynę odmowy akceptacji podania"
                                        value={rejectionReason}
                                        onChange={handleInputChange}
                                        className="border p-2 rounded w-full"
                                        maxLength={150}
                                    />
                                )}

                                {validationError && (
                                    <div className="text-red-600 font-semibold text-base mb-4">{validationError}</div>
                                )}

                                <div className="flex justify-center gap-6 mt-6">
                                    <button onClick={handleConfirmRejection}
                                            className="mt-2 bg-[#3B576C] text-white px-10 py-2 rounded-md border-2 border-[#314757]">
                                        Wyślij
                                    </button>
                                </div>
                            </div>
                        </div>
                    )}

                    <div className="border-t border-gray-300 mt-12 mb-6"></div>

                    <div className="text-center mt-6">
                        <Link to="/system-admin-dashboard">
                            <button
                                className="bg-white text-[#3B576C] text-base font-thin hover:text-[#2D343A] transition-all duration-[0.3s] text-center tracking-[0] leading-[normal]">Powrót
                                do strony głównej
                            </button>
                        </Link>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default SubmissionDetailsLibrary;

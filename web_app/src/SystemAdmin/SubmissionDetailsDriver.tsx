import React, { useState, useEffect } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL;

interface DriverDocument {
    documentType: string;
    documentPhotoUrl: string;
    expiryDate: string;
}

interface DriverApplication {
    id: number;
    userEmail: string;
    reviewerID: string | null;
    status: string;
    submittedAt: string;
    reviewedAt: string | null;
    rejectionReason: string | null;
    driverDocuments: DriverDocument[];
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

const formatDateDocument = (isoString: string) => {
    return new Date(isoString).toLocaleDateString('pl-PL', {
        year: 'numeric',
        month: 'long',
        day: 'numeric'
    });
};

const SubmissionDetailsDriver: React.FC = () => {
    const [email, setEmail] = useState<string | null>(null);
    const { submissionId } = useParams();
    const [driverApplication, setDriverApplication] = useState<DriverApplication | null>(null);
    const [error, setError] = useState<string>('');
    const [formError, setFormError] = useState<string>('');
    const [showRejectionInput, setShowRejectionInput] = useState(false);
    const [selectedReason, setSelectedReason] = useState<string>('');
    const [customRejectionReason, setCustomRejectionReason] = useState('');
    const navigate = useNavigate();

    const getEmail = () => {
        return localStorage.getItem('email');
    };

    const handleLogout = () => {
        localStorage.removeItem('access_token');
        localStorage.removeItem('role');
        navigate('/');
    };

    useEffect(() => {
        const fetchDriverApplication = async () => {
            const token = localStorage.getItem('access_token');
            if (!token) return;

            const userEmail = getEmail();
            if (userEmail) {
                setEmail(userEmail);
            }

            try {
                const response = await fetch(`${API_BASE_URL}/api/driver-applications/${submissionId}`, {
                    method: 'GET',
                    headers: {
                        'Authorization': `Bearer ${token}`,
                        'Content-Type': 'application/json',
                    },
                });

                if (!response.ok) {
                    throw new Error('Błąd podczas pobierania danych.');
                }
                const data: DriverApplication = await response.json();
                setDriverApplication(data);
            } catch (error) {
                setError(error instanceof Error ? error.message : 'Nieznany błąd.');
            }
        };

        fetchDriverApplication();
    }, [submissionId]);

    const handleAccept = async () => {
        const token = localStorage.getItem('access_token');
        if (!token) return;

        try {
            const response = await fetch(`${API_BASE_URL}/api/driver-applications/${submissionId}/status?status=APPROVED`, {
                method: 'PUT',
                headers: {
                    'Authorization': `Bearer ${token}`,
                    'Content-Type': 'application/json',
                },
            });

            if (!response.ok) {
                throw new Error('Błąd podczas zatwierdzania podania.');
            }

            setDriverApplication((prev) => prev ? { ...prev, status: 'APPROVED' } : prev);

            navigate('/system-admin-dashboard');
        } catch (error) {
            setError(error instanceof Error ? error.message : 'Nieznany błąd.');
        }
    };

    const handleDecline = () => {
        setShowRejectionInput(true);
    };

    const handleReasonChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        setSelectedReason(e.target.value);
        setFormError('');
        if (e.target.value !== 'Inne') {
            setCustomRejectionReason('');
        }
    };

    const handleCustomInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        setCustomRejectionReason(e.target.value);
        setFormError('');
    };

    const handleConfirmRejection = async () => {
        const token = localStorage.getItem('access_token');
        if (!token) return;

        const finalRejectionReason = selectedReason === 'Inne' ? customRejectionReason : selectedReason;

        if (finalRejectionReason.trim()) {
            try {
                const response = await fetch(`${API_BASE_URL}/api/driver-applications/${submissionId}/status?status=REJECTED&rejectionReason=${encodeURIComponent(finalRejectionReason)}`, {
                    method: 'PUT',
                    headers: {
                        'Authorization': `Bearer ${token}`,
                        'Content-Type': 'application/json',
                    },
                });

                if (!response.ok) {
                    throw new Error('Błąd podczas odrzucania podania.');
                }

                setDriverApplication((prev) => prev ? { ...prev, status: 'REJECTED', rejectionReason: finalRejectionReason } : prev);
                setShowRejectionInput(false);
                setSelectedReason('');
                setCustomRejectionReason('');
                setFormError('');

                navigate('/system-admin-dashboard');
            } catch (error) {
                setError(error instanceof Error ? error.message : 'Nieznany błąd.');
            }
        } else {
            setFormError('Proszę podać powód odrzucenia.');
        }
    };

    if (error) {
        return <div className="text-red-600 text-center mt-6">{error}</div>;
    }

    return (
        <div className="bg-[#314757] min-h-screen flex flex-col items-center">
            <header className="flex justify-between items-center w-screen bg-[#3B576C] text-white sticky top-0 z-50 shadow-md px-2">
                <div>
                    <img
                        className="w-[7%] h-auto object-cover"
                        alt="Book Rider Logo"
                        src="/book-rider-high-resolution-logo.png"
                    />
                </div>
                <div className="text-white flex items-center">
                    {email && <span className="mr-4">{email}</span>}
                    <button
                        onClick={handleLogout}
                        className="bg-gray-700 text-white px-6 py-2 rounded ml-4 whitespace-nowrap"
                    >
                        Wyloguj się
                    </button>
                </div>
            </header>

            <div className="flex justify-center items-center w-full mt-10">
                <div className="bg-white shadow-lg p-8 rounded-lg w-[90%] max-w-3xl">
                    <h1 className="text-2xl font-bold text-center mb-10">
                        Szczegóły podania nr: {driverApplication?.id}
                    </h1>

                    <div className="submission-info space-y-2 text-lg">
                        <p><strong>Email użytkownika:</strong> {driverApplication?.userEmail}</p>
                        <p><strong>Status:</strong> {driverApplication?.status}</p>
                        <p><strong>Data
                            złożenia:</strong> {driverApplication?.submittedAt ? formatDate(driverApplication.submittedAt) : "Brak danych"}
                        </p>
                    </div>

                    <div className="border-t border-gray-300 my-8 mb-[6%]"></div>

                    {driverApplication?.driverDocuments && driverApplication.driverDocuments.length > 0 && (
                        <div className="mt-12 flex flex-col items-center">
                            <h2 className="text-lg font-semibold mb-4 text-center">Dokument kierowcy</h2>
                            {driverApplication.driverDocuments.map((doc, index) => (
                                <div key={index} className="border p-4 rounded-md shadow-sm mb-4 w-full max-w-md">
                                    <p><strong>Typ dokumentu:</strong> {doc.documentType}</p>
                                    <p><strong>Data ważności:</strong> {formatDateDocument(doc.expiryDate)}</p>
                                    <div className="flex justify-center mt-2">
                                        <img src={doc.documentPhotoUrl} alt="Zdjęcie dokumentu"
                                             className="rounded-lg shadow max-w-xs"/>
                                    </div>
                                </div>
                            ))}
                        </div>
                    )}

                    {!showRejectionInput ? (
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
                    ) : (
                        <div className="rejection-input-container mt-6 ml-6">
                            <label className="font-semibold block mb-2 py-5 text-xl">Wybierz powód odrzucenia:</label>
                            <div className="space-y-3">
                                {["Nieprawidłowy dokument", "Zdjęcie dokumentu ma zbyt niską jakość", "Nieprawidłowe dane kierowcy", "Inne"].map((reason) => (
                                    <label key={reason} className="block">
                                        <input
                                            type="radio"
                                            name="rejectionReason"
                                            value={reason}
                                            checked={selectedReason === reason}
                                            onChange={handleReasonChange}
                                            className="mr-2"
                                        />
                                        {reason}
                                    </label>
                                ))}

                                {selectedReason === "Inne" && (
                                    <input
                                        type="text"
                                        placeholder="Podaj przyczynę odmowy akceptacji aplikacji"
                                        value={customRejectionReason}
                                        onChange={handleCustomInputChange}
                                        className="border p-2 rounded w-full"
                                        maxLength={150}
                                    />
                                )}

                                {formError && (
                                    <div className="text-red-600 font-semibold text-base mt-2">{formError}</div>
                                )}

                                <div className="flex justify-center gap-6 mt-6 mr-6">
                                    <button onClick={handleConfirmRejection}
                                            className="mt-2 bg-[#3B576C] text-white px-10 py-2 rounded-md border-2 border-[#314757]">
                                        Wyślij
                                    </button>
                                </div>
                            </div>
                        </div>
                    )}


                    <div className="border-t border-gray-300 mt-12 mb-6"></div>

                    <div className="text-center mt-1">
                        <Link to="/system-admin-dashboard">
                            <button className="bg-white text-[#3B576C] text-base font-thin hover:text-[#2D343A] transition-all duration-[0.3s] text-center tracking-[0] leading-[normal]">Powrót do strony głównej</button>
                        </Link>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default SubmissionDetailsDriver;

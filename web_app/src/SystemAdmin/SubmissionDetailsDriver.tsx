import React, { useState, useEffect } from 'react';
import { Link, useParams } from 'react-router-dom';

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
    rejectionReason: string | null;
}

const SubmissionDetailsDriver: React.FC = () => {
    const { submissionType, submissionId } = useParams();
    const [libraryRequest, setLibraryRequest] = useState<LibraryRequest | null>(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string>('');
    const [showRejectionInput, setShowRejectionInput] = useState(false);
    const [selectedReason, setSelectedReason] = useState<string>('');
    const [customRejectionReason, setCustomRejectionReason] = useState('');

    useEffect(() => {
        const fetchLibraryRequest = async () => {
            const token = localStorage.getItem('access_token');
            if (!token) return;

            try {
                const response = await fetch(`${API_BASE_URL}/api/driver-applications/${submissionId}`, {
                    method: 'GET',
                    headers: {
                        'Authorization': `Bearer ${token}`,
                        'Content-Type': 'application/json',
                    },
                });

                if (!response.ok) {
                    throw new Error('Failed to fetch driver application');
                }
                const data: LibraryRequest = await response.json();
                setLibraryRequest(data);
            } catch (error) {
                setError(error instanceof Error ? error.message : 'Unknown error');
            } finally {
                setLoading(false);
            }
        };

        fetchLibraryRequest();
    }, [submissionId]);

    const handleAccept = () => {
        alert('Podanie zostało zaakceptowane!');
    };

    const handleDecline = () => {
        setShowRejectionInput(true);
    };

    const handleReasonChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        setSelectedReason(e.target.value);
        if (e.target.value !== 'Inne') {
            setCustomRejectionReason('');
        }
    };

    const handleCustomInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        setCustomRejectionReason(e.target.value);
    };

    const handleConfirmRejection = () => {
        const finalRejectionReason = selectedReason === 'Inne' ? customRejectionReason : selectedReason;

        if (finalRejectionReason.trim()) {
            alert(`Podanie zostało odrzucone: ${finalRejectionReason}`);
            setShowRejectionInput(false);
            setSelectedReason('');
            setCustomRejectionReason('');
        } else {
            alert('Proszę podać powód odrzucenia.');
        }
    };

    if (loading) {
        return <div>Loading...</div>;
    }

    if (error) {
        return <div>Error: {error}</div>;
    }

    return (
        <section className="submission-details-container">
            <h1 className="submission-title">
                Szczegóły podania ({submissionType}) nr: {submissionId}
            </h1>

            <div className="submission-card">
                <div className="submission-info">
                    <p><strong>Typ:</strong> {submissionType}</p>
                    <p><strong>ID:</strong> {submissionId}</p>
                    <p><strong>Opis:</strong> {libraryRequest?.libraryName || 'Brak opisu'}</p>
                    <p><strong>Status:</strong> {libraryRequest?.status}</p>
                    <p><strong>Data złożenia:</strong> {libraryRequest?.submittedAt}</p>
                    <p><strong>Data przeglądu:</strong> {libraryRequest?.reviewedAt}</p>
                </div>

                <div className="button-container">
                    <button onClick={handleAccept} className="accept-button">
                        Zatwierdź
                    </button>
                    <button onClick={handleDecline} className="decline-button">
                        Odrzuć
                    </button>
                </div>

                {showRejectionInput && (
                    <div className="rejection-input-container">
                        <label><strong>Wybierz powód odrzucenia:</strong></label>
                        <div className="rejection-options">
                            <label>
                                <input
                                    type="radio"
                                    name="rejectionReason"
                                    value="Nieprawidłowy dokument"
                                    checked={selectedReason === "Nieprawidłowy dokument"}
                                    onChange={handleReasonChange}
                                />
                                Nieprawidłowy dokument
                            </label>
                            <label>
                                <input
                                    type="radio"
                                    name="rejectionReason"
                                    value="Zdjęcie dokumentu ma zbyt niską jakość"
                                    checked={selectedReason === "Zdjęcie dokumentu ma zbyt niską jakość"}
                                    onChange={handleReasonChange}
                                />
                                Zdjęcie dokumentu ma zbyt niską jakość
                            </label>
                            <label>
                                <input
                                    type="radio"
                                    name="rejectionReason"
                                    value="Nieprawidłowe dane kierowcy"
                                    checked={selectedReason === "Nieprawidłowe dane kierowcy"}
                                    onChange={handleReasonChange}
                                />
                                Nieprawidłowe dane kierowcy
                            </label>
                            <label>
                                <input
                                    type="radio"
                                    name="rejectionReason"
                                    value="Inne"
                                    checked={selectedReason === "Inne"}
                                    onChange={handleReasonChange}
                                />
                                Inne
                            </label>

                        {selectedReason === "Inne" && (
                            <input
                                type="text"
                                placeholder="Podaj przyczynę odmowy"
                                value={customRejectionReason}
                                onChange={handleCustomInputChange}
                                className="rejection-input"
                                maxLength={100}
                            />
                        )}

                        <button onClick={handleConfirmRejection} className="confirm-rejection-button">
                            Wyślij
                        </button>
                        </div>
                    </div>
                )}
            </div>
            <div style={{ marginTop: '20px', textAlign: 'center' }}>
                <Link to="/system-admin-dashboard">
                    <button className="back-button">Powrót do strony głównej</button>
                </Link>
            </div>
        </section>
    );
};

export default SubmissionDetailsDriver;

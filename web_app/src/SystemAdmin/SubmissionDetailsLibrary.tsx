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
    rejectionReason: string;
}

const SubmissionDetailsLibrary: React.FC = () => {
    const { submissionType, submissionId } = useParams();
    const [libraryRequest, setLibraryRequest] = useState<LibraryRequest | null>(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string>('');
    const [showRejectionInput, setShowRejectionInput] = useState(false);
    const [rejectionReason, setRejectionReason] = useState('');
    const [selectedReason, setSelectedReason] = useState<string>('');

    useEffect(() => {
        const fetchLibraryRequest = async () => {
            const token = localStorage.getItem('access_token');
            if (!token) return;

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
            } finally {
                setLoading(false);
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

            alert('Podanie zostało zaakceptowane!');
            setLibraryRequest((prev) => prev ? { ...prev, status: 'APPROVED' } : prev);
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
            alert('Wybierz powód odrzucenia.');
            return;
        }

        if (selectedReason === 'Inne' && !rejectionReason.trim()) {
            alert('Podaj przyczynę odmowy.');
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

            alert(`Podanie zostało odrzucone: ${rejectionReason}`);
            setLibraryRequest((prev) => prev ? { ...prev, status: 'REJECTED', rejectionReason } : prev);
            setShowRejectionInput(false);
            setSelectedReason('');
            setRejectionReason('');
        } catch (error) {
            setError(error instanceof Error ? error.message : 'Unknown error');
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
                        <p>Wybierz powód odrzucenia:</p>
                        <label>
                            <input
                                type="radio"
                                name="rejectionReason"
                                value="Wprowadzono niepoprawne dane (nie możemy potwierdzić istnienia biblioteki)"
                                onChange={handleReasonChange}
                            />
                            Wprowadzono niepoprawne dane (nie możemy potwierdzić istnienia biblioteki)
                        </label>
                        <label>
                            <input
                                type="radio"
                                name="rejectionReason"
                                value="Biblioteka została już dodana do systemu"
                                onChange={handleReasonChange}
                            />
                            Biblioteka została już dodana do systemu
                        </label>
                        <label>
                            <input
                                type="radio"
                                name="rejectionReason"
                                value="Inne"
                                onChange={handleReasonChange}
                            />
                            Inne
                        </label>

                        {selectedReason === 'Inne' && (
                            <input
                                type="text"
                                placeholder="Podaj przyczynę odmowy realizacji podania"
                                value={rejectionReason}
                                onChange={handleInputChange}
                                className="rejection-input"
                                maxLength={100}
                            />
                        )}

                        <button
                            onClick={handleConfirmRejection}
                            className="confirm-rejection-button"
                        >
                            Wyślij
                        </button>
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

export default SubmissionDetailsLibrary;

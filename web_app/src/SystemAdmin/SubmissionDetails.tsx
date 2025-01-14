import React, { useState } from 'react';
import { Link, useParams } from 'react-router-dom';

const SubmissionDetails: React.FC = () => {
    const { submissionType, submissionId } = useParams();
    const [showRejectionInput, setShowRejectionInput] = useState(false);
    const [rejectionReason, setRejectionReason] = useState('');

    const handleAccept = () => {
        alert(`Podanie zostało zaakceptowane!`);
    };

    const handleDecline = () => {
        setShowRejectionInput(true);
    };

    const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        setRejectionReason(e.target.value);
    };

    const handleConfirmRejection = () => {
        if (rejectionReason.trim()) {
            alert(`Podanie zostało odrzucone: ${rejectionReason}`);
            setShowRejectionInput(false);
            setRejectionReason('');
        } else {
            alert('Please provide a rejection reason.');
        }
    };

    return (
        <section className="submission-details-container">
            <h1 className="submission-title">
                Szczegóły podania ({submissionType}) nr: {submissionId}
            </h1>

            <div className="submission-card">
                <div className="submission-info">
                    <p>
                        <strong>Typ:</strong> {submissionType}
                    </p>
                    <p>
                        <strong>ID:</strong> {submissionId}
                    </p>
                    <p>
                        Tutaj będzie znajdowało się podanie.
                    </p>
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
                        <input
                            type="text"
                            placeholder="Podaj przyczynę odmowy realizacji podania"
                            value={rejectionReason}
                            onChange={handleInputChange}
                            className="rejection-input"
                            maxLength={100}
                        />
                        <button
                            onClick={handleConfirmRejection}
                            className="confirm-rejection-button"
                        >
                            Wyślij
                        </button>
                    </div>
                )}
            </div>
            <div style={{marginTop: '20px', textAlign: 'center'}}>
                <Link to="/sys-admin-dashboard">
                    <button className="back-button">Powrót do
                        strony głównej
                    </button>
                </Link>
            </div>
        </section>
    )
        ;
};

export default SubmissionDetails;

import React from 'react';
import {Link, useParams} from 'react-router-dom';

const SubmissionDetails: React.FC = () => {
    const { submissionType, submissionId } = useParams(); // Retrieve params from URL

    const handleAccept = () => {
        alert(`${submissionType} submission ${submissionId} accepted!`);
        // Add functionality for accepting the submission here
    };

    const handleDecline = () => {
        alert(`${submissionType} submission ${submissionId} declined!`);
        // Add functionality for declining the submission here
    };

    return (
        <div style={{ padding: '20px', fontFamily: 'Arial, sans-serif' }}>
            <h1 style={{ color: '#fff' }}>
                {submissionType} Submission {submissionId} Details
            </h1>
            <div
                style={{
                    backgroundColor: '#38424a',
                    padding: '20px',
                    borderRadius: '4px',
                    marginTop: '20px',
                    color: '#fff',
                }}
            >
                <p>Details of {submissionType} submission {submissionId} go here.</p>
                <div>
                    <button
                        onClick={handleAccept}
                        style={acceptButtonStyle}
                    >
                        Zatwierdź
                    </button>
                    <button
                        onClick={handleDecline}
                        style={declineButtonStyle}
                    >
                        Odrzuć
                    </button>
                </div>
            </div>
            {/* Navigation */}
            <div style={{marginTop: '20px', textAlign: 'center'}}>
                <Link to="/sys-admin-dashboard">
                    <button style={{backgroundColor: '#282e33', margin: '10px', padding: '10px 20px'}}>Powrót do strony głównej</button>
                </Link>
            </div>
        </div>
    );
};

const acceptButtonStyle = {
    padding: '8px 16px',
    margin: '0 10px',
    backgroundColor: '#3B576C',
    color: '#fff',
    border: 'none',
    borderRadius: '4px',
    cursor: 'pointer',
};

const declineButtonStyle = {
    padding: '8px 16px',
    margin: '0 10px',
    backgroundColor: '#282e33',
    color: '#fff',
    border: 'none',
    borderRadius: '4px',
    cursor: 'pointer',
};

export default SubmissionDetails;

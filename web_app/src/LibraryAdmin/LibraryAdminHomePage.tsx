import { useState } from 'react';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL;

interface Librarian {
    id: string;
    username: string;
    firstName: string;
    lastName: string;
}

const LibraryAdminHomePage = () => {
    const [usernameSearch, setUsernameSearch] = useState('');
    const [newLibrarian, setNewLibrarian] = useState({ username: '', firstName: '', lastName: '' });
    const [librarians, setLibrarians] = useState<Librarian[]>([]);
    const [message, setMessage] = useState('');

    const token = localStorage.getItem('access_token');

    const fetchLibrarian = async () => {
        try {
            const response = await fetch(`${API_BASE_URL}/api/library-admins/librarians?username=${usernameSearch}`, {
                method: 'GET',
                headers: {
                    Authorization: `Bearer ${token}`,
                },
            });

            if (response.ok) {
                const data = await response.json();
                setLibrarians([data]);
            } else {
                setLibrarians([]);
                setMessage('Librarian not found');
            }
        } catch (err) {
            console.error(err);
            setMessage('Error fetching librarian');
        }
    };

    const addLibrarian = async () => {
        try {
            const res = await fetch(`${API_BASE_URL}/api/library-admins/librarians`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    Authorization: `Bearer ${token}`,
                },
                body: JSON.stringify(newLibrarian),
            });
            if (res.ok) {
                setMessage('Librarian added successfully');
                setNewLibrarian({ username: '', firstName: '', lastName: '' });
            } else {
                setMessage('Failed to add librarian');
            }
        } catch (err) {
            console.error(err);
            setMessage('Error adding librarian');
        }
    };

    const resetPassword = async (username: string) => {
        try {
            const res = await fetch(`${API_BASE_URL}/api/library-admins/librarians/reset-password/${username}`, {
                method: 'PATCH',
                headers: {
                    Authorization: `Bearer ${token}`,
                },
            });
            setMessage(res.ok ? 'Password reset successfully' : 'Failed to reset password');
        } catch (err) {
            console.error(err);
            setMessage('Error resetting password');
        }
    };

    const deleteLibrarian = async (username: string) => {
        try {
            const res = await fetch(`${API_BASE_URL}/api/library-admins/librarians/${username}`, {
                method: 'DELETE',
                headers: {
                    Authorization: `Bearer ${token}`,
                },
            });
            if (res.ok) {
                setLibrarians(librarians.filter(lib => lib.username !== username));
                setMessage('Librarian deleted');
            } else {
                setMessage('Failed to delete librarian');
            }
        } catch (err) {
            console.error(err);
            setMessage('Error deleting librarian');
        }
    };

    return (
        <div className="max-w-4xl mx-auto p-6">
            <h2 className="text-2xl font-bold text-gray-800 mb-6">Librarian Management</h2>

            <div className="mb-8">
                <h3 className="text-xl font-semibold mb-2">Search Librarian</h3>
                <div className="flex gap-2">
                    <input
                        type="text"
                        placeholder="Username"
                        value={usernameSearch}
                        onChange={(e) => setUsernameSearch(e.target.value)}
                        className="border border-gray-300 rounded p-2 w-full"
                    />
                    <button
                        onClick={fetchLibrarian}
                        className="bg-[#3B576C] text-white px-4 py-2 rounded"
                    >
                        Search
                    </button>
                </div>
            </div>

            <div className="mb-8">
                <h3 className="text-xl font-semibold mb-2">Add Librarian</h3>
                <div className="grid grid-cols-1 sm:grid-cols-3 gap-2">
                    <input
                        type="text"
                        placeholder="Username"
                        value={newLibrarian.username}
                        onChange={(e) => setNewLibrarian({ ...newLibrarian, username: e.target.value })}
                        className="border border-gray-300 rounded p-2"
                    />
                    <input
                        type="text"
                        placeholder="First Name"
                        value={newLibrarian.firstName}
                        onChange={(e) => setNewLibrarian({ ...newLibrarian, firstName: e.target.value })}
                        className="border border-gray-300 rounded p-2"
                    />
                    <input
                        type="text"
                        placeholder="Last Name"
                        value={newLibrarian.lastName}
                        onChange={(e) => setNewLibrarian({ ...newLibrarian, lastName: e.target.value })}
                        className="border border-gray-300 rounded p-2"
                    />
                </div>
                <button
                    onClick={addLibrarian}
                    className="mt-3 bg-[#3B576C] text-white px-6 py-2 rounded"
                >
                    Add Librarian
                </button>
            </div>

            {librarians.length > 0 && (
                <div className="mb-6">
                    <h3 className="text-xl font-semibold mb-2">Librarian Info</h3>
                    <ul className="space-y-4">
                        {librarians.map(lib => (
                            <li key={lib.id} className="border border-gray-300 rounded p-4 flex justify-between items-center">
                                <div>
                                    <p className="font-medium">{lib.firstName} {lib.lastName}</p>
                                    <p className="text-gray-600">Username: {lib.username}</p>
                                </div>
                                <div className="flex gap-2">
                                    <button
                                        onClick={() => resetPassword(lib.username)}
                                        className="bg-yellow-500 text-white px-3 py-1 rounded"
                                    >
                                        Reset Password
                                    </button>
                                    <button
                                        onClick={() => deleteLibrarian(lib.username)}
                                        className="bg-red-600 text-white px-3 py-1 rounded"
                                    >
                                        Delete
                                    </button>
                                </div>
                            </li>
                        ))}
                    </ul>
                </div>
            )}

            {message && <p className="text-sm text-gray-700 mt-4">{message}</p>}
        </div>
    );
};

export default LibraryAdminHomePage;

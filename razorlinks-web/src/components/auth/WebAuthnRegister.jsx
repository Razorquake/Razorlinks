import React, { useState, useEffect } from 'react';
import axios from 'axios';
import toast from 'react-hot-toast';

const WebAuthnRegister = () => {
    const [isLoading, setIsLoading] = useState(false);
    const [credentials, setCredentials] = useState([]);
    const [isSupported, setIsSupported] = useState(true);

    useEffect(() => {
        if (!window.PublicKeyCredential) {
            setIsSupported(false);
            return;
        }
        loadCredentials();
    }, []);

    const base64urlToUint8Array = (base64url) => {
        const base64 = base64url.replace(/-/g, '+').replace(/_/g, '/');
        const padLen = (4 - (base64.length % 4)) % 4;
        const padded = base64 + '='.repeat(padLen);
        const binary = atob(padded);
        const bytes = new Uint8Array(binary.length);
        for (let i = 0; i < binary.length; i++) {
            bytes[i] = binary.charCodeAt(i);
        }
        return bytes;
    };

    const uint8ArrayToBase64url = (uint8Array) => {
        let binary = '';
        uint8Array.forEach(byte => binary += String.fromCharCode(byte));
        const base64 = btoa(binary);
        return base64.replace(/\+/g, '-').replace(/\//g, '_').replace(/=/g, '');
    };

    const loadCredentials = async () => {
        try {
            const token = localStorage.getItem('JWT_TOKEN');
            const response = await axios.get(
                `${import.meta.env.VITE_BACKEND_URL}/api/auth/webauthn/credentials`,
                { headers: { Authorization: `Bearer ${token}` }}
            );
            setCredentials(response.data);
        } catch (err) {
            console.error('Failed to load credentials:', err);
        }
    };

    const handleRegisterPasskey = async () => {
        setIsLoading(true);

        try {
            const token = localStorage.getItem('JWT_TOKEN');

            const optionsResponse = await axios.post(
                `${import.meta.env.VITE_BACKEND_URL}/api/auth/webauthn/register/options`,
                {},
                { headers: { Authorization: `Bearer ${token}` }}
            );

            const options = optionsResponse.data;

            const credential = await navigator.credentials.create({
                publicKey: {
                    challenge: base64urlToUint8Array(options.challenge),
                    rp: options.rp,
                    user: {
                        id: base64urlToUint8Array(options.user.id),
                        name: options.user.name,
                        displayName: options.user.displayName
                    },
                    pubKeyCredParams: options.pubKeyCredParams,
                    timeout: options.timeout,
                    attestation: options.attestation,
                    authenticatorSelection: options.authenticatorSelection,
                    excludeCredentials: options.excludeCredentials
                        ? options.excludeCredentials.map(cred => ({
                            type: cred.type,
                            id: base64urlToUint8Array(cred.id)
                        }))
                        : []
                }
            });

            if (!credential) {
                throw new Error('Failed to create credential');
            }

            const credentialForServer = {
                id: credential.id,
                rawId: uint8ArrayToBase64url(new Uint8Array(credential.rawId)),
                response: {
                    clientDataJSON: uint8ArrayToBase64url(new Uint8Array(credential.response.clientDataJSON)),
                    attestationObject: uint8ArrayToBase64url(new Uint8Array(credential.response.attestationObject)),
                    transports: credential.response.getTransports ? credential.response.getTransports() : []
                },
                type: credential.type
            };

            await axios.post(
                `${import.meta.env.VITE_BACKEND_URL}/api/auth/webauthn/register/verify`,
                credentialForServer,
                { headers: { Authorization: `Bearer ${token}` }}
            );

            toast.success('Passkey registered successfully!');
            loadCredentials();

        } catch (err) {
            console.error('Passkey registration error:', err);

            if (err.name === 'NotAllowedError') {
                toast.error('Registration cancelled');
            } else if (err.name === 'InvalidStateError') {
                toast.error('Device already registered');
            } else {
                toast.error('Failed to register passkey');
            }
        } finally {
            setIsLoading(false);
        }
    };

    const handleDeleteCredential = async (credentialId) => {
        if (!window.confirm('Delete this passkey?')) return;

        try {
            const token = localStorage.getItem('JWT_TOKEN');
            await axios.delete(
                `${import.meta.env.VITE_BACKEND_URL}/api/auth/webauthn/credentials/${credentialId}`,
                { headers: { Authorization: `Bearer ${token}` }}
            );

            toast.success('Passkey deleted');
            loadCredentials();
        } catch (err) {
            toast.error('Failed to delete passkey');
        }
    };

    if (!isSupported) {
        return (
            <div className="p-4 bg-red-50 rounded-md">
                <p className="text-red-600">Passkeys not supported in this browser</p>
            </div>
        );
    }

    return (
        <div className="space-y-6">
            <div>
                <h2 className="text-2xl font-bold text-slate-800">Manage Passkeys</h2>
                <p className="text-slate-600 text-sm mt-1">
                    Sign in quickly with fingerprint, face, or device PIN
                </p>
            </div>

            <button
                onClick={handleRegisterPasskey}
                className="bg-custom-gradient text-white px-6 py-2 rounded-md font-semibold hover:opacity-90 disabled:opacity-50"
                disabled={isLoading}
            >
                {isLoading ? 'Creating...' : '➕ Add New Passkey'}
            </button>

            {credentials.length > 0 && (
                <div className="space-y-3">
                    <h3 className="font-semibold text-lg">Your Passkeys</h3>
                    {credentials.map((cred) => (
                        <div key={cred.id} className="flex justify-between items-center p-4 bg-slate-50 rounded-md border">
                            <div>
                                <p className="font-semibold">🔑 {cred.credentialName || 'Passkey'}</p>
                                <p className="text-sm text-slate-600">
                                    Created: {new Date(cred.createdAt).toLocaleDateString()}
                                </p>
                            </div>
                            <button
                                onClick={() => handleDeleteCredential(cred.credentialId)}
                                className="text-red-600 hover:text-red-800"
                            >
                                🗑️
                            </button>
                        </div>
                    ))}
                </div>
            )}

            {credentials.length === 0 && (
                <div className="text-center py-8 bg-slate-50 rounded-md">
                    <p className="text-slate-600">No passkeys yet. Add your first!</p>
                </div>
            )}
        </div>
    );
};

export default WebAuthnRegister;
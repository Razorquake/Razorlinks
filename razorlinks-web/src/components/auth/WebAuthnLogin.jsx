import React, { useState, useEffect } from 'react';
import axios from 'axios';

const WebAuthnLogin = ({ onSuccess }) => {
    const [email, setEmail] = useState('');
    const [isLoading, setIsLoading] = useState(false);
    const [error, setError] = useState('');
    const [hasPasskey, setHasPasskey] = useState(false);
    const [isSupported, setIsSupported] = useState(true);

    useEffect(() => {
        if (!window.PublicKeyCredential) {
            setIsSupported(false);
            setError('Passkeys not supported in this browser');
        }
    }, []);

    useEffect(() => {
        const checkPasskeys = async () => {
            if (email && email.includes('@')) {
                try {
                    const response = await axios.get(
                        `${import.meta.env.VITE_BACKEND_URL}/api/auth/public/webauthn/has-credentials?email=${email}`
                    );
                    setHasPasskey(response.data.hasCredentials);
                } catch (err) {
                    setHasPasskey(false);
                }
            }
        };
        const debounce = setTimeout(checkPasskeys, 500);
        return () => clearTimeout(debounce);
    }, [email]);

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

    const handlePasskeyLogin = async (e) => {
        e.preventDefault();

        if (!email) {
            setError('Please enter your email address');
            return;
        }

        setIsLoading(true);
        setError('');

        try {
            const optionsResponse = await axios.post(
                `${import.meta.env.VITE_BACKEND_URL}/api/auth/public/webauthn/authenticate/options`,
                { email }
            );

            const options = optionsResponse.data;
            const challenge = base64urlToUint8Array(options.challenge);

            const credential = await navigator.credentials.get({
                publicKey: {
                    ...options,
                    challenge,
                    allowCredentials: options.allowCredentials.map(cred => ({
                        ...cred,
                        id: base64urlToUint8Array(cred.id),
                        transports: cred.transports || []
                    }))
                }
            });

            if (!credential) {
                throw new Error('No credential received');
            }

            const credentialForServer = {
                id: credential.id,
                rawId: uint8ArrayToBase64url(new Uint8Array(credential.rawId)),
                response: {
                    clientDataJSON: uint8ArrayToBase64url(new Uint8Array(credential.response.clientDataJSON)),
                    authenticatorData: uint8ArrayToBase64url(new Uint8Array(credential.response.authenticatorData)),
                    signature: uint8ArrayToBase64url(new Uint8Array(credential.response.signature)),
                    userHandle: credential.response.userHandle
                        ? uint8ArrayToBase64url(new Uint8Array(credential.response.userHandle))
                        : null
                },
                type: credential.type
            };

            const verifyResponse = await axios.post(
                `${import.meta.env.VITE_BACKEND_URL}/api/auth/public/webauthn/authenticate/verify`,
                { email, credential: credentialForServer }
            );

            const { token } = verifyResponse.data;

            if (onSuccess) {
                onSuccess({ token, email });
            }

        } catch (err) {
            console.error('Passkey login error:', err);

            if (err.name === 'NotAllowedError') {
                setError('Authentication cancelled or timed out');
            } else if (err.name === 'InvalidStateError') {
                setError('Passkey not available');
            } else if (err.response?.data?.error) {
                setError(err.response.data.error);
            } else {
                setError('Failed to sign in with passkey');
            }
        } finally {
            setIsLoading(false);
        }
    };

    if (!isSupported) {
        return (
            <div className="text-center py-4">
                <p className="text-red-600">{error}</p>
                <p className="text-sm text-slate-600 mt-2">
                    Please use Chrome, Safari, Edge, or Firefox
                </p>
            </div>
        );
    }

    return (
        <div className="space-y-4">
            <div className="text-center">
                <h2 className="text-xl font-bold text-slate-800">Sign in with Passkey</h2>
                <p className="text-sm text-slate-600">Use your fingerprint, face, or device PIN</p>
            </div>

            <form onSubmit={handlePasskeyLogin} className="space-y-3">
                <div>
                    <label htmlFor="email" className="block text-sm font-semibold text-slate-700 mb-1">
                        Email Address
                    </label>
                    <input
                        id="email"
                        type="email"
                        placeholder="your.email@example.com"
                        value={email}
                        onChange={(e) => setEmail(e.target.value)}
                        disabled={isLoading}
                        required
                        className="w-full px-3 py-2 border border-slate-600 rounded-md outline-none"
                    />
                    {hasPasskey && (
                        <span className="text-xs text-green-600 mt-1 block">✓ Passkey available</span>
                    )}
                </div>

                {error && (
                    <div className="bg-red-50 border border-red-200 text-red-600 px-3 py-2 rounded-md text-sm">
                        {error}
                    </div>
                )}

                <button
                    type="submit"
                    className="w-full bg-custom-gradient text-white py-2 rounded-md font-semibold hover:opacity-90 disabled:opacity-50"
                    disabled={isLoading || !email}
                >
                    {isLoading ? 'Authenticating...' : '🔑 Sign in with Passkey'}
                </button>
            </form>
        </div>
    );
};

export default WebAuthnLogin;
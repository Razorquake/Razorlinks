// src/hooks/useEmailVerification.js
import { useState } from 'react';
import api from '../services/api.js';
import toast from 'react-hot-toast';

export const useEmailVerification = () => {
    const [resendingEmail, setResendingEmail] = useState(false);

    const resendVerificationEmail = async (username) => {
        if (!username) {
            toast.error('Username is required to resend verification email');
            return false;
        }

        setResendingEmail(true);
        try {
            const { data: response } = await api.post(
                "/auth/public/resend-verification",
                { username }
            );
            toast.success(response.message);
            return true;
        } catch (error) {
            console.error('Resend verification error:', error);
            if (error.response?.data?.message) {
                toast.error(error.response.data.message);
            } else {
                toast.error("Failed to resend verification email. Please try again.");
            }
            return false;
        } finally {
            setResendingEmail(false);
        }
    };

    return {
        resendVerificationEmail,
        resendingEmail
    };
};
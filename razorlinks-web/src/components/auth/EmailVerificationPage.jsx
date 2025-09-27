import React, {useEffect, useState} from 'react';
import {useNavigate, useSearchParams, Link} from "react-router-dom";
import {useStoreContext} from "../../store/ContextApi.jsx";
import toast from "react-hot-toast";
import {jwtDecode} from "jwt-decode";
import api from "../../services/api.js";

const EmailVerificationPage = () => {
    const [searchParams] = useSearchParams();
    const navigate = useNavigate();
    const { setToken } = useStoreContext();
    const [verificationStatus, setVerificationStatus] = useState("verifying"); // verifying, success, error
    const [message, setMessage] = useState("");
    const [loader, setLoader] = useState(true);

    useEffect(() => {
        const token = searchParams.get("token");

        if (!token) {
            setVerificationStatus("error");
            setMessage("Invalid verification link. No token found.");
            setLoader(false);
            return;
        }

        verifyEmail(token);
    }, [searchParams]);

    const verifyEmail = async (token) => {
        try {
            const { data: response } = await api.get(
                `/auth/public/verify-email?token=${token}`
            );

            if (response.status && response.token) {
                // Decode token to get user info
                const decodedToken = jwtDecode(response.token);
                const user = {
                    username: decodedToken.sub,
                    roles: decodedToken.roles ? decodedToken.roles.split(",") : [],
                };

                // Store token and user info
                localStorage.setItem("JWT_TOKEN", response.token);
                localStorage.setItem("USER", JSON.stringify(user));
                setToken(response.token);

                setVerificationStatus("success");
                setMessage(response.message);
                toast.success("Email verified successfully! You are now logged in.");

                // Redirect to dashboard after 3 seconds
                setTimeout(() => {
                    navigate("/dashboard");
                }, 3000);
            }
        } catch (error) {
            console.log(error);
            setVerificationStatus("error");

            if (error.response?.data?.message) {
                setMessage(error.response.data.message);
                toast.error(error.response.data.message);
            } else {
                setMessage("Email verification failed. Please try again.");
                toast.error("Email verification failed. Please try again.");
            }
        } finally {
            setLoader(false);
        }
    };

    const renderVerificationContent = () => {
        if (loader) {
            return (
                <div className="text-center">
                    <div className="w-16 h-16 mx-auto mb-4 border-4 border-blue-200 border-t-blue-600 rounded-full animate-spin"></div>
                    <h2 className="text-xl font-semibold text-gray-700 mb-2">Verifying your email...</h2>
                    <p className="text-gray-500">Please wait while we verify your account.</p>
                </div>
            );
        }

        if (verificationStatus === "success") {
            return (
                <div className="text-center">
                    <div className="w-20 h-20 mx-auto mb-4 bg-green-100 rounded-full flex items-center justify-center">
                        <svg className="w-10 h-10 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"></path>
                        </svg>
                    </div>
                    <h2 className="text-2xl font-bold text-green-600 mb-2">Email Verified Successfully!</h2>
                    <p className="text-gray-600 mb-6">{message}</p>
                    <div className="p-4 bg-green-50 rounded-md mb-6">
                        <p className="text-sm text-green-800">
                            You are now logged in and will be redirected to your dashboard in a few seconds.
                        </p>
                    </div>
                    <Link
                        to="/dashboard"
                        className="inline-block bg-custom-gradient text-white py-2 px-6 rounded-md hover:opacity-90 transition-opacity duration-200"
                    >
                        Go to Dashboard
                    </Link>
                </div>
            );
        }

        if (verificationStatus === "error") {
            return (
                <div className="text-center">
                    <div className="w-20 h-20 mx-auto mb-4 bg-red-100 rounded-full flex items-center justify-center">
                        <svg className="w-10 h-10 text-red-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"></path>
                        </svg>
                    </div>
                    <h2 className="text-2xl font-bold text-red-600 mb-2">Verification Failed</h2>
                    <p className="text-gray-600 mb-6">{message}</p>
                    <div className="space-y-3">
                        <Link
                            to="/register"
                            className="block w-full bg-custom-gradient text-white py-2 px-4 rounded-md hover:opacity-90 transition-opacity duration-200"
                        >
                            Register Again
                        </Link>
                        <Link
                            to="/login"
                            className="block w-full border border-gray-300 text-gray-700 py-2 px-4 rounded-md hover:bg-gray-50 transition-colors duration-200"
                        >
                            Back to Login
                        </Link>
                    </div>
                </div>
            );
        }
    };

    return (
        <div className="min-h-[calc(100vh-64px)] flex justify-center items-center">
            <div className="sm:w-[500px] w-[360px] shadow-custom py-8 sm:px-8 px-4 rounded-md">
                {renderVerificationContent()}
            </div>
        </div>
    );
};

export default EmailVerificationPage;
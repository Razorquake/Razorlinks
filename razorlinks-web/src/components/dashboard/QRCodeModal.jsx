import React, {useEffect, useState} from 'react';
import toast from "react-hot-toast";
import {Modal} from "@mui/material";
import {MdDownload} from "react-icons/md";
import {RxCross2} from "react-icons/rx";
import api from "../../services/api.js";

const QrCodeModal = ({ open, onClose, shortUrl }) => {
    const [isLoading, setIsLoading] = useState(true);
    const [qrCodeBlobUrl, setQrCodeBlobUrl] = useState(null);

    // Fetch QR code with authentication
    useEffect(() => {
        if (open && shortUrl) {
            fetchQRCode();
        }

        // Cleanup blob URL when modal closes
        return () => {
            if (qrCodeBlobUrl) {
                URL.revokeObjectURL(qrCodeBlobUrl);
            }
        };
    }, [open, shortUrl]);

    const fetchQRCode = async () => {
        setIsLoading(true);
        try {
            const response = await api.get(`/urls/qr/${shortUrl}?size=400`, {
                responseType: 'blob'
            });

            const blobUrl = URL.createObjectURL(response.data);
            setQrCodeBlobUrl(blobUrl);
            setIsLoading(false);
        } catch (error) {
            console.error('Error fetching QR code:', error);
            toast.error("Failed to load QR code");
            setIsLoading(false);
        }
    };

    const handleDownload = async () => {
        try {
            const response = await api.get(`/urls/qr/${shortUrl}?size=400`, {
                responseType: 'blob'
            });
            const url = window.URL.createObjectURL(response.data);
            const link = document.createElement('a');
            link.href = url;
            link.download = `qr-code-${shortUrl}.png`;
            document.body.appendChild(link);
            link.click();
            document.body.removeChild(link);
            window.URL.revokeObjectURL(url);
            toast.success("QR Code downloaded successfully!");
        }  catch (error) {
            console.error('Error downloading QR code:', error);
            toast.error("Failed to download QR code");
        }
    };

    const handleShare = async () => {
        const fullUrl = `${import.meta.env.VITE_REACT_SUBDOMAIN}/${shortUrl}`;

        if (navigator.share) {
            try {
                await navigator.share({
                    title: 'Short URL',
                    text: 'Check out this link!',
                    url: fullUrl,
                });
                toast.success("Shared successfully!");
            } catch (error) {
                if (error.name !== 'AbortError') {
                    console.error('Error sharing:', error);
                }
            }
        } else {
            // Fallback: copy to clipboard
            try {
                await navigator.clipboard.writeText(fullUrl);
                toast.success("Link copied to clipboard!");
            } catch (error) {
                console.error('Error copying to clipboard:', error);
                toast.error("Failed to copy link");
            }
        }
    };

    return (
        <Modal
            open={open}
            onClose={onClose}
            aria-labelledby="qr-code-modal"
        >
            <div className="flex justify-center items-center h-full w-full">
                <div className="bg-white p-6 rounded-lg shadow-lg max-w-md w-full mx-4 relative">
                    <button
                        onClick={onClose}
                        className="absolute right-4 top-4 text-slate-600 hover:text-slate-800"
                    >
                        <RxCross2 className="text-2xl" />
                    </button>

                    <h2 className="text-2xl font-bold mb-6 text-slate-800 text-center">
                        QR Code
                    </h2>

                    <div className="flex flex-col items-center">
                        <div className="bg-white p-4 rounded-lg shadow-md mb-4 relative">
                            {isLoading && (
                                <div className="absolute inset-0 flex items-center justify-center bg-gray-100 rounded-lg">
                                    <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
                                </div>
                            )}
                            <img
                                src={qrCodeBlobUrl}
                                alt="QR Code"
                                className="w-80 h-80"
                                onLoad={() => setIsLoading(false)}
                                onError={() => {
                                    setIsLoading(false);
                                    toast.error("Failed to load QR code");
                                }}
                            />
                        </div>

                        <div className="text-center mb-4">
                            <p className="text-sm text-slate-600 mb-1">Scan to visit:</p>
                            <p className="text-sm font-mono text-blue-600 break-all px-4">
                                ${import.meta.env.VITE_REACT_SUBDOMAIN}/${shortUrl}
                            </p>
                        </div>

                        <div className="flex gap-3 w-full">
                            <button
                                onClick={handleDownload}
                                className="flex-1 flex items-center justify-center gap-2 bg-blue-600 text-white py-2 px-4 rounded-md hover:bg-blue-700 transition-colors"
                            >
                                <MdDownload className="text-xl" />
                                Download
                            </button>
                            <button
                                onClick={handleShare}
                                className="flex-1 bg-green-600 text-white py-2 px-4 rounded-md hover:bg-green-700 transition-colors"
                            >
                                Share
                            </button>
                        </div>
                    </div>
                </div>
            </div>
        </Modal>
    );
};

export default QrCodeModal;
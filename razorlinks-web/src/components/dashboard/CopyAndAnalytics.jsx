import {useState} from 'react';
import {IoCopy, IoQrCode} from 'react-icons/io5';
import {LiaCheckSolid} from 'react-icons/lia';
import {MdAnalytics, MdDelete} from 'react-icons/md';
import ConfirmationModal from "./ConfirmationModal.jsx";
import QRCodeModal from "./QRCodeModal.jsx";

const CopyAndAnalytics = ({shortUrl, analyticsHandler, onDelete}) => {
    const [isCopied, setIsCopied] = useState(false);
    const [isDeleting, setIsDeleting] = useState(false);
    const [showDeleteModal, setShowDeleteModal] = useState(false);
    const [showQRModal, setShowQRModal] = useState(false);

    const handleCopyToClipboard = async () => {
        try {
            await navigator.clipboard.writeText(
                `${import.meta.env.VITE_REACT_SUBDOMAIN}/${shortUrl}`
            );
            setIsCopied(true);
            setTimeout(() => setIsCopied(false), 2000); // Reset after 2 seconds
        } catch (err) {
            console.error('Failed to copy text: ', err);
            // Handle error (e.g., show a message to the user)
        }
    };

    const handleDeleteClick = () => {
        setShowDeleteModal(true);
    };

    const handleDelete = async () => {
        setIsDeleting(true);
        try {
            await onDelete(shortUrl);
            setShowDeleteModal(false)
        } finally {
            setIsDeleting(false);
        }
    };

    const handleDeleteCancel = () => {
        if (!isDeleting) {
            setShowDeleteModal(false);
        }
    };

    const handleQRClick = () => {
        setShowQRModal(true);
    };

    return (
        <>
            <div className="flex flex-1 sm:justify-end items-center gap-4">
                <div className="flex gap-4 flex-col">
                    <div
                        onClick={handleCopyToClipboard}
                        className="flex cursor-pointer gap-1 items-center bg-btn-color py-2 font-semibold shadow-md shadow-slate-500 px-6 rounded-md text-white "
                    >
                        <button>{isCopied ? 'Copied' : 'Copy'}</button>
                        {isCopied ? <LiaCheckSolid className="text-md"/> : <IoCopy className="text-md"/>}
                    </div>

                    <div
                        onClick={handleDeleteClick}
                        className={`flex cursor-pointer gap-1 items-center bg-rose-700 py-2 font-semibold shadow-md shadow-slate-500 px-6 rounded-md text-white ${isDeleting ? 'opacity-50' : ''}`}
                    >
                        <button disabled={isDeleting}>
                            {isDeleting ? 'Deleting...' : 'Delete'}
                        </button>
                        <MdDelete className="text-md"/>
                    </div>

                </div>
                <div className="flex gap-4 flex-col">
                    <div
                        onClick={handleQRClick}
                        className="flex cursor-pointer gap-1 items-center bg-green-700 py-2 font-semibold shadow-md shadow-slate-500 px-6 rounded-md text-white "
                    >
                        <button>QR Code</button>
                        <IoQrCode className="text-md"/>
                    </div>


                    <div
                        onClick={() => analyticsHandler(shortUrl)}
                        className="flex cursor-pointer gap-1 items-center bg-yellow-700 py-2 font-semibold shadow-md shadow-slate-500 px-6 rounded-md text-white"
                    >
                        <button>Analytics</button>
                        <MdAnalytics className="text-md"/>
                    </div>

                </div>
            </div>
            <ConfirmationModal
                open={showDeleteModal}
                onClose={handleDeleteCancel}
                onConfirm={handleDelete}
                title="Delete Short URL"
                message="Are you sure you want to delete this short URL? This action cannot be undone."
                isLoading={isDeleting}
            />
            <QRCodeModal
                open={showQRModal}
                onClose={() => setShowQRModal(false)}
                shortUrl={shortUrl}
            />
        </>
    );
};

export default CopyAndAnalytics;
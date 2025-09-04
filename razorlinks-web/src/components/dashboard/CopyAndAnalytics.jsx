import { useState } from 'react';
import { IoCopy } from 'react-icons/io5';
import { LiaCheckSolid } from 'react-icons/lia';
import { MdAnalytics } from 'react-icons/md';

const CopyAndAnalytics = ({ shortUrl, analyticsHandler }) => {
    const [isCopied, setIsCopied] = useState(false);

    const handleCopyToClipboard = async () => {
        try {
            await navigator.clipboard.writeText(
                `${import.meta.env.VITE_REACT_FRONT_END_URL}/s/${shortUrl}`
            );
            setIsCopied(true);
            setTimeout(() => setIsCopied(false), 2000); // Reset after 2 seconds
        } catch (err) {
            console.error('Failed to copy text: ', err);
            // Handle error (e.g., show a message to the user)
        }
    };

    return (
        <div className="flex flex-1 sm:justify-end items-center gap-4">
            <div
                onClick={handleCopyToClipboard}
                className="flex cursor-pointer gap-1 items-center bg-btn-color py-2 font-semibold shadow-md shadow-slate-500 px-6 rounded-md text-white "
            >
                <button>{isCopied ? 'Copied' : 'Copy'}</button>
                {isCopied ? <LiaCheckSolid className="text-md" /> : <IoCopy className="text-md" />}
            </div>

            <div
                onClick={() => analyticsHandler(shortUrl)}
                className="flex cursor-pointer gap-1 items-center bg-rose-700 py-2 font-semibold shadow-md shadow-slate-500 px-6 rounded-md text-white "
            >
                <button>Analytics</button>
                <MdAnalytics className="text-md" />
            </div>
        </div>
    );
};

export default CopyAndAnalytics;
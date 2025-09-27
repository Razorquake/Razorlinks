import { Modal } from "@mui/material";

const ConfirmationModal = ({ open, onClose, onConfirm, title, message, isLoading }) => {
    return (
        <Modal
            open={open}
            onClose={isLoading ? undefined : onClose}
            aria-labelledby="confirmation-modal-title"
            aria-describedby="confirmation-modal-description"
        >
            <div className="flex justify-center items-center h-full w-full">
                <div className="bg-white p-6 rounded-lg shadow-lg max-w-md w-full mx-4">
                    <h2 className="text-xl font-bold mb-4 text-slate-800">{title}</h2>
                    <p className="text-slate-600 mb-6">{message}</p>
                    <div className="flex gap-3 justify-end">
                        <button
                            onClick={onClose}
                            disabled={isLoading}
                            className="px-4 py-2 bg-gray-300 text-gray-700 rounded-md hover:bg-gray-400 disabled:opacity-50"
                        >
                            Cancel
                        </button>
                        <button
                            onClick={onConfirm}
                            disabled={isLoading}
                            className="px-4 py-2 bg-red-600 text-white rounded-md hover:bg-red-700 disabled:opacity-50"
                        >
                            {isLoading ? 'Deleting...' : 'Delete'}
                        </button>
                    </div>
                </div>
            </div>
        </Modal>
    );
};

export default ConfirmationModal;
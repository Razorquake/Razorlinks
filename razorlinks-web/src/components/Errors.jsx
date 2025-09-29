import {useNavigate} from "react-router-dom";
import {FiAlertCircle} from "react-icons/fi";

const Errors = ({ message }) => {
    const navigate = useNavigate();
    const onBackHandler = () => {
        navigate(-1);
    };
    return (
        <div className="flex flex-col items-center justify-center min-h-[calc(100vh-74px)]  p-4">
            <div className="text-center">
                <div className="flex justify-center">
                    <FiAlertCircle className="text-red-500 mb-4" size={48} />
                </div>
                <h2 className="text-2xl font-bold text-gray-800 mb-4">
                    Oops! Something went wrong.
                </h2>
                <p className="text-gray-600 mb-6 font-semibold">{message}</p>
                <div className="flex justify-center">
                    <button
                        onClick={onBackHandler}
                        className="flex items-center px-4 py-2 bg-btn-color text-white rounded  focus:outline-none focus:ring-2 focus:ring-blue-300"
                    >
                        Go Back
                    </button>
                </div>
            </div>
        </div>
    );
};

export default Errors;

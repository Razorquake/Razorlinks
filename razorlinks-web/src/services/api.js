import axios from "axios";
import toast from "react-hot-toast";

// export default axios.create({
//     baseURL: import.meta.env.VITE_BACKEND_URL,
//
// })

//Create an axios instance with the base URL and headers
const api = axios.create({
    baseURL: `${import.meta.env.VITE_BACKEND_URL}/api`,
    headers: {
        "Content-Type": "application/json",
        Accept: "application/json",
    },
    withCredentials: true,
    });

// Add a request interceptor to include JWT and CSRF tokens
api.interceptors.request.use(
    async (config) => {
        const token = localStorage.getItem("JWT_TOKEN");
        if (token) {
            config.headers.Authorization = `Bearer ${token}`;
        }
        return config;
    },
    (error) => {
        return Promise.reject(error);
    }
);

api.interceptors.response.use(
    (response) => response,
    (error) => {
        if (error.response && error.response.status === 401) {
            localStorage.removeItem("JWT_TOKEN");
            localStorage.removeItem("USER");
            localStorage.removeItem("IS_ADMIN");
            window.location.href = "/login";
            toast.error("Session expired. Please log in again.");
        }
        return Promise.reject(error);
    }
);

export default api;
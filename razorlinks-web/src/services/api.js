import axios from "axios";
import toast from "react-hot-toast";
import {jwtDecode} from "jwt-decode";

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

// Helper function to check if the token is expired
const isTokenExpired = (token) => {
    if (!token) return true;

    try {
        const decoded = jwtDecode(token);
        const currentTime = Date.now() / 1000;

        // Check if the token will expire in the next 5 minutes (300 seconds)
        // This gives you time to refresh or warn the user
        return decoded.exp < currentTime + 300;
    } catch (error) {
        console.error("Error decoding token:", error);
        return true;
    }
};

// Helper function to log out user
const logout = () => {
    localStorage.removeItem("JWT_TOKEN");
    localStorage.removeItem("USER");
    localStorage.removeItem("IS_ADMIN");
    window.location.href = "/login";
};


// Add a request interceptor to include JWT and CSRF tokens
api.interceptors.request.use(
    async (config) => {
        const token = localStorage.getItem("JWT_TOKEN");
        if (token) {
            // Check if the token is expired before making request
            if (isTokenExpired(token)) {
                logout();
                toast.error("Session expired. Please log in again.");
                return Promise.reject(new Error("Token expired"));
            }
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
            logout();
            toast.error("Session expired. Please log in again.");
        }
        return Promise.reject(error);
    }
);

export default api;
import axios from "axios";

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

        let csrfToken = localStorage.getItem("CSRF_TOKEN");
        if (!csrfToken) {
            try {
                const response = await axios.get(
                    `${import.meta.env.VITE_BACKEND_URL}/api/csrf-token`,
                    { withCredentials: true }
                );
                csrfToken = response.data.token;
                localStorage.setItem("CSRF_TOKEN", csrfToken);
            } catch (error) {
                console.error("Failed to fetch CSRF token", error);
            }
        }

        if (csrfToken) {
            config.headers["X-XSRF-TOKEN"] = csrfToken;
        }
        console.log("X-XSRF-TOKEN " + csrfToken);
        return config;
    },
    (error) => {
        return Promise.reject(error);
    }
);

export default api;
import {createContext, useContext, useEffect, useState} from "react";
import api from "../services/api.js";
import toast from "react-hot-toast";

const ContextApi = createContext();

export const ContextApiProvider = ({ children }) => {
    //find the token in the localstorage
    const getToken = localStorage.getItem("JWT_TOKEN")
        ? JSON.stringify(localStorage.getItem("JWT_TOKEN"))
        : null;
    //find is the user status from the localstorage
    const isADmin = localStorage.getItem("IS_ADMIN")
        ? JSON.stringify(localStorage.getItem("IS_ADMIN"))
        : false;

    //store the token
    const [token, setToken] = useState(getToken);

    //store the current loggedIn user
    const [currentUser, setCurrentUser] = useState(null);
    //handle sidebar opening and closing in the admin panel
    const [openSidebar, setOpenSidebar] = useState(true);
    //check the loggedin user is admin or not
    const [isAdmin, setIsAdmin] = useState(isADmin);
    const fetchUser = async () => {
        const user = JSON.parse(localStorage.getItem("USER"));

        if (user?.username) {
            try {
                const { data } = await api.get(`/auth/user`);
                const roles = data.roles;

                if (roles.includes("ROLE_ADMIN")) {
                    localStorage.setItem("IS_ADMIN", JSON.stringify(true));
                    setIsAdmin(true);
                } else {
                    localStorage.removeItem("IS_ADMIN");
                    setIsAdmin(false);
                }
                setCurrentUser(data);
            } catch (error) {
                console.error("Error fetching current user", error);
                toast.error("Error fetching current user");
            }
        }
    };

    useEffect(() => {
        if (token) {
            fetchUser();
        }
    }, [token]);
    
    return <ContextApi.Provider value={{
        token,
        setToken,
        currentUser,
        setCurrentUser,
        openSidebar,
        setOpenSidebar,
        isAdmin,
        setIsAdmin,
    }}>{children}</ContextApi.Provider>
};

export const useStoreContext = () => {
    return useContext(ContextApi);
}
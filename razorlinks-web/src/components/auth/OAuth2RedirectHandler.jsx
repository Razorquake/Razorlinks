import React, {useEffect} from 'react';
import {useLocation, useNavigate} from "react-router-dom";
import {useStoreContext} from "../../store/ContextApi.jsx";
import {jwtDecode} from "jwt-decode";

const OAuth2RedirectHandler = () => {
    const navigate = useNavigate();
    const location = useLocation();
    const { setToken, setIsAdmin } = useStoreContext();

    useEffect(() => {
        const params = new URLSearchParams(location.search);
        const token = params.get('token');
        console.log("OAuth2RedirectHandler: Params:", params.toString());
        console.log("OAuth2RedirectHandler: Token:", token);

        if (token) {
            try {
                const decodedToken = jwtDecode(token);

                localStorage.setItem('JWT_TOKEN', token);

                const user = {
                    username: decodedToken.sub,
                    roles: decodedToken.roles.split(','),
                };

                localStorage.setItem('USER', JSON.stringify(user));

                // Update context state
                setToken(token);
                setIsAdmin(user.roles.includes('ADMIN'));

                // Delay navigation to ensure local storage operations complete
                setTimeout(() => {
                    console.log("Navigating to /dashboard after 100ms delay");
                    navigate('/dashboard');
                }, 100); // 100ms delay
            } catch (error) {
                console.error('Token decoding failed:', error);
                navigate('/login');
            }
        } else {
            console.log("Token not found in URL, redirecting to login");
            navigate('/login');
        }
    }, [location, navigate, setToken, setIsAdmin]);

    return (
        <div className="flex items-center justify-center min-h-[calc(100vh-74px)]">
            Redirecting...
        </div>
    );
};

export default OAuth2RedirectHandler;
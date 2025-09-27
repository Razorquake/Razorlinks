import {useStoreContext} from "./store/ContextApi.jsx";
import {Navigate} from "react-router-dom";

export default function PrivateRoute({children , publicPage, adminPage}) {
    const {token, isAdmin} = useStoreContext();
    if (publicPage) {
        return token ? <Navigate to="/dashboard" /> : children;
    }
    //navigate to the access-denied page if a user tries to access the admin page
    if (token && adminPage && !isAdmin) {
        return <Navigate to="/access-denied" />;
    }
    return !token ? <Navigate to="/login" /> : children;
}
import {useParams} from "react-router-dom";
import {useEffect} from "react";

const ShortenUrlPage = () => {
    const { url } = useParams();

    useEffect(() => {
        if (url) {
            window.location.href = import.meta.env.VITE_BACKEND_URL + `/${url}`;
        }
    }, [url]);
    return <p>Redirecting...</p>;
}

export default ShortenUrlPage;
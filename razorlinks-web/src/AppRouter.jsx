import {Toaster} from "react-hot-toast";
import {BrowserRouter as Router, Route, Routes} from "react-router-dom";
import LandingPage from "./components/LandingPage.jsx";
import AboutPage from "./components/AboutPage.jsx";
import RegisterPage from "./components/RegisterPage.jsx";
import LoginPage from "./components/LoginPage.jsx";
import DashboardLayout from "./components/dashboard/DashboardLayout.jsx";
import Footer from "./components/Footer.jsx";
import ShortenUrlPage from "./components/ShortenUrlPage.jsx";
import Navbar from "./components/NavBar.jsx";
import PrivateRoute from "./PrivateRoute.jsx";
import ErrorPage from "./components/ErrorPage.jsx";

const AppRouter = () => {
    const hideHeaderFooter = location.pathname.startsWith("/s");
    return (
        <>
            {!hideHeaderFooter && <Navbar/>}
            <Toaster position='bottom-center'/>
            <Routes>
                <Route path='/' element={<LandingPage/>}/>
                <Route path='/about' element={<AboutPage/>}/>
                <Route path="/s/:url" element={<ShortenUrlPage />} />
                <Route path="/register" element={<PrivateRoute publicPage={true}><RegisterPage /></PrivateRoute>} />
                <Route path="/login" element={<PrivateRoute publicPage={true}><LoginPage /></PrivateRoute>} />
                <Route path="/dashboard" element={ <PrivateRoute publicPage={false}><DashboardLayout /></PrivateRoute>} />
                <Route path="/error" element={ <ErrorPage />} />
                <Route path="*" element={<ErrorPage/>}/>
            </Routes>
            {!hideHeaderFooter && <Footer/>}
        </>
    );
}

export default AppRouter;

export const SubDomainRouter = () => {
    return (
        <Routes>
            <Route path="/:url" element={<ShortenUrlPage/>}/>
        </Routes>
    )
}
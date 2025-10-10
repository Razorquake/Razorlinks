import {Toaster} from "react-hot-toast";
import {Route, Routes} from "react-router-dom";
import LandingPage from "./components/LandingPage.jsx";
import AboutPage from "./components/AboutPage.jsx";
import RegisterPage from "./components/auth/RegisterPage.jsx";
import LoginPage from "./components/auth/LoginPage.jsx";
import DashboardLayout from "./components/dashboard/DashboardLayout.jsx";
import Footer from "./components/Footer.jsx";
import ShortenUrlPage from "./components/ShortenUrlPage.jsx";
import Navbar from "./components/NavBar.jsx";
import PrivateRoute from "./PrivateRoute.jsx";
import ErrorPage from "./components/ErrorPage.jsx";
import UserProfile from "./components/auth/UserProfile.jsx";
import Admin from "./components/auditlogs/Admin.jsx";
import AccessDenied from "./components/auth/AccessDenied.jsx";
import NotFound from "./components/NotFound.jsx";
import EmailVerificationPage from "./components/auth/EmailVerificationPage.jsx";
import ForgotPassword from "./components/auth/ForgotPassword.jsx";
import ResetPassword from "./components/auth/ResetPassword.jsx";
import OAuth2RedirectHandler from "./components/auth/OAuth2RedirectHandler.jsx";

const AppRouter = () => {
    //const hideHeaderFooter = location.pathname.startsWith("/s");
    return (
        <>
            <Toaster position='bottom-center'/>
            <Routes>
                <Route path='/' element={<LandingPage/>}/>
                <Route path='/about' element={<AboutPage/>}/>
                {/*<Route path="/s/:url" element={<ShortenUrlPage />} />*/}
                <Route path="/register" element={<PrivateRoute publicPage={true}><RegisterPage /></PrivateRoute>} />
                <Route path="/login" element={<PrivateRoute publicPage={true}><LoginPage /></PrivateRoute>} />
                <Route path="/forgot-password" element={<PrivateRoute publicPage={true}><ForgotPassword/></PrivateRoute>} />
                <Route path="/reset-password" element={<PrivateRoute publicPage={true}><ResetPassword/></PrivateRoute> }/>
                <Route path="/verify-email" element={<PrivateRoute publicPage={true}><EmailVerificationPage /></PrivateRoute>} />
                <Route path="/oauth2/redirect" element={<PrivateRoute publicPage={true}><OAuth2RedirectHandler /></PrivateRoute>} />
                <Route path="/dashboard" element={ <PrivateRoute publicPage={false}><DashboardLayout /></PrivateRoute>} />
                <Route path="/profile" element={ <PrivateRoute publicPage={false}><UserProfile /></PrivateRoute>} />

                <Route path="/error" element={ <ErrorPage />} />
                <Route path="*" element={<NotFound/>}/>
                <Route path="/access-denied" element={<AccessDenied />} />
                <Route
                    path="/admin/*"
                    element={
                        <PrivateRoute adminPage={true}>
                            <Admin />
                        </PrivateRoute>
                    }
                />
            </Routes>
            {/*{!hideHeaderFooter && <Navbar/>}*/}
            {/*{!hideHeaderFooter && <Footer/>}*/}
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
import {useState} from "react";
import {useForm} from "react-hook-form";
import TextField from "../TextField.jsx";
import {Link } from "react-router-dom";
import api from "../../services/api.js";
import toast from "react-hot-toast";
import { useEmailVerification } from '../../hooks/useEmailVerification.js';
import Divider from "@mui/material/Divider";
import {FcGoogle} from "react-icons/fc";
import {FaGithub} from "react-icons/fa";

const RegisterPage = () => {
    const apiUrl = import.meta.env.VITE_BACKEND_URL;
    const [registrationComplete, setRegistrationComplete] = useState(false);
    const [loader, setLoader] = useState(false);
    const { resendVerificationEmail, resendingEmail } = useEmailVerification();
    const [userEmail, setUserEmail] = useState("");
    const [userUsername, setUserUsername] = useState(""); // Add this state

    const {
        register,
        handleSubmit,
        reset,
        formState: {errors},
    } = useForm({
        defaultValues: {
            username: "",
            email: "",
            password: "",
        },
        mode: "onTouched"
    });

    const registerHandler = async (data) => {
        setLoader(true);
        try {
            const { data: response} = await api.post(
                "/auth/public/register",
                data
            );
            // Show a success message
            toast.success(response.message);
            // Store email for resend functionality
            setUserEmail(data.email);
            setUserUsername(data.username); // Store username instead of email
            // Show verification message
            setRegistrationComplete(true);

            reset();
        } catch (error){
            console.log(error);
            if (error.response?.data?.message) {
                toast.error(error.response.data.message);
            } else {
                toast.error("Registration failed. Please try again.");
            }
        } finally {
            setLoader(false);
        }
    };

    if (registrationComplete) {
        return (
            <div className="min-h-[calc(100vh-64px)] flex justify-center items-center">
                <div className="sm:w-[500px] w-[360px] shadow-custom py-8 sm:px-8 px-4 rounded-md text-center">
                    <div className="mb-6">
                        <div className="w-20 h-20 mx-auto mb-4 bg-green-100 rounded-full flex items-center justify-center">
                            <svg className="w-10 h-10 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M3 8l7.89 4.26a2 2 0 002.22 0L21 8M5 19h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z"></path>
                            </svg>
                        </div>
                        <h1 className="text-2xl font-bold text-gray-800 mb-2">Check Your Email</h1>
                        <p className="text-gray-600 mb-4">
                            We've sent a verification email to:
                        </p>
                        <p className="font-semibold text-gray-800 mb-4">{userEmail}</p>
                        <p className="text-sm text-gray-500 mb-6">
                            Please click the verification link in your email to activate your account.
                            The link will expire in 24 hours.
                        </p>
                    </div>

                    <div className="space-y-3">
                        <button
                            onClick={()=>resendVerificationEmail(userUsername)}
                            disabled={resendingEmail}
                            className="w-full bg-custom-gradient text-white py-2 px-4 rounded-md hover:opacity-90 transition-opacity duration-200 disabled:opacity-50"
                        >
                            {resendingEmail ? "Resending..." : "Resend Verification Email"}
                        </button>

                        <Link
                            to="/login"
                            className="block w-full border border-gray-300 text-gray-700 py-2 px-4 rounded-md hover:bg-gray-50 transition-colors duration-200"
                        >
                            Back to Login
                        </Link>
                    </div>

                    <div className="mt-6 p-4 bg-blue-50 rounded-md">
                        <p className="text-sm text-blue-800">
                            <strong>Didn't receive the email?</strong><br/>
                            Check your spam folder or click "Resend Verification Email" above.
                        </p>
                    </div>
                </div>
            </div>
        );
    }

    return (
        <div
            className="min-h-[calc(100vh-64px)] flex justify-center items-center">
            <form
                onSubmit={handleSubmit(registerHandler)}
                className="sm:w-[450px] w-[360px] shadow-custom py-8 sm:px-8 px-4 rounded-md"
            >
                <h1 className="text-center font-montserrat font-bold lg:text-3xl text-2xl">
                    Register here
                </h1>
                <p className="text-slate-600 text-center">
                    Enter your credentials to create new account
                </p>
                <Divider className="font-semibold pb-4"></Divider>
                <div className="flex flex-col gap-3">
                    <TextField
                        label="Username"
                        required
                        id="username"
                        type="text"
                        placeholder="Type your username"
                        register={register}
                        errors={errors}
                    />
                    <TextField
                        label="Email"
                        required
                        id="email"
                        type="email"
                        message="*Email is required"
                        placeholder="Type your email"
                        register={register}
                        errors={errors}
                    />

                    <TextField
                        label="Password"
                        required
                        id="password"
                        type="password"
                        message="*Password is required"
                        placeholder="Type your password"
                        register={register}
                        min={6}
                        errors={errors}
                    />
                </div>
                <button
                    disabled={loader}
                    type='submit'
                    className='bg-customRed font-semibold text-white  bg-custom-gradient w-full py-2 hover:text-slate-400 transition-colors duration-100 rounded-sm my-3'>
                    {loader ? "Loading..." : "Register"}
                </button>
                <p className='text-center text-sm text-slate-700 mt-3 mb-3'>
                    Already have an account? {""}
                    <Link
                        className='font-semibold underline hover:text-black text-btn-color'
                        to="/login"
                    >
                        <span>Login</span>
                    </Link>
                </p>
                <Divider className="font-semibold">OR</Divider>
                <div className="flex items-center justify-between gap-1 py-5 ">
                    <Link
                        to={`${apiUrl}/oauth2/authorization/google`}
                        className="flex gap-1 items-center justify-center flex-1 border p-2 shadow-sm shadow-slate-200 rounded-md hover:bg-slate-300 transition-all duration-300"
                    >
                  <span>
                    <FcGoogle className="text-2xl"/>
                  </span>
                        <span className="font-semibold sm:text-customText text-xs">
                    Login with Google
                  </span>
                    </Link>
                    <Link
                        to={`${apiUrl}/oauth2/authorization/github`}
                        className="flex gap-1 items-center justify-center flex-1 border p-2 shadow-sm shadow-slate-200 rounded-md hover:bg-slate-300 transition-all duration-300"
                    >
                  <span>
                    <FaGithub className="text-2xl"/>
                  </span>
                        <span className="font-semibold sm:text-customText text-xs">
                    Login with Github
                  </span>
                    </Link>
                </div>
            </form>
        </div>
    );
}

export default RegisterPage;
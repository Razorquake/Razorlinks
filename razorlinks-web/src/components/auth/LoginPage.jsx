import {Link, useNavigate} from "react-router-dom";
import {Fragment, useState} from "react";
import {useForm} from "react-hook-form";
import {jwtDecode} from "jwt-decode";
import TextField from "../TextField.jsx";
import api from "../../services/api.js";
import toast from "react-hot-toast";
import {useStoreContext} from "../../store/ContextApi.jsx";
import Divider from "@mui/material/Divider";
import {useEmailVerification} from '../../hooks/useEmailVerification.js';
import {FcGoogle} from "react-icons/fc";
import {FaGithub} from "react-icons/fa";

const LoginPage = () => {
    // Step 1: Login method and Step 2: Verify 2FA
    const apiUrl = import.meta.env.VITE_BACKEND_URL;
    const {resendVerificationEmail, resendingEmail} = useEmailVerification();
    const [step, setStep] = useState(1);
    const [jwtToken, setJwtToken] = useState("");
    const navigate = useNavigate()
    const [loader, setLoader] = useState(false);
    const {setToken} = useStoreContext();

    const {
        register,
        handleSubmit,
        reset,
        formState: {errors}
    } = useForm({
        defaultValues: {
            username: "",
            email: "",
            password: "",
            code: "",
        }, mode: "onTouched",
    });

    const showResendVerificationToast = (username) => {
        toast((t) => (<div className="text-center">
            <p className="mb-2">Need to resend verification email?</p>
            <button
                onClick={async () => {
                    const success = await resendVerificationEmail(username);
                    toast.dismiss(t.id);
                    if (success) {
                        toast.success("Verification email sent! Please check your inbox.");
                    }
                }}
                disabled={resendingEmail}
                className="bg-blue-600 text-white px-3 py-1 rounded text-sm hover:bg-blue-700 disabled:opacity-50"
            >
                {resendingEmail ? "Sending..." : "Resend Email"}
            </button>
        </div>), {
            duration: 10000, id: 'resend-verification'
        })
    };

    const handleSuccessfulLogin = (token, decodedToken) => {
        const user = {
            username: decodedToken.sub, roles: decodedToken.roles ? decodedToken.roles.split(",") : [],
        };
        localStorage.setItem("JWT_TOKEN", token);
        localStorage.setItem("USER", JSON.stringify(user));

        //store the token on the context state  so that it can be shared any where in our application by context provider
        setToken(token);

        navigate("/dashboard");
    };

    const loginHandler = async (data) => {
        setLoader(true);
        try {
            const response = await api.post("/auth/public/login", data);
            //showing success message with React hot toast
            toast.success("Login Successful");
            if (response.status === 200 && response.data.token) {
                setJwtToken(response.data.token);
                const decodedToken = jwtDecode(response.data.token);
                console.log(decodedToken);
                if (decodedToken.is2faEnabled) {

                    setStep(2); // Move to 2FA verification step
                } else {
                    handleSuccessfulLogin(response.data.token, decodedToken);
                }
            } else {
                toast.error("Login failed. Please check your credentials and try again.");
            }
            //reset the input field by using reset() function provided by react hook form after submission
            reset();

        } catch (error) {
            console.log(error);
            if (error.response?.data?.message) {
                toast.error(error.response.data.message);
                // Check if it's an email verification error
                if (error.response.data.message.includes("verify your email")) {
                    const errorMessage = error.response.data.message;
                    toast.error(errorMessage);

                    // Check if it's an email verification error
                    if (errorMessage.includes("verify your email")) {
                        setTimeout(() => {
                            showResendVerificationToast(data.username);
                        }, 1000);
                    }
                }
            } else {
                toast.error("Login failed. Please try again.");
            }
        } finally {
            setLoader(false);
        }
    };

    //function for verify 2fa authentication
    const onVerify2FaHandler = async (data) => {
        const code = data.code;
        setLoader(true);

        try {
            const formData = new URLSearchParams();
            formData.append("code", code);
            formData.append("jwtToken", jwtToken);

            await api.post("/auth/public/verify-2fa-login", formData, {
                headers: {
                    "Content-Type": "application/x-www-form-urlencoded",
                },
            });

            const decodedToken = jwtDecode(jwtToken);
            handleSuccessfulLogin(jwtToken, decodedToken);
        } catch (error) {
            console.error("2FA verification error", error);
            toast.error("Invalid 2FA code. Please try again.");
        } finally {
            setLoader(false);
        }
    };

    return (<div className='min-h-[calc(100vh-64px)] flex justify-center items-center'>
        {step === 1 ? (<Fragment>

            <form
                onSubmit={handleSubmit(loginHandler)}
                className='sm:w-[450px] w-[360px] shadow-custom py-8 sm:px-8 px-4 rounded-md'
            >
                <h1 className="font-montserrat text-center font-bold lg:text-3xl text-2xl">
                    Login here
                </h1>
                <p className="text-slate-600 text-center">
                    Please Enter your username and password{" "}
                </p>

                <Divider className="font-semibold pb-4"></Divider>
                <div className='flex flex-col gap-3'>
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
                        label='Password'
                        required
                        id='password'
                        type='password'
                        message='*Password is required'
                        placeholder='Type your password'
                        register={register}
                        min={6}
                        errors={errors}
                    />
                </div>
                <button
                    disabled={loader}
                    type='text'
                    className='bg-customRed font-semibold text-white  bg-custom-gradient w-full py-2 hover:text-slate-400 transition-colors duration-100 rounded-sm my-3'>
                    {loader ? 'Loading...' : 'Login'}
                </button>
                <p className=" text-sm text-slate-700 ">
                    <Link
                        className=" underline hover:text-black"
                        to="/forgot-password"
                    >
                        Forgot Password?
                    </Link>
                </p>
                <p className='text-center text-sm text-slate-700 mt-3 mb-3'>
                    Don't have an account? {''}
                    <Link
                        className='font-semibold underline hover:text-black text-btn-color'
                        to="/register">
                        <span>SignUp</span>
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
        </Fragment>) : (<Fragment>
            <form
                onSubmit={handleSubmit(onVerify2FaHandler)}
                className="sm:w-[450px] w-[360px]  shadow-custom py-8 sm:px-8 px-4"
            >
                <div>
                    <h1 className="font-montserrat text-center font-bold text-2xl">
                        Verify 2FA
                    </h1>
                    <p className="text-slate-600 text-center">
                        Enter the correct code to complete 2FA Authentication
                    </p>

                    <Divider className="font-semibold pb-4"></Divider>
                </div>

                <div className="flex flex-col gap-2 mt-4">
                    <TextField
                        label="Enter Code"
                        required
                        id="code"
                        type="text"
                        message="*Code is required"
                        placeholder="Enter your 2FA code"
                        register={register}
                        errors={errors}
                    />
                </div>
                <button
                    disabled={loader}
                    onClick={() => {
                    }}
                    className="bg-custom-gradient font-semibold text-white w-full py-2 hover:text-slate-400 transition-colors duration-100 rounded-sm my-3"
                    type="text"
                >
                    {loader ? <span>Loading...</span> : "Verify 2FA"}
                </button>
            </form>
        </Fragment>)}

    </div>)
};

export default LoginPage;
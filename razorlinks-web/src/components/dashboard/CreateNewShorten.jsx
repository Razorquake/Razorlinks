import {useStoreContext} from "../../contextApi/ContextApi.jsx";
import {useState} from "react";
import {useForm} from "react-hook-form";
import api from "../../api/api.js";
import toast from "react-hot-toast";
import TextField from "../TextField.jsx";
import {Tooltip} from "@mui/material";
import {RxCross2} from "react-icons/rx";

const CreateNewShorten = ({ setOpen, refetch }) => {
    const { token } = useStoreContext();
    const [loading, setLoading] = useState(false);
    const {
        register,
        handleSubmit,
        reset,
        formState: { errors },
    } = useForm({
        defaultValues: {
            originalUrl: "",
        },
        mode: "onTouched"
    });

    const createShortenHandler = async (data) => {
        setLoading(true);
        try {
            const { data: response } = await api.post(
                "/api/urls/shorten",
                data,
                {
                    headers: {
                        "Content-Type": "application/json",
                        Accept: "application/json",
                        Authorization: `Bearer ${token}`,
                    }
                }
            );
            const shortUrl = `${import.meta.env.VITE_REACT_FRONT_END_URL + "/s/" + `${response.shortUrl}`}`;
            navigator.clipboard.writeText(shortUrl).then(()=> {
                toast.success("Shortened successfully & copied to clipboard!", {
                    position: "bottom-center",
                    className: "mb-5",
                    duration: 3000
                });
            });
            reset();
            setOpen(false);
        } catch (error) {
            toast.error("Error creating shorten"+error.message,);
        } finally {
            setLoading(false);
        }
    };
    return (
        <div className="flex justify-center items-center bg-white rounded-md">
            <form
                onSubmit={handleSubmit(createShortenHandler)}
                className="sm:w-[450px] w-[360px] relative  shadow-custom pt-8 pb-5 sm:px-8 px-4 rounded-lg"
            >

                <h1 className="font-montserrat sm:mt-0 mt-3 text-center  font-bold sm:text-2xl text-[22px] text-slate-800 ">
                    Create New Shorten Url
                </h1>

                <hr className="mt-2 sm:mb-5 mb-3 text-slate-950" />

                <div>
                    <TextField
                        label="Enter URL"
                        required
                        id="originalUrl"
                        placeholder="https://example.com"
                        type="url"
                        message="Url is required"
                        register={register}
                        errors={errors}
                    />
                </div>

                <button
                    className="bg-customRed font-semibold text-white w-32  bg-custom-gradient  py-2  transition-colors  rounded-md my-3"
                    type="text"
                >
                    {loading ? "Loading..." : "Create"}
                </button>

                {!loading && (
                    <Tooltip title="Close">
                        <button
                            disabled={loading}
                            onClick={() => setOpen(false)}
                            className=" absolute right-2 top-2  "
                        >
                            <RxCross2 className="text-slate-800   text-3xl" />
                        </button>
                    </Tooltip>
                )}

            </form>
        </div>
    );
};

export default CreateNewShorten;
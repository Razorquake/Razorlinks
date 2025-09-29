import {useStoreContext} from "../../store/ContextApi.jsx";
import {useLocation} from "react-router-dom";
import {FaArrowLeft, FaArrowRight, FaUser} from "react-icons/fa";
import {Tooltip} from "@mui/material";
import {LiaBlogSolid} from "react-icons/lia";
import {Link} from "react-router-dom";

const Sidebar = () => {
    // Access the openSidebar and setOpenSidebar function using the useMyContext hook from the ContextProvider
    const {openSidebar, setOpenSidebar} = useStoreContext();

    //access the current path
    const pathName = useLocation().pathname;

    return (
        <div
            className={`fixed p-2 top-[74px] min-h-[calc(100vh-74px)] max-h-[calc(100vh-74px)]  z-20  left-0 bg-black ${
                openSidebar ? "w-52" : "w-12"
            } transition-all duration-150  `}
        >
            <div className=" min-h-10  max-h-10 flex flex-end">
                {openSidebar ? (
                    <button
                        className="flex w-full text-white justify-end items-center gap-1"
                        onClick={() => setOpenSidebar(!openSidebar)}
                    >
                        <span>
                            <FaArrowLeft className="text-sm"/>
                        </span>
                        <span className="font-semibold">Close</span>
                    </button>
                ) : (
                    <Tooltip title="Click To Expand">
                        <button
                            className="flex w-full text-white justify-center items-center gap-1"
                            onClick={() => setOpenSidebar(!openSidebar)}
                        >
                            <span>
                                <FaArrowRight className="text-lg"/>
                            </span>
                        </button>
                    </Tooltip>
                )}
            </div>
            <div className="flex flex-col gap-5 mt-4">
                <Tooltip title={`${openSidebar ? "" : "All Users"}`}>
                    <Link
                        to="/admin/users"
                        className={`flex text-white items-center gap-2 ${
                            pathName.startsWith("/admin/users")
                                ? "bg-btn-color"
                                : "bg-transparent"
                        }   min-h-10 max-h-10 py-2 px-2 rounded-md hover:bg-btn-color`}
                    >
                        <span><FaUser/></span>
                        <span
                            className={` ${
                                !openSidebar ? "opacity-0" : ""
                            } transition-all font-semibold duration-150  ease-in-out`}
                        >All Users</span>
                    </Link>
                </Tooltip>
                <Tooltip title={`${openSidebar ? "" : "Audit Logs"}`}>
                    <Link
                        to="/admin/audit-logs"
                        className={`flex text-white items-center gap-2 ${
                            pathName.startsWith("/admin/audit-logs")
                                ? "bg-btn-color"
                                : "bg-transparent"
                        }   min-h-10 max-h-10 py-2 px-2 rounded-md hover:bg-btn-color`}
                    >
                        <span><LiaBlogSolid className="text-xl" /></span>
                        <span
                            className={` ${
                                !openSidebar ? "opacity-0" : ""
                            } transition-all font-semibold duration-150  ease-in-out`}
                        >Audit Logs</span>
                    </Link>
                </Tooltip>
            </div>
        </div>
    );

};

export default Sidebar;
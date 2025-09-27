import {FaExternalLinkAlt, FaRegCalendarAlt} from "react-icons/fa";
import {Link, useNavigate} from "react-router-dom";
import {MdOutlineAdsClick} from "react-icons/md";
import dayjs from "dayjs";
import CopyAndAnalytics from "./CopyAndAnalytics.jsx";
import {Fragment, useEffect, useState} from "react";
import Graph from "./Graph.jsx";
import api from "../../services/api.js";
import toast from "react-hot-toast";

const ShortenItem = ({originalUrl, shortUrl, clickCount, createDate, onUrlDeleted}) => {
    const navigate = useNavigate();
    const subDomain = import.meta.env.VITE_REACT_FRONT_END_URL.replace(/^https?:\/\//, "");
    const [analyticToggle, setAnalyticToggle] = useState(false);
    const [loader, setLoader] = useState(false);
    const [selectedUrl, setSelectedUrl] = useState("");
    const [analyticsData, setAnalyticsData] = useState([]);
    const analyticsHandler = (shortUrl) => {
        if (!analyticToggle) {
            setSelectedUrl(shortUrl);
        }
        setAnalyticToggle(!analyticToggle);
    }

    const handleDelete = async (shortUrl) => {
        try {
            await api.delete(`/urls/${shortUrl}`);
            toast.success("Short URL deleted successfully!");
            onUrlDeleted();
        } catch (error) {
            console.error("Error deleting URL:", error);
            toast.error("Failed to delete URL. Please try again.");
        }
    };

    const fetchMyShortUrl = async () => {
        setLoader(true);
        try {
            const { data } = await api.get(`/urls/analytics/${selectedUrl}?startDate=2024-12-01T00:00:00&endDate=2025-12-31T23:59:59`);
            setAnalyticsData(data);
            setSelectedUrl("");
            console.log(data);
        } catch (error) {
            navigate("/error");
            console.log(error);
        } finally {
            setLoader(false);
        }
    }

    useEffect(() => {
        if (selectedUrl) {
            fetchMyShortUrl();
        }
    }, [selectedUrl]);


    return (<div
        className={`bg-slate-100 shadow-lg border border-dotted border-slate-500 px-6 sm:py-1 py-3 rounded-md transition-all duration-100`}>
        <div className={`flex sm:flex-row flex-col sm:justify-between w-full sm:gap-0 gap-5 py-5`}>
            <div className="flex-1 sm:space-y-1 max-w-full overflow-x-auto overflow-y-hidden ">
                <div className="text-slate-900 pb-1 sm:pb-0   flex items-center gap-2 ">
                    <Link
                        target='_'
                        className='text-[17px]  font-montserrat font-[600] text-link-color'
                        to={import.meta.env.VITE_REACT_FRONT_END_URL + "/s/" + `${shortUrl}`}>
                        {subDomain + "/s/" + `${shortUrl}`}
                    </Link>
                    <FaExternalLinkAlt className="text-link-color"/>
                </div>
                <div className="flex items-center gap-1 ">
                    <h3 className=" text-slate-700 font-[400] text-[17px] ">
                        {originalUrl}
                    </h3>
                </div>
                <div className="flex items-center gap-8 pt-6">
                    <div className="flex gap-1 items-center font-semibold text-green-800">
                            <span>
                                <MdOutlineAdsClick className="text-[22px] me-1"/>
                            </span>
                        <span className="text-[16px]">{clickCount}</span>
                        <span className="text-[15px] ">
                {clickCount === 0 || clickCount === 1 ? "Click" : "Clicks"}
                        </span>
                    </div>
                    <div className="flex items-center gap-2 font-semibold text-lg text-slate-800">
              <span>
                <FaRegCalendarAlt/>
              </span>
                        <span className="text-[17px]">
                {dayjs(createDate).format("MMM DD, YYYY")}
                        </span>
                    </div>
                </div>
            </div>
            <CopyAndAnalytics
                shortUrl={shortUrl}
                analyticsHandler={analyticsHandler}
                onDelete={handleDelete}
            />
        </div>
        <Fragment>
            <div className={`${
                analyticToggle ? "flex" : "hidden"
            }  max-h-96 sm:mt-0 mt-5 min-h-96 relative  border-t-2 w-[100%] overflow-hidden `}>
                {loader ? (
                    <div className="min-h-[calc(450px-140px)] flex justify-center items-center w-full">
                        <div className="flex flex-col items-center gap-1">
                            <p className='text-slate-700'>Please Wait...</p>
                        </div>
                    </div>
                ) : (
                    <>{analyticsData.length === 0 && (
                        <div className="absolute flex flex-col  justify-center sm:items-center items-end  w-full left-0 top-0 bottom-0 right-0 m-auto">
                            <h1 className=" text-slate-800 font-serif sm:text-2xl text-[15px] font-bold mb-1">
                                No Data For This Time Period
                            </h1>
                            <h3 className="sm:w-96 w-[90%] sm:ml-0 pl-6 text-center sm:text-lg text-[12px] text-slate-600 ">
                                Share your short link to view where your engagements are
                                coming from
                            </h3>
                        </div>
                    )}
                        <Graph graphData={analyticsData} />
                    </>
                )}
            </div>
        </Fragment>
    </div>
    );
};

export default ShortenItem;
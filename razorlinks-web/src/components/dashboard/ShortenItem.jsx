import {FaExternalLinkAlt, FaRegCalendarAlt} from "react-icons/fa";
import {MdOutlineAdsClick} from "react-icons/md";
import dayjs from "dayjs";
import CopyAndAnalytics from "./CopyAndAnalytics.jsx";
import {Fragment} from "react";
import Graph from "./Graph.jsx";
import api from "../../services/api.js";
import toast from "react-hot-toast";
import {useFetchUrlAnalytics} from "../../hooks/useQuery.js";

const ShortenItem = ({
    originalUrl,
    shortUrl,
    clickCount,
    createDate,
    onUrlDeleted,
    isAnalyticsOpen,
    analyticsStartDate,
    analyticsEndDate,
    analyticsSortOrder,
    onToggleAnalytics,
    onAnalyticsStartDateChange,
    onAnalyticsEndDateChange,
    onAnalyticsSortOrderChange,
}) => {
    const subDomain = import.meta.env.VITE_REACT_SUBDOMAIN.replace(/^https?:\/\//, "");
    const invalidAnalyticsRange = analyticsEndDate < analyticsStartDate;

    const analyticsQuery = useFetchUrlAnalytics({
        shortUrl,
        startDate: analyticsStartDate,
        endDate: analyticsEndDate,
        sortOrder: analyticsSortOrder,
        enabled: isAnalyticsOpen && !invalidAnalyticsRange,
    });

    const analyticsData = analyticsQuery.data?.content ?? [];

    const handleDelete = async (shortCode) => {
        try {
            await api.delete(`/urls/${shortCode}`);
            toast.success("Short URL deleted successfully!");
            await onUrlDeleted();
        } catch (error) {
            console.error("Error deleting URL:", error);
            toast.error("Failed to delete URL. Please try again.");
        }
    };

    return (
        <div className="bg-slate-100 shadow-lg border border-dotted border-slate-500 px-6 sm:py-1 py-3 rounded-md transition-all duration-100">
            <div className="flex sm:flex-row flex-col sm:justify-between w-full sm:gap-0 gap-5 py-5">
                <div className="flex-1 sm:space-y-1 max-w-full overflow-x-auto overflow-y-hidden">
                    <div className="text-slate-900 pb-1 sm:pb-0 flex items-center gap-2">
                        <a
                            href={`${import.meta.env.VITE_REACT_SUBDOMAIN}/${shortUrl}`}
                            target="_blank"
                            className="text-[17px] font-montserrat font-[600] text-link-color"
                            rel="noreferrer"
                        >
                            {subDomain + "/" + `${shortUrl}`}
                        </a>
                        <FaExternalLinkAlt className="text-link-color"/>
                    </div>

                    <div className="flex items-center gap-1">
                        <h3 className="text-slate-700 font-[400] text-[17px]">
                            {originalUrl}
                        </h3>
                    </div>

                    <div className="flex items-center gap-8 pt-6">
                        <div className="flex gap-1 items-center font-semibold text-green-800">
                            <MdOutlineAdsClick className="text-[22px] me-1"/>
                            <span className="text-[16px]">{clickCount}</span>
                            <span className="text-[15px]">
                                {clickCount === 0 || clickCount === 1 ? "Click" : "Clicks"}
                            </span>
                        </div>

                        <div className="flex items-center gap-2 font-semibold text-lg text-slate-800">
                            <FaRegCalendarAlt/>
                            <span className="text-[17px]">
                                {dayjs(createDate).format("MMM DD, YYYY")}
                            </span>
                        </div>
                    </div>
                </div>

                <CopyAndAnalytics
                    shortUrl={shortUrl}
                    analyticsHandler={() => onToggleAnalytics(shortUrl)}
                    onDelete={handleDelete}
                />
            </div>

            <Fragment>
                <div
                    className={`${
                        isAnalyticsOpen ? "flex" : "hidden"
                    } max-h-[36rem] sm:mt-0 mt-5 min-h-96 relative border-t-2 w-full overflow-hidden flex-col`}
                >
                    <div className="flex flex-wrap items-end gap-3 border-b border-slate-200 py-4">
                        <label className="flex min-w-[160px] flex-col gap-1 text-sm text-slate-600">
                            Start Date
                            <input
                                type="date"
                                value={analyticsStartDate}
                                onChange={(event) => onAnalyticsStartDateChange(event.target.value)}
                                className="rounded-md border border-slate-300 px-3 py-2 text-slate-800 outline-none focus:border-btn-color"
                            />
                        </label>

                        <label className="flex min-w-[160px] flex-col gap-1 text-sm text-slate-600">
                            End Date
                            <input
                                type="date"
                                value={analyticsEndDate}
                                onChange={(event) => onAnalyticsEndDateChange(event.target.value)}
                                className="rounded-md border border-slate-300 px-3 py-2 text-slate-800 outline-none focus:border-btn-color"
                            />
                        </label>

                        <label className="flex min-w-[160px] flex-col gap-1 text-sm text-slate-600">
                            Sort Direction
                            <select
                                value={analyticsSortOrder}
                                onChange={(event) => onAnalyticsSortOrderChange(event.target.value)}
                                className="rounded-md border border-slate-300 px-3 py-2 text-slate-800 outline-none focus:border-btn-color"
                            >
                                <option value="ASC">Oldest to newest</option>
                                <option value="DESC">Newest to oldest</option>
                            </select>
                        </label>
                    </div>

                    {analyticsQuery.isLoading || analyticsQuery.isFetching ? (
                        <div className="min-h-[calc(450px-140px)] flex justify-center items-center w-full">
                            <div className="flex flex-col items-center gap-1">
                                <p className="text-slate-700">Please Wait...</p>
                            </div>
                        </div>
                    ) : invalidAnalyticsRange ? (
                        <div className="flex min-h-[calc(450px-140px)] items-center justify-center text-center text-amber-700">
                            Choose an end date that is the same as or after the start date.
                        </div>
                    ) : analyticsQuery.error ? (
                        <div className="flex min-h-[calc(450px-140px)] items-center justify-center px-6 text-center text-red-600">
                            {analyticsQuery.error?.response?.data?.message ?? "Unable to load analytics for this link."}
                        </div>
                    ) : (
                        <>
                            {analyticsData.length === 0 && (
                                <div className="absolute flex flex-col justify-center sm:items-center items-end w-full left-0 top-[72px] bottom-0 right-0 m-auto">
                                    <h1 className="text-slate-800 font-serif sm:text-2xl text-[15px] font-bold mb-1">
                                        No Data For This Time Period
                                    </h1>
                                    <h3 className="sm:w-96 w-[90%] sm:ml-0 pl-6 text-center sm:text-lg text-[12px] text-slate-600">
                                        Try a wider range or share your short link to generate activity.
                                    </h3>
                                </div>
                            )}
                            <Graph graphData={analyticsData}/>
                        </>
                    )}
                </div>
            </Fragment>
        </div>
    );
};

export default ShortenItem;

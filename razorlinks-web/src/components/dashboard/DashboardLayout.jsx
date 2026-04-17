import dayjs from "dayjs";
import {useDeferredValue, useState} from "react";
import Graph from "./Graph.jsx";
import {useFetchMyShortUrls, useFetchTotalClicks} from "../../hooks/useQuery.js";
import ShortenPopUp from "./ShortenPopUp.jsx";
import {FaLink} from "react-icons/fa";
import ShortenUrlList from "./ShortenUrlList.jsx";
import Loader from "../Loader.jsx";
import Errors from "../Errors.jsx";
import {useSearchParams} from "react-router-dom";
import {buildUpdatedSearchParams, readNumberParam, readStringParam} from "../../utils/queryParams.js";

const DEFAULT_END_DATE = dayjs().format("YYYY-MM-DD");
const DEFAULT_START_DATE = dayjs().subtract(29, "day").format("YYYY-MM-DD");

const DASHBOARD_SORT_OPTIONS = {
    newest: {sortBy: "createdDate", sortOrder: "DESC"},
    oldest: {sortBy: "createdDate", sortOrder: "ASC"},
    mostClicked: {sortBy: "clickCount", sortOrder: "DESC"},
    leastClicked: {sortBy: "clickCount", sortOrder: "ASC"},
    shortUrlAsc: {sortBy: "shortUrl", sortOrder: "ASC"},
    shortUrlDesc: {sortBy: "shortUrl", sortOrder: "DESC"},
};
const ACTIVITY_FILTERS = new Set(["all", "clicked", "unclicked"]);

const DashboardLayout = () => {
    const [shortenPopUp, setShortenPopUp] = useState(false);
    const [searchParams, setSearchParams] = useSearchParams();
    const graphStartDate = readStringParam(searchParams, "graphStart", DEFAULT_START_DATE);
    const graphEndDate = readStringParam(searchParams, "graphEnd", DEFAULT_END_DATE);
    const searchText = readStringParam(searchParams, "q", "");
    const deferredSearchText = useDeferredValue(searchText);
    const activityParam = readStringParam(searchParams, "activity", "all");
    const activityFilter = ACTIVITY_FILTERS.has(activityParam) ? activityParam : "all";
    const sortParam = readStringParam(searchParams, "sort", "newest");
    const sortOption = DASHBOARD_SORT_OPTIONS[sortParam] ? sortParam : "newest";
    const page = readNumberParam(searchParams, "page", 0, 0);
    const pageSize = readNumberParam(searchParams, "size", 5, 1);
    const openAnalyticsShortUrl = readStringParam(searchParams, "analytics", "");
    const analyticsStartDate = readStringParam(searchParams, "analyticsStart", DEFAULT_START_DATE);
    const analyticsEndDate = readStringParam(searchParams, "analyticsEnd", DEFAULT_END_DATE);
    const analyticsSortParam = readStringParam(searchParams, "analyticsSort", "ASC").toUpperCase();
    const analyticsSortOrder = analyticsSortParam === "DESC" ? "DESC" : "ASC";
    const paginationModel = {page, pageSize};
    const invalidGraphRange = graphEndDate < graphStartDate;

    const updateUrlState = (updates) => {
        setSearchParams(buildUpdatedSearchParams(searchParams, updates), {replace: true});
    };

    const totalClicksQuery = useFetchTotalClicks({
        startDate: graphStartDate,
        endDate: graphEndDate,
        enabled: !invalidGraphRange,
    });

    const myUrlsQuery = useFetchMyShortUrls({
        page,
        size: pageSize,
        search: deferredSearchText || undefined,
        minClickCount: activityFilter === "clicked" ? 1 : undefined,
        maxClickCount: activityFilter === "unclicked" ? 0 : undefined,
        ...DASHBOARD_SORT_OPTIONS[sortOption],
    });

    const totalClicks = totalClicksQuery.data?.content ?? [];
    const myShortenUrls = myUrlsQuery.data?.content ?? [];
    const totalPages = myUrlsQuery.data?.totalPages ?? 0;
    const totalElements = myUrlsQuery.data?.totalElements ?? 0;

    const handleRefreshUrls = async () => {
        const result = await myUrlsQuery.refetch();

        if (page > 0 && (result.data?.content?.length ?? 0) === 0) {
            updateUrlState({page: page - 1});
        }
    };

    const handleResetFilters = () => {
        setSearchParams(new URLSearchParams(), {replace: true});
    };

    if (myUrlsQuery.error) {
        return <Errors message={myUrlsQuery.error?.response?.data?.message ?? "Unable to load your links"} />;
    }

    return (
        <div className="lg:px-14 sm:px-8 px-4 min-h-[calc(100vh-64px)]">
            <div className="lg:w-[90%] w-full mx-auto py-16">
                <div className="mb-6 rounded-xl border border-slate-200 bg-white p-4 shadow-sm">
                    <div className="flex flex-wrap items-end gap-3">
                        <label className="flex min-w-[180px] flex-col gap-1 text-sm text-slate-600">
                            Graph Start
                            <input
                                type="date"
                                value={graphStartDate}
                                onChange={(event) => updateUrlState({graphStart: event.target.value})}
                                className="rounded-md border border-slate-300 px-3 py-2 text-slate-800 outline-none focus:border-btn-color"
                            />
                        </label>

                        <label className="flex min-w-[180px] flex-col gap-1 text-sm text-slate-600">
                            Graph End
                            <input
                                type="date"
                                value={graphEndDate}
                                onChange={(event) => updateUrlState({graphEnd: event.target.value})}
                                className="rounded-md border border-slate-300 px-3 py-2 text-slate-800 outline-none focus:border-btn-color"
                            />
                        </label>

                        <div className="text-sm text-slate-500">
                            <p className="font-semibold text-slate-700">Total click trend</p>
                            <p>Backend-filtered by the selected date range.</p>
                        </div>
                    </div>
                </div>

                <div className="h-96 relative">
                    {totalClicksQuery.isLoading ? (
                        <Loader/>
                    ) : invalidGraphRange ? (
                        <div className="flex h-full items-center justify-center rounded-xl border border-amber-200 bg-amber-50 text-center text-amber-700">
                            Choose an end date that is the same as or after the start date.
                        </div>
                    ) : totalClicksQuery.error ? (
                        <div className="flex h-full items-center justify-center rounded-xl border border-red-200 bg-red-50 px-6 text-center text-red-600">
                            {totalClicksQuery.error?.response?.data?.message ?? "Unable to load click analytics."}
                        </div>
                    ) : (
                        <>
                            {totalClicks.length === 0 && (
                                <div className="absolute flex flex-col justify-center sm:items-center items-end w-full left-0 top-0 bottom-0 right-0 m-auto">
                                    <h1 className="text-slate-800 font-serif sm:text-2xl text-[18px] font-bold mb-1">
                                        No Data For This Time Period
                                    </h1>
                                    <h3 className="sm:w-96 w-[90%] sm:ml-0 pl-6 text-center sm:text-lg text-sm text-slate-600">
                                        Share your short link to view where your engagements are
                                        coming from
                                    </h3>
                                </div>
                            )}
                            <Graph graphData={totalClicks}/>
                        </>
                    )}
                </div>

                <div className="py-5 sm:text-end text-center">
                    <button
                        className="bg-custom-gradient px-4 py-2 rounded-md text-white"
                        onClick={() => setShortenPopUp(true)}
                    >
                        Create a New Short URL
                    </button>
                </div>

                <div className="mb-6 rounded-xl border border-slate-200 bg-white p-4 shadow-sm">
                    <div className="grid gap-3 lg:grid-cols-[minmax(0,1fr)_180px_220px_160px]">
                        <label className="flex flex-col gap-1 text-sm text-slate-600">
                            Search
                            <input
                                type="text"
                                value={searchText}
                                onChange={(event) => {
                                    updateUrlState({q: event.target.value, page: 0});
                                }}
                                placeholder="Search short or original URLs"
                                className="rounded-md border border-slate-300 px-3 py-2 text-slate-800 outline-none focus:border-btn-color"
                            />
                        </label>

                        <label className="flex flex-col gap-1 text-sm text-slate-600">
                            Activity
                            <select
                                value={activityFilter}
                                onChange={(event) => {
                                    updateUrlState({activity: event.target.value, page: 0});
                                }}
                                className="rounded-md border border-slate-300 px-3 py-2 text-slate-800 outline-none focus:border-btn-color"
                            >
                                <option value="all">All links</option>
                                <option value="clicked">With clicks</option>
                                <option value="unclicked">No clicks yet</option>
                            </select>
                        </label>

                        <label className="flex flex-col gap-1 text-sm text-slate-600">
                            Sort
                            <select
                                value={sortOption}
                                onChange={(event) => {
                                    updateUrlState({sort: event.target.value, page: 0});
                                }}
                                className="rounded-md border border-slate-300 px-3 py-2 text-slate-800 outline-none focus:border-btn-color"
                            >
                                <option value="newest">Newest first</option>
                                <option value="oldest">Oldest first</option>
                                <option value="mostClicked">Most clicked</option>
                                <option value="leastClicked">Least clicked</option>
                                <option value="shortUrlAsc">Short URL A-Z</option>
                                <option value="shortUrlDesc">Short URL Z-A</option>
                            </select>
                        </label>

                        <label className="flex flex-col gap-1 text-sm text-slate-600">
                            Page Size
                            <select
                                value={pageSize}
                                onChange={(event) => {
                                    updateUrlState({
                                        page: 0,
                                        size: Number(event.target.value),
                                    });
                                }}
                                className="rounded-md border border-slate-300 px-3 py-2 text-slate-800 outline-none focus:border-btn-color"
                            >
                                <option value={5}>5 per page</option>
                                <option value={10}>10 per page</option>
                                <option value={20}>20 per page</option>
                            </select>
                        </label>

                        <div className="flex items-end">
                            <button
                                type="button"
                                onClick={handleResetFilters}
                                className="rounded-md border border-slate-300 px-4 py-2 text-sm font-semibold text-slate-700 transition hover:border-slate-400"
                            >
                                Reset View
                            </button>
                        </div>
                    </div>

                    <div className="mt-3 flex items-center justify-between text-sm text-slate-500">
                        <span>{totalElements} links found</span>
                        <span>{myUrlsQuery.isFetching ? "Refreshing..." : "Server-side results"}</span>
                    </div>
                </div>

                {myUrlsQuery.isLoading ? (
                    <Loader/>
                ) : myShortenUrls.length === 0 ? (
                    <div className="flex justify-center pt-8">
                        <div className="flex gap-2 items-center justify-center py-6 sm:px-8 px-5 rounded-md shadow-lg bg-gray-50">
                            <h1 className="text-slate-800 font-montserrat sm:text-[18px] text-[14px] font-semibold mb-1">
                                {searchText || activityFilter !== "all"
                                    ? "No short links matched your current filters"
                                    : "You haven't created any short link yet"}
                            </h1>
                            <FaLink className="text-blue-500 sm:text-xl text-sm"/>
                        </div>
                    </div>
                ) : (
                    <>
                        <ShortenUrlList
                            data={myShortenUrls}
                            onRefetch={handleRefreshUrls}
                            openAnalyticsShortUrl={openAnalyticsShortUrl}
                            analyticsStartDate={analyticsStartDate}
                            analyticsEndDate={analyticsEndDate}
                            analyticsSortOrder={analyticsSortOrder}
                            onToggleAnalytics={(shortUrl) => updateUrlState({
                                analytics: openAnalyticsShortUrl === shortUrl ? undefined : shortUrl,
                            })}
                            onAnalyticsStartDateChange={(value) => updateUrlState({analyticsStart: value})}
                            onAnalyticsEndDateChange={(value) => updateUrlState({analyticsEnd: value})}
                            onAnalyticsSortOrderChange={(value) => updateUrlState({analyticsSort: value})}
                        />
                        <div className="mt-6 flex flex-col gap-3 rounded-xl border border-slate-200 bg-white px-4 py-3 shadow-sm sm:flex-row sm:items-center sm:justify-between">
                            <span className="text-sm text-slate-600">
                                Page {totalPages === 0 ? 0 : page + 1} of {Math.max(totalPages, 1)}
                            </span>

                            <div className="flex items-center gap-2">
                                <button
                                    type="button"
                                    onClick={() => updateUrlState({page: page - 1})}
                                    disabled={page === 0 || myUrlsQuery.isFetching}
                                    className="rounded-md border border-slate-300 px-4 py-2 text-sm font-semibold text-slate-700 transition disabled:cursor-not-allowed disabled:opacity-50"
                                >
                                    Previous
                                </button>

                                <button
                                    type="button"
                                    onClick={() => updateUrlState({page: page + 1})}
                                    disabled={page >= totalPages - 1 || myUrlsQuery.isFetching || totalPages === 0}
                                    className="rounded-md bg-btn-color px-4 py-2 text-sm font-semibold text-white transition disabled:cursor-not-allowed disabled:opacity-50"
                                >
                                    Next
                                </button>
                            </div>
                        </div>
                    </>
                )}
            </div>

            <ShortenPopUp
                refetch={handleRefreshUrls}
                open={shortenPopUp}
                setOpen={setShortenPopUp}
            />
        </div>
    );
}

export default DashboardLayout;

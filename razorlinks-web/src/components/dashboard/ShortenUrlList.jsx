import ShortenItem from "./ShortenItem.jsx";

const ShortenUrlList = ({
    data,
    onRefetch,
    openAnalyticsShortUrl,
    analyticsStartDate,
    analyticsEndDate,
    analyticsSortOrder,
    onToggleAnalytics,
    onAnalyticsStartDateChange,
    onAnalyticsEndDateChange,
    onAnalyticsSortOrderChange,
}) => {

    return (
        <div className="my-6 space-y-4">
            {data.map((item)=>(
                <ShortenItem
                    key={item.id}
                    {...item}
                    onUrlDeleted={onRefetch}
                    isAnalyticsOpen={openAnalyticsShortUrl === item.shortUrl}
                    analyticsStartDate={analyticsStartDate}
                    analyticsEndDate={analyticsEndDate}
                    analyticsSortOrder={analyticsSortOrder}
                    onToggleAnalytics={onToggleAnalytics}
                    onAnalyticsStartDateChange={onAnalyticsStartDateChange}
                    onAnalyticsEndDateChange={onAnalyticsEndDateChange}
                    onAnalyticsSortOrderChange={onAnalyticsSortOrderChange}
                />
            ))}
        </div>
    )
}
export default ShortenUrlList;

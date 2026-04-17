import api from "../services/api.js";
import {keepPreviousData, useQuery} from "@tanstack/react-query";
import {cleanParams, getRangePageSize, toEndOfDay, toStartOfDay} from "../utils/queryParams.js";

export const useFetchMyShortUrls = (params) => {
    return useQuery({
        queryKey: ['my-shortenurls', params],
        queryFn: async () => {
            const response = await api.get("/urls/myurls", {
                params: cleanParams(params),
            });
            return response.data;
        },
        placeholderData: keepPreviousData,
        staleTime: 5000,
    })
}

export const useFetchTotalClicks = ({ startDate, endDate, enabled = true }) => {
    return useQuery({
            queryKey: ['url-totalclick', startDate, endDate],
            enabled,
            queryFn: async () => {
                const response = await api.get("/urls/totalClicks", {
                    params: cleanParams({
                        startDate: toStartOfDay(startDate),
                        endDate: toEndOfDay(endDate),
                        page: 0,
                        size: getRangePageSize(startDate, endDate, 31),
                        sortBy: "clickDate",
                        sortOrder: "ASC",
                    }),
                });
                return response.data;
            },
            placeholderData: keepPreviousData,
            staleTime: 5000
        },
    )
}

export const useFetchUrlAnalytics = ({ shortUrl, startDate, endDate, sortOrder = "ASC", enabled = true }) => {
    return useQuery({
        queryKey: ['url-analytics', shortUrl, startDate, endDate, sortOrder],
        enabled: enabled && Boolean(shortUrl),
        queryFn: async () => {
            const response = await api.get(`/urls/analytics/${shortUrl}`, {
                params: cleanParams({
                    startDate: toStartOfDay(startDate),
                    endDate: toEndOfDay(endDate),
                    page: 0,
                    size: getRangePageSize(startDate, endDate, 31),
                    sortBy: "clickDate",
                    sortOrder,
                }),
            });
            return response.data;
        },
        placeholderData: keepPreviousData,
        staleTime: 5000,
    });
};

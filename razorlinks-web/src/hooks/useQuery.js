import api from "../api/api.js";
import {useQuery} from "@tanstack/react-query";

export const useFetchMyShortUrls = (token) => {
    return useQuery({
        queryKey: ['my-shortenurls'],
        queryFn: async () => {
            return await api.get(
                "/api/urls/myurls",
                {
                    headers: {
                        "Content-Type": "application/json",
                        Accept: "application/json",
                        Authorization: "Bearer " + token,
                    },
                }
            );
        },
        select: (data) => {
            return data.data.sort(
                (a, b) => new Date(b.createdDate) - new Date(a.createdDate)
            );
        },
        staleTime: 5000,
    })
}

export const useFetchTotalClicks = (token) => {
    return useQuery({
            queryKey: ['url-totalclick'],
            queryFn: async () => {
                return await api.get(
                    "/api/urls/totalClicks?startDate=2024-01-01&endDate=2025-12-31",
                    {
                        headers: {
                            "Content-Type": "application/json",
                            Accept: "application/json",
                            Authorization: "Bearer " + token,
                        },
                    }
                );
            },
            select: (data) => {
                return Object.keys(data.data).map((key) => ({
                    clickDate: key,
                    count: data.data[key],
                }));

            },
            staleTime: 5000
        },
    )
}
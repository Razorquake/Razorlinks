import dayjs from "dayjs";

export const cleanParams = (params) =>
    Object.fromEntries(
        Object.entries(params).filter(([, value]) =>
            value !== undefined && value !== null && value !== ""
        )
    );

export const resolveSortParams = (sortModel, defaultSort, fieldMap = {}) => {
    const activeSort = sortModel?.[0];
    const field = activeSort?.field ?? defaultSort.field;
    const sort = activeSort?.sort ?? defaultSort.sort;

    return {
        sortBy: fieldMap[field] ?? field,
        sortOrder: sort.toUpperCase(),
    };
};

export const resolveGridQueryParams = ({
    paginationModel,
    sortModel,
    defaultSort,
    fieldMap,
    filters = {},
}) =>
    cleanParams({
        page: paginationModel.page,
        size: paginationModel.pageSize,
        ...resolveSortParams(sortModel, defaultSort, fieldMap),
        ...filters,
    });

export const toStartOfDay = (date) => (date ? `${date}T00:00:00` : undefined);

export const toEndOfDay = (date) => (date ? `${date}T23:59:59` : undefined);

export const getRangePageSize = (startDate, endDate, fallback = 31) => {
    if (!startDate || !endDate) {
        return fallback;
    }

    const start = dayjs(startDate);
    const end = dayjs(endDate);

    if (!start.isValid() || !end.isValid() || end.isBefore(start)) {
        return fallback;
    }

    return Math.min(Math.max(end.diff(start, "day") + 1, 1), 1000);
};

export const readStringParam = (searchParams, key, fallback = "") =>
    searchParams.get(key) ?? fallback;

export const readNumberParam = (searchParams, key, fallback, minimum = 0) => {
    const value = Number(searchParams.get(key));

    if (!Number.isFinite(value) || value < minimum) {
        return fallback;
    }

    return Math.trunc(value);
};

export const readSortDirectionParam = (searchParams, key, fallback = "desc") => {
    const value = (searchParams.get(key) ?? fallback).toLowerCase();
    return value === "asc" ? "asc" : "desc";
};

export const buildUpdatedSearchParams = (searchParams, updates) => {
    const next = new URLSearchParams(searchParams);

    Object.entries(updates).forEach(([key, value]) => {
        if (value === undefined || value === null || value === "") {
            next.delete(key);
            return;
        }

        next.set(key, String(value));
    });

    return next;
};

import {useDeferredValue} from "react";
import {keepPreviousData, useQuery} from "@tanstack/react-query";
import {MdDateRange} from "react-icons/md";
import api from "../../services/api.js";
import dayjs from "dayjs";
import {Link, useSearchParams} from "react-router-dom";
import {DataGrid} from "@mui/x-data-grid";
import Errors from "../Errors.jsx";
import {
    buildUpdatedSearchParams,
    readNumberParam,
    readSortDirectionParam,
    readStringParam,
    resolveGridQueryParams
} from "../../utils/queryParams.js";

const DEFAULT_SORT_MODEL = [{field: "timestamp", sort: "desc"}];
const DEFAULT_PAGINATION_MODEL = {page: 0, pageSize: 10};

const auditLogColumns = [
    {
        field: "action",
        headerName: "Action",
        width: 180,
        headerAlign: "center",
        disableColumnMenu: true,
        align: "center",
        editable: false,
        headerClassName: "text-black font-semibold border",
        cellClassName: "text-slate-700 font-normal border",
        renderHeader: () => <span>Action</span>,
    },
    {
        field: "username",
        headerName: "UserName",
        width: 180,
        editable: false,
        disableColumnMenu: true,
        headerAlign: "center",
        align: "center",
        headerClassName: "text-black font-semibold border",
        cellClassName: "text-slate-700 font-normal border",
        renderHeader: () => <span>UserName</span>,
    },
    {
        field: "timestamp",
        headerName: "TimeStamp",
        disableColumnMenu: true,
        width: 230,
        editable: false,
        headerAlign: "center",
        align: "center",
        headerClassName: "text-black font-semibold border",
        cellClassName: "text-slate-700 font-normal border",
        renderHeader: () => <span>TimeStamp</span>,
        renderCell: (params) => (
            <div className="flex items-center justify-center gap-1">
                <MdDateRange className="text-slate-700 text-lg"/>
                <span>{dayjs(params.row.timestamp).format("MMMM DD, YYYY, hh:mm A")}</span>
            </div>
        ),
    },
    {
        field: "urlMappingId",
        headerName: "URLMappingId",
        disableColumnMenu: true,
        width: 150,
        editable: false,
        headerAlign: "center",
        align: "center",
        headerClassName: "text-black font-semibold border",
        cellClassName: "text-slate-700 font-normal border",
        renderHeader: () => <span>URLMappingId</span>,
    },
    {
        field: "shortUrl",
        headerName: "Short URL",
        width: 220,
        editable: false,
        headerAlign: "center",
        disableColumnMenu: true,
        align: "center",
        headerClassName: "text-black font-semibold border",
        cellClassName: "text-slate-700 font-normal border",
        renderHeader: () => <span>Short URL</span>,
    },
    {
        field: "view",
        headerName: "Action",
        width: 150,
        editable: false,
        headerAlign: "center",
        align: "center",
        headerClassName: "text-black font-semibold",
        cellClassName: "text-slate-700 font-normal",
        sortable: false,
        renderHeader: () => <span>Action</span>,
        renderCell: (params) => (
            <Link
                to={`/admin/audit-logs/${params.row.urlMappingId}`}
                className="h-full flex justify-center items-center"
            >
                <button className="bg-btn-color text-white px-4 flex justify-center items-center h-9 rounded-md">
                    Views
                </button>
            </Link>
        ),
    },
];

const AdminAuditLogs = () => {
    const [searchParams, setSearchParams] = useSearchParams();
    const page = readNumberParam(searchParams, "page", DEFAULT_PAGINATION_MODEL.page, 0);
    const pageSize = readNumberParam(searchParams, "size", DEFAULT_PAGINATION_MODEL.pageSize, 1);
    const sortBy = readStringParam(searchParams, "sortBy", DEFAULT_SORT_MODEL[0].field);
    const sortOrder = readSortDirectionParam(searchParams, "sortOrder", DEFAULT_SORT_MODEL[0].sort);
    const searchText = readStringParam(searchParams, "q", "");
    const deferredSearchText = useDeferredValue(searchText);
    const actionFilter = readStringParam(searchParams, "action", "");
    const startDate = readStringParam(searchParams, "start", "");
    const endDate = readStringParam(searchParams, "end", "");
    const paginationModel = {page, pageSize};
    const sortModel = [{field: sortBy, sort: sortOrder}];

    const {data, error, isLoading, isFetching} = useQuery({
        queryKey: [
            "admin-audit-logs",
            page,
            pageSize,
            sortBy,
            sortOrder,
            deferredSearchText,
            actionFilter,
            startDate,
            endDate,
        ],
        queryFn: async () => {
            const response = await api.get("/audit", {
                params: resolveGridQueryParams({
                    paginationModel,
                    sortModel,
                    defaultSort: DEFAULT_SORT_MODEL[0],
                    fieldMap: {
                        action: "action",
                        username: "username",
                        timestamp: "timestamp",
                        urlMappingId: "urlMappingId",
                        shortUrl: "shortUrl",
                    },
                    filters: {
                        search: deferredSearchText,
                        action: actionFilter || undefined,
                        startDate: startDate || undefined,
                        endDate: endDate || undefined,
                    },
                }),
            });

            return response.data;
        },
        placeholderData: keepPreviousData,
        staleTime: 5000,
    });

    const rows = (data?.content ?? []).map((item) => ({
        id: item.id,
        action: item.action,
        username: item.username,
        timestamp: item.timestamp,
        urlMappingId: item.urlMappingId,
        shortUrl: item.shortUrl,
    }));

    const updateUrlState = (updates) => {
        setSearchParams(buildUpdatedSearchParams(searchParams, updates), {replace: true});
    };

    const handleClearFilters = () => {
        setSearchParams(new URLSearchParams(), {replace: true});
    };

    if (error) {
        return <Errors message={error?.response?.data?.message ?? "Error fetching audit logs"} />;
    }

    return (
        <div className="p-4">
            <div className="py-4">
                <h1 className="text-center text-2xl font-bold text-slate-800 uppercase">
                    Audit Logs
                </h1>
                <p className="mt-2 text-center text-sm text-slate-500">
                    Review audit history with backend-driven search, filters, sorting, and pagination.
                </p>
            </div>

            <div className="mb-4 rounded-xl border border-slate-200 bg-white p-4 shadow-sm">
                <div className="grid gap-3 xl:grid-cols-[minmax(0,1fr)_220px_180px_180px_120px]">
                    <label className="flex flex-col gap-1 text-sm text-slate-600">
                        Search
                        <input
                            type="text"
                            value={searchText}
                            onChange={(event) => {
                                updateUrlState({q: event.target.value, page: 0});
                            }}
                            placeholder="Search action, user, or short URL"
                            className="rounded-md border border-slate-300 px-3 py-2 text-slate-800 outline-none focus:border-btn-color"
                        />
                    </label>

                    <label className="flex flex-col gap-1 text-sm text-slate-600">
                        Action
                        <select
                            value={actionFilter}
                            onChange={(event) => {
                                updateUrlState({action: event.target.value, page: 0});
                            }}
                            className="rounded-md border border-slate-300 px-3 py-2 text-slate-800 outline-none focus:border-btn-color"
                        >
                            <option value="">All actions</option>
                            <option value="SHORT_URL_CREATED">Created</option>
                            <option value="SHORT_URL_CLICKED">Clicked</option>
                            <option value="SHORT_URL_DELETED">Deleted</option>
                        </select>
                    </label>

                    <label className="flex flex-col gap-1 text-sm text-slate-600">
                        Start Date
                        <input
                            type="date"
                            value={startDate}
                            onChange={(event) => {
                                updateUrlState({start: event.target.value, page: 0});
                            }}
                            className="rounded-md border border-slate-300 px-3 py-2 text-slate-800 outline-none focus:border-btn-color"
                        />
                    </label>

                    <label className="flex flex-col gap-1 text-sm text-slate-600">
                        End Date
                        <input
                            type="date"
                            value={endDate}
                            onChange={(event) => {
                                updateUrlState({end: event.target.value, page: 0});
                            }}
                            className="rounded-md border border-slate-300 px-3 py-2 text-slate-800 outline-none focus:border-btn-color"
                        />
                    </label>

                    <div className="flex items-end">
                        <button
                            type="button"
                            onClick={handleClearFilters}
                            className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm font-semibold text-slate-700 transition hover:border-slate-400"
                        >
                            Reset
                        </button>
                    </div>
                </div>

                <div className="mt-3 flex items-center justify-between text-sm text-slate-500">
                    <span>{data?.totalElements ?? 0} logs found</span>
                    <span>{isFetching ? "Refreshing..." : "Server-side results"}</span>
                </div>
            </div>

            <div className="overflow-x-auto w-full mx-auto">
                <DataGrid
                    className="w-fit mx-auto px-0"
                    rows={rows}
                    columns={auditLogColumns}
                    rowCount={data?.totalElements ?? 0}
                    loading={isLoading || isFetching}
                    paginationModel={paginationModel}
                    onPaginationModelChange={(model) => updateUrlState({
                        page: model.pageSize !== pageSize ? 0 : model.page,
                        size: model.pageSize,
                    })}
                    paginationMode="server"
                    sortModel={sortModel}
                    onSortModelChange={(model) => {
                        const activeSort = model[0] ?? DEFAULT_SORT_MODEL[0];
                        updateUrlState({
                            sortBy: activeSort.field,
                            sortOrder: activeSort.sort ?? DEFAULT_SORT_MODEL[0].sort,
                            page: 0,
                        });
                    }}
                    sortingMode="server"
                    pageSizeOptions={[5, 10, 25, 50]}
                    disableRowSelectionOnClick
                    disableColumnResize
                />
            </div>
        </div>
    );
};

export default AdminAuditLogs;

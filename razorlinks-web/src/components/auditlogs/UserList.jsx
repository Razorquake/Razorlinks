import {useDeferredValue} from "react";
import {keepPreviousData, useQuery} from "@tanstack/react-query";
import {DataGrid} from "@mui/x-data-grid";
import api from "../../services/api.js";
import dayjs from "dayjs";
import {MdDateRange, MdOutlineEmail} from "react-icons/md";
import {Link, useSearchParams} from "react-router-dom";
import Errors from "../Errors.jsx";
import {
    buildUpdatedSearchParams,
    readNumberParam,
    readSortDirectionParam,
    readStringParam,
    resolveGridQueryParams
} from "../../utils/queryParams.js";

const DEFAULT_SORT_MODEL = [{field: "createdDate", sort: "desc"}];
const DEFAULT_PAGINATION_MODEL = {page: 0, pageSize: 10};

const userListsColumns = [
    {
        field: "username",
        headerName: "UserName",
        minWidth: 200,
        headerAlign: "center",
        align: "center",
        editable: false,
        disableColumnMenu: true,
        headerClassName: "text-black font-semibold border",
        cellClassName: "text-slate-700 font-normal border",
        renderHeader: () => <span className="text-center">Username</span>,
    },
    {
        field: "email",
        headerName: "Email",
        width: 260,
        editable: false,
        headerAlign: "center",
        headerClassName: "text-black font-semibold text-center border",
        cellClassName: "text-slate-700 font-normal border text-center",
        align: "center",
        disableColumnMenu: true,
        renderHeader: () => <span>Email</span>,
        renderCell: (params) => (
            <div className="flex items-center justify-center gap-1">
                <MdOutlineEmail className="text-slate-700 text-lg"/>
                <span>{params.row.email}</span>
            </div>
        ),
    },
    {
        field: "createdDate",
        headerName: "Created At",
        headerAlign: "center",
        width: 220,
        editable: false,
        headerClassName: "text-black font-semibold border",
        cellClassName: "text-slate-700 font-normal border",
        align: "center",
        disableColumnMenu: true,
        renderHeader: () => <span>Created At</span>,
        renderCell: (params) => (
            <div className="flex justify-center items-center gap-1">
                <MdDateRange className="text-slate-700 text-lg"/>
                <span>{dayjs(params.row.createdDate).format("MMMM DD, YYYY, hh:mm A")}</span>
            </div>
        ),
    },
    {
        field: "enabled",
        headerName: "Status",
        headerAlign: "center",
        align: "center",
        width: 200,
        editable: false,
        disableColumnMenu: true,
        headerClassName: "text-black font-semibold border",
        cellClassName: "text-slate-700 font-normal border",
        renderHeader: () => <span className="ps-10">Status</span>,
        renderCell: (params) => (
            <span className={params.row.enabled ? "text-green-700 font-semibold" : "text-slate-500 font-semibold"}>
                {params.row.enabled ? "Active" : "Inactive"}
            </span>
        ),
    },
    {
        field: "view",
        headerName: "Action",
        headerAlign: "center",
        editable: false,
        headerClassName: "text-black font-semibold text-center",
        cellClassName: "text-slate-700 font-normal",
        sortable: false,
        width: 200,
        renderHeader: () => <span>Action</span>,
        renderCell: (params) => (
            <Link
                to={`/admin/users/${params.id}`}
                className="h-full flex items-center justify-center"
            >
                <button className="bg-btn-color text-white px-4 flex justify-center items-center h-9 rounded-md">
                    Views
                </button>
            </Link>
        ),
    },
];

const UserList = () => {
    const [searchParams, setSearchParams] = useSearchParams();
    const page = readNumberParam(searchParams, "page", DEFAULT_PAGINATION_MODEL.page, 0);
    const pageSize = readNumberParam(searchParams, "size", DEFAULT_PAGINATION_MODEL.pageSize, 1);
    const sortBy = readStringParam(searchParams, "sortBy", DEFAULT_SORT_MODEL[0].field);
    const sortOrder = readSortDirectionParam(searchParams, "sortOrder", DEFAULT_SORT_MODEL[0].sort);
    const searchText = readStringParam(searchParams, "q", "");
    const deferredSearchText = useDeferredValue(searchText);
    const roleFilter = readStringParam(searchParams, "role", "");
    const statusFilter = readStringParam(searchParams, "status", "");
    const paginationModel = {page, pageSize};
    const sortModel = [{field: sortBy, sort: sortOrder}];

    const {data, error, isLoading, isFetching} = useQuery({
        queryKey: [
            "admin-users",
            page,
            pageSize,
            sortBy,
            sortOrder,
            deferredSearchText,
            roleFilter,
            statusFilter,
        ],
        queryFn: async () => {
            const response = await api.get("/admin/get-users", {
                params: resolveGridQueryParams({
                    paginationModel,
                    sortModel,
                    defaultSort: DEFAULT_SORT_MODEL[0],
                    fieldMap: {
                        username: "username",
                        email: "email",
                        createdDate: "createdDate",
                        enabled: "enabled",
                    },
                    filters: {
                        search: deferredSearchText,
                        roleName: roleFilter || undefined,
                        enabled: statusFilter === ""
                            ? undefined
                            : statusFilter === "active",
                    },
                }),
            });

            return response.data;
        },
        placeholderData: keepPreviousData,
        staleTime: 5000,
    });

    const users = data?.content ?? [];
    const rows = users.map((item) => ({
        id: item.userId,
        username: item.userName,
        email: item.email,
        createdDate: item.createdDate,
        enabled: item.enabled,
    }));

    const updateUrlState = (updates) => {
        setSearchParams(buildUpdatedSearchParams(searchParams, updates), {replace: true});
    };

    const handleClearFilters = () => {
        setSearchParams(new URLSearchParams(), {replace: true});
    };

    if (error) {
        return <Errors message={error?.response?.data?.message ?? "Error fetching users"} />;
    }

    return (
        <div className="p-4">
            <div className="py-4">
                <h1 className="text-center text-2xl font-bold text-slate-800 uppercase">
                    All Users
                </h1>
                <p className="mt-2 text-center text-sm text-slate-500">
                    Search, filter, sort, and paginate directly against the backend.
                </p>
            </div>

            <div className="mb-4 rounded-xl border border-slate-200 bg-white p-4 shadow-sm">
                <div className="grid gap-3 lg:grid-cols-[minmax(0,1fr)_180px_180px_120px]">
                    <label className="flex flex-col gap-1 text-sm text-slate-600">
                        Search
                        <input
                            type="text"
                            value={searchText}
                            onChange={(event) => updateUrlState({q: event.target.value, page: 0})}
                            placeholder="Search username or email"
                            className="rounded-md border border-slate-300 px-3 py-2 text-slate-800 outline-none focus:border-btn-color"
                        />
                    </label>

                    <label className="flex flex-col gap-1 text-sm text-slate-600">
                        Role
                        <select
                            value={roleFilter}
                            onChange={(event) => updateUrlState({role: event.target.value, page: 0})}
                            className="rounded-md border border-slate-300 px-3 py-2 text-slate-800 outline-none focus:border-btn-color"
                        >
                            <option value="">All roles</option>
                            <option value="ROLE_ADMIN">Admins</option>
                            <option value="ROLE_USER">Users</option>
                        </select>
                    </label>

                    <label className="flex flex-col gap-1 text-sm text-slate-600">
                        Status
                        <select
                            value={statusFilter}
                            onChange={(event) => updateUrlState({status: event.target.value, page: 0})}
                            className="rounded-md border border-slate-300 px-3 py-2 text-slate-800 outline-none focus:border-btn-color"
                        >
                            <option value="">All statuses</option>
                            <option value="active">Active</option>
                            <option value="inactive">Inactive</option>
                        </select>
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
                    <span>{data?.totalElements ?? 0} users found</span>
                    <span>{isFetching ? "Refreshing..." : "Server-side results"}</span>
                </div>
            </div>

            <div className="overflow-x-auto w-full mx-auto">
                <DataGrid
                    className="w-fit mx-auto"
                    rows={rows}
                    columns={userListsColumns}
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
                    disableRowSelectionOnClick
                    pageSizeOptions={[5, 10, 25, 50]}
                    disableColumnResize
                />
            </div>
        </div>
    );
};

export default UserList;

//Material ui data grid has used for the table
// to initialize the columns for the tables, and (field) value is used to show data in a specific column dynamically
import {MdDateRange} from "react-icons/md";
import {auditLogsTruncateTexts} from "../../utils/truncateText.js";
import {useEffect, useState} from "react";
import toast from "react-hot-toast";
import api from "../../services/api.js";
import moment from "moment";
import {DataGrid} from "@mui/x-data-grid";
import {FadeLoader} from "react-spinners";

const auditLogcolumns = [
    {
        field: "actions",
        headerName: "Action",
        width: 160,
        headerAlign: "center",
        disableColumnMenu: true,
        align: "center",
        editable: false,
        headerClassName: "text-black font-semibold border",
        cellClassName: "text-slate-700 font-normal  border",
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
        cellClassName: "text-slate-700 font-normal  border",
        renderHeader: () => <span>UserName</span>,
    },

    {
        field: "timestamp",
        headerName: "TimeStamp",
        disableColumnMenu: true,
        width: 220,
        editable: false,
        headerAlign: "center",
        align: "center",
        headerClassName: "text-black font-semibold border",
        cellClassName: "text-slate-700 font-normal  border",
        renderHeader: () => <span>TimeStamp</span>,
        renderCell: (params) => {
            return (
                <div className=" flex  items-center justify-center  gap-1 ">
          <span>
            <MdDateRange className="text-slate-700 text-lg" />
          </span>
                    <span>{params?.row?.timestamp}</span>
                </div>
            );
        },
    },
    {
        field: "noteid",
        headerName: "NoteId",
        disableColumnMenu: true,
        width: 150,
        editable: false,
        headerAlign: "center",
        align: "center",
        headerClassName: "text-black font-semibold border",
        cellClassName: "text-slate-700 font-normal  border",
        renderHeader: () => <span>NoteId</span>,
    },
    {
        field: "note",
        headerName: "Note Content",
        width: 220,
        editable: false,
        headerAlign: "center",
        disableColumnMenu: true,
        align: "center",
        headerClassName: "text-black font-semibold border",
        cellClassName: "text-slate-700 font-normal  border",
        renderHeader: () => <span>Note Content</span>,
        renderCell: (params) => {
            const contens = JSON.parse(params?.value)?.content;

            const response = auditLogsTruncateTexts(contens);

            return <p className=" text-slate-700 text-center   ">{response}</p>;
        },
    },
    {
        field: "action",
        headerName: "Action",
        width: 150,
        editable: false,
        headerAlign: "center",
        align: "center",
        headerClassName: "text-black font-semibold ",
        cellClassName: "text-slate-700 font-normal  ",
        sortable: false,

        renderHeader: () => <span>Action</span>,
        renderCell: (params) => {
            return (
                <Link
                    to={`/admin/audit-logs/${params.row.noteId}`}
                    className="h-full flex justify-center  items-center   "
                >
                    <button className="bg-btnColor text-white px-4 flex justify-center items-center  h-9 rounded-md ">
                        Views
                    </button>
                </Link>
            );
        },
    },
];

const AdminAuditLogs = () => {
    const [auditLogs, setAuditLogs] = useState([]);
    const [error, setError] = useState(null);
    const [loading, setLoading] = useState(false);

    const fetchAuditLogs = async () => {
        setLoading(true);
        try {
            const response = await api.get("/audit");
            setAuditLogs(response.data);
        } catch (err) {
            setError(err?.response?.data?.message);
            toast.error("Error fetching audit logs");
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchAuditLogs();
    }, []);

    const rows = auditLogs.map((item) => {
        //format the time bu using moment npm package

        const formattedDate = moment(item.timestamp).format(
            "MMMM DD, YYYY, hh:mm A"
        );

        //set the data for each row in the table according to the field name in columns
        //Example: username is the keyword in row it should matche with the field name in column so that the data will show on that column dynamically
        return {
            id: item.id,
            noteId: item.noteId,
            actions: item.action,
            username: item.username,
            timestamp: formattedDate,
            noteid: item.noteId,
            note: item.noteContent,
        };
    });

    if (error) {
        return <Errors message={error} />;
    }

    return (
        <div className="p-4">
            <div className="py-4">
                <h1 className="text-center text-2xl font-bold text-slate-800 uppercase">
                    Audit Logs
                </h1>
            </div>
            {loading ? (
                <>
                    {" "}
                    <div className="flex  flex-col justify-center items-center  h-72">
            <span>
              <FadeLoader
                  height="70"
                  width="70"
                  color="#4fa94d"
                  ariaLabel="blocks-loading"
                  wrapperStyle={{}}
                  wrapperClass="blocks-wrapper"
                  visible={true}
              />
            </span>
                        <span>Please wait...</span>
                    </div>
                </>
            ) : (
                <>
                    {" "}
                    <div className="overflow-x-auto w-full mx-auto">
                        <DataGrid
                            className="w-fit mx-auto px-0"
                            rows={rows}
                            columns={auditLogcolumns}
                            initialState={{
                                pagination: {
                                    paginationModel: {
                                        pageSize: 6,
                                    },
                                },
                            }}
                            pageSizeOptions={[6]}
                            disableRowSelectionOnClick
                            disableColumnResize
                        />
                    </div>
                </>
            )}
        </div>
    );
};

export default AdminAuditLogs;

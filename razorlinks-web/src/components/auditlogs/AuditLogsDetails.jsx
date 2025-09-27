import {MdDateRange} from "react-icons/md";
import {auditLogsTruncateTexts} from "../../utils/truncateText.js";
import Errors from "../Errors.jsx";
import {DataGrid} from "@mui/x-data-grid";
import moment from "moment";
import {useCallback, useEffect, useState} from "react";
import api from "../../services/api.js";
import {useParams} from "react-router-dom";
import {FadeLoader} from "react-spinners";

const auditLogscolumn = [
    {
        field: "actions",
        headerName: "Action",
        width: 160,
        headerAlign: "center",
        align: "center",
        editable: false,

        headerClassName: "text-black font-semibold border",
        cellClassName: "text-slate-700 font-normal  border",
        renderHeader: () => <span className="ps-10">Action</span>,
    },

    {
        field: "username",
        headerName: "UserName",
        width: 200,
        editable: false,
        headerAlign: "center",
        disableColumnMenu: true,
        align: "center",
        headerClassName: "text-black font-semibold border",
        cellClassName: "text-slate-700 font-normal  border",
        renderHeader: () => <span className="ps-10">UserName</span>,
    },

    {
        field: "timestamp",
        headerName: "TimeStamp",
        width: 220,
        editable: false,
        headerAlign: "center",
        disableColumnMenu: true,
        align: "center",
        headerClassName: "text-black font-semibold border",
        cellClassName: "text-slate-700 font-normal  border",
        renderHeader: () => <span className="ps-10">TimeStamp</span>,
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
        width: 350,
        disableColumnMenu: true,
        editable: false,
        headerAlign: "center",
        align: "center",
        headerClassName: "text-black font-semibold ",
        cellClassName: "text-slate-700 font-normal  ",
        renderHeader: () => <span className="ps-10">Note Content</span>,
        renderCell: (params) => {
            const contens = JSON.parse(params?.value)?.content;

            const response = auditLogsTruncateTexts(contens, 50);

            return <p className=" text-slate-700 text-center   ">{response}</p>;
        },
    },
];

const AuditLogsDetails = () => {
    //access the noteId
    const { noteId } = useParams();
    const [auditLogs, setAuditLogs] = useState([]);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);

    const fetchSingleAuditLogs = useCallback(async () => {
        setLoading(true);
        try {
            const { data } = await api.get(`/audit/note/${noteId}`);

            setAuditLogs(data);
        } catch (err) {
            setError(err?.response?.data?.message);
            console.log(err);
        } finally {
            setLoading(false);
        }
    }, [noteId]);

    useEffect(() => {
        if (noteId) {
            fetchSingleAuditLogs();
        }
    }, [noteId, fetchSingleAuditLogs]);

    const rows = auditLogs.map((item) => {
        const formattedDate = moment(item.timestamp).format(
            "MMMM DD, YYYY, hh:mm A"
        );

        //set the data for each rows in the table according to the field name in columns
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
            <div className="py-6">
                {auditLogs.length > 0 && (
                    <h1 className="text-center sm:text-2xl text-lg font-bold text-slate-800 ">
                        Audit Log for Note ID - {noteId}
                    </h1>
                )}
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
                    {auditLogs.length === 0 ? (
                        <Errors message="Invalid NoteId" />
                    ) : (
                        <>
                            {" "}
                            <div className="overflow-x-auto w-full">
                                <DataGrid
                                    className="w-fit mx-auto px-0"
                                    rows={rows}
                                    columns={auditLogscolumn}
                                    initialState={{
                                        pagination: {
                                            paginationModel: {
                                                pageSize: 6,
                                            },
                                        },
                                    }}
                                    disableRowSelectionOnClick
                                    pageSizeOptions={[6]}
                                    disableColumnResize
                                />
                            </div>
                        </>
                    )}{" "}
                </>
            )}
        </div>
    );
};

export default AuditLogsDetails;
import React from 'react';
import AdminAuditLogs from "./AdminAuditLogs.jsx";
import {Route, Routes} from "react-router-dom";
import {useStoreContext} from "../../store/ContextApi.jsx";
import Sidebar from "./AdminAreaSidebar.jsx";
import UserList from "./UserList.jsx";
import UserDetails from "./UserDetails.jsx";
import AuditLogsDetails from "./AuditLogsDetails.jsx";

const Admin = () => {
    const { openSidebar } = useStoreContext();
    return (
        <div className="flex">
            <Sidebar/>
            <div
                className={`transition-all overflow-hidden flex-1 duration-150 w-full min-h-[calc(100vh-74px)] ${
                    openSidebar ? "lg:ml-52 ml-12" : "ml-12"
                }`}
            >
                <Routes>
                    <Route path="audit-logs" element={<AdminAuditLogs />} />
                    <Route path="audit-logs/:urlMappingId" element={<AuditLogsDetails />} />
                    <Route path="users" element={<UserList />} />
                    <Route path="users/:userId" element={<UserDetails />} />
                    {/* Add other routes as necessary */}
                </Routes>
            </div>
        </div>
    );
};

export default Admin;
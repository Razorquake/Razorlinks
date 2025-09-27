import React, {useCallback, useEffect, useState} from 'react';
import {useForm} from "react-hook-form";
import {useParams} from "react-router-dom";
import api from "../../services/api.js";
import toast from "react-hot-toast";
import Errors from "../Errors.jsx";
import {FadeLoader} from "react-spinners";
import TextField from "../TextField.jsx";

const UserDetails = () => {
    const {
        register,
        handleSubmit,
        setValue,
        formState: { errors },
    } = useForm({
        defaultValues: {
            username: "",
            email: "",
            password: "",
        },
        mode: "onSubmit",
    });

    const [loading, setLoading] = useState(false);
    const [updateRoleLoader, setUpdateRoleLoader] = useState(false);
    const [passwordLoader, setPasswordLoader] = useState(false);

    const { userId } = useParams();
    const [user, setUser] = useState(null);
    const [roles, setRoles] = useState([]);
    const [selectedRole, setSelectedRole] = useState("");
    const [error, setError] = useState(null);
    const [isEditingPassword, setIsEditingPassword] = useState(false);

    const fetchUserDetails = useCallback(async () => {
        setLoading(true);
        try {
            const response = await api.get(`/admin/user/${userId}`);
            setUser(response.data);

            setSelectedRole(response.data.role?.roleName || "");
        } catch (err) {
            setError(err?.response?.data?.message);
            console.error("Error fetching user details", err);
        } finally {
            setLoading(false);
        }
    }, [userId]);

    useEffect(() => {
        //if user exist set the value by using the setValue function provided my react-hook-form
        if (user && Object.keys(user).length > 0) {
            setValue("username", user.userName);
            setValue("email", user.email);
        }
    }, [user, setValue]);

    const fetchRoles = useCallback(async () => {
        try {
            const response = await api.get("/admin/roles");
            setRoles(response.data);
        } catch (err) {
            setError(err?.response?.data?.message);
            console.error("Error fetching roles", err);
        }
    }, []);

    useEffect(() => {
        fetchUserDetails();
        fetchRoles();
    }, [fetchUserDetails, fetchRoles]);

    //set the selected role
    const handleRoleChange = (e) => {
        setSelectedRole(e.target.value);
    };

    //handle update role
    const handleUpdateRole = async () => {
        setUpdateRoleLoader(true);
        try {
            const formData = new URLSearchParams();
            formData.append("userId", userId);
            formData.append("roleName", selectedRole);

            await api.put(`/admin/update-role`, formData, {
                headers: {
                    "Content-Type": "application/x-www-form-urlencoded",
                },
            });
            fetchUserDetails();
            toast.success("Update role successful");
        } catch (err) {
            console.log(err);
            toast.error("Update Role Failed");
        } finally {
            setUpdateRoleLoader(false);
        }
    };

    //handle update the password
    const handleSavePassword = async (data) => {
        setPasswordLoader(true);
        const newPassword = data.password;

        try {
            const formData = new URLSearchParams();
            formData.append("userId", userId);
            formData.append("password", newPassword);

            await api.put(`/admin/update-password`, formData, {
                headers: {
                    "Content-Type": "application/x-www-form-urlencoded",
                },
            });
            setIsEditingPassword(false);
            setValue("password", "");
            //fetchUserDetails();
            toast.success("password update success");
        } catch (err) {
            toast.error("Error updating password " + err.response.data);
        } finally {
            setPasswordLoader(false);
        }
    };

    const handleCheckboxChange = async (e, updateUrl) => {
        const { name, checked } = e.target;

        let message = null;
        if (name === "lock") {
            message = "Update Account Lock status Successful";
        } else if (name === "expire") {
            message = "Update Account Expiry status Successful";
        } else if (name === "enabled") {
            message = "Update Account Enabled status Successful";
        } else if (name === "credentialsExpire") {
            message = "Update Account Credentials Expired status Successful";
        }

        try {
            const formData = new URLSearchParams();
            formData.append("userId", userId);

            formData.append(name, checked);

            await api.put(updateUrl, formData, {
                headers: {
                    "Content-Type": "application/x-www-form-urlencoded",
                },
            });
            fetchUserDetails();
            toast.success(message);
        } catch (err) {
            toast.error(err?.response?.data?.message);
            console.log(`Error updating ${name}:`);
        } finally {
            message = null;
        }
    };

    if (error) {
        return <Errors message={error} />;
    }

    return (
        <div className="sm:px-12 px-4 py-10   ">
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
                    <div className="lg:w-[70%] sm:w-[90%] w-full  mx-auto shadow-lg shadow-gray-300 p-8 rounded-md">
                        <div>
                            <h1 className="text-slate-800 text-2xl font-bold  pb-4">
                                Profile Information
                                <hr />
                            </h1>
                            <form
                                className="flex  flex-col  gap-2  "
                                onSubmit={handleSubmit(handleSavePassword)}
                            >
                                <TextField
                                    label="UserName"
                                    required
                                    id="username"
                                    className="w-full"
                                    type="text"
                                    message="*UserName is required"
                                    placeholder="Enter your UserName"
                                    register={register}
                                    errors={errors}
                                    readOnly
                                />
                                <TextField
                                    label="Email"
                                    required
                                    id="email"
                                    className="flex-1"
                                    type="text"
                                    message="*Email is required"
                                    placeholder="Enter your Email"
                                    register={register}
                                    errors={errors}
                                    readOnly
                                />
                                <TextField
                                    label="Password"
                                    required
                                    autoFocus={isEditingPassword}
                                    id="password"
                                    className="w-full"
                                    type="password"
                                    message="*Password is required"
                                    placeholder="Enter your Password"
                                    register={register}
                                    errors={errors}
                                    readOnly={!isEditingPassword}
                                    min={6}
                                />{" "}
                                {!isEditingPassword ? (
                                    <button
                                        type="button"
                                        onClick={() =>
                                            setIsEditingPassword(!isEditingPassword)
                                        }
                                        className="bg-customRed mb-0 w-fit px-4 py-2 rounded-md text-white"
                                    >
                                        Click To Edit Password
                                    </button>
                                ) : (
                                    <div className="flex items-center gap-2 ">
                                        <button
                                            type="submit"
                                            className="bg-btnColor mb-0 w-fit px-4 py-2 rounded-md text-white"
                                        >
                                            {passwordLoader ? "Loading.." : "Save"}
                                        </button>
                                        <button
                                            type="button"
                                            onClick={() =>
                                                setIsEditingPassword(!isEditingPassword)
                                            }
                                            className="bg-customRed mb-0 w-fit px-4 py-2 rounded-md text-white"
                                        >
                                            Cancel
                                        </button>
                                    </div>
                                )}
                            </form>
                        </div>
                    </div>
                    <div className="lg:w-[70%] sm:w-[90%] w-full  mx-auto shadow-lg shadow-gray-300 p-8 rounded-md">
                        <h1 className="text-slate-800 text-2xl font-bold  pb-4">
                            Admin Actions
                            <hr />
                        </h1>

                        <div className="py-4 flex sm:flex-row flex-col sm:items-center items-start gap-4">
                            <div className="flex items-center gap-2">
                                <label className="text-slate-600 text-lg font-semibold ">
                                    Role:{" "}
                                </label>
                                <select
                                    className=" px-8 py-1 rounded-md  border-2 uppercase border-slate-600  "
                                    value={selectedRole}
                                    onChange={handleRoleChange}
                                >
                                    {roles.map((role) => (
                                        <option
                                            className="bg-slate-200 flex flex-col gap-4 uppercase text-slate-700"
                                            key={role.roleId}
                                            value={role.roleName}
                                        >
                                            {role.roleName}
                                        </option>
                                    ))}
                                </select>
                            </div>
                            <button
                                className="bg-btnColor hover:text-slate-300 px-4 py-2 rounded-md text-white "
                                onClick={handleUpdateRole}
                            >
                                {updateRoleLoader ? "Loading..." : "Update Role"}
                            </button>
                        </div>

                        <hr className="py-2" />
                        <div className="flex flex-col gap-4 py-4">
                            <div className="flex items-center gap-2">
                                <label className="text-slate-600 text-sm font-semibold uppercase">
                                    {" "}
                                    Lock Account
                                </label>
                                <input
                                    className="text-14 w-5 h-5"
                                    type="checkbox"
                                    name="lock"
                                    checked={!user?.accountNonLocked}
                                    onChange={(e) =>
                                        handleCheckboxChange(e, "/admin/update-lock-status")
                                    }
                                />
                            </div>
                            <div className="flex items-center gap-2">
                                <label className="text-slate-600 text-sm font-semibold uppercase">
                                    {" "}
                                    Account Expiry
                                </label>
                                <input
                                    className="text-14 w-5 h-5"
                                    type="checkbox"
                                    name="expire"
                                    checked={!user?.accountNonExpired}
                                    onChange={(e) =>
                                        handleCheckboxChange(e, "/admin/update-expiry-status")
                                    }
                                />
                            </div>
                            <div className="flex items-center gap-2">
                                <label className="text-slate-600 text-sm font-semibold uppercase">
                                    {" "}
                                    Account Enabled
                                </label>
                                <input
                                    className="text-14 w-5 h-5"
                                    type="checkbox"
                                    name="enabled"
                                    checked={user?.enabled}
                                    onChange={(e) =>
                                        handleCheckboxChange(e, "/admin/update-enabled-status")
                                    }
                                />
                            </div>
                            <div className="flex items-center gap-2">
                                <label className="text-slate-600 text-sm font-semibold uppercase">
                                    {" "}
                                    Credentials Expired
                                </label>
                                <input
                                    className="text-14 w-5 h-5"
                                    type="checkbox"
                                    name="credentialsExpire"
                                    checked={!user?.credentialsNonExpired}
                                    onChange={(e) =>
                                        handleCheckboxChange(
                                            e,
                                            `/admin/update-credentials-expiry-status?userId=${userId}&expire=${user?.credentialsNonExpired}`
                                        )
                                    }
                                />
                            </div>
                        </div>
                    </div>
                </>
            )}
        </div>
    );
};

export default UserDetails;
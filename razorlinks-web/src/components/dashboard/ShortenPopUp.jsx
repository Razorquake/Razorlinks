import {Modal} from "@mui/material";
import CreateNewShorten from "./CreateNewShorten.jsx";

const ShortenPopUp = ({open, setOpen, refetch}) => {
    const handleClose = () => {
        setOpen(false);
    };
    return (
        <Modal open={open}
               onClose = {handleClose}
               aria-labelledby="modal-modal-title"
               aria-describedby="modal-modal-description"
               >
            <div className="flex justify-center items-center h-full w-full">
                <CreateNewShorten setOpen={setOpen} refetch={refetch} />
            </div>
        </Modal>
    );
};

export default ShortenPopUp;
import {FadeLoader} from "react-spinners";

function Loader() {
    return (
        <div className="flex justify-center items-center w-full h-[450px]">
            <div className="flex flex-col items-center gap-1">
                <FadeLoader
                    height={20}
                    width={5}
                    color="red"
                    loading={true}
                    radius={5}
                    speedMultiplier={0.75}
                />
            </div>
        </div>
    )
}

export default Loader;
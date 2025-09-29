import {FadeLoader} from "react-spinners";

function Loader() {
    return (
        <div className="flex justify-center items-center w-full h-[450px]">
            <div className="flex flex-col items-center gap-1">
                <FadeLoader
                    height={20}
                    width={5}
                    color="#4fa94d"
                    loading={true}
                    radius={5}
                    speedMultiplier={0.75}
                />
                <span className="text-slate-700 text-lg font-semibold">
                    Please wait...
                </span>
            </div>
        </div>
    )
}

export default Loader;
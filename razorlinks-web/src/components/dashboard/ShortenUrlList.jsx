import ShortenItem from "./ShortenItem.jsx";

const ShortenUrlList = ({ data, onRefetch }) => {

    return (
        <div className="my-6 space-y-4">
            {data.map((item)=>(
                <ShortenItem key={item.id}{...item} onUrlDeleted={onRefetch} />
            ))}
        </div>
    )
}
export default ShortenUrlList;
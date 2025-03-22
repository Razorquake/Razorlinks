import React from 'react';

const LandingPage = () => {
    let desc =
        "Generate short, memorable links with ease using Razorlinks’s intuitive interface. Share URLs effortlessly across platforms. Optimize your sharing strategy with Razorlinks. Track clicks and manage your links seamlessly to enhance your online presence. Generate short, memorable links with ease using Razorlinks’s intuitive interface. Share URLs effortlessly across platforms.";
    return (
        <div className="min-h-[calc(100vh-64px)]  lg:px-14 sm:px-8 px-4">
            <div className="lg:flex-row flex-col lg:py-5 pt-16 lg:gap-10 gap-8 flex justify-between items-center">
                <div className=" flex-1">
                    <h1 className="font-bold font-roboto text-slate-800 md:text-5xl text-3xl md:leading-[55px] sm:leading-[45px] leading-10 lg:w-full md:w-[70%] w-full">
                        Razorlinks Simplifies URL Shortening for Efficient Sharing
                    </h1>
                </div>
            </div>
        </div>
    );
}

export default LandingPage;
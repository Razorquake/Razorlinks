import React from "react";
import { FaFacebook, FaTwitter, FaInstagram, FaLinkedin } from "react-icons/fa";
import {FaDiscord, FaReddit, FaX, FaXTwitter} from "react-icons/fa6";

const Footer = () => {
    return (
        <footer className="bg-black text-white py-8 z-40 relative">
            <div
                className="container mx-auto px-6 lg:px-14 flex flex-col lg:flex-row lg:justify-between items-center gap-4">
                <div className="text-center lg:text-left">
                    <h2 className="text-3xl font-bold mb-2">Razorlinks</h2>
                    <p>Simplifying URL shortening for efficient sharing</p>
                </div>

                <p className="mt-4 lg:mt-0">
                    &copy; 2024 Razorlinks. All rights reserved.
                </p>

                <div className="flex space-x-6 mt-4 lg:mt-0">
                    <a href="#" className="hover:text-gray-200">
                        <FaReddit size={24}/>
                    </a>
                    <a href="#" className="hover:text-gray-200">
                        <FaXTwitter size={24}/>
                    </a>
                    <a href="#" className="hover:text-gray-200">
                        <FaDiscord size={24}/>
                    </a>
                    <a href="#" className="hover:text-gray-200">
                        <FaLinkedin size={24}/>
                    </a>
                </div>
            </div>
        </footer>
    );
};

export default Footer;
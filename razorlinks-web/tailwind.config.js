/** @type {import('tailwindcss').Config} */
export default {
  content: ["./index.html", "./src/**/*.{js,ts,jsx,tsx}"],
  theme: {
    extend: {
      backgroundImage: {
        "custom-gradient": "linear-gradient(to right, #3b82f6, #9333ea)", // equivalent to from-blue-500 to-purple-600
        "custom-gradient-2": "linear-gradient(to left, #3b82f6, #f43f5e)",
        "card-gradient": "linear-gradient(to right, #38b2ac, #4299e1)",
      },
      colors: {
          headerColor: "#242530",
          textColor: "#ffffff",
        navbarColor: "#ffffff",
        btnColor: "#3364F7",
        linkColor: "#2a5bd7",
      },
        fontWeight: {
            customWeight: 500,
        },
        height: {
            headerHeight: "74px",
        },
        maxHeight: {
            navbarHeight: "420px",
        },
        minHeight: {
            customHeight: "530px",
        },
      boxShadow: {
        custom: "0 0 15px rgba(0, 0, 0, 0.3)",
        right: "10px 0px 10px -5px rgba(0, 0, 0, 0.3)",
      },
      fontFamily: {
        roboto: ["Roboto", "sans-serif"],
          dancingScript: ["Dancing Script"],
        montserrat: ["Montserrat"],
      },
        fontSize: {
            logoText: "30px",
            customText: "15px",
            tablehHeaderText: "16px",
            headerText: ["50px", "60px"],
            tableHeader: ["15px", "25px"],
        },
        backgroundColor: {
            testimonialCard: "#F9F9F9",
        },
    },
  },

  variants: {
    extend: {
      backgroundImage: ["responsive"],
    },
  },

  plugins: [],
};
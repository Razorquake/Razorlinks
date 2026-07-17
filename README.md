# 🔗 Razorlinks

<div align="center">

<img src="razorlinks-web/public/images/img.svg" alt="Razorlinks Logo" style="width: 400px; height: auto;" />

**A modern, full-stack URL shortening service with powerful analytics and QR code generation**

[![Live Demo](https://custom-icon-badges.demolab.com/badge/Live%20Demo-razorlinks.dev-FF9900?logo=aws&logoColor=white)](https://razorlinks.dev)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://github.com/Razorquake/Razorlinks/blob/master/LICENSE)
[![Java](https://img.shields.io/badge/Java-21-orange?logo=openjdk)](https://www.oracle.com/java/)
[![Node.js](https://img.shields.io/badge/Node.js-22.14.0-green?logo=node.js)](https://nodejs.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.x-green?logo=spring-boot)](https://spring.io/projects/spring-boot)
[![Spring](https://img.shields.io/badge/Spring-7.x-brightgreen?logo=spring)](https://spring.io/projects/spring)
[![React](https://img.shields.io/badge/React-18.x-61DAFB?logo=react)](https://reactjs.org/)
[![Hacktoberfest](https://img.shields.io/badge/Hacktoberfest-Accepted-ff69b4)](https://hacktoberfest.com/)
[![Deploy Backend to Elastic Beanstalk](https://github.com/Razorquake/Razorlinks/actions/workflows/deploy-backend.yml/badge.svg)](https://github.com/Razorquake/Razorlinks/actions/workflows/deploy-backend.yml)

</div>

---

## 🌟 Overview

Razorlinks simplifies URL shortening for efficient sharing. Easily generate, manage, and track your shortened links with our intuitive interface and comprehensive analytics dashboard. With features like QR code generation and click tracking, Razorlinks streamlines the process of sharing and monitoring your links.

> ## Deployment Status
> ⚠️ This project is currently **not deployed**.
>
> The previous AWS deployment was discontinued after the free tier expired, so the app is currently available for **local development/use only**.
>
> If you want to deploy it on AWS yourself, please follow the guide in [`AWS.md`](./AWS.md).

---

## ✨ Features

### 🔗 Core Functionality
- **Simple URL Shortening** - Create short, memorable URLs in just a few clicks
- **Custom Short URLs** - Personalize your shortened links with custom aliases
- **Link Management** - Organize and manage all your shortened URLs in one place
- **QR Code Generation** - Automatically generate QR codes for easy sharing of your shortened links

### 📊 Analytics & Tracking
- **Click Tracking** - Monitor link performance with timestamp-based click analytics
- **Visual Analytics Dashboard** - Interactive charts and graphs to visualize your link performance
- **Date Range Analytics** - View click data for specific time periods
- **Total Clicks Monitoring** - Track cumulative clicks across all your links

### 👥 User Management
- **User Authentication** - Secure login and registration system with Spring Security
- **Email Verification** - Verify user accounts via email with beautiful templates powered by jte
- **Password Reset** - Secure password recovery functionality with email notifications
- **Admin Dashboard** - Comprehensive admin panel for user and link management
- **Audit Logs** - Track user activities and system events

### 🎨 User Experience
- **Responsive Design** - Seamless experience across desktop, tablet, and mobile devices
- **Subdomain Routing** - Smart routing based on subdomains for different app experiences
- **Real-time Updates** - Instant feedback with React Query for data fetching and caching
- **Smooth Animations** - Enhanced UI with Framer Motion animations

---

## 🏗️ Architecture

### Tech Stack

#### Backend
- **Language:** Java 21
- **Framework:** Spring Boot 4.x
- **Security:** Spring Security with JWT authentication
- **Database:** PostgreSQL (via Spring Data JPA)
- **Task Scheduling:** Spring Scheduling (`@EnableScheduling`)
- **Build Tool:** Gradle
- **Email Templates:** jte (Java Template Engine)

#### Frontend
- **Runtime:** Node.js 22.14.0
- **Framework:** React 18.x
- **Routing:** React Router DOM
- **State Management:** Context API
- **Data Fetching:** TanStack React Query (formerly React Query)
- **Forms:** React Hook Form
- **Styling:** Tailwind CSS
- **UI Components:** Material-UI (MUI)
- **Icons:** React Icons
- **Animations:** Framer Motion
- **Notifications:** React Hot Toast
- **Data Visualization:** Custom charts for analytics
- **Data Grid:** MUI DataGrid for tabular data
- **Build Tool:** Vite

---

## ☁️ AWS Infrastructure

Razorlinks is fully hosted on AWS with a production-ready architecture:

### Network Layer
- **VPC:** Custom Virtual Private Cloud with isolated network
- **Subnets:** Public and private subnets across multiple availability zones
- **Route Tables:** Custom routing for secure network traffic management

### Application Layer
- **Backend:** AWS Elastic Beanstalk + CloudFront CDN
  - Elastic Beanstalk for application hosting and auto-scaling
  - CloudFront for global content delivery and reduced latency
- **Frontend:** AWS Amplify
  - Automated deployments connected to GitHub repository
  - Built-in CI/CD pipeline for frontend changes

### Database Layer
- **Database:** Amazon RDS (PostgreSQL)
  - Hosted in private subnet group
  - Only accessible by Elastic Beanstalk application
  - Secure, isolated database environment

### Security & SSL
- **ACM (AWS Certificate Manager):** Domain certificates for HTTPS
- **Private Network:** Database isolated in private subnets

---

## 🚀 CI/CD Pipeline

### Backend Deployment (GitHub Actions)
Automated deployment pipeline for backend to AWS Elastic Beanstalk:

- **Trigger:** Push to `master` branch (only when backend files change)
- **Authentication:** AWS OIDC for secure, keyless authentication
- **Build Process:**
  1. Checkout code
  2. Configure AWS credentials via OIDC
  3. Set up JDK 21
  4. Build with Gradle (skip tests)
  5. Generate version label with timestamp
  6. Deploy JAR to Elastic Beanstalk
  7. Wait for deployment completion

- **Configuration File:** `.github/workflows/deploy-backend.yml`
- **Target:** Elastic Beanstalk environment in `ap-south-1` region

### Frontend Deployment (AWS Amplify)
- **Trigger:** Push to `master` branch (frontend changes)
- **Process:** Automatically handled by AWS Amplify's built-in CI/CD
- **Connected Repository:** Direct GitHub integration

---

## 🛠️ Local Development Setup

### Prerequisites
- Java 21 or higher
- Node.js 22.14.0 or higher
- PostgreSQL database
- Gradle (or use the included Gradle wrapper)

### Backend Setup

1. **Clone the repository**
   ```bash
   git clone https://github.com/Razorquake/Razorlinks.git
   cd Razorlinks/razorlinks
   ```

2. **Configure application properties**
   ```bash
   # Create application.properties or application.yml
   # Configure database connection, JWT secret, email settings, etc.
   ```

3. **Build and run**
   ```bash
   ./gradlew clean build
   ./gradlew bootRun
   ```

   Or run directly:
   ```bash
   java -jar build/libs/razorlinks-0.0.1-SNAPSHOT.jar
   ```

### Frontend Setup

1. **Navigate to frontend directory**
   ```bash
   cd razorlinks-web
   ```

2. **Install dependencies**
   ```bash
   npm install
   ```

3. **Configure environment variables**
   ```bash
   # Create .env file with API endpoint configuration
   ```

4. **Run development server**
   ```bash
   npm run dev
   ```

5. **Build for production**
   ```bash
   npm run build
   ```

---

## 📁 Project Structure

```
Razorlinks/
├── razorlinks/                    # Backend (Spring Boot)
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   │   └── com/razorquake/razorlinks/
│   │   │   │       ├── RazorlinksApplication.java
│   │   │   │       ├── controller/
│   │   │   │       ├── service/
│   │   │   │       ├── repository/
│   │   │   │       ├── model/
│   │   │   │       ├── config/
│   │   │   │       └── security/
│   │   │   └── resources/
│   │   │       └── application.properties
│   │   └── test/
│   ├── build.gradle
│   └── gradlew
├── razorlinks-web/               # Frontend (React + Vite)
│   ├── src/
│   │   ├── components/
│   │   │   ├── auth/
│   │   │   ├── dashboard/
│   │   │   ├── auditlogs/
│   │   │   ├── LandingPage.jsx
│   │   │   ├── AboutPage.jsx
│   │   │   └── ...
│   │   ├── hooks/
│   │   ├── services/
│   │   │   └── api.js
│   │   ├── store/
│   │   │   └── ContextApi.jsx
│   │   ├── utils/
│   │   ├── App.jsx
│   │   └── main.jsx
│   ├── public/
│   ├── package.json
│   └── vite.config.js
├── .github/
│   └── workflows/
│       └── deploy-backend.yml    # Backend CI/CD pipeline
└── README.md
```

---

## 🔒 Security Features

- **JWT Authentication:** Secure token-based authentication
- **Spring Security:** Comprehensive security configuration
- **Email Verification:** Account verification via email
- **Password Encryption:** Secure password hashing
- **Private Database:** RDS in private subnet, not publicly accessible
- **HTTPS:** SSL/TLS certificates via AWS ACM
- **OIDC Authentication:** Keyless AWS authentication in CI/CD

---

## 🤝 Contributing

We welcome contributions! This project participates in **Hacktoberfest**.

### How to Contribute

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

### Topics
- `hacktoberfest`
- `hacktoberfest-accepted`
- `java`
- `spring-boot`
- `spring-data-jpa`
- `spring-security`

---

## 📝 API Endpoints

### Public Endpoints
- `POST /auth/public/register` - User registration
- `POST /auth/public/login` - User login
- `POST /auth/public/resend-verification` - Resend verification email
- `POST /auth/public/reset-password` - Reset password

### Protected Endpoints
- `POST /urls/shorten` - Create shortened URL
- `GET /urls/myurls` - Get user's shortened URLs
- `GET /urls/totalClicks` - Get click analytics with date range

### Admin Endpoints
- `GET /admin/get-users` - Get all users
- `GET /admin/users/{id}` - Get user details

---

## 📊 Analytics Capabilities

Currently, Razorlinks provides:
- ✅ **Click Tracking** - Track every click with timestamp
- ✅ **Time-based Analytics** - View clicks over custom date ranges
- ✅ **Visual Charts** - Interactive graphs for data visualization
- ✅ **Total Click Counts** - Aggregate click statistics

**Note:** Geographical data and referral source tracking are not currently implemented but may be added in future updates.

---

## 🌐 Subdomain Routing

The application supports intelligent subdomain routing:
- **Main App (`www`):** Landing page, about page, authentication
- **URL Subdomain (`url`):** Dedicated subdomain for URL management and analytics

---

## 📜 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

### MIT License Summary

Copyright (c) 2025 Anant Jaiswal

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED.

---

## 👨‍💻 Author

**Razorquake**
- GitHub: [@Razorquake](https://github.com/Razorquake)
- Website: [razorlinks.dev](https://razorlinks.dev)

---

## 🙏 Acknowledgments

- Spring Boot team for the excellent framework
- React team for the powerful UI library
- AWS for reliable cloud infrastructure
- All contributors who help improve Razorlinks

---

<div align="center">

**Made with ❤️ by Razorquake**

⭐ Star this repository if you find it helpful!

</div>

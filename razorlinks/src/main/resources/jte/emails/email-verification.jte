@param String verificationUrl
@param String username

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Email Verification - RazorLinks</title>
    <style>
        body {
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
            line-height: 1.6;
            color: #333;
            max-width: 600px;
            margin: 0 auto;
            padding: 20px;
            background-color: #f5f5f5;
        }
        .container {
            background-color: #ffffff;
            border-radius: 10px;
            padding: 30px;
            box-shadow: 0 2px 10px rgba(0,0,0,0.1);
        }
        .header {
            text-align: center;
            margin-bottom: 30px;
            border-bottom: 3px solid #16a34a;
            padding-bottom: 20px;
        }
        .logo {
            font-size: 32px;
            font-weight: bold;
            color: #2563eb;
            margin-bottom: 10px;
        }
        .welcome-badge {
            background: linear-gradient(135deg, #16a34a 0%, #15803d 100%);
            color: white;
            padding: 5px 15px;
            border-radius: 20px;
            font-size: 14px;
            display: inline-block;
            margin-top: 10px;
        }
        .content {
            padding: 20px 0;
        }
        .button {
            display: inline-block;
            padding: 14px 35px;
            background: linear-gradient(135deg, #16a34a 0%, #15803d 100%);
            color: white !important;
            text-decoration: none;
            border-radius: 8px;
            margin: 25px 0;
            font-weight: 600;
            box-shadow: 0 4px 6px rgba(22, 163, 74, 0.3);
            transition: all 0.3s ease;
        }
        .button-container {
            text-align: center;
        }
        .info-box {
            background-color: #dbeafe;
            border-left: 4px solid #2563eb;
            padding: 15px;
            margin: 25px 0;
            border-radius: 4px;
        }
        .info-box strong {
            color: #1e40af;
        }
        .info-box ul {
            margin: 10px 0 5px 0;
            padding-left: 20px;
        }
        .info-box li {
            margin: 5px 0;
            color: #1e3a8a;
        }
        .features {
            background-color: #f3f4f6;
            padding: 20px;
            border-radius: 8px;
            margin: 25px 0;
        }
        .features h3 {
            color: #1f2937;
            margin-top: 0;
            font-size: 16px;
        }
        .features ul {
            margin: 10px 0;
            padding-left: 20px;
        }
        .features li {
            margin: 8px 0;
            color: #4b5563;
        }
        .footer {
            text-align: center;
            margin-top: 40px;
            padding-top: 20px;
            border-top: 1px solid #e5e7eb;
            font-size: 12px;
            color: #6b7280;
        }
        .footer a {
            color: #2563eb;
            text-decoration: none;
        }
        .url-box {
            background-color: #f3f4f6;
            padding: 12px;
            border-radius: 6px;
            word-break: break-all;
            font-size: 11px;
            color: #2563eb;
            margin-top: 15px;
        }
        h2 {
            color: #1f2937;
            margin-top: 0;
        }
        p {
            color: #4b5563;
            margin: 15px 0;
        }
        .emoji {
            font-size: 24px;
            margin-right: 8px;
        }
    </style>
</head>
<body>
<div class="container">
    <div class="header">
        <div class="logo">🔗 RazorLinks</div>
        <div class="welcome-badge">Welcome to RazorLinks!</div>
    </div>

    <div class="content">
        <h2><span class="emoji">🎉</span>Hello ${username}!</h2>

        <p>Thank you for signing up with RazorLinks! We're excited to have you join our community of smart link managers.</p>

        <p>To get started and access all features, please verify your email address by clicking the button below:</p>

        <div class="button-container">
            <a href="${verificationUrl}" class="button">Verify Email Address</a>
        </div>

        <div class="features">
            <h3><span class="emoji">🚀</span>What you can do with RazorLinks:</h3>
            <ul>
                <li>✨ Create custom short URLs instantly</li>
                <li>📊 Track click analytics and insights in real-time</li>
                <li>📱 Generate QR codes for easy sharing</li>
                <li>🎯 Manage all your links in one centralized dashboard</li>
                <li>🔐 Secure authentication with 2FA support</li>
                <li>🌍 Access your links from anywhere</li>
            </ul>
        </div>

        <div class="info-box">
            <strong>📝 Important Information:</strong>
            <ul>
                <li>This verification link expires in <strong>24 hours</strong></li>
                <li>You need to verify your email before accessing your dashboard</li>
                <li>Keep your account secure with our 2FA feature</li>
            </ul>
        </div>

        <p style="margin-top: 25px; font-size: 13px; color: #6b7280;">
            If the button doesn't work, copy and paste this link into your browser:
        </p>
        <div class="url-box">${verificationUrl}</div>

        <p style="margin-top: 25px; font-size: 13px; color: #9ca3af;">
            If you didn't create an account with RazorLinks, you can safely ignore this email.
        </p>
    </div>

    <div class="footer">
        <p style="margin: 5px 0;">© 2025 RazorLinks. All rights reserved.</p>
        <p style="margin: 5px 0;">This is an automated email, please do not reply.</p>
        <p style="margin: 15px 0 5px 0;">
            <a href="https://razorlinks.dev">Visit Website</a>
        </p>
    </div>
</div>
</body>
</html>
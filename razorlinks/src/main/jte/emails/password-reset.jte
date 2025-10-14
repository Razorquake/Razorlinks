@param String resetUrl
@param String username

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Password Reset - RazorLinks</title>
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
            border-bottom: 3px solid #2563eb;
            padding-bottom: 20px;
        }
        .logo {
            font-size: 32px;
            font-weight: bold;
            color: #2563eb;
            margin-bottom: 10px;
        }
        .content {
            padding: 20px 0;
        }
        .button {
            display: inline-block;
            padding: 14px 35px;
            background: linear-gradient(135deg, #2563eb 0%, #1d4ed8 100%);
            color: white !important;
            text-decoration: none;
            border-radius: 8px;
            margin: 25px 0;
            font-weight: 600;
            box-shadow: 0 4px 6px rgba(37, 99, 235, 0.3);
            transition: all 0.3s ease;
        }
        .button-container {
            text-align: center;
        }
        .warning {
            background-color: #fef3c7;
            border-left: 4px solid #f59e0b;
            padding: 15px;
            margin: 25px 0;
            border-radius: 4px;
        }
        .warning strong {
            color: #92400e;
        }
        .warning ul {
            margin: 10px 0 5px 0;
            padding-left: 20px;
        }
        .warning li {
            margin: 5px 0;
            color: #78350f;
        }
        .footer {
            text-align: center;
            margin-top: 40px;
            padding-top: 20px;
            border-top: 1px solid #e5e7eb;
            font-size: 12px;
            color: #6b7280;
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
    </style>
</head>
<body>
<div class="container">
    <div class="header">
        <div class="logo">🔗 RazorLinks</div>
        <p style="color: #6b7280; margin: 5px 0;">Password Reset Request</p>
    </div>

    <div class="content">
        <h2>Hello ${username}!</h2>

        <p>We received a request to reset your password for your RazorLinks account.</p>

        <p>Click the button below to create a new password:</p>

        <div class="button-container">
            <a href="${resetUrl}" class="button">Reset Password</a>
        </div>

        <div class="warning">
            <strong>⚠️ Security Notice:</strong>
            <ul>
                <li>This link will expire in <strong>1 hour</strong></li>
                <li>If you didn't request this, please ignore this email</li>
                <li>Never share this link with anyone</li>
                <li>Our team will never ask for your password</li>
            </ul>
        </div>

        <p style="margin-top: 25px; font-size: 13px; color: #6b7280;">
            If the button doesn't work, copy and paste this link into your browser:
        </p>
        <div class="url-box">${resetUrl}</div>
    </div>

    <div class="footer">
        <p style="margin: 5px 0;">© 2025 RazorLinks. All rights reserved.</p>
        <p style="margin: 5px 0;">This is an automated email, please do not reply.</p>
    </div>
</div>
</body>
</html>
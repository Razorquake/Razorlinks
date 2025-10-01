package com.razorquake.razorlinks.service;

import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import com.warrenstrange.googleauth.GoogleAuthenticatorQRGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TotpService {

    private final GoogleAuthenticator googleAuthenticator;

    public TotpService() {
        googleAuthenticator = new GoogleAuthenticator();
    }

    public GoogleAuthenticatorKey generateSecret() {
        return googleAuthenticator.createCredentials();
    }

    public String getQrCodeUrl(GoogleAuthenticatorKey secret, String username) {
        return GoogleAuthenticatorQRGenerator.getOtpAuthURL(
                "Razorlinks",
                username,
                secret
        );
    }

    public boolean verifyCode(String secret, int code) {
        return googleAuthenticator.authorize(secret, code);
    }

}

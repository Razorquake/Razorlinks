package com.razorquake.razorlinks.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.DefaultCsrfToken;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CsrfControllerTest {

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private CsrfController csrfController;

    @Test
    void csrfToken_ReturnsTokenFromRequest() {
        CsrfToken token = new DefaultCsrfToken("X-CSRF-TOKEN", "_csrf", "token-value");
        when(request.getAttribute(CsrfToken.class.getName())).thenReturn(token);

        CsrfToken result = csrfController.csrfToken(request);

        assertThat(result).isSameAs(token);
    }
}

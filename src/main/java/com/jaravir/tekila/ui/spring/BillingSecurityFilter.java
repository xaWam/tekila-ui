package com.jaravir.tekila.ui.spring;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import spring.security.Constants;
import spring.security.wrapper.Holder;
import spring.service.UserHolder;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by KamranMa on 26.12.2017.
 */
@Component
public class BillingSecurityFilter extends OncePerRequestFilter {
    private static final Logger log = Logger.getLogger(BillingSecurityFilter.class);
    @Autowired
    private HttpServletRequest httpServletRequest;

    @Autowired
    private TokenProvider tokenProvider;

    private String resolveToken(HttpServletRequest request) {

        String bearerToken = request.getHeader(Constants.AUTHORIZATION_HEADER);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7, bearerToken.length());
        }
        String jwt = request.getParameter(Constants.AUTHORIZATION_TOKEN);
        if (StringUtils.hasText(jwt)) {
            return jwt;
        }

        return null;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest servletRequest,
            HttpServletResponse servletResponse,
            FilterChain filterChain) throws ServletException, IOException {
        String requestUri = servletRequest.getRequestURI();

        String jwt = resolveToken(servletRequest);

        if (StringUtils.hasText(jwt) && this.tokenProvider.validateToken(jwt) && !requestUri.contains("remember") && !requestUri.contains("clone-to-security")  && !requestUri.contains("tekila-jobs")) {
            Authentication authentication = this.tokenProvider.getAuthentication(jwt);
            SecurityContextHolder.getContext().setAuthentication(authentication);
            Holder securityHolder = null;
            try {
                securityHolder = (Holder) authentication.getPrincipal();
                servletRequest.login(securityHolder.getUser().getLogin(), UserHolder.credentials.get(securityHolder.getUser().getLogin()));
            } catch (Exception e) {
                throw new SecurityException("username or password were incorrect, username = " + securityHolder.getUser().getLogin() + "" +
                        ", password = " + UserHolder.credentials.get(securityHolder.getUser().getLogin()));
            }

        }


        log.info(String.format("Uri on SpringFilter = %s", requestUri));

        if (true || SecurityContextHolder.getContext().getAuthentication() != null
                && SecurityContextHolder.getContext().getAuthentication().isAuthenticated()) {
            servletRequest.getRequestDispatcher(requestUri).forward(servletRequest, servletResponse);
        } else {
            filterChain.doFilter(servletRequest, servletResponse);
        }
    }
}

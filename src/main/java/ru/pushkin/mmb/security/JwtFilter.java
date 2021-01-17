package ru.pushkin.mmb.security;


import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;


@Component
@RequiredArgsConstructor
@Slf4j
public class JwtFilter extends GenericFilterBean {

    private final JwtTokenProvider jwtTokenProvider;


    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {
        logger.debug("Do JWT filter...");
        String token = JwtUtil.resolveToken(request);
        try {
            SecurityContextHolder.getContext().setAuthentication(jwtTokenProvider.getAuthentication(token));
        } catch (ExpiredJwtException e) {
            log.error("Token expired", e);
            throw new AccessDeniedException(e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.error("Unsupported jwt", e);
            return;
        } catch (MalformedJwtException e) {
            log.error("Malformed jwt", e);
            return;
        } catch (Exception e) {
            log.error("invalid token", e);
            return;
        }

        filterChain.doFilter(request, response);
    }
}

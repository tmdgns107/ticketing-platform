package com.ticketing.global.security;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import com.ticketing.global.error.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Reads a {@code Bearer} access token and, on success, populates the SecurityContext with a
 * {@link LoginMember} principal. A missing token is left for downstream authorization to reject;
 * a present-but-invalid token is rejected immediately via the entry point (401 envelope).
 *
 * <p>Not a {@code @Component} on purpose — it is wired into the security chain by
 * {@link SecurityConfig}, and auto-registration would also add it to the plain servlet chain.
 */
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtTokenProvider tokenProvider;
    private final RestAuthenticationEntryPoint entryPoint;

    public JwtAuthenticationFilter(JwtTokenProvider tokenProvider, RestAuthenticationEntryPoint entryPoint) {
        this.tokenProvider = tokenProvider;
        this.entryPoint = entryPoint;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String token = resolveToken(request);
        if (token != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                LoginMember principal = tokenProvider.parseAccessToken(token);
                var authorities = List.of(new SimpleGrantedAuthority(principal.role().authority()));
                var authentication =
                        new UsernamePasswordAuthenticationToken(principal, null, authorities);
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (JwtException | IllegalArgumentException e) {
                log.debug("rejecting invalid jwt: {}", e.getMessage());
                SecurityContextHolder.clearContext();
                entryPoint.writeError(response, ErrorCode.INVALID_TOKEN);
                return;
            }
        }
        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header != null && header.startsWith(BEARER_PREFIX)) {
            return header.substring(BEARER_PREFIX.length()).trim();
        }
        return null;
    }
}

package com.hc.application.security;

import java.io.IOException;
import java.util.Arrays;
import java.util.Set;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    public static final String SESSION_COOKIE = "__Host-huellitas-session";
    public static final String BROWSER_HEADER = "X-Huellitas-Request";
    private static final Set<String> SAFE_METHODS = Set.of("GET", "HEAD", "OPTIONS");

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        Cookie[] cookies = request.getCookies();
        String cookieToken = cookies == null ? null : Arrays.stream(cookies)
                .filter(cookie -> SESSION_COOKIE.equals(cookie.getName()))
                .map(Cookie::getValue).findFirst().orElse(null);
        // A cross-origin browser must pass the CORS preflight to send this header.
        if (!SAFE_METHODS.contains(request.getMethod())
                && cookieToken != null
                && !"1".equals(request.getHeader(BROWSER_HEADER))) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Falta la cabecera de seguridad.");
            return;
        }

        String header = request.getHeader("Authorization");
        String token = header != null && header.startsWith("Bearer ") ? header.substring(7) : cookieToken;
        if (token == null) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String email = jwtService.extraerUsername(token);

            if (SecurityContextHolder.getContext()
                    .getAuthentication() == null) {

                UserDetails usuario =
                        userDetailsService.loadUserByUsername(email);

                if (jwtService.esValido(token, usuario)) {

                    UsernamePasswordAuthenticationToken autenticacion =
                            new UsernamePasswordAuthenticationToken(
                                    usuario,
                                    null,
                                    usuario.getAuthorities()
                            );

                    autenticacion.setDetails(
                            new WebAuthenticationDetailsSource()
                                    .buildDetails(request)
                    );

                    SecurityContextHolder.getContext()
                            .setAuthentication(autenticacion);
                }
            }

        } catch (JwtException | IllegalArgumentException | UsernameNotFoundException e) {
            // Token inválido o expirado; la solicitud continuará sin autenticar
        }

        filterChain.doFilter(request, response);
    }
}
package com.example.erpserver.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.stream.Collectors;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    public JwtAuthFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            final String token = authHeader.substring(7);

            try {
                if (jwtUtil.tokenValido(token)) {

                    String email = jwtUtil.extrairEmail(token);
                    var roles = jwtUtil.extrairRoles(token).stream()
                            .map(r -> new SimpleGrantedAuthority("ROLE_" + r))
                            .collect(Collectors.toList());

                    var authToken = new UsernamePasswordAuthenticationToken(email, null, roles);
                    SecurityContextHolder.getContext().setAuthentication(authToken);

                } else {

                    SecurityContextHolder.clearContext();
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401
                    response.getWriter().write("Token expirado ou inválido");
                    return;
                }
            } catch (Exception e) {

                SecurityContextHolder.clearContext();
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Token inválido");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}

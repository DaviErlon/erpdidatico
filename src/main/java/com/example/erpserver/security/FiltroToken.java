package com.example.erpserver.security;

import com.example.erpserver.services.Autenticador;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class FiltroToken extends OncePerRequestFilter {

    private final Autenticador autenticador;

    public FiltroToken(Autenticador autenticador) {
        this.autenticador = autenticador;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // Permitir login sem token
        String path = request.getRequestURI();
        if (path.equals("/api/login")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Para outras rotas, verificar token
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Token ausente ou inválido");
            return;
        }

        String token = authHeader.substring(7);

        if (!autenticador.verificarToken(token)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Token inválido ou expirado");
            return;
        }

        // Token válido → continua
        filterChain.doFilter(request, response);
    }
}

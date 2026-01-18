package nl.fontys.db3.backend.security;

import lombok.extern.slf4j.Slf4j;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import nl.fontys.db3.backend.entity.Device;
import nl.fontys.db3.backend.service.DeviceService;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.Optional;

/**
 * Filter to authenticate ESP32 devices using X-API-Key header
 * Allows devices to authenticate without user JWT tokens
 */
@Slf4j
@Component
@Order(0)
public class DeviceApiKeyAuthFilter extends OncePerRequestFilter {

    private final DeviceService deviceService;

    public DeviceApiKeyAuthFilter(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();
        String method = request.getMethod();
        
        // Only apply device API key authentication for POST/PUT requests to telemetry endpoints
        // GET requests should use JWT authentication instead
        boolean isTelemetryWriteEndpoint = (path.startsWith("/api/telemetry/live") || 
                                            path.startsWith("/api/telemetry/history")) &&
                                           ("POST".equals(method) || "PUT".equals(method));
        
        if (!isTelemetryWriteEndpoint) {
            filterChain.doFilter(request, response);
            return;
        }

        String apiKey = request.getHeader("X-API-Key");
        
        if (apiKey != null && !apiKey.isBlank()) {
            Optional<Device> device = deviceService.authenticateDevice(apiKey);
            
            if (device.isPresent()) {
                log.debug("Device API key authentication successful - deviceId: {}, path: {}", 
                        device.get().getDeviceId(), path);
                Authentication auth = new UsernamePasswordAuthenticationToken(
                    device.get().getDeviceId(),
                    null,
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_DEVICE"))
                );
                
                SecurityContextHolder.getContext().setAuthentication(auth);
                deviceService.updateLastSeen(device.get().getDeviceId());
            } else {
                log.warn("Device API key authentication failed - invalid API key, path: {}", path);
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("{\"error\":\"Invalid API key\"}");
                return;
            }
        } else {
            log.warn("Device API key authentication failed - missing X-API-Key header, path: {}", path);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"error\":\"X-API-Key header required\"}");
            return;
        }

        filterChain.doFilter(request, response);
    }
}

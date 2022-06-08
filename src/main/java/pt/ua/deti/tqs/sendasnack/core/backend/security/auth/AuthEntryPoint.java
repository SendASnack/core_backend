package pt.ua.deti.tqs.sendasnack.core.backend.security.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import pt.ua.deti.tqs.sendasnack.core.backend.requests.MessageResponse;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class AuthEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {

        ObjectMapper objectMapper = new ObjectMapper();

        MessageResponse messageResponse = new MessageResponse(authException.getMessage());

        response.addHeader("Content-Type", "application/json");

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.getWriter().println(objectMapper.writeValueAsString(messageResponse));

    }

}

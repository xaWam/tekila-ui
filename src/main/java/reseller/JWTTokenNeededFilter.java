package reseller;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.apache.log4j.Logger;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.util.Date;

@Provider
@JWTTokenNeeded
@Priority(Priorities.AUTHENTICATION)
public class JWTTokenNeededFilter implements ContainerRequestFilter {
    private final static Logger log = Logger.getLogger(JWTTokenNeededFilter.class);
    //@EJB
    //private KeyProvider keyProvider;

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        String token = null;
        try {
            // Get the HTTP Authorization header from the request
            String authorizationHeader = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);

            // Extract the token from the HTTP Authorization header
            token = authorizationHeader.substring("Bearer".length()).trim();
            log.info(String.format("JWTTokenNeededFilter.token = %s", token));

            // Validate the token
            //Key key = keyProvider.getKey();
            Claims claims = Jwts.parser().setSigningKey("secretkey1").parseClaimsJws(token).getBody();
            if (claims.getExpiration() == null || claims.getExpiration().before(new Date())) {
                throw new RuntimeException("Jwt Token is not set or has been expired");
            }
            log.info("#### valid token : " + token);

        } catch (Exception e) {
            log.error("#### invalid token : " + token, e);
            requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).build());
        }
    }
}
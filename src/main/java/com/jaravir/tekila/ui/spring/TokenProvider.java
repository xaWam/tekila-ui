package com.jaravir.tekila.ui.spring;

import spring.security.Constants;
import spring.security.wrapper.Holder;
import spring.security.wrapper.Subject;
import io.jsonwebtoken.*;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class TokenProvider {
    private static final Logger log = Logger.getLogger(TokenProvider.class);

    public Authentication getAuthentication(String token) {
        log.info("*************** authentication starts ****************** ");
        Claims claims = Jwts.parser()
                .setSigningKey(Constants.SECRET_KEY)
                .parseClaimsJws(token)
                .getBody();

        Map<String, Object> holderMap = claims;

        Collection<? extends GrantedAuthority> authorities =
                Arrays.stream(holderMap.get(Constants.AUTHORITIES_KEY).toString().split(","))
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());

        ObjectMapper mapper = new ObjectMapper();

        Subject user = mapper.convertValue(holderMap.get(Constants.SUBJECT), Subject.class);

        User principal = new User(user.getLogin().toString(), "", authorities);

        Holder holder = new Holder();

        holder.setSpringUser(principal);
        holder.setUser(user);

//        log.info(" ************************************* "+user.getLogin()+" "+user.getId()+" "+user.getLastName());

        return new UsernamePasswordAuthenticationToken(holder, token, authorities);
    }

    public boolean validateToken(String authToken) {
        try {
//            log.info("Token is : "+authToken);
            Jwts.parser().setSigningKey(Constants.SECRET_KEY).parseClaimsJws(authToken);
            return true;
        } catch (SignatureException e) {
            log.info("Invalid JWT signature.");
            log.trace("Invalid JWT signature trace: {}", e);
        } catch (MalformedJwtException e) {
            log.info("Invalid JWT token.");
            e.printStackTrace();
            log.trace("Invalid JWT token trace: {}", e);
        } catch (ExpiredJwtException e) {
            log.info("Expired JWT token.");
            log.trace("Expired JWT token trace: {}", e);
        } catch (UnsupportedJwtException e) {
            log.info("Unsupported JWT token.");
            log.trace("Unsupported JWT token trace: {}", e);
        } catch (IllegalArgumentException e) {
            log.info("JWT token compact of handler are invalid.");
            log.trace("JWT token compact of handler are invalid trace: {}", e);
        }
        return false;
    }
}

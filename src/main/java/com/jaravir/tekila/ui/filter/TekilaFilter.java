package com.jaravir.tekila.ui.filter;

import com.jaravir.tekila.base.auth.persistence.manager.SecurityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.EJB;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Serializable;
import java.util.Enumeration;

/**
 * Created by sajabrayilov on 12/17/2014.
 */
public class TekilaFilter implements Filter, Serializable {

    private final static Logger log = LoggerFactory.getLogger(TekilaFilter.class);
    private FilterConfig config;
    @EJB
    private SecurityManager securityManager;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        config = filterConfig;
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse resp, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) servletRequest;
        String path = req.getRequestURI();

        if (path == null
                || path.isEmpty()
                || path.equals("/")
                || path.equals("/login.xhtml")
                || path.equals("/unauth.xhtml")
                || path.equals("/admin.xhtml")
                || path.startsWith("/javax.faces.resource")
                || path.startsWith("/resources")
                || path.endsWith(".png")
                || path.endsWith(".ico")
                || path.endsWith(".gif")
                || path.endsWith(".jpg")
                || path.endsWith(".jpeg")) {
            chain.doFilter(servletRequest, resp);
            return;
        }

        log.debug("Path received " + path);

        boolean res = false;
        try {
//            log.info(" DO FILTER ------------------------------------->>>>>>>>> "+securityManager.checkPermissionsForPath(path));
            res = securityManager.checkPermissionsForPath(path);
//            res = true;
        } catch (Exception ex) {
            log.error("doFilter: Cannot call security manager", ex);
        }

        log.debug(String.format("check permissions for path=%s, res=%b", path, res));
        if (!res) {
            ((HttpServletResponse) resp).sendRedirect("/unauth.xhtml");
        }

        chain.doFilter(servletRequest, resp);
    }

    @Override
    public void destroy() {

    }
}

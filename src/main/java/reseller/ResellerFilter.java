package reseller;

import org.apache.log4j.Logger;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * Created by KamranMa on 26.12.2017.
 */
public class ResellerFilter implements Filter {
    private static final Logger log = Logger.getLogger(ResellerFilter.class);

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        log.debug("****ResellerFilter.doFilter()****");

        String requestUri = ((HttpServletRequest)servletRequest).getRequestURI();
        log.info(String.format("Uri on ResellerFilter = %s", requestUri));

        servletRequest.getRequestDispatcher(requestUri).forward(servletRequest, servletResponse);
    }

    @Override
    public void destroy() {

    }
}
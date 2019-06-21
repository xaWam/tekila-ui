package com.jaravir.tekila.ui.servket.redirect;

import org.apache.log4j.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by sajabrayilov on 28.01.2015.
 */
public class RedirectServlet extends HttpServlet {
    private final static Logger log = Logger.getLogger(RedirectServlet.class);

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getPathInfo();

        if (path != null)
            path = path.replaceFirst("^/", "");
        else {
            path = "";
        }

        String query = req.getQueryString();

        String redirect = "https://tekila.azerfon.az/" + path + (query != null && !query.isEmpty() ? "?" + query : "");

        log.debug(String.format("path=%s, query=%s, url=%s", path, query, redirect));
        resp.sendRedirect(redirect);
    }
}

package com.jaravir.tekila.ui.listener;

import com.jaravir.tekila.base.auth.persistence.manager.SessionFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

/**
 * Created by sajabrayilov on 12/17/2014.
 */

@ManagedBean
@SessionScoped
public class UserSessionListener implements HttpSessionListener {
    private final static Logger log = LoggerFactory.getLogger(UserSessionListener.class);
    private final static int timeoutInMinutes = 30;

    @EJB
    private SessionFacade sessionFacade;

    @Override
    public void sessionCreated(HttpSessionEvent httpSessionEvent) {

    }

    public String getSessionID () {
        HttpSession session = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);
        return (String) session.getAttribute("sessionID");
    }

    public Long getUserID() {
        HttpSession session = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);
        return (Long) session.getAttribute("userID");
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent httpSessionEvent) {
//        log.debug("Session destroyed");
        Long userID = (Long) httpSessionEvent.getSession().getAttribute("userID");
        String username = (String) httpSessionEvent.getSession().getAttribute("username");
        if (userID != null) {
            sessionFacade.removeExpiredSessions(userID, timeoutInMinutes);
            log.debug("Session destroyed for username :{}", username);
        }
    }

}

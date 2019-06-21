package com.jaravir.tekila.ui.jsf.managed;

import com.jaravir.tekila.base.auth.UserManager;
import com.jaravir.tekila.base.auth.UserStatus;
import com.jaravir.tekila.base.auth.persistence.User;
import com.jaravir.tekila.base.auth.persistence.manager.PasswordResetConfirmationPersistenceFacade;
import com.jaravir.tekila.base.auth.persistence.manager.SessionFacade;

import java.io.IOException;
import java.io.Serializable;
import java.util.Enumeration;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.component.UIComponent;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.ComponentSystemEvent;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.jaravir.tekila.base.auth.persistence.manager.UserPersistenceFacade;
import com.jaravir.tekila.base.entity.PasswordResetConfirmation;
import com.jaravir.tekila.base.persistence.manager.BillingSettingsManager;
import com.jaravir.tekila.module.auth.security.PasswordGenerator;
import com.jaravir.tekila.module.system.SystemEvent;
import com.jaravir.tekila.module.system.log.SystemLogger;

import java.util.logging.Level;
import javax.faces.bean.ViewScoped;

import com.jaravir.tekila.ui.jsf.exception.handler.UserBlockedException;
import org.apache.log4j.Logger;

import org.apache.commons.lang3.RandomStringUtils;
import org.joda.time.DateTime;

import static spring.security.Constants.USER_GROUP;

@ManagedBean
@ViewScoped
public class LoginManager implements Serializable {

    private String username;
    private String password;
    @EJB
    private UserManager userManager;
    @EJB
    private SessionFacade sessionFacade;
    @EJB
    private SystemLogger systemLogger;
    @EJB
    private UserPersistenceFacade userFacade;
    @EJB
    private UserPersistenceFacade userPersistenceFacade;

    @EJB
    private BillingSettingsManager settingsManager;
//    @EJB
//    private SessionContext sessionContext;

    private User user;
    private transient UIComponent loginForm;
    private static Logger log = Logger.getLogger(LoginManager.class);

    public void onPrerenderView(ComponentSystemEvent ev) throws IOException {
        checkLoggedIn();

        int port = ((HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest()).getServerPort();
        try {
            if (username.equals("testuser") && port == 28080) {
                password = "gT@7D9q-";
                login();
            }
        } catch (NullPointerException n) {

        }
    }

    private void checkLoggedIn() throws IOException {
        String referer = FacesContext.getCurrentInstance().getExternalContext().getRequestHeaderMap().get("Referer");
        String ticketID = ((HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest()).getParameter("ticket_id");
        String tt_redirect = FacesContext.getCurrentInstance().getExternalContext().getInitParameter("com.jaravir.tekila.tt.redirect.showticket.address");
        String app_redirect = FacesContext.getCurrentInstance().getExternalContext().getInitParameter("com.jaravir.tekila.app.redirect");

        Cookie cookie = (Cookie) FacesContext.getCurrentInstance().getExternalContext().getRequestCookieMap().get("JSESSIONID");
        HttpSession session = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);

        /*log.debug(String.format("Sessionid=%s, cookie=%s", session.getId(), cookie.getValue()));
         SessionInfo sessionInfo = sessionFacade.reloginBySessionID(session.getId(), cookie.getValue());

         log.debug("Relogin returned SessionInfo: " + sessionInfo);
         */
        if (FacesContext.getCurrentInstance().getExternalContext().getUserPrincipal() != null) {
            if (ticketID != null && !ticketID.isEmpty()) {
                String redirectUrl = tt_redirect + ticketID;
                log.debug("User is redirected to: " + tt_redirect + ticketID);
                FacesContext.getCurrentInstance().getExternalContext().redirect(redirectUrl);
            } else if (referer != null && !referer.isEmpty() && referer.contains("pages")) {
                FacesContext.getCurrentInstance().getExternalContext().redirect(referer);
            } else {
                log.debug("User need to login");
                FacesContext.getCurrentInstance().getExternalContext().redirect("/admin.xhtml");
            }
        }
    }

    public String getUsername() {
        if (this.username == null) {
            this.username = "";
        }
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        if (this.password == null) {
            this.password = "";
        }
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User us) {
        this.user = us;
    }

    public UIComponent getLoginForm() {
        return loginForm;
    }

    public void setLoginForm(UIComponent messageDiv) {
        this.loginForm = messageDiv;
    }

    public String getLoginFromSession() {
        log.debug("Username in getLoginFromSession: " + username);
        HttpSession session = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);
        String login = (String) session.getAttribute("username");
        return (login != null) ? login : "";
    }

    public void setLoginFromSession(String username) {

    }

    public String getSessionUsername() {
        FacesContext ctx = FacesContext.getCurrentInstance();
        HttpSession session = (HttpSession) ctx.getExternalContext().getSession(false);
        return session.getAttribute("username").toString();
    }

    public String getSessionFirstname() {
        FacesContext ctx = FacesContext.getCurrentInstance();
        HttpSession session = (HttpSession) ctx.getExternalContext().getSession(false);
        return session.getAttribute("firstname").toString();
    }

    public String getSessionSurname() {
        FacesContext ctx = FacesContext.getCurrentInstance();
        HttpSession session = (HttpSession) ctx.getExternalContext().getSession(false);
        return session.getAttribute("surname").toString();
    }

    public String login() {
        long start = System.currentTimeMillis();
        log.debug(String.format("before login: username=%s", username));

        String referer = FacesContext.getCurrentInstance().getExternalContext().getRequestHeaderMap().get("Referer");

        log.debug("Referer: " + referer);

        if (username == null || username.isEmpty() || password == null || password.isEmpty()) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Login or password incorrect", "Login or password incorrect")
            );
            systemLogger.logLoginFailure(user, username, "Login or password incorrect");
            return null;
        }

        FacesContext ctx = FacesContext.getCurrentInstance();
        HttpServletRequest req = (HttpServletRequest) ctx.getExternalContext().getRequest();

        try {
            this.user = userManager.findByUsername(this.username);

            if (user.getStatus() == UserStatus.BLOCKED)
                throw new UserBlockedException(this.username + " is blocked");

            req.login(this.username, this.password);

            password = null;

            log.debug("User logged in is: " + this.username);
            log.debug("firstName: " + this.getUser().getFirstName());
            log.debug("surname: " + this.getUser().getSurname());
            log.debug("X-forwared-for: " + req.getHeader("x-forwarded-for"));
            Enumeration<String> headers = req.getHeaderNames();

//            while (headers.hasMoreElements()) {
//                log.debug("header  name: " + headers.nextElement());
//            }

            HttpSession session = (HttpSession) ctx.getExternalContext().getSession(true);

            session.setAttribute("username", this.username);
            session.setAttribute("firstname", this.user.getFirstName());
            session.setAttribute("surname", this.user.getSurname());
            session.setAttribute("theme", this.user.getTheme());

            session.setAttribute("userID", this.user.getId());
            session.setAttribute("group", this.user.getGroup().getGroupName());
            session.setAttribute(USER_GROUP, this.user.getGroup());

            String sessionID = sessionFacade.generateKey();
            session.setAttribute("sessionID", sessionID);

            log.debug("User info: " + this.user.toString());
            log.debug("Session info: " + session.getAttributeNames());

            sessionFacade.createSessionInfo(session.getId(), username, this.user.getGroup().getGroupName(), sessionID, req.getRemoteAddr(), req.getHeader("x-forwarded-for"));

            systemLogger.success(SystemEvent.LOGIN_SUCCESS, null,
                    "User logged in is:" + this.username + " , retryCounter is :" + user.getLoginRetryCounter());
            user.setLoginRetryCounter(0);
            user.setLastLoginDate(DateTime.now());
            user = userPersistenceFacade.update(user);
            checkLoggedIn();
            return null;
        } catch (ServletException ex) {
            User user = null;
            StringBuilder sb = new StringBuilder();
            sb.append("Username=").append(username);
            try {
                user = userFacade.findByUserName(username);
                user.incrementRetryCounter();
                if (user.getLoginRetryCounter() >= settingsManager.getSettings().getMaximumLoginRetryCount()) {
                    userPersistenceFacade.forceBlock(user);
                } else
                    user = userPersistenceFacade.update(user);
                sb.append(", retryCounter=").append(user.getLoginRetryCounter());
            } catch (Exception ex2) {
                log.error(ex2);
            }

            String dsc = sb.toString();

            log.error("Login failed: " + dsc + " " + user);
            systemLogger.logLoginFailure(user, username, dsc);
            ctx.addMessage(this.loginForm.getClientId(), new FacesMessage(FacesMessage.SEVERITY_ERROR, "Login or password incorrect", "Login or password incorrect"));
            return "login";
        } catch (UserBlockedException e) {
            ctx.addMessage(this.loginForm.getClientId(), new FacesMessage(FacesMessage.SEVERITY_ERROR, "You are blocked", "You are blocked"));
            return "login";
        } catch (IOException ex) {
            log.error(ex.getMessage());
            return "login";
        } catch (Exception e) {
            log.error(e);
            ctx.addMessage(this.loginForm.getClientId(), new FacesMessage(FacesMessage.SEVERITY_ERROR, "Login or password incorrect", "Login or password incorrect"));
            return "login";
        }
    }

    public void logout() throws IOException {
        ExternalContext ctx = FacesContext.getCurrentInstance().getExternalContext();
        HttpServletRequest req = (HttpServletRequest) ctx.getRequest();

        try {
            systemLogger.success(SystemEvent.LOGOUT, null, "user logout");
            req.logout();
            HttpSession session = (HttpSession) ctx.getSession(false);
            sessionFacade.deleteByUsernameAndKey((String) session.getAttribute("username"), (String) session.getAttribute("sessionID"));
            ctx.invalidateSession();
            username = null;
            ctx.redirect(ctx.getRequestContextPath() + "/login.xhtml");
        } catch (ServletException ex) {
            log.error("Cannot logout", ex.getCause());
        }
    }

    @EJB
    PasswordResetConfirmationPersistenceFacade resetFacade;

    public void startResetPassword() throws IOException {

        if (username == null) {
            FacesContext.getCurrentInstance().addMessage(this.loginForm.getClientId(),
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Please fill username field", "Please fill username field"));
            return;
        }

        try {
            this.user = userManager.findByUsername(username);
        } catch (Exception e) {
            log.error("User " + username + " not found !");

            FacesContext.getCurrentInstance().addMessage(this.loginForm.getClientId(),
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "User " + username + " not found !", "User " + username + " not found !"));

            return;
        }

        resetFacade.processedByUserName(username);
        PasswordResetConfirmation confirm = new PasswordResetConfirmation();
        confirm.setUserName(username);
        confirm.setStatus(0);
        confirm.setKey(RandomStringUtils.randomAlphanumeric(20));
        resetFacade.save(confirm);

        FacesContext.getCurrentInstance().addMessage(this.loginForm.getClientId(),
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Confirmation mail has been sent to you.", "Confirmation mail has been sent to you."));

    }

    public String getTheme() {
        try {
            ExternalContext ctx = FacesContext.getCurrentInstance().getExternalContext();
            HttpSession session = (HttpSession) ctx.getSession(false);
            return userPersistenceFacade.getTheme((String) session.getAttribute("username"));
        } catch (Exception n) {
            return "cupertino";
        }
    }

    // Confirmation
    @EJB
    private PasswordGenerator passwordGenerator;

    public String charArray2String(char[] charArray) {
        String s = "";

        for (char c : charArray) {
            s += c;
        }
        return s;
    }

    String key;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public void resetPassword(String username) throws IOException {
        User user = userPersistenceFacade.findByUserName(username);
        char[] rawPass = passwordGenerator.generatePassword();
        user.setPassword(passwordGenerator.encodePassword(rawPass));
        userPersistenceFacade.update(user, charArray2String(rawPass));
    }

    public void passwordResetConfirmation() {

        if (key != null) {

            PasswordResetConfirmation confirm;
            if ((confirm = resetFacade.check(this.key)) != null) {
                try {
                    resetPassword(confirm.getUserName());
                    resetFacade.processedByUserName(confirm.getUserName());

                    FacesContext.getCurrentInstance().addMessage(this.loginForm.getClientId(),
                            new FacesMessage(FacesMessage.SEVERITY_INFO, "Your password has been reset! Check your email.", "Your password has been reset! Check your email."));

                    return;
                } catch (IOException ex) {
                    java.util.logging.Logger.getLogger(LoginManager.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

            FacesContext.getCurrentInstance().addMessage(this.loginForm.getClientId(),
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Invalid confirmation key !", "Invalid confirmation key !"));

        }
    }
}

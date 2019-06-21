package com.jaravir.tekila.ui.jsf.managed;

import com.jaravir.tekila.base.auth.persistence.User;
import com.jaravir.tekila.base.auth.persistence.manager.UserPersistenceFacade;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;
import org.apache.log4j.Logger;

/**
 *
 * @author khsadigov
 */
@ManagedBean
@ViewScoped
public class ThemeManager {

    private transient final static Logger log = Logger.getLogger(com.jaravir.tekila.ui.jsf.managed.ThemeManager.class);
    @EJB
    private UserPersistenceFacade userPersistenceFacade;
    HttpSession session = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);
    String selectedTheme = null;

    public ThemeManager() {
    }

    public String getSelectedTheme() {
        if (selectedTheme == null) {
            selectedTheme = userPersistenceFacade.getTheme((String) session.getAttribute("username"));
        }
        return selectedTheme;
    }

    public void setSelectedTheme(String selectedTheme) {
        this.selectedTheme = selectedTheme;
    }

    public final String[] THEMES = {"afterdark", "afternoon",
        "afterwork", "aristo", "black-tie", "blitzer",
        "bluesky", "casablanca", "cruze", "cupertino", "dark-hive", "dot-luv",
        "eggplant", "excite-bike", "flick", "glass-x", "home", "hot-sneaks",
        "humanity", "le-frog",
        "midnight", "mint-choc", "overcast", "pepper-grinder",
        "redmond", "rocket",
        "sam", "smoothness",
        "south-street", "start",
        "sunny", "swanky-purse",
        "trontastic", "ui-darkness",
        "ui-lightness", "vader"};

    public String[] getThemes() {
        return THEMES;
    }

    public void saveTheme() {
        User user = userPersistenceFacade.findByUserName((String) session.getAttribute("username"));
        user.setTheme(selectedTheme);
        userPersistenceFacade.update(user);
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "The theme is changed to " + selectedTheme, "The theme is changed to " + selectedTheme));

    }

}

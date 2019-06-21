package com.jaravir.tekila.ui.jsf.managed.extern;

import com.jaravir.tekila.base.auth.entity.SessionInfo;
import com.jaravir.tekila.base.auth.persistence.manager.SessionFacade;
import com.jaravir.tekila.extern.tt.TroubleTicket;
import com.jaravir.tekila.extern.tt.manager.TroubleTicketPersistenceFacade;
import com.jaravir.tekila.module.subscription.persistence.entity.Subscriber;
import com.jaravir.tekila.module.subscription.persistence.management.SubscriberPersistenceFacade;
import org.apache.log4j.Logger;
import org.primefaces.model.LazyDataModel;

import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.view.facelets.FaceletContext;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;

/**
 * Created by sajabrayilov on 12/16/2014.
 */
@ManagedBean(name = "ticketManager")
@ViewScoped
public class TroubleTicketManager  implements Serializable {
    private transient final static Logger log = Logger.getLogger(TroubleTicketManager.class);
    private final static String PATH = "/pages/subscriber/tt/";

    @EJB private TroubleTicketPersistenceFacade ticketFacade;
    @EJB private SubscriberPersistenceFacade subscriberFacade;
    @EJB private SessionFacade sessionFacade;

    private List<TroubleTicket> troubleTickets;
    private TroubleTicket selected;
    private List<TroubleTicket> filteredTickets;
    private Long subscriberID;
    private Subscriber subscriber;

    public List<TroubleTicket> getTroubleTickets() {
        if (troubleTickets == null) {
            if (subscriber == null)
                subscriber = subscriberFacade.find(subscriberID);

            if (subscriber != null)
                troubleTickets = ticketFacade.findAllBySubscriber(subscriber);
        }
        return troubleTickets;
    }

    public void setTroubleTickets(List<TroubleTicket> troubleTickets) {
        this.troubleTickets = troubleTickets;
    }

    public TroubleTicket getSelected() {
        return selected;
    }

    public void setSelected(TroubleTicket selected) {
        this.selected = selected;
    }

    public List<TroubleTicket> getFilteredTickets() {
        return filteredTickets;
    }

    public void setFilteredTickets(List<TroubleTicket> filteredTickets) {
        this.filteredTickets = filteredTickets;
    }

    public Long getSubscriberID() {
        return subscriberID;
    }

    public void setSubscriberID(Long subscriberID) {
        this.subscriberID = subscriberID;
    }

    public String redirectToSubscriber() {
        if (subscriberID == null) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Please select ticket", "Please select ticket"));
            return null;
        }

        return new StringBuilder("/pages/subscriber/view.xhtml?subscriber=").append(subscriberID).append("&amp;faces-redirect=true&amp;includeViewParams=true").toString();
    }

    public String redirectToMainPage() {
       // try {
            String mainPage = FacesContext.getCurrentInstance().getExternalContext()
                    .getInitParameter("com.jaravir.tekila.tt.redirect.main.address");
            HttpSession session = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);
            Long userID = (Long) session.getAttribute("userID");
            String key = (String) session.getAttribute("sessionID");
            try {
                SessionInfo sessionInfo = sessionFacade.findByUserID(userID, key);
            }
            catch (Exception ex) {
                log.error("redirectToMainPage: cannot find user", ex);
            }
            return String.format(mainPage, key);
            //FacesContext.getCurrentInstance().getExternalContext().redirect(String.format(mainPage, sessionInfo.getUsername(), key));
        /*}
        catch (IOException ex) {
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Cannot redirect to trouble tickets main page","Cannot redirect to trouble tickets main page"));
            log.error("redirect to TT main page failed", ex);
        }*/
    }

    public void redirectToTicketPage() {
        if (selected == null) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Please select ticket", "Please select ticket"));
            return;
        }
        try {
            String showTicketURL = FacesContext.getCurrentInstance().getExternalContext().getInitParameter("com.jaravir.tekila.tt.redirect.showticket.address");
            FacesContext.getCurrentInstance().getExternalContext().redirect(showTicketURL + selected.getId());
        }
        catch (IOException ex) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Cannot redirect to trouble ticket","Cannot redirect to trouble ticket"));
            log.error("redirect failed", ex);
        }

    }
}

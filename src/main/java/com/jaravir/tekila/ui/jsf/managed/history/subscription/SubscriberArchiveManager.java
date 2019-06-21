package com.jaravir.tekila.ui.jsf.managed.history.subscription;

import com.jaravir.tekila.module.archive.subscription.SubscriberArchivePersistenceFacade;
import com.jaravir.tekila.module.archive.subscription.SubscriberArchived;
import com.jaravir.tekila.ui.model.LazyTableModel;
import org.apache.log4j.Logger;

import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import java.io.Serializable;

/**
 * Created by sajabrayilov on 22.01.2015.
 */
@ManagedBean
@ViewScoped
public class SubscriberArchiveManager  implements Serializable {
    private transient final static Logger log = Logger.getLogger(SubscriberArchiveManager.class);
    private final static String PATH = "/pages/subscriber/history/";

    @EJB private SubscriberArchivePersistenceFacade archiveFacade;
    private LazyTableModel<SubscriberArchived> archivedData;
    private SubscriberArchived selectedArchivedData;
    private Long archivedDataID;
    private Long subscriberID;

    public LazyTableModel<SubscriberArchived> getArchivedData() {
        if (archivedData == null && subscriberID != null) {
            archiveFacade.clearFilters();
            archiveFacade.addFilter(SubscriberArchivePersistenceFacade.Filter.SUBSCRIBER_ID, subscriberID);
            archivedData = new LazyTableModel<>(archiveFacade);
        }
        return archivedData;
    }

    public void setArchivedData(LazyTableModel<SubscriberArchived> archivedData) {
        this.archivedData = archivedData;
    }

    public SubscriberArchived getSelectedArchivedData() {
        if (selectedArchivedData == null && archivedDataID != null && subscriberID != null) {
            selectedArchivedData = archiveFacade.find(subscriberID, archivedDataID);
        }
        return selectedArchivedData;
    }

    public void setSelectedArchivedData(SubscriberArchived selectedArchivedData) {
        this.selectedArchivedData = selectedArchivedData;
    }

    public Long getArchivedDataID() {
        return archivedDataID;
    }

    public void setArchivedDataID(Long archivedDataID) {
        this.archivedDataID = archivedDataID;
    }

    public Long getSubscriberID() {
        return subscriberID;
    }

    public void setSubscriberID(Long subscriberID) {
        this.subscriberID = subscriberID;
    }

    public String show () {
        if (selectedArchivedData == null) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Please select an entry", "Please select an entry"));
            return null;
        }

        return String.format("%sview.xhtml?subscriber=%d&amp;version=%d&amp;faces-redirect=true&amp;includeViewParams=true", PATH, subscriberID, selectedArchivedData.getObjectVersion());
    }

    public void restore () {

    }
}

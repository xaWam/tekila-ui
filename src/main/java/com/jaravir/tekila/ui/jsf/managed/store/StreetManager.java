package com.jaravir.tekila.ui.jsf.managed.store;

import com.jaravir.tekila.module.store.ats.AtsPersistenceFacade;
import com.jaravir.tekila.module.store.street.StreetPersistenceFacade;
import com.jaravir.tekila.module.subscription.persistence.entity.Ats;
import com.jaravir.tekila.module.subscription.persistence.entity.Streets;
import com.jaravir.tekila.ui.model.LazyTableModel;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.primefaces.context.RequestContext;
import org.primefaces.event.RowEditEvent;
import org.primefaces.model.LazyDataModel;

import javax.annotation.ManagedBean;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;

/**
 * Created by shnovruzov on 8/4/2016.
 */
@ManagedBean
@ViewScoped
public class StreetManager implements Serializable {

    private final static Logger log = Logger.getLogger(StreetManager.class);
    private final static String CREATE_PATH = "/pages/store/street/create.xhtml";
    private final static String INDEX_PATH = "/pages/store/street/index.xhtml";

    @EJB
    private StreetPersistenceFacade streetFacade;
    @EJB
    private AtsPersistenceFacade atsFacade;

    private Ats ats;
    private Ats selectedAts;
    private String name;
    private Long streetIndex;
    private Streets selectedStreet;

    private List<Ats> atsSelectList;

    @PostConstruct
    public void init() {
        streetFacade.clearFilters();
    }

    public Ats getSelectedAts() {
        return selectedAts;
    }

    public void setSelectedAts(Ats selectedAts) {
        this.selectedAts = selectedAts;
    }

    public Ats getAts() {
        return ats;
    }

    public void setAts(Ats ats) {
        this.ats = ats;
    }

    public List<Ats> getAtsSelectList() {
        if (atsSelectList == null) {
            atsSelectList = atsFacade.findAllAts();
        }
        return atsSelectList;
    }

    public void setAtsSelectList(List<Ats> atsSelectList) {
        this.atsSelectList = atsSelectList;
    }

    public Streets getSelectedStreet() {
        return selectedStreet;
    }

    public void setSelectedStreet(Streets selectedStreet) {
        this.selectedStreet = selectedStreet;
    }

    private LazyDataModel<Streets> streetList;

    public LazyDataModel<Streets> getStreetList() {
        if (streetList == null)
            streetList = new LazyTableModel<>(streetFacade);
        return streetList;
    }

    public void setStreetList(LazyDataModel<Streets> streetList) {
        this.streetList = streetList;
    }

    public Long getStreetIndex() {
        return streetIndex;
    }

    public void setStreetIndex(Long streetIndex) {
        this.streetIndex = streetIndex;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void search() {
        streetFacade.clearFilters();

        if (name != null && !name.isEmpty())
            streetFacade.addFilter(StreetPersistenceFacade.Filter.NAME, name);
        if (ats != null) {
            //atsIndex in streets table is foreign key
            //to primary key in the ats table
            streetFacade.addFilter(StreetPersistenceFacade.Filter.ATSINDEX, ats.getId());
        }
        if (streetIndex != null)
            streetFacade.addFilter(StreetPersistenceFacade.Filter.STREETINDEX, streetIndex);

        streetList = new LazyTableModel<>(streetFacade);
        log.debug("Found street count = " + streetFacade.count());
    }

    public void create() throws IOException {
        if (name == null || name.isEmpty() || ats == null) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Fill in all fields", "Fill in all fields"));
            return;
        }

        Object lock = new Object();
        synchronized (lock) {
            streetIndex = streetFacade.getStreetIndex() + 1;
            streetFacade.create(name, String.valueOf(ats.getId()), streetIndex);
        }
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Created new street", "Created new street"));
        FacesContext.getCurrentInstance().getExternalContext().redirect("/pages/store/street/index.xhtml");
        RequestContext.getCurrentInstance().update("eqForm");
    }


    public void onRowEdit(RowEditEvent event) {
        Streets street = (Streets) event.getObject();
        streetFacade.update(street);
    }
}

package com.jaravir.tekila.ui.jsf.managed.store;

import com.jaravir.tekila.module.store.ats.AtsPersistenceFacade;
import com.jaravir.tekila.module.subscription.persistence.entity.Ats;
import com.jaravir.tekila.module.subscription.persistence.entity.AtsStatus;
import com.jaravir.tekila.ui.model.LazyTableModel;
import org.apache.log4j.Logger;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by shnovruzov on 5/19/2016.
 */
@ManagedBean
@ViewScoped
public class AtsManager implements Serializable{

    private final static Logger log = Logger.getLogger(AtsManager.class);
    private final static String CREATE_PATH = "/pages/store/ats/create.xhtml";
    private final static String INDEX_PATH = "/pages/store/ats/index.xhtml";

    private Long atsId;
    private String atsName;
    private String atsIndex;
    private AtsStatus atsStatus;
    private List<AtsStatus> atsStatusList;
    private String coor;
    private Integer dayOfBilling;

    private LazyDataModel<Ats> atsList;
    private List<Integer> dayOfBillingList;
    private Ats selectedAts;
    private List<Ats> atsFilteredList;

    @EJB
    private AtsPersistenceFacade atsFacade;

    @PostConstruct
    public void init() {
        atsFacade.clearFilters();
    }

    public String getAtsIndex() {
        return atsIndex;
    }

    public void setAtsIndex(String atsIndex) {
        this.atsIndex = atsIndex;
    }

    public AtsStatus getAtsStatus() {
        return atsStatus;
    }

    public void setAtsStatus(AtsStatus atsStatus) {
        this.atsStatus = atsStatus;
    }

    public List<AtsStatus> getAtsStatusList() {
        if (atsStatusList == null) {
            atsStatusList = Arrays.asList(AtsStatus.values());
        }
        return atsStatusList;
    }

    public void setAtsStatusList(List<AtsStatus> atsStatusList) {
        this.atsStatusList = atsStatusList;
    }

    public Long getAtsId() {
        return atsId;
    }

    public void setAtsId(Long atsId) {
        this.atsId = atsId;
    }

    public List<Ats> getAtsFilteredList() {
        return atsFilteredList;
    }

    public void setAtsFilteredList(List<Ats> atsFilteredList) {
        this.atsFilteredList = atsFilteredList;
    }

    public List<Integer> getDayOfBillingList() {
        if (dayOfBillingList == null) {
            dayOfBillingList = new ArrayList<>();
            for (int i = 1; i <= 28; i++)
                dayOfBillingList.add(i);
        }
        return dayOfBillingList;
    }

    public void setDayOfBillingList(List<Integer> dayOfBillingList) {
        this.dayOfBillingList = dayOfBillingList;
    }

    public Integer getDayOfBilling() {
        return dayOfBilling;
    }

    public void setDayOfBilling(Integer dayOfBilling) {
        this.dayOfBilling = dayOfBilling;
    }

    public String getCoor() {
        return coor;
    }

    public void setCoor(String coor) {
        this.coor = coor;
    }

    public Ats getSelectedAts() {
        if (selectedAts == null && atsId != null)
            selectedAts = atsFacade.find(atsId);
        return selectedAts;
    }

    public void setSelectedAts(Ats selectedAts) {
        this.selectedAts = selectedAts;
    }

    public LazyDataModel<Ats> getAtsList() {
        if (atsList == null) {
            atsFacade.clearFilters();
            //atsFacade.addFilter(AtsPersistenceFacade.Filter.STATUS, AtsStatus.ACTIVE);
            atsList = new LazyTableModel<Ats>(atsFacade);
        }
        return atsList;
    }

    public void setAtsList(LazyDataModel<Ats> atsList) {
        this.atsList = atsList;
    }

    public String getAtsName() {
        return atsName;
    }

    public void setAtsName(String atsName) {
        this.atsName = atsName;
    }

    public boolean check() {
        if (dayOfBilling == null || atsName == null || atsName.isEmpty() || atsIndex == null || atsIndex.isEmpty()
                || atsStatus == null)
            return false;
        return true;
    }

    public void search() {
        atsFacade.clearFilters();
        if (atsName != null && !atsName.isEmpty())
            atsFacade.addFilter(AtsPersistenceFacade.Filter.NAME, atsName);
        if (atsIndex != null && !atsIndex.isEmpty())
            atsFacade.addFilter(AtsPersistenceFacade.Filter.ATSINDEX, atsIndex);

        atsList = new LazyTableModel<>(atsFacade);
        long count = atsFacade.count();
        log.debug(String.format("Found %s ats", count));
    }

    public void create() throws IOException {

        if (!check()) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Fill in the fields", "Fill in the fields"));
            return;
        }

        if (dayOfBilling == null || dayOfBilling < 0 || dayOfBilling > 28) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Select billing day", "Select billing day"));
            return;
        }

        Ats ats = null;
        ats = atsFacade.create(atsName, atsIndex, coor, dayOfBilling, atsStatus);

        if (ats == null) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Cannot create ats", "Cannot create ats"));
            return;
        }

        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Created new ats: " + ats.getName(), "Created new ats: " + ats.getName()));
        FacesContext.getCurrentInstance().getExternalContext().redirect("/pages/store/ats/index.xhtml");
        RequestContext.getCurrentInstance().update("eqForm");

    }

    public void onRowEdit(RowEditEvent event) {
        Ats ats = (Ats) event.getObject();
        atsFacade.update(ats);
    }

}

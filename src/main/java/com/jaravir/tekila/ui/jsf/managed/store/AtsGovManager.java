package com.jaravir.tekila.ui.jsf.managed.store;


import com.jaravir.tekila.base.entity.ATSGOV;
import com.jaravir.tekila.module.store.ats.AtsGovsPersistenceFacade;
import com.jaravir.tekila.ui.model.LazyTableModel;
import org.apache.log4j.Logger;
import org.primefaces.event.RowEditEvent;
import org.primefaces.model.LazyDataModel;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import java.util.List;

@ManagedBean
@ViewScoped
public class AtsGovManager {

    private final static Logger logger = Logger.getLogger(AtsGovManager.class);

    private LazyDataModel<ATSGOV> atsGovList;
    private ATSGOV selectedAtsGov;
    private List<ATSGOV> atsGovFilteredList;
    private String atsGovName;

    @EJB
    private AtsGovsPersistenceFacade atsGovsPersistenceFacade;

    @PostConstruct
    public void init() {
        atsGovsPersistenceFacade.clearFilters();
    }

    public LazyDataModel<ATSGOV> getAtsGovList() {
        if (atsGovList == null) {
            atsGovsPersistenceFacade.clearFilters();
            atsGovList = new LazyTableModel<ATSGOV>(atsGovsPersistenceFacade);
        }
        return atsGovList;
    }

    public void onRowEdit(RowEditEvent event) {
        ATSGOV atsGOV = (ATSGOV) event.getObject();
        atsGovsPersistenceFacade.update(atsGOV);
    }


    public void search() {
        atsGovsPersistenceFacade.clearFilters();
        if (atsGovName != null && !atsGovName.isEmpty())
            atsGovsPersistenceFacade.addFilter(AtsGovsPersistenceFacade.Filter.NAME, atsGovName);
        atsGovList = new LazyTableModel<>(atsGovsPersistenceFacade);
        long count = atsGovsPersistenceFacade.count();
        logger.debug(String.format("Found %s ats", count));
    }


    public void setAtsGovList(LazyDataModel<ATSGOV> atsGovList) {
        this.atsGovList = atsGovList;
    }

    public ATSGOV getSelectedAtsGov() {
        return selectedAtsGov;
    }

    public void setSelectedAtsGov(ATSGOV selectedAtsGov) {
        this.selectedAtsGov = selectedAtsGov;
    }

    public List<ATSGOV> getAtsGovFilteredList() {
        return atsGovFilteredList;
    }

    public void setAtsGovFilteredList(List<ATSGOV> atsGovFilteredList) {
        this.atsGovFilteredList = atsGovFilteredList;
    }

    public String getAtsGovName() {
        return atsGovName;
    }

    public void setAtsGovName(String atsGovName) {
        this.atsGovName = atsGovName;
    }
}

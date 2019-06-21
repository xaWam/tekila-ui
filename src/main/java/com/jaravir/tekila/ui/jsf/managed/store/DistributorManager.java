package com.jaravir.tekila.ui.jsf.managed.store;

import com.jaravir.tekila.base.auth.persistence.manager.UserPersistenceFacade;
import com.jaravir.tekila.module.accounting.manager.SalesPartnerInvoicePersistenceFacade;
import com.jaravir.tekila.module.sales.SalesPartnerType;
import com.jaravir.tekila.module.sales.persistence.entity.SalesPartner;
import com.jaravir.tekila.module.store.SalesPartnerPersistenceFacade;
import com.jaravir.tekila.module.store.SalesPartnerStore;
import com.jaravir.tekila.module.store.SalesPartnerStorePersistenceFacade;
import com.jaravir.tekila.module.store.equip.Equipment;
import com.jaravir.tekila.ui.model.LazyTableModel;
import org.apache.log4j.Logger;
import org.primefaces.component.datatable.DataTable;
import org.primefaces.model.LazyDataModel;

import javax.annotation.ManagedBean;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ViewScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import java.util.List;

/**
 * Created by sajabrayilov on 11/26/2015.
 */
@ViewScoped
@ManagedBean
public class DistributorManager {
    private final static Logger log = Logger.getLogger(DistributorManager.class);
    @EJB private SalesPartnerPersistenceFacade partnerFacade;
    @EJB private SalesPartnerStorePersistenceFacade partnerStoreFacade;
    private SalesPartner partner;
    private final static String INDEX_PATH = "/pages/store/distrib/index.xhtml?faces-redirect=true";
    private final static String VIEW_PATH = "/pages/store/distrib/view.xhtml?faces-redirect=true&amp;includeViewParams=true&amp;distributor=%d";
    private final static String INVOICES_PATH = "/pages/store/distrib/invoice/index.xhtml?distributor=%d&amp;faces-redirect=true&amp;includeViewParams=true";

    private LazyDataModel<SalesPartner> partnerList;
    private List<SalesPartner> fitleredPartnerList;
    private SalesPartner selectedPartner;
    private String nameParameter;
    private UIComponent distributorsTable;
    private Long partnerID;

    @PostConstruct
    public void init () {
        partnerFacade.clearFilters();
        partnerStoreFacade.clearFilters();
    }

    @PreDestroy
    public void cleanUp () {
        partnerFacade.clearFilters();
        partnerStoreFacade.clearFilters();
    }
    
    public SalesPartner getPartner() {
        if (partner == null)
            partner = new SalesPartner();
        return partner;
    }

    public void setPartner(SalesPartner partner) {
        this.partner = partner;
    }

    public String create () {
        if (partner.getName() == null || partner.getName().isEmpty()) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Please fill name","Please fill name"));
            return null;
        }

        partner.setType(SalesPartnerType.DISTRIBUTOR);
        partner = partnerFacade.create(partner);
        partnerStoreFacade.create(partner);
        return INDEX_PATH;
    }

    public String save () {
        if (selectedPartner.getName() == null || selectedPartner.getName() == null || selectedPartner.getName().isEmpty()) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Please fill name","Please fill name"));
            return null;
        }

        selectedPartner = partnerFacade.update(selectedPartner);

        return INDEX_PATH;
    }

    public LazyDataModel<SalesPartner> getPartnerList() {
        return partnerList;
    }

    public void setPartnerList(LazyDataModel<SalesPartner> partnerList) {
        this.partnerList = partnerList;
    }

    public SalesPartner getSelectedPartner() {
        if (partnerID != null) {
            selectedPartner = partnerFacade.find(partnerID);
        }
        return selectedPartner;
    }

    public void setSelectedPartner(SalesPartner selectedPartner) {
        this.selectedPartner = selectedPartner;
    }

    public String getNameParameter() {
        return nameParameter;
    }

    public void setNameParameter(String nameParameter) {
        this.nameParameter = nameParameter;
    }

    public List<SalesPartner> getFitleredPartnerList() {
        return fitleredPartnerList;
    }

    public void setFitleredPartnerList(List<SalesPartner> fitleredPartnerList) {
        this.fitleredPartnerList = fitleredPartnerList;
    }

    public UIComponent getDistributorsTable() {
        return distributorsTable;
    }

    public void setDistributorsTable(UIComponent distributorsTable) {
        this.distributorsTable = distributorsTable;
    }

    public Long getPartnerID() {
        return partnerID;
    }

    public void setPartnerID(Long partnerID) {
        this.partnerID = partnerID;
    }

    public void search () {
        if (distributorsTable instanceof DataTable)
            ((DataTable)distributorsTable).reset();

        partnerFacade.clearFilters();

        partnerFacade.addFilter(SalesPartnerPersistenceFacade.Filter.TYPE, SalesPartnerType.DISTRIBUTOR);

        if (nameParameter != null && !nameParameter.isEmpty())
            partnerFacade.addFilter(SalesPartnerPersistenceFacade.Filter.NAME, nameParameter);

        partnerList = new LazyTableModel<>(partnerFacade);
    }

    public String show () {
        if (selectedPartner == null) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Please select a distributor","Please select a distributor"));
            return null;
        }
        partnerFacade.clearFilters();
        partnerStoreFacade.clearFilters();
        return String.format(VIEW_PATH, selectedPartner.getId());
    }

    public String goToIndex () {
        partnerFacade.clearFilters();
        partnerStoreFacade.clearFilters();
        return INDEX_PATH;
    }

    private boolean check () {
        return nameParameter != null && !nameParameter.isEmpty();
    }

    public String viewInvoices () {
        if (partnerID == null) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Please select a distributor","Please select a distributor"));
            return null;
        }
        partnerFacade.clearFilters();
        partnerStoreFacade.clearFilters();
        return String.format(INVOICES_PATH, partnerID);
    }
}

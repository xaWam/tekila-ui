package com.jaravir.tekila.ui.jsf.managed.store;

import com.jaravir.tekila.module.sales.SalesPartnerType;
import com.jaravir.tekila.module.sales.persistence.entity.SalesPartner;
import com.jaravir.tekila.module.store.SalesPartnerPersistenceFacade;
import com.jaravir.tekila.module.store.SalesPartnerStorePersistenceFacade;
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
 * Created by sajabrayilov on 12/14/2015.
 */
@ManagedBean
@ViewScoped
public class DealerManager {
    private final static Logger log = Logger.getLogger(DealerManager.class);
    private final static String VIEW_PATH = "/pages/store/dealer/view.xhtml?dealer=%d&amp;faces-redirect=true&amp;includeViewParams=true";
    private final static String CREATE_PATH = "/pages/store/dealer/create.xhtml";
    private final static String INDEX_PATH = "/pages/store/dealer/index.xhtml";

    @EJB private SalesPartnerPersistenceFacade partnerFacade;
    @EJB private SalesPartnerStorePersistenceFacade partnerStoreFacade;

    private SalesPartner partner;
    private LazyDataModel<SalesPartner> partnerList;
    private List<SalesPartner> fitleredPartnerList;
    private SalesPartner selectedPartner;
    private String nameParameter;
    private UIComponent distributorsTable;
    private Long partnerID;
    private String userEmail;
    private String distributorName;
    private LazyDataModel<SalesPartner> distributorList;
    private List<SalesPartner> filteredDistributors;
    private SalesPartner seletedDistributor;
    private String firstName;
    private String surname;

    public SalesPartner getPartner() {
        if (partner == null)
            partner = new SalesPartner();
        return partner;
    }

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

    public void setPartner(SalesPartner partner) {
        this.partner = partner;
    }

    public String create () {
        if (partner.getName() == null || partner.getName().isEmpty()) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Please fill name","Please fill name"));
            return null;
        }

        if (seletedDistributor == null) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Please select distributor","Please select distributor"));
            return null;
        }

        partner.setPrincipal(seletedDistributor);
        partner.setType(SalesPartnerType.DEALER);
        partner = partnerFacade.create(partner);
        partnerStoreFacade.create(partner);
        seletedDistributor = partnerFacade.addPartner(seletedDistributor, partner);
        partnerFacade.createDealerUser(partner,userEmail, firstName, surname);

        partnerFacade.clearFilters();
        partnerStoreFacade.clearFilters();

        return String.format(VIEW_PATH, partner.getId());
    }

    public String save () {
        if (selectedPartner.getName() == null || selectedPartner.getName() == null || selectedPartner.getName().isEmpty()) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Please fill name","Please fill name"));
            return null;
        }

        selectedPartner = partnerFacade.update(selectedPartner);
        distributorList = null;
        ((DataTable)distributorsTable).reset();

        partnerFacade.clearFilters();
        partnerStoreFacade.clearFilters();

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

        partnerFacade.addFilter(SalesPartnerPersistenceFacade.Filter.TYPE, SalesPartnerType.DEALER);

        if (nameParameter != null && !nameParameter.isEmpty())
            partnerFacade.addFilter(SalesPartnerPersistenceFacade.Filter.NAME, nameParameter);

        partnerList = new LazyTableModel<>(partnerFacade);
    }

    public String show () {
        if (selectedPartner == null) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Please select a dealer","Please select a dealer"));
            return null;
        }

        return String.format(VIEW_PATH, selectedPartner.getId());
    }

    public String goToIndex () {
        return INDEX_PATH;
    }

    private boolean check () {
        return nameParameter != null && !nameParameter.isEmpty();
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public SalesPartner getSeletedDistributor() {
        return seletedDistributor;
    }

    public void setSeletedDistributor(SalesPartner seletedDistributor) {
        this.seletedDistributor = seletedDistributor;
    }

    public List<SalesPartner> getFilteredDistributors() {
        return filteredDistributors;
    }

    public void setFilteredDistributors(List<SalesPartner> filteredDistributors) {
        this.filteredDistributors = filteredDistributors;
    }

    public LazyDataModel<SalesPartner> getDistributorList() {
        return distributorList;
    }

    public void setDistributorList(LazyDataModel<SalesPartner> distributorList) {
        this.distributorList = distributorList;
    }

    public String getDistributorName() {
        return distributorName;
    }

    public void setDistributorName(String distributorName) {
        this.distributorName = distributorName;
    }

    public void searchDistributors () {
        partnerFacade.clearFilters();
        ((DataTable) distributorsTable).reset();
        partnerFacade.addFilter(SalesPartnerPersistenceFacade.Filter.TYPE, SalesPartnerType.DISTRIBUTOR);

        if (distributorName != null) {
            partnerFacade.addFilter(SalesPartnerPersistenceFacade.Filter.NAME, distributorName);
        }

        distributorList = new LazyTableModel<>(partnerFacade);
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }
}

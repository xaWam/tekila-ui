package com.jaravir.tekila.ui.jsf.managed.store;

import com.jaravir.tekila.module.sales.SalesPartnerType;
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
import javax.faces.model.SelectItem;
import java.io.Serializable;
import java.util.List;

/**
 * Created by sajabrayilov on 11/26/2015.
 */
@ManagedBean
@ViewScoped
public class DealerStoreManager implements Serializable{
    private final static Logger log = Logger.getLogger(DistributorManager.class);
    @EJB private SalesPartnerStorePersistenceFacade partnerStoreFacade;
    private LazyDataModel<SalesPartnerStore> partnerStoreList;
    private List<SalesPartnerStore> filteredPartnerStoreList;
    private SalesPartnerStore selectedSalesPartnerStore;
    private UIComponent storeTable;
    //search
    private String partnerNameParameter;
    private String partNumberParameter;
    private final static SalesPartnerType SALES_PARTNER_TYPE = SalesPartnerType.DEALER;

    //view
    private Equipment selectedEquipment;
    private Long storeID;
    private List<Equipment> filteredEquipmentList;
    private final static String VIEW_PATH = "/pages/store/dealer/store/view.xhtml?store=%d&amp;faces-redirect=true&amp;includeViewParams=true";
    public LazyDataModel<SalesPartnerStore> getPartnerStoreList() {
        return partnerStoreList;
    }

    @PostConstruct
    public void init () {
        partnerStoreFacade.clearFilters();
    }

    @PreDestroy
    public void cleanUp () {
        partnerStoreFacade.clearFilters();
    }

    public void setPartnerStoreList(LazyDataModel<SalesPartnerStore> partnerStoreList) {
        this.partnerStoreList = partnerStoreList;
    }

    public List<SalesPartnerStore> getFilteredPartnerStoreList() {
        return filteredPartnerStoreList;
    }

    public void setFilteredPartnerStoreList(List<SalesPartnerStore> filteredPartnerStoreList) {
        this.filteredPartnerStoreList = filteredPartnerStoreList;
    }

    public SalesPartnerStore getSelectedSalesPartnerStore() {
        if (selectedSalesPartnerStore == null && storeID != null) {
            selectedSalesPartnerStore = partnerStoreFacade.find(storeID);
        }
        return selectedSalesPartnerStore;
    }

    public void setSelectedSalesPartnerStore(SalesPartnerStore selectedSalesPartnerStore) {
        this.selectedSalesPartnerStore = selectedSalesPartnerStore;
    }

    public String getPartnerNameParameter() {
        return partnerNameParameter;
    }

    public void setPartnerNameParameter(String partnerNameParameter) {
        this.partnerNameParameter = partnerNameParameter;
    }

    public String getPartNumberParameter() {
        return partNumberParameter;
    }

    public void setPartNumberParameter(String partNumberParameter) {
        this.partNumberParameter = partNumberParameter;
    }

    public UIComponent getStoreTable() {
        return storeTable;
    }

    public void setStoreTable(UIComponent storeTable) {
        this.storeTable = storeTable;
    }

    public void search () {
        if (storeTable instanceof  DataTable)
            ((DataTable)storeTable).reset();

        partnerStoreFacade.clearFilters();

        if (partnerNameParameter != null && !partnerNameParameter.isEmpty()) {
            partnerStoreFacade.addFilter(SalesPartnerStorePersistenceFacade.Filter.PARTNER_NAME, partnerNameParameter);
        }

        if (partNumberParameter != null && !partNumberParameter.isEmpty()) {
            partnerStoreFacade.addFilter(SalesPartnerStorePersistenceFacade.Filter.EQUIPMENT_PARTNUMBER, partNumberParameter);
        }

        partnerStoreFacade.addFilter(SalesPartnerStorePersistenceFacade.Filter.PARTNER_TYPE, SALES_PARTNER_TYPE);

        partnerStoreList = new LazyTableModel<>(partnerStoreFacade);
    }

    public Equipment getSelectedEquipment() {
        return selectedEquipment;
    }

    public void setSelectedEquipment(Equipment selectedEquipment) {
        this.selectedEquipment = selectedEquipment;
    }

    public Long getStoreID() {
        return storeID;
    }

    public void setStoreID(Long storeID) {
        this.storeID = storeID;
    }

    public List<Equipment> getFilteredEquipmentList() {
        return filteredEquipmentList;
    }

    public void setFilteredEquipmentList(List<Equipment> filteredEquipmentList) {
        this.filteredEquipmentList = filteredEquipmentList;
    }

    public String show () {
        if (selectedSalesPartnerStore == null) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Please select a distributor store","Please select a distributor store"));
            return null;
        }

        return String.format(VIEW_PATH, selectedSalesPartnerStore.getId());
    }

}

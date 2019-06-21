package com.jaravir.tekila.ui.jsf.managed.store;

import com.jaravir.tekila.base.filter.MatchingOperation;
import com.jaravir.tekila.base.persistence.facade.AbstractPersistenceFacade;
import com.jaravir.tekila.equip.EquipmentPersistenceFacade;
import com.jaravir.tekila.module.sales.SalesPartnerType;
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
import javax.faces.event.AjaxBehaviorEvent;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by sajabrayilov on 11/26/2015.
 */
@ManagedBean
@ViewScoped
public class DistributorStoreManager implements Serializable {
    private final static Logger log = Logger.getLogger(DistributorManager.class);
    @EJB private SalesPartnerStorePersistenceFacade partnerStoreFacade;

    private LazyDataModel<SalesPartnerStore> partnerStoreList;
    private List<SalesPartnerStore> filteredPartnerStoreList;
    private SalesPartnerStore selectedSalesPartnerStore;
    private UIComponent storeTable;
    //search
    private String partnerNameParameter;
    private String partNumberParameter;

    //view
    private Equipment selectedEquipment;
    private Long storeID;
    private List<Equipment> filteredEquipmentList;
    private final static String VIEW_PATH = "/pages/store/distrib/store/view.xhtml?store=%d&amp;faces-redirect=true&amp;includeViewParams=true";

    private List<Equipment> distributorStoreEquipment;
    private LazyDataModel<SalesPartnerStore> dealerStoreList;
    private SalesPartnerStore selectedDealerStore;
    private List<SalesPartnerStore> filteredDealerStoreList;
    private String parameterDealerName;
    private String parameterPartNumberFrom;
    private String parameterPartNumberTo;
    private UIComponent dealerStoreTable;

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

        partnerStoreFacade.addFilter(SalesPartnerStorePersistenceFacade.Filter.PARTNER_TYPE, SalesPartnerType.DISTRIBUTOR);

        if (partnerNameParameter != null && !partnerNameParameter.isEmpty()) {
            partnerStoreFacade.addFilter(SalesPartnerStorePersistenceFacade.Filter.PARTNER_NAME, partnerNameParameter);
        }

        if (partNumberParameter != null && !partNumberParameter.isEmpty()) {
            partnerStoreFacade.addFilter(SalesPartnerStorePersistenceFacade.Filter.EQUIPMENT_PARTNUMBER, partNumberParameter);
        }

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
        partnerStoreFacade.clearFilters();
        return String.format(VIEW_PATH, selectedSalesPartnerStore.getId());
    }

    public SalesPartnerStore getSelectedDealerStore() {
        return selectedDealerStore;
    }

    public void setSelectedDealerStore(SalesPartnerStore selectedDealerStore) {
        this.selectedDealerStore = selectedDealerStore;
    }

    public List<SalesPartnerStore> getFilteredDealerStoreList() {
        return filteredDealerStoreList;
    }

    public void setFilteredDealerStoreList(List<SalesPartnerStore> filteredDealerStoreList) {
        this.filteredDealerStoreList = filteredDealerStoreList;
    }

    public String getParameterDealerName() {
        return parameterDealerName;
    }

    public void setParameterDealerName(String parameterDealerName) {
        this.parameterDealerName = parameterDealerName;
    }

    public UIComponent getDealerStoreTable() {
        return dealerStoreTable;
    }

    public void setDealerStoreTable(UIComponent dealerStoreTable) {
        this.dealerStoreTable = dealerStoreTable;
    }

    public LazyDataModel<SalesPartnerStore> getDealerStoreList() {
        if (dealerStoreList == null) {
            searchDealers();
        }
        return dealerStoreList;
    }

    public void setDealerStoreList(LazyDataModel<SalesPartnerStore> dealerStoreList) {
        this.dealerStoreList = dealerStoreList;
    }

    public void searchDistributorStore () {
        if (selectedSalesPartnerStore == null) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "No distributor store found","No distributor store found"));
            return;
        }

        ((DataTable) storeTable).reset();
        if (parameterPartNumberFrom != null && !parameterPartNumberFrom.isEmpty() && parameterPartNumberTo != null && !parameterPartNumberTo.isEmpty()) {
            log.debug(String.format("searchDistributorStore: from=%s, to=%s", parameterPartNumberFrom, parameterPartNumberTo));
            distributorStoreEquipment = null;
            distributorStoreEquipment = new ArrayList<>();

            for (Equipment eq : selectedSalesPartnerStore.getEquipmentList()) {
                if (eq.getPartNumber().compareTo(parameterPartNumberFrom) >= 0
                        && eq.getPartNumber().compareTo(parameterPartNumberTo) <= 0) {
                    log.debug(String.format("searchDistributorStore: partnumber=%s, result=%b", eq.getPartNumber(), eq.getPartNumber().compareTo(parameterPartNumberFrom) >= 0
                            && eq.getPartNumber().compareTo(parameterPartNumberTo) <= 0));
                    distributorStoreEquipment.add(eq);
                }
            }

            log.debug(String.format("searchDistributorStore: size=%d, list=%s", distributorStoreEquipment.size(), distributorStoreEquipment));
        }
    }

    public void resetDistributorStore () {
        distributorStoreEquipment = null;
        getDistributorStoreEquipment();
        parameterPartNumberFrom = null;
        parameterPartNumberTo = null;
    }

    public void searchDealers () {
        if (selectedSalesPartnerStore == null) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "No distributor store found","No distributor store found"));
            return;
        }

        ((DataTable) dealerStoreTable).reset();
        partnerStoreFacade.clearFilters();

        partnerStoreFacade.addFilter(SalesPartnerStorePersistenceFacade.Filter.PARTNER_TYPE, SalesPartnerType.DEALER);
        partnerStoreFacade.addFilter(SalesPartnerStorePersistenceFacade.Filter.PRINCIPAL_ID, selectedSalesPartnerStore.getOwner().getId());

        if (parameterDealerName != null && !parameterDealerName.isEmpty())
            partnerStoreFacade.addFilter(SalesPartnerStorePersistenceFacade.Filter.PARTNER_NAME,parameterDealerName);

        dealerStoreList = new LazyTableModel<>(partnerStoreFacade);
    }

    public String getParameterPartNumberTo() {
        return parameterPartNumberTo;
    }

    public void setParameterPartNumberTo(String parameterPartNumberTo) {
        this.parameterPartNumberTo = parameterPartNumberTo;
    }

    public String getParameterPartNumberFrom() {
        return parameterPartNumberFrom;
    }

    public void setParameterPartNumberFrom(String parameterPartNumberFrom) {
        this.parameterPartNumberFrom = parameterPartNumberFrom;
    }

    public List<Equipment> getDistributorStoreEquipment() {
       if (distributorStoreEquipment == null && selectedSalesPartnerStore != null) {
            this.distributorStoreEquipment = new ArrayList<>(selectedSalesPartnerStore.getEquipmentList());
        }
        return distributorStoreEquipment;
    }

    public void setDistributorStoreEquipment(List<Equipment> distributorStoreEquipment) {
        this.distributorStoreEquipment = distributorStoreEquipment;
    }

    public String transfer () {
        if (selectedSalesPartnerStore == null) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "No distributor store found", "No distributor store found"));
            return null;
        }

        if (selectedDealerStore == null) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Please select target dealer store", "Please select target dealer store"));
            return null;
        }
        if (parameterPartNumberFrom == null || parameterPartNumberFrom.isEmpty() || parameterPartNumberTo == null || parameterPartNumberTo.isEmpty()) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Please provide search criteria", "Please provide search criteria"));
            return null;
        }

        try {
            log.info(String.format("transfer: partnumber from={%s} partnumber to={%s}, total count=%d",
                    parameterPartNumberFrom, parameterPartNumberTo, distributorStoreEquipment.size()));
            log.debug(String.format("transfer: filters from%s, to=%s", parameterPartNumberFrom, parameterPartNumberTo));

            partnerStoreFacade.transfer(distributorStoreEquipment, selectedDealerStore, selectedSalesPartnerStore);
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Equipment transfered sucessfully", "Equipment transfered sucessfully"));

            return String.format(VIEW_PATH, selectedSalesPartnerStore.getId());
        } catch (Exception ex) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Cannot transfer equipment", "Cannot transfer equipment"));
            log.error("transfer: cannot transfer equipment, ");
            return null;
        }
        finally {
            partnerStoreFacade.clearFilters();
        }
    }
}

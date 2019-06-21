package com.jaravir.tekila.ui.jsf.managed.store;

import com.jaravir.tekila.base.filter.MatchingOperation;
import com.jaravir.tekila.base.persistence.facade.AbstractPersistenceFacade;
import com.jaravir.tekila.common.device.DeviceStatus;
import com.jaravir.tekila.equip.EquipmentPersistenceFacade;
import com.jaravir.tekila.module.sales.SalesPartnerType;
import com.jaravir.tekila.module.service.entity.ServiceProvider;
import com.jaravir.tekila.module.service.persistence.manager.ServiceProviderPersistenceFacade;
import com.jaravir.tekila.module.store.SalesPartnerPersistenceFacade;
import com.jaravir.tekila.module.store.SalesPartnerStore;
import com.jaravir.tekila.module.store.SalesPartnerStorePersistenceFacade;
import com.jaravir.tekila.module.store.equip.Equipment;
import com.jaravir.tekila.module.store.equip.EquipmentStatus;
import com.jaravir.tekila.ui.model.LazyTableModel;
import org.apache.log4j.Logger;
import org.primefaces.model.LazyDataModel;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.AjaxBehaviorEvent;
import javax.faces.model.SelectItem;
import java.io.Serializable;
import java.util.*;

/**
 * Created by sajabrayilov on 12/18/2014.
 */
@ManagedBean
@ViewScoped
public class EquipmentManager  implements Serializable {
    private transient final static Logger log = Logger.getLogger(EquipmentManager.class);
    private final String PATH = "/pages/store/equip/";

    @EJB private EquipmentPersistenceFacade equipmentFacade;
    @EJB private ServiceProviderPersistenceFacade providerFacade;
    @EJB private SalesPartnerPersistenceFacade partnerFacade;
    @EJB private SalesPartnerStorePersistenceFacade partnerStoreFacade;

    private LazyDataModel<Equipment> equipmentList;
    private Equipment selected;
    private List<Equipment> filteredEquipmentList;

    //search
    private String partNumber;
    private String partNumberTo;
    private String model;
    private String type;
    private String brand;
    private String macAddress;
    private Long providerID;
    private List<SelectItem> providerSelectList;
    private EquipmentStatus status;
    private List<SelectItem> equipmentStatusList;
    private List<SelectItem> deviceStatusList;
    private Long equipmentID;

    //transfer dialog
    private String distributorStoreName;
    private SalesPartnerStore selectedDistributorStore;
    private LazyDataModel<SalesPartnerStore> distributorStoreList;
    private List<SalesPartnerStore> fitleredDistributorStores;
    private Double transferPrice;

    @PostConstruct
    public void init () {
        equipmentFacade.clearFilters();
        partnerFacade.clearFilters();
        partnerStoreFacade.clearFilters();
    }

    @PreDestroy
    public void cleanUp() {
        equipmentFacade.clearFilters();
        partnerFacade.clearFilters();
        partnerStoreFacade.clearFilters();
    }

    public Long getEquipmentID() {
        return equipmentID;
    }

    public void setEquipmentID(Long equipmentID) {
        this.equipmentID = equipmentID;
    }

    public LazyDataModel<Equipment> getEquipmentList() {
          return equipmentList;
    }

    public void setEquipmentList(LazyDataModel<Equipment> equipmentList) {
        this.equipmentList = equipmentList;
    }

    public Equipment getSelected() {
        if (equipmentID != null)
            selected = equipmentFacade.findForceRefresh(equipmentID);
        return selected;
    }

    public void setSelected(Equipment selected) {
        this.selected = selected;
    }

    public List<Equipment> getFilteredEquipmentList() {
        return filteredEquipmentList;
    }

    public void setFilteredEquipmentList(List<Equipment> filteredEquipmentList) {
        this.filteredEquipmentList = filteredEquipmentList;
    }

    //search

    public String getPartNumber() {
        return partNumber;
    }

    public void setPartNumber(String partNumber) {
        this.partNumber = partNumber;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    public Long getProviderID() {
        return providerID;
    }

    public void setProviderID(Long providerID) {
        this.providerID = providerID;
    }

    public List<SelectItem> getProviderSelectList() {
        if (providerSelectList == null) {
            providerSelectList = new ArrayList<>();

            if (selected != null)
                providerSelectList.add(new SelectItem(selected.getProvider().getId(), selected.getProvider().getName()));

            for (ServiceProvider prov : providerFacade.findAll()) {
                if (selected != null && prov.getId() == selected.getProvider().getId())
                    continue;

                providerSelectList.add(new SelectItem(prov.getId(), prov.getName()));
            }
        }
        return providerSelectList;
    }

    public void setProviderSelectList(List<SelectItem> providerSelectList) {
        this.providerSelectList = providerSelectList;
    }

    public EquipmentStatus getStatus() {
        return status;
    }

    public void setStatus(EquipmentStatus status) {
        this.status = status;
    }

    public List<SelectItem> getEquipmentStatusList() {
        if (equipmentStatusList == null) {
            equipmentStatusList = new ArrayList<>();
            equipmentStatusList.add(new SelectItem(EquipmentStatus.AVAILABLE));

            for (EquipmentStatus st : EquipmentStatus.values()) {
                if (st != EquipmentStatus.AVAILABLE)
                    equipmentStatusList.add(new SelectItem(st));
            }
        }
        return equipmentStatusList;
    }

    public void setEquipmentStatusList(List<SelectItem> equipmentStatusList) {
        this.equipmentStatusList = equipmentStatusList;
    }

    public List<SelectItem> getDeviceStatusList() {
        if (deviceStatusList == null) {
            deviceStatusList = new ArrayList<>();

            if (selected != null)
                deviceStatusList.add(new SelectItem(selected.getDeviceStatus()));

            for (DeviceStatus status : DeviceStatus.values()) {
                if (selected != null && status == selected.getDeviceStatus())
                    continue;

                deviceStatusList.add(new SelectItem(status));
            }
        }
        return deviceStatusList;
    }

    public void setDeviceStatusList(List<SelectItem> deviceStatusList) {
        this.deviceStatusList = deviceStatusList;
    }

    public void search() {
        log.debug("Search entered");
        equipmentFacade.clearFilters();

        if (!check()) {
            log.debug("Fields empty");
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Please provide search criteria", "Please provide search criteria"));
            return;
        }

        if (partNumber != null && !partNumber.isEmpty() && (partNumberTo == null || partNumberTo.isEmpty()))
            equipmentFacade.addFilter(EquipmentPersistenceFacade.Filter.PART_NUMBER, partNumber);
        if (partNumber != null && !partNumber.isEmpty() && partNumberTo != null && !partNumberTo.isEmpty()) {
            Map<String,String> partNumberMap = new HashMap<>();
            partNumberMap.put("from",partNumber);
            partNumberMap.put("to",partNumberTo);
            EquipmentPersistenceFacade.Filter partNumberMapFilter = EquipmentPersistenceFacade.Filter.PART_NUMBER;
            partNumberMapFilter.setOperation(MatchingOperation.BETWEEN_STRING);
            equipmentFacade.addFilter(partNumberMapFilter, partNumberMap);
            equipmentFacade.addOrdering("partNumber", AbstractPersistenceFacade.Ordering.ASC);
        }
        if (model != null && !model.isEmpty())
            equipmentFacade.addFilter(EquipmentPersistenceFacade.Filter.MODEL, model);
        if (brand != null && !brand.isEmpty())
            equipmentFacade.addFilter(EquipmentPersistenceFacade.Filter.BRAND, brand);
        if (type != null && !type.isEmpty())
            equipmentFacade.addFilter(EquipmentPersistenceFacade.Filter.TYPE, type);
        if (providerID != null)
            equipmentFacade.addFilter(EquipmentPersistenceFacade.Filter.PROVIDER, providerID);
        if (status != null)
            equipmentFacade.addFilter(EquipmentPersistenceFacade.Filter.STATUS, status);

        log.debug("Search Filters: " + equipmentFacade.getFilters());
        equipmentList = new LazyTableModel<>(equipmentFacade);

        return;
    }

    private boolean check() {
        log.debug("Search criteriea empty? " +     ((partNumber == null || partNumber.isEmpty())
                        && (model == null || model.isEmpty())
                        && (brand  == null || brand.isEmpty())
                        && (type == null || type.isEmpty())
                        && providerID == null && status == null
        ));
        if (
                (partNumber == null || partNumber.isEmpty())
                && (model == null || model.isEmpty())
                && (brand  == null || brand.isEmpty())
                && (type == null || type.isEmpty())
                && providerID == null && status == null
                ) {
            return false;
        }
        return true;
    }

    public String show() {
        if (selected == null) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Please select equipment","Please select equipment"));
            return null;
        }

        return String.format(
            "%sview.xhtml?equipment=%d&amp;faces-redirect=true&amp;includeViewParams=true",
                PATH, selected.getId());
    }

    public String edit(){
        if (selected == null) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Please select equipment","Please select equipment"));
            return null;
        }

        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Equipment update successfully","Equipment update successfully"));
        equipmentFacade.update(selected);
        return null;
    }

    public String getDistributorStoreName() {
        return distributorStoreName;
    }

    public void setDistributorStoreName(String distributorStoreName) {
        this.distributorStoreName = distributorStoreName;
    }

    public SalesPartnerStore getSelectedDistributorStore() {
        return selectedDistributorStore;
    }

    public void setSelectedDistributorStore(SalesPartnerStore selectedDistributorStore) {
        this.selectedDistributorStore = selectedDistributorStore;
    }

    public LazyDataModel<SalesPartnerStore> getDistributorStoreList() {
        if (distributorStoreList == null) {
            partnerStoreFacade.addFilter(SalesPartnerStorePersistenceFacade.Filter.PARTNER_TYPE, SalesPartnerType.DISTRIBUTOR);
            distributorStoreList = new LazyTableModel<>(partnerStoreFacade);
        }
        return distributorStoreList;
    }

    public void setDistributorStoreList(LazyDataModel<SalesPartnerStore> distributorList) {
        this.distributorStoreList = distributorList;
    }

    public List<SalesPartnerStore> getFitleredDistributorStores() {
        return fitleredDistributorStores;
    }

    public void setFitleredDistributorStores (List<SalesPartnerStore> fitleredDistributors) {
        this.fitleredDistributorStores = fitleredDistributors;
    }

    public String getPartNumberTo() {
        return partNumberTo;
    }

    public void setPartNumberTo(String partNumberTo) {
        this.partNumberTo = partNumberTo;
    }

    public String transfer () throws Exception {
        if (selectedDistributorStore == null) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Please select distributor store","Please select distributor store"));
            return null;
        }

        try {
            if (transferPrice != null && partNumber != null && !partNumber.isEmpty() && partNumberTo != null && !partNumberTo.isEmpty()
                    && equipmentList.getRowCount() > 0 && equipmentList.getRowCount() < 5000) {
                log.info(String.format("transfer: transfer price=%f, partnumber from={%s} partnumber to={%s}, total count=%d",
                        transferPrice, partNumber, partNumberTo, equipmentList.getRowCount()));
                log.debug(String.format("transfer: filters %s", equipmentFacade.getFilters()));
                /*
                if (!equipmentFacade.getFilters().containsKey(EquipmentPersistenceFacade.Filter.PART_NUMBER)) {
                    if (partNumber != null && !partNumber.isEmpty() && partNumberTo != null && !partNumberTo.isEmpty()) {
                        Map<String,String> partNumberMap = new HashMap<>();
                        partNumberMap.put("from",partNumber);
                        partNumberMap.put("to",partNumberTo);
                        EquipmentPersistenceFacade.Filter partNumberMapFilter = EquipmentPersistenceFacade.Filter.PART_NUMBER;
                        partNumberMapFilter.setOperation(MatchingOperation.BETWEEN_STRING);
                        equipmentFacade.addFilter(partNumberMapFilter, partNumberMap);
                        equipmentFacade.addOrdering("partNumber", AbstractPersistenceFacade.Ordering.ASC);
                    }
                }
                log.debug(String.format("transfer: filters after add %s", equipmentFacade.getFilters()));
                */
                List<Equipment> equipList = equipmentFacade.findAllPaginated(0, 100000);
                log.debug(String.format("transfer: transfer price=%f, equipment count to be transfered %d, %s", transferPrice, equipList != null ? equipList.size() : 0,
                        equipList));
                equipmentFacade.transfer(equipList, selectedDistributorStore, transferPrice);

                String dsc = String.format("%d equipment with partnumber from {%s} to {%s} transfered successfully to distributor store %s",
                        equipmentList.getRowCount(), partNumber, partNumberTo, selectedDistributorStore.getName());

                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, dsc, dsc));
                return String.format("/pages/store/distrib/store/view.xhtml?store=%d&amp;includeViewParams=true&amp;faces-redirect=true",
                        selectedDistributorStore.getId());
            }
        }

        catch (Exception ex) {
            log.error(String.format("transfer: cannot transfer partnumber from={%s} partnumber to={%s}, total count=%d",
                    partNumber, partNumberTo, equipmentList.getRowCount()), ex);

            String dsc = String.format("Cannot transfer %d equipment with partnumber from {%s} to {%s} to distributor store %s",
                    equipmentList.getRowCount(), partNumber, partNumberTo, selectedDistributorStore.getName());

            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, dsc, dsc));
        }

        return null;
    }

    public void onEquipmentTransferDialogClosed (AjaxBehaviorEvent ev) {
        resetDistributorList();
        partnerFacade.clearFilters();
        partnerStoreFacade.clearFilters();
    }

    public void onDistributorSearch () {
        //partnerFacade.clearFilters();
        partnerStoreFacade.clearFilters();

        partnerStoreFacade.addFilter(SalesPartnerStorePersistenceFacade.Filter.PARTNER_TYPE, SalesPartnerType.DISTRIBUTOR);

        if (distributorStoreName != null && !distributorStoreName.isEmpty()) {
            partnerStoreFacade.addFilter(SalesPartnerStorePersistenceFacade.Filter.PARTNER_NAME, distributorStoreName);
        }

        distributorStoreList = new LazyTableModel<>(partnerStoreFacade);
        log.debug(String.format("onDistributorSearch: filters=%s", partnerStoreFacade.getFilters().entrySet()));
    }

    public void resetDistributorList () {
        distributorStoreName = null;
        selectedDistributorStore = null;
        fitleredDistributorStores = null;
        onDistributorSearch();
    }

    public Double getTransferPrice() {
        return transferPrice;
    }

    public void setTransferPrice(Double transferPrice) {
        this.transferPrice = transferPrice;
    }
}

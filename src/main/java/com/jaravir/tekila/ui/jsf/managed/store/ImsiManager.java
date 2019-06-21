package com.jaravir.tekila.ui.jsf.managed.store;

import com.jaravir.tekila.base.filter.MatchingOperation;
import com.jaravir.tekila.module.service.entity.ServiceProvider;
import com.jaravir.tekila.module.service.persistence.manager.ServiceProviderPersistenceFacade;
import com.jaravir.tekila.module.store.mobile.Imsi;
import com.jaravir.tekila.module.store.mobile.ImsiPersistenceFacade;
import com.jaravir.tekila.module.store.mobile.MobileCategory;
import com.jaravir.tekila.module.store.status.StoreItemStatus;
import com.jaravir.tekila.ui.model.LazyTableModel;
import org.apache.log4j.Logger;
import org.primefaces.model.LazyDataModel;

import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by sajabrayilov on 13.03.2015.
 */
@ManagedBean
@ViewScoped
public class ImsiManager implements Serializable {
    @EJB private ImsiPersistenceFacade imsiFacade;
    @EJB private ServiceProviderPersistenceFacade providerFacade;

    private static final Logger log = Logger.getLogger(ImsiManager.class);

    private LazyDataModel<Imsi> imsiList;
    private Imsi selected;

    //search
    private String iccid;
    private String imsiIdentifier;
    private List<SelectItem> categoryList;
    private Integer categoryPosition;
    private MobileCategory category;
    private List<SelectItem> statusList;
    private StoreItemStatus status;
    private String msisdnValue;
    private List<SelectItem> providerList;
    private Long providerID;

    private Long imsiID;

    public LazyDataModel<Imsi> getImsiList() {
        return imsiList;
    }

    public void setImsiList(LazyDataModel<Imsi> imsiList) {
        this.imsiList = imsiList;
    }

    public Imsi getSelected() {
        if (selected == null && imsiID != null) {
            selected = imsiFacade.find(imsiID);
        }
        return selected;
    }

    public void setSelected(Imsi selected) {
        this.selected = selected;
    }

    public String getIccid() {
        return iccid;
    }

    public void setIccid(String iccid) {
        this.iccid = iccid;
    }

    public String getImsiIdentifier() {
        return imsiIdentifier;
    }

    public void setImsiIdentifier(String imsiIdentifier) {
        this.imsiIdentifier = imsiIdentifier;
    }

    public List<SelectItem> getCategoryList() {
        if (categoryList == null) {
            categoryList = new ArrayList<>();

            for (MobileCategory cat : MobileCategory.values())
                categoryList.add(new SelectItem(cat));
        }
        return categoryList;
    }

    public void setCategoryList(List<SelectItem> categoryList) {
        this.categoryList = categoryList;
    }

    public MobileCategory getCategory() {
        return category;
    }

    public void setCategory(MobileCategory category) {
        this.category = category;
    }

    public List<SelectItem> getStatusList() {
        if (statusList == null) {
            statusList = new ArrayList<>();

            for (StoreItemStatus st : StoreItemStatus.values())
                statusList.add(new SelectItem(st));
        }
        return statusList;
    }

    public void setStatusList(List<SelectItem> statusList) {
        this.statusList = statusList;
    }

    public StoreItemStatus getStatus() {
        return status;
    }

    public void setStatus(StoreItemStatus status) {
        this.status = status;
    }

    public String getMsisdnValue() {
        return msisdnValue;
    }

    public void setMsisdnValue(String msisdnValue) {
        this.msisdnValue = msisdnValue;
    }

    public Long getProviderID() {
        return providerID;
    }

    public void setProviderID(Long providerID) {
        this.providerID = providerID;
    }

    public List<SelectItem> getProviderList() {
        if (providerList == null) {
            providerList = new ArrayList<>();

            for (ServiceProvider pr : providerFacade.findAll())
                providerList.add(new SelectItem(pr.getId(), pr.getName()));
        }
        return providerList;
    }

    public void setProviderList(List<SelectItem> providerList) {
        this.providerList = providerList;
    }

    public Long getImsiID() {
        return imsiID;
    }

    public void setImsiID(Long imsiID) {
        this.imsiID = imsiID;
    }

    public Integer getCategoryPosition() {
        return categoryPosition;
    }

    public void setCategoryPosition(Integer categoryPosition) {
        this.categoryPosition = categoryPosition;
    }

    public boolean check () {
        if (
                (iccid == null || iccid.isEmpty())
                && (imsiIdentifier == null || imsiIdentifier.isEmpty())
                && (msisdnValue == null || msisdnValue.isEmpty())
                && category == null && status == null && providerID == null
           )
            return false;

        return true;
    }


    public void search () {
        imsiFacade.clearFilters();

        if (!check()) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Please fill any field"));
            return;
        }

        if (iccid != null && !iccid.isEmpty()) {
            ImsiPersistenceFacade.Filter iccidFilter = ImsiPersistenceFacade.Filter.ICCID;
            iccidFilter.setOperation(MatchingOperation.LIKE);
            imsiFacade.addFilter(iccidFilter, iccid);
        }

        if (imsiIdentifier != null && !imsiIdentifier.isEmpty()) {
            ImsiPersistenceFacade.Filter imsiFilter = ImsiPersistenceFacade.Filter.IDENTIFIER;
            imsiFilter.setOperation(MatchingOperation.LIKE);
            imsiFacade.addFilter(imsiFilter, imsiIdentifier);
        }

        if (msisdnValue != null && !msisdnValue.isEmpty()) {
            ImsiPersistenceFacade.Filter msisdnFilter = ImsiPersistenceFacade.Filter.MSISDN;
            msisdnFilter.setOperation(MatchingOperation.LIKE);
            imsiFacade.addFilter(msisdnFilter, msisdnValue);
        }

        if (category != null) {
            imsiFacade.addFilter(ImsiPersistenceFacade.Filter.CATEGORY, category);
        }

        if (status != null) {
            imsiFacade.addFilter(ImsiPersistenceFacade.Filter.STATUS, status);
        }

        if (providerID != null)
            imsiFacade.addFilter(ImsiPersistenceFacade.Filter.PROVIDER, providerID);

        imsiList = new LazyTableModel<>(imsiFacade);
    }

    public String show() {
        if (selected == null) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Please select item","Please select item"));
        }

        return String.format("/pages/store/mobile/imsi/view.xhtml?imsi=%d&amp;faces-redirect=true&amp;includeViewParams=true", selected.getId());
    }

    public void edit () {
        if (selected == null) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "IMSI unknown", "IMSI unknown"));
            return;
        }

        try {
            selected = imsiFacade.update(selected);
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "IMSI unknown", "IMSI unknown"));
        }
        catch (Exception ex) {
            log.error("Cannot update imsi " + selected, ex);
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "IMSI cannot be update", "IMSI cannot be update"));
        }
    }
}


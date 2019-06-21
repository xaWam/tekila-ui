package com.jaravir.tekila.ui.jsf.managed.store;

import com.jaravir.tekila.base.auth.persistence.manager.UserPersistenceFacade;
import com.jaravir.tekila.base.entity.Util;
import com.jaravir.tekila.base.filter.MatchingOperation;
import com.jaravir.tekila.module.service.entity.ServiceProvider;
import com.jaravir.tekila.module.service.persistence.manager.ServiceProviderPersistenceFacade;
import com.jaravir.tekila.module.store.mobile.*;
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

import static com.jaravir.tekila.module.store.mobile.MsidnPersistenceFacade.Filter;
/**
 * Created by sajabrayilov on 18.03.2015.
 */
@ManagedBean
@ViewScoped
public class MsisdnManager implements Serializable {
    @EJB private MsidnPersistenceFacade msisdnFacade;
    @EJB private ServiceProviderPersistenceFacade providerFacade;
    @EJB private UserPersistenceFacade userFacade;
    @EJB private CommercialCategoryPersistenceFacade comFacade;

    private final static Logger log = Logger.getLogger(MsisdnManager.class);

    private MobileCategory category;
    private List<SelectItem> categoryList;
    private List<SelectItem> statusList;
    private StoreItemStatus status;
    private List<SelectItem> providerList;
    private Long providerID;
    private String msisdnValue;
    private LazyDataModel<Msisdn> msisdnList;
    private Msisdn selected;
    private Long msisdnID;
    private List<SelectItem> comCatList;
    private Long comCatID;

    private String priceWholeAmount;
    private String priceDecimalAmount;
    private Boolean isRemovePriceSetting;

    public MobileCategory getCategory() {
        return category;
    }

    public void setCategory(MobileCategory category) {
        this.category = category;
    }

    public Boolean getIsRemovePriceSetting() {
        return isRemovePriceSetting;
    }

    public void setIsRemovePriceSetting(Boolean isRemovePriceSetting) {
        this.isRemovePriceSetting = isRemovePriceSetting;
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

    public List<SelectItem> getProviderList() {
        if (providerList == null) {
            providerList = new ArrayList<>();

            List<ServiceProvider> provList = providerFacade.findAll();

            for (ServiceProvider prov : provList)
                providerList.add(new SelectItem(prov.getId(), prov.getName()));
        }
        return providerList;
    }

    public void setProviderList(List<SelectItem> providerList) {
        this.providerList = providerList;
    }

    public Long getProviderID() {
        return providerID;
    }

    public void setProviderID(Long providerID) {
        this.providerID = providerID;
    }

    public String getMsisdnValue() {
        return msisdnValue;
    }

    public void setMsisdnValue(String msisdnValue) {
        this.msisdnValue = msisdnValue;
    }

    public LazyDataModel<Msisdn> getMsisdnList() {
        return msisdnList;
    }

    public void setMsisdnList(LazyDataModel<Msisdn> msisdnList) {
        this.msisdnList = msisdnList;
    }

    public String getPriceWholeAmount() {
        if (priceWholeAmount == null && msisdnID != null
              && getSelected().getPrice() != null
                ) {
            String[] am = Util.convertFromLongToStringArray(getSelected().getPrice());
            priceWholeAmount = am[0];
        }
        return priceWholeAmount;
    }

    public void setPriceWholeAmount(String priceWholeAmount) {
        this.priceWholeAmount = priceWholeAmount;
    }

    public String getPriceDecimalAmount() {
        if (priceDecimalAmount == null && msisdnID != null
                && getSelected().getPrice() != null
                ) {
            String[] am = Util.convertFromLongToStringArray(getSelected().getPrice());

            if (am.length > 1)
                priceDecimalAmount = am[1];
        }
        return priceDecimalAmount;
    }

    public void setPriceDecimalAmount(String priceDecimalAmount) {
        this.priceDecimalAmount = priceDecimalAmount;
    }

    public Msisdn getSelected() {
        if (selected == null && msisdnID != null) {
            selected = msisdnFacade.find(msisdnID);
        }
        return selected;
    }

    public void setSelected(Msisdn selected) {
        this.selected = selected;
    }

    public Long getMsisdnID() {
        return msisdnID;
    }

    public void setMsisdnID(Long msisdnID) {
        this.msisdnID = msisdnID;
    }

    public List<SelectItem> getComCatList() {
        if (comCatList == null) {
            comCatList = new ArrayList<>();

            List<CommercialCategory> cl = comFacade.findAll();

            for (CommercialCategory cat : cl) {
                comCatList.add(new SelectItem(cat.getId(), cat.getName()));
            }
        }
        return comCatList;
    }

    public void setComCatList(List<SelectItem> comCatList) {
        this.comCatList = comCatList;
    }

    public Long getComCatID() {
        if (comCatID == null && msisdnID != null && getSelected().getCommercialCategory() != null) {
            comCatID = getSelected().getCommercialCategory().getId();
        }
        return comCatID;
    }

    public void setComCatID(Long comCatID) {
        this.comCatID = comCatID;
    }

    public boolean check () {
        return ((msisdnValue != null && !msisdnValue.isEmpty()) || providerID != null
            || status != null || category != null || comCatID != null);
    }

    public void search () {
        if (!check()) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Please fill al least one field","Please fill al least one field"));
            return;
        }

        msisdnFacade.clearFilters();

        if (msisdnValue != null && !msisdnValue.isEmpty()) {
            Filter msisdnValueFilter = Filter.VALUE;
            msisdnValueFilter.setOperation(MatchingOperation.LIKE);
            msisdnFacade.addFilter(msisdnValueFilter, msisdnValue);
        }

        if (category != null) {
            msisdnFacade.addFilter(Filter.CATEGORY, category);
        }

        if (comCatID != null) {
            msisdnFacade.addFilter(Filter.COMMERCIAL_CATEGORY, comCatID);
        }

        if (status != null) {
            msisdnFacade.addFilter(Filter.STATUS, status);
        }

        if (providerID != null) {
            msisdnFacade.addFilter(ImsiPersistenceFacade.Filter.PROVIDER, providerID);
        }

        msisdnList = new LazyTableModel<>(msisdnFacade);
    }

    public String show () {
        if (selected == null) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Please select MSISDN","Please select MSISDN"));
            return null;
        }

        return String.format("/pages/store/mobile/msisdn/view.xhtml?msisdn=%d&amp;faces-redirect=true&amp;includeViewParams=true", selected.getId());
    }

    public void edit() {
        if (selected == null) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Uknown MSISDN","Uknown MSISDN"));
            return;
        }

        try {
            //update price setting
            if (isRemovePriceSetting) {
                selected.removePriceSetting();
            }

            else if (priceWholeAmount != null) {
                Long newPrice = Util.parseAmountFromStringIntoLong(priceWholeAmount, priceDecimalAmount);

                if (!newPrice.equals(selected.getPrice())) {
                    selected.setPrice(newPrice);
                }
            }

            //update commercial category
            if (comCatID != null) {
                CommercialCategory commercialCategory = comFacade.find(comCatID);

                if (commercialCategory != null)
                    selected.setCommercialCategory(commercialCategory);
            }

            selected = msisdnFacade.update(selected);
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "MSISDN successfully update","MSISDN successfully updated"));
        }
        catch (Exception ex) {
            log.debug(String.format("Cannot update msisdn %s", selected), ex);
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Cannot update MSISDN", "Cannot update MSISDN"));
        }
    }
}

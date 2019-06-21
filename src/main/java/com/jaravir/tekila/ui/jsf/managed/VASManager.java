/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.jaravir.tekila.ui.jsf.managed;

import com.jaravir.tekila.base.persistence.manager.BillingSettingsManager;
import com.jaravir.tekila.module.accounting.entity.TaxationCategory;
import com.jaravir.tekila.module.accounting.manager.InvoicePersistenceFacade;
import com.jaravir.tekila.module.accounting.manager.TaxCategoryPeristenceFacade;
import com.jaravir.tekila.module.service.ResourceBucketType;
import com.jaravir.tekila.module.service.ServiceType;
import com.jaravir.tekila.module.service.VASSetting;
import com.jaravir.tekila.module.service.ValueAddedServiceType;
import com.jaravir.tekila.module.service.entity.Resource;
import com.jaravir.tekila.module.service.entity.ResourceBucket;
import com.jaravir.tekila.module.service.entity.ServiceProvider;
import com.jaravir.tekila.module.service.entity.VASCode;
import com.jaravir.tekila.module.service.entity.ValueAddedService;
import com.jaravir.tekila.module.service.persistence.manager.*;
import com.jaravir.tekila.module.store.ChargeItemRegister;
import com.jaravir.tekila.module.store.contract.ChargeableItem;
import com.jaravir.tekila.module.subscription.persistence.management.SubscriberPersistenceFacade;
import com.jaravir.tekila.module.subscription.persistence.management.SubscriptionPersistenceFacade;
import com.jaravir.tekila.ui.model.LazyTableModel;

import javax.annotation.PostConstruct;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ActionEvent;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;
import javax.faces.event.AjaxBehaviorEvent;
import javax.faces.model.SelectItem;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormat;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.LazyDataModel;


/**
 * :#{p:component('resourcePanel')}
 *
 * @author sajabrayilov
 */
@ViewScoped
@ManagedBean(name = "vasManager")
public class VASManager implements Serializable {
    private transient static final Logger log = Logger.getLogger(VASManager.class);
    private static final String PATH = "/pages/service/vas/";
    private final static ChargeItemRegister chargeableItemRegister = ChargeItemRegister.getInstance();

    @EJB
    private VASPersistenceFacade vasFacade;
    @EJB
    private ServiceProviderPersistenceFacade providerFacade;
    @EJB
    private TaxCategoryPeristenceFacade taxCategoryFacade;
    @EJB
    private VASCodePersistenceFacade vasCodeFacade;
    @EJB
    private ServiceGroupPersistenceFacade groupFacade;
    @EJB
    private InvoicePersistenceFacade invoiceFacade;
    @EJB
    private SubscriberPersistenceFacade subscriberFacade;
    @EJB
    private SubscriptionPersistenceFacade subscriptionFacade;
    @EJB
    private BillingSettingsManager billSettings;
    @EJB
    private ResourcePersistenceFacade resourceFacade;
    @EJB
    private ResourceBucketPersistenceFacade bucketFacade;

    private List<VASCode> codeList;
    private LazyDataModel<VASCode> vasCodeList;
    private List<VASCode> filteredVasCodeList;
    private VASCode selectedVasCode;

    private ValueAddedService vas;
    private List<SelectItem> providerList;
    private String providerID;
    private List<SelectItem> taxCategoryList;
    private String taxCategoryID;
    private Resource resource;
    private List<ResourceBucket> bucketList;
    private boolean showResources;

    private ValueAddedService selectedVas;
    private List<ValueAddedService> allVasList;
    private List<ValueAddedService> filteredVasList;
    private Long vasId;
    private Long subscriptionID;
    private Long subscriberID;
    private List<ValueAddedService> vasListStatic;
    private ValueAddedService selectedVasFromSubscriber;

    private VASSetting selectedSetting;
    private VASSetting setting;

    private List<SelectItem> chargeableItemList;
    private ChargeableItem chargeableItem;
    private String resourceBucketName;
    private List<SelectItem> bucketTypes;
    private ResourceBucketType bucketType;
    private double vasPrice;

    @PostConstruct
    public void init() {
        vasFacade.clearFilters();
    }

    public ValueAddedService getVas() {
        if (vas == null) {
            vas = new ValueAddedService();
        }
        return vas;
    }

    public void setVas(ValueAddedService vas) {
        this.vas = vas;
    }

    public List<SelectItem> getProviderList() {
        if (providerList == null) {
            List<ServiceProvider> providers = providerFacade.findAll();

            if (!providers.isEmpty()) {
                providerList = new ArrayList<SelectItem>();

                for (ServiceProvider prov : providers) {
                    providerList.add(new SelectItem(prov.getId(), prov.getName()));
                }
            }
        }
        return providerList;
    }

    public void setProviderList(List<SelectItem> providerList) {
        this.providerList = providerList;
    }

    public List<SelectItem> getTaxCategoryList() {
        if (taxCategoryList == null) {
            List<TaxationCategory> taxCats = taxCategoryFacade.findAll();

            if (!taxCats.isEmpty()) {
                taxCategoryList = new ArrayList<>();

                for (TaxationCategory cat : taxCats) {
                    taxCategoryList.add(new SelectItem(cat.getId(), cat.getName()));
                }
            }
        }
        return taxCategoryList;
    }

    public void setTaxCategoryList(List<SelectItem> taxCategoryList) {
        this.taxCategoryList = taxCategoryList;
    }

    public ServiceType[] getServiceTypeList() {
        return ServiceType.values();
    }

    public ValueAddedServiceType[] getVASTypeList() {
        return ValueAddedServiceType.values();
    }

    public String getProviderID() {
        return providerID;
    }

    public void setProviderID(String providerID) {
        this.providerID = providerID;
    }

    public String getTaxCategoryID() {
        return taxCategoryID;
    }

    public void setTaxCategoryID(String taxCategoryID) {
        this.taxCategoryID = taxCategoryID;
    }


    public Resource getResource() {
        return resource;
    }

    public void setResource(Resource resource) {
        this.resource = resource;
    }

    public List<ResourceBucket> getBucketList() {
        if (bucketList == null) {
            bucketList = new ArrayList<>();
            bucketList.add(new ResourceBucket());
        }
        return bucketList;
    }

    public void setBucketList(List<ResourceBucket> bucketList) {
        this.bucketList = bucketList;
    }

    public LazyDataModel<VASCode> getVasCodeList() {
        if (vasCodeList == null) {
            vasCodeList = new LazyTableModel<>(vasCodeFacade);
        }
        return vasCodeList;
    }

    public ChargeableItem getChargeableItem() {
        return chargeableItem;
    }

    public void setChargeableItem(ChargeableItem chargeableItem) {
        this.chargeableItem = chargeableItem;
    }

    public List<SelectItem> getChargeableItemList() {
        if (chargeableItemList == null) {
            chargeableItemList = new ArrayList<>();

            for (String item : chargeableItemRegister.getKeys()) {
                chargeableItemList.add(new SelectItem(item));
            }
        }
        return chargeableItemList;
    }

    public void setChargeableItemList(List<SelectItem> chargeableItemList) {
        this.chargeableItemList = chargeableItemList;
    }

    public void setVasCodeList(LazyDataModel<VASCode> vasCodeList) {
        this.vasCodeList = vasCodeList;
    }

    public List<VASCode> getFilteredVasCodeList() {
        return filteredVasCodeList;
    }

    public void setFilteredVasCodeList(List<VASCode> filteredVasCodeList) {
        this.filteredVasCodeList = filteredVasCodeList;
    }

    public VASCode getSelectedVasCode() {
        return selectedVasCode;
    }

    public void setSelectedVasCode(VASCode selectedVasCode) {
        this.selectedVasCode = selectedVasCode;
    }

    public List<VASCode> getCodeList() {
        if (codeList == null) {
            codeList = vasCodeFacade.findAll();
        }
        return codeList;
    }

    public void setCodeList(List<VASCode> codeList) {
        this.codeList = codeList;
    }


    public void onRowSelect(SelectEvent ev) {
        log.debug("Row select event: selectedVasCode=" + selectedVasCode + ", object=" + (VASCode) ev.getObject());
        if (selectedVasCode != null && (selectedVasCode.getType() == ValueAddedServiceType.PERIODIC_STATIC || selectedVasCode.getType() == ValueAddedServiceType.PERIODIC_DYNAMIC)) {
            showResources = true;
        } else {
            if (showResources)
                showResources = false;
        }
    }

    public boolean isShowResources() {
        return showResources;
    }

    public void setShowResources(boolean showResources) {
        this.showResources = showResources;
    }

    public void addResource(AjaxBehaviorEvent ev) {
        this.bucketList.add(new ResourceBucket());
    }

    public void addResourceNA() {
        bucketList.add(new ResourceBucket());
    }

    public void removeResource(ResourceBucket bucket) {
        if (!getBucketList().isEmpty()) {
            getBucketList().remove(bucket);
        }
    }

    public ValueAddedService getSelectedVas() {
        if (selectedVas == null && vasId != null) {
            selectedVas = vasFacade.find(vasId);
        }
        return selectedVas;
    }

    public void setSelectedVas(ValueAddedService selectedVas) {
        this.selectedVas = selectedVas;
    }

    public List<ValueAddedService> getAllVasList() {
        if (allVasList == null) {
            allVasList = vasFacade.findAll();
        }
        return allVasList;
    }

    public void setAllVasList(List<ValueAddedService> allVasList) {
        this.allVasList = allVasList;
    }

    public List<ValueAddedService> getFilteredVasList() {
        return filteredVasList;
    }

    public void setFilteredVasList(List<ValueAddedService> filteredVasList) {
        this.filteredVasList = filteredVasList;
    }

    public String create() {
        if (vas == null) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "VAS not recognized", "VAS not recognized"));
            return null;
        }

        if (selectedVasCode == null) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Code must be selected", "Code must be selected"));
            return null;
        }

        if (selectedVasCode.getType() == ValueAddedServiceType.PERIODIC_DYNAMIC && bucketType == null) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Please select bucket type", "Please select bucket type"));
            return null;
        }

        log.debug(String.format("create: vas=%s, code=%s, chargeableItem=%s", vas.getPrice(), vas.getCode(), vas.getChargeableItem()));
        if (selectedVasCode.getType() == ValueAddedServiceType.ONETIME_VARIABLE_CHARGE && (vas.getSettings() == null || vas.getSettings().isEmpty()
                //|| vas.getExpression() == null || vas.getExpression().isEmpty()
        )) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(
                    FacesMessage.SEVERITY_ERROR, "Please either select a Rate profile or add setting named 'price'",
                    "Please either select a Rate profile or add setting named 'price'"));
            return null;
        }

        try {
            vas.setCode(selectedVasCode);
            vas.setProvider(selectedVasCode.getProvider());
            vas.setPriceInDouble(vasPrice);
            TaxationCategory taxCat = taxCategoryFacade.find(Long.valueOf(taxCategoryID));
            vas.setCategory(taxCat);


            if (vas.getCode().getType() == ValueAddedServiceType.PERIODIC_DYNAMIC) {
                log.debug("VAS Dynamic debug");
                Resource res = new Resource();
                res.setActiveThroughWeek();
                res.setActiveTill(LocalTime.parse("23:59:59", DateTimeFormat.forPattern("HH:mm:ss")));
                res.setActiveFrom(LocalTime.parse("00:00:00", DateTimeFormat.forPattern("HH:mm:ss")));
                res.setExpirationDate(DateTime.parse("9999-12-31 23:59:59", DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")));
                res.setName("VAS " + vas.getName());
                res.setDsc("Resource for VAS " + vas.getName());

                /*if (bucketList.size() > 0) {
                    for (ResourceBucket bucket : bucketList) {
                        if (bucket.getType() != null && bucket.getCapacity() != null) {
                            res.addBucket(bucket);
                        }
                    }
                }*/
                ResourceBucket bucket = new ResourceBucket();
                bucket.setType(bucketType);

                res.addBucket(bucket);

                vas.setResource(res);
            }

            if (vas.getSettings() != null && !vas.getName().isEmpty()) {
                VASSetting stFound = null;

                for (VASSetting set : vas.getSettings()) {
                    stFound = vasFacade.findSettingByName(set.getName());
                    if (stFound != null) {
                        vas.removeSetting(set);
                        vas.addSetting(stFound);
                    } else {
                        vasFacade.saveSetting(set);
                    }
                }
            }
            log.debug("create: chargeAbleItem=" + vas.getChargeableItem());

            vasFacade.save(vas);
            return PATH + "index";
        } catch (Exception ex) {
            log.error("Cannot create VAS: ", ex);
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Cannot create VAS", "Cannot create VAS"));
            return null;
        }
    }

    public String edit() {
        if (selectedVas == null) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Please select a VAS", "Please select a VAS"));
            return null;
        }

        if (selectedVas.getCode().getType() == ValueAddedServiceType.ONETIME_VARIABLE_CHARGE && (selectedVas.getSettings() == null || selectedVas.getSettings().isEmpty()
                //|| selectedVas.getExpression() == null) || selectedVas.getExpression().isEmpty()
        )) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(
                    FacesMessage.SEVERITY_ERROR, "Please either select a Rate profile or add setting named 'price'",
                    "Please either select a Rate profile or add setting named 'price'"));
            return null;
        }

        try {

            vasFacade.edit(selectedVas, bucketType);
            return PATH + "index";
        } catch (Exception ex) {
            log.error("Cannot update VAS " + selectedVas, ex);
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Cannot update VAS", "Cannot update VAS"));
            return null;
        }
    }

    public Long getVasId() {
        return vasId;
    }

    public void setVasId(Long vasId) {
        this.vasId = vasId;
    }

    public Long getSubscriptionID() {
        return subscriptionID;
    }

    public void setSubscriptionID(Long subscriptionID) {
        this.subscriptionID = subscriptionID;
    }

    public List<ValueAddedService> getVasListStatic() {
        if (vasListStatic == null) {
            vasListStatic = vasFacade.findAll();
        }
        return vasListStatic;
    }

    public void setVasListStatic(List<ValueAddedService> vasListStatic) {
        this.vasListStatic = vasListStatic;
    }

    public ValueAddedService getSelectedVasFromSubscriber() {
        return selectedVasFromSubscriber;
    }

    public void setSelectedVasFromSubscriber(ValueAddedService selectedVasFromSubscriber) {
        this.selectedVasFromSubscriber = selectedVasFromSubscriber;
    }

    public Long getSubscriberID() {
        return subscriberID;
    }

    public void setSubscriberID(Long subscriberID) {
        this.subscriberID = subscriberID;
    }

    public VASSetting getSelectedSetting() {
        return selectedSetting;
    }

    public void setSelectedSetting(VASSetting selectedSetting) {
        this.selectedSetting = selectedSetting;
    }

    public VASSetting getSetting() {
        if (setting == null)
            setting = new VASSetting();
        return setting;
    }

    public void setSetting(VASSetting setting) {
        this.setting = setting;
    }

    public String addVasToSubscription() {
        if (selectedVas == null) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "VAS must be selected", "VAS must be selected"));
            return null;
        }
        if (subscriptionID == null) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Subscription id must be provided", "Subscription id must be provided"));
            return null;
        }
        try {
            HttpSession session = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);
            String desc = String.format("Charged for VAS %d - %s", selectedVas.getCode().getCode(), selectedVas.getName());
            long userID = (Long) session.getAttribute("userID");
            long rate = selectedVas.getPrice() * billSettings.getSettings().getRateFactor();
            String msg = String.format("VAS %d - %s added successfully to subscription %d", selectedVas.getCode().getCode(), selectedVas.getName(), subscriptionID);
            vasFacade.addService(selectedVas, subscriptionID, rate, userID, desc);
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, msg, msg));
            return "/pages/subscriber/ind/charges.xhtml?subscriber=" + subscriberID + "&amp;faces-redirect=true&amp;includeViewParams=true";
        } catch (Exception ex) {
            log.error(String.format("VAS %s not added to subscription %d", selectedVas, subscriptionID), ex);
            String msg = String.format("Could not add VAS %d - %s to subscription %d", selectedVas.getCode().getCode(), selectedVas.getName(), subscriptionID);
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, msg, msg));
            return null;
        }
    }


    public String view() {
        if (selectedVas == null) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Please select a value added service", "Please select a value added service")
            );
            return null;
        }

        return PATH + "view.xhtml?vas=" + selectedVas.getId() + "&amp;faces-redirect=true&amp;includeViewParams=true";
    }

    public void addSetting(ActionEvent event) {
        log.debug("addSetting: Setting name:");
        if (getSetting().getName() == null) {
            return;
        }

        ValueAddedService vasService = vasId != null ? getSelectedVas() : getVas();

        log.debug("addSetting: vasService=" + vasService);
        if (vasService == null)
            return;

        vasService.addSetting(setting);
        setting = null;
    }

    public void addExpressionSetting(ActionEvent ev) {
        setting = new VASSetting("expression", "Expression for charge calculation");
        ValueAddedService vasService = vasId != null ? getSelectedVas() : getVas();

        log.debug("addSetting: vasService=" + vasService);
        if (vasService == null)
            return;

        vasService.addSetting(setting);
        setting = null;
    }

    public void deleteSetting(ActionEvent ev) {
        if (selectedSetting == null) {
            return;
        }

        ValueAddedService vasService = vasId != null ? getSelectedVas() : getVas();

        if (vasService == null)
            return;

        vasService.removeSetting(selectedSetting);
        selectedSetting = null;
        setting = null;
    }

    public void resetSetting(ActionEvent event) {
        selectedSetting = null;
        setting = null;
    }

    public void onSettingsRowSelect(SelectEvent event) {
        VASSetting vasSetting = selectedSetting;
        log.debug("onSettingsRowSelect: selectedSetting=" + selectedSetting);
        if (selectedSetting == null)
            return;

        setSetting(vasSetting);
        log.debug("onSettingsRowSelectL  setting=" + getSetting());
    }

    public String getResourceBucketName() {
        return resourceBucketName;
    }

    public void setResourceBucketName(String resourceBucketName) {
        this.resourceBucketName = resourceBucketName;
    }

    public List<SelectItem> getBucketTypes() {
        if (bucketTypes == null) {
            bucketTypes = new ArrayList<>();

            for (ResourceBucketType type : ResourceBucketType.values())
                bucketTypes.add(new SelectItem(type));
        }
        return bucketTypes;
    }

    public void setBucketTypes(List<SelectItem> bucketTypes) {
        this.bucketTypes = bucketTypes;
    }

    public void setBucketType(ResourceBucketType resourceBucketType) {
        this.bucketType = resourceBucketType;
    }

    public ResourceBucketType getBucketType() {
        if (bucketType == null && selectedVas != null && selectedVas.getCode().getType() == ValueAddedServiceType.PERIODIC_STATIC) {
            if (selectedVas.getResource() != null && selectedVas.getResource().getBucketList() != null
                    && selectedVas.getResource().getBucketList().size() > 0 && selectedVas.getResource().getBucketList().get(0) != null)
                bucketType = selectedVas.getResource().getBucketList().get(0).getType();
        }
        log.debug("getBucketType: " + bucketType);

        return bucketType;
    }

    public double getVasPrice() {
        return vasPrice;
    }

    public void setVasPrice(double vasPrice) {
        this.vasPrice = vasPrice;
    }
}

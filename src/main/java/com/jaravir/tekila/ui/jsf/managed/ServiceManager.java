package com.jaravir.tekila.ui.jsf.managed;

import com.jaravir.tekila.base.filter.MatchingOperation;
import com.jaravir.tekila.module.event.BillingEvent;
import com.jaravir.tekila.module.event.notification.channell.NotificationChannell;
import com.jaravir.tekila.module.service.*;
import com.jaravir.tekila.module.service.entity.*;
import com.jaravir.tekila.module.service.persistence.manager.*;
import com.jaravir.tekila.module.subscription.persistence.entity.SubscriptionStatus;
import com.jaravir.tekila.module.subscription.persistence.entity.transition.StatusChangeRule;

import java.awt.event.ActionEvent;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import javax.ejb.EJB;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.AjaxBehaviorEvent;
import javax.faces.model.SelectItem;
import javax.servlet.http.HttpSession;

import com.jaravir.tekila.module.web.soap.ValueAddedServiceResponse;
import com.jaravir.tekila.provision.broadband.devices.manager.Providers;
import com.jaravir.tekila.provision.exception.ProvisionerNotFoundException;
import com.jaravir.tekila.ui.model.LazyTableModel;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormat;
import org.primefaces.event.RowEditEvent;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.LazyDataModel;

@ManagedBean(name = "serviceManager")
@ViewScoped
public class ServiceManager implements Serializable {
    private transient final static Logger log = Logger.getLogger(ServiceManager.class);
    private final static String SERVICE_PATH = "/pages/service/";
    private final static String SESSION_SERVICE_SELECTED = "service.selected";

    private static final long serialVersionUID = -5973997531249470344L;
    @EJB
    private ServicePersistenceFacade serviceFacade;
    @EJB
    private ResourcePersistenceFacade resourceFacade;
    @EJB
    private ZonePersistenceFacade zoneFacade;
    @EJB
    private ProfilePersistenceFacade profilePersistenceFacade;
    @EJB
    private ServiceProviderPersistenceFacade providerFacade;
    @EJB
    private ServiceSettingPersistenceFacade seviceSettingFacade;
    @EJB
    private ServiceGroupPersistenceFacade groupFacade;
    @EJB
    private NotificationSettingPersistenceFacade notifSettingFacade;
    @EJB
    private VASPersistenceFacade vasFacade;
    @EJB
    private ServiceProfilePersistenceFacade serviceProfileFacade;
    @EJB
    private ResourceBucketPersistenceFacade bucketFacade;
    @EJB
    private ServiceSubgroupPersistenceFacade subgroupFacade;

    private List<SelectItem> providerList;
    private String providerIdForView;
    private List<SelectItem> providerListForView;

    private Long subgroupId;
    private List<ServiceSubgroup> subgroupList;

    private Long serviceId;
    private Service srv;
    private String profileId;
    private String profileIdForView;

    private List<SelectItem> serviceTypeSelectList;
    private List<SelectItem> resourceSelectList;
    private List<String> resourceIdList;
    private List<ServiceProfile> serviceProfileList;
    private ServiceProfile selectedServiceProfile;

    private List<ServiceProperty> serviceProperties;
    private ServiceProperty serviceProperty;
    private double propertyPrice;
    private List<SelectItem> zoneSelectList;
    private Long zoneId;

    private Double ratioUp;
    private Double ratioDown;
    private Date from;
    private Date to;
    private List<Service> serviceList;
    private List<Service> filteredServiceList;
    private Service selected;
    private List<ResourceBucket> resourceBucketList;
    private boolean isBroadBand;
    private String unit;
    private String upload;
    private String download;
    private String burstDownload = "512k";
    private String burstUpload = "512k";
    private List<ServiceSetting> serviceSettingList;
    private List<SelectItem> groupSelectList;
    private Long groupId;

    private ServiceType serviceType;

    private List<NotificationSettingRow> notificationSettings;

    private LazyDataModel defaultVASList;
    private List<ValueAddedService> selectedDefaultVASList;
    private List<SelectItem> vasTypeList;
    private ValueAddedServiceType selectedDefaultVasType;
    private String defaultVasName;
    private List<ValueAddedService> allowedVASList;
    private List<ValueAddedService> selectedAllowedVASList;

    //status change rules
    private StatusChangeRule selectedRule;
    private LazyTableModel<ValueAddedService> lazyVasList;
    private ValueAddedService selectedVAS;
    private String filterVasName;
    private ValueAddedServiceType filterVasType;
    private SubscriptionStatus filterInitSTatus;
    private SubscriptionStatus filterFinalStatus;
    private List<SelectItem> statusList;
    private Long installFeeProfileID;
    private ValueAddedService selectedAllowedVAS;
    private LazyTableModel<ServiceProfile> lazyServiceProfileList;
    private double installationFee;
    private double serviceFee;

    public Long getSubgroupId() {
        return subgroupId;
    }

    public void setSubgroupId(Long subgroupId) {
        this.subgroupId = subgroupId;
    }

    public List<ServiceSubgroup> getSubgroupList() {
        if (subgroupList == null) {
            subgroupList = subgroupFacade.findAll();
        }
        return subgroupList;
    }

    public List<ServiceSubgroup> getSubgroupListByProvider() {
        if (subgroupList == null && providerIdForView != null) {
            subgroupList = subgroupFacade.findAll();
        }
        return subgroupList;
    }

    public void setSubgroupList(List<ServiceSubgroup> subgroupList) {
        this.subgroupList = subgroupList;
    }

    public LazyTableModel<ServiceProfile> getLazyServiceProfileList() {
        if (selected != null && lazyServiceProfileList == null) {
            serviceProfileFacade.addFilter(ServiceProfilePersistenceFacade.Filter.SERVICE, selected);
            lazyServiceProfileList = new LazyTableModel<>(serviceProfileFacade);
        }
        return lazyServiceProfileList;
    }

    public void setLazyServiceProfileList(LazyTableModel<ServiceProfile> lazyServiceProfileList) {
        this.lazyServiceProfileList = lazyServiceProfileList;
    }

    public ServiceProfile getSelectedServiceProfile() {
        return selectedServiceProfile;
    }

    public void setSelectedServiceProfile(ServiceProfile selectedServiceProfile) {
        this.selectedServiceProfile = selectedServiceProfile;
    }

    public List<ServiceProperty> getServiceProperties() {
        if (serviceProperties == null) {
            if (getSelected() != null) {
                serviceProperties = getSelected().getProperties();
            } else {
                serviceProperties = new ArrayList<>();
            }
        }
        return serviceProperties;
    }

    public void setServiceProperties(List<ServiceProperty> serviceProperties) {
        this.serviceProperties = serviceProperties;
    }

    public ServiceProperty getServiceProperty() {
        return serviceProperty;
    }

    public void setServiceProperty(ServiceProperty serviceProperty) {
        this.serviceProperty = serviceProperty;
    }

    public double getPropertyPrice() {
        return propertyPrice;
    }

    public void setPropertyPrice(double propertyPrice) {
        this.propertyPrice = propertyPrice;
    }

    public List<SelectItem> getZoneSelectList() {
        if (zoneSelectList == null) {
            List<Zone> zoneList = zoneFacade.findAll();
            zoneSelectList = new ArrayList<>();

            for (Zone zone : zoneList)
                zoneSelectList.add(new SelectItem(zone.getId(), zone.getName()));
        }

        return zoneSelectList;
    }

    public void setZoneSelectList(List<SelectItem> zoneSelectList) {
        this.zoneSelectList = zoneSelectList;
    }

    public Long getZoneId() {
        return zoneId;
    }

    public void setZoneId(Long zoneId) {
        this.zoneId = zoneId;
    }

    public void addProperty() {
        if (zoneId == null) {
            FacesContext.getCurrentInstance().
                    addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Please select valid zone",
                            "Please select valid zone"));
            return;
        }
        for (ServiceProperty property : serviceProperties) {
            if (property.getZone().getId() == zoneId) {
                FacesContext.getCurrentInstance().
                        addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                "This property type has already been added",
                                "This property type has already been added"));
                return;
            }
        }
        log.debug(String.format(
                "addProperty called. zoneId = %d, propertyPrice = %f",
                zoneId,
                propertyPrice
        ));

        final Zone zone = zoneFacade.find(zoneId);

        ServiceProperty serviceProperty = new ServiceProperty();
        serviceProperty.setService(getSelected() != null ? getSelected() : getSrv());
        serviceProperty.setZone(zone);
        serviceProperty.setServicePriceInDouble(propertyPrice);

        serviceProperties.add(serviceProperty);
    }

    public void removeProperty() {
        if (serviceProperty == null) {
            FacesContext.getCurrentInstance().
                    addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Please select property to remove",
                            "Please select property to remove"));
            return;
        }
        log.debug(String.format(
                "removeProperty called. selectedSetting = %s",
                serviceProperty
        ));

        serviceProperties.remove(serviceProperty);
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public Long getServiceId() {
        return serviceId;
    }

    public void setServiceId(Long serviceId) {
        this.serviceId = serviceId;
    }

    public static Logger getLog() {
        return log;
    }

    public Date getTo() {
        return to;
    }

    public void setTo(Date to) {
        this.to = to;
    }

    public Date getFrom() {
        return from;
    }

    public void setFrom(Date from) {
        this.from = from;
    }

    public Double getRatioDown() {
        return ratioDown;
    }

    public void setRatioDown(Double ratioDown) {
        this.ratioDown = ratioDown;
    }

    public Double getRatioUp() {
        return ratioUp;
    }

    public void setRatioUp(Double ratioUp) {
        this.ratioUp = ratioUp;
    }

    public List<ServiceProfile> getServiceProfileList() {
        if (serviceProfileList == null && selected != null) {
            serviceProfileList = new ArrayList<>();
            for (ServiceProfile sp : selected.getServiceProfileList())
                serviceProfileList.add(sp);
        }
        return serviceProfileList;
    }

    public void setServiceProfileList(List<ServiceProfile> serviceProfileList) {
        this.serviceProfileList = serviceProfileList;
    }

    public String getBurstDownload() {
        return burstDownload;
    }

    public void setBurstDownload(String burstDownload) {
        this.burstDownload = burstDownload;
    }

    public String getBurstUpload() {
        return burstUpload;
    }

    public void setBurstUpload(String burstUpload) {
        this.burstUpload = burstUpload;
    }

    public Long getInstallFeeProfileID() {
        return installFeeProfileID;
    }

    public void setInstallFeeProfileID(Long installFeeProfileID) {
        this.installFeeProfileID = installFeeProfileID;
    }

    public List<SelectItem> getStatusList() {
        if (selected != null && statusList == null) {
            statusList = new ArrayList<>();

            for (SubscriptionStatus status : SubscriptionStatus.values()) {
                statusList.add(new SelectItem(status));
            }
        }
        return statusList;
    }

    public void removeRule() {
        if (selected == null) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Service unknown", "Service unknown"));
            return;
        }

        if (selectedRule == null) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Please select a rule", "Please select a rule"));
            return;
        }

        selected = serviceFacade.removeRule(selected, selectedRule);
        selectedRule = null;
    }

    public void onVASSearch(AjaxBehaviorEvent ev) {
        if (selected == null) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Service unknown", "Service unknown"));
            return;
        }

        if (!checkVASSearch()) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Please provide search crteria", "Please provide search crteria"));
            return;
        }

        vasFacade.clearFilters();
        vasFacade.addFilter(VASPersistenceFacade.Filter.PROVIDER, selected.getProvider().getId());

        if (filterVasName != null && !filterVasName.isEmpty()) {
            VASPersistenceFacade.Filter nameFilter = VASPersistenceFacade.Filter.NAME;
            nameFilter.setOperation(MatchingOperation.LIKE);
            vasFacade.addFilter(nameFilter, filterVasName);
        }

        if (filterVasType != null) {
            VASPersistenceFacade.Filter typeFilter = VASPersistenceFacade.Filter.TYPE;
            typeFilter.setOperation(MatchingOperation.EQUALS);
            vasFacade.addFilter(typeFilter, filterVasType);
        }

        lazyVasList = new LazyTableModel<>(vasFacade);
    }

    private boolean checkVASSearch() {
        return (filterVasName != null && !filterVasName.isEmpty()) || filterVasType != null;
    }

    public void onVASReset() {
        filterVasType = null;
        filterVasName = null;
        selectedVAS = null;
        lazyVasList = null;
        getLazyVasList();
    }

    public void addStatusChangeRule() {
        if (selected == null) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Service unknown", "Service unknown"));
            return;
        }

        if (filterFinalStatus == null && filterInitSTatus == null && selectedVAS == null) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Please select Starting Status, Target status, Value Added Service", "Please select Starting Status, Target status, Value Added Service"));
            return;
        }

        selected = serviceFacade.addRule(selected, filterInitSTatus, filterFinalStatus, selectedVAS);
        onVASReset();
        filterFinalStatus = null;
        filterInitSTatus = null;
    }

    public void setStatusList(List<SelectItem> statusList) {
        this.statusList = statusList;
    }

    public SubscriptionStatus getFilterFinalStatus() {
        return filterFinalStatus;
    }

    public void setFilterFinalStatus(SubscriptionStatus filterFinalStatus) {
        this.filterFinalStatus = filterFinalStatus;
    }

    public SubscriptionStatus getFilterInitSTatus() {
        return filterInitSTatus;
    }

    public void setFilterInitSTatus(SubscriptionStatus filterInitSTatus) {
        this.filterInitSTatus = filterInitSTatus;
    }

    public ValueAddedServiceType getFilterVasType() {
        return filterVasType;
    }

    public void setFilterVasType(ValueAddedServiceType filterVasType) {
        this.filterVasType = filterVasType;
    }

    public String getFilterVasName() {
        return filterVasName;
    }

    public void setFilterVasName(String filterVasName) {
        this.filterVasName = filterVasName;
    }

    public ValueAddedService getSelectedVAS() {
        return selectedVAS;
    }

    public void setSelectedVAS(ValueAddedService selectedVAS) {
        this.selectedVAS = selectedVAS;
    }

    public LazyTableModel<ValueAddedService> getLazyVasList() {
        if (selected != null && lazyVasList == null) {
            vasFacade.clearFilters();
            vasFacade.addFilter(VASPersistenceFacade.Filter.PROVIDER, selected.getProvider().getId());

            lazyVasList = new LazyTableModel<>(vasFacade);
        }
        return lazyVasList;
    }

    public void setLazyVasList(LazyTableModel<ValueAddedService> lazyVasList) {
        this.lazyVasList = lazyVasList;
    }

    public StatusChangeRule getSelectedRule() {
        return selectedRule;
    }

    public void setSelectedRule(StatusChangeRule selectedRule) {
        this.selectedRule = selectedRule;
    }

    public Service getSrv() {
        if (srv == null) {
            srv = new Service();
            srv.setIsBillByLifeCycle(true);
        }
        return srv;
    }

    public void setSrv(Service srv) {
        this.srv = srv;
    }

    public String getProfile() {
        return profileId;
    }

    public void setProfile(String profile) {
        this.profileId = profile;
    }


    public List<SelectItem> getServiceTypeSelectList() {
        if (serviceTypeSelectList == null) {
            serviceTypeSelectList = new ArrayList<SelectItem>();

            for (ServiceType type : ServiceType.values())
                serviceTypeSelectList.add(new SelectItem(type.toString()));
        }
        return serviceTypeSelectList;
    }

    public void setServiceTypeSelectList(List<SelectItem> serviceTypeSelectList) {
        this.serviceTypeSelectList = serviceTypeSelectList;
    }

    public List<String> getResourceIdList() {
        return resourceIdList;
    }

    public void setResourceIdList(List<String> resourceIdList) {
        this.resourceIdList = resourceIdList;
    }

    public List<SelectItem> getResourceSelectList() {

        if (resourceSelectList == null) {
            List<Resource> resList = resourceFacade.findAll();
            resourceSelectList = new ArrayList<SelectItem>();

            for (Resource res : resList)
                if (res.getName() != null && res.getBucketList() != null && res.getBucketList().size() > 0)
                    resourceSelectList.add(new SelectItem(res.getId(), res.getName()));
        }

        return resourceSelectList;
    }

    public void setResourceSelectList(List<SelectItem> resourceSelectList) {
        this.resourceSelectList = resourceSelectList;
    }

    public String getProfileId() {
        return profileId;
    }

    public void setProfileId(String profileId) {
        this.profileId = profileId;
    }


    public List<Service> getServiceList() {
        if (serviceList == null)
            loadServices();
        return serviceList;
    }

    public void setServiceList(List<Service> serviceList) {
        this.serviceList = serviceList;
    }

    public List<Service> getFilteredServiceList() {
        Logger log = Logger.getLogger(ServiceManager.class);
        log.debug("Filtered services: " + filteredServiceList);
        return filteredServiceList;
    }

    public void setFilteredServiceList(List<Service> filteredServiceList) {
        this.filteredServiceList = filteredServiceList;
    }

    public Service getSelected() {
        if (selected == null) {
            //HttpSession session = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);
            //Long pk = (Long) session.getAttribute(SESSION_SERVICE_SELECTED);

            log.debug("getSelected serviceId=" + serviceId);

            if (serviceId != null) {
                selected = serviceFacade.find(serviceId);
                //log.debug(selected.toString());
                //session.removeAttribute(SESSION_SERVICE_SELECTED);

                if (selected != null) {
                    if (selected.getServiceType() == ServiceType.BROADBAND) {
                        this.isBroadBand = true;
                    }
                }
            }
        }

        return selected;
    }

    public void setSelected(Service selected) {
        this.selected = selected;
    }


    /**
     * @param profileIdForView the profileIdForView to set
     */
    public void setProfileIdForView(String profileIdForView) {
        this.profileIdForView = profileIdForView;
    }

    public ServiceType getServiceType() {
        return serviceType;
    }

    public boolean check() {

        if (upload == null || upload.isEmpty() || download == null || download.isEmpty()
                || burstUpload == null || burstUpload.isEmpty() || burstDownload == null || burstDownload.isEmpty())
            return false;
        return true;
    }

    public String create() {

        srv.setServicePriceInDouble(serviceFee);
        srv.setProvider(providerFacade.find(Long.valueOf(providerIdForView)));
        srv.setInstallFeeInDouble(installationFee);
        srv.setSubgroup(subgroupId == null ? null : subgroupFacade.find(subgroupId));

        if (srv.getProvider().getId() == Providers.CITYNET.getId()) {
            if (subgroupId == null) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Subgroup must be selected", "Subgroup must be selected"));
                return null;
            }
            if (!check()) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Fill in the fields", "Fill in the fields"));
                return null;
            }
        }

        log.debug("Service from form: " + srv);

        List<ServiceSetting> settings = this.seviceSettingFacade.findAllByServiceTypeAndProviderID(srv.getProvider().getId(), srv.getServiceType());

        if (selectedDefaultVASList != null) {
            for (ValueAddedService defaultVAS : selectedDefaultVASList)
                srv.addDefaultVAS(defaultVAS);
        }

        NotificationSetting setting = null;

        for (NotificationSettingRow row : notificationSettings) {
            if (row.getSelectedChannelList() != null && !row.getSelectedChannelList().isEmpty()) {
                setting = notifSettingFacade.find(row.getEvent(), row.getSelectedChannelListAsChannels());

                srv.addNotification(setting);
                setting = null;
            }
        }

        srv.setSettings(settings);

        if (srv.getServiceType() == ServiceType.BROADBAND) {
            if (srv.getProvider().getId() == Providers.DATAPLUS.getId()) {
                srv.setProperties(serviceProperties);
            } else {
                if (srv.getProvider().getId() == Providers.CITYNET.getId()) {
                    if (!((upload.endsWith("k") || upload.endsWith("m")) && (download.endsWith("k") || download.endsWith("m"))
                            && (burstUpload.endsWith("k") || burstUpload.endsWith("m")) && (burstDownload.endsWith("k") || burstDownload.endsWith("m")))) {
                        FacesContext.getCurrentInstance().addMessage(null,
                                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Fill in correct the resource fields, in the end m or k", "Fill in correct the resource fields, in the end m or k"));
                        return null;
                    }
                }

                if (upload == null || upload.isEmpty() || download == null || download.isEmpty()) {
                    FacesContext.getCurrentInstance().addMessage(null,
                            new FacesMessage(FacesMessage.SEVERITY_ERROR, "Fill in the fields", "Fill in the fields"));
                    return null;
                }

                Resource resource = new Resource();
                resource.setActiveThroughWeek();
                resource.setActiveTill(LocalTime.parse("23:59:59", DateTimeFormat.forPattern("HH:mm:ss")));
                resource.setActiveFrom(LocalTime.parse("00:00:00", DateTimeFormat.forPattern("HH:mm:ss")));
                resource.setExpirationDate(DateTime.parse("9999-12-31 23:59:59", DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")));
                resource.setName("Internet - " + srv.getName());
                resource.setDsc("Resource for Service " + srv.getName());

                if (upload.endsWith("k") || upload.endsWith("m")) {
                    unit = upload.substring(upload.length() - 1, upload.length());
                    upload = upload.substring(0, upload.length() - 1);
                } else unit = "";

                ResourceBucket upBucket = new ResourceBucket();
                upBucket.setType(ResourceBucketType.INTERNET_UP);
                upBucket.setCapacity(upload);
                upBucket.setDsc(upBucket.getType() + " for Resource " + resource.getName());
                upBucket.setUnit(unit);
                resource.addBucket(upBucket);

                if (download.endsWith("k") || download.endsWith("m")) {
                    unit = download.substring(download.length() - 1, download.length());
                    download = download.substring(0, download.length() - 1);
                } else unit = "";
                ResourceBucket downBucket = new ResourceBucket(download, ResourceBucketType.INTERNET_DOWN);
                downBucket.setDsc(downBucket.getType() + " for Resource " + resource.getName());
                downBucket.setUnit(unit);
                resource.addBucket(downBucket);


                if (burstUpload != null) {
                    if (burstUpload.endsWith("k") || burstUpload.endsWith("m")) {
                        unit = burstUpload.substring(burstUpload.length() - 1, burstUpload.length());
                        burstUpload = burstUpload.substring(0, burstUpload.length() - 1);
                    } else unit = "";
                    ResourceBucket upBurstBucket = new ResourceBucket();
                    upBurstBucket.setType(ResourceBucketType.INTERNET_LUP);
                    upBurstBucket.setCapacity(burstUpload);
                    upBurstBucket.setDsc(upBurstBucket.getType() + " for Resource " + resource.getName());
                    upBurstBucket.setUnit(unit);
                    resource.addBucket(upBurstBucket);
                }
                if (burstDownload != null) {
                    if (burstDownload.endsWith("k") || burstDownload.endsWith("m")) {
                        unit = burstDownload.substring(burstDownload.length() - 1, burstDownload.length());
                        burstDownload = burstDownload.substring(0, burstDownload.length() - 1);
                    } else unit = "";
                    ResourceBucket downBurstBucket = new ResourceBucket();
                    downBurstBucket.setType(ResourceBucketType.INTERNET_LDOWN);
                    downBurstBucket.setCapacity(burstDownload);
                    downBurstBucket.setDsc(downBurstBucket.getType() + " for Resource " + resource.getName());
                    downBurstBucket.setUnit(unit);
                    resource.addBucket(downBurstBucket);
                }

                srv.addResource(resource);

                for (ServiceProfile sp : serviceProfileList) {
                    sp.setService(srv);
                    srv.addServiceProfile(sp);
                }
            }
        }

        log.debug("SERVICE: " + srv);
        log.debug("PROFILE: " + profileId);
        serviceFacade.save(srv);
        Service newCreated = serviceFacade.getNewCreated(srv);
        if (newCreated != null)
            serviceFacade.createServiceProfile(newCreated);

        return SERVICE_PATH + "index";
    }

    public String show() {

        log.debug("selected service: " + selected);
        if (selected == null) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Please select service first", "Please select service first"));
            return null;
        }
        String result = null;

        result = String.format("/pages/service/view.xhtml?service=%d&amp;faces-redirect=true&amp;includeViewParams=true", selected.getId());

        log.debug("view: result=" + result);
        return result;

        //HttpSession session = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);
        //session.setAttribute(SESSION_SERVICE_SELECTED, selected.getId());
        //log.debug("SERVICE SHOW PAGE - SELECTED: " + selected);
        //return SERVICE_PATH + "view";
    }

    public void removeResource(Resource res) {
        selected.removeResource(res);
    }

    public void addResources() {
        selected.addAllResources(resourceFacade.findAllInList(resourceIdList));
    }

    public List<ResourceBucket> getResourceBucketList() {
        if (resourceBucketList == null && selected != null) {
            resourceBucketList = new ArrayList<ResourceBucket>();

            for (Resource res : selected.getResourceList()) {
                if (res.getBucketList().size() > 0)
                    resourceBucketList.addAll(res.getBucketList());
            }
        }
        return resourceBucketList;
    }

    public void setResourceBucketList(List<ResourceBucket> resourceBucketList) {
        this.resourceBucketList = resourceBucketList;
    }

    public List<SelectItem> getProviderList() {
        if (providerList == null) {
            List<ServiceProvider> providers = providerFacade.findAll();
            providerList = new ArrayList<SelectItem>();

            for (ServiceProvider sp : providers) {
                providerList.add(new SelectItem(sp.getId(), sp.getName()));
            }
        }
        return providerList;
    }

    public List<SelectItem> getProviderListForView() {
        if (selected != null && providerListForView == null) {
            providerListForView = new ArrayList<SelectItem>();
            providerListForView.add(new SelectItem(this.selected.getProvider().getId(), this.selected.getProvider().getName()));

            List<ServiceProvider> providers = providerFacade.findAllNotIn(this.selected.getProvider().getId());

            for (ServiceProvider sp : providers) {
                providerListForView.add(new SelectItem(sp.getId(), sp.getName()));
            }
        }
        return providerListForView;
    }

    public String getProviderIdForView() {
        return providerIdForView;
    }

    public void setProviderIdForView(String providerIdForView) {
        this.providerIdForView = providerIdForView;
    }

    public void setProviderListForView(List<SelectItem> providerListForView) {
        this.providerListForView = providerListForView;
    }


    public void setProviderList(List<SelectItem> providerList) {
        this.providerList = providerList;
    }

    public void setServiceType(ServiceType serviceType) {
        this.serviceType = serviceType;
    }

    public LazyDataModel getDefaultVASList() {
        log.debug("defaultVASList: "+defaultVASList);
        return defaultVASList;
    }

    public void setDefaultVASList(LazyDataModel defaultVASList) {
        this.defaultVASList = defaultVASList;
    }

    public List<ValueAddedService> getSelectedDefaultVASList() {
        log.debug("selectedDefaultVASList: "+selectedDefaultVASList);
        return selectedDefaultVASList;
    }

    public void setSelectedDefaultVASList(List<ValueAddedService> selectedDefaultVASList) {
        this.selectedDefaultVASList = selectedDefaultVASList;
    }

    public List<SelectItem> getVasTypeList() {
        if (vasTypeList == null) {
            vasTypeList = new ArrayList<>();
log.debug("fetching tps");
            for (ValueAddedServiceType tp : ValueAddedServiceType.values()) {
                log.debug("tp: " + tp);
                vasTypeList.add(new SelectItem(tp));
            }
            }
        return vasTypeList;
    }

    public void setVasTypeList(List<SelectItem> vasTypeList) {
        this.vasTypeList = vasTypeList;
    }

    public ValueAddedServiceType getSelectedDefaultVasType() {
        log.debug(" selectedDefaultVasType: "+selectedDefaultVasType);
        return selectedDefaultVasType;
    }

    public void setSelectedDefaultVasType(ValueAddedServiceType selectedDefaultVasType) {
        this.selectedDefaultVasType = selectedDefaultVasType;
    }

    public String getDefaultVasName() {
        return defaultVasName;
    }

    public void setDefaultVasName(String defaultVasName) {
        this.defaultVasName = defaultVasName;
    }

    public String save() {
        log.debug("Profile id: " + profileIdForView);
        selected.setProvider(providerFacade.find(Long.valueOf(providerIdForView)));
        if (subgroupId != null)
            selected.setSubgroup(subgroupFacade.find(subgroupId));
        //List<ServiceSetting> settings = this.seviceSettingFacade.findAllByServiceTypeAndProviderID(selected.getProvider().getId(), srv.getServiceType());
        NotificationSetting setting = null;
        //srv.setNotificationSettings(null);
        if (notificationSettings != null && !notificationSettings.isEmpty()) {
            for (NotificationSettingRow row : notificationSettings) {
                setting = selected.getNotificationSettingByEvent(row.getEvent());

                if (setting != null) { //update setting
                    if (row.getSelectedChannelList() != null && !row.getSelectedChannelList().isEmpty()) {
                        //setting.setChannelList(row.getSelectedChannelListAsChannels());
                        setting = notifSettingFacade.find(row.getEvent(), row.getSelectedChannelListAsChannels());
                        selected.updateNotificationSetting(setting);
                    } else { //none selected - remove the setting
                        selected.getNotificationSettings().remove(setting);
                        //notifSettingFacade.updateAndDelete(setting);
                    }
                } else {
                    if (row.getSelectedChannelList() != null && !row.getSelectedChannelList().isEmpty()) {
                        setting = notifSettingFacade.find(row.getEvent(), row.getSelectedChannelListAsChannels());
                        selected.addNotification(setting);
                    }
                }
                setting = null;
            }
        }
        selected = serviceFacade.update(selected);
        return SERVICE_PATH + "index";
    }

    private void loadServices() {
        serviceList = serviceFacade.findAll();
    }

    public boolean getIsBroadBand() {
        return isBroadBand;
    }

    public void setIsBroadBand(boolean isBroadBand) {
        this.isBroadBand = isBroadBand;
    }

    public String getUpload() {
        return upload;
    }

    public void setUpload(String upload) {
        this.upload = upload;
    }

    public String getDownload() {
        return download;
    }

    public void setDownload(String download) {
        this.download = download;
    }

    public List<ServiceSetting> getServiceSettingList() {
        return serviceSettingList;
    }

    public void setServiceSettingList(List<ServiceSetting> serviceSettingList) {
        this.serviceSettingList = serviceSettingList;
    }

    public List<SelectItem> getGroupSelectList() {
        if (groupSelectList == null) {
            groupSelectList = new ArrayList<>();

            List<ServiceGroup> groups = groupFacade.findAll();

            for (ServiceGroup gr : groups) {
                groupSelectList.add(new SelectItem(gr.getId(), gr.getName()));
            }
        }
        return groupSelectList;
    }

    public void setGroupSelectList(List<SelectItem> groupSelectList) {
        this.groupSelectList = groupSelectList;
    }

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    public void onSelectProviderChange(AjaxBehaviorEvent event) {
        //if (srv.getServiceType() == ServiceType.BROADBAND) {
        log.debug("Changed: providerID=" + providerIdForView);

        if (providerIdForView != null) {
            List<ServiceGroup> groups = groupFacade.findAllByProviderID(Long.valueOf(providerIdForView));

            ServiceGroup group = groups.get(0);

            if (group.getType() == ServiceType.BROADBAND)
                this.isBroadBand = true;
            else {
                this.isBroadBand = false;
            }
            serviceType = group.getType();
            srv.setServiceType(group.getType());
            serviceSettingList = this.seviceSettingFacade.findAllByServiceTypeAndProviderID(Long.valueOf(providerIdForView), srv.getServiceType());

            groupSelectList = new ArrayList<>();
            serviceProfileList = new ArrayList<>();

            for (ServiceGroup gr : groups) {
                groupSelectList.add(new SelectItem(gr.getId(), gr.getName()));
            }

            vasFacade.clearFilters();
            vasFacade.addFilter(VASPersistenceFacade.Filter.PROVIDER, Long.valueOf(providerIdForView));
            vasFacade.addFilter(VASPersistenceFacade.Filter.ISACTIVE, 1);
            vasFacade.addFilter(VASPersistenceFacade.Filter.TYPE, ValueAddedServiceType.ONETIME_VARIABLE_CHARGE);
            defaultVASList = new LazyTableModel(vasFacade);

            log.debug("Service type " + srv.getServiceType());
        }
    }

    public void onSelectGroupChange(AjaxBehaviorEvent event) {
        //if (srv.getServiceType() == ServiceType.BROADBAND) {
        log.debug("onSelectGroupChange: roviderID=" + providerIdForView + ", groupID=" + groupId);

        if (providerIdForView != null && groupId != null) {
            ServiceGroup gr = groupFacade.find(groupId);

            if (gr.getType() == ServiceType.BROADBAND)
                this.isBroadBand = true;

            serviceType = gr.getType();

            log.debug("onSelectGroupChange: " + gr);
            srv.setServiceType(gr.getType());
            srv.setGroup(gr);
            serviceSettingList = this.seviceSettingFacade.findAllByServiceTypeAndProviderID(Long.valueOf(providerIdForView), srv.getServiceType());
            log.debug("Service type " + srv.getServiceType());
        }
    }

    public void onSelectTypeChange(AjaxBehaviorEvent event) {
        //if (srv.getServiceType() == ServiceType.BROADBAND) {
        log.debug("Changed: providerID=" + providerIdForView);

        if (providerIdForView != null) {
            this.isBroadBand = true;
            serviceSettingList = this.seviceSettingFacade.findAllByServiceTypeAndProviderID(Long.valueOf(providerIdForView), srv.getServiceType());
            List<ServiceGroup> groups = groupFacade.findAllByProviderID(Long.valueOf(providerIdForView));

            groupSelectList = new ArrayList<>();

            for (ServiceGroup gr : groups) {
                groupSelectList.add(new SelectItem(gr.getId(), gr.getName()));
            }

            log.debug("Service type " + srv.getServiceType());
        }
    }


    /**
     * @return the profileIdForView
     */

    public List<NotificationSettingRow> getNotificationSettings() {
        if (notificationSettings == null) {
            notificationSettings = new ArrayList<>();

            NotificationSettingRow notificationSetting = null;

            for (BillingEvent ev : BillingEvent.values()) {
                notificationSetting = new NotificationSettingRow(ev, NotificationChannell.values());
                notificationSettings.add(notificationSetting);

                if (getSelected() != null) {
                    for (NotificationSetting set : getSelected().getNotificationSettings()) {
                        if (set.getEvent() == ev && (set.getChannelList() != null || !set.getChannelList().isEmpty())) {
                            notificationSetting.setSelectedChannelListAsChannels(set.getChannelList());
                            break;
                        }
                    }
                }
            }
        }

        return notificationSettings;
    }

    public void setNotificationSettings(List<NotificationSettingRow> notificationSettings) {
        this.notificationSettings = notificationSettings;
    }

    public void onDefaultVasSearch(AjaxBehaviorEvent event) {
        vasFacade.clearFilters();

        log.debug("VAS SEARCH");
log.debug("selected: => "+selected);
        if (selected != null) {
            vasFacade.addFilter(VASPersistenceFacade.Filter.PROVIDER, selected.getProvider().getId());
        }
log.debug("defaultVasName: => "+defaultVasName+ " + defaultVasName: => "+defaultVasName);
        if (defaultVasName != null && !defaultVasName.isEmpty()) {
            vasFacade.addFilter(VASPersistenceFacade.Filter.NAME, defaultVasName);
        }

        vasFacade.addFilter(VASPersistenceFacade.Filter.ISACTIVE, 1);
log.debug("vasFacade: => "+vasFacade);

log.debug("selectedDefaultVasType: => "+selectedDefaultVasType);
        if (selectedDefaultVasType != null) {
            VASPersistenceFacade.Filter typeFilter = VASPersistenceFacade.Filter.TYPE;
            typeFilter.setOperation(MatchingOperation.EQUALS);
            vasFacade.addFilter(typeFilter, selectedDefaultVasType);
        }

        defaultVASList = new LazyTableModel(vasFacade);
        log.debug("defaultVASList: => "+defaultVASList);
    }

    public void onResetVas(ActionEvent event) {
        defaultVasName = null;
        selectedDefaultVasType = null;

        vasFacade.removeFilter(VASPersistenceFacade.Filter.NAME);
        vasFacade.removeFilter(VASPersistenceFacade.Filter.TYPE);

        defaultVASList = new LazyTableModel(vasFacade);
    }

    public List<ValueAddedService> getAllowedVASList() {
        if (allowedVASList == null)
            allowedVASList = new ArrayList<>();
        return allowedVASList;
    }

    public void setAllowedVASList(List<ValueAddedService> allowedVASList) {
        this.allowedVASList = allowedVASList;
    }

    public List<ValueAddedService> getSelectedAllowedVASList() {
        return selectedAllowedVASList;
    }

    public void setSelectedAllowedVASList(List<ValueAddedService> selectedAllowedVASList) {
        this.selectedAllowedVASList = selectedAllowedVASList;
    }

    public void onAllowedVASSelect(SelectEvent ev) {

    }

    public void addAllowedVas() {
        if (selectedDefaultVASList == null) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Please select VAS", "Please select VAS"));
            return;
        }
        if (selected == null) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Service not recognized", "Service not recognized"));
            return;
        }

        for (ValueAddedService srv : selectedDefaultVASList) {
            selected.addAllowedVAS(srv);
        }
        vasFacade.clearFilters();
    }

    public void removeAllowedVAS(javax.faces.event.ActionEvent ev) {
        if (selected == null) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Service not recognized", "Service not recognized"));
            return;
        }

        if (selected.getAllowedVASList() != null) {
            Iterator<ValueAddedService> it = selected.getAllowedVASList().iterator();

            while (it.hasNext()) {
                if (it.next().getId() == selectedAllowedVAS.getId()) {
                    it.remove();
                    break;
                }
            }
            selectedAllowedVAS = null;
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Value Added Service removed", "Value Added Service removed"));
        }
    }

    public ValueAddedService getSelectedAllowedVAS() {
        return selectedAllowedVAS;
    }

    public void setSelectedAllowedVAS(ValueAddedService selectedAllowedVAS) {
        this.selectedAllowedVAS = selectedAllowedVAS;
    }

    public void addServiceProfile() {
        if (ratioUp != null && ratioDown != null && from != null && to != null) {
            ServiceProfile sp = new ServiceProfile();
            DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
            sp.setUp(ratioUp);
            sp.setDown(ratioDown);
            sp.setTo(dateFormat.format(to));
            sp.setFrom(dateFormat.format(from));
            serviceProfileList.add(sp);
            resetServiceProfile();
        } else {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Fill in the fields", "Fill in the fields"));
        }
    }

    public void removeServiceProfile() {
        if (selectedServiceProfile != null)
            serviceProfileList.remove(selectedServiceProfile);
    }

    public void resetServiceProfile() {
        ratioUp = ratioDown = null;
        from = to = null;
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void onResourceRowEdit(RowEditEvent event) {
        if (selected.getProvider().getId() != Providers.CITYNET.getId() &&
                selected.getProvider().getId() != Providers.GLOBAL.getId()) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "You cannot change resource for " + selected.getProvider().getName(), "You cannot change resource for " + selected.getProvider().getName()));
            return;
        }
        ResourceBucket resourceBucket = (ResourceBucket) event.getObject();
        bucketFacade.update(resourceBucket);
        serviceFacade.update(selected);
    }

    public void onProfileRowEdit(RowEditEvent event) {
        log.debug("HERESSS");
        if (selected.getProvider().getId() != Providers.CITYNET.getId()) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "You cannot change profile for " + selected.getProvider().getName(), "You cannot change profile for " + selected.getProvider().getName()));
            return;
        }
        log.debug("HERE");
        ServiceProfile serviceProfile = (ServiceProfile) event.getObject();
        serviceProfileFacade.update(serviceProfile);
    }

    public String getCitynetProviderId() {
        log.debug(providerIdForView + " " + Providers.CITYNET.getId());
        return String.valueOf(Providers.CITYNET.getId());
    }

    public String getDataplusProviderId() {
        return String.valueOf(Providers.DATAPLUS.getId());
    }

    public String getGlobalProviderId() {
        return String.valueOf(Providers.GLOBAL.getId());
    }

    public void addProfileToList() throws ProvisionerNotFoundException {
        if (ratioUp != null && ratioDown != null && from != null && to != null && selected != null) {
            ServiceProfile sp = new ServiceProfile();
            DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
            sp.setUp(ratioUp);
            sp.setDown(ratioDown);
            sp.setTo(dateFormat.format(to));
            sp.setFrom(dateFormat.format(from));
            sp.setService(selected);
            serviceProfileFacade.save(sp);
            sp = serviceProfileFacade.merge(sp);
            selected.getServiceProfileList().add(sp);
            serviceFacade.update(selected);
            serviceProfileFacade.addToRadius(sp);
            resetServiceProfile();
        } else {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Fill in the fields", "Fill in the fields"));
        }
    }

    public void removeProfileFromList() throws ProvisionerNotFoundException {
        log.debug("HERE");
        if (selectedServiceProfile != null && selected != null) {
            for (ServiceProfile profile : selected.getServiceProfileList())
                if (profile.getId() == selectedServiceProfile.getId()) {
                    selected.getServiceProfileList().remove(profile);
                    break;
                }
            serviceFacade.update(selected);
            serviceProfileFacade.remove(selectedServiceProfile);
            //serviceProfileFacade.removeFromRadius(selectedServiceProfile);
            resetServiceProfile();
        } else {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Select service profile", "Select service profile"));
        }
    }

    public double getInstallationFee() {
        return installationFee;
    }

    public void setInstallationFee(double installationFee) {
        this.installationFee = installationFee;
    }

    public double getServiceFee() {
        return serviceFee;
    }

    public void setServiceFee(double serviceFee) {
        this.serviceFee = serviceFee;
    }
}

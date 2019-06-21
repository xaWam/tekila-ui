package com.jaravir.tekila.ui.jsf.managed;

import com.jaravir.tekila.base.auth.Privilege;
import com.jaravir.tekila.base.auth.persistence.User;
import com.jaravir.tekila.base.auth.persistence.exception.NoPrivilegeFoundException;
import com.jaravir.tekila.base.auth.persistence.manager.*;
import com.jaravir.tekila.base.auth.persistence.manager.SecurityManager;
import com.jaravir.tekila.base.entity.Language;
import com.jaravir.tekila.base.entity.Util;
import com.jaravir.tekila.base.filter.MatchingOperation;
import com.jaravir.tekila.base.model.BillingModelPersistenceFacade;
import com.jaravir.tekila.common.device.DeviceStatus;
import com.jaravir.tekila.engines.*;
import com.jaravir.tekila.equip.EquipmentPersistenceFacade;
import com.jaravir.tekila.inMemory.InMemoryManager;
import com.jaravir.tekila.module.accounting.entity.Invoice;
import com.jaravir.tekila.module.accounting.manager.ChargePersistenceFacade;
import com.jaravir.tekila.module.accounting.manager.InvoicePersistenceFacade;
import com.jaravir.tekila.module.accounting.manager.TransactionPersistenceFacade;
import com.jaravir.tekila.module.campaign.*;
import com.jaravir.tekila.module.event.BillingEvent;
import com.jaravir.tekila.module.event.notification.channell.NotificationChannell;
import com.jaravir.tekila.module.periiodic.JobPersistenceFacade;
import com.jaravir.tekila.module.periodic.Job;
import com.jaravir.tekila.module.periodic.JobPropertyType;
import com.jaravir.tekila.module.queue.PersistentQueueManager;
import com.jaravir.tekila.module.service.*;
import com.jaravir.tekila.module.service.entity.*;
import com.jaravir.tekila.module.service.model.BillingModel;
import com.jaravir.tekila.module.service.model.BillingPrinciple;
import com.jaravir.tekila.module.service.persistence.manager.*;
import com.jaravir.tekila.module.stats.external.ExternalStatusInformation;
import com.jaravir.tekila.module.stats.persistence.entity.OfflineBroadbandStats;
import com.jaravir.tekila.module.stats.persistence.entity.OnlineBroadbandStats;
import com.jaravir.tekila.module.stats.persistence.manager.AuthRejectionReasonPersistenceFacade;
import com.jaravir.tekila.module.stats.persistence.manager.OfflineStatsPersistenceFacade;
import com.jaravir.tekila.module.stats.persistence.manager.OnlineStatsPersistenceFacade;
import com.jaravir.tekila.module.stats.persistence.manager.TechnicalStatusPersistenceFacade;
import com.jaravir.tekila.module.store.*;
import com.jaravir.tekila.module.store.ats.AtsPersistenceFacade;
import com.jaravir.tekila.module.store.equip.Equipment;
import com.jaravir.tekila.module.store.equip.EquipmentStatus;
import com.jaravir.tekila.module.store.ip.StaticIPType;
import com.jaravir.tekila.module.store.ip.persistence.IpAddress;
import com.jaravir.tekila.module.store.ip.persistence.IpAddressRange;
import com.jaravir.tekila.module.store.mobile.*;
import com.jaravir.tekila.module.store.status.StoreItemStatus;
import com.jaravir.tekila.module.store.street.HousePersistenceFacade;
import com.jaravir.tekila.module.store.street.StreetPersistenceFacade;
import com.jaravir.tekila.module.subscription.exception.DuplicateAgreementException;
import com.jaravir.tekila.module.subscription.persistence.entity.*;
import com.jaravir.tekila.module.subscription.persistence.entity.external.TechnicalStatus;
import com.jaravir.tekila.module.subscription.persistence.entity.reactivation.SubscriptionReactivation;
import com.jaravir.tekila.module.subscription.persistence.entity.transition.StatusChangeRule;
import com.jaravir.tekila.module.subscription.persistence.management.*;
import com.jaravir.tekila.module.subscription.persistence.management.sequence.AgreementGenerator;
import com.jaravir.tekila.module.system.SystemEvent;
import com.jaravir.tekila.module.system.log.SystemLogger;
import com.jaravir.tekila.provision.broadband.devices.MiniPop;
import com.jaravir.tekila.provision.broadband.devices.MinipopCategory;
import com.jaravir.tekila.provision.broadband.devices.Port;
import com.jaravir.tekila.provision.broadband.devices.exception.NoFreePortLeftException;
import com.jaravir.tekila.provision.broadband.devices.exception.PortAlreadyReservedException;
import com.jaravir.tekila.provision.broadband.devices.manager.MiniPopPersistenceFacade;
import com.jaravir.tekila.provision.broadband.devices.manager.Providers;
import com.jaravir.tekila.provision.broadband.entity.Usage;
import com.jaravir.tekila.provision.exception.ProvisionerNotFoundException;
import com.jaravir.tekila.ui.jsf.managed.util.ChargeForView;
import com.jaravir.tekila.ui.model.LazyTableModel;
import com.jaravir.tekila.ui.model.LazyTableModelDynamic;

import java.io.*;
import java.text.DecimalFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;

import static javax.faces.application.FacesMessage.SEVERITY_ERROR;
import static javax.faces.application.FacesMessage.SEVERITY_INFO;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.component.UIPanel;
import javax.faces.component.UISelectOne;
import javax.faces.component.html.HtmlOutputText;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.event.AjaxBehaviorEvent;
import javax.faces.event.ValueChangeListener;
import javax.faces.model.SelectItem;
import javax.faces.validator.ValidatorException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.jfree.util.Log;
import org.joda.time.DateTime;
import org.joda.time.DateTimeComparator;
import org.joda.time.Days;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.primefaces.component.dialog.Dialog;
import org.primefaces.component.outputlabel.OutputLabel;
import org.primefaces.component.panelgrid.PanelGrid;
import org.primefaces.context.RequestContext;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.event.RowEditEvent;
import org.primefaces.event.SelectEvent;
import org.primefaces.event.UnselectEvent;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.UploadedFile;

@ManagedBean
@ViewScoped
public class SubscriptionManager implements Serializable {

    @EJB
    private SubscriptionPersistenceFacade subscriptionFacade;
    @EJB
    private ServicePersistenceFacade serviceFacade;
    @EJB
    private SubscriberPersistenceFacade subscriberFacade;
    @EJB
    private ResourcePersistenceFacade resFacade;
    @EJB
    private ResourceBucketPersistenceFacade bucketFacade;
    @EJB
    private SubscriptionResourceBucketPersistenceFacade subResBucketFacade;
    @EJB
    private SubscriptionResourcePersistenceFacade subResFacade;
    @EJB
    private TransactionPersistenceFacade transFacade;
    @EJB
    private UserPersistenceFacade userFacade;
    @EJB
    private ChargePersistenceFacade chargeFacade;
    @EJB
    private EquipmentPersistenceFacade equipmentPersistenceFacade;
    @EJB
    private AgreementGenerator agreementGenerator;
    @EJB
    private ServiceProviderPersistenceFacade providerFacade;
    @EJB
    private ServiceGroupPersistenceFacade groupFacade;
    @EJB
    private PersistentQueueManager queueManager;
    @EJB
    private SystemLogger systemLogger;
    @EJB
    private MsidnPersistenceFacade msisdnFacade;
    @EJB
    private CommercialCategoryPersistenceFacade comFacade;
    @EJB
    private VASPersistenceFacade vasFacade;
    @EJB
    private SubscriptionReactivationPersistenceFacade reactivationFacade;
    @EJB
    private CampaignRegisterPersistenceFacade campaignRegisterFacade;
    @EJB
    private EngineFactory provisioningFactory;
    @EJB
    private SalesPartnerStorePersistenceFacade partnerStoreFacade;
    @EJB
    private InMemoryManager inMemory;
    @EJB
    private AtsPersistenceFacade atsFacade;
    @EJB
    private TechnicalStatusPersistenceFacade technicalStatusPersistenceFacade;
    @EJB
    private AuthRejectionReasonPersistenceFacade rejectionReasonPersistenceFacade;
    @EJB
    private BillingModelPersistenceFacade modelFacade;
    @EJB
    private SubscriptionSettingPersistenceFacade settingFacade;
    @EJB
    private HousePersistenceFacade houseFacade;
    @EJB
    private ServiceSubgroupPersistenceFacade subgroupFacade;
    @EJB
    private EngineFactory engineFactory;
//    @EJB
//    private UninetBillingManager uninetBillingManager;

    @EJB
    private CommonOperationsEngine commonEngine;
    @EJB
    private ServicePropertyPersistenceFacade servicePropertyPersistenceFacade;

    private ProvisioningEngine provisioner;
    private final static int AJAX_ERROR_STATUS_CODE = 500;

    private Subscription subscription;
    private Balance balance;
    private Service service;

    private List<Subscription> subscriptionList;
    private Subscription selected;
    private List<String> serviceIdList;
    private String serviceId;
    private List<SelectItem> serviceSelectList;
    private List<Subscription> filteredSubscriptionList;
    private String subscriberId;
    private List<SelectItem> subscriberSelectList;
    private LazyDataModel<Subscriber> subscriberLazyTableModel;
    private LazyDataModel<Subscription> subscriptionLazyTableModel;
    private Subscriber selectedSubscriber;
    private List<Subscriber> filteredSubscriberList;
    private List<SubscriptionResourceBucket> bucketList;
    private SubscriptionResourceBucket subBucket;
    private List<SelectItem> typeSelectList;
    private List<SelectItem> typeSelectListForView;


    private Long speed = 0L;
    @EJB
    private InvoicePersistenceFacade invoiceFacade;
    private final static String SUBSCRIPTION_PATH = "/pages/subscription/";
    private final static String SESSION_SELECTED_SUBSCRIPTION = "subscription.selected";
    private final static transient Logger log = Logger.getLogger(SubscriptionManager.class);

    private SubscriptionDetails details;
    private Boolean isUseSubscriberAddress = true;
    private transient UIPanel sbnDetailsPanel;
    private Long subscriptionID;
    private BillingModel billingModel;
    private List<BillingModel> billingModelList;

    private int selectFromStockOrManually = 1;
    private Long subscriberID;

    private LazyDataModel<Equipment> equipmentList;
    private Equipment selectedEquipment;
    private List<Equipment> filteredEquipmentList;

    //search for equipment
    private String sqlQuery;
    private String partNumber;
    private String modelName;
    private String typeName;
    private String brandName;
    private String macAddress;
    private List<SelectItem> equipmentStatusSelectList;
    private EquipmentStatus equipmentStatus;
    private Boolean isGenerateAgreement;
    private List<SelectItem> languageList;
    private Language language;
    private List<NotificationSettingRow> notificationSettings;

    private boolean isUseStockEquipment = false;
    private Boolean taxFreeEnabled;

    private String chargedIpAddressString;
    private String freeIpAddressString;
    private IpAddress ipAddress;
    private List<IpAddress> freeIpList;
    private String uninetSlot;
    private String uninetMAC;
    private String uninetPort;
    private String uninetInfo;
    private String uninetAtsPort;
    private String uninetPassword;
    private String username;
    private String password;

    private String staticIp;
    private String sipNumber;
    //minipops
    @EJB
    private MiniPopPersistenceFacade minipopFacade;
    @EJB
    private DealerPersistanceFacade dealerFacade;
    @EJB
    private ResellerPersistenceFacade resellerPersistenceFacade;
    @EJB
    private SubscriptionServiceTypePersistenceFacade serviceTypePersistenceFacade;
    @EJB
    private ZonePersistenceFacade zonePersistenceFacade;
    @EJB
    private OnlineStatsPersistenceFacade onlineStatsFacade;
    @EJB
    private OfflineStatsPersistenceFacade offlineStatsFacade;
    @EJB
    private CitynetCommonEngine citynetCommonEngine;
    private TechnicalStatus technicalStatus;
    private MinipopCategory minipopCategory;
    private List<MinipopCategory> minipopCategoryList;

    Ats ats;
    private String miniPopMacAddress;
    private String minipPopAddress;
    private String switchID;
    private String ip;
    private LazyDataModel<MiniPop> minipopLazyDataModel;
    private List<SelectItem> dealerSelectList;
    private List<MiniPop> minipopList;
    private MiniPop selectedMiniPop;
    private String selectedDealer;
    private String selectedReseller;
    private Long selectedDealerId;
    private List<MiniPop> filteredMinipopList;
    private transient UIComponent equipmentTable;
    private List<OnlineBroadbandStats> onlineStats;
    private List<OfflineBroadbandStats> offlineStats;
    private List<MiniPop> miniPopList;
    private List<MiniPop> minipopListTemp;
    public List<Dealer> dealerList;

    private List<SelectItem> resellerSelectList;
    private List<SelectItem> serviceTypeSelectList;
    private String serviceTypeId;
    private List<SelectItem> zoneSelectList;
    private String zoneId;

    private ServiceType serviceType;
    private Long providerID;
    private ServiceProvider provider;
    private List<SelectItem> serviceGroupList;
    private List<SelectItem> providerList;
    private ServiceGroup serviceGroup;

    private UploadedFile contract;
    private StreamedContent contractContents;
    private List<Addendum> selectedAddendumList;

    private String cableLength;
    private String cablePrice;
    private long cableCharge;
    private Map<Long, Long> defaultVasCharges = new HashMap<>();
    private final static long CABLE_VAS_CODE = 126;

    private List<SelectItem> vasSettingsValuesSelectItem;
    private Long vasSettingID;
    private String vasSettingValue;
    private String vasSettingLength;
    private SubscriptionVAS subscriptionVAS;
    private SubscriptionVASSetting selectedVASSetting;

    //search MSISDN
    private MobileCategory category;
    private List<SelectItem> categoryList;
    private List<SelectItem> statusList;
    private StoreItemStatus status;
    private String msisdnValue;
    private LazyDataModel<Msisdn> msisdnList;
    private Msisdn selectedMsisdn;
    private Long msisdnID;
    private List<SelectItem> comCatList;
    private Long comCatID;

    private final static String DEFAULT_VAS_TAB_NAME = "Default Value Added Services";
    private final static String SIP_VAS_TAB_NAME = "Numbers";
    private String vasTabName = DEFAULT_VAS_TAB_NAME;

    //vas dialog
    private List<SelectItem> vasTypeList;
    private ValueAddedServiceType vasType;

    private String vasName;
    private ValueAddedService selectedVAS;
    private LazyTableModel<ValueAddedService> vasList;

    //VAS
    private List<SubscriptionVAS> completeSbnVASList;
    private SubscriptionVAS selectedSbnVAS;
    private String filterSvbVASName;
    private ValueAddedServiceType sbnVASType;
    private List<SelectItem> sbnVASTypeList;
    private SubscriptionStatus sbnVasStatus;
    private List<SelectItem> sbnVasStatusList;

    private enum SearchVASCriteria {
        NAME, STATUS, TYPE;
    }

    private UIComponent sbnVASDialog;
    private UIComponent form;
    private UIComponent vasGrid;
    private List<ValueAddedService> allowedVasList;
    private SubscriptionStatus selectedStatus;
    private StatusChangeRule statusChangeRule;

    private String statusChangeVAS;
    private Date statusChangeStartDate;
    private Date statusChangeEndDate;
    private boolean applyReversalEndDate;
    private List<SelectItem> subscriptionStatusList;
    private Date vasExpirationDate;

    //vas add on the future
    private Date vasAddDate;

    //service change dialog
    private LazyDataModel<Service> lazyServiceList;
    private Service selectedService;
    private String serviceName;
    private Long filterServiceID;
    private MiniPop subscriptionMiniPop;
    private boolean applyFutureDate;
    private boolean cancelCharge = true;
    private Date serviceChangeFutureDate;
    private List<Service> serviceList;

    //campaigns
    private LazyDataModel<CampaignRegister> campaignList;

    private SubscriptionReactivation reactivation;

    private String desc;
    private String name;
    private String surname;

    //campaigns
    private List<Campaign> availableCampaignListOnCreation;
    private List<Campaign> availableCampaignList;
    private Campaign selectedCampaign;
    private String campaignName;
    private CampaignRegister selectedRegister;
    private String campaignNotes;
    private List<String> authRejectionStatuses;


    private long unbilledInv = 0;

    @EJB
    private CampaignPersistenceFacade campaignFacade;
    @EJB
    private CampaignJoinerBean campaignJoinerBean;

    @EJB
    private SecurityManager securityManager;
    @EJB
    private SalesPartnerPersistenceFacade partnerFacade;
    @EJB
    private RangePersistenceFacade rangeFacade;

    private List<Equipment> dealerEquipment;
    private Equipment selectedDealerEquipment;
    private List<Equipment> filteredDealerEquipment;
    private String parameterDealerEquipmentPartnumber;

    private String provisioningReason;
    private String provisioningReasonStandard;

    private ExternalStatusInformation externalStatusInformation;
    private UIComponent provisioningReasonSelectMenu;
    private UIComponent provisioiningReasonOtherInput;

    private DateTime vasExpDate;
    private int staticIpCount = 1;
    private double vasFee;

    public double getVasFee() {
        return vasFee;
    }

    public void setVasFee(double vasFee) {
        this.vasFee = vasFee;
    }

    public double getTotalVasFee() {
        return totalVasFee;
    }

    public void setTotalVasFee(double totalVasFee) {
        this.totalVasFee = totalVasFee;
    }

    public void totalVasFeeListener() {
        log.debug("getVasFee() " + getVasFee());
        log.debug("getVasAddCount() " + getVasAddCount());
        totalVasFee = getVasFee() * getVasAddCount();
    }

    private double totalVasFee;
    @EJB
    private JobPersistenceFacade jobPersistenceFacade;
    //job list for the subscription
    private LazyDataModel<Job> jobLazyDataModel;
    private Job selectedJob;
    private double installFee;

    private String passportNumber;

    public String getPassportNumber() {
        if (passportNumber == null) {
            passportNumber = selectedSubscriber.getDetails().getPassportNumber();
        }
        return passportNumber;
    }

    public void setPassportNumber(String passportNumber) {
        this.passportNumber = passportNumber;
        this.subscription.setAgreement(this.passportNumber);
    }

    public void prolongPrepaidSubscriptionOneTime() {
        if (selected == null) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Subscription not selected", "Subscription not selected"));
            return;
        }
        try {
            if (selected.getStatus() == SubscriptionStatus.INITIAL || selected.getStatus() == SubscriptionStatus.ACTIVE) {
                if (!systemLogger.checkOneTimeProlongationIsApplied(selected.getAgreement())) {
                    if (selected.getStatus() == SubscriptionStatus.INITIAL) {
                        engineFactory.getOperationsEngine(selected).activatePrepaid(selected);
                    } else if (selected.getStatus() == SubscriptionStatus.ACTIVE) {
                        engineFactory.getOperationsEngine(selected).prolongPrepaid(selected);
                    }
                    engineFactory.getProvisioningEngine(selected).reprovision(selected);
                    systemLogger.success(SystemEvent.SUBSCRIPTION_PROLONGED_FOR_ONE_TIME, selected, "subscription prolonged for one time");
                    FacesContext.getCurrentInstance().addMessage(null,
                            new FacesMessage(FacesMessage.SEVERITY_INFO,
                                    "Successfull operation", "Successfull"));
                } else {
                    FacesContext.getCurrentInstance().addMessage(null,
                            new FacesMessage(FacesMessage.SEVERITY_INFO,
                                    "One time prolongation was used for this subscription", "One time prolongation was used for this subscription"));
                }
            } else {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO,
                                "You can only apply this operation to INITIAL and ACTIVE subscriptions",
                                "You can only apply this operation to INITIAL and ACTIVE subscriptions"));
            }
        } catch (Exception e) {
            log.error(e);
            e.printStackTrace(System.err);
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Subscription can not be prolonged, ", "Subscription can not be prolonged"));
        }
    }

    public boolean isAllowedBillingModelForUniNet(BillingModel mb) {
        return mb.getName().equalsIgnoreCase("grace");
    }

    public List<Service> getServiceList() {
        return serviceList;
    }

    public void setServiceList(List<Service> serviceList) {
        this.serviceList = serviceList;
    }

    public Date getVasExpirationDate() {
        return vasExpirationDate;
    }

    public void setVasExpirationDate(Date vasExpirationDate) {
        this.vasExpirationDate = vasExpirationDate;
    }

    public Date getVasAddDate() {
        return vasAddDate;
    }

    public void setVasAddDate(Date vasAddDate) {
        this.vasAddDate = vasAddDate;
    }

    private List<String> unitList;
    private static final String MB_STR = "m";
    private static final String KB_STR = "k";

    public List<String> getUnitList() {
        if (unitList == null) {
            unitList = new ArrayList<>();
        }
        if (unitList.isEmpty()) {
            unitList.add(MB_STR);
            unitList.add(KB_STR);
        }
        return unitList;
    }

    public void setUnitList(List<String> unitList) {
        this.unitList = unitList;
    }

    public LazyDataModel<Job> getJobLazyDataModel() {
        if (jobLazyDataModel == null) {
            jobPersistenceFacade.clearFilters();
            String sqlQuery = "SELECT DISTINCT j FROM Job j JOIN j.propertyList plist "
                    + "WHERE plist.type = :property_type and plist.value = :property_value";
            jobPersistenceFacade.addFilter(JobPersistenceFacade.Filter.PROPERTY_VALUE, String.valueOf(selected.getId()));
            jobPersistenceFacade.addFilter(JobPersistenceFacade.Filter.PROPERTY_TYPE, JobPropertyType.TARGET);
            jobLazyDataModel = new LazyTableModelDynamic<>(
                    jobPersistenceFacade,
                    Job.class,
                    sqlQuery);

            String countSqlQuery = "SELECT COUNT(DISTINCT j) FROM Job j JOIN j.propertyList plist "
                    + "WHERE plist.type = :property_type and plist.value = :property_value";
            int rowCount = (int) jobPersistenceFacade.countDynamic(countSqlQuery);
            jobLazyDataModel.setRowCount(rowCount);
        }
        return jobLazyDataModel;
    }

    public void setJobLazyDataModel(LazyDataModel<Job> jobLazyDataModel) {
        this.jobLazyDataModel = jobLazyDataModel;
    }

    public Job getSelectedJob() {
        return selectedJob;
    }

    public void setSelectedJob(Job selectedJob) {
        this.selectedJob = selectedJob;
    }

    public void onEditJob() {
        if (selectedJob == null) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Please select job", "Please select job"));
            return;
        }
        log.debug(String.format("onEditJob: selectedJob=%s", selectedJob.toString()));
        //String message = String.format("The job with id %d is updated.", selectedJob.getId());
        //String ref_message = "Please refresh the page to see change";
        //FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, message, ref_message));
        selectedJob.setDeadline(selectedJob.getStartTime().plusDays(1));
        jobPersistenceFacade.update(selectedJob);
    }

    public MinipopCategory getMinipopCategory() {
        return minipopCategory;
    }

    public void setMinipopCategory(MinipopCategory minipopCategory) {
        this.minipopCategory = minipopCategory;
    }

    public List<MinipopCategory> getMinipopCategoryList() {
        if (minipopCategoryList == null) {
            minipopCategoryList = new ArrayList<>();
            for (MinipopCategory category : MinipopCategory.values()) {
                minipopCategoryList.add(category);
            }
        }
        return minipopCategoryList;
    }

    public void setMinipopCategoryList(List<MinipopCategory> minipopCategoryList) {
        this.minipopCategoryList = minipopCategoryList;
    }

    public List<MiniPop> getMinipopListTemp() {
        return minipopListTemp;
    }

    public void setMinipopListTemp(List<MiniPop> minipopListTemp) {
        this.minipopListTemp = minipopListTemp;
    }

    public List<MiniPop> getMiniPopList() {
        if (miniPopList == null) {
            miniPopList = minipopFacade.findAll();
        }
        return miniPopList;
    }

    public void setMiniPopList(List<MiniPop> miniPopList) {
        this.miniPopList = miniPopList;
    }

    public Ats getAts() {
        if (ats != null) {
            return ats;
        }
        if (getSelected() != null) {
            ats = atsFacade.findByIndex(getSelected().getDetails().getAts());
        }
        return ats;
    }

    public void setAts(Ats ats) {
        this.ats = ats;
    }

    public ServiceProvider getProvider() {
        return provider;
    }

    public void setProvider(ServiceProvider provider) {
        this.provider = provider;
    }

    public String getFreeIpAddressString() {
        return freeIpAddressString;
    }

    public void setFreeIpAddressString(String freeIpAddressString) {
        this.freeIpAddressString = freeIpAddressString;
    }

    public String getChargedIpAddressString() {
        return chargedIpAddressString;
    }

    public void setChargedIpAddressString(String chargedIpAddressString) {
        this.chargedIpAddressString = chargedIpAddressString;
    }

    public IpAddress getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(IpAddress ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getUninetAtsPort() {
        if (selected != null) {
            for (SubscriptionSetting ss : selected.getSettings()) {
                if (ss.getProperties().getType() == ServiceSettingType.ATS_PORT) {
                    uninetAtsPort = ss.getValue();
                }
            }
        }
        return uninetAtsPort;
    }


    public void setUninetAtsPort(String uninetAtsPort) {
        this.uninetAtsPort = uninetAtsPort;
    }


    public String getUninetSlot() {
        if (selected != null) {
            for (SubscriptionSetting ss : selected.getSettings()) {
                if (ss.getProperties().getType() == ServiceSettingType.BROADBAND_SWITCH_SLOT) {
                    uninetSlot = ss.getValue();
                }
            }
        }
        return uninetSlot;
    }

    public void setUninetSlot(String uninetSlot) {
        this.uninetSlot = uninetSlot;
    }

    public String getUninetMAC() {
        if (selected != null) {
            for (SubscriptionSetting ss : selected.getSettings()) {
                if (ss.getProperties().getType() == ServiceSettingType.MAC_ADDRESS) {
                    uninetMAC = ss.getValue();
                }
            }
        }
        return uninetMAC;
    }

    public void setUninetMAC(String uninetMAC) {
        this.uninetMAC = uninetMAC;
    }

    public String getUninetPort() {
        if (selected != null) {
            for (SubscriptionSetting ss : selected.getSettings()) {
                if (ss.getProperties().getType() == ServiceSettingType.BROADBAND_SWITCH_PORT) {
                    uninetPort = ss.getValue();
                }
            }
        }
        return uninetPort;
    }

    public void setUninetPort(String uninetPort) {
        this.uninetPort = uninetPort;
    }

    public String getUninetInfo() {
        if (selected != null) {
            for (SubscriptionSetting ss : selected.getSettings()) {
                if (ss.getProperties().getType() == ServiceSettingType.INFO) {
                    uninetInfo = ss.getValue();
                }
            }
        }
        return uninetInfo;
    }

    public void setUninetInfo(String uninetInfo) {
        this.uninetInfo = uninetInfo;
    }

    public String getUninetPassword() {
        if (selected != null) {
            for (SubscriptionSetting ss : selected.getSettings()) {
                if (ss.getProperties().getType() == ServiceSettingType.PASSWORD) {
                    uninetPassword = ss.getValue();
                }
            }
        }
        return uninetPassword;
    }

    public void setUninetPassword(String uninetPassword) {
        this.uninetPassword = uninetPassword;
    }

    public String getStaticIp() {
        if (staticIp != null) {
            return staticIp;
        }
        Subscription subscription = getSelected();
        if (subscription != null && subscription.getSettingByType(ServiceSettingType.IP_ADDRESS) != null) {
            SubscriptionSetting setting = subscription.getSettingByType(ServiceSettingType.IP_ADDRESS);
            staticIp = setting.getValue();
        }
        return staticIp;
    }


    public void setStaticIp(String staticIp) {
        this.staticIp = staticIp;
    }


    public String getSipNumber() {
        if (sipNumber != null) {
            return sipNumber;
        }
        Subscription subscription = getSelected();
        if (subscription != null && subscription.getSettingByType(ServiceSettingType.SIP) != null) {
            SubscriptionSetting setting = subscription.getSettingByType(ServiceSettingType.SIP);
            sipNumber = setting.getValue();
        }
        return sipNumber;
    }

    public void setSipNumber(String sipNumber) {
        this.sipNumber = sipNumber;
    }

    public void saveOperation() {

        if (getProviderID().equals(Providers.DATAPLUS.getId())) {
            saveDataplusSettings();
        } else if (getProviderID().equals(Providers.GLOBAL.getId())) {
            saveGlobalSettings();
        } else if (getProviderID().equals(Providers.CNC.getId())) {
            saveCNCSettings();
        }

        String oldStatic = null;
        String newStatic;
        Subscription subscription = getSelected();

        SubscriptionSetting subscriptionSetting = subscription.getSettingByType(ServiceSettingType.IP_ADDRESS);
        try {
            oldStatic = subscriptionSetting.getValue();
        } catch (Exception e) {
            log.debug("exception " + e);
        }
        newStatic = getStaticIp();

        log.debug("{{{{{" + oldStatic + "}}}}}{{{{{" + newStatic + "}}}}}");
        log.debug("newstatic checking " + newStatic.isEmpty());

        if (oldStatic == null & newStatic.isEmpty()) {
            return;
        }
        if (oldStatic == null & !newStatic.isEmpty()) {

            addStaticIP();
            log.debug("added ip");
        } else if (!oldStatic.isEmpty() & newStatic.isEmpty()) {

            removeStaticIP();
            log.debug("deleted ip");
        } else if (!oldStatic.isEmpty() & !newStatic.isEmpty() & oldStatic != newStatic) {

            modifyStaticIP();
            log.debug("modified ip");
        }

        log.debug("old and new static ips : " + oldStatic + "   -   " + newStatic);

    }


    public void addStaticIP() {
        try {
            Subscription subscription = getSelected();
            log.info(String.format("addStaticIP, static ip = %s, subscription id = %d , count = %d", staticIp,
                    subscription.getId(), staticIpCount));
            ValueAddedService vas = vasFacade.findEligibleVas(subscription);

            String[] ipSplitted = staticIp.split("\\.");
            IpAddress address = new IpAddress(
                    Integer.valueOf(ipSplitted[0]),
                    Integer.valueOf(ipSplitted[1]),
                    Integer.valueOf(ipSplitted[2]),
                    Integer.valueOf(ipSplitted[3])
            );

            selected = engineFactory.getOperationsEngine(subscription).addVAS(
                    new VASCreationParams.Builder().
                            setSubscription(subscription).
                            setValueAddedService(vas).
                            setIpAddress(address).
                            setExpiresDate(null).
                            setCount(staticIpCount >= 0 ? staticIpCount : 0).
                            build()
            );
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(SEVERITY_INFO, "Static IP added successfully", ""));
        } catch (Exception e) {
            e.printStackTrace(System.err);
            log.debug("error " + e);
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(SEVERITY_ERROR, "Static IP can not be added", e + ""));
        }
    }

    private static boolean validSipNumber(String sipNumber) {
        return (sipNumber != null && sipNumber.matches("[0-9]+") && sipNumber.length() == 7);
    }

    public void addSipNumber() {
        try {
            Subscription subscription = getSelected();
            log.info(String.format("addSipNumber, sip number = %s, subscription id = %d", sipNumber, subscription.getId()));
            ValueAddedService vas = vasFacade.findEligibleSipVas(subscription);

            Subscription assigned = subscriptionFacade.getBySetting(ServiceSettingType.SIP, sipNumber);
            if (assigned != null) {
                String msg = String.format("Sip number already assigned to agreement %s", assigned.getAgreement());
                log.debug(msg);
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(SEVERITY_ERROR, msg, msg));
                return;
            }
            if (!validSipNumber(sipNumber)) {
                String msg = "Sip number not valid";
                log.debug(msg);
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(SEVERITY_ERROR, msg, msg));
                return;
            }

            selected = engineFactory.getOperationsEngine(subscription).addVAS(
                    new VASCreationParams.Builder().
                            setSubscription(subscription).
                            setValueAddedService(vas).
                            setSipNumber(sipNumber).
                            setExpiresDate(null).
                            setCount(1).
                            build()
            );
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(SEVERITY_INFO, "SIP number added successfully", ""));
        } catch (Exception e) {
            e.printStackTrace(System.err);
            log.error(e);
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(SEVERITY_ERROR, "SIP number can not be added", ""));
        }
    }

    public void modifyStaticIP() {
        try {
            Subscription subscription = getSelected();
            log.info(String.format("modifyStaticIP, static ip = %s, subscription id = %d", staticIp, subscription.getId()));

            String[] ipSplitted = staticIp.split("\\.");
            IpAddress address = new IpAddress(
                    Integer.valueOf(ipSplitted[0]),
                    Integer.valueOf(ipSplitted[1]),
                    Integer.valueOf(ipSplitted[2]),
                    Integer.valueOf(ipSplitted[3])
            );

            long sbnVasId = 0L;
            if (subscription.getVasList() != null && !subscription.getVasList().isEmpty()) {
                sbnVasId = subscription.getVasList().get(0).getId();
            }
            selected = engineFactory.getOperationsEngine(subscription).editVAS(
                    new VasEditParams.Builder().
                            setSubscription(subscription).
                            setSubscriptionVasId(sbnVasId).
                            setIpAddress(address).
                            setCount(staticIpCount).build());

            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(SEVERITY_INFO, "Static IP modified successfully", ""));
        } catch (Exception e) {
            e.printStackTrace(System.err);
            log.error(e);
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(SEVERITY_ERROR, "Static IP can not be modified", ""));
        }
    }

    public void modifySipNumber() {
        try {
            Subscription subscription = getSelected();
            log.info(String.format("modifySipNumber, sip number = %s, subscription id = %d", sipNumber, subscription.getId()));

            Subscription assigned = subscriptionFacade.getBySetting(ServiceSettingType.SIP, sipNumber);
            if (assigned != null) {
                String msg = String.format("Sip number already assigned to agreement %s", assigned.getAgreement());
                log.debug(msg);
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(SEVERITY_ERROR, msg, msg));
                return;
            }
            if (!validSipNumber(sipNumber)) {
                String msg = "Sip number not valid";
                log.debug(msg);
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(SEVERITY_ERROR, msg, msg));
                return;
            }

            ValueAddedService vas = vasFacade.findEligibleSipVas(subscription);
            long sbnVasId = subscription.getVASByServiceId(vas.getId()).getId();

            selected = engineFactory.getOperationsEngine(subscription).editVAS(
                    new VasEditParams.Builder().
                            setSubscription(subscription).
                            setSubscriptionVasId(sbnVasId).
                            setSipNumber(sipNumber).
                            setCount(1).build());

            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(SEVERITY_INFO, "SIP number modified successfully", ""));
        } catch (Exception e) {
            e.printStackTrace(System.err);
            log.error(e);
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(SEVERITY_ERROR, "SIP number can not be modified", ""));
        }
    }

    public void removeStaticIP() {
        try {
            Subscription subscription = getSelected();

            SubscriptionVAS subscriptionVAS = null;
            if (subscription.getVasList() != null && !subscription.getVasList().isEmpty()) {
                subscriptionVAS = subscription.getVasList().get(0);
            }

            selected = engineFactory.getOperationsEngine(subscription).
                    removeVAS(subscription, subscriptionVAS);
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(SEVERITY_INFO, "Static IP removed successfully", ""));
        } catch (Exception e) {
            e.printStackTrace(System.err);
            log.error(e);
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(SEVERITY_ERROR, "Static IP can not removed", ""));
        }
    }

    public void removeSipNumber() {
        try {
            Subscription subscription = getSelected();

            ValueAddedService vas = vasFacade.findEligibleSipVas(subscription);
            SubscriptionVAS subscriptionVAS = subscription.getVASByServiceId(vas.getId());

            selected = engineFactory.getOperationsEngine(subscription).
                    removeVAS(subscription, subscriptionVAS);
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(SEVERITY_INFO, "Sip Vas removed successfully", ""));
        } catch (Exception e) {
            e.printStackTrace(System.err);
            log.error(e);
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(SEVERITY_ERROR, "Sip Vas can not be removed", ""));
        }
    }

    public void lastMonthsSipDetails() throws IOException {
        Subscription subscription = getSelected();
        String assignedSip = subscription.getSettingByType(ServiceSettingType.SIP).getValue();

        ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
        externalContext.redirect(String.format("http://192.168.40.9?m=l&p=%s", assignedSip));
    }

    public void currentMonthsSipDetails() throws IOException {
        Subscription subscription = getSelected();
        String assignedSip = subscription.getSettingByType(ServiceSettingType.SIP).getValue();

        ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
        externalContext.redirect(String.format("http://192.168.40.9?m=c&p=%s", assignedSip));
    }

    public List<IpAddress> getFreeIpList() {
        if (freeIpList == null && selected != null) {
            freeIpList = new ArrayList<>();
            MiniPop miniPop = subscriptionFacade.findMinipop(selected);
            List<IpAddressRange> ipRanges = rangeFacade.findRangesByNas(miniPop.getNas());
            for (IpAddressRange range : ipRanges) {
                freeIpList.addAll(range.findFreeAddresses());
            }
        }
        return freeIpList;
    }

    public void setFreeIpList(List<IpAddress> freeIpList) {
        this.freeIpList = freeIpList;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public Long getFilterServiceID() {
        return filterServiceID;
    }

    public void setFilterServiceID(Long filterServiceID) {
        this.filterServiceID = filterServiceID;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public Service getSelectedService() {
        return selectedService;
    }

    public void setSelectedService(Service selectedService) {
        this.selectedService = selectedService;
    }

    public LazyDataModel<Service> getLazyServiceList() {
        return lazyServiceList;
    }

    public void setLazyServiceList(LazyDataModel<Service> lazyServiceList) {
        this.lazyServiceList = lazyServiceList;
    }


    public List<Dealer> getDealerList() {
        return dealerList == null ? dealerList = dealerFacade.findAll() : dealerList;
    }

    public void setDealerList(List<Dealer> dealerList) {
        this.dealerList = dealerList;
    }

    public List<SelectItem> getServiceTypeSelectList() {
        if (serviceTypeSelectList == null) {
            serviceTypeSelectList = new ArrayList<>();
            List<SubscriptionServiceType> serviceTypeList = serviceTypePersistenceFacade.findAll();
            for (final SubscriptionServiceType serviceType : serviceTypeList) {

                log.debug("serviceType.getProvider() " + serviceType.getProvider());
                if (serviceType.getProvider() == null || serviceType.getProvider().getId() == getProviderID()) {
                    serviceTypeSelectList.add(new SelectItem(serviceType.getId(), serviceType.getName()));
                }
            }
        }
        return serviceTypeSelectList;
    }

    public void setServiceTypeSelectList(List<SelectItem> serviceTypeSelectList) {
        this.serviceTypeSelectList = serviceTypeSelectList;
    }

    public String getServiceTypeId() {
        if (serviceTypeId != null) {
            return serviceTypeId;
        }
        if (getSelected() != null && getSelected().getSettingByType(ServiceSettingType.SERVICE_TYPE) != null) {
            serviceTypeId = getSelected().getSettingByType(ServiceSettingType.SERVICE_TYPE).getValue();
        }
        return serviceTypeId;
    }

    public void setServiceTypeId(String serviceTypeId) {
        this.serviceTypeId = serviceTypeId;
    }

    public boolean isPPPoE() {

        if (serviceTypeId != null) {
            SubscriptionServiceType serviceType =
                    serviceTypePersistenceFacade.find(Long.parseLong(serviceTypeId));
            return serviceType.getProfile().getProfileType().equals(ProfileType.PPPoE);
        }
        return true;
    }

    public List<SelectItem> getZoneSelectList() {
        if (zoneSelectList == null) {
            List<Zone> zoneList = zonePersistenceFacade.findAll();
            zoneSelectList = new ArrayList<>();

            for (Zone zone : zoneList)
                zoneSelectList.add(new SelectItem(zone.getId(), zone.getName()));
        }

        return zoneSelectList;
    }

    public void setZoneSelectList(List<SelectItem> zoneSelectList) {
        this.zoneSelectList = zoneSelectList;
    }

    public String getZoneId() {
        if (zoneId != null) {
            return zoneId;
        }
        if (getSelected() != null && getSelected().getSettingByType(ServiceSettingType.ZONE) != null) {
            zoneId = getSelected().getSettingByType(ServiceSettingType.ZONE).getValue();
        }
        return zoneId;
    }

    public void setZoneId(String zoneId) {
        this.zoneId = zoneId;
    }

    public List<SelectItem> getSubscriptionStatusList() {
        if (subscriptionStatusList == null && selected != null) {
            subscriptionStatusList = new ArrayList<>();

            for (SubscriptionStatus st : SubscriptionStatus.values()) {
                if (st != st.BLOCKED) {
                    subscriptionStatusList.add(new SelectItem(st));
                }
            }
        }
        return subscriptionStatusList;
    }

    public void setSubscriptionStatusList(List<SelectItem> subscriptionStatusList) {
        this.subscriptionStatusList = subscriptionStatusList;
    }

    public Date getStatusChangeStartDate() {
        return statusChangeStartDate;
    }

    public void setStatusChangeStartDate(Date statusChangeStartDate) {
        this.statusChangeStartDate = statusChangeStartDate;
    }

    public boolean isApplyReversalEndDate() {
        return applyReversalEndDate;
    }

    public void setApplyReversalEndDate(boolean applyReversalEndDate) {
        this.applyReversalEndDate = applyReversalEndDate;
    }

    public Date getStatusChangeEndDate() {
        return statusChangeEndDate;
    }

    public void setStatusChangeEndDate(Date statusChangeEndDate) {
        this.statusChangeEndDate = statusChangeEndDate;
    }

    public StatusChangeRule getStatusChangeRule() {
        return statusChangeRule;
    }

    public void setStatusChangeRule(StatusChangeRule statusChangeRule) {
        this.statusChangeRule = statusChangeRule;
    }

    public SubscriptionStatus getSelectedStatus() {
        if (selectedStatus == null && selected != null) {
            selectedStatus = selected.getStatus();
        }
        return selectedStatus;
    }

    public void setSelectedStatus(SubscriptionStatus selectedStatus) {
        this.selectedStatus = selectedStatus;
    }

    public List<ValueAddedService> getAllowedVasList() {
        if (allowedVasList == null && selected != null) {
            allowedVasList = new ArrayList<>(selected.getService().getAllowedVASList());
            allowedVasList = allowedVasList.stream().filter(vas -> !vas.isSip()).collect(Collectors.toList());

        }

        return allowedVasList;
    }

    public void setAllowedVasList(List<ValueAddedService> allowedVasList) {
        this.allowedVasList = allowedVasList;
    }

    public UIComponent getVasGrid() {
        return vasGrid;
    }

    public void setVasGrid(UIComponent vasGrid) {
        this.vasGrid = vasGrid;
    }

    public UIComponent getForm() {
        return form;
    }

    public void setForm(UIComponent form) {
        this.form = form;
    }

    public UIComponent getSbnVASDialog() {
        return sbnVASDialog;
    }

    public void setSbnVASDialog(UIComponent sbnVASDialog) {
        this.sbnVASDialog = sbnVASDialog;
    }

    public List<SelectItem> getSbnVasStatusList() {
        if (sbnVasStatusList == null) {
            sbnVasStatusList = new ArrayList<>();
            List<SubscriptionStatus> sbnVasStatuses = new ArrayList<>(Arrays.asList(SubscriptionStatus.values()));

            for (SubscriptionStatus st : sbnVasStatuses) {
                sbnVasStatusList.add(new SelectItem(st));
            }
        }
        return sbnVasStatusList;
    }

    public void setSbnVasStatusList(List<SelectItem> sbnVasStatusList) {
        this.sbnVasStatusList = sbnVasStatusList;
    }

    public SubscriptionStatus getSbnVasStatus() {
        return sbnVasStatus;
    }

    public void setSbnVasStatus(SubscriptionStatus sbnVasStatus) {
        this.sbnVasStatus = sbnVasStatus;
    }

    public List<SelectItem> getSbnVASTypeList() {
        if (sbnVASTypeList == null) {
            sbnVASTypeList = new ArrayList<>();

            for (ValueAddedServiceType type : ValueAddedServiceType.values()) {
                sbnVASTypeList.add(new SelectItem(type));
            }
        }
        return sbnVASTypeList;
    }

    public void setSbnVASTypeList(List<SelectItem> sbnVASTypeList) {
        this.sbnVASTypeList = sbnVASTypeList;
    }

    public ValueAddedServiceType getSbnVASType() {
        return sbnVASType;
    }

    public void setSbnVASType(ValueAddedServiceType sbnVASType) {
        this.sbnVASType = sbnVASType;
    }

    public void editVAS() {
        if (selected == null || selectedSbnVAS == null) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "PLease select VAS", "PLease select VAS"));
            return;
        }

        ipAddress = null;
        if (selectedSbnVAS.getVas().getCode().getType() == ValueAddedServiceType.PERIODIC_STATIC && selectedSbnVAS.getVas().isStaticIp() && selectedSbnVAS.getVas().getStaticIPType() == StaticIPType.NORMAL_CHARGED
                && chargedIpAddressString != null && !chargedIpAddressString.isEmpty()) {
            for (IpAddress address : freeIpList) {
                if (address.getAddressAsString().equals(chargedIpAddressString)) {
                    ipAddress = address;
                    break;
                }
            }

            log.debug("Charged ipAddress: " + ipAddress.getAddressAsString());
        } else if (selectedSbnVAS.getVas().getCode().getType() == ValueAddedServiceType.PERIODIC_STATIC && selectedSbnVAS.getVas().isStaticIp() && selectedSbnVAS.getVas().getStaticIPType() == StaticIPType.COMMANDANT_FREE
                && freeIpAddressString != null && !freeIpAddressString.isEmpty()) {
            for (IpAddress address : freeIpList) {
                if (address.getAddressAsString().equals(freeIpAddressString)) {
                    ipAddress = address;
                    break;
                }
            }

            log.debug("Free ipAddress: " + ipAddress.getAddressAsString());
        }

        selected = engineFactory.getOperationsEngine(selected).editVAS(
                new VasEditParams.Builder().
                        setSubscription(selected).
                        setSubscriptionVasId(selectedSbnVAS.getId()).
                        setIpAddress(ipAddress).
                        setExpiresDate(vasExpDate).build()
        );
        selectedSbnVAS = selected.getVASById(selectedSbnVAS.getId());
    }

    public void stayIdle() {
        log.debug("stayIdle called: selected vas=" + selectedSbnVAS);
    }

    public void createVASDialog() {
        log.debug("createVASDialog: starting");

        Dialog dialog = new Dialog();
        dialog.setHeader("VAS Dialog");

        PanelGrid panel = new PanelGrid();
        panel.setColumns(2);
        panel.setRendered(true);

        OutputLabel nameLabel = new OutputLabel();
        nameLabel.setValue("Name");
        nameLabel.setRendered(true);

        HtmlOutputText nameText = new HtmlOutputText();
        nameText.setValue(selectedSbnVAS.getVas().getName());
        nameText.setRendered(true);

        panel.getChildren().add(nameLabel);
        panel.getChildren().add(nameText);

        dialog.getChildren().add(panel);
        dialog.setRendered(true);

        vasGrid.getChildren().add(dialog);

        RequestContext ctx = RequestContext.getCurrentInstance();
        ctx.update(vasGrid.getClientId());

        log.debug("createVASDialog: finished");
    }

    public void removeVAS() {
        if (selected == null || selectedSbnVAS == null) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "PLease select VAS", "PLease select VAS"));
            return;
        }

        selected = engineFactory.getOperationsEngine(selected).
                removeVAS(selected, selectedSbnVAS);
        selectedSbnVAS = null;
        completeSbnVASList = new ArrayList<>(selected.getVasList());

    }

    public void searchSbnVAS(AjaxBehaviorEvent ev) {
        log.debug(String.format("searchSbnVAS: starting search, vasName=%s, vasType=%s, vasList=%s", filterSvbVASName, sbnVASType, completeSbnVASList));
        List<SearchVASCriteria> criteria = checkSearchVAS();

        if (criteria.isEmpty()) {
            return;
        }

        if (selected != null && selected.getVasList() != null && !selected.getVasList().isEmpty()) {
            completeSbnVASList = new ArrayList<>();

            for (SubscriptionVAS sbnVAS : selected.getVasList()) {
                if (isSbnVasMatch(sbnVAS, criteria)) {
                    log.debug(String.format("searchSbnVas: matched, sbnVas id=%d, criteria=%s, completeSbnVasList=%s", sbnVAS.getId(), criteria, completeSbnVASList));
                    completeSbnVASList.add(sbnVAS);
                }
            }
        }
        log.debug("searchSbnVAS: finished search");

    }

    private List<SearchVASCriteria> checkSearchVAS() {
        List<SearchVASCriteria> criteria = new ArrayList<>();

        if (filterSvbVASName != null && !filterSvbVASName.isEmpty()) {
            criteria.add(SearchVASCriteria.NAME);
        }

        if (sbnVASType != null) {
            criteria.add(SearchVASCriteria.TYPE);
        }

        if (sbnVasStatus != null) {
            criteria.add(SearchVASCriteria.STATUS);
        }

        return criteria;
    }

    public String resetPassword() {
        Subscription selected = getSelected();
        selected.getDetails().setPassword(selected.getAgreement());

        //FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Password resetted successfully", "Password resetted successfully"));
        //log.debug("password will be resetted to agreement number upon pressing save button. Subscription id = " + selected.getId());
        //messages
        selected = subscriptionFacade.update(selected);
        return String.format("/pages/subscription/view/view_%d.xhtml?subscriber=%d&amp;subscription=%d&amp;faces-redirect=true&amp;includeViewParams=true", selected.getService().getProvider().getId(), selected.getSubscriber().getId(), selected.getId());
    }

    public void resetVAS() {
        if (selected != null && selected.getVasList() != null && !selected.getVasList().isEmpty()) {
            selectedSbnVAS = null;
            filterSvbVASName = null;
            sbnVASType = null;
            completeSbnVASList = selected.getVasList();
            sbnVasStatus = null;
        }
    }

    public boolean isSbnVasMatch(SubscriptionVAS sbnVAS, List<SearchVASCriteria> criteria) {
        List<Boolean> matrix = new ArrayList<>();

        for (SearchVASCriteria crit : criteria) {
            switch (crit) {
                case NAME:
                    if (sbnVAS.getVas().getName().contains(filterSvbVASName)) {
                        matrix.add(true);
                    }
                    break;
                case TYPE:
                    if (sbnVAS.getVas().getCode().getType() == sbnVASType) {
                        matrix.add(true);
                    }
                    break;
                case STATUS:
                    if (sbnVAS.getStatus() == sbnVasStatus) {
                        matrix.add(true);
                    }
                    break;
            }
        }
        log.debug(String.format("isSbnVasMatch: martix=%s, criteria=%s", matrix, criteria));
        return matrix.size() == criteria.size();
    }

    public List<SubscriptionVAS> getCompleteSbnVASList() {
        if (completeSbnVASList == null && selected != null) {
            completeSbnVASList = selected.getVasList().stream().filter(vas -> !vas.getVas().isSip())
                    .collect(Collectors.toList());
        }
        return completeSbnVASList;
    }

    public void setCompleteSbnVASList(List<SubscriptionVAS> completeSbnVASList) {
        this.completeSbnVASList = completeSbnVASList;
    }

    public void removeSbnVAS() {
        if (selected == null || selectedSbnVAS == null) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Please select VAS", "Please select VAS"));
            return;
        }

        Iterator<SubscriptionVAS> it = completeSbnVASList.iterator();
        SubscriptionVAS vas = null;

        while (it.hasNext()) {
            vas = it.next();

            if (vas.getId() == selectedSbnVAS.getId()) {
                selectedSbnVAS = null;
                it.remove();
            }
        }
    }

    public String getFilterSvbVASName() {
        return filterSvbVASName;
    }

    public void setFilterSvbVASName(String filterSvbVASName) {
        this.filterSvbVASName = filterSvbVASName;
    }

    public SubscriptionVAS getSelectedSbnVAS() {
        return selectedSbnVAS;
    }

    public void setSelectedSbnVAS(SubscriptionVAS selectedSbnVAS) {
        this.selectedSbnVAS = selectedSbnVAS;
    }

    public boolean checkVasExist(Subscription subscription, ValueAddedService vas) {
        if (vas.getMaxNumber() != 1 || subscription.getActiveResource() == null) {
            return false;
        }
        for (SubscriptionVAS sbnVAS : subscription.getVasList()) {
            if (sbnVAS.getVas().getId() == vas.getId()) {
                return true;
            }
        }
        return false;
    }

    private double vasAddCount = 1;

    public long getServiceFee() {
        return serviceFee;
    }

    public void setServiceFee(long serviceFee) {
        this.serviceFee = serviceFee;
    }

    private long serviceFee;


    public void serviceFreeListener() {

        serviceFee = (long) ((this.subscription.getServiceFeeRate() / 1.18) / 100000 * this.speed);
        if (serviceFee > 20000) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(SEVERITY_ERROR, "Monthly Fee More than 20 000 AZN", "Installation fee is not selected"));
        }

    }


    public Long getSpeed() {
        return this.speed;
    }

    public void setSpeed(Long speed) {
        log.debug("SubscriptionManager speed " + speed);

        this.speed = speed;

    }


    public double getVasAddCount() {
        if (vasAddCount == 0) {
            return 1;
        }
        return vasAddCount;
    }

    public void setVasAddCount(double vasAddCount) {
        this.vasAddCount = vasAddCount;
    }

    public void setVasAddCount(int vasAddCount) {
        this.vasAddCount = vasAddCount;
    }

    public void addVas() {
        if (selectedVAS == null) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Please select VAS", "Please select VAS"));
            return;
        }
        if (selected == null) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Subscription not recognized", "Subscription not recognized"));
            return;
        }

        if (checkVasExist(selected, selectedVAS)) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "The VAS already exist", "The VAS already exist"));
            return;
        }

        log.debug("vas fee and count " + getVasFee() + " ** " + getVasAddCount());

        if (selected.getService().getProvider().getId() == Providers.AZERTELECOMPOST.getId()) {
            selectedVAS.setPriceInDouble(getVasFee());
            selectedVAS.setCount(getVasAddCount());
        }

        if (vasAddDate != null && (new DateTime(vasAddDate).isAfter(DateTime.now().plusMinutes(61)))) {
            jobPersistenceFacade.createVasAddJob(
                    selected,
                    selectedVAS.getId(),
                    vasExpirationDate,
                    selectedVAS.getStaticIPType() == StaticIPType.NORMAL_CHARGED ? chargedIpAddressString : freeIpAddressString,
                    getVasAddCount(),
                    new DateTime(vasAddDate));
        } else {
            selected = subscriptionFacade.addVAS(selected, selectedVAS, vasExpirationDate,
                    selectedVAS.getStaticIPType() == StaticIPType.NORMAL_CHARGED ? chargedIpAddressString : freeIpAddressString,
                    getVasAddCount(), freeIpList);
        }

        vasFacade.clearFilters();
        setVasAddCount(1);
        completeSbnVASList = new ArrayList<>(selected.getVasList());
        resetAllowedVasList();
    }


    public void addVasBlocking() throws Exception {
        log.debug("addVasBlocking: start");
        Thread.currentThread().sleep(30000);
        log.debug("addVasBlocking: end");
    }

    public void onStatusChange(AjaxBehaviorEvent ev) {
        if (selectedStatus == null) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Please select status", "Please select status"));
            return;
        }

        if (selected == null) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Subscription not recognized", "Subscription not recognized"));
            return;
        }

        String msg = null;
        HttpServletResponse resp = (HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext().getResponse();

        if (selectedStatus == SubscriptionStatus.FINAL) {
            if (selected.getBalance().getRealBalance() < 0 && selected.getService().getProvider().getId() != Providers.UNINET.getId()) {
                msg = "Subscription cannot be FINALIZED before all debt is paid up";

                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, msg, msg));
                selectedStatus = selected.getStatus();
                return;
            }/*else {
                NEVER EVER delete vas-es on ajax call.
                try {
                    subscriptionFacade.finalizeSubscription(selected);
                } catch (Exception ex) {
                    msg = "Cannot finalize subscription";

                    log.error(String.format("onChangeStatus: %s id=%d", msg, selected.getId()), ex);

                    FacesContext.getCurrentInstance().addMessage(null,
                            new FacesMessage(FacesMessage.SEVERITY_ERROR, msg, msg));
                    selectedStatus = selected.getStatus();
                    return;
                }
            }*/
        }

        if (selected.getService().getStatusChangeRules() != null) {

            if (selectedStatus == SubscriptionStatus.BLOCKED) {
                msg = "This status is not available for manual assignation";

                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, msg, msg));
                selectedStatus = selected.getStatus();
                return;
            }

            statusChangeRule = selected.getService().findRule(selected.getStatus(), selectedStatus);

            if (statusChangeRule == null) {
                msg = String.format("No rule found for transition from %s to %s status", selected.getStatus(), selectedStatus);
                selectedStatus = selected.getStatus();

                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, msg, msg));

                return;
            }

            DateTime tmp = DateTime.now().plusHours(1);
            statusChangeStartDate = tmp.toDate();
            statusChangeEndDate = tmp.plusMonths(1).toDate();
            RequestContext ctx = RequestContext.getCurrentInstance();
            ctx.execute("PF('statusChangeDialog').show();");
        } else {
            msg = "No status change rules found";
            selectedStatus = selected.getStatus();

            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, msg, msg));
        }
    }

    public String restoreToInitial() {
        if (selected.getStatus().equals(SubscriptionStatus.FINAL) ||
                selected.getStatus().equals(SubscriptionStatus.CANCEL)) {
            //FINAL, CANCEL -> INITIAL
            //create transaction, charge for service fee/installation fee and invoice
            boolean restored = subscriptionFacade.restoreSubscription(selected);
            RequestContext context = RequestContext.getCurrentInstance();
            context.addCallbackParam("restored", restored);
        }
        return null;
    }

    public void changeStatus() {
        if (selectedStatus == null) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Please select status", "Please select status"));
            return;
        }

        if (selected == null) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Subscription not recognized", "Subscription not recognized"));
            return;
        }

        if (selected.getService().getStatusChangeRules() != null) {

            if (selectedStatus == SubscriptionStatus.BLOCKED) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "This status is not available for manual assignation", "This status is not available for manual assignation"));
                return;
            }

            statusChangeRule = selected.getService().findRule(selected.getStatus(), selectedStatus);

            if (statusChangeRule != null) {
                try {
                    DateTime endDate = null;
                    if (applyReversalEndDate) {
                        endDate = new DateTime(statusChangeEndDate);
                    }
                    log.debug("1");
                    selected = subscriptionFacade.addChangeStatusJob(selected, selectedStatus, new DateTime(statusChangeStartDate), endDate);
                    log.debug("2");
                    selectedStatus = selected.getStatus();
                    log.debug("3");
                    completeSbnVASList = null;
                    log.debug("4");
                    completeSbnVASList = getCompleteSbnVASList();
                    log.debug("5");
                    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Operation executed successfully", "Operation executed successfully"));
                } catch (Exception ex) {
                    String msg = "Cannot execute operation";
                    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, msg, msg));
                    log.error("changeStatus: " + msg, ex);
                }
            }
        }
    }

    public void activateSubscriptionManually() {

        try {
            DateTime activationDate = selected.getActivationDate();
            ProvisioningEngine provisioner = null;
            boolean provisionResult = false;
            try {
                provisioner = engineFactory.getProvisioningEngine(selected);
            } catch (ProvisionerNotFoundException e) {
                log.error("Privisioner not found on activatePrepaid.", e);
            }

            if (selected.getStatus() == SubscriptionStatus.INITIAL
                    && selected.getBalance().getRealBalance() >= 0
                    && activationDate != null) {
                log.debug("Balance is >= 0");
                selected.setActivationDate(activationDate);
                selected.setBilledUpToDate(activationDate.plusMonths(1).withTime(23, 59, 59, 999));
                selected.setExpirationDate(selected.getBilledUpToDate());
                selected.setExpirationDateWithGracePeriod(selected.getExpirationDate().plusDays(selected.getBillingModel().getGracePeriodInDays()));
                if (selected.getBillingModel().getPrinciple() == BillingPrinciple.GRACE_MONTH) {
                    selected.setExpirationDateWithGracePeriod(selected.getExpirationDate().plusMonths(1));
                }
                selected = commonEngine.changeStatus(selected, SubscriptionStatus.ACTIVE);
                selected = subscriptionFacade.update(selected);
                provisionResult = provisioner.reprovision(selected);

                if (provisionResult) {
                    log.info(String.format("UniNet manual Activation process for prepaid Subscription agreement = %s successfully finished", selected.getAgreement()));
                    log.info(String.format("reprovision: subscription id=%d, agreement=%s, status=%s, result=%b, reprovisionReason=%s reprovisioning successfull",
                            selected.getId(), selected.getAgreement(), selected.getStatus(), provisionResult, provisioningReason != null && !provisioningReason.isEmpty() ? provisioningReason : provisioningReasonStandard));
                    systemLogger.success(SystemEvent.REPROVISION, selected, String.format("status=%s, reason=%s", selected.getStatus(), provisioningReason != null && !provisioningReason.isEmpty()
                            ? provisioningReason : provisioningReasonStandard));
                    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Activation and Provisioning", "Operation executed successfully"));
                    reloadExternalStatusInformation();
                } else {
                    systemLogger.error(SystemEvent.REPROVISION, selected, String.format("status=%s, reason=%s", selected.getStatus(), provisioningReason != null
                            && !provisioningReason.isEmpty() ? provisioningReason : provisioningReasonStandard));
                    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Activation and Provisioning", "Operation failed"));
                    log.error(String.format("reprovision: subscription id=%d, agreement=%s, result=%b, reprovisionReason=%s reprovisioning failed",
                            selected.getId(), selected.getAgreement(), provisionResult, provisioningReason != null && !provisioningReason.isEmpty()
                                    ? provisioningReason : provisioningReasonStandard));
                }
            } else {
                log.debug("Balance is < 0. Skipping...");
                return;
            }
            log.debug("Activation finished for: " + selected.getAgreement());
        } catch (Exception ex) {
            log.info("Activation process for prepaid finished with error: ", ex);
        }
    }


    public void onResetVas(ActionEvent event) {
        vasName = null;
        vasType = null;

        vasFacade.removeFilter(VASPersistenceFacade.Filter.NAME);
        vasFacade.removeFilter(VASPersistenceFacade.Filter.TYPE);

        vasList = new LazyTableModel(vasFacade);
    }

    public void onVASDIalogHide(AjaxBehaviorEvent event) {
        resetAllowedVasList();
    }

    public LazyTableModel<ValueAddedService> getVasList() {
        return vasList;
    }

    public void setVasList(LazyTableModel<ValueAddedService> vasList) {
        this.vasList = vasList;
    }

    public ValueAddedService getSelectedVAS() {
        return selectedVAS;
    }

    public void setSelectedVAS(ValueAddedService selectedVAS) {
        this.selectedVAS = selectedVAS;
    }

    public String getVasName() {
        return vasName;
    }

    public void setVasName(String vasName) {
        this.vasName = vasName;
    }

    public ValueAddedServiceType getVasType() {
        return vasType;
    }

    public void setVasType(ValueAddedServiceType vasType) {
        this.vasType = vasType;
    }

    public void onVasSearch(AjaxBehaviorEvent event) {
        vasFacade.clearFilters();

        if (selected != null) {
            vasFacade.addFilter(VASPersistenceFacade.Filter.PROVIDER, selected.getService().getProvider().getId());
        }

        if (vasName != null && !vasName.isEmpty()) {
            vasFacade.addFilter(VASPersistenceFacade.Filter.NAME, vasName);
        }

        if (vasType != null) {
            VASPersistenceFacade.Filter typeFilter = VASPersistenceFacade.Filter.TYPE;
            typeFilter.setOperation(MatchingOperation.EQUALS);
            vasFacade.addFilter(typeFilter, vasType);
        }

        vasList = new LazyTableModel(vasFacade);
    }

    public void onAllowedVasSearch(AjaxBehaviorEvent ev) {
        List<SearchVASCriteria> criteria = getAllowedVASSearchCriteria();

        if (selected == null) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Subscription unknown", "Subscription unknown"));
            return;
        }
        if (checkAllowedVasSearchCriteria()) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Please provide criteria for search", "Please provide criteria for search"));
            return;
        }
        allowedVasList = new ArrayList<>();
        selectedVAS = null;

        for (ValueAddedService vas : selected.getService().getAllowedVASList()) {
            if (isAllowedVASMatches(vas, criteria)) {
                allowedVasList.add(vas);
            }
        }

    }

    private boolean checkAllowedVasSearchCriteria() {
        return ((vasName == null || vasName.isEmpty()) && vasType == null);
    }

    private boolean isAllowedVASMatches(ValueAddedService vas, List<SearchVASCriteria> criteria) {
        List<Boolean> matches = new ArrayList<>();

        if (vasName != null && !vasName.isEmpty() && vas.getName().toLowerCase().contains(vasName.toLowerCase())) {
            matches.add(true);
        }

        if (vasType != null && vas.getCode().getType() == vasType) {
            matches.add(true);
        }

        if (vas.isActive()) {
            matches.add(true);
        }

        return matches.size() == criteria.size();
    }

    private List<SearchVASCriteria> getAllowedVASSearchCriteria() {
        List<SearchVASCriteria> criteria = new ArrayList<>();

        if (vasName != null && !vasName.isEmpty()) {
            criteria.add(SearchVASCriteria.NAME);
        }

        if (vasType != null) {
            criteria.add(SearchVASCriteria.TYPE);
        }

        criteria.add(SearchVASCriteria.STATUS);

        return criteria;
    }

    public void resetAllowedVasList() {
        vasName = null;
        vasType = null;
        selectedVAS = null;
        allowedVasList = null;
        chargedIpAddressString = null;
        freeIpAddressString = null;
        ipAddress = null;
        freeIpList = null;
        getAllowedVasList();
    }

    //vas
    public List<SelectItem> getVasTypeList() {
        if (vasTypeList == null) {
            vasTypeList = new ArrayList<>();

            for (ValueAddedServiceType tp : ValueAddedServiceType.values()) {
                vasTypeList.add(new SelectItem(tp));
            }
        }
        return vasTypeList;
    }

    //subscription
    public String getVasTabName() {
        return vasTabName;
    }

    public void setVasTabName(String vasTabName) {
        this.vasTabName = vasTabName;
    }

    public Subscription getSubscription() {
        if (subscription == null) {
            subscription = new Subscription();
            subscription.setType(SubscriptionType.STATIC);
            //   subscription.setExpirationDate(DateTime.now().minusDays(1));
        }
        return subscription;
    }

    @PostConstruct
    public void init() {
        try {
            equipmentPersistenceFacade.clearFilters();
            equipmentPersistenceFacade.addFilter(EquipmentPersistenceFacade.Filter.STATUS, EquipmentStatus.AVAILABLE);
            campaignRegisterFacade.clearFilters();

            campaignList = null;

            getAllowedVasList();
            minipopFacade.clearFilters();

            MiniPopPersistenceFacade.Filter statusFilter = MiniPopPersistenceFacade.Filter.STATUS;
            statusFilter.setOperation(MatchingOperation.EQUALS);
            minipopFacade.addFilter(statusFilter, DeviceStatus.ACTIVE);

            if (selected != null) {
                selectedStatus = selected.getStatus();
            }
        } catch (Exception e) {

        }
    }

    public void setSubscription(Subscription subscription) {
        this.subscription = subscription;
    }

    public Balance getBalance() {
        return balance;
    }

    public void setBalance(Balance balance) {
        this.balance = balance;
    }

    public Service getService() {
        return service;
    }

    public void setService(Service service) {
        this.service = service;
    }

    public List<Subscription> getSubscriptionList() {
        return subscriptionList;
    }

    public void setSubscriptionList(List<Subscription> subscriptionList) {
        this.subscriptionList = subscriptionList;
    }

    public Subscription getSelected() {
        if (selected == null && subscriptionID != null) {
            selected = subscriptionFacade.find(subscriptionID);
        }
        /*for (SubscriptionSetting ss : selected.getSettings()) {
            switch (ss.getProperties().getType()) {
                case MAC_ADDRESS:
                    uninetMAC = ss.getValue();
                    break;
                case BROADBAND_SWITCH_PORT:
                    uninetPort = ss.getValue();
                    break;
                case INFO:
                    uninetInfo = ss.getValue();
                    break;
                case BROADBAND_SWITCH_SLOT:
                    uninetSlot = ss.getValue();
                    break;
            }

        }*/
        return selected;
    }

    public void setSelected(Subscription selected) {
        this.selected = selected;
    }

    public TechnicalStatus getTechnicalStatus() {
        try {
            if (selected != null && technicalStatus == null) {
                selected.setTechnicalStatus(technicalStatusPersistenceFacade.getTechnicalStatus(selected));
                if (selected.getTechnicalStatus().getElements().size() > 0) {
                    technicalStatus = selected.getTechnicalStatus();
                }
            }
        } catch (Exception e) {
            log.error(e);
        }
        return technicalStatus;
    }


    public List<String> getAuthRejectionStatuses() {
        try {
            if (authRejectionStatuses == null && selected != null) {
                return authRejectionStatuses = rejectionReasonPersistenceFacade.getAuthRejectionReasons(selected);
            } else
                return authRejectionStatuses;
        } catch (Exception e) {
            log.error(e);
        }
        return new ArrayList<String>();
    }

    public void refreshAuthRejectionReasons() {
        try {
            authRejectionStatuses = rejectionReasonPersistenceFacade.getAuthRejectionReasons(selected);
            log.info("AUTH REJECTION REASONS REFRESHED >>>" + selected);
        } catch (Exception e) {
            e.printStackTrace(System.err);
            log.error(e);
        }
    }

    public void setAuthRejectionStatuses(List<String> authRejectionStatuses) {
        this.authRejectionStatuses = authRejectionStatuses;
    }

    public String getUnbilledAmount() {
        Subscription subscription = getSelected();
        Double totalCharge = ((subscription.calculateTotalCharge()
                - subscription.getBalance().getRealBalance()) / 100000.0); //prepaid calculations minus real balance
        if (totalCharge < 0.0) {
            totalCharge = 0.0;
        }
        return String.valueOf(totalCharge);
    }

    public List<ChargeForView> chargeForViewList;

    public List<ChargeForView> getUnbilledCharges() {
        if (chargeForViewList == null) {
            chargeForViewList = new ArrayList<>();
            Subscription subscription = getSelected();
            int remainingDays = -1;

            if (subscription.getStatus().equals(SubscriptionStatus.BLOCKED)
                    || subscription.getStatus().equals(SubscriptionStatus.PARTIALLY_BLOCKED)) {
                if (subscription.getExpirationDate() != null
                        && DateTimeComparator.getDateOnlyInstance().compare(null, subscription.getExpirationDate()) <= 0) {
                    remainingDays = Days.daysBetween(DateTime.now(), subscription.getExpirationDate()).getDays();
                }
            }
            subscription.getExpirationDate();
            chargeForViewList.add(
                    new ChargeForView(
                            subscription.getService().getName(),
                            1l,
                            subscription.getService().getServicePriceInDouble(),
                            remainingDays
                    ));
            List<SubscriptionVAS> vasList = subscription.getVasList();
            for (SubscriptionVAS vas : vasList) {
                if (vas.getVas().getCode().getType() == ValueAddedServiceType.PERIODIC_STATIC
                        && vas.getStatus() != SubscriptionStatus.FINAL) {
                    chargeForViewList.add(
                            new ChargeForView(
                                    vas.getVas().getName(),
                                    vas.getCount(),
                                    vas.getVas().getPriceInDouble(),
                                    -1
                            ));
                }
            }
        }
        return chargeForViewList;
    }

    public List<OnlineBroadbandStats> getOnlineStats() {
        try {
            if (selected != null && onlineStats == null) {
                OnlineBroadbandStats stats = loadOnlineSession();
                log.debug("Stats: " + stats);
                if (stats != null) {
                    onlineStats = new ArrayList<>();
                    onlineStats.add(stats);
                }
            }
        } catch (Exception e) {
            log.error(e);
        }
        return onlineStats == null ? new ArrayList<OnlineBroadbandStats>() : onlineStats;
    }

    public List<OfflineBroadbandStats> getOfflineStats() {
        if (selected != null && offlineStats == null) {
            offlineStats = offlineStatsFacade.getRecentSessions(selected.getAgreement());
        }
        return offlineStats;
    }

    public void setOfflineStats(List<OfflineBroadbandStats> offlineStats) {
        this.offlineStats = offlineStats;
    }

    public OnlineBroadbandStats loadOnlineSession() {
        return onlineStatsFacade.getOnlineSession(selected);
    }

    public void reloadSession() {
        onlineStats = null;
        technicalStatus = null;
        onlineStats = getOnlineStats();
    }

    public void reprovision() {
        if (selected == null) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "No subscirption found", "No subscirption found"));
            return;
        }

        if ((provisioningReason == null || provisioningReason.isEmpty())
                && (provisioningReasonStandard == null || provisioningReasonStandard.isEmpty())) {
            FacesContext.getCurrentInstance().validationFailed();
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Reason is required", "Reason is required"));
            return;
        }

        // RequestContext.getCurrentInstance().execute("PF('blockUIGeneral').show();");
        try {
            if (provisioner == null) {
                provisioner = engineFactory.getProvisioningEngine(selected);
            }

            boolean res = provisioner.reprovision(selected);

            if (res) {
                log.info(String.format("reprovision: subscription id=%d, agreement=%s, status=%s, result=%b, reprovisionReason=%s reprovisioning successfull",
                        selected.getId(), selected.getAgreement(), selected.getStatus(), res, provisioningReason != null && !provisioningReason.isEmpty() ? provisioningReason : provisioningReasonStandard));
                systemLogger.success(SystemEvent.REPROVISION, selected, String.format("status=%s, reason=%s", selected.getStatus(), provisioningReason != null && !provisioningReason.isEmpty()
                        ? provisioningReason : provisioningReasonStandard));
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Reprovisioning successfull", "Reprovisioning successfull"));
                reloadExternalStatusInformation();
            } else {
                systemLogger.error(SystemEvent.REPROVISION, selected, String.format("status=%s, reason=%s", selected.getStatus(), provisioningReason != null
                        && !provisioningReason.isEmpty() ? provisioningReason : provisioningReasonStandard));
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Reprovsioning failed", "Reprovsioning failed"));
                log.error(String.format("reprovision: subscription id=%d, agreement=%s, result=%b, reprovisionReason=%s reprovisioning failed",
                        selected.getId(), selected.getAgreement(), res, provisioningReason != null && !provisioningReason.isEmpty()
                                ? provisioningReason : provisioningReasonStandard));
            }
        } catch (Exception ex) {
            log.error(String.format("reprovision: agreement=%s. Cannot reprovision", selected.getAgreement()), ex);
        }

        // RequestContext.getCurrentInstance().execute("PF('blockUIGeneral').hide();");
    }

    public List<String> getServiceIdList() {
        return serviceIdList;
    }

    public void setServiceIdList(List<String> serviceIdList) {
        this.serviceIdList = serviceIdList;
    }

    public List<SelectItem> getServiceSelectList() {
        if (serviceSelectList == null) {
            log.debug("serviceSelectList is: empty");
            serviceSelectList = new ArrayList<SelectItem>();
            /*
            List<Service> srvList = serviceFacadeserviceFacade.findAll();

            for (Service srv : srvList)
                serviceSelectList.add(new SelectItem(srv.getId(), srv.getName()));
             */
        }

        return serviceSelectList;
    }

    public void setServiceSelectList(List<SelectItem> serviceSelectList) {
        this.serviceSelectList = serviceSelectList;
    }

    public List<Subscription> getFilteredSubscriptionList() {
        return filteredSubscriptionList;
    }

    public void setFilteredSubscriptionList(List<Subscription> filteredSubscriptionList) {
        this.filteredSubscriptionList = filteredSubscriptionList;
    }

    public String getSubscriberId() {
        return subscriberId;
    }

    public void setSubscriberId(String subscriberId) {
        this.subscriberId = subscriberId;
    }

    public List<SelectItem> getSubscriberSelectList() {
        if (subscriberSelectList == null) {

        }
        return subscriberSelectList;
    }

    public void setSubscriberSelectList(List<SelectItem> subscriberSelectList) {
        this.subscriberSelectList = subscriberSelectList;
    }

    public String getServiceId() {
        if (serviceId != null) {
            return serviceId;
        }
        if (getSelected() != null) {
            serviceId = String.valueOf(getSelected().getService().getId());
        }
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
        if (serviceId != null) {
            service = serviceFacade.find(Long.valueOf(serviceId));
        } else {
            service = null;
        }
    }

    public LazyDataModel<Subscriber> getSubscriberLazyTableModel() {
        if (subscriberLazyTableModel == null) {
            //  subscriberLazyTableModel = new SubscriberLazyTableModel(subscriberFacade);
            subscriberLazyTableModel = new LazyTableModel<Subscriber>(subscriberFacade);
        }
        return subscriberLazyTableModel;
    }

    public Subscriber getSelectedSubscriber() {
        if (selectedSubscriber == null && subscriberID != null) {
            selectedSubscriber = subscriberFacade.find(subscriberID);
        }
        return selectedSubscriber;
    }

    public void setSelectedSubscriber(Subscriber selectedSubscriber) {
        this.selectedSubscriber = selectedSubscriber;
    }

    public List<Subscriber> getFilteredSubscriberList() {
        return filteredSubscriberList;
    }

    public void setFilteredSubscriberList(List<Subscriber> filteredSubscriberList) {
        this.filteredSubscriberList = filteredSubscriberList;
    }

    public LazyDataModel<Subscription> getSubscriptionLazyTableModel() {
        if (subscriptionLazyTableModel == null) //subscriptionLazyTableModel = new SubscriptionLazyTableModel(subscriptionFacade);
        {
            subscriptionLazyTableModel = new LazyTableModel<Subscription>(subscriptionFacade);
        }
        return subscriptionLazyTableModel;
    }

    public void setSubscriptionLazyTableModel(LazyDataModel<Subscription> subscriptionLazyTableModel) {
        this.subscriptionLazyTableModel = subscriptionLazyTableModel;
    }

    public List<SubscriptionResourceBucket> getBucketList() {
        if (bucketList == null && selected != null) {
            bucketList = new ArrayList<SubscriptionResourceBucket>();
            for (SubscriptionResource res : this.selected.getResources()) {
                bucketList.addAll(res.getBucketList());
            }
        }
        /*if (bucketList == null && selected != null) {
                this.selected.getService().getResourceList().size();
                List<Resource> resList = this.selected.getService().getResourceList();

                log.debug("service: " + selected.getService() + ", resource list: " + resList);
                if (resList.size() > 0) {
                    this.bucketList = new ArrayList<SubscriptionResourceBucket>();

                    for (Resource res : resList) {
                        SubscriptionResource subResource = new SubscriptionResource(res);
                        this.selected.addResource(subResource);

                        for (SubscriptionResourceBucket buck : subResource.getBucketList()) {
                            this.bucketList.add(buck);
                        }
                    }
                }
                selected.getResources().size();
                log.debug("sub resourceList: " + selected.getResources());
        }*/
        return bucketList;
    }

    public void setBucketList(List<SubscriptionResourceBucket> bucketList) {
        this.bucketList = bucketList;
    }


    public String getBucket() {
        if (bucketList == null && selected != null) {
            bucketList = new ArrayList<SubscriptionResourceBucket>();
            for (SubscriptionResource res : this.selected.getResources()) {

                subBucket = res.getBucketByType(ResourceBucketType.INTERNET_DOWN);
                log.debug("1 inter speed  " + subBucket.getCapacity());
                return subBucket.getCapacity();
            }
        }

        if (subBucket != null) {
            log.debug("2 inter speed  " + subBucket.getCapacity());
            return subBucket.getCapacity();
        } else {
            return "0";
        }
    }

    public void setBucket(String speed) {
        for (SubscriptionResource subRes : this.selected.getResources()) {

            List<SubscriptionResourceBucket> bucketList = subRes.getBucketList();
            for (SubscriptionResourceBucket bucket : bucketList) {

                bucket.setCapacity(speed);
                log.debug("bucket " + bucket.toString());
            }
        }

//        log.debug("getting speed set up 0 "+Long.parseLong(speed));
//        subscription.setSpeed(Long.parseLong(speed));
//        log.debug("getting speed set up 1");
//        bucketList = new ArrayList<SubscriptionResourceBucket>();
//        log.debug("getting speed set up 2");
//        SubscriptionResourceBucket resource1 = new SubscriptionResourceBucket();
//        log.debug("getting speed set up 3");
//        resource1.setType(ResourceBucketType.INTERNET_UP);
//        log.debug("getting speed set up 4");
//        resource1.setCapacity(speed.toString());
//        log.debug("getting speed set up 5");
//        resource1.setUnit("m");
//        log.debug("getting speed set up 6");
//        bucketList.add(resource1);
//        log.debug("getting speed set up 7");
//        SubscriptionResourceBucket resource2 = new SubscriptionResourceBucket();
//        log.debug("getting speed set up 8");
//        resource2.setType(ResourceBucketType.INTERNET_DOWN);
//        log.debug("getting speed set up 9");
//        resource2.setCapacity(speed.toString());
//        log.debug("getting speed set up 10");
//        resource2.setUnit("m");
//        log.debug("getting speed set up 11");
//        bucketList.add(resource2);
//        log.debug("getting speed set up 12");
//        this.bucketList = bucketList;
        log.debug("speed has been se up");

    }


    public SubscriptionResourceBucket getSubBucket() {
        return subBucket;
    }

    public void setSubBucket(SubscriptionResourceBucket subBucket) {
        this.subBucket = subBucket;
    }

    public List<SelectItem> getTypeSelectList() {
        if (typeSelectList == null) {
            typeSelectList = new ArrayList<SelectItem>();
            for (SubscriptionType type : SubscriptionType.values()) {
                typeSelectList.add(new SelectItem(type, type.toString()));
            }
        }
        return typeSelectList;
    }

    public void setTypeSelectList(List<SelectItem> typeSelectList) {
        this.typeSelectList = typeSelectList;
    }

    public List<SelectItem> getTypeSelectListForView() {
        return typeSelectListForView;
    }

    public void setTypeSelectListForView(List<SelectItem> typeSelectListForView) {
        if (typeSelectListForView == null) {
            typeSelectListForView = new ArrayList<SelectItem>();
            typeSelectListForView.add(new SelectItem(this.selected.getType(), this.selected.getType().toString()));

            for (SubscriptionType type : SubscriptionType.values()) {
                if (type == this.selected.getType()) {
                    continue;
                }

                typeSelectListForView.add(new SelectItem(type, type.toString()));
            }
        }
        this.typeSelectListForView = typeSelectListForView;
    }

    public Long getSubscriptionID() {
        return subscriptionID;
    }

    public void setSubscriptionID(Long subscriptionID) {
        this.subscriptionID = subscriptionID;
    }

    public void onRowSelect(SelectEvent e) {
        log.debug("Select event fired!" + isUseSubscriberAddress);
        // getDetails().setCity("Baku");
        if (isUseSubscriberAddress && selectedSubscriber != null) {
            details = new SubscriptionDetails(selectedSubscriber);
        }
        //selectedSubscriber = (Subscriber) e.getObject();
        //log.debug("onRowSelect: sub=" + selectedSubscriber);
    }

    public MiniPop findTempMinipop(ServiceProvider provider) {
        MiniPop tempMinipop = null;
        List<MiniPop> testMinipops = minipopFacade.findByCategory(provider, MinipopCategory.TEST);
        if (testMinipops == null) {
            log.debug("Cannot find test minipop");
            return null;
        }
        for (MiniPop miniPop : testMinipops) {
            if (miniPop.getNextAvailablePortHintAsNumber() != null && miniPop.getNextAvailablePortHintAsNumber() <= miniPop.getNumberOfPorts()) {
//                log.debug("Found temp minipop: " + miniPop);
                tempMinipop = miniPop;
                break;
            }
        }
        return tempMinipop;
    }

    public String create() {
        if (selectedSubscriber == null) {
            log.debug("Cannot create subscription. Subscriber not selected");
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(SEVERITY_ERROR, "Subscriber must be selected", "There is no subscriber selected"));
            return null;
        }
        if (isUseStockEquipment && selectedEquipment == null) {
            log.debug("Cannot create subscription. Equipment not selected");
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(SEVERITY_ERROR, "Equipment must be selected", "There is no equipment selected"));
            return null;
        }

        if (serviceId == null) {
            log.debug("Cannot create subscription. Service not selected");
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(SEVERITY_ERROR, "Service must be selected", "There is no service selected"));
            return null;
        }

        if (service != null && service.getSettingByType(ServiceSettingType.BROADBAND_SWITCH) != null && selectedMiniPop == null) {
            log.debug("Cannot create subscription. Minipop not selected");
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(SEVERITY_ERROR, "Minipop must be selected", "There is no minipop selected"));
            return null;
        }

        if (service != null && service.getSettingByType(ServiceSettingType.BROADBAND_SWITCH) != null
                && selectedMiniPop != null && selectedMiniPop.getPreferredPort() != null
                && !selectedMiniPop.checkPort(selectedMiniPop.getPreferredPort())) {
            log.debug("Cannot create subscription. Minipop's preferred port is invalid. " + selectedMiniPop);
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(SEVERITY_ERROR, "Minipop's port is invalid", "Minipop's port is invalid"));
            return null;
        }


        //add default VASes
        if (getSubscriptionVAS() != null && getSubscriptionVAS().getSettings().size() > 0) {
            log.debug("subscriptionVAS.getVAS: " + subscriptionVAS.getVas());
            subscription.addVAS(subscriptionVAS);
        }

        if (service != null && service.getDefaultVasList() != null
                && (subscription.getVasList().size() < service.getDefaultVasList().size())) {
            log.debug("Not all charges for default VAS added: subscriptionVas: " + subscriptionVAS.getSettings() + ", defaultVasList: " + service.getDefaultVasList());
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(SEVERITY_ERROR, "Not all charges added", "Not all charges added"));
            return null;
        }

        log.debug("check it "+subscription.getSettingByType(ServiceSettingType.USERNAME));

        subscription.setBillingModel(modelFacade.find(BillingPrinciple.CONTINUOUS));
        //log.debug("selected subscriber: " + selectedSubscriber + ", subscription = " + subscription);
        try {
            /*if (defaultVasCharges != null && !defaultVasCharges.isEmpty()) {
               for (Map.Entry<Long, Long> entry : defaultVasCharges.entrySet())
                   subscription.addDefaultVasCharge(entry.getKey(), entry.getValue());
           }


          if (service.getDefaultVasList() != null && !service.getDefaultVasList().isEmpty()) {
               SubscriptionVAS subscriptionVAS = null;

               for (ValueAddedService vas : service.getDefaultVasList()) {
                   if (vas.getId() == CABLE_VAS_CODE) {
                       subscriptionVAS = new SubscriptionVAS();
                       subscriptionVAS.setVas(vas);
                       subscriptionVAS.setBilledUpToDate(DateTime.now());
                       subscriptionVAS.setExpirationDate(DateTime.now());

                       subscriptionVAS.addSetting("price", String.valueOf(cableCharge),"Fee per 1 meter of cable");
                       subscriptionVAS.addSetting("length",String.valueOf(cableLength),"Cable length in meters");

                       subscription.addVAS(subscriptionVAS);
                   }
               }
           }*/

            subscriptionFacade.findByAgreement(subscription.getAgreement());
            subscription.setDetails(details);
            subscription.getDetails().setLanguage(language);
            subscription.setTaxFreeEnabled(taxFreeEnabled == null ? false : taxFreeEnabled);
            log.debug("Selected minipop: " + selectedMiniPop);
            // Port port = minipopFacade.getAvailablePort(selectedMiniPop);
            // log.debug("Resulting port: " + port);
            log.debug(" this is 0 "+subscription.getSettingByType(ServiceSettingType.USERNAME));
            String userName = FacesContext.getCurrentInstance().getExternalContext().getUserPrincipal().getName();
            log.debug(" this is 1");
            User user = userFacade.findByUserName(userName);
            log.debug(" this is 2 " + service.getProvider());
            OperationsEngine operationsEngine = engineFactory.getOperationsEngine(service.getProvider());

            operationsEngine.createSubscription(selectedSubscriber,
                    subscription,
                    serviceId,
                    selectedMiniPop,
                    isUseStockEquipment,
                    selectedEquipment,
                    notificationSettings,
                    installFee,
                    selectedCampaign,
                    (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false),
                    user);
            log.debug(" this is 3");
            systemLogger.success(SystemEvent.SUBSCRIPTION_CREATED, subscription,
                    "Subscription created"
            );
            log.debug(" this is 4");
            try {
                queueManager.addToNotificationsQueue(BillingEvent.SUBSCRIPTION_ADDED, subscription.getId(), null);
                systemLogger.success(SystemEvent.NOTIFICATION_SCHEDULED, subscription,
                        String.format("Event=" + BillingEvent.SUBSCRIPTION_ADDED)
                );
            } catch (Exception ex) {
                log.error("Cannot add to notification queue: ", ex);
                systemLogger.error(SystemEvent.NOTIFICATION_SCHEDULED, subscription, ex.getCause().getMessage());
            }
            String outcome = new StringBuilder(SUBSCRIPTION_PATH)
                    .append(String.format("view/view_%d.xhtml?subscriber=", subscription.getService().getProvider().getId())).append(selectedSubscriber.getId())
                    .append("&amp;subscription=").append(subscription.getId())
                    .append("&amp;faces-redirect=true&amp;includeViewParams=true").toString();

            log.info("Subscription created successfully. Subscription=" + subscription);
            Log.debug("outcome data: " + outcome);
            //inMemory.addSubscription(subscription);

            subscription = null;
            selectedSubscriber = null;
            balance = null;
            subscriberSelectList = null;
            filteredSubscriberList = null;
            subscriberLazyTableModel = null;

            equipmentPersistenceFacade.clearFilters();
            minipopFacade.clearFilters();
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Subscription created successfully", "Subscription created successfully"));
            installFee = 0;
            campaignList = null;
            return outcome;
        } catch (DuplicateAgreementException ex) {
            log.error("Cannot create subscriptipn: ", ex);
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(SEVERITY_ERROR, "Agreement already exists", "Agreement already exists"));
            return null;
        } catch (NoFreePortLeftException ex) {
            log.error("Cannot create subscription: ", ex);
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(SEVERITY_ERROR, "Minipop has no free ports left", "Minipop has no free ports left"));
            return null;
        } catch (PortAlreadyReservedException ex) {
            String msg = String.format("Port %d already reserved or illegal", selectedMiniPop.getPreferredPort());
            log.error("Cannot create subscription: ", ex);
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(SEVERITY_ERROR, msg, msg));
            return null;
        } catch (Exception ex) {
            log.error("Cannot create subscriptipn: ", ex);
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(SEVERITY_ERROR, "Cannot create subscriptipn", "Cannot create subscriptipn"));
            systemLogger.error(SystemEvent.SUBSCRIPTION_CREATED, subscription, ex.getCause().getMessage());
            return null;
        }
    }




    public String createQutuNarhome() {
        if (selectedSubscriber == null) {
            log.debug("Cannot create subscription. Subscriber not selected");
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(SEVERITY_ERROR, "Subscriber must be selected", "There is no subscriber selected"));
            return null;
        }
        if (isUseStockEquipment && selectedEquipment == null) {
            log.debug("Cannot create subscription. Equipment not selected");
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(SEVERITY_ERROR, "Equipment must be selected", "There is no equipment selected"));
            return null;
        }

        if (serviceId == null) {
            log.debug("Cannot create subscription. Service not selected");
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(SEVERITY_ERROR, "Service must be selected", "There is no service selected"));
            return null;
        }

        if (service != null && service.getSettingByType(ServiceSettingType.BROADBAND_SWITCH) != null && selectedMiniPop == null) {
            log.debug("Cannot create subscription. Minipop not selected");
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(SEVERITY_ERROR, "Minipop must be selected", "There is no minipop selected"));
            return null;
        }

        if (service != null && service.getSettingByType(ServiceSettingType.BROADBAND_SWITCH) != null
                && selectedMiniPop != null && selectedMiniPop.getPreferredPort() != null
                && !selectedMiniPop.checkPort(selectedMiniPop.getPreferredPort())) {
            log.debug("Cannot create subscription. Minipop's preferred port is invalid. " + selectedMiniPop);
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(SEVERITY_ERROR, "Minipop's port is invalid", "Minipop's port is invalid"));
            return null;
        }


        //add default VASes
        if (getSubscriptionVAS() != null && getSubscriptionVAS().getSettings().size() > 0) {
            log.debug("subscriptionVAS.getVAS: " + subscriptionVAS.getVas());
            subscription.addVAS(subscriptionVAS);
        }

        if (service != null && service.getDefaultVasList() != null
                && (subscription.getVasList().size() < service.getDefaultVasList().size())) {
            log.debug("Not all charges for default VAS added: subscriptionVas: " + subscriptionVAS.getSettings() + ", defaultVasList: " + service.getDefaultVasList());
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(SEVERITY_ERROR, "Not all charges added", "Not all charges added"));
            return null;
        }

        subscription.setBillingModel(modelFacade.find(BillingPrinciple.CONTINUOUS));
        //log.debug("selected subscriber: " + selectedSubscriber + ", subscription = " + subscription);
        try {

            subscriptionFacade.findByAgreement(subscription.getAgreement());
            subscription.setDetails(details);
            subscription.getDetails().setLanguage(language);
            subscription.setTaxFreeEnabled(taxFreeEnabled == null ? false : taxFreeEnabled);
            log.debug("Selected minipop: " + selectedMiniPop);
            // Port port = minipopFacade.getAvailablePort(selectedMiniPop);
            // log.debug("Resulting port: " + port);
            log.debug(" this is 0");
            String userName = FacesContext.getCurrentInstance().getExternalContext().getUserPrincipal().getName();
            log.debug(" this is 1");
            User user = userFacade.findByUserName(userName);
            log.debug(" this is 2 " + service.getProvider());
            OperationsEngine operationsEngine = engineFactory.getOperationsEngine(service.getProvider());
log.debug("which class "+operationsEngine.getClass());
            operationsEngine.createSubscription(selectedSubscriber,
                    subscription,
                    serviceId,
                    selectedMiniPop,
                    isUseStockEquipment,
                    selectedEquipment,
                    notificationSettings,
                    installFee,
                    selectedCampaign,
                    (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false),
                    user);
            log.debug(" this is 3");
            systemLogger.success(SystemEvent.SUBSCRIPTION_CREATED, subscription,
                    "Subscription created"
            );
            log.debug(" this is 4");
            try {
                queueManager.addToNotificationsQueue(BillingEvent.SUBSCRIPTION_ADDED, subscription.getId(), null);
                systemLogger.success(SystemEvent.NOTIFICATION_SCHEDULED, subscription,
                        String.format("Event=" + BillingEvent.SUBSCRIPTION_ADDED)
                );
            } catch (Exception ex) {
                log.error("Cannot add to notification queue: ", ex);
                systemLogger.error(SystemEvent.NOTIFICATION_SCHEDULED, subscription, ex.getCause().getMessage());
            }
            String outcome = new StringBuilder(SUBSCRIPTION_PATH)
                    .append(String.format("view/view_%d.xhtml?subscriber=", subscription.getService().getProvider().getId())).append(selectedSubscriber.getId())
                    .append("&amp;subscription=").append(subscription.getId())
                    .append("&amp;faces-redirect=true&amp;includeViewParams=true").toString();

            log.info("Subscription created successfully. Subscription=" + subscription);
            Log.debug("outcome data: " + outcome);
            //inMemory.addSubscription(subscription);

            subscription = null;
            selectedSubscriber = null;
            balance = null;
            subscriberSelectList = null;
            filteredSubscriberList = null;
            subscriberLazyTableModel = null;

            equipmentPersistenceFacade.clearFilters();
            minipopFacade.clearFilters();
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Subscription created successfully", "Subscription created successfully"));
            installFee = 0;
            campaignList = null;
            return outcome;
        } catch (DuplicateAgreementException ex) {
            log.error("Cannot create subscriptipn: ", ex);
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(SEVERITY_ERROR, "Agreement already exists", "Agreement already exists"));
            return null;
        } catch (NoFreePortLeftException ex) {
            log.error("Cannot create subscription: ", ex);
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(SEVERITY_ERROR, "Minipop has no free ports left", "Minipop has no free ports left"));
            return null;
        } catch (PortAlreadyReservedException ex) {
            String msg = String.format("Port %d already reserved or illegal", selectedMiniPop.getPreferredPort());
            log.error("Cannot create subscription: ", ex);
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(SEVERITY_ERROR, msg, msg));
            return null;
        } catch (Exception ex) {
            log.error("Cannot create subscriptipn: ", ex);
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(SEVERITY_ERROR, "Cannot create subscriptipn", "Cannot create subscriptipn"));
            systemLogger.error(SystemEvent.SUBSCRIPTION_CREATED, subscription, ex.getCause().getMessage());
            return null;
        }
    }


    public String createAzPost() {
        if (selectedSubscriber == null) {
            log.debug("Cannot create subscription. Subscriber not selected");
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(SEVERITY_ERROR, "Subscriber must be selected", "There is no subscriber selected"));
            return null;
        }

        if (isUseStockEquipment && selectedEquipment == null) {
            log.debug("Cannot create subscription. Equipment not selected");
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(SEVERITY_ERROR, "Equipment must be selected", "There is no equipment selected"));
            return null;
        }

        if (serviceId == null) {
            log.debug("Cannot create subscription. Service not selected");
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(SEVERITY_ERROR, "Service must be selected", "There is no service selected"));
            return null;
        }

        if (service != null && service.getSettingByType(ServiceSettingType.BROADBAND_SWITCH) != null && selectedMiniPop == null) {
            log.debug("Cannot create subscription. Minipop not selected");
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(SEVERITY_ERROR, "Minipop must be selected", "There is no minipop selected"));
            return null;
        }

        if (service != null && service.getSettingByType(ServiceSettingType.BROADBAND_SWITCH) != null
                && selectedMiniPop != null && selectedMiniPop.getPreferredPort() != null
                && !selectedMiniPop.checkPort(selectedMiniPop.getPreferredPort())) {
            log.debug("Cannot create subscription. Minipop's preferred port is invalid. " + selectedMiniPop);
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(SEVERITY_ERROR, "Minipop's port is invalid", "Minipop's port is invalid"));
            return null;
        }

//        if (selectedInstallFeeRate == null) {
//            log.debug("Cannot create subscription. Installation fee is not selected.");
//            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(SEVERITY_ERROR, "Installation fee is not selected", "Installation fee is not selected"));
//        }
        if (serviceFee > 20000) {
            log.debug("Monthly fee more than 20K ");
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(SEVERITY_ERROR, "Error on setting Monthly Fee", "Monthly Fee More than 20 000 AZN"));
            return null;
        }

        //add default VASes
        if (getSubscriptionVAS() != null && getSubscriptionVAS().getSettings().size() > 0) {
            log.debug("subscriptionVAS.getVAS: " + subscriptionVAS.getVas());
            subscription.addVAS(subscriptionVAS);
        }

        if (service != null && service.getDefaultVasList() != null
                && (subscription.getVasList().size() < service.getDefaultVasList().size())) {
            log.debug("Not all charges for default VAS added: subscriptionVas: " + subscriptionVAS.getSettings() + ", defaultVasList: " + service.getDefaultVasList());
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(SEVERITY_ERROR, "Not all charges added", "Not all charges added"));
            return null;
        }

        subscription.setBillingModel(modelFacade.find(BillingPrinciple.GRACE));
//        log.debug("selected subscriber: " + selectedSubscriber + ", subscription = " + subscription);
        try {

            subscriptionFacade.findByAgreement(subscription.getAgreement());
            subscription.setDetails(details);
            subscription.setSpeed(speed);
            subscription.getDetails().setLanguage(language);
            subscription.setTaxFreeEnabled(taxFreeEnabled == null ? false : taxFreeEnabled);
            log.debug("Selected minipop: " + selectedMiniPop);
            // Port port = minipopFacade.getAvailablePort(selectedMiniPop);
            // log.debug("Resulting port: " + port);
            log.debug(" this is 0");
            String userName = FacesContext.getCurrentInstance().getExternalContext().getUserPrincipal().getName();
            log.debug(" this is 1");
            User user = userFacade.findByUserName(userName);
            log.debug(" this is 2 " + service.getProvider());
            OperationsEngine operationsEngine = engineFactory.getOperationsEngine(service.getProvider());
            log.debug("class name " + operationsEngine.getClass());
            operationsEngine.createSubscription(selectedSubscriber,
                    subscription,
                    serviceId,
                    selectedMiniPop,
                    isUseStockEquipment,
                    selectedEquipment,
                    notificationSettings,
                    installFee,
                    selectedCampaign,
                    (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false),
                    user);
            log.debug(" this is 3");
            systemLogger.success(SystemEvent.SUBSCRIPTION_CREATED, subscription,
                    "Subscription created"
            );
            log.debug(" this is 4");
            try {
                queueManager.addToNotificationsQueue(BillingEvent.SUBSCRIPTION_ADDED, subscription.getId(), null);
                systemLogger.success(SystemEvent.NOTIFICATION_SCHEDULED, subscription,
                        String.format("Event=" + BillingEvent.SUBSCRIPTION_ADDED)
                );
            } catch (Exception ex) {
                log.error("Cannot add to notification queue: ", ex);
                systemLogger.error(SystemEvent.NOTIFICATION_SCHEDULED, subscription, ex.getCause().getMessage());
            }
            String outcome = new StringBuilder(SUBSCRIPTION_PATH)
                    .append(String.format("view/view_%d.xhtml?subscriber=", subscription.getService().getProvider().getId())).append(selectedSubscriber.getId())
                    .append("&amp;subscription=").append(subscription.getId())
                    .append("&amp;faces-redirect=true&amp;includeViewParams=true").toString();

            log.info("Subscription created successfully. Subscription=" + subscription);
            Log.debug("outcome data: " + outcome);
            //inMemory.addSubscription(subscription);

            subscription = null;
            selectedSubscriber = null;
            balance = null;
            subscriberSelectList = null;
            filteredSubscriberList = null;
            subscriberLazyTableModel = null;

            equipmentPersistenceFacade.clearFilters();
            minipopFacade.clearFilters();
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Subscription created successfully", "Subscription created successfully"));
            installFee = 0;
            campaignList = null;
            return outcome;
        } catch (DuplicateAgreementException ex) {
            log.error("Cannot create subscriptipn: ", ex);
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(SEVERITY_ERROR, "Agreement already exists", "Agreement already exists"));
            return null;
        } catch (NoFreePortLeftException ex) {
            log.error("Cannot create subscription: ", ex);
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(SEVERITY_ERROR, "Minipop has no free ports left", "Minipop has no free ports left"));
            return null;
        } catch (PortAlreadyReservedException ex) {
            String msg = String.format("Port %d already reserved or illegal", selectedMiniPop.getPreferredPort());
            log.error("Cannot create subscription: ", ex);
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(SEVERITY_ERROR, msg, msg));
            return null;
        } catch (Exception ex) {
            log.error("Cannot create subscriptipn: ", ex);
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(SEVERITY_ERROR, "Cannot create subscriptipn", "Cannot create subscriptipn"));
            systemLogger.error(SystemEvent.SUBSCRIPTION_CREATED, subscription, ex.getCause().getMessage());
            return null;
        }
    }


    public String createCitynet() {
        if (selectedSubscriber == null) {
            log.debug("Cannot create subscription. Subscriber not selected");
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(SEVERITY_ERROR, "Subscriber must be selected", "There is no subscriber selected"));
            return null;
        }
        if (isUseStockEquipment && selectedEquipment == null) {
            log.debug("Cannot create subscription. Equipment not selected");
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(SEVERITY_ERROR, "Equipment must be selected", "There is no equipment selected"));
            return null;
        }

        if (serviceId == null) {
            log.debug("Cannot create subscription. Service not selected");
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(SEVERITY_ERROR, "Service must be selected", "There is no service selected"));
            return null;
        }

        if (service != null && service.getServiceType() == ServiceType.BROADBAND && service.getProvider().getId() == Providers.NARFIX.getId()) {
            if (selectedMiniPop == null) {
                selectedMiniPop = findTempMinipop(providerFacade.find(Providers.CITYNET.getId()));
                if (selectedMiniPop == null) {
                    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(SEVERITY_ERROR, "Cannot find default minipop", "Cannot find default minipop"));
                    return null;
                }
            }
        }

        if (service != null && service.getServiceType() == ServiceType.BROADBAND && service.getProvider().getId() == Providers.CITYNET.getId()) {
            if (selectedMiniPop == null) {
                selectedMiniPop = findTempMinipop(service.getProvider());
                if (selectedMiniPop == null) {
                    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(SEVERITY_ERROR, "Cannot find default minipop", "Cannot find default minipop"));
                    return null;
                }
            }
        }

        if (service != null && service.getServiceType() == ServiceType.BROADBAND && selectedMiniPop == null) {
            log.debug("Cannot create subscription. Minipop not selected");
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(SEVERITY_ERROR, "Minipop must be selected", "There is no minipop selected"));
            return null;
        }

        if (service != null && service.getSettingByType(ServiceSettingType.BROADBAND_SWITCH) != null
                && selectedMiniPop != null && selectedMiniPop.getPreferredPort() != null
                && !selectedMiniPop.checkPort(selectedMiniPop.getPreferredPort())) {
            log.debug("Cannot create subscription. Minipop's preferred port is invalid. " + selectedMiniPop);
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(SEVERITY_ERROR, "Minipop's port is invalid", "Minipop's port is invalid"));
            return null;
        }

        /*
        * if (service != null && service.getInstallFeeProfile() != null && selectedInstallFeeRate == null) {
            log.debug("Cannot create subscription. Installation fee is not selected.");
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(SEVERITY_ERROR, "Installation fee is not selected", "Installation fee is not selected"));
            return null;
        }
        * */

        //add default VASes
        if (getSubscriptionVAS() != null && getSubscriptionVAS().getSettings().size() > 0) {
            subscription.addVAS(subscriptionVAS);
        }
//        getSubscription().setAgreement(getDetails().getAts() + "00" + getDetails().getStreet() + "00" + getDetails().getBuilding() + "00" + getDetails().getApartment());

        if (!(getDetails().getAts() != null && getDetails().getApartment() != null && getDetails().getBuilding() != null && getDetails().getStreet() != null)) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(SEVERITY_ERROR, "Incorrect aggrement", "Incorrect aggrement"));
            return null;
        }

        if (subscriptionFacade.findByAgreementOrdinary(getSubscription().getAgreement()) != null) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(SEVERITY_ERROR, "Aggrement exist", "Aggrement exist"));
            return null;
        }

        if (service != null && service.getDefaultVasList() != null
                && (subscription.getVasList().size() < service.getDefaultVasList().size())) {
            log.debug("Not all charges for default VAS added: subscriptionVas: " + subscriptionVAS.getSettings() + ", defaultVasList: " + service.getDefaultVasList());
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(SEVERITY_ERROR, "Not all charges added", "Not all charges added"));
            return null;
        }

        //log.debug("selected subscriber: " + selectedSubscriber + ", subscription = " + subscription);
        try {
            /*if (defaultVasCharges != null && !defaultVasCharges.isEmpty()) {
               for (Map.Entry<Long, Long> entry : defaultVasCharges.entrySet())
                   subscription.addDefaultVasCharge(entry.getKey(), entry.getValue());
           }


          if (service.getDefaultVasList() != null && !service.getDefaultVasList().isEmpty()) {
               SubscriptionVAS subscriptionVAS = null;

               for (ValueAddedService vas : service.getDefaultVasList()) {
                   if (vas.getId() == CABLE_VAS_CODE) {
                       subscriptionVAS = new SubscriptionVAS();
                       subscriptionVAS.setVas(vas);
                       subscriptionVAS.setBilledUpToDate(DateTime.now());
                       subscriptionVAS.setExpirationDate(DateTime.now());

                       subscriptionVAS.addSetting("price", String.valueOf(cableCharge),"Fee per 1 meter of cable");
                       subscriptionVAS.addSetting("length",String.valueOf(cableLength),"Cable length in meters");

                       subscription.addVAS(subscriptionVAS);
                   }
               }
           }*/

            subscriptionFacade.findByAgreement(subscription.getAgreement());
            details.setName(name);
            details.setSurname(surname);
            subscription.setDetails(details);
            log.debug("Billing model: " + billingModel);
            subscription.setBillingModel(billingModel);
            subscription.getDetails().setLanguage(language);
            log.debug("Selected minipop: " + selectedMiniPop);
            // Port port = minipopFacade.getAvailablePort(selectedMiniPop);
            // log.debug("Resulting port: " + port);
            String userName = FacesContext.getCurrentInstance().getExternalContext().getUserPrincipal().getName();
            User user = userFacade.findByUserName(userName);

            OperationsEngine operationsEngine = engineFactory.getOperationsEngine(service.getProvider());
            operationsEngine.createSubscription(
                    selectedSubscriber,
                    subscription,
                    serviceId,
                    selectedMiniPop,
                    isUseStockEquipment,
                    selectedEquipment,
                    notificationSettings,
                    installFee,
                    selectedCampaign,
                    (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false),
                    user
            );
            systemLogger.success(SystemEvent.SUBSCRIPTION_CREATED, subscription,
                    "Subscription created"
            );
            try {
                queueManager.addToNotificationsQueue(BillingEvent.SUBSCRIPTION_ADDED, subscription.getId(), null);
                systemLogger.success(SystemEvent.NOTIFICATION_SCHEDULED, subscription,
                        String.format("Event=" + BillingEvent.SUBSCRIPTION_ADDED)
                );
            } catch (Exception ex) {
                log.error("Cannot add to notification queue: ", ex);
                systemLogger.error(SystemEvent.NOTIFICATION_SCHEDULED, subscription, ex.getCause().getMessage());
            }
            String outcome = new StringBuilder(SUBSCRIPTION_PATH)
                    .append(String.format("view/view_%d.xhtml?subscriber=", subscription.getService().getProvider().getId())).append(selectedSubscriber.getId())
                    .append("&amp;subscription=").append(subscription.getId())
                    .append("&amp;faces-redirect=true&amp;includeViewParams=true").toString();

            log.info("Subscription created successfully. Subscription=" + subscription);

            //inMemory.addSubscription(subscription);
            subscription = null;
            selectedSubscriber = null;
            balance = null;
            subscriberSelectList = null;
            filteredSubscriberList = null;
            subscriberLazyTableModel = null;

            equipmentPersistenceFacade.clearFilters();
            minipopFacade.clearFilters();
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Subscription created successfully", "Subscription created successfully"));
            installFee = 0D;
            campaignList = null;
            return outcome;
        } catch (DuplicateAgreementException ex) {
            log.error("Cannot create subscriptipn: ", ex);
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(SEVERITY_ERROR, "Agreement already exists", "Agreement already exists"));
            return null;
        } catch (NoFreePortLeftException ex) {
            log.error("Cannot create subscription: ", ex);
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(SEVERITY_ERROR, "Minipop has no free ports left", "Minipop has no free ports left"));
            return null;
        } catch (PortAlreadyReservedException ex) {
            String msg = String.format("Port %d already reserved or illegal", selectedMiniPop.getPreferredPort());
            log.error("Cannot create subscription: ", ex);
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(SEVERITY_ERROR, msg, msg));
            return null;
        } catch (Exception ex) {
            log.error("Cannot create subscriptipn: ", ex);
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(SEVERITY_ERROR, "Cannot create subscriptipn", "Cannot create subscriptipn"));
            systemLogger.error(SystemEvent.SUBSCRIPTION_CREATED, subscription, ex.getCause().getMessage());
            return null;
        }
    }

    public PaymentTypes[] getPaymentTypes() {
        return PaymentTypes.values();
    }

    public double getServicePrice() {
        Log.debug("getSelected().getService() " + getSelected().getService());
        Log.debug("getSelected().getSettingByType(ServiceSettingType.ZONE) " + getSelected().getSettingByType(ServiceSettingType.ZONE));
        if (getSelected().getService() != null &&
                getSelected().getSettingByType(ServiceSettingType.ZONE) != null &&
                getSelected().getSettingByType(ServiceSettingType.SERVICE_TYPE) != null) {
            ServiceProperty property = servicePropertyPersistenceFacade.find(
                    getSelected().getService(),
                    getSelected().getSettingByType(ServiceSettingType.ZONE));
            if (property != null) {
                return property.getServicePriceInDouble();
            }
        }
        return Double.NaN;
    }

    public String createGlobal() {
        if (serviceTypeId == null) {
            log.debug("Cannot create subscription. Service Type(PPPoE/Wireless/IPoE) must be selected");
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(SEVERITY_ERROR, "Service type must be selected", "There is no service type selected"));
            return null;
        }
        if (selectedSubscriber == null) {
            log.debug("Cannot create subscription. Subscriber not selected");
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(SEVERITY_ERROR, "Subscriber must be selected", "There is no subscriber selected"));
            return null;
        }
        if (isUseStockEquipment && selectedEquipment == null) {
            log.debug("Cannot create subscription. Equipment not selected");
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(SEVERITY_ERROR, "Equipment must be selected", "There is no equipment selected"));
            return null;
        }

        if (serviceId == null) {
            log.debug("Cannot create subscription. Service not selected");
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(SEVERITY_ERROR, "Service must be selected", "There is no service selected"));
            return null;
        }

        SubscriptionServiceType serviceType =
                serviceTypePersistenceFacade.find(Long.parseLong(serviceTypeId));

        if (serviceType.getProfile().getProfileType().equals(ProfileType.IPoE)) {
            log.debug("Cannot create subscription. Service Type Not Valid");
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(SEVERITY_ERROR, "Service type not valid", "Service type not valid"));
            return null;
        }

        //add default VASes
        if (getSubscriptionVAS() != null && getSubscriptionVAS().getSettings().size() > 0) {
            subscription.addVAS(subscriptionVAS);
        }

        if (subscriptionFacade.findByAgreementOrdinary(getSubscription().getAgreement()) != null) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(SEVERITY_ERROR, "Aggrement exist", "Aggrement exist"));
            return null;
        }

        if (service != null && service.getDefaultVasList() != null
                && (subscription.getVasList().size() < service.getDefaultVasList().size())) {
            log.debug("Not all charges for default VAS added: subscriptionVas: " + subscriptionVAS.getSettings() + ", defaultVasList: " + service.getDefaultVasList());
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(SEVERITY_ERROR, "Not all charges added", "Not all charges added"));
            return null;
        }

        try {
            subscriptionFacade.findByAgreement(subscription.getAgreement());
            details.setName(name);
            details.setSurname(surname);
            subscription.setDetails(details);
            subscription.setPaymentType(PaymentTypes.CASH);

            billingModel = modelFacade.find(BillingPrinciple.CONTINUOUS);
            log.debug("Billing model: " + billingModel);
            subscription.setBillingModel(billingModel);

            subscription.getDetails().setLanguage(language);
            Service selected_service = serviceFacade.find(Long.parseLong(serviceId));
            log.debug("selected_service 3133: " + selected_service.getServicePrice());


            // Port port = minipopFacade.getAvailablePort(selectedMiniPop);
            // log.debug("Resulting port: " + port);
            String userName = FacesContext.getCurrentInstance().getExternalContext().getUserPrincipal().getName();
            User user = userFacade.findByUserName(userName);

            OperationsEngine operationsEngine = engineFactory.getOperationsEngine(service.getProvider());
            operationsEngine.createSubscription(
                    selectedSubscriber,
                    subscription,
                    serviceId,
                    selectedMiniPop,
                    isUseStockEquipment,
                    selectedEquipment,
                    notificationSettings,
                    0D,
                    selectedCampaign,
                    (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false),
                    user,
                    serviceTypeId,
                    selectedReseller
            );
            systemLogger.success(SystemEvent.SUBSCRIPTION_CREATED, subscription,
                    "Subscription created"
            );
            try {
                queueManager.addToNotificationsQueue(BillingEvent.SUBSCRIPTION_ADDED, subscription.getId(), null);
                systemLogger.success(SystemEvent.NOTIFICATION_SCHEDULED, subscription,
                        String.format("Event=" + BillingEvent.SUBSCRIPTION_ADDED)
                );
            } catch (Exception ex) {
                log.error("Cannot add to notification queue: ", ex);
                systemLogger.error(SystemEvent.NOTIFICATION_SCHEDULED, subscription, ex.getCause().getMessage());
            }
            String outcome = new StringBuilder(SUBSCRIPTION_PATH)
                    .append(String.format("view/view_%d.xhtml?subscriber=", subscription.getService().getProvider().getId())).append(selectedSubscriber.getId())
                    .append("&amp;subscription=").append(subscription.getId())
                    .append("&amp;faces-redirect=true&amp;includeViewParams=true").toString();

            log.info("Subscription created successfully. Subscription=" + subscription);

            //inMemory.addSubscription(subscription);
            subscription = null;
            selectedSubscriber = null;
            balance = null;
            subscriberSelectList = null;
            filteredSubscriberList = null;
            subscriberLazyTableModel = null;

            equipmentPersistenceFacade.clearFilters();
            minipopFacade.clearFilters();
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Subscription created successfully", "Subscription created successfully"));
            installFee = 0;
            campaignList = null;
            return outcome;
        } catch (DuplicateAgreementException ex) {
            log.error("Cannot create subscriptipn: ", ex);
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(SEVERITY_ERROR, "Agreement already exists", "Agreement already exists"));
            return null;
        } catch (NoFreePortLeftException ex) {
            log.error("Cannot create subscription: ", ex);
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(SEVERITY_ERROR, "Minipop has no free ports left", "Minipop has no free ports left"));
            return null;
        } catch (PortAlreadyReservedException ex) {
            String msg = String.format("Port %d already reserved or illegal", selectedMiniPop.getPreferredPort());
            log.error("Cannot create subscription: ", ex);
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(SEVERITY_ERROR, msg, msg));
            return null;
        } catch (Exception ex) {
            log.error("Cannot create subscriptipn: ", ex);
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(SEVERITY_ERROR, "Cannot create subscriptipn", "Cannot create subscriptipn"));
            systemLogger.error(SystemEvent.SUBSCRIPTION_CREATED, subscription, ex.getCause().getMessage());
            return null;
        }
    }


    public String createCNC() {
        if (serviceTypeId == null) {
            log.debug("Cannot create subscription. Service Type(PPPoE/Wireless/IPoE) must be selected");
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(SEVERITY_ERROR, "Service type must be selected", "There is no service type selected"));
            return null;
        }
        if (selectedSubscriber == null) {
            log.debug("Cannot create subscription. Subscriber not selected");
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(SEVERITY_ERROR, "Subscriber must be selected", "There is no subscriber selected"));
            return null;
        }
        if (isUseStockEquipment && selectedEquipment == null) {
            log.debug("Cannot create subscription. Equipment not selected");
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(SEVERITY_ERROR, "Equipment must be selected", "There is no equipment selected"));
            return null;
        }

        if (serviceId == null) {
            log.debug("Cannot create subscription. Service not selected");
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(SEVERITY_ERROR, "Service must be selected", "There is no service selected"));
            return null;
        }

        SubscriptionServiceType serviceType =
                serviceTypePersistenceFacade.find(Long.parseLong(serviceTypeId));

        if (serviceType.getProfile().getProfileType().equals(ProfileType.IPoE)) {
            log.debug("Cannot create subscription. Service Type Not Valid");
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(SEVERITY_ERROR, "Service type not valid", "Service type not valid"));
            return null;
        }

        //add default VASes
        if (getSubscriptionVAS() != null && getSubscriptionVAS().getSettings().size() > 0) {
            subscription.addVAS(subscriptionVAS);
        }

        if (subscriptionFacade.findByAgreementOrdinary(getSubscription().getAgreement()) != null) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(SEVERITY_ERROR, "Aggrement exist", "Aggrement exist"));
            return null;
        }

        if (service != null && service.getDefaultVasList() != null
                && (subscription.getVasList().size() < service.getDefaultVasList().size())) {
            log.debug("Not all charges for default VAS added: subscriptionVas: " + subscriptionVAS.getSettings() + ", defaultVasList: " + service.getDefaultVasList());
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(SEVERITY_ERROR, "Not all charges added", "Not all charges added"));
            return null;
        }

        try {
            subscriptionFacade.findByAgreement(subscription.getAgreement());
            details.setName(name);
            details.setSurname(surname);
            subscription.setDetails(details);
            subscription.setPaymentType(PaymentTypes.CASH);

            billingModel = modelFacade.find(BillingPrinciple.CONTINUOUS);
            log.debug("Billing model: " + billingModel);
            subscription.setBillingModel(billingModel);

            subscription.getDetails().setLanguage(language);

            Service selected_service = serviceFacade.find(Long.parseLong(serviceId));
            log.debug("selected_service 3282: " + selected_service.getServicePrice());
//            subscription.setServiceFeeRateWoTax(selected_service.getServicePrice()/100000);

            log.debug("Selected minipop: " + selectedMiniPop);
            // Port port = minipopFacade.getAvailablePort(selectedMiniPop);
            // log.debug("Resulting port: " + port);
            String userName = FacesContext.getCurrentInstance().getExternalContext().getUserPrincipal().getName();
            User user = userFacade.findByUserName(userName);

            OperationsEngine operationsEngine = engineFactory.getOperationsEngine(service.getProvider());
            operationsEngine.createSubscription(
                    selectedSubscriber,
                    subscription,
                    serviceId,
                    selectedMiniPop,
                    isUseStockEquipment,
                    selectedEquipment,
                    notificationSettings,
                    0D,
                    selectedCampaign,
                    (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false),
                    user,
                    serviceTypeId,
                    selectedReseller
            );
            systemLogger.success(SystemEvent.SUBSCRIPTION_CREATED, subscription,
                    "Subscription created"
            );
            try {
                queueManager.addToNotificationsQueue(BillingEvent.SUBSCRIPTION_ADDED, subscription.getId(), null);
                systemLogger.success(SystemEvent.NOTIFICATION_SCHEDULED, subscription,
                        String.format("Event=" + BillingEvent.SUBSCRIPTION_ADDED)
                );
            } catch (Exception ex) {
                log.error("Cannot add to notification queue: ", ex);
                systemLogger.error(SystemEvent.NOTIFICATION_SCHEDULED, subscription, ex.getCause().getMessage());
            }
            String outcome = new StringBuilder(SUBSCRIPTION_PATH)
                    .append(String.format("view/view_%d.xhtml?subscriber=", subscription.getService().getProvider().getId())).append(selectedSubscriber.getId())
                    .append("&amp;subscription=").append(subscription.getId())
                    .append("&amp;faces-redirect=true&amp;includeViewParams=true").toString();

            log.info("Subscription created successfully. Subscription=" + subscription);

            //inMemory.addSubscription(subscription);
            subscription = null;
            selectedSubscriber = null;
            balance = null;
            subscriberSelectList = null;
            filteredSubscriberList = null;
            subscriberLazyTableModel = null;

            equipmentPersistenceFacade.clearFilters();
            minipopFacade.clearFilters();
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Subscription created successfully", "Subscription created successfully"));
            installFee = 0;
            campaignList = null;
            return outcome;
        } catch (DuplicateAgreementException ex) {
            log.error("Cannot create subscriptipn: ", ex);
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(SEVERITY_ERROR, "Agreement already exists", "Agreement already exists"));
            return null;
        } catch (NoFreePortLeftException ex) {
            log.error("Cannot create subscription: ", ex);
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(SEVERITY_ERROR, "Minipop has no free ports left", "Minipop has no free ports left"));
            return null;
        } catch (PortAlreadyReservedException ex) {
            String msg = String.format("Port %d already reserved or illegal", selectedMiniPop.getPreferredPort());
            log.error("Cannot create subscription: ", ex);
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(SEVERITY_ERROR, msg, msg));
            return null;
        } catch (Exception ex) {
            log.error("Cannot create subscriptipn: ", ex);
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(SEVERITY_ERROR, "Cannot create subscriptipn", "Cannot create subscriptipn"));
            systemLogger.error(SystemEvent.SUBSCRIPTION_CREATED, subscription, ex.getCause().getMessage());
            return null;
        }
    }


    public String createDataplus() {
        if (zoneId == null) {
            log.debug("Cannot create subscription. Zone must be selected");
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(SEVERITY_ERROR, "Zone must be selected", "There is no zone selected"));
            return null;
        }
        if (serviceTypeId == null) {
            log.debug("Cannot create subscription. Service Type(PPPoE/Wireless/IPoE) must be selected");
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(SEVERITY_ERROR, "Service type must be selected", "There is no service type selected"));
            return null;
        }
        if (selectedSubscriber == null) {
            log.debug("Cannot create subscription. Subscriber not selected");
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(SEVERITY_ERROR, "Subscriber must be selected", "There is no subscriber selected"));
            return null;
        }
        if (isUseStockEquipment && selectedEquipment == null) {
            log.debug("Cannot create subscription. Equipment not selected");
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(SEVERITY_ERROR, "Equipment must be selected", "There is no equipment selected"));
            return null;
        }

        if (serviceId == null) {
            log.debug("Cannot create subscription. Service not selected");
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(SEVERITY_ERROR, "Service must be selected", "There is no service selected"));
            return null;
        }

        if (servicePropertyPersistenceFacade.find(serviceId, zoneId) == null) {
            log.debug("Cannot create subscription. The combination of service and zone is not valid");
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(SEVERITY_ERROR, "The combination of service and zone is not valid", "The combination of service and zone is not valid"));
            return null;
        }

        SubscriptionServiceType serviceType =
                serviceTypePersistenceFacade.find(Long.parseLong(serviceTypeId));

        if (serviceType.getProfile().getProfileType().equals(ProfileType.IPoE)) { //IPoE
            if (minipopId == null || portId == null) {
                log.debug("Cannot create subscription. Minipop and port must be selected");
                FacesContext.getCurrentInstance().addMessage(
                        null,
                        new FacesMessage(
                                SEVERITY_ERROR,
                                "Cannot create subscription. Minipop and port must be selected",
                                "Cannot create subscription. Minipop and port must be selected"));
                return null;
            }

            selectedMiniPop = minipopFacade.find(minipopId);
            selectedMiniPop.setNextAvailablePortHintAsNumber(portId);

            if (service != null && service.getSettingByType(ServiceSettingType.BROADBAND_SWITCH) != null
                    && selectedMiniPop != null && selectedMiniPop.getPreferredPort() != null
                    && !selectedMiniPop.checkPort(selectedMiniPop.getPreferredPort())) {
                log.debug("Cannot create subscription. Minipop's preferred port is invalid. " + selectedMiniPop);
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(SEVERITY_ERROR, "Minipop's port is invalid", "Minipop's port is invalid"));
                return null;
            }
        } else { //PPPoE
            if (minipopId != null || portId != null || selectedMiniPop != null) {
                log.debug("Cannot create subscription. Minipop/port not needed for PPPoE");
                FacesContext.getCurrentInstance().addMessage(
                        null,
                        new FacesMessage(
                                SEVERITY_ERROR,
                                "Cannot create subscription. Minipop/port not needed for PPPoE",
                                "Cannot create subscription. Minipop/port not needed for PPPoE"));
                return null;
            }
        }

        //add default VASes
        if (getSubscriptionVAS() != null && getSubscriptionVAS().getSettings().size() > 0) {
            subscription.addVAS(subscriptionVAS);
        }

        if (subscriptionFacade.findByAgreementOrdinary(getSubscription().getAgreement()) != null) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(SEVERITY_ERROR, "Aggrement exist", "Aggrement exist"));
            return null;
        }

        if (service != null && service.getDefaultVasList() != null
                && (subscription.getVasList().size() < service.getDefaultVasList().size())) {
            log.debug("Not all charges for default VAS added: subscriptionVas: " + subscriptionVAS.getSettings() + ", defaultVasList: " + service.getDefaultVasList());
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(SEVERITY_ERROR, "Not all charges added", "Not all charges added"));
            return null;
        }

        try {
            subscriptionFacade.findByAgreement(subscription.getAgreement());
            details.setName(name);
            details.setSurname(surname);
            subscription.setDetails(details);
            subscription.setPaymentType(PaymentTypes.CASH);

            billingModel = modelFacade.find(BillingPrinciple.CONTINUOUS);
            log.debug("Billing model: " + billingModel);
            subscription.setBillingModel(billingModel);

            subscription.getDetails().setLanguage(language);

            log.debug("Selected minipop: " + selectedMiniPop);
            // Port port = minipopFacade.getAvailablePort(selectedMiniPop);
            // log.debug("Resulting port: " + port);
            String userName = FacesContext.getCurrentInstance().getExternalContext().getUserPrincipal().getName();
            User user = userFacade.findByUserName(userName);

            boolean modemFree = false;
            if (modemFree) {
                for (final Campaign campaign : campaignFacade.findAllActive(service, false)) {
                    if (campaign.isAvailableOnCreation()) {
                        selectedCampaign = campaign;
                        break;
                    }
                }
            } else {
                for (final Campaign campaign : campaignFacade.findAllActive(service, true)) {
                    if (!campaign.isActivateOnPayment()) {
                        selectedCampaign = campaign;
                        break;
                    }
                }
            }

            OperationsEngine operationsEngine = engineFactory.getOperationsEngine(service.getProvider());
            operationsEngine.createSubscription(
                    selectedSubscriber,
                    subscription,
                    serviceId,
                    selectedMiniPop,
                    isUseStockEquipment,
                    selectedEquipment,
                    notificationSettings,
                    0D,
                    selectedCampaign,
                    (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false),
                    user,
                    zoneId,
                    serviceTypeId,
                    selectedReseller
            );
            systemLogger.success(SystemEvent.SUBSCRIPTION_CREATED, subscription,
                    "Subscription created"
            );
            try {
                queueManager.addToNotificationsQueue(BillingEvent.SUBSCRIPTION_ADDED, subscription.getId(), null);
                systemLogger.success(SystemEvent.NOTIFICATION_SCHEDULED, subscription,
                        String.format("Event=" + BillingEvent.SUBSCRIPTION_ADDED)
                );
            } catch (Exception ex) {
                log.error("Cannot add to notification queue: ", ex);
                systemLogger.error(SystemEvent.NOTIFICATION_SCHEDULED, subscription, ex.getCause().getMessage());
            }
            String outcome = new StringBuilder(SUBSCRIPTION_PATH)
                    .append(String.format("view/view_%d.xhtml?subscriber=", subscription.getService().getProvider().getId())).append(selectedSubscriber.getId())
                    .append("&amp;subscription=").append(subscription.getId())
                    .append("&amp;faces-redirect=true&amp;includeViewParams=true").toString();

            log.info("Subscription created successfully. Subscription=" + subscription);

            //inMemory.addSubscription(subscription);
            subscription = null;
            selectedSubscriber = null;
            balance = null;
            subscriberSelectList = null;
            filteredSubscriberList = null;
            subscriberLazyTableModel = null;

            equipmentPersistenceFacade.clearFilters();
            minipopFacade.clearFilters();
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Subscription created successfully", "Subscription created successfully"));
            installFee = 0;
            campaignList = null;
            return outcome;
        } catch (DuplicateAgreementException ex) {
            log.error("Cannot create subscriptipn: ", ex);
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(SEVERITY_ERROR, "Agreement already exists", "Agreement already exists"));
            return null;
        } catch (NoFreePortLeftException ex) {
            log.error("Cannot create subscription: ", ex);
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(SEVERITY_ERROR, "Minipop has no free ports left", "Minipop has no free ports left"));
            return null;
        } catch (PortAlreadyReservedException ex) {
            String msg = String.format("Port %d already reserved or illegal", selectedMiniPop.getPreferredPort());
            log.error("Cannot create subscription: ", ex);
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(SEVERITY_ERROR, msg, msg));
            return null;
        } catch (Exception ex) {
            log.error("Cannot create subscriptipn: ", ex);
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(SEVERITY_ERROR, "Cannot create subscriptipn", "Cannot create subscriptipn"));
            systemLogger.error(SystemEvent.SUBSCRIPTION_CREATED, subscription, ex.getCause().getMessage());
            return null;
        }
    }

    public String createUniNet() {
        if (selectedSubscriber == null) {
            log.debug("Cannot create subscription. Subscriber not selected");
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(SEVERITY_ERROR, "Subscriber must be selected", "There is no subscriber selected"));
            return null;
        }
        if (isUseStockEquipment && selectedEquipment == null) {
            log.debug("Cannot create subscription. Equipment not selected");
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(SEVERITY_ERROR, "Equipment must be selected", "There is no equipment selected"));
            return null;
        }

        if (serviceId == null) {
            log.debug("Cannot create subscription. Service not selected");
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(SEVERITY_ERROR, "Service must be selected", "There is no service selected"));
            return null;
        }

        if (service != null && service.getSettingByType(ServiceSettingType.BROADBAND_SWITCH) != null
                && selectedMiniPop != null && selectedMiniPop.getPreferredPort() != null
                && !selectedMiniPop.checkPort(selectedMiniPop.getPreferredPort())) {
            log.debug("Cannot create subscription. Minipop's preferred port is invalid. " + selectedMiniPop);
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(SEVERITY_ERROR, "Minipop's port is invalid", "Minipop's port is invalid"));
            return null;
        }

        //add default VASes
        if (getSubscriptionVAS() != null && getSubscriptionVAS().getSettings().size() > 0) {
            subscription.addVAS(subscriptionVAS);
        }

        if (subscriptionFacade.findByAgreementOrdinary(getSubscription().getAgreement()) != null) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(SEVERITY_ERROR, "Aggrement exist", "Aggrement exist"));
            return null;
        }

        if (service != null && service.getDefaultVasList() != null
                && (subscription.getVasList().size() < service.getDefaultVasList().size())) {
            log.debug("Not all charges for default VAS added: subscriptionVas: " + subscriptionVAS.getSettings() + ", defaultVasList: " + service.getDefaultVasList());
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(SEVERITY_ERROR, "Not all charges added", "Not all charges added"));
            return null;
        }

        try {
            subscriptionFacade.findByAgreement(subscription.getAgreement());
            details.setName(name);
            details.setSurname(surname);
            subscription.setDetails(details);
            log.debug("Billing model: " + billingModel);
            subscription.setBillingModel(billingModel);
            subscription.getDetails().setLanguage(language);

            log.debug("Selected minipop: " + selectedMiniPop);
            // Port port = minipopFacade.getAvailablePort(selectedMiniPop);
            // log.debug("Resulting port: " + port);
            String userName = FacesContext.getCurrentInstance().getExternalContext().getUserPrincipal().getName();
            User user = userFacade.findByUserName(userName);

            OperationsEngine operationsEngine = engineFactory.getOperationsEngine(service.getProvider());
            operationsEngine.createSubscription(
                    selectedSubscriber,
                    subscription,
                    serviceId,
                    selectedMiniPop,
                    isUseStockEquipment,
                    selectedEquipment,
                    notificationSettings,
                    0D,
                    selectedCampaign,
                    (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false),
                    user,
                    selectedDealer
            );
            systemLogger.success(SystemEvent.SUBSCRIPTION_CREATED, subscription,
                    "Subscription created"
            );
            try {
                queueManager.addToNotificationsQueue(BillingEvent.SUBSCRIPTION_ADDED, subscription.getId(), null);
                systemLogger.success(SystemEvent.NOTIFICATION_SCHEDULED, subscription,
                        String.format("Event=" + BillingEvent.SUBSCRIPTION_ADDED)
                );
            } catch (Exception ex) {
                log.error("Cannot add to notification queue: ", ex);
                systemLogger.error(SystemEvent.NOTIFICATION_SCHEDULED, subscription, ex.getCause().getMessage());
            }
            String outcome = new StringBuilder(SUBSCRIPTION_PATH)
                    .append(String.format("view/view_%d.xhtml?subscriber=", subscription.getService().getProvider().getId())).append(selectedSubscriber.getId())
                    .append("&amp;subscription=").append(subscription.getId())
                    .append("&amp;faces-redirect=true&amp;includeViewParams=true").toString();

            log.info("Subscription created successfully. Subscription=" + subscription);

            //inMemory.addSubscription(subscription);
            subscription = null;
            selectedSubscriber = null;
            balance = null;
            subscriberSelectList = null;
            filteredSubscriberList = null;
            subscriberLazyTableModel = null;

            equipmentPersistenceFacade.clearFilters();
            minipopFacade.clearFilters();
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Subscription created successfully", "Subscription created successfully"));
            installFee = 0;
            campaignList = null;
            return outcome;
        } catch (DuplicateAgreementException ex) {
            log.error("Cannot create subscriptipn: ", ex);
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(SEVERITY_ERROR, "Agreement already exists", "Agreement already exists"));
            return null;
        } catch (NoFreePortLeftException ex) {
            log.error("Cannot create subscription: ", ex);
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(SEVERITY_ERROR, "Minipop has no free ports left", "Minipop has no free ports left"));
            return null;
        } catch (PortAlreadyReservedException ex) {
            String msg = String.format("Port %d already reserved or illegal", selectedMiniPop.getPreferredPort());
            log.error("Cannot create subscription: ", ex);
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(SEVERITY_ERROR, msg, msg));
            return null;
        } catch (Exception ex) {
            log.error("Cannot create subscriptipn: ", ex);
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(SEVERITY_ERROR, "Cannot create subscriptipn", "Cannot create subscriptipn"));
            systemLogger.error(SystemEvent.SUBSCRIPTION_CREATED, subscription, ex.getCause().getMessage());
            return null;
        }
    }


    public String createCitynetConvergent() {

        if (selectedSubscriber == null) {
            log.debug("Cannot create subscription. Subscriber not selected");
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(SEVERITY_ERROR, "Subscriber must be selected", "There is no subscriber selected"));
            return null;
        }
        if (isUseStockEquipment && selectedEquipment == null) {
            log.debug("Cannot create subscription. Equipment not selected");
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(SEVERITY_ERROR, "Equipment must be selected", "There is no equipment selected"));
            return null;
        }

        if (serviceId == null) {
            log.debug("Cannot create subscription. Service not selected");
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(SEVERITY_ERROR, "Service must be selected", "There is no service selected"));
            return null;
        }

        if (service != null && service.getServiceType() == ServiceType.BROADBAND && service.getProvider().getId() == Providers.QUTU.getId()) {
            if (selectedMiniPop == null) {
                selectedMiniPop = findTempMinipop(service.getProvider());
                if (selectedMiniPop == null) {
                    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(SEVERITY_ERROR, "Cannot find default minipop", "Cannot find default minipop"));
                    return null;
                }
            }
        }

        if (service != null && service.getServiceType() == ServiceType.BROADBAND && selectedMiniPop == null) {
            log.debug("Cannot create subscription. Minipop not selected");
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(SEVERITY_ERROR, "Minipop must be selected", "There is no minipop selected"));
            return null;
        }

        //add default VASes
        if (getSubscriptionVAS() != null && getSubscriptionVAS().getSettings().size() > 0) {
            subscription.addVAS(subscriptionVAS);
        }
//        getSubscription().setAgreement(getDetails().getAts() + "00" + getDetails().getStreet() + "00" + getDetails().getBuilding() + "00" + getDetails().getApartment());

        if (!(getDetails().getAts() != null && getDetails().getApartment() != null && getDetails().getBuilding() != null && getDetails().getStreet() != null)) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(SEVERITY_ERROR, "Incorrect aggrement", "Incorrect aggrement"));
            return null;
        }

        if (subscriptionFacade.findByAgreementOrdinary(getSubscription().getAgreement()) != null) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(SEVERITY_ERROR, "Aggrement exist", "Aggrement exist"));
            return null;
        }

        if (service != null && service.getDefaultVasList() != null
                && (subscription.getVasList().size() < service.getDefaultVasList().size())) {
            log.debug("Not all charges for default VAS added: subscriptionVas: " + subscriptionVAS.getSettings() + ", defaultVasList: " + service.getDefaultVasList());
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(SEVERITY_ERROR, "Not all charges added", "Not all charges added"));
            return null;
        }

        //log.debug("selected subscriber: " + selectedSubscriber + ", subscription = " + subscription);
        try {

            subscriptionFacade.findByAgreement(subscription.getAgreement());
            details.setDesc(details.getAts() + "00" + details.getStreet() + "00" + details.getBuilding() + "00" + details.getApartment());
            details.setName(name);
            details.setSurname(surname);
            subscription.setDetails(details);
            subscription.setBillingModel(modelFacade.find(BillingPrinciple.GRACE));
            subscription.getDetails().setLanguage(language);
            log.debug("Selected minipop: " + selectedMiniPop);
            // Port port = minipopFacade.getAvailablePort(selectedMiniPop);
            // log.debug("Resulting port: " + port);
            String userName = FacesContext.getCurrentInstance().getExternalContext().getUserPrincipal().getName();
            User user = userFacade.findByUserName(userName);

            OperationsEngine operationsEngine = engineFactory.getOperationsEngine(service.getProvider());
            operationsEngine.createSubscription(
                    selectedSubscriber,
                    subscription,
                    serviceId,
                    selectedMiniPop,
                    isUseStockEquipment,
                    selectedEquipment,
                    notificationSettings,
                    installFee,
                    selectedCampaign,
                    (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false),
                    user
            );
            systemLogger.success(SystemEvent.SUBSCRIPTION_CREATED, subscription,
                    "Subscription created"
            );
            try {
                queueManager.addToNotificationsQueue(BillingEvent.SUBSCRIPTION_ADDED, subscription.getId(), null);
                systemLogger.success(SystemEvent.NOTIFICATION_SCHEDULED, subscription,
                        String.format("Event=" + BillingEvent.SUBSCRIPTION_ADDED)
                );
            } catch (Exception ex) {
                log.error("Cannot add to notification queue: ", ex);
                systemLogger.error(SystemEvent.NOTIFICATION_SCHEDULED, subscription, ex.getCause().getMessage());
            }
            String outcome = new StringBuilder(SUBSCRIPTION_PATH)
                    .append(String.format("view/view_%d.xhtml?subscriber=", subscription.getService().getProvider().getId())).append(selectedSubscriber.getId())
                    .append("&amp;subscription=").append(subscription.getId())
                    .append("&amp;faces-redirect=true&amp;includeViewParams=true").toString();

            log.info("Subscription created successfully. Subscription=" + subscription);

            //inMemory.addSubscription(subscription);
            subscription = null;
            selectedSubscriber = null;
            balance = null;
            subscriberSelectList = null;
            filteredSubscriberList = null;
            subscriberLazyTableModel = null;

            equipmentPersistenceFacade.clearFilters();
            minipopFacade.clearFilters();
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Subscription created successfully", "Subscription created successfully"));
            installFee = 0;
            campaignList = null;
            return outcome;
        } catch (DuplicateAgreementException ex) {
            log.error("Cannot create subscriptipn: ", ex);
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(SEVERITY_ERROR, "Agreement already exists", "Agreement already exists"));
            return null;
        } catch (NoFreePortLeftException ex) {
            log.error("Cannot create subscription: ", ex);
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(SEVERITY_ERROR, "Minipop has no free ports left", "Minipop has no free ports left"));
            return null;
        } catch (PortAlreadyReservedException ex) {
            String msg = String.format("Port %d already reserved or illegal", selectedMiniPop.getPreferredPort());
            log.error("Cannot create subscription: ", ex);
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(SEVERITY_ERROR, msg, msg));
            return null;
        } catch (Exception ex) {
            log.error("Cannot create subscriptipn: ", ex);
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(SEVERITY_ERROR, "Cannot create subscriptipn", "Cannot create subscriptipn"));
            systemLogger.error(SystemEvent.SUBSCRIPTION_CREATED, subscription, ex.getCause().getMessage());
            return null;
        }
    }


    public String show() {
        //log.debug("selected: " + selected);
        if (selected == null) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(SEVERITY_ERROR, "Subscription must be selected", "No subscription selected"));
            return null;
        }

        return new StringBuilder(SUBSCRIPTION_PATH).append("view?subscription=").append(selected.getId())
                .append("&amp;includeViewParams&amp;faces-redirect=true").toString();
    }


    public String getNameSurname() {

        return selected.getDetails().getName() + " " + selected.getDetails().getSurname();
    }

    public void setNameSurname(String name) {
        String regx = "^[a-zA-Z_ ]*$";

        if (name.matches(regx)) {
            String[] nameandsurname = name.split(" ");

            selected.getDetails().setName(nameandsurname[0]);
            selected.getDetails().setSurname(nameandsurname[1]);

        }
        ;


    }


    public String returnToList() {
        selected = null;
        subscriptionLazyTableModel = null;
        return "/admin.xhtml?faces-redirect=true";
    }

    public String save() {
        if (selected == null) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Please select subscription", "Please select subscription"));
            return null;
        }

        Optional<String> userName = Optional.ofNullable(FacesContext.getCurrentInstance().getExternalContext().getUserPrincipal().getName());
        log.debug("Save clicking for subscription "+selected.getId()+" by user "+userName.orElse(null));
        try {
            log.debug(String.format("save: selectedMinipop=%s", selectedMiniPop));
            selected = this.subscriptionFacade.update(selected, notificationSettings, selectedMiniPop, selectedEquipment);
            //inMemory.updateSubscription(selected);
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Subscription updated successfully", "Subscription updated successfully"));
        } catch (Exception ex) {
            log.error("Cannot update subscription=" + selected, ex);
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Cannot update subscription", "Cannot update subscription"));
        }
        selectedMiniPop = null;
        minipopLazyDataModel = null;
        campaignList = null;
        return null;
    }

    public void addSettings(Subscription subscription, Map<ServiceSettingType, String> settings) {
        if (subscription.getService().getServiceType() == ServiceType.BROADBAND) {

            List<SubscriptionSetting> settingList = subscription.getSettings();
            SubscriptionSetting subscriptionSetting = new SubscriptionSetting();

            for (ServiceSettingType st : settings.keySet()) {
                subscriptionSetting.setValue(settings.get(st));//INFO
                ServiceSetting serviceSetting = commonEngine.createServiceSetting(
                        st.toString(),
                        st,
                        Providers.findById(getSelected().getService().getProvider().getId()),
                        ServiceType.BROADBAND,
                        "");
                subscriptionSetting.setProperties(serviceSetting);
                settingFacade.save(subscriptionSetting);
                subscriptionSetting = settingFacade.update(subscriptionSetting);
                subscription.setSettings(settingList);
                settingList.add(subscriptionSetting);
                subscriptionSetting = new SubscriptionSetting();
            }
        }
    }

    public boolean checkSetting(ServiceSettingType st) {
        for (SubscriptionSetting ss : selected.getSettings()) {
            if (ss.getProperties().getType() == st) {
                return true;
            }
        }
        return false;
    }


    public String saveCNCSettings() {
        log.debug("Old save");

        Map<ServiceSettingType, String> newSettings = new HashMap<>();
        if (selected == null) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Please select subscription", "Please select subscription"));
            return null;
        }

//        SubscriptionServiceType serviceType =
//                serviceTypePersistenceFacade.find(Long.parseLong(serviceTypeId));

//        if (serviceType.getProfile().getProfileType().equals(ProfileType.IPoE)) { //IPoE
//            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "CNC subscription cannot be IPoE", "CNC subscription cannot be IPoE"));
//            return null;
//        }

        if (serviceId != null && getSelected().getService().getId() != Long.parseLong(serviceId)) {
            selected = engineFactory.getOperationsEngine(selected)
                    .changeService(selected, serviceFacade.find(Long.parseLong(serviceId)), false, false);
        }

//        if (!checkSetting(ServiceSettingType.SERVICE_TYPE) && serviceTypeId != null && !serviceTypeId.isEmpty()) {
//            newSettings.put(ServiceSettingType.SERVICE_TYPE, serviceTypeId);
//        }
        if (!checkSetting(ServiceSettingType.DEALER) && selectedReseller != null && !selectedReseller.isEmpty()) {
            newSettings.put(ServiceSettingType.DEALER, selectedReseller);
        }

        if (!checkSetting(ServiceSettingType.USERNAME) && !username.isEmpty()) {
            log.debug("changing username");
            newSettings.put(ServiceSettingType.USERNAME, String.valueOf(username));
        }

        if (!checkSetting(ServiceSettingType.PASSWORD) && !password.isEmpty()) {
            log.debug("changing password");
            newSettings.put(ServiceSettingType.PASSWORD, String.valueOf(password));
        }

        addSettings(selected, newSettings);
        if (newSettings.size() > 0)
            selected = this.subscriptionFacade.update(selected);

        try {
            for (SubscriptionSetting ss : selected.getSettings()) {
                switch (ss.getProperties().getType()) {
                    case SERVICE_TYPE:
                        if (serviceTypeId != null && !serviceTypeId.isEmpty()) {
                            ss.setValue(serviceTypeId);
                        }
                        break;
                    case DEALER:
                        if (selectedReseller != null && !selectedReseller.isEmpty()) {
                            ss.setValue(selectedReseller);
                        }
                        break;
                    case PASSWORD:
                        if (password != null) {
                            ss.setValue(String.valueOf(password));
                        }
                        break;
                    case USERNAME:
                        if (username != null) {
                            ss.setValue(String.valueOf(username));
                        }
                        break;
                }
            }
            selected = this.subscriptionFacade.update(selected);
            ProvisioningEngine provisioner = engineFactory.getProvisioningEngine(selected);
            provisioner.reprovision(selected);
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Subscription settings updated successfully", "Subscription updated successfully"));
            ats = null;
            details = null;
        } catch (Exception ex) {
            log.error("Cannot update subscription settigns = " + selected, ex);
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Cannot update subscription", "Cannot update subscription"));
        }
        return null;
    }


    public String saveGlobalSettings() {
        log.debug("Old save");
        log.debug("serviceTypeId dor global " + serviceTypeId);
        Map<ServiceSettingType, String> newSettings = new HashMap<>();
        if (selected == null) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Please select subscription", "Please select subscription"));
            return null;
        }

//        SubscriptionServiceType serviceType =
//                serviceTypePersistenceFacade.find(Long.parseLong(serviceTypeId));

//        if (serviceType.getProfile().getProfileType().equals(ProfileType.IPoE)) { //IPoE
//            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Global subscription cannot be IPoE", "Global subscription cannot be IPoE"));
//            return null;
//        }

        if (serviceId != null && getSelected().getService().getId() != Long.parseLong(serviceId)) {
            selected = engineFactory.getOperationsEngine(selected)
                    .changeService(selected, serviceFacade.find(Long.parseLong(serviceId)), false, false);
        }

//        if (!checkSetting(ServiceSettingType.SERVICE_TYPE) && serviceTypeId != null && !serviceTypeId.isEmpty()) {
//            newSettings.put(ServiceSettingType.SERVICE_TYPE, serviceTypeId);
//        }
        if (!checkSetting(ServiceSettingType.DEALER) && selectedReseller != null && !selectedReseller.isEmpty()) {
            newSettings.put(ServiceSettingType.DEALER, selectedReseller);
        }

        if (!checkSetting(ServiceSettingType.USERNAME) && !username.isEmpty()) {
            log.debug("changing username");
            newSettings.put(ServiceSettingType.USERNAME, String.valueOf(username));
        }

        if (!checkSetting(ServiceSettingType.PASSWORD) && !password.isEmpty()) {
            log.debug("changing password");
            newSettings.put(ServiceSettingType.PASSWORD, String.valueOf(password));
        }

        addSettings(selected, newSettings);
        if (newSettings.size() > 0)
            selected = this.subscriptionFacade.update(selected);

        try {
            for (SubscriptionSetting ss : selected.getSettings()) {
                switch (ss.getProperties().getType()) {
                    case SERVICE_TYPE:
                        if (serviceTypeId != null && !serviceTypeId.isEmpty()) {
                            ss.setValue(serviceTypeId);
                        }
                        break;
                    case DEALER:
                        if (selectedReseller != null && !selectedReseller.isEmpty()) {
                            ss.setValue(selectedReseller);
                        }
                        break;
                    case PASSWORD:
                        if (password != null) {
                            ss.setValue(String.valueOf(password));
                        }
                        break;
                    case USERNAME:
                        if (username != null) {
                            ss.setValue(String.valueOf(username));
                        }
                        break;
                }
            }
            selected = this.subscriptionFacade.update(selected);
            ProvisioningEngine provisioner = engineFactory.getProvisioningEngine(selected);
            provisioner.reprovision(selected);
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Subscription settings updated successfully", "Subscription updated successfully"));
            ats = null;
            details = null;
        } catch (Exception ex) {
            log.error("Cannot update subscription settigns = " + selected, ex);
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Cannot update subscription", "Cannot update subscription"));
        }
        return null;
    }

    public String saveDataplusSettings() {
        log.debug("Old save");

        log.debug("1st minipopId " + minipopId);
        log.debug("1st portId " + portId);
        log.debug("1st selectedMiniPop " + selectedMiniPop);

        Map<ServiceSettingType, String> newSettings = new HashMap<>();
        if (selected == null) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Please select subscription", "Please select subscription"));
            return null;
        }

        if (servicePropertyPersistenceFacade.find(
                serviceId,
                zoneId) == null) {
            log.debug("Cannot update settings. The combination of service, zone, and type is not valid");
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(SEVERITY_ERROR, "Cannot update settings. The combination of service, zone, and type is not valid", "Cannot update settings. The combination of service, zone, and type is not valid"));
            return null;
        }

        SubscriptionServiceType serviceType =
                serviceTypePersistenceFacade.find(Long.parseLong(serviceTypeId));

        MiniPop minipop = null;
        if (serviceType.getProfile().getProfileType().equals(ProfileType.IPoE)) { //IPoE
            log.debug("IPoE");
            if (minipopId == null) {
                log.debug("Cannot update settings. Minipop must be selected");
                FacesContext.getCurrentInstance().addMessage(
                        null,
                        new FacesMessage(
                                SEVERITY_ERROR,
                                "Cannot update settings. Minipop must be selected",
                                "Cannot update settings. Minipop must be selected"));
                return null;
            }

            if (portId != null) {
                minipop = minipopFacade.find(minipopId);
                minipop.setNextAvailablePortHintAsNumber(portId);
            }

            if (getSelected().getService() != null && getSelected().getService().getSettingByType(ServiceSettingType.BROADBAND_SWITCH) != null
                    && minipop != null && minipop.getPreferredPort() != null
                    && !minipop.checkPort(minipop.getPreferredPort())) {
                log.debug("Cannot update settings. Minipop's preferred port is invalid. " + minipop);
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(SEVERITY_ERROR, "Minipop's port is invalid", "Minipop's port is invalid"));
                return null;
            }
        } else { //PPPoE
            log.debug("PPPoE");
            minipopId = null;
            portId = null;
            selectedMiniPop = null;


            if (minipopId != null || portId != null || selectedMiniPop != null) {
                log.debug("minipopId " + minipopId);
                log.debug("portId " + portId);
                log.debug("selectedMiniPop " + selectedMiniPop);
                log.debug("Cannot update settings. Minipop/port not needed for PPPoE");
                FacesContext.getCurrentInstance().addMessage(
                        null,
                        new FacesMessage(
                                SEVERITY_ERROR,
                                "Cannot update settings. Minipop/port not needed for PPPoE",
                                "Cannot update settings. Minipop /port not needed for PPPoE"));
                return null;
            }
        }


        if (minipop != null) {
            selected = subscriptionFacade.changeMinipop(getSelected(), minipop);
        }

        if (serviceId != null && getSelected().getService().getId() != Long.parseLong(serviceId)) {
            selected = engineFactory.getOperationsEngine(selected)
                    .changeService(selected, serviceFacade.find(Long.parseLong(serviceId)), false, false);
        }

        if (!checkSetting(ServiceSettingType.ZONE) && zoneId != null && !zoneId.isEmpty()) {
            newSettings.put(ServiceSettingType.ZONE, zoneId);
        }
        if (!checkSetting(ServiceSettingType.SERVICE_TYPE) && serviceTypeId != null && !serviceTypeId.isEmpty()) {
            newSettings.put(ServiceSettingType.SERVICE_TYPE, serviceTypeId);
        }
        if (!checkSetting(ServiceSettingType.DEALER) && selectedReseller != null && !selectedReseller.isEmpty()) {
            newSettings.put(ServiceSettingType.DEALER, selectedReseller);
        }
        if (!checkSetting(ServiceSettingType.BROADBAND_SWITCH) && minipopId != null) {
            newSettings.put(ServiceSettingType.BROADBAND_SWITCH, String.valueOf(minipopId));
        }
        if (!checkSetting(ServiceSettingType.BROADBAND_SWITCH_PORT) && portId != null) {
            newSettings.put(ServiceSettingType.BROADBAND_SWITCH_PORT, String.valueOf(portId));
        }
        if (!checkSetting(ServiceSettingType.BROADBAND_SWITCH_SLOT) && slotId != null) {
            newSettings.put(ServiceSettingType.BROADBAND_SWITCH_SLOT, String.valueOf(slotId));
        }
        log.debug("username and password");
        log.debug("username.isEmpty()" + username.isEmpty());
        log.debug("checkSetting(ServiceSettingType.USERNAME) " + checkSetting(ServiceSettingType.USERNAME));


        if (!checkSetting(ServiceSettingType.USERNAME) && !username.isEmpty()) {
            log.debug("changing username");
            newSettings.put(ServiceSettingType.USERNAME, String.valueOf(username));
        }


        if (!checkSetting(ServiceSettingType.PASSWORD) && !password.isEmpty()) {
            log.debug("changing password");
            newSettings.put(ServiceSettingType.PASSWORD, String.valueOf(password));
        }

        addSettings(selected, newSettings);
        if (newSettings.size() > 0)
            selected = this.subscriptionFacade.update(selected);

        try {
            for (SubscriptionSetting ss : selected.getSettings()) {
                switch (ss.getProperties().getType()) {
                    case ZONE:
                        if (zoneId != null && !zoneId.isEmpty()) {
                            ss.setValue(zoneId);
                        }
                        break;
                    case SERVICE_TYPE:
                        if (serviceTypeId != null && !serviceTypeId.isEmpty()) {
                            ss.setValue(serviceTypeId);
                        }
                        break;
                    case DEALER:
                        if (selectedReseller != null && !selectedReseller.isEmpty()) {
                            ss.setValue(selectedReseller);
                        }
                        break;
                    case BROADBAND_SWITCH:
                        if (minipopId != null) {
                            ss.setValue(String.valueOf(minipopId));
                        }
                        break;
                    case BROADBAND_SWITCH_PORT:
                        if (portId != null) {
                            ss.setValue(String.valueOf(portId));
                        }
                        break;
                    case BROADBAND_SWITCH_SLOT:
                        if (slotId != null) {
                            ss.setValue(String.valueOf(slotId));
                        }
                        break;
                    case PASSWORD:
                        if (password != null) {
                            ss.setValue(String.valueOf(password));
                        }
                        break;
                    case USERNAME:
                        if (username != null) {
                            ss.setValue(String.valueOf(username));
                        }
                        break;
                }
            }
            selected = this.subscriptionFacade.update(selected);
            ProvisioningEngine provisioner = engineFactory.getProvisioningEngine(selected);
            provisioner.reprovision(selected);
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Subscription settings updated successfully", "Subscription updated successfully"));
            portSelectList = null;
            ats = null;
            details = null;
        } catch (Exception ex) {
            log.error("Cannot update subscription settigns = " + selected, ex);
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Cannot update subscription", "Cannot update subscription"));
        }
        return null;
    }

    public String saveUninetSettings() {
        Map<ServiceSettingType, String> newSettings = new HashMap<>();
        if (selected == null) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Please select subscription", "Please select subscription"));
            return null;
        }

        if (!checkSetting(ServiceSettingType.INFO) && uninetInfo != null && !uninetInfo.isEmpty() && !uninetInfo.equalsIgnoreCase("null")) {
            newSettings.put(ServiceSettingType.INFO, uninetInfo);
        }
        if (!checkSetting(ServiceSettingType.PASSWORD) && uninetPassword != null && !uninetPassword.isEmpty() && !uninetPassword.equalsIgnoreCase("null")) {
            newSettings.put(ServiceSettingType.PASSWORD, uninetPassword);
        }
        if (!checkSetting(ServiceSettingType.MAC_ADDRESS) && uninetMAC != null && !uninetMAC.isEmpty() && !uninetMAC.equalsIgnoreCase("null")) {
            newSettings.put(ServiceSettingType.MAC_ADDRESS, uninetMAC);
        }
        if (!checkSetting(ServiceSettingType.ATS_PORT) && uninetAtsPort != null && !uninetAtsPort.isEmpty() && !uninetAtsPort.equalsIgnoreCase("null")) {
            newSettings.put(ServiceSettingType.ATS_PORT, uninetAtsPort);
        }
        if (!checkSetting(ServiceSettingType.BROADBAND_SWITCH_PORT) && uninetPort != null && !uninetPort.isEmpty() && !uninetPort.equalsIgnoreCase("null")) {
            newSettings.put(ServiceSettingType.BROADBAND_SWITCH_PORT, uninetPort);
        }
        if (!checkSetting(ServiceSettingType.BROADBAND_SWITCH_SLOT) && uninetSlot != null && !uninetSlot.isEmpty() && !uninetSlot.equalsIgnoreCase("null")) {
            newSettings.put(ServiceSettingType.BROADBAND_SWITCH_SLOT, uninetSlot);
        }

        addSettings(selected, newSettings);
        if (newSettings.size() > 0)
            selected = this.subscriptionFacade.update(selected);

        try {
            boolean isDealerExist = false;
            for (SubscriptionSetting ss : selected.getSettings()) {
                switch (ss.getProperties().getType()) {
                    case DEALER:
                        isDealerExist = true;
                        if (selectedDealerId != Long.parseLong(ss.getValue()))
                            ss.setValue(String.valueOf(selectedDealerId));
                        break;
                    case MAC_ADDRESS:
                        if (uninetMAC != null && !uninetMAC.isEmpty())
                            ss.setValue(uninetMAC);
                        break;
                    case BROADBAND_SWITCH_SLOT:
                        if (uninetSlot != null && !uninetSlot.isEmpty())
                            ss.setValue(uninetSlot);
                        break;
                    case BROADBAND_SWITCH_PORT:
                        if (uninetPort != null && !uninetPort.isEmpty())
                            ss.setValue(uninetPort);
                        break;
                    case INFO:
                        if (uninetInfo != null && !uninetInfo.isEmpty()) {
                            ss.setValue(uninetInfo);
                        }
                        break;
                    case PASSWORD:
                        if (uninetPassword != null && !uninetPassword.isEmpty()) {
                            ss.setValue(uninetPassword);
                        }
                        break;
                    case ATS_PORT:
                        if (uninetAtsPort != null && !uninetAtsPort.isEmpty())
                            ss.setValue(uninetAtsPort);
                        break;
                }
            }

            if(isDealerExist == false && selectedDealerId != null){
                List<SubscriptionSetting> listSettings = selected.getSettings();
                SubscriptionSetting dealerSetting = new SubscriptionSetting();
                dealerSetting.setValue(selectedDealerId.toString());
                ServiceSetting serviceSetting = commonEngine.createServiceSetting("DEALER", ServiceSettingType.DEALER, Providers.UNINET, ServiceType.BROADBAND, "");
                dealerSetting.setProperties(serviceSetting);
                dealerSetting = settingFacade.update(dealerSetting);
                listSettings.add(dealerSetting);
                selected.setSettings(listSettings);

            }



            selected = this.subscriptionFacade.update(selected);
            ProvisioningEngine provisioner = engineFactory.getProvisioningEngine(selected);
            provisioner.reprovision(selected);
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Subscription settings updated successfully", "Subscription updated successfully"));
            uninetPort = null;
            uninetSlot = null;
            uninetMAC = null;
            uninetPassword = null;
        } catch (Exception ex) {
            log.error("Cannot update subscription settigns = " + selected, ex);
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Cannot update subscription", "Cannot update subscription"));
        }
        selectedMiniPop = null;
        minipopLazyDataModel = null;
        campaignList = null;
        return null;
    }

    public boolean getIsAdsl() {
        return (selected != null && selected.getService().getServiceType() == ServiceType.BROADBAND);
    }

    public void setIsAdsl(boolean isAdsl) {

    }

    public SubscriptionDetails getDetails() {
        if (details == null) {
            if (getSelected() != null) {
                details = getSelected().getDetails();
            } else {
                details = isUseSubscriberAddress && getSelectedSubscriber() != null ? new SubscriptionDetails(selectedSubscriber) : new SubscriptionDetails();
            }
        }

        return details;
    }

    public void setDetails(SubscriptionDetails details) {
        this.details = details;
    }

    public Boolean getUseSubscriberAddress() {
        return isUseSubscriberAddress;
    }

    public void setUseSubscriberAddress(Boolean isUseSubscriberAddress) {
        this.isUseSubscriberAddress = isUseSubscriberAddress;
    }

    public void onSubscriberSelect(SelectEvent event) {
        log.debug("Row select fired! isUseSubscriberAddress=" + isUseSubscriberAddress);
        if (isUseSubscriberAddress) {
            this.details = new SubscriptionDetails(selectedSubscriber);
        }
    }

    public UIPanel getSbnDetailsPanel() {
        return sbnDetailsPanel;
    }

    public void setSbnDetailsPanel(UIPanel sbnDetailsPanel) {
        this.sbnDetailsPanel = sbnDetailsPanel;
    }

    public void onClick(AjaxBehaviorEvent event) {
        log.debug("Value changed: " + isUseSubscriberAddress + ", subscriber=" + selectedSubscriber);
        if (isUseSubscriberAddress && selectedSubscriber != null) {
            details = new SubscriptionDetails(selectedSubscriber);
        } else {
            details = new SubscriptionDetails();
        }
        log.debug("details now: " + details);
    }

    public int getSelectFromStockOrManually() {
        return selectFromStockOrManually;
    }

    public void setSelectFromStockOrManually(int selectFromStockOrManually) {
        this.selectFromStockOrManually = selectFromStockOrManually;
    }

    public Long getSubscriberID() {
        return subscriberID;
    }

    public void setSubscriberID(Long subscriberID) {
        this.subscriberID = subscriberID;
    }

    public LazyDataModel<Equipment> getEquipmentList() {
        if (equipmentList == null) {
            equipmentPersistenceFacade.addFilter(EquipmentPersistenceFacade.Filter.STATUS, EquipmentStatus.AVAILABLE);
            if (selected != null) {
                equipmentPersistenceFacade.addFilter(EquipmentPersistenceFacade.Filter.PROVIDER, selected.getService().getProvider().getId());
                equipmentPersistenceFacade.addFilter(EquipmentPersistenceFacade.Filter.STATUS, EquipmentStatus.AVAILABLE);
            }
            equipmentList = new LazyTableModel<>(equipmentPersistenceFacade);
        }
        return equipmentList;
    }

    public void setEquipmentList(LazyDataModel<Equipment> equipmentList) {
        this.equipmentList = equipmentList;
    }

    public Equipment getSelectedEquipment() {
        return selectedEquipment;
    }

    public void setSelectedEquipment(Equipment selectedEquipment) {
        this.selectedEquipment = selectedEquipment;
    }

    public List<Equipment> getFilteredEquipmentList() {
        return filteredEquipmentList;
    }

    public void setFilteredEquipmentList(List<Equipment> filteredEquipmentList) {
        this.filteredEquipmentList = filteredEquipmentList;
    }

    public void onServiceSelect(AjaxBehaviorEvent event) {

        if (serviceId == null) {
            return;
        }

        service = serviceFacade.find(Long.valueOf(serviceId));

        if (service == null) {
            return;
        }

        Service serviceForAgr = serviceFacade.find(Long.valueOf(serviceId));
        log.debug("serviceForAgr : " + serviceForAgr.toString());

        log.debug("serviceForAgr.getProvider() : " + serviceForAgr.getProvider());

        if (service.getProvider().getId() == Providers.QUTUNARHOME.getId()){
            ServiceProvider serviceProvider = providerFacade.find(454100);

            String identifier = agreementGenerator.generate(serviceProvider).toString();
            subscription.setIdentifier(identifier);
            log.debug("Generated identifier "+identifier);

        } else if (service.getProvider().getId() != Providers.CITYNET.getId()
                && service.getProvider().getId() != Providers.QUTU.getId()
                && service.getProvider().getId() != Providers.BBTV_BAKU.getId()
                && service.getProvider().getId() != Providers.AZERTELECOMPOST.getId()) {
            Long agreement = agreementGenerator.generate(serviceForAgr.getProvider());
            log.debug("Generated agreement: " + agreement);

            if (agreement != null) {
                getSubscription().setAgreement(
                        engineFactory.getOperationsEngine(service.getProvider()).
                                generateAgreement(new AgreementGenerationParams.Builder().setAgreement(agreement.toString()).build()));
                log.debug("New agreement field: " + getSubscription().getAgreement());
            }
        }
        equipmentPersistenceFacade.addFilter(EquipmentPersistenceFacade.Filter.PROVIDER, service.getProvider().getId());

        if (service.isAllowEquipment()) {
            isUseStockEquipment = true;
        }

        if (service.getSettingByType(ServiceSettingType.BROADBAND_SWITCH) != null) {

        }

        //this.getDetails().setAvailableEcare(service.isAvailableOnEcare());
        this.getDetails().setAvailableEcare(true);

        //ONE TIME VARIABLE CHARGE LOGIC
        ValueAddedService variableChargeVAS = service.getDefaultVasByType(ValueAddedServiceType.ONETIME_VARIABLE_CHARGE);

        if (variableChargeVAS != null) {
            getSubscriptionVAS().setVas(service.getDefaultVasByType(ValueAddedServiceType.ONETIME_VARIABLE_CHARGE));
        }

        if (variableChargeVAS != null && variableChargeVAS.getChargeableItem() != null) {
            vasSettingID = variableChargeVAS.getSettingByName(variableChargeVAS.getChargeableItem()).getId();
            vasSettingLength = String.valueOf(1);
        }

        if (service.getServiceType() == ServiceType.SIP) {
            isUseStockEquipment = false;
        }

        vasTabName = service.getServiceType() == ServiceType.SIP ? SIP_VAS_TAB_NAME : DEFAULT_VAS_TAB_NAME;
        //Notification settings
        notificationSettings = new ArrayList<>();

        if (service.getNotificationSettings() == null || service.getNotificationSettings().isEmpty()) {
            return;
        }

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
            } else {
                for (NotificationSetting set : service.getNotificationSettings()) {
                    if (set.getEvent() == ev && (set.getChannelList() != null || !set.getChannelList().isEmpty())) {
                        notificationSetting.setSelectedChannelListAsChannels(set.getChannelList());
                    }
                }
            }
        }

        installFee = 0;

        if (service.getInstallFeeInDouble() != 0) {
            installFee = service.getInstallFeeInDouble();
        }
    }

    public EquipmentStatus getEquipmentStatus() {
        return equipmentStatus;
    }

    public void setEquipmentStatus(EquipmentStatus equipmentStatus) {
        this.equipmentStatus = equipmentStatus;
    }

    public List<SelectItem> getEquipmentStatusSelectList() {
        if (equipmentStatusSelectList == null) {
            equipmentStatusSelectList = new ArrayList<>();
            for (EquipmentStatus status : EquipmentStatus.values()) {
                equipmentStatusSelectList.add(new SelectItem(status));
            }
        }
        return equipmentStatusSelectList;
    }

    public void setEquipmentStatusSelectList(List<SelectItem> equipmentStatusSelectList) {
        this.equipmentStatusSelectList = equipmentStatusSelectList;
    }

    public String getBrandName() {
        return brandName;
    }

    public void setBrandName(String brandName) {
        this.brandName = brandName;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public String getPartNumber() {
        return partNumber;
    }

    public void setPartNumber(String partNumber) {
        this.partNumber = partNumber;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    public List<SelectItem> getLanguageList() {
        if (languageList == null) {
            languageList = new ArrayList<>();

            Language lang = null;
            if (getSubscriptionID() != null) {
                lang = getSelected().getDetails().getLanguage();
            } else if (getSubscriberID() != null) {
                lang = getSelectedSubscriber().getDetails().getLanguage();
            }
            //Language lang = Language.AZERI;

            languageList.add(new SelectItem(lang));

            for (Language lg : Language.values()) {
                if (lg == lang) {
                    continue;
                }
                languageList.add(new SelectItem(lg));
            }
        }
        return languageList;
    }

    public void setLanguageList(List<SelectItem> languageList) {
        this.languageList = languageList;
    }

    public Language getLanguage() {
        return language;
    }

    public void setLanguage(Language language) {
        this.language = language;
    }

    public void search(AjaxBehaviorEvent ev) {

        equipmentPersistenceFacade.clearFilters();
        equipmentPersistenceFacade.addFilter(EquipmentPersistenceFacade.Filter.STATUS, EquipmentStatus.AVAILABLE);

        equipmentList = null;

        if (partNumber != null && !partNumber.isEmpty()) {
            equipmentPersistenceFacade.addFilter(EquipmentPersistenceFacade.Filter.PART_NUMBER, partNumber);
        }

        if (modelName != null && !modelName.isEmpty()) {
            equipmentPersistenceFacade.addFilter(EquipmentPersistenceFacade.Filter.MODEL, modelName);
        }

        if (brandName != null && !brandName.isEmpty()) {
            equipmentPersistenceFacade.addFilter(EquipmentPersistenceFacade.Filter.BRAND, brandName);
        }

        if (typeName != null && !typeName.isEmpty()) {
            equipmentPersistenceFacade.addFilter(EquipmentPersistenceFacade.Filter.TYPE, typeName);
        }

        if (macAddress != null && !macAddress.isEmpty()) {
            equipmentPersistenceFacade.addFilter(EquipmentPersistenceFacade.Filter.MAC_ADDRESS, macAddress);
        }

        sqlQuery = equipmentPersistenceFacade.getAllEquimpentSqlQuery(equipmentPersistenceFacade.getFilters());
        log.debug("Search called. Filters: " + equipmentPersistenceFacade.getFilters().toString());
        equipmentList = new LazyTableModelDynamic(equipmentPersistenceFacade, Equipment.class, sqlQuery);
        sqlQuery = sqlQuery.replaceFirst("distinct eqp", "count(distinct eqp.id)");
        long count = equipmentPersistenceFacade.countDynamic(sqlQuery);
        equipmentList.setRowCount((int) count);
        log.debug("Total Found equipment: " + count);
    }

    public void searchMinipopInMemory() {
        minipopListTemp = new ArrayList<>();
        for (MiniPop miniPop : miniPopList) {
            if ((minipPopAddress == null || miniPopMacAddress.isEmpty() || (miniPop.getMac() != null && miniPop.getMac().contains(miniPopMacAddress)))
                    && (minipPopAddress == null || minipPopAddress.isEmpty() || (miniPop.getAddress() != null && miniPop.getAddress().contains(minipPopAddress)))
                    && (switchID == null || switchID.isEmpty() || (miniPop.getSwitch_id() != null && miniPop.getSwitch_id().contains(switchID)))
                    && (ip == null || ip.isEmpty() || (miniPop.getIp() != null && miniPop.getIp().contains(ip)))) {
                minipopListTemp.add(miniPop);
            }
        }
    }

    public void searchMinipops() {

        minipopFacade.clearFilters();
        minipopLazyDataModel = null;
        MiniPopPersistenceFacade.Filter statusFilter = MiniPopPersistenceFacade.Filter.STATUS;
        statusFilter.setOperation(MatchingOperation.EQUALS);
        minipopFacade.addFilter(statusFilter, DeviceStatus.ACTIVE);

        ServiceProvider prov = null;

        if (provider != null) {
            prov = provider;
        } else {
            if (subscriptionID != null) {
                prov = subscriptionFacade.find(subscriptionID).getService().getProvider();
            }
            if (providerID != null && prov == null) {
                prov = providerFacade.find(providerID);
            }
        }

        if (prov.getId() == Providers.NARFIX.getId()) {
            prov = providerFacade.find(Providers.CITYNET.getId());
        }

        minipopFacade.addFilter(MiniPopPersistenceFacade.Filter.PROVIDER, prov);

        if (prov != null && prov.getId() == Providers.CITYNET.getId() && details != null && details.getBuilding() != null && !details.getBuilding().isEmpty()) {
            House house = houseFacade.findByHouseNo(details.getBuilding().trim());
            if (house == null) {
                return;
            }
            minipopFacade.addFilter(MiniPopPersistenceFacade.Filter.HOUSE, house);
        }

        if (prov != null && prov.getId() == Providers.CITYNET.getId() && selected != null && selected.getDetails() != null && selected.getDetails().getBuilding() != null) {
            House house = houseFacade.findByHouseNo(selected.getDetails().getBuilding().trim());
            if (house == null) {
                return;
            }
            minipopFacade.addFilter(MiniPopPersistenceFacade.Filter.HOUSE, house);
        }

        if (miniPopMacAddress != null && !miniPopMacAddress.isEmpty()) {
            minipopFacade.addFilter(MiniPopPersistenceFacade.Filter.MAC_ADDRESS, miniPopMacAddress);
        }

        if (minipPopAddress != null && !minipPopAddress.isEmpty()) {
            minipopFacade.addFilter(MiniPopPersistenceFacade.Filter.ADDRESS, minipPopAddress);
        }

        if (switchID != null && !switchID.isEmpty()) {
            minipopFacade.addFilter(MiniPopPersistenceFacade.Filter.SWITCH_ID, switchID);
        }

        if (ip != null && !ip.isEmpty()) {
            minipopFacade.addFilter(MiniPopPersistenceFacade.Filter.IP, ip);
        }

        if (minipopCategory != null) {
            minipopFacade.addFilter(MiniPopPersistenceFacade.Filter.CATEGORY, minipopCategory);
        }

        log.debug(minipopCategory);
        if ((minipopCategory == null || (minipopCategory != null && minipopCategory != MinipopCategory.TEST)) && prov != null && prov.getId() == Providers.CITYNET.getId()) {
            if (details != null && details.getAts() != null) {
                Ats ats = atsFacade.findByIndex(details.getAts());
                log.debug("ATS: " + ats);
                if (ats != null) {
                    minipopFacade.addFilter(MiniPopPersistenceFacade.Filter.ATS, ats);
                } else {
                    return;
                }
            }
        }

        minipopLazyDataModel = new LazyTableModel<>(minipopFacade);
    }

    public String redirect() {
        return "/pages/subscriber/view.xhtml?subscriber=" + getSelected().getSubscriber().getId() + "&amp;faces-redirect=true&amp;includeViewParams=true";
    }

    public Boolean getIsGenerateAgreement() {
        return isGenerateAgreement;
    }

    public void setIsGenerateAgreement(Boolean isGenerateAgreement) {
        this.isGenerateAgreement = isGenerateAgreement;
    }

    public void onGenerateAgreement() {
        if (isGenerateAgreement && serviceId != null) {
            Service serviceForAgr = serviceFacade.find(Long.valueOf(serviceId));
            Long agreement = agreementGenerator.generate(serviceForAgr.getProvider());
            //Long agreement = 5L;
            log.debug("Generated agreement: " + agreement);

            if (agreement != null) {
                getSubscription().setAgreement(engineFactory.getOperationsEngine(serviceForAgr.getProvider()).
                        generateAgreement(new AgreementGenerationParams.Builder().setAgreement(agreement.toString()).build()));
                log.debug("New agreement field: " + getSubscription().getAgreement());
            }
        }
    }

    public List<NotificationSettingRow> getNotificationSettings() {
        if (notificationSettings == null) {
            notificationSettings = new ArrayList<>();

            NotificationSettingRow notificationSetting = null;

            for (BillingEvent ev : BillingEvent.values()) {
                notificationSetting = new NotificationSettingRow(ev, NotificationChannell.values());
                notificationSettings.add(notificationSetting);

                if (getSelected() != null) {
                    for (NotificationSetting set : getSelected().getNotificationSettings()) {
                        if (set.getEvent() == ev && (set.getChannelList() != null && !set.getChannelList().isEmpty())) {
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

    public Boolean getTaxFreeEnabled() {
        if (taxFreeEnabled == null) {
            if (getSelectedSubscriber().getTaxCategory().getVATRate() == 0L) {
                taxFreeEnabled = true;
            } else {
                taxFreeEnabled = false;
            }
        }
        return taxFreeEnabled;
    }

    public void setTaxFreeEnabled(Boolean taxFreeEnabled) {
        this.taxFreeEnabled = taxFreeEnabled;
    }

    public List<MiniPop> getFilteredMinipopList() {
        return filteredMinipopList;
    }

    public void setFilteredMinipopList(List<MiniPop> filteredMinipopList) {
        this.filteredMinipopList = filteredMinipopList;
    }

    public String getSelectedReseller() {
        if (selectedReseller != null) {
            return selectedReseller;
        }
        if (getSelected() != null && getSelected().getSettingByType(ServiceSettingType.DEALER) != null) {
            selectedReseller = getSelected().getSettingByType(ServiceSettingType.DEALER).getValue();
        }
        return selectedReseller;
    }

    public void setSelectedReseller(String selectedReseller) {
        this.selectedReseller = selectedReseller;
    }

    public String getSelectedDealer() {
        if (selectedDealer != null) {
            return selectedDealer;
        }
        if (getSelected() != null && getSelected().getSettingByType(ServiceSettingType.DEALER) != null) {
            selectedDealer = getSelected().getSettingByType(ServiceSettingType.DEALER).getValue();
        }
        return selectedDealer;
    }

    public void setSelectedDealer(String selectedDealer) {
        this.selectedDealer = selectedDealer;
    }

    public Long getSelectedDealerId() {
        for (SubscriptionSetting ss : selected.getSettings()) {
            if (ss.getProperties().getType() == ServiceSettingType.DEALER) {
                selectedDealerId = Long.valueOf(ss.getValue());
            }
        }
        return selectedDealerId;
    }

    public void setSelectedDealerId(Long selectedDealerId) {
        this.selectedDealerId = selectedDealerId;
    }

    public MiniPop getSelectedMiniPop() {
        return selectedMiniPop;
    }

    public void setSelectedMiniPop(MiniPop selectedMiniPop) {
        this.selectedMiniPop = selectedMiniPop;
    }

    public List<MiniPop> getMinipopList() {
        if (minipopList == null) {
            minipopList = minipopFacade.findAll();
        }
        return minipopList;
    }

    public void setMinipopList(List<MiniPop> minipopList) {
        this.minipopList = minipopList;
    }

    public LazyDataModel<MiniPop> getMinipopLazyDataModel() {
        /*if (minipopLazyDataModel == null) {
            MiniPopPersistenceFacade.Filter statusFilter = MiniPopPersistenceFacade.Filter.STATUS;
            statusFilter.setOperation(MatchingOperation.EQUALS);
            minipopFacade.addFilter(statusFilter, DeviceStatus.ACTIVE);

            minipopLazyDataModel = new LazyTableModel<>(minipopFacade);
        }*/
        return minipopLazyDataModel;
    }

    public void setMinipopLazyDataModel(LazyDataModel<MiniPop> minipopLazyDataModel) {
        this.minipopLazyDataModel = minipopLazyDataModel;
    }

    public String getSwitchID() {
        return switchID;
    }

    public void setSwitchID(String switchID) {
        this.switchID = switchID;
    }

    public String getMinipPopAddress() {
        return minipPopAddress;
    }

    public void setMinipPopAddress(String address) {
        this.minipPopAddress = address;
    }

    public String getMiniPopMacAddress() {
        return miniPopMacAddress;
    }

    public void setMiniPopMacAddress(String miniPopMacAddress) {
        this.miniPopMacAddress = miniPopMacAddress;
    }

    public boolean isUseStockEquipment() {
        return isUseStockEquipment;
    }

    public void setUseStockEquipment(boolean isUseStockEquipment) {
        this.isUseStockEquipment = isUseStockEquipment;
    }

    public void onEquipmentChange() {
        //equipmentTable.setRendered(false);
        log.debug("Use equipment from stock?" + isUseStockEquipment);
    }

    public UIComponent getEquipmentTable() {
        return equipmentTable;
    }

    public void setEquipmentTable(UIComponent equipmentTable) {
        this.equipmentTable = equipmentTable;
    }

    public List<SelectItem> getProviderList() {
        if (providerList == null) {
            providerList = new ArrayList<>();

            for (ServiceProvider prov : providerFacade.findAll()) {
                providerList.add(new SelectItem(prov.getId(), prov.getName()));
            }
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

    public List<SelectItem> getServiceGroupList() {
        if (serviceGroupList == null) {
            serviceGroupList = new ArrayList<>();
        }
        return serviceGroupList;
    }

    public void setServiceGroupList(List<SelectItem> serviceGroupList) {
        this.serviceGroupList = serviceGroupList;
    }

    public ServiceGroup getServiceGroup() {
        return serviceGroup;
    }

    public void setServiceGroup(ServiceGroup serviceGroup) {
        this.serviceGroup = serviceGroup;
    }

    public List<Addendum> getSelectedAddendumList() {
        return selectedAddendumList;
    }

    public void setSelectedAddendumList(List<Addendum> selectedAddendumList) {
        this.selectedAddendumList = selectedAddendumList;
    }

    public void onProviderChange(AjaxBehaviorEvent event) {
        getServiceGroupList().clear();

        if (providerID != null) {
            List<ServiceGroup> groupList = groupFacade.findAllByProviderID(providerID);

            if (groupList != null && groupList.size() > 0) {
                for (ServiceGroup gr : groupList) {
                    getServiceGroupList().add(new SelectItem(gr.getId(), gr.getName()));
                }
            }
        }
    }

    public void onProviderChangePopulateServices(AjaxBehaviorEvent event) {
        getServiceSelectList().clear();

        if (providerID != null) {
            log.debug("provider in service is " + providerID);
            List<Service> servList = serviceFacade.findAll(providerID);

            for (Service srv : servList) {
                log.debug("sevice is " + srv);
                getServiceSelectList().add(new SelectItem(srv.getId(), srv.getName()));
            }
            log.debug("Populated services: " + servList);
            //groupFacade.findAllByProviderID(providerID);
        }
    }

    public void onServiceGroupSelectChange(ValueChangeListener event) {
        log.debug("Service group changed");
    }

    public UploadedFile getContract() {
        return contract;
    }

    public void setContract(UploadedFile contract) {
        this.contract = contract;
    }

    public void handleContractUpload(FileUploadEvent event) {
        contract = event.getFile();

        if (contract == null) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "File not uploaded", "File not uploaded"));
            return;
        }

        if (selected == null) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Subscription unknown", "Subscription unknown"));
            return;
        }

        try {
            if (selected.getContract() == null) {
                Contract contractObj = new Contract();
                contractObj.setDocument(contract.getContents());
                subscriptionFacade.addContractFile(contractObj, selected);
            } else {
                selected.getContract().setDocument(contract.getContents());
                subscriptionFacade.updateContract(selected.getContract());
            }
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Contract successfully added", "Contract successfully added"));
        } catch (Exception ex) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Cannot add contract file", "Cannot add contract file"));
            return;
        }

    }

    public void handleAddendumUpload(FileUploadEvent event) {
        UploadedFile file = event.getFile();

        if (file == null) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "File not uploaded", "File not uploaded"));
            return;
        }

        if (selected == null) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Subscription unknown", "Subscription unknown"));
            return;
        }

        if (selected.getContract() == null) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Upload the contract first", "Upload the contract first"));
            return;
        }

        try {
            log.debug("Selected Addendum List: " + selectedAddendumList);
            if (selectedAddendumList == null || selectedAddendumList.isEmpty()) {
                Addendum addendum = new Addendum();
                addendum.setName(file.getFileName());
                addendum.setDocument(file.getContents());
                subscriptionFacade.addAddendum(addendum, selected);
            } else {
                Addendum selectedAddendum = selectedAddendumList.get(0);
                selectedAddendum.setDocument(file.getContents());
                selectedAddendum.setName(file.getFileName());
                subscriptionFacade.updateAddendum(selectedAddendum);
                selectedAddendumList.remove(0);
            }
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Addendum successfully added", "Addendum successfully added"));
        } catch (Exception ex) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Cannot add addendum file", "Cannot add addendum file"));
            return;
        }

    }

    public StreamedContent getContractContents() {
        if (selected == null || selected.getContract() == null || selected.getContract().getDocument() == null) {
            return null;
        }

        InputStream inputStream = new ByteArrayInputStream(selected.getContract().getDocument());
        contractContents = new DefaultStreamedContent(inputStream, "application/pdf");
        log.debug("Contract contents: " + selected.getContract().getDocument());
        return contractContents;
    }


    public String getUnbilledInv() {
        double invoice_balance = 0;
        List<Invoice> invoices = selected.getSubscriber().getInvoices();

        for (Invoice invoice : invoices) {

            invoice_balance += invoice.getBalance();
        }

        return String.format("%.2f", invoice_balance / 100000);
    }

    public void downloadContract() {
        if (selected == null) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Subscription unknown", "Subscription unknown"));
            return;
        }

        String res = new StringBuilder("/export/contract?format=pdf&sbn=" + selected.getId() + "&file=Contract_" + selected.getAgreement())
                .toString();

        try {
            ExternalContext ctx = FacesContext.getCurrentInstance().getExternalContext();
            ctx.redirect(ctx.getRequestContextPath() + res);
        } catch (IOException ex) {
            log.error("Cannot redirect to export page", ex);
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Cannot export data", "Cannot export data"));
        }
    }

    public void downloadAddendum(Long addendumID) {
        if (selected == null) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Subscription unknown", "Subscription unknown"));
            return;
        }

        if (selected.getContract() == null) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Contract unknown", "Contract unknown"));
            return;
        }

        if (addendumID == null) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Addendum unknown", "Addendum unknown"));
            return;
        }

        String res = new StringBuilder("/export/addendum?format=pdf&addendum=" + addendumID + "&sbn=" + selected.getId() + "&file=" + selected.getAgreement())
                .toString();

        try {
            ExternalContext ctx = FacesContext.getCurrentInstance().getExternalContext();
            ctx.redirect(ctx.getRequestContextPath() + res);
        } catch (IOException ex) {
            log.error("Cannot redirect to export page", ex);
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Cannot export data", "Cannot export data"));
        }
    }

    public void onAddendumRowSelect(SelectEvent ev) {
        if (selectedAddendumList == null) {
            selectedAddendumList = new ArrayList<>();
        }

        selectedAddendumList.add((Addendum) ev.getObject());
    }

    public void onAddendumRowUnSelect(UnselectEvent ev) {
        if (selectedAddendumList != null) {
            selectedAddendumList.remove((Addendum) ev.getObject());
        }
    }

    public String getCableLength() {
        return cableLength;
    }

    public void setCableLength(String cableLength) {
        this.cableLength = cableLength;
    }

    public String getCablePrice() {
        return cablePrice;
    }

    public void setCablePrice(String cablePrice) {
        this.cablePrice = cablePrice;
    }

    public String getCableCharge() {
        DecimalFormat df = new DecimalFormat();
        return String.format("%.2f", cableCharge / 100000d);
    }

    public static long getCableVasCode() {
        return CABLE_VAS_CODE;
    }

    public String getVasSettingValue() {
        return vasSettingValue;
    }

    public void setVasSettingValue(String vasSettingValue) {
        this.vasSettingValue = vasSettingValue;
    }

    public Long getVasSettingID() {
        return vasSettingID;
    }

    public void setVasSettingID(Long vasSettingID) {
        this.vasSettingID = vasSettingID;
    }

    public List<SelectItem> getVasSettingsValuesSelectItem() {
        if (service != null && vasSettingsValuesSelectItem == null) {
            vasSettingsValuesSelectItem = new ArrayList<>();

            for (VASSetting set : service.getDefaultVasByType(ValueAddedServiceType.ONETIME_VARIABLE_CHARGE).getSettings()) {
                vasSettingsValuesSelectItem.add(new SelectItem(set.getId(), set.getName()));
            }
        }
        return vasSettingsValuesSelectItem;
    }

    public void setVasSettingsValuesSelectItem(List<SelectItem> vasSettingsValuesSelectItem) {
        this.vasSettingsValuesSelectItem = vasSettingsValuesSelectItem;
    }

    public SubscriptionVAS getSubscriptionVAS() {
        if (subscriptionVAS == null) {
            subscriptionVAS = new SubscriptionVAS();
        }
        return subscriptionVAS;
    }

    public void setSubscriptionVAS(SubscriptionVAS subscriptionVAS) {
        this.subscriptionVAS = subscriptionVAS;
    }

    public SubscriptionVASSetting getSelectedVASSetting() {
        return selectedVASSetting;
    }

    public void setSelectedVASSetting(SubscriptionVASSetting selectedVASSetting) {
        this.selectedVASSetting = selectedVASSetting;
    }

    public void onCablePriceChange(AjaxBehaviorEvent event) {
        if (service != null && cableLength != null && !cableLength.isEmpty() && cablePrice != null && !cableLength.isEmpty()) {
            try {
                long length = Util.parseAmountFromStringIntoLong(cableLength, null);
                long price = Util.parseAmountFromStringIntoLong(cablePrice, null);

                cableCharge = length * price;
                defaultVasCharges.put(CABLE_VAS_CODE, cableCharge);
            } catch (Exception ex) {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                        "Cannot compute total charge for cable", "Cannot compute total charge for cable"
                ));
            }
        }
    }

    public void addSetting(ActionEvent ev) {
        if (vasSettingID == null || vasSettingValue == null || vasSettingValue.isEmpty() || vasSettingLength == null || vasSettingLength.isEmpty()) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Please fill price and length/cont fields", "Please fill price and length/cont fields"));
            return;
        }
        for (VASSetting set : service.getDefaultVasByType(ValueAddedServiceType.ONETIME_VARIABLE_CHARGE).getSettings()) {
            if (set.getId() == vasSettingID && getSubscriptionVAS().getSettingByName(set.getName()) == null) {
                SubscriptionVASSetting vs = new SubscriptionVASSetting(set.getName(), vasSettingValue, vasSettingLength, "");
                getSubscriptionVAS().addSetting(vs);

                cableCharge += vs.getTotal();
                return;
            }
        }
    }

    public void removeSetting(ActionEvent ev) {
        if (selectedVASSetting == null) {
            return;
        }

        getSubscriptionVAS().removeSetting(selectedVASSetting);
        cableCharge -= selectedVASSetting.getTotal();
    }

    public void resetSettings(ActionEvent ev) {
        selectedVASSetting = null;
        vasSettingID = null;
        vasSettingLength = null;
        vasSettingValue = null;

        ValueAddedService variableChargeVAS = service.getDefaultVasByType(ValueAddedServiceType.ONETIME_VARIABLE_CHARGE);
        if (variableChargeVAS != null && variableChargeVAS.getChargeableItem() != null) {
            vasSettingID = variableChargeVAS.getSettingByName(variableChargeVAS.getChargeableItem()).getId();
            vasSettingLength = String.valueOf(1);
        }
    }

    public String getVasSettingLength() {
        return vasSettingLength;
    }

    public void setVasSettingLength(String vasSettingLength) {
        this.vasSettingLength = vasSettingLength;
    }

    public void onVasSettingSelect(AjaxBehaviorEvent event) {
        vasSettingValue = null;
        vasSettingLength = null;
    }

    public void onSelectEquipment() {
        if (selectedEquipment == null) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Please select equipment", "Please select equipment"));
            return;
        }
        String message = String.format("The equipment with partNumber %s will be assigned to subscription. Please press Save button to finish operation", selectedEquipment.getPartNumber());
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, message, message));
    }

    public void onSelectMinipop() {
        if (selectedMiniPop == null) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Please select equipment", "Please select equipment"));
            return;
        }

        log.debug(String.format("onSelectMinipop: selectedMinipop=%s", selectedMiniPop));
        String message = String.format("The minipop with mac address %s will be assigned to subscription. Please press Save button to finish operation", selectedMiniPop.getMac());
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, message, message));
    }

    public MobileCategory getCategory() {
        return category;
    }

    public void setCategory(MobileCategory category) {
        this.category = category;
    }

    public List<SelectItem> getCategoryList() {
        if (categoryList == null) {
            categoryList = new ArrayList<>();

            for (MobileCategory cat : MobileCategory.values()) {
                categoryList.add(new SelectItem(cat));
            }
        }
        return categoryList;
    }

    public void setCategoryList(List<SelectItem> categoryList) {
        this.categoryList = categoryList;
    }

    public List<SelectItem> getStatusList() {
        if (statusList == null) {
            statusList = new ArrayList<>();

            for (StoreItemStatus st : StoreItemStatus.values()) {
                statusList.add(new SelectItem(st));
            }
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

    public LazyDataModel<Msisdn> getMsisdnList() {
        return msisdnList;
    }

    public void setMsisdnList(LazyDataModel<Msisdn> msisdnList) {
        this.msisdnList = msisdnList;
    }

    public Msisdn getSelectedMsisdn() {
        return selectedMsisdn;
    }

    public void setSelectedMsisdn(Msisdn selected) {
        this.selectedMsisdn = selected;
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
        if (comCatID == null && selectedMsisdn != null && getSelectedMsisdn().getCommercialCategory() != null) {
            comCatID = getSelectedMsisdn().getCommercialCategory().getId();
        }
        return comCatID;
    }

    public void setComCatID(Long comCatID) {
        this.comCatID = comCatID;
    }

    public boolean check() {
        return ((msisdnValue != null && !msisdnValue.isEmpty()) || providerID != null
                || status != null || category != null || comCatID != null);
    }

    public void searchMsisdn(AjaxBehaviorEvent ev) {
        if (!check()) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Please fill al least one field", "Please fill al least one field"));
            return;
        }

        msisdnFacade.clearFilters();

        if (msisdnValue != null && !msisdnValue.isEmpty()) {
            MsidnPersistenceFacade.Filter msisdnValueFilter = MsidnPersistenceFacade.Filter.VALUE;
            msisdnValueFilter.setOperation(MatchingOperation.LIKE);
            msisdnFacade.addFilter(msisdnValueFilter, msisdnValue);
        }

        if (category != null) {
            msisdnFacade.addFilter(MsidnPersistenceFacade.Filter.CATEGORY, category);
        }

        if (comCatID != null) {
            msisdnFacade.addFilter(MsidnPersistenceFacade.Filter.COMMERCIAL_CATEGORY, comCatID);
        }

        if (status != null) {
            msisdnFacade.addFilter(MsidnPersistenceFacade.Filter.STATUS, status);
        }

        if (providerID != null) {
            msisdnFacade.addFilter(ImsiPersistenceFacade.Filter.PROVIDER, providerID);
        }

        msisdnList = new LazyTableModel<>(msisdnFacade);
    }

    public void onMsisdnSelect(AjaxBehaviorEvent ev) {
        if (selectedMsisdn != null) {
            vasSettingValue = selectedMsisdn.getPriceForView() != null
                    ? selectedMsisdn.getPriceForView()
                    : selectedMsisdn.getCommercialCategory().getPriceForView();
        }
    }

    public void disconnectSession(ActionEvent ev) {
        if (selected == null) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Subscription unknown", "Subscription unknown"));
            return;
        }

        try {
            subscriptionFacade.disconnectSession(selected);
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Disconnect successfull", "Disconnect successfull"));
        } catch (Exception ex) {
            log.error("disconnectSession: cannot disconnect session, subscription id=" + selected.getId());
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Cannot disconnect session", "Cannot disconnect session"));
        }
    }

    public void resetServiceList(ActionEvent ev) {
        resetServices();
    }

    public void resetServices() {

        serviceName = null;
        filterServiceID = null;
        lazyServiceList = null;
        serviceList = null;
        selectedService = null;
        ServiceSubgroup subgroupAll;

        if (selected.getService().getProvider().getId() == Providers.CITYNET.getId() && selected.getService().getSubgroup() != null) {
            subgroupAll = subgroupFacade.findSubgroupByName("All");
            if (selected.getService().getSubgroup().getId() == subgroupAll.getId())
                serviceList = serviceFacade.findAll(selected.getService().getProvider().getId());
            else
                serviceList = serviceFacade.findServicesByFilters(selected.getService().getServiceType(), selected.getService().getId(), selected.getService().getSubgroup(), subgroupAll, selected.getService().getProvider().getId());
        } else {

            log.debug("selected.getService().getServiceType() " + selected.getService().getServiceType());
            log.debug(" selected.getService().getId() " + selected.getService().getId());
            log.debug("selected.getService().getProvider().getId() " + selected.getService().getProvider().getId());


            serviceList = serviceFacade.findServicesByFilters(selected.getService().getServiceType(), selected.getService().getId(), selected.getService().getProvider().getId());
//            log.debug("serviceList: " + serviceList);
        }

        log.debug("Service list size: " + serviceList.size());
//        serviceFacade.clearFilters();
//        serviceFacade.addFilter(ServicePersistenceFacade.Filter.PROVIDER_ID, selected.getService().getProvider().getId());
//        serviceFacade.addFilter(ServicePersistenceFacade.Filter.TYPE, selected.getService().getServiceType());
//        if (selected.getService().getProvider().getId() == Providers.CITYNET.getId()) {
//            if (selected.getService().getSubgroup() != null) {
//                serviceFacade.addFilter(ServicePersistenceFacade.Filter.SUBGROUP, selected.getService().getSubgroup());
//            }
//        }
//        ServicePersistenceFacade.Filter serviceIDfilter = ServicePersistenceFacade.Filter.SERVICE_ID;
//        serviceIDfilter.setOperation(MatchingOperation.NOT_EQUALS);
//        serviceFacade.addFilter(serviceIDfilter, selected.getService().getId());
//        serviceFacade.addFilter(ServicePersistenceFacade.Filter.ISACTIVE, 1);
//        lazyServiceList = new LazyTableModel<>(serviceFacade);

    }


    public void resetServiceRate() {


    }


    public void changeService() {
        if (selected == null) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Subscription unknown", "Subscription unknown"));
            return;
        }

        if (selectedService == null) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Please select service", "Please select service"));
            return;
        }

        try {
            if (isApplyFutureDate() && serviceChangeFutureDate != null && serviceChangeFutureDate.after(new Date())) {
                subscriptionFacade.registerServiceChangeJob(selected, selectedService, serviceChangeFutureDate);
                String msg = "Service change job has been registered on " + serviceChangeFutureDate.toString();
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Succesful registration", msg));
            } else {
                OperationsEngine operationsEngine = engineFactory.getOperationsEngine(selected);
                Subscription sbn = operationsEngine.changeService(selected, selectedService, true, !cancelCharge);
                if (sbn == null) {
                    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Cannot change service", "Cannot change service"));
                    return;
                }
                selected = sbn;
                String msg = "Service successfully changed to " + selected.getService().getName();
                log.info(String.format("changeService: service successfully changed, subscription id=%d, selectedService id=%d", selected.getId(), selectedService.getId()));
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, msg, msg));
            }

            bucketList = null;
        } catch (Exception ex) {
            log.error(String.format("changeService: cannot change service, subscription id=%d, selectedService id=%d", selected.getId(), selectedService.getId()), ex);
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Cannot change service", "Cannot change service"));
        }
    }

    public void onServiceChangeDialogClose(AjaxBehaviorEvent ev) {
        resetServices();
    }

    public void onRateChangeClose(AjaxBehaviorEvent ev) {

        resetServiceRate();
    }

    public void onServiceChangeDialogOpen(ActionEvent ev) {
        if (selected == null) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Subscription unknown", "Subscription unknown"));
            return;
        }

        resetServices();
        RequestContext.getCurrentInstance().execute("PF('serviceChangeDialog').show();");
    }

    public void onServiceSearch(AjaxBehaviorEvent ev) {
        if (selected == null) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Subscription unknown", "Subscription unknown"));
            return;
        }
        serviceFacade.clearFilters();
        serviceFacade.addFilter(ServicePersistenceFacade.Filter.PROVIDER_ID, selected.getService().getProvider().getId());
        serviceFacade.addFilter(ServicePersistenceFacade.Filter.TYPE, selected.getService().getServiceType());
        ServicePersistenceFacade.Filter serviceIDfilter = ServicePersistenceFacade.Filter.SERVICE_ID;
        serviceIDfilter.setOperation(MatchingOperation.NOT_EQUALS);
        serviceFacade.addFilter(serviceIDfilter, selected.getService().getId());

        if (filterServiceID != null) {
            serviceFacade.addFilter(ServicePersistenceFacade.Filter.ID, filterServiceID);
        }

        if (serviceName != null && !serviceName.isEmpty()) {
            ServicePersistenceFacade.Filter filterServiceName = ServicePersistenceFacade.Filter.NAME;
            filterServiceName.setOperation(MatchingOperation.LIKE);
            serviceFacade.addFilter(filterServiceName, serviceName);
        }
    }

    public void updateServiceRate(AjaxBehaviorEvent ev) {


    }


    public boolean isApplyFutureDate() {
        return applyFutureDate;
    }

    public void setApplyFutureDate(boolean applyFutureDate) {
        this.applyFutureDate = applyFutureDate;
    }

    public boolean isCancelCharge() {
        return cancelCharge;
    }

    public void setCancelCharge(boolean cancelCharge) {
        this.cancelCharge = cancelCharge;
    }

    public Date getServiceChangeFutureDate() {
        return serviceChangeFutureDate;
    }

    public void setServiceChangeFutureDate(Date serviceChangeFutureDate) {
        this.serviceChangeFutureDate = serviceChangeFutureDate;
    }

    public SubscriptionReactivation getReactivation() {
        if (selected != null && reactivation == null && selected.getService().getBillingModel() != null
                && selected.getService().getBillingModel().getPrinciple() == BillingPrinciple.REQUIRE_REACTIVATION) {
            reactivation = reactivationFacade.findLast(selected);
        }

        return reactivation;
    }

    public void setReactivation(SubscriptionReactivation reactivation) {
        this.reactivation = reactivation;
    }

    public LazyDataModel<CampaignRegister> getCampaignList() {
        if (selected != null && campaignList == null) {
            log.debug("getCampaignList: subscription id = " + selected.getId());
            campaignRegisterFacade.addFilter(CampaignRegisterPersistenceFacade.Filter.SUBSCRIPTION, selected.getId());
            campaignList = new LazyTableModel<>(campaignRegisterFacade);
        }
        return campaignList;
    }

    public void setCampaignList(LazyDataModel<CampaignRegister> campaignList) {
        this.campaignList = campaignList;
    }

    public MiniPop getSubscriptionMiniPop() {
        if (selected != null && selected.getService().getServiceType() == ServiceType.BROADBAND
                && subscriptionMiniPop == null) {
            SubscriptionSetting setting = selected.getSettingByType(ServiceSettingType.BROADBAND_SWITCH);

            if (setting != null && setting.getValue() != null
                    && !setting.getValue().isEmpty() && setting.getValue().length() > 1) {
                try {
                    subscriptionMiniPop = minipopFacade.find(Long.valueOf(setting.getValue()));
                    log.debug(String.format("getSubscriptionMinipop: subscription id=%d, minipop setting: %s, minipop=%s", selected.getId(), setting.getValue(), subscriptionMiniPop));
                } catch (Exception ex) {
                    log.debug(String.format("getSubscriptionMinipop: subscription id=%d, Cannot parse minipop setting: %s", selected.getId(), setting.getValue()), ex);
                }
            }
        }
        return subscriptionMiniPop;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String goToOfflineStats() {
        return "/pages/subscriber/stats/offline.xhtml?subscription=" + getSelected().getAgreement() + "&amp;faces-redirect=true&amp;includeViewParams=true";
    }

    public List<Campaign> getAvailableCampaignListOnCreation() {
        if (availableCampaignListOnCreation == null) {
            List<Campaign> eligibleCampaignList = getAvailableCampaignList();

            availableCampaignListOnCreation = new ArrayList<>();
            for (final Campaign eligible : eligibleCampaignList) {
                if (eligible.isAvailableOnCreation()) {
                    availableCampaignListOnCreation.add(eligible);
                }
            }
        }
        return availableCampaignListOnCreation;
    }

    public void setAvailableCampaignListOnCreation(List<Campaign> availableCampaignListOnCreation) {
        this.availableCampaignListOnCreation = availableCampaignListOnCreation;
    }

    //campaign liist
    public List<Campaign> getAvailableCampaignList() {
        if (selected != null && availableCampaignList == null) {
            List<CampaignRegister> regList = campaignRegisterFacade.findActive(selected);

            List<Campaign> cmpList = new ArrayList<>();

            if (regList != null && !regList.isEmpty()) {
                for (CampaignRegister reg : regList) {
                    cmpList.add(reg.getCampaign());
                }
            }

            availableCampaignList = campaignFacade.findAllActive(selected.getService(), cmpList);

            filterAvailableCampaigns();
        } else if (service != null && availableCampaignList == null) {
            availableCampaignList = campaignFacade.findAllActive(service, new ArrayList<Campaign>());
        }
        return availableCampaignList;
    }

    public void setAvailableCampaignList(List<Campaign> availableCampaignList) {
        this.availableCampaignList = availableCampaignList;
    }

    public Campaign getSelectedCampaign() {
        return selectedCampaign;
    }

    public void setSelectedCampaign(Campaign selectedCampaign) {
        this.selectedCampaign = selectedCampaign;
    }

    public String getCampaignNotes() {
        return campaignNotes;
    }

    public void setCampaignNotes(String campaignNotes) {
        this.campaignNotes = campaignNotes;
    }

    public void addCampaign() {
        if (selectedCampaign == null) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Please select a campaing", "Please select a campaing"));
            return;
        }
        if (selectedCampaign.isActivateOnManualPaymentOnly()) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Please select a valid campaign", "This campaign should be activated through manual payment"));
            return;
        }
        if (selected.getSubscriber().getDetails().getType().equals(SubscriberType.CORP) &&
                (selectedCampaign == null || !selectedCampaign.isCorporateEnabled())) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Cannot add this campaign to corporate clients", "This campaign should not be added to corporate clients"));
            return;
        }
        try {
            campaignJoinerBean.tryAddToCampaigns(selected, selectedCampaign, true, false, campaignNotes);
            campaignList = null;
            getCampaignList();
        } catch (Exception ex) {
            log.error("addCampaign: ", ex);
        }
    }

    public void onCampaignDialogClosed(AjaxBehaviorEvent ev) {
        campaignFacade.clearFilters();
        availableCampaignList = null;
        selectedCampaign = null;
        campaignList = null;
    }

    public String activateCampaign() {
        if (selectedRegister == null) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(SEVERITY_ERROR, "Please select campaign", "Please select campaign"));
            campaignList = null;
            return null;
        }

        log.info(String.format("activateCampaign: subscription agreement=%s, selectedCampaignFromTable id=%d starting campaign activation", selected.getAgreement(), selectedRegister.getId()));

        if (selectedRegister.getCampaign().isActivateOnManualPaymentOnly()) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(SEVERITY_ERROR, "Cannot activate campaign", "This campaign should be activated with manual payment"));
            campaignList = null;
            return null;
        }
        if (!selectedRegister.getCampaign().isActivateOnDemand()) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(SEVERITY_ERROR, "Cannot activate campaign", "Cannot activate campaign"));
            log.error(String.format("activateCampaign: subscription agreement=%s, selectedCampaignFromTable id=%d cannot be manually activated", selected.getAgreement(), selectedRegister.getId()));
            campaignList = null;
            return null;
        }

        try {
            campaignRegisterFacade.tryActivateCampaign(selectedRegister);
        } catch (Exception ex) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(SEVERITY_ERROR, "Error on activating campaign", "Error on activating campaign"));
            log.error(String.format("activateCampaign: subscription agreement=%s, selectedCampaignFromTable id=%d cannot activate", selected.getAgreement(), selectedRegister.getId()), ex);
        }

        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(SEVERITY_INFO, "Campaign successfully activated", "Campaign successfully activated"));
        log.info(String.format("activateCampaign: subscription agreement=%s, selectedCampaignFromTable id=%d successfully activated", selected.getAgreement(), selectedRegister.getId()));
        campaignList = null;

        return String.format("/pages/subscription/view/view_%d.xhtml?subscriber=%d&amp;subscription=%d&amp;faces-redirect=true&amp;includeViewParams=true",
                getSelected().getService().getProvider().getId(),
                getSelected().getSubscriber().getId(),
                getSelected().getId());
    }

    public String deleteCampaign() {
        if (selectedRegister == null) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(SEVERITY_ERROR, "Please select campaign", "Please select campaign to delete"));
            campaignList = null;
            return null;
        }
        if (selectedRegister.getCampaign().getTarget() == CampaignTarget.PAYMENT) {
            subscriptionFacade.removePromo(selectedRegister);
        } else if (selectedRegister.getCampaign().getTarget() == CampaignTarget.EXPIRATION_DATE) {
            if (!subscriptionFacade.rollbackBonusDate(selectedRegister)) {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(SEVERITY_ERROR, "No rollback date could be deducted", "No rollback date could be deducted"));
                campaignList = null;
                return null;
            }
        }
        campaignRegisterFacade.removeDetached(selectedRegister);
        FacesContext.getCurrentInstance().addMessage(
                null,
                new FacesMessage(SEVERITY_INFO, "Campaign removed", "Campaign succesfully removed"));
        String dsc = String.format("subscription id=%s, campaign id=%s", selected.getId(), selectedRegister.getCampaign().getId());
        systemLogger.success(SystemEvent.SUBSCRIPTION_CAMPAIGN_DELETED, selected, dsc);
        return String.format("/pages/subscription/view/view_%d.xhtml?subscriber=%d&amp;subscription=%d&amp;faces-redirect=true&amp;includeViewParams=true",
                getSelected().getService().getProvider().getId(),
                getSelected().getSubscriber().getId(),
                getSelected().getId());
    }

    public String getCampaignName() {
        return campaignName;
    }

    public void setCampaignName(String campaignName) {
        this.campaignName = campaignName;
    }

    public void resetCampaignList() {
        log.debug("resetCampaign list called");
        campaignFacade.clearFilters();
        selectedCampaign = null;
        availableCampaignList = null;
        campaignName = null;
        getAvailableCampaignList();
    }

    public void onCampaignSearch(AjaxBehaviorEvent ev) {
        if (campaignName == null) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(null, "Please fill any field", "Please fill any field"));
            return;
        }

        campaignFacade.clearFilters();
        availableCampaignList = null;

        if (campaignName != null && !campaignName.isEmpty()) {
            campaignFacade.addFilter(CampaignPersistenceFacade.Filter.NAME, campaignName);
        }

        log.debug(String.format("onCampaignSearch: starting keyword=%s", campaignName));

        List<CampaignRegister> regList = campaignRegisterFacade.findActive(selected);

        log.debug(String.format("onCampaignSearch: found registered campaigns %d", regList.size()));

        List<Campaign> cmpList = null;

        if (regList != null && !regList.isEmpty()) {
            cmpList = new ArrayList<>();

            for (CampaignRegister reg : regList) {
                cmpList.add(reg.getCampaign());
            }
        }

        availableCampaignList = campaignFacade.findAllActive(selected != null ? selected.getService() : service, cmpList);
        filterAvailableCampaigns();
        log.debug(String.format("onCampaignSearch: found available campaigns %d", availableCampaignList.size()));
    }

    private void filterAvailableCampaigns() {
        List<CampaignRegister> campRegList = campaignRegisterFacade.findAllBySubscription(selected);
        boolean found = false;

        if (campRegList == null || campRegList.isEmpty()) {
            return;
        }

        Campaign cmp = null;
        Iterator<Campaign> it = availableCampaignList.iterator();

        while (it.hasNext()) {
            cmp = it.next();

            log.debug(String.format("getAvailableCampaignList: analyzing campaign id=%d", cmp.getId()));
            for (CampaignRegister reg : campRegList) {
                if (reg.getStatus().equals(CampaignStatus.PROCESSED)) {
                    continue;
                }
                if (reg.getCampaign().getId() == cmp.getId()) {
                    log.debug(String.format("getAvailableCampaignList: found match, campaign id=%d, regcampid=%d",
                            cmp.getId(), reg.getCampaign().getId()));
                    found = true;
                } else if (cmp.isCompound()) {
                    for (Campaign subCampaign : cmp.getCampaignList()) {
                        log.debug(String.format("getAvailableCampaignList: analyzing subcampaign id=%d", subCampaign.getId()));
                        if (reg.getCampaign().getId() == subCampaign.getId()) {
                            log.debug(String.format("getAvailableCampaignList: found match, subcampaign id=%d, regcampid=%d",
                                    subCampaign.getId(), reg.getCampaign().getId()));
                            found = true;
                        }
                    }
                }

                if (found) {
                    log.debug(String.format("getAvailableCampaignList: removing campaign id=%d", cmp.getId()));
                    try {
                        it.remove();
                    } catch (Exception ex) {
                        log.error(ex);
                    }
                    found = false;
                }

            }

        }
    }

    public CampaignRegister getSelectedRegister() {
        return selectedRegister;
    }

    public void setSelectedRegister(CampaignRegister selectedRegister) {
        this.selectedRegister = selectedRegister;
    }

    public ExternalStatusInformation getExternalStatusInformation() {
        if (selected != null && externalStatusInformation == null) {
            try {
                if (provisioner == null) {
                    provisioner = engineFactory.getProvisioningEngine(selected);
                }

                externalStatusInformation = provisioner.collectExternalStatusInformation(selected);
            } catch (Exception ex) {
                log.error(String.format("getExernalStatusInformation: agreement=%s. Cannot collect info", selected.getAgreement()), ex);
            }
        }
        return externalStatusInformation;
    }

    public void setExternalStatusInformation(ExternalStatusInformation externalStatusInformation) {
        this.externalStatusInformation = externalStatusInformation;
    }

    public void reloadExternalStatusInformation() {
        externalStatusInformation = null;
        externalStatusInformation = getExternalStatusInformation();
    }

    public List<Equipment> getFilteredDealerEquipment() {
        return filteredDealerEquipment;
    }

    public void setFilteredDealerEquipment(List<Equipment> filteredDealerEquipment) {
        this.filteredDealerEquipment = filteredDealerEquipment;
    }

    public Equipment getSelectedDealerEquipment() {
        return selectedDealerEquipment;
    }

    public void setSelectedDealerEquipment(Equipment selectedDealerEquipment) {
        this.selectedDealerEquipment = selectedDealerEquipment;
    }

    public List<Equipment> getDealerEquipment() {
        if (dealerEquipment == null && securityManager.checkUIPermissions("Own stock", Privilege.READ)) {
            dealerEquipment = new ArrayList<>();
            searchDealerEquipment(null);
            log.debug(String.format("getDealerEquipment: size=%d", dealerEquipment.size()));
        }
        return dealerEquipment;
    }

    public void setDealerEquipment(List<Equipment> dealerEquipment) {
        this.dealerEquipment = dealerEquipment;
    }

    public String getParameterDealerEquipmentPartnumber() {
        return parameterDealerEquipmentPartnumber;
    }

    public void setParameterDealerEquipmentPartnumber(String parameterDealerEquipmentPartnumber) {
        this.parameterDealerEquipmentPartnumber = parameterDealerEquipmentPartnumber;
    }

    public boolean isDealer() {
        try {
            return securityManager.checkPermissions("Own stock", Privilege.READ);
        } catch (NoPrivilegeFoundException ex) {
            log.debug("isDealer: not privilege found", ex);
            return false;
        }
    }

    public void searchDealerEquipment(AjaxBehaviorEvent ev) {
        SalesPartnerStore partnerStore = partnerStoreFacade.findByPartnerUsername(FacesContext.getCurrentInstance().getExternalContext().getUserPrincipal().getName());

        dealerEquipment.clear();

        for (Equipment eq : partnerStore.getEquipmentList()) {
            if (eq.getStatus() == EquipmentStatus.RESERVED) {
                continue;
            }
            if (parameterDealerEquipmentPartnumber != null || brandName != null || modelName != null || typeName != null) {
                if ((parameterDealerEquipmentPartnumber != null && !parameterDealerEquipmentPartnumber.isEmpty() && eq.getPartNumber().toLowerCase().contains(parameterDealerEquipmentPartnumber.toLowerCase()))
                        || (brandName != null && !brandName.isEmpty() && eq.getBrand().getName().toLowerCase().contains(brandName.toLowerCase()))
                        || (modelName != null && !modelName.isEmpty() && eq.getModel().getName().toLowerCase().contains(modelName.toLowerCase()))
                        || (typeName != null && !typeName.isEmpty() && eq.getType().getName().toLowerCase().contains(typeName.toLowerCase()))) {
                    dealerEquipment.add(eq);
                }
            } else {
                dealerEquipment.add(eq);
            }
        }
    }

    public String getProvisioningReason() {
        return provisioningReason;
    }

    public void setProvisioningReason(String provisioningReason) {
        this.provisioningReason = provisioningReason;
    }

    public String getProvisioningReasonStandard() {
        return provisioningReasonStandard;
    }

    public void setProvisioningReasonStandard(String provisioningReasonStandard) {
        this.provisioningReasonStandard = provisioningReasonStandard;
    }

    public void validateReprovisioningReason(FacesContext context, UIComponent component, Object obj) {
        UISelectOne selectMenu = ((UISelectOne) provisioningReasonSelectMenu);
        UIInput input = (UIInput) provisioiningReasonOtherInput;

        String standard = (String) (selectMenu.isLocalValueSet() ? selectMenu.getValue() : selectMenu.getSubmittedValue());
        String other = (String) (input.isLocalValueSet() ? input.getValue() : input.getSubmittedValue());

        log.debug(String.format("standard: submitted=%s, local=%s; other: submitted=%s, local=%s", selectMenu.getSubmittedValue(), selectMenu.getValue(), input.getSubmittedValue(), input.getValue()));
        log.debug(String.format("parsed standard: %s; other: %s", standard, other));

        String msg = "Please select one of the standard reasons or provide your own one";

        if ((standard == null || standard.isEmpty()) && (other == null || other.isEmpty())) {
            throw new ValidatorException(new FacesMessage(FacesMessage.SEVERITY_ERROR, msg, msg));
        }
    }

    public UIComponent getProvisioiningReasonOtherInput() {
        return provisioiningReasonOtherInput;
    }

    public void setProvisioiningReasonOtherInput(UIComponent provisioiningReasonOtherInput) {
        this.provisioiningReasonOtherInput = provisioiningReasonOtherInput;
    }

    public UIComponent getProvisioningReasonSelectMenu() {
        return provisioningReasonSelectMenu;
    }

    public void setProvisioningReasonSelectMenu(UIComponent provisioningReasonSelectMenu) {
        this.provisioningReasonSelectMenu = provisioningReasonSelectMenu;
    }

    public void onProvisioningDialogClosed(AjaxBehaviorEvent ev) {
        provisioningReason = null;
        provisioningReasonStandard = null;
    }

    public void onCreateLoad() {
        String url = ((HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest()).getRequestURL().toString();
        log.debug("url" + url);

        if (url.toLowerCase().contains(String.valueOf(Providers.AZERTELECOM.getId()))) {
            setProviderID(Providers.AZERTELECOM.getId());
        } else if (url.toLowerCase().contains(String.valueOf(Providers.AZERTELECOMPOST.getId()))) {
            setProviderID(Providers.AZERTELECOMPOST.getId());
        } else if (url.toLowerCase().contains(String.valueOf(Providers.BBTV_BAKU.getId()))) {
            setProviderID(Providers.BBTV_BAKU.getId());
        } else if (url.toLowerCase().contains(String.valueOf(Providers.BBTV.getId()))) {
            setProviderID(Providers.BBTV.getId());
        } else if (url.toLowerCase().contains(String.valueOf(Providers.CITYNET.getId()))) {
            setProviderID(Providers.CITYNET.getId());
        } else if (url.toLowerCase().contains(String.valueOf(Providers.QUTU.getId()))) {
            setProviderID(Providers.QUTU.getId());
        } else if (url.toLowerCase().contains(String.valueOf(Providers.QUTUNARHOME.getId()))) {
            setProviderID(Providers.QUTUNARHOME.getId());
        } else if (url.toLowerCase().contains(String.valueOf(Providers.UNINET.getId()))) {
            setProviderID(Providers.UNINET.getId());
        } else if (url.toLowerCase().contains(String.valueOf(Providers.DATAPLUS.getId()))) {
            setProviderID(Providers.DATAPLUS.getId());
        } else if (url.toLowerCase().contains(String.valueOf(Providers.NARFIX.getId()))) {
            setProviderID(Providers.NARFIX.getId());
        } else if (url.toLowerCase().contains(String.valueOf(Providers.GLOBAL.getId()))) {
            setProviderID(Providers.GLOBAL.getId());
        } else if (url.toLowerCase().contains(String.valueOf(Providers.CNC.getId()))) {
            setProviderID(Providers.CNC.getId());
        }
        provider = providerFacade.find(getProviderID());

        if (url.toLowerCase().contains(String.valueOf(Providers.CITYNET.getId())) ||
                url.toLowerCase().contains(String.valueOf(Providers.QUTU.getId())) ||
                url.toLowerCase().contains(String.valueOf(Providers.NARFIX.getId()))) {
            getAtsSelectList().clear();
//            details = new SubscriptionDetails();
            List<Ats> atsList = subscriptionFacade.getAtsList();
            List<Long> citynetProviders =
                    Arrays.asList(Providers.CITYNET.getId(), Providers.QUTU.getId(), Providers.NARFIX.getId());
            for (Ats ats : atsList) {
                if (ats.getStatus() != null && ats.getStatus().equals(AtsStatus.ACTIVE)
                        && (ats.getProvider() == null || citynetProviders.contains(ats.getProvider().getId()))) {
                    getAtsSelectList().add(new SelectItem(ats.getAtsIndex(), ats.getName()));
                }
            }
        } else if (url.toLowerCase().contains(String.valueOf(Providers.UNINET.getId()))) {
            getAtsSelectList().clear();
//            details = new SubscriptionDetails();
            List<Ats> atsList = subscriptionFacade.getAtsList();
            for (Ats ats : atsList) {
                if (ats.getProvider() == null || ats.getProvider().getId() == Providers.UNINET.getId()) {
                    getAtsSelectList().add(new SelectItem(ats.getAtsIndex(), ats.getName()));
                }
            }
        } else if (url.toLowerCase().contains(String.valueOf(Providers.DATAPLUS.getId()))) {
            getAtsSelectList().clear();
            List<Ats> atsList = atsFacade.findByProvider(Providers.DATAPLUS.getId());
            for (Ats ats : atsList) {
                getAtsSelectList().add(new SelectItem(ats.getAtsIndex(), ats.getName()));
            }
        } else if (url.toLowerCase().contains(String.valueOf(Providers.GLOBAL.getId()))) {
            getAtsSelectList().clear();
            List<Ats> atsList = atsFacade.findByProvider(Providers.GLOBAL.getId());
            for (Ats ats : atsList) {
                getAtsSelectList().add(new SelectItem(ats.getAtsIndex(), ats.getName()));
            }
        } else if (url.toLowerCase().contains(String.valueOf(Providers.CNC.getId()))) {
            getAtsSelectList().clear();
            List<Ats> atsList = atsFacade.findByProvider(Providers.CNC.getId());
            for (Ats ats : atsList) {
                getAtsSelectList().add(new SelectItem(ats.getAtsIndex(), ats.getName()));
            }
        }

        if (providerID != null) {
            log.debug("serviceSelectList is loading ");
            getServiceSelectList().clear();
            List<Service> servList = serviceFacade.findAll(providerID);

            for (Service srv : servList) {
                getServiceSelectList().add(new SelectItem(srv.getId(), srv.getName()));
            }
//            log.debug("serviceSelectList are: " + getServiceSelectList().get(0).toString());
        }

    }

    private List<SelectItem> streetSelectList;
    private List<SelectItem> atsSelectList;
    private Long minipopId;
    private List<SelectItem> minipopSelectList;
    private Integer portId;
    private Integer slotId;
    private List<SelectItem> portSelectList;

    private String agreementString = "";

    public String getAgreementString() {

        if (agreementString.isEmpty()) {
            return "Blank";
        }

        return agreementString;
    }

    public void setAgreementString(String agreementString) {
        this.agreementString = agreementString;
    }

    public List<SelectItem> getStreetSelectList() {
        if (streetSelectList == null) {
            streetSelectList = new ArrayList<SelectItem>();
        }
        return streetSelectList;
    }

    public void setStreetSelectList(List<SelectItem> streetSelectList) {
        this.streetSelectList = streetSelectList;
    }

    public List<SelectItem> getAtsSelectList() {
        if (atsSelectList == null) {
            atsSelectList = new ArrayList<SelectItem>();
            log.debug("getSelected().getService().getProvider().getId() " + getSelected());
            if (getSelected() != null &&
                    (Providers.DATAPLUS.getId().equals(getSelected().getService().getProvider().getId()) ||
                            Providers.GLOBAL.getId().equals(getSelected().getService().getProvider().getId()) ||
                            Providers.CNC.getId().equals(getSelected().getService().getProvider().getId()))) {
                List<Ats> atsList = atsFacade.findByProvider(getSelected().getService().getProvider().getId());
                log.debug("atsList " + atsList.size());

                for (Ats ats : atsList) {
                    getAtsSelectList().add(new SelectItem(ats.getAtsIndex(), ats.getName()));
                }
            }
        }
        return atsSelectList;
    }

    public void setAtsSelectList(List<SelectItem> atsSelectList) {
        this.atsSelectList = atsSelectList;
    }

    public Long getMinipopId() {
        if (minipopId != null) {
            return minipopId;
        }
        if (getSelected() != null && getSelected().getSettingByType(ServiceSettingType.BROADBAND_SWITCH) != null) {
            minipopId = Long.parseLong(getSelected().getSettingByType(ServiceSettingType.BROADBAND_SWITCH).getValue());
        }
        return minipopId;
    }

    public void setMinipopId(Long minipopId) {
        this.minipopId = minipopId;
    }

    public List<SelectItem> getMinipopSelectList() {
        if (minipopSelectList == null) {
            minipopSelectList = new ArrayList<>();
            if (getSelected() != null) {
                if (getDetails().getAts() != null) {
                    log.debug("ATS: " + getDetails().getAts());
                    Ats ats = atsFacade.findByIndex(getDetails().getAts());
                    ServiceProvider provider = providerFacade.find(Providers.DATAPLUS.getId());

                    if (ats != null && provider != null) {
                        List<MiniPop> miniPopList = minipopFacade.findByFilters(provider, ats, DeviceStatus.ACTIVE);

                        for (MiniPop miniPop : miniPopList) {
                            minipopSelectList.add(new SelectItem(miniPop.getId(), miniPop.getSwitch_id()));
                        }
                    }
                }
            }
        }
        return minipopSelectList;
    }

    public void setMinipopSelectList(List<SelectItem> minipopSelectList) {
        this.minipopSelectList = minipopSelectList;
    }


    public String getUsername() {
        if (username != null) {
            return username;
        }

        if (getSelected() != null && getSelected().getSettingByType(ServiceSettingType.USERNAME) != null) {
            username = getSelected().getSettingByType(ServiceSettingType.USERNAME).getValue();

        }

        return username;
    }


    public void setUsername(String username) {
        this.username = username;

    }


    public String getPassword() {
        if (password != null) {
            return password;
        }

        if (getSelected() != null && getSelected().getSettingByType(ServiceSettingType.PASSWORD) != null) {
            password = getSelected().getSettingByType(ServiceSettingType.PASSWORD).getValue();

        }

        return password;
    }


    public void setPassword(String password) {
        this.password = password;

    }


    public Integer getPortId() {
        if (portId != null) {
            return portId;
        }
        if (getSelected() != null && getSelected().getSettingByType(ServiceSettingType.BROADBAND_SWITCH_PORT) != null) {
            portId = Integer.parseInt(getSelected().getSettingByType(ServiceSettingType.BROADBAND_SWITCH_PORT).getValue());
        }
        return portId;
    }

    public void setPortId(Integer portId) {
        this.portId = portId;
    }

    public Integer getSlotId() {
        if (slotId != null) {
            return slotId;
        }
        if (getSelected() != null && getSelected().getSettingByType(ServiceSettingType.BROADBAND_SWITCH_SLOT) != null) {
            slotId = Integer.parseInt(getSelected().getSettingByType(ServiceSettingType.BROADBAND_SWITCH_SLOT).getValue());
        }
        return slotId;
    }

    public void setSlotId(Integer slotId) {
        this.slotId = slotId;
    }

    public List<SelectItem> getPortSelectList() {
        if (portSelectList == null) {
            portSelectList = new ArrayList<>();
            if (getSelected() != null) {
                log.debug("On port select");

                if (getMinipopId() != null) {
                    log.debug("miniPopId: " + getMinipopId());

                    MiniPop minipop = minipopFacade.find(getMinipopId());

                    if (minipop != null) {
                        for (int portId = 1; portId <= minipop.getNumberOfPorts(); ++portId) {

                            portSelectList.add(new SelectItem(
                                    portId,
                                    String.valueOf(portId),
                                    String.valueOf(portId),
                                    minipop.getReserved(portId) != null));
                        }
                    }
                }
            }
        }
        return portSelectList;
    }

    public void setPortSelectList(List<SelectItem> portSelectList) {
        this.portSelectList = portSelectList;
    }

    public List<SelectItem> getDealerSelectList() {
        if (dealerSelectList == null) {
            dealerSelectList = new ArrayList<>();
            List<Dealer> dealerList = dealerFacade.findAll();
            ;
            for (final Dealer dealer : dealerList) {
                dealerSelectList.add(new SelectItem(dealer.getId(), dealer.getName()));
            }
        }
        return dealerSelectList;
    }

    public void setDealerSelectList(List<SelectItem> dealerSelectList) {
        this.dealerSelectList = dealerSelectList;
    }

    public List<SelectItem> getResellerSelectList() {
        if (resellerSelectList == null) {
            resellerSelectList = new ArrayList<>();
            List<Reseller> resellerList = resellerPersistenceFacade.findByProviderId(getProviderID());
            for (final Reseller reseller : resellerList) {
                resellerSelectList.add(new SelectItem(reseller.getId(), reseller.getName()));
            }
        }
        return resellerSelectList;
    }

    public void setResellerSelectList(List<SelectItem> resellerSelectList) {
        this.resellerSelectList = resellerSelectList;
    }

    public void onAtsSelect(AjaxBehaviorEvent event) {
        getStreetSelectList().clear();
        minipopLazyDataModel = null;
        log.debug("On ats select");

        if (getDetails().getAts() != null) {
            log.debug("ATS: " + getDetails().getAts());
            List<Streets> streetsList = subscriptionFacade.getStreetsOfAts(getDetails().getAts());

            for (Streets street : streetsList) {
                getStreetSelectList().add(new SelectItem(street.getId(), street.getName()));
            }
        }
    }

    public void onServiceTypeSelect(AjaxBehaviorEvent event) {
        log.debug("onServiceTypeSelect " + serviceTypeId);
        if (serviceTypeId.equals("2") || serviceTypeId.equals("3") || serviceTypeId.equals("4")) {
            log.debug("dsfdfg");
            this.minipopId = null;
            this.portId = null;
            this.selectedMiniPop = null;
        }
    }

    public void onAtsSelectPopulateMinipops(AjaxBehaviorEvent event) {
        getMinipopSelectList().clear();
        minipopLazyDataModel = null;
        log.debug("On ats select");

        if (getDetails().getAts() != null) {
            log.debug("ATS: " + getDetails().getAts());
            Ats ats = atsFacade.findByIndex(getDetails().getAts());
            ServiceProvider provider = providerFacade.find(getProviderID());


            if (ats != null && provider != null) {
                List<MiniPop> miniPopList = minipopFacade.findByFilters(provider, ats, DeviceStatus.ACTIVE);

                for (MiniPop miniPop : miniPopList) {
                    getMinipopSelectList().add(new SelectItem(miniPop.getId(), miniPop.getSwitch_id()));
                }
            }
        }
    }

    public void onMinipopSelect(AjaxBehaviorEvent event) {
        getPortSelectList().clear();
        log.debug("On ats select");

        if (minipopId != null) {
            log.debug("miniPopId: " + minipopId);

            MiniPop minipop = minipopFacade.find(minipopId);

            if (minipop != null) {
                for (int portId = 1; portId <= minipop.getNumberOfPorts(); ++portId) {
                    getPortSelectList().add(new SelectItem(
                            portId,
                            String.valueOf(portId),
                            String.valueOf(portId),
                            minipop.getReserved(portId) != null));
                }
            }
        }
    }

    public void onAddDetails() {
        Streets str = null;
        try {
            str = streetFacede.find(Long.parseLong(getDetails().getStreet()));
        } catch (Exception e) {

        }
        String building = getDetails().getBuilding();
        if (building != null) {
            building = building.replace("/", "");
        }
        getSubscription().setAgreement(engineFactory.getOperationsEngine(providerFacade.find(Providers.CITYNET.getId())).
                generateAgreement(
                        new AgreementGenerationParams.Builder().
                                setAts(getDetails().getAts()).
                                setStr(str).
                                setBuilding(building).
                                setApartment(getDetails().getApartment()).build()));

        log.debug("Street: " + getDetails().getStreet());

    }

    public void onAddDetailsConvergent() {
        Streets str = null;
        try {
            str = streetFacede.find(Long.parseLong(getDetails().getStreet()));
        } catch (Exception e) {

        }
        String building = getDetails().getBuilding();
        if (building != null) {
            building = building.replace("/", "");
        }

        Subscription sub = subscriptionFacade.findConvergent(getMsisdn());
        log.debug("onAddDetailsConvergent sub: " + sub);
        log.debug("getMsisdn(): " + getMsisdn());

        if (sub != null) {
            getSubscription().setAgreement(engineFactory.getOperationsEngine(providerFacade.find(Providers.QUTU.getId())).
                    generateAgreement(
                            new AgreementGenerationParams.Builder().
                                    setMsisdn(getMsisdn()).
                                    setAgreement(sub.getAgreement()).
                                    build()));
            getSubscription().setAgreement(getMsisdn() + "Q" + (Integer.parseInt(sub.getAgreement().substring(10)) + 1));
        } else {
            getSubscription().setAgreement(getMsisdn() + "Q1");
        }

        log.debug("Street: " + getDetails().getStreet());

    }

    public void onAddDetailsNarfix() {
        Streets str = null;
        try {
            str = streetFacede.find(Long.parseLong(getDetails().getStreet()));
        } catch (Exception e) {

        }
        String building = getDetails().getBuilding();
        if (building != null) {
            building = building.replace("/", "");
        }
        getSubscription().setAgreement(engineFactory.getOperationsEngine(providerFacade.find(Providers.NARFIX.getId())).
                generateAgreement(
                        new AgreementGenerationParams.Builder().
                                setAts(getDetails().getAts()).
                                setStr(str).
                                setBuilding(building).
                                setApartment(getDetails().getApartment()).build()));

        log.debug("Street: " + getDetails().getStreet());

    }

    public void onResourceRowEdit(RowEditEvent event) {
        SubscriptionResourceBucket buck = (SubscriptionResourceBucket) event.getObject();
        SubscriptionResourceBucket oldBuck = subResBucketFacade.find(buck.getId());
        log.debug("NEW: " + buck.getCapacity() + "OLD: " + oldBuck.getCapacity());
        Subscription subscription = subscriptionFacade.find(subscriptionID);
        if (subscription.getService().getProvider().getId() == Providers.CITYNET.getId()) {
            subResBucketFacade.update(buck);
            subscription = subscriptionFacade.find(subscription.getId());
            boolean res = subscriptionFacade.updateAccount(subscription);
            if (res) {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(SEVERITY_INFO, "Bucket editted", "Bucket editted"));
            } else {
                subResBucketFacade.update(oldBuck);
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(SEVERITY_INFO, "Bucket cannot be editted", "Bucket cannot be editted"));
            }
        }
    }


    public BillingModel getBillingModel() {
        return billingModel;
    }

    public void setBillingModel(BillingModel billingModel) {
        this.billingModel = billingModel;
    }

    public List<BillingModel> getBillingModelList() {
        if (billingModelList == null) {
            billingModelList = modelFacade.findAll();
        }
        return billingModelList;
    }

    public void setBillingModelList(List<BillingModel> billingModelList) {
        this.billingModelList = billingModelList;
    }

    public String getSwitchName(String switchID) {
        if (switchID == null || switchID.isEmpty()) {
            return null;
        }
        MiniPop miniPop = minipopFacade.find(Long.valueOf(switchID));
        if (miniPop == null) {
            return null;
        }
        return miniPop.getSwitch_id();
    }

    public String getNameFromId(SubscriptionSetting set) {
        if (set.getProperties().getType().equals(ServiceSettingType.DEALER)) {
            if (getSelected().getService().getProvider().getId() == Providers.DATAPLUS.getId() ||
                    getSelected().getService().getProvider().getId() == Providers.GLOBAL.getId()) {
                String resellerID = set.getValue();
                if (resellerID == null || resellerID.isEmpty()) {
                    return null;
                }
                Reseller reseller = resellerPersistenceFacade.find(Long.valueOf(resellerID));
                if (reseller == null) {
                    return null;
                }
                return reseller.getName();
            } else {
                String dealerID = set.getValue();
                if (dealerID == null || dealerID.isEmpty()) {
                    return null;
                }
                Dealer dealer = dealerFacade.find(Long.valueOf(dealerID));
                if (dealer == null) {
                    return null;
                }
                return dealer.getName();
            }
        } else if (set.getProperties().getType().equals(ServiceSettingType.BROADBAND_SWITCH)) {
            String switchID = set.getValue();
            if (switchID == null || switchID.isEmpty()) {
                return null;
            }
            MiniPop miniPop = minipopFacade.find(Long.valueOf(switchID));
            if (miniPop == null) {
                return null;
            }
            return miniPop.getSwitch_id();
        } else if (set.getProperties().getType().equals(ServiceSettingType.SERVICE_TYPE)) {
            String serviceTypeId = set.getValue();
            if (serviceTypeId == null || serviceTypeId.isEmpty()) {
                return null;
            }
            SubscriptionServiceType serviceType =
                    serviceTypePersistenceFacade.find(Long.valueOf(serviceTypeId));
            if (serviceType == null) {
                return null;
            }
            return serviceType.getName();
        } else if (set.getProperties().getType().equals(ServiceSettingType.ZONE)) {
            String zoneId = set.getValue();
            if (zoneId == null || zoneId.isEmpty()) {
                return null;
            }
            Zone zone = zonePersistenceFacade.find(Long.valueOf(zoneId));
            if (zone == null) {
                return null;
            }
            return zone.getName();
        }
        return null;
    }

    public Long getCitynetProviderId() {
        return Providers.CITYNET.getId();
    }

    public Long getUninetProviderId() {
        return Providers.UNINET.getId();
    }

    public Long getQutuProviderId() {
        return Providers.QUTU.getId();
    }

    boolean fixPageLoad() {
        try {
            selected = subscriptionFacade.find(subscriptionID);
            return true;
        } catch (Exception e) {
            e.printStackTrace(System.err);
            return false;
        }
    }

    public void onPageLoad() {

        if (selected == null && subscriptionID != null) {
            selected = subscriptionFacade.find(subscriptionID);
//            while (!fixPageLoad()){}
        }

        resetServices();
        allowedVasList = new ArrayList<>(selected.getService().getAllowedVASList());
        availableCampaignList = getAvailableCampaignList();

        provider = selected.getService().getProvider();
        providerID = provider.getId();

        if (provider.getId() == Providers.CITYNET.getId()) {
            if (selected.getDetails() != null && selected.getDetails().getAts() != null) {
                ats = atsFacade.findByIndex(selected.getDetails().getAts());
            }
        }

        try {
            checkSettings();
        } catch (Exception ex) {
        }

        if (provider != null) {
            log.debug("serviceSelectList is loading ");
            getServiceSelectList().clear();
            List<Service> servList = serviceFacade.findAll(provider.getId());

            for (Service srv : servList) {
                getServiceSelectList().add(new SelectItem(srv.getId(), srv.getName()));
            }
            log.debug("serviceSelectList are: " + getServiceSelectList().get(0).toString());
        }
        //miniPopList = minipopFacade.findByFilters(provider, ats, DeviceStatus.ACTIVE);
        //minipopListTemp = new ArrayList<>(miniPopList);
    }

    @EJB
    private StreetPersistenceFacade streetFacede;

    public String getSubscriptionAddress() {
        String street = getSelected().getDetails().getStreet();
        String building = getSelected().getDetails().getBuilding();
        String appartment = getSelected().getDetails().getApartment();
        String entrance = getSelected().getDetails().getEntrance();
        String floor = getSelected().getDetails().getFloor();
        String ats = getSelected().getDetails().getAts();
        Streets strObj;
        Ats atsObj;

        try {
            strObj = streetFacede.find(Long.parseLong(street));
            street = strObj.getName();
        } catch (Exception e) {
        }

        try {
            atsObj = atsFacade.findByIndex(ats);
            ats = atsObj.getName();
        } catch (Exception e) {
        }

        return appartment != null ? ats + ", " + street + ", " + building + ", apt. " + appartment + ", ent. " + entrance + ", floor " + floor : ats + ", " + street + ", " + building;
    }

    public void checkSettings() throws PortAlreadyReservedException, NoFreePortLeftException {

        if (selected != null && selected.getService().getProvider().getId() == Providers.CITYNET.getId() && (selected.getSettings() == null || selected.getSettings().size() == 0)) {

            selected = subscriptionFacade.update(selected);
            MiniPop miniPop = findTempMinipop(selected.getService().getProvider());
            if (miniPop == null) {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(SEVERITY_ERROR, "Cannot find temporary minipop", "Cannot find temporary minipop"));
                return;
            }

            log.debug("Minipop: " + miniPop);

            Port port = minipopFacade.getAvailablePort(miniPop);

            log.debug("PORT: " + port);

            //if (port != null) return;
            List<SubscriptionSetting> settingList = new ArrayList<>();
            SubscriptionSetting subscriptionSetting = new SubscriptionSetting();

            subscriptionSetting.setValue(miniPop.getMasterVlan().toString() + "-" + (miniPop.getSubVlan() + port.getNumber()));
            ServiceSetting serviceSetting = subscriptionFacade.createServiceSetting(43L, "Username", ServiceSettingType.USERNAME, Providers.CITYNET, ServiceType.BROADBAND, "");
            subscriptionSetting.setProperties(serviceSetting);
            settingFacade.save(subscriptionSetting);
            subscriptionSetting = settingFacade.update(subscriptionSetting);
            settingList.add(subscriptionSetting);

            subscriptionSetting = new SubscriptionSetting();
            subscriptionSetting.setValue(String.valueOf(miniPop.getId()));
            serviceSetting = subscriptionFacade.createServiceSetting(45L, "Switch", ServiceSettingType.BROADBAND_SWITCH, Providers.CITYNET, ServiceType.BROADBAND, "");
            subscriptionSetting.setProperties(serviceSetting);
            settingFacade.save(subscriptionSetting);
            subscriptionSetting = settingFacade.update(subscriptionSetting);
            settingList.add(subscriptionSetting);

            subscriptionSetting = new SubscriptionSetting();
            subscriptionSetting.setValue(String.valueOf(port.getNumber()));
            serviceSetting = subscriptionFacade.createServiceSetting(46L, "Switch port", ServiceSettingType.BROADBAND_SWITCH_PORT, Providers.CITYNET, ServiceType.BROADBAND, "");
            subscriptionSetting.setProperties(serviceSetting);
            settingFacade.save(subscriptionSetting);
            subscriptionSetting = settingFacade.update(subscriptionSetting);
            settingList.add(subscriptionSetting);

            subscriptionSetting = new SubscriptionSetting();
            subscriptionSetting.setValue("-");
            serviceSetting = subscriptionFacade.createServiceSetting(44L, "Password", ServiceSettingType.PASSWORD, Providers.CITYNET, ServiceType.BROADBAND, "");
            subscriptionSetting.setProperties(serviceSetting);
            settingFacade.save(subscriptionSetting);
            subscriptionSetting = settingFacade.update(subscriptionSetting);
            settingList.add(subscriptionSetting);

            subscriptionSetting = new SubscriptionSetting();
            subscriptionSetting.setValue(miniPop.getIp());
            serviceSetting = subscriptionFacade.createServiceSetting(47L, "Switch IP", ServiceSettingType.BROADBAND_SWITCH_IP, Providers.CITYNET, ServiceType.BROADBAND, "");
            subscriptionSetting.setProperties(serviceSetting);
            settingFacade.save(subscriptionSetting);
            subscriptionSetting = settingFacade.update(subscriptionSetting);
            settingList.add(subscriptionSetting);

            selected.setSettings(settingList);
            selected = subscriptionFacade.update(selected);
        }
    }

    public boolean isCityNet() {
        return selected.getService().getProvider().getId() == Providers.CITYNET.getId() || selected.getService().getProvider().getId() == Providers.QUTU.getId();
    }

    public String paymentView() {
        if (selected == null) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Please select subscription first", "Please select subscription first"));
            return null;
        }
        String result = String.format("/pages/subscriber/ind/manualpayment.xhtml?subscriber=%d&amp;subscription=%d&amp;faces-redirect=true&amp;includeViewParams=true", selected.getSubscriber().getId(), selected.getId());
        return result;
    }

    public String manualActivation() {
        DateTimeFormatter frm = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
        systemLogger.success(
                SystemEvent.SUBSCRIPTION_STATUS_ACTIVE,
                selected,
                String.format("MANUAL ACTIVATION. Status=%s, biiledUpToDate=%s, expirationDate=%s, expirationDateWithGP=%s before activation attempt after payment.",
                        selected.getStatus(),
                        selected.getBilledUpToDate() != null ? selected.getBilledUpToDate().toString(frm) : null,
                        selected.getExpirationDate() != null ? selected.getExpirationDate().toString(frm) : null,
                        selected.getExpirationDateWithGracePeriod() != null ? selected.getExpirationDateWithGracePeriod().toString(frm) : null
                ));

        if (selected.getBillingModel().getPrinciple() == BillingPrinciple.CONTINUOUS) {

            if (selected.getStatus() == SubscriptionStatus.BLOCKED) {

                log.debug("CONTINUOUS. Status is BLOCKED and Balance is >= 0");
                DateTime estimatedNextExpirationDate = DateTime.now().plusMonths(1).withTime(23, 59, 59, 999);
                if (estimatedNextExpirationDate.getDayOfMonth() < DateTime.now().getDayOfMonth()) {
                    estimatedNextExpirationDate = estimatedNextExpirationDate.plusDays(1);
                }
                selected.setExpirationDate(estimatedNextExpirationDate);
                selected.setExpirationDateWithGracePeriod(selected.getExpirationDate());
                selected.setBilledUpToDate(selected.getExpirationDate());
                selected.setStatus(SubscriptionStatus.ACTIVE);
                selected.setLastStatusChangeDate(DateTime.now());
                selected = subscriptionFacade.update(selected);

                if (provisioner.openService(selected, selected.getExpirationDateWithGracePeriod())) {
                    systemLogger.success(
                            SystemEvent.SUBSCRIPTION_PROLONGED,
                            selected,
                            String.format("MANUAL ACTIVATION. selected id=%d switched from blocked to active, prolonged on radius db", selected.getId()));
                }
            }

        } else if (selected.getStatus() == SubscriptionStatus.BLOCKED) {

            log.debug("GRACE. Status is BLOCKED and Balance is >= 0");
            if (selected.getBilledUpToDate().isAfterNow()) {
                selected.setExpirationDate(selected.getBilledUpToDate());
                selected.setExpirationDateWithGracePeriod(selected.getBilledUpToDate().plusDays(selected.getBillingModel().getGracePeriodInDays()));
                if (selected.getBillingModel().getPrinciple() == BillingPrinciple.GRACE_MONTH) {
                    selected.setExpirationDateWithGracePeriod(selected.getBilledUpToDate().plusMonths(1));
                }
            } else {
                DateTime estimatedNextExpirationDate = selected.getBilledUpToDate().plusMonths(1).withTime(23, 59, 59, 999);
                if (estimatedNextExpirationDate.getDayOfMonth() < selected.getBilledUpToDate().getDayOfMonth()) {
                    estimatedNextExpirationDate = estimatedNextExpirationDate.plusDays(1);
                }
                selected.setExpirationDate(estimatedNextExpirationDate);
                selected.setExpirationDateWithGracePeriod(selected.getExpirationDate().plusDays(selected.getBillingModel().getGracePeriodInDays()));
                if (selected.getBillingModel().getPrinciple() == BillingPrinciple.GRACE_MONTH) {
                    selected.setExpirationDateWithGracePeriod(selected.getExpirationDate().plusMonths(1));
                }
                selected.setBilledUpToDate(selected.getExpirationDate());
            }
            selected.setStatus(SubscriptionStatus.ACTIVE);
            selected.setLastStatusChangeDate(DateTime.now());
            selected = subscriptionFacade.update(selected);

            if (provisioner.openService(selected, selected.getExpirationDateWithGracePeriod())) {
                systemLogger.success(
                        SystemEvent.SUBSCRIPTION_PROLONGED,
                        selected,
                        String.format("MANUAL ACTIVATION. selected id=%d switched from blocked to active, prolonged on radius db", selected.getId()));
            }
        } else if (selected.getStatus() == SubscriptionStatus.PARTIALLY_BLOCKED) {
            log.debug("GRACE. Status is PARTIALLY_BLOCKED and Balance is >= 0");
            if (!selected.getExpirationDate().isAfterNow()) {
                DateTime estimatedNextBillDate = selected.getExpirationDate().plusMonths(1).withTime(23, 59, 59, 999);
                if (estimatedNextBillDate.getDayOfMonth() < selected.getExpirationDate().getDayOfMonth()) {
                    estimatedNextBillDate = estimatedNextBillDate.plusDays(1);
                }
                selected.setBilledUpToDate(estimatedNextBillDate);
                selected.setExpirationDate(selected.getBilledUpToDate());
                selected.setExpirationDateWithGracePeriod(selected.getExpirationDate().plusDays(selected.getBillingModel().getGracePeriodInDays()));
                if (selected.getBillingModel().getPrinciple() == BillingPrinciple.GRACE_MONTH) {
                    selected.setExpirationDateWithGracePeriod(selected.getExpirationDate().plusMonths(1));
                }
            }

            selected.setStatus(SubscriptionStatus.ACTIVE);
            selected.setLastStatusChangeDate(DateTime.now());
            selected = subscriptionFacade.update(selected);


            if (provisioner.openService(selected, selected.getExpirationDateWithGracePeriod())) {
                systemLogger.success(
                        SystemEvent.SUBSCRIPTION_PROLONGED,
                        subscription,
                        String.format("subscription id=%d switched from partial blocked to active, prolonged on radius db", selected.getId()));
            }

//            if (provisioner.openService(selected, selected.getExpirationDateWithGracePeriod())) {
//                systemLogger.success(
//                        SystemEvent.SUBSCRIPTION_PROLONGED,
//                        subscription,
//                        String.format("subscription id=%d switched from partial blocked to active, prolonged on radius db", selected.getId()));
//            }

        }

        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO,
                        "Subscription updated successfully", "Status switched to active state"));
        systemLogger.success(
                SystemEvent.SUBSCRIPTION_STATUS_ACTIVE,
                selected,
                String.format("MANUAL ACTIVATION. Status=%s, biiledUpToDate=%s, expirationDate=%s, expirationDateWithGP=%s successfully activated",
                        selected.getStatus(),
                        selected.getBilledUpToDate() != null ? selected.getBilledUpToDate().toString(frm) : null,
                        selected.getExpirationDate() != null ? selected.getExpirationDate().toString(frm) : null,
                        selected.getExpirationDateWithGracePeriod() != null ? selected.getExpirationDateWithGracePeriod().toString(frm) : null
                ));


        String outcome = new StringBuilder(SUBSCRIPTION_PATH)
                .append(String.format("view/view_%d.xhtml?subscriber=", selected.getService().getProvider().getId())).append(selected.getId())
                .append("&amp;subscription=").append(selected.getId())
                .append("&amp;faces-redirect=true&amp;includeViewParams=true").toString();

        log.info("Subscription created successfully. Subscription=" + selected);
        Log.debug("outcome data: " + outcome);
        //inMemory.addSubscription(subscription);

        subscription = null;
        selectedSubscriber = null;
        balance = null;
        subscriberSelectList = null;
        filteredSubscriberList = null;
        subscriberLazyTableModel = null;

        equipmentPersistenceFacade.clearFilters();
        minipopFacade.clearFilters();
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Subscription created successfully", "Subscription created successfully"));
        installFee = 0;
        campaignList = null;
        return outcome;

    }

    public StaticIPType getNormalCharged() {
        return StaticIPType.NORMAL_CHARGED;
    }

    public StaticIPType getCommendantFree() {
        return StaticIPType.COMMANDANT_FREE;
    }


// ---- CONVEGENCE -----

    public String msisdn;

    public String getMsisdn() {
        try {
            return msisdn == null ? null : msisdn.substring(msisdn.length() - 9);
        } catch (Exception e) {
            return msisdn;
        }
    }

    public void setMsisdn(String msisdn) {
        this.msisdn = msisdn;
    }


    // --- Usage ---

    Usage usage;
    Date usageStartDate;
    Date usageEndDate;
    String usageDownload;
    String usageUpload;

    public void getUsage() {

        log.debug("getUsage");
        log.debug("usageStartDate: " + usageStartDate);
        log.debug("usageEndDate: " + usageEndDate);

        try {
            usage = provisioningFactory.getProvisioningEngine(selected).getUsage(selected, usageStartDate, usageEndDate);
            usageDownload = usage.getDownloadString();
            usageUpload = usage.getUploadString();

            log.debug("usageDownload: " + usageDownload);
            log.debug("usageUpload:" + usageUpload);

        } catch (ProvisionerNotFoundException e) {
            e.printStackTrace();
        }
    }

    public String getUsageUpload() {
        return usageUpload;
    }

    public void setUsageUpload(String usageUpload) {
        this.usageUpload = usageUpload;
    }

    public String getUsageDownload() {
        return usageDownload;
    }

    public void setUsageDownload(String usageDownload) {
        this.usageDownload = usageDownload;
    }

    public void setUsage(Usage usage) {
        this.usage = usage;
    }

    public Date getUsageEndDate() {
        return usageEndDate;
    }

    public void setUsageEndDate(Date usageEndDate) {
        this.usageEndDate = usageEndDate;
    }

    public Date getUsageStartDate() {
        return usageStartDate;
    }

    public void setUsageStartDate(Date usageStartDate) {
        this.usageStartDate = usageStartDate;
    }

    public double getInstallFee() {
        return installFee;
    }

    public void setInstallFee(double installFee) {
        this.installFee = installFee;
    }

    public int getStaticIpCount() {
        if (selected != null) {
            selectedStatus = selected.getStatus();
            if (selected.getVasList() != null) {
                for (SubscriptionVAS sv : selected.getVasList()) {
                    try {
                        if (sv.getVas().isStaticIp()) {
                            staticIpCount = Double.valueOf(sv.getCount()).intValue();
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace(System.err);
                    }
                }
            }
        }
        return staticIpCount <= 0 ? 1 : staticIpCount;
    }

    public String getStaticIpCountAsText() {
        return " ( " + getStaticIpCount() + " )";
    }

    public void setStaticIpCount(int staticIpCount) {
        this.staticIpCount = staticIpCount;
    }

    public DateTime getVasExpDate() {
        if (vasExpDate == null) {
            vasExpDate = selectedSbnVAS.getExpirationDate();
        }
        return vasExpDate;
    }

    public void setVasExpDate(DateTime vasExpDate) {
        this.vasExpDate = vasExpDate;
    }


    public String qutuProvider;
    public List<String> qutuProviderList = Arrays.asList("CityNet", "Azertelecom");

    public String getQutuProvider() {
        return qutuProvider;
    }

    public void setQutuProvider(String qutuProvider) {
        this.qutuProvider = qutuProvider;
    }

    public List<String> getQutuProviderList() {
        return qutuProviderList;
    }

    public void setQutuProviderList(List<String> qutuProviderList) {
        this.qutuProviderList = qutuProviderList;
    }
}

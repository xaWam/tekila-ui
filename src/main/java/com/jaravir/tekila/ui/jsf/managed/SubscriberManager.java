package com.jaravir.tekila.ui.jsf.managed;

import com.jaravir.tekila.base.auth.entity.SessionInfo;
import com.jaravir.tekila.base.auth.persistence.User;
import com.jaravir.tekila.base.auth.persistence.manager.SessionFacade;
import com.jaravir.tekila.base.auth.persistence.manager.UserPersistenceFacade;
import com.jaravir.tekila.base.entity.Language;
import com.jaravir.tekila.base.entity.Util;
import com.jaravir.tekila.base.filter.MatchingOperation;
import com.jaravir.tekila.base.persistence.facade.AbstractPersistenceFacade;
import com.jaravir.tekila.base.persistence.manager.BillingSettingsManager;
import com.jaravir.tekila.engines.EngineFactory;
import com.jaravir.tekila.engines.ProvisioningEngine;
import com.jaravir.tekila.extern.tt.TroubleTicket;
import com.jaravir.tekila.extern.tt.manager.TroubleTicketPersistenceFacade;
import com.jaravir.tekila.inMemory.InMemoryManager;
import com.jaravir.tekila.module.accounting.InvoiceState;
import com.jaravir.tekila.module.accounting.entity.*;
import com.jaravir.tekila.module.accounting.listener.BeforeBillingListener;
import com.jaravir.tekila.module.accounting.manager.*;
import com.jaravir.tekila.module.accounting.periodic.BillingManager;
import com.jaravir.tekila.module.campaign.*;
import com.jaravir.tekila.module.payment.PaymentOptionsPersistenceFacade;
import com.jaravir.tekila.module.periiodic.JobPersistenceFacade;
import com.jaravir.tekila.module.queue.PersistentQueueManager;
import com.jaravir.tekila.module.service.ServiceSettingType;
import com.jaravir.tekila.module.subscription.persistence.entity.*;
import com.jaravir.tekila.module.subscription.persistence.management.SubscriberDetailsPeristenceFacade;
import com.jaravir.tekila.module.subscription.persistence.management.SubscriberPersistenceFacade;
import com.jaravir.tekila.module.subscription.persistence.management.SubscriptionPersistenceFacade;
import com.jaravir.tekila.module.subscription.persistence.management.util.CompensationDetails;
import com.jaravir.tekila.module.system.SystemEvent;
import com.jaravir.tekila.module.system.log.SystemLogger;
import com.jaravir.tekila.module.web.service.exception.NoInvoiceFoundException;
import com.jaravir.tekila.module.web.service.exception.NoSuchSubscriberException;
import com.jaravir.tekila.module.web.service.exception.NoSuchSubscriptionException;
import com.jaravir.tekila.module.web.service.provider.BillingServiceProvider;
import com.jaravir.tekila.provision.broadband.devices.MiniPop;
import com.jaravir.tekila.provision.broadband.devices.manager.MiniPopPersistenceFacade;
import com.jaravir.tekila.provision.broadband.devices.manager.Providers;
import com.jaravir.tekila.provision.exception.ProvisionerNotFoundException;
import com.jaravir.tekila.ui.model.LazyTableModel;
import com.jaravir.tekila.ui.model.LazyTableModelDynamic;

import javax.faces.event.ActionEvent;
import java.io.IOException;
import java.io.Serializable;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.faces.validator.ValidatorException;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.primefaces.component.commandbutton.CommandButton;
import org.primefaces.component.menuitem.UIMenuItem;
import org.primefaces.context.RequestContext;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.LazyDataModel;

@ManagedBean(name = "subscriber")
public class SubscriberManager implements Serializable {

    private static final long serialVersionUID = 5445848024445955066L;
    private final static String INDIVIDUAL_PATH = "/pages/subscriber/ind/";
    private final static String CORPORATE_PATH = "/pages/subscriber/corp/";
    private static final String SESSION_INDIVIDUAL_SELECTED = "subscriber.indv.selected";

    private transient Logger log = Logger.getLogger(SubscriberManager.class);

    private SubscriberType typeFromIndex;
    private SubscriberDetails sub;
    @EJB
    private SubscriberDetailsPeristenceFacade subFacade;
    @EJB
    private SubscriberPersistenceFacade subscriberFacade;
    @EJB
    private BillingServiceProvider billingGateway;
    @EJB
    private AccountingTransactionPersistenceFacade accTransFacade;
    @EJB
    private SystemLogger systemLogger;
    @EJB
    private CampaignPersistenceFacade campaignFacade;
    @EJB
    private CampaignJoinerBean campaignJoinerBean;
    @EJB
    private CampaignRegisterPersistenceFacade campaignRegisterFacade;
    @EJB
    InMemoryManager inMemory;
    @EJB
    private PersistentQueueManager queueManager;
    @EJB
    private PaymentOptionsPersistenceFacade paymentOptionsPersistenceFacade;
    @EJB
    private BankPersistenceFacade bankPersistenceFacade;
    @EJB
    private MiniPopPersistenceFacade miniPopPersistenceFacade;
    @EJB
    private JobPersistenceFacade jobPersistenceFacade;
    //@EJB
    //private ChequeSequence

    private CompensationDetails compensationDetails;
    private String phoneMobileCode = "994";
    private String phoneMobileBody;
    private final static Pattern pattern = Pattern.compile("[0-9]+");
    //private List<SubscriberDetails> subscribers;
    private LazyDataModel<SubscriberDetails> subscribers;
    private List<SubscriberDetails> filteredSubscribers;
    private SubscriberDetails selected;
    private long subscriberId;
    private LazyTableModel<Charge> chargeLazyModel;
    private Charge singleCharge;
    private long singleChargeId;
    private Subscriber chargedSubscriber;
    private Payment payment;
    private String manats;
    private String cents;
    private LazyTableModel<Payment> paymentLazyModel;
    private Payment selectedPayment;
    private Payment singlePayment;
    private boolean isManagedByLifeCycle = true;
    private SubscriberLifeCycleType subscriberLifeCycleType = SubscriberLifeCycleType.PREPAID;
    private List<SelectItem> lifeCycleTypeSelectItems;
    private long singlePaymentID;
    private long chequeNum;
    private String campaignId;
    private List<SelectItem> addedCampaignList;
    private Date campaignAddDate;
    private Long subscriptionId;
    private List<Subscription> chargedSbnList;

    public List<Subscription> getChargedSbnList() {
        if (chargedSbnList == null) {
            if (subscriptionId == null)
                chargedSbnList = chargedSubscriber.getSubscriptions();
            else {
                chargedSbnList = new ArrayList<>();
                chargedSbnList.add(subscriptionFacade.find(subscriptionId));
            }
        }
        return chargedSbnList;
    }

    public void setChargedSbnList(List<Subscription> chargedSbnList) {
        this.chargedSbnList = chargedSbnList;
    }

    public Long getSubscriptionId() {
        return subscriptionId;
    }

    public void setSubscriptionId(Long subscriptionId) {
        this.subscriptionId = subscriptionId;
    }

    public List<SelectItem> getAddedCampaignList() {
        return addedCampaignList;
    }

    public void setAddedCampaignList(List<SelectItem> addedCampaignList) {
        this.addedCampaignList = addedCampaignList;
    }

    public Date getCampaignAddDate() {
        if (campaignAddDate == null) {
            campaignAddDate = DateTime.now().toDate();
        }
        return campaignAddDate;
    }

    public void setCampaignAddDate(Date campaignAddDate) {
        this.campaignAddDate = campaignAddDate;
    }

    public String getCampaignId() {
        return campaignId;
    }

    public void setCampaignId(String campaignId) {
        this.campaignId = campaignId;
    }

    public void selectSubscriptionForPayment(SelectEvent event) {
        Subscription subscription = ((Subscription) event.getObject());

        List<Campaign> campaigns = campaignFacade.findBySubscription(subscription);
        List<SelectItem> addedCampaignListNew = new ArrayList<>();
        for (final Campaign campaign : campaigns) {
            if (campaign.isActivateOnManualPaymentOnly()) {
                addedCampaignListNew.add(new SelectItem(campaign.getId(), campaign.getName()));
            }
        }
        addedCampaignList = addedCampaignListNew;
    }

    @EJB
    private ChequeSequencePersistenceFacade chequeFacade;

    @EJB
    private PaymentPersistenceFacade paymentFacade;

    @EJB
    private ChargePersistenceFacade chargeFacade;
    @EJB
    private RefundPersistenceFacade refundFacade;

    private LazyTableModel<Refund> refundLazyModel;
    private Refund refund;

    private Subscription selectedSubscription;

    @EJB
    private UserPersistenceFacade userFacade;

    @EJB
    private TaxCategoryPeristenceFacade taxFacade;
    @EJB
    private InvoicePersistenceFacade invoiceFacade;
    //@EJB private AzertelekomProvisioner provisioner;
    @EJB
    private EngineFactory engineFactory;
    @EJB
    private SubscriptionPersistenceFacade subscriptionFacade;
    @EJB
    private BillingSettingsManager billingSettings;
    private LazyDataModel<Invoice> invoiceList;
    private List<Invoice> filteredInvoiceList;
    private Invoice selectedInvoice;
    private Long invoiceID;

    private List<SelectItem> taxCategorySelectList;
    private Long selectedTaxCategory;
    private List<SelectItem> langList;

    private final static String enumSubTypeIndv = SubscriberType.INDV.toString();
    private final static String enumSubTypeCorp = SubscriberType.CORP.toString();

    private boolean isCorporate;

    private LazyDataModel<Subscriber> subscriberList;
    private Subscriber selectedSubscriber;
    private List<Compensation> compensationList;

    private Map<String, Object> paymentOptions;
    private String selectedPaymentMethod;

    private String selectedBank;
    private Map<String, Object> bankList;

    public long getChequeNum() {
        return chequeNum;
    }

    public void setChequeNum(long chequeNum) {
        this.chequeNum = chequeNum;
    }

    public String getSelectedBank() {
        return selectedBank;
    }

    public void setSelectedBank(String selectedBank) {
        this.selectedBank = selectedBank;
    }

    public Map<String, Object> getBankList() {
        if (bankList == null) {
            bankList = new HashMap<>();
            for (Bank bank : bankPersistenceFacade.findAll()) {
                bankList.put(bank.getName(), bank.getFullName());
            }
        }
        return bankList;
    }

    public void setBankList(Map<String, Object> bankList) {
        this.bankList = bankList;
    }

    public LazyDataModel<Subscriber> getSubscriberList() {
        if (subscriberList == null) {
            subscriberFacade.clearFilters();
            subscriberList = new LazyTableModel<>(subscriberFacade);
        }
        return subscriberList;
    }

    public void setSubscriberList(LazyDataModel<Subscriber> subscriberList) {
        this.subscriberList = subscriberList;
    }

    public Subscriber getSelectedSubscriber() {
        return selectedSubscriber;
    }

    public void setSelectedSubscriber(Subscriber selectedSubscriber) {
        this.selectedSubscriber = selectedSubscriber;
    }

    private LazyDataModel<Subscription> subscriptionList;
    private List<Subscription> filteredSubscriptionList;
    private Subscription targetSubscription;
    private CompanyType companyType = CompanyType.LOCAL;

    //balance transfer
    private String sbID;
    private String sbnContract;
    private String subscriberName;
    private String subscriberMiddleName;
    private String subscriberSurname;
    private String sbnService;

    //invoices
    private InvoiceState searchInvoiceStatus;
    private List<SelectItem> searchInvoiceStatusList;

    private LazyTableModel<Invoice> searchInvoiceList;

    private SubscriberFunctionalCategory funcCat = SubscriberFunctionalCategory.COMMERCIAL;
    private List<SelectItem> fnCatSelectList;

    public String getEnumSubTypeIndv() {
        return enumSubTypeIndv;
    }

    public void setEnumSubTypeIndv(String enumSubTypeIndv) {

    }

    public String getEnumSubTypeCorp() {
        return enumSubTypeCorp;
    }

    public void setEnumSubTypeCorp(String enumSubTypeCorp) {

    }

    //components
    private SelectItem[] genderSelectList = {
            new SelectItem(Gender.MALE, "Male"),
            new SelectItem(Gender.FEMALE, "Female")
    };

    private SelectItem[] companyTypeSelectList = {
            new SelectItem(CompanyType.LOCAL, "Local"),
            new SelectItem(CompanyType.FOREIGN, "Foreign")
    };

    private transient UIInput subType;

    @PostConstruct
    public void init() {
        this.sub = new SubscriberDetails();
    }

    public SelectItem[] getGenderSelectList() {
        return genderSelectList;
    }

    public SelectItem[] getCompanyTypeSelectList() {
        return companyTypeSelectList;
    }

    public void setCompanyTypeSelectList(SelectItem[] companyTypeSelectList) {
        this.companyTypeSelectList = companyTypeSelectList;
    }

    public void setGenderSelectList(SelectItem[] genderSelectList) {
        this.genderSelectList = genderSelectList;
    }

    public LazyDataModel<SubscriberDetails> getSubscribers() {
        if (subscribers == null) {
            loadSubscribers();
        }
        return subscribers;
    }

    public void setSubscribers(LazyDataModel<SubscriberDetails> subscribers) {
        this.subscribers = subscribers;
    }

    public List<SubscriberDetails> getFilteredSubscribers() {
        return filteredSubscribers;
    }

    public void setFilteredSubscribers(List<SubscriberDetails> filteredSubscribers) {
        this.filteredSubscribers = filteredSubscribers;
    }

    public SubscriberDetails getSub() {
        return sub;
    }

    public void setSub(SubscriberDetails sub) {
        this.sub = sub;
    }

    public SubscriberDetails getSelected() {
        //log.debug("getSelected: subscriberID=" + subscriberId);
        if (selected == null && subscriberId != 0) {
            //HttpSession session = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);
            //Long pk = (Long) session.getAttribute(SESSION_INDIVIDUAL_SELECTED);
            long pk = subscriberId;
            if (pk != 0) {
                this.selected = this.subscriberFacade.findForceRefresh(pk).getDetails();
                //session.removeAttribute(SESSION_INDIVIDUAL_SELECTED);

                if (selected != null && selected.getPhoneMobile() != null && !selected.getPhoneMobile().isEmpty()) {
                    if (selected.getPhoneMobile().startsWith("994")) {
                        phoneMobileBody = selected.getPhoneMobile().substring(3);
                    } else if (selected.getPhoneMobile().startsWith("+994")) {
                        phoneMobileBody = selected.getPhoneMobile().substring(4);
                    } else if (selected.getPhoneMobile().startsWith("0")) {
                        phoneMobileBody = selected.getPhoneMobile().substring(1);
                    }
                }

                Logger log = Logger.getLogger(this.getClass());
                log.debug("Selected: " + selected);
            }
        }
        return selected;
    }

    public void setSelected(SubscriberDetails selected) {
        this.selected = selected;
    }

    public UIInput getSubType() {
        return subType;
    }

    public void setSubType(UIInput subType) {
        this.subType = subType;
    }

    public Subscription getSelectedSubscription() {
        return selectedSubscription;
    }

    public void setSelectedSubscription(Subscription selectedSubscription) {
        this.selectedSubscription = selectedSubscription;
    }

    @Override
    public String toString() {
        return this.sub.toString();
    }

    public void checkNationalCode(FacesContext ctx, UIComponent component, Object objValue) throws ValidatorException {
        String value = objValue.toString();
        String message = null;
        if (value == null || value.isEmpty()) {
            message = "National code is required";
        } else {
            Matcher matcher = pattern.matcher(value);
            if (!matcher.matches()) {
                message = "National code should contain only digits";
            }
        }

        if (message != null) {
            throw new ValidatorException(
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, message, message)
            );
        }
    }

    public void checkPhoneNumber(FacesContext ctx, UIComponent component, Object objValue) throws ValidatorException {
        String value = objValue.toString().trim();
        String message = null;
        if (value == null || value.isEmpty()) {
            message = "Phone number is required";
        } else {
            Matcher matcher = pattern.matcher(value);
            if (!matcher.matches()) {
                message = "Phone number should contain only digits";
            } else if (value.startsWith("0")) {
                message = "Phone number should not start with 0";
            }
        }

        if (message != null) {
            throw new ValidatorException(
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, message, message)
            );
        }
    }

    public String create() {
        sub.setPhoneMobile(phoneMobileCode + phoneMobileBody);
        sub.setType(SubscriberType.INDV);

        log.debug("Type: " + sub.getType());
        //this.sub.setType(SubscriberType.valueOf((String)subType.getValue()));
        //log.debug(this.sub.getType().toString());
        try {
            SubscriberDetails dupSubDetails = null;

            dupSubDetails = subFacade.getDuplicatePincode(sub, SubscriberType.INDV);
            if (dupSubDetails != null) {
                String path = FacesContext.getCurrentInstance().getExternalContext().getRequestContextPath();
                String message = "Error creating subscriber: <a style='color: blue; font-size: bold;' href='"
                        + path + "/pages/subscriber/view.xhtml?subscriber=" + dupSubDetails.getSubscriber().getId() + "'>Subscriber</a> already exists with this pincode. Please add subscriptions to him";
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, message, message));
                return null;
            }
            String userName = FacesContext.getCurrentInstance().getExternalContext().getUserPrincipal().getName();
            User user = userFacade.findByUserName(userName);

            subFacade.createFromForm(this.sub, funcCat, isManagedByLifeCycle, subscriberLifeCycleType, taxFacade.find(selectedTaxCategory), user);
            log.debug("Path is: " + getPath(sub));

            return String.format("/pages/subscriber/view.xhtml?subscriber=%d&amp;faces-redirect=true&amp;includeViewParams=true", sub.getSubscriber().getId());
        } catch (Exception ex) {
            log.debug("Cannot create subscriber", ex);
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error creating subscriber", "Error creating subscriber"));
            return null;
        }
    }

    public String createCorp() {
        sub.setPhoneMobile(phoneMobileCode + phoneMobileBody);

        sub.setType(SubscriberType.CORP);
        sub.setCompanyType(companyType);

        log.debug("Type: " + sub.getType());
        //this.sub.setType(SubscriberType.valueOf((String)subType.getValue()));
        //log.debug(this.sub.getType().toString());
        try {
            SubscriberDetails dupSubDetails = subFacade.getDuplicatePassportNumber(sub.getPassportNumber(), SubscriberType.CORP);
            if (dupSubDetails != null) {
                String path = FacesContext.getCurrentInstance().getExternalContext().getRequestContextPath();
                String message = "Error creating subscriber: <a style='color: blue; font-size: bold;' href='"
                        + path + "/pages/subscriber/view.xhtml?subscriber=" + dupSubDetails.getSubscriber().getId() + "'>Subscriber</a> already exists with this Registration / Tax ID. Please add subscriptions to him";
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, message, message));
                return null;
            }

            String userName = FacesContext.getCurrentInstance().getExternalContext().getUserPrincipal().getName();
            User user = userFacade.findByUserName(userName);
            subFacade.createFromForm(this.sub, funcCat, isManagedByLifeCycle, subscriberLifeCycleType, taxFacade.find(selectedTaxCategory), user);
            log.debug("Path is: " + getPath(sub));

            return String.format("/pages/subscriber/view.xhtml?subscriber=%d&amp;faces-redirect=true&amp;includeViewParams=true", sub.getSubscriber().getId());
        } catch (Exception ex) {
            log.debug("Cannot create subscriber", ex);
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error creating subscriber", "Error creating subscriber"));
            return null;
        }
    }

    private String getPath(SubscriberDetails det) {
        return det.getType() == SubscriberType.INDV ? INDIVIDUAL_PATH : CORPORATE_PATH;
    }

    public SubscriberType getTypeFromIndex() {
        return typeFromIndex;
    }

    public void setTypeFromIndex(SubscriberType typeFromIndex) {
        this.typeFromIndex = typeFromIndex;
    }

    public long getSubscriberId() {
        return subscriberId;
    }

    public void setSubscriberId(long subscriberId) {
        this.subscriberId = subscriberId;
    }

    public String update() {
        try {
            if (phoneMobileCode != null && !phoneMobileCode.isEmpty()
                    && phoneMobileBody != null && !phoneMobileBody.isEmpty()) {
                selected.setPhoneMobile(phoneMobileCode + phoneMobileBody);
            }

            SubscriberDetails old = this.subFacade.find(selected.getId());
            //if (!old.equals(selected)) {
            selected.setType(old.getType());
            selected.getSubscriber().setTaxCategory(taxFacade.find(selectedTaxCategory));
            this.subFacade.update(selected);
            //inMemory.updateSubscriber(selected);
            //}
            //return getPath(selected)+"index";
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Update successfull", "Update successfull"));
            return null;
        } catch (Exception ex) {
            log.error("Cannot update subscriber: ", ex);
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error updating subscriber information", "Error updating subscriber information"));
            return null;
        }
    }

    public String show() {
        //	HttpSession session = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);
        //	session.setAttribute(SESSION_INDIVIDUAL_SELECTED, Long.valueOf(this.selected.getId()));
        Logger log = Logger.getLogger(this.getClass());
        log.debug("SHOW - attribute: " + this.selected.getId());
        //return getPath(selected) + "view";
        return "/pages/subscriber/view?subscriber=" + this.selected.getSubscriber().getId() + "&amp;faces-redirect=true&amp;includeViewParams=true";
    }

    private void loadSubscribers() {
        Map<String, Object> requestMap = FacesContext.getCurrentInstance().getExternalContext().getRequestMap();
        String viewID = FacesContext.getCurrentInstance().getViewRoot().getViewId();
        SubscriberType type = (viewID.indexOf("corp") > 0) ? SubscriberType.CORP : SubscriberType.INDV;

        log.debug(String.format("type: %s\n", type));
        subFacade.setType(type);
        subscribers = new LazyTableModel<>(subFacade);
        log.debug("Subscribers loaded: " + this.subscribers);

    }

    public String viewSubscription() {
        if (selectedSubscription == null) {
            return null;
        }

        //HttpSession session = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);
        //session.setAttribute("subscription.selected", Long.valueOf(selectedSubscription.getId()));
        return String.format("/pages/subscription/view/view_%d?subscriber=" + subscriberId + "&amp;subscription=" + selectedSubscription.getId() + "&amp;faces-redirect=true&amp;includeViewParams=true", selectedSubscription.getService().getProvider().getId());
    }

    public String getUserName(Long user_id) {
        User user = userFacade.find(user_id);
        return user != null ? user.getUserName() : "";
    }

    public LazyTableModel<Charge> getChargeLazyModel() {
        if (chargeLazyModel == null && subscriberId != 0) {
            chargeFacade.clearFilters();
            chargeFacade.addFilter(ChargePersistenceFacade.Filter.SUBSCRIBER_ID, subscriberId);
            chargeLazyModel = new LazyTableModel<>(chargeFacade);
        }
        return chargeLazyModel;
    }

    public void setChargeLazyModel(LazyTableModel<Charge> chargeLazyModel) {
        this.chargeLazyModel = chargeLazyModel;
    }

    public Charge getSingleCharge() {
        if (singleCharge == null && singleChargeId != 0) {
            /*HttpSession session = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);
             Long pk = (Long)session.getAttribute("selected_payment");*/

            singleCharge = chargeFacade.find(singleChargeId);
        }
        return singleCharge;
    }

    public void setSingleCharge(Charge singleCharge) {
        this.singleCharge = singleCharge;
    }

    public long getSingleChargeId() {
        return singleChargeId;
    }

    public void setSingleChargeId(long singleChargeId) {
        this.singleChargeId = singleChargeId;
    }

    public String viewCharge() {
        if (singleCharge == null) {
            return null;
        }

        /* HttpSession session = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);
         session.setAttribute("selected_payment", Long.valueOf(singlePayment.getId()));
         */
        return INDIVIDUAL_PATH + "charge?subscriber=" + subscriberId + "&amp;chargeID=" + singleCharge.getId() + "&amp;faces-redirect=true&amp;includeViewParams=true";
    }

    public Subscriber getChargedSubscriber() {
        if (chargedSubscriber == null && subscriberId != 0) {
            chargedSubscriber = subscriberFacade.find(subscriberId);
        }
        return chargedSubscriber;
    }

    public void setChargedSubscriber(Subscriber chargedSubscriber) {
        this.chargedSubscriber = chargedSubscriber;
    }

    public Payment getPayment() {
        if (payment == null) {
            payment = new Payment();
        }
        return payment;
    }

    public void setPayment(Payment payment) {
        this.payment = payment;
    }

    public String getManats() {
        return manats;
    }

    public void setManats(String manats) {
        this.manats = manats;
    }

    public String getCents() {
        return cents;
    }

    public void setCents(String cents) {
        this.cents = cents;
    }

    public String addCashPayment() {
        if (selectedSubscription == null) {
            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "No subscription selected", "Please select subscription to add cash payment");
            FacesContext.getCurrentInstance().addMessage(null, message);
            return null;
        }

        if (manats == null) {
            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Amount is required", "Amount is required");
            FacesContext.getCurrentInstance().addMessage(null, message);
            return null;
        }

        if (manats.contains(",") || manats.contains(" ")) {
            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Amount pattern is illegal", "Amount pattern is illegal");
            FacesContext.getCurrentInstance().addMessage(null, message);
            return null;
        }

        payment.setChequeID(String.valueOf(chequeNum));
        if (payment != null && paymentFacade.isChequeExists(payment.getChequeID())) {
            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Payment with this cheque already exists", "Payment with this cheque already exists");
            FacesContext.getCurrentInstance().addMessage(null, message);
            return null;
        }
        Double amount = null;

        try {
            amount = Double.parseDouble(manats);
        } catch (NumberFormatException ex) {
            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Amount pattern is illegal", "Amount pattern is illegal");
            FacesContext.getCurrentInstance().addMessage(null, message);
            return null;
        }

        if (amount <= 0) {
            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Not valid amount", "Not valid amount");
            FacesContext.getCurrentInstance().addMessage(null, message);
            return null;
        }

        if ((selectedSubscription.getService().getProvider().getId() == Providers.CITYNET.getId() ||
                selectedSubscription.getService().getProvider().getId() == Providers.UNINET.getId()) && amount > 10000.0) {
            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Amount cannot be more than 10000 AZN", "Amount cannot be more than 10000 AZN");
            FacesContext.getCurrentInstance().addMessage(null, message);
            return null;
        }

        if ((selectedSubscription.getService().getProvider().getId() != Providers.CITYNET.getId() &&
                selectedSubscription.getService().getProvider().getId() != Providers.UNINET.getId() &&
                selectedSubscription.getService().getProvider().getId() != Providers.AZERTELECOMPOST.getId()) && amount > 250.0) {
            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Amount cannot be more than 250 AZN", "Amount cannot be more than 250 AZN");
            FacesContext.getCurrentInstance().addMessage(null, message);
            return null;
        }

        Integer pCode = null;
        try {
            if (selectedPaymentMethod != null) {
                pCode = Integer.parseInt(selectedPaymentMethod);
            }
        } catch (IllegalArgumentException exc) {
        }

        if (pCode == null) {
            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Payment method should be selected", "Payment method should be selected");
            FacesContext.getCurrentInstance().addMessage(null, message);
            return null;
        }

        boolean activateSelectedCampaign = (campaignId != null && Long.parseLong(campaignId) != -1L);
        if (activateSelectedCampaign && new DateTime(campaignAddDate).isBefore(DateTime.now().plusMinutes(60))) {
            List<CampaignRegister> actives = campaignRegisterFacade.findNotProcessedBySubscription(selectedSubscription);
            if (actives != null) {
                for (CampaignRegister register : actives) {
                    if (Long.valueOf(register.getCampaign().getId()).equals(Long.parseLong(campaignId))) {
                        FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Same campaign cannot be active twice", "Same campaign cannot be active twice at the same time");
                        FacesContext.getCurrentInstance().addMessage(null, message);
                        return null;
                    }
                }
            }
        }

        //payment.setUser_id(userFacade.find((Long)session.getAttribute("userID")).getId());
        //payment.setUser_id((Long)session.getAttribute("userID"));

        //GatewayClient client = new GatewayClient();
        //client.callSettlePayment(payment.getSubscriber_id(), payment.getAccount().getId(), payment.getAmount(), payment.getId());
        String message = null;
        try {
            HttpSession session = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);
            User user = userFacade.find((Long) session.getAttribute("userID"));

            payment.setMethod(paymentOptionsPersistenceFacade.find(pCode));
            payment = paymentFacade.create(payment, selectedSubscription, amount, user);
            log.info(String.format("Payment after persist: %s. Settling....", payment));

            systemLogger.success(SystemEvent.PAYMENT, selectedSubscription,
                    String.format("payment id=%d, amount=%f, bank=%s, campaign id=%s",
                            payment.getId(), payment.getAmount(), selectedBank, (campaignId != null ? campaignId : "-1"))
            );

            boolean result = billingGateway.settlePayment(payment.getSubscriber_id(), payment.getAccount().getId(), payment.getAmount(), payment.getId());
            log.info(String.format("settlePayment result: %b", result));

            if (!result) {
                throw new Exception();
            } else {
                try {
                    campaignJoinerBean.tryAddToCampaign(selectedSubscription.getId(), payment.getId(), false);
                } catch (Exception ex) {
                    log.error(String.format("settlePayment: error while searching for campaign, subscription id = %d, payment id=%d",
                            selectedSubscription.getId(), payment.getId()), ex);
                }

                try {
                    if (activateSelectedCampaign) {
                        DateTime campaignAddStartTime = new DateTime(campaignAddDate);
                        if (campaignAddStartTime.isBefore(DateTime.now().plusMinutes(60))) {
                            campaignRegisterFacade.tryActivateCampaignOnManualPayment(selectedSubscription.getId(), Long.parseLong(campaignId), payment.getId());
                        } else {
                            jobPersistenceFacade.createCampaignAddJob(selectedSubscription, Long.parseLong(campaignId), payment.getId(), campaignAddStartTime);
                        }
                    } else {
                        campaignRegisterFacade.tryActivateCampaign(selectedSubscription.getId(), payment.getId());
                    }
                } catch (Exception ex) {
                    log.error(String.format("settlePayment: error while searching for campaigns awaiting activation, subscription id = %d, payment id=%d", selectedSubscription.getId(), payment.getId()), ex);
                }
                return INDIVIDUAL_PATH + "view_payments?subscriber=" + payment.getAccount().getSubscriber().getId() + "&amp;faces-redirect=true&amp;includeViewParams=true";
            }
            //if (result)
            //  return INDIVIDUAL_PATH + "view_payments?subscriber="+payment.getAccount().getSubscriber().getId()+"&amp;faces-redirect=true&amp;includeViewParams=true";

        } catch (NoSuchSubscriberException e) {
            message = String.format("Payment %d not settled - subscriber %d not found", payment.getId(), payment.getSubscriber_id());
            log.error(message, e);
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Cannot process payment - subscriber unknown", "Cannot process payment - subscriber unknown"));
            return null;
        } catch (NoSuchSubscriptionException e) {
            message = String.format("Payment %d not settled - subscription %d not found", payment.getId(), payment.getAccount().getId());
            log.error(message, e);
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Cannot process payment - subscription unknown", "Cannot process payment - subscription unknown"));
            return null;
        } catch (NoInvoiceFoundException e) {
            message = String.format("Payment %d not settled - invoice not found", payment.getId());
            log.error(message, e);
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Cannot process payment - invoice not found", "Cannot process payment - invoice not found"));
            return null;
        } catch (Exception e) {
            message = String.format("Payment %d not settled", payment.getId());
            log.error(message, e);
            systemLogger.error(SystemEvent.PAYMENT, selectedSubscription, "amount=" + manats);
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Cannot process payment", "Cannot process payment"));
            return null;
        }
        //FacesContext.getCurrentInstance().addMessage(null,  new FacesMessage(FacesMessage.SEVERITY_ERROR, "Payment was not settled on the balance", "Payment was not settled on the balance"));

        //return null;
    }

    public Map<String, Object> getPaymentOptions() {
        if (paymentOptions == null) {
            paymentOptions = new LinkedHashMap<>();
            List<PaymentOption> optionList = paymentOptionsPersistenceFacade.findAllAscending();
            for (PaymentOption option : optionList) {
                paymentOptions.put(option.getName(), option.getId());
            }
        }
        return paymentOptions;
    }

    public String getSelectedPaymentMethod() {
        return selectedPaymentMethod;
    }

    public void setSelectedPaymentMethod(String selectedPaymentMethod) {
        this.selectedPaymentMethod = selectedPaymentMethod;
    }

    public LazyTableModel<Payment> getPaymentLazyModel() {
        if (paymentLazyModel == null && subscriberId != 0) {
            //paymentFacade.setSubscriberID(subscriberId);
            paymentFacade.clearFilters();
            paymentFacade.addFilter(PaymentPersistenceFacade.Filter.SUBSCRIBER_ID, subscriberId);
            paymentFacade.addOrdering("lastUpdateDate", AbstractPersistenceFacade.Ordering.DESC);
            paymentLazyModel = new LazyTableModel<Payment>(paymentFacade);
        }
        log.debug("got subscriberID=" + subscriberId + ", model=" + paymentLazyModel);
        return paymentLazyModel;
    }

    public void setPaymentLazyModel(LazyTableModel<Payment> paymentLazyModel) {
        this.paymentLazyModel = paymentLazyModel;
    }

    public Payment getSelectedPayment() {
        return selectedPayment;
    }

    public void setSelectedPayment(Payment selectedPayment) {
        this.selectedPayment = selectedPayment;
    }

    public Payment getSinglePayment() {
        if (singlePayment == null && singlePaymentID != 0) {
            /*HttpSession session = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);
             Long pk = (Long)session.getAttribute("selected_payment");*/

            singlePayment = paymentFacade.find(singlePaymentID);
        }
        return singlePayment;
    }

    public String getCampaignName() {
        if (singlePayment == null && singlePaymentID != 0) {
            /*HttpSession session = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);
             Long pk = (Long)session.getAttribute("selected_payment");*/

            singlePayment = paymentFacade.find(singlePaymentID);
        }
        if (singlePayment != null
                && singlePayment.getCampaignId() != null
                && singlePayment.getCampaignId() != 0L) {
            Campaign campaign = campaignFacade.find(singlePayment.getCampaignId());
            return campaign.getName();
        }
        return "N/A";
    }

    public void setSinglePayment(Payment singlePayment) {
        this.singlePayment = singlePayment;
    }

    public List<SelectItem> getTaxCategorySelectList() {
        if (taxCategorySelectList == null) {

            List<TaxationCategory> catList = taxFacade.findAll();

            if (!catList.isEmpty()) {
                taxCategorySelectList = new ArrayList<SelectItem>();
                for (TaxationCategory cat : catList) {
                    taxCategorySelectList.add(new SelectItem(cat.getId(), cat.getName()));
                }
            }
        }
        return taxCategorySelectList;
    }

    public void setTaxCategorySelectList(List<SelectItem> taxCategorySelectList) {
        this.taxCategorySelectList = taxCategorySelectList;
    }

    public List<SelectItem> getTaxCategorySelectListForView() {
        if (taxCategorySelectList == null && selected != null) {
            TaxationCategory taxCat = selected.getSubscriber().getTaxCategory();
            selectedTaxCategory = taxCat.getId();
            List<TaxationCategory> catList = taxFacade.findAllNotIn(taxCat.getId());

            if (!catList.isEmpty()) {
                taxCategorySelectList = new ArrayList<SelectItem>();
                taxCategorySelectList.add(new SelectItem(taxCat.getId(), taxCat.getName()));
                for (TaxationCategory cat : catList) {
                    taxCategorySelectList.add(new SelectItem(cat.getId(), cat.getName()));
                }
            }
        }
        return taxCategorySelectList;
    }

    public void setTaxCategorySelectListForView(List<SelectItem> taxCategorySelectList) {
        this.taxCategorySelectList = taxCategorySelectList;
    }

    public Long getSelectedTaxCategory() {
        return selectedTaxCategory;
    }

    public void setSelectedTaxCategory(Long selectedTaxCategory) {
        this.selectedTaxCategory = selectedTaxCategory;
    }

    public String viewPayment() {
        if (singlePayment == null) {
            return null;
        }

        /* HttpSession session = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);
         session.setAttribute("selected_payment", Long.valueOf(singlePayment.getId()));
         */
        return INDIVIDUAL_PATH + "payment?subscriber=" + subscriberId + "&amp;paymentID=" + singlePayment.getId() + "&amp;faces-redirect=true&amp;includeViewParams=true";
    }

    public String returnToSubscriber() {
        if (subscriberId != 0) {
            Subscriber subscr = subscriberFacade.find(subscriberId);
            if (subscr == null) {
                return null;
            }

            this.selected = subscr.getDetails();
            Logger log = Logger.getLogger(this.getClass());
            log.debug("Return to: " + this.selected);
            return show();
        }
        return null;
    }

    public boolean isManagedByLifeCycle() {
        return isManagedByLifeCycle;
    }

    public void setManagedByLifeCycle(boolean isManagedByLifeCycle) {
        this.isManagedByLifeCycle = isManagedByLifeCycle;
    }

    public SubscriberLifeCycleType getSubscriberLifeCycleType() {
        return subscriberLifeCycleType;
    }

    public void setSubscriberLifeCycleType(SubscriberLifeCycleType subscriberLifeCycleType) {
        this.subscriberLifeCycleType = subscriberLifeCycleType;
    }

    public List<SelectItem> getLifeCycleTypeSelectItems() {
        if (lifeCycleTypeSelectItems == null) {
            lifeCycleTypeSelectItems = new ArrayList<SelectItem>();
            for (SubscriberLifeCycleType type : SubscriberLifeCycleType.values()) {
                lifeCycleTypeSelectItems.add(new SelectItem(type, type.name()));
            }
        }
        return lifeCycleTypeSelectItems;
    }

    public void setLifeCycleTypeSelectItems(List<SelectItem> lifeCycleTypeSelectItems) {
        this.lifeCycleTypeSelectItems = lifeCycleTypeSelectItems;
    }

    public Long getSubscriberIdForRedirect() {
        log.debug("redirect to: " + ((singlePayment.getAccount() != null) ? singlePayment.getAccount().getSubscriber().getId() : singlePayment.getSubscriber_id()));
        if (singlePayment != null) {

            return (singlePayment.getAccount() != null) ? singlePayment.getAccount().getSubscriber().getId() : singlePayment.getSubscriber_id();
        }
        return null;
    }

    public LazyTableModel<Refund> getRefundLazyModel() {
        if (refundLazyModel == null && subscriberId != 0) {
            refundFacade.setSubscriberId(subscriberId);
            refundLazyModel = new LazyTableModel<Refund>(refundFacade);
        }
        return refundLazyModel;
    }

    public void setRefundLazyModel(LazyTableModel<Refund> refundLazyModel) {
        this.refundLazyModel = refundLazyModel;
    }

    public Refund getRefund() {
        if (refund == null) {
            refund = new Refund();
        }
        return refund;
    }

    public void setRefund(Refund ref) {
        refund = ref;
    }

    public long getSinglePaymentID() {
        return singlePaymentID;
    }

    public void setSinglePaymentID(long singlePaymentID) {
        this.singlePaymentID = singlePaymentID;
    }

    public String addRefund() {
        if (selectedSubscription == null) {
            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "No subscription selected", "Please select subscription to add cash payment");
            FacesContext.getCurrentInstance().addMessage(null, message);
            return null;
        }
        try {
            HttpSession session = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);
            long amount = Util.parseAmountFromStringIntoLong(manats, cents);
            accTransFacade.refund(refund, amount, selectedSubscription, userFacade.find((Long) session.getAttribute("userID")));
        } catch (Exception ex) {
            log.error(String.format("Cannot refund: subscription=%s, amount=%s", selectedSubscription, manats), ex);
            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Cannot refund", "Cannot refund");
            FacesContext.getCurrentInstance().addMessage(null, message);
        }
        /*
         refund.setService(selectedSubscription.getService());
         refund.setSubscriber(selectedSubscription.getSubscriber());
         refund.setDatetime(DateTime.now());
         HttpSession session = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);
         refund.setUser_id(userFacade.find((Long)session.getAttribute("userID")).getId());
                
         String amount = (this.cents != null && !this.cents.isEmpty() && this.cents.length() == 2) ? this.manats + this.cents + "000" : this.manats + "00000";
         refund.setAmount(Long.parseLong(amount));
         refundFacade.save(refund);
         */

        return INDIVIDUAL_PATH + "view_refunds?subscriber=" + refund.getSubscriber().getId() + "&amp;faces-redirect=true&amp;includeViewParams=true";
    }

    public void setSubscriberIdForRedirect(Long id) {
    }

    public LazyDataModel<Invoice> getInvoiceList() {
        if (invoiceList == null && subscriberId != 0) {
            invoiceFacade.addFilter(InvoicePersistenceFacade.Filter.SUBSCRIBER_ID, subscriberId);
            invoiceList = new LazyTableModel<>(invoiceFacade);
        }
        return invoiceList;
    }

    public void setInvoiceList(LazyDataModel<Invoice> invoiceList) {
        this.invoiceList = invoiceList;
    }

    public List<Invoice> getFilteredInvoiceList() {
        return filteredInvoiceList;
    }

    public void setFilteredInvoiceList(List<Invoice> filteredInvoiceList) {
        this.filteredInvoiceList = filteredInvoiceList;
    }

    public boolean isIsCorporate() {
        if (selected != null) {
            isCorporate = (selected.getType() == SubscriberType.CORP);
        }
        log.debug("Corporate: " + isCorporate);
        return isCorporate;
    }

    public void setIsCorporate(boolean isCorporate) {
        this.isCorporate = isCorporate;
    }

    public Invoice getSelectedInvoice() {
        if (selectedInvoice == null && invoiceID != null) {
            selectedInvoice = invoiceFacade.find(invoiceID);
        }
        return selectedInvoice;
    }

    public void setSelectedInvoice(Invoice selectedInvoice) {
        this.selectedInvoice = selectedInvoice;
    }

    public Long getInvoiceID() {
        return invoiceID;
    }

    public void setInvoiceID(Long invoiceID) {
        this.invoiceID = invoiceID;
    }

    public CompanyType getCompanyType() {
        return companyType;
    }

    public void setCompanyType(CompanyType companyType) {
        this.companyType = companyType;
    }

    public String showInvoice() {
        invoiceFacade.clearFilters();

        if (selectedInvoice == null) {
            return null;
        }

        return "/pages/subscriber/ind/view_invoice?subscriber=" + subscriberId + "&amp;invoice=" + selectedInvoice.getId() + "&amp;faces-redirect=true&amp;includeViewParams=true";
    }

    public void downloadInvoice(ActionEvent event) throws IOException {

        if (selectedInvoice == null && !"ExportPDF".equals(event.getComponent().getId())) {
            return;
        }

        RequestContext.getCurrentInstance().execute("window.open('" + "https://ttt.azerfon.az/pdf/pdfinvoice.php?invoice_id=" + selectedInvoice.getId() + "'," + selectedInvoice.getId() + ")");

    }

    public void activate() {
        if (selectedSubscription == null) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Please select Subscription first", "Please select Subscription first")
            );
            return;
        }
        if (selectedSubscription.getSubscriber().getLifeCycle() == SubscriberLifeCycleType.POSTPAID) {
            if (selectedSubscription.getStatus() != SubscriptionStatus.INITIAL) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Subscription must be in INITIAL status", "Subscription must be in INITIAL status")
                );
                return;
            }
            try {
                if (engineFactory.getOperationsEngine(selectedSubscription).activatePostpaid(selectedSubscription)) {
                    FacesContext.getCurrentInstance().addMessage(null,
                            new FacesMessage(FacesMessage.SEVERITY_INFO, "Subscription activated successfully", "Subscription activated successfully")
                    );
                } else {
                    FacesContext.getCurrentInstance().addMessage(null,
                            new FacesMessage(FacesMessage.SEVERITY_ERROR, "Cannot activate subscription", "Cannot activate subscription")
                    );
                }
            } catch (Exception ex) {
                log.error(String.format("Could not activate subscription %s: ", selectedSubscription), ex);
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Cannot activate subscription", "Cannot activate subscription")
                );
            }
        } else {
            engineFactory.getOperationsEngine(selectedSubscription).activatePrepaid(selectedSubscription);
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Done", "Done")
            );
        }
    }

    public String redirectToOneTimeVASList() {
        if (selectedSubscription == null) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Please select Subscription first", "Please select Subscription first")
            );
            return null;
        } else if (selectedSubscription.getStatus() != SubscriptionStatus.ACTIVE) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Subscription must be in ACTIVE status", "Subscription must be in ACTIVE status")
            );
            return null;
        }

        return "/pages/subscriber/vas/onetime.xhtml?subscription=" + selectedSubscription.getId() + "&amp;subscriber=" + selected.getSubscriber().getId() + "&ampfaces-redirect=true&amp;includeViewParams=true";
    }

    public String viewTransferPage() {
        if (selectedPayment == null) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(
                    FacesMessage.SEVERITY_ERROR, "Please select payment first", "Please select payment first")
            );
            return null;
        }

        return new StringBuilder("/pages/finance/transfer.xhtml?payment=")
                .append(selectedPayment.getId())
                .append("&amp;faces-redirect=true&amp;includeViewParams=true").toString();
    }

    public LazyDataModel<Subscription> getSubscriptionList() {
        if (subscriptionList == null) {
            subscriptionList = new LazyTableModel<>(subscriptionFacade);
        }
        return subscriptionList;
    }

    public List<Compensation> getCompensationList() {
        if (compensationList == null) {
            compensationList = new ArrayList<>();
            if (this.selected == null) {
                getSelected();
            }
            for (Subscription subscription : this.selected.getSubscriber().getSubscriptions()) {
                compensationList.addAll(subscription.getCompensationList());
            }
        }
        return compensationList;
    }

    public void setCompensationList(List<Compensation> compensationList) {
        this.compensationList = compensationList;
    }

    public void setSubscriptionList(LazyDataModel<Subscription> subscriptionList) {
        this.subscriptionList = subscriptionList;
    }

    public List<Subscription> getFilteredSubscriptionList() {
        return filteredSubscriptionList;
    }

    public void setFilteredSubscriptionList(List<Subscription> filteredSubscriptionList) {
        this.filteredSubscriptionList = filteredSubscriptionList;
    }

    public Subscription getTargetSubscription() {
        return targetSubscription;
    }

    public void setTargetSubscription(Subscription targetSubscription) {
        this.targetSubscription = targetSubscription;
    }

    public String transfer() {
        if (singlePayment == null) {
            FacesContext.getCurrentInstance().addMessage(cents, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Please select payment", "Please select payment"));
            return null;
        } else if (targetSubscription == null) {
            FacesContext.getCurrentInstance().addMessage(cents, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Please select target subscription", "Please select target subscription"));
            return null;
        }

        try {
            log.debug(String.format("Target subscription=%s", targetSubscription));
            log.debug(String.format("The balance before=%s", targetSubscription.getBalance().getRealBalanceForView()));
            subscriptionFacade.transferPaymentForFinance(singlePayment.getId(), targetSubscription);
            log.debug(String.format("The balance after=%s", targetSubscription.getBalance().getRealBalanceForView()));

            queueManager.addToPaymentQueue(targetSubscription.getId(), singlePayment.getAmount(),-1L);

            return new StringBuilder("/pages/subscriber/ind/view_payments?subscriber=")
                    .append(targetSubscription.getSubscriber().getId())
                    .append("&amp;includeViewParams=true&amp;faces-redirect=true").toString();
        } catch (Exception ex) {
            FacesContext.getCurrentInstance().addMessage(cents, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Cannot transfer balance", "Cannot transfer balance"));
            return null;
        }
    }

    public String transferBalance() {
        if (selectedSubscription == null) {
            FacesContext.getCurrentInstance().addMessage(cents, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Please select subscription", "Please select subscription"));
            return null;
        } else if (targetSubscription == null) {
            FacesContext.getCurrentInstance().addMessage(cents, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Please select target subscription", "Please select target subscription"));
            return null;
        }
        if (selectedSubscription.getBalance().getRealBalance() <= 0) {
            FacesContext.getCurrentInstance().addMessage(cents, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Subscription does not have enough balance", "Subscription does not have enough balance"));
            return null;
        }

        try {
            log.debug(String.format("Target subscription=%s", targetSubscription));

            Long amount = getTransferAmount() != null && getTransferAmount() > 0 ? (long) (getTransferAmount() * 100000) : selectedSubscription.getBalance().getRealBalance();

            if (getTransferAmount() != null && getTransferAmount() > 0)
                subscriptionFacade.transferBalanceForFinance(selectedSubscription.getId(), targetSubscription.getId(), (long) (getTransferAmount() * 100000));
            else
                subscriptionFacade.transferBalanceForFinance(selectedSubscription.getId(), targetSubscription.getId());

            systemLogger.success(SystemEvent.BALANCE_TRANSFER, selectedSubscription, "To " + targetSubscription.getAgreement() + "; Amount=" + (getTransferAmount() != null && getTransferAmount() > 0 ? getTransferAmount() : selectedSubscription.getBalance().getRealBalanceForView()) + "; Description=" + getTransferDesc());
            systemLogger.success(SystemEvent.BALANCE_TRANSFER, targetSubscription, "From " + selectedSubscription.getAgreement() + "; Amount=" + (getTransferAmount() != null && getTransferAmount() > 0 ? getTransferAmount() : selectedSubscription.getBalance().getRealBalanceForView()) + "; Description=" + getTransferDesc());

            /*return new StringBuilder("/pages/subscriber/view.xhtml?subscriber=")
             .append(targetSubscription.getSubscriber().getId())
             .append("&amp;includeViewParams=true&amp;faces-redirect=true").toString();*/
            FacesContext.getCurrentInstance().addMessage(cents, new FacesMessage(FacesMessage.SEVERITY_INFO, "Balance transfer successfull", "Balance transfer successfull"));
            return null;
        } catch (Exception ex) {
            log.error("Cannot transfer balance: ", ex);
            FacesContext.getCurrentInstance().addMessage(cents, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Cannot transfer balance", "Cannot transfer balance"));
            return null;
        }
    }

    public String attachSubscription() {
        log.info("selected subscription: " + selectedSubscription);
        log.info("subscriber detials: " + selectedSubscriber);
        if (selectedSubscription == null || selectedSubscriber == null) {
            String errMessage = "";
            if (selectedSubscription == null) {
                errMessage += "Subscription should be selected\n";
            }
            if (selectedSubscriber == null) {
                errMessage += "Subscriber should be selected\n";
            }
            FacesContext.getCurrentInstance().addMessage(
                    "errMessages",
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Please select subscription and subscriber", errMessage));
            return null;
        }
        subscriptionFacade.attachSubscription(selectedSubscription, selectedSubscriber);
        return new StringBuilder("/pages/subscriber/view.xhtml?subscriber=")
                .append(selectedSubscriber.getId())
                .append("&amp;includeViewParams=true&amp;faces-redirect=true").toString();
    }

    @EJB
    private TroubleTicketPersistenceFacade ticketFacade;
    @EJB
    private SessionFacade sessionFacade;

    private List<TroubleTicket> troubleTickets;
    private TroubleTicket selectedTicket;
    private List<TroubleTicket> filteredTickets;
    private Subscriber subscriber;

    public List<TroubleTicket> getTroubleTickets() {
        if (troubleTickets == null) {
            if (subscriber == null) {
                subscriber = subscriberFacade.find(subscriberId);
            }

            if (subscriber != null) {
                troubleTickets = ticketFacade.findAllBySubscriber(subscriber);
            }
        }
        return troubleTickets;
    }

    public void setTroubleTickets(List<TroubleTicket> troubleTickets) {
        this.troubleTickets = troubleTickets;
    }

    public TroubleTicket getSelectedTicket() {
        return selectedTicket;
    }

    public void setSelectedTicket(TroubleTicket selected) {
        this.selectedTicket = selected;
    }

    public List<TroubleTicket> getFilteredTickets() {
        return filteredTickets;
    }

    public void setFilteredTickets(List<TroubleTicket> filteredTickets) {
        this.filteredTickets = filteredTickets;
    }

    public Long getSubscriberID() {
        return subscriberId;
    }

    public void setSubscriberID(Long subscriberID) {
        this.subscriberId = subscriberID;
    }

    public String redirectToSubscriber() {
        if (subscriberId == 0) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Please select ticket", "Please select ticket"));
            return null;
        }

        return new StringBuilder("/pages/subscriber/view.xhtml?subscriber=").append(subscriberId).append("&amp;faces-redirect=true&amp;includeViewParams=true").toString();
    }

    public String redirectToMainPage() {
        // try {
        String mainPage = FacesContext.getCurrentInstance().getExternalContext()
                .getInitParameter("com.jaravir.tekila.tt.redirect.main.address");
        HttpSession session = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);
        Long userID = (Long) session.getAttribute("userID");
        String key = (String) session.getAttribute("sessionID");

        SessionInfo sessionInfo = sessionFacade.findByUserID(userID, key);
        return String.format(mainPage, sessionInfo.getUsername(), key);
        //FacesContext.getCurrentInstance().getExternalContext().redirect(String.format(mainPage, sessionInfo.getUsername(), key));
        /*}
         catch (IOException ex) {
         FacesContext.getCurrentInstance().addMessage(null,
         new FacesMessage(FacesMessage.SEVERITY_ERROR, "Cannot redirect to trouble tickets main page","Cannot redirect to trouble tickets main page"));
         log.error("redirect to TT main page failed", ex);
         }*/
    }

    /*
     public void redirectToTicketPage() {
     log.debug("Selected ticket: " + selectedTicket);
     if (selectedTicket == null) {
     FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Please select ticket", "Please select ticket"));
     return;
     }
     try {
     String showTicketURL = FacesContext.getCurrentInstance().getExternalContext().getInitParameter("com.jaravir.tekila.tt.redirect.showticket.address");
     FacesContext.getCurrentInstance().getExternalContext().redirect(showTicketURL + selectedTicket.getId());
     }
     catch (IOException ex) {
     FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Cannot redirect to trouble ticket","Cannot redirect to trouble ticket"));
     log.error("redirect failed", ex);
     }

     }*/
    public void getAddTicketPageAddress(SelectEvent event) {

        String mainPage = FacesContext.getCurrentInstance().getExternalContext()
                .getInitParameter("com.jaravir.tekila.tt.add.redirect");
        HttpSession session = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);
        Long userID = (Long) session.getAttribute("userID");
        String key = (String) session.getAttribute("sessionID");

        SessionInfo sessionInfo = sessionFacade.findByUserID(userID, key);
        String url = String.format(mainPage, key, selectedSubscription.getAgreement(), selectedSubscription.getService().getProvider().getId());
        log.debug("Add ticket url: " + url);
        addTicketMenuItem.setUrl(url);
        // FacesContext.getCurrentInstance().getExternalContext().redirect(url);
        //FacesContext.getCurrentInstance().getExternalContext().redirect(String.format(mainPage, sessionInfo.getUsername(), key));

    }

    private UIMenuItem addTicketMenuItem;

    public UIMenuItem getAddTicketMenuItem() {
        return addTicketMenuItem;
    }

    public void setAddTicketMenuItem(UIMenuItem addTicketMenuItem) {
        this.addTicketMenuItem = addTicketMenuItem;
    }

    public List<SelectItem> getLangList() {
        if (langList == null) {
            langList = new ArrayList<>();

            if (selected != null) {
                langList.add(new SelectItem(selected.getLanguage()));
            }
            for (Language lang : Language.values()) {
                if (selected != null && selected.getLanguage() == lang) {
                    continue;
                }

                langList.add(new SelectItem(lang));
            }
        }

        return langList;
    }

    public void setLangList(List<SelectItem> langList) {
        this.langList = langList;
    }

    public void redirectToAddTicketPage() {
        try {
            if (selectedSubscription == null) {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Please select a subscrption", "Please select a subscrption"));
                return;
            }
            String mainPage = FacesContext.getCurrentInstance().getExternalContext()
                    .getInitParameter("com.jaravir.tekila.tt.add.redirect");
            HttpSession session = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);
            Long userID = (Long) session.getAttribute("userID");
            String key = (String) session.getAttribute("sessionID");

            SessionInfo sessionInfo = sessionFacade.findByUserID(userID, key);
            String url = String.format(mainPage, key, selectedSubscription.getAgreement(), selectedSubscription.getService().getProvider().getId());
            log.debug("Add ticket url: " + url);
            addTicketMenuItem.setUrl(url);
            FacesContext.getCurrentInstance().getExternalContext().redirect(url);
            //FacesContext.getCurrentInstance().getExternalContext().redirect(String.format(mainPage, sessionInfo.getUsername(), key));
        } catch (IOException ex) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Cannot redirect to trouble tickets main page", "Cannot redirect to trouble tickets main page"));
            log.error("redirect to TT add page failed", ex);
        }
    }

    private CommandButton viewTicketButton;

    public CommandButton getViewTicketButton() {
        return viewTicketButton;
    }

    public void setViewTicketButton(CommandButton btn) {
        this.viewTicketButton = btn;
    }

    public void getShowTicketAddress(SelectEvent event) {
        //log.debug("Class of selecte object: " + event.getObject().getClass());
        //selectedTicket = (TroubleTicket) event.getObject();
        //log.debug("Selected ticket from event: " + sel.getId());
        HttpSession session = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);
        String mainPage = FacesContext.getCurrentInstance().getExternalContext()
                .getInitParameter("com.jaravir.tekila.tt.redirect.showticket.address");
        log.debug("Selected ticket: " + selectedTicket);
        String url = String.format(mainPage, selectedTicket.getId(), session.getAttribute("sessionID"));
        log.debug("Show ticket url: " + url);
        viewTicketButton.setOnclick("window.open('" + url + "')");
        // FacesContext.getCurrentInstance().getExternalContext().redirect(url);
        //FacesContext.getCurrentInstance().getExternalContext().redirect(String.format(mainPage, sessionInfo.getUsername(), key));

    }

    public String addSubscription() {
        return "/pages/subscription/create.xhtml?subscriber=" + subscriberId + "&amp;faces-redirect=true&amp;includeViewParams=true";
    }

    public String addSubscriptionCitynet() {
        return "/pages/subscription/create/create_454105.xhtml?subscriber=" + subscriberId + "&amp;faces-redirect=true&amp;includeViewParams=true";
    }

    public String addSubscriptionAzertelecom() {
        return "/pages/subscription/create/create_454100.xhtml?subscriber=" + subscriberId + "&amp;faces-redirect=true&amp;includeViewParams=true";
    }

    public String addSubscriptionAzertelecomPOST() {
        return "/pages/subscription/create/create_454110.xhtml?subscriber=" + subscriberId + "&amp;faces-redirect=true&amp;includeViewParams=true";
    }

    public String addSubscriptionBbtv() {
        return "/pages/subscription/create/create_454101.xhtml?subscriber=" + subscriberId + "&amp;faces-redirect=true&amp;includeViewParams=true";
    }

    public String addSubscriptionBbtvBaku() {
        return "/pages/subscription/create/create_454104.xhtml?subscriber=" + subscriberId + "&amp;faces-redirect=true&amp;includeViewParams=true";
    }

    public String addSubscriptionQutuCityNet() {
        return "/pages/subscription/create/create_454106.xhtml?subscriber=" + subscriberId + "&amp;faces-redirect=true&amp;includeViewParams=true";
    }


    public String addSubscriptionQutuNarHome() {
        return "/pages/subscription/create/create_454114.xhtml?subscriber=" + subscriberId + "&amp;faces-redirect=true&amp;includeViewParams=true";
    }

    public String addSubscriptionUninet() {
        return "/pages/subscription/create/create_454107.xhtml?subscriber=" + subscriberId + "&amp;faces-redirect=true&amp;includeViewParams=true";
    }

    public String addSubscriptionNarFix() {
        return "/pages/subscription/create/create_454108.xhtml?subscriber=" + subscriberId + "&amp;faces-redirect=true&amp;includeViewParams=true";
    }

    public String addSubscriptionDataplus() {
        return "/pages/subscription/create/create_454111.xhtml?subscriber=" + subscriberId + "&amp;faces-redirect=true&amp;includeViewParams=true";
    }

    public String addSubscriptionGlobal() {
        return "/pages/subscription/create/create_454112.xhtml?subscriber=" + subscriberId + "&amp;faces-redirect=true&amp;includeViewParams=true";
    }

    public String addSubscriptionCNC() {
        return "/pages/subscription/create/create_454113.xhtml?subscriber=" + subscriberId + "&amp;faces-redirect=true&amp;includeViewParams=true";
    }

    public void onTicketViewClicked() {
        if (selectedTicket == null) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Please select a ticket", "Please select a ticket"));
        }
    }

    public void onTicketAddClicked() {
        if (selectedSubscription == null) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Please select a subscription", "Please select a subscription"));
        }
    }

    public void cancelCharge() {
        if (singleCharge == null) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Please select charge", "Please select charge"));
            return;
        }
        try {
            chargeFacade.cancelCharge(singleCharge.getId());
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Charge successfully cancelled", "Charge successfully cancelled"));
        } catch (Exception ex) {
            log.error("Cannot cancel charge=" + singleCharge, ex);
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Charge cannot be cancelled", "Charge cannot be cancelled"));
        }
    }

    public void cancelPayment() {
        if (singlePayment == null) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Please select payment", "Please select payment"));
            return;
        }
        try {
            paymentFacade.cancelPayment(singlePayment.getId());
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Payment successfully cancelled", "Payment successfully cancelled"));
        } catch (Exception ex) {
            log.error("Cannot cancel payment=" + singlePayment, ex);
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Payment cannot be cancelled", "Payment cannot be cancelled"));
        }
    }

    public String getPhoneMobileCode() {
        return phoneMobileCode;
    }

    public void setPhoneMobileCode(String phoneMobileCode) {
        this.phoneMobileCode = phoneMobileCode;
    }

    public String getPhoneMobileBody() {
        return phoneMobileBody;
    }

    public void setPhoneMobileBody(String phoneMobileBody) {
        this.phoneMobileBody = phoneMobileBody;
    }

    public void clearFilters(ActionEvent event) {
        subscriptionFacade.clearFilters();
        subscriptionList = new LazyTableModel<>(subscriptionFacade);
    }

    public String getSubscriberSurname() {
        return subscriberSurname;
    }

    public void setSubscriberSurname(String subscriberSurname) {
        this.subscriberSurname = subscriberSurname;
    }

    public String getSubscriberMiddleName() {
        return subscriberMiddleName;
    }

    public void setSubscriberMiddleName(String subscriberMiddleName) {
        this.subscriberMiddleName = subscriberMiddleName;
    }

    public String getSubscriberName() {
        return subscriberName;
    }

    public void setSubscriberName(String subscriberName) {
        this.subscriberName = subscriberName;
    }

    public String getSbnContract() {
        return sbnContract;
    }

    public void setSbnContract(String sbnContract) {
        this.sbnContract = sbnContract;
    }

    public String getSbID() {
        return sbID;
    }

    public void setSbID(String sbID) {
        this.sbID = sbID;
    }

    public String getSbnService() {
        return sbnService;
    }

    public void setSbnService(String sbnService) {
        this.sbnService = sbnService;
    }

    public void searchSbn() {
        if ((sbID == null || sbID.isEmpty()) && (sbnContract == null || sbnContract.isEmpty())
                && (sbnService == null && sbnService.isEmpty())
                && (subscriberName == null || subscriberName.isEmpty())
                && (subscriberMiddleName == null || subscriberMiddleName.isEmpty())
                && (subscriberSurname == null || subscriberSurname.isEmpty())) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Please fill the fields", "Please fill the fields"));
            return;
        }

        subscriptionFacade.clearFilters();

        if (sbID != null && !sbID.isEmpty()) {
            subscriptionFacade.addFilter(SubscriptionPersistenceFacade.Filter.ID, sbID);
        }
        if (sbnContract != null && !sbnContract.isEmpty()) {
            subscriptionFacade.addFilter(SubscriptionPersistenceFacade.Filter.AGREEMENT, sbnContract);
        }
        if (subscriberName != null && !subscriberName.isEmpty()) {
            subscriptionFacade.addFilter(SubscriptionPersistenceFacade.Filter.SUBSCRIBER_NAME, subscriberName);
        }
        if (subscriberMiddleName != null && !subscriberMiddleName.isEmpty()) {
            subscriptionFacade.addFilter(SubscriptionPersistenceFacade.Filter.SUBSCRIBER_MIDDLENAME, subscriberMiddleName);
        }
        if (subscriberSurname != null && !subscriberSurname.isEmpty()) {
            subscriptionFacade.addFilter(SubscriptionPersistenceFacade.Filter.SUBSCRIBER_SURNAME, subscriberSurname);
        }

        if (sbnService != null && !sbnService.isEmpty()) {
            subscriptionFacade.addFilter(SubscriptionPersistenceFacade.Filter.SERVICE, sbnService);
        }

        subscriptionList = new LazyTableModel<>(subscriptionFacade);
    }

    public void searchSubscribers() {
        if (subscriberName == null && subscriberMiddleName == null &&
                subscriberSurname == null) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Please fill the fields", "Please fill the fields"));
            return;
        }
        subscriberFacade.clearFilters();

        if (subscriberName != null && !subscriberName.isEmpty()) {
            subscriberFacade.addFilter(SubscriberPersistenceFacade.Filter.NAME, subscriberName);
        }

        if (subscriberMiddleName != null && !subscriberMiddleName.isEmpty()) {
            subscriberFacade.addFilter(SubscriberPersistenceFacade.Filter.MIDDLENAME, subscriberMiddleName);
        }

        if (subscriberSurname != null && !subscriberSurname.isEmpty()) {
            subscriberFacade.addFilter(SubscriberPersistenceFacade.Filter.SURNAME, subscriberSurname);
        }
        subscriberList = new LazyTableModel<>(subscriberFacade);
    }

    public void resetMoveForms(ActionEvent event) {
        subscriberName = null;
        subscriberMiddleName = null;
        subscriberSurname = null;

        subscriberFacade.clearFilters();
    }

    public void resetForms(ActionEvent event) {
        sbID = null;
        sbnContract = null;
        subscriberName = null;
        subscriberMiddleName = null;
        subscriberSurname = null;
        subscriptionList = null;

        invoiceFacade.clearFilters();
        invoiceFacade.addFilter(InvoicePersistenceFacade.Filter.SUBSCRIBER_ID, subscriberId);

        invoiceList = null;
        invoiceID = null;
        searchInvoiceStatus = null;

        transactionType = null;
        transactionID = null;
        agreement = null;
        transactionFacade.clearFilters();

        subscriptionFacade.clearFilters();

        chargeFacade.clearFilters();
        chargeFacade.addFilter(ChargePersistenceFacade.Filter.SUBSCRIBER_ID, subscriberId);
        chargeLazyModel = new LazyTableModel<>(chargeFacade);

        paymentFacade.clearFilters();
        paymentFacade.addFilter(PaymentPersistenceFacade.Filter.SUBSCRIBER_ID, subscriberId);
        paymentLazyModel = new LazyTableModel<>(paymentFacade);
    }

    public InvoiceState getSearchInvoiceStatus() {
        return searchInvoiceStatus;
    }

    public void setSearchInvoiceStatus(InvoiceState searchInvoiceStatus) {
        this.searchInvoiceStatus = searchInvoiceStatus;
    }

    public void searchInvoices() {
        if (invoiceID == null && searchInvoiceStatus == null
                && (subscriberName == null || subscriberName.isEmpty())
                && (subscriberMiddleName == null || subscriberMiddleName.isEmpty())
                && (subscriberSurname == null || subscriberSurname.isEmpty())) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Please fill the fields", "Please fill the fields"));
            return;
        }

        invoiceFacade.clearFilters();
        invoiceFacade.addFilter(InvoicePersistenceFacade.Filter.SUBSCRIBER_ID, subscriberId);

        if (invoiceID != null) {
            InvoicePersistenceFacade.Filter invoiceIdFilter = InvoicePersistenceFacade.Filter.ID;
            invoiceIdFilter.setMatchingOperation(MatchingOperation.EQUALS);
            invoiceFacade.addFilter(invoiceIdFilter, invoiceID);
        }

        if (searchInvoiceStatus != null) {
            invoiceFacade.addFilter(InvoicePersistenceFacade.Filter.STATE, searchInvoiceStatus);
        }

        if (subscriberName != null && !subscriberName.isEmpty()) {
            invoiceFacade.addFilter(InvoicePersistenceFacade.Filter.SUBSCRIBER_NAME, subscriberName);
        }

        if (subscriberMiddleName != null && !subscriberMiddleName.isEmpty()) {
            invoiceFacade.addFilter(InvoicePersistenceFacade.Filter.SUBSCRIBER_MIDDLENAME, subscriberMiddleName);
        }

        if (subscriberSurname != null && !subscriberSurname.isEmpty()) {
            invoiceFacade.addFilter(InvoicePersistenceFacade.Filter.SUBSCRIBER_SURNAME, subscriberSurname);
        }
        log.debug("searchInvoices: invoce filters=" + invoiceFacade.getFilters());
        invoiceList = new LazyTableModel<>(invoiceFacade);
    }

    public List<SelectItem> getSearchInvoiceStatusList() {
        if (searchInvoiceStatusList == null) {
            searchInvoiceStatusList = new ArrayList<>();

            for (InvoiceState state : InvoiceState.values()) {
                searchInvoiceStatusList.add(new SelectItem(state));
            }
        }
        return searchInvoiceStatusList;
    }

    public void setSearchInvoiceStatusList(List<SelectItem> searchInvoiceStatusList) {
        this.searchInvoiceStatusList = searchInvoiceStatusList;
    }

    public SubscriberFunctionalCategory getFuncCat() {
        return funcCat;
    }

    public void setFuncCat(SubscriberFunctionalCategory funcCat) {
        this.funcCat = funcCat;
    }

    public List<SelectItem> getFnCatSelectList() {
        if (fnCatSelectList == null) {
            fnCatSelectList = new ArrayList<>();

            for (SubscriberFunctionalCategory cat : SubscriberFunctionalCategory.values()) {
                fnCatSelectList.add(new SelectItem(cat));
            }
        }
        return fnCatSelectList;
    }

    public void setFnCatSelectList(List<SelectItem> fnCatSelectList) {
        this.fnCatSelectList = fnCatSelectList;
    }

    public boolean isPostpaid() {
        return subscriberFacade.getLifeCycleTypeBySubscriberId(subscriberId) != SubscriberLifeCycleType.PREPAID;
    }

    // Transactions / Adjustments
    @EJB
    private TransactionPersistenceFacade transactionFacade;
    private List<SelectItem> TransactionsTypeList;
    private List<SelectItem> AgreementList;
    private LazyDataModel<Transaction> transactionLazyModel;
    private Transaction singleTransaction;
    private long singleTransactionID;

    private TransactionType transactionType;
    private Long transactionID;
    private String agreement;

    public long getSingleTransactionID() {
        return singleTransactionID;
    }

    public void setSingleTransactionID(long singleTransactionID) {
        this.singleTransactionID = singleTransactionID;
    }

    public Long getTransactionID() {
        return transactionID;
    }

    public void setTransactionID(Long transactionID) {
        this.transactionID = transactionID;
    }

    public String getAgreement() {
        return agreement;
    }

    public void setAgreement(String agreement) {
        this.agreement = agreement;
    }

    public TransactionType getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(TransactionType transactionType) {
        this.transactionType = transactionType;
    }

    public List<SelectItem> getTransactionsTypeList() {
        if (TransactionsTypeList == null) {
            TransactionsTypeList = new ArrayList<>();

            TransactionsTypeList.add(new SelectItem(transactionType.CREDIT));
            TransactionsTypeList.add(new SelectItem(transactionType.DEBIT));
            TransactionsTypeList.add(new SelectItem(transactionType.OVERWRITE));
            TransactionsTypeList.add(new SelectItem(transactionType.PAYMENT));

        }
        return TransactionsTypeList;
    }

    public List<SelectItem> getAgreementList() {
        if (AgreementList == null) {
            AgreementList = new ArrayList<>();

            List<Subscription> subList = subscriptionFacade.findBySubscriberId(subscriberId);

            for (Subscription l : subList) {
                AgreementList.add(new SelectItem(l.getAgreement()));
            }

        }
        return AgreementList;
    }

    public Transaction getSingleTransaction() {
        log.debug("from getSingleTransaction: transactionID: " + transactionID + " singleTransaction:" + singleTransactionID);
        if (singleTransaction == null && singleTransactionID != 0) {
            singleTransaction = transactionFacade.find(singleTransactionID);
        }
        return singleTransaction;
    }

    public void setSingleTransaction(Transaction singleTransaction) {
        this.singleTransaction = singleTransaction;
    }

    public LazyDataModel<Transaction> getTransactionLazyModel() {
        // if (transactionLazyModel == null && subscriptionId != 0) {
        if (transactionLazyModel == null) {
            transactionFacade.clearFilters();

            String query = transactionFacade.getTransactionsBySubscriberID(subscriberId);
            query += " order by t.lastUpdateDate desc";
            transactionLazyModel = new LazyTableModelDynamic<>(transactionFacade, Transaction.class, query);

            query = query.replaceFirst("select t", "select count(t)");
            long count = transactionFacade.countDynamic(query);
            transactionLazyModel.setRowCount((int) count);
        }

        return transactionLazyModel;
    }

    public void searchAdjustments() {

        if ((transactionType == null)
                && (transactionID == null)
                && (agreement == null || agreement.isEmpty())) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Please fill the fields", "Please fill the fields"));

            transactionFacade.clearFilters();

            String query = transactionFacade.getTransactionsBySubscriberID(subscriberId);
            query += " order by t.lastUpdateDate desc";
            transactionLazyModel = new LazyTableModelDynamic<>(transactionFacade, Transaction.class, query);

            return;
        }

        String query = transactionFacade.getTransactionsBySubscriberID(subscriberId);

        if (transactionID != null) {
            transactionFacade.addFilter(TransactionPersistenceFacade.Filter.ID, transactionID);
            query += " and t.id=:id";
        }

        if (transactionType != null) {
            transactionFacade.addFilter(TransactionPersistenceFacade.Filter.TYPE, transactionType);
            query += " and t.type=:type";
        }

        if (agreement != null && !agreement.isEmpty()) {
            query += " and t.subscription.agreement= '" + agreement + "'";
        }
        log.debug("Query: " + query);
        log.debug("searchTransactions: transaction filters=" + transactionFacade.getFilters());
        query += " order by t.lastUpdateDate desc";
        transactionLazyModel = new LazyTableModelDynamic<>(transactionFacade, Transaction.class, query);

        query = query.replaceFirst("select t", "select count(t)");
        long count = transactionFacade.countDynamic(query);
        transactionLazyModel.setRowCount((int) count);
    }

    public void searchCharges() {
        if (agreement == null || agreement.isEmpty()) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Please fill the fields", "Please fill the fields"));
            return;
        }

        chargeFacade.clearFilters();
        chargeFacade.addFilter(ChargePersistenceFacade.Filter.AGREEMENT, agreement);
        chargeLazyModel = new LazyTableModel<>(chargeFacade);
    }

    public void searchPayments() {
        if (agreement == null || agreement.isEmpty()) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Please fill the fields", "Please fill the fields"));
            return;
        }

        paymentFacade.clearFilters();
        PaymentPersistenceFacade.Filter agreementFilter = PaymentPersistenceFacade.Filter.AGREEMENT;
        agreementFilter.setOperation(MatchingOperation.EQUALS);
        paymentFacade.addFilter(agreementFilter, agreement);
        paymentFacade.addOrdering("lastUpdateDate", AbstractPersistenceFacade.Ordering.DESC);
        paymentLazyModel = new LazyTableModel<>(paymentFacade);
    }

    public String viewTransaction() {
        log.debug("from viewTransaction");
        log.debug("getSingleTransaction(): " + getSingleTransaction());
        if (singleTransaction == null) {
            return null;
        }

        String goto_path = INDIVIDUAL_PATH + "view_transaction?transaction=" + singleTransaction.getId() + "&ampsubscriber=" + subscriberId + "&amp;faces-redirect=true&amp;includeViewParams=true";
        log.debug("Goto: " + goto_path);
        return goto_path;
    }

    public String viewTransactionBack() {

        String goto_path = INDIVIDUAL_PATH + "transactions?subscriber=" + subscriberId + "&amp;faces-redirect=true&amp;includeViewParams=true";
        log.debug("Goto: " + goto_path);
        return goto_path;
    }

    // Billing Of Postpaid
    @EJB
    private BillingManager billing;
    @EJB
    private BeforeBillingListener campaignManager;

    public void billPostpaid() {

        if (selectedSubscription == null) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Select an agreement !", "Error"));
            return;
        }

        if (subscriberFacade.getLifeCycleTypeBySubscriberId(subscriberId) == SubscriberLifeCycleType.PREPAID) {
            return;
        }

        try {
            engineFactory.getBillingEngine(selectedSubscription).billPrepaid(selectedSubscription);
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Successfully Billed", "Successfull"));

        } catch (Exception ex) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Billing Error", "Error"));
            log.error(String.format("Cannot bill postpaid subscription id =%d, agreement=%s", selectedSubscription.getId(), selectedSubscription.getAgreement()), ex);
        }
    }


    public void bill() {

        if (selectedSubscription == null) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Select an agreement !", "Error"));
            return;
        }

        if (subscriberFacade.getLifeCycleTypeBySubscriberId(subscriberId) == SubscriberLifeCycleType.PREPAID) {
            try {
                campaignManager.beforeBilling(selectedSubscription);
                Subscription updatedSub = subscriptionFacade.find(selectedSubscription.getId());
                engineFactory.getBillingEngine(updatedSub).billPrepaid(updatedSub);
                campaignRegisterFacade.processPrepaidForAfterBilling(selectedSubscription);

                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Successfully Billed", "Successfull"));

            } catch (Exception ex) {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Billing Error", "Error"));
                log.error(String.format("Cannot bill prepaid subscription id =%d, agreement=%s", selectedSubscription.getId(), selectedSubscription.getAgreement()), ex);
            }
        } else {

            try {
                engineFactory.getBillingEngine(selectedSubscription).billPostpaid(selectedSubscription);

                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Successfully Billed", "Successfull"));

            } catch (Exception ex) {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Billing Error", "Error"));
                log.error(String.format("Cannot bill postpaid subscription id =%d, agreement=%s", selectedSubscription.getId(), selectedSubscription.getAgreement()), ex);
            }
        }
    }

    public String delete() {
        if (selectedSubscription == null) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Select an agreement !", "Error"));
            return null;
        }

        //odenishi varsa silmesin
        List<Payment> paymentList = paymentFacade.findAllByAgreement(selectedSubscription.getAgreement());
        if (paymentList != null && !paymentList.isEmpty()) {
            boolean removable = true;
            for (final Payment payment : paymentList) {
                if (payment.getStatus() != -1) {
                    removable = false;
                }
            }
            if (!removable) {
                FacesContext.getCurrentInstance().addMessage(
                        null,
                        new FacesMessage(
                                FacesMessage.SEVERITY_ERROR,
                                "This agreement has a payment!",
                                "Please delete payment to delete agreement"));
                return null;
            }
        }

        SubscriptionSetting minipopSetting = selectedSubscription.getSettingByType(ServiceSettingType.BROADBAND_SWITCH);
        SubscriptionSetting minipopPort = selectedSubscription.getSettingByType(ServiceSettingType.BROADBAND_SWITCH_PORT);
        if (minipopSetting != null && !minipopSetting.equals("-")) {
            miniPopPersistenceFacade.clearFilters();
            try {
                MiniPop miniPop = miniPopPersistenceFacade.find(Long.valueOf(minipopSetting.getValue()));
                if (miniPop != null && minipopPort != null) {
                    miniPop.free(Integer.valueOf(minipopPort.getValue()));
                }
                miniPopPersistenceFacade.update(miniPop);
            } catch (Exception ex) {
                log.error(String.format("Could not free minipop port %d from minipop id %d", minipopPort.getValue(), minipopSetting.getValue()));
            }
        }

        selectedSubscription.setDetails(null);
        Subscriber subscriber = selectedSubscription.getSubscriber();
        List<Invoice> invoiceList = subscriber.getInvoices();
        if (invoiceList != null) {
            for (Invoice invoice : invoiceList) {
                List<Payment> payments = invoice.getPayments();
                List<Payment> updatedPayments = new ArrayList<>();
                for (final Payment payment : payments) {
                    if (!payment.getContract().equals(selectedSubscription.getAgreement())) {
                        updatedPayments.add(payment);
                    }
                }
                invoice.setPayments(updatedPayments);


                List<Charge> charges = invoice.getCharges();
                List<Charge> updatedCharges = new ArrayList<>();
                for (final Charge charge : charges) {
                    if (charge.getSubscription().getId() != selectedSubscription.getId()) {
                        updatedCharges.add(charge);
                    }
                }
                invoice.setCharges(updatedCharges);

                invoiceFacade.update(invoice);
                if (updatedCharges.isEmpty()) {
                    invoiceFacade.removeDetached(invoice);
                }
            }
        }
        List<Transaction> transactions = transactionFacade.getTransactionsBySubscriptionId(selectedSubscription.getId());
        systemLogger.removeSystemLogs(selectedSubscription);
        for (final Transaction transaction : transactions) {
            try {
                transactionFacade.removeDetached(transaction);
            } catch (Exception ex) {
                log.error(ex);
            }
        }
        List<CampaignRegister> campaigns =
                campaignRegisterFacade.findAllBySubscription(selectedSubscription);
        if (campaigns != null) {
            for (final CampaignRegister campaign : campaigns) {
                campaignRegisterFacade.removeDetached(campaign);
            }
        }

        subscriptionFacade.removeDetached(selectedSubscription);

        try {
            ProvisioningEngine provisioner = engineFactory.getProvisioningEngine(selectedSubscription);
            provisioner.removeAccount(selectedSubscription);
        } catch (ProvisionerNotFoundException e) {
            log.error(e);
        }

        return new StringBuilder("/pages/subscriber/view.xhtml?subscriber=")
                .append(getSubscriberId())
                .append("&amp;includeViewParams=true&amp;faces-redirect=true").toString();
    }

    Double transferAmount;
    String transferDesc;

    public String getTransferDesc() {
        return transferDesc;
    }

    public void setTransferDesc(String transferDesc) {
        this.transferDesc = transferDesc;
    }

    public Double getTransferAmount() {
        return transferAmount;
    }

    public void setTransferAmount(Double transferAmount) {
        this.transferAmount = transferAmount;
    }


    public CompensationDetails getCompensationDetails() {
        if (compensationDetails == null) {
            compensationDetails = new CompensationDetails();
        }
        return compensationDetails;
    }

    public void setCompensationDetails(CompensationDetails details) {
        this.compensationDetails = details;
    }

    public String compensate() {
        if (selectedSubscription == null) {
            FacesContext.getCurrentInstance().addMessage(
                    "errMsgs",
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Please select subscription", "Subscription to add compensation"));
            return null;
        }
        if (!compensationDetails.isFull()) {
            FacesContext.getCurrentInstance().addMessage(
                    "errMsgs",
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Please enter all compensation details", "All details are mandatory"));
            return null;
        }
        log.info("Start compensation for subscription = " + selectedSubscription);

        try {
            org.json.simple.JSONArray fieldList = subscriptionFacade.addCompensation(selectedSubscription, compensationDetails);
            ProvisioningEngine provisioner = engineFactory.getProvisioningEngine(selectedSubscription);

            boolean ok = provisioner.reprovision(selectedSubscription);

            if (ok) {
                subscriptionFacade.update(selectedSubscription);
                systemLogger.success(SystemEvent.COMPENSATION_ADD, selectedSubscription, "updated fields = " + fieldList.toJSONString());
            } else {
                systemLogger.error(SystemEvent.COMPENSATION_ADD, selectedSubscription, "could not compensate for subscription");
            }

        } catch (Exception ex) {
            log.error("Could not provision", ex);
            FacesContext.getCurrentInstance().addMessage(
                    "errMsgs",
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Could not provision", "Either radius or oracle connection error"));
            return null;
        }

        return new StringBuilder("/pages/subscription/view.xhtml?subscription=")
                .append(selectedSubscription.getId())
                .append("&amp;includeViewParams=true&amp;faces-redirect=true").toString();
    }

    public void selectSubscription() {
        if (selectedSubscription.getStatus().equals(SubscriptionStatus.ACTIVE)) {
            compensationDetails.setFromDate(selectedSubscription.getExpiresAsDate());
        } else {
            compensationDetails.setFromDate(new Date());
        }
    }

    public void onLoad() {
        log.debug("SubscriptionId: " + subscriptionId);
        chequeNum = chequeFacade.findAndUpdateChequeNum();
    }
}

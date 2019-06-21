package com.jaravir.tekila.ui.jsf.managed.search;

import com.jaravir.tekila.inMemory.InMemoryManager;
import com.jaravir.tekila.base.filter.MatchingOperation;
import com.jaravir.tekila.base.persistence.facade.AbstractPersistenceFacade;
import com.jaravir.tekila.module.accounting.InvoiceState;
import com.jaravir.tekila.module.accounting.entity.Invoice;
import com.jaravir.tekila.module.accounting.entity.Payment;
import com.jaravir.tekila.module.accounting.manager.InvoicePersistenceFacade;
import com.jaravir.tekila.module.accounting.manager.PaymentPersistenceFacade;
import com.jaravir.tekila.module.service.ResourceBucketType;
import com.jaravir.tekila.module.service.ServiceSettingType;
import com.jaravir.tekila.module.service.entity.ServiceProvider;
import com.jaravir.tekila.module.service.persistence.manager.ServiceProviderPersistenceFacade;
import com.jaravir.tekila.module.store.ScratchCardPersistenceFacade;
import com.jaravir.tekila.module.store.ScratchCardSessionPersistenceFacade;
import com.jaravir.tekila.module.store.SerialPersistenceFacade;
import com.jaravir.tekila.module.store.scratchcard.persistence.entity.ScratchCard;
import com.jaravir.tekila.module.store.scratchcard.persistence.entity.ScratchCardSession;
import com.jaravir.tekila.module.subscription.persistence.entity.Subscriber;
import com.jaravir.tekila.module.subscription.persistence.entity.SubscriberType;
import com.jaravir.tekila.module.subscription.persistence.entity.Subscription;
import com.jaravir.tekila.module.subscription.persistence.entity.SubscriptionStatus;
import com.jaravir.tekila.module.subscription.persistence.management.SubscriberPersistenceFacade;
import com.jaravir.tekila.module.subscription.persistence.management.SubscriptionPersistenceFacade;
import com.jaravir.tekila.module.subscription.persistence.management.SubscriptionViewPersistenceFacade;
import com.jaravir.tekila.provision.broadband.devices.MiniPop;
import com.jaravir.tekila.provision.broadband.devices.manager.MiniPopPersistenceFacade;
import com.jaravir.tekila.provision.broadband.devices.manager.Providers;
import com.jaravir.tekila.ui.model.LazyTableModel;
import com.jaravir.tekila.ui.model.LazyTableModelDynamic;
import com.jaravir.tekila.module.store.scratchcard.persistence.entity.Serial;

import java.io.IOException;

import com.jaravir.tekila.ui.util.FrontUniversalTool;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.primefaces.component.datatable.DataTable;
import org.primefaces.context.RequestContext;
import org.primefaces.event.TabChangeEvent;
import org.primefaces.model.LazyDataModel;

import javax.annotation.ManagedBean;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.*;
import javax.faces.component.UIComponent;
import javax.faces.component.UIForm;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.event.AjaxBehaviorEvent;
import javax.faces.event.ComponentSystemEvent;
import javax.faces.model.SelectItem;
import javax.naming.OperationNotSupportedException;
import java.io.Serializable;
import java.util.*;
import javax.annotation.PostConstruct;

import org.primefaces.event.SelectEvent;

/**
 * Created by sajabrayilov on 11/5/2014.
 */
@ManagedBean
@SessionScoped
public class SearchManager implements Serializable {

    private transient final static Logger log = Logger.getLogger(SearchManager.class);

    @EJB
    private SubscriptionPersistenceFacade subscriptionFacade;
    @EJB
    private SubscriptionViewPersistenceFacade viewFacade;
    @EJB
    private PaymentPersistenceFacade paymentFacade;
    @EJB
    private MiniPopPersistenceFacade minipopFacade;
    @EJB
    private InvoicePersistenceFacade invoiceFacade;
    @EJB
    private InMemoryManager inMemory;
    @EJB
    private SubscriberPersistenceFacade subscriberFacade;
    @EJB
    private ScratchCardPersistenceFacade scratchCardFacade;
    @EJB
    private SerialPersistenceFacade serialFacade;
    @EJB
    private ScratchCardSessionPersistenceFacade sessionFacade;
    @EJB
    private ServiceProviderPersistenceFacade providerPersistenceFacade;

    List<String> instantSearchResultLst;

    private String sqlQuery;

    private String agreement;
    private boolean exactSearch;
    private String identifier;
    private String subscriberFirstName;
    private String subscriberLastName;
    private String subscriberMiddleName;
    private String country;
    private String city;
    private String ats;
    private String street;
    private String building;
    private String apartment;
    private String cityOfBirth;
    private String citizenship;
    private String country_of_issue;
    private String passport_series;
    private String passport_number;
    private String issuedBy;
    private String valid_till;
    private String email;
    private String phone_mobile;
    private String phone_alt;
    private String phone_landline;
    private Date dateOfBirth;
    private Date creationDate;
    private Date entryDate;
    private String companyName;
    private boolean isRenderSubscriptionList = false;
    private LazyDataModel<Subscription> subscriptionList;
    private Subscription selectedSubscription;
    private Subscription selectedSubscriptionItem;
    private String taxID;
    private String userName;
    private boolean isRenderViewButton = false;
    private String tabTitle = "Subscription";

    //subscriber
    private String subscriberId;
    private String passportNumber;
    private String subscriberName;
    private String subscriberSurname;
    private LazyDataModel<Subscriber> subscriberList;
    private Subscriber selectedSubscriber;
    private UIComponent subscribersTable;

    //payment / invoice
    private String cheque;
    private String rrn;
    private Long serialId;
    private LazyTableModel<Payment> paymentData;
    private Payment selectedPayment;
    private String financeType = "Payment";
    private LazyDataModel<ScratchCardSession> sessionList;
    private ScratchCardSession selectedSession;

    private transient UIForm adminForm;
    private String keyword;

    //stock
    private List<String> stockTypeList;
    private List<SelectItem> stockTypeSelectList;
    private String stockType = "Equipment";
    private String partNumber;
    private LazyDataModel<Subscription> subscriptionData;
    private String macAddress;
    private String minipopSwitchName;
    private String miniPopPort;
    private String IP;

    private LazyTableModel miniPopData;
    private MiniPop selectedMiniPop;
    private String miniPopAddress;
    private String activeTabeID;

    //invoice
    private LazyDataModel<Invoice> invoiceList;
    private Long invoiceID;
    private String subscriberID;
    private InvoiceState searchInvoiceStatus;
    private List<SelectItem> searchInvoiceStatusList;
    private Invoice selectedInvoice;
    private String invoiceSubName;
    private String invoiceSubMiddleName;
    private String invoiceSubLastName;
    private UIComponent subscriptionsTable;

    SubscriptionStatus status;

    public ScratchCardSession getSelectedSession() {
        return selectedSession;
    }

    public void setSelectedSession(ScratchCardSession selectedSession) {
        this.selectedSession = selectedSession;
    }

    public Long getSerial() {
        return serialId;
    }

    public void setSerial(Long serialId) {
        this.serialId = serialId;
    }

    public LazyDataModel<ScratchCardSession> getSessionList() {
        return sessionList;
    }

    public void setSessionList(LazyDataModel<ScratchCardSession> sessionList) {
        this.sessionList = sessionList;
    }

    public String getTabTitle() {
        return tabTitle;
    }

    public void changeField(AjaxBehaviorEvent event) {
    }

    public String getAgreement() {
        return agreement;
    }

    public void setAgreement(String agreement) {
        this.agreement = agreement.trim();
    }

    public boolean isExactSearch() {
        return exactSearch;
    }

    public void setExactSearch(boolean exactSearch) {
        this.exactSearch = exactSearch;
    }

    public String getSubscriberFirstName() {
        return subscriberFirstName;
    }

    public void setSubscriberFirstName(String subscriberFirstName) {
        this.subscriberFirstName = subscriberFirstName;
    }

    public String getSubscriberLastName() {
        return subscriberLastName;
    }

    public void setSubscriberLastName(String subscriberLastName) {
        this.subscriberLastName = subscriberLastName;
    }

    public String getSubscriberMiddleName() {
        return subscriberMiddleName;
    }

    public void setSubscriberMiddleName(String subscriberMiddleName) {
        this.subscriberMiddleName = subscriberMiddleName;
    }

    public boolean isRenderSubscriptionList() {
        return isRenderSubscriptionList;
    }

    public void setRenderSubscriptionList(boolean isRenderSubscriptionList) {
        this.isRenderSubscriptionList = isRenderSubscriptionList;
    }

    public LazyDataModel<Subscription> getSubscriptionList() {
        return subscriptionList;
    }

    public void setSubscriptionList(LazyDataModel<Subscription> subscriptionList) {
        this.subscriptionList = subscriptionList;
    }

    public Subscription getSelectedSubscription() {
        return selectedSubscription;
    }

    public void setSelectedSubscription(Subscription selectedSubscription) {
        this.selectedSubscription = selectedSubscription;
    }


    public SubscriptionStatus getStatus() {
        return status;
    }

    public void setStatus(SubscriptionStatus status) {
        this.status = status;
    }

    public List<SubscriptionStatus> getStatuses() {
        List<SubscriptionStatus> list = new ArrayList<>();
        list.add(SubscriptionStatus.INITIAL);
        list.add(SubscriptionStatus.ACTIVE);
        list.add(SubscriptionStatus.PARTIALLY_BLOCKED);
        list.add(SubscriptionStatus.BLOCKED);
        list.add(SubscriptionStatus.SUSPENDED);
        list.add(SubscriptionStatus.PRE_FINAL);
        list.add(SubscriptionStatus.CANCEL);
        list.add(SubscriptionStatus.FINAL);
        return list;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public void setUserName(String name) {
        this.userName = name;
    }

    public String getUserName() {
        return this.userName;
    }

    public String getAts() {
        return ats;
    }

    public void setAts(String ats) {
        this.ats = ats;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getBuilding() {
        return building;
    }

    public void setBuilding(String building) {
        this.building = building;
    }

    public String getApartment() {
        return apartment;
    }

    public void setApartment(String apartment) {
        this.apartment = apartment;
    }

    public String getCityOfBirth() {
        return cityOfBirth;
    }

    public void setCityOfBirth(String cityOfBirth) {
        this.cityOfBirth = cityOfBirth;
    }

    public String getCitizenship() {
        return citizenship;
    }

    public void setCitizenship(String citizenship) {
        this.citizenship = citizenship;
    }

    public String getCountry_of_issue() {
        return country_of_issue;
    }

    public void setCountry_of_issue(String country_of_issue) {
        this.country_of_issue = country_of_issue;
    }

    public String getPassport_series() {
        return passport_series;
    }

    public void setPassport_series(String passport_series) {
        this.passport_series = passport_series;
    }

    public String getPassport_number() {
        return passport_number;
    }

    public void setPassport_number(String passport_number) {
        this.passport_number = passport_number;
    }

    public String getIssuedBy() {
        return issuedBy;
    }

    public void setIssuedBy(String issuedBy) {
        this.issuedBy = issuedBy;
    }

    public String getValid_till() {
        return valid_till;
    }

    public void setValid_till(String valid_till) {
        this.valid_till = valid_till;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone_mobile() {
        return phone_mobile;
    }

    public void setPhone_mobile(String phone_mobile) {
        this.phone_mobile = phone_mobile;
    }

    public String getPhone_alt() {
        return phone_alt;
    }

    public void setPhone_alt(String phone_alt) {
        this.phone_alt = phone_alt;
    }

    public String getPhone_landline() {
        return phone_landline;
    }

    public void setPhone_landline(String phone_landline) {
        this.phone_landline = phone_landline;
    }

    public boolean isRenderViewButton() {
        return isRenderViewButton;
    }

    public void setRenderViewButton(boolean isRenderViewButton) {
        this.isRenderViewButton = isRenderViewButton;
    }

    public Date getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(Date dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public Date getEntryDate() {
        return entryDate;
    }

    public void setEntryDate(Date entryDate) {
        this.entryDate = entryDate;
    }

    public UIForm getAdminForm() {
        return adminForm;
    }

    public void setAdminForm(UIForm adminForm) {
        this.adminForm = adminForm;
    }

    public String getKeyword() {
        //return String.valueOf(inMemory.getAllSubscriptions().size());
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    //subscriber

    public String getSubscriberId() {
        return subscriberId;
    }

    public void setSubscriberId(String subscriberId) {
        this.subscriberId = subscriberId;
    }

    public String getPassportNumber() {
        return passportNumber;
    }

    public void setPassportNumber(String passportNumber) {
        this.passportNumber = passportNumber;
    }

    public String getSubscriberName() {
        return subscriberName;
    }

    public void setSubscriberName(String subscriberName) {
        this.subscriberName = subscriberName;
    }

    public String getSubscriberSurname() {
        return subscriberSurname;
    }

    public void setSubscriberSurname(String subscriberSurname) {
        this.subscriberSurname = subscriberSurname;
    }

    public LazyDataModel<Subscriber> getSubscriberList() {
        return subscriberList;
    }

    public void setSubscriberList(LazyDataModel<Subscriber> subscriberList) {
        this.subscriberList = subscriberList;
    }

    public Subscriber getSelectedSubscriber() {
        return selectedSubscriber;
    }

    public void setSelectedSubcriber(Subscriber selectedSubscriber) {
        this.selectedSubscriber = selectedSubscriber;
    }

    public UIComponent getSubscribersTable() {
        return subscribersTable;
    }

    public void setSubscribersTable(UIComponent subscribersTable) {
        this.subscribersTable = subscribersTable;
    }

    //finance
    public String getRrn() {
        return rrn;
    }

    public void setRrn(String rrn) {
        this.rrn = rrn;
    }

    public String getCheque() {
        return cheque;
    }

    public void setCheque(String cheque) {
        this.cheque = cheque;
    }

    public String getIP() {
        return this.IP;
    }

    public void setIP(String IP) {
        this.IP = IP;
    }

    public void search(ActionEvent ev) throws OperationNotSupportedException {
        /*if ((agreement == null || agreement.isEmpty())
         && ((subscriberFirstName == null || subscriberFirstName.isEmpty())
         || (subscriberLastName == null || subscriberLastName.isEmpty())
         )) {
         FacesContext.getCurrentInstance().addMessage(null,
         new FacesMessage(FacesMessage.SEVERITY_ERROR,
         "Please provide Agreement number or Subscriber information",
         "Please provide Agreement number or Subscriber information"));
         return;
         }*/

        log.debug("tab: " + tabTitle);

        if (!(check() || checkSubscriber() || checkInvoiceId() || checkPayments() || checkEquipment() || checkMinipop() || checkIP() || checkSerial())) {
            String message = "Please fill any field";

            if (activeTabeID != null && activeTabeID.equals("stock") && stockType != null && stockType.equals("Minipop")) {
                message = "Please fill <a href='#' style='color: blue;' onclick=\"PF('macAddress').jq.focus(); return false;\">Mac address</a> or  <a href='#' style='color: blue;' onclick=\"PF('address').jq.focus(); return false;\">Address</a> field";
            }

            FacesContext.getCurrentInstance().addMessage(
                    null, new FacesMessage(FacesMessage.SEVERITY_ERROR, message, message));
            return;
        }

        log.debug("is invoice search? " + checkInvoice());
        long count = 0;
        //searchSubscriptions();

        /*if (checkInvoice()) {
         searchInvoices();
         log.debug(String.format("Finished invoice search. Found %d invoices", invoiceFacade.count()));
         }
         */
        //clearTableData();
        miniPopData = null;

        if (tabTitle.equals("Subscription") && check()) {
            //searchSubscriptionsSbnView();
            searchSubscriptions();
            count = subscriptionFacade.count();
            log.debug(String.format("Finished search. Found %d subscriptions", count));
        } else if (tabTitle.equals("Subscriber")) {
            if (checkSubscriber()) {
                searchSubscriber();
                if (sqlQuery != null) {
                    sqlQuery = sqlQuery.replaceFirst("distinct sub", "count(distinct sub.id)");
                    count = subscriberFacade.countDynamic(sqlQuery);
                }
                if (subscriberList != null) {
                    subscriberList.setRowCount((int) count);
                }
                log.debug(String.format("Finished search. Found %d subscriber", count));
            }
        } else if (tabTitle.equals("Finance")) {
            invoiceList = null;
            sessionList = null;
            paymentData = null;
            if (financeType.equals("Payment") && checkPayments()) {
                searchPayments();
                count = paymentFacade.count();
                log.debug(String.format("Finished search. Found %d payments", count));
            } else if (financeType.equals("Invoice")) {
                searchInvoices();
                count = invoiceFacade.count();
                log.debug(String.format("Finished invoice search. Found %d invoices", count));
            } else if (financeType.equals("Scratch card") && serialId != null) {
                searchCardSession();
            }
        } else if (stockType.equals("Equipment") && checkEquipment()) {
            searchByEquipment();
            count = subscriptionFacade.count();
            log.debug(String.format("Finished equipment search. Found %d subscription", count));
        } else if (stockType.equals("Minipop") && checkMinipop()) {
            searchMiniPop();
            count = minipopFacade.count();
            log.debug(String.format("Finished minipop search. Found %d minipops", count));
        } else if (checkIP()) {
            searchByIP();
            if (sqlQuery != null) {
                sqlQuery = sqlQuery.replaceAll("distinct vas.subscription", "count(distinct vas.subscription.id)");
                count = subscriptionFacade.countDynamic(sqlQuery);
            }
            if (subscriptionData != null) {
                subscriptionData.setRowCount((int) count);
            }
            log.debug(String.format("Finished IP search. Found %d subscription", count));
        }

        //if (subscriptionFacade.count() > 0)
        if (count > 0) {
            setRenderViewButton(true);
        }
        //return "/pages/admin.xhtml?faces-redirect=true";
    }

    private boolean check() {
        log.debug("aggrement: " + agreement);
        if ((agreement == null || agreement.isEmpty())
                && (identifier == null || identifier.isEmpty())
                && (taxID == null || taxID.isEmpty())
                && creationDate == null
                && (companyName == null || companyName.isEmpty())
                && (subscriberFirstName == null || subscriberFirstName.isEmpty())
                && (subscriberLastName == null || subscriberLastName.isEmpty())
                && (subscriberMiddleName == null || subscriberMiddleName.isEmpty())
                && (cityOfBirth == null || cityOfBirth.isEmpty())
                && (citizenship == null || citizenship.isEmpty())
                && (country == null || country.isEmpty())
                && (passport_series == null || passport_series.isEmpty())
                && (passport_number == null || passport_number.isEmpty())
                && (issuedBy == null || issuedBy.isEmpty())
                && (valid_till == null || valid_till.isEmpty())
                && (email == null || email.isEmpty())
                && (phone_mobile == null || phone_mobile.isEmpty())
                && (phone_alt == null || phone_alt.isEmpty())
                && (phone_landline == null || phone_landline.isEmpty())
                && (city == null || city.isEmpty())
                && (ats == null || ats.isEmpty())
                && (userName == null || userName.isEmpty())
                && (street == null || street.isEmpty())
                && (building == null || building.isEmpty())
                && (apartment == null || apartment.isEmpty())
                && dateOfBirth == null && entryDate == null
                && status == null
                && provider == null) {
            return false;
        }

        return true;

    }

    private boolean checkSubscriber() {
        if ((subscriberId == null || subscriberId.isEmpty()) && (subscriberName == null || subscriberName.isEmpty())
                && (subscriberSurname == null || subscriberSurname.isEmpty()) && (passportNumber == null || passportNumber.isEmpty())) {
            return false;
        }
        return true;

    }

    private boolean checkSerial() {
        if (serialId == null)
            return false;
        return true;
    }

    private boolean checkPayments() {
        if ((cheque == null || cheque.isEmpty())
                && (rrn == null || rrn.isEmpty())) {
            return false;
        }
        return true;
    }

    private boolean checkEquipment() {
        if ((partNumber == null || partNumber.isEmpty())) {
            return false;
        }
        return true;
    }

    private boolean checkMinipop() {
        if ((macAddress == null || macAddress.isEmpty()) && (minipopSwitchName == null || minipopSwitchName.isEmpty()) && (miniPopPort == null || miniPopPort.isEmpty()) && (miniPopAddress == null || miniPopAddress.isEmpty())) {
            return false;
        }
        return true;
    }

    private boolean checkInvoiceId() {
        if (invoiceID == null) return false;
        return true;
    }

    private boolean checkInvoice() {
        if ((subscriberID == null || subscriberID.isEmpty()) && invoiceID == null && searchInvoiceStatus == null
                && (subscriberFirstName == null || subscriberFirstName.isEmpty())
                && (subscriberMiddleName == null || subscriberMiddleName.isEmpty())
                && (subscriberLastName == null || subscriberLastName.isEmpty())) {
            return false;
        }
        return true;
    }

    private boolean checkIP() {
        if (IP == null || IP.isEmpty()) {
            return false;
        }
        return true;
    }

    private void searchSubscriptionsSbnViewbyKeyword() {
        viewFacade.clearFilters();

        viewFacade.addFilter(SubscriptionViewPersistenceFacade.Filter.AGREEMENT, keyword);

        viewFacade.addFilter(SubscriptionViewPersistenceFacade.Filter.CORPORATE_COMPANY, keyword);

        viewFacade.addFilter(SubscriptionViewPersistenceFacade.Filter.FIRSTNAME, keyword);

        viewFacade.addFilter(SubscriptionViewPersistenceFacade.Filter.LASTNAME, keyword);

        viewFacade.addFilter(SubscriptionViewPersistenceFacade.Filter.MIDDLENAME, keyword);

        viewFacade.addFilter(SubscriptionViewPersistenceFacade.Filter.CITY_OF_BIRTH, keyword);

        viewFacade.addFilter(SubscriptionViewPersistenceFacade.Filter.CITIZENSHIP, keyword);

        viewFacade.addFilter(SubscriptionViewPersistenceFacade.Filter.COUNTRY, keyword);

        viewFacade.addFilter(SubscriptionViewPersistenceFacade.Filter.PASSPORT_SERIES, keyword);

        viewFacade.addFilter(SubscriptionViewPersistenceFacade.Filter.PASSPORT_NUMBER, keyword);

        //viewFacade.addFilter(SubscriptionViewPersistenceFacade.Filter.PASSPORT_AUTHORITY, keyword);
        // viewFacade.addFilter(SubscriptionViewPersistenceFacade.Filter.PASSPORT_VALID, keyword);
        viewFacade.addFilter(SubscriptionViewPersistenceFacade.Filter.EMAIL, keyword);

        viewFacade.addFilter(SubscriptionViewPersistenceFacade.Filter.PHONE_MOBILE, keyword);

        viewFacade.addFilter(SubscriptionViewPersistenceFacade.Filter.PHONE_ALT, keyword);

        viewFacade.addFilter(SubscriptionViewPersistenceFacade.Filter.PHONE_LANDLINE, keyword);

        viewFacade.addFilter(SubscriptionViewPersistenceFacade.Filter.ADDRESS_CITY, keyword);

        viewFacade.addFilter(SubscriptionViewPersistenceFacade.Filter.ADDRESS_ATS, keyword);

        viewFacade.addFilter(SubscriptionViewPersistenceFacade.Filter.ADDRESS_STREET, keyword);

        viewFacade.addFilter(SubscriptionViewPersistenceFacade.Filter.ADDRESS_BUILDING, keyword);

        viewFacade.addFilter(SubscriptionViewPersistenceFacade.Filter.ADDRESS_APARTMENT, keyword);

        log.debug("Filters for search: " + viewFacade.getFilters());

        if (viewFacade.count() > 0) {
            setRenderViewButton(true);
        }
        //clearForm();
    }

    private void searchSubscriptionsSbnView() {
        viewFacade.clearFilters();
        viewFacade.setPredicateJoinOperation(AbstractPersistenceFacade.PredicateJoinOperation.AND);

        if (agreement != null && !agreement.isEmpty()) {
            viewFacade.addFilter(SubscriptionViewPersistenceFacade.Filter.AGREEMENT, agreement);
        } else if (creationDate != null) {
            viewFacade.addFilter(SubscriptionViewPersistenceFacade.Filter.CREATED_ON, creationDate);
        } else {
            if (companyName != null && !companyName.isEmpty()) {
                viewFacade.addFilter(SubscriptionViewPersistenceFacade.Filter.CORPORATE_COMPANY, companyName);
            }

            if (subscriberFirstName != null && !subscriberFirstName.isEmpty()) {
                viewFacade.addFilter(SubscriptionViewPersistenceFacade.Filter.FIRSTNAME, subscriberFirstName);
            }

            if (subscriberLastName != null && !subscriberLastName.isEmpty()) {
                viewFacade.addFilter(SubscriptionViewPersistenceFacade.Filter.LASTNAME, subscriberLastName);
            }

            if (subscriberMiddleName != null && !subscriberMiddleName.isEmpty()) {
                viewFacade.addFilter(SubscriptionViewPersistenceFacade.Filter.MIDDLENAME, subscriberMiddleName);
            }

            if (cityOfBirth != null && !cityOfBirth.isEmpty()) {
                viewFacade.addFilter(SubscriptionViewPersistenceFacade.Filter.CITY_OF_BIRTH, cityOfBirth);
            }

            if (citizenship != null && !citizenship.isEmpty()) {
                viewFacade.addFilter(SubscriptionViewPersistenceFacade.Filter.CITIZENSHIP, citizenship);
            }

            if (country != null && !country.isEmpty()) {
                viewFacade.addFilter(SubscriptionViewPersistenceFacade.Filter.COUNTRY, country);
            }

            if (passport_series != null && !passport_series.isEmpty()) {
                viewFacade.addFilter(SubscriptionViewPersistenceFacade.Filter.PASSPORT_SERIES, passport_series);
            }

            if (passport_number != null && !passport_number.isEmpty()) {
                viewFacade.addFilter(SubscriptionViewPersistenceFacade.Filter.PASSPORT_NUMBER, passport_number);
            }

            /*if (issuedBy != null && !issuedBy.isEmpty())
             viewFacade.addFilter(SubscriptionViewPersistenceFacade.Filter.PASSPORT_AUTHORITY, issuedBy);

             if (valid_till != null && !valid_till.isEmpty())
             viewFacade.addFilter(SubscriptionViewPersistenceFacade.Filter.PASSPORT_VALID, valid_till);
             */
            if (email != null && !email.isEmpty()) {
                viewFacade.addFilter(SubscriptionViewPersistenceFacade.Filter.EMAIL, email);
            }

            if (phone_mobile != null && !phone_mobile.isEmpty()) {
                viewFacade.addFilter(SubscriptionViewPersistenceFacade.Filter.PHONE_MOBILE, phone_mobile);
            }

            if (phone_alt != null && !phone_alt.isEmpty()) {
                viewFacade.addFilter(SubscriptionViewPersistenceFacade.Filter.PHONE_ALT, phone_alt);
            }

            if (phone_landline != null && !phone_landline.isEmpty()) {
                viewFacade.addFilter(SubscriptionViewPersistenceFacade.Filter.PHONE_LANDLINE, phone_landline);
            }

            if (city != null && !city.isEmpty()) {
                viewFacade.addFilter(SubscriptionViewPersistenceFacade.Filter.ADDRESS_CITY, city);
            }

            if (ats != null && !ats.isEmpty()) {
                viewFacade.addFilter(SubscriptionViewPersistenceFacade.Filter.ADDRESS_ATS, ats);
            }

            if (street != null && !street.isEmpty()) {
                viewFacade.addFilter(SubscriptionViewPersistenceFacade.Filter.ADDRESS_STREET, street);
            }

            if (building != null && !building.isEmpty()) {
                viewFacade.addFilter(SubscriptionViewPersistenceFacade.Filter.ADDRESS_BUILDING, building);
            }

            if (apartment != null && !apartment.isEmpty()) {
                viewFacade.addFilter(SubscriptionViewPersistenceFacade.Filter.ADDRESS_APARTMENT, apartment);
            }

            if (dateOfBirth != null) {
                viewFacade.addFilter(SubscriptionViewPersistenceFacade.Filter.DATE_OF_BIRTH, dateOfBirth);
            }

            log.debug("creation date: " + entryDate);

            if (entryDate != null) {
                Map<String, Date> entryDateMap = new HashMap<>();
                entryDateMap.put("from", entryDate);
                entryDateMap.put("to", new DateTime(entryDate).withTime(23, 59, 59, 999).toDate());
                SubscriptionViewPersistenceFacade.Filter entryDateFilter = SubscriptionViewPersistenceFacade.Filter.ENTRY_DATE;
                entryDateFilter.setOperation(MatchingOperation.BETWEEN);

                viewFacade.addFilter(SubscriptionViewPersistenceFacade.Filter.ENTRY_DATE, entryDateMap);
            }
        }

        log.debug("Filters for search: " + viewFacade.getFilters());
        //subscriptionViewList = new LazyTableModel<>(viewFacade);
        //clearForm();
    }

    private void searchSubscriptions() {

        subscriptionList = null;
        subscriptionFacade.clearFilters();
//        ((DataTable) subscriptionsTable).reset();

        if (agreement != null && !agreement.isEmpty()) {
            if (isExactSearch()) {
                SubscriptionPersistenceFacade.Filter agreementFilter = SubscriptionPersistenceFacade.Filter.AGREEMENT;
                agreementFilter.setOperation(MatchingOperation.EQUALS);
                subscriptionFacade.addFilter(agreementFilter, agreement);
            } else {
                SubscriptionPersistenceFacade.Filter agreementFilter = SubscriptionPersistenceFacade.Filter.AGREEMENT;
                agreementFilter.setOperation(MatchingOperation.LIKE);
                subscriptionFacade.addFilter(agreementFilter, FrontUniversalTool.applyPattern(agreement));
            }
        }

        if (status != null) {
            subscriptionFacade.addFilter(SubscriptionPersistenceFacade.Filter.STATUS, status);
        }

        if (provider != null) {
            subscriptionFacade.addFilter(SubscriptionPersistenceFacade.Filter.PROVIDER, providerPersistenceFacade.findByName(provider));
        }

        if (identifier != null && !identifier.isEmpty()) {
            subscriptionFacade.addFilter(SubscriptionPersistenceFacade.Filter.IDENTIFIER, identifier);
        }

        if (taxID != null && !taxID.isEmpty()) {
            subscriptionFacade.addFilter(SubscriptionPersistenceFacade.Filter.PASSPORT_NUMBER, taxID);
            subscriptionFacade.addFilter(SubscriptionPersistenceFacade.Filter.SUBSCRIBER_TYPE, SubscriberType.CORP);
        } else if (creationDate != null) {
            subscriptionFacade.addFilter(SubscriptionPersistenceFacade.Filter.CREATED_ON, creationDate);
        } else {
            if (companyName != null && !companyName.isEmpty()) {
                subscriptionFacade.addFilter(SubscriptionPersistenceFacade.Filter.CORPORATE_COMPANY, companyName);
            }

            if (subscriberFirstName != null && !subscriberFirstName.isEmpty()) {
                subscriptionFacade.addFilter(SubscriptionPersistenceFacade.Filter.FIRSTNAME, subscriberFirstName);
            }

            if (subscriberLastName != null && !subscriberLastName.isEmpty()) {
                subscriptionFacade.addFilter(SubscriptionPersistenceFacade.Filter.LASTNAME, subscriberLastName);
            }

            if (subscriberMiddleName != null && !subscriberMiddleName.isEmpty()) {
                subscriptionFacade.addFilter(SubscriptionPersistenceFacade.Filter.MIDDLENAME, subscriberMiddleName);
            }

            if (cityOfBirth != null && !cityOfBirth.isEmpty()) {
                subscriptionFacade.addFilter(SubscriptionPersistenceFacade.Filter.CITY_OF_BIRTH, cityOfBirth);
            }

            if (citizenship != null && !citizenship.isEmpty()) {
                subscriptionFacade.addFilter(SubscriptionPersistenceFacade.Filter.CITIZENSHIP, citizenship);
            }

            if (country != null && !country.isEmpty()) {
                subscriptionFacade.addFilter(SubscriptionPersistenceFacade.Filter.COUNTRY, country);
            }

            if (passport_series != null && !passport_series.isEmpty()) {
                subscriptionFacade.addFilter(SubscriptionPersistenceFacade.Filter.PASSPORT_SERIES, passport_series);
            }

            if (passport_number != null && !passport_number.isEmpty()) {
                subscriptionFacade.addFilter(SubscriptionPersistenceFacade.Filter.PASSPORT_NUMBER, passport_number);
            }

            if (issuedBy != null && !issuedBy.isEmpty()) {
                subscriptionFacade.addFilter(SubscriptionPersistenceFacade.Filter.PASSPORT_AUTHORITY, issuedBy);
            }

            if (valid_till != null && !valid_till.isEmpty()) {
                subscriptionFacade.addFilter(SubscriptionPersistenceFacade.Filter.PASSPORT_VALID, valid_till);
            }

            if (email != null && !email.isEmpty()) {
                subscriptionFacade.addFilter(SubscriptionPersistenceFacade.Filter.EMAIL, email);
            }

            if (phone_mobile != null && !phone_mobile.isEmpty()) {
                subscriptionFacade.addFilter(SubscriptionPersistenceFacade.Filter.PHONE_MOBILE, phone_mobile);
            }

            if (phone_alt != null && !phone_alt.isEmpty()) {
                subscriptionFacade.addFilter(SubscriptionPersistenceFacade.Filter.PHONE_ALT, phone_alt);
            }

            if (phone_landline != null && !phone_landline.isEmpty()) {
                subscriptionFacade.addFilter(SubscriptionPersistenceFacade.Filter.PHONE_LANDLINE, phone_landline);
            }

            if (city != null && !city.isEmpty()) {
                subscriptionFacade.addFilter(SubscriptionPersistenceFacade.Filter.ADDRESS_CITY, city);
            }

            if (userName != null && !userName.isEmpty()) {
                subscriptionFacade.addFilter(SubscriptionPersistenceFacade.Filter.USERNAME, userName);
            }

            if (ats != null && !ats.isEmpty()) {
                subscriptionFacade.addFilter(SubscriptionPersistenceFacade.Filter.ADDRESS_ATS, ats);
            }

            if (street != null && !street.isEmpty()) {
                subscriptionFacade.addFilter(SubscriptionPersistenceFacade.Filter.ADDRESS_STREET, street);
            }

            if (building != null && !building.isEmpty()) {
                subscriptionFacade.addFilter(SubscriptionPersistenceFacade.Filter.ADDRESS_BUILDING, building);
            }

            if (apartment != null && !apartment.isEmpty()) {
                subscriptionFacade.addFilter(SubscriptionPersistenceFacade.Filter.ADDRESS_APARTMENT, apartment);
            }

            if (dateOfBirth != null) {
                subscriptionFacade.addFilter(SubscriptionPersistenceFacade.Filter.DATE_OF_BIRTH, dateOfBirth);
            }

            log.debug("creation date: " + entryDate);

            if (entryDate != null) {
                subscriptionFacade.addFilter(SubscriptionPersistenceFacade.Filter.ENTRY_DATE, entryDate);
            }
        }

        log.debug("Filters for search: " + subscriptionFacade.getFilters());
        subscriptionList = new LazyTableModel<>(subscriptionFacade);
        //clearForm();
    }

    public void searchSubscriber() {

        subscriberFacade.clearFilters();

        if (subscriberId != null && !subscriberId.isEmpty()) {
            subscriberFacade.addFilter(SubscriberPersistenceFacade.Filter.ID, subscriberId);
        }

        if (passportNumber != null && !passportNumber.isEmpty()) {
            subscriberFacade.addFilter(SubscriberPersistenceFacade.Filter.PASSPORTNUMBER, passportNumber);
        }

        if (subscriberName != null && !subscriberName.isEmpty()) {
            subscriberFacade.addFilter(SubscriberPersistenceFacade.Filter.NAME, subscriberName);
        }

        if (subscriberSurname != null && !subscriberSurname.isEmpty()) {
            subscriberFacade.addFilter(SubscriberPersistenceFacade.Filter.SURNAME, subscriberSurname);
        }

        log.debug("subscriber filters: " + subscriberFacade.getFilters());
        sqlQuery = subscriberFacade.getSubsciberSqlQuery(subscriberFacade.getFilters());
        log.debug("subscriber sqlQuery: " + sqlQuery);
        subscriberList = new LazyTableModelDynamic(subscriberFacade, Subscriber.class, sqlQuery);

    }

    public void clearTableData() {
        log.debug("search from clearenceData");

        //subscriptionViewList = null;
        invoiceList = null;
        subscriptionData = null;
        subscriptionList = null;
        paymentData = null;
        miniPopData = null;
        subscriberList = null;
        sessionList = null;
    }

    public void clearForm(ActionEvent event) {
        log.debug("search form clearance");

        agreement = null;
        identifier = null;
        selectedSubscriptionItem = null;
        subscriberFirstName = null;
        subscriberLastName = null;
        subscriberMiddleName = null;
        cityOfBirth = null;
        citizenship = null;
        country = null;
        passport_series = null;
        passport_number = null;
        issuedBy = null;
        valid_till = null;
        email = null;
        phone_mobile = null;
        phone_landline = null;
        phone_alt = null;
        city = null;
        userName = null;
        ats = null;
        street = null;
        building = null;
        apartment = null;
        companyName = null;
        entryDate = null;
        status = null;

        //subscriber
        subscriberId = null;
        subscriberName = null;
        subscriberSurname = null;
        passportNumber = null;
        subscriberList = null;

        keyword = null;
        //payment
        cheque = null;
        rrn = null;
        paymentData = null;
        paymentFacade.clearFilters();

        //stock
        IP = null;
        partNumber = null;
        macAddress = null;
        miniPopPort = null;
        minipopSwitchName = null;
        miniPopAddress = null;

        subscriptionData = null;
        subscriptionList = null;
        subscriptionFacade.clearFilters();
        miniPopData = null;
        minipopFacade.clearFilters();
        subscriptionList = null;
        viewFacade.clearFilters();

        //invoice
        invoiceID = null;
        invoiceList = null;
        invoiceFacade.clearFilters();
        subscriberID = null;
        searchInvoiceStatus = null;
        invoiceSubName = null;
        invoiceSubMiddleName = null;
        invoiceSubLastName = null;

        serialId = null;
    }

    public String view() {
        log.debug("selected subscription: " + selectedSubscription);
        if (selectedSubscriptionItem == null) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Please select subscription first", "Please select subscription first"));
            return null;
        }
        String result = null;

        if (selectedSubscriptionItem != null) {
            result = String.format("/pages/subscription/view/view_%d.xhtml?subscription=%d&amp;subscriber=%d&amp;faces-redirect=true&amp;includeViewParams=true", selectedSubscriptionItem.getService().getProvider().getId(), selectedSubscriptionItem.getId(), selectedSubscriptionItem.getSubscriber().getId());
        } else {
            result = String.format("/pages/subscriber/view/view_%d.xhtml?subscriber=%d&amp;faces-redirect=true&amp;includeViewParams=true", selectedSubscriptionItem.getService().getProvider().getId(), selectedSubscriptionItem.getSubscriber().getId());
        }

        log.debug("view: result=" + result);
        return result;
    }

    public String viewSubscriberDetails() {
        if (selectedSubscriber == null) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Please select subscriber first", "Please select subscriber first"));
            return null;
        }
        return String.format("/pages/subscriber/view.xhtml?subscriber=%d&amp;faces-redirect=true&amp;includeViewParams=true", selectedSubscriber.getId());
    }

    public String viewSubscriber() {
        log.debug("selected subscription: " + selectedSubscription);
        if (selectedSubscriptionItem == null) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Please select subscription first", "Please select subscription first"));
            return null;
        }
        String result = null;

        if (selectedSubscriptionItem != null) {
            result = String.format("/pages/subscriber/view.xhtml?subscriber=%d&amp;faces-redirect=true&amp;includeViewParams=true", selectedSubscriptionItem.getSubscriber().getId());
        }

        log.debug("view: result=" + result);
        return result;
    }

    public String viewSubView() {
        log.debug("selected subscription: " + selectedSubscription);
        if (selectedSubscription == null) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Please select subscription first", "Please select subscription first"));
            return null;
        }
        String result = null;

        log.debug("viewSub: " + selectedSubscription);
        if (selectedSubscription != null) {
            result = String.format("/pages/subscription/view.xhtml?subscription=%d&amp;subscriber=%d&amp;faces-redirect=true&amp;includeViewParams=true", selectedSubscription.getId(), selectedSubscription.getSubscriber().getId());
        }

        log.debug("view: result=" + result);
        return result;
    }

    public void setFieldsToKeyword(ComponentSystemEvent ev) {
        //log.debug("Keyword: " + getKeyword());
        if (keyword == null || keyword.isEmpty()) {
            return;
        }

        agreement = keyword;
        subscriberFirstName = keyword;
        subscriberLastName = keyword;
        subscriberMiddleName = keyword;
        country = keyword;
        city = keyword;
        userName = keyword;
        ats = keyword;
        street = keyword;
        building = keyword;
        apartment = keyword;
        cityOfBirth = keyword;
        citizenship = keyword;
        passport_series = keyword;
        passport_number = keyword;
        email = keyword;
        phone_mobile = keyword;
        phone_alt = keyword;
        phone_landline = keyword;
        //companyName = keyword;
    }

    public LazyTableModel<Payment> getPaymentData() {
        return paymentData;
    }

    public void setPaymentData(LazyTableModel<Payment> paymentData) {
        this.paymentData = paymentData;
    }

    public Payment getSelectedPayment() {
        return selectedPayment;
    }

    public void setSelectedPayment(Payment selectedPayment) {
        this.selectedPayment = selectedPayment;
    }

    public void searchPayments() {

        paymentData = null;
        paymentFacade.clearFilters();

        if (rrn != null && !rrn.isEmpty()) {
            PaymentPersistenceFacade.Filter rrnFilter = PaymentPersistenceFacade.Filter.RRN;
            rrnFilter.setOperation(MatchingOperation.EQUALS);
            paymentFacade.addFilter(rrnFilter, rrn);
        }

        if (cheque != null && !cheque.isEmpty()) {
            PaymentPersistenceFacade.Filter chequeFilter = PaymentPersistenceFacade.Filter.CHEQUE_ID;
            chequeFilter.setOperation(MatchingOperation.EQUALS);
            paymentFacade.addFilter(chequeFilter, cheque);
        }

        paymentData = new LazyTableModel<>(paymentFacade);
    }

    public String viewPayment() {
        if (selectedPayment == null) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "PLease select a payment", "PLease select a payment"));
        }

        return String.format("/pages/subscriber/ind/payment.xhtml?paymentID=%d&amp;subscriber=%d&amp;faces-redirect=true&amp;includeViewParams=true",
                selectedPayment.getId(), selectedPayment.getSubscriber_id());
    }

    public List<String> getStockTypeList() {
        if (stockTypeList == null) {
            stockTypeList = new ArrayList<>();
            stockTypeList.add("Equipment");
            stockTypeList.add("Minipop");
            stockTypeList.add("IP");
        }
        return stockTypeList;
    }

    public void setStockTypeList(List<String> stockTypeList) {
        this.stockTypeList = stockTypeList;
    }

    public String getStockType() {
        return stockType;
    }

    public void setStockType(String stockType) {
        this.stockType = stockType;
    }

    public List<SelectItem> getStockTypeSelectList() {
        if (stockTypeSelectList == null) {
            stockTypeSelectList = new ArrayList<>();

            for (String type : getStockTypeList()) {
                stockTypeSelectList.add(new SelectItem(type));
            }
        }
        return stockTypeSelectList;
    }

    public void setStockTypeSelectList(List<SelectItem> stockTypeSelectList) {
        this.stockTypeSelectList = stockTypeSelectList;
    }

    public void onStockTypeChange(AjaxBehaviorEvent ev) {

    }

    public String getPartNumber() {
        return partNumber;
    }

    public void setPartNumber(String partNumber) {
        this.partNumber = partNumber;
    }

    public LazyDataModel<Subscription> getSubscriptionData() {
        return subscriptionData;
    }

    public void setSubscriptionData(LazyDataModel<Subscription> subscriptionData) {
        this.subscriptionData = subscriptionData;
    }

    public String viewEquipmentOwner() {
        if (selectedSubscription == null) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Please select a Subscription", "Please select a Subscription"));
            return null;
        }

        long providerId = selectedSubscription.getService().getProvider().getId();
        return String.format("/pages/subscription/view/view_%d.xhtml?subscription=%d&amp;faces-redirect=true&amp;includeViewParams=true", providerId, selectedSubscription.getId());
    }

    public String viewSearchPage() {
        if (keyword != null && !keyword.isEmpty()) {
            RequestContext.getCurrentInstance().update("adminForm");
            log.debug("update Finished " + subscriptionList);
            return String.format("/admin.xhtml?keyword=%s&amp;includeViewParams=true&amp;faces-redirect=true", keyword);
        }
        return null;
    }

    public void searchByKeyword() throws IOException {

        if (keyword == null || keyword.isEmpty()) {
            return;
        }
        /*
        subscriptionFacade.setPredicateJoinOperation(AbstractPersistenceFacade.PredicateJoinOperation.OR);
        subscriptionList = null;
        subscriptionFacade.clearFilters();
        subscriptionFacade.addFilter(SubscriptionPersistenceFacade.Filter.AGREEMENT, keyword);
        subscriptionFacade.addFilter(SubscriptionPersistenceFacade.Filter.IDENTIFIER, keyword);
        subscriptionFacade.addFilter(SubscriptionPersistenceFacade.Filter.SUBSCRIBER_NAME, keyword);
        subscriptionFacade.addFilter(SubscriptionPersistenceFacade.Filter.SUBSCRIBER_SURNAME, keyword);
        subscriptionFacade.addFilter(SubscriptionPersistenceFacade.Filter.ADDRESS_CITY, keyword);
        subscriptionFacade.addFilter(SubscriptionPersistenceFacade.Filter.ADDRESS_STREET, keyword);
        subscriptionFacade.addFilter(SubscriptionPersistenceFacade.Filter.USERNAME, keyword);
        subscriptionFacade.addFilter(SubscriptionPersistenceFacade.Filter.PHONE_MOBILE, keyword);
        subscriptionFacade.addFilter(SubscriptionPersistenceFacade.Filter.PASSPORT_NUMBER, keyword);
        
        subscriptionList = new LazyTableModel(subscriptionFacade);
         */
        sqlQuery = subscriptionFacade.searchByKeyword(keyword);
        subscriptionList = new LazyTableModelDynamic(subscriptionFacade, Subscription.class, sqlQuery);
        sqlQuery = sqlQuery.replaceFirst("distinct s", "count(distinct s.id)");
        long count = subscriptionFacade.countDynamic(sqlQuery);
        subscriptionList.setRowCount((int) count);
        log.debug(String.format("Finished search. Found %d subscriptionsByKeyword", count));

        if (count > 0) {
            setRenderViewButton(true);
        }

        FacesContext.getCurrentInstance().getExternalContext().redirect("/admin.xhtml");
        RequestContext.getCurrentInstance().update("adminForm");
        //return viewSearchPage();
        //clearForm(null);
    }

    private void searchByIP() {

        subscriptionData = null;
        subscriptionFacade.clearFilters();

        if (checkIP()) {
            SubscriptionPersistenceFacade.Filter equipmentPartNumberFilter = SubscriptionPersistenceFacade.Filter.IP;
            equipmentPartNumberFilter.setOperation(MatchingOperation.LIKE);
            subscriptionFacade.addFilter(equipmentPartNumberFilter, IP);
            subscriptionFacade.addFilter(SubscriptionPersistenceFacade.Filter.BUCKET_TYPE, ResourceBucketType.INTERNET_IP_ADDRESS);
            sqlQuery = subscriptionFacade.findSubscriptionByIp(subscriptionFacade.getFilters());
        }

        subscriptionData = new LazyTableModelDynamic(subscriptionFacade, Subscription.class, sqlQuery);
    }

    private void searchCardSession() {

        Serial serial = serialFacade.find(serialId);
        ScratchCard scratchCard = scratchCardFacade.findBySerial(serial);
        ScratchCardSession cardSession = scratchCardFacade.findSessionByCard(scratchCard);
        sqlQuery = sessionFacade.getSessionQuery(cardSession.getId());
        sessionList = new LazyTableModelDynamic(sessionFacade, ScratchCardSession.class, sqlQuery);
        sqlQuery = sqlQuery.replaceFirst("distinct ss", "count(distinct ss.id)");
        long count = sessionFacade.countDynamic(sqlQuery);
        sessionList.setRowCount((int) count);
        log.debug(String.format("Finished search. Found %d sessionBySerial", count));

        if (count > 0) {
            setRenderViewButton(true);
        }
    }

    private void searchByEquipment() {

        subscriptionData = null;
        subscriptionFacade.clearFilters();

        if (partNumber != null && !partNumber.isEmpty()) {
            SubscriptionPersistenceFacade.Filter equipmentPartNumberFilter = SubscriptionPersistenceFacade.Filter.EQUIPMENT_PARTNUMBER;
            equipmentPartNumberFilter.setOperation(MatchingOperation.LIKE);
            subscriptionFacade.addFilter(equipmentPartNumberFilter, partNumber);
            subscriptionFacade.addFilter(SubscriptionPersistenceFacade.Filter.SETTING_TYPE, ServiceSettingType.TV_EQUIPMENT);
        }

        subscriptionData = new LazyTableModel<>(subscriptionFacade);
    }

    public String searchMiniPop() {

        miniPopData = null;
        minipopFacade.clearFilters();

        if (macAddress != null && !macAddress.isEmpty()) {
            MiniPopPersistenceFacade.Filter macAddressFilter = MiniPopPersistenceFacade.Filter.MAC_ADDRESS;
            macAddressFilter.setOperation(MatchingOperation.LIKE);
            minipopFacade.addFilter(macAddressFilter, macAddress);
        }

        if (miniPopAddress != null && !miniPopAddress.isEmpty()) {
            MiniPopPersistenceFacade.Filter addressFilter = MiniPopPersistenceFacade.Filter.ADDRESS;
            addressFilter.setOperation(MatchingOperation.LIKE);
            minipopFacade.addFilter(addressFilter, miniPopAddress);
        }

        if (minipopSwitchName != null && !minipopSwitchName.isEmpty()) {
            MiniPopPersistenceFacade.Filter addressFilter = MiniPopPersistenceFacade.Filter.SWITCH_ID;
            addressFilter.setOperation(MatchingOperation.LIKE);
            minipopFacade.addFilter(addressFilter, minipopSwitchName);
        }

        if (miniPopPort != null && !miniPopPort.isEmpty()) {
            MiniPopPersistenceFacade.Filter addressFilter = MiniPopPersistenceFacade.Filter.PORT;
            addressFilter.setOperation(MatchingOperation.LIKE);
            minipopFacade.addFilter(addressFilter, miniPopPort);
        }

        miniPopData = new LazyTableModel(minipopFacade);
        RequestContext ctx = RequestContext.getCurrentInstance();

        //ctx.execute("mpDialog.show()");
        log.debug("searchMiniPop: returned");
        return null;
    }

    public void showSubscriptionsOnMinipop() {
        log.debug("enter search subscriptions in minipop: selectedMinipop=" + selectedMiniPop);
        subscriptionFacade.clearFilters();

        if (selectedMiniPop != null) {
            log.debug("Searching subscriptions on minipop " + selectedMiniPop);
            /*SubscriptionPersistenceFacade.Filter macAddressFilter = SubscriptionPersistenceFacade.Filter.MINIPOP_MAC_ADDRESS;
            macAddressFilter.setOperation(MatchingOperation.LIKE);
            subscriptionFacade.addFilter(macAddressFilter, selectedMiniPop.getMac());
            subscriptionFacade.addFilter(SubscriptionPersistenceFacade.Filter.SETTING_TYPE, ServiceSettingType.USERNAME);*/

            SubscriptionPersistenceFacade.Filter switchIdFilter = SubscriptionPersistenceFacade.Filter.MINIPOP_MAC_ADDRESS;
            switchIdFilter.setOperation(MatchingOperation.EQUALS);
            subscriptionFacade.addFilter(switchIdFilter, selectedMiniPop.getId());
            subscriptionFacade.addFilter(SubscriptionPersistenceFacade.Filter.SETTING_TYPE, ServiceSettingType.BROADBAND_SWITCH);

            if (miniPopPort != null && !miniPopPort.isEmpty()) {
                SubscriptionPersistenceFacade.Filter minipopPortFilter = SubscriptionPersistenceFacade.Filter.MINIPOP_PORT;
                minipopPortFilter.setOperation(MatchingOperation.EQUALS);
                subscriptionFacade.addFilter(minipopPortFilter, miniPopPort);
                subscriptionFacade.addFilter(SubscriptionPersistenceFacade.Filter.SETTING_TYPE, ServiceSettingType.BROADBAND_SWITCH_PORT);
            }

            subscriptionData = new LazyTableModel<>(subscriptionFacade);
            miniPopData = null;
            minipopFacade.clearFilters();
        }
    }

    public String getSwitchName() {
        return this.minipopSwitchName;
    }

    public void setSwitchName(String switchName) {
        this.minipopSwitchName = switchName;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    public String getMiniPopPort() {
        return miniPopPort;
    }

    public void setMiniPopPort(String miniPopPort) {
        this.miniPopPort = miniPopPort;
    }

    public LazyTableModel getMiniPopData() {
        return miniPopData;
    }

    public void setMiniPopData(LazyTableModel miniPopData) {
        this.miniPopData = miniPopData;
    }

    public MiniPop getSelectedMiniPop() {
        return selectedMiniPop;
    }

    public void setSelectedMiniPop(MiniPop selectedMiniPop) {
        this.selectedMiniPop = selectedMiniPop;
    }

    public String getMiniPopAddress() {
        return miniPopAddress;
    }

    public void setMiniPopAddress(String miniPopAddress) {
        this.miniPopAddress = miniPopAddress;
    }

    public void onTabChange(TabChangeEvent event) {
        activeTabeID = event.getTab().getId();
        tabTitle = event.getTab().getTitle();
        log.debug("tab: " + tabTitle);
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

    public InvoiceState getSearchInvoiceStatus() {
        return searchInvoiceStatus;
    }

    public void setSearchInvoiceStatus(InvoiceState searchInvoiceStatus) {
        this.searchInvoiceStatus = searchInvoiceStatus;
    }

    public String getSubscriberID() {
        return subscriberID;
    }

    public void setSubscriberID(String sbID) {
        this.subscriberID = sbID;
    }

    public Long getInvoiceID() {
        return invoiceID;
    }

    public void setInvoiceID(Long invoiceID) {
        this.invoiceID = invoiceID;
    }

    public LazyDataModel<Invoice> getInvoiceList() {
        return invoiceList;
    }

    public void setInvoiceList(LazyDataModel<Invoice> invoiceList) {
        this.invoiceList = invoiceList;
    }

    public String getInvoiceSubName() {
        return invoiceSubName;
    }

    public void setInvoiceSubName(String invoiceSubName) {
        this.invoiceSubName = invoiceSubName;
    }

    public String getInvoiceSubMiddleName() {
        return invoiceSubMiddleName;
    }

    public void setInvoiceSubMiddleName(String invoiceSubMiddleName) {
        this.invoiceSubMiddleName = invoiceSubMiddleName;
    }

    public String getInvoiceSubLastName() {
        return invoiceSubLastName;
    }

    public void setInvoiceSubLastName(String invoiceSubLastName) {
        this.invoiceSubLastName = invoiceSubLastName;
    }

    public void searchInvoices() {

        invoiceList = null;

        if (invoiceID == null) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Please fill the fields", "Please fill the fields"));
            return;
        }

        invoiceFacade.clearFilters();

        if (cheque != null && !cheque.isEmpty()) {
            InvoicePersistenceFacade.Filter invoiceIdFilter = InvoicePersistenceFacade.Filter.ID;
            invoiceIdFilter.setMatchingOperation(MatchingOperation.EQUALS);
            invoiceFacade.addFilter(invoiceIdFilter, Long.valueOf(cheque));
        }

        //log.debug("searchInvoices: invoce filters="+invoiceFacade.getFilters());
        invoiceList = new LazyTableModel<>(invoiceFacade);
    }

    public void searchInvoicesFull() {

        invoiceList = null;

        if (invoiceID == null && searchInvoiceStatus == null
                && (invoiceSubName == null || invoiceSubName.isEmpty())
                && (invoiceSubMiddleName == null || invoiceSubMiddleName.isEmpty())
                && (invoiceSubLastName == null || invoiceSubLastName.isEmpty())) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Please fill the fields", "Please fill the fields"));
            return;
        }

        invoiceFacade.clearFilters();

        if (invoiceID != null) {
            InvoicePersistenceFacade.Filter invoiceIdFilter = InvoicePersistenceFacade.Filter.ID;
            invoiceIdFilter.setMatchingOperation(MatchingOperation.EQUALS);
            invoiceFacade.addFilter(invoiceIdFilter, invoiceID);
        }

        if (subscriberID != null && !subscriberID.isEmpty()) {
            invoiceFacade.addFilter(InvoicePersistenceFacade.Filter.SUBSCRIBER_ID, subscriberID);
        }

        if (searchInvoiceStatus != null) {
            invoiceFacade.addFilter(InvoicePersistenceFacade.Filter.STATE, searchInvoiceStatus);
        }

        if (invoiceSubName != null && !invoiceSubName.isEmpty()) {
            invoiceFacade.addFilter(InvoicePersistenceFacade.Filter.SUBSCRIBER_NAME, invoiceSubName);
        }

        if (invoiceSubMiddleName != null && !invoiceSubMiddleName.isEmpty()) {
            invoiceFacade.addFilter(InvoicePersistenceFacade.Filter.SUBSCRIBER_MIDDLENAME, invoiceSubMiddleName);
        }

        if (invoiceSubLastName != null && !invoiceSubLastName.isEmpty()) {
            invoiceFacade.addFilter(InvoicePersistenceFacade.Filter.SUBSCRIBER_SURNAME, invoiceSubLastName);
        }
        log.debug("searchInvoices: invoce filters=" + invoiceFacade.getFilters());
        invoiceList = new LazyTableModel<>(invoiceFacade);
    }

    public String viewInvoice() {
        if (selectedInvoice == null) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Please select invoice", "Please select invoice"));
        }
        invoiceFacade.clearFilters();
        return String.format("pages/subscriber/ind/view_invoice.xhtml?invoice=%d&amp;subscriber=%d&amp;faces-redirect=true&amp;includeViewParams=true", selectedInvoice.getId(), selectedInvoice.getSubscriber().getId());
    }

    public Invoice getSelectedInvoice() {
        return selectedInvoice;
    }

    public void setSelectedInvoice(Invoice selectedInvoice) {
        this.selectedInvoice = selectedInvoice;
    }

    public String getFinanceType() {
        return financeType;
    }

    public void setFinanceType(String financeType) {
        this.financeType = financeType;
    }

    public UIComponent getSubscriptionsTable() {
        return subscriptionsTable;
    }

    public void setSubscriptionsTable(UIComponent subscriptionsTable) {
        this.subscriptionsTable = subscriptionsTable;
    }

    public Subscription getSelectedSubscriptionItem() {
        return selectedSubscriptionItem;
    }

    public void setSelectedSubscriptionItem(Subscription selectedSubscriptionItem) {
        this.selectedSubscriptionItem = selectedSubscriptionItem;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getTaxID() {
        return taxID;
    }

    public void setTaxID(String taxID) {
        this.taxID = taxID;
    }

    public List<String> completeText(String pattern) {

        if (instantSearchResultLst == null) {
            instantSearchResultLst = new LinkedList();
        }
        int count = 0;
        instantSearchResultLst.clear();

//        Subscription sbn = null;
//
//        log.debug("pattern: " + pattern + " size: " + inMemory.getAllSubscriptions().size());
//
//        try {
//            for (Subscription s : inMemory.getAllSubscriptions()) {
//                sbn = s;
//                if (count == 8) {
//                    break;
//                }
//
//                if (checkMatching(sbn.getAgreement(), pattern)) {
//                    if (!instantSearchResultLst.contains(sbn.getAgreement())) {
//                        instantSearchResultLst.add(sbn.getAgreement());
//                        count++;
//                    }
//                }
//
//                if (checkMatching(sbn.getIdentifier(), pattern)) {
//                    if (!instantSearchResultLst.contains(sbn.getIdentifier())) {
//                        instantSearchResultLst.add(sbn.getIdentifier());
//                        count++;
//                    }
//                }
//
//                if (checkMatching(sbn.getSubscriber().getDetails().getFirstName() + " " + sbn.getSubscriber().getDetails().getSurname(), pattern)) {
//                    if (!instantSearchResultLst.contains(sbn.getSubscriber().getDetails().getFirstName() + " " + sbn.getSubscriber().getDetails().getSurname())) {
//                        instantSearchResultLst.add(sbn.getSubscriber().getDetails().getFirstName() + " " + sbn.getSubscriber().getDetails().getSurname());
//                        count++;
//                    }
//                }
//
//                if (checkMatching(sbn.getSubscriber().getDetails().getSurname(), pattern)) {
//                    if (!instantSearchResultLst.contains(sbn.getSubscriber().getDetails().getFirstName() + " " + sbn.getSubscriber().getDetails().getSurname())) {
//                        instantSearchResultLst.add(sbn.getSubscriber().getDetails().getFirstName() + " " + sbn.getSubscriber().getDetails().getSurname());
//                        count++;
//                    }
//                }
//
//                if (checkMatching(sbn.getSubscriber().getDetails().getCity(), pattern)) {
//                    if (!instantSearchResultLst.contains(sbn.getSubscriber().getDetails().getCity())) {
//                        instantSearchResultLst.add(sbn.getSubscriber().getDetails().getCity());
//                        count++;
//                    }
//                }
//
//                if (checkMatching(sbn.getSubscriber().getDetails().getStreet(), pattern)) {
//                    if (!instantSearchResultLst.contains(sbn.getSubscriber().getDetails().getStreet())) {
//                        instantSearchResultLst.add(sbn.getSubscriber().getDetails().getStreet());
//                        count++;
//                    }
//                }
//                if (checkMatching(sbn.getSubscriber().getDetails().getPhoneMobile(), pattern)) {
//                    if (!instantSearchResultLst.contains(sbn.getSubscriber().getDetails().getPhoneMobile())) {
//                        instantSearchResultLst.add(sbn.getSubscriber().getDetails().getPhoneMobile());
//                        count++;
//                    }
//                }
//                if (checkMatching(sbn.getSubscriber().getDetails().getPassportNumber(), pattern)) {
//                    if (!instantSearchResultLst.contains(sbn.getSubscriber().getDetails().getPassportNumber())) {
//                        instantSearchResultLst.add(sbn.getSubscriber().getDetails().getPassportNumber());
//                        count++;
//                    }
//                }
//                String radiusUsername = sbn.findRadiusUserName(sbn);
//                if (checkMatching(radiusUsername, pattern)) {
//                    if (!instantSearchResultLst.contains(radiusUsername)) {
//                        instantSearchResultLst.add(radiusUsername);
//                        count++;
//                    }
//                }
//            }
//
//
//
//        } catch (Exception ex) {
//            log.debug("exception occured in completeText: " + ex.getMessage() + " subId: " + sbn == null ? null : sbn.getId());
//        }
        return instantSearchResultLst;
    }

    public boolean checkMatching(String base, String pattern) {
        return base != null && base.toLowerCase().startsWith(pattern.toLowerCase());
    }

    public void onRowSelect(SelectEvent event) {

        selectedSubscriber = (Subscriber) event.getObject();
        log.debug("onRowSelect: " + selectedSubscriber.getId());
    }

    String provider;

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public List<String> getProviders() {
        List<ServiceProvider> providers = providerPersistenceFacade.findAll();
        List<String> list = new ArrayList<>();
        for (ServiceProvider p : providers) {
            list.add(p.getName());
        }
        return list;
    }
}

package com.jaravir.tekila.ui.jsf.managed.stats;

import com.jaravir.tekila.base.auth.Privilege;
import com.jaravir.tekila.base.auth.persistence.ExternalUser;
import com.jaravir.tekila.base.auth.persistence.Role;
import com.jaravir.tekila.base.auth.persistence.User;
import com.jaravir.tekila.base.auth.persistence.manager.*;
import com.jaravir.tekila.base.auth.persistence.manager.SecurityManager;
import com.jaravir.tekila.base.filter.MatchingOperation;
import com.jaravir.tekila.base.persistence.facade.AbstractPersistenceFacade;
import com.jaravir.tekila.module.accounting.entity.Payment;
import com.jaravir.tekila.module.accounting.entity.PaymentOption;
import com.jaravir.tekila.module.accounting.manager.PaymentPersistenceFacade;
import com.jaravir.tekila.module.payment.PaymentOptionsPersistenceFacade;
import com.jaravir.tekila.module.report.email.EmailReportPersistenceFacade;
import com.jaravir.tekila.module.service.entity.ServiceProvider;
import com.jaravir.tekila.module.service.persistence.manager.ServiceProviderPersistenceFacade;
import com.jaravir.tekila.module.subscription.persistence.entity.SubscriberFunctionalCategory;
import com.jaravir.tekila.provision.broadband.devices.manager.Providers;
import com.jaravir.tekila.ui.model.LazyTableModel;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.primefaces.model.LazyDataModel;

import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by sajabrayilov on 23.01.2015.
 */
@ManagedBean
@ViewScoped
public class PaymentReportManager implements Serializable {
    private transient final static Logger log = Logger.getLogger(PaymentReportManager.class);
    @EJB
    private PaymentPersistenceFacade paymentFacade;
    @EJB
    private UserPersistenceFacade userPersistenceFacade;
    @EJB
    private SecurityManager securityManager;
    @EJB
    private UserPersistenceFacade userFacade;
    @EJB
    private ExternalUserPersistenceFacade extUserFacade;
    @EJB
    private ServiceProviderPersistenceFacade serviceProviderPersistenceFacade;
    @EJB
    private EmailReportPersistenceFacade emailReportPersistenceFacade;
    @EJB
    private PaymentOptionsPersistenceFacade payOptionFacade;

    private LazyDataModel<Payment> paymentData;
    private Payment selectedPayment;
    private Date defaultFromDate;
    private Date defaultTtToDate;

    private Date fromDate;
    private Date toDate;
    private String userName;
    private List<SelectItem> userNameList;
    private List<SelectItem> extUserNameList;

//    public List<String> getMethodList() {
//        return methodList = Arrays.asList("CASH", "BANK", "OTHERS");
//    }



    private String method;
    private String externalUserName;
    private double total;
    private boolean isShowVoidPayments;
    private boolean isShowTests;
    private String email;

    private String selectedProviderId;
    private List<SelectItem> providerList;
    private String selectedProviderName;

    public List<String> getMethodList() {
        if (methodList == null) {
            methodList = new ArrayList<>();
            List<PaymentOption> paymentOptions = payOptionFacade.findAll();
            for (PaymentOption pay : paymentOptions)
                methodList.add(pay.getName());
        }
        return methodList;
    }



    private List<String> methodList;

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getSelectedProviderId() {
        return selectedProviderId;
    }

    public void setSelectedProviderId(String selectedProviderId) {
        this.selectedProviderId = selectedProviderId;
    }

    public List<SelectItem> getProviderList() {
        if (providerList == null) {
            providerList = new ArrayList<>();
            for (ServiceProvider provider : serviceProviderPersistenceFacade.findAll()) {
                providerList.add(new SelectItem(String.valueOf(provider.getId()), provider.getName()));
            }
        }
        return providerList;
    }

    public void setProviderList(List<SelectItem> providerList) {
        this.providerList = providerList;
    }

    public String getSelectedProviderName() {
        if (selectedProviderId != null) {
            try {
                ServiceProvider provider = serviceProviderPersistenceFacade.find(Integer.parseInt(selectedProviderId));
                if (provider != null && provider.getName() != null) {
                    return provider.getName();
                }
            } catch (Exception e) {
                log.error("Exception on fetching provider name: ", e);
            }
        }
        return "N/A";
    }

    public void setSelectedProviderName(String selectedProviderName) {
        this.selectedProviderName = selectedProviderName;
    }

    public Payment getSelectedPayment() {
        return selectedPayment;
    }

    public void setSelectedPayment(Payment selectedPayment) {
        this.selectedPayment = selectedPayment;
    }

    public List<String> searchForUserName(String query) {
        List<String> users = new ArrayList<>();
        for (User user : userFacade.findStartsWith(query)) {
            users.add(user.getUserName());
        }
        return users;
    }

    public List<SelectItem> getUserNameList() {
        if (userNameList == null) {
            userNameList = new ArrayList<>();

            List<User> userList = userFacade.findAllByGroupID(69025L); //find all cashiers
            //log.debug("users list:" + userList);
            for (User user : userList)
                userNameList.add(new SelectItem(user.getUserName()));
        }
        return userNameList;
    }

    public void setUserNameList(List<SelectItem> userNameList) {
        this.userNameList = userNameList;
    }

    public List<SelectItem> getExtUserNameList() {
        if (extUserNameList == null) {
            extUserNameList = new ArrayList<>();
            List<ExternalUser> externalUsers = extUserFacade.findAll();
            for (ExternalUser user : externalUsers)
                extUserNameList.add(new SelectItem(user.getUsername()));
        }
        return extUserNameList;
    }

    public void setExtUserNameList(List<SelectItem> extUserNameList) {
        this.extUserNameList = extUserNameList;
    }

    public Date getFromDate() {
        if (fromDate == null) {
            fromDate = getDefaultFromDate();
        }
        return fromDate;
    }

    public void setFromDate(Date fromDate) {
        this.fromDate = fromDate;
    }

    public Date getToDate() {
        if (toDate == null) {
            toDate = getDefaultTtToDate();
        }
        return toDate;
    }

    public void setToDate(Date toDate) {
        this.toDate = toDate;
    }

    public String getUserName() {
        try {
            if (userName == null && securityManager.checkPermissions("OwnPaymentReport", Privilege.READ))
                userName = FacesContext.getCurrentInstance().getExternalContext().getUserPrincipal().getName();
        } catch (Exception ex) {
            log.error("Privilige OwnPaymentReport not found", ex);
        }
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getExternalUserName() {
        return externalUserName;
    }

    public void setExternalUserName(String externalUserName) {
        this.externalUserName = externalUserName;
    }

    public String getExternalUserNameView() {
        return (externalUserName == null || externalUserName.equals("-1")) ? "N/A" : externalUserName;
    }

    public LazyDataModel<Payment> getPaymentData() {
        return paymentData;
    }

    public void setPaymentData(LazyDataModel<Payment> paymentData) {
        this.paymentData = paymentData;
    }

    public Date getDefaultFromDate() {
        if (defaultFromDate == null) {
            defaultFromDate = DateTime.now().withTimeAtStartOfDay().toDate();
        }
        return defaultFromDate;
    }

    public void setDefaultFromDate(Date defaultFromDate) {
        this.defaultFromDate = defaultFromDate;
    }

    public Date getDefaultTtToDate() {
        if (defaultTtToDate == null) {
            defaultTtToDate = DateTime.now().withTime(23, 59, 59, 999).toDate();
        }

        return defaultTtToDate;
    }

    public void setDefaultTtToDate(Date defaultTtToDate) {
        this.defaultTtToDate = defaultTtToDate;
    }

    public boolean isShowVoidPayments() {
        return isShowVoidPayments;
    }

    public void setShowVoidPayments(boolean isShowVoidPayments) {
        this.isShowVoidPayments = isShowVoidPayments;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public boolean isCashierGroup() {
        String userName = FacesContext.getCurrentInstance().getExternalContext().getUserPrincipal().getName();
        User user = userPersistenceFacade.findByUserName(userName);

        if (user.getGroup().getId() == 69025) { //cashiers groups
            return true;
        }

        return false;
    }

    public boolean isShowTests() {
        return isShowTests;
    }

    public void setShowTests(boolean isShowTests) {
        this.isShowTests = isShowTests;
    }

    public String getEmail() {
        if (email == null) {
            String username = FacesContext.getCurrentInstance().getExternalContext().getUserPrincipal().getName();
            userPersistenceFacade.clearFilters();
            userPersistenceFacade.addFilter(UserPersistenceFacade.Filter.USERNAME, username);
            List<User> users = userPersistenceFacade.findAllPaginated(0, 1);
            if (users != null && !users.isEmpty()) {
                for (final User user : users) {
                    email = user.getEmail();
                }
            }
        }
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void sendEmail() {
        if (fromDate == null || toDate == null || email == null) {
            FacesContext.getCurrentInstance().addMessage(
                    null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Please enter all details", "Please enter all details"));
            return;
        }
        log.info(String.format(
                        "Email Reporter. fromDate = %s, toDate = %s, email = %s, selected provider = %s, external username = %s",
                        fromDate.toString(),
                        toDate.toString(),
                        email,
                        getSelectedProviderName(),
                        getExternalUserNameView()));
        emailReportPersistenceFacade.saveReport(fromDate, toDate, email, selectedProviderId, externalUserName);
        FacesContext.getCurrentInstance().addMessage(
                null,
                new FacesMessage(
                        FacesMessage.SEVERITY_INFO,
                        "email report is getting populated",
                        String.format("Report will be sent to %s", email))
        );
    }

    public void search() {
        log.debug(String.format("Submitted values: user=%s, extUser=%s, dateFrom=%3$tY-%3$tm-%3$td %3$tH, dateTo=%3$tY-%3$tm-%3$td %3$tH", userName, externalUserName, fromDate, toDate));
        try {
            paymentFacade.clearFilters();
            paymentFacade.setPredicateJoinOperation(AbstractPersistenceFacade.PredicateJoinOperation.AND);

            if (!isShowVoidPayments) {
                PaymentPersistenceFacade.Filter statusFilter = PaymentPersistenceFacade.Filter.STATUS;
                statusFilter.setOperation(MatchingOperation.NOT_EQUALS);
                paymentFacade.addFilter(statusFilter, -1);
            }

            if (!isShowTests) {
                PaymentPersistenceFacade.Filter testFilter = PaymentPersistenceFacade.Filter.SHOW_TEST;
                testFilter.setOperation(MatchingOperation.NOT_EQUALS);
                paymentFacade.addFilter(testFilter, SubscriberFunctionalCategory.TEST);
            }

            if (securityManager.checkUIPermissions("OwnPaymentReport", Privilege.READ)) {
                userName = FacesContext.getCurrentInstance().getExternalContext().getUserPrincipal().getName();
            }

            if (userName != null && !userName.isEmpty()) {
                PaymentPersistenceFacade.Filter usernameFilter = PaymentPersistenceFacade.Filter.USER;
                usernameFilter.setOperation(MatchingOperation.LIKE);
                paymentFacade.addFilter(usernameFilter, userName);
            }

            if (fromDate != null && toDate != null) {
                DateTime dt = new DateTime(toDate);
                //dt = dt.plusDays(1).minusSeconds(1);
                toDate = dt.toDate();
                PaymentPersistenceFacade.Filter dateFilter = PaymentPersistenceFacade.Filter.DATE;
                dateFilter.setOperation(MatchingOperation.BETWEEN);
                Map<String, Date> dateMap = new HashMap<>();
                dateMap.put("from", new DateTime(fromDate).toDate());
                dateMap.put("to", new DateTime(toDate).toDate());
                paymentFacade.addFilter(dateFilter, dateMap);
            }

            if (externalUserName != null && !externalUserName.equals("-1")) {
                PaymentPersistenceFacade.Filter extUseFilter = PaymentPersistenceFacade.Filter.EXT_USER;
                extUseFilter.setOperation(MatchingOperation.EQUALS);
                paymentFacade.addFilter(extUseFilter, externalUserName);
            }

            if (method != null && !method.equals("-1")) {
                log.debug("8888888 method  "+method);
                PaymentPersistenceFacade.Filter methodFilter = PaymentPersistenceFacade.Filter.METHOD;
                log.debug("extUseFilter "+methodFilter );
                methodFilter.setOperation(MatchingOperation.EQUALS);
                paymentFacade.addFilter(methodFilter, method);
            }

            if (selectedProviderId != null && Integer.parseInt(selectedProviderId) != -1) {
                PaymentPersistenceFacade.Filter filter = PaymentPersistenceFacade.Filter.PROVIDER_ID;
                filter.setOperation(MatchingOperation.EQUALS);
                paymentFacade.addFilter(PaymentPersistenceFacade.Filter.PROVIDER_ID, selectedProviderId);
            }

            if (method != null && !method.equals("-1")) {
                log.debug("8888888 method  "+method);
                PaymentPersistenceFacade.Filter methodFilter = PaymentPersistenceFacade.Filter.METHOD;
                log.debug("extUseFilter "+methodFilter );
                methodFilter.setOperation(MatchingOperation.EQUALS);
                paymentFacade.addFilter(methodFilter, method);
            }

            User user = userFacade.findByUserName(
                    FacesContext.getCurrentInstance().getExternalContext().getUserPrincipal().getName()
            );
            Set<ServiceProvider> providers = new HashSet<>();
            for (Role role : user.getGroup().getRoles()) {
                providers.add(role.getProvider());
            }

            //CITYNET specific logic START
            long citynetId = Providers.CITYNET.getId();
            boolean isCitynet = false;
            boolean isAnother = false;
            for (ServiceProvider provider : providers) {
                if (provider.getId() == citynetId) {
                    isCitynet = true;
                } else {
                    isAnother = true;
                }
            }
            if (isCitynet && !isAnother) {
                PaymentPersistenceFacade.Filter filter = PaymentPersistenceFacade.Filter.PROVIDER_ID;
                filter.setOperation(MatchingOperation.EQUALS);
                paymentFacade.addFilter(PaymentPersistenceFacade.Filter.PROVIDER_ID, citynetId);
            }
            //END

            log.debug("Filters: " + paymentFacade.getFilters());
            paymentFacade.addOrdering("lastUpdateDate", AbstractPersistenceFacade.Ordering.DESC);
            paymentData = new LazyTableModel<>(paymentFacade);
            total = paymentFacade.sumWithFilters();
        } catch (Exception ex) {
            log.error("Cannot generate report: ", ex);
        }
    }

    public String show() {
        if (selectedPayment == null) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Please select a payment", "Please select a payment"));
            return null;
        }

        return String.format("/pages/subscriber/ind/payment.xhtml?paymentID=%d&amp;subscriber=%d&amp;faces-redirect=true&amp;includeViewParams=true", selectedPayment.getId(), selectedPayment.getSubscriber_id());
    }

    public String getFileName() {
        DateTimeFormatter frm = DateTimeFormat.forPattern("yyyy_MM_dd");
        String fileName = new StringBuilder("Payments_Report")
                .append(fromDate != null ? "_" + new DateTime(fromDate).toString(frm) : "")
                .append(toDate != null ? "_" + new DateTime(toDate).toString(frm) : "")
                .toString();
        return fileName;
    }

    public void exportToExcel() {
        DateTimeFormatter frm = DateTimeFormat.forPattern("yyyy_MM_dd");
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyy");

        StringBuilder resBuilder = new StringBuilder("/export/payments?format=xlsx&file=Payments_Report")
                .append(fromDate != null ? "_" + new DateTime(fromDate).toString(frm) : "")
                .append(toDate != null ? "_" + new DateTime(toDate).toString(frm) : "")
                .append((externalUserName != null && !externalUserName.equals("-1")) ? "_" + getExternalUserName() : "")
                .append((selectedProviderId != null && !selectedProviderId.equals("-1")) ? "_" + getSelectedProviderName() : "")
                .append("&from=" + dateFormat.format(fromDate))
                .append("&to=" + dateFormat.format(toDate));
        if (externalUserName != null && !externalUserName.equals("-1")) {
            resBuilder.append("&extusername=" + externalUserName);
        }
        if (selectedProviderId != null && !selectedProviderId.equals("-1")) {
            resBuilder.append("&providerid=" + selectedProviderId);
        }
        String res = resBuilder.toString();

        if (!securityManager.checkUIPermissions("PaymentReport", Privilege.READ)
                && !securityManager.checkUIPermissions("OwnPaymentReport", Privilege.READ)) {
            log.error("Not enough privileges to generate report");
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Not enough privileges to generate report", "Not enough privileges to generate report"));
            return;
        }

        try {
            if (total > 0) {
                ExternalContext ctx = FacesContext.getCurrentInstance().getExternalContext();
                ctx.redirect(ctx.getRequestContextPath() + res);
            }
        } catch (IOException ex) {
            log.error("Cannot redirect to export page", ex);
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Cannot export data", "Cannot export data"));
        }
    }

    public void exportToPdf() {
        DateTimeFormatter frm = DateTimeFormat.forPattern("yyyy_MM_dd");
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyy");

        String res = new StringBuilder("/export/payments?format=pdf&file=Payments_Report")
                .append(fromDate != null ? "_" + new DateTime(fromDate).toString(frm) : "")
                .append(toDate != null ? "_" + new DateTime(toDate).toString(frm) : "")
                .append("&from=" + dateFormat.format(fromDate))
                .append("&to=" + dateFormat.format(toDate))
                .toString();

        if (!securityManager.checkUIPermissions("PaymentReport", Privilege.READ)
                && !securityManager.checkUIPermissions("OwnPaymentReport", Privilege.READ)) {
            log.error("Not enough privileges to generate report");
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Not enough privileges to generate report", "Not enough privileges to generate report"));
            return;
        }

        try {
            if (total > 0) {
                ExternalContext ctx = FacesContext.getCurrentInstance().getExternalContext();
                ctx.redirect(ctx.getRequestContextPath() + res);
            }
        } catch (IOException ex) {
            log.error("Cannot redirect to export page", ex);
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Cannot export data", "Cannot export data"));
        }
    }
}

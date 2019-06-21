/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jaravir.tekila.ui.jsf.managed.accounting;

import com.jaravir.tekila.base.auth.persistence.User;
import com.jaravir.tekila.base.auth.persistence.manager.UserPersistenceFacade;
import com.jaravir.tekila.base.entity.Util;
import com.jaravir.tekila.module.accounting.AccountingCategoryType;
import com.jaravir.tekila.module.accounting.entity.AccountingCategory;
import com.jaravir.tekila.module.accounting.entity.AccountingTransaction;
import com.jaravir.tekila.module.accounting.entity.Operation;
import com.jaravir.tekila.module.accounting.entity.TaxationCategory;
import com.jaravir.tekila.module.accounting.entity.TransactionType;
import com.jaravir.tekila.module.accounting.manager.AccountingCategoryPersistenceFacade;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import com.jaravir.tekila.module.accounting.manager.AccountingTransactionPersistenceFacade;
import com.jaravir.tekila.module.service.entity.ServiceProvider;
import com.jaravir.tekila.module.service.model.BillingPrinciple;
import com.jaravir.tekila.module.service.persistence.manager.ServiceProviderPersistenceFacade;
import com.jaravir.tekila.module.subscription.persistence.entity.SubscriberLifeCycleType;
import com.jaravir.tekila.module.subscription.persistence.entity.Subscription;
import com.jaravir.tekila.module.subscription.persistence.entity.SubscriptionStatus;
import com.jaravir.tekila.module.subscription.persistence.management.SubscriberPersistenceFacade;
import com.jaravir.tekila.module.subscription.persistence.management.SubscriptionPersistenceFacade;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIData;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.faces.event.AjaxBehaviorEvent;
import javax.faces.model.SelectItem;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.primefaces.component.datatable.DataTable;

/**
 * @author sajabrayilov
 */
@ManagedBean
@ViewScoped
public class AdjustmentManager implements Serializable {

    @EJB
    private AccountingTransactionPersistenceFacade accTransFacade;
    @EJB
    private SubscriptionPersistenceFacade subscriptionFacade;
    @EJB
    private ServiceProviderPersistenceFacade providerFacade;
    @EJB
    private AccountingCategoryPersistenceFacade accCatFacade;
    @EJB
    private SubscriberPersistenceFacade subscriberFacade;
    @EJB
    private UserPersistenceFacade userFacade;

    private transient static final Logger log = Logger.getLogger(AdjustmentManager.class);

    //create.xhtml
    private AccountingTransaction transaction;
    private List<SelectItem> accoutingCategoryList;
    private List<SelectItem> providerSelectList;
    private Long providerID;
    private List<Subscription> subscriptionList;
    private List<Subscription> filteredList;
    private Operation operation;
    private Long accCatID;
    private TaxationCategory taxCategory;
    private String manats;
    private String qepics;
    private Long subscriberID;
    private TransactionType transactionType;
    private List<SelectItem> transTypeList;
    private TransactionType creditType = TransactionType.CREDIT;
    private TransactionType debitType = TransactionType.DEBIT;
    private TransactionType creditPromoType = TransactionType.CREDIT_PROMO;
    private TransactionType debitPromoType = TransactionType.DEBIT_PROMO;
    private String dsc;
    //index.xhtml

    //adj
    private String netAmount;
    private String vatAmount;
    private transient UIInput qep;

    //create_credit
    private TaxationCategory taxCat_creditAdj;

    private transient UIData subscriptionsTable;

    public AccountingTransaction getTransaction() {
        if (transaction == null) {
            transaction = new AccountingTransaction();
        }

        return transaction;
    }

    public void setTransaction(AccountingTransaction transaction) {
        this.transaction = transaction;
    }

    public List<SelectItem> getProviderSelectList() {
        if (providerSelectList == null) {
            providerSelectList = new ArrayList<>();
            List<ServiceProvider> providers = providerFacade.findAll();

            if (!providers.isEmpty()) {
                for (ServiceProvider prov : providers) {
                    providerSelectList.add(new SelectItem(prov.getId(), prov.getName()));
                }
            }
        }
        return providerSelectList;
    }

    public void setProviderSelectList(List<SelectItem> providerSelectList) {
        this.providerSelectList = providerSelectList;
    }

    public Long getProviderID() {
        return providerID;
    }

    public void setProviderID(Long providerID) {
        this.providerID = providerID;
    }

    public List<Subscription> getSubscriptionList() {
        if (subscriptionList == null && subscriberID != null) {
            try {
                subscriptionList = subscriberFacade.findForceRefresh(subscriberID).getSubscriptions();
            } catch (Exception ex) {
                log.debug(String.format("Cannot find Subscriber id=%d", subscriberID), ex);
            }
        }

        return subscriptionList;
    }

    public void setSubscriptionList(List<Subscription> subscriptionList) {
        this.subscriptionList = subscriptionList;
    }

    public Operation getOperation() {
        if (operation == null) {
            operation = new Operation();
        }

        return operation;
    }

    public void setOperation(Operation operation) {
        this.operation = operation;
    }

    public List<SelectItem> getAccoutingCategoryList() {
        if (accoutingCategoryList == null) {
            accoutingCategoryList = new ArrayList<>();
            List<AccountingCategory> cats = accCatFacade.findAllAdj();

            if (!cats.isEmpty()) {
                for (AccountingCategory cat : cats) {
                    accoutingCategoryList.add(new SelectItem(cat.getId(), cat.getName()));
                }
            }
        }
        return accoutingCategoryList;
    }

    public void setAccoutingCategoryList(List<SelectItem> accoutingCategoryList) {
        this.accoutingCategoryList = accoutingCategoryList;
    }

    public Long getAccCatID() {
        return accCatID;
    }

    public void setAccCatID(Long accCatID) {
        this.accCatID = accCatID;
    }

    public TaxationCategory getTaxCategory() {
        return taxCategory;
    }

    public void setTaxCategory(TaxationCategory taxCategory) {
        this.taxCategory = taxCategory;
    }

    public List<Subscription> getFilteredList() {
        return filteredList;
    }

    public void setFilteredList(List<Subscription> filteredList) {
        this.filteredList = filteredList;
    }

    public String getManats() {
        return manats;
    }

    public void setManats(String manats) {
        this.manats = manats;
    }

    public String getQepics() {
        return qepics;
    }

    public void setQepics(String qepics) {
        this.qepics = qepics;
    }

    public Long getSubscriberID() {
        return subscriberID;
    }

    public void setSubscriberID(Long subscriberID) {
        this.subscriberID = subscriberID;
    }

    public TransactionType getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(TransactionType transactionType) {
        this.transactionType = transactionType;
    }

    public TransactionType getCreditType() {
        return creditType;
    }

    public void setCreditType(TransactionType creditType) {
        this.creditType = creditType;
    }

    public TransactionType getDebitType() {
        return debitType;
    }

    public void setDebitType(TransactionType debitType) {
        this.debitType = debitType;
    }

    public List<SelectItem> getTransTypeList() {
        if (transTypeList == null) {
            transTypeList = new ArrayList<>();

            for (TransactionType tp : TransactionType.values()) {
                if (tp != TransactionType.PAYMENT) {
                    transTypeList.add(new SelectItem(tp));
                }
            }
        }
        return transTypeList;
    }

    public void setTransTypeList(List<SelectItem> transTypeList) {
        this.transTypeList = transTypeList;
    }

    public void onTaxCategoryChange(AjaxBehaviorEvent ev) {
        if (accCatID != null) {
            try {
                taxCategory = accCatFacade.find(accCatID).getTaxCategory();
            } catch (Exception ex) {
                log.error(String.format("Cannot find AccountingCategoory for id=%d", accCatID), ex);
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Category not found", "Categiry not found"));
            }
        }
    }

    public String createCreditAdjustmnet() {
        if (operation.getSubscription() == null) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Please select subscription", "Please select subscription"));
            return null;
        }
        if (manats == null || creditType == null) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Please fill all required fields", "Please fill all required fields"));
            return null;
        }

        HttpSession session = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);
        //String referer = FacesContext.getCurrentInstance().getExternalContext().getRequestHeaderMap().get("referer");

        try {
            User user = userFacade.find((Long) session.getAttribute("userID"));
            operation.setUser(user);

            long amount = Util.parseAmountFromStringIntoLong(manats, qepics);
            //long amount = Double.valueOf(manats).longValue();

            if (amount <= 0) {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Amount must be poisitive", "Amount must be poisitive"));
                return null;
            }

            operation.setAmount(amount);
            operation.setDsc(dsc);

            if (accCatID != null) {
                AccountingCategory accountingCategory = accCatFacade.find(accCatID);
                operation.setCategory(accountingCategory);
            }

            //accTransFacade.createFromForm(operation, creditType, accCatID);
            AccountingTransaction accountingTransaction = accTransFacade.createFromForm(operation, creditType, null);
            Subscription subscription = operation.getSubscription();

            log.debug("Balance: " + subscription != null ? subscription.getBalance().getRealBalance() : "null subscription");

            if (subscription != null && subscription.getSubscriber().getLifeCycle() == SubscriberLifeCycleType.PREPAID
                    && subscription.getStatus() == SubscriptionStatus.BLOCKED && subscription.getBalance().getRealBalance() >= 0
                    && (subscription.getBillingModel() == null || (subscription.getBillingModel().getPrinciple() != BillingPrinciple.GRACE
                    && subscription.getBillingModel().getPrinciple() != BillingPrinciple.GRACE_MONTH))) {
                try {
                    subscriptionFacade.activatePrepaidOnAdjustment(subscription.getId(), accountingTransaction.getId(), String.format("accounting trasaction id=%d", accountingTransaction.getId()));
                } catch (Exception ex) {
                    log.error(String.format("accouting transaction id=%d provisioning error", accountingTransaction.getId()), ex);
                }
            }
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Adjustment successfull", "Adjustment successfull"));
            clearForm();
            return null;
        } catch (Exception ex) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Cannot create adjustment", "Cannot create adjustment"));
            log.error(ex);
            return null;
        }
    }

    public String createDebitAdjustmnet() {
        if (operation.getSubscription() == null) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Please select subscription", "Please select subscription"));
            return null;
        }
        if (accCatID == null || manats == null || debitType == null) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Please fill all required fields", "Please fill all required fields"));
            return null;
        }

        HttpSession session = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);
        try {
            User user = userFacade.find((Long) session.getAttribute("userID"));
            operation.setUser(user);

            long amount = Util.parseAmountFromStringIntoLong(manats, qepics);
            //long amount = Double.valueOf(manats).longValue();

            if (amount <= 0) {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Amount must be poisitive", "Amount must be poisitive"));
                return null;
            }

            operation.setAmount(amount);
            operation.setDsc(dsc);

            if (accCatID != null) {
                AccountingCategory accountingCategory = accCatFacade.find(accCatID);
                operation.setCategory(accountingCategory);
            }
            //accTransFacade.createFromForm(operation, transactionType, accCatID);
            accTransFacade.createFromForm(operation, debitType, accCatID);
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Adjustment successfull", "Adjustment successfull"));
            clearForm();
            return null;
        } catch (Exception ex) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Cannot create adjustment", "Cannot create adjustment"));
            log.error(ex);
            return null;
        }
    }

    public String createPromoCreditAdjustment() {
        if (operation.getSubscription() == null) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Please select subscription", "Please select subscription"));
            return null;
        }
        if (manats == null || creditPromoType == null) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Please fill all required fields", "Please fill all required fields"));
            return null;
        }

        HttpSession session = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);
        //String referer = FacesContext.getCurrentInstance().getExternalContext().getRequestHeaderMap().get("referer");

        try {
            User user = userFacade.find((Long) session.getAttribute("userID"));
            operation.setUser(user);

            long amount = Util.parseAmountFromStringIntoLong(manats, qepics);
            //long amount = Double.valueOf(manats).longValue();

            if (amount <= 0) {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Amount must be poisitive", "Amount must be poisitive"));
                return null;
            }

            operation.setAmount(amount);
            operation.setDsc(dsc);

            if (accCatID != null) {
                AccountingCategory accountingCategory = accCatFacade.find(accCatID);
                operation.setCategory(accountingCategory);
            }

            accTransFacade.createFromFormPromo(operation, creditPromoType, null);

            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Promo adjustment successfull", "Promo adjustment successfull"));
            clearForm();
            return null;
        } catch (Exception ex) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Cannot create promo adjustment", "Cannot create promo adjustment"));
            log.error(ex);
            return null;
        }
    }

    public String createPromoDebitAdjustmnet() {
        if (operation.getSubscription() == null) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Please select subscription", "Please select subscription"));
            return null;
        }
        if (accCatID == null || manats == null || debitPromoType == null) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Please fill all required fields", "Please fill all required fields"));
            return null;
        }

        HttpSession session = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);
        try {
            User user = userFacade.find((Long) session.getAttribute("userID"));
            operation.setUser(user);

            long amount = Util.parseAmountFromStringIntoLong(manats, qepics);
            //long amount = Double.valueOf(manats).longValue();

            if (amount <= 0) {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Amount must be poisitive", "Amount must be poisitive"));
                return null;
            }

            operation.setAmount(amount);
            operation.setDsc(dsc);

            if (accCatID != null) {
                AccountingCategory accountingCategory = accCatFacade.find(accCatID);
                operation.setCategory(accountingCategory);
            }
            //accTransFacade.createFromForm(operation, transactionType, accCatID);
            accTransFacade.createFromFormPromo(operation, debitPromoType, accCatID);
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Adjustment successfull", "Adjustment successfull"));
            clearForm();
            return null;
        } catch (Exception ex) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Cannot create adjustment", "Cannot create adjustment"));
            log.error(ex);
            return null;
        }
    }

    private void clearForm() {
        setAccCatID(null);
        setTaxCategory(null);
        setManats(null);
        setQepics(null);
        setNetAmount(null);
        setVatAmount(null);
        setOperation(null);
        if (subscriptionsTable != null) {
            ((DataTable) subscriptionsTable).reset();
        }
        subscriptionList = null;
        getSubscriptionList();
    }

    public String getNetAmount() {
        return netAmount;
    }

    public void setNetAmount(String netAmount) {
        this.netAmount = netAmount;
    }

    public String getVatAmount() {
        return vatAmount;
    }

    public void setVatAmount(String vatAmount) {
        this.vatAmount = vatAmount;
    }

    public UIInput getQep() {
        return qep;
    }

    public void setQep(UIInput qep) {
        this.qep = qep;
    }

    public void onAmountChange(AjaxBehaviorEvent ev) {
        if (taxCategory == null || manats == null) {
            return;
        }

        double grossAmount = Double.parseDouble(manats);
        double vatAmountLong = 0;
        double netAmountLong = grossAmount;
        log.debug(String.format("onAmountChange triggered! gross=%f, mamats=%s", grossAmount, manats));

        if (taxCategory.getVATRate() > 0 && grossAmount > 0) {
            double vatRate = taxCategory.getVATRate() / 100000d;
            log.debug(String.format("grossamount=%f, vatRate=%f", netAmountLong, vatRate));

            if (vatRate != 0) {
                netAmountLong = netAmountLong / (1 + vatRate);
            }
            vatAmountLong = grossAmount - netAmountLong;

            log.debug(String.format("after grossamount=%f, vatRate=%f", netAmountLong, vatRate));

        }

        this.netAmount = String.format("%.5f", netAmountLong);
        this.vatAmount = String.format("%.5f", vatAmountLong);

    }

    public void onAmountChangeCreditAdj(AjaxBehaviorEvent ev) {
        if (getTaxCat_creditAdj() == null || manats == null) {
            return;
        }

        double grossAmount = Double.parseDouble(manats);
        double vatAmountLong = 0;
        double netAmountLong = grossAmount;
        log.debug(String.format("onAmountChange triggered! gross=%f, mamats=%s", grossAmount, manats));

        if (getTaxCat_creditAdj().getVATRate() > 0 && grossAmount > 0) {
            double vatRate = getTaxCat_creditAdj().getVATRate() / 100000d;
            log.debug(String.format("grossamount=%f, vatRate=%f", netAmountLong, vatRate));
            vatAmountLong = netAmountLong * vatRate;
            netAmountLong = netAmountLong - vatAmountLong;
            log.debug(String.format("after grossamount=%f, vatRate=%f", netAmountLong, vatRate));

        }

        this.netAmount = String.format("%.5f", netAmountLong);
        this.vatAmount = String.format("%.5f", vatAmountLong);

    }

    public void onReset() {
        clearForm();
    }

    public TaxationCategory getTaxCat_creditAdj() {
        if (taxCat_creditAdj == null) {
            taxCat_creditAdj = accCatFacade.findByType(AccountingCategoryType.REALIZATION).getTaxCategory();
        }
        return taxCat_creditAdj;
    }

    public void setTaxCat_creditAdj(TaxationCategory taxCat_creditAdj) {
        this.taxCat_creditAdj = taxCat_creditAdj;
    }

    public String getDsc() {
        return dsc;
    }

    public void setDsc(String dsc) {
        this.dsc = dsc;
    }

    public UIData getSubscriptionsTable() {
        return subscriptionsTable;
    }

    public void setSubscriptionsTable(UIData subscriptionsTable) {
        this.subscriptionsTable = subscriptionsTable;
    }

    public String redirect() {
        if (subscriberID != null) {
            return "/pages/subscriber/view.xhtml?subscriber=" + subscriberID + "&amp;faces-redirect=true&amp;includeViewParams=true";
        }

        return null;
    }

}

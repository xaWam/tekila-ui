package com.jaravir.tekila.ui.jsf.managed.accounting;

import com.jaravir.tekila.module.accounting.entity.AccountingTransaction;
import com.jaravir.tekila.module.accounting.entity.Operation;
import com.jaravir.tekila.module.accounting.manager.AccountingTransactionPersistenceFacade;
import com.jaravir.tekila.ui.model.LazyTableModel;
import org.apache.log4j.Logger;
import org.primefaces.model.LazyDataModel;

import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import java.io.Serializable;
import java.util.List;

/**
 * Created by sajabrayilov on 11/18/2014.
 */
@ManagedBean(name = "accTransManager")
@ViewScoped
public class AccountingTransactionManager  implements Serializable {
    private transient static final Logger log = Logger.getLogger(AccountingTransactionManager.class);

    @EJB private AccountingTransactionPersistenceFacade accTransFacade;

    //index
    private AccountingTransaction selected;
    private LazyDataModel<AccountingTransaction> accTransList;
    private List<AccountingTransaction> filteredAccTransList;

    //view
    private Long accountingTransactionId;
    private Operation selectedOperation;

    public List<AccountingTransaction> getFilteredAccTransList() {
        return filteredAccTransList;
    }

    public void setFilteredAccTransList(List<AccountingTransaction> filteredAccTransList) {
        this.filteredAccTransList = filteredAccTransList;
    }

    public AccountingTransaction getSelected() {
        log.debug("Accounting transaction id: " + accountingTransactionId);
        if (selected == null && accountingTransactionId != null) {
            selected = accTransFacade.findForceRefresh(accountingTransactionId);
        }
        return selected;
    }

    public void setSelected(AccountingTransaction selected) {
        this.selected = selected;
    }

    public LazyDataModel<AccountingTransaction> getAccTransList() {
        if (accTransList == null) {
            accTransList = new LazyTableModel<>(accTransFacade);
        }
        return accTransList;
    }

    public void setAccTransList(LazyDataModel<AccountingTransaction> accTransList) {
        this.accTransList = accTransList;
    }

    public Long getAccountingTransactionId() {
        return accountingTransactionId;
    }

    public void setAccountingTransactionId(Long accountingTransactionId) {
        this.accountingTransactionId = accountingTransactionId;
    }

    public Operation getSelectedOperation() {
        return selectedOperation;
    }

    public void setSelectedOperation(Operation selectedOperation) {
        this.selectedOperation = selectedOperation;
    }

    public String view() {
        if (selected == null) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Please select a transaction", "Please select a transaction"));
            return null;
        }

        return new StringBuilder("/pages/finance/acc_trans/view.xhtml?transaction=").append(selected.getId()).append("&amp;faces-redirect=true&amp;includeViewParams=true").toString();
    }

    public String redirectToSubscriber() {
        if (selectedOperation == null) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Please select an operation", "Please select an operation"));
            return null;
        }

        return new StringBuilder("/pages/subscriber/view.xhtml?subscriber=").append(selectedOperation.getSubscription().getSubscriber().getId()).append("&amp;faces-redirect=true&amp;includeViewParams=true").toString();

    }

    public String redirectToSubscription() {
        if (selectedOperation == null) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Please select an operation", "Please select an operation"));
            return null;
        }

        return new StringBuilder("/pages/subscription/view.xhtml?subscription=").append(selectedOperation.getSubscription().getId()).append("&amp;subscriber=").append(selectedOperation.getSubscription().getSubscriber().getId()).append("&amp;faces-redirect=true&amp;includeViewParams=true").toString();

    }
}

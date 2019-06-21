package com.jaravir.tekila.ui.jsf.managed.store;

import com.jaravir.tekila.module.accounting.entity.SalesPartnerInvoice;
import com.jaravir.tekila.module.accounting.manager.SalesPartnerInvoicePersistenceFacade;
import com.jaravir.tekila.ui.model.LazyTableModel;
import org.primefaces.component.datatable.DataTable;
import org.primefaces.model.LazyDataModel;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import java.util.List;

/**
 * Created by sajabrayilov on 12/17/2015.
 */
@ManagedBean
@ViewScoped
public class SalesPartnerInvoceManager {
    @EJB private SalesPartnerInvoicePersistenceFacade invoiceFacade;

    private LazyDataModel<SalesPartnerInvoice> invoiceList;
    private SalesPartnerInvoice invoice;
    private List<SalesPartnerInvoice> filteredInvoicesList;
    private Long salesPartnerID;
    private Long invoiceID;
    private UIComponent invoiceTable;
    private final String VIEW_PATH = "/pages/store/distrib/invoice/view.xhtml?invoice=%d&amp;includeViewParams=true&amp;faces-redirect=true";
    private final String INDEX_PATH = "/pages/store/distrib/invoice/index.xhtml?distributor=%d&amp;includeViewParams=true&amp;faces-redirect=true";
    private final String DISTRIB_PATH = "/pages/store/distrib/view.xhtml?distributor=%d&amp;includeViewParams=true&amp;faces-redirect=true";

    @PostConstruct
    public void init () {
        invoiceFacade.clearFilters();
    }

    @PreDestroy
    public void cleanUp () {
        invoiceFacade.clearFilters();
    }

    public LazyDataModel<SalesPartnerInvoice> getInvoiceList() {
        if (invoiceList == null && salesPartnerID != null) {
            ((DataTable) invoiceTable).reset();
            invoiceFacade.clearFilters();
            invoiceFacade.addFilter(SalesPartnerInvoicePersistenceFacade.Filter.PARTNER_ID, salesPartnerID);
            invoiceList = new LazyTableModel<>(invoiceFacade);
        }
        return invoiceList;
    }

    public void setInvoiceList(LazyDataModel<SalesPartnerInvoice> invoiceList) {
        if (invoiceList == null) {

        }
        this.invoiceList = invoiceList;
    }

    public SalesPartnerInvoice getInvoice() {
        if (invoice == null && invoiceID != null) {
            ((DataTable) invoiceTable).reset();
            invoice = invoiceFacade.find(invoiceID);
        }
        return invoice;
    }

    public void setInvoice(SalesPartnerInvoice invoice) {
        this.invoice = invoice;
    }

    public List<SalesPartnerInvoice> getFilteredInvoicesList() {
        return filteredInvoicesList;
    }

    public void setFilteredInvoicesList(List<SalesPartnerInvoice> filteredInvoicesList) {
        this.filteredInvoicesList = filteredInvoicesList;
    }

    public Long getInvoiceID() {
        return invoiceID;
    }

    public void setInvoiceID(Long invoiceID) {
        this.invoiceID = invoiceID;
    }

    public UIComponent getInvoiceTable() {
        return invoiceTable;
    }

    public void setInvoiceTable(UIComponent invoiceTable) {
        this.invoiceTable = invoiceTable;
    }

    public String show () {
        if (invoice == null) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage());
            return null;
        }

        return String.format(VIEW_PATH, invoice.getId());
    }

    public String returnToDistributor () {
        if (invoice == null && salesPartnerID == null) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage());
            return null;
        }

        long distributorID = (invoice != null) ? invoice.getPartner().getId() : salesPartnerID;
        return String.format(DISTRIB_PATH, distributorID);
    }

    public Long getSalesPartnerID() {
        return salesPartnerID;
    }

    public void setSalesPartnerID(Long salesPartnerID) {
        this.salesPartnerID = salesPartnerID;
    }

    public String returnToInvoices() {
        if (invoice == null) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage());
            return null;
        }

        return String.format(INDEX_PATH, invoice.getPartner().getId());
    }
}

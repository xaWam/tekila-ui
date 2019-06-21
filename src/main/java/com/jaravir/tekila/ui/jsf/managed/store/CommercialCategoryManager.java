package com.jaravir.tekila.ui.jsf.managed.store;

import com.jaravir.tekila.base.auth.persistence.User;
import com.jaravir.tekila.base.auth.persistence.manager.UserPersistenceFacade;
import com.jaravir.tekila.base.entity.Util;
import com.jaravir.tekila.base.filter.MatchingOperation;
import com.jaravir.tekila.module.accounting.entity.TaxationCategory;
import com.jaravir.tekila.module.accounting.manager.TaxCategoryPeristenceFacade;
import com.jaravir.tekila.module.store.mobile.CommercialCategory;
import com.jaravir.tekila.module.store.mobile.CommercialCategoryPersistenceFacade;
import com.jaravir.tekila.ui.model.LazyTableModel;
import org.apache.log4j.Logger;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import org.primefaces.model.LazyDataModel;

/**
 * Created by sajabrayilov on 01.04.2015.
 */
@ManagedBean
@ViewScoped
public class CommercialCategoryManager implements Serializable {
    private final static Logger log = Logger.getLogger(CommercialCategoryManager.class);

    @EJB private CommercialCategoryPersistenceFacade comFacade;
    @EJB private TaxCategoryPeristenceFacade taxFacade;
    @EJB private UserPersistenceFacade userFacade;

    private String amountWholePart;
    private String amountDecimalPart;

    private Long categoryID;
    private CommercialCategory category;


    private LazyDataModel<CommercialCategory> categoryList;
    private CommercialCategory selected;
    private List<SelectItem> taxCategoryList;
    private Long taxCat;
    private String filterName;
    private String filterPriceWholePart;
    private String filterPriceDecimalPart;

    @PostConstruct
    public void init () {
        comFacade.clearFilters();
    }

    public String getAmountWholePart() {
        if (amountWholePart == null && categoryID != null) {
            String[] am = Util.convertFromLongToStringArray(getSelected().getPrice());
            amountWholePart = am[0];
        }
        return amountWholePart;
    }

    public void setAmountWholePart(String amountWholePart) {
        this.amountWholePart = amountWholePart;
    }

    public String getAmountDecimalPart() {
        if (amountDecimalPart == null && categoryID != null) {
            String[] am = Util.convertFromLongToStringArray(getSelected().getPrice());

            if (am.length > 1) {
                amountDecimalPart = am[1];
            }
        }
        return amountDecimalPart;
    }

    public void setAmountDecimalPart(String amountDecimalPart) {
        this.amountDecimalPart = amountDecimalPart;
    }

    public Long getCategoryID() {
        return categoryID;
    }

    public void setCategoryID(Long categoryID) {
        this.categoryID = categoryID;
    }

    public CommercialCategory getCategory() {
        if (category == null)
            category = new CommercialCategory();

        return category;
    }

    public void setCategory(CommercialCategory category) {
        this.category = category;
    }

    public LazyDataModel <CommercialCategory> getCategoryList() {
        return categoryList;
    }

    public void setCategoryList(LazyDataModel<CommercialCategory> categoryList) {
        this.categoryList = categoryList;
    }

    public CommercialCategory getSelected() {
        if (selected == null && categoryID != null) {
            selected = comFacade.find(categoryID);
        }
        return selected;
    }

    public void setSelected(CommercialCategory selected) {
        this.selected = selected;
    }

    public String getFilterName() {
        return filterName;
    }

    public void setFilterName(String filterName) {
        this.filterName = filterName;
    }

    public String getFilterPriceWholePart() {
        return filterPriceWholePart;
    }

    public void setFilterPriceWholePart(String filterPriceWholePart) {
        this.filterPriceWholePart = filterPriceWholePart;
    }

    public String getFilterPriceDecimalPart() {
        return filterPriceDecimalPart;
    }

    public void setFilterPriceDecimalPart(String filterPriceDecimalPart) {
        this.filterPriceDecimalPart = filterPriceDecimalPart;
    }

    public List<SelectItem> getTaxCategoryList() {
        if (taxCategoryList == null) {
            taxCategoryList = new ArrayList<>();

            List<TaxationCategory> taxCats = taxFacade.findAll();
            for (TaxationCategory cat : taxCats) {
                taxCategoryList.add(new SelectItem(cat.getId(), cat.getName()));
            }
        }
        return taxCategoryList;
    }

    public void setTaxCategoryList(List<SelectItem> taxCategoryList) {
        this.taxCategoryList = taxCategoryList;
    }

    public Long getTaxCat() {
        if (taxCat == null && categoryID != null) {
            taxCat = getSelected().getTaxCategory().getId();
        }
        return taxCat;
    }

    public void setTaxCat(Long taxCat) {
        this.taxCat = taxCat;
    }

    public void create () {
        if (
           category.getName() == null || category.getName().isEmpty()
            || taxCat == null || amountWholePart == null) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Please fill all required fields","Please fill all required fields"));
            return;
        }

        try {
            TaxationCategory taxationCategory = taxFacade.find(taxCat);

            if (taxationCategory == null) {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Tax category not found", "Tax category not found"));
                return;
            }

            User user = userFacade.findByUserName(FacesContext.getCurrentInstance().getExternalContext().getUserPrincipal().getName());

            category.setTaxCategory(taxationCategory);
            category.setUser(user);

            category.setPrice(Util.parseAmountFromStringIntoLong(amountWholePart, amountDecimalPart));

            comFacade.save(category);
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Operation successfull", "Operation successfull"));
        }
        catch (Exception ex) {
            log.error(String.format("Cannot insert category %s", category), ex);
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Operation failed","Operation failed"));
        }
    }

    public String show () {
        if (selected == null) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "No commercial category found", "No commercial category found"));
            return null;
        }

        return String.format("/pages/store/comcat/view.xhtml?category=%d&amp;faces-redirect=true&amp;includeViewParams=true", selected.getId());
    }

    public void edit () {
        if (selected == null) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "No commercial category found","No commercial category found"));
            return;
        }

        try {
            if (amountWholePart != null && !amountDecimalPart.isEmpty()) {
                long amount = Util.parseAmountFromStringIntoLong(amountWholePart, getAmountDecimalPart());

                if (selected.getPrice() != amount) {
                    selected.setPrice(amount);
                }
            }

            if (taxCat != null) {
                TaxationCategory taxationCategory = taxFacade.find(taxCat);

                if (selected.getTaxCategory().getId() != taxationCategory.getId()) {
                    selected.setTaxCategory(taxationCategory);
                }
            }

            selected = comFacade.update(selected);
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Operation successfull","Operation successfull"));
        }
        catch (Exception ex) {
            log.error(String.format("Cannot update category %s", selected), ex);
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Operation failed","Operation failed"));
        }
    }

    public void search () {
        if (
                (filterName == null || filterName.isEmpty())
                && taxCat == null
            ) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Please provide at least one criteria for search","Please provide at least one criteria for search"));
            return;
        }

        comFacade.clearFilters();

        if (filterName != null && !filterName.isEmpty()) {
            CommercialCategoryPersistenceFacade.Filter nameFilter = CommercialCategoryPersistenceFacade.Filter.NAME;
            nameFilter.setMatchingOperation(MatchingOperation.LIKE);
            comFacade.addFilter(nameFilter, filterName);
        }

        if (taxCat != null) {
            comFacade.addFilter(CommercialCategoryPersistenceFacade.Filter.TAX_CATEGORY, taxCat);
        }

        categoryList = new LazyTableModel(comFacade);
    }
}

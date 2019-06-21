/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jaravir.tekila.ui.jsf.managed.accounting;

import com.jaravir.tekila.module.accounting.AccountingCategoryType;
import com.jaravir.tekila.module.accounting.entity.AccountingCategory;
import com.jaravir.tekila.module.accounting.entity.TaxationCategory;
import com.jaravir.tekila.module.accounting.manager.AccountingCategoryPersistenceFacade;
import com.jaravir.tekila.module.accounting.manager.TaxCategoryPeristenceFacade;
import com.jaravir.tekila.ui.model.LazyTableModel;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import org.apache.log4j.Logger;
import org.primefaces.model.LazyDataModel;

/**
 *
 * @author sajabrayilov
 */
@ManagedBean
@ViewScoped
public class AccCategoryManager  implements Serializable {
    @EJB private AccountingCategoryPersistenceFacade accCatFacade;
    @EJB private TaxCategoryPeristenceFacade taxFacade;    
    private transient static final Logger log = Logger.getLogger(AccCategoryManager.class);
    private static final String PATH = "/pages/finance/categories/";
    private AccountingCategory category;
    private List<SelectItem> taxCatList;
    private List<SelectItem> accCatList;
    private Long taxCatID;
    //index.xhtml
    private LazyDataModel<AccountingCategory> lazyCategoryList;
    private List<AccountingCategory> filteredCategoryList;
    private AccountingCategory selectedCategory;
    //view.xhtml
    private Long categoryID;
    private List<SelectItem> viewTaxCatList;
    private List<SelectItem> viewAccCatList;
    
    public AccountingCategory getCategory() {
        if (category == null)
            category = new AccountingCategory();
        
        return category;
    }

    public void setCategory(AccountingCategory category) {
        this.category = category;
    }

    public List<SelectItem> getTaxCatList() {
        if (taxCatList == null) {
            taxCatList = new ArrayList<>();
            List<TaxationCategory> taxCats = taxFacade.findAll();
            
            if (!taxCats.isEmpty())
                for (TaxationCategory cat : taxCats)
                    taxCatList.add(new SelectItem(cat.getId(), cat.getName()));                
        }
        return taxCatList;
    }

    public void setTaxCatList(List<SelectItem> taxCatList) {
        this.taxCatList = taxCatList;
    }

    public List<SelectItem> getAccCatList() {
        if (accCatList == null) {
            accCatList = new ArrayList<>();

            for (AccountingCategoryType cat : AccountingCategoryType.values()) {
                accCatList.add(new SelectItem(cat, cat.toString()));
            }
        }
        return accCatList;
    }

    public void setAccCatList(List<SelectItem> accCatList) {
        this.accCatList = accCatList;
    }

    public Long getTaxCatID() {
        return taxCatID;
    }

    public void setTaxCatID(Long taxCatID) {
        this.taxCatID = taxCatID;
    }

    public LazyDataModel<AccountingCategory> getLazyCategoryList() {
        if (lazyCategoryList == null)
            lazyCategoryList = new LazyTableModel<>(accCatFacade);
        
        return lazyCategoryList;
    }

    public void setLazyCategoryList(LazyDataModel<AccountingCategory> lazyCategoryList) {
        this.lazyCategoryList = lazyCategoryList;
    }

    public List<AccountingCategory> getFilteredCategoryList() {
        return filteredCategoryList;
    }

    public void setFilteredCategoryList(List<AccountingCategory> filteredCategoryList) {
        this.filteredCategoryList = filteredCategoryList;
    }

    public AccountingCategory getSelectedCategory() {
        if(selectedCategory == null && categoryID != null)
            selectedCategory = accCatFacade.find(categoryID);
        
        return selectedCategory;
    }

    public void setSelectedCategory(AccountingCategory selectedCategory) {
        this.selectedCategory = selectedCategory;
    }

    public Long getCategoryID() {
        return categoryID;
    }

    public void setCategoryID(Long categoryID) {
        this.categoryID = categoryID;
    }

    public List<SelectItem> getViewTaxCatList() {
        if (viewTaxCatList == null  && categoryID != null ) {
            //log.debug(String.format("Selected categoory: %s", getSelectedCategory()));
            viewTaxCatList = new ArrayList<>();
            viewTaxCatList.add(new SelectItem(getSelectedCategory().getTaxCategory().getId(), getSelectedCategory().getTaxCategory().getName()));
            List<TaxationCategory> taxCats = taxFacade.findAllNotIn(getSelectedCategory().getTaxCategory().getId());
            
            if (!taxCats.isEmpty())
                for (TaxationCategory cat : taxCats)
                    viewTaxCatList.add(new SelectItem(cat.getId(), cat.getName()));
        }
        return viewTaxCatList;
    }

    public void setViewTaxCatList(List<SelectItem> viewTaxCatList) {
        this.viewTaxCatList = viewTaxCatList;
    }

    public List<SelectItem> getViewAccCatList() {
        if (viewAccCatList == null && categoryID != null) {
            viewAccCatList = new ArrayList<>();
            viewAccCatList.add(new SelectItem(getSelectedCategory().getType()));
            
            for (AccountingCategoryType type : AccountingCategoryType.values())
                if (type != getSelectedCategory().getType())
                    viewAccCatList.add(new SelectItem(type));
        }
        return viewAccCatList;
    }

    public void setViewAccCatList(List<SelectItem> viewAccCatList) {
        this.viewAccCatList = viewAccCatList;
    }    
    
    public String create() {
        try {
            accCatFacade.createFromFrom(taxCatID, category);
            return PATH + "index.xhtml";
        }
        catch (Exception ex) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Cannot create accounting category",  "Cannot create accounting category"));
            return null;
        }
    }    
    
    public String view () {
        if (selectedCategory == null) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Please select category",  "Please select category"));
            return null;            
        }
        
        return new StringBuilder(PATH).append("view?category=").append(selectedCategory.getId())
                .append("&amp;faces-redirect=true&amp;includeViewParams=true").toString();
    }
    
    public String edit() {
        log.debug("Accounting category before update: " + selectedCategory);
        
        if (selectedCategory == null) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Please select a category", "Please select a category"));
            return null;
        }
       
        try {
            accCatFacade.edit(selectedCategory, taxCatID);
            return PATH + "index.xhtml";
        }
        catch (Exception ex) {
            String msg = String.format("Cannot edit accounting category id=%d", selectedCategory.getId());
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, msg, msg));            
            return null;
        }
    }
}

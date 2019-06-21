/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jaravir.tekila.ui.jsf.managed.accounting;

import com.jaravir.tekila.module.accounting.entity.Bank;
import com.jaravir.tekila.module.accounting.entity.Currency;
import com.jaravir.tekila.module.accounting.manager.BankPersistenceFacade;
import com.jaravir.tekila.module.accounting.manager.CurrencyPersistenceFacade;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;

/**
 *
 * @author sajabrayilov
 */
@ManagedBean
@ViewScoped
public class BankManager  implements Serializable {
    @EJB private CurrencyPersistenceFacade currencyFacade;
    @EJB private BankPersistenceFacade bankFacade;
    private final String PATH = "/pages/finance/banks/";
    //create.xhtml
    private Bank bank;
    private List<SelectItem> currencyList;
    private String currencyCode;
    private List<SelectItem> parentList;
    private Long parentID;        
    //index.xhtml
    private List<Bank> bankList;
    private List<Bank> filteredBankList;
    private Bank selectedBank;
    //view.xhtml
    private List<SelectItem> selectedBankCurrencyList;
    private List<SelectItem> selectedBankParentList;
    private Long bankID;
    /**
     * Creates a new instance of BankManager
     */
    public BankManager() {
    }

    public Bank getBank() {
        if (bank == null)
            bank = new Bank();
        
        return bank;
    }

    public void setBank(Bank bank) {
        this.bank = bank;
    }

    public List<SelectItem> getCurrencyList() {
        if (currencyList == null) {
            currencyList = new ArrayList<>();
            List<Currency> curList = currencyFacade.findAll();
            
            if (!curList.isEmpty())
                for (Currency cur : curList)
                    currencyList.add(new SelectItem(cur.getCode()));
        }
        return currencyList;
    }

    public void setCurrencyList(List<SelectItem> currencyList) {
        this.currencyList = currencyList;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    public List<SelectItem> getParentList() {
        if (parentList == null) {
            parentList = new ArrayList<>();
            List<Bank> parents = bankFacade.findAllParents();
            if (!parents.isEmpty())
                for (Bank par : parents)
                    parentList.add(new SelectItem(par.getId(), par.getFullName()));
        }
        return parentList;
    }

    public void setParentList(List<SelectItem> parentList) {
        this.parentList = parentList;
    }

    public Long getParentID() {
        return parentID;
    }

    public void setParentID(Long parentID) {
        this.parentID = parentID;
    }   

    public List<Bank> getBankList() {
        if (bankList == null)
            bankList = bankFacade.findAll();
        
        return bankList;
    }

    public void setBankList(List<Bank> bankList) {
        this.bankList = bankList;
    }

    public List<Bank> getFilteredBankList() {
        return filteredBankList;
    }

    public void setFilteredBankList(List<Bank> filteredBankList) {
        this.filteredBankList = filteredBankList;
    }

    public Bank getSelectedBank() {
        if (bankID != null && selectedBank == null) {
            selectedBank = bankFacade.find(bankID);
        }
        return selectedBank;
    }

    public void setSelectedBank(Bank selectedBank) {
        this.selectedBank = selectedBank;
    }

    public List<SelectItem> getSelectedBankCurrencyList() {
        if (selectedBank != null && selectedBankCurrencyList == null) {
            selectedBankCurrencyList = new ArrayList<>();
            selectedBankCurrencyList.add(new SelectItem(selectedBank.getCurrency().getCode()));
            List<Currency> curList = currencyFacade.findAllNotIn(selectedBank.getCurrency().getId());
            
            if (!curList.isEmpty())
                for (Currency cur : curList)
                    selectedBankCurrencyList.add(new SelectItem(cur.getCode()));                
        }
        return selectedBankCurrencyList;
    }

    public void setSelectedBankCurrencyList(List<SelectItem> selectedBankCurrencyList) {
        this.selectedBankCurrencyList = selectedBankCurrencyList;
    }

    public Long getBankID() {
        return bankID;
    }

    public void setBankID(Long bankID) {
        this.bankID = bankID;
    }
    
    public List<SelectItem> getSelectedBankParentList() {
        if (selectedBank != null && selectedBankParentList == null) {
            selectedBankParentList = new ArrayList<>();
            List<Bank> parList = null;
            
            if (selectedBank.getParent() != null) {
                selectedBankParentList.add(new SelectItem(selectedBank.getParent().getId(), selectedBank.getParent().getFullName()));
                parList = bankFacade.findAllParentsNotIn(selectedBank.getParent().getId(), selectedBank.getId());
            }
            else {
                parList = bankFacade.findAllParentsNotIn(selectedBank.getId());
            }
            
            if (!parList.isEmpty())
                for (Bank par : parList)
                    selectedBankParentList.add(new SelectItem(par.getId(), par.getName()));            
        }
        return selectedBankParentList;
    }

    public void setSelectedBankParentList(List<SelectItem> selectedBankParentList) {
        this.selectedBankParentList = selectedBankParentList;
    }    
    
    public String create() {
        if (bank == null) {
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Bank not found", "Bank not found")
            );
            return null;
        }        
        try {
            bankFacade.createFromForm(bank, currencyCode, parentID);
            return PATH + "index.xhtml?faces-redirect=true";
        }
        catch (Exception ex) {
            FacesContext.getCurrentInstance().addMessage(
                null, new FacesMessage(
                    FacesMessage.SEVERITY_ERROR, "Cannot create bank", "Cannot create bank")
            );
            return null;
        }
    }    
    
    public String view() {
        if (selectedBank == null) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(
                FacesMessage.SEVERITY_ERROR, "Please select a Bank", "Please select a Bank"
            ));
            return null;
        }
        
        return new StringBuilder(PATH)
            .append("view?bank=").append(selectedBank.getId())
            .append("&amp;faces-redirect=true&amp;includeViewParams=true")
            .toString(); 
            
    }
    
    public String update() {
        if (selectedBank == null) {
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Bank not found", "Bank not found")
            );
            return null;
        }
        try {
            bankFacade.updateFromForm(selectedBank, currencyCode, parentID);
            FacesContext.getCurrentInstance().addMessage(
                null, new FacesMessage(
                    FacesMessage.SEVERITY_INFO, "Bank updated successfully", "Bank updated successfully")
            );            
            return PATH + "index.xhtml?faces-redirect=true";
        }
        catch (Exception ex) {
            FacesContext.getCurrentInstance().addMessage(
                null, new FacesMessage(
                    FacesMessage.SEVERITY_ERROR, "Cannot create bank", "Cannot create bank")
            );
            return null;
        } 
    }
}

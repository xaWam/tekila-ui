/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.jaravir.tekila.ui.jsf.managed;

import com.jaravir.tekila.module.service.ServiceType;
import com.jaravir.tekila.module.service.ValueAddedServiceType;
import com.jaravir.tekila.module.service.entity.ServiceProvider;
import com.jaravir.tekila.module.service.entity.VASCode;
import com.jaravir.tekila.module.service.persistence.manager.ServiceProviderPersistenceFacade;
import com.jaravir.tekila.module.service.persistence.manager.VASCodePersistenceFacade;
import com.jaravir.tekila.ui.model.LazyTableModel;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import org.apache.log4j.Logger;
import org.primefaces.model.LazyDataModel;

/**
 *
 * @author sajabrayilov
 */
@ManagedBean(name="vasCodeManager")
public class VASCodeManager  implements Serializable {
    private transient final static Logger log = Logger.getLogger(VASCodeManager.class);
    private final static String PATH = "/pages/service/vascode/";
    @EJB
    private VASCodePersistenceFacade vasCodeFacade;
    @EJB
    private ServiceProviderPersistenceFacade providerFacade;
    
    private LazyDataModel<VASCode> vasCodeList;
    private List<VASCode> filteredVasCodeList;
    private List<SelectItem> providerList;
    private String providerID;  
    private VASCode vasCode;
    private List<SelectItem> serviceTypeSelectList;
    
    public VASCode getVasCode() {
        if (vasCode == null) {
            vasCode = new VASCode();
        }
        return vasCode;
    }

    public void setVasCode(VASCode vasCode) {
        this.vasCode = vasCode;
    }

    public List<SelectItem> getProviderList() {
        if (providerList == null) {
            List<ServiceProvider> providers = providerFacade.findAll();
            if(!providers.isEmpty()) {
                providerList = new ArrayList<>();
                for (ServiceProvider prov : providers) {
                    providerList.add(new SelectItem(prov.getId(), prov.getName()));
                }
            }
        }
        return providerList;
    }

    public void setProviderList(List<SelectItem> providerList) {
        this.providerList = providerList;
    }

    public String getProviderID() {
        return providerID;
    }

    public void setProviderID(String providerID) {
        this.providerID = providerID;
    }

    public LazyDataModel<VASCode> getVasCodeList() {
        if (vasCodeList == null) {
            vasCodeList = new LazyTableModel<>(vasCodeFacade);            
        }
        return vasCodeList;
    }

    public void setVasCodeList(LazyDataModel<VASCode> vasCodeList) {
        this.vasCodeList = vasCodeList;
    }

    public List<VASCode> getFilteredVasCodeList() {
        return filteredVasCodeList;
    }

    public void setFilteredVasCodeList(List<VASCode> filteredVasCodeList) {
        this.filteredVasCodeList = filteredVasCodeList;
    }

    public List<SelectItem> getServiceTypeSelectList() {
        if (serviceTypeSelectList == null) {
            serviceTypeSelectList = new ArrayList<>();
            
            for (ValueAddedServiceType type : ValueAddedServiceType.values()) {
                serviceTypeSelectList.add(new SelectItem(type.toString()));
            }
        }
        return serviceTypeSelectList;
    }

    public void setServiceTypeSelectList(List<SelectItem> serviceTypeSelectList) {
        this.serviceTypeSelectList = serviceTypeSelectList;
    }    
    
    public String create() {
        try {
            log.debug("Provider id: " + providerID);
           ServiceProvider provider = providerFacade.find(Long.valueOf(providerID));
           vasCode.setProvider(provider);
           vasCodeFacade.save(vasCode);
           FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "VAS Code successfully created", "VAS Code successfully created"));
           return PATH + "index"; 
        }
        catch (Exception ex) {
            log.error("Cannot create VASCode: ", ex);
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Cannot create VAS Code", "Cannot create VAS Code"));
            return null;
        }            
    }
}

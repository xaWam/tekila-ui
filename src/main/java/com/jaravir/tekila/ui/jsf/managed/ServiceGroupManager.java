/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jaravir.tekila.ui.jsf.managed;

import com.jaravir.tekila.module.service.ServiceType;
import com.jaravir.tekila.module.service.entity.ServiceGroup;
import com.jaravir.tekila.module.service.entity.ServiceProvider;
import com.jaravir.tekila.module.service.persistence.manager.ServiceGroupPersistenceFacade;
import com.jaravir.tekila.module.service.persistence.manager.ServiceProviderPersistenceFacade;
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
public class ServiceGroupManager  implements Serializable {
    private transient final static Logger log = Logger.getLogger(ServiceManager.class);
    private final static String GROUP_PATH = "/pages/service/group/";
    @EJB private ServiceGroupPersistenceFacade groupFacade;
    @EJB private ServiceProviderPersistenceFacade providerFacade;
    private ServiceGroup group;
    private List<SelectItem> providerSelectList;
    private Long serviceProviderId;
    private LazyDataModel<ServiceGroup> groupList;
    private ServiceGroup selected;
    private List<ServiceGroup> filteredServiceGroupList;
    private List<SelectItem> typeList;
    
    public ServiceGroup getGroup() {
        if (group == null) {
            group = new ServiceGroup();
        }
        return group;
    }

    public void setGroup(ServiceGroup group) {
        this.group = group;
    }

    public List<SelectItem> getProviderSelectList() {
        if (providerSelectList == null) {
            providerSelectList = new ArrayList<>();
            List<ServiceProvider> providers = providerFacade.findAll();
            for (ServiceProvider prov : providers) {
                providerSelectList.add(new SelectItem(prov.getId(), prov.getName()));
            }            
        }
        return providerSelectList;
    }

    public void setProviderSelectList(List<SelectItem> providerSelectList) {
        this.providerSelectList = providerSelectList;
    }

    public LazyDataModel<ServiceGroup> getGroupList() {
        if (groupList == null) {
            groupList = new LazyTableModel<>(groupFacade);
        }
        
        return groupList;
    }

    public void setGroupList(LazyDataModel<ServiceGroup> groupList) {
        this.groupList = groupList;
    }

    public ServiceGroup getSelected() {
        return selected;
    }

    public void setSelected(ServiceGroup selected) {
        this.selected = selected;
    }

    public Long getServiceProviderId() {
        return serviceProviderId;
    }

    public void setServiceProviderId(Long serviceProviderId) {
        this.serviceProviderId = serviceProviderId;
    }   

    public List<ServiceGroup> getFilteredServiceGroupList() {
        return filteredServiceGroupList;
    }

    public void setFilteredServiceGroupList(List<ServiceGroup> filteredServiceGroupList) {
        this.filteredServiceGroupList = filteredServiceGroupList;
    }

    public List<SelectItem> getTypeList() {
        if (typeList == null) {
            for (ServiceType type : ServiceType.values()) {
                typeList.add(new SelectItem(type.toString()));
            }
        }
        return typeList;
    }

    public void setTypeList(List<SelectItem> typeList) {
        this.typeList = typeList;
    }
          
    public String create() {
        if (serviceProviderId == null) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Provider must be selected", "Provider must be selected"));
            return null;
        }
        
        group.setProvider(providerFacade.find(serviceProviderId));
        groupFacade.save(group);
        
        return GROUP_PATH + "index";
    }
    
    public void save() {
        if (selected == null) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "First select an item", "First select an item"));            
            return;
        }
        
        groupFacade.update(selected);
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Item updated successfully", "Item updated successfully"));                    
    }
}

package com.jaravir.tekila.ui.jsf.managed;

import com.jaravir.tekila.module.service.ServiceType;
import com.jaravir.tekila.module.service.entity.ServiceProvider;
import com.jaravir.tekila.module.service.entity.ServiceSubgroup;
import com.jaravir.tekila.module.service.persistence.manager.ServiceGroupPersistenceFacade;
import com.jaravir.tekila.module.service.persistence.manager.ServiceProviderPersistenceFacade;
import com.jaravir.tekila.module.service.persistence.manager.ServiceSubgroupPersistenceFacade;
import com.jaravir.tekila.ui.model.LazyTableModel;
import org.apache.log4j.Logger;
import org.primefaces.context.RequestContext;
import org.primefaces.event.RowEditEvent;
import org.primefaces.model.LazyDataModel;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by shnovruzov on 8/25/2016.
 */
@ManagedBean
@ViewScoped
public class ServiceSubgroupManager {

    private transient final static Logger log = Logger.getLogger(ServiceSubgroupManager.class);
    private final static String GROUP_PATH = "/pages/service/subgroup/";
    @EJB
    private ServiceSubgroupPersistenceFacade subgroupFacade;
    @EJB
    private ServiceProviderPersistenceFacade providerFacade;

    private String name;
    private ServiceProvider provider;
    private ServiceType type;
    private ServiceSubgroup selected;
    private LazyDataModel<ServiceSubgroup> subgroupLazyList;

    private Long providerId;
    private List<ServiceProvider> providerList;
    private List<ServiceType> typeList;

    public List<ServiceType> getTypeList() {
        if (typeList == null) {
            typeList = new ArrayList<>();
            for (ServiceType type : ServiceType.values()) {
                typeList.add(type);
            }
        }
        return typeList;
    }

    public void setTypeList(List<ServiceType> typeList) {
        this.typeList = typeList;
    }

    public List<ServiceProvider> getProviderList() {
        if (providerList == null) {
            providerList = providerFacade.findAll();
        }
        return providerList;
    }

    public void setProviderList(List<ServiceProvider> providerList) {
        this.providerList = providerList;
    }

    public Long getProviderId() {
        return providerId;
    }

    public void setProviderId(Long providerId) {
        this.providerId = providerId;
    }

    @PostConstruct
    private void init(){
        subgroupFacade.clearFilters();
    }

    public LazyDataModel<ServiceSubgroup> getSubgroupLazyList() {
        if(subgroupLazyList == null){
            subgroupLazyList = new LazyTableModel<>(subgroupFacade);
        }
        return subgroupLazyList;
    }

    public void setSubgroupLazyList(LazyDataModel<ServiceSubgroup> subgroupLazyList) {
        this.subgroupLazyList = subgroupLazyList;
    }

    public ServiceSubgroup getSelected() {
        return selected;
    }

    public void setSelected(ServiceSubgroup selected) {
        this.selected = selected;
    }

    public ServiceType getType() {
        return type;
    }

    public void setType(ServiceType type) {
        this.type = type;
    }

    public ServiceProvider getProvider() {
        return provider;
    }

    public void setProvider(ServiceProvider provider) {
        this.provider = provider;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void save() {

    }

    public void create() throws IOException {
        if (providerId == null || name == null || type == null) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Fill all fields", "Fill all fields"));
            return;
        }
        subgroupFacade.create(name, providerFacade.find(providerId), type);

        FacesContext.getCurrentInstance().getExternalContext().redirect("/pages/service/subgroup/index.xhtml");
        RequestContext.getCurrentInstance().update("subForm");
    }

    public void onRowEdit(RowEditEvent event) {
        ServiceSubgroup subgroup = (ServiceSubgroup) event.getObject();
        subgroupFacade.update(subgroup);
    }
}

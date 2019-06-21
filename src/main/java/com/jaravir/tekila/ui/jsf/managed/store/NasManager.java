package com.jaravir.tekila.ui.jsf.managed.store;

import com.jaravir.tekila.module.service.entity.ServiceProvider;
import com.jaravir.tekila.module.service.persistence.manager.ServiceProviderPersistenceFacade;
import com.jaravir.tekila.module.store.RangePersistenceFacade;
import com.jaravir.tekila.module.store.ats.AtsPersistenceFacade;
import com.jaravir.tekila.module.store.attribute.AttributePersistenceFacade;
import com.jaravir.tekila.module.store.ip.persistence.IpAddressRange;
import com.jaravir.tekila.module.store.nas.Attribute;
import com.jaravir.tekila.module.store.nas.Nas;
import com.jaravir.tekila.module.store.nas.NasPersistenceFacade;
import com.jaravir.tekila.module.subscription.persistence.entity.Ats;
import com.jaravir.tekila.ui.model.LazyTableModel;
import org.apache.log4j.Logger;
import org.primefaces.context.RequestContext;
import org.primefaces.event.RowEditEvent;
import org.primefaces.event.SelectEvent;
import org.primefaces.event.ToggleEvent;
import org.primefaces.model.LazyDataModel;

import javax.annotation.ManagedBean;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by shnovruzov on 6/2/2016.
 */
@ManagedBean
@ViewScoped
public class NasManager implements Serializable {

    private final static Logger log = Logger.getLogger(AtsManager.class);

    @EJB
    private NasPersistenceFacade nasFacade;
    @EJB
    private AttributePersistenceFacade attributeFacade;
    @EJB
    private RangePersistenceFacade rangeFacade;
    @EJB
    private ServiceProviderPersistenceFacade providerFacade;

    private Long nasId;
    private String name;
    private String IP;
    private String desc;
    private String secretKey;
    private LazyDataModel<Nas> nasList;
    private Nas selectedNas;
    private ServiceProvider provider;
    private List<Attribute> attributeList;
    private List<String> selectedAttrList;
    private List<Attribute> existAttrList;
    private List<ServiceProvider> providerSelectList;

    @PostConstruct
    public void init() {
        nasFacade.clearFilters();
    }

    public List<ServiceProvider> getProviderSelectList() {
        if (providerSelectList == null) {
            providerSelectList = new ArrayList<>();


            for (ServiceProvider prov : providerFacade.findAll()) {
                providerSelectList.add(prov);
            }
        }
        return providerSelectList;
    }

    public void setProviderSelectList(List<ServiceProvider> providerList) {
        this.providerSelectList = providerList;
    }

    public ServiceProvider getProvider() {
        return provider;
    }

    public void setProvider(ServiceProvider provider) {
        this.provider = provider;
    }

    public Long getNasId() {
        return nasId;
    }

    public void setNasId(Long nasId) {
        this.nasId = nasId;
    }

    public List<Attribute> getExistAttrList() {
        existAttrList = new ArrayList<>();
        log.debug("SELECTED NAS:" + selectedNas);
        if (selectedNas != null) {
            for (Attribute att : selectedNas.getAttributeList())
                existAttrList.add(att);
        }
        return existAttrList;
    }

    public void setExistAttrList(List<Attribute> existAttrList) {
        this.existAttrList = existAttrList;
    }

    public List<String> getSelectedAttrList() {
        if (nasId != null) {
            selectedAttrList = new ArrayList<>();
            for (Attribute attribute : nasFacade.find(nasId).getAttributeList())
                selectedAttrList.add(String.valueOf(attribute.getId()));
        }
        return selectedAttrList;
    }

    public void setSelectedAttrList(List<String> selectedAttrList) {
        this.selectedAttrList = selectedAttrList;
    }

    public String getSecretKey() {
        if (nasId != null)
            secretKey = nasFacade.find(nasId).getSecretKey();

        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public List<Attribute> getAttributeList() {
        if (attributeList == null)
            attributeList = attributeFacade.findAll();
        return attributeList;
    }

    public void setAttributeList(List<Attribute> attributeList) {
        this.attributeList = attributeList;
    }

    public Nas getSelectedNas() {
        return selectedNas;
    }

    public void setSelectedNas(Nas selectedNas) {
        this.selectedNas = selectedNas;

    }

    public LazyDataModel<Nas> getNasList() {
        if (nasList == null)
            nasList = new LazyTableModel<Nas>(nasFacade);
        long count = nasFacade.count();
        log.debug(String.format("Found %s nas", count));
        return nasList;
    }

    public void setNasList(LazyDataModel<Nas> nasList) {
        this.nasList = nasList;
    }

    public String getDesc() {
        if (nasId != null)
            desc = nasFacade.find(nasId).getDesc();
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getIP() {
        if (nasId != null)
            IP = nasFacade.find(nasId).getIP();
        return IP;
    }

    public void setIP(String IP) {
        this.IP = IP;
    }

    public String getName() {
        if (nasId != null)
            name = nasFacade.find(nasId).getName();
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean check() {
        if (provider == null || name == null || name.isEmpty()
                || IP == null || IP.isEmpty()
                || secretKey == null || secretKey.isEmpty()) return false;
        return true;
    }

    public void search() {
        nasFacade.clearFilters();
        if (nasId != null)
            nasFacade.addFilter(NasPersistenceFacade.Filter.ID, nasId);
        if (provider != null)
            nasFacade.addFilter(NasPersistenceFacade.Filter.PROVIDER, provider);
        if (name != null && !name.isEmpty())
            nasFacade.addFilter(NasPersistenceFacade.Filter.NAME, name);
        if (IP != null && !IP.isEmpty())
            nasFacade.addFilter(NasPersistenceFacade.Filter.IP, IP);
        if (desc != null && !desc.isEmpty())
            nasFacade.addFilter(NasPersistenceFacade.Filter.desc, desc);

        nasList = new LazyTableModel<>(nasFacade);
        long count = nasFacade.count();
        log.debug(String.format("Found %s nas", count));
    }

    public void create() throws IOException {

        if (!check()) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Fill in the fields", "Fill in the fields"));
            return;
        }

        log.debug("attlist size: " + selectedAttrList.size());

        List<Attribute> attributeList = new ArrayList<>();
        for (String id : selectedAttrList)
            attributeList.add(attributeFacade.find(Long.valueOf(id)));

        nasFacade.create(name,provider, IP, secretKey, desc, attributeList);

        FacesContext.getCurrentInstance().getExternalContext().redirect("/pages/store/radius/nas/index.xhtml");
        RequestContext.getCurrentInstance().update("nasForm");

    }

    public String edit() {
        log.debug("selected nas: " + selectedNas);
        if (selectedNas == null) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Please select nas first", "Please select nas first"));
            return null;
        }
        String result = null;

        result = String.format("/pages/store/radius/nas/edit.xhtml?nas=%d&amp;faces-redirect=true&amp;includeViewParams=true", selectedNas.getId());

        log.debug("editNas: result=" + result);
        return result;
    }

    public void onRowEdit(RowEditEvent event) {
        Nas nas = (Nas) event.getObject();
        nasFacade.update(nas);
    }

    public void onRowSelect(SelectEvent event) {
        Nas nas = (Nas) event.getObject();
        selectedNas = nas;
    }

    public void onRowToggle(ToggleEvent event) {
        //Nas nas = (Nas) event.getData();
        //attributeFacade.addFilter(AttributePersistenceFacade.Filter.NAS, nas);
    }

    public List<IpAddressRange> getIpRangeListByNas(Nas nas) {
        return rangeFacade.findRangesByNas(nas);
    }


    public void save() throws IOException {
        nasFacade.save(selectedNas);
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Edited nas: " + selectedNas.getName(), "Edited nas: " + selectedNas.getName()));
        FacesContext.getCurrentInstance().getExternalContext().redirect("/pages/store/radius/nas/index.xhtml");
        RequestContext.getCurrentInstance().update("nasForm");
    }

    public void updateNas() throws IOException {

        List<Attribute> attributeList = new ArrayList<>();
        for (String id : selectedAttrList)
            attributeList.add(attributeFacade.find(Long.valueOf(id)));

        log.debug("Attrb size: " + attributeList.size());

        Nas nas = nasFacade.find(nasId);
        nas.setName(name);
        nas.setProvider(provider);
        nas.setIP(IP);
        nas.setDescription(desc);
        nas.setSecretKey(secretKey);
        nas.setAttributeList(attributeList);

        nas = nasFacade.update(nas);

        log.debug("updateNas: " + nas.getId());

        FacesContext.getCurrentInstance().getExternalContext().redirect("/pages/store/radius/nas/index.xhtml");
        RequestContext.getCurrentInstance().update("nasForm");
    }
}

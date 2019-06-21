package com.jaravir.tekila.ui.jsf.managed.store;

import com.jaravir.tekila.module.store.ats.AtsPersistenceFacade;
import com.jaravir.tekila.module.store.attribute.AttributePersistenceFacade;
import com.jaravir.tekila.module.store.midipop.MidipopPersistenceFacade;
import com.jaravir.tekila.module.subscription.persistence.entity.Ats;
import com.jaravir.tekila.provision.broadband.devices.Midipop;
import com.jaravir.tekila.ui.model.LazyTableModel;
import org.apache.log4j.Logger;
import org.primefaces.context.RequestContext;
import org.primefaces.event.RowEditEvent;
import org.primefaces.event.ToggleEvent;
import org.primefaces.model.LazyDataModel;

import javax.annotation.ManagedBean;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.AjaxBehaviorEvent;
import javax.faces.model.SelectItem;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by shnovruzov on 7/8/2016.
 */
@ManagedBean
@ViewScoped
public class MidipopManager implements Serializable {

    private final static Logger log = Logger.getLogger(MidipopManager.class);
    private final static String CREATE_PATH = "/pages/store/midipop/create.xhtml";
    private final static String INDEX_PATH = "/pages/store/midipop/index.xhtml";

    @EJB
    private AtsPersistenceFacade atsFacade;
    @EJB
    private MidipopPersistenceFacade midipopFacade;


    private String name;
    private Ats ats;
    private String switchIp;
    private Integer switchPort;
    private Integer status;
    private Integer cable;
    private Integer nodes;
    private Integer vlan;
    private Integer newStatus;

    private LazyDataModel<Midipop> midipopList;
    private List<Ats> atsSelectList;
    private List<SelectItem> statusList;
    private Midipop selectedMidipop;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Ats getAts() {
        return ats;
    }

    public void setAts(Ats ats) {
        this.ats = ats;
    }

    public Integer getNewStatus() {
        return newStatus;
    }

    public void setNewStatus(Integer newStatus) {
        this.newStatus = newStatus;
    }

    @PostConstruct
    private void init() {
        midipopFacade.clearFilters();
    }

    public LazyDataModel<Midipop> getMidipopList() {
        if (midipopList == null)
            midipopList = new LazyTableModel<>(midipopFacade);
        return midipopList;
    }

    public void setMidipopList(LazyDataModel<Midipop> midipopList) {
        this.midipopList = midipopList;
    }

    public Midipop getSelectedMidipop() {
        return selectedMidipop;
    }

    public void setSelectedMidipop(Midipop selectedMidipop) {
        this.selectedMidipop = selectedMidipop;
    }

    public List<Ats> getAtsSelectList() {
        if (atsSelectList == null) {
            atsSelectList = atsFacade.findAllAts();
        }
        return atsSelectList;
    }

    public void setAtsSelectList(List<Ats> atsSelectList) {
        this.atsSelectList = atsSelectList;
    }

    public Integer getVlan() {
        return vlan;
    }

    public void setVlan(Integer vlan) {
        this.vlan = vlan;
    }

    public Integer getNodes() {
        return nodes;
    }

    public void setNodes(Integer nodes) {
        this.nodes = nodes;
    }

    public Integer getCable() {
        return cable;
    }

    public void setCable(Integer cable) {
        this.cable = cable;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getSwitchPort() {
        return switchPort;
    }

    public void setSwitchPort(Integer switchPort) {
        this.switchPort = switchPort;
    }

    public String getSwitchIp() {
        return switchIp;
    }

    public void setSwitchIp(String switchIp) {
        this.switchIp = switchIp;
    }

    public List<SelectItem> getStatusList() {
        if (statusList == null) {
            statusList = new ArrayList<>();
            statusList.add(new SelectItem(1, "Available"));
            statusList.add(new SelectItem(0, "Not available"));
        }
        return statusList;
    }

    public void setStatusList(List<SelectItem> statusList) {
        this.statusList = statusList;
    }

    public void search() {
        midipopFacade.clearFilters();
        if (ats != null)
            midipopFacade.addFilter(MidipopPersistenceFacade.Filter.ATS, ats);
        if (status != null)
            midipopFacade.addFilter(MidipopPersistenceFacade.Filter.STATUS, status);
        if (switchIp != null && !switchIp.isEmpty())
            midipopFacade.addFilter(MidipopPersistenceFacade.Filter.IP, switchIp);
        if (vlan != null)
            midipopFacade.addFilter(MidipopPersistenceFacade.Filter.VLAN, vlan);


        midipopList = new LazyTableModel<>(midipopFacade);
        long count = midipopFacade.count();
        log.debug(String.format("Found %s midipops", count));
    }

    private boolean check() {
        if (name == null || name.isEmpty() || switchIp == null || switchIp.isEmpty() || switchPort == null || ats == null || vlan == null || status == null)
            return false;
        return true;
    }

    public void create() throws IOException {
        if (!check()) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Fill in the fiels", "Fill in the fields"));
            return;
        }

        boolean vlanExist = midipopFacade.findMidipopByVlan(vlan);
        if (vlanExist) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "The vlan exists, write another", "The vlan exists, write another"));
            return;
        }

        midipopFacade.create(name, status, cable, nodes, switchIp, switchPort, vlan, ats);

        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Created new midipop", "Created new midipop"));

        FacesContext.getCurrentInstance().getExternalContext().redirect("/pages/store/midipop/index.xhtml");
        RequestContext.getCurrentInstance().update("eqForm");

    }

    public void onRowEdit(RowEditEvent event) {
        Midipop midipop = (Midipop) event.getObject();
        log.debug("MIDI: " + midipop.getAts().getName());
        midipopFacade.update(midipop);
    }

    public void onRowToggle(ToggleEvent event) {
        Ats ats = (Ats) event.getData();
        midipopFacade.addFilter(MidipopPersistenceFacade.Filter.ATS, ats);
    }

}

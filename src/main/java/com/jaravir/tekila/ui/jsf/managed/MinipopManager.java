/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jaravir.tekila.ui.jsf.managed;

import com.jaravir.tekila.common.device.DeviceStatus;
import com.jaravir.tekila.module.service.entity.ServiceProvider;
import com.jaravir.tekila.module.service.persistence.manager.ServiceProviderPersistenceFacade;
import com.jaravir.tekila.module.store.ats.AtsPersistenceFacade;
import com.jaravir.tekila.module.store.midipop.MidipopPersistenceFacade;
import com.jaravir.tekila.module.store.midipop.MidipopPortPersistenceFacade;
import com.jaravir.tekila.module.store.nas.Nas;
import com.jaravir.tekila.module.store.nas.NasPersistenceFacade;
import com.jaravir.tekila.module.store.street.HousePersistenceFacade;
import com.jaravir.tekila.module.store.street.StreetPersistenceFacade;
import com.jaravir.tekila.module.subscription.persistence.entity.Ats;
import com.jaravir.tekila.module.subscription.persistence.entity.House;
import com.jaravir.tekila.module.subscription.persistence.entity.Streets;
import com.jaravir.tekila.provision.broadband.devices.Midipop;
import com.jaravir.tekila.provision.broadband.devices.MidipopPort;
import com.jaravir.tekila.provision.broadband.devices.MiniPop;
import com.jaravir.tekila.provision.broadband.devices.MinipopCategory;
import com.jaravir.tekila.provision.broadband.devices.manager.MiniPopPersistenceFacade;
import com.jaravir.tekila.provision.broadband.devices.manager.Providers;
import com.jaravir.tekila.ui.model.LazyTableModel;
import org.apache.log4j.Logger;
import org.primefaces.event.RowEditEvent;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.AjaxBehaviorEvent;
import javax.faces.model.SelectItem;

/**
 * @author sajabrayilov
 */
@ManagedBean
@ViewScoped
public class MinipopManager implements Serializable {

    private transient final static Logger log = Logger.getLogger(MinipopManager.class);
    private final static String PATH = "/pages/minipops/";
    @EJB
    private MiniPopPersistenceFacade minipopFacade;
    @EJB
    private ServiceProviderPersistenceFacade providerFacade;
    @EJB
    private AtsPersistenceFacade atsFacade;
    @EJB
    private NasPersistenceFacade nasFacade;
    @EJB
    private MidipopPersistenceFacade midipopFacade;
    @EJB
    private MidipopPortPersistenceFacade portFacade;
    @EJB
    private StreetPersistenceFacade streetFacade;
    @EJB
    private HousePersistenceFacade houseFacade;

    private ServiceProvider provider;
    private MiniPop miniPop;
    private Midipop midipop;
    private Nas nas;
    private Ats ats;
    private LazyTableModel<MiniPop> miniPopLazyTableModel;
    private List<MiniPop> miniPopFilteredList;
    private Long minipopID;
    private MiniPop selected;
    private List<SelectItem> deviceStatusList;

    private Integer midipopPortNumber;
    private Integer midipopSlot;
    private List<MidipopPort> midipopPortList;
    private List<Midipop> midipopList;
    private List<Midipop> midipopSelectList;
    private String mac;
    private String ip;
    private String switch_id;
    private String address;
    private DeviceStatus status;
    private List<ServiceProvider> providerSelectList;
    private String atsName;
    private List<Ats> atsSelectList;
    private Integer masterVlan;
    private Integer subVlan;
    private List<Nas> nasSelectList;
    private String serial;
    private String model;
    private MinipopCategory category;
    private List<MinipopCategory> categoryList;
    private List<Streets> streetsList;
    private Streets street;
    private String housesAsText;

    @PostConstruct
    public void init() {
        minipopFacade.clearFilters();
        midipopFacade.clearFilters();
    }

    public String getHousesAsText() {
        return housesAsText;
    }

    public void setHousesAsText(String housesAsText) {
        this.housesAsText = housesAsText;
    }

    public List<Streets> getStreetsList() {
        if (streetsList == null && ats != null) {
            streetsList = streetFacade.findByATS(getAts());
        }
        return streetsList;
    }

    public void onAtsSelect(AjaxBehaviorEvent event) {
        log.debug("ATS: " + getAts());
        streetsList = streetFacade.findByATS(getAts());
    }

    public void setStreetsList(List<Streets> streetsList) {
        this.streetsList = streetsList;
    }

    public Streets getStreet() {
        return street;
    }

    public void setStreet(Streets street) {
        this.street = street;
    }

    public MinipopCategory getCategory() {
        return category;
    }

    public void setCategory(MinipopCategory category) {
        this.category = category;
    }

    public List<MinipopCategory> getCategoryList() {
        if (categoryList == null) {
            categoryList = new ArrayList<>();
            for (MinipopCategory category : MinipopCategory.values())
                categoryList.add(category);
        }
        return categoryList;
    }

    public void setCategoryList(List<MinipopCategory> categoryList) {
        this.categoryList = categoryList;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public Integer getMidipopSlot() {
        return midipopSlot;
    }

    public void setMidipopSlot(Integer midipopSlot) {
        this.midipopSlot = midipopSlot;
    }

    public Ats getAts() {
        return ats;
    }

    public void setAts(Ats ats) {
        this.ats = ats;
    }

    public Nas getNas() {
        return nas;
    }

    public void setNas(Nas nas) {
        this.nas = nas;
    }

    public Midipop getMidipop() {
        return midipop;
    }

    public void setMidipop(Midipop midipop) {
        this.midipop = midipop;
    }

    public ServiceProvider getProvider() {
        return provider;
    }

    public void setProvider(ServiceProvider provider) {
        this.provider = provider;
    }

    public List<Midipop> getMidipopSelectList() {
        if (midipopSelectList == null)
            midipopSelectList = midipopFacade.findAll();
        return midipopSelectList;
    }

    public void setMidipopSelectList(List<Midipop> midipopSelectList) {
        this.midipopSelectList = midipopSelectList;
    }

    public String getSerial() {
        return serial;
    }

    public void setSerial(String serial) {
        this.serial = serial;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public List<MidipopPort> getMidipopPortList() {

        return midipopPortList;
    }

    public void setMidipopPortList(List<MidipopPort> midipopPortList) {
        this.midipopPortList = midipopPortList;
    }

    public Integer getMidipopPortNumber() {
        return midipopPortNumber;
    }

    public void setMidipopPortNumber(Integer midipopPortNumber) {
        this.midipopPortNumber = midipopPortNumber;
    }

    public List<Midipop> getMidipopList() {
        midipopList = new ArrayList<>();
        midipop = null;
        log.debug(ats);
        if (ats != null) {
            midipopList = midipopFacade.findMidipopByAts(ats);

        }
        return midipopList;
    }

    public void setMidipopList(List<Midipop> midipopList) {
        this.midipopList = midipopList;
    }

    public List<Midipop> midipopListByAts(Ats ats) {
        midipopList = new ArrayList<>();
        if (ats != null)
            midipopList = midipopFacade.findMidipopByAts(ats);
        return midipopList;
    }

    public Integer getSubVlan() {
        return subVlan;
    }

    public void setSubVlan(Integer subVlan) {
        this.subVlan = subVlan;
    }

    public Integer getMasterVlan() {
        return masterVlan;
    }

    public void setMasterVlan(Integer masterVlan) {
        this.masterVlan = masterVlan;
    }

    public List<Nas> getNasSelectList() {
        if (nasSelectList == null) {
            nasSelectList = nasFacade.findAllNas();
        }
        return nasSelectList;
    }

    public void setNasSelectList(List<Nas> nasSelectList) {
        this.nasSelectList = nasSelectList;
    }

    public MiniPop getMiniPop() {
        //miniPop = minipopID != null ? minipopFacade.find(minipopID) : new MiniPop();
        if (miniPop == null) {
            miniPop = new MiniPop();
        }

        return miniPop;
    }

    public void setMiniPop(MiniPop miniPop) {
        this.miniPop = miniPop;
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


    public String getAtsName() {
        return atsName;
    }

    public void setAtsName(String atsName) {
        this.atsName = atsName;
    }

    public List<ServiceProvider> getProviderSelectList() {
        if (providerSelectList == null) {
            providerSelectList = new ArrayList<>();

            if (selected != null)
                providerSelectList.add(selected.getProvider());

            for (ServiceProvider prov : providerFacade.findAll()) {
                if (selected != null && prov.getId() == selected.getProvider().getId())
                    continue;

                providerSelectList.add(prov);
            }
        }
        return providerSelectList;
    }

    public void setProviderSelectList(List<ServiceProvider> providerSelectList) {
        this.providerSelectList = providerSelectList;
    }

    public LazyTableModel<MiniPop> getMiniPopLazyTableModel() {
        if (miniPopLazyTableModel == null) {
            miniPopLazyTableModel = new LazyTableModel<MiniPop>(minipopFacade);
        }
        return miniPopLazyTableModel;
    }

    public void setMiniPopLazyTableModel(LazyTableModel<MiniPop> miniPopLazyTableModel) {
        this.miniPopLazyTableModel = miniPopLazyTableModel;
    }

    public List<MiniPop> getMiniPopFilteredList() {
        return miniPopFilteredList;
    }

    public void setMiniPopFilteredList(List<MiniPop> miniPopFilteredList) {
        this.miniPopFilteredList = miniPopFilteredList;
    }

    public Long getMinipopID() {
        return minipopID;
    }

    public void setMinipopID(Long minipopID) {
        this.minipopID = minipopID;
    }

    public List<SelectItem> getDeviceStatusList() {
        if (deviceStatusList == null) {
            deviceStatusList = new ArrayList<>();

            if (miniPop != null) {
                deviceStatusList.add(new SelectItem(miniPop.getDeviceStatus()));
            }

            for (DeviceStatus deviceStatus : DeviceStatus.values()) {
                if (miniPop != null && deviceStatus == miniPop.getDeviceStatus()) {
                    continue;
                }

                deviceStatusList.add(new SelectItem(deviceStatus));
            }
        }
        return deviceStatusList;
    }

    public void setDeviceStatusList(List<SelectItem> deviceStatusList) {
        this.deviceStatusList = deviceStatusList;
    }

    public MiniPop getSelected() {
        log.debug("getSelected");
        if (selected == null && minipopID != null) {
            selected = minipopFacade.find(minipopID);
        }
        return selected;
    }

    public void setSelected(MiniPop selected) {
        this.selected = selected;
    }

    public void setDeviceStatus(DeviceStatus status) {
        this.status = status;
    }

    public DeviceStatus getDeviceStatus() {
        return this.status;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public String getSwitch_id() {
        return switch_id;
    }

    public void setSwitch_id(String switch_id) {
        this.switch_id = switch_id;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String create() {
        try {

            if (category == null) {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Category must be selected", "Category must be selected"));
                return null;
            }

            if (provider == null) {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Provider must be selected", "Provider must be selected"));
                return null;
            }

            if (provider.getId() != Providers.CITYNET.getId() && (miniPop.getMac() == null || miniPop.getMac().isEmpty())) {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Mac must be filled", "Mac must be filled"));
                return null;
            }

            log.debug("PROVIDER: " + provider + "   " + Providers.CITYNET.getId());
            log.debug(provider.getId() == Providers.CITYNET.getId());

            if (provider.getId() == Providers.CITYNET.getId()) {
                log.debug("provider: " + provider);
                if (ats == null) {
                    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Ats must be selected", "Ats must be selected"));
                    return null;
                }
                if (midipop == null) {
                    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Midipop must be selected", "Midipop must be selected"));
                    return null;
                }

                if (midipopPortNumber == null) {
                    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Midipop port must be filled", "Midipop port must be filled"));
                    return null;
                }


                if (!(midipopPortNumber >= 1 && midipopPortNumber <= midipop.getSwitchPort())) {
                    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Midipop port is not available", "Midipop port is not available"));
                    return null;
                }

                if (!(midipopSlot >= 1 && midipopSlot <= midipop.getNodes())) {
                    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Midipop slot is not available", "Midipop slot is not available"));
                    return null;
                }

                if (nas == null) {
                    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Nas must be selected", "Nas must be selected"));
                    return null;
                }

                if (miniPop.getSubVlan() == null) {
                    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Sub vlan be selected", "Sub vlan be selected"));
                    return null;
                }

                /*
                Object lock = new Object();
                synchronized (lock) {
                    boolean isFreePort = portFacade.isFreePort(midipopPortNumber, midipop);
                    if (!isFreePort) {
                        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "The port reserved, select another midipop port or midipop", "The port reserved, select another midipop port or midipop"));
                        return null;
                    }
                    miniPop.setMasterVlan(midipop.getVlan() + midipopPortNumber);
                    MidipopPort mdpPort = new MidipopPort(midipopPortNumber);
                    mdpPort.setMidipop(midipop);
                    mdpPort.setOccupied(true);
                    portFacade.save(mdpPort);
                }
                */

                MidipopPort midipopPort = portFacade.findPortByMidipopAndPortNumber(midipop, midipopPortNumber, midipopSlot);
                if (midipopPort == null) {
                    midipopPort = new MidipopPort();
                    midipopPort.setOccupied(true);
                    midipopPort.setMidipop(midipop);
                    midipopPort.setMidipopSlot(midipopSlot);
                    midipopPort.setMidipopPort(midipopPortNumber);
                    portFacade.save(midipopPort);
                } else {
                    midipopPort.setOccupied(true);
                    portFacade.update(midipopPort);
                }

                log.debug("SLOT: " + midipopSlot + "PORT: " + midipopPortNumber);

                miniPop.setMidipopSlot(midipopSlot);
                miniPop.setMidipopPort(midipopPortNumber);
                miniPop.setMasterVlan((midipopSlot - 1) * 50 + midipopPortNumber + midipop.getVlan());
            }

            if (housesAsText != null && !housesAsText.isEmpty()) {
                List<House> houseList = new ArrayList<>();
                House houseEntity;
                String[] strHouses = housesAsText.split(",");
                for (String no : strHouses) {
                    no = no.trim();
                    houseEntity = houseFacade.findByHouseNo(no);
                    if (houseEntity == null) {
                        houseEntity = new House();
                        houseEntity.setHouseNo(no);
                        houseFacade.save(houseEntity);
                    }
                    houseList.add(houseEntity);
                }
                miniPop.setHouses(houseList);
            }

            miniPop.setStreet(street);
            miniPop.setCategory(category);
            miniPop.setNas(nas);
            miniPop.setAts(ats);
            miniPop.setMidipop(midipop);
            miniPop.setProvider(provider);
            this.minipopFacade.save(miniPop);

            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Minipop created successfully", "Minipop created successfully"));
            return String.format("%sindex.xhtml?faces-redirect=true", PATH);

        } catch (Exception ex) {
            log.error(String.format("Insert of minipop %s failed", miniPop), ex);
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Edit failed", "Edit failed"));
            return null;
        }
    }

    public String edit() {
        if (selected == null) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Minipop not found", "Minipop not found"));
            return null;
        }
        try {
            log.debug("minipop edited: " + selected);
            minipopFacade.update(selected);
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Edit successfull", "Edit successfull"));
            return String.format("%sindex.xhtml?faces-redirect=true", PATH);
        } catch (Exception ex) {
            log.error(String.format("Update of minipop %s failed", miniPop), ex);
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Edit failed", "Edit failed"));
            return null;
        }
    }

    public String show() {
        if (selected == null) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Please select a minipop", "Please select a minipop"));
            return null;
        }

        return String.format("%sview.xhtml?minipop=%d&amp;faces-redirect=true&amp;includeViewParams=true", PATH, selected.getId());
    }

    public void onChange(AjaxBehaviorEvent ev) {
        log.debug("Change detected: new value=" + miniPop.getNumberOfPorts());
    }

    private boolean check() {
        log.debug("Search criteriea empty? " + ((switch_id == null || switch_id.isEmpty())
                && (mac == null || mac.isEmpty())
                && (status == null)
                && (address == null || address.isEmpty())
                && (provider == null)
                && (nas == null)
                && (masterVlan == null)
                && (midipop == null)
                && (ats == null)));

        return !((switch_id == null || switch_id.isEmpty())
                && (mac == null || mac.isEmpty())
                && (status == null)
                && (ip == null || ip.isEmpty())
                && (nas == null)
                && (midipop == null)
                && (masterVlan == null)
                && (address == null || address.isEmpty())
                && (provider == null)
                && (ats == null));
    }

    public void search() {
        log.debug("Search entered");
        minipopFacade.clearFilters();

        if (!check()) {
            log.debug("Fields empty");
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Please provide search criteria", "Please provide search criteria"));
            return;
        }

        if (switch_id != null && !switch_id.isEmpty()) {
            minipopFacade.addFilter(MiniPopPersistenceFacade.Filter.SWITCH_ID, switch_id);
        }
        if (mac != null && !mac.isEmpty()) {
            minipopFacade.addFilter(MiniPopPersistenceFacade.Filter.MAC_ADDRESS, mac);
        }
        if (address != null && !address.isEmpty()) {
            minipopFacade.addFilter(MiniPopPersistenceFacade.Filter.ADDRESS, address);
        }
        if (status != null) {
            minipopFacade.addFilter(MiniPopPersistenceFacade.Filter.STATUS, status);
        }

        if (ats != null) {
            minipopFacade.addFilter(MiniPopPersistenceFacade.Filter.ATS, ats);
        }

        if (provider != null) {
            minipopFacade.addFilter(MiniPopPersistenceFacade.Filter.PROVIDER, provider);
        }

        if (nas != null) {
            minipopFacade.addFilter(MiniPopPersistenceFacade.Filter.NAS, nas);
        }

        if (midipop != null) {
            minipopFacade.addFilter(MiniPopPersistenceFacade.Filter.MIDIPOP, midipop);
        }

        if (masterVlan != null) {
            minipopFacade.addFilter(MiniPopPersistenceFacade.Filter.MASTERVLAN, masterVlan);
        }

        if (ip != null && !ip.isEmpty()) {
            minipopFacade.addFilter(MiniPopPersistenceFacade.Filter.IP, ip);
        }

        log.debug("Search Filters: " + minipopFacade.getFilters());
        miniPopLazyTableModel = new LazyTableModel<>(minipopFacade);
    }

    public void onRowEdit(RowEditEvent event) {

        MiniPop miniPop = (MiniPop) event.getObject();
        if (miniPop.getProvider().getId() == Providers.CITYNET.getId()) {
            if (miniPop.getAts() == null || miniPop.getNas() == null || miniPop.getMidipop() == null || miniPop.getMasterVlan() == null || miniPop.getSubVlan() == null) {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Fill all required fields", "Fill all required fields"));
                return;
            }

            miniPop.setMasterVlan((miniPop.getMidipopSlot() - 1) * 50 + miniPop.getMidipopPort() + miniPop.getMidipop().getVlan());

            if (!(miniPop.getMasterVlan() > miniPop.getMidipop().getVlan() && miniPop.getMasterVlan() <= miniPop.getMidipop().getVlan() + miniPop.getMidipop().getNodes() * 50)) {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Master vlan is incorrect", "Master vlan is incorrect"));
                return;
            }

            MiniPop miniPopOld = minipopFacade.find(miniPop.getId());
            Midipop midipopOld = miniPopOld.getMidipop();
            Integer portOld = miniPopOld.getMidipopPort();
            Integer slotOld = miniPopOld.getMidipopSlot();

            MidipopPort midipopPort = portFacade.findPortByMidipopAndPortNumber(midipopOld, portOld, slotOld);
            if (midipopPort != null) {
                log.debug(midipopOld.getId() + " " + slotOld + " " + portOld);
                boolean res = minipopFacade.isMidipopPortReserved(midipopOld, slotOld, portOld);
                log.debug("RES: " + res);
                if (!res) {
                    midipopPort.setOccupied(false);
                    portFacade.update(midipopPort);
                }
            }

            midipopPort = null;
            midipopPort = portFacade.findPortByMidipopAndPortNumber(miniPop.getMidipop(), miniPop.getMidipopPort(), miniPop.getMidipopSlot());
            if (midipopPort != null) {
                midipopPort.setOccupied(true);
                portFacade.update(midipopPort);
            } else {
                midipopPort = new MidipopPort();
                midipopPort.setOccupied(true);
                midipopPort.setMidipopSlot(miniPop.getMidipopSlot());
                midipopPort.setMidipopPort(miniPop.getMidipopPort());
                midipopPort.setMidipop(miniPop.getMidipop());
                portFacade.save(midipopPort);
            }

        }

        if (miniPop.getHousesAsText() != null && !miniPop.getHousesAsText().isEmpty()) {
            List<House> houseList = new ArrayList<>();
            House houseEntity;
            String[] strHouses = miniPop.getHousesAsText().split(",");
            for (String no : strHouses) {
                no = no.trim();
                houseEntity = houseFacade.findByHouseNo(no);
                if (houseEntity == null) {
                    houseEntity = new House();
                    houseEntity.setHouseNo(no);
                    houseFacade.save(houseEntity);
                }
                houseList.add(houseEntity);
            }
            miniPop.setHouses(houseList);
        }

        log.debug("House TEXT: " + miniPop.getHousesAsText());

        log.debug(miniPop);
        minipopFacade.update(miniPop);
        FacesMessage msg = new FacesMessage("Minipop Edited", "" + ((MiniPop) event.getObject()));
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }

}

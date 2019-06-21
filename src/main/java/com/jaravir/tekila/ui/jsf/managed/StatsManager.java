/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.jaravir.tekila.ui.jsf.managed;

import com.jaravir.tekila.module.service.entity.ServiceProvider;
import com.jaravir.tekila.module.service.persistence.manager.ServiceProviderPersistenceFacade;
import com.jaravir.tekila.module.stats.persistence.entity.OfflineBroadbandStats;
import com.jaravir.tekila.module.stats.persistence.entity.OnlineBroadbandStats;

import com.jaravir.tekila.module.stats.persistence.manager.OfflineStatsPersistenceFacade;
import com.jaravir.tekila.module.stats.persistence.manager.OnlineStatsPersistenceFacade;

import com.jaravir.tekila.module.subscription.persistence.entity.Subscription;
import com.jaravir.tekila.module.subscription.persistence.management.SubscriptionPersistenceFacade;
import com.jaravir.tekila.ui.model.LazyTableModel;

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

/**
 *
 * @author sajabrayilov
 */
@ManagedBean(name="statsManager")
@ViewScoped
public class StatsManager  implements Serializable {
    @EJB
    private OfflineStatsPersistenceFacade offlineFacade;
    private LazyTableModel<OfflineBroadbandStats> offlineStats;
    @EJB
    private OnlineStatsPersistenceFacade onlineFacade;
    @EJB private SubscriptionPersistenceFacade subscriptionFacade;
    @EJB private ServiceProviderPersistenceFacade serviceProviderPersistenceFacade;

    private OfflineBroadbandStats selectedOfflineStats;
    private String framedAddress;
    private String nasIpAddress;
    private String agreement;

    private OnlineBroadbandStats selectedOnlineStats;
    private String userName;
    private String callingStationID;
    private String accountID;

    private LazyTableModel<OnlineBroadbandStats> onlineStats;

    private String selectedProviderId;
    private List<SelectItem> providerList;

    public String getFramedAddress() {
        return framedAddress;
    }

    public void setFramedAddress(String framedAddress) {
        this.framedAddress = framedAddress;
    }

    public String getNasIpAddress() {
        return nasIpAddress;
    }

    public void setNasIpAddress(String nasIpAddress) {
        this.nasIpAddress = nasIpAddress;
    }

    public String getAgreement() {
        return agreement;
    }

    public void setAgreement(String agreement) {
        this.agreement = agreement;
    }

    public String getSelectedProvider() {
        return selectedProviderId;
    }

    public void setSelectedProvider(String selectedProvider) {
        this.selectedProviderId = selectedProvider;
    }

    public List<SelectItem> getProviderList() {
        if (providerList == null) {
            providerList = new ArrayList<>();
            for (ServiceProvider provider : serviceProviderPersistenceFacade.findAll()) {
                providerList.add(new SelectItem(String.valueOf(provider.getId()), provider.getName()));
            }
        }
        return providerList;
    }

    public void setProviderList(List<SelectItem> providerList) {
        this.providerList = providerList;
    }

    @PostConstruct
    public void init () {
        onlineFacade.clearFilters();
    }

    public LazyTableModel<OfflineBroadbandStats> getOfflineStats() {
        if (offlineStats == null) {
            offlineStats = new LazyTableModel<OfflineBroadbandStats>(offlineFacade);
        }
        return offlineStats;
    }

    public void setOfflineStats(LazyTableModel<OfflineBroadbandStats> offlineStats) {
        this.offlineStats = offlineStats;
    }

    public LazyTableModel<OnlineBroadbandStats> getOnlineStats() {
        if (onlineStats == null) {
            onlineStats = new LazyTableModel<OnlineBroadbandStats>(onlineFacade);
        }
        return onlineStats;
    }

    public void setOnlineStats(LazyTableModel<OnlineBroadbandStats> onlineStats) {
        this.onlineStats = onlineStats;
    }

    public OnlineBroadbandStats getSelectedOnlineStats() {
           return selectedOnlineStats;
    }

    public void setSelectedOnlineStats(OnlineBroadbandStats selectedOnlineStats) {
        this.selectedOnlineStats = selectedOnlineStats;
    }

    public OfflineBroadbandStats getSelectedOfflineStats() {
        return selectedOfflineStats;
    }

    public void setSelectedOfflineStats(OfflineBroadbandStats selectedOfflineStats) {
        this.selectedOfflineStats = selectedOfflineStats;
    }

    public String navigateToSubscription () {
        if (selectedOnlineStats == null) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Please select a record","Please select a record"));
            return null;
        }

        try {
            Subscription subscription = subscriptionFacade.findByAgreementOrdinary(selectedOnlineStats.getAccountID());
            return String.format("/pages/subscription/view?subscription=%d&amp;subscriber=%d&amp;faces-redirect=true&amp;includeViewParams=true",
                    subscription.getId(), subscription.getSubscriber().getId());
        }
        catch (Exception ex) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Subscription not found","Subscription not found"));
            return null;
        }
    }

    public String navigateToSubscriptionOffline () {
        if (selectedOfflineStats == null) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Please select a record","Please select a record"));
            return null;
        }

        try {
            Subscription subscription = subscriptionFacade.findByAgreementOrdinary(selectedOfflineStats.getAccountID());
            return String.format("/pages/subscription/view?subscription=%d&amp;subscriber=%d&amp;faces-redirect=true&amp;includeViewParams=true",
                    subscription.getId(), subscription.getSubscriber().getId());
        }
        catch (Exception ex) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Subscription not found","Subscription not found"));
            return null;
        }
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getCallingStationID() {
        return callingStationID;
    }

    public void setCallingStationID(String callingStationID) {
        this.callingStationID = callingStationID;
    }

    public String getAccountID() {
        return accountID;
    }

    public void setAccountID(String accountID) {
        this.accountID = accountID;
    }

    public void search () {
        if (!check()) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Please fill any field","Please fill any field"));
            return;
        }

        onlineFacade.clearFilters();

        if (userName != null && !userName.isEmpty()) {
            onlineFacade.addFilter(OnlineStatsPersistenceFacade.Filter.USERNAME, userName);
        }

        if (callingStationID != null && !callingStationID.isEmpty()) {
            onlineFacade.addFilter(OnlineStatsPersistenceFacade.Filter.CALLING_STATION, callingStationID);
        }

        if (selectedProviderId != null && Integer.parseInt(selectedProviderId) != -1) {
            onlineFacade.addFilter(OnlineStatsPersistenceFacade.Filter.PROVIDER, selectedProviderId);
        }
        onlineStats = new LazyTableModel<>(onlineFacade);
    }

    public void searchOffline () {
        if (!check()) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Please fill any field","Please fill any field"));
            return;
        }

        offlineFacade.clearFilters();

        if (userName != null && !userName.isEmpty()) {
            offlineFacade.addFilter(OfflineStatsPersistenceFacade.Filter.USERNAME, userName);
        }

        if (callingStationID != null && !callingStationID.isEmpty()) {
            offlineFacade.addFilter(OfflineStatsPersistenceFacade.Filter.CALLING_STATION, callingStationID);
        }

        if (selectedProviderId != null && Integer.parseInt(selectedProviderId) != -1) {
            offlineFacade.addFilter(OfflineStatsPersistenceFacade.Filter.PROVIDER, selectedProviderId);
        }

        if (agreement != null && !agreement.isEmpty()) {
            offlineFacade.addFilter(OfflineStatsPersistenceFacade.Filter.ACCOUNT_ID, agreement);
        }

        if (framedAddress != null && !framedAddress.isEmpty()) {
            offlineFacade.addFilter(OfflineStatsPersistenceFacade.Filter.FRAMED_ADDRESS, framedAddress);
        }

        if (nasIpAddress != null && !nasIpAddress.isEmpty()) {
            offlineFacade.addFilter(OfflineStatsPersistenceFacade.Filter.NAS_IP_ADDRESS, nasIpAddress);
        }
        offlineStats = new LazyTableModel<>(offlineFacade);
    }

    public void clear () {
        userName = null;
        callingStationID = null;
        onlineFacade.clearFilters();
        onlineStats = new LazyTableModel<>(onlineFacade);
    }

    public void clearOffline () {
        userName = null;
        callingStationID = null;
        agreement = null;
        framedAddress = null;
        nasIpAddress = null;
        offlineFacade.clearFilters();
        offlineStats = new LazyTableModel<>(offlineFacade);
    }

    private boolean check () {
        return (userName != null && !userName.isEmpty()) || (callingStationID != null && !callingStationID.isEmpty())
                || (selectedProviderId != null && Integer.parseInt(selectedProviderId) != -1)
                || (agreement != null && !agreement.isEmpty()) || (framedAddress != null && !framedAddress.isEmpty())
                || (nasIpAddress != null && !nasIpAddress.isEmpty());
    }
}
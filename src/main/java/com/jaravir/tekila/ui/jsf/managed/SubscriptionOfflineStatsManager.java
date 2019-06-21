package com.jaravir.tekila.ui.jsf.managed;

import com.jaravir.tekila.base.filter.MatchingOperation;
import com.jaravir.tekila.engines.EngineFactory;
import com.jaravir.tekila.engines.ProvisioningEngine;
import com.jaravir.tekila.module.stats.persistence.entity.OfflineBroadbandStats;
import com.jaravir.tekila.module.stats.persistence.manager.OfflineStatsPersistenceFacade;
import com.jaravir.tekila.module.subscription.persistence.entity.Subscription;
import com.jaravir.tekila.module.subscription.persistence.management.SubscriptionPersistenceFacade;
import com.jaravir.tekila.provision.broadband.devices.manager.Providers;
import com.jaravir.tekila.provision.exception.ProvisionerNotFoundException;
import com.jaravir.tekila.ui.model.LazyTableModel;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.primefaces.component.datatable.DataTable;
import org.primefaces.model.LazyDataModel;

import javax.annotation.ManagedBean;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.*;

/**
 * Created by sajabrayilov on 8/17/2015.
 */
@ManagedBean
@ViewScoped
public class SubscriptionOfflineStatsManager {
    @EJB
    private OfflineStatsPersistenceFacade offlineFacade;
    @EJB
    private SubscriptionPersistenceFacade subFacade;
    @EJB
    private EngineFactory provisioningFactory;
    private final static Logger logger = Logger.getLogger(SubscriptionOfflineStatsManager.class);

    private Date filterStartTime;
    private Date filterEndTime;
    private String filterFrameIpAddress;
    private String subscriptionAgreement;
    private String filterUsername;
    private String filterTeminationCause;
    private String totalDown;
    private String totalUp;
    private Object[] totalList;
    private final static DecimalFormat df = new DecimalFormat();

    private LazyDataModel<OfflineBroadbandStats> offlineStatsList;
    private List<OfflineBroadbandStats> citynetOfflineStatsList;

    @PostConstruct
    public void init() {
        offlineFacade.clearFilters();
        df.setRoundingMode(RoundingMode.HALF_UP);
        totalDown = "0.00";
        totalUp = "0.00";
    }

    public List<OfflineBroadbandStats> getCitynetOfflineStatsList() {
        return citynetOfflineStatsList;
    }

    public void setCitynetOfflineStatsList(List<OfflineBroadbandStats> citynetOfflineStatsList) {
        this.citynetOfflineStatsList = citynetOfflineStatsList;
    }

    public Date getFilterStartTime() {
        if (filterStartTime == null) {
            filterStartTime = DateTime.now().minusDays(1).withTimeAtStartOfDay().toDate();
        }
        return filterStartTime;
    }

    public void setFilterStartTime(Date filterStartTime) {
        this.filterStartTime = filterStartTime;
    }

    public Date getFilterEndTime() {
        if (filterEndTime == null) {
            filterEndTime = DateTime.now().withTime(23, 59, 59, 999).toDate();
        }
        return filterEndTime;
    }

    public void setFilterEndTime(Date filterEndTime) {
        this.filterEndTime = filterEndTime;
    }

    public LazyDataModel<OfflineBroadbandStats> getOfflineStatsList() {
        return offlineStatsList;
    }

    public void setOfflineStatsList(LazyDataModel<OfflineBroadbandStats> offlineStatsList) {
        this.offlineStatsList = offlineStatsList;
    }

    public String getFilterFrameIpAddress() {
        return filterFrameIpAddress;
    }

    public void setFilterFrameIpAddress(String filterFrameIpAddress) {
        this.filterFrameIpAddress = filterFrameIpAddress;
    }

    public String getSubscriptionAgreement() {
        return subscriptionAgreement;
    }

    public void setSubscriptionAgreement(String subscriptionAgreement) {
        this.subscriptionAgreement = subscriptionAgreement;
    }

    public String getFilterUsername() {
        return filterUsername;
    }

    public void setFilterUsername(String filterUsername) {
        this.filterUsername = filterUsername;
    }

    public String getFilterTeminationCause() {
        return filterTeminationCause;
    }

    public void setFilterTeminationCause(String filterTeminationCause) {
        this.filterTeminationCause = filterTeminationCause;
    }

    public void search() throws ProvisionerNotFoundException {
        if (!check()) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Please provide at least one criteria", "Please provide at least one criteria"));
            return;
        }

        DataTable table = (DataTable) FacesContext.getCurrentInstance().getViewRoot().findComponent("eqForm:subDataTable");
        table.reset();

        offlineFacade.clearFilters();
        offlineFacade.addFilter(OfflineStatsPersistenceFacade.Filter.ACCOUNT_ID, subscriptionAgreement);

        if (filterFrameIpAddress != null && !filterFrameIpAddress.isEmpty())
            offlineFacade.addFilter(OfflineStatsPersistenceFacade.Filter.FRAMED_ADDRESS, filterFrameIpAddress);

        if (filterUsername != null && !filterUsername.isEmpty())
            offlineFacade.addFilter(OfflineStatsPersistenceFacade.Filter.USERNAME, filterUsername);

        if (filterTeminationCause != null && !filterTeminationCause.isEmpty())
            offlineFacade.addFilter(OfflineStatsPersistenceFacade.Filter.TERMINATION_CAUSE, filterTeminationCause);

        if (filterStartTime != null && filterEndTime != null && new DateTime(filterStartTime).isBefore(new DateTime(filterEndTime))) {
            OfflineStatsPersistenceFacade.Filter startDate = OfflineStatsPersistenceFacade.Filter.START_DATE;
            startDate.setOperation(MatchingOperation.BETWEEN);
            Map<String, Date> dateMap = new HashMap<>();
            //dateMap.put("from", new DateTime(filterStartTime).withTimeAtStartOfDay().toDate());
            //dateMap.put("to", new DateTime(filterEndTime).withTime(23, 59, 59, 999).toDate());
            dateMap.put("from", new DateTime(filterStartTime).toDate());
            dateMap.put("to", new DateTime(filterEndTime).toDate());
            offlineFacade.addFilter(startDate, dateMap);
        }

        Subscription subscription = subFacade.findByAgreementOrdinary(subscriptionAgreement);
        if (subscription.getService().getProvider().getId() == Providers.CITYNET.getId()) {

            offlineFacade.addFilter(OfflineStatsPersistenceFacade.Filter.START_DATE, filterStartTime);
            offlineFacade.addFilter(OfflineStatsPersistenceFacade.Filter.END_TIME,filterEndTime);

            ProvisioningEngine provisioner = provisioningFactory.getProvisioningEngine(subscription);
            citynetOfflineStatsList = provisioner.offlineBroadbandStats(subscription, offlineFacade.getFilters());
            double totDown = 0.0, totUp = 0.0;
            for (OfflineBroadbandStats offline : citynetOfflineStatsList) {
                totUp += Double.valueOf(offline.getUp());
                totDown += Double.valueOf(offline.getDown());
            }
            totalUp = String.format("%.2f", totUp);
            totalDown = String.format("%.2f", totDown);
            logger.debug("offlineList size: " + citynetOfflineStatsList.size());
        } else {

            offlineFacade.clearFilters();
            offlineFacade.addFilter(OfflineStatsPersistenceFacade.Filter.ACCOUNT_ID, subscriptionAgreement);

            if (filterFrameIpAddress != null && !filterFrameIpAddress.isEmpty())
                offlineFacade.addFilter(OfflineStatsPersistenceFacade.Filter.FRAMED_ADDRESS, filterFrameIpAddress);

            if (filterUsername != null && !filterUsername.isEmpty())
                offlineFacade.addFilter(OfflineStatsPersistenceFacade.Filter.USERNAME, filterUsername);

            if (filterTeminationCause != null && !filterTeminationCause.isEmpty())
                offlineFacade.addFilter(OfflineStatsPersistenceFacade.Filter.TERMINATION_CAUSE, filterTeminationCause);

            if (filterStartTime != null && filterEndTime != null && new DateTime(filterStartTime).isBefore(new DateTime(filterEndTime))) {
                OfflineStatsPersistenceFacade.Filter startDate = OfflineStatsPersistenceFacade.Filter.START_DATE;
                startDate.setOperation(MatchingOperation.BETWEEN);
                Map<String, Date> dateMap = new HashMap<>();
                //dateMap.put("from", new DateTime(filterStartTime).withTimeAtStartOfDay().toDate());
                //dateMap.put("to", new DateTime(filterEndTime).withTime(23, 59, 59, 999).toDate());
                dateMap.put("from", new DateTime(filterStartTime).toDate());
                dateMap.put("to", new DateTime(filterEndTime).toDate());
                offlineFacade.addFilter(startDate, dateMap);
            }
            offlineStatsList = new LazyTableModel<>(offlineFacade);
            try {
                totalList = offlineFacade.<Double>sumWithFilters(Double.class, "down", "up");

                if (totalList != null && totalList.length > 0 && totalList.length == 2) {
                    if (totalList[0] != null) {
                        totalDown = String.format("%.2f", ((BigDecimal) totalList[0]).doubleValue());
                    }
                    if (totalList[1] != null) {
                        totalUp = String.format("%.2f", ((BigDecimal) totalList[1]).doubleValue());
                    }
                }

                logger.debug("search: totals=" + totalList);

            } catch (Exception ex) {
                logger.error("search: cannot get totals", ex);
            }
        }
    }

    public void reset() {
        offlineFacade.clearFilters();
        offlineStatsList = null;
        filterFrameIpAddress = null;
        filterTeminationCause = null;
        filterUsername = null;
        filterStartTime = null;
        filterEndTime = null;
        getOfflineStatsList();

        DataTable table = (DataTable) FacesContext.getCurrentInstance().getViewRoot().findComponent("eqForm:subDataTable");
        table.reset();
    }

    private boolean check() {
        if (filterStartTime == null && filterStartTime == null
                && (filterFrameIpAddress == null || filterFrameIpAddress.isEmpty())
                && (filterUsername == null || filterUsername.isEmpty())
                && (filterTeminationCause == null || filterTeminationCause.isEmpty())
                ) {
            return false;
        }

        return true;
    }

    public String redirect() {
        if (subscriptionAgreement != null && !subscriptionAgreement.isEmpty()) {
            Subscription subsription = subFacade.findByAgreementOrdinary(subscriptionAgreement);
            return "/pages/subscription/view.xhtml?subscription=" + subsription.getId() +
                    "&amp;subscriber=" + subsription.getSubscriber().getId() + "&amp;faces-redirect=true&amp;includeViewParams=true";
        }

        return null;
    }

    public String getTotalDown() {
        return totalDown;
    }

    public void setTotalDown(String totalDown) {
        this.totalDown = totalDown;
    }

    public String getTotalUp() {
        return totalUp;
    }

    public void setTotalUp(String totalUp) {
        this.totalUp = totalUp;
    }

    public Long findProviderByAgreement(String agreement) {
        return subFacade.findByAgreementOrdinary(agreement).getService().getProvider().getId();
    }

    public String getCitynetProviderId(){
        return String.valueOf(Providers.CITYNET.getId());
    }

    public String getAzertelecomProviderId(){
        return String.valueOf(Providers.AZERTELECOM.getId());
    }

}

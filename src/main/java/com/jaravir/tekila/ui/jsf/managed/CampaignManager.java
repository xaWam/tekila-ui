package com.jaravir.tekila.ui.jsf.managed;

import com.jaravir.tekila.base.filter.MatchingOperation;
import com.jaravir.tekila.module.campaign.Campaign;
import com.jaravir.tekila.module.campaign.CampaignPersistenceFacade;
import com.jaravir.tekila.module.campaign.CampaignStatus;
import com.jaravir.tekila.module.campaign.CampaignTarget;
import com.jaravir.tekila.module.service.entity.Service;
import com.jaravir.tekila.module.service.entity.ServiceProvider;
import com.jaravir.tekila.module.service.entity.ValueAddedService;
import com.jaravir.tekila.module.service.persistence.manager.ServicePersistenceFacade;
import com.jaravir.tekila.module.service.persistence.manager.ServiceProviderPersistenceFacade;
import com.jaravir.tekila.module.service.persistence.manager.VASPersistenceFacade;
import com.jaravir.tekila.ui.jsf.managed.util.ServiceWithBonus;
import com.jaravir.tekila.ui.model.LazyTableModel;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.LazyDataModel;

import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.event.AjaxBehaviorEvent;
import java.util.*;

/**
 * Created by kmaharov on 02.08.2016.
 */
@ManagedBean(name = "campaignManager")
@ViewScoped
public class CampaignManager {
    private static final Logger log = Logger.getLogger(CampaignManager.class);

    @EJB
    CampaignPersistenceFacade campaignPersistenceFacade;
    @EJB
    ServiceProviderPersistenceFacade providerPersistenceFacade;
    @EJB
    ServicePersistenceFacade servicePersistenceFacade;
    @EJB
    VASPersistenceFacade vasPersistenceFacade;

    //fields for campaign list / campaign view
    private Integer campaignId;
    private Campaign selected;
    private Integer selectedProviderId;  // can be removed
    private Date selectedExpirationDate; // can be removed
    private Date selectedBonusDate;      // can be removed
    private List<Campaign> campaigns;
    private List<Campaign> filteredCampaigns;

    //fields for campaign creation
    private Campaign newCampaign;
    private Map<String, Object> providersForView;
    private Integer providerId;
    private Date expirationDate;
    private Date bonusDate;

    private String searchedServiceName;
    private LazyDataModel<Service> serviceLazyModel;
    private Service selectedService;
    private List<ServiceWithBonus> selectedServices; //used for creation and edit

    private String searchedVasName;
    private LazyDataModel<ValueAddedService> vasLazyModel;
    private ValueAddedService selectedVas;
    private List<ValueAddedService> selectedVasList;

    private String searchedCampaignName;
    private LazyDataModel<Campaign> campaignLazyModel;
    private Campaign selectedCampaign;
    private List<Campaign> selectedCampaigns; //used for creation and edit

    public Integer getCampaignId() {
        return campaignId;
    }

    public void setCampaignId(Integer campaignId) {
        this.campaignId = campaignId;
    }

    public Campaign getSelected() {
        if (selected == null && campaignId != null) {
            selected = campaignPersistenceFacade.find(campaignId);
        }
        return selected;
    }

    public void setSelected(Campaign selected) {
        this.selected = selected;
    }

    public List<Campaign> getCampaigns() {
        if (campaigns == null) {
            campaigns = campaignPersistenceFacade.findAll();
        }
        return campaigns;
    }

    public void setCampaigns(List<Campaign> campaigns) {
        this.campaigns = campaigns;
    }

    public List<Campaign> getFilteredCampaigns() {
        return filteredCampaigns;
    }

    public void setFilteredCampaigns(List<Campaign> filteredCampaigns) {
        this.filteredCampaigns = filteredCampaigns;
    }

    public CampaignTarget[] getTargetList() {
        return CampaignTarget.values();
    }

    public CampaignStatus[] getStatuses() {
        return CampaignStatus.values();
    }

    public Integer getSelectedProviderId() {
        Campaign selected = getSelected();
        this.selectedProviderId = (int) selected.getProvider().getId();
        return selectedProviderId;
    }

    public void setSelectedProviderId(Integer selectedProviderId) {
        Campaign selected = getSelected();
        ServiceProvider provider = this.providerPersistenceFacade.find(selectedProviderId);
        selected.setProvider(provider);
        this.selectedProviderId = selectedProviderId;
    }

    public Date getSelectedExpirationDate() {
        Campaign selected = getSelected();
        this.selectedExpirationDate = selected.getExpirationDate().toDate();
        return selectedExpirationDate;
    }

    public void setSelectedExpirationDate(Date selectedExpirationDate) {
        Campaign selected = getSelected();
        selected.setExpirationDate(new DateTime(selectedExpirationDate));
        this.selectedExpirationDate = selectedExpirationDate;
    }

    public Date getSelectedBonusDate() {
        Campaign selected = getSelected();
        if (selected.getBonusDate() != null) {
            this.selectedBonusDate = selected.getBonusDate().toDate();
        }
        return selectedBonusDate;
    }

    public void setSelectedBonusDate(Date selectedBonusDate) {
        Campaign selected = getSelected();
        selected.setBonusDate(new DateTime(selectedBonusDate));
        this.selectedBonusDate = selectedBonusDate;
    }

    public Integer getProviderId() {
        return providerId;
    }

    public void setProviderId(Integer providerId) {
        this.providerId = providerId;
    }

    public Date getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(Date expirationDate) {
        this.expirationDate = expirationDate;
    }

    public Date getBonusDate() {
        return bonusDate;
    }

    public void setBonusDate(Date bonusDate) {
        this.bonusDate = bonusDate;
    }

    public String getSearchedServiceName() {
        return searchedServiceName;
    }

    public void setSearchedServiceName(String searchedServiceName) {
        this.searchedServiceName = searchedServiceName;
    }

    public void onServiceSearch(AjaxBehaviorEvent event) {
        servicePersistenceFacade.clearFilters();

        if (this.searchedServiceName != null) {
            ServicePersistenceFacade.Filter nameFilter = ServicePersistenceFacade.Filter.NAME;
            nameFilter.setOperation(MatchingOperation.LIKE);
            servicePersistenceFacade.addFilter(nameFilter, searchedServiceName);
        }
        serviceLazyModel = new LazyTableModel<>(servicePersistenceFacade);
    }

    /*public void onServiceSearchReset() {
        servicePersistenceFacade.clearFilters();
        serviceLazyModel = new LazyTableModel<>(servicePersistenceFacade);
    }*/
    public void onServiceRowSelect(SelectEvent event) {
        Service selected = (Service) event.getObject();
        //Service selected = selectedService;
        for (ServiceWithBonus srv : selectedServices) {
            if (srv.getService().getId() == selected.getId()) {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(
                        FacesMessage.SEVERITY_WARN,
                        "Service duplication",
                        "You have already selected this service"));
                return;
            }
        }
        selectedServices.add(
                new ServiceWithBonus(selected, null)
        );
    }

    public Service getSelectedService() {
        return selectedService;
    }

    public void setSelectedService(Service selectedService) {
        this.selectedService = selectedService;
    }

    public List<ServiceWithBonus> getSelectedServices() {
        if (selectedServices == null) {
            selectedServices = new ArrayList<>();
            if (campaignId != null && campaignId > 0) { //there is a campaign for editing, fetch its info
                Campaign campaign = getSelected();
                for (Service service : campaign.getServiceList()) {
                    Long bonusLimit = null;
                    try {
                        bonusLimit = campaign.getBonusLimitByService(service);
                    } catch (Exception ex) {
                    }
                    selectedServices.add(new ServiceWithBonus(service, bonusLimit));
                }
            }
        }
        return selectedServices;
    }

    public void removeSelectedService(ServiceWithBonus serviceWithBonus) {
        selectedServices.remove(serviceWithBonus);
    }

    public void setSelectedServices(List<ServiceWithBonus> selectedServices) {
        this.selectedServices = selectedServices;
    }

    public void setServiceLazyModel(LazyDataModel<Service> serviceLazyModel) {
        this.serviceLazyModel = serviceLazyModel;
    }

    public LazyDataModel<Service> getServiceLazyModel() {
        if (serviceLazyModel == null) {
            serviceLazyModel = new LazyTableModel<>(servicePersistenceFacade);
        }
        return serviceLazyModel;
    }







    public void onVASSearch(AjaxBehaviorEvent event) {
        vasPersistenceFacade.clearFilters();

        if (this.searchedVasName != null) {
            VASPersistenceFacade.Filter nameFilter = VASPersistenceFacade.Filter.NAME;
            nameFilter.setOperation(MatchingOperation.LIKE);
            vasPersistenceFacade.addFilter(nameFilter, searchedVasName);
        }
        vasLazyModel = new LazyTableModel<>(vasPersistenceFacade);
    }

    public String getSearchedVasName() {
        return searchedVasName;
    }

    public void setSearchedVasName(String searchedVasName) {
        this.searchedVasName = searchedVasName;
    }

    public LazyDataModel<ValueAddedService> getVasLazyModel() {
        if (vasLazyModel == null) {
            vasLazyModel = new LazyTableModel<>(vasPersistenceFacade);
        }
        return vasLazyModel;
    }

    public void setVasLazyModel(LazyDataModel<ValueAddedService> vasLazyModel) {
        this.vasLazyModel = vasLazyModel;
    }

    public ValueAddedService getSelectedVas() {
        return selectedVas;
    }

    public void setSelectedVas(ValueAddedService selectedVas) {
        this.selectedVas = selectedVas;
    }

    public void onVasRowSelect(SelectEvent event) {
        ValueAddedService selected = (ValueAddedService) event.getObject();
        if (selectedVasList != null) {
            for (ValueAddedService vas : selectedVasList) {
                if (vas.getId() == selected.getId()) {
                    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(
                            FacesMessage.SEVERITY_WARN,
                            "Vas duplication",
                            "You have already selected this value added service"));
                    return;
                }
            }
        }
        selectedVasList.add(selected);
    }

    public List<ValueAddedService> getSelectedVasList() {
        if (selectedVasList == null) {
            if (campaignId != null && campaignId > 0) { //there is a campaign for editing, fetch its info
                Campaign campaign = getSelected();
                selectedVasList = campaign.getVasList();
            }
            if (selectedVasList == null) {
                selectedVasList = new ArrayList<>();
            }
        }
        return selectedVasList;
    }

    public void setSelectedVasList(List<ValueAddedService> selectedVasList) {
        this.selectedVasList = selectedVasList;
    }

    public void removeSelectedVas(ValueAddedService vas) {
        selectedVasList.remove(vas);
    }







    public String getSearchedCampaignName() {
        return searchedCampaignName;
    }

    public void setSearchedCampaignName(String searchedCampaignName) {
        this.searchedCampaignName = searchedCampaignName;
    }

    public LazyDataModel<Campaign> getCampaignLazyModel() {
        if (campaignLazyModel == null) {
            campaignLazyModel = new LazyTableModel<>(campaignPersistenceFacade);
        }
        return campaignLazyModel;
    }

    public void setCampaignLazyModel(LazyDataModel<Campaign> campaignLazyModel) {
        this.campaignLazyModel = campaignLazyModel;
    }

    public void onCampaignSearch(AjaxBehaviorEvent event) {
        campaignPersistenceFacade.clearFilters();

        if (this.searchedCampaignName != null) {
            CampaignPersistenceFacade.Filter nameFilter = CampaignPersistenceFacade.Filter.NAME;
            nameFilter.setOperation(MatchingOperation.LIKE);
            campaignPersistenceFacade.addFilter(nameFilter, searchedCampaignName);
        }
        campaignLazyModel = new LazyTableModel<>(campaignPersistenceFacade);
    }

    public Campaign getSelectedCampaign() {
        return selectedCampaign;
    }

    public void setSelectedCampaign(Campaign selectedCampaign) {
        this.selectedCampaign = selectedCampaign;
    }

    public void onCampaignRowSelect(SelectEvent event) {
        Campaign selected = (Campaign) event.getObject();
        //Service selected = selectedService;

        for (Campaign cmp : selectedCampaigns) {
            if (cmp.getId() == selected.getId()) {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(
                        FacesMessage.SEVERITY_WARN,
                        "Campaign duplication",
                        "You have already selected this campaign"));
                return;
            }
        }

        selectedCampaigns.add(selected);
    }

    public List<Campaign> getSelectedCampaigns() {
        if (selectedCampaigns == null) {
            selectedCampaigns = new ArrayList<>();
            if (campaignId != null && campaignId > 0) { //this campaign is for editing
                Campaign selected = getSelected();
                for (Campaign childCampaign : selected.getCampaignList()) {
                    selectedCampaigns.add(childCampaign);
                }
            }
        }
        return selectedCampaigns;
    }

    public void setSelectedCampaigns(List<Campaign> selectedCampaigns) {
        this.selectedCampaigns = selectedCampaigns;
    }








    public Map<String, Object> getProvidersForView() {
        if (providersForView == null) {
            providersForView = new LinkedHashMap<>();
            List<ServiceProvider> providers = providerPersistenceFacade.findAll();
            for (ServiceProvider provider : providers) {
                providersForView.put(provider.getName(), provider.getId());
            }
        }
        return providersForView;
    }

    public void setProvidersForView(Map<String, Object> providersForView) {
        this.providersForView = providersForView;
    }

    /*public List<String> getTargetList() {
        List<String> targetList = new ArrayList<>();
        for (CampaignTarget target : CampaignTarget.values()) {
            targetList.add(target.name());
        }
        return targetList;
    }*/


    public Campaign getNewCampaign() {
        if (newCampaign == null) {
            newCampaign = new Campaign();
        }
        return newCampaign;
    }

    public void setNewCampaign(Campaign newCampaign) {
        this.newCampaign = newCampaign;
    }

    public String create() {
        if (providerId == null) {
            log.debug("provider id is null. Cannot create campaign");
            FacesContext.getCurrentInstance().addMessage(
                    null,
                    new FacesMessage(
                            FacesMessage.SEVERITY_INFO,
                            "Provider id is null",
                            "Provider id cannot be null")
            );
        }
        ServiceProvider provider = providerPersistenceFacade.find(providerId);
        newCampaign.setProvider(provider);

        if (expirationDate == null) {
            log.debug("expiration date is null. Cannot create campaign");
            FacesContext.getCurrentInstance().addMessage(
                    null,
                    new FacesMessage(
                            FacesMessage.SEVERITY_INFO,
                            "Expiration date is null",
                            "Expiration date cannot be null")
            );
        }
        newCampaign.setExpirationDate(new DateTime(expirationDate));

        if (bonusDate == null && newCampaign.getTarget() == CampaignTarget.EXPIRATION_DATE) {
            log.debug("bonus date is null for EXPIRATION_DATE campaign. Cannot create campaign");
            FacesContext.getCurrentInstance().addMessage(
                    null,
                    new FacesMessage(
                            FacesMessage.SEVERITY_INFO,
                            "Bonus date is null",
                            "Bonus date cannot be null")
            );
        }
        newCampaign.setBonusDate(new DateTime(bonusDate));
        newCampaign.setLastUpdateDate();

        if (selectedServices != null && !selectedServices.isEmpty()) {
            List<Service> services = new ArrayList<>();
            Map<Service, Long> bonusLimits = new HashMap<>();
            for (ServiceWithBonus srv : selectedServices) {
                services.add(srv.getService());
                if (srv.getBonus() != null) {
                    bonusLimits.put(srv.getService(), srv.getBonus());
                }
            }
            newCampaign.setServiceList(services);
            newCampaign.setBonusLimits(bonusLimits);
        }

        if (selectedVasList != null && !selectedVasList.isEmpty()) {
            newCampaign.setVasList(selectedVasList);
        }

        if (selectedCampaigns != null && !selectedCampaigns.isEmpty()) {
            newCampaign.setCampaignList(selectedCampaigns);
        }
        campaignPersistenceFacade.save(newCampaign);
        return "/pages/campaigns/index.xhtml";
    }

    public String update() {
        selected.setLastUpdateDate();

        if (selectedServices != null && !selectedServices.isEmpty()) {
            List<Service> services = new ArrayList<>();
            Map<Service, Long> bonusLimits = new HashMap<>();
            for (ServiceWithBonus srv : selectedServices) {
                services.add(srv.getService());
                if (srv.getBonus() != null) {
                    bonusLimits.put(srv.getService(), srv.getBonus());
                }
            }
            selected.setServiceList(services);
            selected.setBonusLimits(bonusLimits);
        }

        if (selectedCampaigns != null && !selectedCampaigns.isEmpty()) {
            selected.setCampaignList(selectedCampaigns);
        }
        if (selectedVasList != null && !selectedVasList.isEmpty()) {
            selected.setVasList(selectedVasList);
        }
        campaignPersistenceFacade.update(selected);
        return null;
    }

    public String goToCampaignViewEditPage() {
        if (selected == null) {
            FacesContext.getCurrentInstance().addMessage(
                    null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Selection error", "Please select campaign to view")
            );
            return null;
        }
        String result = String.format("/pages/campaigns/view.xhtml?campaign=%d&amp;faces-redirect=true&amp;includeViewParams=true", selected.getId());
        log.debug(String.format("Campaign selected: %d, switching to the page: %s", selected.getId(), result));
        return result;
    }

    public String goToCampaignListPage() {
        return "/pages/campaigns/index.xhtml";
    }
}

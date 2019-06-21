package com.jaravir.tekila.ui.jsf.managed.notification;

import com.jaravir.tekila.base.entity.Language;
import com.jaravir.tekila.module.event.BillingEvent;
import com.jaravir.tekila.module.event.notification.Notification;
import com.jaravir.tekila.module.event.notification.channell.NotificationChannell;
import com.jaravir.tekila.module.notification.NotificationPersistenceFacade;
import com.jaravir.tekila.module.service.entity.Service;
import com.jaravir.tekila.module.service.entity.ServiceProvider;
import com.jaravir.tekila.module.service.persistence.manager.ServicePersistenceFacade;
import com.jaravir.tekila.module.service.persistence.manager.ServiceProviderPersistenceFacade;
import com.jaravir.tekila.module.subscription.persistence.entity.SubscriberLifeCycleType;
import com.jaravir.tekila.module.subscription.persistence.entity.SubscriberType;
import com.jaravir.tekila.ui.model.LazyTableModel;
import org.apache.log4j.Logger;
import org.primefaces.model.LazyDataModel;

import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.faces.event.ActionEvent;

/**
 * Created by sajabrayilov on 12/1/2014.
 */
@ManagedBean
@ViewScoped
public class NotificationManager implements Serializable {

    private transient final static Logger log = Logger.getLogger(NotificationManager.class);
    private final static String PATH = "/pages/notifications/";
    @EJB
    private NotificationPersistenceFacade notificationFacade;
    @EJB
    private ServicePersistenceFacade serviceFacade;
    @EJB
    private ServiceProviderPersistenceFacade providerFacade;

    //create
    private Notification notification;
    private List<SelectItem> eventList;
    private List<SelectItem> channellList;
    private BillingEvent event;
    private NotificationChannell notificationChannell;
    private ServiceProvider provider;
    private List<SelectItem> langList;
    private String notificationText;
    private String subject;
    private List<SelectItem> lifeCycleTypes;
    private List<SelectItem> subscriberTypes;
    private Boolean isApplyToAllLifecycleTypes;
    private Boolean isApplyToAllSubscriberTypes;
    private Long providerID;
    private List<SelectItem> providerSelectList;
    private LazyDataModel<Service> serviceList;
    private Service selectedService;
    private List<ServiceProvider> providers;

    //index
    private List<Notification> notificationList;
    private Notification selected;
    private List<Notification> filteredNotificationList;

    //view
    private Long notificationID;
    private Language lang;

    public void setNotificationText(String text) {
        this.notificationText = text;
    }

    public String getNotificationText() {
        return notificationText;
    }

    public Long getProviderID() {
        return providerID;
    }

    public void setProviderID(Long providerID) {
        this.providerID = providerID;
    }

    public List<ServiceProvider> getProviders() {
        if (providers == null) {
            providers = providerFacade.findAll();
        }
        return providers;
    }

    public void setProviders(List<ServiceProvider> providers) {
        this.providers = providers;
    }

    public Notification getNotification() {
        if (notification == null) {
            notification = new Notification();
        }
        return notification;
    }

    public void setNotification(Notification notification) {
        this.notification = notification;
    }

    public List<SelectItem> getEventList() {
        eventList = new ArrayList<>();

        for (BillingEvent event : BillingEvent.values()) {
            eventList.add(new SelectItem(event, event.toString()));
        }

        return eventList;
    }

    public void setEventList(List<SelectItem> eventList) {
        this.eventList = eventList;
    }

    public List<SelectItem> getChannellList() {
        channellList = new ArrayList<>();

        for (NotificationChannell channell : NotificationChannell.values()) {
            channellList.add(new SelectItem(channell, channell.getCode()));
        }

        return channellList;
    }

    public void setChannellList(List<SelectItem> channellList) {
        this.channellList = channellList;
    }

    public BillingEvent getEvent() {
        return event;
    }

    public void setEvent(BillingEvent event) {
        this.event = event;
    }

    public NotificationChannell getNotificationChannell() {
        return notificationChannell;
    }

    public void setNotificationChannell(NotificationChannell notificationChannell) {
        this.notificationChannell = notificationChannell;
    }

    public List<SelectItem> getLangList() {
        if (langList == null) {
            langList = new ArrayList<>();

            for (Language lang : Language.values()) {
                langList.add(new SelectItem(lang, lang.getCode()));
            }
        }
        return langList;
    }

    public void setLangList(List<SelectItem> langList) {
        this.langList = langList;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getSubject() {
        return subject;
    }

    public NotificationPersistenceFacade getNotificationFacade() {
        return notificationFacade;
    }

    public void setNotificationFacade(NotificationPersistenceFacade notificationFacade) {
        this.notificationFacade = notificationFacade;
    }

    public List<Notification> getNotificationList() {
        if (notificationList == null) {
            notificationList = notificationFacade.findAll();
        }
        return notificationList;
    }

    public void setNotificationList(List<Notification> notificationList) {
        this.notificationList = notificationList;
    }

    public Notification getSelected() {
        if (selected == null && notificationID != null) {
            selected = notificationFacade.find(notificationID);
        }
        log.debug("getSelected: notificationID=" + getNotificationID() + ", selected=" + selected);

        return selected;
    }

    public void setSelected(Notification selected) {
        this.selected = selected;
    }

    public List<Notification> getFilteredNotificationList() {
        if (filteredNotificationList == null) {
            filteredNotificationList = notificationFacade.findAll();
            notificationList = notificationFacade.findAll();
        }
        return filteredNotificationList;
    }

    public void setFilteredNotificationList(List<Notification> filteredNotificationList) {
        this.filteredNotificationList = filteredNotificationList;
    }

    public Long getNotificationID() {
        return notificationID;
    }

    public void setNotificationID(Long notificationID) {
        this.notificationID = notificationID;
    }

    public Language getLang() {
        return lang;
    }

    public void setLang(Language lang) {
        this.lang = lang;
    }

    public List<SelectItem> getLifeCycleTypes() {
        if (lifeCycleTypes == null) {
            lifeCycleTypes = new ArrayList<>();

            for (SubscriberLifeCycleType type : SubscriberLifeCycleType.values()) {
                lifeCycleTypes.add(new SelectItem(type));
            }
        }
        return lifeCycleTypes;
    }

    public void setLifeCycleTypes(List<SelectItem> lifeCycleTypes) {
        this.lifeCycleTypes = lifeCycleTypes;
    }

    public List<SelectItem> getSubscriberTypes() {
        subscriberTypes = new ArrayList<>();

        for (SubscriberType type : SubscriberType.values()) {
            subscriberTypes.add(new SelectItem(type));
        }

        return subscriberTypes;
    }

    public void setSubscriberTypes(List<SelectItem> subscriberTypes) {
        this.subscriberTypes = subscriberTypes;
    }

    public Boolean getApplyToAllLifecycleTypes() {
        if (selected != null && selected.getLifeCycleType() == null) {
            isApplyToAllLifecycleTypes = true;
        }
        return isApplyToAllLifecycleTypes;
    }

    public void setApplyToAllLifecycleTypes(Boolean isApplyToAllLifecycleTypes) {
        this.isApplyToAllLifecycleTypes = isApplyToAllLifecycleTypes;
    }

    public Boolean getApplyToAllSubscriberTypes() {
        if (selected != null && selected.getSubscriberType() == null) {
            isApplyToAllSubscriberTypes = true;
        }
        return isApplyToAllSubscriberTypes;
    }

    public void setApplyToAllSubscriberTypes(Boolean isApplyToAllSubscriberTypes) {
        this.isApplyToAllSubscriberTypes = isApplyToAllSubscriberTypes;
    }

    public List<SelectItem> getProviderSelectList() {
        if (providerSelectList == null) {
            List<ServiceProvider> provList = providerFacade.findAll();
            providerSelectList = new ArrayList<>();

            for (ServiceProvider prov : provList) {
                providerSelectList.add(new SelectItem(prov.getId(), prov.getName()));
            }
        }
        return providerSelectList;
    }

    public void setProviderSelectList(List<SelectItem> providerSelectList) {
        this.providerSelectList = providerSelectList;
    }

    public LazyDataModel<Service> getServiceList() {
        if (serviceList == null) {
            serviceList = new LazyTableModel<>(serviceFacade);
        }
        return serviceList;
    }

    public void setServiceList(LazyDataModel<Service> serviceList) {
        this.serviceList = serviceList;
    }

    public Service getSelectedService() {
        return selectedService;
    }

    public void setSelectedService(Service selectedService) {
        this.selectedService = selectedService;
    }

    public void onLifeCycleCheckBoxValueChange() {
        if (isApplyToAllLifecycleTypes) {
            getNotification().setLifeCycleType(null);
        }
    }

    public void onSubscriberTypeValueChange() {
        if (isApplyToAllSubscriberTypes) {
            getNotification().setSubscriberType(null);
        }
    }

    public String create() {
        if (notification == null || notificationChannell == null || event == null
                || notification.getNotification() == null || notification.getNotification().isEmpty()
                || (notificationChannell == NotificationChannell.EMAIL && (notification.getSubject() == null || notification.getSubject().isEmpty()))
                || (notification.getSubscriberType() == null && !isApplyToAllSubscriberTypes)
                || (notification.getLifeCycleType() == null && !isApplyToAllLifecycleTypes)) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "All fields are required", "All fields are required"));
            return null;
        } else if ((notification.getServiceList().isEmpty() && notification.getProviderList().isEmpty())
                || (!notification.getServiceList().isEmpty() && !notification.getProviderList().isEmpty())) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Either provider or service should be selected", "Either provider or service should be selected"));
            return null;
        }

        log.debug("Selected service: " + selectedService);

        notification.setEvent(event);
        notification.setChannell(notificationChannell);
        log.debug("Notification: " + notification);
        notificationFacade.save(notification);
        return PATH + "index.xhtml";
    }

    public String show() {
        if (selected == null) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Please select a notification", "Please select a notification"));
            return null;
        }

        return new StringBuilder(PATH)
                .append("view.xhtml?notification=")
                .append(selected.getId())
                .append("&amp;includeViewParams=true&amp;faces-redirect=true").toString();
    }

    public String save() {
        if (selected == null || selected.getChannell() == null || selected.getEvent() == null
                || (selected.getChannell() == NotificationChannell.EMAIL && (selected.getSubject() == null || selected.getSubject().isEmpty()))
                || selected.getNotification() == null || selected.getNotification().isEmpty()
                || (selected.getSubscriberType() == null && !isApplyToAllSubscriberTypes)
                || (selected.getLifeCycleType() == null && !isApplyToAllLifecycleTypes)) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "All fields are required", "All fields are required"));
            return null;
        } else if ((selected.getServiceList().isEmpty() && selected.getProviderList().isEmpty())
                || (!selected.getServiceList().isEmpty() && !selected.getProviderList().isEmpty())) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Either provider or service should be selected", "Either provider or service should be selected"));
            return null;
        }

        log.debug("Selected service: " + selectedService);

        if (isApplyToAllLifecycleTypes) {
            selected.setLifeCycleType(null);
        }

        if (isApplyToAllSubscriberTypes) {
            selected.setSubscriberType(null);
        }

        notificationFacade.update(selected);

        //log.debug("Selected notification: " + selected);
        return PATH + "index.xhtml";
    }

    public boolean check(Notification sub) {
        boolean notf = sub.getNotification().toLowerCase().contains(notificationText.toLowerCase()) || notificationText.isEmpty();
        boolean prov = false;
        log.debug(providerID);

        if (providerID == null) {
            prov = true;
        } else {
            for (ServiceProvider sp : sub.getProviderList()) {
                log.debug(sp.getName() + " " + sp.getId() + " " + providerID);
                if (sp.getId() == providerID) {
                    prov = true;
                    break;
                }
            }
        }
        if ((sub.getEvent() == event || event == null) && (sub.getChannell() == notificationChannell || notificationChannell == null)
                && (sub.getLanguage() == lang || lang == null)
                && ((sub.getSubject() != null && sub.getSubject().toLowerCase().contains(subject.toLowerCase())) || subject.isEmpty())
                && prov && notf) {
            return true;
        }
        return false;
    }

    public void search() {
        log.debug("search: " + notificationList.size());
        filteredNotificationList.clear();
        log.debug("search: " + notificationList.size());
        for (Notification sub : notificationList) {
            //log.debug(sub.getEvent() + " " + sub.getChannell() + " " + sub.getLanguage() + " " + sub.getSubject() + " " + sub.getNotification());
            if (check(sub)) {
                filteredNotificationList.add(sub);
            }
        }
        FacesContext context = FacesContext.getCurrentInstance();
        context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Searching successfully completed", "Searching successfully completed"));
    }

    public void reset() {

        filteredNotificationList = notificationFacade.findAll();
        setEvent(null);
        setNotificationChannell(null);
        setLang(null);
        setSubject("");
        setProviderID(null);
        setNotificationText("");
        FacesContext context = FacesContext.getCurrentInstance();
        context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Resetting successfully completed", "Resetting successfully completed"));

    }
}

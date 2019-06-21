package com.jaravir.tekila.ui.converter;

import com.jaravir.tekila.module.accounting.manager.PaymentPersistenceFacade;
import com.jaravir.tekila.module.campaign.CampaignPersistenceFacade;
import com.jaravir.tekila.module.periodic.JobProperty;
import com.jaravir.tekila.module.periodic.JobPropertyType;
import com.jaravir.tekila.module.service.entity.Service;
import com.jaravir.tekila.module.service.persistence.manager.ServicePersistenceFacade;
import com.jaravir.tekila.module.service.persistence.manager.VASPersistenceFacade;

import javax.ejb.EJB;
import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

/**
 * Created by kmaharov on 15.10.2016.
 */
@ManagedBean
@ApplicationScoped
public class JobPropertyConverter implements Converter {
    @EJB
    ServicePersistenceFacade servicePersistenceFacade;
    @EJB
    VASPersistenceFacade vasPersistenceFacade;
    @EJB
    CampaignPersistenceFacade campaignPersistenceFacade;
    @EJB
    PaymentPersistenceFacade paymentPersistenceFacade;

    private String convertTarget(JobProperty property) {
        return "SUBSCRIPTION_ID: " + property.getValue();
    }

    private String convertServiceId(JobProperty property) {
        return "SERVICE: " +
                servicePersistenceFacade.find(Long.parseLong(property.getValue())).getName();
    }

    private String convertVasId(JobProperty property) {
        return "VAS: " +
                vasPersistenceFacade.find(Long.parseLong(property.getValue())).getName();
    }

    private String convertCampaignId(JobProperty property) {
        return "CAMPAIGN: " +
                campaignPersistenceFacade.find(Long.parseLong(property.getValue())).getName();
    }

    private String convertPaymentId(JobProperty property) {
        return "PAYMENT: " +
                paymentPersistenceFacade.find(Long.parseLong(property.getValue())).getAmount() + " AZN";
    }

    @Override
    public Object getAsObject(FacesContext facesContext, UIComponent uiComponent, String s) {
        return s;
    }

    @Override
    public String getAsString(FacesContext facesContext, UIComponent uiComponent, Object o) {
        JobProperty jobProperty = (JobProperty) o;
        String jobPropertyStr;
        if (jobProperty.getType().equals(JobPropertyType.TARGET)) {
            jobPropertyStr = convertTarget(jobProperty);
        } else if (jobProperty.getType().equals(JobPropertyType.SUBSCRIPTION_SERVICE_ID)) {
            jobPropertyStr = convertServiceId(jobProperty);
        } else if (jobProperty.getType().equals(JobPropertyType.VAS_ID)) {
            jobPropertyStr = convertVasId(jobProperty);
        } else if (jobProperty.getType().equals(JobPropertyType.CAMPAIGN_ID)) {
            jobPropertyStr = convertCampaignId(jobProperty);
        } else if (jobProperty.getType().equals(JobPropertyType.PAYMENT_ID)) {
            jobPropertyStr = convertPaymentId(jobProperty);
        } else {
            jobPropertyStr = jobProperty.getType().toString() + ": " +
                    jobProperty.getValue();
        }
        return jobPropertyStr;
    }
}
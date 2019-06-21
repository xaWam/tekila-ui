package com.jaravir.tekila.ui.converter;

import com.jaravir.tekila.base.model.BillingModelPersistenceFacade;
import com.jaravir.tekila.module.service.model.BillingModel;

import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import java.io.Serializable;

/**
 * Created by shnovruzov on 8/9/2016.
 */
public class BillingModelConverter implements Serializable, Converter {

    @EJB
    private BillingModelPersistenceFacade modelFacade;

    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String submittedValue) {
        if (submittedValue == null || submittedValue.isEmpty()) {
            return null;
        }

        try {
            return modelFacade.find(Long.valueOf(submittedValue));
        } catch (NumberFormatException e) {
            throw new ConverterException(new FacesMessage(String.format("%s is not a valid Model ID", submittedValue)), e);
        }
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object modelValue) {
        if (modelValue == null) {
            return "";
        }

        if (modelValue instanceof BillingModel) {
            return String.valueOf(((BillingModel) modelValue).getId());
        } else {
            throw new ConverterException(new FacesMessage(String.format("%s is not a valid Model", modelValue)));
        }
    }
}

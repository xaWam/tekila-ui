package com.jaravir.tekila.ui.converter;

import com.jaravir.tekila.module.store.midipop.MidipopPersistenceFacade;
import com.jaravir.tekila.module.store.nas.Nas;
import com.jaravir.tekila.provision.broadband.devices.Midipop;

import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import java.io.Serializable;

/**
 * Created by shnovruzov on 7/16/2016.
 */
@ManagedBean
@ViewScoped
public class MidipopConverter implements Serializable, Converter{

    @EJB
    private MidipopPersistenceFacade midipopFacade;

    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String submittedValue) {
        if (submittedValue == null || submittedValue.isEmpty()) {
            return null;
        }

        try {
            return midipopFacade.find(Long.valueOf(submittedValue));
        } catch (NumberFormatException e) {
            throw new ConverterException(new FacesMessage(String.format("%s is not a valid Midipop ID", submittedValue)), e);
        }
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object modelValue) {
        if (modelValue == null) {
            return "";
        }

        if (modelValue instanceof Midipop) {
            return String.valueOf(((Midipop) modelValue).getId());
        } else {
            throw new ConverterException(new FacesMessage(String.format("%s is not a valid Midipop", modelValue)));
        }
    }
}

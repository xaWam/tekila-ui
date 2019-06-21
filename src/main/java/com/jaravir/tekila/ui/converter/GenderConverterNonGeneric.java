package com.jaravir.tekila.ui.converter;

import com.jaravir.tekila.module.subscription.persistence.entity.Gender;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.FacesConverter;
import org.apache.log4j.Logger;

import java.io.Serializable;

@FacesConverter("enmConv")
public class GenderConverterNonGeneric implements javax.faces.convert.Converter, Serializable{

	@Override
	public Object getAsObject(FacesContext context, UIComponent component,
			String value) {		
		return Gender.valueOf(value);		
	}

	@Override
	public String getAsString(FacesContext context, UIComponent component,
			Object value) {
		Gender gender = null;
		try {
			gender = (Gender) value;
		}
		catch (ClassCastException ex) {
			Logger log = Logger.getLogger(GenderConverterNonGeneric.class);
			log.error("Object is not of type GENDER: "+ex.getMessage());
		}
		
		return (gender != null) ? gender.toString() : null;
	}	
}

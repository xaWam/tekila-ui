package com.jaravir.tekila.ui.converter;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import org.apache.log4j.Logger;

import java.io.Serializable;

public abstract class AbstractEnumConverter<T extends Enum<T>> implements javax.faces.convert.Converter, Serializable {
	private Class<T> clazz;
	
	public AbstractEnumConverter(Class<T> cl) {
		this.clazz = cl;
	}
	
	@Override
	public Object getAsObject(FacesContext context, UIComponent component,
			String value) {		
		return Enum.valueOf(this.clazz, value);		
	}

	@SuppressWarnings("unchecked")
	@Override
	public String getAsString(FacesContext context, UIComponent component,
			Object value) {
		T enumObject = null;
		try {
			enumObject = (T) value;
		}
		catch (ClassCastException ex) {
			Logger log = Logger.getLogger(GenderConverterNonGeneric.class);
			log.error("Object is not of type GENDER: "+ex.getMessage());
		}
		
		return (enumObject != null) ? enumObject.toString() : null;
	}	
}

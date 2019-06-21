package com.jaravir.tekila.ui.converter;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import java.io.Serializable;

@FacesConverter(value="dateTimeConverter")
public class DateTimeConverter implements Converter, Serializable {

	@Override
	public Object getAsObject(FacesContext arg0, UIComponent arg1, String arg2) {		
		return DateTimeFormat.forPattern("MM/dd/yyyy hh:mm:ss a").parseDateTime(arg2);
	}

	@Override
	public String getAsString(FacesContext arg0, UIComponent arg1, Object arg2) {
		return DateTimeFormat.forPattern("MM/dd/yyyy hh:mm:ss a").print((DateTime)arg2);
	}

}

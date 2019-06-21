package com.jaravir.tekila.ui.entity;

import javax.faces.convert.FacesConverter;

import com.jaravir.tekila.module.subscription.persistence.entity.SubscriberType;
import com.jaravir.tekila.ui.converter.AbstractEnumConverter;

import java.io.Serializable;

@FacesConverter(forClass=SubscriberType.class)
public class SubscriberTypeConverter extends AbstractEnumConverter<SubscriberType> implements Serializable {
	public SubscriberTypeConverter() {
		super(SubscriberType.class);
	}
}

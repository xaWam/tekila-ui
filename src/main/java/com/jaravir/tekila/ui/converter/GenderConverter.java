package com.jaravir.tekila.ui.converter;

import javax.faces.convert.FacesConverter;

import com.jaravir.tekila.module.subscription.persistence.entity.Gender;

import java.io.Serializable;

@FacesConverter(forClass=Gender.class)
public class GenderConverter extends AbstractEnumConverter<Gender> implements Serializable {
	public GenderConverter() {
		super(Gender.class);
	}
}

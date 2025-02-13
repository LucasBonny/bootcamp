package br.com.gunthercloud.bootcamp.resources.exceptions;

import java.util.ArrayList;
import java.util.List;

public class ValidationError extends StandardError{
	private static final long serialVersionUID = 1L;
	
	private List<FieldMessage> field = new ArrayList<>();

	public List<FieldMessage> getField() {
		return field;
	}
	
	public void addError(String fieldName, String message) {
		field.add(new FieldMessage(fieldName, message));
	}
	
}

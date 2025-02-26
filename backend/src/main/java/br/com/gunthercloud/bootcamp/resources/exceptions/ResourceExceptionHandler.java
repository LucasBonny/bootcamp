package br.com.gunthercloud.bootcamp.resources.exceptions;

import java.time.Instant;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import br.com.gunthercloud.bootcamp.services.exceptions.DatabaseException;
import br.com.gunthercloud.bootcamp.services.exceptions.ResourceNotFoundException;
import jakarta.servlet.http.HttpServletRequest;

@ControllerAdvice
public class ResourceExceptionHandler {

	@ExceptionHandler(ResourceNotFoundException.class)
	public ResponseEntity<StandardError> findById(ResourceNotFoundException e, HttpServletRequest request) {
		StandardError err = new StandardError();
		err.setTimestamp(Instant.now());
		err.setStatus(HttpStatus.NOT_FOUND.value());
		err.setError("Resource Not Found");
		err.setMessage(e.getMessage());
		err.setPath(request.getRequestURI());
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(err);
	}

	@ExceptionHandler(DatabaseException.class)
	public ResponseEntity<StandardError> database(DatabaseException e, HttpServletRequest request) {
		StandardError standard = new StandardError();
		standard.setTimestamp(Instant.now());
		standard.setStatus(HttpStatus.BAD_REQUEST.value());
		standard.setError("Database error");
		standard.setPath(request.getRequestURI());
		standard.setMessage(e.getMessage());
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(standard);
	}
	
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ValidationError> validation(MethodArgumentNotValidException e, HttpServletRequest request) {
		HttpStatus status = HttpStatus.UNPROCESSABLE_ENTITY;
		ValidationError err = new ValidationError();
		err.setTimestamp(Instant.now());
		err.setStatus(status.value());
		err.setError("Validation Exception");
		err.setMessage(e.getLocalizedMessage());
		err.setPath(request.getRequestURI());		
		
		for(FieldError f : e.getBindingResult().getFieldErrors()) {
			err.addError(f.getField(), f.getDefaultMessage());;
		}

		return ResponseEntity.status(status).body(err);
	}
}

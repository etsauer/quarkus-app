package api.pelorus.org.errors;

import java.util.List;
import java.util.stream.Collectors;

import javax.validation.ConstraintViolationException;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;

public class ConstraintViolationExceptionMapper implements ExceptionMapper<ConstraintViolationException> {

    @Override
    public Response toResponse(ConstraintViolationException e) {
        List<ErrorResponse.ErrorMessage> errorMessages = e.getConstraintViolations().stream().map(constraintViolation -> new ErrorResponse.ErrorMessage(constraintViolation.getPropertyPath().toString(), constraintViolation.getMessage())).collect(Collectors.toList());
        return Response.status(Response.Status.NO_CONTENT).entity(new ErrorResponse(errorMessages)).build();
    }
    
}

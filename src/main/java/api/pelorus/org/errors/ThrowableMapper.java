
// package api.pelorus.org.errors;

// import lombok.extern.slf4j.Slf4j;

// import org.jboss.resteasy.reactive.RestResponse;
// import org.jboss.resteasy.reactive.server.ServerExceptionMapper;
// import java.util.ResourceBundle;
// import java.util.UUID;

// @Slf4j
// public class ThrowableMapper {

//     @ServerExceptionMapper
//     public RestResponse<ErrorResponse> toResponse(Throwable e) {
//         String errorId = UUID.randomUUID().toString();
//         log.error("errorId[{}]", errorId, e);
//         String defaultErrorMessage = ResourceBundle.getBundle("ValidationMessages").getString("System.error");
//         ErrorResponse.ErrorMessage errorMessage = new ErrorResponse.ErrorMessage(defaultErrorMessage);
//         ErrorResponse errorResponse = new ErrorResponse(errorId, errorMessage);
//         return RestResponse.status(RestResponse.Status.INTERNAL_SERVER_ERROR, errorResponse);
//     }

// }

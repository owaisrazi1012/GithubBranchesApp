package com.nisum.exception.handling;


import static org.springframework.http.HttpStatus.valueOf;

import com.nisum.auth.domain.dto.ApiResponse;
import com.nisum.auth.domain.dto.ExceptionResponse;
import com.nisum.auth.exception.InvalidUserException;
import com.nisum.exception.custom.BadRequestException;
import com.nisum.exception.custom.BitBucketCustomException;
import com.nisum.exception.custom.ElasticSearchCustomException;
import com.nisum.exception.custom.ExecutionServiceException;
import com.nisum.exception.custom.GeneralException;
import com.nisum.exception.custom.GitPresetServiceException;
import com.nisum.exception.custom.GithubCustomException;
import com.nisum.exception.custom.GitlabCustomException;
import com.nisum.exception.custom.JenkinsCustomException;
import com.nisum.exception.custom.JenkinsPresetServiceException;
import com.nisum.exception.custom.JiraPresetServiceException;
import com.nisum.exception.custom.NotificationPresetServiceException;
import com.nisum.exception.custom.ResourceNotFoundException;
import com.nisum.exception.custom.TestingPresetServiceException;
import com.nisum.exception.custom.UnauthorizedException;
import com.nisum.exception.custom.UserAccessDeniedException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.lang.Nullable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
@Slf4j
public class ControllerAdviser {

    public ResponseEntity<ApiResponse> resolveException(GeneralException exception) {
        String message = exception.getMessage();
        HttpStatus status = exception.getStatus();

        ApiResponse apiResponse = new ApiResponse();

        apiResponse.setSuccess(Boolean.FALSE);
        apiResponse.setMessage(message);

        return new ResponseEntity<>(apiResponse, status);
    }

    @ExceptionHandler(UnauthorizedException.class)
    @ResponseBody
    @ResponseStatus(code = HttpStatus.UNAUTHORIZED)
    public ResponseEntity<ApiResponse> resolveException(UnauthorizedException exception) {

        ApiResponse apiResponse = exception.getApiResponse();

        return new ResponseEntity<>(apiResponse, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(BadRequestException.class)
    @ResponseBody
    public ResponseEntity<ApiResponse> resolveException(BadRequestException exception) {
        ApiResponse apiResponse = exception.getApiResponse();

        return new ResponseEntity<>(apiResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseBody
    public ResponseEntity<ApiResponse> resolveException(ResourceNotFoundException exception) {
        ApiResponse apiResponse = exception.getApiResponse();

        return new ResponseEntity<>(apiResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(UserAccessDeniedException.class)
    @ResponseBody
    public ResponseEntity<ApiResponse> resolveException(UserAccessDeniedException exception) {
        ApiResponse apiResponse = exception.getApiResponse();

        return new ResponseEntity< >(apiResponse, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler({ MethodArgumentNotValidException.class })
    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ExceptionResponse> resolveException(MethodArgumentNotValidException ex) {
        List<FieldError> fieldErrors = ex.getBindingResult().getFieldErrors();
        List<String> messages = new ArrayList<>(fieldErrors.size());
        for (FieldError error : fieldErrors) {
            messages.add(error.getField() + " - " + error.getDefaultMessage());
        }
        return new ResponseEntity<>(new ExceptionResponse(messages, HttpStatus.BAD_REQUEST.getReasonPhrase(),
                HttpStatus.BAD_REQUEST.value()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({ MethodArgumentTypeMismatchException.class })
    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ExceptionResponse> resolveException(MethodArgumentTypeMismatchException ex) {
        String message = "Parameter '" + ex.getParameter().getParameterName() + "' must be '"
                + Objects.requireNonNull(ex.getRequiredType()).getSimpleName() + "'";
        List<String> messages = new ArrayList<>(1);
        messages.add(message);
        return new ResponseEntity<>(new ExceptionResponse(messages, HttpStatus.BAD_REQUEST.getReasonPhrase(),
                HttpStatus.BAD_REQUEST.value()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({ HttpRequestMethodNotSupportedException.class })
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    @ResponseBody
    public ResponseEntity<ExceptionResponse> resolveException(HttpRequestMethodNotSupportedException ex) {
        String message = "Request method '" + ex.getMethod() + "' not supported. List of all supported methods - "
                + ex.getSupportedHttpMethods();
        List<String> messages = new ArrayList<>(1);
        messages.add(message);

        return new ResponseEntity<>(new ExceptionResponse(messages, HttpStatus.METHOD_NOT_ALLOWED.getReasonPhrase(),
                HttpStatus.METHOD_NOT_ALLOWED.value()), HttpStatus.METHOD_NOT_ALLOWED);
    }

    @ExceptionHandler({ HttpMessageNotReadableException.class })
    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ExceptionResponse> resolveException(HttpMessageNotReadableException ex) {
        String message = "Please provide Request Body in valid JSON format";
        List<String> messages = new ArrayList<>(1);
        messages.add(message);
        return new ResponseEntity<>(new ExceptionResponse(messages, HttpStatus.BAD_REQUEST.getReasonPhrase(),
                HttpStatus.BAD_REQUEST.value()), HttpStatus.BAD_REQUEST);
    }
    protected ResponseEntity handleExceptionInternal(Exception ex, @Nullable Object body, HttpHeaders headers,
                                                     HttpStatus status, WebRequest request) {
        if (HttpStatus.INTERNAL_SERVER_ERROR.equals(status)) {
            request.setAttribute("javax.servlet.error.exception", ex, 0);
        }
        return new EntityResponseFailure((ResponseModel) body, headers, status).getResponse();
    }
    @ExceptionHandler(GitPresetServiceException.class)
    public ResponseEntity<Object> handleException(final GitPresetServiceException ex, final WebRequest request) {
        ResponseModel error = ex.getResponseModel();
        return handleExceptionInternal(ex, error, new HttpHeaders(), valueOf(error.getStatusCode()), request);
    }

    @ExceptionHandler(ExecutionServiceException.class)
    public ResponseEntity<Object> handleException(final ExecutionServiceException ex, final WebRequest request) {
        ResponseModel error = ex.getResponseModel();
        return handleExceptionInternal(ex, error, new HttpHeaders(), valueOf(error.getStatusCode()), request);
    }

    @ExceptionHandler(JenkinsPresetServiceException.class)
    public ResponseEntity<Object> handleException(final JenkinsPresetServiceException ex, final WebRequest request) {
        ResponseModel error = ex.getResponseModel();
        return handleExceptionInternal(ex, error, new HttpHeaders(), valueOf(error.getStatusCode()), request);
    }

    @ExceptionHandler(NotificationPresetServiceException.class)
    public ResponseEntity<Object> handleException(final NotificationPresetServiceException ex, final WebRequest request) {
        ResponseModel error = ex.getResponseModel();
        return handleExceptionInternal(ex, error, new HttpHeaders(), valueOf(error.getStatusCode()), request);
    }

    @ExceptionHandler(JiraPresetServiceException.class)
    public ResponseEntity<Object> handleException(final JiraPresetServiceException ex, final WebRequest request) {
        ResponseModel error = ex.getResponseModel();
        return handleExceptionInternal(ex, error, new HttpHeaders(), valueOf(error.getStatusCode()), request);
    }


    @ExceptionHandler(TestingPresetServiceException.class)
    public ResponseEntity<Object> handleException(final TestingPresetServiceException ex, final WebRequest request) {
        ResponseModel error = ex.getResponseModel();
        return handleExceptionInternal(ex, error, new HttpHeaders(), valueOf(error.getStatusCode()), request);
    }

    @ExceptionHandler(BitBucketCustomException.class)
    public ResponseEntity<Object> handleException(final BitBucketCustomException ex, final WebRequest request) {
        ResponseModel error = ex.getResponseModel();
        return handleExceptionInternal(ex, error, new HttpHeaders(), valueOf(error.getStatusCode()), request);
    }

    @ExceptionHandler(GithubCustomException.class)
    public ResponseEntity<Object> handleException(final GithubCustomException ex, final WebRequest request) {
        ResponseModel error = ex.getResponseModel();
        return handleExceptionInternal(ex, error, new HttpHeaders(), valueOf(error.getStatusCode()), request);
    }

    @ExceptionHandler(GitlabCustomException.class)
    public ResponseEntity<Object> handleException(final GitlabCustomException ex, final WebRequest request) {
        ResponseModel error = ex.getResponseModel();
        return handleExceptionInternal(ex, error, new HttpHeaders(), valueOf(error.getStatusCode()), request);
    }

    @ExceptionHandler(ElasticSearchCustomException.class)
    public ResponseEntity<Object> handleException(final ElasticSearchCustomException ex, final WebRequest request) {
        ResponseModel error = ex.getResponseModel();
        return handleExceptionInternal(ex, error, new HttpHeaders(), valueOf(error.getStatusCode()), request);
    }

    @ExceptionHandler(JenkinsCustomException.class)
    public ResponseEntity<Object> handleException(final JenkinsCustomException ex, final WebRequest request) {
        ResponseModel error = ex.getResponseModel();
        return handleExceptionInternal(ex, error, new HttpHeaders(), valueOf(error.getStatusCode()), request);
    }
    @ExceptionHandler(InvalidUserException.class)
    public ResponseEntity<Object> handleException(final InvalidUserException ex, final WebRequest request) {
        ResponseModel error = ex.getResponseModel();
        return handleExceptionInternal(ex, error, new HttpHeaders(), valueOf(error.getStatusCode()), request);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<Object> handleException(final EntityNotFoundException ex, final WebRequest request) {
        return getInternalServerError(ex, request);
    }

    @ExceptionHandler(EmptyResultDataAccessException.class)
    public ResponseEntity<Object> handleException(final EmptyResultDataAccessException ex, final WebRequest request) {
        return getInternalServerError(ex, request);
    }

    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<Object> handleException(final NullPointerException ex, final WebRequest request) {
        return getInternalServerError(ex, request);
    }

    @ExceptionHandler(ArrayIndexOutOfBoundsException.class)
    public ResponseEntity<Object> handleException(final ArrayIndexOutOfBoundsException ex, final WebRequest request) {
        return getInternalServerError(ex, request);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Object> handleException(final IllegalStateException ex, final WebRequest request) {
        return getInternalServerError(ex, request);
    }


    @ExceptionHandler(ClassCastException.class)
    public ResponseEntity<Object> handleException(final ClassCastException ex, final WebRequest request) {
        return getInternalServerError(ex, request);
    }

    @ExceptionHandler(InvocationTargetException.class)
    public ResponseEntity<Object> handleException(final InvocationTargetException ex, final WebRequest request) {
        return getInternalServerError(ex, request);
    }

    @ExceptionHandler(InterruptedException.class)
    public ResponseEntity<Object> handleException(final InterruptedException ex, final WebRequest request) {
        return getInternalServerError(ex, request);
    }

    @ExceptionHandler(UnsupportedOperationException.class)
    public ResponseEntity<Object> handleException(final UnsupportedOperationException ex, final WebRequest request) {
        return getInternalServerError(ex, request);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Object> handleException(final DataIntegrityViolationException ex, final WebRequest request) {
        try {
            String message = ((ConstraintViolationException) ex.getCause()).getSQLException().getMessage();
            String[] splitMessages = message.split(" for key '");
            return getInternalServerError(ex, HttpStatus.NOT_ACCEPTABLE.value(), splitMessages[0] ,request);
        } catch (NullPointerException nullPointerException){
            return getInternalServerError(ex,request);
        }

    }
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Object> handleException(final IllegalArgumentException ex, final WebRequest request) {
        return getInternalServerError(ex, request);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Object> handleException(final RuntimeException ex, final WebRequest request) {
        return getInternalServerError(ex, request);
    }

    private ResponseEntity<Object> getInternalServerError(Exception ex, final WebRequest request) {
        ResponseModel error = ResponseModel.builder()
                .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .message("Something Went Wrong")
                .build();
        log.info("Exception {} " ,ex);
        return handleExceptionInternal(ex, error, new HttpHeaders(), valueOf(error.getStatusCode()), request);
    }

    private ResponseEntity<Object> getInternalServerError(Exception ex, Integer code, String message, final WebRequest request) {
        ResponseModel error = ResponseModel.builder()
                .statusCode(code)
                .message(message)
                .build();
        log.info("Exception {} " ,ex);
        return handleExceptionInternal(ex, error, new HttpHeaders(), valueOf(error.getStatusCode()), request);
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseBody
    public ResponseEntity<ApiResponse> resolveException(AccessDeniedException exception) {
        ApiResponse apiResponse = new ApiResponse(false,"Access is denied", HttpStatus.UNAUTHORIZED);
        return new ResponseEntity< >(apiResponse, HttpStatus.UNAUTHORIZED);
    }

}

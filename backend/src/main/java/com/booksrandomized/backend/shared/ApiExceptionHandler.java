package com.booksrandomized.backend.shared;

import com.booksrandomized.backend.catalog.UpstreamCatalogException;
import jakarta.validation.ConstraintViolationException;
import java.net.URI;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.converter.HttpMessageNotReadableException;

@RestControllerAdvice
public final class ApiExceptionHandler {
    @ExceptionHandler({
        ConstraintViolationException.class,
        MethodArgumentNotValidException.class,
        HttpMessageNotReadableException.class,
        MissingServletRequestParameterException.class,
        MethodArgumentTypeMismatchException.class
    })
    ProblemDetail invalidRequest(Exception exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST, "One or more request parameters are invalid");
        problem.setTitle("Invalid request");
        problem.setType(URI.create("urn:problem:invalid-request"));
        return problem;
    }

    @ExceptionHandler(ApiException.class)
    ProblemDetail apiFailure(ApiException exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(exception.status(), exception.getMessage());
        problem.setTitle(exception.code());
        problem.setType(URI.create("urn:problem:" + exception.code()));
        return problem;
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    ProblemDetail conflict(DataIntegrityViolationException exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT, "The resource already exists");
        problem.setTitle("Conflict");
        problem.setType(URI.create("urn:problem:conflict"));
        return problem;
    }

    @ExceptionHandler(UpstreamCatalogException.class)
    ProblemDetail upstreamFailure(UpstreamCatalogException exception) {
        HttpStatus status = exception.isTimeout() ? HttpStatus.GATEWAY_TIMEOUT : HttpStatus.BAD_GATEWAY;
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, exception.getMessage());
        problem.setTitle(exception.isTimeout() ? "Catalog timeout" : "Catalog unavailable");
        problem.setType(URI.create("urn:problem:catalog-upstream"));
        return problem;
    }
}

package com.github.daggerok.app;

import lombok.extern.log4j.Log4j2;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import static java.util.Collections.singletonMap;
import static javax.ws.rs.core.HttpHeaders.ACCEPT;
import static javax.ws.rs.core.HttpHeaders.CONTENT_TYPE;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;

@Log4j2
@Provider
public class ErrorHandler implements ExceptionMapper<Exception> {

  @Override
  public Response toResponse(final Exception exception) {
    final String message = exception.getLocalizedMessage();
    log.info("Unexpected error: {}", message);
    return Response.status(BAD_REQUEST)
                   .header(ACCEPT, APPLICATION_JSON)
                   .header(CONTENT_TYPE, APPLICATION_JSON)
                   .entity(singletonMap("error", message))
                   .build();
  }
}

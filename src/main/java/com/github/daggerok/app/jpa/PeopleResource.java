package com.github.daggerok.app.jpa;

import lombok.extern.log4j.Log4j2;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Log4j2
@Path("people")
@ApplicationScoped
// @Path("{path: .*?}")
@Produces(APPLICATION_JSON)
public class PeopleResource {

  @Inject
  EntityManager em;

  @Context
  UriInfo uriInfo;

  @POST
  @Path("")
  @Transactional
  @Consumes(APPLICATION_JSON)
  public Response createPerson(Map<String, String> request) {
    log.info("hohoho {}", request);

    String name = request.getOrDefault("name", "");
    Person person = Person.of(null, name);
    em.persist(person);

    return Response.created(uriInfo.getRequestUriBuilder()
                                   .path(PeopleResource.class, "findPerson")
                                   .build(person.getId()))
                   .build();
  }

  @PUT
  @Path("{id}")
  @Transactional
  @Consumes(APPLICATION_JSON)
  public Response updatePerson(@PathParam("id") Long id, Map<String, String> request) {
    log.info("trololo {}: {}", id, request);

    Long givenId = Objects.requireNonNull(id, "id may not be null or non number");
    String name = request.getOrDefault("name", "");
    Person person = em.find(Person.class, givenId);
    Person newPerson = Person.of(person.getId(), name);
    em.merge(newPerson);

    Person updated = em.find(Person.class, givenId);
    return Response.accepted(updated).build();
  }

  @GET
  @Path("{id}")
  public Person findPerson(@PathParam("id") Long id) {
    log.info("nonono {}", id);
    Long givenId = Objects.requireNonNull(id, "id may not be null or non number");
    return em.find(Person.class, givenId);
  }

  @GET
  @Path("")
  public Collection<Person> getAllPeople() {
    log.info("ololo");
    return em.createNamedQuery(Person.FIND_ALL, Person.class)
             .getResultList();
  }
}

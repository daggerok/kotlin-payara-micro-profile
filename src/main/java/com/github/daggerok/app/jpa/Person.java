package com.github.daggerok.app.jpa;

import lombok.*;

import javax.persistence.*;
import java.io.Serializable;

@Data
@Entity
@Table(name = "people")
@Setter(AccessLevel.PACKAGE)
@AllArgsConstructor(staticName = "of")
@NoArgsConstructor(access = AccessLevel.PACKAGE)
@NamedQueries({
    @NamedQuery(
        name = Person.FIND_ALL,
        query = "select p from Person p"
    ),
    @NamedQuery(
        name = Person.FIND_ALL_BY_NAME,
        query = "select p from Person p where p.name = :name"
    ),
})
public class Person implements Serializable {

  public static final String FIND_ALL = "Person.findAll";
  public static final String FIND_ALL_BY_NAME = "Person.findByName";

  @Id
  @Column(nullable = false)
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "people_sequence")
  @SequenceGenerator(name = "people_sequence", sequenceName = "people_sequence", allocationSize = 1)
  private Long id;

  @Column(nullable = false, length = 36)
  private String name;
}

package jpastudy.querydsl.study.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Getter
@Setter
@Entity
public class Hello {
    @Id
    @GeneratedValue
    private Long id;
}
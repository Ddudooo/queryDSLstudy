package jpastudy.querydsl.study.repo;

import jpastudy.querydsl.study.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

import java.util.List;

public interface MemberRepo extends JpaRepository<Member, Long>, MemberRepoCustom,
    QuerydslPredicateExecutor<Member> {

    List<Member> findByUsername(String username);
}
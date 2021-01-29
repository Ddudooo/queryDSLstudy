package jpastudy.querydsl.study.repo;

import jpastudy.querydsl.study.dto.MemberSearchCondition;
import jpastudy.querydsl.study.dto.MemberTeamDto;
import jpastudy.querydsl.study.entity.Member;
import jpastudy.querydsl.study.entity.Team;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import javax.persistence.EntityManager;

import static jpastudy.querydsl.study.entity.QMember.member;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class MemberRepoTest {


    @Autowired
    EntityManager em;

    @Autowired
    MemberRepo memberRepo;

    @Test
    public void basicTest() {
        Member member = new Member("member1", 10);
        memberRepo.save(member);

        Member findMember = memberRepo.findById(member.getId()).get();
        assertThat(findMember).isEqualTo(member);

        List<Member> result1 = memberRepo.findAll();
        assertThat(result1).containsExactly(member);

        List<Member> result2 = memberRepo.findByUsername("member1");
        assertThat(result2).containsExactly(member);
    }

    @Test
    public void searchTest() {
        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");
        em.persist(teamA);
        em.persist(teamB);
        Member member1 = new Member("member1", 10, teamA);
        Member member2 = new Member("member2", 20, teamA);
        Member member3 = new Member("member3", 30, teamB);
        Member member4 = new Member("member4", 40, teamB);
        em.persist(member1);
        em.persist(member2);
        em.persist(member3);
        em.persist(member4);

        MemberSearchCondition condition = new MemberSearchCondition();
        condition.setAgeGoe(35);
        condition.setAgeLoe(40);
        condition.setTeamName("teamB");

        List<MemberTeamDto> result = memberRepo.search(condition);

        assertThat(result).extracting("username").containsExactly("member4");
    }

    @Test
    public void searchPageSimpleTest() {
        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");
        em.persist(teamA);
        em.persist(teamB);
        Member member1 = new Member("member1", 10, teamA);
        Member member2 = new Member("member2", 20, teamA);
        Member member3 = new Member("member3", 30, teamB);
        Member member4 = new Member("member4", 40, teamB);
        em.persist(member1);
        em.persist(member2);
        em.persist(member3);
        em.persist(member4);

        MemberSearchCondition condition = new MemberSearchCondition();

        PageRequest pageRequest = PageRequest.of(0, 3);

        Page<MemberTeamDto> result = memberRepo.searchPageSimple(condition, pageRequest);

        assertThat(result.getSize()).isEqualTo(3);
        assertThat(result.getContent()).extracting("username")
            .containsExactly("member1", "member2", "member3");
    }

    @Test
    public void querydslPredicateExecutorTest() {
        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");
        em.persist(teamA);
        em.persist(teamB);
        Member member1 = new Member("member1", 10, teamA);
        Member member2 = new Member("member2", 20, teamA);
        Member member3 = new Member("member3", 30, teamB);
        Member member4 = new Member("member4", 40, teamB);
        em.persist(member1);
        em.persist(member2);
        em.persist(member3);
        em.persist(member4);

        Iterable<Member> result = memberRepo
            .findAll(member.age.between(10, 40).and(member.username.eq("member1")));

        for (Member member : result) {
            System.out.println("member = " + member);
        }
    }
}
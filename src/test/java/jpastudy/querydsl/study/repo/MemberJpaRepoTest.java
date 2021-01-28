package jpastudy.querydsl.study.repo;

import jpastudy.querydsl.study.dto.MemberSearchCondition;
import jpastudy.querydsl.study.dto.MemberTeamDto;
import jpastudy.querydsl.study.entity.Member;
import jpastudy.querydsl.study.entity.Team;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import javax.persistence.EntityManager;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class MemberJpaRepoTest {

    @Autowired
    EntityManager em;

    @Autowired
    MemberJpaRepo memberJpaRepo;

    @Test
    public void basicTest() {
        Member member = new Member("member1", 10);
        memberJpaRepo.save(member);

        Member findMember = memberJpaRepo.findById(member.getId()).get();
        assertThat(findMember).isEqualTo(member);

        List<Member> result1 = memberJpaRepo.findAll();
        assertThat(result1).containsExactly(member);

        List<Member> result2 = memberJpaRepo.findByUsername("member1");
        assertThat(result2).containsExactly(member);
    }

    @Test
    public void basicQueryDSLTest() {
        Member member = new Member("member1", 10);
        memberJpaRepo.save(member);

        Member findMember = memberJpaRepo.findById(member.getId()).get();
        assertThat(findMember).isEqualTo(member);

        List<Member> result1 = memberJpaRepo.findAllQueryDsl();
        assertThat(result1).containsExactly(member);

        List<Member> result2 = memberJpaRepo.findByUsernameQueryDsl("member1");
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

        List<MemberTeamDto> result = memberJpaRepo.searchByBuilder(condition);

        assertThat(result).extracting("username").containsExactly("member4");
    }

    @Test
    public void searchTestByBooleanExpression() {
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

        List<MemberTeamDto> result = memberJpaRepo.search(condition);

        assertThat(result).extracting("username").containsExactly("member4");
    }
}
package jpastudy.querydsl.study;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jpastudy.querydsl.study.dto.MemberDto;
import jpastudy.querydsl.study.dto.QMemberDto;
import jpastudy.querydsl.study.dto.UserDto;
import jpastudy.querydsl.study.entity.Member;
import jpastudy.querydsl.study.entity.QMember;
import jpastudy.querydsl.study.entity.Team;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceUnit;

import static jpastudy.querydsl.study.entity.QMember.member;
import static jpastudy.querydsl.study.entity.QTeam.team;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
public class QuerydslBasicTest {

    @PersistenceContext
    EntityManager em;

    @PersistenceUnit
    EntityManagerFactory emf;

    JPAQueryFactory queryFactory;

    @BeforeEach
    public void before() {
        queryFactory = new JPAQueryFactory(em);
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
    }

    @Test
    public void startJPQL() {
//member1을 찾아라.
        String qlString =
            "select m from Member m " +
                "where m.username = :username";
        Member findMember = em.createQuery(qlString, Member.class)
            .setParameter("username", "member1")
            .getSingleResult();
        assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    @Test
    public void startQuerydsl() {

        Member findMember = queryFactory
            .select(member)
            .from(member)
            .where(member.username.eq("member1"))//파라미터 바인딩 처리
            .fetchOne();
        assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    @Test
    public void search() {
        Member findMember
            = queryFactory.selectFrom(member)
            .where(member.username.eq("member1")
                .and(member.age.eq(10)))
            .fetchOne();

        assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    @Test
    public void searchAndParam() {
        Member findMember
            = queryFactory.selectFrom(member)
            .where(
                member.username.eq("member1"),
                member.age.eq(10), null
            )
            .fetchOne();

        assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    @Test
    public void resultFetch() {
        /*List<Member> fetch = queryFactory.selectFrom(member)
            .fetch();

        Member fetchOne = queryFactory.selectFrom(member)
            .fetchOne();

        Member fetchFirst = queryFactory.selectFrom(member)
            .fetchFirst();*/

        /*QueryResults<Member> results = queryFactory.selectFrom(member)
            .fetchResults();

        results.getTotal();
        List<Member> content = results.getResults();
        results.getOffset();
        results.getLimit();*/

        long total = queryFactory.selectFrom(member)
            .fetchCount();
    }

    @Test
    public void sort() {
        em.persist(new Member(null, 100));
        em.persist(new Member("member5", 100));
        em.persist(new Member("member6", 100));

        List<Member> result = queryFactory.selectFrom(member).where(member.age.eq(100))
            .orderBy(member.age.desc(), member.username.asc().nullsLast())
            .fetch();

        Member member5 = result.get(0);
        Member member6 = result.get(1);
        Member memberNull = result.get(2);

        assertThat(member5.getUsername()).isEqualTo("member5");
        assertThat(member6.getUsername()).isEqualTo("member6");
        assertThat(memberNull.getUsername()).isNull();
    }

    @Test
    public void paging1() {
        List<Member> result = queryFactory.selectFrom(member)
            .orderBy(member.username.desc())
            .offset(1)
            .limit(2)
            .fetch();

        assertThat(result.size()).isEqualTo(2);
    }

    @Test
    public void paging2() {
        QueryResults<Member> results = queryFactory.selectFrom(member)
            .orderBy(member.username.desc())
            .offset(1)
            .limit(2)
            .fetchResults();

        assertThat(results.getTotal()).isEqualTo(4);
        assertThat(results.getLimit()).isEqualTo(2);
        assertThat(results.getOffset()).isEqualTo(1);
        assertThat(results.getResults().size()).isEqualTo(2);
    }

    @Test
    public void aggregation() {
        List<Tuple> result = queryFactory.select(member.count(),
            member.age.sum(),
            member.age.avg(),
            member.age.max(),
            member.age.min())
            .from(member)
            .fetch();

        Tuple tuple = result.get(0);

        assertThat(tuple.get(member.count())).isEqualTo(4);
        assertThat(tuple.get(member.age.sum())).isEqualTo(100);
        assertThat(tuple.get(member.age.avg())).isEqualTo(25);
        assertThat(tuple.get(member.age.max())).isEqualTo(40);
        assertThat(tuple.get(member.age.min())).isEqualTo(10);
    }

    @Test
    public void groupBy() {
        List<Tuple> result = queryFactory
            .select(team.name, member.age.avg())
            .from(member)
            .join(member.team, team)
            .groupBy(team.name)
            .fetch();

        Tuple teamA = result.get(0);
        Tuple teamB = result.get(1);

        assertThat(teamA.get(team.name)).isEqualTo("teamA");
        assertThat(teamB.get(member.age.avg())).isEqualTo(35);

        assertThat(teamB.get(team.name)).isEqualTo("teamB");
        assertThat(teamB.get(member.age.avg())).isEqualTo(35);
    }

    @Test
    public void join() {
        List<Member> result = queryFactory.selectFrom(member)
            .join(member.team, team)
            .where(team.name.eq("teamA"))
            .fetch();

        assertThat(result)
            .extracting("username")
            .containsExactly("member1", "member2");
    }

    @Test
    public void theta_join() {
        em.persist(new Member("teamA"));
        em.persist(new Member("teamB"));
        em.persist(new Member("teamC"));
        em.flush();
        em.clear();

        List<Member> result = queryFactory.select(member)
            .from(member, team)
            .where(member.username.eq(team.name))
            .fetch();

        assertThat(result)
            .extracting("username")
            .containsExactly("teamA", "teamB");
    }

    @Test
    public void join_on_filtering() {
        List<Tuple> result = queryFactory.select(member, team)
            .from(member)
            .leftJoin(member.team, team).on(team.name.eq("teamA"))
            .fetch();

        for (Tuple tuple : result) {
            System.out.println("tuple = " + tuple);
        }
    }

    @Test
    public void join_on_no_relation() {
        em.persist(new Member("teamA"));
        em.persist(new Member("teamB"));
        em.persist(new Member("teamC"));
        em.flush();
        em.clear();

        List<Tuple> result
            = queryFactory
            .select(member, team)
            .from(member)
            .leftJoin(team).on(member.username.eq(team.name))
            .fetch();

        for (Tuple tuple : result) {
            System.out.println("tuple = " + tuple);
        }
    }

    @Test
    public void fetchJoin() {
        Member result = queryFactory.select(member)
            .from(member)
            .join(member.team, team)
            .fetchJoin()
            .where(member.username.eq("member1"))
            .fetchOne();

        boolean loaded = emf.getPersistenceUnitUtil().isLoaded(result.getTeam());

        assertThat(loaded).as("페치 조인").isTrue();
    }

    @Test
    public void subQuery() {
        QMember sub = new QMember("sub");
        List<Member> result = queryFactory.selectFrom(member)
            .where(member.age.eq(
                JPAExpressions
                    .select(sub.age.max())
                    .from(sub)
            ))
            .fetch();

        assertThat(result).extracting("age")
            .containsExactly(40);
    }

    @Test
    public void subQueryGoe() {
        QMember sub = new QMember("sub");
        List<Member> result = queryFactory.selectFrom(member)
            .where(member.age.goe(
                JPAExpressions
                    .select(sub.age.avg())
                    .from(sub)
            ))
            .fetch();

        assertThat(result).extracting("age")
            .containsExactly(30, 40);
    }

    @Test
    public void subQueryIn() {
        QMember sub = new QMember("sub");
        List<Member> result = queryFactory.selectFrom(member)
            .where(member.age.in(
                JPAExpressions
                    .select(sub.age)
                    .from(sub)
                    .where(sub.age.gt(10))
            ))
            .fetch();

        assertThat(result).extracting("age")
            .containsExactly(20, 30, 40);
    }

    @Test
    public void selectSubQuery() {
        QMember sub = new QMember("sub");
        List<Tuple> result
            = queryFactory
            .select(
                member.username,
                JPAExpressions
                    .select(sub.age.avg())
                    .from(sub)
            ).from(member)
            .fetch();
        for (Tuple tuple : result) {
            System.out.println("Tuple = " + tuple);
        }
    }

    @Test
    public void basicCase() {
        List<String> result = queryFactory.select(member.age
            .when(10).then("열살")
            .when(20).then("스무살")
            .otherwise("기타")
        ).from(member).fetch();

        for (String s : result) {
            System.out.println("s = " + s);
        }
    }

    @Test
    public void complexCase() {
        List<String> result = queryFactory.select(
            new CaseBuilder()
                .when(member.age.between(0, 20)).then("0~20살")
                .when(member.age.between(21, 30)).then("21~30살")
                .otherwise("기타")
        ).from(member).fetch();

        for (String s : result) {
            System.out.println("s = " + s);
        }
    }

    @Test
    public void constant() {
        List<Tuple> result
            = queryFactory
            .select(
                member.username,
                Expressions.constant("A")
            )
            .from(member)
            .fetch();
        for (Tuple tuple : result) {
            System.out.println("tuple = " + tuple);
        }
    }

    @Test
    public void concat() {
        List<String> result
            = queryFactory
            .select(
                member.username.concat("_").concat(member.age.stringValue())
            )
            .from(member)
            .where(member.username.eq("member1"))
            .fetch();
        for (String s : result) {
            System.out.println("s = " + s);
        }
    }

    @Test
    public void simpleProjection() {
        List<String> result = queryFactory.select(
            member.username
        ).from(member)
            .fetch();

        for (String s : result) {
            System.out.println("s = " + s);
        }
    }

    @Test
    public void tupleProjection() {
        List<Tuple> result = queryFactory.select(member.username, member.age)
            .from(member)
            .fetch();

        for (Tuple tuple : result) {
            String username = tuple.get(member.username);
            Integer age = tuple.get(member.age);
            System.out.println("username = " + username);
            System.out.println("age = " + age);
        }
    }

    @Test
    public void findDtoByJPQL() {
        List<MemberDto> result = em.createQuery(
            "select new jpastudy.querydsl.study.dto.MemberDto(m.username, m.age) from Member m",
            MemberDto.class)
            .getResultList();

        for (MemberDto dto : result) {
            System.out.println("member dto = " + dto);
        }
    }

    @Test
    public void findDtoBySetter() {
        List<MemberDto> result = queryFactory.select(
            Projections.bean(
                MemberDto.class,
                member.username,
                member.age
            )
        ).from(member)
            .fetch();

        for (MemberDto dto : result) {
            System.out.println("member dto = " + dto);
        }
    }

    @Test
    public void findDtoByConstructor() {
        List<MemberDto> result = queryFactory.select(
            Projections.constructor(
                MemberDto.class,
                member.username,
                member.age
            )
        ).from(member)
            .fetch();

        for (MemberDto dto : result) {
            System.out.println("member dto = " + dto);
        }
    }

    @Test
    public void findUserDto() {
        /*List<UserDto> result = queryFactory.select(
            Projections.fields(
                UserDto.class,
                member.username.as("name"),
                member.age
            )
        ).from(member)
            .fetch();*/
        QMember sub = new QMember("sub");

        List<UserDto> result = queryFactory.select(
            Projections.fields(
                UserDto.class,
                member.username.as("name"),

                ExpressionUtils.as(
                    JPAExpressions
                        .select(sub.age.max())
                        .from(sub), "age"
                )
            )
        ).from(member)
            .fetch();

        for (UserDto dto : result) {
            System.out.println("member dto = " + dto);
        }
    }

    @Test
    public void findUserDtoByConstructor() {
        List<UserDto> result = queryFactory.select(
            Projections.constructor(
                UserDto.class,
                member.username,
                member.age
            )
        ).from(member)
            .fetch();

        for (UserDto dto : result) {
            System.out.println("member dto = " + dto);
        }
    }

    @Test
    public void findDtoByQueryProjection() {
        List<MemberDto> result = queryFactory
            .select(new QMemberDto(member.username, member.age))
            .from(member)
            .fetch();

        for (MemberDto dto : result) {
            System.out.println("member dto = " + dto);
        }
    }

    @Test
    public void dynamicQueryByBooleanBuilder() {
        String usernameParam = "member1";
        Integer ageParam = 10;

        List<Member> result = searchMember1(usernameParam, ageParam);
        assertThat(result.size()).isEqualTo(1);
    }

    private List<Member> searchMember1(String usernameCond, Integer ageCond) {
        BooleanBuilder builder = new BooleanBuilder();
        if (usernameCond != null) {
            builder.and(member.username.eq(usernameCond));
        }

        if (ageCond != null) {
            builder.and(member.age.eq(ageCond));
        }
        return queryFactory
            .selectFrom(member)
            .where(builder)
            .fetch();
    }

    @Test
    public void dynamicQueryByWhereParam() {
        String usernameParam = "member1";
        Integer ageParam = 10;

        List<Member> result = searchMember2(usernameParam, ageParam);
        assertThat(result.size()).isEqualTo(1);
    }

    private List<Member> searchMember2(String usernameCond, Integer ageCond) {
        return queryFactory
            .selectFrom(member)
            //.where(usernameEq(usernameCond), ageEq(ageCond))
            .where(allEq(usernameCond, ageCond))
            .fetch();
    }

    private BooleanExpression ageEq(Integer ageCond) {
        return ageCond != null ? member.age.eq(ageCond) : null;
    }

    private BooleanExpression usernameEq(String usernameCond) {
        return usernameCond != null ? member.username.eq(usernameCond) : null;
    }

    private BooleanExpression allEq(String usernameCond, Integer ageCond) {
        return usernameEq(usernameCond).and(ageEq(ageCond));
    }

    @Test
    public void bulkUpdate() {
        long count = queryFactory
            .update(member)
            .set(member.username, "비회원")
            .where(member.age.lt(28))
            .execute();

        System.out.println("update count " + count);

        //영속성 컨텍스트 와 db 불일치 로 인한 초기화
        em.flush();
        em.clear();

        List<Member> result =
            queryFactory
                .selectFrom(member)
                .fetch();

        for (Member member : result) {
            System.out.println("member = " + member);
        }
    }

    @Test
    public void bulkAdd() {
        queryFactory.update(member)
            .set(member.age, member.age.add(1))
            .execute();
    }

    @Test
    public void bulkDelete() {
        queryFactory.delete(member)
            .where(member.age.gt(18))
            .execute();
    }

    @Test
    public void sqlFunction() {
        List<String> result = queryFactory.select(
            Expressions.stringTemplate(
                "function('replace',{0}, {1}, {2})",
                member.username,
                "member",
                "M"
            ))
            .from(member)
            .fetch();
        for (String s : result) {
            System.out.println("s = " + s);
        }
    }

    @Test
    public void sqlFunction2() {
        List<String> result = queryFactory
            .select(
                member.username
            )
            .from(member)
            /*.where(member.username.eq(
                Expressions.stringTemplate("function('lower', {0})",
                    member.username)))*/

            .where(member.username.eq(member.username.lower()))
            .fetch();

        for (String s : result) {
            System.out.println("s = " + s);
        }
    }
}
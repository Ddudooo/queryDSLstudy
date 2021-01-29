package jpastudy.querydsl.study.repo;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jpastudy.querydsl.study.dto.MemberSearchCondition;
import jpastudy.querydsl.study.dto.MemberTeamDto;
import jpastudy.querydsl.study.dto.QMemberTeamDto;
import org.springframework.stereotype.Repository;

import java.util.List;
import javax.persistence.EntityManager;

import static jpastudy.querydsl.study.entity.QMember.member;
import static jpastudy.querydsl.study.entity.QTeam.team;
import static org.springframework.util.StringUtils.hasText;

@Repository
public class MemberQueryRepo {

    private final JPAQueryFactory queryFactory;

    public MemberQueryRepo(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }
    
    public List<MemberTeamDto> search(MemberSearchCondition condition) {
        return queryFactory
            .select(new QMemberTeamDto(
                member.id.as("memberId"),
                member.username,
                member.age,
                team.id.as("teamId"),
                team.name.as("teamName")
            ))
            .from(member)
            .leftJoin(member.team, team)
            .where(usernameEq(condition.getUsername()),
                teamNameEq(condition.getTeamName()),
                ageGoe(condition.getAgeGoe()),
                ageLoe(condition.getAgeLoe())
            )
            .fetch();
    }

    private BooleanExpression ageLoe(Integer ageLoe) {
        return ageLoe != null ? member.age.loe(ageLoe) : null;
    }

    private BooleanExpression ageGoe(Integer ageGoe) {
        return ageGoe != null ? member.age.goe(ageGoe) : null;
    }

    private BooleanExpression teamNameEq(String teamName) {
        return hasText(teamName) ? team.name.eq(teamName) : null;
    }

    private BooleanExpression usernameEq(String username) {
        return hasText(username) ? member.username.eq(username) : null;
    }
}
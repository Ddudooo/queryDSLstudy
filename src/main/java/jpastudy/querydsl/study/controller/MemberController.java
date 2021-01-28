package jpastudy.querydsl.study.controller;

import jpastudy.querydsl.study.dto.MemberSearchCondition;
import jpastudy.querydsl.study.dto.MemberTeamDto;
import jpastudy.querydsl.study.repo.MemberJpaRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class MemberController {

    private final MemberJpaRepo memberJpaRepo;

    @GetMapping("/v1/members")
    public List<MemberTeamDto> searchMemberV1(MemberSearchCondition condition) {
        return memberJpaRepo.search(condition);
    }
}
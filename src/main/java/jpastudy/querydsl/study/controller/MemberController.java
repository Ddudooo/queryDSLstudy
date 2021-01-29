package jpastudy.querydsl.study.controller;

import jpastudy.querydsl.study.dto.MemberSearchCondition;
import jpastudy.querydsl.study.dto.MemberTeamDto;
import jpastudy.querydsl.study.repo.MemberJpaRepo;
import jpastudy.querydsl.study.repo.MemberRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class MemberController {

    private final MemberJpaRepo memberJpaRepo;

    private final MemberRepo memberRepo;

    @GetMapping("/v1/members")
    public List<MemberTeamDto> searchMemberV1(MemberSearchCondition condition) {
        return memberJpaRepo.search(condition);
    }

    @GetMapping("/v2/members")
    public Page<MemberTeamDto> searchMemberV2(MemberSearchCondition condition, Pageable pageable) {
        return memberRepo.searchPageSimple(condition, pageable);
    }

    @GetMapping("/v3/members")
    public Page<MemberTeamDto> searchMemberV3(MemberSearchCondition condition, Pageable pageable) {
        return memberRepo.searchPageComplex(condition, pageable);
    }
}
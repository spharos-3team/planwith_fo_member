package com.planwith.planwith_fo_member.application.port.out;

import java.util.Optional;
import java.util.UUID;

import com.planwith.planwith_fo_member.domain.member.LoginType;
import com.planwith.planwith_fo_member.domain.member.Member;
import com.planwith.planwith_fo_member.domain.member.MemberProfile;

public interface MemberRepositoryPort {

	boolean existsByEmail(String email);

	boolean existsByNickname(String nickname);

	boolean existsByLoginTypeAndSocialId(LoginType loginType, String socialId);

	Member saveMember(Member member, MemberProfile profile);

	Optional<Member> findByUuid(UUID memberUuid);
}

package com.planwith.planwith_fo_member.application.port.out;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import com.planwith.planwith_fo_member.domain.member.LoginType;
import com.planwith.planwith_fo_member.domain.member.Member;
import com.planwith.planwith_fo_member.domain.member.MemberProfile;
import com.planwith.planwith_fo_member.domain.member.MemberStatus;

public interface MemberRepositoryPort {

	boolean existsByEmail(String email);

	boolean existsByNickname(String nickname);

	boolean existsByNicknameExcludingMember(String nickname, UUID memberUuid);

	boolean existsByLoginTypeAndSocialId(LoginType loginType, String socialId);

	Member saveMember(Member member, MemberProfile profile);

	Optional<Member> findByUuid(UUID memberUuid);

	Optional<Member> findByEmail(String email);

	Optional<Member> findByLoginTypeAndSocialId(LoginType loginType, String socialId);

	Optional<Member> findByPhoneNumber(String phoneNumber);

	Optional<MemberProfile> findProfileByMemberUuid(UUID memberUuid);

	void updateLastLoginAt(UUID memberUuid, Instant lastLoginAt);

	void updatePassword(UUID memberUuid, String passwordHash);

	void updatePhoneNumber(UUID memberUuid, String phoneNumber);

	void updateProfile(UUID memberUuid, String nickname, String profileImage, String profileIntro);

	void updateProfileImage(UUID memberUuid, String profileImageUrl);

	void softDelete(UUID memberUuid, MemberStatus status, Instant deletedAt);
}

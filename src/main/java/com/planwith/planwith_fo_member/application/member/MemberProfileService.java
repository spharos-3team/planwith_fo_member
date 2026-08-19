package com.planwith.planwith_fo_member.application.member;

import java.io.IOException;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import javax.imageio.ImageIO;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.planwith.planwith_fo_member.application.auth.PhoneVerificationService;
import com.planwith.planwith_fo_member.application.exception.BusinessException;
import com.planwith.planwith_fo_member.application.exception.ErrorCode;
import com.planwith.planwith_fo_member.application.port.in.ChangePasswordUseCase;
import com.planwith.planwith_fo_member.application.port.in.GetMyMemberUseCase;
import com.planwith.planwith_fo_member.application.port.in.GetMyProfileUseCase;
import com.planwith.planwith_fo_member.application.port.in.GetPublicProfileUseCase;
import com.planwith.planwith_fo_member.application.port.in.ListMembersUseCase;
import com.planwith.planwith_fo_member.application.port.in.MemberAgreementUseCase;
import com.planwith.planwith_fo_member.application.port.in.UpdateMyPageUseCase;
import com.planwith.planwith_fo_member.application.port.in.UploadProfileImageUseCase;
import com.planwith.planwith_fo_member.application.port.out.FollowRepositoryPort;
import com.planwith.planwith_fo_member.application.port.out.MemberRepositoryPort;
import com.planwith.planwith_fo_member.application.port.out.PhoneVerificationStorePort;
import com.planwith.planwith_fo_member.domain.member.Member;
import com.planwith.planwith_fo_member.domain.member.MemberProfile;
import com.planwith.planwith_fo_member.domain.member.MemberStatus;

@Service
@Transactional
public class MemberProfileService implements
		GetMyProfileUseCase,
		UpdateMyPageUseCase,
		UploadProfileImageUseCase,
		GetPublicProfileUseCase,
		ListMembersUseCase {

	private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
			"image/jpeg",
			"image/jpg",
			"image/png",
			"image/webp"
	);
	private static final long MAX_BYTES = 2 * 1024 * 1024;
	private static final int REQUIRED_SIZE = 400;
	private static final int MAX_PAGE_SIZE = 50;

	private final MemberRepositoryPort memberRepository;
	private final FollowRepositoryPort followRepository;
	private final PhoneVerificationStorePort phoneVerificationStore;
	private final PhoneVerificationService phoneVerificationService;
	private final MemberAgreementUseCase memberAgreementUseCase;
	private final ChangePasswordUseCase changePasswordUseCase;
	private final GetMyMemberUseCase getMyMemberUseCase;

	public MemberProfileService(
			MemberRepositoryPort memberRepository,
			FollowRepositoryPort followRepository,
			PhoneVerificationStorePort phoneVerificationStore,
			PhoneVerificationService phoneVerificationService,
			MemberAgreementUseCase memberAgreementUseCase,
			ChangePasswordUseCase changePasswordUseCase,
			GetMyMemberUseCase getMyMemberUseCase
	) {
		this.memberRepository = memberRepository;
		this.followRepository = followRepository;
		this.phoneVerificationStore = phoneVerificationStore;
		this.phoneVerificationService = phoneVerificationService;
		this.memberAgreementUseCase = memberAgreementUseCase;
		this.changePasswordUseCase = changePasswordUseCase;
		this.getMyMemberUseCase = getMyMemberUseCase;
	}

	@Override
	@Transactional(readOnly = true)
	public ProfileResult get(UUID memberUuid) {
		requireActiveMember(memberUuid);
		return toResult(requireProfile(memberUuid));
	}

	@Override
	public UpdateMyPageResult update(UUID memberUuid, UpdateMyPageCommand command) {
		requireActiveMember(memberUuid);
		MemberProfile current = requireProfile(memberUuid);

		boolean hasPhoneChange = StringUtils.hasText(command.phoneNumber());
		boolean hasProfileChange = command.nickname() != null
				|| command.profileImage() != null
				|| command.profileIntro() != null;
		boolean hasAgreements = command.agreements() != null && !command.agreements().isEmpty();
		boolean hasPasswordChange = StringUtils.hasText(command.newPassword())
				|| StringUtils.hasText(command.currentPassword());

		if (!hasPhoneChange && !hasProfileChange && !hasAgreements && !hasPasswordChange) {
			throw new BusinessException(ErrorCode.INVALID_REQUEST, "수정할 정보가 없습니다.");
		}

		if (hasPhoneChange) {
			PhoneVerificationStorePort.VerifiedPhone verifiedPhone = phoneVerificationService.requireMatchingVerified(
					command.phoneNumber(),
					command.name()
			);
			memberRepository.updatePhoneIdentity(
					memberUuid,
					verifiedPhone.phoneNumber(),
					verifiedPhone.name()
			);
			phoneVerificationStore.clear(verifiedPhone.phoneNumber());
		}

		if (hasProfileChange) {
			String nextNickname = current.getNickname();
			if (command.nickname() != null) {
				nextNickname = command.nickname().trim();
				if (nextNickname.length() < 2 || nextNickname.length() > 10) {
					throw new BusinessException(ErrorCode.INVALID_REQUEST, "닉네임은 2자 이상 10자 이하여야 합니다.");
				}
				if (!nextNickname.equals(current.getNickname())
						&& memberRepository.existsByNicknameExcludingMember(nextNickname, memberUuid)) {
					throw new BusinessException(ErrorCode.NICKNAME_ALREADY_EXISTS);
				}
			}

			if (command.profileIntro() != null && command.profileIntro().trim().length() > 20) {
				throw new BusinessException(ErrorCode.INVALID_REQUEST, "프로필 소개는 20자 이하여야 합니다.");
			}

			memberRepository.updateProfile(
					memberUuid,
					command.nickname() == null ? null : nextNickname,
					command.profileImage(),
					command.profileIntro() == null ? null : command.profileIntro().trim()
			);
		}

		if (hasAgreements) {
			memberAgreementUseCase.upsertOptional(
					memberUuid,
					command.agreements().stream()
							.map(item -> new MemberAgreementUseCase.AgreementInput(item.termUuid(), item.agreed()))
							.toList()
			);
		}

		if (hasPasswordChange) {
			changePasswordUseCase.change(memberUuid, command.currentPassword(), command.newPassword());
		}

		return new UpdateMyPageResult(
				getMyMemberUseCase.get(memberUuid),
				toResult(requireProfile(memberUuid)),
				memberAgreementUseCase.list(memberUuid)
		);
	}

	@Override
	public ProfileResult upload(UUID memberUuid, MultipartFile file) {
		requireActiveMember(memberUuid);
		requireProfile(memberUuid);
		if (file == null || file.isEmpty()) {
			throw new BusinessException(ErrorCode.INVALID_PROFILE_IMAGE, "이미지 파일이 필요합니다.");
		}
		if (file.getSize() > MAX_BYTES) {
			throw new BusinessException(ErrorCode.INVALID_PROFILE_IMAGE, "이미지 용량은 2MB 이하여야 합니다.");
		}
		String contentType = file.getContentType() == null ? "" : file.getContentType().toLowerCase(Locale.ROOT);
		if (!ALLOWED_CONTENT_TYPES.contains(contentType)) {
			throw new BusinessException(ErrorCode.INVALID_PROFILE_IMAGE, "jpg/jpeg/png/webp만 업로드할 수 있습니다.");
		}

		if (!"image/webp".equals(contentType)) {
			try {
				BufferedImage image = ImageIO.read(file.getInputStream());
				if (image == null) {
					throw new BusinessException(ErrorCode.INVALID_PROFILE_IMAGE);
				}
				if (image.getWidth() != REQUIRED_SIZE || image.getHeight() != REQUIRED_SIZE) {
					throw new BusinessException(
							ErrorCode.INVALID_PROFILE_IMAGE,
							"프로필 이미지는 400x400 이어야 합니다."
					);
				}
			}
			catch (IOException exception) {
				throw new BusinessException(ErrorCode.INVALID_PROFILE_IMAGE);
			}
		}

		String extension = extensionOf(contentType);
		String stubUrl = "stub://profiles/" + UUID.randomUUID() + "." + extension;
		memberRepository.updateProfileImage(memberUuid, stubUrl);
		return toResult(requireProfile(memberUuid));
	}

	@Override
	@Transactional(readOnly = true)
	public PublicProfileResult getPublic(UUID memberUuid, UUID viewerMemberUuid) {
		requireActiveMember(memberUuid);
		MemberProfile profile = requireProfile(memberUuid);
		Boolean isFollowing = null;
		if (viewerMemberUuid != null) {
			isFollowing = !viewerMemberUuid.equals(memberUuid)
					&& followRepository.existsActive(viewerMemberUuid, memberUuid);
		}
		return new PublicProfileResult(
				profile.getMemberUuid(),
				profile.getNickname(),
				profile.getProfileImage(),
				profile.getProfileIntro(),
				profile.getGrade(),
				profile.isProfileBadge(),
				profile.isProfileSpecialBorder(),
				followRepository.countActiveFollowers(memberUuid),
				followRepository.countActiveFollowings(memberUuid),
				isFollowing
		);
	}

	@Override
	@Transactional(readOnly = true)
	public PagedProfiles list(String nickname, UUID viewerMemberUuid, int page, int size) {
		if (page < 0) {
			throw new BusinessException(ErrorCode.INVALID_REQUEST, "page는 0 이상이어야 합니다.");
		}
		if (size < 1 || size > MAX_PAGE_SIZE) {
			throw new BusinessException(ErrorCode.INVALID_REQUEST, "size는 1 이상 " + MAX_PAGE_SIZE + " 이하여야 합니다.");
		}
		String keyword = StringUtils.hasText(nickname) ? nickname.trim() : null;
		if (keyword != null && keyword.length() > 10) {
			throw new BusinessException(ErrorCode.INVALID_REQUEST, "닉네임은 10자 이하여야 합니다.");
		}
		MemberRepositoryPort.PagedProfiles result = memberRepository.findActiveProfiles(
				keyword,
				viewerMemberUuid,
				page,
				size
		);
		return new PagedProfiles(
				result.profiles().stream().map(this::toResult).toList(),
				result.page(),
				result.size(),
				result.totalElements(),
				result.totalPages()
		);
	}

	private void requireActiveMember(UUID memberUuid) {
		Member member = memberRepository.findByUuid(memberUuid)
				.orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
		if (member.getStatus() != MemberStatus.ACTIVE) {
			throw new BusinessException(ErrorCode.MEMBER_NOT_FOUND);
		}
	}

	private MemberProfile requireProfile(UUID memberUuid) {
		return memberRepository.findProfileByMemberUuid(memberUuid)
				.orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
	}

	private ProfileResult toResult(MemberProfile profile) {
		return new ProfileResult(
				profile.getMemberUuid(),
				profile.getNickname(),
				profile.getProfileImage(),
				profile.getProfileIntro(),
				profile.getGrade(),
				profile.isProfileBadge(),
				profile.isProfileSpecialBorder()
		);
	}

	private String extensionOf(String contentType) {
		return switch (contentType) {
			case "image/png" -> "png";
			case "image/webp" -> "webp";
			default -> "jpg";
		};
	}
}

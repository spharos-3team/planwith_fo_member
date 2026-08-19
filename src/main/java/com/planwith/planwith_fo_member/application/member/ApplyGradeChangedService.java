package com.planwith.planwith_fo_member.application.member;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.planwith.planwith_fo_member.application.exception.BusinessException;
import com.planwith.planwith_fo_member.application.exception.ErrorCode;
import com.planwith.planwith_fo_member.application.port.in.ApplyGradeChangedUseCase;
import com.planwith.planwith_fo_member.application.port.out.MemberRepositoryPort;
import com.planwith.planwith_fo_member.application.port.out.ProcessedMemberEventPort;

@Service
@Transactional
public class ApplyGradeChangedService implements ApplyGradeChangedUseCase {

	private static final Logger log = LoggerFactory.getLogger(ApplyGradeChangedService.class);

	private final MemberRepositoryPort memberRepository;
	private final ProcessedMemberEventPort processedMemberEventPort;

	public ApplyGradeChangedService(
			MemberRepositoryPort memberRepository,
			ProcessedMemberEventPort processedMemberEventPort
	) {
		this.memberRepository = memberRepository;
		this.processedMemberEventPort = processedMemberEventPort;
	}

	@Override
	public void apply(GradeChangedCommand command) {
		Objects.requireNonNull(command, "Grade changed command is required.");
		UUID eventUuid = parseUuid(command.eventUuid(), "eventUuid");
		UUID memberUuid = parseUuid(command.memberUuid(), "memberUuid");
		if (!StringUtils.hasText(command.currentGradeCode())) {
			throw new IllegalArgumentException("currentGradeCode is required.");
		}

		if (processedMemberEventPort.existsByEventUuid(eventUuid)) {
			log.warn("ApplyGradeChangedService : apply : 중복 등급 변경 이벤트 무시 - eventUuid={}", eventUuid);
			return;
		}

		if (memberRepository.findProfileByMemberUuid(memberUuid).isEmpty()) {
			log.warn(
					"ApplyGradeChangedService : apply : 프로필이 없어 등급 변경 반영을 생략 - memberUuid={}, eventUuid={}",
					memberUuid,
					eventUuid
			);
			saveProcessed(eventUuid, memberUuid);
			return;
		}

		log.info(
				"ApplyGradeChangedService : apply : 회원 프로필 등급 반영 시작 - memberUuid={}, currentGradeCode={}, profileBadge={}, profileSpecialBorder={}",
				memberUuid,
				command.currentGradeCode(),
				command.profileBadge(),
				command.profileSpecialBorder()
		);
		try {
			memberRepository.updateGradeBenefits(
					memberUuid,
					command.currentGradeCode().trim(),
					command.profileBadge(),
					command.profileSpecialBorder()
			);
		}
		catch (BusinessException exception) {
			if (exception.getErrorCode() != ErrorCode.MEMBER_NOT_FOUND) {
				throw exception;
			}
			log.warn("ApplyGradeChangedService : apply : 회원이 없어 등급 변경 반영을 생략 - memberUuid={}", memberUuid);
			saveProcessed(eventUuid, memberUuid);
			return;
		}
		saveProcessed(eventUuid, memberUuid);
		log.info(
				"ApplyGradeChangedService : apply : 회원 프로필 등급 반영 완료 - memberUuid={}, eventUuid={}, currentGradeCode={}",
				memberUuid,
				eventUuid,
				command.currentGradeCode()
		);
	}

	private void saveProcessed(UUID eventUuid, UUID memberUuid) {
		try {
			processedMemberEventPort.save(eventUuid, memberUuid, Instant.now());
		}
		catch (DataIntegrityViolationException exception) {
			log.warn("ApplyGradeChangedService : apply : 동시 중복 등급 변경 이벤트 무시 - eventUuid={}", eventUuid);
		}
	}

	private UUID parseUuid(String value, String fieldName) {
		if (!StringUtils.hasText(value)) {
			throw new IllegalArgumentException(fieldName + " is required.");
		}
		try {
			return UUID.fromString(value.trim());
		}
		catch (IllegalArgumentException exception) {
			throw new IllegalArgumentException(fieldName + " must be a UUID.");
		}
	}
}

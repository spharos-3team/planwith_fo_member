package com.planwith.planwith_fo_member.application.member;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.planwith.planwith_fo_member.application.port.in.ApplyGradeChangedUseCase;
import com.planwith.planwith_fo_member.application.port.out.MemberRepositoryPort;
import com.planwith.planwith_fo_member.application.port.out.ProcessedMemberEventPort;
import com.planwith.planwith_fo_member.domain.member.MemberProfile;

@ExtendWith(MockitoExtension.class)
class ApplyGradeChangedServiceTest {

	@Mock
	private MemberRepositoryPort memberRepository;

	@Mock
	private ProcessedMemberEventPort processedMemberEventPort;

	@InjectMocks
	private ApplyGradeChangedService applyGradeChangedService;

	@Test
	void updatesGradeAndBenefitsThenMarksProcessed() {
		UUID eventUuid = UUID.randomUUID();
		UUID memberUuid = UUID.randomUUID();
		when(processedMemberEventPort.existsByEventUuid(eventUuid)).thenReturn(false);
		when(memberRepository.findProfileByMemberUuid(memberUuid)).thenReturn(Optional.of(profile(memberUuid)));

		applyGradeChangedService.apply(new ApplyGradeChangedUseCase.GradeChangedCommand(
				eventUuid.toString(),
				memberUuid.toString(),
				"EXPLORER",
				true,
				true
		));

		verify(memberRepository).updateGradeBenefits(memberUuid, "EXPLORER", true, true);
		verify(processedMemberEventPort).save(eq(eventUuid), eq(memberUuid), any(Instant.class));
	}

	@Test
	void ignoresDuplicateEventUuid() {
		UUID eventUuid = UUID.randomUUID();
		UUID memberUuid = UUID.randomUUID();
		when(processedMemberEventPort.existsByEventUuid(eventUuid)).thenReturn(true);

		applyGradeChangedService.apply(new ApplyGradeChangedUseCase.GradeChangedCommand(
				eventUuid.toString(),
				memberUuid.toString(),
				"EXPLORER",
				true,
				false
		));

		verify(memberRepository, never()).updateGradeBenefits(any(), any(), anyBoolean(), anyBoolean());
		verify(processedMemberEventPort, never()).save(any(), any(), any());
	}

	@Test
	void rejectsBlankCurrentGradeCode() {
		assertThatThrownBy(() -> applyGradeChangedService.apply(new ApplyGradeChangedUseCase.GradeChangedCommand(
				UUID.randomUUID().toString(),
				UUID.randomUUID().toString(),
				" ",
				false,
				false
		))).isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void skipsMissingProfileAndStillMarksProcessed() {
		UUID eventUuid = UUID.randomUUID();
		UUID memberUuid = UUID.randomUUID();
		when(processedMemberEventPort.existsByEventUuid(eventUuid)).thenReturn(false);
		when(memberRepository.findProfileByMemberUuid(memberUuid)).thenReturn(Optional.empty());

		applyGradeChangedService.apply(new ApplyGradeChangedUseCase.GradeChangedCommand(
				eventUuid.toString(),
				memberUuid.toString(),
				"ROOKIE",
				false,
				false
		));

		verify(memberRepository, never()).updateGradeBenefits(any(), any(), anyBoolean(), anyBoolean());
		verify(processedMemberEventPort).save(eq(eventUuid), eq(memberUuid), any(Instant.class));
		assertThat(eventUuid).isNotNull();
	}

	private MemberProfile profile(UUID memberUuid) {
		return new MemberProfile(1L, memberUuid, "닉네임", null, null, "ROOKIE");
	}
}

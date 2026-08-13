package com.planwith.planwith_fo_member.application.port.in;

import java.util.UUID;

import org.springframework.web.multipart.MultipartFile;

public interface UploadProfileImageUseCase {

	GetMyProfileUseCase.ProfileResult upload(UUID memberUuid, MultipartFile file);
}

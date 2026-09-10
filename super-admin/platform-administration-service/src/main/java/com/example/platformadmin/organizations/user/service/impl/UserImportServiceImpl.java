package com.example.platformadmin.organizations.user.service.impl;

import com.example.platformadmin.organizations.user.dto.BulkUploadResponseDto;
import com.example.platformadmin.organizations.user.dto.BulkUserRequestDto;
import com.example.platformadmin.organizations.user.dto.ImportErrorDto;
import com.example.platformadmin.organizations.user.dto.ImportSummaryDto;
import com.example.platformadmin.organizations.user.entity.User;
import com.example.platformadmin.organizations.user.enums.UserStatus;
import com.example.platformadmin.organizations.user.repository.UserRepository;
import com.example.platformadmin.organizations.user.service.UserImportService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

@Service
public class UserImportServiceImpl implements UserImportService {

    private final UserRepository userRepository;

    public UserImportServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public BulkUploadResponseDto bulkUpload(
            List<BulkUserRequestDto> users) {

        int success = 0;
        int failed = 0;

        for (BulkUserRequestDto dto : users) {

            if (userRepository.existsByEmail(dto.getEmail())) {
                failed++;
                continue;
            }

            User user = new User();

            user.setFirstName(dto.getFirstName());
            user.setLastName(dto.getLastName());
            user.setEmail(dto.getEmail());
            user.setCompanyId(dto.getCompanyId());
            user.setDepartmentId(dto.getDepartmentId());
            user.setStatus(UserStatus.ACTIVE);
            user.setDeleted(false);

            userRepository.save(user);
            success++;
        }

        return new BulkUploadResponseDto(
                users.size(),
                success,
                failed);
    }

    @Override
    @Transactional
    public ImportSummaryDto importCsv(MultipartFile file)
            throws IOException {

        List<ImportErrorDto> errors = new ArrayList<>();

        int total = 0;
        int success = 0;

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream()))) {

             reader.readLine();

            String line;

            while ((line = reader.readLine()) != null) {

                if (line.isBlank()) {
                    continue;
                }

                total++;

                String[] data = line.split(",");

                if (data.length < 5) {
                    errors.add(
                            new ImportErrorDto(
                                    total,
                                    "",
                                    "Invalid CSV row"));
                    continue;
                }

                String firstName = data[0].trim();
                String lastName = data[1].trim();
                String email = data[2].trim();
                String companyIdValue = data[3].trim();
                String departmentIdValue = data[4].trim();

                if (userRepository.existsByEmail(email)) {
                    errors.add(
                            new ImportErrorDto(
                                    total,
                                    email,
                                    "Email already exists"));
                    continue;
                }

                try {

                    Long companyId = Long.parseLong(companyIdValue);
                    Long departmentId = Long.parseLong(departmentIdValue);

                    User user = new User();

                    user.setFirstName(firstName);
                    user.setLastName(lastName);
                    user.setEmail(email);
                    user.setCompanyId(companyId);
                    user.setDepartmentId(departmentId);
                    user.setStatus(UserStatus.ACTIVE);
                    user.setDeleted(false);

                    userRepository.save(user);

                    success++;

                } catch (NumberFormatException e) {

                    errors.add(
                            new ImportErrorDto(
                                    total,
                                    email,
                                    "Invalid company ID or department ID"));
                }
            }
        }

        ImportSummaryDto summary = new ImportSummaryDto();

        summary.setTotalRecords(total);
        summary.setSuccessfulRecords(success);
        summary.setFailedRecords(errors.size());
        summary.setErrors(errors);

        return summary;
    }
}
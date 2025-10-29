package com.mcq.server.service;
import java.security.SecureRandom;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.mcq.server.repository.ClassroomRepository;

@Component
public class UniqueCodeGenerator {
    private static final String ALPHANUM = "ABCDEFGHIJKLMNOPQRSTUVWXYZ012346789";
    private static final int LENGTH = 4;
    private static final SecureRandom random = new SecureRandom();

    private final ClassroomRepository classroomRepository;

    @Autowired
    public UniqueCodeGenerator(ClassroomRepository classroomRepository) {
        this.classroomRepository = classroomRepository;
    }

    public String generateUniqueCode() {
        String code;
        do {
            code = randomCode();
        } while (classroomRepository.findById(code).isPresent());
        return code;
    }

    private String randomCode() {
        StringBuilder sb = new StringBuilder(LENGTH);
        for (int i = 0; i < LENGTH; i++) {
            sb.append(ALPHANUM.charAt(random.nextInt(ALPHANUM.length())));
        }
        return sb.toString();
    }
}
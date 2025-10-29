package com.mcq.server.dto;

import com.mcq.server.model.Classroom;
import java.util.List;

public class ClassroomDTO {

    private String code;
    private String classroomname;
    private UserDTO classroomteacher;
    private List<String> classroomstudents;

    public ClassroomDTO(Classroom classroom) {
        this.code = classroom.getCode();
        this.classroomname = classroom.getClassroomname();
        this.classroomteacher = new UserDTO(classroom.getClassroomteacher());
        this.classroomstudents = classroom.getClassroomstudents();
    }

    // Getters
    public String getCode() {
        return code;
    }

    public String getClassroomname() {
        return classroomname;
    }

    public UserDTO getClassroomteacher() {
        return classroomteacher;
    }

    public List<String> getClassroomstudents() {
        return classroomstudents;
    }
}
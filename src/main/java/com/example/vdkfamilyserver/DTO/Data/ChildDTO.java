package com.example.vdkfamilyserver.DTO.Data;

import com.example.vdkfamilyserver.Models.Child;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChildDTO {
    private Long id;
    private LocalDate birthDate;
    private String gender;

    public ChildDTO(Child child) {
        this.id = child.getId();
        this.birthDate = child.getBirthDate();
        this.gender = String.valueOf(child.getGender());
    }
}


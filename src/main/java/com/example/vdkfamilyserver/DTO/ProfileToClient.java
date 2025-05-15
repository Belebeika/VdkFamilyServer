package com.example.vdkfamilyserver.DTO;
import com.fasterxml.jackson.annotation.JsonProperty;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProfileToClient {
    @JsonProperty("firstName")
    private String firstName;

    @JsonProperty("lastName")
    private String lastName;

    @JsonProperty("patronymic")
    private String patronymic;

    @JsonProperty("married")
    private boolean married;
}
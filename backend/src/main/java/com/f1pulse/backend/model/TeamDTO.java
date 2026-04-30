package com.f1pulse.backend.model;

public class TeamDTO {

    private String name;
    private String nationality;

    public TeamDTO(String name, String nationality) {
        this.name = name;
        this.nationality = nationality;
    }

    public String getName() { return name; }
    public String getNationality() { return nationality; }
}

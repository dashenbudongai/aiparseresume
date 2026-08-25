package com.aiparse.cli.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Education {
    private String school;
    private String major;
    private String degree;

    @JsonProperty("graduation_time")
    private String graduationTime;

    public String getSchool() { return school; }
    public void setSchool(String school) { this.school = school; }

    public String getMajor() { return major; }
    public void setMajor(String major) { this.major = major; }

    public String getDegree() { return degree; }
    public void setDegree(String degree) { this.degree = degree; }

    public String getGraduationTime() { return graduationTime; }
    public void setGraduationTime(String graduationTime) { this.graduationTime = graduationTime; }
}

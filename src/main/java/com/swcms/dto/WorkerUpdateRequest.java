package com.swcms.dto;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
public class WorkerUpdateRequest {
    @NotBlank @Size(max=100) private String name;
    @NotBlank @Email private String email;
    private Boolean active;
    public String getName(){return name;} public void setName(String name){this.name=name;}
    public String getEmail(){return email;} public void setEmail(String email){this.email=email;}
    public Boolean getActive(){return active;} public void setActive(Boolean active){this.active=active;}
}

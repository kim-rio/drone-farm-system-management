package com.dmfs.company.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CreateCompanyRequest {

    @NotBlank(message = "Company name is required")
    @Size(max = 150, message = "Company name must not exceed 150 characters")
    private String name;

    @NotBlank(message = "Registration number is required")
    @Size(max = 100, message = "Registration number must not exceed 100 characters")
    private String registrationNumber;

    @Size(max = 100, message = "TIN must not exceed 100 characters")
    private String tin;

    @NotBlank(message = "Company email is required")
    @Email(message = "Invalid company email address")
    @Size(max = 100, message = "Company email must not exceed 100 characters")
    private String email;

    @NotBlank(message = "Phone number is required")
    @Size(max = 30, message = "Phone number must not exceed 30 characters")
    private String phone;

    @NotBlank(message = "Country is required")
    @Size(max = 100, message = "Country must not exceed 100 characters")
    private String country;

    @NotBlank(message = "Region is required")
    @Size(max = 100, message = "Region must not exceed 100 characters")
    private String region;

    @NotBlank(message = "City is required")
    @Size(max = 100, message = "City must not exceed 100 characters")
    private String city;

    @NotBlank(message = "Physical address is required")
    @Size(max = 255, message = "Physical address must not exceed 255 characters")
    private String physicalAddress;

    @Valid
    private InitialAdminRequest initialAdmin;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRegistrationNumber() {
        return registrationNumber;
    }

    public void setRegistrationNumber(String registrationNumber) {
        this.registrationNumber = registrationNumber;
    }

    public String getTin() {
        return tin;
    }

    public void setTin(String tin) {
        this.tin = tin;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getPhysicalAddress() {
        return physicalAddress;
    }

    public void setPhysicalAddress(String physicalAddress) {
        this.physicalAddress = physicalAddress;
    }

    public InitialAdminRequest getInitialAdmin() {
        return initialAdmin;
    }

    public void setInitialAdmin(InitialAdminRequest initialAdmin) {
        this.initialAdmin = initialAdmin;
    }

    public static class InitialAdminRequest {

        @NotBlank(message = "Admin first name is required")
        @Size(max = 100, message = "Admin first name must not exceed 100 characters")
        private String firstName;

        @NotBlank(message = "Admin last name is required")
        @Size(max = 100, message = "Admin last name must not exceed 100 characters")
        private String lastName;

        @NotBlank(message = "Admin email is required")
        @Email(message = "Invalid admin email address")
        @Size(max = 100, message = "Admin email must not exceed 100 characters")
        private String email;

        @NotBlank(message = "Admin password is required")
        @Size(min = 8, max = 100, message = "Admin password must be between 8 and 100 characters")
        private String password;

        public String getFirstName() {
            return firstName;
        }

        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }

        public String getLastName() {
            return lastName;
        }

        public void setLastName(String lastName) {
            this.lastName = lastName;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }
}

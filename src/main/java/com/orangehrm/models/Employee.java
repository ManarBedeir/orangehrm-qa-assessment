package com.orangehrm.models;

/** Employee test data as entered in PIM. */
public record Employee(String firstName, String middleName, String lastName, String employeeId) {

    /** Value of the employee list "First (&amp; Middle) Name" column. */
    public String firstAndMiddleName() {
        return middleName.isEmpty() ? firstName : firstName + " " + middleName;
    }

    public String fullName() {
        return firstAndMiddleName() + " " + lastName;
    }

    public Employee withLastName(String newLastName) {
        return new Employee(firstName, middleName, newLastName, employeeId);
    }

    public Employee withEmployeeId(String newEmployeeId) {
        return new Employee(firstName, middleName, lastName, newEmployeeId);
    }
}

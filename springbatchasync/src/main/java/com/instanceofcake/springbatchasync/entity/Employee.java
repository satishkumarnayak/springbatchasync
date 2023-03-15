package com.instanceofcake.springbatchasync.entity;

public class Employee {
  private int id;
  private String name;
  private String staffNumber;

  public Employee() {

  }

  public Employee(int id, String name, String rollNumber) {
    this.id = id;
    this.name = name;
    this.staffNumber = rollNumber;
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getRollNumber() {
    return staffNumber;
  }

  public void setRollNumber(String rollNumber) {
    this.staffNumber = rollNumber;
  }


}

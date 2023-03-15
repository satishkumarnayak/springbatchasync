package com.instanceofcake.springbatchasync.processor;

import org.springframework.batch.item.ItemProcessor;
import com.instanceofcake.springbatchasync.entity.Employee;

public class EmployeeItemProcessor implements ItemProcessor<Employee, Employee> {

  @Override
  public Employee process(Employee item) throws Exception {
    
    System.out.println("Processing :"+Thread.currentThread().getName()+item.getName());
    Thread.sleep(5000);
    
    return item;
  }

}

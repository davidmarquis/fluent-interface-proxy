package com.fluentinterface.domain.dao;

import com.fluentinterface.builder.Builder;

public interface EmployeeBuilder 
         extends HumanCommonBuilder<EmployeeBuilder>,
                 Builder<Employee> {

	EmployeeBuilder withDepartment(final String department);
	
	EmployeeBuilder withSalary(final double salary);
}

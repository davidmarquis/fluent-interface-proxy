package com.fluentinterface;

import static com.fluentinterface.ReflectionBuilder.implementationFor;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.is;

import java.util.Calendar;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import com.fluentinterface.domain.PersonBuilder;
import com.fluentinterface.domain.dao.Customer;
import com.fluentinterface.domain.dao.CustomerBuilder;
import com.fluentinterface.domain.dao.CustomerType;
import com.fluentinterface.domain.dao.Employee;
import com.fluentinterface.domain.dao.EmployeeBuilder;
import com.fluentinterface.proxy.AttributeAccessStrategy;
import com.fluentinterface.proxy.impl.FieldAttributeAccessStrategy;
import com.fluentinterface.proxy.impl.SetterAttributeAccessStrategy;

@RunWith(Parameterized.class)
public class BuilderProxyDaoTest {
	
    @Parameterized.Parameters
    public static Iterable<Object[]> strategies() {
        return asList(
                new Object[] {new FieldAttributeAccessStrategy()},
                new Object[] {new SetterAttributeAccessStrategy()}
        );
    }
	
	private CustomerBuilder customerBuilder;
	
	private EmployeeBuilder employeeBuilder;
	
    private AttributeAccessStrategy attributeAccessStrategy;

    public BuilderProxyDaoTest(AttributeAccessStrategy attributeAccessStrategy) {
        this.attributeAccessStrategy = attributeAccessStrategy;
    }
	
    @Before
    public void setup() throws InstantiationException, IllegalAccessException {
        customerBuilder = aCustomer();
        employeeBuilder = anEmployee();
    }
    
	private CustomerBuilder aCustomer(){
        return implementationFor(CustomerBuilder.class)
        		.builds(Customer.class)
                .usingAttributeAccessStrategy(attributeAccessStrategy)
                .create();
	}	
	
	private EmployeeBuilder anEmployee(){
        return implementationFor(EmployeeBuilder.class)
        		.builds(Employee.class)
                .usingAttributeAccessStrategy(attributeAccessStrategy)
                .create();
	}
	
	@Test
	public void whenBuildCustomer() {
		Calendar cal      = Calendar.getInstance();
		
		Customer customer = customerBuilder
				               .withId("c-001")
				               .withVersion(1)
				               .withDescription("register via internet")
				               .withCreatedBy("admin-agent")
				               .withCreated(cal)
				               .withUpdatedBy("admin-agent")
				               .withUpdated(cal)
				            
  				               .withFirstName("Charlee")
				               .withLastName("Ch.")
				               .withAddress("my-address")
				            
				               .withType(CustomerType.BASIC)
				            
				               .build();
		
		assertThat(customer.getId(), is("c-001"));
		assertThat(customer.getVersion(), is(1));
		assertThat(customer.getDescription(), is("register via internet"));
		assertThat(customer.getCreatedBy(), is("admin-agent"));
		assertThat(customer.getCreated().getTimeInMillis(), is(cal.getTimeInMillis()));
		assertThat(customer.getUpdatedBy(), is("admin-agent"));
		assertThat(customer.getUpdated().getTimeInMillis(), is(cal.getTimeInMillis()));
		
		assertThat(customer.getFirstName(), is("Charlee"));
		assertThat(customer.getLastName(), is("Ch."));
		assertThat(customer.getAddress(), is("my-address"));
		
		assertThat(customer.getType(), is(CustomerType.BASIC));
	}	
	
	@Test
	public void whenBuildEmployee() {
		Calendar cal      = Calendar.getInstance();
		
		Employee employee = employeeBuilder
				               .withId("c-001")
				               .withVersion(1)
				               .withDescription("register via internet")
				               .withCreatedBy("admin-agent")
				               .withCreated(cal)
				               .withUpdatedBy("admin-agent")
				               .withUpdated(cal)
				            
				               .withFirstName("Charlee")
				               .withLastName("Ch.")
				               .withAddress("my-address")
				            
				               .withDepartment("my-dept")
				               .withSalary(100.50D)
				               
				               .build();
		
		assertThat(employee.getId(), is("c-001"));
		assertThat(employee.getVersion(), is(1));
		assertThat(employee.getDescription(), is("register via internet"));
		assertThat(employee.getCreatedBy(), is("admin-agent"));
		assertThat(employee.getCreated().getTimeInMillis(), is(cal.getTimeInMillis()));
		assertThat(employee.getUpdatedBy(), is("admin-agent"));
		assertThat(employee.getUpdated().getTimeInMillis(), is(cal.getTimeInMillis()));
		
		assertThat(employee.getFirstName(), is("Charlee"));
		assertThat(employee.getLastName(), is("Ch."));
		assertThat(employee.getAddress(), is("my-address"));
		
		assertThat(employee.getDepartment(), is("my-dept"));
		assertThat(employee.getSalary(), is(100.50D));
	}
}

package com.fluentinterface;

import static com.fluentinterface.ReflectionBuilder.implementationFor;
import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Calendar;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

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
        Customer customer = customerBuilder
                .withId("c-001").withVersion(1)

                .withFirstName("Charlee")
                .withLastName("Ch.")

                .build();

        assertThat(customer.getId(), is("c-001"));
        assertThat(customer.getVersion(), is(1));

        assertThat(customer.getFirstName(), is("Charlee"));
        assertThat(customer.getLastName(), is("Ch."));

    }

    @Test
    public void whenBuildEmployee() {

        Employee employee = employeeBuilder
                .withId("c-001")
                .withVersion(1)

                .withFirstName("Charlee")
                .withLastName("Ch.")

                .withDepartment("my-dept")

                .build();

        assertThat(employee.getId(), is("c-001"));
        assertThat(employee.getVersion(), is(1));


        assertThat(employee.getFirstName(), is("Charlee"));
        assertThat(employee.getLastName(), is("Ch."));

        assertThat(employee.getDepartment(), is("my-dept"));
    }

    @Test
    public void shouldSetPropertyToEnum() {
        Customer customer = customerBuilder
                .withType(CustomerType.BASIC)
                .build();

        assertThat(customer.getType(), is(CustomerType.BASIC));
    }

    @Test
    public void shouldSetPropertyToCalendar() {
        Calendar calendar = Calendar.getInstance();
        Customer customer = customerBuilder.withCreated(calendar).build();

        assertThat(customer.getCreated().getTimeInMillis(),
                is(calendar.getTimeInMillis()));
    }

    @Test
    public void shouleSetPropertyToDouble() {
        Employee employee = employeeBuilder.withSalary(100.50D).build();

        assertThat(employee.getSalary(), is(100.50D));
    }

    @Test(expected = IllegalStateException.class)
    public void shouldFailWhenBuilderUsesAnUnknownProperty() {

        customerBuilder.withAnUnknownProperty("fails").build();
    }
}

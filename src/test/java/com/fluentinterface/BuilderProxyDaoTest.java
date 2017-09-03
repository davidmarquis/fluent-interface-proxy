package com.fluentinterface;

import com.fluentinterface.domain.dao.*;
import com.fluentinterface.proxy.PropertyAccessStrategy;
import com.fluentinterface.proxy.impl.FieldPropertyAccessStrategy;
import com.fluentinterface.proxy.impl.SetterPropertyAccessStrategy;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Calendar;

import static com.fluentinterface.ReflectionBuilder.implementationFor;
import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(Parameterized.class)
public class BuilderProxyDaoTest {

    @Parameterized.Parameters
    public static Iterable<Object[]> strategies() {
        return asList(
                new Object[] {new FieldPropertyAccessStrategy()},
                new Object[] {new SetterPropertyAccessStrategy()}
                );
    }

    private PropertyAccessStrategy propertyAccessStrategy;

    public BuilderProxyDaoTest(PropertyAccessStrategy propertyAccessStrategy) {
        this.propertyAccessStrategy = propertyAccessStrategy;
    }

    private CustomerBuilder aCustomer(){
        return implementationFor(CustomerBuilder.class)
                .usingAttributeAccessStrategy(propertyAccessStrategy)
                .create();
    }

    private EmployeeBuilder anEmployee(){
        return implementationFor(EmployeeBuilder.class)
                .usingAttributeAccessStrategy(propertyAccessStrategy)
                .create();
    }

    @Test
    public void whenBuildCustomer() {
        Customer customer = aCustomer()
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

        Employee employee = anEmployee()
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
        Customer customer = aCustomer()
                .withType(CustomerType.BASIC)
                .build();

        assertThat(customer.getType(), is(CustomerType.BASIC));
    }

    @Test
    public void shouldSetPropertyToCalendar() {
        Calendar calendar = Calendar.getInstance();
        Customer customer = aCustomer().withCreated(calendar).build();

        assertThat(customer.getCreated().getTimeInMillis(),
                is(calendar.getTimeInMillis()));
    }

    @Test
    public void shouleSetPropertyToDouble() {
        Employee employee = anEmployee().withSalary(100.50D).build();

        assertThat(employee.getSalary(), is(100.50D));
    }

    @Test(expected = IllegalStateException.class)
    public void shouldFailWhenBuilderUsesAnUnknownProperty() {

        aCustomer().withAnUnknownProperty("a value").build();
    }
}

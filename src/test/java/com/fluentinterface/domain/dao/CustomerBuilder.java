package com.fluentinterface.domain.dao;

import com.fluentinterface.builder.Builder;

public interface CustomerBuilder 
         extends HumanCommonBuilder<CustomerBuilder>,
                 Builder<Customer> {
	CustomerBuilder withType(final CustomerType type);
}

package com.fluentinterface.domain.dao;

public class Customer extends HumanCommon {
	private CustomerType type;

	public CustomerType getType() {
		return type;
	}

	public void setType(CustomerType type) {
		this.type = type;
	}
}

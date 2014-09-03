package com.wordpress.ezegrande.drools.examples.model;

/**
 * A simple representation of a Potential Customer:
 * <ul>
 * <li>Customer Name
 * <li>Credit Limit
 * </ul>
 * 
 * @author ezegrande
 *
 */
public class PotentialCustomer {
    private String customerName;
    private double creditLimit;

    public PotentialCustomer(String customerName, double creditLimit) {
        this.customerName = customerName;
        this.creditLimit = creditLimit;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public double getCreditLimit() {
        return creditLimit;
    }

    public void setCreditLimit(double creditLimit) {
        this.creditLimit = creditLimit;
    }

    public String toString() {
        return "Potential Customer [Name: " + customerName + " | Credit Limit: " + creditLimit + "]";
    }
}

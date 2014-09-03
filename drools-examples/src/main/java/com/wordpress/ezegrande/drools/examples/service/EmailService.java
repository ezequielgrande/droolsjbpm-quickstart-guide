package com.wordpress.ezegrande.drools.examples.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.wordpress.ezegrande.drools.examples.model.PotentialCustomer;

public class EmailService {
    private Logger logger = LoggerFactory.getLogger(EmailService.class);

    private static EmailService instance = new EmailService();

    private EmailService() {
        // Non-instantiable from outside
    }

    public static EmailService getInstance() {
        return instance;
    }

    public void sendCreditCardOffer(PotentialCustomer pc) {
        logger.info("*********** ...Sending Credit Card Offer... ********************");
        logger.info("*****  To: " + pc.getCustomerName());
        logger.info("*****  Credit limit: " + pc.getCreditLimit());
        logger.info("*********** ...email sent... ********************");
        
    }
}

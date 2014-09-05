package com.wordpress.ezegrande.drools.examples.simple;

import java.util.Collection;

import junit.framework.Assert;

import org.drools.core.ClassObjectFilter;
import org.drools.core.common.DefaultFactHandle;
import org.junit.Test;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.rule.FactHandle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.wordpress.ezegrande.drools.examples.TestUtil;
import com.wordpress.ezegrande.drools.examples.model.PaymentMethod;
import com.wordpress.ezegrande.drools.examples.model.PotentialCustomer;
import com.wordpress.ezegrande.drools.examples.model.Purchase;
import com.wordpress.ezegrande.drools.examples.service.EmailService;

/**
 * Tests cases for the Business Rules included in the files 'discount.drl' and
 * 'potentialCustomer02.drl'. The goal of this Test is to show that Business
 * Rules can be written independently from each other, even in different DRL
 * files.
 * 
 * @see DiscountRulesTestCase
 * @see PotentialCustomerRulesTestCase
 * 
 * @author ezegrande
 */
public class AllRulesTestCase {
    private Logger logger = LoggerFactory.getLogger(AllRulesTestCase.class);

    private static final String POTENTIAL_CUSTOMER_DRL = "com/wordpress/ezegrande/drools/examples/simple/potentialCustomer02.drl";
    private static final String DISCOUNT_DRL = "com/wordpress/ezegrande/drools/examples/simple/discount.drl";

    /**
     * Tests the Discout Rules and the Potential Customer Rules. Inserts the
     * following Purchases into the Working Memory in order to evaluate if they
     * are correctly evaluated by the rules:
     * <ul>
     * <li>Customer 'john' spends $350 in Cash
     * <li>Customer 'mary' spends $250 in Cash
     * <li>Customer 'john' spends $400 in Cash
     * <li>Customer 'george' spends $500 in Credit Card
     * <li>Customer 'john' spends $500 in Debit Card
     * </ul>
     * 
     * After firing the Rules, it asserts that the discount has benn applied and
     * the Potential Customer has been identified.
     */
    @Test
    public void testAllDrlFiles() {
        logger.info("Starting @Test testAllDrlFiles()");
        // Create the Stateful Session
        KieSession session = TestUtil.createKieSession(POTENTIAL_CUSTOMER_DRL, DISCOUNT_DRL);
        // Add SLF4j Logger as a Global Variable
        session.setGlobal("logger", logger);
        session.setGlobal("emailService", EmailService.getInstance());

        // Create objects that will be inserted into the Session
        Purchase cashPurchasePotentialCustomer1 = new Purchase("john", 350, PaymentMethod.CASH);
        Purchase cashPurchaseLowAmount = new Purchase("mary", 250, PaymentMethod.CASH);
        Purchase cashPurchasePotentialCustomer2 = new Purchase("john", 400, PaymentMethod.CASH);
        Purchase creditPurchase = new Purchase("george", 500, PaymentMethod.CREDIT);
        Purchase debitPurchase = new Purchase("john", 500, PaymentMethod.DEBIT);

        logger.info("Inserting objects into Session...");
        // Insert objects into the working memory
        session.insert(cashPurchasePotentialCustomer1);
        session.insert(cashPurchaseLowAmount);
        session.insert(cashPurchasePotentialCustomer2);
        session.insert(creditPurchase);
        session.insert(debitPurchase);

        // Since rules were not fired, the discount has not been calculated yet
        Assert.assertEquals(0d, cashPurchasePotentialCustomer1.getDiscount());
        Assert.assertEquals(0d, cashPurchaseLowAmount.getDiscount());
        Assert.assertEquals(0d, cashPurchasePotentialCustomer2.getDiscount());
        Assert.assertEquals(0d, creditPurchase.getDiscount());

        // Since rules were not fired, the Potential Customer has not been
        // inserted into the Working Memory
        Collection<FactHandle> factHandles = session.getFactHandles(new ClassObjectFilter(PotentialCustomer.class));
        Assert.assertEquals(0, factHandles.size());

        // Now fire all the rules
        logger.info("Fire all rules!!");
        session.fireAllRules();

        // After firing the rules, discounts have been calculated
        Assert.assertEquals(0d, cashPurchasePotentialCustomer1.getDiscount());
        Assert.assertEquals(0d, cashPurchaseLowAmount.getDiscount());
        Assert.assertEquals(0d, cashPurchasePotentialCustomer2.getDiscount());
        Assert.assertEquals(0.05, debitPurchase.getDiscount());
        Assert.assertEquals(0.1, creditPurchase.getDiscount());

        // After firing the rules, the Potential Customer has been inserted
        factHandles = session.getFactHandles(new ClassObjectFilter(PotentialCustomer.class));
        Assert.assertEquals(1, factHandles.size());
        for (FactHandle fh : factHandles) {
            PotentialCustomer pc = (PotentialCustomer) ((DefaultFactHandle) fh).getObject();
            Assert.assertEquals("john", pc.getCustomerName());
        }

        // Release resources
        session.dispose();
        logger.info("===> End of test <===\n");
    }

}

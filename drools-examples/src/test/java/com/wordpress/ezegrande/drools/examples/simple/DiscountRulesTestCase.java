package com.wordpress.ezegrande.drools.examples.simple;

import java.util.Arrays;

import junit.framework.Assert;

import org.junit.Test;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.StatelessKieSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.wordpress.ezegrande.drools.examples.TestUtil;
import com.wordpress.ezegrande.drools.examples.model.PaymentMethod;
import com.wordpress.ezegrande.drools.examples.model.Purchase;

/**
 * Tests cases for the Business Rules included in the file 'discount.drl". The
 * goal of these rules is to apply a discount to a Purchase based on the Payment
 * Method. There are three different cases:
 * <ul>
 * <li>Cash purchases have no discount
 * <li>Debit Card purchases have 5% of discount
 * <li>Credit Card purchases have 10% of discount
 * </ul>
 * 
 * A Global Variable is used for Logging purposes. To set a Global Variable in
 * the session, use the methods {@link KieSession#setGlobal(String, Object)} and
 * {@link StatelessKieSession#setGlobal(String, Object)}. In the DRL file it
 * must be defined in this way: 'global com.package.ClassName variableName'. For
 * example, 'global org.slf4j.Logger logger'.
 * 
 * Note that in this example Business Rules update a field of the objects in
 * Memory. Rules do not insert new objects into the Working Memory.
 * 
 * @author ezegrande
 */
public class DiscountRulesTestCase {
    private Logger logger = LoggerFactory.getLogger(DiscountRulesTestCase.class);

    private static final String DRL_PATH = "com/wordpress/ezegrande/drools/examples/simple/discount.drl";

    /**
     * Tests Business Rules with a {@link StatelessKieSession}. Note that a
     * Stateless Session wraps a StatefulKieSession. These are the actions taken
     * by the engine when the {@link StatelessKieSession#execute(Iterable)}
     * method is called:
     * <ul>
     * <li>A new Working Memory is created
     * <li>Objects are inserted into the Working Memory
     * <li>All rules are fired
     * <li>Session is disposed
     * </ul>
     * 
     * @see StatelessKieSession
     * @see StatelessKieSession#execute(Object)
     * @see StatelessKieSession#execute(Iterable)
     * @see StatelessKieSession#execute(org.kie.api.command.Command)
     */
    @Test
    public void testStatelessSession() {
        logger.info("Starting @Test testStatelessSession()");
        // Create the Stateless Session
        StatelessKieSession session = TestUtil.createStatelessKieSession(DRL_PATH);
        // Add SLF4j Logger as a Global Variable
        session.setGlobal("logger", logger);
        // Create our 'input' objects, that will be inserted into the Session
        Purchase cashPurchase = new Purchase("john", 100, PaymentMethod.CASH);
        Purchase debitPurchase = new Purchase("peter", 100, PaymentMethod.DEBIT);
        Purchase creditPurchase = new Purchase("george", 100, PaymentMethod.CREDIT);

        logger.info("Executing Stateless Session...");
        // Execute the StatelessSession
        session.execute(Arrays.asList(cashPurchase, debitPurchase, creditPurchase));

        // Assert that the discounts were calculated by the rules
        Assert.assertEquals(0d, cashPurchase.getDiscount());
        Assert.assertEquals(0.05, debitPurchase.getDiscount());
        Assert.assertEquals(0.1, creditPurchase.getDiscount());

        logger.info("===> End of test <===\n");
    }

    /**
     * Tests Business Rules with a Stateful Session, by following these steps:
     * <ul>
     * <li>Objects are inserted into the Working Memory.
     * <li>Assert that the objects weren't modified by the rules
     * <li>Fire all rules
     * <li>Assert that the objects were modified by the rules
     * </ul>
     */
    @Test
    public void testStatefulSession() {
        logger.info("Starting @Test testStatefulSession()");
        // Create the Stateful Session
        KieSession session = TestUtil.createKieSession(DRL_PATH);
        // Add SLF4j Logger as a Global Variable
        session.setGlobal("logger", logger);

        // Create objects that will be inserted into the Session
        Purchase cashPurchase = new Purchase("john", 100, PaymentMethod.CASH);
        Purchase debitPurchase = new Purchase("peter", 100, PaymentMethod.DEBIT);
        Purchase creditPurchase = new Purchase("george", 100, PaymentMethod.CREDIT);

        logger.info("Inserting objects into Session...");
        // Insert objects into the working memory
        session.insert(cashPurchase);
        session.insert(debitPurchase);
        session.insert(creditPurchase);

        // Since rules were not fired, the discount has not been calculated yet
        Assert.assertEquals(0d, cashPurchase.getDiscount());
        Assert.assertEquals(0d, debitPurchase.getDiscount());
        Assert.assertEquals(0d, creditPurchase.getDiscount());

        // Now fire all the rules
        logger.info("Fire all rules!!");
        session.fireAllRules();

        // After firing the rules, discounts have been calculated
        Assert.assertEquals(0d, cashPurchase.getDiscount());
        Assert.assertEquals(0.05, debitPurchase.getDiscount());
        Assert.assertEquals(0.1, creditPurchase.getDiscount());

        // Release resources
        session.dispose();
        logger.info("===> End of test <===\n");
    }
}

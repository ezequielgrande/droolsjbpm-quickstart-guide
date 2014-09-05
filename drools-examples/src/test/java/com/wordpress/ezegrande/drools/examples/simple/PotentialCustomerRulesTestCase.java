package com.wordpress.ezegrande.drools.examples.simple;

import java.util.Collection;

import junit.framework.Assert;

import org.drools.core.ClassObjectFilter;
import org.drools.core.common.DefaultFactHandle;
import org.junit.Test;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.StatelessKieSession;
import org.kie.api.runtime.rule.FactHandle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.wordpress.ezegrande.drools.examples.TestUtil;
import com.wordpress.ezegrande.drools.examples.model.PaymentMethod;
import com.wordpress.ezegrande.drools.examples.model.PotentialCustomer;
import com.wordpress.ezegrande.drools.examples.model.Purchase;
import com.wordpress.ezegrande.drools.examples.service.EmailService;

/**
 * Tests cases for the Business Rules included in the file
 * 'potentialCustomer01.drl' and 'potentialCustomer02.drl'. The goal
 * of these rules is to detect Potential Customers of a Credit Card. This is
 * done by using 'inference', which means to create new knowledge based on
 * current facts/knowledge. Rules description:
 * <ul>
 * <li>Identify Potential Customer: When a Customer spends more than $300 cash.
 * A new "Potential Customer" object is inserted into the working memory,
 * defining his limit credit
 * <li>When a Potential Customer is found in the Working Memory, notify the
 * Credit Card Offer to him. This can be done by email or phone, based on their
 * limit credit.
 * </ul>
 * 
 * Two Global Variables are used for logging purposes and to access a Service
 * from the Business Rules. To set a variable in the session, use the methods
 * {@link KieSession#setGlobal(String, Object)} and
 * {@link StatelessKieSession#setGlobal(String, Object)}. In the DRL file it
 * must be defined in this way: 'global com.package.ClassName variableName'. For
 * example, 'global org.slf4j.Logger logger'.
 * 
 * NOTE: There is an intentional error in the rules of
 * 'potentialCustomer01.drl' since the rule that identifies Potential
 * Customers will be fired for each Cash Purchase > $300. Because of this, we
 * might have more than one PotentialCustomer object for a same Customer. The
 * fix for this is shown in 'potentialCustomer02.drl'
 * 
 * @author ezegrande
 */
public class PotentialCustomerRulesTestCase {
    private Logger logger = LoggerFactory.getLogger(PotentialCustomerRulesTestCase.class);

    private static final String DRL01_PATH = "com/wordpress/ezegrande/drools/examples/simple/potentialCustomer01.drl";
    private static final String DRL02_PATH = "com/wordpress/ezegrande/drools/examples/simple/potentialCustomer02.drl";

    /**
     * Tests the Rule 'Identify potential customers' of the DRL file
     * 'potentialCustomer01.drl'. It will insert the following Purchases
     * into the Working Memory in order to evaluate if they are correctly
     * evaluated by the rules:
     * <ul>
     * <li>Customer 'john' spends $250 in Cash
     * <li>Customer 'mary' spends $450 in Cash
     * <li>Customer 'peter' spends $100 in Debit Card
     * <li>Customer 'george' spends $500 in Credit Card
     * </ul>
     * 
     * The second Purchase will be the only one that will trigger the Rule that
     * identifies a Potential Customer. Because of this, after firing the Rules
     * there will be only one "PotentialCustomer" object in the working memory
     * (inserted by the triggered Rule).
     */
    @Test
    public void testIdentifyPotentialCustomer() {
        logger.info("Starting @Test testIdentifyPotentialCustomer()");
        // Create the Stateful Session
        KieSession session = TestUtil.createKieSession(DRL01_PATH);
        // Add SLF4j Logger as a Global Variable
        session.setGlobal("logger", logger);
        // Add the Email Service as a Global Variable
        session.setGlobal("emailService", EmailService.getInstance());

        // Create objects that will be inserted into the Session
        Purchase cashPurchaseLowAmount = new Purchase("john", 250, PaymentMethod.CASH);
        Purchase cashPurchasePotentialCustomer = new Purchase("mary", 450, PaymentMethod.CASH);
        Purchase debitPurchase = new Purchase("peter", 100, PaymentMethod.DEBIT);
        Purchase creditPurchase = new Purchase("george", 500, PaymentMethod.CREDIT);

        logger.info("Inserting objects into Session...");
        // Insert objects into the working memory
        session.insert(cashPurchaseLowAmount);
        session.insert(cashPurchasePotentialCustomer);
        session.insert(debitPurchase);
        session.insert(creditPurchase);

        // Since rules were not fired, the Potential Customer has not been
        // inserted into the Working Memory
        Collection<FactHandle> factHandles = session.getFactHandles(new ClassObjectFilter(PotentialCustomer.class));
        Assert.assertEquals(0, factHandles.size());

        // Now fire all the rules
        logger.info("Fire all rules!!");
        session.fireAllRules();

        // After firing the rules, the Potential Customer has been inserted
        factHandles = session.getFactHandles(new ClassObjectFilter(PotentialCustomer.class));
        Assert.assertEquals(1, factHandles.size());
        FactHandle fh  = factHandles.iterator().next();
        PotentialCustomer pc = (PotentialCustomer) ((DefaultFactHandle) fh).getObject();
        Assert.assertEquals("mary", pc.getCustomerName());

        // Release resources
        session.dispose();
        logger.info("===> End of test <===\n");
    }

    /**
     * Tests the Rule 'Identify potential customers' of the DRL file
     * 'potentialCustomer01.drl'. It will insert the following Purchases
     * into the Working Memory in order to evaluate if they are correctly
     * evaluated by the rules:
     * <ul>
     * <li>Customer 'john' spends $350 in Cash
     * <li>Customer 'mary' spends $250 in Cash
     * <li>Customer 'john' spends $400 in Cash
     * <li>Customer 'george' spends $500 in Credit Card
     * </ul>
     * 
     * Note that 'john' has two associated Purchases which will trigger the Rule
     * being tested.Because of this, after firing the Rules the working memory
     * will have two "PotentialCustomer" objects for the same Customer. This
     * error is fixed in the DRL file 'potentialCustomer02.drl'.
     */
    @Test
    public void testIdentifyPotentialCustomer_Two_Purchases_Wrong() {
        logger.info("Starting @Test testIdentifyPotentialCustomer_Two_Purchases_Wrong()");
        // Create the Stateful Session
        KieSession session = TestUtil.createKieSession(DRL01_PATH);
        // Add SLF4j Logger as a Global Variable
        session.setGlobal("logger", logger);
        session.setGlobal("emailService", EmailService.getInstance());

        // Create objects that will be inserted into the Session
        Purchase cashPurchasePotentialCustomer1 = new Purchase("john", 350, PaymentMethod.CASH);
        Purchase cashPurchaseLowAmount = new Purchase("mary", 250, PaymentMethod.CASH);
        Purchase cashPurchasePotentialCustomer2 = new Purchase("john", 400, PaymentMethod.CASH);
        Purchase creditPurchase = new Purchase("george", 500, PaymentMethod.CREDIT);

        logger.info("Inserting objects into Session...");
        // Insert objects into the working memory
        session.insert(cashPurchasePotentialCustomer1);
        session.insert(cashPurchaseLowAmount);
        session.insert(cashPurchasePotentialCustomer2);
        session.insert(creditPurchase);

        // Since rules were not fired, the Potential Customer has not been
        // inserted into the Working Memory
        Collection<FactHandle> factHandles = session.getFactHandles(new ClassObjectFilter(PotentialCustomer.class));
        Assert.assertEquals(0, factHandles.size());

        // Now fire all the rules
        logger.info("Fire all rules!!");
        session.fireAllRules();

        // After firing the rules, two Potential Customers have been inserted
        // (which is wrong)
        factHandles = session.getFactHandles(new ClassObjectFilter(PotentialCustomer.class));
        Assert.assertEquals(2, factHandles.size());
        for (FactHandle fh : factHandles) {
            PotentialCustomer pc = (PotentialCustomer) ((DefaultFactHandle) fh).getObject();
            Assert.assertEquals("john", pc.getCustomerName());
        }

        // Release resources
        session.dispose();
        logger.info("===> End of test <===\n");
    }

    /**
     * Tests the Rule 'Identify potential customers' of the DRL file
     * 'potentialCustomer02.drl'. It will insert the following Purchases
     * into the Working Memory in order to evaluate if they are correctly
     * evaluated by the rules:
     * <ul>
     * <li>Customer 'john' spends $350 in Cash
     * <li>Customer 'mary' spends $250 in Cash
     * <li>Customer 'john' spends $400 in Cash
     * <li>Customer 'george' spends $500 in Credit Card
     * </ul>
     * 
     * Note that 'john' has two associated Purchases which will trigger the Rule
     * being tested. This version of the Rule works OK since it validates if
     * there is a "PotentialCustomer" object in memory before inserting a new
     * one.
     */
    @Test
    public void testIdentifyPotentialCustomer_Two_Purchases_OK() {
        logger.info("Starting @Test testIdentifyPotentialCustomer_Two_Purchases_OK()");
        // Create the Stateful Session
        KieSession session = TestUtil.createKieSession(DRL02_PATH);
        // Add SLF4j Logger as a Global Variable
        session.setGlobal("logger", logger);
        session.setGlobal("emailService", EmailService.getInstance());

        // Create objects that will be inserted into the Session
        Purchase cashPurchasePotentialCustomer1 = new Purchase("john", 350, PaymentMethod.CASH);
        Purchase cashPurchaseLowAmount = new Purchase("mary", 250, PaymentMethod.CASH);
        Purchase cashPurchasePotentialCustomer2 = new Purchase("john", 400, PaymentMethod.CASH);
        Purchase creditPurchase = new Purchase("george", 500, PaymentMethod.CREDIT);

        logger.info("Inserting objects into Session...");
        // Insert objects into the working memory
        session.insert(cashPurchasePotentialCustomer1);
        session.insert(cashPurchaseLowAmount);
        session.insert(cashPurchasePotentialCustomer2);
        session.insert(creditPurchase);

        // Since rules were not fired, the Potential Customer has not been
        // inserted into the Working Memory
        Collection<FactHandle> factHandles = session.getFactHandles(new ClassObjectFilter(PotentialCustomer.class));
        Assert.assertEquals(0, factHandles.size());

        // Now fire all the rules
        logger.info("Fire all rules!!");
        session.fireAllRules();

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

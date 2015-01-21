package com.wordpress.ezegrande.drools.attributes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.drools.core.time.SessionPseudoClock;
import org.junit.Assert;
import org.junit.Test;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.StatelessKieSession;
import org.kie.api.runtime.rule.FactHandle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.wordpress.ezegrande.drools.attributes.model.Gift;
import com.wordpress.ezegrande.drools.attributes.model.Person;
import com.wordpress.ezegrande.drools.util.TestUtil;

/**
 * Tests cases for the Business Rules included in the project drools-attributes.
 * The goal of these rules is to explain by examples the available Rules
 * attributes.
 * 
 * @author ezegrande
 * @see http://ezegrande.wordpress.com/2015/01/21/drools-rules-attributes
 */
public class DroolsAttributesTestCase {
    private static final String FIRST = "FIRST";

    private static final String SECOND = "SECOND";

    private static final String THIRD = "THIRD";

    private static final String ME = "me";

    private static final String JOHN = "John";

    private static final String CHOCOLATES = "Chocolates";

    private Logger logger = LoggerFactory.getLogger(DroolsAttributesTestCase.class);

    private static final String DRL_PATH_DIALECT = "dialect.drl";
    private static final String DRL_PATH_DATE_EFFECTIVE = "date-effective.drl";
    private static final String DRL_PATH_DATE_EXPIRES = "date-expires.drl";
    private static final String DRL_PATH_DURATION = "duration.drl";
    private static final String DRL_PATH_SALIENCE = "salience.drl";
    private static final String DRL_PATH_NO_LOOP_DISABLED = "no-loop-disabled.drl";
    private static final String DRL_PATH_NO_LOOP_DISABLED_NO_MODIFY = "no-loop-disabled-no-modify.drl";
    private static final String DRL_PATH_NO_LOOP_ENABLED = "no-loop-enabled.drl";

    /**
     * Tests the rules included in the file dialect.drl.
     * 
     * @see: http://mvel.codehaus.org/MVEL+2.0+Block+WITH+Operator
     */
    @Test
    public void testDialect() {
        List<Person> adults = new ArrayList<Person>();
        List<Gift> gifts = new ArrayList<Gift>();
        // Create the Stateless Session
        StatelessKieSession session = TestUtil.createStatelessKieSession(DRL_PATH_DIALECT);
        // Add Global Variables
        session.setGlobal("adults", adults);
        session.setGlobal("gifts", gifts);

        Person child = new Person(JOHN, 4);
        Person adult = new Person("Peter", 22);

        // Execute the StatelessSession
        session.execute(Arrays.asList(child, adult));

        // Assert Java rule
        Assert.assertEquals(1, adults.size());
        Assert.assertEquals(adult, adults.get(0));

        // Asert MVEL rule
        Assert.assertEquals(1, gifts.size());
        Assert.assertEquals(JOHN, gifts.get(0).getRecipient());
    }

    /**
     * Tests the rules included in the file date-effective.drl.<br />
     * <b>Note:</b>in Drools the date format dd-mmm-yyyy is supported by
     * default. It can be overridden by the system property drools.dateformat
     */
    @Test
    public void testDateEffective() {
        List<Gift> gifts = new ArrayList<Gift>();
        // Create the Stateless Session
        StatelessKieSession session = TestUtil.createStatelessKieSession(DRL_PATH_DATE_EFFECTIVE);
        // Add Global Variables
        session.setGlobal("gifts", gifts);

        // Insert one Person, so the rules conditions are met
        Person john = new Person(JOHN, 30);

        // Execute the StatelessSession
        session.execute(john);

        // Assert that the only gift given are the Chocolates, so only 1 rule
        // was triggered
        Assert.assertEquals(1, gifts.size());
        Gift chocolates = gifts.get(0);
        Assert.assertEquals(CHOCOLATES, chocolates.getDescription());
        Assert.assertEquals(john.getName(), chocolates.getRecipient());
    }

    /**
     * Tests the rules included in the file date-expires.drl.<br />
     * <b>Note:</b>in Drools the date format dd-mmm-yyyy is supported by
     * default. It can be overridden by the system property drools.dateformat
     */
    @Test
    public void testDateExpires() {
        List<Gift> gifts = new ArrayList<Gift>();
        // Create the Stateless Session
        StatelessKieSession session = TestUtil.createStatelessKieSession(DRL_PATH_DATE_EXPIRES);
        // Add Global Variables
        session.setGlobal("gifts", gifts);

        // Insert one Person, so the rules conditions are met
        Person john = new Person(JOHN, 30);

        // Execute the StatelessSession
        session.execute(john);

        // Assert that the only gift given are the Fruits
        Assert.assertEquals(1, gifts.size());
        Gift fruits = gifts.get(0);
        Assert.assertEquals("Fruits", fruits.getDescription());
        Assert.assertEquals(john.getName(), fruits.getRecipient());
    }

    /**
     * Tests the rules included in the file duration.drl<br />
     * A Stateful Session with Pseudo clock is used, in order to emulate the
     * flow of time.
     * 
     * @throws InterruptedException
     */
    @Test
    public void testDuration() throws InterruptedException {
        // Create the Stateful Session
        KieSession session = TestUtil.createKieSession(true, DRL_PATH_DURATION);
        List<Gift> gifts = new ArrayList<Gift>();
        try {
            // Add Global Variables
            session.setGlobal("gifts", gifts);

            // Insert one Person, so the rules conditions are met
            Person john = new Person(JOHN, 30);

            // Retrieve the pseudo clock so we can advance time for testing the
            // duration attribute
            SessionPseudoClock clock = session.getSessionClock();

            // Execute the Stateful Session
            session.insert(john);

            session.fireAllRules();

            // Assert that the rule did NOT get fired
            Assert.assertEquals(0, gifts.size());

            clock.advanceTime(5, TimeUnit.SECONDS);

            session.fireAllRules();

            // Assert that the rule DID get fired
            Assert.assertEquals(1, gifts.size());
            Gift chocolates = gifts.get(0);
            Assert.assertEquals(CHOCOLATES, chocolates.getDescription());
            Assert.assertEquals(john.getName(), chocolates.getRecipient());
        } finally {
            if (session != null) {
                session.dispose();
            }
        }
    }

    /**
     * Tests the rules included in the file duration.drl<br />
     * A Stateful Session with Pseudo clock is used, in order to emulate the
     * flow of time.
     */
    @Test
    public void testDuration_NotMet() {
        List<Gift> gifts = new ArrayList<Gift>();
        // Create the Stateful Session
        KieSession session = TestUtil.createKieSession(true, DRL_PATH_DURATION);
        try {
            // Add Global Variables
            session.setGlobal("gifts", gifts);

            // Insert one Person, so the rules conditions are met
            Person john = new Person(JOHN, 30);

            // Retrieve the pseudo clock so we can advance time for testing the
            // duration attribute
            SessionPseudoClock clock = session.getSessionClock();

            // Insert the Object and Fire all rules
            FactHandle johnFactHandle = session.insert(john);
            session.fireAllRules();

            clock.advanceTime(4, TimeUnit.SECONDS);

            // Remove the object from the memory
            session.delete(johnFactHandle);

            clock.advanceTime(1, TimeUnit.SECONDS);

            // Let's fire rules again after 5s (duration of the rule)
            session.fireAllRules();

            // Assert that the rule did NOT get fired
            Assert.assertEquals(0, gifts.size());
        } finally {
            if (session != null) {
                session.dispose();
            }
        }
    }

    /**
     * Tests the rules included in the file salience.drl<br />
     */
    @Test
    public void testSalience() {
        List<String> messages = new ArrayList<String>();
        // Create the Stateless Session
        StatelessKieSession session = TestUtil.createStatelessKieSession(DRL_PATH_SALIENCE);
        // Add Global Variables
        session.setGlobal("messages", messages);

        session.execute("stringFact");

        // Assert that the rule did NOT get fired
        Assert.assertEquals(3, messages.size());
        Assert.assertEquals(FIRST, messages.get(0));
        Assert.assertEquals(SECOND, messages.get(1));
        Assert.assertEquals(THIRD, messages.get(2));
    }

    /**
     * Tests the rules included in the file noloop-disabled-no-modify.drl, which
     * does not use the modify clause. Note that the engine does not get
     * notified of the modification and because of this the rule is not
     * evaluated again.<br />
     * 
     */
    @Test
    public void testNoLoop_Disabled_NoModify() {
        // Create the Stateless Session
        StatelessKieSession session = TestUtil.createStatelessKieSession(DRL_PATH_NO_LOOP_DISABLED_NO_MODIFY);
        LoopCountAgendaEventListener listener = new LoopCountAgendaEventListener();
        session.addEventListener(listener);
        session.setGlobal("logger", logger);
        Gift chocolateForJohn = new Gift(CHOCOLATES, JOHN);
        session.execute(chocolateForJohn);

        // Assert that the rule ran more than one time
        Assert.assertEquals(1, listener.getMatchCount());

        // Assert that the Gift object was modified by the rule
        Assert.assertEquals(ME, chocolateForJohn.getRecipient());
    }

    /**
     * Tests the rules included in the file noloop-disabled.drl, using a
     * Stateless Session<br />
     * 
     * @throws InterruptedException
     */
    @Test
    public void testNoLoop_Disabled_Stateless() throws InterruptedException {
        // Create the Stateless Session
        final StatelessKieSession session = TestUtil.createStatelessKieSession(DRL_PATH_NO_LOOP_DISABLED);
        LoopCountAgendaEventListener listener = new LoopCountAgendaEventListener();
        session.addEventListener(listener);
        session.setGlobal("logger", logger);
        final Gift chocolateForJohn = new Gift(CHOCOLATES, JOHN);

        // Fire the rules in a separate thread, which will be stacked in an
        // infinite loop firing itself
        Thread executeSessionThread = new Thread() {
            @Override
            public void run() {
                session.execute(chocolateForJohn);
            }
        };
        executeSessionThread.start();

        Thread.sleep(2 * 1000); // Give time to the rule to run in an infinite
                                // loop

        // Assert that the rule ran more than one time
        Assert.assertTrue(listener.getMatchCount() > 1);

        // Assert that the Gift object was modified by the rule
        Assert.assertEquals(ME, chocolateForJohn.getRecipient());
    }

    /**
     * Tests the rules included in the file noloop-disabled.drl, using a
     * Stateful Session<br />
     * 
     * @throws InterruptedException
     */
    @Test
    public void testNoLoop_Disabled_Stateful() throws InterruptedException {
        // Create the Stateful Session
        final KieSession session = TestUtil.createKieSession(DRL_PATH_NO_LOOP_DISABLED);
        try {
            LoopCountAgendaEventListener listener = new LoopCountAgendaEventListener();
            session.addEventListener(listener);
            session.setGlobal("logger", logger);
            final Gift chocolateForJohn = new Gift(CHOCOLATES, JOHN);
            session.insert(chocolateForJohn);

            // Fire the rules in a separate thread, which will be stacked in an
            // infinite loop firing itself
            Thread executeSessionThread = new Thread() {
                @Override
                public void run() {
                    session.fireAllRules();
                }
            };
            executeSessionThread.start();

            Thread.sleep(2 * 1000); // Give time to the rule to run in an
                                    // infinite loop

            // Assert that the rule ran more than one time
            Assert.assertTrue(listener.getMatchCount() > 1);

            // Assert that the Gift object was modified by the rule
            Assert.assertEquals(ME, chocolateForJohn.getRecipient());
        } finally {
            if (session != null) {
                session.dispose();
            }
        }
    }

    /**
     * Tests the rules included in the file noloop-enabled.drl, using a
     * Stateless Session<br />
     */
    @Test
    public void testNoLoop_Enabled_Stateless() {
        // Create the Stateless Session
        final StatelessKieSession session = TestUtil.createStatelessKieSession(DRL_PATH_NO_LOOP_ENABLED);
        LoopCountAgendaEventListener listener = new LoopCountAgendaEventListener();
        session.addEventListener(listener);
        session.setGlobal("logger", logger);
        final Gift chocolateForJohn = new Gift(CHOCOLATES, JOHN);
        session.execute(chocolateForJohn);

        // Assert that the rule only fired once
        Assert.assertEquals(1, listener.getMatchCount());

        // Assert that the Gift object was modified by the rule
        Assert.assertEquals(ME, chocolateForJohn.getRecipient());
    }

    /**
     * Tests the rules included in the file noloop-enabled.drl, using a Stateful
     * session<br />
     */
    @Test
    public void testNoLoop_Enabled_Stateful() {
        // Create the Stateful Session
        final KieSession session = TestUtil.createKieSession(DRL_PATH_NO_LOOP_ENABLED);
        try {
            LoopCountAgendaEventListener listener = new LoopCountAgendaEventListener();
            session.addEventListener(listener);
            session.setGlobal("logger", logger);
            final Gift chocolateForJohn = new Gift(CHOCOLATES, JOHN);
            session.insert(chocolateForJohn);
            session.fireAllRules();

            // Assert that the rule only fired once
            Assert.assertEquals(1, listener.getMatchCount());

            // Assert that the Gift object was modified by the rule
            Assert.assertEquals(ME, chocolateForJohn.getRecipient());
        } finally {
            if (session != null) {
                session.dispose();
            }
        }
    }

}

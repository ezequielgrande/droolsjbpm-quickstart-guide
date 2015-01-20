package com.wordpress.ezegrande.drools.attributes;

import org.kie.api.event.rule.AfterMatchFiredEvent;
import org.kie.api.event.rule.AgendaEventListener;
import org.kie.api.event.rule.AgendaGroupPoppedEvent;
import org.kie.api.event.rule.AgendaGroupPushedEvent;
import org.kie.api.event.rule.BeforeMatchFiredEvent;
import org.kie.api.event.rule.MatchCancelledEvent;
import org.kie.api.event.rule.MatchCreatedEvent;
import org.kie.api.event.rule.RuleFlowGroupActivatedEvent;
import org.kie.api.event.rule.RuleFlowGroupDeactivatedEvent;

/**
 * This listener keeps a count of the Match Created Events in the Agenda. It is
 * used to know how many times a match was created. Note that there will be only
 * one rule, so we do not need to identify which rule was activated during the
 * Match. In case that you need it, you can call these methods:
 * event.getMatch().getRule().getName()
 * 
 */
public class LoopCountAgendaEventListener implements AgendaEventListener {
    private int matchCount = 0;

    public int getMatchCount() {
        return matchCount;
    }

    public void matchCreated(MatchCreatedEvent event) {
        matchCount++;
    }

    public void matchCancelled(MatchCancelledEvent event) {
        matchCount--;
    }

    public void beforeMatchFired(BeforeMatchFiredEvent event) {
    }

    public void afterMatchFired(AfterMatchFiredEvent event) {
    }

    public void agendaGroupPopped(AgendaGroupPoppedEvent event) {
    }

    public void agendaGroupPushed(AgendaGroupPushedEvent event) {
    }

    public void beforeRuleFlowGroupActivated(RuleFlowGroupActivatedEvent event) {
    }

    public void afterRuleFlowGroupActivated(RuleFlowGroupActivatedEvent event) {
    }

    public void beforeRuleFlowGroupDeactivated(RuleFlowGroupDeactivatedEvent event) {
    }

    public void afterRuleFlowGroupDeactivated(RuleFlowGroupDeactivatedEvent event) {
    }
}

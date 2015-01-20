package com.wordpress.ezegrande.drools.attributes.model;

public class Gift {
    private String description;
    private String recipient;
    
    public Gift() {
        // Used from the mvel example
    }
    
    public Gift(String description, String recipient) {
        this.description = description;
        this.recipient = recipient;
    }
    
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getRecipient() {
        return recipient;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    public String toString() {
        return description + " for " + recipient;
    }
}

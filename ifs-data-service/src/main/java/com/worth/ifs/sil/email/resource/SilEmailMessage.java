package com.worth.ifs.sil.email.resource;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.List;

/**
 * The Email message envelope for the SIL API
 */
public class SilEmailMessage {

    @JsonProperty("From")
    private SilEmailAddress from;

    @JsonProperty("ToRecipients")
    private List<SilEmailAddress> to;

    @JsonProperty("Subject")
    private String subject;

    @JsonProperty("Body")
    private SilEmailBody body;

    public SilEmailMessage(SilEmailAddress from, List<SilEmailAddress> to, String subject, SilEmailBody body) {
        this.from = from;
        this.to = to;
        this.subject = subject;
        this.body = body;
    }

    public SilEmailAddress getFrom() {
        return from;
    }

    public List<SilEmailAddress> getTo() {
        return to;
    }

    public String getSubject() {
        return subject;
    }

    public SilEmailBody getBody() {
        return body;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        SilEmailMessage that = (SilEmailMessage) o;

        return new EqualsBuilder()
                .append(from, that.from)
                .append(to, that.to)
                .append(subject, that.subject)
                .append(body, that.body)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(from)
                .append(to)
                .append(subject)
                .append(body)
                .toHashCode();
    }
}

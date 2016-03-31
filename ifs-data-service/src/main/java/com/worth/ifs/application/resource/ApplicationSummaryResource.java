package com.worth.ifs.application.resource;

import java.math.BigDecimal;

/**
 * Represents a high-level overview of an application.
 */
public class ApplicationSummaryResource {
    private Long id;
    private String name;
    private String lead;
    private Long applicationStatus;
    private String applicationStatusName;
    private BigDecimal completedPercentage;
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getLead() {
		return lead;
	}
	public void setLead(String lead) {
		this.lead = lead;
	}
	public Long getApplicationStatus() {
		return applicationStatus;
	}
	public void setApplicationStatus(Long applicationStatus) {
		this.applicationStatus = applicationStatus;
	}
	public String getApplicationStatusName() {
		return applicationStatusName;
	}
	public void setApplicationStatusName(String applicationStatusName) {
		this.applicationStatusName = applicationStatusName;
	}
	public BigDecimal getCompletedPercentage() {
		return completedPercentage;
	}
	public void setCompletedPercentage(BigDecimal completedPercentage) {
		this.completedPercentage = completedPercentage;
	}
	
    
}
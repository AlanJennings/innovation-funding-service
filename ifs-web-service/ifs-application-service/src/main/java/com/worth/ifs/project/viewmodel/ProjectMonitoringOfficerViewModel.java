package com.worth.ifs.project.viewmodel;

import com.worth.ifs.address.resource.AddressResource;

import java.time.LocalDate;
import java.util.List;

/**
 * View model to back the Monitoring Officer page
 */
public class ProjectMonitoringOfficerViewModel {

    private String projectTitle;
    private String area;
    private AddressResource primaryAddress;
    private LocalDate targetProjectStartDate;
    private String projectManagerName;
    private List<String> partnerOrganisationNames;

    public ProjectMonitoringOfficerViewModel(String projectTitle, String area, AddressResource primaryAddress, LocalDate targetProjectStartDate, String projectManagerName, List<String> partnerOrganisationNames) {
        this.projectTitle = projectTitle;
        this.area = area;
        this.primaryAddress = primaryAddress;
        this.targetProjectStartDate = targetProjectStartDate;
        this.projectManagerName = projectManagerName;
        this.partnerOrganisationNames = partnerOrganisationNames;
    }

    public String getProjectTitle() {
        return projectTitle;
    }

    public String getArea() {
        return area;
    }

    public AddressResource getPrimaryAddress() {
        return primaryAddress;
    }

    public LocalDate getTargetProjectStartDate() {
        return targetProjectStartDate;
    }

    public String getProjectManagerName() {
        return projectManagerName;
    }

    public List<String> getPartnerOrganisationNames() {
        return partnerOrganisationNames;
    }
}

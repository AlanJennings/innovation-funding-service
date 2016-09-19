package com.worth.ifs.project.transactional;

import com.worth.ifs.bankdetails.domain.BankDetails;
import com.worth.ifs.commons.service.ServiceResult;
import com.worth.ifs.competition.domain.Competition;
import com.worth.ifs.competition.repository.CompetitionRepository;
import com.worth.ifs.project.constant.ProjectActivityStates;
import com.worth.ifs.project.domain.Project;
import com.worth.ifs.project.finance.domain.SpendProfile;
import com.worth.ifs.project.status.resource.CompetitionProjectsStatusResource;
import com.worth.ifs.project.status.resource.ProjectStatusResource;
import com.worth.ifs.user.domain.Organisation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.worth.ifs.project.constant.ProjectActivityStates.*;

@Service
public class ProjectStatusServiceImpl extends AbstractProjectServiceImpl implements ProjectStatusService {

    @Autowired
    private CompetitionRepository competitionRepository;

    @Override
    public ServiceResult<CompetitionProjectsStatusResource> getCompetitionStatus(Long competitionId) {
        Competition competition = competitionRepository.findOne(competitionId);

        List<Project> projects = projectRepository.findByApplicationCompetitionId(competitionId);

        List<ProjectStatusResource> projectStatusResources = projects.stream().map(project -> {
            ProjectActivityStates projectDetailsStatus = getProjectDetailsStatus(project);
            return new ProjectStatusResource(
                        project.getName(),
                        project.getId() + "",
                        getProjectPartnerCount(project.getId()),
                        project.getApplication().getLeadOrganisation().getName(),
                        projectDetailsStatus,
                        getBankDetailsStatus(project),
                        getFinanceChecksStatus(project),
                        getSpendProfileStatus(project),
                        getMonitoringOfficerStatus(project, projectDetailsStatus),
                        getOtherDocumentsStatus(project),
                        getGrantOfferLetterStatus(project));
        }).collect(Collectors.toList());


        CompetitionProjectsStatusResource competitionProjectsStatusResource = new CompetitionProjectsStatusResource(competition.getId() + "",competition.getName(), projectStatusResources);

        return ServiceResult.serviceSuccess(competitionProjectsStatusResource);
    }

    private Integer getProjectPartnerCount(Long projectId){
        return getProjectUsersByProjectId(projectId).size();
    }

    private ProjectActivityStates getProjectDetailsStatus(Project project){
        return createProjectDetailsStatus(project);
    }

    private ProjectActivityStates getBankDetailsStatus(Project project){
        List<Organisation> organisations = project.getOrganisations();
        for(Organisation organisation : organisations){
            Optional<BankDetails> bankDetails = Optional.ofNullable(bankDetailsRepository.findByProjectIdAndOrganisationId(project.getId(), organisation.getId()));
            ProjectActivityStates organisationBankDetailsStatus = createBankDetailStatus(project, bankDetails, organisation);
            if(organisationBankDetailsStatus != NOT_REQUIRED && organisationBankDetailsStatus != COMPLETE){
                return PENDING;
            }
        }
        return COMPLETE;
    }

    private ProjectActivityStates getFinanceChecksStatus(Project project){
        return ACTION_REQUIRED;

    }

    private ProjectActivityStates getSpendProfileStatus(Project project){
        List<Organisation> organisations = project.getOrganisations();

        for(Organisation organisation : organisations) {
            Optional<SpendProfile> spendProfile = Optional.ofNullable(spendProfileRepository.findOneByProjectIdAndOrganisationId(project.getId(), organisation.getId()));

            ProjectActivityStates financeChecksStatus = ACTION_REQUIRED;
            if (spendProfile.isPresent()) {
                if(!financeChecksStatus.equals(COMPLETE)){
                    return NOT_STARTED;
                } else {
                    ProjectActivityStates orgSPStatus = createSpendProfileStatus(financeChecksStatus, spendProfile);
                    if (orgSPStatus != COMPLETE) {
                        return PENDING;
                    }
                }
            }
        }

        return NOT_STARTED;
    }

    private ProjectActivityStates getMonitoringOfficerStatus(Project project, ProjectActivityStates projectDetailsStatus){
        return createMonitoringOfficerStatus(getExistingMonitoringOfficerForProject(project.getId()).getOptionalSuccessObject(), projectDetailsStatus);
    }

    private ProjectActivityStates getOtherDocumentsStatus(Project project){
        return createOtherDocumentStatus(project);
    }

    private ProjectActivityStates getGrantOfferLetterStatus(Project project){
        return createGrantOfferLetterStatus();
    }
}

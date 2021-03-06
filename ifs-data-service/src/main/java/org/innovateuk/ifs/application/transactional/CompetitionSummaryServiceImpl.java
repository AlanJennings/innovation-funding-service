package org.innovateuk.ifs.application.transactional;

import org.innovateuk.ifs.application.constant.ApplicationStatusConstants;
import org.innovateuk.ifs.application.resource.CompetitionSummaryResource;
import org.innovateuk.ifs.commons.service.ServiceResult;
import org.innovateuk.ifs.competition.domain.Competition;
import org.innovateuk.ifs.invite.domain.CompetitionParticipantRole;
import org.innovateuk.ifs.invite.repository.CompetitionParticipantRepository;
import org.innovateuk.ifs.transactional.BaseTransactionalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

import static org.innovateuk.ifs.application.transactional.ApplicationSummaryServiceImpl.CREATED_AND_OPEN_STATUS_IDS;
import static org.innovateuk.ifs.application.transactional.ApplicationSummaryServiceImpl.SUBMITTED_STATUS_IDS;
import static org.innovateuk.ifs.commons.service.ServiceResult.serviceSuccess;

@Service
public class CompetitionSummaryServiceImpl extends BaseTransactionalService implements CompetitionSummaryService {

    @Autowired
    private CompetitionParticipantRepository competitionParticipantRepository;

    @Override
    public ServiceResult<CompetitionSummaryResource> getCompetitionSummaryByCompetitionId(Long competitionId) {
        Competition competition = competitionRepository.findById(competitionId);
        BigDecimal limit = new BigDecimal(50L);

        CompetitionSummaryResource competitionSummaryResource = new CompetitionSummaryResource();
        competitionSummaryResource.setCompetitionId(competitionId);
        competitionSummaryResource.setCompetitionName(competition.getName());
        competitionSummaryResource.setCompetitionStatus(competition.getCompetitionStatus());
        competitionSummaryResource.setTotalNumberOfApplications(applicationRepository.countByCompetitionId(competitionId));
        competitionSummaryResource.setApplicationsStarted(
                applicationRepository.countByCompetitionIdAndApplicationStatusIdInAndCompletionLessThanEqual(
                        competitionId, CREATED_AND_OPEN_STATUS_IDS, limit
                )
        );
        competitionSummaryResource.setApplicationsInProgress(
                applicationRepository.countByCompetitionIdAndApplicationStatusIdNotInAndCompletionGreaterThan(
                        competitionId, SUBMITTED_STATUS_IDS, limit
                )
        );
        competitionSummaryResource.setApplicationsSubmitted(
                applicationRepository.countByCompetitionIdAndApplicationStatusIdIn(competitionId, SUBMITTED_STATUS_IDS)
        );
        competitionSummaryResource.setApplicationsNotSubmitted(
                competitionSummaryResource.getTotalNumberOfApplications() - competitionSummaryResource.getApplicationsSubmitted()
        );
        competitionSummaryResource.setApplicationDeadline(competition.getEndDate());
        competitionSummaryResource.setApplicationsFunded(
                applicationRepository.countByCompetitionIdAndApplicationStatusId(competitionId, ApplicationStatusConstants.APPROVED.getId())
        );
        competitionSummaryResource.setAssessorsInvited(
                competitionParticipantRepository.countByCompetitionIdAndRole(competitionId, CompetitionParticipantRole.ASSESSOR)
        );
        competitionSummaryResource.setAssessorDeadline(competition.getAssessorDeadlineDate());

        return serviceSuccess(competitionSummaryResource);
    }
}

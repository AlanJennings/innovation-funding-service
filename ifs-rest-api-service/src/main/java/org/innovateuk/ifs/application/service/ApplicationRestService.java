package org.innovateuk.ifs.application.service;

import java.util.List;
import java.util.concurrent.Future;
import org.innovateuk.ifs.application.resource.ApplicationResource;
import org.innovateuk.ifs.commons.rest.RestResult;
import org.innovateuk.ifs.user.resource.UserRoleType;

/**
 * Interface for CRUD operations on {@link ApplicationResource} related data.
 */
public interface ApplicationRestService {
    RestResult<ApplicationResource> getApplicationById(Long applicationId);
    RestResult<List<ApplicationResource>> getApplicationsByUserId(Long userId);
    RestResult<Boolean> isApplicationReadyForSubmit(Long applicationId);
    RestResult<List<ApplicationResource>> getApplicationsByCompetitionIdAndUserId(Long competitionID, Long userId, UserRoleType role);
    RestResult<Void> saveApplication(ApplicationResource application);
    RestResult<ApplicationResource> createApplication(Long competitionId, Long userId, String applicationName);
    RestResult<Void> updateApplicationStatus(Long applicationId, Long statusId);
    Future<RestResult<Double>> getCompleteQuestionsPercentage(Long applicationId);
    RestResult<Integer> getAssignedQuestionsCount(Long applicationId, Long assigneeId);
    RestResult<ApplicationResource> findByProcessRoleId(Long id);
}

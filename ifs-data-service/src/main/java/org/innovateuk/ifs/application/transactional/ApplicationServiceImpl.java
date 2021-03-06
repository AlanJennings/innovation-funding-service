package org.innovateuk.ifs.application.transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.innovateuk.ifs.application.constant.ApplicationStatusConstants;
import org.innovateuk.ifs.application.domain.Application;
import org.innovateuk.ifs.application.domain.ApplicationStatus;
import org.innovateuk.ifs.application.domain.Question;
import org.innovateuk.ifs.application.domain.Section;
import org.innovateuk.ifs.application.mapper.ApplicationMapper;
import org.innovateuk.ifs.application.resource.ApplicationResource;
import org.innovateuk.ifs.application.resource.CompletedPercentageResource;
import org.innovateuk.ifs.application.resource.FormInputResponseFileEntryId;
import org.innovateuk.ifs.application.resource.FormInputResponseFileEntryResource;
import org.innovateuk.ifs.commons.error.Error;
import org.innovateuk.ifs.commons.service.ServiceResult;
import org.innovateuk.ifs.competition.domain.Competition;
import org.innovateuk.ifs.competition.resource.CompetitionStatus;
import org.innovateuk.ifs.email.resource.EmailAddress;
import org.innovateuk.ifs.email.resource.EmailContent;
import org.innovateuk.ifs.file.domain.FileEntry;
import org.innovateuk.ifs.file.resource.FileEntryResource;
import org.innovateuk.ifs.file.resource.FileEntryResourceAssembler;
import org.innovateuk.ifs.file.transactional.FileService;
import org.innovateuk.ifs.finance.handler.ApplicationFinanceHandler;
import org.innovateuk.ifs.form.domain.FormInput;
import org.innovateuk.ifs.form.domain.FormInputResponse;
import org.innovateuk.ifs.form.repository.FormInputRepository;
import org.innovateuk.ifs.form.repository.FormInputResponseRepository;
import org.innovateuk.ifs.notifications.resource.*;
import org.innovateuk.ifs.notifications.service.NotificationService;
import org.innovateuk.ifs.notifications.service.senders.NotificationSender;
import org.innovateuk.ifs.transactional.BaseTransactionalService;
import org.innovateuk.ifs.user.domain.Organisation;
import org.innovateuk.ifs.user.domain.ProcessRole;
import org.innovateuk.ifs.user.domain.Role;
import org.innovateuk.ifs.user.domain.User;
import org.innovateuk.ifs.user.resource.UserRoleType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotNull;
import java.io.File;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.innovateuk.ifs.commons.error.CommonErrors.notFoundError;
import static org.innovateuk.ifs.commons.error.CommonFailureKeys.COMPETITION_NOT_OPEN;
import static org.innovateuk.ifs.commons.error.CommonFailureKeys.FILES_UNABLE_TO_DELETE_FILE;
import static org.innovateuk.ifs.commons.service.ServiceResult.*;
import static org.innovateuk.ifs.notifications.resource.NotificationMedium.EMAIL;
import static org.innovateuk.ifs.user.resource.UserRoleType.LEADAPPLICANT;
import static org.innovateuk.ifs.util.CollectionFunctions.simpleFilter;
import static org.innovateuk.ifs.util.CollectionFunctions.simpleMap;
import static org.innovateuk.ifs.util.EntityLookupCallbacks.find;
import static org.innovateuk.ifs.util.MapFunctions.asMap;
import static org.innovateuk.ifs.util.MathFunctions.percentage;

/**
 * Transactional and secured service focused around the processing of Applications
 */
@Service
public class ApplicationServiceImpl extends BaseTransactionalService implements ApplicationService {
    enum Notifications {
        APPLICATION_SUBMITTED,
        APPLICATION_FUNDED_ASSESSOR_FEEDBACK_PUBLISHED
    }

    private static final Log LOG = LogFactory.getLog(ApplicationServiceImpl.class);

    // TODO DW - INFUND-1555 - put into a DTO
    public static final String READY_FOR_SUBMIT = "readyForSubmit";
    public static final String PROGRESS = "progress";
    public static final String RESEARCH_PARTICIPATION = "researchParticipation";
    public static final String RESEARCH_PARTICIPATION_VALID = "researchParticipationValid";
    public static final String ALL_SECTION_COMPLETE = "allSectionComplete";

    @Autowired
    private FileService fileService;
    @Autowired
    private FormInputResponseRepository formInputResponseRepository;
    @Autowired
    private FormInputRepository formInputRepository;
    @Autowired
    private QuestionService questionService;
    @Autowired
    private ApplicationFinanceHandler applicationFinanceHandler;
    @Autowired
    private SectionService sectionService;
    @Autowired
    private NotificationService notificationService;
    @Autowired
    private SystemNotificationSource systemNotificationSource;
    @Autowired
    private ApplicationMapper applicationMapper;
    @Autowired
    private NotificationSender notificationSender;

    @Override
    public ServiceResult<ApplicationResource> createApplicationByApplicationNameForUserIdAndCompetitionId(String applicationName, Long competitionId, Long userId) {
        return find(user(userId), competition(competitionId)).andOnSuccess((user, competition) -> createApplicationByApplicationNameForUserIdAndCompetitionId(applicationName, user, competition));
    }

    private void generateProcessRolesForApplication(User user, Role role, Application application) {
        List<ProcessRole> usersProcessRoles = processRoleRepository.findByUser(user);
        List<Organisation> usersOrganisations = organisationRepository.findByUsers(user);
        Long userOrganisationId = usersProcessRoles.size() != 0
                ? usersProcessRoles.get(0).getOrganisationId()
                : usersOrganisations.get(0).getId();
        ProcessRole processRole = new ProcessRole(user, application.getId(), role, userOrganisationId);
        processRoleRepository.save(processRole);
        List<ProcessRole> processRoles = new ArrayList<>();
        processRoles.add(processRole);
        application.setProcessRoles(processRoles);
        applicationRepository.save(application);
    }

    private ServiceResult<ApplicationResource> createApplicationByApplicationNameForUserIdAndCompetitionId(String applicationName, User user, Competition competition) {
        Application application = new Application();
        application.setName(applicationName);
        application.setStartDate(null);

        String name = ApplicationStatusConstants.CREATED.getName();

        List<ApplicationStatus> applicationStatusList = applicationStatusRepository.findByName(name);
        ApplicationStatus applicationStatus = applicationStatusList.get(0);
        application.setApplicationStatus(applicationStatus);
        application.setDurationInMonths(3L);
        application.setCompetition(competition);

        return getRole(LEADAPPLICANT).andOnSuccess(role -> {
            Application savedApplication = applicationRepository.save(application);
            generateProcessRolesForApplication(user, role, savedApplication);
            savedApplication = applicationRepository.findOne(savedApplication.getId());
            return serviceSuccess(applicationMapper.mapToResource(savedApplication));
        });
    }

    @Override
    public ServiceResult<FormInputResponseFileEntryResource> createFormInputResponseFileUpload(FormInputResponseFileEntryResource formInputResponseFile, Supplier<InputStream> inputStreamSupplier) {

        long applicationId = formInputResponseFile.getCompoundId().getApplicationId();
        long processRoleId = formInputResponseFile.getCompoundId().getProcessRoleId();
        long formInputId = formInputResponseFile.getCompoundId().getFormInputId();

        return getOpenApplication(applicationId).andOnSuccess(application -> {
            FormInputResponse existingResponse = formInputResponseRepository.findByApplicationIdAndUpdatedByIdAndFormInputId(applicationId, processRoleId, formInputId);

            // Removing and replacing if file already exists here
            if (existingResponse != null && existingResponse.getFileEntry() != null) {
                FormInputResponseFileEntryId formInputResponseFileEntryId = new FormInputResponseFileEntryId(formInputId, applicationId, processRoleId);
                final ServiceResult<FormInputResponse> deleteResult = deleteFormInputResponseFileUpload(formInputResponseFileEntryId);
                ;
                if (deleteResult.isFailure()) {
                    return serviceFailure(new Error(FILES_UNABLE_TO_DELETE_FILE, existingResponse.getFileEntry().getId()));
                }
            }

            return fileService.createFile(formInputResponseFile.getFileEntryResource(), inputStreamSupplier).andOnSuccess(successfulFile ->
                            createFormInputResponseFileUpload(successfulFile, existingResponse, processRoleId, applicationId, formInputId, formInputResponseFile)
            );
        });
    }

    private ServiceResult<FormInputResponseFileEntryResource> createFormInputResponseFileUpload(Pair<File, FileEntry> successfulFile, FormInputResponse existingResponse, long processRoleId, long applicationId, long formInputId, FormInputResponseFileEntryResource formInputResponseFile) {
        FileEntry fileEntry = successfulFile.getValue();

        if (existingResponse != null) {

            existingResponse.setFileEntry(fileEntry);
            formInputResponseRepository.save(existingResponse);
            FormInputResponseFileEntryResource fileEntryResource = new FormInputResponseFileEntryResource(FileEntryResourceAssembler.valueOf(fileEntry), formInputResponseFile.getCompoundId());
            return serviceSuccess(fileEntryResource);

        }

        return find(processRole(processRoleId), () -> getFormInput(formInputId), application(applicationId)).andOnSuccess((processRole, formInput, application) -> {

            FormInputResponse newFormInputResponse = new FormInputResponse(LocalDateTime.now(), fileEntry, processRole, formInput, application);
            formInputResponseRepository.save(newFormInputResponse);
            FormInputResponseFileEntryResource fileEntryResource = new FormInputResponseFileEntryResource(FileEntryResourceAssembler.valueOf(fileEntry), formInputId, applicationId, processRoleId);
            return serviceSuccess(fileEntryResource);
        });
    }

    @Override
    public ServiceResult<Void> updateFormInputResponseFileUpload(FormInputResponseFileEntryResource formInputResponseFile, Supplier<InputStream> inputStreamSupplier) {

        ServiceResult<FormInputResponseFileAndContents> existingFileResult =
                getFormInputResponseFileUpload(formInputResponseFile.getCompoundId());

        return existingFileResult.andOnSuccess(existingFile -> {

            FormInputResponseFileEntryResource existingFormInputResource = existingFile.getFormInputResponseFileEntry();

            FileEntryResource existingFileResource = existingFormInputResource.getFileEntryResource();
            FileEntryResource updatedFileDetails = formInputResponseFile.getFileEntryResource();
            FileEntryResource updatedFileDetailsWithId = new FileEntryResource(existingFileResource.getId(), updatedFileDetails.getName(), updatedFileDetails.getMediaType(), updatedFileDetails.getFilesizeBytes());

            return fileService.updateFile(updatedFileDetailsWithId, inputStreamSupplier).andOnSuccessReturnVoid();
        });
    }

    @Override
    public ServiceResult<FormInputResponse> deleteFormInputResponseFileUpload(FormInputResponseFileEntryId fileEntry) {
        return getOpenApplication(fileEntry.getApplicationId()).andOnSuccess(application -> deleteFormInputResponseFileUploadonGetApplicationAndSuccess(fileEntry));
    }

    private ServiceResult<FormInputResponse> deleteFormInputResponseFileUploadonGetApplicationAndSuccess(FormInputResponseFileEntryId fileEntry) {

            ServiceResult<FormInputResponseFileAndContents> existingFileResult =
                    getFormInputResponseFileUpload(fileEntry);

            return existingFileResult.andOnSuccess(existingFile -> {

                FormInputResponseFileEntryResource formInputFileEntryResource = existingFile.getFormInputResponseFileEntry();
                Long fileEntryId = formInputFileEntryResource.getFileEntryResource().getId();

                FormInput formInput = formInputRepository.findOne(formInputFileEntryResource.getCompoundId().getFormInputId());
                if (formInput != null) {
                    boolean questionHasMultipleStatuses = questionHasMultipleStatuses(formInput);
                    return fileService.deleteFile(fileEntryId).
                            andOnSuccess(deletedFile -> {
                                if (questionHasMultipleStatuses)
                                    return getFormInputResponse(formInputFileEntryResource.getCompoundId());
                                else
                                    return getFormInputResponseForQuestionAssignee(formInputFileEntryResource.getCompoundId());
                            }).
                            andOnSuccess(this::unlinkFileEntryFromFormInputResponse);
                } else {
                    return serviceFailure(notFoundError(FormInput.class, formInputFileEntryResource.getCompoundId().getFormInputId()));
                }
            });
    }

    private boolean questionHasMultipleStatuses(@NotNull FormInput formInput) {
        Question question = formInput.getQuestion();
        return question.hasMultipleStatuses();
    }

    @Override
    public ServiceResult<FormInputResponseFileAndContents> getFormInputResponseFileUpload(FormInputResponseFileEntryId fileEntry) {
        final FormInput formInput = formInputRepository.findOne(fileEntry.getFormInputId());
        if (formInput == null) {
            return serviceFailure(notFoundError(FormInput.class, fileEntry.getFormInputId()));
        }

        boolean hasMultipleStatuses = questionHasMultipleStatuses(formInput);

        ServiceResult<FormInputResponse> formInputResponse;
        if (hasMultipleStatuses) {
            formInputResponse = getFormInputResponse(fileEntry);
        } else {
            formInputResponse = getFormInputResponseForQuestionAssignee(fileEntry);
        }
        return formInputResponse.
                andOnSuccess(fir -> fileService.getFileByFileEntryId(fir.getFileEntry().getId()).
                andOnSuccessReturn(inputStreamSupplier -> {
                    FormInputResponseFileEntryResource formInputResponseFileEntry = formInputResponseFileEntryResource(fir.getFileEntry(), fileEntry);
                    return new FormInputResponseFileAndContents(formInputResponseFileEntry, inputStreamSupplier);
                }));
    }

    private ServiceResult<FormInputResponse> unlinkFileEntryFromFormInputResponse(FormInputResponse formInputResponse) {
        formInputResponse.setFileEntry(null);
        FormInputResponse unlinkedResponse = formInputResponseRepository.save(formInputResponse);
        formInputResponseRepository.delete(formInputResponse);
        return serviceSuccess(unlinkedResponse);
    }

    private ServiceResult<FormInput> getFormInput(long formInputId) {
        return find(formInputRepository.findOne(formInputId), notFoundError(FormInput.class, formInputId));
    }

    private FormInputResponseFileEntryResource formInputResponseFileEntryResource(FileEntry fileEntry, FormInputResponseFileEntryId fileEntryId) {
        FileEntryResource fileEntryResource = FileEntryResourceAssembler.valueOf(fileEntry);
        return new FormInputResponseFileEntryResource(fileEntryResource, fileEntryId.getFormInputId(), fileEntryId.getApplicationId(), fileEntryId.getProcessRoleId());
    }

    private ServiceResult<FormInputResponse> getFormInputResponse(FormInputResponseFileEntryId fileEntry) {
        Error formInputResponseNotFoundError = notFoundError(FormInputResponse.class, fileEntry.getApplicationId(), fileEntry.getProcessRoleId(), fileEntry.getFormInputId());
        return find(formInputResponseRepository.findByApplicationIdAndUpdatedByIdAndFormInputId(
                        fileEntry.getApplicationId(),
                        fileEntry.getProcessRoleId(),
                        fileEntry.getFormInputId()),
                formInputResponseNotFoundError);
    }

    /**
     * Use this method for finding a form input response when a question has single status (shared across application)
     *
     * @param fileEntry - in this case the FormInputResponseFileEntryId will contain the id of person to whom the question is assigned.
     */
    private ServiceResult<FormInputResponse> getFormInputResponseForQuestionAssignee(FormInputResponseFileEntryId fileEntry) {
        Error formInputResponseNotFoundError = notFoundError(FormInputResponse.class, fileEntry.getApplicationId(), fileEntry.getProcessRoleId(), fileEntry.getFormInputId());
        List<FormInputResponse> formInputResponses = formInputResponseRepository.findByApplicationIdAndFormInputId(fileEntry.getApplicationId(), fileEntry.getFormInputId());
        if (formInputResponses != null && !formInputResponses.isEmpty()) {
            return serviceSuccess(formInputResponses.get(0));
        }
        return serviceFailure(formInputResponseNotFoundError);
    }

    @Override
    public ServiceResult<ApplicationResource> getApplicationById(final Long id) {
        return getApplication(id).andOnSuccessReturn(applicationMapper::mapToResource);
    }

    @Override
    public ServiceResult<List<ApplicationResource>> findAll() {
        return serviceSuccess(applicationsToResources(applicationRepository.findAll()));
    }

    @Override
    public ServiceResult<List<ApplicationResource>> findByUserId(final Long userId) {
        return getUser(userId).andOnSuccessReturn(user -> {
            List<ProcessRole> roles = processRoleRepository.findByUser(user);
            List<Application> applications = simpleMap(roles, processRole -> {
                Long appId = processRole.getApplicationId();
                return appId != null ? applicationRepository.findOne(appId) : null;
            });
            return applicationsToResources(applications);
        });
    }

    @Override
    public ServiceResult<ApplicationResource> saveApplicationDetails(final Long id, ApplicationResource application) {
        if (!applicationBelongsToOpenCompetition(id)) {
            return serviceFailure(COMPETITION_NOT_OPEN);
        }

        return getApplication(id).andOnSuccessReturn(existingApplication -> {

            existingApplication.setName(application.getName());
            existingApplication.setDurationInMonths(application.getDurationInMonths());
            existingApplication.setStartDate(application.getStartDate());
            existingApplication.setStateAidAgreed(application.getStateAidAgreed());
            existingApplication.setResubmission(application.getResubmission());
            existingApplication.setPreviousApplicationNumber(application.getPreviousApplicationNumber());
            existingApplication.setPreviousApplicationTitle(application.getPreviousApplicationTitle());

            Application savedApplication = applicationRepository.save(existingApplication);
            return applicationMapper.mapToResource(savedApplication);
        });
    }

    @Override
    public ServiceResult<ApplicationResource> saveApplicationSubmitDateTime(final Long id, LocalDateTime date) {
        return getOpenApplication(id).andOnSuccessReturn(existingApplication -> {
            existingApplication.setSubmittedDate(date);
            Application savedApplication = applicationRepository.save(existingApplication);
            return applicationMapper.mapToResource(savedApplication);
        });
    }

    @Override
    public ServiceResult<ApplicationResource> setApplicationFundingEmailDateTime(final Long applicationId, final LocalDateTime fundingEmailDateTime) {
        return getApplication(applicationId).andOnSuccessReturn(application -> {
            application.setManageFundingEmailDate(fundingEmailDateTime);
            Application savedApplication = applicationRepository.save(application);
            return applicationMapper.mapToResource(savedApplication);
        });
    }

    @Override
    public ServiceResult<CompletedPercentageResource> getProgressPercentageByApplicationId(final Long applicationId) {
        return getApplicationById(applicationId).andOnSuccessReturn(applicationResource -> {
            CompletedPercentageResource resource = new CompletedPercentageResource();
            resource.setCompletedPercentage(applicationResource.getCompletion());
            return resource;
        });
    }

    @Override
    public ServiceResult<ApplicationResource> updateApplicationStatus(final Long id,
                                                                      final Long statusId) {
        return find(application(id), applicationStatus(statusId)).andOnSuccess((application, applicationStatus) -> {
            application.setApplicationStatus(applicationStatus);
            applicationRepository.save(application);
            return serviceSuccess(applicationMapper.mapToResource(application));
        });
    }

    @Override
    public ServiceResult<Void> sendNotificationApplicationSubmitted(Long applicationId) {
        return getApplication(applicationId).andOnSuccess(application -> {
            NotificationSource from = systemNotificationSource;
            NotificationTarget to = new ExternalUserNotificationTarget(application.getLeadApplicant().getName(), application.getLeadApplicant().getEmail());

            Map<String, Object> notificationArguments = new HashMap<>();
            Competition competition = application.getCompetition();

            notificationArguments.put("applicationName", application.getName());
            notificationArguments.put("applicationId", application.getId());
            notificationArguments.put("competitionName", competition.getName());
            notificationArguments.put("assesmentEndDate", competition.getFundersPanelDate());

            Notification notification = new Notification(from, singletonList(to), Notifications.APPLICATION_SUBMITTED, notificationArguments);
            return notificationService.sendNotification(notification, EMAIL);
        });
    }

    @Override
    public ServiceResult<List<ApplicationResource>> getApplicationsByCompetitionIdAndUserId(final Long competitionId,
                                                                                            final Long userId,
                                                                                            final UserRoleType role) {
        List<Application> allApps = applicationRepository.findAll();
        List<Application> filtered = simpleFilter(allApps, app -> app.getCompetition().getId().equals(competitionId) &&
                applicationContainsUserRole(app.getProcessRoles(), userId, role));
        List<ApplicationResource> filteredResource = applicationsToResources(filtered);
        return serviceSuccess(filteredResource);
    }

    private static boolean applicationContainsUserRole(List<ProcessRole> roles, final Long userId, UserRoleType role) {
        boolean contains = false;
        int i = 0;
        while (!contains && i < roles.size()) {
            contains = roles.get(i).getUser().getId().equals(userId) && roles.get(i).getRole().getName().equals(role.getName());
            i++;
        }

        return contains;
    }

    @Override
    public ServiceResult<ApplicationResource> findByProcessRole(final Long id) {
        return getProcessRole(id).andOnSuccessReturn(processRole -> {
            Long appId = processRole.getApplicationId();
            Application application = applicationRepository.findOne(appId);
            return applicationMapper.mapToResource(application);
        });
    }

    // TODO DW - INFUND-1555 - try to remove the usage of ObjectNode
    @Override
    public ServiceResult<ObjectNode> applicationReadyForSubmit(Long id) {
        return find(application(id), () -> getProgressPercentageBigDecimalByApplicationId(id)).andOnSuccess((application, progressPercentage) ->
                        sectionService.childSectionsAreCompleteForAllOrganisations(null, id, null).andOnSuccessReturn(allSectionsComplete -> {
                            Competition competition = application.getCompetition();
                            BigDecimal researchParticipation = applicationFinanceHandler.getResearchParticipationPercentage(id);

                            boolean readyForSubmit = false;
                            if (allSectionsComplete &&
                                    progressPercentage.compareTo(BigDecimal.valueOf(100)) == 0 &&
                                    researchParticipation.compareTo(BigDecimal.valueOf(competition.getMaxResearchRatio())) < 0) {
                                readyForSubmit = true;
                            }

                            ObjectMapper mapper = new ObjectMapper();
                            ObjectNode node = mapper.createObjectNode();
                            node.put(READY_FOR_SUBMIT, readyForSubmit);
                            node.put(PROGRESS, progressPercentage);
                            node.put(RESEARCH_PARTICIPATION, researchParticipation);
                            node.put(RESEARCH_PARTICIPATION_VALID, researchParticipation.compareTo(BigDecimal.valueOf(competition.getMaxResearchRatio())) < 0);
                            node.put(ALL_SECTION_COMPLETE, allSectionsComplete);
                            return node;
                        })
        );
    }

    @Override
    public ServiceResult<List<Application>> getApplicationsByCompetitionIdAndStatus(Long competitionId, Collection<Long> applicationStatusId) {
        List<Application> applicationResults = applicationRepository.findByCompetitionIdAndApplicationStatusIdIn(competitionId, applicationStatusId);
        return serviceSuccess(applicationResults);
    }

    // TODO DW - INFUND-1555 - deal with rest results
    @Override
    public ServiceResult<BigDecimal> getProgressPercentageBigDecimalByApplicationId(final Long applicationId) {
        return getApplication(applicationId).andOnSuccessReturn(this::progressPercentageForApplication);
    }

    public ServiceResult<Void> notifyApplicantsByCompetition(Long competitionId) {
        List<ProcessRole> applicants = applicationRepository.findByCompetitionId(competitionId)
                .stream()
                .flatMap(x -> x.getProcessRoles().stream())
                .filter(ProcessRole::isLeadApplicantOrCollaborator)
                .collect(toList());

        return processAnyFailuresOrSucceed(applicants
                .stream()
                .map(this::sendNotification)
                .collect(toList()));
    }

    private ServiceResult<List<EmailAddress>> sendNotification(ProcessRole processRole) {
        Application application = applicationRepository.findOne(processRole.getApplicationId());

        NotificationTarget recipient =
                new ExternalUserNotificationTarget(processRole.getUser().getName(), processRole.getUser().getEmail());

        Notification notification = new Notification(
                systemNotificationSource,
                singletonList(recipient),
                Notifications.APPLICATION_FUNDED_ASSESSOR_FEEDBACK_PUBLISHED,
                asMap("name", processRole.getUser().getName(),
                        "applicationName", application.getName(),
                        "competitionName", application.getCompetition().getName(),
                        "dashboardUrl", processRole.getRole().getUrl()));

        EmailContent content = notificationSender.renderTemplates(notification).getSuccessObject().get(recipient);

        return notificationSender.sendEmailWithContent(notification, recipient, content);
    }

    private BigDecimal progressPercentageForApplication(Application application) {
        List<Section> sections = application.getCompetition().getSections();

        List<Question> questions = sections.stream()
                .flatMap(section -> section.getQuestions().stream())
                .filter(Question::isMarkAsCompletedEnabled)
                .collect(toList());

        List<ProcessRole> processRoles = application.getProcessRoles();

        Set<Organisation> organisations = processRoles.stream()
                .filter(p -> p.getRole().getName().equals(LEADAPPLICANT.getName()) 
					|| p.getRole().getName().equals(UserRoleType.APPLICANT.getName()) 
					|| p.getRole().getName().equals(UserRoleType.COLLABORATOR.getName()))
                .map(processRole -> {
                    return organisationRepository.findOne(processRole.getOrganisationId());
                }).collect(Collectors.toSet());

        Long countMultipleStatusQuestionsCompleted = organisations.stream()
                .mapToLong(org -> questions.stream()
                        .filter(Question::getMarkAsCompletedEnabled)
                        .filter(q -> q.hasMultipleStatuses() && questionService.isMarkedAsComplete(q, application.getId(), org.getId()).getSuccessObject()).count())
                .sum();

        Long countSingleStatusQuestionsCompleted = questions.stream()
                .filter(Question::getMarkAsCompletedEnabled)
                .filter(q -> !q.hasMultipleStatuses() && questionService.isMarkedAsComplete(q, application.getId(), 0L).getSuccessObject()).count();

        Long countCompleted = countMultipleStatusQuestionsCompleted + countSingleStatusQuestionsCompleted;

        Long totalMultipleStatusQuestions = questions.stream().filter(Question::hasMultipleStatuses).count() * organisations.size();
        Long totalSingleStatusQuestions = questions.stream().filter(q -> !q.hasMultipleStatuses()).count();

        Long totalQuestions = totalMultipleStatusQuestions + totalSingleStatusQuestions;

        return percentage(countCompleted, totalQuestions);
    }

    private List<ApplicationResource> applicationsToResources(List<Application> filtered) {
        return simpleMap(filtered, application -> applicationMapper.mapToResource(application));
    }

    private boolean applicationBelongsToOpenCompetition(Long applicationId) {
        Application application = applicationRepository.findOne(applicationId);
        if (application != null && application.getCompetition() != null) {
            return CompetitionStatus.OPEN.equals(application.getCompetition().getCompetitionStatus());
        }
        return true;
    }
}

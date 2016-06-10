package com.worth.ifs.form.transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Supplier;

import com.worth.ifs.application.domain.Application;
import com.worth.ifs.commons.service.ServiceResult;
import com.worth.ifs.competition.resource.CompetitionResource;
import com.worth.ifs.form.domain.FormInput;
import com.worth.ifs.form.domain.FormInputResponse;
import com.worth.ifs.form.domain.FormInputType;
import com.worth.ifs.form.mapper.FormInputMapper;
import com.worth.ifs.form.mapper.FormInputResponseMapper;
import com.worth.ifs.form.mapper.FormInputTypeMapper;
import com.worth.ifs.form.repository.FormInputRepository;
import com.worth.ifs.form.repository.FormInputResponseRepository;
import com.worth.ifs.form.repository.FormInputTypeRepository;
import com.worth.ifs.form.resource.FormInputResource;
import com.worth.ifs.form.resource.FormInputResponseCommand;
import com.worth.ifs.form.resource.FormInputResponseResource;
import com.worth.ifs.form.resource.FormInputTypeResource;
import com.worth.ifs.transactional.BaseTransactionalService;
import com.worth.ifs.user.domain.ProcessRole;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.worth.ifs.commons.error.CommonErrors.notFoundError;
import static com.worth.ifs.commons.error.CommonFailureKeys.COMPETITION_NOT_OPEN;
import static com.worth.ifs.commons.service.ServiceResult.serviceFailure;
import static com.worth.ifs.commons.service.ServiceResult.serviceSuccess;
import static com.worth.ifs.util.CollectionFunctions.simpleMap;
import static com.worth.ifs.util.EntityLookupCallbacks.find;

@Service
public class FormInputServiceImpl extends BaseTransactionalService implements FormInputService {

    @Autowired
    private FormInputRepository formInputRepository;

    @Autowired
    private FormInputTypeRepository formInputTypeRepository;

    @Autowired
    private FormInputResponseRepository formInputResponseRepository;

    @Autowired
    private FormInputTypeMapper formInputTypeMapper;

    @Autowired
    private FormInputMapper formInputMapper;

    @Autowired
    private FormInputResponseMapper formInputResponseMapper;

    @Override
    public ServiceResult<FormInputResource> findFormInput(Long id) {
        return findFormInputEntity(id).andOnSuccessReturn(formInputMapper::mapToResource);
    }

    @Override
    public ServiceResult<List<FormInputResource>> findByQuestionId(Long questionId) {
        return serviceSuccess(formInputToResources(formInputRepository.findByQuestionId(questionId)));
    }

    @Override
    public ServiceResult<List<FormInputResource>> findByCompetitionId(Long competitionId) {
        return serviceSuccess(formInputToResources(formInputRepository.findByCompetitionId(competitionId)));
    }

    private ServiceResult<FormInput> findFormInputEntity(Long id) {
        return find(formInputRepository.findOne(id), notFoundError(FormInput.class, id));
    }

    @Override
    public ServiceResult<FormInputTypeResource> findFormInputType(Long id) {
        return find(formInputTypeRepository.findOne(id), notFoundError(FormInputType.class, id)).
                andOnSuccessReturn(formInputTypeMapper::mapToResource);
    }

    @Override
    public ServiceResult<List<FormInputResponseResource>> findResponsesByApplication(final Long applicationId) {
        return serviceSuccess(formInputResponsesToResources(formInputResponseRepository.findByApplicationId(applicationId)));
    }

    @Override
    public ServiceResult<List<FormInputResponseResource>> findResponsesByFormInputIdAndApplicationId(final Long formInputId, final Long applicationId) {
        return serviceSuccess(formInputResponsesToResources(formInputResponseRepository.findByApplicationIdAndFormInputId(applicationId, formInputId)));
    }

    @Override
    public ServiceResult<FormInputResponse> saveQuestionResponse(FormInputResponseCommand formInputResponseCommand) {
        long applicationId = formInputResponseCommand.getApplicationId();
        long formInputId = formInputResponseCommand.getFormInputId();
        String htmlUnescapedValue = formInputResponseCommand.getValue();
        long userId = formInputResponseCommand.getUserId();
        ProcessRole userAppRole = processRoleRepository.findByUserIdAndApplicationId(userId, applicationId);

        Application application = applicationRepository.findOne(applicationId);
        if(!applicationBelongsToOpenCompetition(application)){
            return serviceFailure(COMPETITION_NOT_OPEN);
        }
        return find(user(userId), formInput(formInputId)).
                andOnSuccess((user, formInput) ->
                                getOrCreateResponse(application, formInput, userAppRole).andOnSuccessReturn(response -> {
                                    if (!response.getValue().equals(htmlUnescapedValue)) {
                                        response.setUpdateDate(LocalDateTime.now());
                                        response.setUpdatedBy(userAppRole);
                                    }
                                    response.setValue(htmlUnescapedValue);
                                    formInputResponseRepository.save(response);
                                    return response;
                                })
                );
    }

    private ServiceResult<FormInputResponse> getOrCreateResponse(Application application, FormInput formInput, ProcessRole userAppRole) {

        List<FormInputResponse> existingResponse = formInputResponseRepository.findByApplicationIdAndFormInputId(application.getId(), formInput.getId());

        return existingResponse != null && !existingResponse.isEmpty() ?
                serviceSuccess(existingResponse.get(0)) :
                serviceSuccess(new FormInputResponse(LocalDateTime.now(), "", userAppRole, formInput, application));
    }

    private Supplier<ServiceResult<FormInput>> formInput(Long id) {
        return () -> findFormInputEntity(id);
    }

    private List<FormInputResource> formInputToResources(List<FormInput> filtered) {
        return simpleMap(filtered, formInput -> formInputMapper.mapToResource(formInput));
    }

    private List<FormInputResponseResource> formInputResponsesToResources(List<FormInputResponse> filtered) {
        return simpleMap(filtered, formInputResponse -> formInputResponseMapper.mapToResource(formInputResponse));
    }

    private boolean applicationBelongsToOpenCompetition(final Application application) {
        return application.getCompetition().getCompetitionStatus().equals(CompetitionResource.Status.OPEN);
    }
}
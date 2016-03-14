package com.worth.ifs.application.finance.view.jes;

import com.worth.ifs.Application;
import com.worth.ifs.application.domain.Question;
import com.worth.ifs.application.finance.model.FinanceFormField;
import com.worth.ifs.application.finance.service.CostService;
import com.worth.ifs.application.finance.service.FinanceService;
import com.worth.ifs.application.finance.view.FinanceFormHandler;
import com.worth.ifs.application.finance.view.item.CostHandler;
import com.worth.ifs.application.resource.ApplicationResource;
import com.worth.ifs.application.service.ApplicationService;
import com.worth.ifs.application.service.CompetitionService;
import com.worth.ifs.application.service.QuestionService;
import com.worth.ifs.finance.resource.ApplicationFinanceResource;
import com.worth.ifs.finance.resource.CostFieldResource;
import com.worth.ifs.finance.resource.cost.CostItem;
import com.worth.ifs.finance.resource.cost.CostType;
import com.worth.ifs.form.domain.FormInputType;
import org.apache.commons.lang3.NotImplementedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class JESFinanceFormHandler implements FinanceFormHandler {
    @Autowired
    private CostService costService;

    @Autowired
    private FinanceService financeService;

    @Autowired
    private QuestionService questionService;


    @Override
    public void update(HttpServletRequest request, Long userId, Long applicationId) {
        storeCostItems(request, userId, applicationId);
    }

    private void storeCostItems(HttpServletRequest request, Long userId, Long applicationId) {
        Enumeration<String> parameterNames = request.getParameterNames();
        while(parameterNames.hasMoreElements()) {
            String parameter = parameterNames.nextElement();
            String[] parameterValues = request.getParameterValues(parameter);
            String value = "";

            if(parameterValues.length > 0) {
                value = parameterValues[0];
                storeCost(userId, applicationId, parameter, value);
            }
        }
    }

    @Override
    public void storeCost(Long userId, Long applicationId, String fieldName, String value) {
        if (fieldName != null && value != null) {
            String cleanedFieldName = fieldName;
            if (fieldName.startsWith("cost-")) {
                cleanedFieldName = fieldName.replace("cost-", "");
            } else {
                // only accept cost fields for storage
                return;
            }
            storeField(cleanedFieldName, value, userId, applicationId);
        }
    }

    private void storeField(String fieldName, String value, Long userId, Long applicationId) {
        FinanceFormField financeFormField = getCostFormField(fieldName, value);
        if(financeFormField==null)
            return;

        CostHandler costHandler = new AcademicFinanceHandler();
        Long costFormFieldId = 0L;
        if (financeFormField.getId() != null && !financeFormField.getId().equals("null")) {
            costFormFieldId = Long.parseLong(financeFormField.getId());
        }
        CostItem costItem = costHandler.toCostItem(costFormFieldId, Arrays.asList(financeFormField));
        storeCostItem(costItem, userId, applicationId, financeFormField.getQuestionId());
    }

    private FinanceFormField getCostFormField(String costTypeKey, String value) {
        // check for question id
        String[] keyParts = costTypeKey.split("-");
        if (keyParts.length == 2) {
            Long questionId = getQuestionId(keyParts[1]);
            return new FinanceFormField(costTypeKey, value, keyParts[0], String.valueOf(questionId), keyParts[1], "");
        }
        return null;
    }

    private void storeCostItem(CostItem costItem, Long userId, Long applicationId, String question) {
        if (costItem.getId().equals(0L)) {
            addCostItem(costItem, userId, applicationId, question);
        } else {
            costService.update(costItem);
        }
    }

    private void addCostItem(CostItem costItem, Long userId, Long applicationId, String question) {
        ApplicationFinanceResource applicationFinanceResource = financeService.getApplicationFinanceDetails(userId, applicationId);

        if (question != null && !question.isEmpty()) {
            Long questionId = Long.parseLong(question);
            costService.add(applicationFinanceResource.getId(), questionId, costItem);
        }
    }

    private Long getQuestionId(String costFieldName) {
        Question question = null;
        switch (costFieldName) {
            case "tsb_reference":
                question = questionService.getQuestionByFormInputType("your_finance").getSuccessObject();
                break;
            case "incurred_staff":
                question = questionService.getQuestionByFormInputType("labour").getSuccessObject();
                break;
            case "incurred_travel_subsistence":
                question = questionService.getQuestionByFormInputType("travel").getSuccessObject();
                break;
            case "incurred_other_costs":
                question = questionService.getQuestionByFormInputType("materials").getSuccessObject();
                break;
            case "allocated_investigators":
                question = questionService.getQuestionByFormInputType("labour").getSuccessObject();
                break;
            case "allocated_estates_costs":
                question = questionService.getQuestionByFormInputType("other_costs").getSuccessObject();
                break;
            case "allocated_other_costs":
                question = questionService.getQuestionByFormInputType("other_costs").getSuccessObject();
                break;
            case "indirect_costs":
                question = questionService.getQuestionByFormInputType("overheads").getSuccessObject();
                break;
            case "exceptions_staff":
                question = questionService.getQuestionByFormInputType("labour").getSuccessObject();
                break;
            case "exceptions_other_costs":
                question = questionService.getQuestionByFormInputType("other_costs").getSuccessObject();
                break;
        }
        if (question != null) {
            return question.getId();
        } else {
            return null;
        }
    }

    @Override
    public void updateFinancePosition(Long userId, Long applicationId, String fieldName, String value) {
        // not to be implemented, there are not any finance positions for the JES form
        throw new NotImplementedException("There are not any finance positions for the JES form");
    }

    @Override
    public CostItem addCost(Long applicationId, Long userId, Long questionId) {
        // not to be implemented, can't add extra rows of finance to the JES form
        throw new NotImplementedException("Can't add extra rows of finance to the JES form");
    }
}
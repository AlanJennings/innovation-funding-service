package com.worth.ifs.assessment.model;

import com.worth.ifs.application.resource.ApplicationResource;
import com.worth.ifs.application.resource.QuestionResource;
import com.worth.ifs.application.service.CompetitionService;
import com.worth.ifs.application.service.QuestionService;
import com.worth.ifs.assessment.viewmodel.AssessmentFeedbackApplicationDetailsViewModel;
import com.worth.ifs.competition.resource.CompetitionResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Build the model for the Assessment Feedback Application Details view.
 */
@Component
public class AssessmentFeedbackApplicationDetailsModelPopulator {

    @Autowired
    private CompetitionService competitionService;

    @Autowired
    private QuestionService questionService;

    public AssessmentFeedbackApplicationDetailsViewModel populateModel(final ApplicationResource application, final Long questionId) {
        final CompetitionResource competition = competitionService.getById(application.getCompetition());
        final QuestionResource question = questionService.getById(questionId);
        return new AssessmentFeedbackApplicationDetailsViewModel(competition.getAssessmentDaysLeft(), competition.getAssessmentDaysLeftPercentage(), competition, application, question.getShortName());
    }

}
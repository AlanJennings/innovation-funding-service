package com.worth.ifs.controller;

import com.worth.ifs.domain.*;
import com.worth.ifs.resource.ApplicationFinanceResource;
import com.worth.ifs.security.TokenAuthenticationService;
import com.worth.ifs.security.UserAuthentication;
import com.worth.ifs.service.*;
import org.mockito.Mock;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.method.annotation.ExceptionHandlerMethodResolver;
import org.springframework.web.servlet.mvc.method.annotation.ExceptionHandlerExceptionResolver;
import org.springframework.web.servlet.mvc.method.annotation.ServletInvocableHandlerMethod;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import org.thymeleaf.expression.Lists;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

public class BaseUnitTest {
    public MockMvc mockMvc;
    public User loggedInUser;
    public UserAuthentication loggedInUserAuthentication;

    @Mock
    TokenAuthenticationService tokenAuthenticationService;
    @Mock
    public ResponseService responseService;
    @Mock
    public ApplicationService applicationService;
    @Mock
    public ApplicationFinanceService applicationFinanceService;
    @Mock
    public UserService userService;

    @Mock
    public SectionService sectionService;

    public List<Application> applications;
    public List<Section> sections;
    public Map<Long, Question> questions;
    public Competition competition;

    public InternalResourceViewResolver viewResolver() {
        InternalResourceViewResolver viewResolver = new InternalResourceViewResolver();
        viewResolver.setPrefix("/resources");
        viewResolver.setSuffix(".html");
        return viewResolver;
    }

    public void setup(){
        loggedInUser = new User(1L, "Nico Bijl", "email@email.nl", "test", "tokenABC", "image", new ArrayList());
        loggedInUserAuthentication = new UserAuthentication(loggedInUser);

        applications = new ArrayList<>();
        sections = new ArrayList<>();
        questions = new HashMap<>();
    }

    public void loginDefaultUser(){
        when(tokenAuthenticationService.getAuthentication(any(HttpServletRequest.class))).thenReturn(loggedInUserAuthentication);
    }

    public void setupCompetition(){
        competition = new Competition(1L, "Competition x", "Description afds", LocalDateTime.now().minusDays(2), LocalDateTime.now().plusDays(5));
        sections.add(new Section(1L, competition, null, "Application details", null));
        sections.add(new Section(2L, competition, null, "Scope (Gateway question)", null));
        sections.add(new Section(3L, competition, null, "Business proposition (Q1 - Q4)", null));
        sections.add(new Section(4L, competition, null, "Project approach (Q5 - Q8)", null));
        sections.add(new Section(5L, competition, null, "Funding (Q9 - Q10)", null));
        sections.add(new Section(6L, competition, null, "Finances", null));

        Question q01 = new Question(1L, competition, sections.get(0), "Application details");
        sections.get(0).setQuestions(Arrays.asList(q01));

        Question q20 = new Question(20L, competition, sections.get(2), "1. What is the business opportunity that this project addresses?");
        Question q21 = new Question(21L, competition, sections.get(2), "2. What is the size of the market opportunity that this project might open up?");
        Question q22 = new Question(22L, competition, sections.get(2), "3. How will the results of the project be exploited and disseminated?");
        Question q23 = new Question(23L, competition, sections.get(2), "4. What economic, social and environmental benefits is the project expected to deliver?");
        sections.get(2).setQuestions(Arrays.asList(q20, q21, q22, q23));

        Question q30 = new Question(30L, competition, sections.get(3), "5. What technical approach will be adopted and how will the project be managed?");
        Question q31 = new Question(31L, competition, sections.get(3), "6. What is innovative about this project?");
        Question q32 = new Question(32L, competition, sections.get(3), "7. What are the risks (technical, commercial and environmental) to project success? What is the project's risk management strategy?");
        Question q33 = new Question(33L, competition, sections.get(3), "8. Does the project team have the right skills and experience and access to facilities to deliver the identified benefits?");
        sections.get(3).setQuestions(Arrays.asList(q30, q31, q32, q33));


        ArrayList<Question> questionList = new ArrayList<>();
        for (Section section : sections) {
            List<Question> sectionQuestions = section.getQuestions();
            if(sectionQuestions != null){
                Map<Long, Question> questionsMap =
                        sectionQuestions.stream().collect(Collectors.toMap(Question::getId,
                                Function.identity()));
                questionList.addAll(sectionQuestions);
                questions.putAll(questionsMap);
            }
        }

        competition.setSections(sections);
        competition.setQuestions(questionList);

    }

    public void setupApplicationWithRoles(){
        Application app1 = new Application(1L, "Rovel Additive Manufacturing Process", new ApplicationStatus(1L, "created"));
        Application app2 = new Application(2L, "Providing sustainable childcare", new ApplicationStatus(2L, "submitted"));
        Application app3 = new Application(3L, "Mobile Phone Data for Logistics Analytics", new ApplicationStatus(3L, "approved"));
        Application app4 = new Application(4L, "Using natural gas to heat homes", new ApplicationStatus(4L, "rejected"));
        Role role = new Role(1L, "leadapplicant", null);

        Organisation organisation1 = new Organisation(1L, "Organisation name");

        UserApplicationRole userAppRole1 = new UserApplicationRole(1L, loggedInUser, app1, role, organisation1);
        UserApplicationRole userAppRole2 = new UserApplicationRole(2L, loggedInUser, app2, role, organisation1);
        UserApplicationRole userAppRole3 = new UserApplicationRole(3L, loggedInUser, app3, role, organisation1);
        UserApplicationRole userAppRole4 = new UserApplicationRole(4L, loggedInUser, app4, role, organisation1);

        competition.addApplication(app1, app2, app3, app4);

        app1.setCompetition(competition);
        app1.setUserApplicationRoles(Arrays.asList(userAppRole1));
        app2.setCompetition(competition);
        app2.setUserApplicationRoles(Arrays.asList(userAppRole2));
        app3.setCompetition(competition);
        app3.setUserApplicationRoles(Arrays.asList(userAppRole3));
        app4.setCompetition(competition);
        app4.setUserApplicationRoles(Arrays.asList(userAppRole4));

        loggedInUser.addUserApplicationRole(userAppRole1, userAppRole2, userAppRole3, userAppRole3);
        applications = Arrays.asList(app1, app2, app3, app4);

        when(sectionService.getCompletedSectionIds(app1.getId())).thenReturn(Arrays.asList(1L, 2L));
        when(sectionService.getIncompletedSectionIds(app1.getId())).thenReturn(Arrays.asList(3L, 4L));
        when(userService.findUserApplicationRole(app1.getId(), loggedInUser.getId())).thenReturn(userAppRole1);
        when(applicationService.getApplicationsByUserId(loggedInUser.getId())).thenReturn(applications);
        when(applicationService.getApplicationById(app1.getId())).thenReturn(app1);
        when(applicationService.getApplicationById(app2.getId())).thenReturn(app2);
        when(applicationService.getApplicationById(app3.getId())).thenReturn(app3);
        when(applicationService.getApplicationById(app4.getId())).thenReturn(app4);

    }

    public void setupApplicationResponses(){
        Application application = applications.get(0);

        Boolean markAsComplete = false;
        UserApplicationRole userApplicationRole = loggedInUser.getUserApplicationRoles().get(0);

        Response response = new Response(1L, LocalDate.now(), "value 1", markAsComplete, userApplicationRole, questions.get(20L), application);
        Response response2 = new Response(2L, LocalDate.now(), "value 1", markAsComplete, userApplicationRole, questions.get(21L), application);

        List<Response> responses = Arrays.asList(response, response2);
        userApplicationRole.setResponses(responses);

        questions.get(20L).setResponses(Arrays.asList(response));
        questions.get(21L).setResponses(Arrays.asList(response2));

        when(responseService.getResponsesByApplicationId(application.getId())).thenReturn(responses);

        //TODO: create representative application finance resource object.
        ApplicationFinanceResource applicationFinanceResource = new ApplicationFinanceResource();
        long organisationId = userApplicationRole.getOrganisation().getId();
        Long applicationId = application.getId();
        when(applicationFinanceService.getApplicationFinance(applicationId, organisationId)).thenReturn(applicationFinanceResource);
    }

    public ExceptionHandlerExceptionResolver createExceptionResolver() {
        ExceptionHandlerExceptionResolver exceptionResolver = new ExceptionHandlerExceptionResolver() {
            protected ServletInvocableHandlerMethod getExceptionHandlerMethod(HandlerMethod handlerMethod, Exception exception) {
                Method method = new ExceptionHandlerMethodResolver(ErrorController.class).resolveMethod(exception);
                return new ServletInvocableHandlerMethod(new ErrorController(), method);
            }
        };
        exceptionResolver.afterPropertiesSet();
        return exceptionResolver;
    }
}

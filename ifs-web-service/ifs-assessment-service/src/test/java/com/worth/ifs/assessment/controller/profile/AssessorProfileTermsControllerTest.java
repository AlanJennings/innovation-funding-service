package com.worth.ifs.assessment.controller.profile;

import com.worth.ifs.BaseControllerMockMVCTest;
import com.worth.ifs.assessment.form.profile.AssessorProfileTermsForm;
import com.worth.ifs.assessment.model.profile.AssessorProfileTermsModelPopulator;
import com.worth.ifs.assessment.viewmodel.profile.AssessorProfileTermsViewModel;
import com.worth.ifs.user.resource.ProfileContractResource;
import com.worth.ifs.user.resource.UserResource;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.validation.BindingResult;

import java.time.LocalDateTime;

import static com.worth.ifs.commons.service.ServiceResult.serviceSuccess;
import static com.worth.ifs.user.builder.ContractResourceBuilder.newContractResource;
import static com.worth.ifs.user.builder.ProfileContractResourceBuilder.newProfileContractResource;
import static com.worth.ifs.user.builder.UserResourceBuilder.newUserResource;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class AssessorProfileTermsControllerTest extends BaseControllerMockMVCTest<AssessorProfileTermsController> {

    @Spy
    @InjectMocks
    private AssessorProfileTermsModelPopulator assessorProfileTermsModelPopulator;

    @Override
    protected AssessorProfileTermsController supplyControllerUnderTest() {
        return new AssessorProfileTermsController();
    }

    @Test
    public void getTerms() throws Exception {
        UserResource user = newUserResource().build();
        setLoggedInUser(user);

        LocalDateTime expectedContractSignedDate = LocalDateTime.now();
        String expectedText = "Contract text...";
        String expectedAnnexOne = "Annex one...";
        String expectedAnnexTwo = "Annex two...";
        String expectedAnnexThree = "Annex three...";

        ProfileContractResource profileContract = newProfileContractResource()
                .withContractSignedDate(expectedContractSignedDate)
                .withCurrentAgreement(true)
                .withContract(newContractResource()
                        .withText(expectedText)
                        .withAnnexOne(expectedAnnexOne)
                        .withAnnexTwo(expectedAnnexTwo)
                        .withAnnexThree(expectedAnnexThree)
                        .build())
                .build();

        when(userService.getProfileContract(user.getId())).thenReturn(profileContract);

        AssessorProfileTermsViewModel expectedViewModel = new AssessorProfileTermsViewModel();
        expectedViewModel.setCurrentAgreement(true);
        expectedViewModel.setContractSignedDate(expectedContractSignedDate);
        expectedViewModel.setText(expectedText);
        expectedViewModel.setAnnexOne(expectedAnnexOne);
        expectedViewModel.setAnnexTwo(expectedAnnexTwo);
        expectedViewModel.setAnnexThree(expectedAnnexThree);

        AssessorProfileTermsForm expectedForm = new AssessorProfileTermsForm();
        expectedForm.setAgreesToTerms(TRUE);

        mockMvc.perform(get("/profile/terms"))
                .andExpect(status().isOk())
                .andExpect(model().hasNoErrors())
                .andExpect(model().attribute("model", expectedViewModel))
                .andExpect(model().attribute("form", expectedForm))
                .andExpect(view().name("profile/terms"));
    }

    @Test
    public void submitTerms() throws Exception {
        UserResource user = newUserResource().build();
        setLoggedInUser(user);

        when(userService.updateProfileContract(user.getId())).thenReturn(serviceSuccess());

        mockMvc.perform(post("/profile/terms")
                .contentType(APPLICATION_FORM_URLENCODED)
                .param("agreesToTerms", "true"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/assessor/dashboard"));

        verify(userService, only()).updateProfileContract(user.getId());
    }

    @Test
    public void submitTerms_invalidForm() throws Exception {
        UserResource user = newUserResource().build();
        setLoggedInUser(user);

        LocalDateTime expectedContractSignedDate = LocalDateTime.now();
        String expectedText = "Contract text...";
        String expectedAnnexOne = "Annex one...";
        String expectedAnnexTwo = "Annex two...";
        String expectedAnnexThree = "Annex three...";

        ProfileContractResource profileContract = newProfileContractResource()
                .withContractSignedDate(expectedContractSignedDate)
                .withCurrentAgreement(true)
                .withContract(newContractResource()
                        .withText(expectedText)
                        .withAnnexOne(expectedAnnexOne)
                        .withAnnexTwo(expectedAnnexTwo)
                        .withAnnexThree(expectedAnnexThree)
                        .build())
                .build();

        when(userService.getProfileContract(user.getId())).thenReturn(profileContract);

        AssessorProfileTermsViewModel expectedViewModel = new AssessorProfileTermsViewModel();
        expectedViewModel.setCurrentAgreement(true);
        expectedViewModel.setContractSignedDate(expectedContractSignedDate);
        expectedViewModel.setText(expectedText);
        expectedViewModel.setAnnexOne(expectedAnnexOne);
        expectedViewModel.setAnnexTwo(expectedAnnexTwo);
        expectedViewModel.setAnnexThree(expectedAnnexThree);

        MvcResult result = mockMvc.perform(post("/profile/terms")
                .contentType(APPLICATION_FORM_URLENCODED)
                .param("agreesToTerms", "false"))
                .andExpect(status().isOk())
                .andExpect(model().hasErrors())
                .andExpect(model().attributeExists("form"))
                .andExpect(model().attribute("model", expectedViewModel))
                .andExpect(model().attributeExists("form"))
                .andExpect(model().attributeHasFieldErrors("form", "agreesToTerms"))
                .andExpect(view().name("profile/terms"))
                .andReturn();

        AssessorProfileTermsForm form = (AssessorProfileTermsForm) result.getModelAndView().getModel().get("form");

        assertEquals(FALSE, form.getAgreesToTerms());

        BindingResult bindingResult = form.getBindingResult();

        assertTrue(bindingResult.hasErrors());
        assertEquals(0, bindingResult.getGlobalErrorCount());
        assertEquals(1, bindingResult.getFieldErrorCount());
        assertTrue(bindingResult.hasFieldErrors("agreesToTerms"));
        assertEquals("Please agree to the terms and conditions", bindingResult.getFieldError("agreesToTerms").getDefaultMessage());

        verify(userService).getProfileContract(user.getId());
        verifyZeroInteractions(userService);
    }
}
package org.innovateuk.ifs.assessment.form.profile.populator;

import org.innovateuk.ifs.assessment.form.profile.AssessorProfileAppointmentForm;
import org.innovateuk.ifs.assessment.form.profile.AssessorProfileDeclarationForm;
import org.innovateuk.ifs.assessment.form.profile.AssessorProfileFamilyAffiliationForm;
import org.innovateuk.ifs.assessment.model.profile.AssessorProfileDeclarationBasePopulator;
import org.innovateuk.ifs.user.resource.AffiliationResource;
import org.innovateuk.ifs.user.resource.AffiliationType;
import org.innovateuk.ifs.user.resource.UserResource;
import org.innovateuk.ifs.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static org.innovateuk.ifs.user.resource.AffiliationType.FAMILY;
import static org.innovateuk.ifs.user.resource.AffiliationType.PERSONAL;

/**
 * Hydrates the form for the Assessor Declaration of Interest edit view.
 */
@Component
public class AssessorProfileDeclarationFormPopulator extends AssessorProfileDeclarationBasePopulator {

    @Autowired
    private UserService userService;

    public AssessorProfileDeclarationForm populateForm(AssessorProfileDeclarationForm form, UserResource user) {
        Map<AffiliationType, List<AffiliationResource>> affiliations = getAffiliationsMap(userService.getUserAffiliations(user.getId()));

        form.setPrincipalEmployer(getPrincipalEmployer(affiliations).map(AffiliationResource::getOrganisation).orElse(null));
        form.setRole(getPrincipalEmployer(affiliations).map(AffiliationResource::getPosition).orElse(null));
        form.setProfessionalAffiliations(getProfessionalAffiliations(affiliations));

        form.setHasAppointments(hasAppointments(affiliations));
        form.setAppointments(getAppointmentForms(affiliations));

        form.setHasFinancialInterests(hasFinancialInterests(affiliations));
        form.setFinancialInterests(getFinancialInterests(affiliations));

        form.setHasFamilyAffiliations(hasFamilyAffiliations(affiliations));
        form.setFamilyAffiliations(getFamilyAffiliationForms(affiliations));

        form.setHasFamilyFinancialInterests(hasFamilyFinancialInterests(affiliations));
        form.setFamilyFinancialInterests(getFamilyFinancialInterests(affiliations));

        return form;
    }

    private List<AssessorProfileAppointmentForm> getAppointmentForms(Map<AffiliationType, List<AffiliationResource>> affiliations) {
        return getAffiliationsByType(PERSONAL, affiliations)
                .map(affiliationResources -> affiliationResources.stream()
                        .filter(AffiliationResource::getExists)
                        .map(row -> new AssessorProfileAppointmentForm(row.getOrganisation(), row.getPosition()))
                        .collect(toList())
                )
                .orElse(emptyList());
    }

    private List<AssessorProfileFamilyAffiliationForm> getFamilyAffiliationForms(Map<AffiliationType, List<AffiliationResource>> affiliations) {
        return getAffiliationsByType(FAMILY, affiliations)
                .map(affiliationResources -> affiliationResources.stream()
                        .filter(AffiliationResource::getExists)
                        .map(row -> new AssessorProfileFamilyAffiliationForm(
                                row.getRelation(),
                                row.getOrganisation(),
                                row.getPosition())
                        )
                        .collect(toList())
                )
                .orElse(emptyList());
    }
}

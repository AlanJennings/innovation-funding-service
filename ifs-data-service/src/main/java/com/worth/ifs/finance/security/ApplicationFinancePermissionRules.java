package com.worth.ifs.finance.security;

import com.worth.ifs.finance.resource.ApplicationFinanceResource;
import com.worth.ifs.security.PermissionRule;
import com.worth.ifs.security.PermissionRules;
import com.worth.ifs.security.SecurityRuleUtil;
import com.worth.ifs.user.domain.ProcessRole;
import com.worth.ifs.user.domain.Role;
import com.worth.ifs.user.domain.UserRoleType;
import com.worth.ifs.user.repository.ProcessRoleRepository;
import com.worth.ifs.user.repository.RoleRepository;
import com.worth.ifs.user.resource.UserResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.worth.ifs.user.domain.UserRoleType.*;

/**
 *  ApplicationFinancePermissionRules are applying rules for seeing / updating the application
 */

@Component
@PermissionRules
public class ApplicationFinancePermissionRules {

    @Autowired
    private ProcessRoleRepository processRoleRepository;

    @Autowired
    private RoleRepository roleRepository;

    @PermissionRule(value = "READ", description = "The consortium can see the application finances of their own organisation")
    public boolean consortiumCanSeeTheApplicationFinancesForTheirOrganisation(final ApplicationFinanceResource applicationFinanceResource, final UserResource user) {
        final boolean isLeadApplicant = checkRole(user, applicationFinanceResource.getApplication(), applicationFinanceResource.getOrganisation(), LEADAPPLICANT);
        final boolean isCollaborator = checkRole(user, applicationFinanceResource.getApplication(), applicationFinanceResource.getOrganisation(), COLLABORATOR);
        return isLeadApplicant || isCollaborator;
    }

    @PermissionRule(value = "READ", description = "An assessor can see the application finances for organisations in the applications they assess")
    public boolean assessorCanSeeTheApplicationFinanceForOrganisationsInApplicationsTheyAssess(final ApplicationFinanceResource applicationFinanceResource, final UserResource user){
        final boolean isAssessor = checkRole(user, applicationFinanceResource.getApplication(), applicationFinanceResource.getOrganisation(), ASSESSOR);
        return isAssessor;
    }

    @PermissionRule(value = "READ", description = "A comp admin can see application finances for organisations")
    public boolean compAdminCanSeeApplicationFinancesForOrganisations(final ApplicationFinanceResource applicationFinanceResource, final UserResource user){
        return SecurityRuleUtil.isCompAdmin(user);
    }

    private boolean checkRole(UserResource user, Long applicationId, Long organisationId, UserRoleType userRoleType) {
        final List<Role> roles = roleRepository.findByName(userRoleType.getName());
        final Role role = roles.get(0);
        final ProcessRole processRole = processRoleRepository.findByUserIdAndRoleIdAndApplicationIdAndOrganisationId(user.getId(), role.getId(), applicationId, organisationId);
        return processRole != null;
    }

}
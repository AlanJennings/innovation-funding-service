package org.innovateuk.ifs.user.repository;

import org.innovateuk.ifs.BaseRepositoryIntegrationTest;
import org.innovateuk.ifs.user.domain.ProcessRole;
import org.innovateuk.ifs.user.domain.Role;
import org.innovateuk.ifs.user.resource.UserRoleType;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class ProcessRoleRepositoryIntegrationTest extends BaseRepositoryIntegrationTest<ProcessRoleRepository> {

    @Autowired
    private RoleRepository roleRepository;

    @Override
    @Autowired
    protected void setRepository(ProcessRoleRepository repository) {
        this.repository = repository;
    }

    @Test
    public void test_findByUserIdAndRoleAndApplicationId() {

        long userId = 2L;
        long applicationId = 1L;
        String roleName = UserRoleType.COLLABORATOR.getName();

        Role role = roleRepository.findOneByName(roleName);
        List<ProcessRole> processRoles = repository.findByUserIdAndRoleAndApplicationId(userId, role, applicationId);

        assertEquals(1, processRoles.size());

        ProcessRole processRole = processRoles.stream().findFirst().get();
        assertEquals(roleName, processRole.getRole().getName());
        assertEquals(Long.valueOf(applicationId), processRole.getApplicationId());
        assertEquals(Long.valueOf(userId), processRole.getUser().getId());
    }
}

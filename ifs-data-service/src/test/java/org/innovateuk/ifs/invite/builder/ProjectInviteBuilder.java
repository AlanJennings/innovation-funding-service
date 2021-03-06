package org.innovateuk.ifs.invite.builder;

import org.innovateuk.ifs.BaseBuilder;
import org.innovateuk.ifs.Builder;
import org.innovateuk.ifs.invite.domain.ProjectInvite;
import org.innovateuk.ifs.project.domain.Project;
import org.innovateuk.ifs.user.domain.Organisation;
import org.innovateuk.ifs.user.domain.User;

import java.util.List;
import java.util.function.BiConsumer;

import static org.innovateuk.ifs.base.amend.BaseBuilderAmendFunctions.uniqueIds;
import static java.util.Collections.emptyList;


public class ProjectInviteBuilder extends BaseBuilder<ProjectInvite, ProjectInviteBuilder> {

    private ProjectInviteBuilder(List<BiConsumer<Integer, ProjectInvite>> multiActions) {
        super(multiActions);
    }

    public static ProjectInviteBuilder newInvite() {
        return new ProjectInviteBuilder(emptyList()).with(uniqueIds());
    }

    @Override
    protected ProjectInviteBuilder createNewBuilderWithActions(List<BiConsumer<Integer, ProjectInvite>> actions) {
        return new ProjectInviteBuilder(actions);
    }

    public ProjectInviteBuilder withProject(Builder<Project, ?> project) {
        return withProject(project.build());
    }

    public ProjectInviteBuilder withProject(Project... projects) {
        return withArray((project, invite) -> invite.setTarget(project), projects);
    }

    public ProjectInviteBuilder withOrganisation(Builder<Organisation, ?> organisation) {
        return withOrganisation(organisation.build());
    }

    public ProjectInviteBuilder withOrganisation(Organisation... organisations) {
        return withArray((organisation, invite) -> invite.setOrganisation(organisation), organisations);
    }

    public ProjectInviteBuilder withName(String... names) {
        return withArray((name, invite) -> invite.setName(name), names);
    }

    public ProjectInviteBuilder withEmailAddress(String... emails) {
        return withArray((email, invite) -> invite.setEmail(email), emails);
    }

    public ProjectInviteBuilder withHash(String... hashes) {
        return withArray((hash, invite) -> invite.setHash(hash), hashes);
    }

    public ProjectInviteBuilder withUser(User... users) {
        return withArray((user, invite) -> invite.setUser(user), users);
    }




    @Override
    protected ProjectInvite createInitial() {
        return new ProjectInvite();
    }
}

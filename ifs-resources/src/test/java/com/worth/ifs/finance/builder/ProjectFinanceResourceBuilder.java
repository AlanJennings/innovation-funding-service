package com.worth.ifs.finance.builder;

import com.worth.ifs.finance.resource.ProjectFinanceResource;

import java.util.List;
import java.util.function.BiConsumer;

import static com.worth.ifs.base.amend.BaseBuilderAmendFunctions.uniqueIds;
import static java.util.Collections.emptyList;

/**
 * Builder for ProjectFinance entities.
 */
public class ProjectFinanceResourceBuilder extends BaseFinanceResourceBuilder<ProjectFinanceResource, ProjectFinanceResourceBuilder> {

    private ProjectFinanceResourceBuilder(List<BiConsumer<Integer, ProjectFinanceResource>> newMultiActions) {
        super(newMultiActions);
    }

    public static ProjectFinanceResourceBuilder newProjectFinanceResource() {
        return new ProjectFinanceResourceBuilder(emptyList()).with(uniqueIds());
    }

    @Override
    protected ProjectFinanceResourceBuilder createNewBuilderWithActions(List<BiConsumer<Integer, ProjectFinanceResource>> actions) {
        return new ProjectFinanceResourceBuilder(actions);
    }

    @Override
    protected ProjectFinanceResource createInitial() {
        return new ProjectFinanceResource();
    }
}
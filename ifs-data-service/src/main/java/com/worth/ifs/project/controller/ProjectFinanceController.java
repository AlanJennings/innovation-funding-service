package com.worth.ifs.project.controller;

import com.worth.ifs.commons.rest.RestResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.worth.ifs.commons.rest.RestResult.restSuccess;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

/**
 * ProjectFinanceController exposes Project finance data and operations through a REST API.
 */
@RestController
@RequestMapping("/project")
public class ProjectFinanceController {

    @RequestMapping(value = "/{projectId}/partner-organisation/{partnerOrganisationId}/spend-profile/generate", method = POST)
    public RestResult<Void> generateSpendProfile(@PathVariable("projectId") final Long projectId,
                                                 @PathVariable("partnerOrganisationId") final Long partnerOrganisationId) {
        System.out.println("Spend Profile generated!");
        return restSuccess();
    }
}

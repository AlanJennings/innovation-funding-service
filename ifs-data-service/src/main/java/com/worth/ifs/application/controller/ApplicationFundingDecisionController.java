package com.worth.ifs.application.controller;

import com.worth.ifs.application.resource.FundingDecision;
import com.worth.ifs.application.transactional.ApplicationFundingService;
import com.worth.ifs.commons.rest.RestResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Responsible for receiving calls to set the funding decision for all applications for a given competition.
 */
@RestController
@RequestMapping("/applicationfunding")
public class ApplicationFundingDecisionController {

    @Autowired
    private ApplicationFundingService applicationFundingService;

    @RequestMapping(value="/{competitionId}/submit", method=RequestMethod.POST)
    public RestResult<Void> makeFundingDecision(@PathVariable("competitionId") final Long competitionId, @RequestBody Map<Long, FundingDecision> applicationFundingDecisions) {
        return applicationFundingService.makeFundingDecision(competitionId, applicationFundingDecisions).
                andOnSuccess(() -> applicationFundingService.notifyLeadApplicantsOfFundingDecisions(competitionId, applicationFundingDecisions)).
                toPostResponse();
    }
    
    @RequestMapping(value="/{competitionId}", method=RequestMethod.PUT)
    public RestResult<Void> saveFundingDecisionData(@PathVariable("competitionId") final Long competitionId, @RequestBody Map<Long, FundingDecision> applicationFundingDecisions) {
        return applicationFundingService.saveFundingDecisionData(competitionId, applicationFundingDecisions).
                toPutResponse();
    }
    
    @RequestMapping(value="/{competitionId}", method=RequestMethod.GET)
    public RestResult<Map<Long, FundingDecision>> getFundingDecisionData(@PathVariable("competitionId") final Long competitionId) {
        return applicationFundingService.getFundingDecisionData(competitionId).
                toGetResponse();
    }
}

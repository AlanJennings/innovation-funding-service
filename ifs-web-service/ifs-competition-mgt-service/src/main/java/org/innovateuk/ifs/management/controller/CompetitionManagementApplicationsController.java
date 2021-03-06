package org.innovateuk.ifs.management.controller;

import org.innovateuk.ifs.management.controller.CompetitionManagementApplicationController.ApplicationOverviewOrigin;
import org.innovateuk.ifs.management.model.AllApplicationsPageModelPopulator;
import org.innovateuk.ifs.management.model.ApplicationsMenuModelPopulator;
import org.innovateuk.ifs.management.model.SubmittedApplicationsModelPopulator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

import static org.innovateuk.ifs.util.BackLinkUtil.buildOriginQueryString;


/**
 * This controller will handle all requests that are related to the applications of a Competition within Competition Management.
 */
@Controller
@RequestMapping("/competition/{competitionId}/applications")
@PreAuthorize("hasAnyAuthority('comp_admin', 'project_finance')")
public class CompetitionManagementApplicationsController {

    @Autowired
    private ApplicationsMenuModelPopulator applicationsMenuModelPopulator;

    @Autowired
    private AllApplicationsPageModelPopulator allApplicationsPageModelPopulator;

    @Autowired
    private SubmittedApplicationsModelPopulator submittedApplicationsModelPopulator;

    @GetMapping
    public String applicationsMenu(Model model, @PathVariable("competitionId") long competitionId) {
        model.addAttribute("model", applicationsMenuModelPopulator.populateModel(competitionId));
        return "competition/applications-menu";
    }

    @GetMapping("/all")
    public String allApplications(Model model,
                                  @PathVariable("competitionId") long competitionId,
                                  @RequestParam MultiValueMap<String, String> queryParams,
                                  @RequestParam(value = "page", defaultValue = "0") int page,
                                  @RequestParam(value = "sort", defaultValue = "") String sort,
                                  @RequestParam(value = "filterSearch", defaultValue = "") String filter) {
        String originQuery = buildOriginQueryString(ApplicationOverviewOrigin.ALL_APPLICATIONS, queryParams);
        model.addAttribute("model", allApplicationsPageModelPopulator.populateModel(competitionId, originQuery, page, sort, filter));
        model.addAttribute("originQuery", originQuery);

        return "competition/all-applications";
    }

    @GetMapping("/submitted")
    public String submittedApplications(Model model,
                                        @PathVariable("competitionId") long competitionId,
                                        @RequestParam MultiValueMap<String, String> queryParams,
                                        @RequestParam(value = "page", defaultValue = "0") int page,
                                        @RequestParam(value = "sort", defaultValue = "") String sort,
                                        @RequestParam(value = "filterSearch", defaultValue = "") String filter) {
        String originQuery = buildOriginQueryString(ApplicationOverviewOrigin.SUBMITTED_APPLICATIONS, queryParams);
        model.addAttribute("model", submittedApplicationsModelPopulator.populateModel(competitionId, originQuery, page, sort, filter));
        model.addAttribute("originQuery", originQuery);

        return "competition/submitted-applications";
    }
}

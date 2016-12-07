*** Settings ***
Documentation     INFUND-4851 As a project manager I want to be able to submit an uploaded Grant Offer Letter so that Innovate UK can review my signed copy
...
...               INFUND-6059 As the contracts team I want to be able to send a Grant Offer Letter to the partners so that the project can begin
Suite Setup       all the other sections of the project are completed
Suite Teardown    the user closes the browser
Force Tags        Project Setup    Upload
Resource          ../../resources/defaultResources.robot
Resource          PS_Variables.robot

*** Test Cases ***
PM can view the grant offer letter page
    [Documentation]    INFUND-4848
    [Tags]    HappyPath
    [Setup]    log in as a different user    ${PS_GOL_APPLICATION_PM_EMAIL}    Passw0rd
    Given the user clicks the button/link    link=${PS_GOL_APPLICATION_HEADER}
    When the user clicks the button/link    link=Grant offer letter
    Then the user should see the text in the page    The grant offer letter is provided by Innovate UK
    And the user should see the element    jQuery=label:contains(+ Upload)
    And the user should not see the text in the page    This document is awaiting signature by the Project Manager

Partners should not be able to submit the Grant Offer
    [Documentation]    INFUND-4851, INFUND-6133
    [Tags]
    [Setup]    log in as a different user    ${PS_GOL_APPLICATION_PARTNER_EMAIL}    Passw0rd
    Given the user clicks the button/link    link=${PS_GOL_APPLICATION_HEADER}
    And the user clicks the button/link    link=Grant offer letter
    Then the user should not see the element    jQuery=label:contains(+ Upload)
    And the user should not see the element    jQuery=.button:contains("Submit signed offer letter")
    Then the user goes back to the previous page
    And the user should see the element    jQuery=li.waiting:nth-child(8)

Links to other sections in Project setup dependent on project details (applicable for Lead/ partner)
    [Documentation]    INFUND-4428
    [Tags]    HappyPath
    [Setup]    Log in as a different user    ${PS_GOL_APPLICATION_PARTNER_EMAIL}    Passw0rd
    Given the user clicks the button/link    link=${PS_GOL_APPLICATION_HEADER}
    And the user should see the element    jQuery=ul li.complete:nth-child(1)
    And the user should see the text in the page    Successful application
    Then the user should see the element    link = Monitoring Officer
    And the user should see the element    link = Bank details
    And the user should not see the element    link = Finance checks
    And the user should see the element    link= Spend profile
    And the user should see the element    link = Grant offer letter

PM should not be able to upload big Grant Offer files
    [Documentation]    INFUND-4851, INFUND-4972
    [Tags]
    [Setup]    log in as a different user    ${PS_GOL_APPLICATION_PM_EMAIL}    Passw0rd
    Given the user clicks the button/link    link=${PS_GOL_APPLICATION_HEADER}
    And the user clicks the button/link    link=Grant offer letter
    When the lead uploads a grant offer letter    ${too_large_pdf}
    Then the user should see the text in the page    ${too_large_pdf_validation_error}
    And the user goes back to the previous page

PM should be able upload a file and then access the Submit button
    [Documentation]    INFUND-4851, INFUND-4972
    [Tags]    HappyPath
    [Setup]    log in as a different user    ${PS_GOL_APPLICATION_PM_EMAIL}    Passw0rd
    Given the user clicks the button/link    link=${PS_GOL_APPLICATION_HEADER}
    And the user clicks the button/link    link=Grant offer letter
    When the lead uploads a grant offer letter    ${valid_pdf}
    Then the user should see the text in the page    ${valid_pdf}
    When the user reloads the page
    Then the user should see the element    jQuery=.button:contains("Submit signed offer letter")

PM can view the generated Grant Offer Letter
    [Documentation]    INFUND-6059
    [Tags]    Pending
    [Setup]    log in as a different user    ${PS_GOL_APPLICATION_PM_EMAIL}    Passw0rd
    Given the user navigates to the page  ${server}/project-setup/project/${PS_GOL_APPLICATION_PROJECT}/
    Then the user should see the element  jQuery=ul li.require-action:nth-child(8)
    When the user clicks the button/link  link=Grant offer letter
    Then the user should see the element  jQuery=h2:contains("Grant offer letter")
#    And the user should see the element   link=grant-offer-letter
#    Then the user clicks the button/link  link=grant-offer-letter
#TODO Pending due to INFUND-5998. In 5998 the CompAdmin can upload the GOL

Other external users can see the uploaded Grant Offer letter
    [Documentation]    INFUND-6059
    [Tags]    Pending
    Given log in as a different user      ${PS_GOL_APPLICATION_PARTNER_EMAIL}    Passw0rd
    And the user navigates to the page    ${server}/project-setup/project/${PS_GOL_APPLICATION_PROJECT}/
    Then the user should see the element  jQuery=ul li.waiting:nth-child(8)
    When the user clicks the button/link  link=Grant offer letter
    Then the user should see the element  jQuery=h2:contains("Grant offer letter")
#    And the user should see the element   link=grant-offer-letter
#    Then the user clicks the button/link  link=grant-offer-letter
#TODO Pending due to INFUND-5998. In 5998 the CompAdmin can upload the GOL

PM can view the uploaded Annex file
    [Documentation]    INFUND-4851, INFUND-4849
    [Tags]    HappyPath
    [Setup]    log in as a different user    ${PS_GOL_APPLICATION_PM_EMAIL}    Passw0rd
    Given the user navigates to the page     ${server}/project-setup/project/${PS_GOL_APPLICATION_PROJECT}/offer
    When the user clicks the button/link     link=${valid_pdf}
    Then the user should not see an error in the page
    And the user goes back to the previous page

PM Submits the Grant Offer letter
    [Documentation]    INFUND-4851
    [Tags]    HappyPath
    When the user clicks the button/link    jQuery=.button:contains("Submit signed offer letter")
    And the user clicks the button/link     jQuery=button:contains("Confirm Submission")
    Then the user should not see an error in the page

PM's dashboard should be updated
    [Documentation]    INFUND-4851
    [Tags]    Pending
    # TODO Pending due to INFUND-5998
    When the user clicks the button/link    link=What's the status of each of my partners?
    # TODO - To be fixed when 'PM Submitting GOL' story is worked upon
    # Then the user should see the element    jQuery=#table-project-status tr:nth-of-type(1) td:nth-of-type(7).status.ok

Internal Dashboard should be updated
    [Documentation]    INFUND-4851
    [Tags]    Pending
    [Setup]    log in as a different user    &{Comp_admin1_credentials}
    # Pending due to ongoing work in sprint 19, will attach a ticket number once the ticket has been created by BAs
    When the user navigates to the page     ${server}/project-setup-management/competition/${PS_GOL_Competition_Id}/status
    Then the user should see the element    jQuery=#table-project-status tr:nth-of-type(1) td:nth-of-type(7).status.ok

*** Keywords ***
the lead uploads a grant offer letter
    [Arguments]    ${file_name}
    choose file    name=signedGrantOfferLetter    ${upload_folder}/${file_name}

all the other sections of the project are completed
    the project finance user has approved bank details
    other documents have been uploaded and approved
    project finance generates the Spend Profile
    all parteners submit their Spend Profile
    proj finance approves the spend profiles

the project finance user has approved bank details
    Guest user log-in  &{internal_finance_credentials}
    the project finance user approves bank details for    Gabtype
    the project finance user approves bank details for    Kazio
    the project finance user approves bank details for    Cogilith

the project finance user approves bank details for
    [Arguments]    ${org_name}
    the user navigates to the page            ${server}/project-setup-management/project/${PS_GOL_APPLICATION_PROJECT}/review-all-bank-details
    the user clicks the button/link           link=${org_name}
    the user should see the text in the page  ${org_name}
    the user clicks the button/link           jQuery=.button:contains("Approve bank account details")
    the user clicks the button/link           jQuery=.button:contains("Approve account")
    the user should not see the element       jQuery=.button:contains("Approve bank account details")
    the user should see the text in the page  The bank details provided have been approved.

other documents have been uploaded and approved
    log in as a different user        ${PS_GOL_APPLICATION_PM_EMAIL}    Passw0rd
    the user navigates to the page    ${server}/project-setup/project/${PS_GOL_APPLICATION_PROJECT}/partner/documents
    choose file    name=collaborationAgreement    ${upload_folder}/testing.pdf
    choose file    name=exploitationPlan    ${upload_folder}/testing.pdf
    the user reloads the page
    the user clicks the button/link    jQuery=.button:contains("Submit partner documents")
    the user clicks the button/link    jQuery=.button:contains("Submit")
    log in as a different user         &{internal_finance_credentials}
    the user navigates to the page     ${SERVER}/project-setup-management/project/${PS_GOL_APPLICATION_PROJECT}/partner/documents
    the user clicks the button/link    jQuery=.button:contains("Accept documents")
    the user clicks the button/link    jQuery=.modal-accept-docs .button:contains("Accept Documents")

project finance generates the Spend Profile
    log in as a different user      &{internal_finance_credentials}
    the user navigates to the page  ${server}/project-setup-management/project/${PS_GOL_APPLICATION_PROJECT}/finance-check
    the user clicks the button/link  jQuery=.button:contains("Generate Spend Profile")
    the user clicks the button/link  jQuery=.button:contains("Generate spend profile")

all parteners submit their Spend Profile
    log in as a different user         ${PS_GOL_APPLICATION_PARTNER_EMAIL}    Passw0rd
    the user navigates to the page     ${server}/project-setup/project/${PS_GOL_Competition_Id}/partner-organisation/${Kazio_Id}/spend-profile
    the user clicks the button/link    jQuery=.button:contains("Submit to lead partner")
    log in as a different user         ${PS_GOL_APPLICATION_ACADEMIC_EMAIL}    Passw0rd
    the user navigates to the page     ${server}/project-setup/project/${PS_GOL_Competition_Id}/partner-organisation/${Cogilith_Id}/spend-profile
    the user clicks the button/link    jQuery=.button:contains("Submit to lead partner")
    log in as a different user         ${PS_GOL_APPLICATION_LEAD_PARTNER_EMAIL}    Passw0rd
    the user navigates to the page     ${server}/project-setup/project/${PS_GOL_Competition_Id}/partner-organisation/${Gabtype_Id}/spend-profile
    the user clicks the button/link    link=${Gabtype_Name}
    the user clicks the button/link    jQuery=.button:contains("Mark as complete")
    the user navigates to the page     ${server}/project-setup/project/${PS_GOL_Competition_Id}/partner-organisation/${Gabtype_Id}/spend-profile
    the user clicks the button/link    jQuery=.button:contains("Review and submit total project")
    the user clicks the button/link    jQuery=.button:contains("Submit project spend profile")
    the user clicks the button/link    jQuery=.modal-confirm-spend-profile-totals .button[value="Submit"]

proj finance approves the spend profiles
    log in as a different user         &{internal_finance_credentials}
    the user navigates to the page     ${server}/project-setup-management/project/${PS_GOL_Competition_Id}/spend-profile/approval
    the user selects the checkbox      id=approvedByLeadTechnologist
    the user clicks the button/link    jQuery=.button:contains("Approved")
    the user clicks the button/link    jQuery=.modal-accept-profile button:contains("Accept documents")

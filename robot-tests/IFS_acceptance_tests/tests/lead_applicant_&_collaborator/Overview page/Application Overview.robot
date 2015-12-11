*** Settings ***
Documentation     -INFUND-408: As an applicant, and I am on the application overview I do not need to see progress updates for certain questions such as appendix questions
...
...               -INFUND-39: As an applicant and I am on the application overview, I can select a section of the application, so I can see the status of each subsection in this section.
...
...               -INFUND-37: As an applicant and I am on the application overview, I can view the status of this application, so I know what actions I need to take.
...
...               -INFUND-32: As an applicant and I am on the MyApplications page, I can view the status of all my current applications, so I know what actions I need to take
Suite Setup       Login as User    &{lead_applicant_credentials}
Suite Teardown    TestTeardown User closes the browser
Resource          ../../../resources/GLOBAL_LIBRARIES.robot
Resource          ../../../resources/variables/GLOBAL_VARIABLES.robot
Resource          ../../../resources/keywords/Login_actions.robot
Resource          ../../../resources/keywords/Applicant_actions.robot
Resource          ../../../resources/variables/User_credentials.robot

*** Test Cases ***
Verify that the Applicant can access the Overview page
    [Tags]    Applicant    Overview
    Given the user is logged in as applicant
    When the Applicant clicks the "Rovel Additive Manufacturing Process" application
    Then the Applicant should see the overview of the selected application

Verify that the Applicant can see the List with the sections
    [Tags]    Applicant    Overview
    When Applicant goes to the Overview page
    Then the applicant should see six sections

Verify that when Applicant clicks the "Scope" this section is expanded
    [Tags]    Applicant
    #Given the Applicant is in the application overview page
    #Then the Applicant clicks the "Scope" section
    #Then the First section should not be expanded
    #And the first section should be hidden
    #And the second button should be expanded
    #And the second section should be visible
    #And the "Scope" sub-section should be visible

Verify that only the main questions show and not the file uploads
    [Documentation]    INFUND-428
    [Tags]    Applicant
    When the Applicant is in the application overview page
    Then the uploads should not be visible

Verify the applicant can see the days left to submit
    [Documentation]    -INFUND-37
    [Tags]    Applicant
    When the Applicant is in the application overview page
    The "Days left to submit" should be visible in the overview page
    The "days left to submit" should be correct in the overview page

Verify the "Progress bar" is 0% when the application is empty
    [Documentation]    INFUND-32
    [Tags]    Applicant
    When the Applicant is in the application overview page
    The Progress bar should be 0% in the overview page
    and the progress bar on the My applications page should be 0%

Verify the "Progress bar" is 14% when the applicant marks as complete one question from each section
    [Documentation]    INFUND-32
    [Tags]    Applicant
    When the applicant completes one question from every section
    Then the Progress bar should be 14% in the overview page
    and the progress bar on the my applications page should be 14%

*** Keywords ***
the user is logged in as Applicant
    Location Should Be    ${applicant_dashboard_url}

the Applicant clicks the "Rovel Additive Manufacturing Process" application
    Click Link    link=A novel solution to an old problem

the Applicant should see the overview of the selected application
    Location Should Be    ${APPLICATION_OVERVIEW_URL}

the Applicant is in the application overview page
    Applicant goes to the Overview page

the applicant should see six sections
    Element Should Be Visible    css=#section-1 .heading-medium
    Element Should Be Visible    css=#section-2 .heading-medium
    Element Should Be Visible    css=#section-3 .heading-medium
    Element Should Be Visible    css=#section-4 .heading-medium
    Element Should Be Visible    css=#section-5 .heading-medium
    Element Should Be Visible    css=#section-6 .heading-medium

the Applicant clicks the "Scope" section
    [Documentation]    1. click second section
    Click Element    css=.section-overview > h2:nth-of-type(2) button

The First section should not be expanded
    [Documentation]    Assumption: page is loaded with first section option because of /section/1
    ...
    ...
    ...    2. check if the first button is now not expanded anymore (aria-expanded=false)
    ...
    ...    4. check if the second button is now expanded (aria-expanded=true)
    ...
    ...    5. check if the second section is now visible \ (aria-hidden=false)
    Page Should Contain Element    css=.section-overview > h2:nth-of-type(1) button[aria-expanded="false"]

the first section should be hidden
    Page Should Contain Element    css=.section-overview > div:nth-of-type(1)[aria-hidden="true"]

the second button should be expanded
    Page Should Contain Element    css=.section-overview > h2:nth-of-type(2) button[aria-expanded="true"]

the second section should be visible
    Page Should Contain Element    css=.section-overview > div:nth-of-type(2)[aria-hidden="false"]

the "Scope" sub-section should be visible
    Element Should Be Visible    link=How does your project align with the scope of this competition?

the uploads should not be visible
    Element Should Not Be Visible    css=#question-14 > div > input[type="file"]
    Element Should Not Be Visible    css=#question-17 > div > input[type="file"]
    Element Should Not Be Visible    css=#question-18 > div > input[type="file"]

The "Days left to submit" should be visible in the overview page
    Element Should Be Visible    css=.sub-header .deadline .pie-overlay > div

The Progress bar should be 0% in the overview page
    Element Text Should Be    css=#content > div.grid-row > div.column-half.competition-details > div > div > div.progress    0%

the applicant completes one question from every section
    go to    ${APPLICATION_URL}
    Input Text    id=11    test
    Click Element    css=#question-11 > div > div.textarea-wrapped.word-count > div.textarea-footer > button
    click link    link=Scope
    Input Text    id=13    test
    Click Element    css=#question-13 > div > div.textarea-wrapped.word-count > div.textarea-footer > button
    Click Link    link=Your business proposition
    Input Text    id=1    test
    Click Element    css=#question-1 > div > div > div.textarea-footer > button
    Click Link    link=Your approach to the project
    input text    id=5    test
    Click Element    css=#question-5 > div > div.textarea-wrapped.word-count > div.textarea-footer > button
    Click Link    link=Funding
    Input Text    id=15    test
    Click Element    css=#question-15 > div > div.textarea-wrapped.word-count > div.textarea-footer > button
    click link    link=Your finances
    Click Element    css=#question-20 > div.collapsible > h2:nth-child(1) > button
    Click Element    css=#collapsible-1 > div:nth-child(2) > div > button

the Progress bar should be 14% in the overview page
    go to    ${APPLICATION_OVERVIEW_URL}
    Element Text Should Be    css=#content > div.grid-row > div.column-half.competition-details > div > div > div.progress    14%

the progress bar on the My applications page should be 0%
    go to    ${DASHBOARD_URL}
    Element Text Should Be    css=#content > div > section.in-progress > ul > li:nth-child(1) > div > div:nth-child(1) > div > div > div.progress > div    0%

the progress bar on the my applications page should be 14%
    go to    ${DASHBOARD_URL}
    Element Text Should Be    css=#content > div > section.in-progress > ul > li:nth-child(1) > div > div:nth-child(1) > div > div > div.progress > div    14%

The "days left to submit" should be correct in the overview page
    Element Text Should Be    css=.sub-header .deadline .pie-overlay > div    8
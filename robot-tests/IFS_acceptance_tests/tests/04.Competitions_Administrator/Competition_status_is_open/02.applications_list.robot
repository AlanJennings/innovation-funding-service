*** Settings ***
Documentation     INFUND-2135 As a Competition Administrator I want to be able to view a listing of applications for an open competition, so that I have the latest status of the applications
...
...               INFUND-2259 \ As a competitions administrator I want to see summary details of all applications in a competition displayed alongside the list of applications so that I can reference \ information relating to the status of the competition
Suite Setup       Run Keywords    Log in as user    &{Comp_admin1_credentials}
...               AND    Given the user navigates to the page    ${COMP_MANAGEMENT_APPLICATIONS_LIST}
Suite Teardown    User closes the browser
Force Tags
Resource          ../../../resources/GLOBAL_LIBRARIES.robot
Resource          ../../../resources/variables/GLOBAL_VARIABLES.robot
Resource          ../../../resources/variables/User_credentials.robot
Resource          ../../../resources/keywords/Login_actions.robot
Resource          ../../../resources/keywords/User_actions.robot

*** Test Cases ***
Competitions admin should be able to see the list of applications
    [Documentation]    INFUND-2135: listing of applications for an open competition
    [Tags]    Competition management
    Then the user should see the text in the page    Application list

The correct columns show for the application list table
    [Documentation]    INFUND-2135: listing of applications for an open competition
    [Tags]    Competition management
    Then the user should see the text in the page    Application no.
    And the user should see the text in the page    Project title
    And the user should see the text in the page    Lead
    And the user should see the text in the page    Status
    And the user should see the text in the page    Percentage complete

The applications can be sorted by application number
    [Documentation]    INFUND-2135: listing of applications for an open competition
    [Tags]    Competition management
    When the application list is sorted by    Application no.
    Then the applications should be sorted by application number

The applications can be sorted by project title
    [Documentation]    INFUND-2135: listing of applications for an open competition
    [Tags]    Competition management
    When the application list is sorted by    Project title
    Then the applications should be sorted by project title

The applications can be sorted by project lead
    [Documentation]    INFUND-2300: listing of applications for an open competition
    [Tags]    Competition management    Pending
    # pending until has been refactored(INFUND-2176)
    When the application list is sorted by    Lead
    Then the applications should be sorted by project lead

The applications can be sorted by percentage complete
    [Documentation]    INFUND-2300: listing of applications for an open competition
    [Tags]    Competition management
    When the application list is sorted by    Percentage complete
    Then the applications should be sorted by percentage complete

Calculations of the open applications
    [Documentation]    INFUND-2259
    Then The calculations should be correct    jQuery=td:contains("open")    css=.info-area p:nth-child(3) span

Calculations of the submitted application
    [Documentation]    INFUND-2259
    [Tags]    Failing
    Then The calculations should be correct    jQuery=td:contains("submitted")    css=.info-area p:nth-child(5) span

Calculations for the Number of applications
    [Documentation]    INFUND-2259
    Then The calculations should be correct    jQuery=td:contains("00000")    css=.info-area p:nth-child(2) span
    And both caclculations in the page should show the same

Comp admin can open the view mode of the application
    [Documentation]    INFUND-2300: listing of applications for an open competition
    ...
    ...    INFUND-2304: Read only view mode of applications from the application list page
    [Tags]    Competition management
    When the user clicks the button/link    link=00000001
    Then the user should be redirected to the correct page    ${COMP_MANAGEMENT_APPLICATION_1_OVERVIEW}
    And the user should see the text in the page    A novel solution to an old problem
    And the user can see the uplaod for the 'Technical approach' question

*** Keywords ***
the application list is sorted by
    [Arguments]    ${sorting_factor}
    Select From List    name=sort    ${sorting_factor}

the applications should be sorted by application number
    element should contain    css=table tbody tr td a    00000001

the applications should be sorted by project title
    element should contain    css=table tbody tr td a    00000008

the applications should be sorted by Project lead
    element should contain    css=table tbody tr td a    foo

the applications should be sorted by Percentage complete
    element should contain    css=table tbody tr td a    00000007

the user can see the uplaod for the 'Technical approach' question
    the user clicks the button/link    css=[aria-controls="collapsible-8"]
    the user should see the text in the page    testing.pdf

The calculations should be correct
    [Arguments]    ${LIST_LOCATOR}    ${SUMMARY_LOCATOR}
    ${ELEMENT}=    Get Webelements    ${LIST_LOCATOR}
    ${LENGTH_LIST}=    Get Length    ${ELEMENT}
    log    ${LENGTH_LIST}
    ${LENGTH_SUMMARY}=    Get text    ${SUMMARY_LOCATOR}
    log    ${LENGTH_SUMMARY}
    Should Be Equal As Integers    ${LENGTH_SUMMARY}    ${LENGTH_LIST}

both caclculations in the page should show the same
    ${APPLICATIOS_NUMBER_SUMMARY}=    get text    css=.info-area p:nth-child(2) span
    ${APPLICATIOS_NUMBER_LIST}=    Get text    css=.column-two-thirds span
    Should Be Equal As Integers    ${APPLICATIOS_NUMBER_LIST}    ${APPLICATIOS_NUMBER_SUMMARY}
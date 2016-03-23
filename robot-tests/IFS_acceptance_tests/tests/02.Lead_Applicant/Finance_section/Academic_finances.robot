*** Settings ***
Documentation     INFUND-917: As an academic partner i want to input my finances according to the JES field headings, so that i enter my figures into the correct sections
...
...
...               INFUND-918: As an academic partner i want to be able to mark my finances as complete, so that the lead partner can have confidence in my finances
Suite Setup       Guest user log-in    &{collaborator2_credentials}
Suite Teardown    User closes the browser
Force Tags        Finances    HappyPath
Resource          ../../../resources/GLOBAL_LIBRARIES.robot
Resource          ../../../resources/variables/GLOBAL_VARIABLES.robot
Resource          ../../../resources/variables/User_credentials.robot
Resource          ../../../resources/keywords/Login_actions.robot
Resource          ../../../resources/keywords/User_actions.robot

*** Variables ***
${valid_pdf}      testing.pdf
${text_file}      testing.txt

*** Test Cases ***
Academic finance calculations
    [Documentation]    INFUND-917
    Given the user navigates to the page    ${YOUR_FINANCES_URL}
    When the academic partner fills the finances
    Then the calculations should be correct

Academic invalid upload
    When the academic partner uploads a file    ${text_file}
    Then the user should see the element    css=.error-summary
    And the user should see the text in the page    No file currently uploaded

Academics upload
    [Documentation]    INFUND-917
    When the academic partner uploads a file    ${valid_pdf}
    Then the user should not see the text in the page    No file currently uploaded
    And the user should see the element    link=testing.pdf

Mark all as complete
    [Documentation]    INFUND-918
    When the academic partner clicks the mark as complete
    Then the user should be redirected to the correct page    ${APPLICATION_OVERVIEW_URL}
    And the user navigates to the page    ${FINANCES_OVERVIEW_URL}
    And the user should see the element    css=.finance-summary tr:nth-of-type(3) img[src="/images/field/tick-icon.png

Academic finance overview
    [Documentation]    INFUND-917
    Given the user navigates to the page    ${FINANCES_OVERVIEW_URL}
    Then the finance table should be correct
    When the user clicks the button/link    link=testing.pdf
    Then the user should see the text in the page    Adobe Acrobat PDF Files

*** Keywords ***
the academic partner fills the finances
    Input Text    id=incurred-staff    1000
    Input Text    id=travel    1000
    Input Text    id=other    1000
    Input Text    id=investigators    1000
    Input Text    id=estates    1000
    Input Text    id=other-direct    1000
    Input Text    id=indirect    1000
    Input Text    id=exceptions-staff    1000
    Input Text    id=exceptions-other-direct    1000
    Input Text    id=tsb-ref    123123

the calculations should be correct
    Textfield Value Should Be    id=subtotal-directly-allocated    £ 3,000
    Textfield Value Should Be    id=subtotal-exceptions    £ 2,000
    Textfield Value Should Be    id=total    £ 9,000

the academic partner uploads a file
    [Arguments]    ${file_name}
    Choose File    name=jes-upload    ${UPLOAD_FOLDER}/${file_name}
    Sleep    500ms

the finance table should be correct
    #pending the reset of the database from Rogier
    #Element Should Contain    css=.project-cost-breakdown tr:nth-of-type(3) td:nth-of-type(1)    £0
    #Element Should Contain    css=.project-cost-breakdown tr:nth-of-type(3) td:nth-of-type(2)    £180,000
    Element Should Contain    css=.project-cost-breakdown tr:nth-of-type(3) td:nth-of-type(3)    £1,000
    #Element Should Contain    css=.project-cost-breakdown tr:nth-of-type(3) td:nth-of-type(4)    £60,000
    #Element Should Contain    css=.project-cost-breakdown tr:nth-of-type(3) td:nth-of-type(5)    £60,000
    #Element Should Contain    css=.project-cost-breakdown tr:nth-of-type(3) td:nth-of-type(6)    £60,000
    Element Should Contain    css=.project-cost-breakdown tr:nth-of-type(3) td:nth-of-type(7)    £1,000
    Element Should Contain    css=.project-cost-breakdown tr:nth-of-type(3) td:nth-of-type(8)    £3,000

the academic partner clicks the mark as complete
    focus     jQuery=.button:contains("Mark all as complete")
    click button    jQuery=.button:contains("Mark all as complete")
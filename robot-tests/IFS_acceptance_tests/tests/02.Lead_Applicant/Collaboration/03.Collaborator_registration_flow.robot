*** Settings ***
Documentation     INFUND-1005: As a collaborator I want to select my organisation type, so that I can create the correct account type for my organisation
...
...               INFUND-1779: As a collaborator registering my company as Business, I want to be able provide my organisation name and address details so I can successfully register for the competition
Test Setup        The guest user opens the browser
Test Teardown     TestTeardown User closes the browser
Force Tags        Create new application    collaboration
Resource          ../../../resources/GLOBAL_LIBRARIES.robot
Resource          ../../../resources/variables/GLOBAL_VARIABLES.robot
Resource          ../../../resources/variables/User_credentials.robot
Resource          ../../../resources/keywords/Login_actions.robot
Resource          ../../../resources/keywords/Applicant_actions.robot

*** Variables ***
${INVITE_LINK}    ${SERVER}/accept-invite/4e09372b85241cb03137ffbeb2110a1552daa1086b0bce0ff7d8ff5d2063c8ffc10e943acf4a3c7a

*** Test Cases ***
Lead applicant details should show in the invite page
    [Documentation]    INFUND-1005
    Given user navigates to the page    ${INVITE_LINK}
    When user clicks the button/link    jQuery=.button:contains("Create")
    Then user should see the text in the page    Lead organisation: Empire Ltd
    And user should see the text in the page    Lead applicant: Steve Smith

User can not continue if an organisation type is not selected
    [Tags]    Pending
    #pending because there is no validation
    When user clicks the button/link    jQuery=.button:contains("Continue")
    Then user should see the text in the page    Please select your organisation type

Accept Invitation flow (Business organisation)
    [Documentation]    INFUND-1005
    ...    INFUND-1779
    [Tags]    HappyPath
    Given user navigates to the page    ${INVITE_LINK}
    When user clicks the button/link    jQuery=.button:contains("Create")
    And user selects the radio button
    And user clicks the button/link    jQuery=.button:contains("Continue")
    Then user should be redirected to the correct page    ${SERVER}/accept-invite/create-organisation/?organisationType=1
    When user enters text to a text field    id=org-name    Empire
    And user clicks the button/link    id=org-search
    And user clicks the button/link    link=EMPIRE LTD
    and user enters text to a text field    css=#postcode-check    postcode
    And user clicks the button/link    id=postcode-lookup
    And user clicks the button/link    css=#select-address-block > button
    And user clicks the button/link    jQuery=.button:contains("Save organisation and")
    And the user fills the create account form
    Then user should be redirected to the correct page    ${DASHBOARD_URL}

User who accepted the invite should be able to log-in
    Given user navigates to the page    ${INVITE_LINK}
    When the guest user enters the login credentials    rogier@worth.systems    testtest
    And user clicks the button/link    css=button[name="_eventId_proceed"]
    Then user should be redirected to the correct page    ${DASHBOARD_URL}
    And user should see the text in the page    A novel solution to an old problem

*** Keywords ***
user selects the radio button
    Select Radio Button    organisationType    1

the user fills the create account form
    Input Text    id=firstName    Rogier
    Input Text    id=lastName    De Regt
    Input Text    id=phoneNumber    0612121212
    Input Password    id=password    testtest
    Input Password    id=retypedPassword    testtest
    Select Checkbox    termsAndConditions
    Submit Form
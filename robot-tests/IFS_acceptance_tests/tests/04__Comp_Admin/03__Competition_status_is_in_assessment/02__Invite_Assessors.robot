*** Settings ***
Documentation     INFUND-6604 As a member of the competitions team I can view the Invite assessors dashboard...
...
...               INFUND-6602 As a member of the competitions team I can navigate to the dashboard of an 'In assessment' competition...
...
...               INFUND-6392 As a member of the competitions team, I can add/remove an assessor...
...
...               INFUND-6412 As a member of the competitions team, I can view the invite list before sending invites...
...
...               INFUND-6414 As a member of the competitions team, I can select 'Invite individual' to review invitation and then 'Send invite' ...
...
...               INFUND-6411 As a member of the competitions team, I can add a non-registered assessor to my invite list so...
...
...               INFUND-6450 As a member of the competitions team, I can see the status of each assessor invite so...
...
...               INFUND-6448 As a member of the competitions team, I can remove an assessor from the invite list so...
...
...               INFUND-6450 As a member of the competitions team, I can see the status of each assessor invite so I know if they have accepted, declined or still awaiting repsonse
...
...               INFUND-6389 As a member of the competitions team I can see the innovation sector and innovation area(s) on the Invite assessors dashboard so ...
...
...               INFUND-6449 As a member of the competitions team, I can see the invited assessors list so...
...
...               INFUND-6669 As a member of the competitions team I can view an assessors profile so that I can decide if they are suitable to assess the competition
...
...               INFUND-6388 As a member of the competitions team I can see the key statistics on the Invite Assessors dashboard so that I can easily see how invitations are progressing
Suite Setup       Guest user log-in    &{Comp_admin1_credentials}
Suite Teardown    The user closes the browser
Force Tags        CompAdmin    Assessor
Resource          ../../../resources/defaultResources.robot

*** Test Cases ***
Check the initial key statistics
    [Documentation]    INFUND-6388
    [Tags]
    Given the user clicks the button/link    link=${IN_ASSESSMENT_COMPETITION_NAME}
    And the user clicks the button/link    jQuery=.button:contains("Invite assessors")
    And the key statistics are calculated
    And the user should see the element    jQuery=.column-quarter:nth-child(1) span:contains(${ks_invited})
    And the user should see the element    jQuery=.column-quarter:nth-child(2) span:contains(${ks_accepted})
    And the user should see the element    jQuery=.column-quarter:nth-child(3) span:contains(${ks_declined})
    And the user should see the element    jQuery=.column-quarter:nth-child(4) span:contains(${ks_assessors})

The User can Add and Remove Assessors
    [Documentation]    INFUND-6602 INFUND-6604 INFUND-6392 INFUND-6412
    [Tags]
    And The user should see the element    link=Overview
    When The user clicks the button/link    jQuery=tr:nth-child(1) .button:contains(Add)
    And The user clicks the button/link    link=Invite
    Then The user should see the text in the page    will.smith@gmail.com
    And The user should see the text in the page    Will Smith
    And The user should see the element    jQuery=tr:nth-child(1) .yes
    And the user should see the element    jQuery=tr:nth-child(1) td:nth-child(3):contains("Precision Medicine, Advanced Materials, Energy Systems")
    When The user clicks the button/link    link=Find
    And The user clicks the button/link    jQuery=tr:nth-child(1) .button:contains(Remove)
    And The user clicks the button/link    link=Invite
    Then The user should not see the text in the page    will.smith@gmail.com
    [Teardown]    The user clicks the button/link    link=Find

The user can select the profile link
    [Documentation]    INFUND-6669
    [Tags]
    When the user clicks the button/link    link=Will Smith
    Then the user should see the text in the page    will.smith@gmail.com
    And the user should see the text in the page    028572565937
    And the user should see the text in the page    Solar energy research
    And the user should see the text in the page    Precision Medicine
    And the user should see the text in the page    Business
    [Teardown]    The user clicks the button/link    link=Invite assessors

Innovation sector and area are correct
    [Documentation]    INFUND-6389
    [Tags]
    Given the user should see the element    jQuery=.heading-secondary:contains("Sustainable living models for the future")
    And the user should see the element    jQuery=.standard-definition-list dt:contains("Innovation sector")
    And the user should see the element    jQuery=.standard-definition-list dt:contains("Innovation area")
    And the user should see the element    jQuery=.standard-definition-list dd:contains("Materials and manufacturing")
    And the user should see the element    jQuery=.standard-definition-list dd:contains("Earth Observation")

Remove users from the list
    [Documentation]    INFUND-7354
    ...
    ...    INFUND-6448
    When The user clicks the button/link    jQuery=tr:nth-child(1) .button:contains(Add)
    And The user clicks the button/link    link=Invite
    And The user should see the text in the page    will.smith@gmail.com
    And the user should see the element    jQuery=.column-quarter:nth-child(4) span:contains(${inc_ks_assessors})
    And The user clicks the button/link    jQuery=tr:nth-child(1) .button:contains(Remove from list)
    Then The user should not see the text in the page    will.smith@gmail.com
    And The user clicks the button/link    link=Find
    And the user should see the element    jQuery=tr:nth-child(1) button:contains(Add)
    And the user should see the element    jQuery=.column-quarter:nth-child(4) span:contains(${ks_assessors})
    [Teardown]    The user clicks the button/link    link=Find

Invite Individual Assessors
    [Documentation]    INFUND-6414
    [Tags]
    Given The user clicks the button/link    jQuery=tr:nth-child(1) .button:contains(Add)
    And The user clicks the button/link    link=Invite
    When the user clicks the button/link    jQuery=tr:nth-child(1) .button:contains(Invite individual)
    And The user should see the text in the page    Please visit our new online Innovation funding service to respond to this request
    And The user enters text to a text field    css=#subject    Invitation to assess 'Sustainable living models for the future' @
    And the user clicks the button/link    jQuery=.button:contains(Send invite)
    Then The user should not see the text in the page    Will Smith
    And The user clicks the button/link    link=Find
    And the user should not see the text in the page    Will Smith
    And the user should see the element    jQuery=.column-quarter:nth-child(1) span:contains(${inc_ks_invited})

Invite non-registered assessors server side validations
    [Documentation]    INFUND-6411
    [Tags]
    Given the user clicks the button/link    link=Invite
    When the user clicks the button/link    jQuery=span:contains("Add a non-registered assessor to your list")
    And the user clicks the button/link    jQuery=.button:contains("Add assessors to list")
    Then the user should see a field error    Please select an innovation area.
    And the user should see a field error    Please enter a name.
    And the user should see a field error    Please enter an email address.

Invite non-registered users
    [Documentation]    INFUND-6411
    ...
    ...    INFUND-6448
    [Tags]
    When The user enters text to a text field    css=#invite-table tr:nth-of-type(1) td:nth-of-type(1) input    Olivier Giroud
    And The user should not see the text in the page    Please enter a name.    #check for the client side validation
    And The user enters text to a text field    css=#invite-table tr:nth-of-type(1) td:nth-of-type(2) input    worth.email.test+OlivierGiroud@gmail.com
    And The user should not see the text in the page    Please enter a name.    #check for the client side validation
    And the user selects the option from the drop-down menu    Emerging and enabling technologies    css=.js-progressive-group-select
    And the user selects the option from the drop-down menu    Data    id=grouped-innovation-area
    And The user should not see the text in the page    Please select an innovation area.    #check for the client side validation
    And the user clicks the button/link    jQuery=.button:contains("Add assessors to list")
    Then the user should see the element    css=.no
    And The user should see the element    jQuery=tr:nth-child(1) td:contains(Olivier Giroud)
    And The user should see the element    jQuery=tr:nth-child(1) td:contains(worth.email.test+OlivierGiroud@gmail.com)
    And The user should see the element    jQuery=tr:nth-child(1) td:contains(Data)
    And The user should see the element    jQuery=tr:nth-child(1) button:contains(Remove from list)

Assessor overview information
    [Documentation]    INFUND-6450
    ...
    ...    INFUND-6449
    [Tags]
    Given The user clicks the button/link    link=Overview
    Then the user should see the element    jQuery=tr:nth-child(2) td:contains(Invite accepted)
    And the user should see the element    jQuery=td:contains(Will Smith) ~ td:nth-child(5):contains(Awaiting response)
    And the user should see the element    jQuery=td:contains(Will Smith) ~ td:nth-child(6):contains(Invite sent:)
    And the user should see the element    jQuery=tr:nth-child(4) td:nth-child(5):contains(Invite declined)
    And the user should see the element    jQuery=tr:nth-child(4) td:contains(Academic)
    And the user should see the element    jQuery=tr:nth-child(4) td:contains(Yes)
    And the user should see the element    jQuery=tr:nth-child(4) td:contains(Invite declined as not available)
    And the user should see the element    jQuery=tr:nth-child(4) td:contains(Manufacturing Readiness)

*** Keywords ***
The key statistics are calculated
    ${ks_invited}=    Get Text    jQuery=.column-quarter:nth-child(1) span
    ${increment}=   Set Variable      ${1}
    ${inc_ks_invited} =     Evaluate     ${ks_invited}+${increment}
    ${ks_accepted}=    Get Text    jQuery=.column-quarter:nth-child(2) span
    ${ks_declined}=    Get Text    jQuery=.column-quarter:nth-child(3) span
    ${ks_assessors}=    Get Text    jQuery=.column-quarter:nth-child(4) span
    ${inc_ks_assessors}=    Evaluate    ${ks_assessors}+${increment}
    Set Suite Variable    ${inc_ks_invited}
    Set Suite Variable    ${inc_ks_assessors}
    Set Suite Variable    ${ks_invited}
    Set Suite Variable    ${ks_accepted}
    Set Suite Variable    ${ks_declined}
    Set Suite Variable    ${ks_assessors}
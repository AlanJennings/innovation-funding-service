*** Settings ***
Resource          ../defaultResources.robot

*** Keywords ***
The user navigates to the page
    [Arguments]    ${TARGET_URL}
    Wait for autosave
    Wait Until Keyword Succeeds Without Screenshots    30    200ms    Go To    ${TARGET_URL}
    Run Keyword And Ignore Error Without Screenshots    Confirm Action
    # Error checking
    Page Should Not Contain    Error
    Page Should Not Contain    something went wrong
    Page Should Not Contain    Page or resource not found
    Page Should Not Contain    You do not have the necessary permissions for your request
    # Header checking (INFUND-1892)
    Wait Until Element Is Visible Without Screenshots    id=global-header
    Page Should Contain    BETA
    # "Contact us" checking (INFUND-1289)
    # Pending completion of INFUND-2544, INFUND-2545
    # Wait Until Page Contains Element Without Screenshots    link=Contact Us
    # Page Should Contain Link    href=${SERVER}/info/contact

The user navigates to the assessor page
    [Arguments]    ${TARGET_URL}
    Wait for autosave
    Go To    ${TARGET_URL}
    # Error checking
    Page Should Not Contain    Error
    Page Should Not Contain    something went wrong

The user navigates to the page without the usual headers
    [Arguments]    ${TARGET_URL}
    Wait for autosave
    Go To    ${TARGET_URL}
    # Error checking
    Page Should Not Contain    Error
    Page Should Not Contain    something went wrong
    Page Should Not Contain    Page or resource not found
    Page Should Not Contain    You do not have the necessary permissions for your request

The user navigates to the page and gets a custom error message
    [Arguments]    ${TARGET_URL}    ${CUSTOM_ERROR_MESSAGE}
    Wait for autosave
    Go To    ${TARGET_URL}
    Page Should Contain    ${CUSTOM_ERROR_MESSAGE}

The user is on the page
    [Arguments]    ${TARGET_URL}
    Location Should Contain    ${TARGET_URL}
    # Error checking
    Page Should Not Contain    Error
    Page Should Not Contain    something went wrong
    Page Should Not Contain    Page or resource not found
    Page Should Not Contain    You do not have the necessary permissions for your request
    # Header checking (INFUND-1892)
    Wait Until Element Is Visible Without Screenshots    id=global-header
    Page Should Contain    BETA
    # "Contact us" checking (INFUND-1289)
    # Pending completion of INFUND-2544, INFUND-2545
    # Wait Until Page Contains Element Without Screenshots    link=Contact Us
    # Page Should Contain Link    href=${SERVER}/info/contact

the user is on the page or will navigate there
    [Arguments]    ${TARGET_URL}
    ${current_location} =    Get Location
    ${status}    ${value} =    Run Keyword And Ignore Error Without Screenshots    Location Should Contain    ${TARGET_URL}
    Run keyword if    '${status}' == 'FAIL'    The user navigates to the assessor page    ${TARGET_URL}

The user should be redirected to the correct page
    [Arguments]    ${URL}
    Wait Until Keyword Succeeds Without Screenshots    30    200ms    Location Should Contain    ${URL}
    Page Should Not Contain    error
    Page Should Not Contain    Page or resource not found
    Page Should Not Contain    You do not have the necessary permissions for your request
    # Header checking (INFUND-1892)
    Wait Until Element Is Visible Without Screenshots    id=global-header
    Page Should Contain    BETA

the user should be redirected to the correct page without the usual headers
    [Arguments]    ${URL}
    Wait Until Keyword Succeeds Without Screenshots    30    200ms    Location Should Contain    ${URL}
    Page Should Not Contain    error
    Page Should Not Contain    Page or resource not found
    Page Should Not Contain    You do not have the necessary permissions for your request

the user should be redirected to the correct page without error checking
    [Arguments]    ${URL}
    Wait Until Keyword Succeeds Without Screenshots    30    200ms    Location Should Contain    ${URL}
    # Header checking (INFUND-1892)
    Wait Until Element Is Visible Without Screenshots    id=global-header
    Page Should Contain    BETA

The user should see permissions error message
    Wait Until Page Contains Without Screenshots    You do not have the necessary permissions for your request
    Page Should Contain    You do not have the necessary permissions for your request

The user should see the text in the page
    [Arguments]    ${VISIBLE_TEXT}
    Wait Until Page Contains Without Screenshots    ${VISIBLE_TEXT}
    Page Should Not Contain    Error
    Page Should Not Contain    Page or resource not found
    Page Should Not Contain    You do not have the necessary permissions for your request
    Page Should Not Contain    something went wrong

The user goes back to the previous page
    Wait for autosave
    Go Back

the user reloads the page
    Wait for autosave
    Reload Page
    Run Keyword And Ignore Error Without Screenshots    confirm action
    # Error checking
    Page Should Not Contain    Error
    Page Should Not Contain    something went wrong
    Page Should Not Contain    Page or resource not found
    Page Should Not Contain    You do not have the necessary permissions for your request
    # Header checking (INFUND-1892)
    Element Should Be Visible    id=global-header
    Page Should Contain    BETA

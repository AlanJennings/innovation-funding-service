
UPDATE `question` SET `description` = '<p>You may incur indirect support staff costs linked with your administrative work for the project. To be eligible, these costs should be directly attributable and incremental to the project. Indirect costs associated with commercial activities are not eligible and must not be included. For further information on which costs are eligible please read our <a href="https://www.gov.uk/government/publications/innovate-uk-completing-your-application-project-costs-guidance">project costs guidance</a>.</p>' WHERE id = '29';
UPDATE `question` SET `description` = null WHERE id = '22';
UPDATE `form_input` SET `guidance_question` = null, `guidance_answer` = null WHERE id = '22';
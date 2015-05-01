Feature: Preserve instance specific configuration information in the event of a configuration
  Update or other synchronization event.

  Scenario: Update of a job config while preserving GIT credentials configuration
    Given: An existing job on active master and hot-standby
    And: An existing credential on active master and hot-standby
    And: The job is configured with GIT scm using the credential
    And: The GIT config credential field is present in the preservedFields.xml file
    When: The job is changed and saved on active master
    And: The job xml config file is received on hot-standby for update
    Then: The job is reloaded with new configuration on hot-standby except for the GIT config
          credential field

  Scenario: Update of a job config when there is no preservedFields.xml file
    Given: An existing job on active master and hot-standby
    And: An existing credential on active master and hot-standby
    And: The job is configured with GIT scm using the credential
    And: The preservedFields.xml file does not exist
    When: The job is changed and saved on active master
    And: The job xml config file is received on hot-standby for update
    Then: The job is reloaded with new configuration on hot-standby
    And: The hot-standby credential field is replaced by active master field.

  Scenario: Update of a job config when there is no matching preserved field
    Given: An existing job on active master and hot-standby
    And: An existing credential on active master and hot-standby
    And: The job is configured with GIT scm using the credential
    And: The preservedFields.xml file does not contain GIT config credential field
    When: The job is changed and saved on active master
    And: The job xml config file is received on hot-standby for update
    Then: The job is reloaded with new configuration on hot-standby
    And: The hot-standby credential field is replaced by active master field.

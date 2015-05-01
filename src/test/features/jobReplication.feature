Feature: Job replication.
  Freestyle job configuration information must be replicated across cluster members.

  Scenario: Create a new freestyle job and see it created across the cluster
    Given: there are no existing jobs on the active master
    And: there are no existing jobs on other hot-standby members.
    When: I create a new freestyle job on the active master
    Then: I should see the same freestyle job on other hot-standby members.

  Scenario: Modify an existing freestyle job and see the same modification applied across the cluster
    Given: there is an existing freestyle job on the active master
    And: this job exists on other cluster hot-standby members
    When: I modify the job on the active master
    Then: I should see the modification affected on other hot-standby members

  Scenario: Rename an existing freestyle job and see it renamed across the cluster
    Given: there is an existing freestyle job on the active master
    And: this job exists on other hot-standby members
    When: I modify the job on the active master
    Then: I should see that job renamed on other hot-standby members

  Scenario: Delete an existing freestyle job and see it deleted across the cluster
    Given: there is an existing freestyle job on the active master
    And: this job exists on other hot-standby members
    When: I delete the job on the active master
    Then: I should see that job deleted on other hot-standby members

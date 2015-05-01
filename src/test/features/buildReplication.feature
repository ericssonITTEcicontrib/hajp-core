Feature: Build replication.
  Freestyle job's build information must be replicated across cluster members.

  Scenario: Launch new build of a freestyle job and see it created across the cluster
    Given: there is a freestyle job on the active master
    And: the same job exists on other hot-standby members
    And: the build #x is launched for that job
    When: the build #x of that job completes on the active master
    Then: I should see the same build #x of the same job on hot-standby members
    And: the next build number should be the same for all members.

  @todo
  Scenario: Modify an existing build of a freestyle job and see it modified across the cluster
    Given: there is an existing freestyle job with a build #x on the active master
    And: the same job with a build #x exists on other hot-standby members
    When: the build #x of that job is modified on the active master
    Then: I should see the build #x of the same job modified on hot-standby members.

  Scenario: Delete an existing build of a freestyle job and see it deleted across the cluster
    Given: there is an existing freestyle job with a build #x on the active master
    And: the same job with a build #x exists on other hot-standby members
    When: the build #x of that job is deleted on the active master
    Then: I should see the build #x of the same job deleted on hot-standby members.

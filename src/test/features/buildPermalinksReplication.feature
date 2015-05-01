Feature: Build permalinks replication.
  Freestyle job's build permalinks must be replicated across cluster members.

  @todo
  Scenario: Update of permalinks on build success and see them updated across the cluster
    Given: there is a freestyle job on the active master
    And: the same job exists on hot-standby members
    And: the lastSuccessfulBuild is referring to build #x for that job
    And: a build #x+1 is launched for that job
    When: the build #x+1 of that job successfully completes on the active master
    Then: I should see the lastSuccessfulBuild and lastStableBuild referring to the build #x+1 of the same job on hot-standby members
    And: the build status icon changed to successful

  @todo
  Scenario: Update of permalinks on build failure and see them updated across the cluster
    Given: there is a freestyle job on the active master
    And: the same job exists on hot-standby members
    And: a build #x+1 is launched for that job
    When: the build #x+1 of that job fails on the active master
    Then: I should see the lastFailedBuild and lastUnsuccessfulBuild referring to the build #x+1 of the same job on hot-standby members
    And: the build status icon changed to failed

  @todo
  Scenario: Update of permalinks on build abortion and see them updated across the cluster
    Given: there is a freestyle job on the active master
    And: the same job exists on hot-standby members
    And: a build #x+1 is launched for that job
    When: the build #x+1 of that job aborted on the active master
    And: the build status icon changed to aborted

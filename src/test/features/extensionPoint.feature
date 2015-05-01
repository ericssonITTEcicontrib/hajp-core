Feature: Jenkins Plugin Extension Point.
  Cluster membership status is made available to interested Jenkins plugins.

  Scenario: A Jenkins plugin is notified when the instance becomes a cluster member
    Given: there is plugin P installed on a Jenkins instance
    And: plugin P implements the HAJP extension point
    When: the instance joins the cluster
    Then: plugin P is notified that it is a cluster member

  Scenario: A Jenkins plugin is notified when the instance leaves a cluster
    Given: there is plugin P installed on a Jenkins instance
    And: plugin P implements the HAJP extension point
    When: the instance leaves the cluster
    Then: plugin P is notified that it is a no longer cluster member

  Scenario: A Jenkins plugin is notified when the cluster member becomes the active master
    Given: there is plugin P installed on a Jenkins instance
    And: plugin P implements the HAJP extension point
    When: a cluster member is elected to be the active master
    Then: plugin P is notified that its cluster member is the active master

  Scenario: A Jenkins plugin is notified when the cluster member is no longer the active master
    Given: there is plugin P installed on a Jenkins instance
    And: plugin P implements the HAJP extension point
    When: a cluster member is not elected to be the active master
    Then: plugin P is notified that its cluster member is not the active master


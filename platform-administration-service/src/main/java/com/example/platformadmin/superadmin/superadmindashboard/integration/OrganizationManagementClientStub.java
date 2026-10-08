package com.example.platformadmin.superadmin.superadmindashboard.integration;

/**
	 * TEMPORARY stub - returns mock data so the dashboard pipeline can be built and
	 * tested before the Organization Management service is available.
	 *
	 * TODO: replace with a real RestClient-based implementation once confirmed.
	**/

import com.example.platformadmin.superadmin.superadmindashboard.integration.OrganizationManagementClient;
import org.springframework.stereotype.Component;

@Component("organizationManagementClientStub")
public class OrganizationManagementClientStub implements OrganizationManagementClient {

		  @Override
          public OrganizationStatistics getOrganizationStatistics() {
              return new OrganizationStatistics(58, 12);
          }
}

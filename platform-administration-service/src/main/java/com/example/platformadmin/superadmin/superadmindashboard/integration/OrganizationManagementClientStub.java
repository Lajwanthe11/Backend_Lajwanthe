 package com.example.platformadmin.superadmin.superadmindashboard.integration;
  
  import org.springframework.stereotype.Component;
 
@Component
public class OrganizationManagementClientStub implements
		  OrganizationManagementClient {
		 
		  @Override public OrganizationStatistics getOrganizationStatistics() { return
		  new OrganizationStatistics(58, 12); } }
		 

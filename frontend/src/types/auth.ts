export interface UserInfo {
  sub: string;
  username: string | null;
  email: string | null;
}

export interface AvailableCompany {
  cmpCd: string;
  companyName: string;
  companyNameShort: string | null;
  availableDomains: string[];
}

export interface BootstrapResponse {
  user: UserInfo;
  roles: string[];
  availableCompanies: AvailableCompany[];
  hasIntegrationAccess: boolean;
}

export interface AuthContext {
  user: UserInfo | null;
  roles: string[];
  availableCompanies: AvailableCompany[];
  hasIntegrationAccess: boolean;
  isLoading: boolean;
  isInitialized: boolean;
  error: Error | null;
}

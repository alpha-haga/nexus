/**
 * NEXUS 共通型定義
 *
 * バックエンドと同期した型定義
 */

// ============================================
// ID Types
// ============================================

export type CorporationId = string;
export type PersonId = string;
export type HouseholdId = string;
export type GojoContractId = string;
export type FuneralCaseId = string;
export type BridalCaseId = string;
export type PointAccountId = string;

// ============================================
// Common Types
// ============================================

export interface ApiResponse<T> {
  data: T;
  status: number;
}

export interface ApiError {
  timestamp: string;
  status: number;
  error: string;
  message: string;
  code: string;
  details?: Record<string, unknown>;
}

export interface PaginatedResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  page: number;
  size: number;
}

// ============================================
// Person Types
// ============================================

export type Gender = 'MALE' | 'FEMALE' | 'OTHER';

export interface Person {
  id: PersonId;
  corporationId: CorporationId;
  lastName: string;
  firstName: string;
  fullName: string;
  lastNameKana?: string;
  firstNameKana?: string;
  fullNameKana?: string;
  birthDate?: string;
  gender?: Gender;
  phoneNumber?: string;
  email?: string;
  postalCode?: string;
  prefecture?: string;
  city?: string;
  street?: string;
  building?: string;
}

export interface RegisterPersonRequest {
  corporationId: CorporationId;
  lastName: string;
  firstName: string;
  lastNameKana?: string;
  firstNameKana?: string;
  birthDate?: string;
  gender?: Gender;
  phoneNumber?: string;
  email?: string;
  postalCode?: string;
  prefecture?: string;
  city?: string;
  street?: string;
  building?: string;
}

export interface UpdatePersonRequest {
  lastName?: string;
  firstName?: string;
  lastNameKana?: string;
  firstNameKana?: string;
  birthDate?: string;
  gender?: Gender;
  phoneNumber?: string;
  email?: string;
  postalCode?: string;
  prefecture?: string;
  city?: string;
  street?: string;
  building?: string;
}

// ============================================
// Household Types
// ============================================

export type Relationship =
  | 'HEAD'
  | 'SPOUSE'
  | 'CHILD'
  | 'PARENT'
  | 'GRANDPARENT'
  | 'GRANDCHILD'
  | 'SIBLING'
  | 'OTHER_RELATIVE'
  | 'OTHER';

export interface HouseholdMember {
  personId: PersonId;
  relationship: Relationship;
  isActive: boolean;
}

export interface Household {
  id: HouseholdId;
  corporationId: CorporationId;
  name?: string;
  headPersonId?: PersonId;
  postalCode?: string;
  prefecture?: string;
  city?: string;
  street?: string;
  building?: string;
  members: HouseholdMember[];
}

// ============================================
// Gojo Types
// ============================================

export type ContractStatus =
  | 'ACTIVE'
  | 'MATURED'
  | 'USED'
  | 'CANCELLED'
  | 'SUSPENDED';

export interface GojoContract {
  id: GojoContractId;
  corporationId: CorporationId;
  contractorPersonId: PersonId;
  beneficiaryPersonId?: PersonId;
  planCode: string;
  planName: string;
  monthlyFee: number;
  maturityAmount: number;
  contractDate: string;
  maturityDate?: string;
  status: ContractStatus;
  totalPaidAmount: number;
  progressRate: number;
}

// ============================================
// Funeral Types
// ============================================

export type FuneralStatus =
  | 'INQUIRY'
  | 'CONSULTATION'
  | 'CONTRACTED'
  | 'PREPARATION'
  | 'COMPLETED'
  | 'CANCELLED';

export interface FuneralCase {
  id: FuneralCaseId;
  corporationId: CorporationId;
  chiefMournerPersonId: PersonId;
  deceasedPersonId?: PersonId;
  gojoContractId?: GojoContractId;
  planCode?: string;
  planName?: string;
  ceremonyDate?: string;
  venue?: string;
  totalAmount: number;
  status: FuneralStatus;
}

// ============================================
// Bridal Types
// ============================================

export type BridalStatus =
  | 'INQUIRY'
  | 'CONSULTATION'
  | 'CONTRACTED'
  | 'PREPARATION'
  | 'COMPLETED'
  | 'CANCELLED';

export interface BridalCase {
  id: BridalCaseId;
  corporationId: CorporationId;
  groomPersonId: PersonId;
  bridePersonId: PersonId;
  gojoContractId?: GojoContractId;
  planCode?: string;
  planName?: string;
  ceremonyDate?: string;
  venue?: string;
  guestCount: number;
  totalAmount: number;
  status: BridalStatus;
}

// ============================================
// Point Types
// ============================================

export type AccountStatus = 'ACTIVE' | 'SUSPENDED' | 'CLOSED';
export type TransactionType =
  | 'EARN'
  | 'USE'
  | 'EXPIRE'
  | 'ADJUST'
  | 'TRANSFER';

export interface PointAccount {
  id: PointAccountId;
  corporationId: CorporationId;
  ownerPersonId: PersonId;
  balance: number;
  totalEarned: number;
  totalUsed: number;
  status: AccountStatus;
}

export interface PointTransaction {
  id: number;
  transactionType: TransactionType;
  points: number;
  balanceAfter: number;
  reason?: string;
  referenceId?: string;
  expiresAt?: string;
  createdAt: string;
}

// ============================================
// NextAuth Session Types
// ============================================

/**
 * NextAuth セッション拡張
 * Keycloak から取得した社員情報をセッションに含める
 */
declare module 'next-auth' {
  interface Session {
    user: {
      id: string;
      employeeId: string;
      name: string;
      email: string;
      corporationId?: string;
      corporationName?: string;
    };
    accessToken?: string;
  }

  interface User {
    id: string;
    employeeId: string;
    name: string;
    email: string;
    corporationId?: string;
    corporationName?: string;
  }
}

declare module 'next-auth/jwt' {
  interface JWT {
    id: string;
    employeeId: string;
    corporationId?: string;
    corporationName?: string;
    accessToken?: string;
  }
}

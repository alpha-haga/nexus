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
  code?: string;
  correlationId?: string;
}

export interface PaginatedResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  page: number;
  size: number;
}

// ============================================
// Region Types
// ============================================

export type Region = 'INTEGRATION' | 'SAITAMA' | 'FUKUSHIMA' | 'TOCHIGI' | null;

// ============================================
// Company Types
// ============================================

export interface Company {
  cmpCd: string;
  cmpShortNm: string;
  regionCd?: string | null;
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
//  Group Contract Search Types
// ============================================
export interface GroupContractSearchCondition {
  contractReceiptYmdFrom?: string;
  contractReceiptYmdTo?: string;
  contractNo?: string;
  contractorName?: string;
  telNo?: string;
  staffName?: string;
  cmpCds?: string[];
  bosyuCd?: string;
  courseCd?: string;
  courseName?: string;
  contractStatusKbn?: string;
}

export interface GroupContractSearchResponse {
  // 基本情報
  cmpCd: string;
  cmpShortName: string | null;
  contractNo: string;
  familyNo: string;
  houseNo: string | null;
  familyNameGaiji: string | null;
  firstNameGaiji: string | null;
  familyNameKana: string | null;
  firstNameKana: string | null;
  contractReceiptYmd: string | null; // YYYYMMDD format
  birthday: string | null; // YYYYMMDD format;
  
  // 契約状態
  contractStatusKbn: string | null;
  dmdStopReasonKbn: string | null;
  cancelReasonKbn: string | null;
  cancelStatusKbn: string | null;
  zashuReasonKbn: string | null;
  contractStatus: string | null;
  taskName: string | null;
  statusUpdateYmd: string | null; // YYYYMMDD format;
  
  // コース・保障内容
  courseCd: string | null;
  courseName: string | null;
  shareNum: number | null;
  monthlyPremium: number | null;
  contractGaku: number | null;
  totalSaveNum: number | null;
  totalGaku: number | null;
  
  // 住所
  zipCd: string | null;
  prefName: string | null;
  cityTownName: string | null;
  oazaTownName: string | null;
  azaChomeName: string | null;
  addr1: string | null;
  addr2: string | null;
  
  // 連絡先
  telNo: string | null;
  mobileNo: string | null;
  
  // ポイント
  saPoint: number | null;
  aaPoint: number | null;
  aPoint: number | null;
  newPoint: number | null;
  addPoint: number | null;
  noallwPoint: number | null;
  ssPoint: number | null;
  upPoint: number | null;
  
  // 募集
  entryKbnName: string | null;
  recruitRespBosyuCd: string | null;
  bosyuFamilyNameKanji: string | null;
  bosyuFirstNameKanji: string | null;
  entryRespBosyuCd: string | null;
  entryFamilyNameKanji: string | null;
  entryFirstNameKanji: string | null;
  
  // 供給ランク / 部門
  motoSupplyRankOrgCd: string | null;
  motoSupplyRankOrgName: string | null;
  supplyRankOrgCd: string | null;
  supplyRankOrgName: string | null;
  sectCd: string | null;
  sectName: string | null;
  
  // その他
  anspFlg: string | null;
  agreementKbn: string | null;
  collectOfficeCd: string | null;
  foreclosureFlg: string | null;
  registYmd: string | null; // YYYYMMDD format
  receptionNo: string | null;
}

export interface PaginatedGroupContractResponse {
  content: GroupContractSearchResponse[];
  totalElements: number;
  totalPages: number;
  page: number;
  size: number;
}

export interface GroupContractDetailResponse {
  // 基本識別情報
  cmpCd: string;
  cmpShortName: string | null;
  contractNo: string;
  familyNo: string;
  houseNo: string | null;
  
  // 契約者情報
  familyNameGaiji: string | null;
  firstNameGaiji: string | null;
  familyNameKana: string | null;
  firstNameKana: string | null;
  contractReceiptYmd: string | null; // YYYYMMDD format
  birthday: string | null; // YYYYMMDD format
  
  // 契約状態（表示用材料）
  contractStatusKbn: string | null;
  contractStatusName: string | null;
  dmdStopReasonKbn: string | null;
  dmdStopReasonName: string | null;
  cancelReasonKbn: string | null;
  cancelReasonName: string | null;
  zashuReasonKbn: string | null;
  zashuReasonName: string | null;
  anspApproveKbn: string | null;
  anspApproveName: string | null;
  torikeshiReasonKbn: string | null;
  torikeshiReasonName: string | null;
  ecApproveKbn: string | null;
  ecApproveName: string | null;
  cancelStatusKbn: string | null;
  cancelStatusName: string | null;
  contractStatus: string | null;
  
  // コース情報
  courseCd: string | null;
  courseName: string | null;
  
  // 連絡先
  telNo: string | null;
  mobileNo: string | null;
  
  // 住所
  prefName: string | null;
  cityTownName: string | null;
  addr1: string | null;
  addr2: string | null;
}

// ============================================
// Group Contract Detail Sub-resources (P2-6)
// ============================================

export interface GroupContractContractContentsResponse {
  cmpCd: string;
  contractNo: string;
  attributes: Record<string, string | null>;
}

export interface GroupContractStaffResponse {
  cmpCd: string;
  contractNo: string;
  staffs: Array<{
    role: string;
    roleLabel: string;
    bosyuCd: string | null;
    staffName: string | null;
  }>;
}

export interface GroupContractBankAccountResponse {
  cmpCd: string;
  contractNo: string;
  // 支払方法
  debitMethodKbn: string | null;
  debitMethodName: string | null;
  // 積立方法
  saveMethodKbn: string | null;
  saveMethodName: string | null;
  // 銀行情報
  bankCd: string | null;
  bankName: string | null;
  bankBranchCd: string | null;
  bankBranchName: string | null;
  // 口座情報
  depositorName: string | null;
  accTypeKbn: string | null;
  accNo: string | null;
  accStatusKbn: string | null;
  registrationUpdateYmd: string | null;
  // その他
  abolishFlg: string | null;
  compelMonthPayFlg: string | null;
  monthlyPremium: number | null;
  remainingSaveNum: number | null;
  remainingReceiptGaku: number | null;
  discountGaku: number | null;
  viewFlg: number | null;
}

export interface GroupContractReceiptResponse {
  cmpCd: string;
  contractNo: string;
  receipts: Array<{
    listNo: number | null;
    ym: string | null;
    dmdMethodKbn: string | null;
    dmdRsltKbn: string | null;
    dmdMethodName: string | null;
    dmdRsltName: string | null;
    clientConsignorKbn: string | null;
    clientConsignorName: string | null;
    discountGaku: number | null;
    shareNum: number | null;
    courseMonthlyPremium: number | null;
    receiptReceiptMethodKbn: string | null;
    receiptReceiptMethodName: string | null;
    receiptReceiptYmd: string | null;
    receiptReceiptGaku: number | null;
    receiptNum: number | null;
    pekeReceiptMethodKbn: string | null;
    pekeReceiptReasonKbn: string | null;
    pekeReceiptReasonName: string | null;
    pekeReceiptYmd: string | null;
    pekeReceiptGaku: number | null;
    pekeNum: number | null;
    refundReasonKbn: string | null;
    refundReasonName: string | null;
    refundGaku: number | null;
    refundYmd: string | null;
    count: number | null;
    paymentRec: number | null;
    refundCount: number | null;
    refundPayment: number | null;
    pekeReceiptReasonKbnCd: string | null;
    opeRecFlg: string | null;
    opeUsageKbn: string | null;
    opeUsageName: string | null;
    opeUsagePurposeKbn: string | null;
    opeUsagePurposeName: string | null;
    partUsageGaku: number | null;
    opeYmd: string | null;
  }>;
}

export interface GroupContractPaymentsResponse {
  cmpCd: string;
  contractNo: string;
  payments: Array<{
    id: string;
  }>;
}

export interface GroupContractActivityHistoryResponse {
  cmpCd: string;
  contractNo: string;
  activities: Array<{
    id: string;
  }>;
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
    error?: 'RefreshAccessTokenError' | 'NoRefreshToken';
    dbAccess?: import('@/services/auth/dbAccess').DbAccessClaims;
    dbAccessRaw?: string[];
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
    refreshToken?: string;
    accessTokenExpires?: number;
    error?: 'RefreshAccessTokenError' | 'NoRefreshToken';
  }
}
/**
 * NEXUS DB Access Claim 解析
 * 
 * Backend の DbAccessRoleExtractor.kt と同一の規則で解析する
 * 参照: backend/nexus-bff/src/main/kotlin/nexus/bff/security/DbAccessRoleExtractor.kt
 * 設計正本: docs/architecture/p04-5-keycloak-claims-db-routing.md
 */

/**
 * DomainAccount の union 型（固定）
 */
export type DomainAccount = 'GOJO' | 'FUNERAL' | 'GROUP';

/**
 * 解析済み DB Access Role
 */
export interface DbAccessRole {
  region: string;
  corporation: string;
  domainAccount: DomainAccount;
  raw: string; // 元の role 文字列（trim 済み）
}

/**
 * DB Access Claims の解析結果
 */
export interface DbAccessClaims {
  roles: DbAccessRole[];
  errors: string[];
  raw: string[]; // 元の claim 配列
}

const SEP = '__';
const ALL_LOWER = 'all';
const INTEGRATION = 'integration';
const GROUP = 'GROUP';

/**
 * Base64URL デコード（追加ライブラリ禁止のため自前実装）
 * server (Node.js) でのみ実行可能（Fail Fast）
 */
function base64UrlDecode(str: string): string {
  // Buffer が無い環境では throw（server-only）
  if (typeof Buffer === 'undefined') {
    throw new Error('Base64URL decode は server (Node.js) でのみ実行可能です');
  }

  // Base64URL を標準 Base64 に変換
  // - を + に、_ を / に変換
  let base64 = str.replace(/-/g, '+').replace(/_/g, '/');
  
  // padding を補完
  const padLength = (4 - (base64.length % 4)) % 4;
  base64 += '='.repeat(padLength);
  
  return Buffer.from(base64, 'base64').toString('utf-8');
}

/**
 * JWT token を decode して payload を取得
 * 署名検証は行わない（claim を読むだけ）
 */
function decodeJwtPayload(accessToken: string): unknown {
  try {
    // JWT は header.payload.signature の形式
    const parts = accessToken.split('.');
    if (parts.length !== 3) {
      throw new Error('Invalid JWT format: 3 parts expected');
    }

    // payload 部分を Base64URL decode
    const payloadBase64Url = parts[1];
    const decoded = base64UrlDecode(payloadBase64Url);
    
    return JSON.parse(decoded);
  } catch (error) {
    throw new Error(`Failed to decode JWT payload: ${error instanceof Error ? error.message : String(error)}`);
  }
}

/**
 * JWT payload から nexus_db_access claim を抽出
 * claim無し / 空配列 / 存在 を区別できる形で返す
 */
interface ExtractNexusDbAccessResult {
  raw: string[];
  status: 'missing' | 'empty' | 'present';
}

function extractNexusDbAccess(payload: unknown): ExtractNexusDbAccessResult {
  if (!payload || typeof payload !== 'object') {
    return {
      raw: [],
      status: 'missing',
    };
  }

  const claim = (payload as Record<string, unknown>).nexus_db_access;
  if (!claim) {
    return {
      raw: [],
      status: 'missing',
    };
  }

  if (!Array.isArray(claim)) {
    return {
      raw: [],
      status: 'missing',
    };
  }

  // List<String> として返す（各要素は string である必要がある）
  const raw = claim.filter((item): item is string => typeof item === 'string');
  
  if (raw.length === 0) {
    return {
      raw: [],
      status: 'empty',
    };
  }

  return {
    raw,
    status: 'present',
  };
}

/**
 * DomainAccount の正規化と検証
 */
function normalizeDomainAccount(domain: string): DomainAccount | null {
  const upper = domain.trim().toUpperCase();
  if (upper === 'GOJO' || upper === 'FUNERAL' || upper === 'GROUP') {
    return upper as DomainAccount;
  }
  return null;
}

/**
 * Wildcard 規則の検証
 * integration__ALL__GROUP のみ許可、それ以外の ALL は禁止
 */
function validateWildcard(
  region: string,
  corporation: string,
  domainAccount: string
): string | null {
  const regionLower = region.toLowerCase();
  const corpLower = corporation.toLowerCase();
  const domainUpper = domainAccount.toUpperCase();

  // integration__ALL__GROUP は許可（case-insensitive 比較）
  if (regionLower === INTEGRATION && corpLower === ALL_LOWER && domainUpper === GROUP) {
    return null; // 許可
  }

  // region wildcard は常に禁止
  if (regionLower === ALL_LOWER) {
    return 'Region 側での ALL は禁止されています';
  }

  // corporation wildcard は禁止（integration__ALL__GROUP は既に処理済み）
  if (corpLower === ALL_LOWER) {
    return 'Corporation 側での ALL は禁止されています（integration__ALL__GROUP を除く）';
  }

  return null; // 許可
}

/**
 * nexus_db_access claim を解析して Region / Corporation / DomainAccount を抽出
 * parse 失敗時は errors に記録（設計憲法「状態を隠さない」に準拠）
 */
export function parseDbAccessRoles(raw: string[]): DbAccessClaims {
  const result: DbAccessClaims = {
    roles: [],
    errors: [],
    raw: raw,
  };

  if (!raw || raw.length === 0) {
    result.errors.push('nexus_db_access claim が存在しない、または空配列です');
    return result;
  }

  // role 値は raw（trim のみ）を維持。wildcard 判定用にのみ正規化を使う
  const trimmed = raw.map((r) => r.trim()).filter((r) => r.length > 0);

  for (const rawRole of trimmed) {
    try {
      // 形式: "{region}__{corporation}__{domainAccount}"
      const parts = rawRole.split(SEP);
      if (parts.length !== 3) {
        result.errors.push(
          `nexus_db_access の形式が不正です: ${rawRole} (区切り文字は ${SEP} である必要があります)`
        );
        continue;
      }

      const [regionRaw, corporationRaw, domainAccountRaw] = parts;
      const region = regionRaw.trim();
      const corporation = corporationRaw.trim();
      const domainAccountStr = domainAccountRaw.trim();

      // 空文字チェック
      if (!region || !corporation || !domainAccountStr) {
        result.errors.push(
          `nexus_db_access の要素が不足しています: ${rawRole}`
        );
        continue;
      }

      // DomainAccount の正規化と検証
      const domainAccount = normalizeDomainAccount(domainAccountStr);
      if (!domainAccount) {
        result.errors.push(
          `nexus_db_access の DomainAccount が不正です: ${rawRole} (GOJO / FUNERAL / GROUP のみ許可)`
        );
        continue;
      }

      // Wildcard 規則の検証
      const wildcardError = validateWildcard(region, corporation, domainAccount);
      if (wildcardError) {
        result.errors.push(`nexus_db_access の wildcard 規則違反: ${rawRole} (${wildcardError})`);
        continue;
      }

      // 正常に解析できた場合
      result.roles.push({
        region,
        corporation,
        domainAccount,
        raw: rawRole,
      });
    } catch (error) {
      result.errors.push(
        `nexus_db_access の解析中にエラーが発生しました: ${rawRole} (${error instanceof Error ? error.message : String(error)})`
      );
    }
  }

  return result;
}

/**
 * accessToken から nexus_db_access claim を取得・解析
 * token 無し / decode失敗 / claim無し / 空配列 → errors に残す
 */
export function getDbAccessClaimsFromAccessToken(
  accessToken?: string
): DbAccessClaims {
  // token 無し
  if (!accessToken) {
    return {
      roles: [],
      errors: ['accessToken が存在しません'],
      raw: [],
    };
  }

  try {
    // JWT decode
    const payload = decodeJwtPayload(accessToken);

    // claim 抽出（claim無し / 空配列 / 存在 を区別）
    const extractResult = extractNexusDbAccess(payload);
    if (extractResult.status === 'missing') {
      return {
        roles: [],
        errors: ['nexus_db_access claim が JWT payload に存在しません'],
        raw: [],
      };
    }
    if (extractResult.status === 'empty') {
      return {
        roles: [],
        errors: ['nexus_db_access claim が空配列です'],
        raw: [],
      };
    }

    // 解析（status === 'present' の場合）
    return parseDbAccessRoles(extractResult.raw);
  } catch (error) {
    return {
      roles: [],
      errors: [
        `JWT decode または claim 抽出に失敗しました: ${error instanceof Error ? error.message : String(error)}`,
      ],
      raw: [],
    };
  }
}

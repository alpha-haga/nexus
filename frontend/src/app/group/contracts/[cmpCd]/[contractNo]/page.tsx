'use client';

import { useEffect, useState } from 'react';
import { useSession } from 'next-auth/react';
import { useParams } from 'next/navigation';
import Link from 'next/link';
import { AppLayout } from '@/modules/core';
import { useScreenPermission } from '@/modules/core/auth/useScreenPermission';
import { Forbidden } from '@/modules/core/components/errors/Forbidden';
import { groupService } from '@/services/group';
import type { ApiError, GroupContractDetailResponse } from '@/types';

// TODO 表示用ヘルパーコンポーネント
const TodoCard = ({ title }: { title: string }) => (
  <div className="card p-6">
    <h2 className="text-lg font-semibold text-gray-900 mb-4">{title}</h2>
    <div className="text-sm text-amber-600">
      <p className="font-medium">TODO: 未実装（P2-7 以降で詳細API接続予定）</p>
      <p className="mt-1 text-gray-500">このカードは仕様確定後に項目を追加します。</p>
    </div>
  </div>
);

export default function GroupContractDetailPage() {
  const { data: session, status } = useSession();
  const params = useParams();
  const permission = useScreenPermission('groupContracts');

  // URLパラメータから識別子を取得
  const cmpCd = params?.cmpCd as string | undefined;
  const contractNo = params?.contractNo as string | undefined;

  // API呼び出し状態
  const [data, setData] = useState<GroupContractDetailResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<ApiError | null>(null);

  // API呼び出し
  useEffect(() => {
    if (!cmpCd || !contractNo) {
      setLoading(false);
      return;
    }

    let cancelled = false;
    setLoading(true);
    setError(null);

    groupService
      .getContractDetail(cmpCd, contractNo)
      .then((result) => {
        if (!cancelled) {
          setData(result);
          setLoading(false);
        }
      })
      .catch((err: ApiError) => {
        if (!cancelled) {
          setError(err);
          setLoading(false);
        }
      });

    return () => {
      cancelled = true;
    };
  }, [cmpCd, contractNo]);

  // 認証中は一旦表示（ローディング状態）
  if (status === 'loading') {
    return (
      <AppLayout>
        <div className="w-full max-w-none min-w-0">
          <div className="mb-6">
            <h1 className="text-xl font-semibold text-gray-900">契約詳細</h1>
            <p className="text-sm text-gray-500 mt-1">読み込み中...</p>
          </div>
        </div>
      </AppLayout>
    );
  }

  // 未認証の場合はログイン画面にリダイレクト（NextAuth が処理）
  if (!session) {
    return null;
  }

  // 権限がない場合は 403 表示
  if (!permission.canView) {
    return (
      <Forbidden
        screenName="契約詳細"
        reason={permission.reason}
      />
    );
  }

  // URLパラメータが不正な場合はエラー表示
  if (!cmpCd || !contractNo) {
    return (
      <AppLayout>
        <div className="w-full max-w-none min-w-0">
          <div className="mb-6">
            <h1 className="text-xl font-semibold text-gray-900">契約詳細</h1>
            <p className="text-sm text-red-600 mt-1">
              エラー: 契約識別情報が不正です（法人コード: {cmpCd || '未指定'}, 契約番号: {contractNo || '未指定'}）
            </p>
            <div className="mt-4">
              <Link 
                href="/group/contracts"
                className="text-blue-600 hover:text-blue-800 underline"
              >
                ← 一覧に戻る
              </Link>
            </div>
          </div>
        </div>
      </AppLayout>
    );
  }

  // API読み込み中
  if (loading) {
    return (
      <AppLayout>
        <div className="w-full max-w-none min-w-0">
          <div className="mb-6">
            <h1 className="text-xl font-semibold text-gray-900">契約詳細</h1>
            <p className="text-sm text-gray-500 mt-1">読み込み中...</p>
          </div>
        </div>
      </AppLayout>
    );
  }

  // APIエラー（403以外）
  if (error) {
    if (error.status === 403) {
      return (
        <Forbidden
          screenName="契約詳細"
          reason="この契約へのアクセス権限がありません。"
        />
      );
    }

    if (error.status === 404) {
      return (
        <AppLayout>
          <div className="w-full max-w-none min-w-0">
            <div className="mb-6">
              <h1 className="text-xl font-semibold text-gray-900">契約詳細</h1>
              <p className="text-sm text-red-600 mt-1">
                エラー: 指定された契約は存在しません。
              </p>
              <div className="mt-4">
                <Link
                  href="/group/contracts"
                  className="text-blue-600 hover:text-blue-800 underline"
                >
                  ← 一覧に戻る
                </Link>
              </div>
            </div>
          </div>
        </AppLayout>
      );
    }

    // その他のエラー
    return (
      <AppLayout>
        <div className="w-full max-w-none min-w-0">
          <div className="mb-6">
            <h1 className="text-xl font-semibold text-gray-900">契約詳細</h1>
            <p className="text-sm text-red-600 mt-1">
              エラー: {error.message} (HTTP {error.status})
            </p>
            <div className="mt-4">
              <Link
                href="/group/contracts"
                className="text-blue-600 hover:text-blue-800 underline"
              >
                ← 一覧に戻る
              </Link>
            </div>
          </div>
        </div>
      </AppLayout>
    );
  }

  // データ取得成功
  if (data) {
    return (
      <AppLayout>
        <div className="w-full max-w-none min-w-0">
          <div className="mb-6">
            <div className="flex items-center justify-between">
              <div>
                <h1 className="text-xl font-semibold text-gray-900">契約詳細</h1>
                <p className="text-sm text-gray-500 mt-1">
                  法人コード: <span className="font-mono font-medium">{data.cmpCd}</span> / 契約番号: <span className="font-mono font-medium">{data.contractNo}</span>
                </p>
              </div>
              <div>
                <Link
                  href="/group/contracts"
                  className="text-blue-600 hover:text-blue-800 underline text-sm"
                >
                  ← 一覧に戻る
                </Link>
              </div>
            </div>
          </div>

          <div className="space-y-6">
            {/* 1. 契約基本情報 */}
            <div className="card p-6">
              <h2 className="text-lg font-semibold text-gray-900 mb-4">基本情報</h2>
              <dl className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div>
                  <dt className="text-sm font-medium text-gray-500">法人コード</dt>
                  <dd className="mt-1 text-sm text-gray-900 font-mono">{data.cmpCd}</dd>
                </div>
                <div>
                  <dt className="text-sm font-medium text-gray-500">法人名</dt>
                  <dd className="mt-1 text-sm text-gray-900">{data.cmpShortName || '—'}</dd>
                </div>
                <div>
                  <dt className="text-sm font-medium text-gray-500">契約番号</dt>
                  <dd className="mt-1 text-sm text-gray-900 font-mono">{data.contractNo}</dd>
                </div>
                <div>
                  <dt className="text-sm font-medium text-gray-500">家族番号</dt>
                  <dd className="mt-1 text-sm text-gray-900 font-mono">{data.familyNo}</dd>
                </div>
                <div>
                  <dt className="text-sm font-medium text-gray-500">世帯番号</dt>
                  <dd className="mt-1 text-sm text-gray-900 font-mono">{data.houseNo || '—'}</dd>
                </div>
              </dl>
            </div>

            {/* 2. 契約者情報 */}
            <div className="card p-6">
              <h2 className="text-lg font-semibold text-gray-900 mb-4">契約者情報</h2>
              <dl className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div>
                  <dt className="text-sm font-medium text-gray-500">氏名（漢字）</dt>
                  <dd className="mt-1 text-sm text-gray-900">
                    {data.familyNameGaiji && data.firstNameGaiji
                      ? `${data.familyNameGaiji} ${data.firstNameGaiji}`
                      : '—'}
                  </dd>
                </div>
                <div>
                  <dt className="text-sm font-medium text-gray-500">氏名（カナ）</dt>
                  <dd className="mt-1 text-sm text-gray-900">
                    {data.familyNameKana && data.firstNameKana
                      ? `${data.familyNameKana} ${data.firstNameKana}`
                      : '—'}
                  </dd>
                </div>
                <div>
                  <dt className="text-sm font-medium text-gray-500">契約受付年月日</dt>
                  <dd className="mt-1 text-sm text-gray-900">
                    {data.contractReceiptYmd
                      ? `${data.contractReceiptYmd.slice(0, 4)}/${data.contractReceiptYmd.slice(4, 6)}/${data.contractReceiptYmd.slice(6, 8)}`
                      : '—'}
                  </dd>
                </div>
                <div>
                  <dt className="text-sm font-medium text-gray-500">生年月日</dt>
                  <dd className="mt-1 text-sm text-gray-900">
                    {data.birthday
                      ? `${data.birthday.slice(0, 4)}/${data.birthday.slice(4, 6)}/${data.birthday.slice(6, 8)}`
                      : '—'}
                  </dd>
                </div>
              </dl>
            </div>

            {/* 3. 連絡先 */}
            <div className="card p-6">
              <h2 className="text-lg font-semibold text-gray-900 mb-4">連絡先</h2>
              <dl className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div>
                  <dt className="text-sm font-medium text-gray-500">電話番号</dt>
                  <dd className="mt-1 text-sm text-gray-900">{data.telNo || '—'}</dd>
                </div>
                <div>
                  <dt className="text-sm font-medium text-gray-500">携帯番号</dt>
                  <dd className="mt-1 text-sm text-gray-900">{data.mobileNo || '—'}</dd>
                </div>
              </dl>
            </div>

            {/* 4. 住所 */}
            <div className="card p-6">
              <h2 className="text-lg font-semibold text-gray-900 mb-4">住所</h2>
              <dl className="grid grid-cols-1 gap-4">
                <div>
                  <dt className="text-sm font-medium text-gray-500">住所</dt>
                  <dd className="mt-1 text-sm text-gray-900">
                    {[data.prefName, data.cityTownName, data.addr1, data.addr2]
                      .filter(Boolean)
                      .join(' ') || '—'}
                  </dd>
                </div>
              </dl>
            </div>

            {/* 5. 契約状態 */}
            <div className="card p-6">
              <h2 className="text-lg font-semibold text-gray-900 mb-4">契約状態</h2>
              {data.contractStatus ? (
                <div className="mb-4">
                  <p className="text-base font-medium text-gray-900">{data.contractStatus}</p>
                </div>
              ) : (
                <p className="text-sm text-gray-500 mb-4">契約状態情報がありません</p>
              )}
              {(data.dmdStopReasonName || data.dmdStopReasonKbn || data.cancelReasonName || data.cancelReasonKbn || data.zashuReasonName || data.zashuReasonKbn || data.anspApproveName || data.anspApproveKbn || data.torikeshiReasonName || data.torikeshiReasonKbn || data.ecApproveName || data.ecApproveKbn || data.cancelStatusName || data.cancelStatusKbn) && (
                <div>
                  <h3 className="text-sm font-medium text-gray-700 mb-2">詳細情報</h3>
                  <dl className="grid grid-cols-1 md:grid-cols-2 gap-4">
                    {data.dmdStopReasonName || data.dmdStopReasonKbn ? (
                      <div>
                        <dt className="text-sm font-medium text-gray-500">請求停止理由</dt>
                        <dd className="mt-1 text-sm text-gray-900">{data.dmdStopReasonName || (data.dmdStopReasonKbn ? `[${data.dmdStopReasonKbn}]` : '—')}</dd>
                      </div>
                    ) : null}
                    {data.cancelReasonName || data.cancelReasonKbn ? (
                      <div>
                        <dt className="text-sm font-medium text-gray-500">解約理由</dt>
                        <dd className="mt-1 text-sm text-gray-900">{data.cancelReasonName || (data.cancelReasonKbn ? `[${data.cancelReasonKbn}]` : '—')}</dd>
                      </div>
                    ) : null}
                    {data.zashuReasonName || data.zashuReasonKbn ? (
                      <div>
                        <dt className="text-sm font-medium text-gray-500">雑収理由</dt>
                        <dd className="mt-1 text-sm text-gray-900">{data.zashuReasonName || (data.zashuReasonKbn ? `[${data.zashuReasonKbn}]` : '—')}</dd>
                      </div>
                    ) : null}
                    {data.anspApproveName || data.anspApproveKbn ? (
                      <div>
                        <dt className="text-sm font-medium text-gray-500">ANSP承認</dt>
                        <dd className="mt-1 text-sm text-gray-900">{data.anspApproveName || (data.anspApproveKbn ? `[${data.anspApproveKbn}]` : '—')}</dd>
                      </div>
                    ) : null}
                    {data.torikeshiReasonName || data.torikeshiReasonKbn ? (
                      <div>
                        <dt className="text-sm font-medium text-gray-500">取消理由</dt>
                        <dd className="mt-1 text-sm text-gray-900">{data.torikeshiReasonName || (data.torikeshiReasonKbn ? `[${data.torikeshiReasonKbn}]` : '—')}</dd>
                      </div>
                    ) : null}
                    {data.ecApproveName || data.ecApproveKbn ? (
                      <div>
                        <dt className="text-sm font-medium text-gray-500">EC承認</dt>
                        <dd className="mt-1 text-sm text-gray-900">{data.ecApproveName || (data.ecApproveKbn ? `[${data.ecApproveKbn}]` : '—')}</dd>
                      </div>
                    ) : null}
                    {data.cancelStatusName || data.cancelStatusKbn ? (
                      <div>
                        <dt className="text-sm font-medium text-gray-500">解約状態</dt>
                        <dd className="mt-1 text-sm text-gray-900">{data.cancelStatusName || (data.cancelStatusKbn ? `[${data.cancelStatusKbn}]` : '—')}</dd>
                      </div>
                    ) : null}
                  </dl>
                </div>
              )}
            </div>

            {/* 6. 契約詳細（未実装） */}
            <TodoCard title="契約詳細" />

            {/* 7. 担当者情報（未実装） */}
            <TodoCard title="担当者情報" />

            {/* 8. 口座情報（未実装） */}
            <TodoCard title="口座情報" />

            {/* 9. 入金情報（未実装） */}
            <TodoCard title="入金情報" />

            {/* 10. 対応履歴（未実装） */}
            <TodoCard title="対応履歴" />
          </div>
        </div>
      </AppLayout>
    );
  }

  // フォールバック（通常は到達しない）
  return null;
}

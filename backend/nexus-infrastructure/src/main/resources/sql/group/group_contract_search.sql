/* group_contract_search.sql
 * 法人横断契約一覧（P0: 導線固定用。現時点は空結果）
 */
SELECT
  1 AS dummy
FROM dual
WHERE 1 = 0
OFFSET :offset ROWS FETCH NEXT :limit ROWS ONLY
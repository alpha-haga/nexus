SELECT
  c.contract_id,
  c.corporation_id,
  c.contractor_person_id,
  c.beneficiary_person_id,
  c.plan_code,
  c.plan_name,
  c.monthly_fee,
  c.maturity_amount,
  c.contract_date,
  c.maturity_date,
  c.status
FROM group_contract c
WHERE c.corporation_id = :corporationId
  AND (:personId IS NULL OR c.contractor_person_id = :personId)
ORDER BY c.contract_id
OFFSET :offset ROWS FETCH NEXT :limit ROWS ONLY
SELECT COUNT(1)
FROM group_contract c
WHERE c.corporation_id = :corporationId
  AND (:personId IS NULL OR c.contractor_person_id = :personId)

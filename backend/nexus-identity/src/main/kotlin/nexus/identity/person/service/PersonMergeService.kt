package nexus.identity.person.service

import nexus.core.exception.BusinessRuleViolationException
import nexus.core.exception.PersonMergeConflictException
import nexus.core.id.PersonId
import nexus.identity.person.dto.MergeCandidate
import nexus.identity.person.dto.MatchType
import nexus.identity.person.entity.Person
import nexus.identity.person.repository.PersonRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

/**
 * 人物名寄せサービス
 *
 * 重複した人物レコードを統合する
 * FEDERATION アーキテクチャの中核機能
 */
@Service
@Transactional
class PersonMergeService(
    private val personRepository: PersonRepository,
    private val personService: PersonService
) {

    /**
     * 名寄せ候補を検出
     *
     * 同一人物の可能性がある人物を検索
     */
    @Transactional(readOnly = true)
    fun findMergeCandidates(personId: PersonId): List<MergeCandidate> {
        val person = personService.findById(personId)
        val candidates = mutableListOf<MergeCandidate>()

        // 電話番号一致
        person.phoneNumber?.let { phone ->
            personRepository.findByPhoneNumber(phone)
                .filter { it.id != person.id && !it.isMerged }
                .forEach { candidate ->
                    candidates.add(MergeCandidate(
                        person = candidate,
                        matchType = MatchType.PHONE_NUMBER,
                        score = 0.9
                    ))
                }
        }

        // メール一致
        person.email?.let { email ->
            personRepository.findByEmail(email)
                .filter { it.id != person.id && !it.isMerged }
                .forEach { candidate ->
                    val existing = candidates.find { it.person.id == candidate.id }
                    if (existing != null) {
                        // 複数条件で一致するとスコアアップ
                        candidates.remove(existing)
                        candidates.add(existing.copy(
                            matchType = MatchType.MULTIPLE,
                            score = minOf(existing.score + 0.1, 1.0)
                        ))
                    } else {
                        candidates.add(MergeCandidate(
                            person = candidate,
                            matchType = MatchType.EMAIL,
                            score = 0.85
                        ))
                    }
                }
        }

        // 名前類似 (簡易版)
        person.lastNameKana?.let { kana ->
            personRepository.searchByName(kana)
                .filter { it.id != person.id && !it.isMerged }
                .filter { it.lastNameKana == person.lastNameKana && it.firstNameKana == person.firstNameKana }
                .forEach { candidate ->
                    if (candidates.none { it.person.id == candidate.id }) {
                        candidates.add(MergeCandidate(
                            person = candidate,
                            matchType = MatchType.NAME_KANA,
                            score = 0.7
                        ))
                    }
                }
        }

        return candidates.sortedByDescending { it.score }
    }

    /**
     * 人物を統合
     *
     * @param sourceId 統合元（こちらが消える）
     * @param targetId 統合先（こちらに残る）
     */
    fun merge(sourceId: PersonId, targetId: PersonId): Person {
        if (sourceId == targetId) {
            throw BusinessRuleViolationException(
                "SAME_PERSON_MERGE",
                "Cannot merge person into itself"
            )
        }

        val source = personService.findById(sourceId)
        val target = personService.findById(targetId)

        if (source.isMerged) {
            throw PersonMergeConflictException(
                listOf(sourceId.value),
                "Source person is already merged"
            )
        }

        if (target.isMerged) {
            throw PersonMergeConflictException(
                listOf(targetId.value),
                "Target person is already merged"
            )
        }

        // 統合元を統合済みとしてマーク
        source.mergedIntoId = targetId.value
        source.updatedAt = LocalDateTime.now()
        personRepository.save(source)

        // 統合先に欠損情報を補完
        target.apply {
            if (lastNameKana == null) lastNameKana = source.lastNameKana
            if (firstNameKana == null) firstNameKana = source.firstNameKana
            if (birthDate == null) birthDate = source.birthDate
            if (gender == null) gender = source.gender
            if (phoneNumber == null) phoneNumber = source.phoneNumber
            if (email == null) email = source.email
            if (postalCode == null) postalCode = source.postalCode
            if (prefecture == null) prefecture = source.prefecture
            if (city == null) city = source.city
            if (street == null) street = source.street
            if (building == null) building = source.building
            updatedAt = LocalDateTime.now()
        }

        return personRepository.save(target)
    }
}

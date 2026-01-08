package nexus.batch.job

import nexus.core.id.CorporationId
import nexus.identity.service.PersonService
import nexus.identity.service.RegisterPersonCommand
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.launch.support.RunIdIncrementer
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.batch.item.ItemProcessor
import org.springframework.batch.item.ItemReader
import org.springframework.batch.item.ItemWriter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.transaction.PlatformTransactionManager

/**
 * 人物データ取り込みジョブ
 *
 * 外部システムからのデータを取り込み、identityに登録
 */
@Configuration
class PersonImportJobConfig(
    private val jobRepository: JobRepository,
    private val transactionManager: PlatformTransactionManager,
    private val personService: PersonService
) {

    @Bean
    fun personImportJob(): Job {
        return JobBuilder("personImportJob", jobRepository)
            .incrementer(RunIdIncrementer())
            .start(personImportStep())
            .build()
    }

    @Bean
    fun personImportStep(): Step {
        return StepBuilder("personImportStep", jobRepository)
            .chunk<PersonImportRecord, RegisterPersonCommand>(100, transactionManager)
            .reader(personImportReader())
            .processor(personImportProcessor())
            .writer(personImportWriter())
            .build()
    }

    @Bean
    fun personImportReader(): ItemReader<PersonImportRecord> {
        // TODO: 実際のデータソース（CSV、DB等）からの読み込みを実装
        return ItemReader { null }
    }

    @Bean
    fun personImportProcessor(): ItemProcessor<PersonImportRecord, RegisterPersonCommand> {
        return ItemProcessor { record ->
            RegisterPersonCommand(
                corporationId = CorporationId(record.corporationId),
                lastName = record.lastName,
                firstName = record.firstName,
                lastNameKana = record.lastNameKana,
                firstNameKana = record.firstNameKana,
                phoneNumber = record.phoneNumber,
                email = record.email,
                postalCode = record.postalCode,
                prefecture = record.prefecture,
                city = record.city,
                street = record.street,
                building = record.building
            )
        }
    }

    @Bean
    fun personImportWriter(): ItemWriter<RegisterPersonCommand> {
        return ItemWriter { commands ->
            commands.forEach { command ->
                personService.register(command)
            }
        }
    }
}

/**
 * インポートレコード（外部データ形式）
 */
data class PersonImportRecord(
    val corporationId: String,
    val lastName: String,
    val firstName: String,
    val lastNameKana: String? = null,
    val firstNameKana: String? = null,
    val phoneNumber: String? = null,
    val email: String? = null,
    val postalCode: String? = null,
    val prefecture: String? = null,
    val city: String? = null,
    val street: String? = null,
    val building: String? = null
)

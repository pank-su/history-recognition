package su.pank

import io.getenv
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.serialization.kotlinx.xml.*
import kotlinx.coroutines.delay
import kotlinx.serialization.json.Json
import nl.adaptivity.xmlutil.QName
import nl.adaptivity.xmlutil.XmlReader
import nl.adaptivity.xmlutil.serialization.InputKind
import nl.adaptivity.xmlutil.serialization.UnknownChildHandler
import nl.adaptivity.xmlutil.serialization.XML
import nl.adaptivity.xmlutil.serialization.structure.XmlDescriptor
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.io.path.fileVisitor
import kotlin.io.path.pathString
import kotlin.time.Duration.Companion.seconds

const val s3Url = "https://storage.yandexcloud.net/"
const val speechKitUrl = "https://stt.api.cloud.yandex.net/stt/v3/"
val folderOutput = "${System.getProperty("user.home")}/recognitions/"

val ignoredFiles = listOf<String>("test.mp3", "1.mp3", "10.mp3")

val YaSpeechKitToken by lazy { getenv("YATOKEN") }
val bucketName by lazy { getenv("BUCKET") }


private val xmlClient = HttpClient {
    install(ContentNegotiation) {
        //json(Json{ignoreUnknownKeys = true})
        xml(XML {
            this.unknownChildHandler = object : UnknownChildHandler {
                override fun handleUnknownChildRecovering(
                    input: XmlReader,
                    inputKind: InputKind,
                    descriptor: XmlDescriptor,
                    name: QName?,
                    candidates: Collection<Any>
                ): List<XML.ParsedData<*>> {
                    return listOf()
                }
            }
        })
    }

    install(Logging) {
        level = LogLevel.BODY
    }


    defaultRequest {
        // header(HttpHeaders.Authorization, "")
        header(HttpHeaders.Accept, ContentType.Application.Json)
    }
}

val speechKitClient = HttpClient {
    install(ContentNegotiation) {
        json(Json {
            explicitNulls = false
            ignoreUnknownKeys = true
        })
    }
    install(Logging) {
        level = LogLevel.BODY
    }
    defaultRequest {
        contentType(ContentType.Application.Json)
        //url(speechKitUrl)
        header(HttpHeaders.Authorization, "Api-Key $YaSpeechKitToken")
    }
}

suspend fun getAudioUrls()  = xmlClient.get(s3Url) {
        url {
            requireNotNull(bucketName)
            path(bucketName!!)
        }

    }.body<ListBucketResult>().Contents!!.filter { s3Object -> !ignoredFiles.contains(s3Object.Key) }
        .map { "$s3Url$bucketName/${it.Key}" }


/**
 * @return operation id
 */
suspend fun sendFile(uri: String): String {
    return speechKitClient.post("${speechKitUrl}recognizeFileAsync") {
        this.setBody(
            SpeechKitRequest(
                uri = uri,
                recognitionModel = RecognitionModel(
                    model = "general",
                    audioFormat = AudioFormat(containerAudio = ContainerAudio("MP3")),
                    audioProcessingType = "FULL_DATA",
                    textNormalization = TextNormalization(
                        "TEXT_NORMALIZATION_ENABLED",
                        false,
                        true,
                        "PHONE_FORMATTING_MODE_DISABLED"
                    )
                ),

                speechAnalysis = SpeechAnalysis(false, false)
            )
        )
    }.body<SpeechKitResponse>().id
}

suspend fun waitRecognitionResult(operationId: String): String {
    do {
        val isDone = speechKitClient.get("https://operation.api.cloud.yandex.net/operations/${operationId}")
            .body<SpeechKitResponse>().done
        delay(10.seconds)
    } while (!isDone)
    val json = Json { ignoreUnknownKeys = true }
    val text = speechKitClient.get("${speechKitUrl}getRecognition") {
        url {
            parameters.append("operationId", operationId)
        }
    }.bodyAsText().lines().filterIndexed { index, _ -> index % 6 == 1 }.map{json.decodeFromString<AudioRecognitionResponse>(it)}.joinToString("\n"){it.result.finalRefinement.normalizedText.alternatives.first().text}
    return text
}


fun File.writeToFile(fileName: String, text: String){
    require(this.isDirectory)
    val file = File(this, fileName)
    file.writeText(text)
}

suspend fun main() {
    val directory = File(folderOutput).also { folder -> if (!folder.exists()) folder.mkdirs() }
    getAudioUrls().forEach { uri ->
        val fileName = uri.split("/").last().split(".").first()
        val operationId = sendFile(uri)
        println("${uri} $operationId")
        val text = waitRecognitionResult(operationId)

        directory.writeToFile("$fileName.txt", text)
    }
//    val operationWaitId = ""
//    val text = waitRecognitionResult(operationWaitId)
//    directory.writeToFile("10.txt", text)

    //println(response)
    delay(1.seconds)

}
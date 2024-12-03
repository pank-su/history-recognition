package su.pank

import kotlinx.serialization.Serializable

@Serializable
data class SpeechKitRequest(
    val content: String? = null,
    val uri: String? = null,
    val recognitionModel: RecognitionModel? = null,
    val recognitionClassifier: RecognitionClassifier? = null,
    val speechAnalysis: SpeechAnalysis? = null,
    val speakerLabeling: SpeakerLabeling? = null
)

@Serializable
data class RecognitionModel(
    val model: String,
    val audioFormat: AudioFormat,
    val textNormalization: TextNormalization? = null,
    val languageRestriction: LanguageRestriction? =null,
    val audioProcessingType: String? = null
)

@Serializable
data class AudioFormat(
    val rawAudio: RawAudio? = null,
    val containerAudio: ContainerAudio? = null
)

@Serializable
data class RawAudio(
    val audioEncoding: String,
    val sampleRateHertz: String,
    val audioChannelCount: String
)

@Serializable
data class ContainerAudio(
    val containerAudioType: String
)

@Serializable
data class TextNormalization(
    val textNormalization: String,
    val profanityFilter: Boolean,
    val literatureText: Boolean,
    val phoneFormattingMode: String
)

@Serializable
data class LanguageRestriction(
    val restrictionType: String,
    val languageCode: List<String>
)

@Serializable
data class RecognitionClassifier(
    val classifiers: List<Classifier>
)

@Serializable
data class Classifier(
    val classifier: String,
    val triggers: List<String>
)

@Serializable
data class SpeechAnalysis(
    val enableSpeakerAnalysis: Boolean,
    val enableConversationAnalysis: Boolean,
    val descriptiveStatisticsQuantiles: List<String>? = null
)

@Serializable
data class SpeakerLabeling(
    val speakerLabeling: String
)

@Serializable
data class SpeechKitResponse(
    val id: String,
    val description: String,
    val createdAt: String,
    val createdBy: String,
    val modifiedAt: String,
    val done: Boolean,

    val error: ErrorResponse? = null
)

@Serializable
data class ErrorResponse(
    val code: Int,
    val message: String,
)




@Serializable
data class AudioRecognitionResponse(
    val result: Result
)

@Serializable
data class Result(
    val sessionUuid: SessionUuid,
    val audioCursors: AudioCursors,
    val responseWallTimeMs: String,
    val finalRefinement: FinalRefinement,
    val channelTag: String
)

@Serializable
data class SessionUuid(
    val uuid: String,
    val userRequestId: String
)

@Serializable
data class AudioCursors(
    val receivedDataMs: String,
    val resetTimeMs: String,
    val partialTimeMs: String,
    val finalTimeMs: String,
    val finalIndex: String,
    val eouTimeMs: String
)

@Serializable
data class FinalRefinement(
    val finalIndex: String,
    val normalizedText: NormalizedText,
)

@Serializable
data class NormalizedText(
    val alternatives: List<Alternative>
)

@Serializable
data class Alternative(
    val words: List<Word>,
    val text: String,
    val startTimeMs: String,
    val endTimeMs: String,
    val confidence: Int,
    val languages: List<String>
)

@Serializable
data class Word(
    val text: String,
    val startTimeMs: String,
    val endTimeMs: String
)


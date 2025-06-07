package com.example.cipher.data.model

data class Contact(
    val id: Long,
    val username: String,
    val displayName: String,
    val status: ContactStatus,
    val createdAt: String
)

enum class ContactStatus {
    PENDING,
    ACCEPTED,
    BLOCKED
}

data class Call(
    val id: Long,
    val caller: String,
    val callee: String,
    val status: CallStatus,
    val noiseReductionEnabled: Boolean,
    val createdAt: String,
    val startedAt: String? = null,
    val endedAt: String? = null
)

enum class CallStatus {
    INITIATED,
    RINGING,
    ANSWERED,
    ENDED,
    DECLINED,
    MISSED
}

data class CallSignal(
    val callId: Long,
    val type: CallSignalType,
    val signal: String,
    val sender: String? = null
)

enum class CallSignalType {
    OFFER,
    ANSWER,
    ICE_CANDIDATE,
    CALL_SIGNAL
}

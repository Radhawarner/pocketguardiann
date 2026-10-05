package com.example.data.model

object EmergencyDirectory {
    val resources = listOf(
        EmergencyResource(
            name = "National Emergency Dispatch",
            number = "911",
            description = "Immediate police, fire rescue, and paramedic emergency dispatch.",
            category = "General Emergency",
            isTextSupported = true
        ),
        EmergencyResource(
            name = "International Emergency (GSM/Europe)",
            number = "112",
            description = "Universal emergency standard reachable on all mobile devices worldwide.",
            category = "General Emergency",
            isTextSupported = false
        ),
        EmergencyResource(
            name = "Suicide & Crisis Lifeline",
            number = "988",
            description = "24/7 free, confidential crisis counseling via call or text message.",
            category = "Crisis Support",
            isTextSupported = true
        ),
        EmergencyResource(
            name = "Crisis Text Line",
            number = "741741",
            description = "Discreet text-only support with trained crisis volunteer counselors. Text 'HOME'.",
            category = "Discreet Help",
            isTextSupported = true
        ),
        EmergencyResource(
            name = "Domestic Violence Hotline",
            number = "1-800-799-7233",
            description = "24/7 confidential advocacy, shelter info, and safety planning.",
            category = "Crisis Support",
            isTextSupported = true
        ),
        EmergencyResource(
            name = "RAINN Sexual Assault Hotline",
            number = "1-800-656-4673",
            description = "National sexual assault hotline providing anonymous professional support.",
            category = "Crisis Support",
            isTextSupported = false
        ),
        EmergencyResource(
            name = "Poison Control Center",
            number = "1-800-222-1222",
            description = "Instant medical guidance for accidental poisoning or hazardous exposure.",
            category = "General Emergency",
            isTextSupported = false
        )
    )
}

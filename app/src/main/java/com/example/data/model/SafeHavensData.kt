package com.example.data.model

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

object SafeHavensCatalog {
    val defaultHavens = listOf(
        SafeHavenItem(
            id = "haven_police_central",
            name = "Downtown Police Precinct & Safe Haven",
            category = "Police",
            address = "850 Bryant St, Central District",
            phone = "911",
            latitude = 37.7712,
            longitude = -122.4042,
            isOpen24Hours = true,
            securityLevel = "24/7 Armed Officers & Secure Walk-in Lobby"
        ),
        SafeHavenItem(
            id = "haven_hospital_trauma",
            name = "City Regional Trauma Center & ER",
            category = "Trauma ER",
            address = "1001 Potrero Ave, Medical Wing",
            phone = "+1 (555) 206-8000",
            latitude = 37.7558,
            longitude = -122.4045,
            isOpen24Hours = true,
            securityLevel = "Level 1 Trauma ER, 24/7 Security Desk"
        ),
        SafeHavenItem(
            id = "haven_pharmacy_247",
            name = "Walgreens 24/7 Pharmacy & First Aid",
            category = "24/7 Pharmacy",
            address = "135 Powell St, Union Square",
            phone = "+1 (555) 391-7222",
            latitude = 37.7858,
            longitude = -122.4080,
            isOpen24Hours = true,
            securityLevel = "Well-lit 24h store with on-duty security guard"
        ),
        SafeHavenItem(
            id = "haven_fire_station",
            name = "Municipal Fire & Paramedic Station 3",
            category = "Fire Station",
            address = "1067 Post St, Civic Center",
            phone = "911",
            latitude = 37.7869,
            longitude = -122.4189,
            isOpen24Hours = true,
            securityLevel = "24/7 Active Duty Firefighters & EMTs"
        ),
        SafeHavenItem(
            id = "haven_transit_security",
            name = "Metro Central Transit Security Office",
            category = "Transit Security",
            address = "Civic Center Station Concourse Level",
            phone = "+1 (555) 987-2278",
            latitude = 37.7797,
            longitude = -122.4138,
            isOpen24Hours = true,
            securityLevel = "Transit Police & Monitored CCTV Kiosk"
        ),
        SafeHavenItem(
            id = "haven_campus_safety",
            name = "University Public Safety Escort Station",
            category = "Campus Safety",
            address = "North Campus Gateway, Booth 1",
            phone = "+1 (555) 642-6760",
            latitude = 37.7749,
            longitude = -122.4194,
            isOpen24Hours = true,
            securityLevel = "Blue Light Emergency Phone & Escort Patrols"
        )
    )

    fun calculateSortedHavens(userLat: Double?, userLng: Double?): List<SafeHavenItem> {
        val lat1 = userLat ?: 37.7749
        val lon1 = userLng ?: -122.4194

        return defaultHavens.map { haven ->
            val dist = haversineDistanceMiles(lat1, lon1, haven.latitude, haven.longitude)
            haven.copy(distanceMiles = (dist * 10).roundToInt() / 10f)
        }.sortedBy { it.distanceMiles }
    }

    private fun haversineDistanceMiles(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Float {
        val earthRadiusMiles = 3958.8
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return (earthRadiusMiles * c).toFloat()
    }
}

object EmergencyTranslationsCatalog {
    val languages = listOf("Spanish", "French", "Mandarin", "Hindi", "German", "Arabic")

    val phrases = listOf(
        EmergencyPhrase(
            english = "I need immediate emergency help / Call police!",
            language = "Spanish",
            translation = "¡Necesito ayuda de emergencia inmediata! ¡Llame a la policía!",
            phonetic = "Neh-seh-SEE-toh ah-YOO-dah deh eh-mehr-HEHN-syah. YAH-meh ah lah poh-lee-SEE-ah!"
        ),
        EmergencyPhrase(
            english = "I am being followed / Please let me stay here",
            language = "Spanish",
            translation = "Alguien me está siguiendo. Por favor, ¿puedo quedarme aquí?",
            phonetic = "Ahl-gyen meh eh-STAH see-gyen-doh. Pohr fah-VOHR, pweh-doh keh-DAHR-meh ah-KEE?"
        ),
        EmergencyPhrase(
            english = "I need an ambulance / Medical emergency",
            language = "Spanish",
            translation = "Necesito una ambulancia. Es una emergencia médica.",
            phonetic = "Neh-seh-SEE-toh OO-nah ahm-boo-LAHN-syah. Ehs OO-nah eh-mehr-HEHN-syah MEH-dee-kah."
        ),
        EmergencyPhrase(
            english = "I need immediate emergency help / Call police!",
            language = "French",
            translation = "J'ai besoin d'aide d'urgence immédiatement ! Appelez la police !",
            phonetic = "Zhay buh-ZWAN dehd door-ZHAHNS ee-may-dyaht-MAHN! Ah-play lah poh-LEES!"
        ),
        EmergencyPhrase(
            english = "I am being followed / Please let me stay here",
            language = "French",
            translation = "On me suit. S'il vous plaît, laissez-moi rester ici.",
            phonetic = "Ohn muh SWEE. Seel voo play, lay-say mwah res-tay ee-SEE."
        ),
        EmergencyPhrase(
            english = "I need immediate emergency help / Call police!",
            language = "Mandarin",
            translation = "我需要紧急帮助！请帮我报警！",
            phonetic = "Wǒ xūyào jǐnjí bāngzhù! Qǐng bāng wǒ bàojǐng!"
        ),
        EmergencyPhrase(
            english = "I am being followed / Please let me stay here",
            language = "Mandarin",
            translation = "有人在跟踪我。请让我留在这里。",
            phonetic = "Yǒurén zài gēnzōng wǒ. Qǐng ràng wǒ liú zài zhèlǐ."
        ),
        EmergencyPhrase(
            english = "I need an ambulance / Medical emergency",
            language = "Mandarin",
            translation = "我需要救护车。这是医疗紧急情况。",
            phonetic = "Wǒ xūyào jiùhùchē. Zhè shì yīliáo jǐnjí qíngkuàng."
        ),
        EmergencyPhrase(
            english = "I need immediate emergency help / Call police!",
            language = "Hindi",
            translation = "मुझे तुरंत आपातकालीन मदद चाहिए! कृपया पुलिस को बुलाएं!",
            phonetic = "Mujhe turant aapaatkaleen madad chahiye! Kripya police ko bulaayein!"
        ),
        EmergencyPhrase(
            english = "I am being followed / Please let me stay here",
            language = "Hindi",
            translation = "कोई मेरा पीछा कर रहा है। कृपया मुझे यहां रहने दें।",
            phonetic = "Koi mera peechha kar raha hai. Kripya mujhe yahan rehne dein."
        ),
        EmergencyPhrase(
            english = "I need immediate emergency help / Call police!",
            language = "German",
            translation = "Ich brauche sofortige Nothilfe! Bitte rufen Sie die Polizei!",
            phonetic = "Ikh brow-khe zo-FOR-ti-ge NOT-hil-fe! BI-te ROO-fen zee dee po-lee-TSYE!"
        ),
        EmergencyPhrase(
            english = "I need an ambulance / Medical emergency",
            language = "German",
            translation = "Ich brauche einen Krankenwagen. Es ist ein medizinischer Notfall.",
            phonetic = "Ikh brow-khe EYE-nen KRANK-en-vah-gen. Es ist ein me-dee-TSEE-nee-sher NOT-fahl."
        ),
        EmergencyPhrase(
            english = "I need immediate emergency help / Call police!",
            language = "Arabic",
            translation = "أحتاج إلى مساعدة طارئة فورية! يرجى الاتصال بالشرطة!",
            phonetic = "Ahtaju ila musa'adah tari'ah fawriyyah! Yurja al-ittisal bi-shurtah!"
        )
    )
}

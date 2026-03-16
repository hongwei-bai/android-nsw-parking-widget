package com.melonapp.android_nsw_parking_widget.util

object CarParkUtils {
    private val nameToAbbrMap = mapOf(
        "Park&Ride - Ashfield" to "ASH",
        "Park&Ride - Kogarah" to "KOG",
        "Park&Ride - Seven Hills" to "SVH",
        "Park&Ride - Manly Vale" to "MNV",
        "Park&Ride - Gordon Henry St (north)" to "GON",
        "Park&Ride - Kiama" to "KIA",
        "Park&Ride - Revesby" to "REV",
        "Park&Ride - Narrabeen" to "NAR",
        "Park&Ride - Dee Why" to "DEW",
        "Park&Ride - Mona Vale" to "MON",
        "Park&Ride - Warriewood" to "WAR",
        "Park&Ride - Brookvale" to "BRO",
        "Park&Ride - West Ryde" to "WRY",
        "Park&Ride - Leppington" to "LEP",
        "Park&Ride - Edmondson Park (south)" to "EDS",
        "Park&Ride - Sutherland" to "SUT",
        "Park&Ride - St Marys" to "STM",
        "Park&Ride - Gosford" to "GOS",
        "Park&Ride - Campbelltown Farrow Rd (north)" to "CPN",
        "Park&Ride - Campbelltown Hurley St" to "CPH",
        "Park&Ride - Penrith (at-grade)" to "PEN",
        "Park&Ride - Penrith (multi-level)" to "PEM",
        "Park&Ride - Warwick Farm" to "WAF",
        "Park&Ride - Schofields" to "SCH",
        "Park&Ride - Hornsby" to "HOR",
        "Park&Ride - Tallawong P1" to "TA1",
        "Park&Ride - Tallawong P2" to "TA2",
        "Park&Ride - Tallawong P3" to "TA3",
        "Park&Ride - Kellyville (north)" to "KEN",
        "Park&Ride - Kellyville (south)" to "KES",
        "Park&Ride - Bella Vista" to "BEL",
        "Park&Ride - Hills Showground" to "HIL",
        "Park&Ride - Cherrybrook" to "CHE",
        "Park&Ride - Lindfield Village Green" to "LIN",
        "Park&Ride - Emu Plains" to "EMU",
        "Park&Ride - Beverly Hills" to "BEH",
        "Park&Ride - Riverwood" to "RIV",
        "Park&Ride - North Rocks" to "NRO",
        "Park&Ride - Edmondson Park (north)" to "EDN"
    )

    fun getAbbreviation(fullName: String?): String {
        if (fullName == null) return "???"
        return nameToAbbrMap[fullName] ?: fullName.take(3).uppercase()
    }
}

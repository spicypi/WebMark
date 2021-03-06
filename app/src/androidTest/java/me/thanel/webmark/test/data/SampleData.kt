package me.thanel.webmark.test.data

import androidx.core.net.toUri
import me.thanel.webmark.data.WebMarkQueries

const val TITLE_WEBMARK =
    "Humor, Charm, and Creativity Save Guardians of the Galaxy from Stock Storytelling"
const val LINK_WEBMARK = "http://www.theandrewblog.net/2017/05/03/guardians-of-the-galaxy-2014/"

const val TITLE_BLACK_PANTHER = "Black Panther Makes the MCU a Deeper, Richer Place"
const val TITLE_CAPTAIN_MARVEL =
    "Captain Marvel Is a Throwback to the Earliest MCU Films, Even as It Breaks New Ground"

const val TITLE_ENDGAME_TRAILER = "Avengers: Endgame trailer released!"
const val LINK_ENDGAME_TRAILER = "https://www.scified.com/news/avengers-endgame-trailer-released"

const val TITLE_ARCHIVED_WEBMARK =
    "'Avengers: Endgame' Continues To Outpace 'Avengers: Infinity War's Presells"
const val LINK_ARCHIVED_WEBMARK = "https://mcuexchange.com/avengers-endgame-presells-infinity-war/"

const val LINK_NOT_SAVED = "https://mcucosmic.com/2019/03/13/mcu-phase-4-movies/"

fun WebMarkQueries.insertSampleData() = transaction {
    insert(
        url = LINK_WEBMARK,
        title = TITLE_WEBMARK,
        imageUrl = "http://www.theandrewblog.net/wp-content/uploads/2017/05/gotg1-1024x682.jpg",
        readingTime = 12
    )
    insert(
        url = "http://www.theandrewblog.net/2018/02/19/black-panther/",
        title = TITLE_BLACK_PANTHER,
        faviconUrl = "http://www.theandrewblog.net"
    )
    insert(
        url = "http://www.theandrewblog.net/2019/03/11/captain-marvel-is-a-throwback-to-the-earliest-mcu-films-even-as-it-breaks-new-ground/",
        title = TITLE_CAPTAIN_MARVEL,
        imageUrl = "http://www.theandrewblog.net/wp-content/uploads/2019/03/cm1.jpg",
        faviconUrl = "http://www.theandrewblog.net",
        readingTime = 360
    )
    insert(
        url = LINK_ENDGAME_TRAILER,
        title = TITLE_ENDGAME_TRAILER
    )

    insert(
        url = LINK_ARCHIVED_WEBMARK,
        title = TITLE_ARCHIVED_WEBMARK,
        archived = true
    )
}

private fun WebMarkQueries.insert(
    url: String,
    title: String,
    imageUrl: String? = null,
    readingTime: Int = 0,
    archived: Boolean = false,
    faviconUrl: String? = null
) {
    insert(null, url.toUri())
    val id = lastInsertId().executeAsOne()
    updateById(title, faviconUrl?.toUri(), readingTime, imageUrl?.toUri(), id)
    if (archived) {
        archiveById(id)
    }
}

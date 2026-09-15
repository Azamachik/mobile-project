fun main() {
    val song = Song("Bohemian Rhapsody", "Queen", 1975, 12000)

    song.printDescription()
    println("Is the song popular? ${song.isPopular}")
}

class Song(
    val title: String,
    val artist: String,
    val yearPublished: Int,
    val playCount: Int
) {
    val isPopular: Boolean
        get() = playCount >= 1000

    fun printDescription() {
        println("$title, performed by $artist, was released in $yearPublished.")
    }
}
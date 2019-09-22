fun main() {
    val store = DiskStore()
    val crawler = Crawler(store)
    crawler.crawl("https://google.com")
//    val robotParser = RobotsParser("SatomiBot")
//    val sites = listOf(
//            "google.com",
//            "facebook.com",
//            "hltv.org",
//            "pornhub.com",
//            "reddit.com",
//            "wikipedia.com",
//            "studydojo.ninja"
//        ).asSequence()
//    sites.map {
//        it to robotParser.parse(crawler.getRobot(it).split("\n"))
//    }.forEach(::println)
}
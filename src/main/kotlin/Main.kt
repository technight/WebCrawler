import crawler.SynchronousCrawler
import frontier.SimpleFrontier
import robot.RobotsCrawler
import store.DiskStore

fun main() {
    val store = DiskStore()
    val robotsCrawler = RobotsCrawler("SatomiBot")
    val frontier = SimpleFrontier()
    val crawler = SynchronousCrawler(store, robotsCrawler, frontier)
    crawler.crawl("https://webassembly.org/")
}
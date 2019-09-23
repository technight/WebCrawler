package robot

sealed class RobotDecision {
    object CanCrawl: RobotDecision()
    object NoCrawl: RobotDecision()
    data class CrawlDelayed(val delay: Int): RobotDecision()
}
package robot

sealed class RobotRule {
    data class Disallow(val path: String): RobotRule()
    data class Delay(val delay: Int): RobotRule()
}
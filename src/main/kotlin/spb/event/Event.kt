package spb.event

/**
 * @author Kai
 */
abstract class Event(var timeToExecute : Int) {
    var isRunning = true
    abstract fun run()
}
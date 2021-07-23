package spb.event

/**
 * @author Kai
 */
abstract class Event(var delay : Int) {

    var isRunning = true

    abstract fun run()
}
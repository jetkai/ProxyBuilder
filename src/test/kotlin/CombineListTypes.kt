import kotlinx.serialization.ExperimentalSerializationApi
import spb.util.Config

@ExperimentalSerializationApi
class CombineListTypes {

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            Config.init()
            CombineListTypes().combine()
        }
    }

    /**
     * Testing Compatability between combining lists
     */

    private fun combine() {
        val joinListsToString =
            getArrayString()
                .plus(getListString())
                .plus(getMutibleListString())
                .joinToString()
        println(joinListsToString)
    }

    private fun getArrayString() : Array<String> {
        return arrayOf("Test", "Test2", "Test3")
    }

    private fun getListString() : List<String> {
        return arrayOf("Test4", "Test5").toList()
    }

    private fun getMutibleListString() : MutableList<String> {
        return arrayOf("Test6", "Test7").toMutableList()
    }

}
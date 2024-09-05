import kotlinx.coroutines.runBlocking
import org.junit.Test

annotation class Test {
    @Test
    @Throws(Exception::class)
    fun test() = runBlocking {

    }
}

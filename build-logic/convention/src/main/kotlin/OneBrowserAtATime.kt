import org.gradle.api.services.BuildService
import org.gradle.api.services.BuildServiceParameters

/**
 * Lets one module's browser tests run at a time (echochat.web.tools). Several headless Chromes at
 * once starve each other, and a test page then goes quiet long enough for Karma to give up on it.
 */
abstract class OneBrowserAtATime : BuildService<BuildServiceParameters.None>

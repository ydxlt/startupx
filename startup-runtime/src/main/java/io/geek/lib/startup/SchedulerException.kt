package io.geek.lib.startup

import java.lang.RuntimeException

internal class SchedulerException : RuntimeException {
    constructor(message: String?) : super(message)
    constructor(cause: Throwable?) : super(cause)
    constructor(message: String?, cause: Throwable?) : super(message, cause)
}

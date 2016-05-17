package net.contrapt.dbidea.model

import com.intellij.openapi.application.Application

/**
 * Created by mark on 4/18/16.
 *
 * Wrapper for intellij invocation mechanism
 */
class UIInvoker(val application : Application) {

    fun invokeLater(task: () -> Unit) : Unit {
        application.invokeLater { task.invoke() }
    }
}
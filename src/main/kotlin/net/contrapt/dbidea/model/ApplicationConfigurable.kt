package net.contrapt.dbidea.model

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.options.SearchableConfigurable
import net.contrapt.dbidea.DBIdea
import net.contrapt.dbidea.controller.ApplicationController
import net.contrapt.dbidea.ui.ConnectionComponent
import net.contrapt.dbidea.ui.DriverComponent

/**
 * Parent configurable component for the application data
 */
class ApplicationConfigurable : SearchableConfigurable.Parent.Abstract() {

    override fun buildConfigurables(): Array<out Configurable>? {
        return arrayOf(DriverComponent(), ConnectionComponent())
    }

    override fun getId(): String {
        return DBIdea.APP_ID
    }

    override fun getDisplayName(): String? {
        return DBIdea.APP_NAME
    }

    override fun getHelpTopic(): String? {
        return ""
    }

    val logger : Logger = Logger.getInstance(javaClass)

    val applicationController : ApplicationController

    init {
        logger.warn("Creating app config")
        applicationController = ApplicationManager.getApplication().getComponent(ApplicationController::class.java)
    }

}
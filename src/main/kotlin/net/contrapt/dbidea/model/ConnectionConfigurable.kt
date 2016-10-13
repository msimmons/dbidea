package net.contrapt.dbidea.model

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.ui.NamedConfigurable
import net.contrapt.dbidea.controller.ApplicationController
import net.contrapt.dbidea.controller.ConnectionData
import net.contrapt.dbidea.ui.ConnectionDataPanel
import javax.swing.JComponent

/**
 * Congfigurable component for connection data
 */
class ConnectionConfigurable(val connection : ConnectionData, val updater: Runnable) : NamedConfigurable<ConnectionData>() {

    val logger : Logger = Logger.getInstance(javaClass)

    val applicationController : ApplicationController
    var original: ConnectionData
    var isNew : Boolean = false
    lateinit var connectionPanel : ConnectionDataPanel

    init {
        logger.warn("Creating connection config")
        applicationController = ApplicationManager.getApplication().getComponent(ApplicationController::class.java)
        original = connection.deepCopy()
    }

    override fun getBannerSlogan(): String? {
        return "banner slogan"
    }

    override fun getEditableObject(): ConnectionData? {
        logger.debug("Getting editable object $connection")
        return connection
    }

    override fun createOptionsPanel(): JComponent? {
        connectionPanel = ConnectionDataPanel(connection, applicationController.applicationData, isNew)
        return connectionPanel
    }

    override fun setDisplayName(p0: String?) {
        connection.name = p0 ?: ""
    }

    override fun isModified(): Boolean {
        logger.warn("isModified")
        logger.warn("${original.schemas} ?= ${connection.schemas}")
        return original != connection
    }

    override fun disposeUIResources() {
    }

    override fun apply() {
        logger.warn("Applying $connection")
        applicationController.updateConnection(connection)
        original = connection.deepCopy()
        isNew = false
        connectionPanel.apply()
    }

    override fun reset() {
        logger.warn("Resetting to $original -- why?")
    }

    override fun getDisplayName(): String? {
        return connection.name
    }

    override fun getHelpTopic(): String? {
        return null
    }
}
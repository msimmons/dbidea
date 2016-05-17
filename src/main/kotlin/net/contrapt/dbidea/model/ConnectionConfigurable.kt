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
    val connectionPanel: ConnectionDataPanel
    var original: ConnectionData
    var _modified : Boolean = false

    init {
        logger.warn("Creating connection config")
        applicationController = ApplicationManager.getApplication().getComponent(ApplicationController::class.java)
        original = connection.copy()
        connectionPanel = ConnectionDataPanel(connection)
    }

    override fun getBannerSlogan(): String? {
        return "banner slogan"
    }

    override fun getEditableObject(): ConnectionData? {
        logger.warn("Getting editable object $connection")
        return connection
    }

    override fun createOptionsPanel(): JComponent? {
        return connectionPanel
    }

    override fun setDisplayName(p0: String?) {
        connection.name = p0 ?: ""
    }

    override fun isModified(): Boolean {
        logger.warn("isModified")
        return original != connection
    }

    fun setModified(value: Boolean) {
        logger.warn("setModified=$value")
        _modified = value
    }

    override fun disposeUIResources() {
        //throw UnsupportedOperationException()
    }

    override fun apply() {
        logger.warn("Applying $connection")
        applicationController.applicationData.updateConnection(connection)
        original = connection.copy()
        //throw UnsupportedOperationException()
    }

    override fun reset() {
        logger.warn("Resetting to $original -- why?")
        //editable = connection.copy()
        //throw UnsupportedOperationException()
    }

    override fun getDisplayName(): String? {
        return connection.name
    }

    override fun getHelpTopic(): String? {
        return null
        //throw UnsupportedOperationException()
    }
}
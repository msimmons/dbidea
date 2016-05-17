package net.contrapt.dbidea.model

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.options.SearchableConfigurable
import net.contrapt.dbidea.controller.ApplicationController
import net.contrapt.dbidea.ui.ConnectionComponent
import net.contrapt.dbidea.ui.DriverComponent
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.JTextField

/**
 * Parent configurable component for the application data
 */
class ApplicationConfigurable : SearchableConfigurable.Parent.Abstract() {

    override fun buildConfigurables(): Array<out Configurable>? {
        return arrayOf(ConnectionComponent(), DriverComponent())
    }

    override fun getId(): String {
        return "DbIdea"
    }

    override fun getDisplayName(): String? {
        return "DbIdea"
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

    /*
    override fun isModified(): Boolean {
        return false;
        //throw UnsupportedOperationException()
    }

    override fun disposeUIResources() {
        //throw UnsupportedOperationException()
    }

    override fun apply() {
        //throw UnsupportedOperationException()
    }

    override fun createComponent(): JComponent? {
        return JPanel()
        //throw UnsupportedOperationException()
    }

    override fun reset() {
        //throw UnsupportedOperationException()
    }

    override fun getDisplayName(): String? {
        return "DbIdea"
        //throw UnsupportedOperationException()
    }

    override fun getHelpTopic(): String? {
        return null
        //throw UnsupportedOperationException()
    }
    */
}
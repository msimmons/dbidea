package net.contrapt.dbidea.model

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.ui.NamedConfigurable
import net.contrapt.dbidea.controller.ApplicationController
import net.contrapt.dbidea.controller.DriverData
import net.contrapt.dbidea.ui.DriverDataPanel
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.JTextField

/**
 * Configurable component for driver data
 */
class DriverConfigurable(val driver: DriverData, val updater: Runnable) : NamedConfigurable<DriverData>() {

    val logger : Logger = Logger.getInstance(javaClass)

    val applicationController : ApplicationController
    val driverPanel: DriverDataPanel
    var _modified : Boolean = false

    init {
        logger.warn("Creating driver config")
        applicationController = ApplicationManager.getApplication().getComponent(ApplicationController::class.java)
        driverPanel = DriverDataPanel()
    }

    override fun getBannerSlogan(): String? {
        return "banner slogan"
    }

    override fun getEditableObject(): DriverData? {
        logger.warn("Getting editable object")
        return driver
    }

    override fun createOptionsPanel(): JComponent? {
        return driverPanel
    }

    override fun setDisplayName(p0: String?) {
        driver.name = p0 ?: ""
    }

    override fun isModified(): Boolean {
        return _modified;
    }

    fun setModified(value: Boolean) {
        _modified = value
    }

    override fun disposeUIResources() {
        //throw UnsupportedOperationException()
    }

    override fun apply() {
        //throw UnsupportedOperationException()
    }

    override fun reset() {
        //throw UnsupportedOperationException()
    }

    override fun getDisplayName(): String? {
        return driver.name
    }

    override fun getHelpTopic(): String? {
        return null
        //throw UnsupportedOperationException()
    }
}
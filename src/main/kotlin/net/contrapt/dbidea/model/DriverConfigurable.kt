package net.contrapt.dbidea.model

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.ui.NamedConfigurable
import net.contrapt.dbidea.controller.ApplicationController
import net.contrapt.dbidea.controller.DriverData
import net.contrapt.dbidea.ui.DriverDataPanel
import javax.swing.JComponent

/**
 * Configurable component for driver data
 */
class DriverConfigurable(val driver: DriverData, val updater: Runnable) : NamedConfigurable<DriverData>() {

    val logger : Logger = Logger.getInstance(javaClass)

    val applicationController : ApplicationController
    lateinit var driverPanel: DriverDataPanel
    var original : DriverData
    var isNew : Boolean = false

    init {
        logger.warn("Creating driver config")
        applicationController = ApplicationManager.getApplication().getComponent(ApplicationController::class.java)
        original = driver.copy()
    }

    override fun getBannerSlogan(): String? {
        return "banner slogan"
    }

    override fun getEditableObject(): DriverData? {
        logger.debug("Getting editable object")
        return driver
    }

    override fun createOptionsPanel(): JComponent? {
        driverPanel = DriverDataPanel(driver, isNew)
        return driverPanel
    }

    override fun setDisplayName(p0: String?) {
        driver.name = p0 ?: ""
    }

    override fun isModified(): Boolean {
        return original != driver
    }

    override fun disposeUIResources() {
        //throw UnsupportedOperationException()
    }

    override fun apply() {
        logger.debug("Applying $driver")
        applicationController.applicationData.updateDriver(driver)
        original = driver.copy()
        isNew = false
        driverPanel.apply()
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
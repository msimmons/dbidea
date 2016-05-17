package net.contrapt.dbidea.controller

import com.intellij.openapi.components.*

/**
 * Application level controller, takes care of application level configs
 */
@State(name="applicationConfig", reloadable = true, storages = arrayOf(Storage(id="dbidea", file= "\$APP_CONFIG\$/dbidea.xml")))
class ApplicationController : ApplicationComponent, PersistentStateComponent<ApplicationData> {

    var applicationData: ApplicationData = ApplicationData("DbIdea")

    override fun getComponentName(): String {
        return "DbIdea"
    }

    override fun disposeComponent() {
        println("disposeComponent ${applicationData.connections}")
    }

    override fun initComponent() {
        println("initComponent ${applicationData.connections}")
    }

    override fun loadState(p0: ApplicationData?) {
        println("loadState = $p0")
        applicationData = p0 ?: ApplicationData()
    }

    override fun getState(): ApplicationData? {
        println("getState = ${applicationData.connections}")
        return applicationData
    }
}
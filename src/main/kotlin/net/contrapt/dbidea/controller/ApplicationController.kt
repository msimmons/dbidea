package net.contrapt.dbidea.controller

import com.intellij.openapi.components.ApplicationComponent
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import org.apache.tomcat.jdbc.pool.DataSource
import org.springframework.jdbc.datasource.SimpleDriverDataSource
import java.sql.Driver

/**
 * Application level controller, takes care of application level configs
 */
@State(name="applicationConfig", reloadable = true, storages = arrayOf(Storage(id="dbidea", file= "\$APP_CONFIG\$/dbidea.xml")))
class ApplicationController : ApplicationComponent, PersistentStateComponent<ApplicationData> {

    var applicationData: ApplicationData = ApplicationData("DbIdea")

    var pools : MutableMap<String, DataSource> = mutableMapOf()

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
        applicationData = p0 ?: ApplicationData()
    }

    override fun getState(): ApplicationData? {
        return applicationData
    }

    /**
     * Get or create the connection pool for the given connection name
     */
    fun getPool(connectionName : String) : DataSource {
        return pools.getOrPut(connectionName, {createPool(connectionName)})
    }

    /**
     * Create a new connection pool
     */
    fun createPool(connectionName: String) : DataSource {
        val connectionData = applicationData.connections.find { it.name == connectionName } ?: throw IllegalArgumentException("No such connection $connectionName")
        val driverData = applicationData.drivers.find { it.name == connectionData.driver } ?: throw IllegalArgumentException("No such driver ${connectionData.driver}")
        val driver = Class.forName(driverData.className).newInstance() as Driver
        val pool = DataSource()
        pool.dataSource = SimpleDriverDataSource(driver, connectionData.url, connectionData.user, connectionData.password)
        pool.initialSize = 2
        pool.maxActive = 100
        pool.maxIdle = 2
        pool.isTestOnBorrow = true
        pool.validationQuery = "select user() from dual" //TODO make part of connection config
        pool.defaultAutoCommit = false
        return pool
    }

}
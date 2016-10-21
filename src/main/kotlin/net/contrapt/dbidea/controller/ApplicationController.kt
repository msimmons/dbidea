package net.contrapt.dbidea.controller

import com.intellij.openapi.components.ApplicationComponent
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.diagnostic.Logger
import net.contrapt.dbidea.DBIdea
import org.apache.tomcat.jdbc.pool.DataSource
import org.springframework.jdbc.datasource.SimpleDriverDataSource
import java.sql.Driver

/**
 * Application level controller, takes care of application level configs and operations on them
 */
@State(name="applicationConfig", reloadable = true, storages = arrayOf(Storage(id=DBIdea.APP_ID, file= "\$APP_CONFIG\$/dbidea.xml")))
class ApplicationController : ApplicationComponent, PersistentStateComponent<ApplicationData> {

    val logger : Logger = Logger.getInstance(javaClass)

    var applicationData: ApplicationData = ApplicationData(DBIdea.APP_NAME)

    var pools : MutableMap<String, DataSource> = mutableMapOf()

    override fun getComponentName(): String {
        return DBIdea.APP_NAME
    }

    override fun disposeComponent() {
        logger.debug("disposeComponent ${applicationData.connections}")
    }

    override fun initComponent() {
        logger.debug("initComponent ${applicationData.connections}")
    }

    override fun loadState(p0: ApplicationData?) {
        applicationData = p0 ?: ApplicationData()
    }

    override fun getState(): ApplicationData? {
        return applicationData
    }

    /**
     * Get the connection for the given name
     */
    fun getConnection(connectionName: String) : ConnectionData {
        return applicationData.connections.find { it.name == connectionName } ?: throw IllegalArgumentException("No connection found with name $connectionName")
    }

    /**
     * Update existing connection with the given ConnectionData
     */
    fun updateConnection(connectionData: ConnectionData) {
        val found = applicationData.connections.indexOfFirst {
            it.name == connectionData.name
        }
        if ( found >= 0 ) {
            applicationData.connections.removeAt(found)
            removePool(connectionData.name)
        }
        applicationData.connections.add(connectionData)
    }

    /**
     * Get the driver for the given name
     */
    fun getDriver(driverName: String) : DriverData {
        return applicationData.drivers.find { it.name == driverName } ?: throw IllegalArgumentException("No driver found with name $driverName")
    }

    /**
     * Update existing driver with the given DriverData
     */
    fun updateDriver(driverData: DriverData) {
        val found = applicationData.drivers.indexOfFirst {
            it.name == driverData.name
        }
        if ( found >= 0 ) {
            applicationData.drivers.removeAt(found)
        }
        applicationData.drivers.add(driverData)
    }

    /**
     * Remove the given connection, remove the pool if one exists
     */
    fun removeConnection(connection: ConnectionData) {
        val found = applicationData.connections.indexOfFirst { it.name == connection.name }
        if ( found >= 0 ) {
            applicationData.connections.removeAt(found)
            removePool(connection.name)
        }
    }

    /**
     * Remove the given driver, only if it is not referenced by any connection
     */
    fun removeDriver(driver: DriverData) {
        if (applicationData.connections.any { it.driver == driver.name }) {
            throw IllegalStateException("Cannot remove driver that is still referenced")
        }
        val found = applicationData.drivers.indexOfFirst { it.name == driver.name }
        if ( found >= 0 ) {
            applicationData.drivers.removeAt(found)
        }
    }

    /**
     * Does the given driver exist
     */
    fun driverExists(driver: DriverData): Boolean {
        return applicationData.drivers.any{ it.name==driver.name}
    }

    /**
     * Get and/or create the connection pool for the given connection name
     */
    fun getPool(connectionData: ConnectionData) : DataSource {
        return pools.getOrPut(connectionData.name, {createPool(connectionData)})
    }

    /**
     * Create a new connection pool
     */
    private fun createPool(connectionData: ConnectionData) : DataSource {
        val driverData = getDriver(connectionData.driver)
        val driver = Class.forName(driverData.className).newInstance() as Driver
        val pool = DataSource()
        pool.dataSource = SimpleDriverDataSource(driver, connectionData.url, connectionData.user, connectionData.password)
        pool.initialSize = 2
        pool.maxActive = 100
        pool.maxIdle = 2
        pool.minIdle = 2
        pool.isTestOnBorrow = true
        pool.validationQuery = "select user() from dual" //TODO make part of connection config
        pool.defaultAutoCommit = connectionData.autocommit
        return pool
    }

    /**
     * Remove and close the pool with the given name (usually when connection is removed)
     */
    fun removePool(connectionName: String) {
        pools.remove(connectionName)?.close(true)
    }

}
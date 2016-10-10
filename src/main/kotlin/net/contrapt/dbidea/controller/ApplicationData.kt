package net.contrapt.dbidea.controller

/**
 * Created by mark on 4/21/16.
 */
data class ApplicationData(var name : String ="DbIdea") {

    constructor() : this("DbIdea")

    var drivers : MutableList<DriverData> = mutableListOf()

    var connections : MutableList<ConnectionData> = mutableListOf()

    fun updateConnection(connectionData: ConnectionData) {
        val found = connections.indexOfFirst {
            it.name == connectionData.name
        }
        if ( found >= 0 ) {
            connections.removeAt(found)
        }
        connections.add(connectionData)
    }

    fun updateDriver(driverData: DriverData) {
        val found = drivers.indexOfFirst {
            it.name == driverData.name
        }
        if ( found >= 0 ) {
            drivers.removeAt(found)
        }
        drivers.add(driverData)
    }

    fun removeConnection(connection: ConnectionData) {
        val found = connections.indexOfFirst { it.name == connection.name }
        if ( found >= 0 ) {
            connections.removeAt(found)
        }
    }

    fun removeDriver(driver: DriverData) {
        if (connections.any { it.driver == driver.name }) {
            throw IllegalStateException("Cannot remove driver that is still referenced")
        }
        val found = drivers.indexOfFirst { it.name == driver.name }
        if ( found >= 0 ) {
            drivers.removeAt(found)
        }
    }

    fun driverExists(driver: DriverData): Boolean {
        return drivers.any{ it.name==driver.name}
    }
}
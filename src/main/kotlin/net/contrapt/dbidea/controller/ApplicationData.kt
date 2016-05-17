package net.contrapt.dbidea.controller

import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage

/**
 * Created by mark on 4/21/16.
 */
data class ApplicationData(var name : String ="DbIdea") {

    public constructor() : this("DbIdea")

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
}
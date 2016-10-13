package net.contrapt.dbidea.controller

import net.contrapt.dbidea.DBIdea

/**
 * Created by mark on 4/21/16.
 * Container for the various types of configuration data we need
 */
data class ApplicationData(var name : String = DBIdea.APP_NAME) {

    constructor() : this(DBIdea.APP_NAME)

    var drivers : MutableList<DriverData> = mutableListOf()

    var connections : MutableList<ConnectionData> = mutableListOf()

}
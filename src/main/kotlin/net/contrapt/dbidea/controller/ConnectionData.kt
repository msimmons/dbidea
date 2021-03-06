package net.contrapt.dbidea.controller

/**
 * Created by mark on 4/21/16.
 * Data needed to describe a database connection pool
 */
data class ConnectionData(
        var name : String = "",
        var driver : String = "",
        var url : String = "",
        var user : String = "",
        var password : String = "",
        var autocommit : Boolean = false,
        var fetchLimit : Int = 500,
        var schemas : MutableList<String> = mutableListOf()
) {

    fun deepCopy() : ConnectionData {
        val copy = this.copy()
        copy.schemas = mutableListOf()
        copy.schemas.addAll(schemas)
        return copy
    }
}
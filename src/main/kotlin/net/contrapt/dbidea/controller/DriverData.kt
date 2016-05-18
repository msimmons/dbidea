package net.contrapt.dbidea.controller

/**
 * Describes a jdbc driver configuration
 */
data class DriverData(
        var name : String = "",
        var className : String = "",
        var jarFile : String = ""
) {
}
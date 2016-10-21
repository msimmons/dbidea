package net.contrapt.dbidea.model

import net.contrapt.dbidea.controller.ConnectionData
import java.sql.Connection
import java.sql.ResultSet
import javax.swing.tree.DefaultMutableTreeNode

/**
 * Various types of nodes for displaying the schema object tree
 */
sealed class DBTreeNode : DefaultMutableTreeNode(null) {

    abstract fun addChildren(connection: Connection)
    abstract fun getDetails(connection: Connection)

    /**
     * A ConnectionTreeNode represents the connection and has schema and catalog as children
     */
    class ConnectionTreeNode(val data: ConnectionData) : DBTreeNode() {

        override fun addChildren(connection: Connection) {
            val catalogRows = connection.metaData.catalogs
            while (catalogRows.next()) {
                add(CatalogTreeNode(catalogRows.getString(1)))
            }
            val schemaRows = connection.metaData.schemas
            while (schemaRows.next()) {
                add(SchemaTreeNode(schemaRows.getString(1)))
            }
        }

        override fun getDetails(connection: Connection) {}

        override fun toString() = data.name
    }

    /**
     * Represents a catalog (?)
     */
    class CatalogTreeNode(val catalogName: String) : DBTreeNode() {
        override fun toString() = "Catalog: $catalogName"

        override fun addChildren(connection: Connection) {
        }

        override fun getDetails(connection: Connection) {}
    }

    /**
     * A SchemaTreeNode represents a schema and has database objects as children
     */
    class SchemaTreeNode(val schemaName: String) : DBTreeNode() {
        override fun toString() = "Schema: $schemaName"

        override fun addChildren(connection: Connection) {
            val objectsByType = mutableMapOf<String, MutableSet<TableTreeNode>>()
            val tables = connection.metaData.getTables(null, schemaName, null, null)
            while (tables.next()) {
                val node = TableTreeNode(tables)
                val objectSet = objectsByType.getOrElse(node.type, {mutableSetOf<TableTreeNode>()})
                objectSet.add(node)
                objectsByType.put(node.type, objectSet)
            }
            objectsByType.keys.forEach {
                val typeNode = DefaultMutableTreeNode(it)
                objectsByType[it]?.forEach {
                    typeNode.add(it)
                }
                add(typeNode)
            }
            // Procedures
            val procedures = connection.metaData.getProcedures(null, schemaName, null)
            val procedureNode = DefaultMutableTreeNode("PROCEDURE")
            while (procedures.next()) {
                procedureNode.add(ProcedureTreeNode(procedures))
            }
            if (procedureNode.childCount>0) add(procedureNode)
        }

        override fun getDetails(connection: Connection) {}
    }

    /**
     * Represents a database object contained within a schema retrieved by the
     * getTables() method of db meta data
     */
    class TableTreeNode(row: ResultSet) : DBTreeNode() {
        init {
            allowsChildren=false
        }

        override fun addChildren(connection: Connection) {
            //NOOP
        }

        val schemaName : String = row.getString("TABLE_SCHEM")
        val name : String = row.getString("TABLE_NAME")
        val type : String = row.getString("TABLE_TYPE")
        val remarks : String? = row.getString("REMARKS")

        override fun toString() = name

        override fun getDetails(connection: Connection) {
            val columns = connection.metaData.getColumns(null, schemaName, name, null)
            while ( columns.next() ) {
                println(columns.getString("COLUMN_NAME"))
            }
            val indexes = connection.metaData.getIndexInfo(null, schemaName, name, false, false)
            while (indexes.next()) {
                println(indexes.getString("INDEX_NAME"))
            }
        }
    }

    /**
     * Represents database object retrieved by the get getProcedures, typically stored procedures
     */
    class ProcedureTreeNode(row: ResultSet) : DBTreeNode() {
        init {
            allowsChildren=false
        }

        override fun addChildren(connection: Connection) {
            //NOOP
        }

        val schemaName : String = row.getString("PROCEDURE_SCHEM")
        val name : String = row.getString("PROCEDURE_NAME")
        val type : Int = row.getInt("PROCEDURE_TYPE")
        val remarks : String? = row.getString("REMARKS")
        val specificName : String = row.getString("SPECIFIC_NAME")

        override fun toString() = name

        override fun getDetails(connection: Connection) {
            val columns = connection.metaData.getProcedureColumns(null, schemaName, name, null)
            while ( columns.next() ) {
                println(columns.getString("COLUMN_NAME"))
            }
        }
    }

}
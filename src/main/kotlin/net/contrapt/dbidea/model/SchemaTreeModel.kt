package net.contrapt.dbidea.model

import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.application.ApplicationManager
import com.intellij.ui.treeStructure.Tree
import net.contrapt.dbidea.DBIdea
import net.contrapt.dbidea.controller.ConnectionData
import java.sql.Connection
import java.sql.ResultSet
import javax.sql.DataSource
import javax.swing.event.TreeExpansionEvent
import javax.swing.event.TreeSelectionEvent
import javax.swing.event.TreeSelectionListener
import javax.swing.event.TreeWillExpandListener
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel
import javax.swing.tree.TreeNode

/**
 * Data model for displaying the schema as a tree
 */
class SchemaTreeModel(val connectionData: ConnectionData, val dataSource: DataSource, val invoker: UIInvoker) : DefaultTreeModel(ConnectionTreeNode(connectionData), true) {

    val tree = Tree(this)

    init {
        tree.addTreeSelectionListener(DetailsSelectionListener())
        tree.addTreeWillExpandListener(NodeExpansionListener())
    }

    fun querySchema() {
        val myRoot = root as ConnectionTreeNode
        val connection = dataSource.connection
        val catalogRows = connection.metaData.catalogs
        while ( catalogRows.next() ) {
            myRoot.add(CatalogTreeNode(catalogRows.getString(1)))
        }
        val schemaRows = connection.metaData.schemas
        while ( schemaRows.next() ) {
            myRoot.add(SchemaTreeNode(schemaRows.getString(1)))
        }
        connection.close()
        nodesWereInserted(myRoot)
    }

    fun nodesWereInserted(node : TreeNode) {
        invoker.invokeLater {
            nodesWereInserted(node, (0..node.childCount-1).toList().toIntArray())
        }
    }

    inner class DetailsSelectionListener : TreeSelectionListener {
        override fun valueChanged(e: TreeSelectionEvent?) {
            val node = e?.newLeadSelectionPath?.lastPathComponent
            when (node) {
                null -> null
                is ConnectionTreeNode -> println("${node.data}")
                is CatalogTreeNode -> println("${node.catalogName}")
                is SchemaTreeNode -> println("${node.schemaName}")
                is TableTreeNode -> println("${node.type} ${node.remarks}")
            }
        }
    }

    inner class NodeExpansionListener : TreeWillExpandListener {
        override fun treeWillExpand(event: TreeExpansionEvent?) {
            val node = event?.path?.lastPathComponent
            when ( node ) {
                null -> return
                is DBTreeNode -> addChildren(node)
                else -> return
            }
        }

        override fun treeWillCollapse(event: TreeExpansionEvent?) {
        }

        fun addChildren(node : DBTreeNode) {
            if ( node.childCount > 0 ) return
            ApplicationManager.getApplication().executeOnPooledThread({
                val connection = dataSource.connection
                try {
                    node.addChildren(connection)
                }
                catch(e:Exception) {
                    Notifications.Bus.notify(Notification(DBIdea.APP_ID, e.message ?: "Unknown Exception", "Error querying schema", NotificationType.WARNING))
                }
                finally {
                    connection.close()
                }
                nodesWereInserted(node)
            })
        }
    }

    abstract class DBTreeNode : DefaultMutableTreeNode(null) {
        abstract fun addChildren(connection: Connection)
    }

    class ConnectionTreeNode(val data : ConnectionData) : DBTreeNode() {

        override fun addChildren(connection: Connection) {
        }

        override fun toString() = data.name
    }

    class SchemaTreeNode(val schemaName: String) : DBTreeNode() {
        override fun toString() = "Schema: $schemaName"

        override fun addChildren(connection: Connection) {
            val tables = connection.metaData.getTables(null, schemaName, null, null)
            while (tables.next()) {
                add(TableTreeNode(tables))
            }
        }
    }

    class CatalogTreeNode(val catalogName: String) : DBTreeNode() {
        override fun toString() = "Catalog: $catalogName"

        override fun addChildren(connection: Connection) {
        }
    }

    class TableTreeNode(row : ResultSet) : DBTreeNode() {

        override fun addChildren(connection: Connection) {
        }

        val name = row.getString("TABLE_NAME")
        val type = row.getString("TABLE_TYPE")
        val remarks = row.getString("REMARKS")

        override fun toString() = "$name ($type)"
    }
}
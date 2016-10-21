package net.contrapt.dbidea.model

import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.application.ApplicationManager
import com.intellij.ui.treeStructure.Tree
import net.contrapt.dbidea.DBIdea
import net.contrapt.dbidea.controller.ConnectionData
import javax.sql.DataSource
import javax.swing.event.TreeExpansionEvent
import javax.swing.event.TreeSelectionEvent
import javax.swing.event.TreeSelectionListener
import javax.swing.event.TreeWillExpandListener
import javax.swing.tree.DefaultTreeModel
import javax.swing.tree.TreeNode

/**
 * Data model for displaying the schema as a tree
 *
 */
class SchemaTreeModel(val connectionData: ConnectionData, val dataSource: DataSource, val invoker: UIInvoker) : DefaultTreeModel(DBTreeNode.ConnectionTreeNode(connectionData), true) {

    val tree = Tree(this)

    init {
        tree.addTreeSelectionListener(DetailsSelectionListener())
        tree.addTreeWillExpandListener(NodeExpansionListener())
    }

    fun initTree() {
        val myRoot = root as DBTreeNode.ConnectionTreeNode
        addChildren(myRoot)
    }

    fun addChildren(node : DBTreeNode) {
        if ( node.childCount > 0 ) return
        ApplicationManager.getApplication().executeOnPooledThread({
            val nodeClass = node.javaClass.simpleName
            val connection = dataSource.connection
            try {
                node.addChildren(connection)
            }
            catch(e:Exception) {
                Notifications.Bus.notify(Notification(DBIdea.APP_ID, e.message ?: "Unknown Exception", "Error adding $nodeClass children", NotificationType.WARNING))
                return@executeOnPooledThread
            }
            finally {
                connection.commit()
                connection.close()
            }
            nodesWereInserted(node)
        })
    }

    fun getDetails(node : DBTreeNode) {
        ApplicationManager.getApplication().executeOnPooledThread({
            val nodeClass = node.javaClass.simpleName
            val connection = dataSource.connection
            try {
                node.getDetails(connection)
            }
            catch(e:Exception) {
                Notifications.Bus.notify(Notification(DBIdea.APP_ID, e.message ?: "Unknown Exception", "Error getting $nodeClass details", NotificationType.WARNING))
                return@executeOnPooledThread
            }
            finally {
                connection.commit()
                connection.close()
            }
        })
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
                null -> return
                is DBTreeNode -> getDetails(node)
                else -> return
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
    }

}
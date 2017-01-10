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
class SchemaTreeModel(val connectionData: ConnectionData, val dataSource: DataSource, val invoker: UIInvoker, tableModel: SchemaTableModel) : DefaultTreeModel(DBTreeNode.ConnectionTreeNode(connectionData, tableModel), true) {

    val tree = Tree(this)

    init {
        tree.addTreeSelectionListener(DetailsSelectionListener())
        tree.addTreeWillExpandListener(NodeExpansionListener())
    }

    fun initTree() {
        val myRoot = root as DBTreeNode.ConnectionTreeNode
        if ( myRoot.childCount > 0 ) return
        addChildren(myRoot)
    }

    fun refreshTree() {
        val myRoot = root as DBTreeNode.ConnectionTreeNode
        removeChildren(myRoot)
        addChildren(myRoot)
    }

    fun removeChildren(node: DBTreeNode) {
        if ( node.childCount == 0 ) return
        ApplicationManager.getApplication().executeOnPooledThread({
            val connection = dataSource.connection
            val children = node.children().toList().toTypedArray()
            val indices = (0..node.childCount-1).toList().toIntArray()
            node.removeAllChildren()
            nodesWereRemoved(node, indices, children)
        })
    }

    fun addChildren(node : DBTreeNode) {
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
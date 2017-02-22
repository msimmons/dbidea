package net.contrapt.dbidea.ui

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonShortcuts
import com.intellij.openapi.actionSystem.CustomShortcutSet
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.ui.MasterDetailsComponent
import com.intellij.util.IconUtil
import com.intellij.util.PlatformIcons
import net.contrapt.dbidea.controller.ApplicationController
import net.contrapt.dbidea.controller.ConnectionData
import net.contrapt.dbidea.model.ConnectionConfigurable
import java.awt.event.InputEvent
import java.awt.event.KeyEvent
import java.util.*
import javax.swing.KeyStroke
import javax.swing.SwingUtilities
import javax.swing.tree.TreePath

/**
 * Component showing connection list and editing panel
 */
class ConnectionComponent : MasterDetailsComponent() {

    val logger = Logger.getInstance(javaClass)

    var myInitialized = false
    //val myUpdate : Runnable = {} as Runnable
    val applicationController : ApplicationController

    val removedItems : MutableList<ConnectionConfigurable> = mutableListOf()

    init {
        applicationController = ApplicationManager.getApplication().getComponent(ApplicationController::class.java)
        initTree()
    }

    override fun getComponentStateKey() : String {
        return "Connections.UI"
    }

    override fun wasObjectStored(p0: Any?): Boolean {
        logger.warn("wasObjectStored with $p0")
        return true
    }

    override fun processRemovedItems() {
        removedItems.forEach {
            applicationController.removeConnection(it.connection)
        }
        removedItems.clear()
    }

    override fun getDisplayName(): String? {
        return "Connections"
    }

    override fun getHelpTopic() : String {
        return ""
    }

    override fun apply() {
        logger.warn("apply")
        super.apply()
    }

    override fun disposeUIResources() {

    }

    override fun createActions(fromPopup: Boolean): ArrayList<AnAction> {
        return ArrayList(listOf(createAddAction(), createDeleteAction(), createCopyAction()))
    }

    override fun removePaths(vararg paths : TreePath) {
        logger.warn("removePaths with $paths")
        super.removePaths(*paths)
        paths.forEach {
            val node = it.lastPathComponent as MyNode
            removedItems.add(node.configurable as ConnectionConfigurable)
        }
    }

    override fun reset() {
        logger.warn("reset")
        reloadTree()
        super.reset()
    }

    override fun getEmptySelectionString() : String {
        return "Select a connection to view or edit its details here"
    }

    public fun getAllConnections() : Map<String, ConnectionData> {
        logger.warn("getAllConnections $myInitialized")
        val connections = mutableMapOf<String, ConnectionData>()
        if (!myInitialized) {
            applicationController.applicationData.connections.forEach {
                connections.put(it.name, it)
            }
        }
        else {
            myRoot.children().iterator().forEach {
                val node = it as MyNode
                val connection = node.configurable.editableObject as ConnectionData
                connections.put(connection.name, connection)
            }
        }
        return connections
    }

    fun addItemsChangeListener(runnable : Runnable) {
        addItemsChangeListener(object : ItemsChangeListener {

            override fun itemChanged(deletedItem : Any?) {
                SwingUtilities.invokeLater(runnable)
            }

            override fun itemsExternallyChanged() {
                SwingUtilities.invokeLater(runnable)
            }
        })
    }

    private fun reloadTree() {
        myRoot.removeAllChildren()
        logger.warn("Reloading tree with ${applicationController.applicationData.connections}")
        applicationController.applicationData.connections.sortedBy { it.name }.forEach {
            val copy = it.deepCopy()
            addNode(MyNode(ConnectionConfigurable(copy, TREE_UPDATER)), myRoot)
        }
        myInitialized = true
    }

    private fun addConnectionNode(connection : ConnectionData) {
        val configurable = ConnectionConfigurable(connection, TREE_UPDATER)
        configurable.isNew = true
        val node = MyNode(configurable)
        addNode(node, myRoot)
        selectNodeInTree(node)
    }

    fun createAddAction() : DumbAwareAction {
        return object : DumbAwareAction("Add", "Add", IconUtil.getAddIcon()) {
            var nameCounter = 0
            init {
                registerCustomShortcutSet(CommonShortcuts.INSERT, this@ConnectionComponent.myTree)
            }
            override fun actionPerformed(p0: AnActionEvent?) {
                nameCounter++
                val connection = ConnectionData("Connection-$nameCounter")
                this@ConnectionComponent.addConnectionNode(connection)
            }

        }
    }

    fun createCopyAction() : DumbAwareAction {
        return object : DumbAwareAction("Copy", "Copy", PlatformIcons.COPY_ICON) {
            init {
                registerCustomShortcutSet(CustomShortcutSet(KeyStroke.getKeyStroke(KeyEvent.VK_D, InputEvent.CTRL_MASK)), this@ConnectionComponent.myTree)
            }

            override fun actionPerformed(event: AnActionEvent) {
                val selected = this@ConnectionComponent.selectedConfigurable?.editableObject as ConnectionData
                val connection = selected.copy("Copy of ${selected.name}")
                this@ConnectionComponent.addConnectionNode(connection)
            }

            override fun update(event: AnActionEvent) {
                super.update(event)
                event.presentation.isEnabled = (this@ConnectionComponent.selectedObject != null)
            }
        }
    }

    fun createDeleteAction() : DumbAwareAction {
        return object : DumbAwareAction("Delete", "Delete", PlatformIcons.DELETE_ICON) {
            init {
                registerCustomShortcutSet(CommonShortcuts.getDelete(), this@ConnectionComponent.myTree)
            }

            override fun actionPerformed(p0: AnActionEvent?) {
                this@ConnectionComponent.removePaths(*this@ConnectionComponent.myTree.selectionPaths)
            }

            override fun update(e: AnActionEvent) {
                val presentation = e.presentation
                presentation.isEnabled = false
                val selectionPaths = this@ConnectionComponent.myTree.selectionPaths
                if (selectionPaths != null) {
                    //Object[] nodes = ContainerUtil.map2Array(selectionPath, new Function<TreePath, Object>() {
                    //                    @Override
                    //                  public Object fun(TreePath treePath) {
                    //                    return treePath.getLastPathComponent();
                    //              }
                    //        });
                    //      if (!myCondition.value(nodes)) return;
                    presentation.isEnabled = true
                }
            }
        }
    }

}

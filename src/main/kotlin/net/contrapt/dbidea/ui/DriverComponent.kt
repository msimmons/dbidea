package net.contrapt.dbidea.ui

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonShortcuts
import com.intellij.openapi.actionSystem.CustomShortcutSet
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.ui.MasterDetailsComponent
import com.intellij.util.IconUtil
import com.intellij.util.PlatformIcons
import net.contrapt.dbidea.controller.ApplicationController
import net.contrapt.dbidea.controller.DriverData
import net.contrapt.dbidea.model.DriverConfigurable
import org.apache.commons.logging.LogFactory
import java.awt.event.InputEvent
import java.awt.event.KeyEvent
import java.util.*
import javax.swing.KeyStroke
import javax.swing.SwingUtilities
import javax.swing.tree.TreePath

/**
 * Component showing driver list and driver editing panel
 */
class DriverComponent : MasterDetailsComponent() {

    val logger = LogFactory.getLog(javaClass)

    var myInitialized = false
    //val myUpdate : Runnable = {} as Runnable
    val applicationController : ApplicationController

    val removedItems : MutableList<DriverConfigurable> = mutableListOf()

    init {
        applicationController = ApplicationManager.getApplication().getComponent(ApplicationController::class.java)
        initTree()
    }

    override fun getComponentStateKey() : String {
        return "Drivers.UI"
    }

    override fun wasObjectStored(p0: Any?): Boolean {
        val driver = p0 as DriverData
        logger.warn("Was object $p0 stored?")
        return applicationController.applicationData.driverExists(driver)
    }

    override fun processRemovedItems() {
        removedItems.forEach {
            applicationController.applicationData.removeDriver(it.driver)
        }
        removedItems.clear()
    }

    override fun getDisplayName(): String? {
        return "Drivers"
    }

    override fun getHelpTopic() : String {
        return ""
    }

    override fun apply() {
        super.apply()
    }

    override fun disposeUIResources() {

    }

    override fun createActions(fromPopup: Boolean): ArrayList<AnAction> {
        return ArrayList<AnAction>(listOf(createAddAction(), createDeleteAction(), createCopyAction()))
    }

    override fun removePaths(vararg paths : TreePath) {
        logger.warn("Removing paths with $paths")
        super.removePaths(*paths);
        paths.forEach {
            val node = it.lastPathComponent as MyNode
            removedItems.add(node.configurable as DriverConfigurable)
        }
    }

    override fun reset() {
        reloadTree();
        super.reset();
    }

    override fun getEmptySelectionString() : String {
        return "Select a driver to view or edit its details here"
    }

    public fun getAllDrivers() : Map<String, Any> {
        val drivers = mutableMapOf<String, DriverData>()
        if (!myInitialized) {
            applicationController.applicationData.drivers.forEach {
                drivers.put(it.name, it)
            }
        }
        else {
            myRoot.children().iterator().forEach {
                val node = it as MyNode
                val driver = node.configurable.editableObject as DriverData
                drivers.put(driver.name, driver)
            }
        }
        return drivers;
    }

    fun addItemsChangeListener(runnable : Runnable) {
        addItemsChangeListener(object : ItemsChangeListener {

            override fun itemChanged(deletedItem : Any?) {
                SwingUtilities.invokeLater(runnable);
            }

            override fun itemsExternallyChanged() {
                SwingUtilities.invokeLater(runnable);
            }
        });
    }

    private fun reloadTree() {
        myRoot.removeAllChildren();
        applicationController.applicationData.drivers.forEach {
            val copy = it.copy(it.name)
            addNode(MyNode(DriverConfigurable(copy, TREE_UPDATER)), myRoot)
        }
        myInitialized = true
    }

    private fun addDriverNode(driver: DriverData) {
        val  configurable = DriverConfigurable(driver, TREE_UPDATER)
        configurable.isNew = true
        val node = MyNode(configurable)
        addNode(node, myRoot)
        selectNodeInTree(node)
    }

    fun createAddAction() : DumbAwareAction {
        return object : DumbAwareAction("Add", "Add", IconUtil.getAddIcon()) {
            var nameCounter = 0
            init {
                registerCustomShortcutSet(CommonShortcuts.INSERT, this@DriverComponent.myTree);
            }
            override fun actionPerformed(p0: AnActionEvent?) {
                nameCounter++
                val driver = DriverData("Driver-$nameCounter")
                this@DriverComponent.addDriverNode(driver);
            }

        }
    }

    fun createCopyAction() : DumbAwareAction {
        return object : DumbAwareAction("Copy", "Copy", PlatformIcons.COPY_ICON) {
            init {
                registerCustomShortcutSet(CustomShortcutSet(KeyStroke.getKeyStroke(KeyEvent.VK_D, InputEvent.CTRL_MASK)), this@DriverComponent.myTree)
            }

            override fun actionPerformed(event: AnActionEvent) {
                val selected = this@DriverComponent.selectedConfigurable?.editableObject as DriverData
                val driver = selected.copy("Copy of ${selected.name}")
                this@DriverComponent.addDriverNode(driver);
            }

            override fun update(event: AnActionEvent) {
                super.update(event);
                event.getPresentation().setEnabled(this@DriverComponent.getSelectedObject() != null);
            }
        }
    }

    fun createDeleteAction() : DumbAwareAction {
        return object : DumbAwareAction("Delete", "Delete", PlatformIcons.DELETE_ICON) {
            init {
                registerCustomShortcutSet(CommonShortcuts.getDelete(), this@DriverComponent.myTree)
            }

            override fun actionPerformed(p0: AnActionEvent?) {
                this@DriverComponent.removePaths(*this@DriverComponent.myTree.selectionPaths)
            }

            override fun update(e: AnActionEvent) {
                val presentation = e.getPresentation();
                presentation.setEnabled(false);
                val selectionPaths = this@DriverComponent.myTree.selectionPaths
                if (selectionPaths != null) {
                    //Object[] nodes = ContainerUtil.map2Array(selectionPath, new Function<TreePath, Object>() {
                    //                    @Override
                    //                  public Object fun(TreePath treePath) {
                    //                    return treePath.getLastPathComponent();
                    //              }
                    //        });
                    //      if (!myCondition.value(nodes)) return;
                    presentation.setEnabled(true);
                }
            }
        }
    }
}

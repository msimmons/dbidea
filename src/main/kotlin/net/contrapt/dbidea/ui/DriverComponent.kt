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

    var myInitialized = false
    //val myUpdate : Runnable = {} as Runnable
    val applicationController : ApplicationController

    init {
        applicationController = ApplicationManager.getApplication().getComponent(ApplicationController::class.java)
        initTree()
    }

    override fun getComponentStateKey() : String {
        return "Drivers.UI"
    }

    override fun wasObjectStored(p0: Any?): Boolean {
        throw UnsupportedOperationException()
    }

    override fun processRemovedItems() {
        throw UnsupportedOperationException()
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
        return ArrayList<AnAction>(listOf(AddAction(this), DeleteAction(this), CopyAction(this)))
    }

    override fun removePaths(vararg paths : TreePath) {
        super.removePaths(*paths);
        reloadAvailableDrivers();
    }

    override fun reset() {
        reloadTree();
        super.reset();
    }

    override fun getEmptySelectionString() : String {
        return "Select a driver to view or edit its details here"
    }

    protected fun reloadAvailableDrivers() {
        //myUpdate.run()
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
        val configurable = DriverConfigurable(driver, TREE_UPDATER)
        configurable.isModified = true
        val node = MyNode(configurable)
        addNode(node, myRoot)
        selectNodeInTree(node)
        reloadAvailableDrivers()
    }

    class AddAction(val panel: DriverComponent) : DumbAwareAction("Add", "Add", IconUtil.getAddIcon()) {
        init {
            registerCustomShortcutSet(CommonShortcuts.INSERT, panel.myTree);
        }
        override fun actionPerformed(event: AnActionEvent) {
            val name = "" //askForProfileName("Create Copyright Profile", "")
            //final CopyrightProfile copyrightProfile = new CopyrightProfile(name);
            //panel.addProfileNode(copyrightProfile);
        }
    }

    class CopyAction(val panel: DriverComponent) : DumbAwareAction("Copy", "Copy", PlatformIcons.COPY_ICON) {
        init {
            registerCustomShortcutSet(CustomShortcutSet(KeyStroke.getKeyStroke(KeyEvent.VK_D, InputEvent.CTRL_MASK)), panel.myTree)
        }

        override fun actionPerformed(event: AnActionEvent) {
            //final String profileName = askForProfileName("Copy Copyright Profile", "");
            //if (profileName == null) return;
            //final CopyrightProfile clone = new CopyrightProfile();
            //clone.copyFrom((CopyrightProfile)getSelectedObject());
            //clone.setName(profileName);
            //addProfileNode(clone);
        }

        override fun update(event: AnActionEvent) {
            super.update(event);
            event.getPresentation().setEnabled(panel.getSelectedObject() != null);
        }
    }

    protected class DeleteAction(val panel: DriverComponent) : DumbAwareAction("Delete", "Delete", PlatformIcons.DELETE_ICON) {
        init {
            registerCustomShortcutSet(CommonShortcuts.getDelete(), panel.myTree)
        }

        override fun actionPerformed(p0: AnActionEvent?) {
            panel.removePaths(*panel.myTree.selectionPaths)
        }

        override fun update(e : AnActionEvent) {
            val presentation = e.getPresentation();
            presentation.setEnabled(false);
            val selectionPaths = panel.myTree.selectionPaths
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

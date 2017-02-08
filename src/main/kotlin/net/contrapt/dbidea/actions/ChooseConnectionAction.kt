package net.contrapt.dbidea.actions

import com.intellij.ide.DataManager
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DataKeys
import com.intellij.openapi.actionSystem.Presentation
import com.intellij.openapi.actionSystem.ex.CustomComponentAction
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import net.contrapt.dbidea.controller.ApplicationController
import net.contrapt.dbidea.controller.ToolWindowController
import javax.swing.DefaultComboBoxModel
import javax.swing.JComponent

/**
 * Choose the database connection to use and set it on the [ToolWindowController]
 */
class ChooseConnectionAction : AnAction(), CustomComponentAction {

    val applicationComponent = ApplicationManager.getApplication().getComponent(ApplicationController::class.java)

    override fun createCustomComponent(p0: Presentation?): JComponent {
        val comboBox = ComboBox<String>()
        comboBox.toolTipText = "Choose DB Connection"
        val connectionNames = arrayOf("") + applicationComponent.applicationData.connections.map { it.name }.toTypedArray()
        comboBox.model = ChooseConnectionModel(comboBox, connectionNames)
        comboBox.setMinLength(12)
        return comboBox
    }

    /**
     * When action is performed, popup connection chooser -- i don't think this
     * ever happens
     */
    override fun actionPerformed(e: AnActionEvent) {
        println("Action Performed: $e")
        val project = e.project
        when (project) {
            null -> return
            else -> return
        }
    }

    class ChooseConnectionModel(val comboBox: ComboBox<String>, connectionNames : Array<String>) : DefaultComboBoxModel<String>(connectionNames) {
        val applicationComponent = ApplicationManager.getApplication().getComponent(ApplicationController::class.java)

        init {
            selectedItem = ""
        }

        override fun setSelectedItem(anItem: Any?) {
            super.setSelectedItem(anItem)
            val project : Project? = when (DataManager.getInstance().getDataContext(comboBox).getData(DataKeys.PROJECT.name)) {
                null -> null
                else -> DataManager.getInstance().getDataContext(comboBox).getData(DataKeys.PROJECT.name) as Project
            }
            if ( project == null ) {
                return
            }
            project.getComponent(ToolWindowController::class.java).connectionListModel = this
            project.getComponent(ToolWindowController::class.java).connectionName = selectedItem as String
        }
    }

}
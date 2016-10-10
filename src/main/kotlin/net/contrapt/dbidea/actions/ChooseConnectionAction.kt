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
import net.contrapt.dbidea.controller.ConnectionData
import net.contrapt.dbidea.controller.StatementController
import javax.swing.ComboBoxModel
import javax.swing.JComponent
import javax.swing.event.ListDataListener

/**
 *
 * Choose the database connection to use and associate it with a File/Buffer ?
 * Custom component allows placing a choice combobox on a toolbar
 */
class ChooseConnectionAction : AnAction(), CustomComponentAction {

    override fun createCustomComponent(p0: Presentation?): JComponent {
        val comboBox = ComboBox()
        comboBox.toolTipText = "Choose DbIdea Connection"
        comboBox.model = ChooseConnectionModel(comboBox)
        return comboBox
    }

    /**
     * When action is performed, popup connection chooser
     */
    override fun actionPerformed(e: AnActionEvent) {
        println("Action Performed: $e")
        val project = e.project
        when (project) {
            null -> return
            else -> return
        }
    }

    fun setChosenConnection(connectionData: ConnectionData?) {

    }

    class ChooseConnectionModel(val comboBox : ComboBox) : ComboBoxModel<String> {
        val applicationComponent = ApplicationManager.getApplication().getComponent(ApplicationController::class.java)
        var selectedItem: String? = null

        override fun setSelectedItem(anItem: Any?) {
            if ( anItem == null ) {
                println("No item selected")
                return
            }
            selectedItem = anItem as String
            val project : Project? = when (DataManager.getInstance().getDataContext(comboBox).getData(DataKeys.PROJECT.name)) {
                null -> null
                else -> DataManager.getInstance().getDataContext(comboBox).getData(DataKeys.PROJECT.name) as Project
            }
            if ( project == null ) {
                println("Didn't find the project")
                return
            }
            project.getComponent(StatementController::class.java).connectionName = selectedItem as String
        }

        override fun getSelectedItem(): Any? {
            return selectedItem
        }

        override fun getSize(): Int {
            return applicationComponent.applicationData.connections.size
        }

        override fun addListDataListener(l: ListDataListener?) {
        }

        override fun getElementAt(index: Int): String {
            if (index >= size) return ""
            else return applicationComponent.applicationData.connections[index].name
        }

        override fun removeListDataListener(l: ListDataListener?) {

        }
    }
}
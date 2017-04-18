package net.contrapt.dbidea.model

import com.intellij.ide.DataManager
import com.intellij.openapi.actionSystem.DataKeys
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import net.contrapt.dbidea.controller.ApplicationController
import net.contrapt.dbidea.controller.ToolWindowController
import javax.swing.DefaultComboBoxModel

/**
 * Created by mark on 2/22/17.
 */
class ChooseConnectionListModel(val comboBox: ComboBox<String>, connectionNames : Array<String>) : DefaultComboBoxModel<String>(connectionNames){

    val applicationComponent = ApplicationManager.getApplication().getComponent(ApplicationController::class.java)
    val project = DataManager.getInstance().getDataContext(comboBox).getData(DataKeys.PROJECT.name)
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
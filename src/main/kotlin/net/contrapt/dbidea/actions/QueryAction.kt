package net.contrapt.dbidea.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DataKeys
import com.intellij.openapi.util.Key
import net.contrapt.dbidea.controller.StatementController

/**
 * Created by mark on 2/8/16.
 *
 * Execute selected text as sql
 */
class QueryAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project
        //DataKeys.VIRTUAL_FILE.getData(e.dataContext)?.getUserData(Key.create<String>("H"))
        val sql = DataKeys.EDITOR.getData(e.dataContext)?.selectionModel?.selectedText
        if ( project != null && sql != null ) {
            val component = project.getComponent(StatementController::class.java)
            component.executeSql(sql)
        }
    }
}
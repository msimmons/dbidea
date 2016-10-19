package net.contrapt.dbidea.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.IdeActions
import com.intellij.openapi.editor.actionSystem.EditorActionManager
import net.contrapt.dbidea.controller.ToolWindowController

/**
 * Created by mark on 2/8/16.
 *
 * Execute selected text as sql
 */
class QueryAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project
        val editor = e.getData(CommonDataKeys.EDITOR)
        val sql = editor?.selectionModel?.selectedText
        if ( project != null && sql != null ) {
            val component = project.getComponent(ToolWindowController::class.java)
            component.executeSql(sql)
        }
    }
}
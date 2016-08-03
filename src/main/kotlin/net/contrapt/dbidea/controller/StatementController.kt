package net.contrapt.dbidea.controller

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.AbstractProjectComponent
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.SimpleToolWindowPanel
import com.intellij.openapi.util.Key
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowAnchor
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.ui.content.Content
import com.intellij.ui.content.ContentManagerEvent
import com.intellij.ui.content.ContentManagerListener
import com.mysql.jdbc.Driver
import net.contrapt.dbidea.model.ResultSetTableModel
import net.contrapt.dbidea.model.UIInvoker
import net.contrapt.dbidea.ui.ResultSetPanel
import org.apache.tomcat.jdbc.pool.DataSource
import org.springframework.jdbc.datasource.SimpleDriverDataSource
import java.io.StringWriter

/**
 * Created by mark on 4/4/16.
 * Orchestrate table model and view
 */
class StatementController(project: Project) : AbstractProjectComponent(project) {

    val logger : Logger = Logger.getInstance(javaClass)

    lateinit var toolWindow: ToolWindow
    lateinit var applicationController : ApplicationController
    var panelCount = 0
    var pool: DataSource
    val modelMap : MutableMap<Content, ResultSetTableModel> = mutableMapOf()

    init {
        val mysql = Driver()
        val url = "jdbc:mysql://localhost/etl_dev?relaxAutoCommit=true"
        val username = "app"
        val password = "qwerty"
        val dataSource = SimpleDriverDataSource(mysql, url, username, password)
        pool = DataSource()
        pool.dataSource = dataSource
        pool.initialSize = 2
        pool.isTestOnBorrow = true
        pool.validationQuery = "select user() from dual"
        pool.defaultAutoCommit = false
    }

    override fun getComponentName(): String = "StatementController"

    override fun disposeComponent() {
        logger.debug("disposeComponent")
    }

    override fun initComponent() {
        logger.debug("initComponent")
    }

    override fun projectClosed() {
        toolWindow.contentManager.canCloseAllContents()
    }

    override fun projectOpened() {
        toolWindow = ToolWindowManager.getInstance(myProject).registerToolWindow("DbIdea", true, ToolWindowAnchor.BOTTOM)
        toolWindow.contentManager.addContentManagerListener(cml)
        applicationController = ApplicationManager.getApplication().getComponent(ApplicationController::class.java)
    }

    fun executeSql(sql: String) {
        var model = ResultSetTableModel(pool.connection, sql, UIInvoker(ApplicationManager.getApplication()))
        addOrReplaceContent(model)
    }

    fun chooseConnection(dataContext: DataContext) {
        val connectionName = DataKeys.VIRTUAL_FILE.getData(dataContext)?.getUserData(Key.create<String>("dbidea.connection"))
    }

    private fun addOrReplaceContent(model: ResultSetTableModel) {
        val selectedContent = toolWindow.contentManager.selectedContent
        when (selectedContent) {
            null -> addContent(model)
            else -> {
                val currentModel = modelMap[selectedContent] ?: throw IllegalStateException("Expected to find model")
                when ( currentModel.isPinned ) {
                    true -> addContent(model)
                    else -> replaceContent(currentModel, model, selectedContent)
                }
            }
        }
    }

    private fun addContent(model: ResultSetTableModel) {
        var resultSetPanel = ResultSetPanel(model)
        panelCount++
        val  content = createResultSetContent(resultSetPanel, toolWindow)
        modelMap[content] = model
        toolWindow.contentManager.addContent(content)
        toolWindow.contentManager.setSelectedContent(content)
        toolWindow.contentManager.requestFocus(content, true)
        toolWindow.show { doExecute(model) }
    }

    private fun replaceContent(currentModel: ResultSetTableModel, newModel: ResultSetTableModel, currentContent: Content) {
        currentModel.close()
        modelMap.remove(currentContent)
        toolWindow.contentManager.removeContent(currentContent, true)
        addContent(newModel)
    }

    fun createResultSetContent(resultSetPanel: ResultSetPanel, toolWindow: ToolWindow): Content {

        val panel = SimpleToolWindowPanel(false, true)
        val content = toolWindow.contentManager.factory.createContent(panel, "Statement-$panelCount", true)
        panel.setContent(resultSetPanel);
        //panel.addFocusListener(createFocusListener());

        val toolbar = createToolbar(resultSetPanel)
        //toolbar.getComponent().addFocusListener(createFocusListener());
        panel.setToolbar(toolbar.component);
        content.preferredFocusableComponent = resultSetPanel
        content.setDisposer { resultSetPanel.tableModel.close() }
        return content;
    }

    fun createToolbar(panel: ResultSetPanel): ActionToolbar {
        val group = DefaultActionGroup()
        group.add(ExecuteStatement(panel.tableModel))
        group.add(CommitStatement(panel.tableModel))
        group.add(RollbackStatement(panel.tableModel))
        group.add(CancelStatement(panel.tableModel))
        group.add(ExportResults(panel.tableModel))
        group.add(PinStatement(panel.tableModel))
        group.add(LimitStatement(panel.tableModel))
        val toolbar = ActionManager.getInstance().createActionToolbar(ActionPlaces.UNKNOWN, group, false)
        toolbar.setTargetComponent(panel)
        return toolbar
    }

    fun doExecute(model: ResultSetTableModel) {
        ApplicationManager.getApplication().executeOnPooledThread({
            model.updateStatus("Executing...")
            try {
                model.execute()
                model.fetch()
            }
            catch(e : Exception) {
                model.updateStatus("Execution Failed")
                throw(e)
            }
            model.updateStatus()
        })
    }

    class ExecuteStatement(val model: ResultSetTableModel) : DumbAwareAction("Execute", "Execute this statement again", AllIcons.General.Run) {
        override fun actionPerformed(e : AnActionEvent) {
            ApplicationManager.getApplication().executeOnPooledThread {
                model.updateStatus("Executing")
                try {
                    model.execute()
                }
                catch(e : Exception) {
                    model.updateStatus("Execution Failed")
                    throw(e)
                }
                try {
                    model.fetch()
                }
                catch(e : Exception) {
                    model.updateStatus("Fetch Failed")
                    throw(e)
                }
                model.updateStatus()
            }
        }
    }

    class CommitStatement(val model: ResultSetTableModel) : DumbAwareAction("Commit", "Commit this statement", AllIcons.Process.State.GreenOK) {
        override fun actionPerformed(e : AnActionEvent) {
            model.updateStatus("Committing")
            try {
                model.commit()
            }
            catch(e : Exception) {
                model.updateStatus("Commit failed")
                throw(e)
            }
            model.updateStatus()
        }

    }

    class RollbackStatement(val model: ResultSetTableModel) : DumbAwareAction("Rollback", "Rollback this statement", AllIcons.Actions.Rollback) {
        override fun actionPerformed(e : AnActionEvent) {
            model.updateStatus("Rolling Back")
            try {
                model.rollback()
            }
            catch(e : Exception) {
                model.updateStatus("Rollback Failed")
                throw(e)
            }
            model.updateStatus("Rolled Back")
        }
    }

    class CancelStatement(val model: ResultSetTableModel) : DumbAwareAction("Cancel", "Cancel this statement", AllIcons.Ide.Macro.Recording_stop) {
        override fun actionPerformed(e : AnActionEvent) {
            ApplicationManager.getApplication().executeOnPooledThread {
                model.updateStatus("Cancelling")
                try {
                    model.cancel()
                }
                catch(e : Exception) {
                    model.updateStatus("Cancel failed")
                    throw(e)
                }
                model.updateStatus("Cancelled")
            }
        }
    }

    class ExportResults(val model: ResultSetTableModel) : DumbAwareAction("Export", "Export results to csv", AllIcons.Actions.Export) {
        override fun actionPerformed(e : AnActionEvent) {
            model.updateStatus("Exporting")
            val writer = StringWriter()
            try {
                model.export(writer.buffered())
            }
            catch (e:Exception) {
                model.updateStatus("Export failed")
                throw(e)
            }
            finally {
                writer.close()
            }
            model.updateStatus()
            Logger.getInstance(javaClass).debug(writer.buffer.toString())
        }
    }

    class PinStatement(val model: ResultSetTableModel) : ToggleAction("Pin", "Pin this statements", AllIcons.General.Pin_tab) {
        override fun isSelected(p0: AnActionEvent?): Boolean {
            return model.isPinned
        }

        override fun setSelected(p0: AnActionEvent?, p1: Boolean) {
            model.isPinned = p1
        }
    }

    class LimitStatement(val model: ResultSetTableModel) : ToggleAction("Limit", "Toggle fetch limit", AllIcons.General.Bullet) {
        override fun isSelected(p0: AnActionEvent?): Boolean {
            return model.isLimited
        }

        override fun setSelected(p0: AnActionEvent?, p1: Boolean) {
            model.isLimited = p1
        }
    }

    object cml : ContentManagerListener {
        override fun contentAdded(p0: ContentManagerEvent?) {
            println("Add content $p0")
        }

        override fun contentRemoveQuery(p0: ContentManagerEvent?) {
            println("Remove query $p0")
        }

        override fun contentRemoved(p0: ContentManagerEvent?) {
            val component = p0?.content?.component
            when ( component ) {
                is SimpleToolWindowPanel -> println(component)
                else -> println("Unknown component ${p0?.content?.component}")
            }
        }

        override fun selectionChanged(p0: ContentManagerEvent?) {
            println("selection $p0")
        }

    }

}
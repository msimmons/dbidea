package net.contrapt.dbidea.controller

import com.intellij.icons.AllIcons
import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.AbstractProjectComponent
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.ui.SimpleToolWindowPanel
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowAnchor
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.ui.content.Content
import com.intellij.ui.content.ContentManagerEvent
import com.intellij.ui.content.ContentManagerListener
import net.contrapt.dbidea.DBIdea
import net.contrapt.dbidea.model.ResultSetTableModel
import net.contrapt.dbidea.model.SchemaTableModel
import net.contrapt.dbidea.model.SchemaTreeModel
import net.contrapt.dbidea.model.UIInvoker
import net.contrapt.dbidea.ui.ResultSetPanel
import net.contrapt.dbidea.ui.SchemaTreePanel
import java.io.File
import java.io.OutputStreamWriter
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.swing.ComboBoxModel

/**
 * Control operation of the main tool window
 */
class ToolWindowController(project: Project) : AbstractProjectComponent(project) {

    val logger : Logger = Logger.getInstance(javaClass)

    lateinit var toolWindow: ToolWindow
    lateinit var applicationController : ApplicationController
    lateinit var connectionListModel : ComboBoxModel<String>

    var connectionName : String = ""
       set(value) {
           field = value
           describeSchema()
       }
    var panelCount = 0
    val resultModelMap: MutableMap<Content, ResultSetTableModel> = mutableMapOf()
    val schemaModelMap: MutableMap<String, SchemaTreeModel> = mutableMapOf()

    override fun getComponentName(): String = javaClass.simpleName

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
        toolWindow = ToolWindowManager.getInstance(myProject).registerToolWindow(DBIdea.APP_NAME, true, ToolWindowAnchor.BOTTOM)
        toolWindow.contentManager.addContentManagerListener(contentListener)
        applicationController = ApplicationManager.getApplication().getComponent(ApplicationController::class.java)
    }

    fun executeSql(sql: String) {
        if ( connectionName.isNullOrEmpty() ) {
            Notifications.Bus.notify(Notification(DBIdea.APP_ID, "No Connection", "You have not chosen a connection", NotificationType.WARNING))
            return
        }
        val connectionData = applicationController.getConnection(connectionName)
        val pool = applicationController.getPool(connectionData)
        val model = ResultSetTableModel(connectionData, pool.connection, sql, UIInvoker(ApplicationManager.getApplication()))
        addOrReplaceContent(model)
    }

    fun describeSchema() {
        if ( connectionName.isNullOrEmpty() ) {
            return
        }
        val schemaTreeModel = schemaModelMap[connectionName]
        when(schemaTreeModel) {
            null -> initSchemaContent()
            else -> schemaTreeModel.refreshTree()
        }
    }

    fun initSchemaContent() {
        if ( connectionName.isNullOrEmpty() ) {
            Notifications.Bus.notify(Notification(DBIdea.APP_ID, "No Connection", "You have not chosen a connection", NotificationType.WARNING))
            return
        }
        val connectionData = applicationController.getConnection(connectionName)
        val pool = applicationController.getPool(connectionData)
        val tableModel = SchemaTableModel(UIInvoker(ApplicationManager.getApplication()))
        val treeModel = SchemaTreeModel(connectionData, pool, UIInvoker(ApplicationManager.getApplication()), tableModel)
        addSchemaContent(treeModel, tableModel)
        schemaModelMap[connectionName] = treeModel
        doSchema(treeModel)
    }

    private fun addSchemaContent(treeModel: SchemaTreeModel, tableModel: SchemaTableModel) {
        val schemaPanel = SchemaTreePanel(treeModel, tableModel)
        toolWindow.contentManager.addContent(createSchemaContent(schemaPanel))
    }

    private fun addOrReplaceContent(model: ResultSetTableModel) {
        if ( toolWindow.contentManager.contentCount == 0 ) {
            initSchemaContent()
        }
        val selectedContent = toolWindow.contentManager.selectedContent
        when (selectedContent) {
            null -> addContent(model)
            else -> {
                val currentModel = resultModelMap[selectedContent]
                when (currentModel) {
                    null -> addContent(model)
                    else -> when(currentModel.isPinned) {
                        true -> addContent(model)
                        else -> replaceContent(currentModel, model, selectedContent)
                    }
                }
            }
        }
    }

    private fun addContent(model: ResultSetTableModel) {
        val resultSetPanel = ResultSetPanel(model)
        panelCount++
        val  content = createResultSetContent(resultSetPanel)
        resultModelMap[content] = model
        toolWindow.contentManager.addContent(content)
        toolWindow.contentManager.setSelectedContent(content)
        //toolWindow.contentManager.requestFocus(content, true)
        toolWindow.show { doExecute(model) }
    }

    private fun replaceContent(currentModel: ResultSetTableModel, newModel: ResultSetTableModel, currentContent: Content) {
        currentModel.close()
        resultModelMap.remove(currentContent)
        toolWindow.contentManager.removeContent(currentContent, true)
        addContent(newModel)
    }

    fun createSchemaContent(schemaPanel : SchemaTreePanel) : Content {
        val panel = SimpleToolWindowPanel(false, true)
        panel.setContent(schemaPanel)
        //panel.addFocusListener(createFocusListener());
        val toolbar = createSchemaToolbar(schemaPanel)
        //toolbar.getComponent().addFocusListener(createFocusListener());
        panel.setToolbar(toolbar.component)
        val content = toolWindow.contentManager.factory.createContent(panel, connectionName, false)
        content.preferredFocusableComponent = schemaPanel
        content.isCloseable = true
        content.setDisposer { disposeSchemaContent(schemaPanel.treeModel.connectionData) }
        return content
    }

    fun disposeSchemaContent(connectionData : ConnectionData) {
        schemaModelMap.remove(connectionData.name)
        applicationController.removePool(connectionData.name)
        if ( connectionListModel.selectedItem == connectionData.name ) connectionListModel.selectedItem = ""
    }

    fun createResultSetContent(resultSetPanel: ResultSetPanel): Content {

        val panel = SimpleToolWindowPanel(false, true)
        panel.setContent(resultSetPanel)
        //panel.addFocusListener(createFocusListener());
        val toolbar = createStatementToolbar(resultSetPanel)
        //toolbar.getComponent().addFocusListener(createFocusListener());
        panel.setToolbar(toolbar.component)
        val content = toolWindow.contentManager.factory.createContent(panel, "Statement-$panelCount", true)
        content.preferredFocusableComponent = resultSetPanel
        content.setDisposer { resultSetPanel.tableModel.close() }
        return content
    }

    fun createStatementToolbar(panel: ResultSetPanel): ActionToolbar {
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

    fun createSchemaToolbar(panel: SchemaTreePanel) : ActionToolbar {
        val group = DefaultActionGroup()
        group.add(RefreshSchema(panel.treeModel))
        val toolbar = ActionManager.getInstance().createActionToolbar(ActionPlaces.UNKNOWN, group, false)
        toolbar.setTargetComponent(panel)
        return toolbar
    }

    fun doSchema(model: SchemaTreeModel) {
        ApplicationManager.getApplication().executeOnPooledThread({
            try {
                model.initTree()
            }
            catch(e : Exception) {
                Notifications.Bus.notify(Notification(DBIdea.APP_ID, e.message ?: "Unknown Exception", "Error querying schema", NotificationType.ERROR))
            }
        })
    }

    fun doExecute(model: ResultSetTableModel) {
        ApplicationManager.getApplication().executeOnPooledThread({
            model.updateStatus("Executing...")
            try {
                model.execute()
                model.fetch()
            }
            catch(e : Exception) {
                model.updateStatus("Execution Failed: $e")
                Notifications.Bus.notify(Notification(DBIdea.APP_ID, e.message ?: "Unkown Exception", e.cause?.message ?: "NA", NotificationType.ERROR))
            }
            model.updateStatus()
        })
    }

    class RefreshSchema(val model: SchemaTreeModel) : DumbAwareAction("Refresh", "Refresh Connection Tree", AllIcons.Actions.Refresh) {
        override fun actionPerformed(e: AnActionEvent) {
            ApplicationManager.getApplication().executeOnPooledThread {
                model.refreshTree()
            }
        }
    }

    class ExecuteStatement(val model: ResultSetTableModel) : DumbAwareAction("Execute", "Execute this statement again", AllIcons.General.Run) {
        override fun actionPerformed(e : AnActionEvent) {
            ApplicationManager.getApplication().executeOnPooledThread {
                model.updateStatus("Executing")
                try {
                    model.execute()
                }
                catch(e : Exception) {
                    model.updateStatus("Execution Failed: $e")
                    throw(e)
                }
                try {
                    model.fetch()
                }
                catch(e : Exception) {
                    model.updateStatus("Fetch Failed: $e")
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
                model.updateStatus("Commit failed: $e")
                throw(e)
            }
            model.updateStatus("Committed")
        }

        override fun update(e: AnActionEvent?) {
            super.update(e)
            e?.presentation?.isEnabled = model.inTransaction()
        }
    }

    class RollbackStatement(val model: ResultSetTableModel) : DumbAwareAction("Rollback", "Rollback this statement", AllIcons.Actions.Rollback) {
        override fun actionPerformed(e : AnActionEvent) {
            model.updateStatus("Rolling Back")
            try {
                model.rollback()
            }
            catch(e : Exception) {
                model.updateStatus("Rollback Failed: $e")
                throw(e)
            }
            model.updateStatus("Rolled Back")
        }

        override fun update(e: AnActionEvent?) {
            super.update(e)
            e?.presentation?.isEnabled = model.inTransaction()
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
                    model.updateStatus("Cancel failed: $e")
                    throw(e)
                }
                model.updateStatus("Cancelled")
            }
        }
    }

    class ExportResults(val model: ResultSetTableModel) : DumbAwareAction("Export", "Export results to csv", AllIcons.Actions.Export) {
        override fun actionPerformed(e : AnActionEvent) {
            val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
            val proposedFileName = "${e.project?.basePath}/$timestamp.csv"
            val fileName = Messages.showInputDialog("Choose Export Filename", "Export File", Messages.getQuestionIcon(), proposedFileName, null) ?: return
            model.updateStatus("Exporting")
            val writer = OutputStreamWriter(File(fileName).outputStream())
            try {
                model.export(writer.buffered())
            }
            catch (e:Exception) {
                model.updateStatus("Export failed: $e")
                throw(e)
            }
            finally {
                writer.flush()
                writer.close()
            }
            model.updateStatus("Exported ${model.rowCount} rows")
            //TODO open the file in a buffer?
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
            if ( !p1 ) {
                try {
                    model.fetch()
                }
                catch(e : Exception) {
                    model.updateStatus("Fetch Failed: $e")
                    throw(e)
                }
                model.updateStatus()
            }
        }

    }

    object contentListener : ContentManagerListener {

        val logger : Logger = Logger.getInstance(javaClass)

        override fun contentAdded(p0: ContentManagerEvent?) {
            logger.debug("Add content $p0")
        }

        override fun contentRemoveQuery(p0: ContentManagerEvent?) {
            logger.debug("Remove query $p0")
        }

        override fun contentRemoved(p0: ContentManagerEvent?) {
            val component = p0?.content?.component
            when ( component ) {
                is SimpleToolWindowPanel -> logger.debug("$component")
                else -> logger.debug("Unknown component ${p0?.content?.component}")
            }
        }

        override fun selectionChanged(p0: ContentManagerEvent?) {
            logger.debug("selection $p0")
        }

    }
}
package net.contrapt.dbidea.ui

import net.contrapt.dbidea.model.SchemaTableModel
import net.contrapt.dbidea.model.SchemaTreeModel
import java.awt.Color
import javax.swing.JScrollPane
import javax.swing.JSplitPane
import javax.swing.JTable

/**
 * A panel to show the description of a schema as a tree
 */
class SchemaTreePanel(val treeModel: SchemaTreeModel, val tableModel: SchemaTableModel) : JSplitPane(JSplitPane.HORIZONTAL_SPLIT) {

    private val treePanel : JScrollPane
    private val detailsPanel : JScrollPane

    init {
        // Create a scrolling panel for the schema tree
        treePanel = JScrollPane(treeModel.tree)
        treePanel.background = Color.WHITE
        treePanel.autoscrolls = true
        // Create a scrolling panel for the details
        detailsPanel = JScrollPane(JTable(tableModel))
        detailsPanel.background = Color.WHITE
        detailsPanel.autoscrolls = true
        // Put them into the split pane
        setDividerLocation(0.6)
        setLeftComponent(treePanel)
        setRightComponent(detailsPanel)
    }

}
package net.contrapt.dbidea.ui

import net.contrapt.dbidea.model.ResultSetTableModel
import java.awt.BorderLayout
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.JTextField

/**
 * Display results of sql statement according to the given table model
 */
class ResultSetPanel(val tableModel: ResultSetTableModel) : JPanel() {

    private val resultPanel: JScrollPane
    private val statusPanel: JPanel
    private val statusText: JTextField

    init {
        // Create a scrolling panel for the sql results
        resultPanel = JScrollPane(tableModel.table)
        resultPanel.autoscrolls = true
        // Create a panel to show status, sql and connection info
        val connectionText = JTextField(tableModel.getConnectionInfo(), 15)
        connectionText.isEditable = false
        val sqlText = JTextField(tableModel.sql, 90)
        sqlText.toolTipText = tableModel.sql
        sqlText.isEditable=false
        sqlText.caretPosition=0
        statusText = JTextField("", 30)
        statusText.isEditable = false
        statusText.document = tableModel.statusModel
        statusPanel = JPanel()
        statusPanel.layout = BorderLayout()
        statusPanel.add(connectionText, BorderLayout.WEST)
        statusPanel.add(sqlText, BorderLayout.CENTER)
        statusPanel.add(statusText, BorderLayout.EAST)
        // Put them all together on the content pane
        layout = BorderLayout()
        add(resultPanel, BorderLayout.CENTER)
        add(statusPanel, BorderLayout.SOUTH)
    }

}

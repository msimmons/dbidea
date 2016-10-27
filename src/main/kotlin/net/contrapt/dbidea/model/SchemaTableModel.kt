package net.contrapt.dbidea.model

import javax.swing.table.AbstractTableModel

/**
 * The schema tree nodes use this table model to populate the details about themselves
 */
class SchemaTableModel(val uiInvoker: UIInvoker) : AbstractTableModel() {

    private val columnNames = mutableListOf<String>()
    private val rows = mutableListOf<List<Any?>>()

    fun initTable(columns: List<String>) {
        columnNames.clear()
        rows.clear()
        columnNames.addAll(columns)
        uiInvoker.invokeLater {
            fireTableStructureChanged()
        }
    }

    fun addRow(row : List<Any?>) {
        rows.add(row)
    }

    fun finishTable() {
        uiInvoker.invokeLater {
            fireTableRowsInserted(0, rowCount)
        }
    }

    override fun getRowCount(): Int {
        return rows.size
    }

    override fun getColumnCount(): Int {
        return columnNames.size
    }

    override fun getValueAt(rowIndex: Int, columnIndex: Int): Any {
        return rows[rowIndex][columnIndex] ?: ""
    }

    override fun getColumnName(column: Int): String {
        return columnNames[column]
    }

}
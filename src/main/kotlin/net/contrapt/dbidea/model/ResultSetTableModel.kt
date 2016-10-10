package net.contrapt.dbidea.model

import java.io.BufferedWriter
import java.sql.*
import java.text.SimpleDateFormat
import javax.swing.JTable
import javax.swing.table.AbstractTableModel
import javax.swing.table.TableColumnModel
import javax.swing.text.PlainDocument
import kotlin.system.measureTimeMillis

/**
 * Table model for the given sql statement and connection
 */
class ResultSetTableModel(val connectionName : String, val connection: Connection, val sql: String, val invoker: UIInvoker) : AbstractTableModel() {

    private val statement: PreparedStatement = connection.prepareStatement(sql)

    private var results: ResultSet? = null
    private var rows: MutableList<MutableList<Any>> = mutableListOf()
    private var updateCount = -1

    var isPinned = false
    var isLimited = true
    var autocommit = false

    var fetchBatchSize = 500
    var fetchLimit = 500
    var fetchSleepTime = 1000

    var executionTime = 0L
        private set

    var fetchTime = 0L
        private set

    var executionCount = 0

    val table: JTable
    val columnModel: TableColumnModel
    val statusModel = PlainDocument()

    init {
        connection.autoCommit = autocommit
        table = JTable(this)
        table.autoResizeMode = JTable.AUTO_RESIZE_OFF
        columnModel = table.columnModel
        columnModel.columnSelectionAllowed = true
    }

    /**
     * Update the status text model with the given string
     */
    fun updateStatus(status: String) {
        invoker.invokeLater {
            statusModel.replace(0, statusModel.length, status, null)
        }
    }

    /**
     * Update status to reflect execution and/or fetch status
     */
    fun updateStatus() {
        updateStatus("Execution # $executionCount (${executionTime / 1000.00}s): $action")
    }

    /**
     * Return connection info as string
     */
    fun getConnectionInfo() :String {
        return connectionName
    }

    /**
     * Excecute the sql statement for this table model
     */
    fun execute() {
        rows.clear()
        updateCount = -1
        results?.close()
        invoker.invokeLater {
            fireTableDataChanged()
        }
        executionCount++
        executionTime = measureTimeMillis {
            statement.execute()
        }
        updateCount = statement.updateCount
        results = statement.resultSet
        invoker.invokeLater {
            if (table.columnModel.columnCount == 0) {
                table.autoCreateColumnsFromModel = true
                fireTableStructureChanged()
                setColumnAttributes()
            }
        }
    }

    /**
     * Fetch the rows from the result set
     */
    fun fetch() {
        if (results == null) return
        fetchTime = measureTimeMillis {
            val tempRows: MutableList<MutableList<Any>> = mutableListOf()
            var lastRow = 0
            val columnCount = results?.metaData?.columnCount ?: 0
            while ( results?.next() ?: false ) {
                val row = Array<Any>(columnCount, {})
                for (j in 0..columnCount - 1) {
                    row[j] = convertToDisplay(results, j + 1)
                }
                tempRows.add(row.toMutableList())
                if (isLimited && tempRows.size == fetchLimit) {
                    addRows(tempRows, lastRow)
                    tempRows.clear()
                    return
                } else if (tempRows.size % fetchBatchSize == 0) {
                    addRows(tempRows, lastRow)
                    tempRows.clear()
                    lastRow = rows.size
                    sleep(fetchSleepTime.toLong())
                }
            }
            if (tempRows.size > 0) addRows(tempRows, lastRow)
        }
    }

    /**
     * Sleep for given number of milliseconds unless interrupted
     */
    private fun sleep(milliseconds: Long) {
        try {
            sleep(milliseconds)
        } catch (e: InterruptedException) {
            println("${Thread.currentThread()}  interrupted: $e")
        }

    }

    /**
     * Add rows to the table model
     */
    private fun addRows(rowsToAdd: MutableList<MutableList<Any>>, start: Int) {
        rows.addAll(rowsToAdd)
        invoker.invokeLater {
            fireTableRowsInserted(start, rows.size - 1)
        }
    }

    /**
     * Cancel the current statement if possible
     */
    fun cancel() {
        statement.cancel()
        connection.rollback()
        try {
            results?.close()
        } catch (e: SQLException) {
            System.err.println("$javaClass.cancel(): $e")
        }
        results = null
    }

    /**
     * Commit the current connection
     */
    fun commit() {
        connection.commit()
    }

    /**
     * Rollback the current connection
     */
    fun rollback() {
        connection.rollback()
    }

    /**
     * Export the data as CSV
     */
    fun export(out: BufferedWriter) {
        val headerString = table.columnModel.columns.toList().joinToString(",") {
            it.headerValue.toString()
        }
        if ( headerString.length == 0 ) return
        out.write(headerString)
        out.newLine()
        0.rangeTo(table.rowCount).forEach { row ->
            var rowString = 0.rangeTo(table.columnCount).joinToString(",") { column ->
                var value = table.model.getValueAt(row, column)?.toString() ?: ""
                if ( value.contains("\"") ) value = "\"$value\""
                value
            }
            out.write(rowString)
            out.newLine()
        }
    }

    /**
     * Close resources used by this model
     */
    fun close() {
        cancel()
        try {
            statement.close()
        } catch (e: SQLException) {
            System.err.println("$javaClass.close(): $e")
        }
        try {
            connection.close()
        } catch (e: SQLException) {
            e.printStackTrace()
        }
    }

    /**
     * Tells the table view how many columns we have
     */
    override fun getColumnCount(): Int {
        return results?.metaData?.columnCount ?: 0
    }

    override fun getColumnName(column: Int): String {
        return results?.metaData?.getColumnName(column + 1) ?: "?"
    }

    override fun getColumnClass(column: Int): Class<*> {
        if ( results?.metaData?.columnCount ?: -1 < column ) return String::class.java
        when (results?.metaData?.getColumnType(column + 1)) {
            null -> return String::class.java
            Types.DATE, Types.TIMESTAMP, Types.TIME -> return String::class.java
            Types.OTHER -> return String::class.java
            else -> return Class.forName(results?.metaData?.getColumnClassName(column + 1))
        }
    }

    /**
     * Return the row count for use by the table view
     */
    override fun getRowCount(): Int {
        if (updateCount >= 0) return updateCount
        return rows.size
    }

    /**
     * Used by table view to get the objects for each table cell
     */
    override fun getValueAt(row: Int, column: Int): Any? {
        return if (row < rows.size && column < columnCount) rows[row][column] else ""
    }

    /**
     * Return a string describing whether rows were selected or affected by DML
     */
    val action: String
        get() = "$rowCount ${rowString()} ${if (updateCount >= 0) " affected" else " retrieved"} ${hasMoreText()}"

    /**
     * Return the appropriate string row or rows depending on row count
     */
    private fun rowString(): String = if ( rowCount == 1 ) "row" else "rows"

    private fun hasMoreText(): String = if (results?.isAfterLast ?: true) "" else "...more available"

    /**
     * Use result set meta data to set column attributes in the column model
     */
    private fun setColumnAttributes() {
        val meta = results?.metaData
        if ( meta != null ) {
            val charWidth = table.getFontMetrics(table.font).charWidth('A')
            1.rangeTo(meta.columnCount).forEach {
                val headerLength = meta.getColumnLabel(it).length
                val valueLength = 3 * meta.getColumnDisplaySize(it)
                val maxLength = Math.max(valueLength, headerLength)
                val column = columnModel.getColumn(it - 1)
                column.headerValue = meta.getColumnLabel(it)
                column.minWidth = 0
                column.maxWidth = maxLength * charWidth * 2
                column.preferredWidth = headerLength * charWidth
            }
            table.autoCreateColumnsFromModel = false
            fireTableStructureChanged()
        }
    }

    /**
     * Convert certain objects (esp date/timestamp) returned from sql to appropriate
     * displayable objects
     */
    private fun convertToDisplay(row: ResultSet?, column: Int): Any {
        if (row == null) return ""
        try {
            when (row.metaData.getColumnType(column)) {
                Types.DATE, Types.TIMESTAMP, Types.TIME -> {
                    val value = row.getTimestamp(column)
                    return if (value == null) "" else dateFormat.format(value)
                }
                Types.BINARY, Types.VARBINARY, Types.LONGVARBINARY, Types.BLOB, Types.CLOB, Types.OTHER -> return row.getString(column)
                else -> return row.getObject(column) ?: ""
            }
        } catch (e: Exception) {
            return e.toString()
        }

    }

    companion object {

        private val DEFAULT_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss"
        private var dateFormat = SimpleDateFormat(DEFAULT_DATE_FORMAT)

        fun setDateFormat(format: String) {
            dateFormat = SimpleDateFormat(format)
        }
    }

}
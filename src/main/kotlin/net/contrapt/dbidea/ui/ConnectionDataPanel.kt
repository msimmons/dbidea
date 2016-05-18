package net.contrapt.dbidea.ui

import net.contrapt.dbidea.controller.ConnectionData
import org.apache.batik.ext.swing.GridBagConstants
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import javax.swing.*
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener
import javax.swing.text.*

/**
 * UI for editing connection configuration
 */
class ConnectionDataPanel(val connection: ConnectionData, var isNew : Boolean) : JPanel() {

    private val nameField : JTextField
    private val driverField : JTextField
    private val urlField : JTextField
    private val userField : JTextField
    private val passwordField : JTextField

    init {

        val combo = JComboBox<String>()
        combo.addItem("driver1")
        combo.addItem("driver2")

        nameField = createTextField(connection, ConnectionData::name.getter, ConnectionData::name.setter)
        if ( isNew ) nameField.isEditable = true else nameField.isEditable = false
        driverField = createTextField(connection, ConnectionData::driver.getter, ConnectionData::driver.setter)
        urlField = createTextField(connection, ConnectionData::url.getter, ConnectionData::url.setter)
        userField = createTextField(connection, ConnectionData::user.getter, ConnectionData::user.setter)
        passwordField = createTextField(connection, ConnectionData::password.getter, ConnectionData::password.setter)

        layout = GridBagLayout()
        val gbc = GridBagConstraints()
        gbc.fill = GridBagConstants.HORIZONTAL
        gbc.ipadx = 4
        gbc.ipady = 4
        add(createLabel("Name:"), setConstraints(gbc, 0, 0, GridBagConstraints.LINE_END))
        add(nameField, setConstraints(gbc, 1, 0, GridBagConstraints.LINE_START))
        add(createLabel("Driver:"), setConstraints(gbc, 0, 1, GridBagConstraints.LINE_END))
        add(combo, setConstraints(gbc, 1, 1, GridBagConstraints.LINE_START))
        add(createLabel("URL:"), setConstraints(gbc, 0, 2, GridBagConstraints.LINE_END))
        add(urlField, setConstraints(gbc, 1, 2, GridBagConstraints.LINE_START))
        add(createLabel("User:"), setConstraints(gbc, 0, 3, GridBagConstraints.LINE_END))
        add(userField, setConstraints(gbc, 1, 3, GridBagConstraints.LINE_START))
        add(createLabel("Password:"), setConstraints(gbc, 0, 4, GridBagConstraints.LINE_END))
        add(passwordField, setConstraints(gbc, 1, 4, GridBagConstraints.LINE_START))
        JLabel().horizontalAlignment=SwingConstants.RIGHT
    }

    fun apply() {
        nameField.isEditable = false
    }

    private fun setConstraints(gbc : GridBagConstraints, x : Int, y : Int, anchor : Int) : GridBagConstraints {
        gbc.gridx = x
        gbc.gridy = y
        gbc.anchor = anchor
        return gbc
    }

    private fun createLabel(text : String) : JLabel {
        val label = JLabel(text)
        label.horizontalAlignment = SwingConstants.RIGHT
        return label
    }

    private fun createTextField(connection: ConnectionData, getter : (ConnectionData)->String, setter : (ConnectionData, String)->Unit) : JTextField {
        val field = JTextField()
        field.columns = 40
        field.document = createDocument(connection, getter, setter)
        return field
    }

    private fun createDocument(connection: ConnectionData, getter : (ConnectionData)->String, setter : (ConnectionData, String)->Unit) : Document {
        val document = PlainDocument()
        document.insertString(0, getter(connection), null)
        document.addDocumentListener(object : DocumentListener {
            override fun changedUpdate(e: DocumentEvent?) {
                if ( e != null ) setter(connection, document.getText(0, document.length))
            }

            override fun insertUpdate(e: DocumentEvent?) {
                if ( e != null ) setter(connection, document.getText(0, document.length))
            }

            override fun removeUpdate(e: DocumentEvent?) {
                if ( e != null ) setter(connection, document.getText(0, document.length))
            }
        })
        return document
    }
}
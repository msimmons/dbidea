package net.contrapt.dbidea.ui

import net.contrapt.dbidea.controller.ConnectionData
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JTextField
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener
import javax.swing.text.*

/**
 * UI for editing connection configuration
 */
class ConnectionDataPanel(val connection: ConnectionData) : JPanel() {

    val nameField : JTextField
    val driverField : JTextField
    val urlField : JTextField
    val userField : JTextField
    val passwordField : JTextField

    init {
        nameField = createTextField(connection, ConnectionData::name.getter, ConnectionData::name.setter)
        driverField = createTextField(connection, ConnectionData::driver.getter, ConnectionData::driver.setter)
        urlField = createTextField(connection, ConnectionData::url.getter, ConnectionData::url.setter)
        userField = createTextField(connection, ConnectionData::user.getter, ConnectionData::user.setter)
        passwordField = createTextField(connection, ConnectionData::password.getter, ConnectionData::password.setter)
        add(JLabel("Name"))
        add(nameField)
        add(JLabel("Driver"))
        add(driverField)
        add(JLabel("URL"))
        add(urlField)
        add(JLabel("User"))
        add(userField)
        add(JLabel("Password"))
        add(passwordField)
    }

    fun createTextField(connection: ConnectionData, getter : (ConnectionData)->String, setter : (ConnectionData, String)->Unit) : JTextField {
        val field = JTextField()
        field.columns = 40
        field.document = createDocument(connection, getter, setter)
        return field
    }

    fun createDocument(connection: ConnectionData, getter : (ConnectionData)->String, setter : (ConnectionData, String)->Unit) : Document {
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
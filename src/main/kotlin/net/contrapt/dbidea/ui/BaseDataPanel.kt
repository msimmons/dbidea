package net.contrapt.dbidea.ui

import java.awt.GridBagConstraints
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JTextField
import javax.swing.SwingConstants
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener
import javax.swing.text.Document
import javax.swing.text.PlainDocument

/**
 * UI for editing connection configuration
 */
abstract class BaseDataPanel<T>(var isNew : Boolean) : JPanel() {

    protected fun setConstraints(gbc : GridBagConstraints, x : Int, y : Int, anchor : Int) : GridBagConstraints {
        gbc.gridx = x
        gbc.gridy = y
        gbc.anchor = anchor
        return gbc
    }

    protected fun createLabel(text : String) : JLabel {
        val label = JLabel(text)
        label.horizontalAlignment = SwingConstants.RIGHT
        return label
    }

    protected fun createTextField(target: T, getter : (T)->String, setter : (T, String)->Unit) : JTextField {
        val field = JTextField()
        field.columns = 40
        field.document = createDocument(target, getter, setter)
        return field
    }

    protected fun createDocument(target: T, getter : (T)->String, setter : (T, String)->Unit) : Document {
        val document = PlainDocument()
        document.insertString(0, getter(target), null)
        document.addDocumentListener(object : DocumentListener {
            override fun changedUpdate(e: DocumentEvent?) {
                if ( e != null ) setter(target, document.getText(0, document.length))
            }

            override fun insertUpdate(e: DocumentEvent?) {
                if ( e != null ) setter(target, document.getText(0, document.length))
            }

            override fun removeUpdate(e: DocumentEvent?) {
                if ( e != null ) setter(target, document.getText(0, document.length))
            }
        })
        return document
    }
}
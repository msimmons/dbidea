package net.contrapt.dbidea.ui

import org.apache.batik.ext.swing.GridBagConstants
import java.awt.GridBagConstraints
import java.awt.event.ActionEvent
import javax.swing.*
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener
import javax.swing.text.Document
import javax.swing.text.PlainDocument

/**
 * UI for editing connection configuration
 */
abstract class BaseDataPanel<T>(var isNew : Boolean) : JPanel() {

    protected fun setConstraints(gbc : GridBagConstraints, x : Int, y : Int, anchor : Int, weight : Double) : GridBagConstraints {
        gbc.gridx = x
        gbc.gridy = y
        gbc.anchor = anchor
        gbc.fill = GridBagConstants.HORIZONTAL
        gbc.ipadx = 4
        gbc.ipady = 4
        gbc.weightx = weight
        gbc.weighty = 0.0
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

    protected fun createIntField(target: T, getter : (T)->Int, setter : (T, Int)->Unit) : JTextField {
        val field = JTextField()
        field.columns = 40
        field.document = createIntDocument(target, getter, setter)
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

    protected fun createIntDocument(target: T, getter : (T)->Int, setter : (T, Int)->Unit) : Document {
        val document = PlainDocument()
        document.insertString(0, getter(target).toString(), null)
        document.addDocumentListener(object : DocumentListener {
            override fun changedUpdate(e: DocumentEvent?) {
                if ( e != null ) setter(target, document.getText(0, document.length).toInt())
            }

            override fun insertUpdate(e: DocumentEvent?) {
                if ( e != null ) setter(target, document.getText(0, document.length).toInt())
            }

            override fun removeUpdate(e: DocumentEvent?) {
                if ( e != null ) setter(target, document.getText(0, document.length).toInt())
            }
        })
        return document
    }

    protected fun createListDocument(data: MutableList<String>) : Document {
        val document = PlainDocument()
        document.insertString(0, data.joinToString(","), null)
        document.addDocumentListener(object : DocumentListener {
            override fun changedUpdate(e: DocumentEvent?) {
                if ( e != null ) {
                    data.clear()
                    document.getText(0, document.length).split(",").forEach {
                        data.add(it.trim())
                    }
                }
            }

            override fun insertUpdate(e: DocumentEvent?) {
                if ( e != null ) {
                    data.clear()
                    document.getText(0, document.length).split(",").forEach {
                        data.add(it.trim())
                    }
                }
            }

            override fun removeUpdate(e: DocumentEvent?) {
                if ( e != null ) {
                    data.clear()
                    document.getText(0, document.length).split(",").forEach {
                        data.add(it.trim())
                    }
                }
            }
        })
        return document
    }

    protected fun createCheckBox(target: T, getter: (T) -> Boolean, setter : (T, Boolean)->Unit) : JCheckBox {
        val action = object : AbstractAction() {
            override fun actionPerformed(e: ActionEvent?) {
                setter(target, !getter(target))
            }
        }
        val box = JCheckBox(action)
        box.isSelected = getter(target)
        return box
    }

    protected fun createListField(data: MutableList<String>) : JTextField {
        val field = JTextField()
        field.columns = 60
        field.document = createListDocument(data)
        return field
    }

}
package net.contrapt.dbidea.ui

import net.contrapt.dbidea.controller.ConnectionData
import net.contrapt.dbidea.controller.DriverData
import org.apache.batik.ext.swing.GridBagConstants
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import javax.swing.*

/**
 * UI for editing driver configuration
 */
class DriverDataPanel(driver : DriverData, isNew : Boolean) : BaseDataPanel<DriverData>(isNew) {

    private val nameField : JTextField
    private val classNameField : JTextField
    private val jarFileField : JTextField

    init {

        nameField = createTextField(driver, DriverData::name.getter, DriverData::name.setter)
        if ( isNew ) nameField.isEditable = true else nameField.isEditable = false
        classNameField = createTextField(driver, DriverData::className.getter, DriverData::className.setter)
        jarFileField = createTextField(driver, DriverData::jarFile.getter, DriverData::jarFile.setter)

        layout = GridBagLayout()
        val gbc = GridBagConstraints()
        gbc.fill = GridBagConstants.HORIZONTAL
        gbc.ipadx = 4
        gbc.ipady = 4
        add(createLabel("Name:"), setConstraints(gbc, 0, 0, GridBagConstraints.LINE_END))
        add(nameField, setConstraints(gbc, 1, 0, GridBagConstraints.LINE_START))
        add(createLabel("Driver Class Name:"), setConstraints(gbc, 0, 1, GridBagConstraints.LINE_END))
        add(classNameField, setConstraints(gbc, 1, 1, GridBagConstraints.LINE_START))
        add(createLabel("JAR File:"), setConstraints(gbc, 0, 2, GridBagConstraints.LINE_END))
        add(jarFileField, setConstraints(gbc, 1, 2, GridBagConstraints.LINE_START))
    }

    fun apply() {
        nameField.isEditable = false
    }
}
package net.contrapt.dbidea.ui

import net.contrapt.dbidea.controller.ApplicationData
import net.contrapt.dbidea.controller.ConnectionData
import net.contrapt.dbidea.controller.DriverData
import org.apache.batik.ext.swing.GridBagConstants
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import javax.swing.*
import javax.swing.event.ListDataListener

/**
 * UI for editing connection configuration
 */
class ConnectionDataPanel(val connection: ConnectionData, val applicationData : ApplicationData, isNew : Boolean) : BaseDataPanel<ConnectionData>(isNew) {

    private val nameField : JTextField
    private val driverCombo: JComboBox<String>
    private val urlField : JTextField
    private val userField : JTextField
    private val passwordField : JTextField
    private val autocommitField : JCheckBox
    private val fetchLimitField : JTextField
    private val schemasField : JTextField

    init {

        nameField = createTextField(connection, ConnectionData::name.getter, ConnectionData::name.setter)
        if ( isNew ) nameField.isEditable = true else nameField.isEditable = false
        driverCombo = createDriverCombo(connection, applicationData)
        urlField = createTextField(connection, ConnectionData::url.getter, ConnectionData::url.setter)
        userField = createTextField(connection, ConnectionData::user.getter, ConnectionData::user.setter)
        passwordField = createTextField(connection, ConnectionData::password.getter, ConnectionData::password.setter)
        autocommitField = createCheckBox(connection, ConnectionData::autocommit.getter, ConnectionData::autocommit.setter)
        fetchLimitField = createIntField(connection, ConnectionData::fetchLimit.getter, ConnectionData::fetchLimit.setter)
        schemasField = createListField(connection.schemas)

        layout = GridBagLayout()

        val gbc = GridBagConstraints()

        add(createLabel("Name:"), setConstraints(gbc, 0, 0, GridBagConstraints.LINE_END, .25))
        add(nameField, setConstraints(gbc, 1, 0, GridBagConstraints.LINE_START, 1.0))
        add(createLabel("Driver:"), setConstraints(gbc, 0, 1, GridBagConstraints.LINE_END, .25))
        add(driverCombo, setConstraints(gbc, 1, 1, GridBagConstraints.LINE_START, 1.0))
        add(createLabel("URL:"), setConstraints(gbc, 0, 2, GridBagConstraints.LINE_END, .25))
        add(urlField, setConstraints(gbc, 1, 2, GridBagConstraints.LINE_START, 1.0))
        add(createLabel("User:"), setConstraints(gbc, 0, 3, GridBagConstraints.LINE_END, .25))
        add(userField, setConstraints(gbc, 1, 3, GridBagConstraints.LINE_START, 1.0))
        add(createLabel("Password:"), setConstraints(gbc, 0, 4, GridBagConstraints.LINE_END, .25))
        add(passwordField, setConstraints(gbc, 1, 4, GridBagConstraints.LINE_START, 1.0))
        add(createLabel("Auto Commit:"), setConstraints(gbc, 0, 5, GridBagConstraints.LINE_END, .25))
        add(autocommitField, setConstraints(gbc, 1, 5, GridBagConstraints.LINE_START, 1.0))
        add(createLabel("Fetch Limit:"), setConstraints(gbc, 0, 6, GridBagConstraints.LINE_END, .25))
        add(fetchLimitField, setConstraints(gbc, 1, 6, GridBagConstraints.LINE_START, 1.0))
        add(createLabel("Schemas:"), setConstraints(gbc, 0, 7, GridBagConstraints.LINE_END, .25))
        add(schemasField, setConstraints(gbc, 1, 7, GridBagConstraints.LINE_START, 1.0))


    }

    private fun createDriverCombo(connection: ConnectionData, applicationData: ApplicationData) : JComboBox<String> {
        val combo = JComboBox<String>()
        combo.model = object : ComboBoxModel<String> {
            val appData = applicationData
            var selected = connection.driver
            override fun getElementAt(index: Int): String? {
                return appData.drivers[index]?.name
            }

            override fun getSize(): Int {
                return appData.drivers.size
            }

            override fun getSelectedItem(): Any? {
                return selected
            }

            override fun setSelectedItem(anItem: Any?) {
                selected = anItem as String
                connection.driver = selected
            }

            override fun addListDataListener(l: ListDataListener?) {
            }

            override fun removeListDataListener(l: ListDataListener?) {
            }
        }
        return combo
    }

    fun apply() {
        nameField.isEditable = false
    }

}
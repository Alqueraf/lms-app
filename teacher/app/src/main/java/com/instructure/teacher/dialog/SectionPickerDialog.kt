/*
 * Copyright (C) 2017 - present Instructure, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.instructure.teacher.dialog

import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v4.app.FragmentManager
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import com.instructure.canvasapi2.models.Section
import com.instructure.pandautils.utils.*
import com.instructure.teacher.R
import kotlinx.android.synthetic.main.dialog_section_picker.view.*
import kotlinx.android.synthetic.main.view_section_list_item.view.*
import java.io.Serializable

class SectionPickerDialog : DialogFragment() {

    init {
        retainInstance = true
    }

    var sections: List<Section> by SerializableListArg(default = emptyList())

    // Callback takes a String, which is a comma separated list of section ids
    var callback: (String) -> Unit = {}

    var initialSelectedSections: List<Section> by SerializableListArg(default = mutableListOf()) // All sections selected by default
    private lateinit var updatedCallback: (MutableList<Section>) -> Unit

    private var mutableSelectedSections: MutableList<Section> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mutableSelectedSections = initialSelectedSections.toMutableList()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        updatedCallback = {
            mutableSelectedSections = it
        } // Update the bundled arg for our initial sections; used on config changes

        // Add the "All sections" option
        val mutableSections = sections.toMutableList()
        mutableSections.add(0, Section().apply { name = activity.getString(R.string.allSections) })

        // Setup the view and recycler adapter
        val sectionsAdapter = SectionRecyclerViewAdapter(mutableSections, mutableSelectedSections, updatedCallback)

        val inflater = activity.layoutInflater
        val view = inflater.inflate(R.layout.dialog_section_picker, null, false)
        view.sectionRecyclerView.adapter = sectionsAdapter
        view.sectionRecyclerView.layoutManager = LinearLayoutManager(activity).apply { orientation = LinearLayoutManager.VERTICAL }

        val dialog = AlertDialog.Builder(activity)
                .setView(view)
                .setPositiveButton(android.R.string.ok, { _, _ ->
                    // Return the ids of the selected sections
                    callback(mutableSelectedSections.map { it.id }.joinToString(separator = ",") { it.toString() })
                })
                .setNegativeButton(android.R.string.cancel, { _, _ ->
                    callback(initialSelectedSections.map { it.id }.joinToString(separator = ",") { it.toString() })
                })
                .create()

        return dialog.apply {
            setOnShowListener {
                // Style dialog
                getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(ThemePrefs.buttonColor)
                getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(ThemePrefs.buttonColor)
            }
        }
    }

    override fun onDestroyView() {
        // Fix for rotation bug
        dialog?.let { if (retainInstance) it.setDismissMessage(null) }
        super.onDestroyView()
    }

    companion object {
        fun show(fragmentManager: FragmentManager, sections: List<Section>, selectedSections: List<Section>, callback: (String) -> Unit) = SectionPickerDialog().apply {
            fragmentManager.dismissExisting<SectionPickerDialog>()
            this.sections = sections
            this.callback = callback
            this.initialSelectedSections = selectedSections.toMutableList()
            show(fragmentManager, SectionPickerDialog::class.java.simpleName)
        }
    }
}

class SectionRecyclerViewAdapter(val sections: List<Section>, val selectedSections: MutableList<Section>, private val updatedCallback: (MutableList<Section>) -> Unit) : RecyclerView.Adapter<SectionRecyclerViewAdapter.SectionViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): SectionViewHolder {
        val sectionCheckView = LayoutInflater.from(parent?.context).inflate(R.layout.view_section_list_item, parent, false)
        sectionCheckView.checkbox.applyTheme(ThemePrefs.brandColor)
        return SectionViewHolder(sectionCheckView)
    }

    override fun getItemCount(): Int = sections.size

    override fun onBindViewHolder(holder: SectionViewHolder, position: Int) {
        val checked = if (selectedSections.isEmpty() && position == 0) true else selectedSections.contains(sections[position])
        holder.bind(sections[position].name, checked) { isChecked ->
            if (position == 0) {
                // 'All Sections' is selected, clear out all other selections and mark their positions
                sections.forEachIndexed { index, section -> if (selectedSections.contains(section)) notifyItemChanged(index) }
                selectedSections.apply { clear(); }
                notifyItemChanged(0)
            } else {
                if (isChecked) {
                    if (selectedSections.isEmpty()) {
                        // A specific section was selected; Uncheck 'all'
                        notifyItemChanged(0) // Allows checkbox animation to happen
                    }

                    selectedSections.add(sections[position]) // Add the newly selected section
                } else {
                    selectedSections.remove(sections[position]) // Section was deselected; remove it

                    if (selectedSections.isEmpty()) {
                        // Have to have at least one section selected... default to 'all'
                        notifyItemChanged(0)
                    }
                }

                updatedCallback(selectedSections)

                notifyItemChanged(position)
            }
        }
    }

    class SectionViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        fun bind(sectionName: String, checked: Boolean, callback: (Boolean) -> Unit) {
            view.sectionName.text = sectionName

            if ((checked && !view.checkbox.isChecked) || (!checked && view.checkbox.isChecked)) {
                // Checked state changed; update the checkbox
                view.checkbox.toggle()
            }

            view.checkbox.setOnClickListener { callback(view.checkbox.isChecked); (it as CheckBox).toggle()  }
            view.setOnClickListener { view.checkbox.performClick(); }
        }
    }
}
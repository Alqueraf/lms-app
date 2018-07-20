/*
 * Copyright (C) 2017 - present  Instructure, Inc.
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
 */
package com.instructure.teacher.dialog

import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.FragmentManager
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatDialogFragment
import android.support.v7.view.ContextThemeWrapper
import android.view.View
import com.instructure.canvasapi2.models.Course
import com.instructure.pandautils.utils.*
import com.instructure.pandautils.views.ColorPickerIcon
import com.instructure.teacher.R
import kotlin.properties.Delegates

class ColorPickerDialog: AppCompatDialogFragment() {

    init {
        retainInstance = true
    }

    private var callback: (Int) -> Unit by Delegates.notNull()
    private var course by ParcelableArg<Course>(key = Const.COURSE)

    companion object {
        @JvmStatic
        fun newInstance(manager: FragmentManager, course: Course, callback: (Int) -> Unit): ColorPickerDialog {
            manager.dismissExisting<ColorPickerDialog>()
            val dialog = ColorPickerDialog()
            val args = Bundle()
            args.putParcelable(Const.COURSE, course)
            dialog.arguments = args
            dialog.callback = callback
            return dialog
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(context)
        val view = View.inflate(ContextThemeWrapper(activity, 0), R.layout.dialog_color_picker, null)
        setupViews(view)
        builder.setView(view)
        builder.setTitle(R.string.colorPickerDialogTitle)
        builder.setCancelable(true)
        return builder.create()
    }

    fun setupViews(view: View) {

        val comp_colorCottonCandy = view.findViewById<ColorPickerIcon>(R.id.colorCottonCandy).circle
        val comp_colorBarbie = view.findViewById<ColorPickerIcon>(R.id.colorBarbie).circle
        val comp_colorBarneyPurple = view.findViewById<ColorPickerIcon>(R.id.colorBarneyPurple).circle
        val comp_colorEggplant = view.findViewById<ColorPickerIcon>(R.id.colorEggplant).circle
        val comp_colorUltramarine = view.findViewById<ColorPickerIcon>(R.id.colorUltramarine).circle

        val comp_colorOcean11 = view.findViewById<ColorPickerIcon>(R.id.colorOcean11).circle
        val comp_colorCyan = view.findViewById<ColorPickerIcon>(R.id.colorCyan).circle
        val comp_colorAquaMarine = view.findViewById<ColorPickerIcon>(R.id.colorAquaMarine).circle
        val comp_colorEmeraldGreen = view.findViewById<ColorPickerIcon>(R.id.colorEmeraldGreen).circle
        val comp_colorFreshCutLawn = view.findViewById<ColorPickerIcon>(R.id.colorFreshCutLawn).circle

        val comp_colorChartreuse = view.findViewById<ColorPickerIcon>(R.id.colorChartreuse).circle
        val comp_colorSunFlower = view.findViewById<ColorPickerIcon>(R.id.colorSunFlower).circle
        val comp_colorTangerine = view.findViewById<ColorPickerIcon>(R.id.colorTangerine).circle
        val comp_colorBloodOrange = view.findViewById<ColorPickerIcon>(R.id.colorBloodOrange).circle
        val comp_colorSriracha = view.findViewById<ColorPickerIcon>(R.id.colorSriracha).circle

        ColorUtils.colorIt(ContextCompat.getColor(context, R.color.colorCottonCandy), comp_colorCottonCandy)
        ColorUtils.colorIt(ContextCompat.getColor(context, R.color.colorBarbie), comp_colorBarbie)
        ColorUtils.colorIt(ContextCompat.getColor(context, R.color.colorBarneyPurple), comp_colorBarneyPurple)
        ColorUtils.colorIt(ContextCompat.getColor(context, R.color.colorEggplant), comp_colorEggplant)
        ColorUtils.colorIt(ContextCompat.getColor(context, R.color.colorUltramarine), comp_colorUltramarine)

        ColorUtils.colorIt(ContextCompat.getColor(context, R.color.colorOcean11), comp_colorOcean11)
        ColorUtils.colorIt(ContextCompat.getColor(context, R.color.colorCyan), comp_colorCyan)
        ColorUtils.colorIt(ContextCompat.getColor(context, R.color.colorAquaMarine), comp_colorAquaMarine)
        ColorUtils.colorIt(ContextCompat.getColor(context, R.color.colorEmeraldGreen), comp_colorEmeraldGreen)
        ColorUtils.colorIt(ContextCompat.getColor(context, R.color.colorFreshCutLawn), comp_colorFreshCutLawn)

        ColorUtils.colorIt(ContextCompat.getColor(context, R.color.colorChartreuse), comp_colorChartreuse)
        ColorUtils.colorIt(ContextCompat.getColor(context, R.color.colorSunFlower), comp_colorSunFlower)
        ColorUtils.colorIt(ContextCompat.getColor(context, R.color.colorTangerine), comp_colorTangerine)
        ColorUtils.colorIt(ContextCompat.getColor(context, R.color.colorBloodOrange), comp_colorBloodOrange)
        ColorUtils.colorIt(ContextCompat.getColor(context, R.color.colorSriracha), comp_colorSriracha)

        findSelectedColor(view)

        comp_colorCottonCandy.setOnClickListener {
            callback(ContextCompat.getColor(context, R.color.colorCottonCandy))
            dismiss()
        }
        comp_colorBarbie.setOnClickListener {
            callback(ContextCompat.getColor(context, R.color.colorBarbie))
            dismiss()
        }
        comp_colorBarneyPurple.setOnClickListener {
            callback(ContextCompat.getColor(context, R.color.colorBarneyPurple))
            dismiss()
        }
        comp_colorEggplant.setOnClickListener {
            callback(ContextCompat.getColor(context, R.color.colorEggplant))
            dismiss()
        }
        comp_colorUltramarine.setOnClickListener {
            callback(ContextCompat.getColor(context, R.color.colorUltramarine))
            dismiss()
        }
        comp_colorOcean11.setOnClickListener {
            callback(ContextCompat.getColor(context, R.color.colorOcean11))
            dismiss()
        }
        comp_colorCyan.setOnClickListener {
            callback(ContextCompat.getColor(context, R.color.colorCyan))
            dismiss()
        }
        comp_colorAquaMarine.setOnClickListener {
            callback(ContextCompat.getColor(context, R.color.colorAquaMarine))
            dismiss()
        }
        comp_colorEmeraldGreen.setOnClickListener {
            callback(ContextCompat.getColor(context, R.color.colorEmeraldGreen))
            dismiss()
        }
        comp_colorFreshCutLawn.setOnClickListener {
            callback(ContextCompat.getColor(context, R.color.colorFreshCutLawn))
            dismiss()
        }
        comp_colorChartreuse.setOnClickListener {
            callback(ContextCompat.getColor(context, R.color.colorChartreuse))
            dismiss()
        }
        comp_colorSunFlower.setOnClickListener {
            callback(ContextCompat.getColor(context, R.color.colorSunFlower))
            dismiss()
        }
        comp_colorTangerine.setOnClickListener {
            callback(ContextCompat.getColor(context, R.color.colorTangerine))
            dismiss()
        }
        comp_colorBloodOrange.setOnClickListener {
            callback(ContextCompat.getColor(context, R.color.colorBloodOrange))
            dismiss()
        }
        comp_colorSriracha.setOnClickListener {
            callback(ContextCompat.getColor(context, R.color.colorSriracha))
            dismiss()
        }
    }

    fun findSelectedColor(view: View) {
        when (ColorKeeper.getOrGenerateColor(course)) {
            ContextCompat.getColor(context, R.color.colorCottonCandy) -> view.findViewById<ColorPickerIcon>(R.id.colorCottonCandy).setSelected()
            ContextCompat.getColor(context, R.color.colorBarbie) -> view.findViewById<ColorPickerIcon>(R.id.colorBarbie).setSelected()
            ContextCompat.getColor(context, R.color.colorBarneyPurple) -> view.findViewById<ColorPickerIcon>(R.id.colorBarneyPurple).setSelected()
            ContextCompat.getColor(context, R.color.colorEggplant) -> view.findViewById<ColorPickerIcon>(R.id.colorEggplant).setSelected()
            ContextCompat.getColor(context, R.color.colorUltramarine) -> view.findViewById<ColorPickerIcon>(R.id.colorUltramarine).setSelected()
            ContextCompat.getColor(context, R.color.colorOcean11) -> view.findViewById<ColorPickerIcon>(R.id.colorOcean11).setSelected()
            ContextCompat.getColor(context, R.color.colorCyan) -> view.findViewById<ColorPickerIcon>(R.id.colorCyan).setSelected()
            ContextCompat.getColor(context, R.color.colorAquaMarine) -> view.findViewById<ColorPickerIcon>(R.id.colorAquaMarine).setSelected()
            ContextCompat.getColor(context, R.color.colorEmeraldGreen) -> view.findViewById<ColorPickerIcon>(R.id.colorEmeraldGreen).setSelected()
            ContextCompat.getColor(context, R.color.colorFreshCutLawn) -> view.findViewById<ColorPickerIcon>(R.id.colorFreshCutLawn).setSelected()
            ContextCompat.getColor(context, R.color.colorChartreuse) -> view.findViewById<ColorPickerIcon>(R.id.colorChartreuse).setSelected()
            ContextCompat.getColor(context, R.color.colorSunFlower) -> view.findViewById<ColorPickerIcon>(R.id.colorSunFlower).setSelected()
            ContextCompat.getColor(context, R.color.colorTangerine) -> view.findViewById<ColorPickerIcon>(R.id.colorTangerine).setSelected()
            ContextCompat.getColor(context, R.color.colorBloodOrange) -> view.findViewById<ColorPickerIcon>(R.id.colorBloodOrange).setSelected()
            ContextCompat.getColor(context, R.color.colorSriracha) -> view.findViewById<ColorPickerIcon>(R.id.colorSriracha).setSelected()
        }
    }

    override fun onDestroyView() {
        // Fix for rotation bug
        dialog?.let { if (retainInstance) it.setDismissMessage(null) }
        super.onDestroyView()
    }
}

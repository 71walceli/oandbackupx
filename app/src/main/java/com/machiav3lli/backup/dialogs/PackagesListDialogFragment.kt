/*
 * OAndBackupX: open-source apps backup and restore app.
 * Copyright (C) 2020  Antonios Hazim
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.machiav3lli.backup.dialogs

import android.app.Dialog
import android.content.DialogInterface
import android.content.pm.PackageInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.machiav3lli.backup.PACKAGES_LIST_ARGS_PACKAGES
import com.machiav3lli.backup.R
import com.machiav3lli.backup.handler.BackendController

class PackagesListDialogFragment(val filter: Int, private val isBlocklist: Boolean, private val onPackagesListChanged: (newList: Set<String>) -> Unit) : DialogFragment() {

    override fun onCreateDialog(savedInstance: Bundle?): Dialog {
        val pm = requireContext().packageManager
        val args = this.requireArguments()
        val selectedPackages = args.getStringArrayList(PACKAGES_LIST_ARGS_PACKAGES) ?: arrayListOf()

        var packageInfoList = BackendController.getPackageInfoList(requireContext(), filter)
        packageInfoList = packageInfoList.sortedWith { pi1: PackageInfo, pi2: PackageInfo ->
            val b1 = selectedPackages.contains(pi1.packageName)
            val b2 = selectedPackages.contains(pi2.packageName)
            if (b1 != b2)
                if (b1) -1 else 1
            else {
                val l1 = pi1.applicationInfo.loadLabel(pm).toString()
                val l2 = pi2.applicationInfo.loadLabel(pm).toString()
                l1.compareTo(l2, ignoreCase = true)
            }
        }
        val labels = mutableListOf<String>()
        val packagesNames = mutableListOf<String>()
        val checkedIndexes = BooleanArray(packageInfoList.size)
        val selections = mutableListOf<Int>()
        packageInfoList.forEachIndexed { i, packageInfo ->
            labels.add(packageInfo.applicationInfo.loadLabel(pm).toString())
            packagesNames.add(packageInfo.packageName)
            if (selectedPackages.contains(packageInfo.packageName)) {
                checkedIndexes[i] = true
                selections.add(i)
            }
        }
        val viewGroup = requireView().findViewById(android.R.id.content) as ViewGroup
        val dialogLayout = LayoutInflater.from(requireContext()).inflate(R.layout.activity_main_x, viewGroup)
        val dialogBuilder = AlertDialog.Builder(requireActivity())
                .setTitle(if (isBlocklist) R.string.sched_blocklist else R.string.customListTitle)
                .setView(dialogLayout)
                .setPositiveButton(R.string.dialogOK) { _: DialogInterface?, _: Int -> saveSelected(packagesNames, selections) }
                .setNegativeButton(R.string.dialogCancel) { dialog: DialogInterface?, _: Int -> dialog?.cancel() }
        return dialogBuilder.create()
    }

    private fun saveSelected(packagesNames: List<String>, selections: List<Int>) {
        val selectedPackages = selections
                .map { packagesNames[it] }
                .toSet()
        onPackagesListChanged(selectedPackages)
    }
}
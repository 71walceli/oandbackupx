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
import android.os.Bundle
import android.widget.Button
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.machiav3lli.backup.PACKAGES_LIST_ARGS_PACKAGES
import com.machiav3lli.backup.R
import com.machiav3lli.backup.handler.BackendController
import com.machiav3lli.backup.items.ItemMultichoice
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.adapters.ItemAdapter
import com.mikepenz.fastadapter.select.SelectExtension


class PackagesListDialogFragment(val filter: Int, private val isBlocklist: Boolean, private val onPackagesListChanged: (newList: Set<String>) -> Unit) : DialogFragment() {

    val multichoiceAdapter = ItemAdapter<ItemMultichoice>()
    var multichoiceFastAdapter: FastAdapter<ItemMultichoice>? = null
    lateinit var selectExtension: SelectExtension<ItemMultichoice>

    override fun onCreateDialog(savedInstance: Bundle?): Dialog {
        val selectedPackages = requireArguments().getStringArrayList(PACKAGES_LIST_ARGS_PACKAGES) ?: arrayListOf()
        val packagesNames = mutableListOf<String>()
        packagesNames.addAll(selectedPackages)
        val appList = BackendController.getApplicationList(requireContext(), false)
        
        multichoiceFastAdapter = FastAdapter.with(multichoiceAdapter)
        multichoiceFastAdapter?.onClickListener = { view, adapter, item, position ->
            item.isChecked = !item.isChecked
            if (item.isChecked)
                packagesNames.add(item.app.packageName)
            else
                packagesNames.remove(item.app.packageName)
            true // consume otherwise radio/checkbox will be deselected
        }
        //multichoiceFastAdapter?.onPreClickListener = { _: View?, _: IAdapter<ItemMultichoice>, _: ItemMultichoice, _: Int ->
        //    false
        //}
        //selectExtension = multichoiceFastAdapter!!.getSelectExtension()
        //selectExtension.isSelectable = true

        multichoiceAdapter.set(appList.map { 
            val itemMultiChoice = ItemMultichoice(it, it.packageName in packagesNames)
            itemMultiChoice
        })
        val dialog = Dialog(requireActivity())
        dialog.setTitle(if (isBlocklist) R.string.sched_blocklist else R.string.customListTitle)
        dialog.setContentView(R.layout.fragment_multichoice_list)
        val recyclerView = dialog.findViewById<RecyclerView>(R.id.multichoiceRecyclerView)
        recyclerView.adapter = multichoiceFastAdapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        dialog.findViewById<Button>(R.id.ok).setOnClickListener{
            saveSelected(packagesNames)
            dialog.dismiss()
        }
        dialog.findViewById<Button>(R.id.cancel).setOnClickListener{ dialog.cancel() }
        val width = (resources.displayMetrics.widthPixels * 0.90).toInt()
        val height = (resources.displayMetrics.heightPixels * 0.90).toInt()
        dialog.window?.setLayout(width, height)
        return dialog
    }

    private fun saveSelected(packagesNames: List<String>) {
        onPackagesListChanged(packagesNames.toSet())
    }
}
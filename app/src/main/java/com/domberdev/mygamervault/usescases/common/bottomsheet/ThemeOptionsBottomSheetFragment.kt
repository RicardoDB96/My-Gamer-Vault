package com.domberdev.mygamervault.usescases.common.bottomsheet

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import com.domberdev.mygamervault.databinding.FragmentThemeOptionsBottomSheetBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class ThemeOptionsBottomSheetFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentThemeOptionsBottomSheetBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentThemeOptionsBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val darkMode = this.activity?.getSharedPreferences("userdata", Context.MODE_PRIVATE)!!
        println(darkMode.getInt("darkModeStatus", 2))
        when (darkMode.getInt("darkModeStatus", 2)) {
            0 -> {
                binding.lightB.isChecked = true
                binding.darkB.isChecked = false
                binding.systemB.isChecked = false
            }
            1 -> {
                binding.lightB.isChecked = false
                binding.darkB.isChecked = true
                binding.systemB.isChecked = false
            }
            2 -> {
                binding.lightB.isChecked = false
                binding.darkB.isChecked = false
                binding.systemB.isChecked = true
            }
        }
        changeTheme()
    }

    private fun changeTheme(){
        val darkMode = this.activity?.getSharedPreferences("userdata", Context.MODE_PRIVATE)!!
        binding.lightB.setOnClickListener {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            darkMode.edit()?.putInt("darkModeStatus", 0)?.apply()
            darkMode.edit()?.putInt("modeStatus", 1)?.apply()
            dismiss()
        }
        binding.darkB.setOnClickListener {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            darkMode.edit()?.putInt("darkModeStatus", 1)?.apply()
            darkMode.edit()?.putInt("modeStatus", 2)?.apply()
            dismiss()
        }
        binding.systemB.setOnClickListener {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            darkMode.edit()?.putInt("darkModeStatus", 2)?.apply()
            darkMode.edit()?.putInt("modeStatus", -1)?.apply()
            dismiss()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}
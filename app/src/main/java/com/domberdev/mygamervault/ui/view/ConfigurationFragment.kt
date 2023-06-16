package com.domberdev.mygamervault.ui.view

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.domberdev.mygamervault.R
import com.domberdev.mygamervault.databinding.FragmentConfigurationBinding
import com.domberdev.mygamervault.usescases.common.bottomsheet.AboutBottomSheetFragment
import com.domberdev.mygamervault.usescases.common.bottomsheet.ThemeOptionsBottomSheetFragment

class ConfigurationFragment : Fragment() {

    private var _binding: FragmentConfigurationBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentConfigurationBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.configurationToolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        binding.changeThemeConfig.setOnClickListener {
            darkMode()
        }

        binding.openSourceConfig.setOnClickListener {
            findNavController().navigate(R.id.configurationFragment_to_licenseFragment)
        }

        binding.aboutConfig.setOnClickListener {
            aboutMGV()
        }
    }

    private fun darkMode() {
        val darkMode = this.activity?.getSharedPreferences("userdata", Context.MODE_PRIVATE)
        val status = darkMode?.getInt("darkModeStatus", 2)

        //Bottom Sheet
        val bottomSheetFragment = ThemeOptionsBottomSheetFragment()
        val bundle = Bundle()
        bundle.putInt("status", status!!)
        bottomSheetFragment.arguments = bundle
        bottomSheetFragment.show(parentFragmentManager, "ThemeOption")
    }

    private fun aboutMGV(){
        //Bottom Sheet
        val bottomSheetFragment = AboutBottomSheetFragment()
        bottomSheetFragment.show(parentFragmentManager, "AboutMGV")
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}
package com.domberdev.mygamervault.usescases.common.bottomsheet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.domberdev.mygamervault.databinding.FragmentAboutBottomSheetBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class AboutBottomSheetFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentAboutBottomSheetBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentAboutBottomSheetBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}
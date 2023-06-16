package com.domberdev.mygamervault.usescases.common.bottomsheet

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.domberdev.mygamervault.R
import com.domberdev.mygamervault.databinding.FragmentGameOptionsBottomSheetBinding
import com.domberdev.mygamervault.ui.viewmodel.GameViewModel
import com.domberdev.mygamervault.ui.viewmodel.GameViewModelFactory
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class GameOptionsBottomSheetFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentGameOptionsBottomSheetBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentGameOptionsBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val id = arguments?.getInt("gameID")
        binding.editGameBottom.setOnClickListener {
            val bundle = Bundle()
            bundle.putInt("mode", 1)
            bundle.putInt("gameID", id!!)
            findNavController().navigate(R.id.homeFragment_to_addEditGameFragment, bundle)
            dismiss()
        }
        binding.deleteSubjectBottom.setOnClickListener {
            deleteGame(id)
        }
    }

    private fun deleteGame(gameId: Int?){
        val dialogA = AlertDialog.Builder(requireContext())
        dialogA.setTitle(R.string.delete_game)
            .setMessage(getString(R.string.delete_game_message))
            .setPositiveButton(getString(R.string.yes))
            { dialog, _ ->
                val modelFactory = GameViewModelFactory(activity!!.application)
                val model = ViewModelProvider(this, modelFactory)[GameViewModel::class.java]
                model.delete(gameId!!)
                Toast.makeText(
                    requireContext(),
                    getString(R.string.deleting),
                    Toast.LENGTH_SHORT
                ).show()
                dialog.dismiss()
                dismiss()
            }
            .setNegativeButton(getString(R.string.no))
            { dialog, _ ->
                dialog.cancel()
            }.show()
    }
}
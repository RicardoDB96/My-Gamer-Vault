package com.domberdev.mygamervault.ui.view

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.domberdev.mygamervault.R
import com.domberdev.mygamervault.data.database.entities.ProfileEntity
import com.domberdev.mygamervault.databinding.FragmentHomeBinding
import com.domberdev.mygamervault.ui.viewmodel.GameViewModel
import com.domberdev.mygamervault.ui.viewmodel.GameViewModelFactory
import com.domberdev.mygamervault.ui.viewmodel.ProfileViewModel
import com.domberdev.mygamervault.ui.viewmodel.ProfileViewModelFactory
import com.domberdev.mygamervault.usescases.common.PermissionRequester
import com.domberdev.mygamervault.usescases.common.adapter.GamesAdapter
import com.domberdev.mygamervault.usescases.common.bottomsheet.GameOptionsBottomSheetFragment
import com.yalantis.ucrop.UCrop
import de.hdodenhof.circleimageview.CircleImageView
import java.io.File
import java.util.*

class HomeFragment : Fragment(), GamesAdapter.OnGameClickListener {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val photosPermission = PermissionRequester(this,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        onDenied = {
            Toast.makeText(
                requireContext(),
                getString(R.string.media_access_denied),
                Toast.LENGTH_SHORT
            ).show()
        })

    private lateinit var wantAdapter: GamesAdapter
    private lateinit var playingAdapter: GamesAdapter
    private lateinit var playedAdapter: GamesAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.window?.statusBarColor =
            ContextCompat.getColor(requireContext(), R.color.color_primary)
        user()
        binding.mainToolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.configuration -> {
                    findNavController().navigate(R.id.homeFragment_to_configurationFragment)
                    true
                }
                else -> {
                    false
                }
            }
        }
        binding.profileImage.setOnClickListener {
            findNavController().navigate(R.id.homeFragment_to_profileFragment)
        }
        binding.addFAB.setOnClickListener {
            val bundle = Bundle()
            bundle.putInt("mode", 0)
            findNavController().navigate(R.id.homeFragment_to_addEditGameFragment, bundle)
        }
        setProfilePic()

        val modelFactory = GameViewModelFactory(activity?.application!!)
        val model = ViewModelProvider(this, modelFactory)[GameViewModel::class.java]

        wantAdapter = GamesAdapter(requireContext(), this)
        playingAdapter = GamesAdapter(requireContext(), this)
        playedAdapter = GamesAdapter(requireContext(), this)

        binding.gamesWantRV.layoutManager = GridLayoutManager(requireContext(), 3)
        binding.gamesWantRV.adapter = wantAdapter

        binding.gamesPlayingRV.layoutManager = GridLayoutManager(requireContext(), 3)
        binding.gamesPlayingRV.adapter = playingAdapter

        binding.gamesPlayedRV.layoutManager = GridLayoutManager(requireContext(), 3)
        binding.gamesPlayedRV.adapter = playedAdapter

        model.wantGames.observe(viewLifecycleOwner) {
            if (it.isNotEmpty()) {
                binding.noGamesFoundT.visibility = View.GONE
                binding.gamesToPlay.visibility = View.VISIBLE
                wantAdapter.setListData(it)
                wantAdapter.notifyDataSetChanged()
            } else {
                binding.gamesToPlay.visibility = View.GONE
            }
        }

        model.playingGames.observe(viewLifecycleOwner) {
            if (it.isNotEmpty()) {
                binding.noGamesFoundT.visibility = View.GONE
                binding.gamesPlayingCard.visibility = View.VISIBLE
                playingAdapter.setListData(it)
                playingAdapter.notifyDataSetChanged()
            } else {
                binding.gamesPlayingCard.visibility = View.GONE
            }
        }

        model.playedGames.observe(viewLifecycleOwner) {
            if (it.isNotEmpty()) {
                binding.noGamesFoundT.visibility = View.GONE
                binding.gamesPlayed.visibility = View.VISIBLE
                playedAdapter.setListData(it)
                playedAdapter.notifyDataSetChanged()
            } else {
                binding.gamesPlayed.visibility = View.GONE
            }
        }

        model.allGames.observe(viewLifecycleOwner) {
            if (it.isEmpty()) {
                binding.noGamesFoundT.visibility = View.VISIBLE
            } else {
                binding.noGamesFoundT.visibility = View.GONE
            }
        }
    }

    private fun user() {
        val modelFactory = ProfileViewModelFactory(activity?.application!!)
        val model = ViewModelProvider(this, modelFactory)[ProfileViewModel::class.java]

        model.getProfile.observe(viewLifecycleOwner) {
            if (it.isNotEmpty()) {
                if (it[0].newUser) {
                    newUser()
                }
            } else {
                newUser()
            }
        }
    }

    private var pic: Bitmap? = null
    private var picImage: CircleImageView? = null

    private fun newUser() {
        val dialog = AlertDialog.Builder(requireContext()).create()
        val v = layoutInflater.inflate(R.layout.profile_information, null)
        dialog.setView(v)
        dialog.setCancelable(false)
        dialog.window?.decorView?.setBackgroundResource(R.drawable.dialog_background)
        dialog.show()

        val cancelButton = v.findViewById<TextView>(R.id.cancelProfileButton)
        cancelButton.visibility = View.GONE

        val profilePic = v.findViewById<CircleImageView>(R.id.profileImageChange)
        val changeUsername = v.findViewById<EditText>(R.id.changeUsername)
        val saveButton = v.findViewById<TextView>(R.id.saveProfileButton)

        picImage = profilePic

        profilePic.setOnClickListener {
            photosPermission.runWithPermission {
                val intent = Intent(Intent.ACTION_PICK)
                intent.type = "image/*"
                resultLauncher.launch(intent)
            }
        }

        saveButton.setOnClickListener {
            changeUsername.error = null
            if (changeUsername.text!!.isNotEmpty()) {
                val modelFactory = ProfileViewModelFactory(activity?.application!!)
                val model = ViewModelProvider(this, modelFactory)[ProfileViewModel::class.java]

                val uuid = UUID.randomUUID().toString()

                pic = if (picImage != null){
                    picImage!!.drawable.toBitmap()
                } else {
                    null
                }

                val profile = ProfileEntity(
                    UUID = uuid,
                    username = changeUsername.text.toString().trim(),
                    userPicture = pic,
                    userConsoles = null,
                    newUser = false
                )

                model.insert(profile)
                setProfilePic()
                dialog.dismiss()
            } else {
                if (changeUsername.text!!.isEmpty()) {
                    changeUsername.error = getString(R.string.write_username)
                }
            }
        }
    }

    private var resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val imageUri = result.data?.data!!

                startCrop(imageUri)
            }
        }

    private fun startCrop(uri: Uri) {
        val gameCoverUri = Uri.fromFile(File(requireContext().cacheDir, "profilePicture"))
        val uCrop = UCrop.of(uri, gameCoverUri)
        uCrop.withAspectRatio(1f, 1f)
        uCrop.withMaxResultSize(5000, 500)
        uCrop.withOptions(getCropOptions())

        resultUCropLauncher.launch(uCrop.getIntent(requireContext()))
    }

    private var resultUCropLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                if (picImage != null) {
                    Glide.with(this)
                        .asBitmap()
                        .load(UCrop.getOutput(result.data!!))
                        .placeholder(R.drawable.ic_profile_pic)
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .skipMemoryCache(true)
                        .into(picImage!!)
                }
            }
        }

    private fun getCropOptions(): UCrop.Options {
        val options = UCrop.Options()

        options.setCompressionQuality(70)

        //CompressType
        options.setCompressionFormat(Bitmap.CompressFormat.PNG)

        //UI
        options.setHideBottomControls(false)
        options.setFreeStyleCropEnabled(true)


        //UI Color
        options.setStatusBarColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.color_primary
            )
        )
        options.setToolbarColor(ContextCompat.getColor(requireContext(), R.color.color_primary))
        options.setActiveControlsWidgetColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.blue_light
            )
        )
        options.setToolbarWidgetColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.white
            )
        )//Toolbar icon and text color

        options.setToolbarTitle(getString(R.string.edit_game_cover))//Toolbar title

        return options
    }

    //Set the image profile
    private fun setProfilePic() {
        val modelFactory = ProfileViewModelFactory(activity?.application!!)
        val model = ViewModelProvider(this, modelFactory)[ProfileViewModel::class.java]

        model.getProfile.observe(viewLifecycleOwner) {
            if (it.size != 0) {
                Glide.with(this)
                    .load(it[0].userPicture)
                    .placeholder(R.drawable.ic_profile_pic)
                    .into(binding.profileImage)
            }
        }
    }

    override fun onGameClickListener(id: Int) {
        val bundle = Bundle()
        bundle.putInt("gameID", id)
        findNavController().navigate(R.id.homeFragment_to_gameInfoFragment, bundle)
    }

    override fun onGameLongClickListener(id: Int) {
        val bottomSheetFragment = GameOptionsBottomSheetFragment()
        val bundle = Bundle()
        bundle.putInt("gameID", id)
        bottomSheetFragment.arguments = bundle
        bottomSheetFragment.show(parentFragmentManager, "GameOptions")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
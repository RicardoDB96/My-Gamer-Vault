package com.domberdev.mygamervault.ui.view

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.ContentValues
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.graphics.drawable.toBitmap
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.domberdev.mygamervault.R
import com.domberdev.mygamervault.data.database.entities.ProfileEntity
import com.domberdev.mygamervault.databinding.FragmentProfileBinding
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
import java.io.FileOutputStream
import java.io.IOException

class ProfileFragment : Fragment(), GamesAdapter.OnGameClickListener {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var favoritesAdapter: GamesAdapter

    private val photosPermission = PermissionRequester(this,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        onDenied = {
            Toast.makeText(
                requireContext(),
                getString(R.string.media_access_denied),
                Toast.LENGTH_SHORT
            ).show()
        })

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentProfileBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        when (this.resources.configuration.uiMode.and(Configuration.UI_MODE_NIGHT_MASK)) {
            Configuration.UI_MODE_NIGHT_NO -> {
                val decorView = activity?.window?.decorView!!
                val wic = WindowInsetsControllerCompat(activity?.window!!, decorView)

                wic.isAppearanceLightStatusBars = true
            }
        }

        activity?.window?.statusBarColor =
            ContextCompat.getColor(requireContext(), R.color.status_bar_color)

        binding.profileToolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        val modelFactory = GameViewModelFactory(activity?.application!!)
        val model = ViewModelProvider(this, modelFactory)[GameViewModel::class.java]

        val modelFactoryP = ProfileViewModelFactory(activity?.application!!)
        val modelP = ViewModelProvider(this, modelFactoryP)[ProfileViewModel::class.java]

        modelP.getProfile.observe(viewLifecycleOwner) {
            if (it.isNotEmpty()) {
                binding.profileName.text = it[0].username
                Glide.with(requireContext())
                    .load(it[0].userPicture)
                    .placeholder(R.drawable.ic_profile_pic)
                    .into(binding.profileImage)

                binding.profileToolbar.setOnMenuItemClickListener { menu ->
                    when (menu.itemId){
                        R.id.editProfileInfo -> {
                            editUser(it[0])
                            true
                        }
                        R.id.shareProfileInfo -> {
                            val profileInfoLayout = binding.profileInfoLayout
                            val bitmap = getBitmapFromView(profileInfoLayout)
                            val uri = saveImage(bitmap)

                            val intent = Intent(Intent.ACTION_SEND)
                            intent.putExtra(Intent.EXTRA_STREAM, uri)
                            intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                            intent.type = "image/png"

                            startActivity(Intent.createChooser(intent, getString(R.string.share)))
                            true
                        }
                        else -> false
                    }
                }
            }
        }

        model.favoriteGame.observe(viewLifecycleOwner) {
            if (it.isNotEmpty()) {
                binding.favoriteGameProfileText.visibility = View.VISIBLE
                binding.favoriteGameLayout.visibility = View.VISIBLE
                val game = it[0]

                binding.gameTitleProfile.text = game.gameTitle
                Glide.with(requireContext())
                    .load(game.gameCover)
                    .placeholder(R.drawable.game_cover_background)
                    .into(binding.gameImageCoverProfile)
            } else {
                binding.favoriteGameProfileText.visibility = View.GONE
                binding.favoriteGameLayout.visibility = View.GONE
            }
        }

        favoritesAdapter = GamesAdapter(requireContext(), this)
        binding.favoriteGameRV.adapter = favoritesAdapter

        model.playedGames.observe(viewLifecycleOwner) {
            binding.gamePlayedProfile.text = "${it.size} ${getString(R.string.games)}"
        }

        model.wantGames.observe(viewLifecycleOwner) {
            binding.wantedGamesProfile.text = "${it.size} ${getString(R.string.games)}"
        }

        model.favoriteGames.observe(viewLifecycleOwner) {
            if (it.isNotEmpty()) {
                binding.favoriteGamesCV.visibility = View.VISIBLE
                favoritesAdapter.setListData(it)
                println(it.size)
                favoritesAdapter.notifyDataSetChanged()
            } else {
                binding.favoriteGamesCV.visibility = View.GONE
            }
        }
    }

    private fun getBitmapFromView(view: View): Bitmap {
        val returnedBitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(returnedBitmap)

        canvas.drawColor(context?.getColor(R.color.background_color)!!)

        view.draw(canvas)
        return returnedBitmap
    }

    private fun saveImage(image: Bitmap): Uri? {
        val imagesFolder = File(context?.cacheDir, "images")
        var uri: Uri? = null
        try {
            imagesFolder.mkdirs()
            val file = File(imagesFolder, "shared_image.png")
            val stream = FileOutputStream(file)
            image.compress(Bitmap.CompressFormat.PNG, 90, stream)
            stream.flush()
            stream.close()
            uri = FileProvider.getUriForFile(requireContext(), "com.domberdev.fileprovider", file)
        } catch (e: IOException) {
            Log.d(ContentValues.TAG, "IOException while trying to write file for sharing: " + e.message)
        }
        return uri
    }

    private var pic: Bitmap? = null
    private var picImage: CircleImageView? = null

    private fun editUser(profileEntity: ProfileEntity) {
        val dialog = AlertDialog.Builder(requireContext()).create()
        val v = layoutInflater.inflate(R.layout.profile_information, null)
        dialog.setView(v)
        dialog.window?.decorView?.setBackgroundResource(R.drawable.dialog_background)
        dialog.show()

        val cancelButton = v.findViewById<TextView>(R.id.cancelProfileButton)

        val profilePic = v.findViewById<CircleImageView>(R.id.profileImageChange)
        val changeUsername = v.findViewById<EditText>(R.id.changeUsername)
        val saveButton = v.findViewById<TextView>(R.id.saveProfileButton)

        picImage = profilePic

        if (profileEntity.userPicture != null){
            pic = profileEntity.userPicture
            Glide.with(this)
                .load(profileEntity.userPicture)
                .placeholder(R.drawable.ic_profile_pic)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .into(picImage!!)
        }

        changeUsername.setText(profileEntity.username)

        profilePic.setOnClickListener {
            photosPermission.runWithPermission {
                val intent = Intent(Intent.ACTION_PICK)
                intent.type = "image/*"
                resultLauncher.launch(intent)
            }
        }

        cancelButton.setOnClickListener {
            dialog.cancel()
        }

        saveButton.setOnClickListener {
            changeUsername.error = null
            if (changeUsername.text!!.isNotEmpty()) {
                val modelFactory = ProfileViewModelFactory(activity?.application!!)
                val model = ViewModelProvider(this, modelFactory)[ProfileViewModel::class.java]

                pic = if (picImage != null){
                    picImage!!.drawable.toBitmap()
                } else {
                    null
                }

                val profile = ProfileEntity(
                    UUID = profileEntity.UUID,
                    username = changeUsername.text.toString().trim(),
                    userPicture = pic,
                    userConsoles = null,
                    newUser = false
                )

                model.update(profile)
                setProfileInfo()
                dialog.dismiss()
            } else {
                if (changeUsername.text!!.isEmpty()) {
                    changeUsername.error = getString(R.string.write_username)
                }
            }
        }
    }

    //Set the image profile
    private fun setProfileInfo() {
        val modelFactory = ProfileViewModelFactory(activity?.application!!)
        val model = ViewModelProvider(this, modelFactory)[ProfileViewModel::class.java]

        model.getProfile.observe(viewLifecycleOwner) {
            if (it.size != 0) {
                binding.profileName.text = it[0].username

                Glide.with(this)
                    .asBitmap()
                    .load(it[0].userPicture)
                    .placeholder(R.drawable.ic_profile_pic)
                    .into(binding.profileImage)
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
                if (picImage != null){
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

    override fun onGameClickListener(id: Int) {
        val bundle = Bundle()
        bundle.putInt("gameID", id)
        findNavController().navigate(R.id.profileFragment_to_gameInfoFragment, bundle)
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
        when (this.resources.configuration.uiMode.and(Configuration.UI_MODE_NIGHT_MASK)) {
            Configuration.UI_MODE_NIGHT_NO -> {
                val decorView = activity?.window?.decorView!!
                val wic = WindowInsetsControllerCompat(activity?.window!!, decorView)

                wic.isAppearanceLightStatusBars = false
            }
        }
        _binding = null
    }
}
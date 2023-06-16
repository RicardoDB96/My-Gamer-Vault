package com.domberdev.mygamervault.ui.view

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat.getColor
import androidx.core.content.ContextCompat.getDrawable
import androidx.core.graphics.ColorUtils
import androidx.core.graphics.drawable.toBitmap
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.domberdev.mygamervault.R
import com.domberdev.mygamervault.data.database.entities.GameEntity
import com.domberdev.mygamervault.databinding.FragmentAddEditGameBinding
import com.domberdev.mygamervault.ui.viewmodel.GameViewModel
import com.domberdev.mygamervault.ui.viewmodel.GameViewModelFactory
import com.domberdev.mygamervault.usescases.common.PermissionRequester
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.gson.Gson
import com.yalantis.ucrop.UCrop
import java.io.File
import java.util.*

class AddEditGameFragment : Fragment() {

    private var _binding: FragmentAddEditGameBinding? = null
    private val binding get() = _binding!!

    private var interstitial: InterstitialAd? = null

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
        _binding = FragmentAddEditGameBinding.inflate(inflater, container, false)
        return binding.root
    }

    private var wantStatus = false
    private var playingStatus = false
    private var playedStatus = false

    private var gameStatus = -1
    private var imageURL: Bitmap? = null

    private var gameScoreEdit: Int? = null

    private fun initAds() {
        val adRequest = AdRequest.Builder().build()

        InterstitialAd.load(
            requireContext(),
            "ca-app-pub-3940256099942544/1033173712",
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    interstitial = interstitialAd
                }

                override fun onAdFailedToLoad(p0: LoadAdError) {
                    interstitial = null
                }
            })
    }

    private fun showAds() {
        val random = (0..100).random()
        if (random <= 90) {
            interstitial?.show(requireActivity())
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initAds()
        when (this.resources.configuration.uiMode.and(Configuration.UI_MODE_NIGHT_MASK)) {
            Configuration.UI_MODE_NIGHT_NO -> {
                val decorView = activity?.window?.decorView!!
                val wic = WindowInsetsControllerCompat(activity?.window!!, decorView)

                wic.isAppearanceLightStatusBars = true
            }
        }
        activity?.window?.statusBarColor = getColor(requireContext(), R.color.status_bar_color)

        val modelFactory = GameViewModelFactory(activity?.application!!)
        val model = ViewModelProvider(this, modelFactory)[GameViewModel::class.java]

        val mode = arguments?.getInt("mode")

        if (mode == 1) {
            binding.addEditGameB.text = getString(R.string.apply_changes)
            binding.addEditGameB.contentDescription = getString(R.string.apply_changes)
            val gameID = arguments?.getInt("gameID")!!

            model.getGameByID(gameID).observe(viewLifecycleOwner) {
                editGame(it)
                addEditGame(mode, it)
                gameScoreEdit = it.gameScore
            }
        } else {
            binding.addEditGameB.text = getString(R.string.add)
            binding.addEditGameB.contentDescription = getString(R.string.add)
            addEditGame(mode, null)
        }

        binding.materialToolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        gameImageChange()
        binding.wantB.setOnClickListener {
            wantStatus = !wantStatus
            gameStatus = if (wantStatus) 0 else -1
            playingStatus = false
            playedStatus = false
            changeStatus()
            setOptionalOptions()
        }
        binding.playingB.setOnClickListener {
            wantStatus = false
            playingStatus = !playingStatus
            gameStatus = if (playingStatus) 1 else -1
            playedStatus = false
            changeStatus()
            setOptionalOptions()
        }
        binding.playedB.setOnClickListener {
            wantStatus = false
            playingStatus = false
            playedStatus = !playedStatus
            gameStatus = if (playedStatus) 2 else -1
            changeStatus()
            setOptionalOptions()
        }

        binding.selectPlatformsB.setOnClickListener {
            platformsDialog()
        }
        setOptionalOptions()
    }

    private fun addEditGame(mode: Int?, gameEntity: GameEntity?) {
        val modelFactory = GameViewModelFactory(activity?.application!!)
        val model = ViewModelProvider(this, modelFactory)[GameViewModel::class.java]

        if (mode == 1) {
            binding.addEditGameB.setOnClickListener {
                binding.gameTitleET.error = null

                imageURL = if (!binding.gameCover.drawable.equals(getDrawable(requireContext(), R.drawable.game_cover_background))) {
                    binding.gameCover.drawable.toBitmap()
                } else {
                    null
                }

                val platformsList = arrayListOf<Int>()

                for (i in 0..4) {
                    if (platformsSelected[i]) {
                        platformsList.add(i)
                    }
                }

                val gameReview = if (binding.gameReviewET.text!!.isNotEmpty()) {
                    if (playedStatus) {
                        binding.gameReviewET.text.toString()
                    } else {
                        null
                    }
                } else {
                    null
                }

                val gameScore =
                    if (binding.scoreT.text != "" && binding.scoreT.text.isNotEmpty() && binding.scoreT.text != null) {
                        if (playedStatus) {
                            binding.scoreT.text.toString().toInt()
                        } else {
                            null
                        }
                    } else {
                        null
                    }

                val gameDescription = if (binding.gameDescriptionET.text!!.isNotEmpty()) {
                    binding.gameDescriptionET.text.toString()
                } else {
                    null
                }

                val isValidData =
                    binding.gameTitleET.text!!.isNotEmpty() && gameStatus != -1 && platformsList.isNotEmpty()

                val gson = Gson()
                val list = gson.toJson(platformsList)

                val favorite = if (gameStatus == 2) {
                    gameEntity!!.gameFavorite
                } else {
                    false
                }

                if (isValidData) {
                    val game = GameEntity(
                        id = gameEntity!!.id,
                        gameTitle = binding.gameTitleET.text.toString(),
                        gameStatus = gameStatus,
                        gameCover = imageURL,
                        gamePlatforms = list,
                        gameReview = gameReview,
                        gameScore = gameScore,
                        gameDescription = gameDescription,
                        gameEnd = null,
                        gameStart = null,
                        gameFavorite = favorite
                    )
                    model.update(game)
                    showAds()
                    findNavController().popBackStack()
                } else {
                    errorMessages(platformsList)
                }
            }
        } else {
            binding.addEditGameB.setOnClickListener {
                binding.gameTitleET.error = null

                println(binding.gameCover.drawable.current)
                println(getDrawable(requireContext(), R.drawable.game_cover_background))

                imageURL = if (!binding.gameCover.drawable.equals(getDrawable(requireContext(), R.drawable.game_cover_background))) {
                    binding.gameCover.drawable.toBitmap()
                } else {
                    null
                }

                val platformsList = arrayListOf<Int>()

                for (i in 0..4) {
                    if (platformsSelected[i]) {
                        platformsList.add(i)
                    }
                }

                val gameReview = if (binding.gameReviewET.text!!.isNotEmpty()) {
                    if (playedStatus) {
                        binding.gameReviewET.text.toString()
                    } else {
                        null
                    }
                } else {
                    null
                }

                val gameScore =
                    if (binding.scoreT.text != "" && binding.scoreT.text != null) {
                        if (playedStatus) {
                            binding.scoreT.text.toString()
                        } else {
                            null
                        }
                    } else {
                        null
                    }

                val gameDescription = if (binding.gameDescriptionET.text!!.isNotEmpty()) {
                    binding.gameDescriptionET.text.toString()
                } else {
                    null
                }

                val isValidData =
                    binding.gameTitleET.text!!.isNotEmpty() && gameStatus != -1 && platformsList.isNotEmpty()

                val gson = Gson()
                val list = gson.toJson(platformsList)

                if (isValidData) {
                    val game = GameEntity(
                        gameTitle = binding.gameTitleET.text.toString().trim(),
                        gameStatus = gameStatus,
                        gameCover = imageURL,
                        gamePlatforms = list,
                        gameReview = gameReview?.trim(),
                        gameScore = gameScore?.toInt(),
                        gameDescription = gameDescription?.trim(),
                        gameEnd = null,
                        gameStart = null,
                        gameFavorite = false
                    )
                    //Insert game in DB and back to Home
                    model.insert(game)
                    showAds()
                    findNavController().popBackStack()
                } else {//Check and notify the non-optional information
                    errorMessages(platformsList)
                }
            }
        }
    }

    private fun errorMessages(platformsList: ArrayList<Int>) {
        if (binding.gameTitleET.text!!.isEmpty()) {
            binding.gameTitleET.error = getString(R.string.must_write_game_title)
        }
        if (gameStatus == -1) {
            Toast.makeText(
                requireContext(),
                getString(R.string.must_select_state_game),
                Toast.LENGTH_SHORT
            ).show()
        }
        if (platformsList.isEmpty()) {
            Toast.makeText(
                requireContext(),
                getString(R.string.select_least_one_platform),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun gameImageChange() {
        binding.gameCoverChange.setOnClickListener {
            photosPermission.runWithPermission {
                val intent = Intent(Intent.ACTION_PICK)
                intent.type = "image/*"
                resultLauncher.launch(intent)
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

    private var uniqueID = UUID.randomUUID().toString()

    private fun startCrop(uri: Uri) {
        val gameCoverUri = Uri.fromFile(File(requireContext().cacheDir, uniqueID))
        val uCrop = UCrop.of(uri, gameCoverUri)
        uCrop.withAspectRatio(3f, 4f)
        uCrop.withMaxResultSize(1200, 1600)
        uCrop.withOptions(getCropOptions())

        resultUCropLauncher.launch(uCrop.getIntent(requireContext()))
    }

    private var resultUCropLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val imageUri = UCrop.getOutput(result.data!!)

                Glide.with(requireContext())
                    .asBitmap()
                    .load(imageUri)
                    .placeholder(R.drawable.game_cover_background)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .into(binding.gameCover)
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
            getColor(
                requireContext(),
                R.color.color_primary
            )
        )
        options.setToolbarColor(getColor(requireContext(), R.color.color_primary))
        options.setActiveControlsWidgetColor(
            getColor(
                requireContext(),
                R.color.blue_light
            )
        )
        options.setToolbarWidgetColor(
            getColor(
                requireContext(),
                R.color.white
            )
        )//Toolbar icon and text color

        options.setToolbarTitle(getString(R.string.edit_game_cover))//Toolbar title

        return options
    }

    private fun changeStatus() {
        if (wantStatus || playingStatus || playedStatus) {
            if (wantStatus) {
                changeDrawable(binding.wantB, R.drawable.ic_want_checked)
                changeDrawable(binding.playingB, R.drawable.ic_controller)
                changeDrawable(binding.playedB, R.drawable.ic_played)
            }
            if (playingStatus) {
                changeDrawable(binding.wantB, R.drawable.ic_want)
                changeDrawable(binding.playingB, R.drawable.ic_controller_checked)
                changeDrawable(binding.playedB, R.drawable.ic_played)
            }
            if (playedStatus) {
                changeDrawable(binding.wantB, R.drawable.ic_want)
                changeDrawable(binding.playingB, R.drawable.ic_controller)
                changeDrawable(binding.playedB, R.drawable.ic_played_checked)
            }
        } else {
            changeDrawable(binding.wantB, R.drawable.ic_want)
            changeDrawable(binding.playingB, R.drawable.ic_controller)
            changeDrawable(binding.playedB, R.drawable.ic_played)
        }
    }

    private fun changeDrawable(textView: TextView, drawable: Int) {
        textView.setCompoundDrawablesRelativeWithIntrinsicBounds(
            null,
            getDrawable(requireContext(), drawable)!!,
            null,
            null
        )
    }

    private fun setOptionalOptions() {
        binding.gameScoreChange.setOnClickListener {
            if (!playedStatus) {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.must_mark_game_played),
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                selectScore(binding.scoreT)
            }
        }
        if (playedStatus) {
            binding.gameReviewT.visibility = View.VISIBLE
            binding.gameReviewET.visibility = View.VISIBLE
            if (gameScoreEdit != null) {
                binding.scoreT.text = gameScoreEdit.toString()
                binding.scoreT.setBackgroundColor(
                    ColorUtils.blendARGB(
                        red,
                        green,
                        gameScoreEdit!! / 100f
                    )
                )
            }
        } else {
            binding.gameReviewT.visibility = View.GONE
            binding.gameReviewET.visibility = View.GONE
            binding.scoreT.text = ""
            binding.scoreT.setBackgroundColor(getColor(requireContext(), R.color.cover_background))
        }
    }

    val green = 0xFF66CC33.toInt()
    val red = 0xFFFF0000.toInt()

    private fun selectScore(textView: TextView) {
        val dialog = AlertDialog.Builder(requireContext())
        val v = layoutInflater.inflate(R.layout.select_score_dialog, null)
        dialog.setView(v)
        dialog.setTitle(getString(R.string.select_game_score))

        val scoreText = v.findViewById<TextView>(R.id.scoreText)
        val scoreBar = v.findViewById<SeekBar>(R.id.scoreBar)

        if (textView.text.isNotEmpty()) {
            scoreBar.progress = textView.text.toString().toInt()
            scoreText.text = textView.text
            scoreText.setBackgroundColor(
                ColorUtils.blendARGB(
                    red,
                    green,
                    textView.text.toString().toInt() / 100f
                )
            )
        }

        scoreBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                scoreText.text = p1.toString()
                scoreText.setBackgroundColor(ColorUtils.blendARGB(red, green, p1 / 100f))
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {
            }

            override fun onStopTrackingTouch(p0: SeekBar?) {
            }

        })

        dialog.setPositiveButton(getString(R.string.ok)) { d, _ ->
            if (scoreText.text.isNotEmpty()) {
                textView.text = scoreText.text
                textView.setBackgroundColor(
                    ColorUtils.blendARGB(
                        red,
                        green,
                        textView.text.toString().toInt() / 100f
                    )
                )
                gameScoreEdit = scoreText.text.toString().toInt()
            }
            d.dismiss()
        }
        dialog.setNegativeButton(getString(R.string.cancel)) { d, _ ->
            d.cancel()
        }
        dialog.create().show()
    }

    private val platformsSelected = booleanArrayOf(false, false, false, false, false)

    private fun platformsDialog() {
        val dialog = AlertDialog.Builder(requireContext(), R.style.MyAlertDialogTheme)

        dialog.setTitle(getString(R.string.select_platform))
            .setMultiChoiceItems(R.array.platforms, platformsSelected) { _, selectedItem, _ ->
                when (selectedItem) {
                    0 -> {
                        platformsSelected[selectedItem] = !platformsSelected[selectedItem]
                        platformsSelected[selectedItem] = !platformsSelected[selectedItem]
                    }
                    1 -> {
                        platformsSelected[selectedItem] = !platformsSelected[selectedItem]
                        platformsSelected[selectedItem] = !platformsSelected[selectedItem]
                    }
                    2 -> {
                        platformsSelected[selectedItem] = !platformsSelected[selectedItem]
                        platformsSelected[selectedItem] = !platformsSelected[selectedItem]
                    }
                    3 -> {
                        platformsSelected[selectedItem] = !platformsSelected[selectedItem]
                        platformsSelected[selectedItem] = !platformsSelected[selectedItem]
                    }
                    4 -> {
                        platformsSelected[selectedItem] = !platformsSelected[selectedItem]
                        platformsSelected[selectedItem] = !platformsSelected[selectedItem]
                    }
                }
            }
        dialog.setPositiveButton(getString(R.string.ok)) { d, _ ->
            d.dismiss()
        }
        dialog.setOnCancelListener { showSelectedPlatforms() }
        dialog.setOnDismissListener { showSelectedPlatforms() }
        dialog.show().getButton(Dialog.BUTTON_POSITIVE)
            .setTextColor(getColor(requireContext(), R.color.dialog_text_color))
    }

    private fun showSelectedPlatforms() {
        for (i in 0..4) {
            val icon = when (i) {
                0 -> binding.pcIcon
                1 -> binding.xboxIcon
                2 -> binding.playstationIcon
                3 -> binding.nintendoIcon
                4 -> binding.mobileIcon
                else -> binding.pcIcon
            }

            if (platformsSelected[i]) {
                icon.visibility = View.VISIBLE
            } else {
                icon.visibility = View.GONE
            }
        }
    }

    private fun editGame(gameEntity: GameEntity) {
        Glide.with(requireContext())
            .load(gameEntity.gameCover)
            .centerCrop()
            .placeholder(R.drawable.game_cover_background)
            .into(binding.gameCover)

        imageURL = gameEntity.gameCover

        binding.gameTitleET.setText(gameEntity.gameTitle)

        when (gameEntity.gameStatus) {
            0 -> {
                gameStatus = 0
                wantStatus = true
                playingStatus = false
                playedStatus = false
                changeStatus()
            }
            1 -> {
                gameStatus = 1
                wantStatus = false
                playingStatus = true
                playedStatus = false
                changeStatus()
            }
            2 -> {
                gameStatus = 2
                wantStatus = false
                playingStatus = false
                playedStatus = true
                changeStatus()
                setOptionalOptions()
            }
            else -> {
                gameStatus = -1
                wantStatus = false
                playingStatus = false
                playedStatus = false
                changeStatus()
            }
        }

        val gson = Gson()
        val list = gson.fromJson(gameEntity.gamePlatforms, ArrayList<Int>()::class.java)

        for (i in list.indices) {
            platformsSelected[i] = true
            val icon = when (i) {
                0 -> binding.pcIcon
                1 -> binding.xboxIcon
                2 -> binding.playstationIcon
                3 -> binding.nintendoIcon
                4 -> binding.mobileIcon
                else -> binding.pcIcon
            }

            if (platformsSelected[i]) {
                icon.visibility = View.VISIBLE
            } else {
                icon.visibility = View.GONE
            }
        }

        if (gameEntity.gameDescription != null) {
            binding.gameDescriptionET.setText(gameEntity.gameDescription)
        }

        if (gameEntity.gameScore != null) {
            binding.scoreT.text = gameEntity.gameScore.toString()
            binding.scoreT.setBackgroundColor(
                ColorUtils.blendARGB(
                    red,
                    green,
                    binding.scoreT.text.toString().toInt() / 100f
                )
            )
        }

        if (gameEntity.gameReview != null) {
            binding.gameReviewET.setText(gameEntity.gameReview)
        }
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
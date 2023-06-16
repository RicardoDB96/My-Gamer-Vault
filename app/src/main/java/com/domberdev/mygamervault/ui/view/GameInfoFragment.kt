package com.domberdev.mygamervault.ui.view

import android.app.AlertDialog
import android.content.ContentValues.TAG
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.graphics.ColorUtils
import androidx.core.view.get
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.domberdev.mygamervault.R
import com.domberdev.mygamervault.data.database.entities.GameEntity
import com.domberdev.mygamervault.databinding.FragmentGameInfoBinding
import com.domberdev.mygamervault.ui.viewmodel.GameViewModel
import com.domberdev.mygamervault.ui.viewmodel.GameViewModelFactory
import com.google.gson.Gson
import java.io.*

class GameInfoFragment : Fragment() {

    private var _binding: FragmentGameInfoBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentGameInfoBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    private var favoriteGame: Boolean = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.window?.statusBarColor =
            ContextCompat.getColor(requireContext(), R.color.color_primary)

        binding.gameInfoToolbar.overflowIcon?.setTint(Color.WHITE)

        val gameID = arguments?.getInt("gameID")!!

        val modelFactory = GameViewModelFactory(activity?.application!!)
        val model = ViewModelProvider(this, modelFactory)[GameViewModel::class.java]

        binding.gameInfoToolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        model.getGameByID(gameID).observe(viewLifecycleOwner) {
            bindData(it)
            if (it.gameStatus == 2) {
                binding.gameInfoToolbar.menu[0].isVisible = true
                favoriteGame = it.gameFavorite
                changeFavorite(it.gameFavorite)
            } else {
                binding.gameInfoToolbar.menu[0].isVisible = false
            }

        }

        var descriptionExpanded = false

        binding.descriptionInfo.setOnClickListener {
            if (descriptionExpanded) {
                binding.descriptionInfo.maxLines = 5
                descriptionExpanded = false
            } else {
                binding.descriptionInfo.maxLines = Int.MAX_VALUE
                descriptionExpanded = true
            }
        }

        var opinionExpanded = false

        binding.opinionInfo.setOnClickListener {
            if (opinionExpanded) {
                binding.opinionInfo.maxLines = 6
                opinionExpanded = false
            } else {
                binding.opinionInfo.maxLines = Int.MAX_VALUE
                opinionExpanded = true
            }
        }

        binding.gameInfoToolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.gameInfoFavorite -> {
                    favoriteGame = !favoriteGame
                    val text =
                        if (favoriteGame) R.string.marked_favorite else R.string.removed_favorites
                    Toast.makeText(requireContext(), text, Toast.LENGTH_SHORT).show()
                    changeFavorite(favoriteGame)
                    model.getGameByID(gameID).observe(viewLifecycleOwner) { game ->
                        updateFavoriteState(game)
                    }
                    true
                }
                R.id.shareGameInfo -> {
                    val gameInfoLayout = binding.gameInfoLayout
                    val bitmap = getBitmapFromView(gameInfoLayout)
                    val uri = saveImage(bitmap)

                    val intent = Intent(Intent.ACTION_SEND)
                    intent.putExtra(Intent.EXTRA_STREAM, uri)
                    intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                    intent.type = "image/png"

                    startActivity(Intent.createChooser(intent, getString(R.string.share)))
                    true
                }
                R.id.gameInfoEdit -> {
                    val bundle = Bundle()
                    bundle.putInt("mode", 1)
                    bundle.putInt("gameID", gameID)
                    findNavController().navigate(
                        R.id.gameInfoFragment_to_addEditGameFragment,
                        bundle
                    )
                    true
                }
                R.id.gameInfoDelete -> {
                    val dialog = AlertDialog.Builder(requireContext())
                    dialog.setTitle(R.string.delete_game)
                        .setMessage(getString(R.string.delete_game_message))
                        .setPositiveButton(getString(R.string.yes))
                        { dialogP, _ ->
                            model.delete(gameID)
                            Toast.makeText(
                                requireContext(),
                                getString(R.string.deleting),
                                Toast.LENGTH_SHORT
                            ).show()
                            dialogP.dismiss()
                            findNavController().popBackStack()
                        }
                        .setNegativeButton(getString(R.string.no))
                        { dialogN, _ ->
                            dialogN.cancel()
                        }.show()
                    true
                }
                else -> {
                    false
                }
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
            Log.d(TAG, "IOException while trying to write file for sharing: " + e.message)
        }
        return uri
    }

    private fun bindData(gameEntity: GameEntity) {
        binding.gameInfoTitle.text = gameEntity.gameTitle

        Glide.with(requireContext())
            .asBitmap()
            .load(gameEntity.gameCover)
            .placeholder(R.drawable.game_cover_background)
            .into(binding.gameCoverInfo)

        when (gameEntity.gameStatus) {
            0 -> {
                binding.wantInfo.visibility = View.VISIBLE
                binding.playingInfo.visibility = View.GONE
                binding.playedInfo.visibility = View.GONE
            }
            1 -> {
                binding.wantInfo.visibility = View.GONE
                binding.playingInfo.visibility = View.VISIBLE
                binding.playedInfo.visibility = View.GONE
            }
            2 -> {
                binding.wantInfo.visibility = View.GONE
                binding.playingInfo.visibility = View.GONE
                binding.playedInfo.visibility = View.VISIBLE
            }
            else -> {
                binding.wantInfo.visibility = View.GONE
                binding.playingInfo.visibility = View.GONE
                binding.playedInfo.visibility = View.GONE
            }
        }

        val gson = Gson()
        val list = gson.fromJson(gameEntity.gamePlatforms, ArrayList<Int>()::class.java)

        val green = 0xFF66CC33.toInt()
        val red = 0xFFFF0000.toInt()

        for (i in list.indices) {
            val icon = when (i) {
                0 -> binding.pcIconInfo
                1 -> binding.xboxIconInfo
                2 -> binding.playstationIconInfo
                3 -> binding.nintendoIconInfo
                4 -> binding.mobileIconInfo
                else -> binding.pcIconInfo
            }
            icon.visibility = View.VISIBLE
        }

        if (gameEntity.gameScore != null) {
            binding.scoreInfo.text = gameEntity.gameScore.toString()
            binding.scoreInfo.setBackgroundColor(
                ColorUtils.blendARGB(
                    red,
                    green,
                    binding.scoreInfo.text.toString().toInt() / 100f
                )
            )
        } else {
            binding.scoreInfoLayout.visibility = View.GONE
        }

        if (gameEntity.gameDescription != null) {
            binding.descriptionInfoLayout.visibility = View.VISIBLE
            binding.descriptionInfo.text = gameEntity.gameDescription
        } else {
            binding.descriptionInfoLayout.visibility = View.GONE
        }

        if (gameEntity.gameReview != null) {
            binding.opinionInfo.text = gameEntity.gameReview
            binding.opinionLayout.visibility = View.VISIBLE
        } else {
            binding.opinionLayout.visibility = View.GONE
        }
    }

    private fun updateFavoriteState(gameEntity: GameEntity) {
        val modelFactory = GameViewModelFactory(activity?.application!!)
        val model = ViewModelProvider(this, modelFactory)[GameViewModel::class.java]

        val game = GameEntity(
            id = gameEntity.id,
            gameTitle = gameEntity.gameTitle,
            gameStatus = gameEntity.gameStatus,
            gameCover = gameEntity.gameCover,
            gamePlatforms = gameEntity.gamePlatforms,
            gameReview = gameEntity.gameReview,
            gameScore = gameEntity.gameScore,
            gameDescription = gameEntity.gameDescription,
            gameEnd = null,
            gameStart = null,
            gameFavorite = favoriteGame
        )

        model.update(game)
    }

    private fun changeFavorite(favorite: Boolean) {
        binding.gameInfoToolbar.menu[0].icon = if (favorite) {
            requireContext().getDrawable(R.drawable.ic_star)
        } else {
            requireContext().getDrawable(R.drawable.ic_star_border)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}
package com.domberdev.mygamervault.usescases.common.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.ColorUtils
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.domberdev.mygamervault.R
import com.domberdev.mygamervault.data.database.entities.GameEntity
import com.domberdev.mygamervault.databinding.GameCoverItemRowBinding

class GamesAdapter(
    private val context: Context,
    private val onItemClickListener: OnGameClickListener
) :
    RecyclerView.Adapter<GamesAdapter.GamesViewHolder>() {

    private var dataList = mutableListOf<GameEntity>()

    fun setListData(data: MutableList<GameEntity>) {
        dataList = data
    }

    interface OnGameClickListener {
        fun onGameClickListener(id: Int)
        fun onGameLongClickListener(id: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GamesViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.game_cover_item_row, parent, false)
        return GamesViewHolder(view)
    }

    override fun onBindViewHolder(holder: GamesViewHolder, position: Int) {
        val game = dataList[position]
        holder.bindView(game)
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    inner class GamesViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val binding = GameCoverItemRowBinding.bind(itemView)
        fun bindView(gameEntity: GameEntity) {
            //Set cover image
            Glide.with(itemView)
                .load(gameEntity.gameCover)
                .centerCrop()
                .placeholder(R.drawable.game_cover_background)
                .into(binding.gameImageCover)

            val green = 0xFF66CC33.toInt()
            val red = 0xFFFF0000.toInt()

            if (gameEntity.gameStatus == 2 && gameEntity.gameScore != null){
                binding.gameScore.visibility = View.VISIBLE
                binding.gameScore.text = gameEntity.gameScore.toString()
                binding.gameScore.background.setTint(
                    ColorUtils.blendARGB(
                        red,
                        green,
                        gameEntity.gameScore / 100f
                    )
                )
            }else{
                binding.gameScore.visibility = View.GONE
            }

            binding.gameTitle.text = gameEntity.gameTitle //Set game title

            //Set on item click listener
            itemView.setOnClickListener {
                onItemClickListener.onGameClickListener(gameEntity.id)
            }
            itemView.setOnLongClickListener {
                onItemClickListener.onGameLongClickListener(gameEntity.id);true
            }
        }
    }
}
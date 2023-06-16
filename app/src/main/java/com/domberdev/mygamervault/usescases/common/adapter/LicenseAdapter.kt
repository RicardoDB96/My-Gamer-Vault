package com.domberdev.mygamervault.usescases.common.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.domberdev.mygamervault.R
import com.domberdev.mygamervault.databinding.LicenseListBinding
import com.domberdev.mygamervault.domain.model.License

class LicenseAdapter(private val license: List<License>, private val context: Context): RecyclerView.Adapter<LicenseAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.license_list, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindView(license[position])
    }

    override fun getItemCount(): Int {
        return license.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        private val binding =  LicenseListBinding.bind(itemView)
        fun bindView(license: License){
            binding.licenseTitle.text = license.licenseTitle
            binding.licenseContent.text = license.licenseContent
        }
    }
}
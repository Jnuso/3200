package com.example.criminalintent

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.criminalintent.databinding.ListItemCrimeBinding


class CrimeHolder(
    val binding : ListItemCrimeBinding
) : RecyclerView.ViewHolder(binding.root){

}
class CrimeListAdapter(
    private val crimesc : List<Crime>
): RecyclerView.Adapter<CrimeHolder() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CrimeHolder {
        TODO("Not yet implemented")
    }

    override fun onBindViewHolder(holder: CrimeHolder, position: Int) {
        TODO("Not yet implemented")
    }

    override fun getItemCount(): Int {
        TODO("Not yet implemented")
    }
}
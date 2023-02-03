package com.ngodesain.mondokin.ui.home

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.*
import com.ngodesain.mondokin.R
import com.ngodesain.mondokin.databinding.FragmentHomeBinding
import com.ngodesain.mondokin.ui.activity.BantuanActivity
import com.ngodesain.mondokin.ui.activity.DetailActivity


class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private var dataListPondokPesantren = ArrayList<DataPondokPesantren>()
    private var pondokPesantrenAdapter = PondokPesantrenAdapter()
    var progressDialog: Dialog? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        isLoading()
        getAllPondokPesantren("")
        binding.etSearch.addTextChangedListener(object : TextWatcher {


            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable) {
                if (s.toString() != null) {
                    getAllPondokPesantren(s.toString())
                } else {
                    getAllPondokPesantren("")
                }
            }
        })

        binding.fabHelp.setOnClickListener {
            startActivity(Intent(context, BantuanActivity::class.java))
        }


    }

    private fun getAllPondokPesantren(data: String) {

        progressDialog?.show()

        val mDatabaseTravel = FirebaseDatabase.getInstance().getReference("Daftar Pondok Pesantren")
        val query: Query = mDatabaseTravel
            .orderByChild("nama_pondok")
            .startAt(data)
            .endAt(data + "\uf8ff")
        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                dataListPondokPesantren.clear()
                for (getdataSnapshot in dataSnapshot.children) {

                    val daftarPondok = getdataSnapshot
                        .getValue(DataPondokPesantren::class.java)

                    dataListPondokPesantren.add(daftarPondok!!)
                    pondokPesantrenAdapter.setData(dataListPondokPesantren)
                    progressDialog?.dismiss()

                }

            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "" + error.message, Toast.LENGTH_LONG).show()
                progressDialog?.dismiss()
            }

        })
        if (activity != null) {

            pondokPesantrenAdapter = PondokPesantrenAdapter()
            pondokPesantrenAdapter.onItemClick = { selectedData ->
                val intent = Intent(activity, DetailActivity::class.java)
                intent.putExtra(DetailActivity.EXTRA_DATA, selectedData)
                startActivity(intent)
            }

            with(binding.rvList) {
                layoutManager = LinearLayoutManager(context)
                setHasFixedSize(true)
                adapter = pondokPesantrenAdapter
            }


        }

    }

    private fun isLoading() {
        progressDialog = Dialog(requireContext())
        val dialogLayout = layoutInflater.inflate(R.layout.dialog_loading, null)

        progressDialog?.let {
            it.setContentView(dialogLayout)
            it.setCancelable(false)
            it.window?.setBackgroundDrawableResource(android.R.color.transparent)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }



}
package com.example.heremapsdkandapirnd.dialog

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.heremapsdkandapirnd.R
import com.example.heremapsdkandapirnd.Routing
import com.example.heremapsdkandapirnd.Search
import com.example.heremapsdkandapirnd.SearchEngineListener
import com.example.heremapsdkandapirnd.adapters.RoutesAdapter
import com.example.heremapsdkandapirnd.viewmodel.MainViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.here.sdk.core.GeoCoordinates
import com.here.sdk.routing.Route
import com.here.sdk.routing.Waypoint
import com.here.sdk.search.Place
import kotlinx.android.synthetic.main.layout_dialog_routes.*

class RoutesDialog(val routeInfo: RouteInfo) : BottomSheetDialogFragment() {

    var routesAdapter = RoutesAdapter()

    val placeList = ArrayList<Place>()

    private var routesDialogListener: RoutesDialogListener? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.layout_dialog_routes, container, false)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is RoutesDialogListener){
            routesDialogListener = context
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        rvRoutes.apply {
            adapter = routesAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }

        routesAdapter.setItem(routeInfo.destinationList)

        btnCreateRoute.setOnClickListener {
            routeInfo.search.clearMap()

            if (routeInfo.destinationList.size >= 2) {
                routesDialogListener?.onClickedCreateRoute()
            }else{
                Toast.makeText(requireContext(),"please enter at least two locations",Toast.LENGTH_SHORT).show()
            }

            dismiss()
        }


    }

    fun setOnClickLister(routesDialogListener: RoutesDialogListener){
        this.routesDialogListener = routesDialogListener
    }

    companion object {
        const val TAG = "RoutesDialog"

        fun newInstance(routeInfo: RouteInfo): RoutesDialog {
            return RoutesDialog(routeInfo)
        }
    }

}

data class RouteInfo(
    val startLocation: GeoCoordinates,
    val destinationList: List<Place>,
    val routing: Routing,
    val search: Search
)

interface RoutesDialogListener{
    fun onClickedCreateRoute()
}
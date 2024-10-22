import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.interop.UIKitView
import cocoapods.GoogleMaps.GMSAdvancedMarker.Companion.markerImageWithColor
import cocoapods.GoogleMaps.GMSMarker
import cocoapods.Google_Maps_iOS_Utils.GMSCameraPosition
import cocoapods.Google_Maps_iOS_Utils.GMSCameraUpdate
import cocoapods.Google_Maps_iOS_Utils.GMSMapView
import cocoapods.Google_Maps_iOS_Utils.GMSMapViewDelegateProtocol
import cocoapods.Google_Maps_iOS_Utils.GMUClusterManager
import cocoapods.Google_Maps_iOS_Utils.GMUDefaultClusterIconGenerator
import cocoapods.Google_Maps_iOS_Utils.GMUDefaultClusterRenderer
import cocoapods.Google_Maps_iOS_Utils.GMUNonHierarchicalDistanceBasedAlgorithm
import kotlinx.cinterop.ExperimentalForeignApi
import platform.CoreLocation.CLLocationCoordinate2D
import platform.CoreLocation.CLLocationCoordinate2DMake
import platform.UIKit.UIColor
import platform.UIKit.UIEdgeInsetsMake
import platform.darwin.NSObject

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun MapView() {
    // TODO pass these in from map viewModel and have the mapViewModel
    // correctly handle passing these into this component
    val initialized = remember { mutableStateOf(false) }
    val googleMapViewEntries = remember { getMarkers() }
    val cameraPosition: GMSCameraPosition =
        GMSCameraPosition.cameraWithLatitude(
            latitude = 30.274185, // googleMapViewEntries.first().lat,
            longitude = -97.74054, // googleMapViewEntries.first().lng,
            zoom = 16f,
        )

    val mapView = remember { GMSMapView() }
    val iconGenerator = remember { GMUDefaultClusterIconGenerator() }
    val algorithm = remember { GMUNonHierarchicalDistanceBasedAlgorithm() }
    val renderer = remember { GMUDefaultClusterRenderer(mapView, iconGenerator) }
    val clusterManager = remember { GMUClusterManager(mapView, algorithm, renderer) }
    val cameraUpdate = GMSCameraUpdate.setCamera(cameraPosition)

    val delegate = remember {
        object : NSObject(), GMSMapViewDelegateProtocol {
            override fun mapView(
                mapView: GMSMapView,
                didTapMarker: cocoapods.Google_Maps_iOS_Utils.GMSMarker,
            ): Boolean {
//                val userData = didTapMarker.userData()
//                if (userData is CustomEvent) {
//                    onMarkerClick(userData)
//                }
                println("marker tapped")
                return false
            }

            // Note: this shows an error,
            // but it compiles and runs fine(!)
            // Kotlin doesn't like the multi overload, but it is swift compliant
            override fun mapView(
                mapView: GMSMapView,
                didTapInfoWindowOfMarker: cocoapods.Google_Maps_iOS_Utils.GMSMarker,
            ) {
                println("info window tapped")
            }

            // Note: this shows an error,
            // but it compiles and runs fine(!)
            // Kotlin doesn't like the multi overload, but it is swift compliant
            override fun mapView(
                mapView: GMSMapView,
                didCloseInfoWindowOfMarker: cocoapods.Google_Maps_iOS_Utils.GMSMarker,
            ) {
                print("info window closed")
//                val customEvent = (didCloseInfoWindowOfMarker.userData() as CustomEvent)
            }
        }
    }

    UIKitView(
        modifier = Modifier,
        factory = { mapView },
        onRelease = {
            it.removeFromSuperview()
        },
        update = {
            if (!initialized.value) {
                setupMapSettings(mapView)
                addMarkersToClusterer(googleMapViewEntries, clusterManager)
                mapView.moveCamera(cameraUpdate)
                clusterManager.setMapDelegate(delegate)
                initialized.value = true
            }
        },
        interactive = true,
    )
}

@OptIn(ExperimentalForeignApi::class)
private fun addMarkersToClusterer(
    googleMapViewEntries: List<GMSMarker>,
    clusterManager: GMUClusterManager,
) {
    clusterManager.addItems(googleMapViewEntries)
    clusterManager.cluster()
}

@OptIn(ExperimentalForeignApi::class)
private fun setupMapSettings(mapView: GMSMapView) {
    // Map Settings
    mapView.settings().setCompassButton(true)
    mapView.settings().setMyLocationButton(true)
    mapView.settings().setAllGesturesEnabled(true)
    mapView.settings().setIndoorPicker(true)
    mapView.setPadding(UIEdgeInsetsMake(0.0, 0.0, 40.0, 0.0))
}

@OptIn(ExperimentalForeignApi::class)
private fun getMarkers(): List<GMSMarker> {

// Custom Markers for Austin Area
    val austinMarkers = listOf(
        GMSMarker().apply {
            this.position = CLLocationCoordinate2DMake(latitude= 30.267153, longitude= -97.743057)
            this.title = "Texas State Capitol"
            this.snippet = "The iconic Texas State Capitol building, a must-visit landmark."
        },
        GMSMarker().apply {
            this.position = CLLocationCoordinate2DMake(latitude= 30.265340, longitude= -97.748905)
            this.title = "Barton Springs Pool"
            this.snippet = "A popular outdoor swimming pool filled with natural spring water."
        },
        GMSMarker().apply {
            this.position = CLLocationCoordinate2DMake(latitude= 30.262579, longitude= -97.739020)
            this.title = "6th Street Entertainment District"
            this.snippet = "Austin's famous street for nightlife, live music, and entertainment."
        },
        GMSMarker().apply {
            this.position = CLLocationCoordinate2DMake(latitude= 30.280556, longitude= -97.732222)
            this.title = "University of Texas at Austin"
            this.snippet = "Explore the beautiful campus of UT Austin, home to the Longhorns."
        },
        GMSMarker().apply {
            this.position = CLLocationCoordinate2DMake(latitude= 30.280556, longitude= -97.732222)
            this.title = "University of Texas at Austin"
            this.snippet = "Explore the beautiful campus of UT Austin, home to the Longhorns."
        }
    )
    return austinMarkers
}
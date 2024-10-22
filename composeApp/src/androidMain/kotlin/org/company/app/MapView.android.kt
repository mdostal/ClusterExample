import android.content.ContentValues.TAG
import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.Cluster
import com.google.maps.android.clustering.ClusterItem
import com.google.maps.android.clustering.algo.NonHierarchicalViewBasedAlgorithm
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.MapsComposeExperimentalApi
import com.google.maps.android.compose.clustering.Clustering
import com.google.maps.android.compose.clustering.rememberClusterManager
import com.google.maps.android.compose.clustering.rememberClusterRenderer
import com.google.maps.android.compose.rememberCameraPositionState
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
@OptIn(
    ExperimentalComposeUiApi::class,
    ExperimentalMaterial3Api::class,
    MapsComposeExperimentalApi::class,
)
@Composable
actual fun MapView() {
    val zoomLevel = remember { mutableFloatStateOf(16f) }
    val currentLocation = LatLng(30.266666, -97.733330)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(currentLocation, zoomLevel.value)
    }

    val mapUiSettings by remember {
        mutableStateOf(
            MapUiSettings(
                zoomGesturesEnabled = true,
            ),
        )
    }
    val markers = remember { mutableStateListOf<CustomMarker>() }
    var finePermissionGranted by remember { mutableStateOf(false) }

    val properties =
        MapProperties(
            isBuildingEnabled = true,
            isIndoorEnabled = true,
            isMyLocationEnabled = finePermissionGranted,
            mapType = MapType.NORMAL,
        )

    GoogleMap(
        modifier = Modifier,
        contentPadding = PaddingValues(bottom = 40.dp),
        properties = properties,
        uiSettings = mapUiSettings,
        onMyLocationButtonClick = {
            false
        },
        onMapLoaded = {
            setMarkers(markers, getMarkerList())
        },
        onMapClick = {},
        cameraPositionState = cameraPositionState,
    ) {
        CustomClustering(
            items = markers,
            onMarkerClick = {false},
            onMarkerInfoClick = {},
        )
    }
}

fun getMarkerList(): List<CustomMarker> {

    val customMarkers = listOf(
        CustomMarker(0f, LatLng(30.266666, -97.733330), "Marker 1", "Description"),
        CustomMarker(0f, LatLng(30.266666, -97.733330), "Marker 2", "Description"),
        CustomMarker(0f, LatLng(30.266666, -97.733330), "Marker 3", "Description"),
        CustomMarker(0f, LatLng(30.266666, -97.733330), "Marker 4", "Description"),
        CustomMarker(0f, LatLng(30.266666, -97.733330), "Marker 5", "Description"),
        CustomMarker(0f, LatLng(30.266666, -97.733330), "Marker 6", "Description"),
        CustomMarker(0f, LatLng(30.266666, -97.733330), "Marker 7", "Description"),
        CustomMarker(0f, LatLng(30.266666, -97.733330), "Marker 8", "Description"),
        CustomMarker(0f, LatLng(30.266666, -97.733330), "Marker 9", "Description"),
        CustomMarker(0f, LatLng(30.266666, -97.733330), "Marker 10", "Description")
    )
    return customMarkers
}
fun setMarkers(markers: MutableList<CustomMarker>, markerList: List<CustomMarker>) {
    markers.clear()
    if (markerList.isNotEmpty()) {
        markers.addAll(markerList)
    }
}

fun getClusterProps(cluster: Cluster<CustomMarker>): Triple<Float, Color, String> {
    val (circleSize, color, text) = when {
        cluster.size < 10 -> Triple(50f, Color(18, 53, 143), cluster.size.toString())
        cluster.size < 20 -> Triple(60f, Color(8, 88, 153), "10+")
        cluster.size < 50 -> Triple(70f, Color(22, 122, 150), "20+")
        cluster.size < 100 -> Triple(70f, Color(17, 139, 67), "50+")
        cluster.size < 200 -> Triple(70f, Color(85, 151, 43), "100+")
        else -> Triple(80f, Color(133, 46, 13), "200+")
    }
    return Triple(circleSize, color, text)
}

@Composable
fun DefaultCluster(cluster: Cluster<CustomMarker>) {
    val (circleSize, color, text) = getClusterProps(cluster)
    Box(
        modifier = Modifier
            .size(circleSize.dp),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            drawCircle(
                color = Color.LightGray,
                radius = circleSize + 5,
            )
            drawCircle(
                color = color,
                radius = circleSize,
            )
        }
        // Draw text centered on top of the circle
        Text(
            // TODO REVIEWER: Do we want the text to be generic after a certain number or always exact?
            text = text,
            // text= cluster.size.toString(),
            color = Color.White,
            modifier = Modifier.align(Alignment.Center),
        )
    }
}

@OptIn(MapsComposeExperimentalApi::class)
@Composable
fun CustomClustering(items: List<CustomMarker>, onMarkerClick: (CustomMarker) -> Boolean, onMarkerInfoClick: (Any) -> Unit) {
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val screenWidth = configuration.screenWidthDp.dp
    val clusterManager = rememberClusterManager<CustomMarker>()
    var clickedClusterPosition by remember { mutableStateOf<LatLng?>(null) }
    var clickedCluster by remember { mutableStateOf<Cluster<CustomMarker>?>(null) }

    // Here the clusterManager is being customized with a NonHierarchicalViewBasedAlgorithm.
    // This speeds up by a factor the rendering of items on the screen.
    clusterManager?.setAlgorithm(
        NonHierarchicalViewBasedAlgorithm(
            screenWidth.value.toInt(),
            screenHeight.value.toInt(),
        ),
    )

    val renderer = rememberClusterRenderer(
        clusterContent = { cluster: Cluster<CustomMarker> ->
            if (clickedClusterPosition == cluster.position) {
                ExpandedClusterComposable(
                    cluster,
                )
            } else {
                DefaultCluster(cluster)
            }
        },
        clusterItemContent = { it: CustomMarker ->
//            val event = it.getEvent()
            val color = Color.Red
            Icon(
                imageVector = Icons.Filled.Place,
                "Marker with color of $color",
                modifier = Modifier.size(50.dp),
                tint = color,
            )
        },
        clusterManager = clusterManager,
    )
    SideEffect {
        clusterManager ?: return@SideEffect
        clusterManager.setOnClusterClickListener { cluster ->
            if (clickedClusterPosition != cluster.position) {
                clickedClusterPosition = cluster.position
                clickedCluster = cluster
            } else {
                clickedCluster = null
                clickedClusterPosition = null
            }
            Log.d(TAG, "Cluster clicked! $cluster")
            Log.d(TAG, "Clicky Clicky Cluster")
            // TODO: Handle cluster click here, it dies when the following section is triggered
            false
        }
        clusterManager.setOnClusterItemClickListener {
                it: CustomMarker ->
//            onMarkerClick(it.getEvent())
            Log.d(TAG, "Cluster item clicked! $it")
            false
        }
        clusterManager.setOnClusterItemInfoWindowClickListener { it ->
            onMarkerInfoClick(it)
            Log.d(TAG, "Cluster item info window clicked! $it")
        }
    }
    SideEffect {
        if (clusterManager?.renderer != renderer) {
            clusterManager?.renderer = renderer ?: return@SideEffect
        }
    }

    if (clusterManager != null) {
        Clustering(
            items = items,
            clusterManager = clusterManager,
        )
    }
}

@Composable
fun ExpandedClusterComposable(cluster: Cluster<CustomMarker>) {
    val itemsToShow = cluster.items.take(10)
    val (circleSize, color, text) = getClusterProps(cluster)
    val boxSize = circleSize * 4 // 200
    val radius = (boxSize / 10).dp // 20.dp
    val circlePadding = 0.8 // this decreases the circle space used by 10%
    Box(
        modifier = Modifier
            .size((boxSize + 10).dp)
            .shadow(5.dp),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            drawCircle(
                color = Color.LightGray,
                radius = ((boxSize / 2) + 5).dp.toPx(),
            )
            drawCircle(
                color = color,
                radius = (boxSize / 2).dp.toPx(),
            )
            drawCircle(
                color = Color.Black,
                radius = 15.dp.toPx(),
            )
        }
        // Close "button" in the center
        Text(
            text = "X",
            modifier = Modifier.align(Alignment.Center),
            color = Color.White,
            fontWeight = FontWeight.Bold,
        )

        itemsToShow.forEachIndexed { index, item ->
            val angleIncrement = (2 * PI / itemsToShow.size).toFloat()
            val angle = index * angleIncrement
            val x = with(LocalDensity.current) { radius.toPx() * cos(angle) }
            val y = with(LocalDensity.current) { radius.toPx() * sin(angle) }
            val iconColor = remember { mutableStateOf(Color.Red) }
            Column(
                modifier = Modifier
                    .size((boxSize * circlePadding * 2 / itemsToShow.size).dp)
//                    .height(((boxSize * circlePadding * 2) / itemsToShow.size).dp)
//                    .width((boxSize / 2).dp)
                    .align(Alignment.Center)
                    .offset(x.dp, y.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                item.title?.let {
                    Text(
                        text = it,
                        textAlign = TextAlign.Center,
                        color = Color.White,
                    )
                }
                Icon(
                    imageVector = Icons.Filled.Place,
                    contentDescription = item.title,
                    modifier = Modifier.size(40.dp),
                    tint = iconColor.value,
                )
            }
        }
    }
}

class CustomMarker(
    zIdx: Float = 0f,
    pos: LatLng,
    title: String,
    desc: String,
) : ClusterItem {

    private val position: LatLng
    private val title: String
    private val snippet: String
    private val zIndex: Float
    init {
        position = pos
        this.title = title
        snippet = desc
        zIndex = zIdx
    }
    override fun getPosition(): LatLng {
        return position
    }

    override fun getTitle(): String? {
        return title
    }

    override fun getSnippet(): String? {
        return snippet
    }

    override fun getZIndex(): Float? {
        return zIndex
    }
}

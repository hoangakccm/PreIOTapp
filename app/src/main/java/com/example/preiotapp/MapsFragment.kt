import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Canvas
import android.graphics.Paint
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.example.preiotapp.R
import org.mapsforge.core.model.LatLong
import org.mapsforge.map.android.graphics.AndroidBitmap
import org.mapsforge.map.android.graphics.AndroidGraphicFactory
import org.mapsforge.map.android.util.AndroidUtil
import org.mapsforge.map.android.view.MapView
import org.mapsforge.map.layer.overlay.Marker
import org.mapsforge.map.layer.renderer.TileRendererLayer
import org.mapsforge.map.reader.MapFile
import org.mapsforge.map.rendertheme.InternalRenderTheme
import java.io.*

class MapsFragment : Fragment(), LocationListener {
    private lateinit var locationManager: LocationManager
    private lateinit var currentLocationMarker: Marker
    private lateinit var lastLocation: Location
    private lateinit var mapView : MapView
    private lateinit var load_map: Button
    private lateinit var center_button: Button
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        AndroidGraphicFactory.createInstance(requireActivity().application)
        val view = inflater.inflate(R.layout.fragment_maps, container, false)

        mapView = view.findViewById(R.id.map)
        load_map = view.findViewById(R.id.load_map)
        center_button = view.findViewById(R.id.center_button)

        val mapFileName = "world.map"
        loadMapFromAssets(mapFileName)

        center_button.setOnClickListener {
            moveMapToLocation(lastLocation)
        }
        return view
    }
    private fun loadMapFromAssets(mapFileName: String) {
        val context = requireContext()
        val cache = AndroidUtil.createTileCache(
            context,
            "mycache",
            mapView.model.displayModel.tileSize,
            1f,
            mapView.model.frameBufferModel.overdrawFactor
        )

        try {
            // Copy the map file from assets to external storage directory
            val mapDir = context.getExternalFilesDir("maps")
            val mapFile = File(mapDir, mapFileName)

            if (!mapFile.exists()) {
                mapDir?.mkdirs()
                mapFile.createNewFile()

                val inputStream: InputStream = context.assets.open(mapFileName)
                val outputStream: OutputStream = FileOutputStream(mapFile)

                val buffer = ByteArray(1024)
                var length: Int

                while (inputStream.read(buffer).also { length = it } > 0) {
                    outputStream.write(buffer, 0, length)
                }

                outputStream.flush()
                outputStream.close()
                inputStream.close()
            }

            val mapStore = MapFile(mapFile.absolutePath)

            val renderLayer = TileRendererLayer(
                cache,
                mapStore,
                mapView.model.mapViewPosition,
                AndroidGraphicFactory.INSTANCE
            )

            renderLayer.setXmlRenderTheme(InternalRenderTheme.DEFAULT)

            mapView.layerManager.layers.add(renderLayer)

            mapView.setCenter(LatLong(21.0468762, 105.7977244))
            mapView.setZoomLevel(10)

            currentLocationMarker = createMarker()
            mapView.layerManager.layers.add(currentLocationMarker)

            locationManager = requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
            requestLocationUpdates()

            updateLocationMarker()
            moveMapToLocation(lastLocation)

        } catch (e: IOException) {
            e.printStackTrace()
            // Handle any errors that might occur when loading the map from assets
        }
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val contract = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            result?.data?.data?.let { uri ->
                openMap(uri)
            }
        }
        load_map.setOnClickListener {
            contract.launch(
                Intent(
                    Intent.ACTION_OPEN_DOCUMENT
                ).apply {
                    type = "*/*"
                    addCategory(Intent.CATEGORY_OPENABLE)
                }
            )

        }
    }

    private fun openMap(uri: Uri) {
        center_button.setOnClickListener {
            moveMapToLocation(lastLocation)
        }

        mapView.mapScaleBar.isVisible = true
        mapView.setBuiltInZoomControls(true)
        val cache = AndroidUtil.createTileCache(
            requireContext(),
            "mycache",
            mapView.model.displayModel.tileSize,
            1f,
            mapView.model.frameBufferModel.overdrawFactor
        )

        val stream = requireContext().contentResolver.openInputStream(uri) as FileInputStream

        val mapStore = MapFile(stream)

        val renderLayer = TileRendererLayer(
            cache,
            mapStore,
            mapView.model.mapViewPosition,
            AndroidGraphicFactory.INSTANCE
        )

        renderLayer.setXmlRenderTheme(
            InternalRenderTheme.DEFAULT
        )

        mapView.layerManager.layers.add(renderLayer)

        mapView.setCenter(LatLong(21.0468762, 105.7977244))
        mapView.setZoomLevel(2)

        currentLocationMarker = createMarker()
        mapView.layerManager.layers.add(currentLocationMarker)

        locationManager = requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        requestLocationUpdates()

        updateLocationMarker()
        moveMapToLocation(lastLocation)
    }

    override fun onLocationChanged(location: Location) {
        updateLocationMarker()
    }

    private fun requestLocationUpdates() {
        val permission = Manifest.permission.ACCESS_FINE_LOCATION
        if (ActivityCompat.checkSelfPermission(requireContext(), permission) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(permission), PERMISSION_REQUEST_CODE)
        } else {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_BETWEEN_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this)
        }
    }

    @SuppressLint("MissingPermission")
    private fun updateLocationMarker() {
        val lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
        lastKnownLocation?.let {
            val latLong = LatLong(it.latitude, it.longitude)
            currentLocationMarker.latLong = latLong
            mapView.invalidate()
            lastLocation = it
        }
    }

    private fun createMarker(): Marker {
        val circleRadius = 15f

        val bitmap = android.graphics.Bitmap.createBitmap(
            (2 * circleRadius).toInt(),
            (2 * circleRadius).toInt(),
            android.graphics.Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)

        val paint = Paint()
        paint.style = Paint.Style.FILL
        paint.color = android.graphics.Color.RED
        canvas.drawCircle(circleRadius, circleRadius, circleRadius, paint)

        return Marker(LatLong(21.0468762, 105.7977244), AndroidBitmap(bitmap), 0, 0)
    }

    private fun moveMapToLocation(location: Location) {
        val latLong = LatLong(location.latitude, location.longitude)
        mapView.setCenter(latLong)
    }

    companion object {
        private const val PERMISSION_REQUEST_CODE = 1
        private const val MIN_TIME_BETWEEN_UPDATES = 1000L
        private const val MIN_DISTANCE_CHANGE_FOR_UPDATES = 10f
    }
}

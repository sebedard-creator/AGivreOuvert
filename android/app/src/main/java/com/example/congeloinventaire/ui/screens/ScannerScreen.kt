package com.example.congeloinventaire.ui.screens

import android.Manifest
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.congeloinventaire.viewmodel.InventoryViewModel
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScannerScreen(
    viewModel: InventoryViewModel,
    isAdding: Boolean,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    
    val scanResult by viewModel.scanResult.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var hasCameraPermission by remember { mutableStateOf(false) }
    
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            hasCameraPermission = granted
        }
    )

    LaunchedEffect(Unit) {
        permissionLauncher.launch(Manifest.permission.CAMERA)
        viewModel.clearScanResult()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isAdding) "Ajouter un produit" else "Retirer un produit") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Retour")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (hasCameraPermission) {
                // Camera Preview
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { ctx ->
                        val previewView = PreviewView(ctx)
                        val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                        
                        cameraProviderFuture.addListener({
                            val cameraProvider = cameraProviderFuture.get()
                            val preview = Preview.Builder().build().also {
                                it.setSurfaceProvider(previewView.surfaceProvider)
                            }

                            val barcodeScanner = BarcodeScanning.getClient()
                            val imageAnalysis = ImageAnalysis.Builder()
                                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                .build()
                                
                            imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
                                val mediaImage = imageProxy.image
                                if (mediaImage != null && scanResult == null && !isLoading) {
                                    val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
                                    barcodeScanner.process(image)
                                        .addOnSuccessListener { barcodes ->
                                            if (barcodes.isNotEmpty()) {
                                                val upc = barcodes.first().rawValue
                                                if (upc != null) {
                                                    viewModel.lookupBarcode(upc)
                                                }
                                            }
                                        }
                                        .addOnCompleteListener {
                                            imageProxy.close()
                                        }
                                } else {
                                    imageProxy.close()
                                }
                            }

                            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                            try {
                                cameraProvider.unbindAll()
                                cameraProvider.bindToLifecycle(
                                    lifecycleOwner,
                                    cameraSelector,
                                    preview,
                                    imageAnalysis
                                )
                            } catch (e: Exception) {
                                Log.e("Scanner", "Camera bind error", e)
                            }
                        }, ContextCompat.getMainExecutor(ctx))
                        
                        previewView
                    }
                )
                
                // Overlay for scanning area
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(300.dp, 150.dp)
                        .background(Color.Transparent)
                ) {
                    // Cible visuelle (simplifiée)
                    Text("Placez le code-barres ici", color = Color.White, modifier = Modifier.align(Alignment.TopCenter))
                }
                
                // Loading indicator
                if (isLoading) {
                    Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5f)), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color.White)
                    }
                }

                // Bottom Sheet for Results
                if (scanResult != null) {
                    BottomSheetResult(
                        scanResult = scanResult!!,
                        isAdding = isAdding,
                        onConfirm = { name ->
                            if (isAdding) {
                                viewModel.addItem(name, scanResult!!.upc)
                                onNavigateBack()
                            } else {
                                // Pour le retrait, on doit choisir un lot s'il y en a plusieurs,
                                // mais pour simplifier on retire le plus vieux.
                                if (scanResult!!.local_lots.isNotEmpty()) {
                                    viewModel.removeItem(scanResult!!.local_lots.first().id)
                                    onNavigateBack()
                                }
                            }
                        },
                        onCancel = {
                            viewModel.clearScanResult()
                        }
                    )
                }
                
            } else {
                Text("Permission caméra requise.", modifier = Modifier.align(Alignment.Center))
            }
        }
    }
}

@Composable
fun BottomSheetResult(
    scanResult: com.example.congeloinventaire.network.ScanResult,
    isAdding: Boolean,
    onConfirm: (String) -> Unit,
    onCancel: () -> Unit
) {
    // État local pour le formulaire manuel si Open Food Facts ne trouve rien
    var manualName by remember { mutableStateOf(scanResult.off_product_name ?: "") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .padding(24.dp)
    ) {
        if (isAdding) {
            Text("Produit Détecté", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color.Black, modifier = Modifier.padding(bottom = 16.dp))
            
            if (scanResult.off_product_name == null) {
                Text("Produit inconnu. Saisie manuelle :", color = Color.Red, modifier = Modifier.padding(bottom = 8.dp))
            } else {
                Text("Vous pouvez modifier le nom par défaut :", color = Color.Black, modifier = Modifier.padding(bottom = 8.dp))
            }
            
            OutlinedTextField(
                value = manualName,
                onValueChange = { manualName = it },
                label = { Text("Nom du produit") },
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                    cursorColor = Color.Black,
                    focusedBorderColor = Color.Black,
                    unfocusedBorderColor = Color.Black,
                    focusedLabelColor = Color.Black,
                    unfocusedLabelColor = Color.Black
                )
            )
            Button(onClick = { onConfirm(manualName) }, modifier = Modifier.fillMaxWidth(), enabled = manualName.isNotBlank()) {
                Text("Ajouter au congélateur")
            }
        } else {
            // Mode Retrait
            Text("Retirer un produit", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color.Black, modifier = Modifier.padding(bottom = 16.dp))
            if (scanResult.exists_in_database) {
                Text("Trouvé dans l'inventaire ! (${scanResult.local_lots.size} lot(s) disponible(s))", color = Color.Black)
                Text("Nom : ${scanResult.off_product_name ?: "Produit local"}", color = Color.Black)
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { onConfirm("") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Retirer")
                }
            } else {
                Text("Ce produit n'est pas dans votre congélateur.", color = Color.Red)
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        TextButton(onClick = onCancel, modifier = Modifier.fillMaxWidth()) {
            Text("Annuler (Scanner à nouveau)", color = Color.Black)
        }
    }
}

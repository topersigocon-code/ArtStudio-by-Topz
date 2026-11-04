package com.topz.artstudio

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import java.io.File

class MainActivity : Activity() {
    private lateinit var root: LinearLayout
    private lateinit var content: LinearLayout
    private var canvasView: DrawingCanvasView? = null
    private var currentFile: File? = null

    private val bg = Color.rgb(18, 22, 32)
    private val panel = Color.rgb(29, 36, 50)
    private val gold = Color.rgb(238, 184, 67)
    private val ink = Color.rgb(242, 244, 248)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showGallery()
    }

    private fun baseLayout(title: String): LinearLayout {
        root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(bg)
        }
        val header = TextView(this).apply {
            text = title
            textSize = 20f
            setTextColor(gold)
            gravity = Gravity.CENTER_VERTICAL
            setPadding(18, 14, 18, 14)
            setBackgroundColor(panel)
        }
        root.addView(header, LinearLayout.LayoutParams(-1, 58))
        content = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        root.addView(content, LinearLayout.LayoutParams(-1, 0, 1f))
        setContentView(root)
        return content
    }

    private fun makeButton(label: String, action: () -> Unit): Button =
        Button(this).apply {
            text = label
            isAllCaps = false
            setTextColor(ink)
            setBackgroundColor(Color.rgb(48, 58, 76))
            setOnClickListener { action() }
        }

    private fun showGallery() {
        canvasView = null
        val body = baseLayout("ArtStudio by Topz")
        val intro = TextView(this).apply {
            text = "Your offline creative studio"
            textSize = 14f
            setTextColor(ink)
            setPadding(18, 18, 18, 8)
        }
        body.addView(intro)
        body.addView(makeButton("＋  Create New Canvas") { openCanvas(null) },
            LinearLayout.LayoutParams(-1, 58).apply { setMargins(16, 10, 16, 16) })

        val files = artworkFiles()
        if (files.isEmpty()) {
            body.addView(TextView(this).apply {
                text = "No artwork yet. Create a canvas to start drawing."
                setTextColor(Color.LTGRAY)
                gravity = Gravity.CENTER
                textSize = 15f
            }, LinearLayout.LayoutParams(-1, 0, 1f))
        } else {
            files.sortedByDescending { it.lastModified() }.forEach { file ->
                val row = LinearLayout(this).apply {
                    orientation = LinearLayout.HORIZONTAL
                    gravity = Gravity.CENTER_VERTICAL
                    setPadding(14, 8, 14, 8)
                    setBackgroundColor(panel)
                }
                val name = TextView(this).apply {
                    text = file.nameWithoutExtension
                    textSize = 16f
                    setTextColor(ink)
                    setPadding(8, 8, 8, 8)
                }
                row.addView(name, LinearLayout.LayoutParams(0, 52, 1f))
                row.addView(makeButton("Open") { openCanvas(file) })
                body.addView(row, LinearLayout.LayoutParams(-1, 64).apply {
                    setMargins(12, 4, 12, 4)
                })
            }
        }
    }

    private fun openCanvas(file: File?) {
        currentFile = file
        val body = baseLayout("ArtStudio  •  ${file?.nameWithoutExtension ?: "Untitled"}")
        val toolbar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setBackgroundColor(panel)
        }
        body.addView(toolbar, LinearLayout.LayoutParams(-1, 54))
        val view = DrawingCanvasView(this)
        canvasView = view
        if (file != null && file.exists()) view.loadImage(file)
        body.addView(view, LinearLayout.LayoutParams(-1, 0, 1f))

        toolbar.addView(makeButton("Brush") { view.setEraser(false) })
        toolbar.addView(makeButton("Erase") { view.setEraser(true) })
        toolbar.addView(makeButton("Undo") { view.undo() })
        toolbar.addView(makeButton("Redo") { view.redo() })
        toolbar.addView(makeButton("Save") { saveArtwork() })

        val colors = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setBackgroundColor(panel)
        }
        body.addView(colors, LinearLayout.LayoutParams(-1, 48))
        listOf(Color.BLACK, Color.WHITE, Color.RED, Color.BLUE, Color.GREEN, gold).forEach { color ->
            val swatch = View(this).apply {
                setBackgroundColor(color)
                setOnClickListener { view.setColor(color) }
            }
            colors.addView(swatch, LinearLayout.LayoutParams(34, 34).apply {
                setMargins(9, 5, 9, 5)
            })
        }

        val footer = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        body.addView(footer, LinearLayout.LayoutParams(-1, 48))
        footer.addView(makeButton("Gallery") { showGallery() },
            LinearLayout.LayoutParams(0, -1, 1f))
        footer.addView(makeButton("Clear") { view.clearCanvas() },
            LinearLayout.LayoutParams(0, -1, 1f))
    }

    private fun saveArtwork() {
        val view = canvasView ?: return
        try {
            val dir = File(filesDir, "artworks").apply { mkdirs() }
            val target = currentFile ?: File(dir, "Artwork_${System.currentTimeMillis()}.png")
            view.saveImage(target)
            currentFile = target
            android.widget.Toast.makeText(this, "Artwork saved on this device", android.widget.Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            android.widget.Toast.makeText(this, "Save failed: ${e.localizedMessage}", android.widget.Toast.LENGTH_LONG).show()
        }
    }

    private fun artworkFiles(): List<File> =
        File(filesDir, "artworks").listFiles()?.filter {
            it.isFile && it.extension.equals("png", true)
        } ?: emptyList()
}

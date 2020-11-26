package me.hufman.androidautoidrive.phoneui

import android.animation.ValueAnimator
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.MenuInflater
import android.view.View
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.menu.MenuBuilder
import androidx.core.view.GravityCompat
import androidx.lifecycle.observe
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.animation.ArgbEvaluatorCompat
import kotlinx.android.synthetic.main.activity_navhost.*
import me.hufman.androidautoidrive.Analytics
import me.hufman.androidautoidrive.AppSettings
import me.hufman.androidautoidrive.MainService
import me.hufman.androidautoidrive.R
import me.hufman.androidautoidrive.phoneui.viewmodels.ConnectionViewModel
import java.lang.IllegalStateException

class NavHostActivity: AppCompatActivity() {

	private val connectionViewModel by viewModels<ConnectionViewModel> { ConnectionViewModel.Factory(this.applicationContext) }

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		Analytics.init(this)
		AppSettings.loadSettings(this)
		L.loadResources(this)

		setContentView(R.layout.activity_navhost)
		setSupportActionBar(nav_toolbar)

		val navMenu = MenuBuilder(this).also {
			MenuInflater(this).inflate(R.menu.menu_main, it)
		}
		val navController = findNavController(R.id.nav_host_fragment)
		val appBarConfig = AppBarConfiguration(navMenu, drawer_layout)
		setupActionBarWithNavController(navController, appBarConfig)        // title updater
		nav_toolbar.setupWithNavController(navController, appBarConfig)     // hamburger click handler
		nav_view.setupWithNavController(navController)                      // nav menu click handler

		setupNavHeader()

		setupNavMenu()

		startService()

		drawer_layout.post { drawer_layout.openDrawer(GravityCompat.START) }
	}

	fun setupNavHeader() {
		val headerView = nav_view.getHeaderView(0)
		val txtConnectionStatus = nav_view.getHeaderView(0).findViewById<TextView>(R.id.txtConnectionStatus)
		connectionViewModel.connectedStatus.observe(this) {
			txtConnectionStatus.text = this.run(it)
		}
		connectionViewModel.connectedStatusColor.observe(this) {
			val color = this.run(it)
			val startColor = headerView.backgroundTintList?.defaultColor
			if (startColor != color) {
				if (startColor == null) {
					headerView.backgroundTintList = ColorStateList.valueOf(color)
				} else {
					ValueAnimator.ofObject(ArgbEvaluatorCompat(), startColor, color).apply {
						addUpdateListener { headerView.backgroundTintList = ColorStateList.valueOf(it.animatedValue as Int) }
						start()
					}
				}
			}
		}
	}

	fun setupNavMenu() {
		val themeAttrs = obtainStyledAttributes(R.style.optionGmapVisible, arrayOf(android.R.attr.visibility).toIntArray())
		val mapVisibility = themeAttrs.getInt(0, 0)
		themeAttrs.recycle()
		nav_view.menu.findItem(R.id.nav_maps).isVisible = mapVisibility == View.VISIBLE
	}

	fun startService() {
		try {
			this.startService(Intent(this, MainService::class.java).setAction(MainService.ACTION_START))
		} catch (e: IllegalStateException) {
			// Android Oreo strenuously objects to starting the service if the activity isn't visible
			// for example, when Android Studio tries to start the Activity with the screen off
		}
	}
}
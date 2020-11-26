package me.hufman.androidautoidrive.phoneui.viewmodels

import ChassisCode
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import me.hufman.androidautoidrive.CarInformation
import me.hufman.androidautoidrive.CarInformationObserver
import me.hufman.androidautoidrive.R
import me.hufman.androidautoidrive.connections.CarConnectionDebugging

class ConnectionViewModel(val carConnectionDebugging: CarConnectionDebugging, val carInformation: CarInformation): ViewModel() {
	class Factory(val appContext: Context): ViewModelProvider.Factory {
		override fun <T : ViewModel?> create(modelClass: Class<T>): T {
			var viewModel: ConnectionViewModel? = null
			viewModel = ConnectionViewModel(CarConnectionDebugging(appContext) {
				println("Received update from CarConnectionDebugging")
				viewModel?.update()
			}, CarInformationObserver {viewModel?.update()})
			return viewModel as T
		}
	}

	private val _brand = MutableLiveData<String?>(null)
	val brand: LiveData<String?> = _brand

	private val _chassisCode = MutableLiveData<ChassisCode?>(null)
	val chassisCode: LiveData<ChassisCode?> = _chassisCode

	private val _connectedStatus = MutableLiveData<Context.() -> String> {getString(R.string.connectionStatusWaiting)}
	val connectedStatus: LiveData<Context.() -> String> = _connectedStatus

	private val _connectedStatusColor = MutableLiveData<Context.() -> Int> {getColor(R.color.connectionWaiting)}
	val connectedStatusColor: LiveData<Context.() -> Int> = _connectedStatusColor

	private fun update() {
		val brand = carConnectionDebugging.idriveListener.brand?.toUpperCase()
		val chassisCode = ChassisCode.fromCode(carInformation.capabilities["vehicle.type"] ?: "Unknown")
		_brand.postValue(brand)
		_chassisCode.postValue(chassisCode)
		if (!carConnectionDebugging.isConnectedInstalled) {
			_connectedStatus.postValue { getString(R.string.connectionStatusMissingConnectedApp) }
			_connectedStatusColor.postValue { getColor(R.color.connectionError) }
		} else if (carConnectionDebugging.idriveListener.isConnected && chassisCode != null) {
			_connectedStatus.postValue { getString(R.string.connectionStatusConnected, chassisCode) }
			_connectedStatusColor.postValue { getColor(R.color.connectionConnected) }
		} else if (carConnectionDebugging.idriveListener.isConnected && brand != null) {
			_connectedStatus.postValue { getString(R.string.connectionStatusConnected, brand) }
			_connectedStatusColor.postValue { getColor(R.color.connectionConnected) }
		} else {
			_connectedStatus.postValue { getString(R.string.connectionStatusWaiting) }
			_connectedStatusColor.postValue { getColor(R.color.connectionWaiting) }
		}
	}
}
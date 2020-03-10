/*
 * Created by Andrii Kovalchuk
 * Copyright (c) 2020. All rights reserved.
 * Last modified 10.03.20 19:41
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.mmdev.roove.ui.common.base

import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.mmdev.roove.core.injector
import com.mmdev.roove.ui.SharedViewModel
import com.mmdev.roove.utils.showErrorDialog
import java.lang.reflect.ParameterizedType

/**
 * This is the documentation block about the class
 */

abstract class BaseFragment<T: ViewModel> (private val isViewModelActivityHosted: Boolean = false,
                                           layoutId: Int = 0) :
		Fragment(layoutId) {

	val factory = injector.factory()

	internal val associatedViewModel: T by lazy {
		if (isViewModelActivityHosted) {
			activity?.run {
				ViewModelProvider(this, factory).get(getTClass())
			} ?: throw Exception("Invalid Activity")
		}
		else ViewModelProvider(this, factory).get(getTClass())
	}

	internal val sharedViewModel: SharedViewModel by lazy {
		activity?.run {
			ViewModelProvider(this, factory)[SharedViewModel::class.java]
		} ?: throw Exception("Invalid Activity")
	}

	private lateinit var callback: OnBackPressedCallback

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		setBackButtonDispatcher()

		(associatedViewModel as BaseViewModel).showErrorDialog(this, context)
	}

	//get actual class from parameterized <T>
	//CAUTION: REFLECTION USED
	private fun getTClass(): Class<T> =
		(javaClass.genericSuperclass as ParameterizedType).actualTypeArguments[0] as Class<T>


	/**
	 * Adding BackButtonDispatcher callback to activity
	 */
	private fun setBackButtonDispatcher() {
		callback = object : OnBackPressedCallback(true) {
			override fun handleOnBackPressed() {
				onBackPressed()
			}
		}
		requireActivity().onBackPressedDispatcher.addCallback(this, callback)
	}

	/**
	 * Override this method into your fragment to handleBackButton
	 */
	open fun onBackPressed() {}



}
/*
 * Copyright 2018 Google LLC. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.subscriptions.ui

import androidx.databinding.BindingAdapter
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ProgressBar
import com.bumptech.glide.Glide
import com.example.subscriptions.Constants
import com.example.subscriptions.R
import com.example.subscriptions.billing.isAccountHold
import com.example.subscriptions.billing.isBasicContent
import com.example.subscriptions.billing.isGracePeriod
import com.example.subscriptions.billing.isPaused
import com.example.subscriptions.billing.isPremiumContent
import com.example.subscriptions.billing.isSubscriptionRestore
import com.example.subscriptions.billing.isTransferRequired
import com.example.subscriptions.data.ContentResource
import com.example.subscriptions.data.SubscriptionStatus
import com.example.subscriptions.databinding.FragmentHomeBinding
import com.example.subscriptions.databinding.FragmentPremiumBinding
import com.example.subscriptions.databinding.FragmentSettingsBinding
import com.example.subscriptions.utils.basicTextForSubscription
import com.example.subscriptions.utils.premiumTextForSubscription
//import kotlinx.android.synthetic.main.fragment_home.view.home_account_hold_message
//import kotlinx.android.synthetic.main.fragment_home.view.home_account_paused_message
//import kotlinx.android.synthetic.main.fragment_home.view.home_account_paused_message_text
//import kotlinx.android.synthetic.main.fragment_home.view.home_basic_image
//import kotlinx.android.synthetic.main.fragment_home.view.home_basic_message
//import kotlinx.android.synthetic.main.fragment_home.view.home_basic_text
//import kotlinx.android.synthetic.main.fragment_home.view.home_grace_period_message
//import kotlinx.android.synthetic.main.fragment_home.view.home_paywall_message
//import kotlinx.android.synthetic.main.fragment_home.view.home_restore_message
//import kotlinx.android.synthetic.main.fragment_home.view.home_transfer_message
//import kotlinx.android.synthetic.main.fragment_premium.view.premium_account_hold_message
//import kotlinx.android.synthetic.main.fragment_premium.view.premium_account_paused_message
//import kotlinx.android.synthetic.main.fragment_premium.view.premium_account_paused_message_text
//import kotlinx.android.synthetic.main.fragment_premium.view.premium_grace_period_message
//import kotlinx.android.synthetic.main.fragment_premium.view.premium_paywall_message
//import kotlinx.android.synthetic.main.fragment_premium.view.premium_premium_content
//import kotlinx.android.synthetic.main.fragment_premium.view.premium_premium_image
//import kotlinx.android.synthetic.main.fragment_premium.view.premium_premium_text
//import kotlinx.android.synthetic.main.fragment_premium.view.premium_restore_message
//import kotlinx.android.synthetic.main.fragment_premium.view.premium_transfer_message
//import kotlinx.android.synthetic.main.fragment_premium.view.premium_upgrade_message
//import kotlinx.android.synthetic.main.fragment_settings.view.settings_transfer_message
//import kotlinx.android.synthetic.main.fragment_settings.view.settings_transfer_message_text
//import kotlinx.android.synthetic.main.fragment_settings.view.subscription_option_basic_button
//import kotlinx.android.synthetic.main.fragment_settings.view.subscription_option_premium_button
import java.text.SimpleDateFormat
import java.util.Calendar

private const val TAG = "BindingAdapter"

/**
 * Update a loading progress bar when the status changes.
 *
 * When the network state changes, the binding adapter triggers this view in the layout XML.
 * See the layout XML files for the app:loadingProgressBar attribute.
 */
@BindingAdapter("loadingProgressBar")
fun loadingProgressBar(view: ProgressBar, loading: Boolean?) {
    view.visibility = if (loading == true) View.VISIBLE else View.GONE
}

/**
 * Update basic content when the URL changes.
 *
 * When the image URL content changes, the binding adapter triggers this view in the layout XML.
 * See the layout XML files for the app:updateBasicContent attribute.
 */
@BindingAdapter("updateBasicContent")
fun updateBasicContent(view1: View, basicContent: ContentResource?) {
    val view:FragmentHomeBinding = FragmentHomeBinding.inflate(LayoutInflater.from(view1.context))//,view1.,true
    val image = view.homeBasicImage
    val textView = view.homeBasicText
    val url = basicContent?.url
    if (url != null) {
        image.run {
            Log.d(TAG, "Loading image for basic content: $url")
            visibility = View.VISIBLE
            Glide.with(view.root.context)
                .load(url)
                .into(this)
        }
        textView.run {
            text = view.root.context.resources.getString(R.string.basic_content_text)
        }
    } else {
        image.run {
            visibility = View.GONE
        }
        textView.run {
            text = view.root.context.resources.getString(R.string.no_basic_content)
        }
    }
}

/**
 * Update premium content on the Premium fragment when the URL changes.
 *
 * When the image URL content changes, the binding adapter triggers this view in the layout XML.
 * See the layout XML files for the app:updatePremiumContent attribute.
 */
@BindingAdapter("updatePremiumContent")
fun updatePremiumContent(view1:View, premiumContent: ContentResource?) {
    val view:FragmentPremiumBinding = FragmentPremiumBinding.inflate(LayoutInflater.from(view1.context))//,view1.,true
    val image = view.premiumPremiumImage
    val textView = view.premiumPremiumText
    val url = premiumContent?.url
    if (url != null) {
        image.run {
            Log.d(TAG, "Loading image for premium content: $url")
            visibility = View.VISIBLE
            Glide.with(context)
                .load(url)
                .into(this)
        }
        textView.run {
            text = view.root.context.resources.getString(R.string.premium_content_text)
        }
    } else {
        image.run {
            visibility = View.GONE
        }
        textView.run {
            text = resources.getString(R.string.no_premium_content)
        }
    }
}

/**
 * Update subscription views on the Home fragment when the subscription changes.
 *
 * When the subscription changes, the binding adapter triggers this view in the layout XML.
 * See the layout XML files for the app:updateHomeViews attribute.
 */
@BindingAdapter("updateHomeViews")
fun updateHomeViews(view1: View, subscriptions: List<SubscriptionStatus>?) {
    val view: FragmentHomeBinding = FragmentHomeBinding.inflate(LayoutInflater.from(view1.context))//,view1.,true

    // Set visibility assuming no subscription is available.
    // If a subscription is found that meets certain criteria, then the visibility of the paywall
    // will be changed to View.GONE.
    view.homePaywallMessage.visibility = View.VISIBLE
    // The remaining views start hidden. If a subscription is found that meets each criteria,
    // then the visibility will be changed to View.VISIBLE.
    view.homeRestoreMessage.visibility = View.GONE
    view.homeGracePeriodMessage.visibility = View.GONE
    view.homeTransferMessage.visibility = View.GONE
    view.homeAccountHoldMessage.visibility = View.GONE
    view.homeAccountPausedMessage.visibility = View.GONE
    view.homeBasicMessage.visibility = View.GONE
    // Update based on subscription information.
    subscriptions?.let {
        for (subscription in subscriptions) {
            if (isSubscriptionRestore(subscription)) {
                Log.d(TAG, "restore VISIBLE")
                view.homeRestoreMessage.run {
                    visibility = View.VISIBLE
                    val expiryDate = getHumanReadableDate(subscription.activeUntilMillisec)
                    text = view.root.context.resources.getString(
                        R.string.restore_message_with_date,
                        expiryDate
                    )
                }
                view.homePaywallMessage.visibility = View.GONE // Paywall gone.
            }
            if (isGracePeriod(subscription)) {
                Log.d(TAG, "grace period VISIBLE")
                view.homeGracePeriodMessage.visibility = View.VISIBLE
                view.homePaywallMessage.visibility = View.GONE // Paywall gone.
            }
            if (isTransferRequired(subscription) && subscription.sku == Constants.BASIC_SKU) {
                Log.d(TAG, "transfer VISIBLE")
                view.homeTransferMessage.visibility = View.VISIBLE
                view.homePaywallMessage.visibility = View.GONE // Paywall gone.
            }
            if (isAccountHold(subscription)) {
                Log.d(TAG, "account hold VISIBLE")
                view.homeAccountHoldMessage.visibility = View.VISIBLE
                view.homePaywallMessage.visibility = View.GONE // Paywall gone.
            }
            if (isPaused(subscription)) {
                Log.d(TAG, "account paused VISIBLE")
                view.homeAccountPausedMessageText.run {
                    val autoResumeDate = getHumanReadableDate(subscription.autoResumeTimeMillis)
                    text = view.root.context.resources.getString(
                        R.string.account_paused_message,
                        autoResumeDate
                    )
                }
                view.homeAccountPausedMessage.visibility = View.VISIBLE
                view.homePaywallMessage.visibility = View.GONE // Paywall gone.
            }
            if (isBasicContent(subscription) || isPremiumContent(subscription)) {
                Log.d(TAG, "basic VISIBLE")
                view.homeBasicMessage.visibility = View.VISIBLE
                view.homePaywallMessage.visibility = View.GONE // Paywall gone.
            }
        }
    }
}

/**
 * Update subscription views on the Premium fragment when the subscription changes.
 *
 * When the subscription changes, the binding adapter triggers this view in the layout XML.
 * See the layout XML files for the app:updatePremiumViews attribute.
 */
@BindingAdapter("updatePremiumViews")
fun updatePremiumViews(view1: View, subscriptions: List<SubscriptionStatus>?) {
    val view: FragmentPremiumBinding = FragmentPremiumBinding.inflate(LayoutInflater.from(view1.context))//,view1.,true
    // Set visibility assuming no subscription is available.
    // If a subscription is found that meets certain criteria, then the visibility of the paywall
    // will be changed to View.GONE.
    view.premiumPaywallMessage.visibility = View.VISIBLE
    // The remaining views start hidden. If a subscription is found that meets each criteria,
    // then the visibility will be changed to View.VISIBLE.
    view.premiumRestoreMessage.visibility = View.GONE
    view.premiumGracePeriodMessage.visibility = View.GONE
    view.premiumTransferMessage.visibility = View.GONE
    view.premiumAccountHoldMessage.visibility = View.GONE
    view.premiumAccountPausedMessage.visibility = View.GONE
    view.premiumPremiumContent.visibility = View.GONE
    view.premiumUpgradeMessage.visibility = View.GONE

    // The Upgrade button should appear if the user has a basic subscription, but does not
    // have a premium subscription. This variable keeps track of whether a premium subscription
    // has been found when looking throug the list of subscriptions.
    var hasPremium = false
    // Update based on subscription information.
    subscriptions?.let {
        for (subscription in subscriptions) {
            if (isSubscriptionRestore(subscription)) {
                Log.d(TAG, "restore VISIBLE")
                view.premiumRestoreMessage.run {
                    visibility = View.VISIBLE
                    val expiryDate = getHumanReadableDate(subscription.activeUntilMillisec)
                    text = view.root.context.resources.getString(
                        R.string.restore_message_with_date,
                        expiryDate
                    )
                }
                view.premiumPaywallMessage.visibility = View.GONE // Paywall gone.
            }
            if (isGracePeriod(subscription)) {
                Log.d(TAG, "grace period VISIBLE")
                view.premiumGracePeriodMessage.visibility = View.VISIBLE
                view.premiumPaywallMessage.visibility = View.GONE // Paywall gone.
            }
            if (isTransferRequired(subscription) && subscription.sku == Constants.PREMIUM_SKU) {
                Log.d(TAG, "transfer VISIBLE")
                view.premiumTransferMessage.visibility = View.VISIBLE
                view.premiumPaywallMessage.visibility = View.GONE // Paywall gone.
            }
            if (isAccountHold(subscription)) {
                Log.d(TAG, "account hold VISIBLE")
                view.premiumAccountHoldMessage.visibility = View.VISIBLE
                view.premiumPaywallMessage.visibility = View.GONE // Paywall gone.
            }
            if (isPaused(subscription)) {
                Log.d(TAG, "account paused VISIBLE")
                view.premiumAccountPausedMessageText.run {
                    val autoResumeDate = getHumanReadableDate(subscription.autoResumeTimeMillis)
                    text = view.root.context.resources.getString(
                        R.string.account_paused_message,
                        autoResumeDate
                    )
                }
                view.premiumAccountPausedMessage.visibility = View.VISIBLE
                view.premiumPaywallMessage.visibility = View.GONE // Paywall gone.
            }

            // The upgrade message must be shown if there is a basic subscription
            // and there are zero premium subscriptions. We need to keep track of the premium
            // subscriptions and hide the upgrade message if we find any.
            if (isPremiumContent(subscription)) {
                Log.d(TAG, "premium VISIBLE")
                view.premiumPremiumContent.visibility = View.VISIBLE
                view.premiumPaywallMessage.visibility = View.GONE // Paywall gone.
                // Make sure we do not ask for an upgrade when user has premium subscription.
                hasPremium = true
                view.premiumUpgradeMessage.visibility = View.GONE
            }
            if (isBasicContent(subscription) && !isPremiumContent(subscription) && !hasPremium) {
                Log.d(TAG, "basic VISIBLE")
                // Upgrade message will be hidden if a premium subscription is found later.
                view.premiumUpgradeMessage.visibility = View.VISIBLE
                view.premiumPaywallMessage.visibility = View.GONE // Paywall gone.
            }

        }
    }
}

/**
 * Update views on the Settings fragment when the subscription changes.
 *
 * When the subscription changes, the binding adapter triggers this view in the layout XML.
 * See the layout XML files for the app:updateSettingsViews attribute.
 */
@BindingAdapter("updateSettingsViews")
fun updateSettingsViews(view1: View, subscriptions: List<SubscriptionStatus>?) {
    val view: FragmentSettingsBinding = FragmentSettingsBinding.inflate(LayoutInflater.from(view1.context))//,view1.,true
    // Set default button text: it might be overridden based on the subscription state.
    view.subscriptionOptionPremiumButton.text =
        view.root.context.resources.getString(R.string.subscription_option_premium_message)
    view.subscriptionOptionBasicButton.text =
        view.root.context.resources.getString(R.string.subscription_option_basic_message)
    view.settingsTransferMessage.visibility = View.GONE
    // Update based on subscription information.
    var basicRequiresTransfer = false
    var premiumRequiresTransfer = false
    subscriptions?.let {
        for (subscription in it) {
            val sku = subscription.sku
            when (sku) {
                Constants.BASIC_SKU -> {
                    view.subscriptionOptionBasicButton.text =
                        basicTextForSubscription(view.root.context.resources, subscription)
                    if (isTransferRequired(subscription)) {
                        basicRequiresTransfer = true
                    }
                }
                Constants.PREMIUM_SKU -> {
                    view.subscriptionOptionPremiumButton.text =
                        premiumTextForSubscription(view.root.context.resources, subscription)
                    if (isTransferRequired(subscription)) {
                        premiumRequiresTransfer = true
                    }
                }
            }
        }
    }
    val message = when {
        basicRequiresTransfer && premiumRequiresTransfer -> {
            val basicName = view.root.context.resources.getString(R.string.basic_button_text)
            val premiumName = view.root.context.resources.getString(R.string.premium_button_text)
            view.root.context.resources.getString(
                R.string.transfer_message_with_two_skus, basicName, premiumName
            )
        }
        basicRequiresTransfer -> {
            val basicName = view.root.context.resources.getString(R.string.basic_button_text)
            view.root.context.resources.getString(R.string.transfer_message_with_sku, basicName)
        }
        premiumRequiresTransfer -> {
            val premiumName = view.root.context.resources.getString(R.string.premium_button_text)
            view.root.context.resources.getString(R.string.transfer_message_with_sku, premiumName)
        }
        else -> null
    }
    if (message != null) {
        Log.d(TAG, "transfer VISIBLE")
        view.settingsTransferMessage.visibility = View.VISIBLE
        view.settingsTransferMessageText.text = message
    } else {
        view.settingsTransferMessageText.text =
            view.root.context.resources.getString(R.string.transfer_message)
    }
}

/**
 * Get a readable date from the time in milliseconds.
 */
fun getHumanReadableDate(milliSeconds: Long): String {
    val formatter = SimpleDateFormat.getDateInstance()
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = milliSeconds
    if (milliSeconds == 0L) {
        Log.d(TAG, "Suspicious time: 0 milliseconds.")
    } else {
        Log.d(TAG, "Milliseconds: $milliSeconds")
    }
    return formatter.format(calendar.time)
}

package me.thanel.webmark.ui.list

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.longClick
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.matcher.ViewMatchers.isChecked
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.filters.SdkSuppress
import me.thanel.webmark.BuildConfig
import me.thanel.webmark.R
import me.thanel.webmark.preferences.AppTheme
import me.thanel.webmark.preferences.WebMarkPreferences
import me.thanel.webmark.test.base.ui.BaseUserInterfaceTest
import me.thanel.webmark.test.matcher.browserIntent
import me.thanel.webmark.test.matcher.onTooltipView
import me.thanel.webmark.test.matcher.onViewInPopup
import me.thanel.webmark.test.matcher.stubExternalIntents
import me.thanel.webmark.ui.popup.LicensesPopupMenu
import org.hamcrest.CoreMatchers.equalTo
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test

class OptionsMenuTest : BaseUserInterfaceTest() {

    @Before
    fun setupActivity() {
        startActivity()
    }

    @Test
    fun more_options_button_shows_tooltip() {
        onView(withId(R.id.moreOptionsButton)).perform(longClick())

        activityRule.onTooltipView(withText(R.string.action_more_options))
            .check(matches(isDisplayed()))
    }

    @Test
    fun can_select_light_theme_from_options_popup() {
        openThemeOptionsPopup()

        onViewInPopup(withText(R.string.title_theme_light)).perform(click())

        assertThat(WebMarkPreferences.appTheme, equalTo(AppTheme.Light))
        openThemeOptionsPopup()
        onViewInPopup(withText(R.string.title_theme_light)).check(matches(isChecked()))
    }

    @Test
    fun can_select_dark_theme_from_options_popup() {
        openThemeOptionsPopup()

        onViewInPopup(withText(R.string.title_theme_dark)).perform(click())

        assertThat(WebMarkPreferences.appTheme, equalTo(AppTheme.Dark))
        openThemeOptionsPopup()
        onViewInPopup(withText(R.string.title_theme_dark)).check(matches(isChecked()))
    }

    @Test
    @SdkSuppress(minSdkVersion = 28)
    fun can_select_system_theme_from_options_popup() {
        openThemeOptionsPopup()

        onViewInPopup(withText(R.string.title_theme_follow_system)).perform(click())

        assertThat(WebMarkPreferences.appTheme, equalTo(AppTheme.FollowSystem))
        openThemeOptionsPopup()
        onViewInPopup(withText(R.string.title_theme_follow_system)).check(matches(isChecked()))
    }

    @Test
    @SdkSuppress(maxSdkVersion = 27)
    fun can_select_battery_saver_based_theme_from_options_popup() {
        openThemeOptionsPopup()

        onViewInPopup(withText(R.string.title_theme_battery_saver)).perform(click())

        assertThat(WebMarkPreferences.appTheme, equalTo(AppTheme.BatterySaver))
        openThemeOptionsPopup()
        onViewInPopup(withText(R.string.title_theme_battery_saver)).check(matches(isChecked()))
    }

    private fun openThemeOptionsPopup() {
        onView(withId(R.id.moreOptionsButton)).perform(click())
        onViewInPopup(withText(R.string.action_select_theme)).perform(click())
    }

    @Test
    fun can_view_app_information_in_options_popup() {
        onView(withId(R.id.moreOptionsButton)).perform(click())
        onViewInPopup(withText(R.string.title_about)).perform(click())

        val nameVersionText = appContext.getString(R.string.app_name) + " v${BuildConfig.VERSION_NAME}"
        onView(withText(nameVersionText)).check(matches(isDisplayed()))
        onView(withText(R.string.info_app_description)).check(matches(isDisplayed()))
        onView(withId(R.id.appIconView)).check(matches(isDisplayed()))
        onView(withText(R.string.info_made_by)).check(matches(isDisplayed()))
    }

    @Test
    fun can_open_github_repository_from_options_popup() {
        stubExternalIntents()

        onView(withId(R.id.moreOptionsButton)).perform(click())
        onViewInPopup(withText(R.string.title_about)).perform(click())
        onViewInPopup(withText(R.string.action_view_github)).perform(click())

        intended(browserIntent("https://github.com/Tunous/WebMark"))
    }

    @Test
    fun clicking_on_description_in_popup_keeps_popup_open() {
        onView(withId(R.id.moreOptionsButton)).perform(click())
        onViewInPopup(withText(R.string.title_about)).perform(click())

        onView(withText(R.string.info_app_description)).perform(click())

        onView(withText(R.string.info_app_description)).check(matches(isDisplayed()))
    }

    @Test
    fun can_view_open_source_licenses_from_options_popup() {
        stubExternalIntents()

        for ((name, link) in LicensesPopupMenu.licenses) {
            onView(withId(R.id.moreOptionsButton)).perform(click())
            onViewInPopup(withText(R.string.title_about)).perform(click())
            onViewInPopup(withText(R.string.action_licenses)).perform(click())
            onViewInPopup(withText(name)).perform(click())

            intended(browserIntent(link))
        }
    }
}

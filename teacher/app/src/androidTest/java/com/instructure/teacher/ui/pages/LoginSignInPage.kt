package com.instructure.teacher.ui.pages

import android.support.test.espresso.web.sugar.Web
import android.support.test.espresso.web.sugar.Web.onWebView
import android.support.test.espresso.web.webdriver.DriverAtoms.*
import android.support.test.espresso.web.webdriver.Locator
import com.instructure.espresso.OnViewWithId
import com.instructure.espresso.assertDisplayed
import com.instructure.espresso.page.BasePage
import com.instructure.soseedy.CanvasUser
import com.instructure.teacher.R
import com.instructure.teacher.ui.utils.repeatedlyUntil
import com.instructure.teacher.ui.utils.repeatedlyUntilNot

@Suppress("unused")
class LoginSignInPage : BasePage() {

    private val EMAIL_FIELD_CSS = "input[name=\"pseudonym_session[unique_id]\"]"
    private val PASSWORD_FIELD_CSS = "input[name=\"pseudonym_session[password]\"]"
    private val LOGIN_BUTTON_CSS = "button[type=\"submit\"]"
    private val FORGOT_PASSWORD_BUTTON_CSS = "a[class=\"forgot-password flip-to-back\"]"
    private val AUTHORIZE_BUTTON_CSS = "button[type=\"submit\"]"

    private val signInRoot by OnViewWithId(R.id.signInRoot, autoAssert = false)
    private val toolbar by OnViewWithId(R.id.toolbar, autoAssert = false)

    //region UI Element Locator Methods

    private fun emailField(): Web.WebInteraction<*> {
        return onWebView().withElement(findElement(Locator.CSS_SELECTOR, EMAIL_FIELD_CSS))
    }

    private fun passwordField(): Web.WebInteraction<*> {
        return onWebView().withElement(findElement(Locator.CSS_SELECTOR, PASSWORD_FIELD_CSS))
    }

    private fun loginButton(): Web.WebInteraction<*> {
        return onWebView().withElement(findElement(Locator.CSS_SELECTOR, LOGIN_BUTTON_CSS))
    }

    private fun forgotPasswordButton(): Web.WebInteraction<*> {
        return onWebView().withElement(findElement(Locator.CSS_SELECTOR, FORGOT_PASSWORD_BUTTON_CSS))
    }

    // https://github.com/instructure/android-uno/blob/master/candroid/candroid/src/androidTest/java/com/instructure/candroid/test/page/LoginPage.java#L103
    private fun authorizeButton(): Web.WebInteraction<*> {
        return onWebView().withElement(findElement(Locator.CSS_SELECTOR, AUTHORIZE_BUTTON_CSS))
    }

    //endregion

    //region Assertion Helpers

    override fun assertPageObjects() {
        signInRoot.assertDisplayed()
        toolbar.assertDisplayed()

        emailField()
        passwordField()
        loginButton()
        forgotPasswordButton()
    }

    //endregion

    //region UI Action Helpers

    fun enterEmail(email: String) {
        emailField().perform(webKeys(email))
    }

    fun enterPassword(password: String) {
        passwordField().perform(webKeys(password))
    }

    fun clickLoginButton() {
        loginButton().repeatedlyUntilNot(action = webClick(),
                desiredStateMatcher = ::emailField,
                maxAttempts = 20)
    }

    fun clickForgotPasswordButton() {
        forgotPasswordButton().perform(webClick())
    }

    fun loginAs(teacher: CanvasUser) {
        loginAs(teacher.loginId, teacher.password)
    }

    fun loginAs(loginId: String, password: String) {
        enterEmail(loginId)
        enterPassword(password)
        clickLoginButton()
        authorizeButton().repeatedlyUntilNot(action = webClick(),
                desiredStateMatcher = ::authorizeButton,
                maxAttempts = 20)
    }

    //endregion
}

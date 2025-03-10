/*
    Appointment Now - Instrumented Test
    Author: Conor Steward
    Contact: 1conorsteward@gmail.com
    Date Created: 10/8/24
    Last Updated: 03/07/25
    Version: 2.1

    Description:
    This class contains an instrumented test that runs on an Android device or emulator. It validates
    that the application context is correctly retrieved and its package name matches the expected value.

    Key Features:
    - Runs instrumented tests in the Android environment.
    - Ensures correct retrieval of the application context.
    - Verifies the application's package name against an expected value.

    Dependencies:
    - AndroidX Test Library (InstrumentationRegistry, AndroidJUnit4)
    - JUnit (for assertions and test execution)

    Issues:
    - No known issues.
*/

package com.example.appointmentnow_steward;

import android.content.Context;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

/**
 * Runs instrumented tests on an Android device or emulator.
 * Uses the AndroidJUnit4 runner for compatibility with AndroidX testing.
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {

    // Define the expected package name as a constant for maintainability.
    private static final String EXPECTED_PACKAGE_NAME = "com.example.appointmentnow_steward";

    /**
     * This test verifies that the application context retrieved during the test matches the expected package name.
     * If the assertion fails, it indicates a misconfiguration in the app's build settings.
     */
    @Test
    public void useAppContext() {
        // Step 1: Retrieve the app context using InstrumentationRegistry.
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();

        // Step 2: Validate that the package name is correct.
        assertEquals("The package name should match the expected value.", EXPECTED_PACKAGE_NAME, appContext.getPackageName());
    }
}
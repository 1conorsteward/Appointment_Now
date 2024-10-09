/*
    Author: Conor Steward
    Contact: 1conorsteward@gmail.com
    Date Created: 10/8/24
    Version: 2.1

    ExampleInstrumentedTest.java

    This class contains an instrumented test that will execute on an Android device or emulator. Instrumented tests
    allow testing within the Android environment, meaning they interact with the Android OS and components, like
    Context and Activities. This specific test ensures that the application context under test is correctly retrieved
    and matches the expected package name.

    Key Features:
    - Runs instrumented tests on Android devices or emulators.
    - Verifies the application context is set up correctly.
    - Ensures the package name of the application is as expected.

    Issues: No known issues
*/

package com.example.appointmentnow_steward;

import android.content.Context;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {

    /**
     * This test method verifies that the application context retrieved during the test run
     * matches the expected package name ("com.example.appointmentnow_steward"). The test
     * asserts that the package name of the app context equals the expected package name.
     */
    @Test
    public void useAppContext() {
        // Retrieve the context of the app under test using InstrumentationRegistry.
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();

        // Assert that the package name of the application context matches the expected package name.
        assertEquals("com.example.appointmentnow_steward", appContext.getPackageName());
    }
}

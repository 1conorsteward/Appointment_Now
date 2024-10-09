/*
    Author: Conor Steward
    Contact: 1conorsteward@gmail.com
    Date Created: 10/8/24
    Version: 2.1

    ExampleUnitTest.java

    This class contains an example local unit test that will execute on the development machine (host).
    Unit tests are designed to run quickly without the need for an Android environment, allowing them
    to verify small units of functionality, such as methods or calculations, independently of the Android OS.

    Key Features:
    - Runs unit tests locally on the developer's machine (host).
    - Provides an example of a basic mathematical operation (addition) test.
    - Ensures that unit testing can be done efficiently without involving the Android environment.

    Issues: No known issues
*/

package com.example.appointmentnow_steward;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * This class provides an example of a local unit test. Local unit tests run on the development
 * machine without needing to interact with the Android framework. This test checks a basic
 * addition operation to verify that unit tests are functioning correctly.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {

    /**
     * This test method checks if the addition of two numbers (2 + 2) is correct.
     * It uses the `assertEquals` method from JUnit to compare the expected result (4)
     * with the actual result of the addition.
     */
    @Test
    public void addition_isCorrect() {
        // Perform the addition operation and verify if the result equals 4
        assertEquals(4, 2 + 2);
    }
}


package com.example.demo;

import java.time.Duration;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class LoginPageSeleniumTest {

    private WebDriver driver;
    private WebDriverWait wait;

    private static final String BASE_URL = "http://localhost:8080";
    private static final String FOOD_NAME = "Hyderabadi Chicken Biryani";

    // Runs before every test
    @BeforeEach
    public void setUp() {
        driver = new ChromeDriver();
        wait = new WebDriverWait(driver, Duration.ofSeconds(15));
    }

    // Common user login method
    private void loginAsUser() {

        driver.get(BASE_URL + "/userLogin");

        driver.findElement(By.name("userEmail"))
                .sendKeys(requiredEnvironmentVariable("FOODFRENZY_TEST_USER_EMAIL"));

        driver.findElement(By.name("userPassword"))
                .sendKeys(requiredEnvironmentVariable("FOODFRENZY_TEST_USER_PASSWORD"));

        driver.findElement(By.cssSelector("button[type='submit']"))
                .click();

        wait.until(d ->
                d.getCurrentUrl().contains("/dashboard")
        );
    }

    private String requiredEnvironmentVariable(String name) {
        String value = System.getenv(name);
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalStateException("Set " + name + " to run the authenticated Selenium tests.");
        }
        return value;
    }

    // Common food search method
    private void searchFood(String foodName) {

        driver.findElement(By.name("productName"))
                .clear();

        driver.findElement(By.name("productName"))
                .sendKeys(foodName);

        driver.findElement(
                By.xpath("//button[normalize-space()='Search Menu']")
        ).click();
    }

    // TEST 1: User dashboard loads successfully
    @Test
    public void testUserDashboardLoads() {

        loginAsUser();

        String pageText = driver.findElement(By.tagName("body")).getText();

        assertTrue(
                pageText.contains("Welcome Back"),
                "User dashboard did not load"
        );

        System.out.println("TEST 1 PASSED: User dashboard loaded");
    }

    // TEST 2: Invalid user login
    @Test
    public void testInvalidUserLogin() {

        driver.get(BASE_URL + "/userLogin");

        driver.findElement(By.name("userEmail"))
                .sendKeys("wrong@gmail.com");

        driver.findElement(By.name("userPassword"))
                .sendKeys("wrongpassword");

        driver.findElement(By.cssSelector("button[type='submit']"))
                .click();

        boolean errorDisplayed = wait.until(d ->
                d.findElement(By.tagName("body"))
                        .getText()
                        .contains("Invalid email or password")
        );

        assertTrue(
                errorDisplayed,
                "Invalid login error message should be displayed"
        );

        System.out.println("TEST 2 PASSED: Invalid login handled");
    }

    // TEST 3: User login page loads
    @Test
    public void testUserLoginPageLoads() {

        driver.get(BASE_URL + "/userLogin");

        assertTrue(
                driver.findElement(By.name("userEmail")).isDisplayed(),
                "Email field is missing"
        );

        assertTrue(
                driver.findElement(By.name("userPassword")).isDisplayed(),
                "Password field is missing"
        );

        System.out.println("TEST 3 PASSED: User login page loaded");
    }

    // TEST 4: Registration page loads
    @Test
    public void testRegistrationPageLoads() {

        driver.get(BASE_URL + "/register");

        assertTrue(
                driver.getCurrentUrl().contains("/register"),
                "Registration page did not load"
        );

        assertTrue(
                driver.findElement(By.tagName("body"))
                        .getText()
                        .toLowerCase()
                        .contains("register"),
                "Registration page content not found"
        );

        System.out.println("TEST 4 PASSED: Registration page loaded");
    }

    // TEST 5: Admin login page loads
    @Test
    public void testAdminLoginPageLoads() {

        driver.get(BASE_URL + "/login");

        assertTrue(
                driver.getCurrentUrl().contains("/login"),
                "Admin login page did not load"
        );

        assertTrue(
                driver.findElement(By.tagName("body"))
                        .getText()
                        .contains("Admin Login"),
                "Admin login heading not found"
        );

        System.out.println("TEST 5 PASSED: Admin login page loaded");
    }

    // TEST 6: Dashboard search section is visible
    @Test
    public void testDashboardSearchSectionVisible() {

        loginAsUser();

        assertTrue(
                driver.findElement(By.name("productName")).isDisplayed(),
                "Food search box should be visible"
        );

        assertTrue(
                driver.findElement(
                        By.xpath("//button[normalize-space()='Search Menu']")
                ).isDisplayed(),
                "Search Menu button should be visible"
        );

        System.out.println("TEST 6 PASSED: Search section is visible");
    }

    // TEST 7: Search for available food
    @Test
    public void testSearchFoodByName() {

        loginAsUser();

        searchFood(FOOD_NAME);

        boolean productFound = wait.until(d ->
                d.findElement(By.tagName("body"))
                        .getText()
                        .contains(FOOD_NAME)
        );

        assertTrue(
                productFound,
                "Searched product should appear on the result page"
        );

        System.out.println("TEST 7 PASSED: Food search successful");
    }

    // TEST 8: Search for unavailable food
    @Test
    public void testSearchUnavailableFood() {

        loginAsUser();

        String unavailableFood = "NonExistingFood123";

        searchFood(unavailableFood);

        boolean unavailableMessage = wait.until(d -> {
            String text = d.findElement(By.tagName("body")).getText();

            return text.contains(unavailableFood)
                    && text.contains("Unavailable");
        });

        assertTrue(
                unavailableMessage,
                "Unavailable food message should be displayed"
        );

        System.out.println("TEST 8 PASSED: Unavailable food handled");
    }

    // TEST 9: Successful order placement
    @Test
    public void testSuccessfulOrderPlacement() {

        loginAsUser();

        // Search for food
        searchFood(FOOD_NAME);

        // Wait for product quantity field
        wait.until(d ->
                d.findElement(By.name("oQuantity")).isDisplayed()
        );

        // Enter quantity
        driver.findElement(By.name("oQuantity")).clear();

        driver.findElement(By.name("oQuantity"))
                .sendKeys("2");

        // Confirm order
        driver.findElement(
                By.xpath("//button[contains(normalize-space(),'Confirm Order')]")
        ).click();

        // Verify actual success message from Order_success.html
        boolean orderSuccessful = wait.until(d ->
                d.findElement(By.tagName("body"))
                        .getText()
                        .contains("Ordered SuccessFully")
        );

        assertTrue(
                orderSuccessful,
                "Order success message was not displayed"
        );

        System.out.println("TEST 9 PASSED: Order placed successfully");
    }

    // Runs after every test
    @AfterEach
    public void closeBrowser() {

        if (driver != null) {
            driver.quit();
        }
    }
}

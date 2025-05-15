package com.web3platform.wallet_service.ui;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import static org.junit.jupiter.api.Assertions.*;

class AaveInterfaceTest extends BaseUITest {

    private static final By LEND_TAB = By.id("lend-tab");
    private static final By BORROW_TAB = By.id("borrow-tab");
    private static final By ASSET_SELECT = By.id("asset-select");
    private static final By AMOUNT_INPUT = By.id("amount-input");
    private static final By INTEREST_RATE_MODE = By.id("interest-rate-mode");
    private static final By SUBMIT_BUTTON = By.id("submit-button");
    private static final By CONFIRM_BUTTON = By.id("confirm-button");
    private static final By HEALTH_FACTOR = By.id("health-factor");
    private static final By COLLATERAL_RATIO = By.id("collateral-ratio");
    private static final By APY_DISPLAY = By.id("apy-display");

    @Test
    void testLendingFlow() {
        // Navigate to Aave interface
        click(By.id("aave-nav"));
        waitForElement(LEND_TAB);

        // Select asset
        click(ASSET_SELECT);
        click(By.xpath("//div[contains(text(), 'WETH')]"));
        waitForElement(AMOUNT_INPUT);

        // Enter amount
        type(AMOUNT_INPUT, "1.0");
        waitForLoadingSpinner();

        // Verify APY is displayed
        assertTrue(isElementPresent(APY_DISPLAY));

        // Select interest rate mode
        click(INTEREST_RATE_MODE);
        click(By.xpath("//div[contains(text(), 'Stable')]"));

        // Submit transaction
        click(SUBMIT_BUTTON);
        waitForElement(CONFIRM_BUTTON);

        // Confirm transaction
        click(CONFIRM_BUTTON);
        waitForTransactionConfirmation();

        // Verify success message
        assertTrue(isElementPresent(By.className("success-message")));
    }

    @Test
    void testBorrowingFlow() {
        // Navigate to Aave interface
        click(By.id("aave-nav"));
        click(BORROW_TAB);
        waitForElement(ASSET_SELECT);

        // Select asset
        click(ASSET_SELECT);
        click(By.xpath("//div[contains(text(), 'USDC')]"));
        waitForElement(AMOUNT_INPUT);

        // Enter amount
        type(AMOUNT_INPUT, "1000.0");
        waitForLoadingSpinner();

        // Verify health factor and collateral ratio
        assertTrue(isElementPresent(HEALTH_FACTOR));
        assertTrue(isElementPresent(COLLATERAL_RATIO));

        // Select interest rate mode
        click(INTEREST_RATE_MODE);
        click(By.xpath("//div[contains(text(), 'Variable')]"));

        // Submit transaction
        click(SUBMIT_BUTTON);
        waitForElement(CONFIRM_BUTTON);

        // Confirm transaction
        click(CONFIRM_BUTTON);
        waitForTransactionConfirmation();

        // Verify success message
        assertTrue(isElementPresent(By.className("success-message")));
    }

    @Test
    void testHealthFactorWarning() {
        // Navigate to Aave interface
        click(By.id("aave-nav"));
        click(BORROW_TAB);
        waitForElement(ASSET_SELECT);

        // Select asset
        click(ASSET_SELECT);
        click(By.xpath("//div[contains(text(), 'USDC')]"));

        // Enter large amount to trigger health factor warning
        type(AMOUNT_INPUT, "100000.0");
        waitForLoadingSpinner();

        // Verify warning message
        assertTrue(isElementPresent(By.className("warning-message")));
        assertEquals("Low health factor warning", getText(By.className("warning-message")));
    }

    @Test
    void testRepaymentFlow() {
        // Navigate to Aave interface
        click(By.id("aave-nav"));
        click(By.id("repay-tab"));
        waitForElement(ASSET_SELECT);

        // Select asset
        click(ASSET_SELECT);
        click(By.xpath("//div[contains(text(), 'USDC')]"));
        waitForElement(AMOUNT_INPUT);

        // Enter repayment amount
        type(AMOUNT_INPUT, "500.0");
        waitForLoadingSpinner();

        // Submit transaction
        click(SUBMIT_BUTTON);
        waitForElement(CONFIRM_BUTTON);

        // Confirm transaction
        click(CONFIRM_BUTTON);
        waitForTransactionConfirmation();

        // Verify success message
        assertTrue(isElementPresent(By.className("success-message")));
    }
}
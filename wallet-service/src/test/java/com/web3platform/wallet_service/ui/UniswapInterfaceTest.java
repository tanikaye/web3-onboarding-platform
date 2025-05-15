package com.web3platform.wallet_service.ui;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import static org.junit.jupiter.api.Assertions.*;

class UniswapInterfaceTest extends BaseUITest {

    private static final By SWAP_TAB = By.id("swap-tab");
    private static final By TOKEN_IN_SELECT = By.id("token-in-select");
    private static final By TOKEN_OUT_SELECT = By.id("token-out-select");
    private static final By AMOUNT_INPUT = By.id("amount-input");
    private static final By SLIPPAGE_INPUT = By.id("slippage-input");
    private static final By SWAP_BUTTON = By.id("swap-button");
    private static final By CONFIRM_BUTTON = By.id("confirm-button");
    private static final By ESTIMATED_OUTPUT = By.id("estimated-output");
    private static final By PRICE_IMPACT = By.id("price-impact");
    private static final By GAS_ESTIMATE = By.id("gas-estimate");

    @Test
    void testSwapTokenFlow() {
        // Navigate to Uniswap interface
        click(By.id("uniswap-nav"));
        waitForElement(SWAP_TAB);

        // Select input token
        click(TOKEN_IN_SELECT);
        click(By.xpath("//div[contains(text(), 'WETH')]"));
        waitForElement(AMOUNT_INPUT);

        // Select output token
        click(TOKEN_OUT_SELECT);
        click(By.xpath("//div[contains(text(), 'USDC')]"));
        waitForElement(ESTIMATED_OUTPUT);

        // Enter amount
        type(AMOUNT_INPUT, "1.0");
        waitForLoadingSpinner();

        // Verify price impact and gas estimate are displayed
        assertTrue(isElementPresent(PRICE_IMPACT));
        assertTrue(isElementPresent(GAS_ESTIMATE));

        // Set slippage
        type(SLIPPAGE_INPUT, "0.5");

        // Click swap button
        click(SWAP_BUTTON);
        waitForElement(CONFIRM_BUTTON);

        // Confirm transaction
        click(CONFIRM_BUTTON);
        waitForTransactionConfirmation();

        // Verify success message
        assertTrue(isElementPresent(By.className("success-message")));
    }

    @Test
    void testInsufficientBalance() {
        // Navigate to Uniswap interface
        click(By.id("uniswap-nav"));
        waitForElement(SWAP_TAB);

        // Select tokens
        click(TOKEN_IN_SELECT);
        click(By.xpath("//div[contains(text(), 'WETH')]"));
        click(TOKEN_OUT_SELECT);
        click(By.xpath("//div[contains(text(), 'USDC')]"));

        // Enter amount larger than balance
        type(AMOUNT_INPUT, "1000.0");
        waitForLoadingSpinner();

        // Verify error message
        assertTrue(isElementPresent(By.className("error-message")));
        assertEquals("Insufficient balance", getText(By.className("error-message")));
    }

    @Test
    void testHighSlippageWarning() {
        // Navigate to Uniswap interface
        click(By.id("uniswap-nav"));
        waitForElement(SWAP_TAB);

        // Select tokens
        click(TOKEN_IN_SELECT);
        click(By.xpath("//div[contains(text(), 'WETH')]"));
        click(TOKEN_OUT_SELECT);
        click(By.xpath("//div[contains(text(), 'USDC')]"));

        // Enter amount
        type(AMOUNT_INPUT, "1.0");

        // Set high slippage
        type(SLIPPAGE_INPUT, "15.0");

        // Verify warning message
        assertTrue(isElementPresent(By.className("warning-message")));
        assertEquals("High slippage warning", getText(By.className("warning-message")));
    }

    @Test
    void testTokenSearch() {
        // Navigate to Uniswap interface
        click(By.id("uniswap-nav"));
        waitForElement(SWAP_TAB);

        // Open token select
        click(TOKEN_IN_SELECT);

        // Search for token
        type(By.id("token-search"), "USDC");
        waitForElement(By.xpath("//div[contains(text(), 'USDC')]"));

        // Select token
        click(By.xpath("//div[contains(text(), 'USDC')]"));
        waitForElement(AMOUNT_INPUT);

        // Verify token is selected
        assertEquals("USDC", getText(By.id("selected-token")));
    }
}
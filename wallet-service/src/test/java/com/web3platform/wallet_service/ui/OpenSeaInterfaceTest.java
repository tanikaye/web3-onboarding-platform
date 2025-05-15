package com.web3platform.wallet_service.ui;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import static org.junit.jupiter.api.Assertions.*;

class OpenSeaInterfaceTest extends BaseUITest {

    private static final By LIST_TAB = By.id("list-tab");
    private static final By BUY_TAB = By.id("buy-tab");
    private static final By COLLECTION_SELECT = By.id("collection-select");
    private static final By TOKEN_ID_INPUT = By.id("token-id-input");
    private static final By PRICE_INPUT = By.id("price-input");
    private static final By PAYMENT_TOKEN_SELECT = By.id("payment-token-select");
    private static final By ROYALTY_FEE_DISPLAY = By.id("royalty-fee");
    private static final By SUBMIT_BUTTON = By.id("submit-button");
    private static final By CONFIRM_BUTTON = By.id("confirm-button");

    @Test
    void testNFTListingFlow() {
        // Navigate to OpenSea interface
        click(By.id("opensea-nav"));
        waitForElement(LIST_TAB);

        // Select collection
        click(COLLECTION_SELECT);
        click(By.xpath("//div[contains(text(), 'CryptoPunks')]"));
        waitForElement(TOKEN_ID_INPUT);

        // Enter token ID
        type(TOKEN_ID_INPUT, "1234");
        waitForLoadingSpinner();

        // Enter price
        type(PRICE_INPUT, "10.0");

        // Select payment token
        click(PAYMENT_TOKEN_SELECT);
        click(By.xpath("//div[contains(text(), 'WETH')]"));

        // Verify royalty fee is displayed
        assertTrue(isElementPresent(ROYALTY_FEE_DISPLAY));

        // Submit listing
        click(SUBMIT_BUTTON);
        waitForElement(CONFIRM_BUTTON);

        // Confirm transaction
        click(CONFIRM_BUTTON);
        waitForTransactionConfirmation();

        // Verify success message
        assertTrue(isElementPresent(By.className("success-message")));
    }

    @Test
    void testNFTPurchaseFlow() {
        // Navigate to OpenSea interface
        click(By.id("opensea-nav"));
        click(BUY_TAB);
        waitForElement(COLLECTION_SELECT);

        // Select collection
        click(COLLECTION_SELECT);
        click(By.xpath("//div[contains(text(), 'Bored Ape Yacht Club')]"));
        waitForElement(TOKEN_ID_INPUT);

        // Enter token ID
        type(TOKEN_ID_INPUT, "5678");
        waitForLoadingSpinner();

        // Verify price and royalty fee
        assertTrue(isElementPresent(By.id("listing-price")));
        assertTrue(isElementPresent(ROYALTY_FEE_DISPLAY));

        // Click buy button
        click(By.id("buy-button"));
        waitForElement(CONFIRM_BUTTON);

        // Confirm transaction
        click(CONFIRM_BUTTON);
        waitForTransactionConfirmation();

        // Verify success message
        assertTrue(isElementPresent(By.className("success-message")));
    }

    @Test
    void testOfferCreationFlow() {
        // Navigate to OpenSea interface
        click(By.id("opensea-nav"));
        click(By.id("offer-tab"));
        waitForElement(COLLECTION_SELECT);

        // Select collection
        click(COLLECTION_SELECT);
        click(By.xpath("//div[contains(text(), 'Doodles')]"));
        waitForElement(TOKEN_ID_INPUT);

        // Enter token ID
        type(TOKEN_ID_INPUT, "9012");
        waitForLoadingSpinner();

        // Enter offer amount
        type(By.id("offer-amount"), "5.0");

        // Select payment token
        click(PAYMENT_TOKEN_SELECT);
        click(By.xpath("//div[contains(text(), 'WETH')]"));

        // Submit offer
        click(SUBMIT_BUTTON);
        waitForElement(CONFIRM_BUTTON);

        // Confirm transaction
        click(CONFIRM_BUTTON);
        waitForTransactionConfirmation();

        // Verify success message
        assertTrue(isElementPresent(By.className("success-message")));
    }

    @Test
    void testInsufficientBalance() {
        // Navigate to OpenSea interface
        click(By.id("opensea-nav"));
        click(BUY_TAB);
        waitForElement(COLLECTION_SELECT);

        // Select collection and token
        click(COLLECTION_SELECT);
        click(By.xpath("//div[contains(text(), 'Bored Ape Yacht Club')]"));
        type(TOKEN_ID_INPUT, "5678");
        waitForLoadingSpinner();

        // Click buy button
        click(By.id("buy-button"));

        // Verify error message
        assertTrue(isElementPresent(By.className("error-message")));
        assertEquals("Insufficient balance", getText(By.className("error-message")));
    }
}
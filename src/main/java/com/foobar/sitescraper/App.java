package com.foobar.sitescraper;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.LoadState;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class App {
    public static void main(String[] args) {
        try (Playwright playwright = Playwright.create();

             Browser browser = playwright.chromium().launch(
                     new BrowserType.LaunchOptions()
                             .setHeadless(false)
                             .setArgs(Arrays.asList("--start-maximized")));

             BrowserContext context = browser.newContext(
                     new Browser.NewContextOptions()
                             .setViewportSize(null))
        ) {
            Page page = context.newPage();
            final String BASE_URL = "https://levalet.com/en/cards-and-tarot.html";
            for (int i = 1; i <= 15; i++) {
                page.navigate(BASE_URL + "?p=" + i);
                page.waitForLoadState(LoadState.NETWORKIDLE);
                page.screenshot(new Page.ScreenshotOptions()
                    .setPath(Paths.get("snapshots/page_" + i + ".png"))
                    .setFullPage(true));

                productScraper(page);
            }
        }
    }

    private static void productScraper(Page page) {
        List<ElementHandle> handleList = page.querySelectorAll("ol.products li.product-item");

        System.out.println("Number of products on page: " + handleList.size());
        for (ElementHandle handle : handleList) {

            String name = handle.querySelector("strong.product-item-name a").innerText().trim();

            String price = handle.querySelector(".price").innerText().trim();

            String url = handle.querySelector("strong.product-item-name a").getAttribute("href");

            ElementHandle buttonSpan = handle.querySelector("button.btn-cart span");

            String status = (buttonSpan != null) ? buttonSpan.innerText().trim() : "Unknown";

            String availability = checkAvailability(status);

            String summary = String.format("Name: %s\nPrice: %s\nURL: %s\nAvailability: %s\n"
                    , name, price, url, availability);
            System.out.println(summary);
        }
    }

    private static String checkAvailability(String status) {
       return switch (status) {
           case "Add to Cart" -> "In stock";
           case "Currently unavailable", "Out of print" -> "Out of stock";
           default -> "Unknown";
       };
    }
}
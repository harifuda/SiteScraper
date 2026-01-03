package com.foobar.sitescraper;

import com.foobar.sitescraper.model.Item;
import com.foobar.sitescraper.repository.ItemRepository;
import com.foobar.sitescraper.repository.MySQLRepository;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.LoadState;

import java.math.BigDecimal;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.*;


public class App {
    private static final int LIMIT = 37;
    public static void main(String[] args) throws Exception{

        String host = System.getenv("DB_HOST");
        String port = System.getenv("DB_PORT");
        String dbName = System.getenv("DB_NAME");
        String user = System.getenv("DB_USER");
        String password = System.getenv("DB_PASSWORD");

        Class.forName("com.mysql.cj.jdbc.Driver");

        String sqlURL = "jdbc:mysql://" + host + ":" + port + "/" + dbName + "?serverTimezone=UTC";
        System.out.println("Connection URL: " + sqlURL);
        Connection conn = DriverManager.getConnection(sqlURL, user, password);

        ItemRepository repository = new MySQLRepository(conn);

        // min-heap by default, add .reversed() to remove the most expensive items
        Queue<Item> topItems = new PriorityQueue<>(
                Comparator.comparing(Item::getPrice).reversed()
        );

        try (Playwright playwright = Playwright.create();
             Browser browser = playwright.chromium().launch(
                     new BrowserType.LaunchOptions()
                             .setHeadless(true)
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

                productScraper(page, topItems);
            }
        }
        List<Item> listItems = new ArrayList<>();
        while(!topItems.isEmpty()) {
            listItems.add(topItems.poll());
        }

        Collections.reverse(listItems);

        repository.saveAll(listItems);
        System.out.println("Saved " + listItems.size() + " item(s) to repository");
    }

    private static void productScraper(Page page, Queue<Item> queue) {
        List<ElementHandle> handleList = page.querySelectorAll("ol.products li.product-item");

        System.out.println("Number of products on page: " + handleList.size());
        for (ElementHandle handle : handleList) {

            String name = handle.querySelector("strong.product-item-name a").innerText().trim();

            String rawPriceValue = handle.querySelector(".price").innerText().trim();

            BigDecimal price = new BigDecimal(rawPriceValue.replaceAll("[^\\d.]", ""));

            String url = handle.querySelector("strong.product-item-name a").getAttribute("href");

            ElementHandle buttonSpan = handle.querySelector("button.btn-cart span");

            String status = (buttonSpan != null) ? buttonSpan.innerText().trim() : "Unknown";

            String availability = checkAvailability(status.trim());

            Item item = new Item(name, price, url, availability);

            if (item.getStatus().equals("In stock"))
                queue.add(item);

            if (queue.size() > LIMIT)
                queue.poll();

            System.out.println(item);
        }
    }

    private static String checkAvailability(String status) {
       return switch (status) {
           case "Add to Cart" -> "In stock";
           case "Currently unavailable" -> "Out of stock";
           case "Out of Print" -> "Discontinued";
           default -> "Unknown";
       };
    }
}
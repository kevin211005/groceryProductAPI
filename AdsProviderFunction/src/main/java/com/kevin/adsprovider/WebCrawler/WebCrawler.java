package com.kevin.adsprovider.WebCrawler;

import com.kevin.adsprovider.Data.Product;
import com.kevin.adsprovider.Service.ProductService;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.*;


@Component
public class WebCrawler {
    private static ChromeDriver driver;
    private static ChromeOptions options = new ChromeOptions();
    public  WebCrawler() {
        String driverLoc = "/opt/chromedriver";
        System.setProperty("webdriver.chrome.driver", driverLoc);
        options.setBinary("/opt/chrome");
        options.addArguments("--headless");
        options.addArguments("--no-sandbox");
        options.addArguments("--single-process");
        options.addArguments("--disable-dev-shm-usage");
    }
    public static List<Product> getWeeklyAd() {
        String qfcURL = "https://flipp.com/en-us/seattle-wa/weekly_ad/5551160-qfc-weekly-ad?postal_code=98115";
        String safeWayURL = "https://flipp.com/en-us/seattle-wa/weekly_ad/5540273-safeway-weekly-ad?postal_code=98115";
        List<String> hrefList = new ArrayList<>();
        System.out.println("------------------------------Get chrome driver-----------------------------------");
        driver = new ChromeDriver(options);
        List<Product> productDetails = new ArrayList<>();
        int count = 0;
        try {
            String pre = qfcURL;
            for (String url : new String[]{qfcURL, safeWayURL}) {
                driver.get(url);
                WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));
                By selector = By.cssSelector("flipp-flyerview canvas div a");
                List<WebElement> wrapperElement = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(selector));
                for (WebElement ele : wrapperElement) {
                    if (!ele.getAttribute("aria-label").equals("logo")) {
                        hrefList.add(ele.getAttribute("href"));
                    }
                    if (url.equals(pre)) {
                        count += 1;
                    }
                }
                pre = url;
            }
            driver.close();
            productDetails = getProductDetails(hrefList, count);
        } catch (Exception ex) {
            System.out.println(ex);
        }
        return productDetails;
    }
    private static List<Product> getProductDetails(List<String> href, int count) {
        driver = new ChromeDriver(options);
        WebDriverWait wait = new WebDriverWait(driver,  Duration.ofSeconds(2));
        List<Product> products = new ArrayList<>();
        String url = "https://flipp.com";
        System.out.println("Get product details start");
        for (String h: href) {
            count -= 1;
            driver.get(url + h);
            try {
                String itemName = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("h2.slideable.title span"))).getText();
                String price = "";
                price += wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("flipp-price"))).getAttribute("value") + " ";
                price += wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("span.price-text"))).getText();
                String imgUrl =  wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("div.item-info-image img"))).getAttribute("src");
                String brand = "";
                if (count >= 0) {
                    brand = "QFC";
                } else {
                    brand = "SafeWay";
                }
                String id = UUID.randomUUID().toString();
                Product product = new Product(id, price, brand, imgUrl, itemName);
                products.add(product);
            } catch (Exception e) {
                System.out.println("Get products details fail");
            }
        }
        driver.close();
        System.out.println("Get product details finished");
        return products;
    }
}

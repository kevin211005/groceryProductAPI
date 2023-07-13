package com.kevin.adsprovider.Controller;
import com.kevin.adsprovider.Data.Product;
import com.kevin.adsprovider.Service.ProductService;
import com.kevin.adsprovider.Service.UserService;
import com.kevin.adsprovider.Data.User;
import com.kevin.adsprovider.MessageHandler.MessageHandler;
import com.kevin.adsprovider.WebCrawler.WebCrawler;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class Controller {
    @Autowired
    private final MessageHandler msgHandler = new MessageHandler();

    private WebCrawler webCrawler = new WebCrawler();
    @Autowired
    UserService userService;

    @Autowired
    ProductService productService;
    @GetMapping("/test")
    public ResponseEntity<?> test() { return new ResponseEntity<String>("Hello J A V A", HttpStatus.OK); }

    @GetMapping("/users")
    public ResponseEntity<Iterable<User>> getUser() {
        System.out.println("trigger");
        return ResponseEntity.ok(userService.getUsers());
    }
    @DeleteMapping("/delete")
    public  ResponseEntity<String> delete() {
        userService.clearAll();
        return ResponseEntity.ok("Deleted");
    }
    @PostMapping("/user")
    public ResponseEntity<User> addStudent(@RequestBody User student) {
        return ResponseEntity.ok(userService.addUser(student));
    }
    @GetMapping("/products")
    public ResponseEntity<Iterable<Product>> getProduct() {
        System.out.println("trigger");
        return ResponseEntity.ok(productService.getProducts());
    }
    @PostMapping("/product")
    public ResponseEntity<Product> addProduct(@RequestBody Product product) {
        return ResponseEntity.ok(productService.addProduct(product));
    }

    @PostMapping("/messaging")
    public ResponseEntity<?> messagingAPI(@RequestHeader("X-Line-Signature") String xLineSignature,
                                          @RequestBody String requestBody) {
        if (msgHandler.checkFromLine(requestBody, xLineSignature)) {
            System.out.println("Authentication success");
            JSONObject object = new JSONObject(requestBody);
            for (int i = 0; i < object.getJSONArray("events").length(); i++) {
                if (object.getJSONArray("events").getJSONObject(i).getString("type").equals("message")) {
                    msgHandler.handleMsg(object.getJSONArray("events").getJSONObject(i));
                }
            }
            return new ResponseEntity<>("ok", HttpStatus.OK);
        } else {
            System.out.println("Authentication failed");
            return new ResponseEntity<>("Line Platform authentication failed", HttpStatus.BAD_GATEWAY);
        }
    }


    @PostMapping("/getProducts")
    public ResponseEntity<?> sendingAPI() {
        /*
        if (productService.clearAll()) {
            List<Product> products = webCrawler.getWeeklyAd();
            productService.addProducts(products);
            return new ResponseEntity<>("OK", HttpStatus.OK);
        } else {
            System.out.println("Unable to clear previous products");
            return new ResponseEntity<>("OK", HttpStatus.EXPECTATION_FAILED);
        }
        */
        List<Product> products = webCrawler.getWeeklyAd();
        return new ResponseEntity<>("OK", HttpStatus.OK);
    }
}

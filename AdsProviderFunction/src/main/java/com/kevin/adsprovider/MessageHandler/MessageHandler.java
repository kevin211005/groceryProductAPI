package com.kevin.adsprovider.MessageHandler;

import com.kevin.adsprovider.Data.Product;
import com.kevin.adsprovider.Data.User;
import com.kevin.adsprovider.Repository.ProductRepository;
import com.kevin.adsprovider.Service.ProductService;
import com.kevin.adsprovider.Service.UserService;
import okhttp3.*;
import org.apache.tomcat.util.codec.binary.Base64;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.*;

@Component
public class MessageHandler {
    private final OkHttpClient client = new OkHttpClient();
    private final String LINE_TOKEN = "HHOauYE0kvVqby3o/zHFicRZqWzwKXVpRguKVVg67nWHTM13s3UToywemwoRaN2kUfbES6Lh3pYUv6Y4K/LWlHp+hy3JamOdxrPauK89vij20TPCcqMf68bFEfj9XtuMmq3jfMzpNEybHzfH55i3DgdB04t89/1O/w1cDnyilFU=";
    private final String MY_ID = "Uef1deb537240ca8261f5dae16db6735b";
    private final String ERROR = "Ambiguous command \n";
    private final String SUGGESTION = "Common use command: \n\n updateItems: item1, ... \n\n get ## get desired items\n\n getAds ## get ads\n\n clearItems ## clear desired items\n";
    private final String LINE_SECRET = "74f08dba4b44bc0b4d4262373ef5fdd8";
    @Autowired
    UserService userService;
    @Autowired
    ProductService productService;
    public boolean checkFromLine(String requestBody, String xLineSignature) {
        SecretKeySpec key = new SecretKeySpec(LINE_SECRET.getBytes(), "HmacSHA256");
        Mac mac;
        try {
            mac = Mac.getInstance("HmacSHA256");
            mac.init(key);
            byte[] source = requestBody.getBytes("UTF-8");
            String signature = Base64.encodeBase64String(mac.doFinal(source));
            if (signature.equals(xLineSignature)) {
                return true;
            }
        } catch (NoSuchAlgorithmException | InvalidKeyException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return false;
    }
    public void sendMsg(String userId, String msg) {
        JSONObject body = new JSONObject();
        JSONArray messages = new JSONArray();
        JSONObject message = new JSONObject();
        message.put("type", "text");
        message.put("text", msg);
        body.put("to", userId);
        body.put("messages", messages);
        messages.put(message);
        try {
            requestHandler(body);
        } catch (Exception e ) {
            System.out.println("error");
        }
    }
    public void sendImg(String userId, String imgUrl) {
        JSONObject body = new JSONObject();
        JSONArray messages = new JSONArray();
        JSONObject message = new JSONObject();
        message.put("type", "image");
        message.put("originalContentUrl", imgUrl);
        message.put("previewImageUrl", imgUrl);
        body.put("to", userId);
        body.put("messages", messages);
        messages.put(message);
        try {
            requestHandler(body);
        } catch (Exception e ) {
            System.out.println("error");
        }
    }
    private void requestHandler(JSONObject body) throws IOException {
        Request request = new Request.Builder().url("https://api.line.me/v2/bot/message/push")
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer {" + LINE_TOKEN + "}")
                .post(RequestBody.create(MediaType.parse("application/json; charset=utf-8"), body.toString())).build();

        client.newCall(request).execute();
    }
    public void handleMsg(JSONObject event) {
        switch (event.getJSONObject("message").getString("type")) {
            case "text":
                String command = event.getJSONObject("message").getString("text").toLowerCase().replace(" ", "");
                String[] commands = command.split(":");
                analysisMsg(event);
                break;
            default:
                ErrorText(event.getString("replyToken"));
                break;
        }
    }
    private void analysisMsg(JSONObject event) {
        String command = event.getJSONObject("message").getString("text").toLowerCase();
        String[] commands = handleCommand(command);
        String replyToken = event.getString("replyToken");
        String id = event.getJSONObject("source").getString("userId");
        boolean userExisted;
        boolean providedMsg = false;
        String replyMsg = "";
        try {
            userExisted = userService.userExist(id);
            if (!userExisted) {
                replyMsg =  "Please use updateItems command to start";
            }
        } catch (Exception ex) {
            text(replyToken, "Get data from dynamodb failed");
            return;
        }
        switch (commands[0]) {
            case "updateitems":
                String[] items = Arrays.copyOfRange(commands, 1, commands.length);
                if (userExisted) {
                    Optional<User> user = userService.getUser(id);
                    if (user.isPresent()) {
                        List<String> desiredItems = user.get().getDesiredItem();
                        desiredItems.addAll(Arrays.asList(items));
                        user.get().setDesiredItem(desiredItems);
                        userService.addUser(user.get());
                        replyMsg = "Successfully upload your desired items \n";
                        replyMsg += "your userId = " + id + "\n";
                        replyMsg += "desired Item = " + String.join(" ", desiredItems);
                    } else {
                        replyMsg = "database get user conflicts";
                        System.out.println("database get user conflicts");
                    }
                } else {
                    List<String> desiredItems = Arrays.asList(items);
                    User newUser = new User(id, desiredItems);
                    replyMsg = "Successfully upload your desired items \n";
                    replyMsg += "your userId = " + id + "\n";
                    replyMsg += "desired Item = " + String.join(" ", desiredItems);
                    userService.addUser(newUser);
                }
                break;
            case "get":
                if (userExisted) {
                    Optional<User> user = userService.getUser(id);
                    if (user.isPresent()) {
                        List<String> desiredItems = user.get().getDesiredItem();
                        replyMsg = "desired Item = " + String.join(" ", desiredItems);
                    }
                }
                break;
            case "getads":
                Optional<User> user = userService.getUser(id);
                if (user.isPresent()) {
                    List<String> desiredItems = user.get().getDesiredItem();
                    List<Product> allProducts = new ArrayList<>();
                    productService.getProducts().forEach(allProducts::add);
                    List<Product> ads = new ArrayList<>();
                    for (Product product: allProducts) {
                        for (String desireItem: desiredItems) {
                            if (product.getProductName().toLowerCase().contains(desireItem)) {
                                ads.add(product);
                            }
                        }
                    }
                    if (!ads.isEmpty()) {
                        generateAdsMsg(user.get().getId(), ads);
                        providedMsg = true;
                    } else {
                        replyMsg = "No desired Item this period";
                    }
                }
                break;
            case "clearitems":
                if (userExisted) {
                    replyMsg = userService.deleteItems(id) ? "Successfully delete desired item list" : "Delete error";
                }
                break;
            default:
                replyMsg = ERROR + SUGGESTION;
        }
        if (!providedMsg) {
            text(replyToken, replyMsg);
        }
    }
    private void ErrorText(String replyToken) {
        text(replyToken, ERROR + SUGGESTION);
    }
    private void text(String replyToken, String text) {
        JSONObject body = new JSONObject();
        JSONArray messages = new JSONArray();
        JSONObject message = new JSONObject();
        message.put("type", "text");
        message.put("text", text);
        messages.put(message);
        body.put("replyToken", replyToken);
        body.put("messages", messages);
        try {
            sendToLinePlatform(body);
        } catch (IOException ex)
        {
            System.out.println(ex);
        }
        System.out.println(body.toString(5));
    }
    private void generateAdsMsg(String userId, List<Product> products) {
        for (Product product: products) {
            String msg = "";
            msg += product.getProductName() + "\n";
            msg += product.getPrice() + "\n";
            msg += "Grocery store: " + product.getBrand();
            sendMsg(userId, msg);
            sendImg(userId, product.getImgUrl());
        }
    }
    public void sendToLinePlatform(JSONObject json) throws IOException {
        Request request = new Request.Builder().url("https://api.line.me/v2/bot/message/reply")
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer {" + LINE_TOKEN + "}")
                .post(RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json.toString())).build();
        System.out.println("Send request");
        client.newCall(request).execute();
    }
    private String[] handleCommand(String msg) {
        String[] command = msg.split(":");
        command[0] = command[0].replace(" ", "");
        String[] commandWithProducts;
        if (command.length > 1) {
            //split by ","
            String[] products = command[1].split(",");
            // remove leading and tailing whitespace
            for (int i = 0; i < products.length; i++) {
                products[i] = products[i].trim();
            }
            commandWithProducts = new String[1 + products.length];
            // combine commands and product array
            System.arraycopy(command, 0, commandWithProducts, 0, 1);
            System.arraycopy(products, 0, commandWithProducts, 1, products.length);
        } else {
            commandWithProducts = command;
        }
        return commandWithProducts;
    }
}

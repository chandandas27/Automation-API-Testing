package service;

import dto.AuthResponse;
import dto.Authorization;
import dto.OrderDetails;
import dto.Orders;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.specification.RequestSpecification;
import org.testng.Assert;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static io.restassured.RestAssured.given;

public class Ecommerce {
    public static void main(String[] args) {

        // Authorization
        RequestSpecification req = new RequestSpecBuilder().setBaseUri("https://rahulshettyacademy.com/")
                .setContentType(ContentType.JSON).build();
        Authorization auth = new Authorization();
        auth.setUserEmail("ch@g.com");
        auth.setUserPassword("Welcome@123");

        RequestSpecification reqAuth = given().spec(req).body(auth);

        AuthResponse authResponse = reqAuth.when().post("api/ecom/auth/login")
                .then().extract().response().as(AuthResponse.class);
        String token = authResponse.getToken();
        System.out.println(authResponse.getToken());
        String userId = authResponse.getUserId();
        System.out.println(authResponse.getUserId());

        // Add Product
        RequestSpecification addProduct = new RequestSpecBuilder().setBaseUri("https://rahulshettyacademy.com/")
                .addHeader("authorization", token)
                .build();

        RequestSpecification createProduct = given().spec(addProduct)
                .param("productName","Camera")
                .param("productAddedBy", userId)
                .param("productCategory","fashion")
                .param("productSubCategory", "Electronics")
                .param("productPrice", "20000")
                .param("productDescription", "Sony")
                .param("productFor", "Men")
                .multiPart("productImage", new File("C:\\Users\\CHANDAN DAS\\Desktop\\camera.jpeg"));

        String addProductResponse = createProduct.when().post("api/ecom/product/add-product")
                .then().log().all().extract().response().asString();

        JsonPath jp = new JsonPath(addProductResponse);
        String productId = jp.get("productId");

        // Create Order
        RequestSpecification createOrder = new RequestSpecBuilder().setBaseUri("https://rahulshettyacademy.com/")
                .addHeader("authorization", token)
                .setContentType(ContentType.JSON)
                .build();

        OrderDetails orderDetails = new OrderDetails();
        orderDetails.setCountry("India");
        orderDetails.setProductOrderedId(productId);

        List<OrderDetails> orderDetailsList = new ArrayList<>();
        orderDetailsList.add(orderDetails);

        Orders orders = new Orders();
        orders.setOrders(orderDetailsList);

        RequestSpecification createOrderReq = given().log().all().spec(createOrder).body(orders);

        createOrderReq.when().post("api/ecom/order/create-order")
                .then().log().all().extract().response().asString();

        // Delete Order
        RequestSpecification deleteProduct = new RequestSpecBuilder().setBaseUri("https://rahulshettyacademy.com/")
                .addHeader("authorization", token)
                .setContentType(ContentType.JSON)
                .build();

        RequestSpecification deleteProductReq = given().log().all().spec(deleteProduct)
                .pathParam("productId", productId);

        String deleteProductResponse = deleteProductReq.when().delete("/api/ecom/product/delete-product/{productId}")
                .then().log().all().extract().response().asString();

        JsonPath jsonPath = new JsonPath(deleteProductResponse);

        Assert.assertEquals("Product Deleted Successfully",jsonPath.get("message"));
    }
}

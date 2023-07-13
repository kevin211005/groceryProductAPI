package com.kevin.adsprovider.Service;

import com.kevin.adsprovider.Data.Product;
import com.kevin.adsprovider.Repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {

    @Autowired
    ProductRepository productRepo;

    public Iterable<Product> getProducts() {
        return productRepo.findAll();
    }

    public Product addProduct(Product product) {
        try {
            return productRepo.save(product);
        } catch (Exception ex) {
            System.out.println(ex);
        }
        return product;
    }
    public boolean clearAll() {
        try {
            productRepo.deleteAll();
            return true;
        } catch (Exception ex) {
            System.out.println(ex);
            return false;
        }
    }
    public boolean addProducts(List<Product> products) {
        boolean success = false;
        try {
            productRepo.saveAll(products);
            success = true;
        } catch (Exception ex) {
            System.out.println(ex);
            success = false;
        } finally {
            return success;
        }
    }

}

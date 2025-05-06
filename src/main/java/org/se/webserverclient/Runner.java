package org.se.webserverclient;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Scanner;


@Configuration
public class Runner implements CommandLineRunner {
    private  final WebClient webClient = WebClient.builder()
            .baseUrl("http://localhost:8080/api/products")
            .build();

    @Override
    public void run(String... args) throws Exception {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("\n 1.Show all products ");
            System.out.println("2.Add product ");
            System.out.println("3.Update product ");
            System.out.println("4.Delete product . ");
            System.out.println("5.Exit ");
            System.out.println("Choose an option above : ");
            int option = Integer.parseInt(scanner.nextLine());

            switch (option) {
                case 1 -> showAllProducts();
                case 2 -> addProducts(scanner);
                case 3 -> updateProduct(scanner);
                case 4 -> deleteProduct(scanner);
                case 5 -> {
                    System.out.println("Exit ");
                    return;

                }
                default -> System.out.println("Invalid option number ");
            }
        }
    }
    public void showAllProducts() {
        List<ProductData> productDataFlux = webClient
                .get()
                .retrieve()
                .bodyToFlux(ProductData.class)
                .retry(3)
                .onErrorResume(ex -> {
                    System.out.println("Error fetching products: " + ex.getMessage());
                    return Flux.empty();
                })
                .collectList()
                .block();

        if(productDataFlux!=null && !productDataFlux.isEmpty()) {
            System.out.println("Print all products ");
            for (ProductData productData : productDataFlux) {
                System.out.println(productData);
            }

        }else {
            System.out.println("No products found");
        }

    }

    private void addProducts(Scanner scanner) {
        System.out.println("Enter product name : ");
        String productName = scanner.nextLine();

        System.out.println("Enter product Category");
        String productCategory = scanner.nextLine();

        System.out.println("Enter product price : ");
        double productPrice = Double.parseDouble(scanner.nextLine());

        ProductData newProductData = new ProductData(productName,productCategory,productPrice);
        ProductData response= webClient
                .post()
                .bodyValue(newProductData)
                .retrieve()
                .bodyToMono(ProductData.class)
                .onErrorResume( ex->{
                            System.out.println("unable to add product" + ex.getMessage());
                            return Mono.empty();
                        })

                .block();
        System.out.println("Product added" + response);

    }

    private void updateProduct(Scanner scanner) {
        System.out.println("Enter product id: ");
        Long productId = Long.parseLong(scanner.nextLine());

        System.out.println("Enter product name to update: ");
        String newProductName = scanner.nextLine();

        System.out.println("Enter product category to update: ");
        String productCategory = scanner.nextLine();

        System.out.println("Enter product price to update: ");
        double productPrice = Double.parseDouble(scanner.nextLine());

        ProductData newProductData = new ProductData(newProductName,productCategory,productPrice);
        ProductData response = webClient
                .put()
                .uri("/{id}",productId)
                .bodyValue(newProductData)
                .retrieve()
                .bodyToMono(ProductData.class)
                .onErrorResume(ex->{
                    System.out.println("unable to update product" + ex.getMessage());
                    return Mono.empty();
                })
                .block();

        System.out.println("Product updated" + response);
    }
    private void deleteProduct(Scanner scanner) {
        System.out.println("Enter product id: ");
        Long productId = Long.parseLong(scanner.nextLine());
        webClient.delete()
                .uri("/{id}",productId)
                .retrieve()
                .toBodilessEntity()
                .onErrorResume(ex-> {
                    System.out.println("unable to delete product" + ex.getMessage());
                    return Mono.empty();
                })
                .block();
        System.out.println("Product deleted" + productId);
    }

}

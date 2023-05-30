package com.example.springsecurityapplication.controllers;

import com.example.springsecurityapplication.enumm.Status;
import com.example.springsecurityapplication.enumm.StatusConverter;
import com.example.springsecurityapplication.models.*;
import com.example.springsecurityapplication.repositories.CategoryRepository;
import com.example.springsecurityapplication.repositories.OrderRepository;
import com.example.springsecurityapplication.repositories.PersonRepository;
import com.example.springsecurityapplication.services.OrderService;
import com.example.springsecurityapplication.services.PersonService;
import com.example.springsecurityapplication.services.ProductService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.*;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final ProductService productService;
    private final OrderRepository orderRepository;
    private final OrderService orderService;


    private final PersonService personService;
    private final PersonRepository personRepository;
    private final StatusConverter statusConverter;

    @Value("${upload.path}")
    private String uploadPath;

    private final CategoryRepository categoryRepository;

    @Autowired
    public AdminController(ProductService productService, CategoryRepository categoryRepository, OrderRepository orderRepository, OrderService orderService, PersonService personService, PersonRepository personRepository, StatusConverter statusConverter) {
        this.productService = productService;
        this.categoryRepository = categoryRepository;
        this.orderRepository = orderRepository;
        this.orderService = orderService;
        this.personService = personService;
        this.personRepository = personRepository;
        this.statusConverter = statusConverter;
    }

    @GetMapping("/product/add")
    public String addProduct(Model model){
        model.addAttribute("product", new Product());
        model.addAttribute("category", categoryRepository.findAll());
        return "product/addProduct";
    }

    @PostMapping("/product/add")
    public String addProduct(@ModelAttribute("product") @Valid Product product, BindingResult bindingResult, @RequestParam("file_one")MultipartFile file_one, @RequestParam("category") int category, Model model) throws IOException {
        Category category_db = (Category) categoryRepository.findById(category).orElseThrow();
        System.out.println(category_db.getName());
        if(bindingResult.hasErrors()){
            model.addAttribute("category", categoryRepository.findAll());
            return "product/addProduct";
        }

        if(file_one != null){
            File uploadDir = new File(uploadPath);
            if(!uploadDir.exists()){
                uploadDir.mkdir();
            }
            String uuidFile = UUID.randomUUID().toString();
            String resultFileName = uuidFile + "." + file_one.getOriginalFilename();
            file_one.transferTo(new File(uploadPath + "/" + resultFileName));
            Image image = new Image();
            image.setProduct(product);
            image.setFileName(resultFileName);
            product.addImageToProduct(image);

        }

        productService.saveProduct(product, category_db);
        return "redirect:/admin";
    }


    @GetMapping()
    public String admin(Model model)
    {
        model.addAttribute("products", productService.getAllProduct());
        return "admin";
    }

    @GetMapping("/product/delete/{id}")
    public String deleteProduct(@PathVariable("id") int id){
        productService.deleteProduct(id);
        return "redirect:/admin";
    }



    @GetMapping("/product/edit/{id}")
    public String editProduct(Model model, @PathVariable("id") int id){
        model.addAttribute("product", productService.getProductId(id));
        model.addAttribute("category", categoryRepository.findAll());
        return "product/editProduct";
    }
    @PostMapping("/product/edit/{id}")
    public String editProduct(@ModelAttribute("product") @Valid Product product, BindingResult bindingResult, @PathVariable("id") int id, Model model){
        if(bindingResult.hasErrors()){
            model.addAttribute("category", categoryRepository.findAll());
            return "product/editProduct";}
        productService.updateProduct(id, product);
        return "redirect:/admin";
    }
    //Работа с заказами ================
    @GetMapping("/order/edit/{id}")
    public String editOrderForm(@PathVariable("id") int id, Model model) {
        Order order = orderService.getOrderId(id);
        if (order != null) {
            model.addAttribute("order", order);
            return "admin/editOrder";
        } else {
            // Обработка случая, когда заказ не найден
            return "error"; // или любая другая страница ошибки
        }
    }
    @PostMapping("/order/edit/{id}")
    public String editOrder(@PathVariable("id") int id, @RequestParam("status") String status) {
        Order order = orderService.getOrderId(id);
        if (order != null) {
            order.setStatus(Status.valueOf(status));
            orderService.updateOrder(id, order);
            return "redirect:/admin/orders";
        } else {
            // Обработка случая, когда заказ не найден
            return "error"; // или любая другая страница ошибки
        }
    }
    @GetMapping("/orders")
    public String allOrders(Model model) {
        List<Order> orderList = orderRepository.findAll();
        model.addAttribute("orders", orderList);
        return "/admin/orders";
    }
    // Работа с поиском заказов =========================
    @PostMapping("/search")
    public String orderSearch(@RequestParam("search") String search, Model model) {
        List<Order> orderList = orderRepository.findAll();
        model.addAttribute("orders", orderList);
        model.addAttribute("value_search", search);
        model.addAttribute("search_orders", orderRepository.findByNumberContainingIgnoreCase(search));
        return "/admin/orders";
    }
    //    Работа с пользователмя ==============================
    @GetMapping("/all")
    public String allPeople(Model model){
        model.addAttribute("people", personService.findAll());
        return "admin/people";
    }
    @RequestMapping(value = "/changerole/{id}")
    public String changeRole(@PathVariable("id") int id){
        if (personService.findOne(id).getRole().equals("ROLE_USER")){
            personRepository.setRoleAdmin(id);
        }else{
            personRepository.setRoleUser(id);
        }
        return "redirect:/admin/all";
    }


}

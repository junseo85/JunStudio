package com.music.JunStudio.controller;


import com.music.JunStudio.model.User;
import com.music.JunStudio.repository.UserRepository;
import com.stripe.Stripe;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;

@Controller
public class CheckoutController {

    @Autowired
    private UserRepository userRepository;

    // Pulls the secret key from your application.properties
    @Value("${stripe.api.key}")
    private String stripeApiKey;

    // Initializes Stripe when the app starts
    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeApiKey;
    }

    // 1. Add the @RequestParam to catch the number from your HTML form
    @PostMapping("/checkout")
    public String createCheckoutSession(@RequestParam(defaultValue = "1") Long quantity) throws Exception {

        String YOUR_DOMAIN = "http://localhost:8080";

        SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(YOUR_DOMAIN + "/checkout/success?qty=" + quantity) // Pass qty to success route!
                .setCancelUrl(YOUR_DOMAIN + "/dashboard")
                .addLineItem(
                        SessionCreateParams.LineItem.builder()
                                // 2. Pass the dynamic quantity to Stripe
                                .setQuantity(quantity)
                                .setPriceData(
                                        SessionCreateParams.LineItem.PriceData.builder()
                                                .setCurrency("usd")
                                                // 3. Update the base price to $75.00 (7500 cents)
                                                .setUnitAmount(7500L)
                                                .setProductData(
                                                        SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                // Dynamically name it so the receipt looks nice
                                                                .setName(quantity + " Lesson Credit(s)")
                                                                .build())
                                                .build())
                                .build())
                .build();

        Session session = Session.create(params);
        return "redirect:" + session.getUrl();
    }

    // Catch the 'qty' we passed in the success URL above
    @GetMapping("/checkout/success")
    public String checkoutSuccess(@RequestParam(defaultValue = "1") Integer qty, Principal principal) {

        User currentUser = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Add the exact amount they purchased!
        currentUser.setLessonCredits(currentUser.getLessonCredits() + qty);
        userRepository.save(currentUser);

        return "redirect:/dashboard?payment=success";
    }
}
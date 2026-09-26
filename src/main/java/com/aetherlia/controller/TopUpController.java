package com.aetherlia.controller;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.aetherlia.entity.TopUpTransaction;
import com.aetherlia.entity.User;
import com.aetherlia.payment.SepayWebhookPayload;
import com.aetherlia.repository.UserRepository;
import com.aetherlia.service.TopUpService;

import tools.jackson.databind.json.JsonMapper;

@Controller
public class TopUpController {

    private final UserRepository userRepository;
    private final TopUpService topUpService;
    private final JsonMapper jsonMapper;

    @Value("${aetherlia.payment.account-number}")
    private String accountNumber;

    @Value("${aetherlia.payment.account-name}")
    private String accountName;

    @Value("${aetherlia.payment.bank-code}")
    private String bankCode;

    // =====================================================
    // API KEY SEPAY
    // =====================================================

    @Value("${aetherlia.payment.sepay-api-key:}")
    private String sepayApiKey;

    public TopUpController(
            UserRepository userRepository,
            TopUpService topUpService,
            JsonMapper jsonMapper
    ) {
        this.userRepository = userRepository;
        this.topUpService = topUpService;
        this.jsonMapper = jsonMapper;
    }

    // =====================================================
    // TRANG NẠP TIỀN
    // =====================================================

    @GetMapping("/topup")
    public String topUpPage(
            Authentication authentication,
            Model model
    ) {

        User user = userRepository.findByUsername(
                authentication.getName()
        );

        if (user == null) {
            return "redirect:/login";
        }

        model.addAttribute(
                "balance",
                topUpService.getBalance(user)
        );

        return "topup";
    }

    // =====================================================
    // TẠO GIAO DỊCH
    // =====================================================

    @PostMapping("/topup/create")
    public String createTopUp(
            Authentication authentication,
            @RequestParam Long amount
    ) {

        User user = userRepository.findByUsername(
                authentication.getName()
        );

        if (user == null) {
            return "redirect:/login";
        }

        TopUpTransaction transaction =
                topUpService.createTopUp(
                        user,
                        amount
                );

        return "redirect:/payment/"
                + transaction.getPaymentCode();
    }

    // =====================================================
    // TRANG THANH TOÁN
    // =====================================================

    @GetMapping("/payment/{paymentCode}")
    public String paymentPage(
            Authentication authentication,
            @PathVariable String paymentCode,
            Model model
    ) {

        User user = userRepository.findByUsername(
                authentication.getName()
        );

        if (user == null) {
            return "redirect:/login";
        }

        TopUpTransaction transaction =
                topUpService.getTransaction(
                        paymentCode,
                        user
                );

        model.addAttribute(
                "transaction",
                transaction
        );

        model.addAttribute(
                "qrUrl",
                topUpService.buildQrUrl(transaction)
        );

        model.addAttribute(
                "bankCode",
                bankCode
        );

        model.addAttribute(
                "accountNumber",
                accountNumber
        );

        model.addAttribute(
                "accountName",
                accountName
        );

        return "payment";
    }

    // =====================================================
    // KIỂM TRA TRẠNG THÁI THANH TOÁN
    // =====================================================

    @GetMapping(
            value = "/payment/api/status/{paymentCode}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseBody
    public ResponseEntity<Map<String, Object>> paymentStatus(
            Authentication authentication,
            @PathVariable String paymentCode
    ) {

        if (authentication == null) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(
                            Map.of(
                                    "success",
                                    false,
                                    "message",
                                    "Unauthorized"
                            )
                    );
        }

        User user = userRepository.findByUsername(
                authentication.getName()
        );

        if (user == null) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(
                            Map.of(
                                    "success",
                                    false,
                                    "message",
                                    "User không tồn tại"
                            )
                    );
        }

        TopUpTransaction transaction =
                topUpService.getTransaction(
                        paymentCode,
                        user
                );

        return ResponseEntity
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(
                        Map.of(
                                "success",
                                true,

                                "status",
                                transaction
                                        .getStatus()
                                        .name(),

                                "amount",
                                transaction.getAmount(),

                                "balance",
                                topUpService.getBalance(user)
                        )
                );
    }

    // =====================================================
    // SEPAY WEBHOOK
    // =====================================================

    @PostMapping(
            value = "/api/payment/webhook/sepay",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseBody
    public ResponseEntity<Map<String, Object>> sepayWebhook(

            @RequestHeader(
                    value = "Authorization",
                    required = false
            )
            String authorization,

            @RequestBody String rawBody
    ) {

        // =================================================
        // KIỂM TRA API KEY
        // =================================================

        if (!isValidApiKey(authorization)) {

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(
                            Map.of(
                                    "success",
                                    false,
                                    "message",
                                    "Unauthorized"
                            )
                    );
        }

        try {

            // =================================================
            // PARSE JSON
            // =================================================

            SepayWebhookPayload payload =
                    jsonMapper.readValue(
                            rawBody,
                            SepayWebhookPayload.class
                    );

            // =================================================
            // TÌM MÃ TOPUP
            // =================================================

            String paymentCode =
                    extractPaymentCode(payload);

            if (paymentCode == null) {

                return ResponseEntity
                        .ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(
                                Map.of(
                                        "success",
                                        true,
                                        "message",
                                        "Không có mã TOPUP"
                                )
                        );
            }

            // =================================================
            // KIỂM TRA SỐ TIỀN
            // =================================================

            if (payload.getTransferAmount() == null) {

                return ResponseEntity
                        .ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(
                                Map.of(
                                        "success",
                                        true,
                                        "message",
                                        "Không có transferAmount"
                                )
                        );
            }

            // =================================================
            // XỬ LÝ GIAO DỊCH
            // =================================================

            topUpService.processPayment(
                    paymentCode,
                    payload.getTransferAmount(),
                    payload.getTransferType(),
                    payload.getAccountNumber(),
                    payload.getId()
            );

            // =================================================
            // THÀNH CÔNG
            // =================================================

            return ResponseEntity
                    .ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(
                            Map.of(
                                    "success",
                                    true
                            )
                    );

        } catch (Exception e) {

            /*
             * Trả HTTP 500 khi xử lý giao dịch thất bại.
             *
             * Điều này quan trọng vì SePay có cơ chế retry
             * khi webhook nhận response không thành công.
             */

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(
                            Map.of(
                                    "success",
                                    false,
                                    "message",
                                    "Không thể xử lý webhook"
                            )
                    );
        }
    }

    // =====================================================
    // KIỂM TRA API KEY
    // =====================================================

    private boolean isValidApiKey(
            String authorization
    ) {

        if (sepayApiKey == null
                || sepayApiKey.isBlank()) {

            return false;
        }

        if (authorization == null
                || authorization.isBlank()) {

            return false;
        }

        final String prefix = "Apikey ";

        if (!authorization.regionMatches(
                true,
                0,
                prefix,
                0,
                prefix.length()
        )) {

            return false;
        }

        String receivedApiKey =
                authorization.substring(
                        prefix.length()
                );

        if (receivedApiKey.isBlank()) {
            return false;
        }

        return MessageDigest.isEqual(
                sepayApiKey.getBytes(
                        StandardCharsets.UTF_8
                ),
                receivedApiKey.getBytes(
                        StandardCharsets.UTF_8
                )
        );
    }

    // =====================================================
    // TÌM MÃ TOPUP
    // =====================================================

    private String extractPaymentCode(
            SepayWebhookPayload payload
    ) {

        // ---------------------------------------------
        // Ưu tiên trường code
        // ---------------------------------------------

        if (payload.getCode() != null
                && payload.getCode().matches(
                        "TOPUP_[A-Z0-9]{8}"
                )) {

            return payload.getCode();
        }

        // ---------------------------------------------
        // Nếu không có code thì tìm trong content
        // ---------------------------------------------

        String content = payload.getContent();

        if (content == null) {
            return null;
        }

        Pattern pattern =
                Pattern.compile(
                        "TOPUP_[A-Z0-9]{8}",
                        Pattern.CASE_INSENSITIVE
                );

        Matcher matcher =
                pattern.matcher(content);

        if (matcher.find()) {

            return matcher
                    .group()
                    .toUpperCase();
        }

        return null;
    }
}
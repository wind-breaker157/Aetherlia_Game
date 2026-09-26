package com.aetherlia.service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aetherlia.entity.TopUpStatus;
import com.aetherlia.entity.TopUpTransaction;
import com.aetherlia.entity.User;
import com.aetherlia.entity.Wallet;
import com.aetherlia.repository.TopUpTransactionRepository;
import com.aetherlia.repository.WalletRepository;

@Service
public class TopUpService {

    private final WalletRepository walletRepository;
    private final TopUpTransactionRepository topUpTransactionRepository;

    @Value("${aetherlia.payment.bank-code}")
    private String bankCode;

    @Value("${aetherlia.payment.account-number}")
    private String accountNumber;

    @Value("${aetherlia.payment.account-name}")
    private String accountName;

    public TopUpService(
            WalletRepository walletRepository,
            TopUpTransactionRepository topUpTransactionRepository
    ) {
        this.walletRepository = walletRepository;
        this.topUpTransactionRepository = topUpTransactionRepository;
    }

    // =====================================================
    // WALLET
    // =====================================================

    @Transactional
    public Wallet getOrCreateWallet(User user) {

        return walletRepository.findByUser(user)
                .orElseGet(() -> {

                    Wallet wallet = new Wallet(user);

                    wallet.setBalance(0L);

                    return walletRepository.save(wallet);
                });
    }

    public long getBalance(User user) {

        return getOrCreateWallet(user).getBalance();
    }

    // =====================================================
    // TẠO GIAO DỊCH NẠP TIỀN
    // =====================================================

    @Transactional
    public TopUpTransaction createTopUp(
            User user,
            long amount
    ) {

        validateAmount(amount);

        String paymentCode = generatePaymentCode();

        TopUpTransaction transaction =
                new TopUpTransaction();

        transaction.setUser(user);
        transaction.setPaymentCode(paymentCode);
        transaction.setAmount(amount);
        transaction.setStatus(TopUpStatus.PENDING);

        return topUpTransactionRepository.save(transaction);
    }

    // =====================================================
    // TẠO URL QR VIETQR
    // =====================================================

    public String buildQrUrl(
            TopUpTransaction transaction
    ) {

        String description =
                URLEncoder.encode(
                        transaction.getPaymentCode(),
                        StandardCharsets.UTF_8
                );

        String encodedAccountName =
                URLEncoder.encode(
                        accountName,
                        StandardCharsets.UTF_8
                );

        /*
         * Quick Link chính thức của VietQR:
         *
         * https://img.vietqr.io/image/
         * BANK_ID-ACCOUNT_NO-TEMPLATE.jpg
         *
         * Sau đó truyền:
         * amount
         * addInfo
         * accountName
         */

        return "https://img.vietqr.io/image/"
                + bankCode
                + "-"
                + accountNumber
                + "-compact2.jpg"
                + "?amount="
                + transaction.getAmount()
                + "&addInfo="
                + description
                + "&accountName="
                + encodedAccountName;
    }

    // =====================================================
    // LẤY GIAO DỊCH
    // =====================================================

    public TopUpTransaction getTransaction(
            String paymentCode,
            User user
    ) {

        return topUpTransactionRepository
                .findByPaymentCodeAndUser(
                        paymentCode,
                        user
                )
                .orElseThrow(() ->
                        new RuntimeException(
                                "Không tìm thấy giao dịch nạp tiền."
                        )
                );
    }

    // =====================================================
    // XỬ LÝ THANH TOÁN TỪ WEBHOOK
    // =====================================================

    @Transactional
    public boolean processPayment(
            String paymentCode,
            long transferAmount,
            String transferType,
            String receivedAccountNumber,
            Long gatewayTransactionId
    ) {

        // ---------------------------------------------
        // CHỈ NHẬN TIỀN VÀO
        // ---------------------------------------------

        if (!"in".equalsIgnoreCase(
                transferType
        )) {

            return false;
        }

        // ---------------------------------------------
        // KIỂM TRA GIAO DỊCH GATEWAY ĐÃ XỬ LÝ
        // ---------------------------------------------

        if (gatewayTransactionId != null) {

            if (topUpTransactionRepository
                    .findByGatewayTransactionId(
                            gatewayTransactionId
                    )
                    .isPresent()) {

                return true;
            }
        }

        // ---------------------------------------------
        // KHÓA GIAO DỊCH
        // ---------------------------------------------

        TopUpTransaction transaction =
                topUpTransactionRepository
                        .findByPaymentCodeForUpdate(
                                paymentCode
                        )
                        .orElse(null);

        if (transaction == null) {

            return false;
        }

        // ---------------------------------------------
        // ĐÃ THANH TOÁN
        // ---------------------------------------------

        if (transaction.getStatus()
                == TopUpStatus.PAID) {

            return true;
        }

        // ---------------------------------------------
        // KIỂM TRA ĐÚNG TÀI KHOẢN NHẬN
        // ---------------------------------------------

        if (!normalize(receivedAccountNumber)
                .equals(
                    normalize(accountNumber)
                )) {

            return false;
        }

        // ---------------------------------------------
        // KIỂM TRA ĐÚNG SỐ TIỀN
        // ---------------------------------------------

        if (transferAmount
                != transaction.getAmount()) {

            return false;
        }

        // ---------------------------------------------
        // KHÓA WALLET
        // ---------------------------------------------

        Wallet wallet =
                walletRepository
                        .findByUserIdForUpdate(
                                transaction
                                    .getUser()
                                    .getId()
                        )
                        .orElseGet(() -> {

                            Wallet newWallet =
                                    new Wallet(
                                        transaction.getUser()
                                    );

                            newWallet.setBalance(0L);

                            return walletRepository.save(
                                    newWallet
                            );
                        });

        // ---------------------------------------------
        // CỘNG TIỀN
        // ---------------------------------------------

        long newBalance =
                wallet.getBalance()
                + transaction.getAmount();

        wallet.setBalance(newBalance);

        walletRepository.save(wallet);

        // ---------------------------------------------
        // CẬP NHẬT GIAO DỊCH
        // ---------------------------------------------

        transaction.setStatus(
                TopUpStatus.PAID
        );

        transaction.setGatewayTransactionId(
                gatewayTransactionId
        );

        transaction.setPaidAt(
                LocalDateTime.now()
        );

        topUpTransactionRepository.save(
                transaction
        );

        return true;
    }

    // =====================================================
    // VALIDATE SỐ TIỀN
    // =====================================================

    private void validateAmount(long amount) {

        if (amount < 10_000) {

            throw new IllegalArgumentException(
                    "Số tiền nạp tối thiểu là 10.000đ."
            );
        }

        if (amount > 10_000_000) {

            throw new IllegalArgumentException(
                    "Số tiền nạp tối đa là 10.000.000đ."
            );
        }

        if (amount % 1_000 != 0) {

            throw new IllegalArgumentException(
                    "Số tiền phải là bội số của 1.000đ."
            );
        }
    }

    // =====================================================
    // TẠO MÃ THANH TOÁN
    // =====================================================

    private String generatePaymentCode() {

        while (true) {

            String code =
                    "TOPUP_"
                    + UUID.randomUUID()
                            .toString()
                            .replace("-", "")
                            .substring(0, 8)
                            .toUpperCase(
                                    Locale.ROOT
                            );

            if (!topUpTransactionRepository
                    .findByPaymentCodeAndUser(
                            code,
                            null
                    )
                    .isPresent()) {

                return code;
            }

            /*
             * Trường hợp cực kỳ hiếm xảy ra khi mã trùng.
             * Vòng while sẽ tạo mã mới.
             */
        }
    }

    // =====================================================
    // CHUẨN HÓA
    // =====================================================

    private String normalize(String value) {

        if (value == null) {
            return "";
        }

        return value
                .trim()
                .replaceAll("\\s+", "")
                .toUpperCase(
                        Locale.ROOT
                );
    }
}
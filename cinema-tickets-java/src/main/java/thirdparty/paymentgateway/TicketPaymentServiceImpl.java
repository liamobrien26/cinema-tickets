package thirdparty.paymentgateway;

public class TicketPaymentServiceImpl implements TicketPaymentService {

    @Override
    public void makePayment(long accountId, int totalAmountToPay) {
        // Real implementation omitted, assume working code will take the payment using a card pre linked to the account.
        System.out.println("Payment of £" + totalAmountToPay + " made successfully for account ID: " + accountId);
    }

}

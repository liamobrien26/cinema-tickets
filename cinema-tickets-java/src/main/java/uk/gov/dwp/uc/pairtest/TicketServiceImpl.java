package uk.gov.dwp.uc.pairtest;

import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.seatbooking.SeatReservationService;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;


public class TicketServiceImpl implements TicketService {
    private static final int MAXIMUM_TICKETS_ALLOWED_TO_PURCHASE = 25;
    private static final int CHILD_PRICE = 15;
    private static final int ADULT_PRICE = 25;

    private final TicketPaymentService ticketPaymentService;
    private final SeatReservationService seatReservationService;

    public TicketServiceImpl(TicketPaymentService ticketPaymentService, SeatReservationService seatReservationService) {
        this.ticketPaymentService = ticketPaymentService;
        this.seatReservationService = seatReservationService;
    }

    @Override
    public void purchaseTickets(Long accountId, TicketTypeRequest... ticketTypeRequests) {
        if (accountId == null || accountId <= 0 || ticketTypeRequests == null || ticketTypeRequests.length == 0) {
            throw new InvalidPurchaseException("Invalid account or ticket request");
        }

        int totalTickets = 0;
        int totalAmount = 0;
        int adultTickets = 0;
        int childTickets = 0;
        int infantTickets = 0;

        for (TicketTypeRequest request : ticketTypeRequests) {
            if (request == null || request.getNoOfTickets() <= 0) {
                throw new InvalidPurchaseException("Invalid ticket request");
            }

            switch (request.getTicketType()) {
                case ADULT:
                    adultTickets += request.getNoOfTickets();
                    totalAmount += request.getNoOfTickets() * ADULT_PRICE;
                    break;

                case CHILD:
                    childTickets += request.getNoOfTickets();
                    totalAmount += request.getNoOfTickets() * CHILD_PRICE;
                    break;

                case INFANT:
                    infantTickets += request.getNoOfTickets();
                    break;

                default:
                    throw new InvalidPurchaseException("Unknown ticket type");
            }
            totalTickets += request.getNoOfTickets();
        }

        if (totalTickets > MAXIMUM_TICKETS_ALLOWED_TO_PURCHASE) {
            throw new InvalidPurchaseException("Cannot purchase more than 25 tickets at a time");
        }

        if (childTickets > 0 || infantTickets > 0) {
            if (adultTickets == 0) {
                throw new InvalidPurchaseException("Child or Infant tickets cannot be purchased without an Adult ticket");
            }
        }

        ticketPaymentService.makePayment(accountId, totalAmount);
        seatReservationService.reserveSeat(accountId, adultTickets + childTickets);
    }
}

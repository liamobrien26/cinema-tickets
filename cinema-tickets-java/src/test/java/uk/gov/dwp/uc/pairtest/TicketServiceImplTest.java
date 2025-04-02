package uk.gov.dwp.uc.pairtest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.seatbooking.SeatReservationService;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TicketServiceImplTest {

    private TicketPaymentService ticketPaymentService;
    private SeatReservationService seatReservationService;
    private TicketServiceImpl ticketService;

    @BeforeEach
    void setUp() {
        ticketPaymentService = mock(TicketPaymentService.class);
        seatReservationService = mock(SeatReservationService.class);
        ticketService = new TicketServiceImpl(ticketPaymentService, seatReservationService);
    }

    @Test
    void purchaseTickets_ShouldThrowException_WhenAccountIdIsNull() {
        assertThrows(InvalidPurchaseException.class, () ->
                ticketService.purchaseTickets(null, new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 1)));
    }

    @Test
    void purchaseTickets_ShouldThrowException_WhenAccountIdIsZero() {
        assertThrows(InvalidPurchaseException.class, () ->
                ticketService.purchaseTickets(0L, new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 1)));
    }

    @Test
    void purchaseTickets_ShouldThrowException_WhenNoTicketsProvided() {
        assertThrows(InvalidPurchaseException.class, () ->
                ticketService.purchaseTickets(1L));
    }

    @Test
    void purchaseTickets_ShouldThrowException_WhenTotalTicketsExceedLimit() {
        assertThrows(InvalidPurchaseException.class, () ->
                ticketService.purchaseTickets(1L, new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 26)));
    }

    @Test
    void purchaseTickets_ShouldThrowException_WhenChildTicketsPurchasedWithoutAdult() {
        assertThrows(InvalidPurchaseException.class, () ->
                ticketService.purchaseTickets(1L, new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 1)));
    }

    @Test
    void purchaseTickets_ShouldThrowException_WhenInfantTicketsPurchasedWithoutAdult() {
        assertThrows(InvalidPurchaseException.class, () ->
                ticketService.purchaseTickets(1L, new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 1)));
    }

    @Test
    void purchaseTickets_ShouldProcessPaymentAndReserveSeats_WhenValidRequest() {
        ticketService.purchaseTickets(1L,
                new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 2),
                new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 1),
                new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 1));

        verify(ticketPaymentService).makePayment(1L, 65);
        verify(seatReservationService).reserveSeat(1L, 3);
    }
}

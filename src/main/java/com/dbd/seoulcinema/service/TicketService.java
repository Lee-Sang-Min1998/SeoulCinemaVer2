package com.dbd.seoulcinema.service;

import com.dbd.seoulcinema.domain.PaymentDiscountId;
import com.dbd.seoulcinema.domain.entity.*;
import com.dbd.seoulcinema.domain.enumeration.DiscountType;
import com.dbd.seoulcinema.domain.enumeration.PaymentType;
import com.dbd.seoulcinema.dao.ViewSpecificTicketDao;
import com.dbd.seoulcinema.dto.ViewSpecificTicketDto;
import com.dbd.seoulcinema.dto.ViewTicketsListDto;
import com.dbd.seoulcinema.repository.*;
import com.dbd.seoulcinema.repository.PaymentDiscountRepository;
import com.dbd.seoulcinema.vo.CreateTicketFinalVo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TicketService {

    private final TicketRepository ticketRepository;

    private final MemberRepository memberRepository;

    private final ScheduleRepository scheduleRepository;

    private final ScheduleSeatRepository scheduleSeatRepository;

    private final PaymentRepository paymentRepository;

    private final MemberService memberService;

    private final NonmemberRepository nonmemberRepository;

    private final SeatRepository seatRepository;

    private final PaymentDiscountRepository paymentDiscountRepository;

    private final DiscountRepository discountRepository;

    @Transactional
    public List<ViewTicketsListDto> viewTicketList(String clientId) {

        if (memberService.isMember(clientId)) {
            return ticketRepository.findTicketListByMember(clientId);
        } else {
            return ticketRepository.findTicketListByNonMember(clientId);
        }

    }

    @Transactional
    public ViewSpecificTicketDto viewSpecificTicket(String ticketNumber) {

        ViewSpecificTicketDao ticketInfoById = ticketRepository.findSpecificTicketInfoById(ticketNumber);
        List<Seat> seats = seatRepository.findAllSeatsByTicketNumber(ticketNumber);
        //티켓에 좌석 번호 출력을 위한 메소드(ex) A10/ A11)
        String seatInfo = Seat.getSeatLocation(seats);

        return new ViewSpecificTicketDto(ticketInfoById,seatInfo);

    }

    public Integer calculateTotalPrice(Integer seatCount, DiscountType discountType, Integer point) {

        return 12000 * seatCount - discountType.getAmount() - point;
    }

    @Transactional
    public String makeTicketsAndPayment(CreateTicketFinalVo vo, String clientId, PaymentType paymentType) {

        Ticket ticket = null;
        Boolean isMember = memberService.isMember(clientId);

        //고객이 회원인지 비회원인지 판단하는 로직 for 10프로 포인트 적립
        if (isMember) {
            memberRepository.findById(clientId).get().accumulateAndUsePoint(vo.getPoint(),vo.getTotalPrice());
        }

        //티켓 엔티티에 영화 이름을 삽입하기 위함
        String movieName = scheduleRepository.findMovieNameByScheduleNumber(vo.getScheduleNumber());
        //상영 일정 좌석의 티켓 외래키에 티켓 번호를 삽입하기 위해 조회
        List<ScheduleSeat> findScheduleSeats = scheduleSeatRepository.
                findAllByScheduleNumberAndSeats(vo.getScheduleNumber(), vo.getSeats());

        //티켓 엔티티 생성
        if (isMember) {
            Member member = memberRepository.findById(clientId).get();
            ticket = Ticket.makeMemberTicket(vo, member, movieName, findScheduleSeats);
            ticketRepository.save(ticket);
        } else {
            NonMember nonMember = nonmemberRepository.findById(clientId).get();
            ticket = Ticket.makeNonMemberTicket(vo, nonMember, movieName, findScheduleSeats);
            ticketRepository.save(ticket);
        }

        ticketRepository.flush();

        //상영 일정 좌석 외래키에 생성된 티켓 번호를 삽입
        for (ScheduleSeat scheduleSeat : ticket.getScheduleSeats()) {
            scheduleSeat.setTicketWhenPayment(ticket);
        }

        //결제 엔티티 생성
        Payment payment = Payment.makePayment(vo, ticket, paymentType);
        paymentRepository.save(payment);
        paymentRepository.flush();

        System.out.println("break point~~~~~~~~~~~~~~~~~~~");
        //결제 할인 엔티티 생성
        if(vo.getPoint()!=0){

            Discount findDiscount = discountRepository.findDiscountByDiscountType();

            System.out.println("break point2~~~~~~~~~~~~~~~~~~~");
            System.out.println(findDiscount.getDiscountType().getDesc());
            System.out.println(payment.getPaymentNumber());
            System.out.println(findDiscount.getDiscountNumber());

            PaymentDiscount paymentDiscount = PaymentDiscount.builder()
                .paymentNumber(payment)
                .discountNumber(findDiscount)
                .build();

            System.out.println(paymentDiscount.getPaymentNumber().getPaymentNumber());
            System.out.println(paymentDiscount.getDiscountNumber().getDiscountNumber());
            paymentDiscountRepository.save(paymentDiscount);
        }
        return "success";
    }
}

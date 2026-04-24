package com.eni.bookhub.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.eni.bookhub.entity.Reservation;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Integer> {

    List<Reservation> findByUserId(Integer userId);

    List<Reservation> findByBookIdAndStatusOrderByReservationDateAsc(Integer bookId, Reservation.Status status);

    boolean existsByUserIdAndBookIdAndStatusIn(Integer userId, Integer bookId, List<Reservation.Status> statuses);

    int countByBookIdAndStatus(Integer bookId, Reservation.Status status);

    int countByUserIdAndStatusIn(Integer userId, List<Reservation.Status> statuses);

    List<Reservation> findByBookIdAndStatusAndRankWaitingListGreaterThan(
            Integer bookId, Reservation.Status status, Integer rank);
}

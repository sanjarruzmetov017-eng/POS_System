package com.smartpos.service;

import com.smartpos.model.CashSession;
import com.smartpos.model.User;
import com.smartpos.repository.CashSessionRepository;
import com.smartpos.repository.SaleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class CashRegisterService {

    @Autowired
    private CashSessionRepository cashSessionRepository;

    @Autowired
    private SaleRepository saleRepository;

    public CashSession openSession(User user, BigDecimal startCash) {
        Optional<CashSession> existing = cashSessionRepository.findFirstByOpenTrue();
        if (existing.isPresent()) {
            throw new IllegalStateException("A session is already open.");
        }

        CashSession session = new CashSession();
        session.setUser(user);
        session.setStartCash(startCash);
        session.setOpen(true);
        return cashSessionRepository.save(session);
    }

    public CashSession closeSession(BigDecimal actualEndCash, String notes) {
        CashSession session = cashSessionRepository.findFirstByOpenTrue()
                .orElseThrow(() -> new IllegalStateException("No open session found."));

        // Calculate expected cash (Start Cash + Total Sales in this session)
        // Simplified: startCash + total of sales since startTime
        BigDecimal totalSales = saleRepository.findAll().stream()
                .filter(s -> s.getDate().isAfter(session.getStartTime()))
                .map(s -> s.getPaidAmount()) // Only cash/paid amount counts for physical register
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal expected = session.getStartCash().add(totalSales);

        session.setEndTime(LocalDateTime.now());
        session.setEndCash(actualEndCash);
        session.setExpectedEndCash(expected);
        session.setDiscrepancy(actualEndCash.subtract(expected));
        session.setOpen(false);
        session.setClosingNotes(notes);

        return cashSessionRepository.save(session);
    }

    public Optional<CashSession> getActiveSession() {
        return cashSessionRepository.findFirstByOpenTrue();
    }
}

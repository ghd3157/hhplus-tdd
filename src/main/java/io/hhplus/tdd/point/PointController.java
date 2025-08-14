package io.hhplus.tdd.point;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/point")
public class PointController {

    private static final Logger log = LoggerFactory.getLogger(PointController.class);

    @Autowired
    private UserPointTable userPointTable;

    @Autowired
    private PointHistoryTable pointHistoryTable;


    /**
     * TODO - 특정 유저의 포인트를 조회하는 기능을 작성해주세요.
     */
    @GetMapping("{id}")
    public UserPoint point(
            @PathVariable long id
    ) {
        return userPointTable.selectById(id);
    }

    /**
     * TODO - 특정 유저의 포인트 충전/이용 내역을 조회하는 기능을 작성해주세요.
     */
    @GetMapping("{id}/histories")
    public List<PointHistory> history(
            @PathVariable long id
    ) {
        return pointHistoryTable.selectAllByUserId(id);
    }

    /**
     * TODO - 특정 유저의 포인트를 충전하는 기능을 작성해주세요.
     */
    @PatchMapping("{id}/charge")
    public UserPoint charge(
            @PathVariable long id,
            @RequestBody long amount
    ) {
        // amount 값 검증
        if (amount <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "충전 금액은 0보다 커야 합니다.");
        }
        // 사용자의 현재 보유 포인트 확인
        UserPoint current = userPointTable.selectById(id);
        // 보유 포인트 + 충전 포인트
        long updated = current.point() + amount;

        // 사용자 보유 포인트 업데이트
        UserPoint saved = userPointTable.insertOrUpdate(id, updated);
        // 포인트 히스토리 업데이트
        pointHistoryTable.insert(id, amount, TransactionType.CHARGE, System.currentTimeMillis());

        return saved;

    }

    /**
     * TODO - 특정 유저의 포인트를 사용하는 기능을 작성해주세요.
     */
    @PatchMapping("{id}/use")
    public UserPoint use(
            @PathVariable long id,
            @RequestBody long amount
    ) {
        // amount 값 검증
        if (amount <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "사용 금액은 0보다 커야 합니다.");
        }
        // 사용자의 현재 보유 포인트 확인
        UserPoint current = userPointTable.selectById(id);
        // 보유 포인트 보다 사용하려는 포인트가 크다면 오류 리턴
        if (current.point() < amount) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "잔액이 부족합니다.");
        }
        // 보유 포인트 - 사용 포인트
        long updated = current.point() - amount;

        // 사용자 보유 포인트 업데이트
        UserPoint saved = userPointTable.insertOrUpdate(id, updated);
        // 포인트 히스토리 업데이트
        pointHistoryTable.insert(id, amount, TransactionType.USE, System.currentTimeMillis());

        return saved;

    }
}

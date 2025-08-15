package io.hhplus.tdd.point;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PointControllerTest {

    // 테스트 실행 시간에 관계없이 항상 동일한 결과를 보장하기 위해 시간을 상수로 고정
    private final long FIXED_TIME = 1704067200000L; // 2024-01-01 00:00:00 KST

    /**
     * 컨트롤러가 UserPointTable을 올바르게 호출하고 그 결과를 반환하는지 검증
     */
    @Test
    @DisplayName("사용자 포인트 조회 테스트")
    void testPoint() {

        // Given
        UserPointTable testUserPointTable = mock(UserPointTable.class);

        PointHistoryTable testPointHistoryTable = mock(PointHistoryTable.class);
        PointController pointController = new PointController(testUserPointTable, testPointHistoryTable);

        // 테스트에 사용할 사용자 ID와 포인트
        long userId = 1L;
        long expectedPoint = 500L;
        UserPoint expectedUserPoint = new UserPoint(userId, expectedPoint, FIXED_TIME);

        // testUserPointTable의 selectById를 userId(1L) 값으로 호출하면
        // 무조건 위에서 만든 expectedUserPoint 객체를 돌려주는 설정
        when(testUserPointTable.selectById(userId)).thenReturn(expectedUserPoint);

        // When
        // point() 메서드 호출
        UserPoint actualUserPoint = pointController.point(userId);

        // Then
        // 실행 결과가 기대값과 같은지 확인
        // 두 값이 다르면 테스트 실패
        assertEquals(expectedPoint, actualUserPoint.point());
    }

    /**
     * 충전 로직이 컨트롤러 내에서 올바르게 동작하는지 검증
     * 1. UserPointTable에서 현재 포인트를 잘 가져오는지
     * 2. PointHistoryTable에 충전 내역을 잘 저장하는지
     * 3. UserPointTable에 충전된 최종 금액을 잘 업데이트하는지
     */
    @Test
    @DisplayName("사용자 포인트 충전 테스트")
    void testCharge() throws Exception {

        // Given
        UserPointTable testUserPointTable = mock(UserPointTable.class);
        PointHistoryTable testPointHistoryTable = mock(PointHistoryTable.class);
        PointController pointController = new PointController(testUserPointTable, testPointHistoryTable);

        // 테스트에 사용할 사용자 ID와 포인트
        long userId = 1L;
        long currentPoint = 1000L; // 1000 포인트 보유
        long chargeAmount = 500L; // 500 포인트 충전 예정
        long expectedPointAfterCharge = 1500L; // 충전 후 1500 포인트

        // 충전 전과 후의 사용자 포인트 정보를 생성
        UserPoint userBeforeCharge = new UserPoint(userId, currentPoint, FIXED_TIME);
        UserPoint userAfterCharge = new UserPoint(userId, expectedPointAfterCharge, FIXED_TIME);


        // 사용자의 현재 포인트 호출 시 1000 포인트
        when(testUserPointTable.selectById(userId)).thenReturn(userBeforeCharge);
        // 최종 금액 1500 포인트로 업데이트를 요청
        when(testUserPointTable.insertOrUpdate(userId, expectedPointAfterCharge)).thenReturn(userAfterCharge);


        // When
        UserPoint actualUserPoint = pointController.charge(userId, chargeAmount);


        // Then
        // 실행 결과가 기대값과 같은지 확인
        // 두 값이 다르면 테스트 실패
        assertEquals(expectedPointAfterCharge, actualUserPoint.point());
    }

    /**
     * 포인트 사용 로직이 컨트롤러 내에서 올바르게 동작하는지 검증
     */
    @Test
    @DisplayName("사용자 포인트 사용 테스트")
    void testPointUse() throws Exception {

        // Given
        UserPointTable testUserPointTable = mock(UserPointTable.class);
        PointHistoryTable testPointHistoryTable = mock(PointHistoryTable.class);
        PointController pointController = new PointController(testUserPointTable, testPointHistoryTable);

        // 테스트에 사용할 사용자 ID와 포인트
        long userId = 1L;
        long currentPoint = 1000L; // 1000 포인트 보유
        long useAmount = 300L;    // 300 포인트 사용 예정
        long expectedPointAfterUse = 700L; // 사용 후 포인트가 남아야 함

        UserPoint userBeforeUse = new UserPoint(userId, currentPoint, FIXED_TIME);
        UserPoint userAfterUse = new UserPoint(userId, expectedPointAfterUse, FIXED_TIME);

        // 사용자의 현재 포인트 호출 시 1000 포인트
        when(testUserPointTable.selectById(userId)).thenReturn(userBeforeUse);
        // 최종 금액 700 포인트로 업데이트를 요청
        when(testUserPointTable.insertOrUpdate(userId, expectedPointAfterUse)).thenReturn(userAfterUse);


        // When
        UserPoint actualUserPoint = pointController.use(userId, useAmount);

        // Then
        // 최종 반환 사용자의 포인트가 700 포인트와 일치하는지 확인합니다.
        assertEquals(expectedPointAfterUse, actualUserPoint.point());
    }
}

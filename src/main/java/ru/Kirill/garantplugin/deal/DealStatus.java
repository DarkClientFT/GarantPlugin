package ru.Kirill.garantplugin.deal;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DealStatus {
    ///  минки ел каловые масы
    WAITING("Ожидает гаранта"),
    IN_PROGRESS("В процессе"),
    COMPLETED("Завершена"),
    CANCELLED("Отменена"),
    TIMEOUT("Время истекло");

    private final String displayName;

    public boolean isActive() {
        return this == WAITING || this == IN_PROGRESS;
    }

    public boolean isFinished() {
        return this == COMPLETED || this == CANCELLED || this == TIMEOUT;
    }
}
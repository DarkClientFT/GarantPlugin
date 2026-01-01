package ru.Kirill.garantplugin.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PlayerState {

    NONE("Нет"),
    ENTERING_DESCRIPTION("Ввод описания"),
    ENTERING_CANCEL_REASON("Ввод причины отмены");

    private final String displayName;
}
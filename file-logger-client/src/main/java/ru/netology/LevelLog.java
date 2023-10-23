package ru.netology;

public enum LevelLog {
    //Нарушать порядок следоваия элементов нельзя, иначе перестанет работать фильтрация типа логирования
    SERVER,
    DEBUG,
    ERROR,
    WARN,
    INFO
}

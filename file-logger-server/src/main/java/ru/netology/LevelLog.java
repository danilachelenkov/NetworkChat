package ru.netology;

//Нарушать порядок следоваия элементов нельзя,
//иначе перестанет работать фильтрация типа логирования
public enum LevelLog {
    SERVER,
    ERROR,
    WARN,
    DEBUG,
    INFO
}
